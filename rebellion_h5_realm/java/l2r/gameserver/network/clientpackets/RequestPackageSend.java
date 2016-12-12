package l2r.gameserver.network.clientpackets;

import l2r.commons.math.SafeMath;
import l2r.gameserver.Config;
import l2r.gameserver.auction.AuctionManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcFreight;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.items.Warehouse;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.Log;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author VISTALL
 * @date 20:42/16.05.2011
 */
public class RequestPackageSend extends L2GameClientPacket
{
	private static final long _FREIGHT_FEE = 1000; //TODO [VISTALL] hardcode price

	private int _objectId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}

		_items = new int[_count];
		_itemQ = new long[_count];

		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD();
			_itemQ[i] = readQ();
			if(_itemQ[i] < 1 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				return;
			}
		}
	}

	@Override
	protected void runImpl() throws Exception
	{
		if (Config.ENABLE_CUSTOM_AUCTION)
		{
			Player player = getClient().getActiveChar();
			PcInventory inventory = player.getInventory();
			
			if (player == null || _count == 0 || _items == null)
				return;
			
			if (player.isActionsDisabled())
			{
				player.sendActionFailed();
				return;
			}
			
			if (player.isInStoreMode())
			{
				player.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				return;
			}
			
			// Alt game - Karma punishment
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
			{
				player.sendMessage("You cannot do this while karma.");
				return;
			}
						
			if (player.isInTrade())
			{
				player.sendActionFailed();
				return;
			}
			
			if (_items.length > 1)
			{
				player.sendMessage(player.isLangRus() ? "Вы можете выбрать только 1 вещь за один раз." : "You can select only 1 item at a time.");
				return;
			}
			
			if(NexusEvents.isInEvent(player))
			{
				player.sendActionFailed();
				return;
			}
			
			Warehouse warehouse;
			if (player.getUsingWarehouseType() != null)
			{
				switch (player.getUsingWarehouseType())
				{
					case CASTLE:
					case CLAN:
						if(player.getClan() == null || player.getClan().getLevel() == 0)
						{
							player.sendPacket(SystemMsg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
							return;
						}
						
						warehouse = player.getClan().getWarehouse();
						break;
					case FREIGHT:
						warehouse = player.getFreight();
						break;
					default:
						warehouse = player.getWarehouse();
				}
			}
			else
				warehouse = null;
			
			try
			{
				inventory.writeLock();
				if (warehouse != null)
					warehouse.writeLock();
				
				for (int i = 0; i < _count; i++)
				{
					ItemInstance item = inventory.getItemByObjectId(_items[i]);
					long itemQ = _itemQ[i];
					if (item == null || item.isEquipped())
						return;
					
					if (item.getCount() <= 0)
						return;
					
					AuctionManager.itemSlectedForAuction(player, item.getObjectId(), itemQ);
				}
			}
			catch (ArithmeticException ae)
			{
				// TODO audit
				player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			finally
			{
				if (warehouse != null)
					warehouse.writeUnlock();
				inventory.writeUnlock();
			}
		}
		else
		{
			Player player = getClient().getActiveChar();
			if (player == null || _count == 0)
				return;
			
			if (player.isActionsDisabled())
			{
				player.sendActionFailed();
				return;
			}
			
			// Alt game - Karma punishment
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
			{
				player.sendMessage("You cannot do this while karma.");
				return;
			}
						
			if (player.isInStoreMode())
			{
				player.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				return;
			}
			
			if (player.isInTrade())
			{
				player.sendActionFailed();
				return;
			}
			
			// Проверяем наличие npc и расстояние до него
			NpcInstance whkeeper = player.getLastNpc();
			if (whkeeper == null || !player.isInRangeZ(whkeeper, Creature.INTERACTION_DISTANCE))
				return;
			
			if (!player.getAccountChars().containsKey(_objectId))
				return;
			
			PcInventory inventory = player.getInventory();
			PcFreight freight = new PcFreight(_objectId);
			freight.restore();
			
			inventory.writeLock();
			freight.writeLock();
			try
			{
				int slotsleft = 0;
				long adenaDeposit = 0;
				
				slotsleft = Config.FREIGHT_SLOTS - freight.getSize();
				
				int items = 0;
				
				// Создаем новый список передаваемых предметов, на основе полученных данных
				for (int i = 0; i < _count; i++)
				{
					ItemInstance item = inventory.getItemByObjectId(_items[i]);
					if (item == null || item.getCount() < _itemQ[i] || !item.getTemplate().isFreightable())
					{
						_items[i] = 0; // Обнуляем, вещь не будет передана
						_itemQ[i] = 0L;
						continue;
					}
					
					if (!item.isStackable() || freight.getItemByItemId(item.getItemId()) == null) // вещь требует слота
					{
						if (slotsleft <= 0) // если слоты кончились нестекуемые вещи и отсутствующие стекуемые пропускаем
						{
							_items[i] = 0; // Обнуляем, вещь не будет передана
							_itemQ[i] = 0L;
							continue;
						}
						slotsleft--; // если слот есть то его уже нет
					}
					
					if (item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
						adenaDeposit = _itemQ[i];
					
					items++;
				}
				
				// Сообщаем о том, что слоты кончились
				if (slotsleft <= 0)
					player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				
				if (items == 0)
				{
					player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
					return;
				}
				
				// Проверяем, хватит ли у нас денег на уплату налога
				long fee = SafeMath.mulAndCheck(items, _FREIGHT_FEE);
				
				if (fee + adenaDeposit > player.getAdena())
				{
					player.sendPacket(SystemMsg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
					return;
				}
				
				if (!player.reduceAdena(fee, true))
				{
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
				
				for (int i = 0; i < _count; i++)
				{
					if (_items[i] == 0)
						continue;
					ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i]);
					Log.LogItem(player, Log.FreightDeposit, item);
					freight.addItem(item);
				}
			}
			catch (ArithmeticException ae)
			{
				// TODO audit
				player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			finally
			{
				freight.writeUnlock();
				inventory.writeUnlock();
			}
			
			// Обновляем параметры персонажа
			player.sendChanges();
			player.sendPacket(SystemMsg.THE_TRANSACTION_IS_COMPLETE);
		}
	}
}
