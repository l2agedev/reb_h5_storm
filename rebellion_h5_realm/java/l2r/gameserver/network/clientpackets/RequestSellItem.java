package l2r.gameserver.network.clientpackets;

import l2r.commons.math.SafeMath;
import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExBuySellList;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

import org.apache.commons.lang3.ArrayUtils;

/**
 * packet type id 0x37
 * format:		cddb, b - array if (ddd)
 */
public class RequestSellItem extends L2GameClientPacket
{
	int _listId;
	private int _count;
	private int[] _items; // object id
	private long[] _itemQ; // count

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 16 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];

		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD(); // object id
			readD(); //item id
			_itemQ[i] = readQ(); // count
			if(_itemQ[i] < 1 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		for(int i = 0; i < _count; i++)
		{
			int objectId = _items[i];
			ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
			if (item == null)
				continue;
			
			if(activeChar.getPet() != null && activeChar.getPet().getControlItemObjId() == item.getObjectId())
			{
				activeChar.sendMessage(activeChar.isLangRus() ? "Pet предназначен и не может быть продан." : "Pet is intended and can not be sold.");
				return;
			}
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.canOverrideCond(PcCondOverride.ITEM_SHOP_CONDITIONS))
		{
			activeChar.sendActionFailed();
			return;
		}

		NpcInstance merchant = activeChar.getLastNpc();
		boolean isValidMerchant = merchant != null && merchant.isMerchantNpc();
		if(!Config.ALT_SELL_FROM_EVERYWHERE && !activeChar.canOverrideCond(PcCondOverride.ITEM_SHOP_CONDITIONS) && (merchant == null || !isValidMerchant || !activeChar.isInRange(merchant, Creature.INTERACTION_DISTANCE)))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.getInventory().writeLock();
		try
		{
			for(int i = 0; i < _count; i++)
			{
				int objectId = _items[i];
				long count = _itemQ[i];

				ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
				if(item == null || item.getCount() < count || !item.canBeSold(activeChar))
					continue;

				long price = SafeMath.mulAndCheck(item.getReferencePrice(), count) / Config.ALT_SELL_PRICE_DIV;
				if(Config.SELL_ALL_ITEMS_FREE)
					price = 1;
				
				if (Util.contains(Config.ITEM_COST_1_ADENA, item.getItemId()))
					price = 1;
				
				ItemInstance refund = activeChar.getInventory().removeItemByObjectId(objectId, count);
				
				Log.LogItem(activeChar, Log.RefundSell, refund);
				
				activeChar.addAdena(price);
				activeChar.getRefund().addItem(refund);
			}
		}
		catch(ArithmeticException ae)
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}

		activeChar.sendPacket(new ExBuySellList.SellRefundList(activeChar, true));
		activeChar.sendChanges();
	}
}