package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.TradeItem;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.PrivateStoreManageListSell;
import l2r.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.templates.item.ItemTemplate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.ArrayUtils;


/**
 * Это список вещей которые игрок хочет продать в создаваемом им приватном магазине
 *
 */
public class SetPrivateStoreSellList extends L2GameClientPacket
{
	private int _count;
	private boolean _package;
	private int[] _items; // objectId
	private long[] _itemQ; // count
	private long[] _itemP; // price

	@Override
	protected void readImpl()
	{
		_package = readD() == 1;
		_count = readD();
		// Иначе нехватит памяти при создании массива.
		if(_count * 20 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}

		_items = new int[_count];
		_itemQ = new long[_count];
		_itemP = new long[_count];

		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD();
			_itemQ[i] = readQ();
			_itemP[i] = readQ();
			if(_itemQ[i] < 1 || _itemP[i] < 0 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player seller = getClient().getActiveChar();
		if(seller == null || _count == 0)
			return;

		if(!seller.getAccessLevel().allowTransaction())
			return;
		
		if (Config.SECURITY_ENABLED && Config.SECURITY_TRADE_ENABLED && seller.getSecurity())
		{
			seller.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (seller.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			seller.sendActionFailed();
			return;
		}
		
		if(!seller.canOpenPrivateStore(_package ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL))
		{
			seller.sendActionFailed();
			return;
		}
		
		if(NexusEvents.isInEvent(seller))
		{
			seller.sendActionFailed();
			return;
		}

		TradeItem temp;
		List<TradeItem> sellList = new CopyOnWriteArrayList<TradeItem>();

		seller.getInventory().writeLock();
		try
		{
			for(int i = 0; i < _count; i++)
			{
				int objectId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];

				ItemInstance item = seller.getInventory().getItemByObjectId(objectId);

				if(item == null || item.getCount() < count || !item.canBeTraded(seller) || item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
					continue;
				
				if(seller.getPet() != null && seller.getPet().getControlItemObjId() == item.getObjectId())
				{
					seller.sendMessage(seller.isLangRus() ? "Питомец привзан и не может быть продан." : "Pet is intended and can not be sold.");
					continue;
				}

				temp = new TradeItem(item);
				temp.setCount(count);
				temp.setOwnersPrice(price);

				sellList.add(temp);
			}
		}
		catch(Exception e)
		{}
		finally
		{
			seller.getInventory().writeUnlock();
		}

		if(sellList.size() > seller.getTradeLimit())
		{
			seller.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			seller.sendPacket(new PrivateStoreManageListSell(seller, _package));
			return;
		}

		if(!sellList.isEmpty())
		{
			seller.setSellList(_package, sellList);
			seller.saveTradeList();
			seller.setPrivateStoreType(_package ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL);
			seller.broadcastPacket(new PrivateStoreMsgSell(seller));
			seller.sitDown(null);
			seller.broadcastCharInfo();
			
			if (Config.SERVICES_OFFLINE_TRADE_ALLOW)
			{
				seller.ask(new ConfirmDlg(SystemMsg.S1, 10000).addString("Do you want to go in offline store mode?"), new OnAnswerListener()
				{
					@Override
					public void sayYes()
					{
						IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("offline");
						if(vch != null && seller != null)
							vch.useVoicedCommand("offline", seller, "");
					}

					@Override
					public void sayNo()
					{
					}
				});
			}
		}

		seller.sendActionFailed();
	}
}