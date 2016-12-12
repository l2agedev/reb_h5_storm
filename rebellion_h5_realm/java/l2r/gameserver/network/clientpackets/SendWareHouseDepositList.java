package l2r.gameserver.network.clientpackets;

import l2r.commons.math.SafeMath;
import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.items.Warehouse;
import l2r.gameserver.model.items.Warehouse.WarehouseType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.Log;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Format: cdb, b - array of (dd)
 */
public class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final long _WAREHOUSE_FEE = 30; //TODO [G1ta0] hardcode price

	private int _count;
	private int[] _items;
	private long[] _itemQ;

	@Override
	protected void readImpl()
	{
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

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("You cannot deposit items while having karma.");
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

		// Проверяем наличие npc и расстояние до него
		NpcInstance whkeeper = activeChar.getLastNpc();
		if(!Config.ALLOW_BBS_WAREHOUSE && (whkeeper == null || !activeChar.isInRangeZ(whkeeper, Creature.INTERACTION_DISTANCE)))
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_MOVED_TOO_FAR_AWAY_FROM_THE_WAREHOUSE_TO_PERFORM_THAT_ACTION);
			return;
		}
		
		PcInventory inventory = activeChar.getInventory();
		Warehouse warehouse;
		switch (activeChar.getUsingWarehouseType())
		{
			case CASTLE:
			case CLAN:
				if(activeChar.getClan() == null || activeChar.getClan().getLevel() == 0)
				{
					activeChar.sendPacket(SystemMsg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
					return;
				}
				
				warehouse = activeChar.getClan().getWarehouse();
				break;
			case FREIGHT:
				warehouse = activeChar.getFreight();
				break;
			default:
				warehouse = activeChar.getWarehouse();
		}

		inventory.writeLock();
		warehouse.writeLock();
		try
		{
			int slotsleft = 0;
			long adenaDeposit = 0;

			if(activeChar.getUsingWarehouseType() != WarehouseType.CLAN)
				slotsleft = activeChar.getWarehouseLimit() - warehouse.getSize();
			else
				slotsleft = activeChar.getClan().getWhBonus() + Config.WAREHOUSE_SLOTS_CLAN - warehouse.getSize();

			int items = 0;

			// Создаем новый список передаваемых предметов, на основе полученных данных
			for(int i = 0; i < _count; i++)
			{
				ItemInstance item = inventory.getItemByObjectId(_items[i]);
				if(item == null || item.getCount() < _itemQ[i] || !item.canBeStored(activeChar, activeChar.getUsingWarehouseType() != WarehouseType.CLAN || activeChar.getUsingWarehouseType() == WarehouseType.CLAN))
				{
					_items[i] = 0; // Обнуляем, вещь не будет передана
					_itemQ[i] = 0L;
					continue;
				}

				if(!item.isStackable() || warehouse.getItemByItemId(item.getItemId()) == null) // вещь требует слота
				{
					if(slotsleft <= 0) // если слоты кончились нестекуемые вещи и отсутствующие стекуемые пропускаем
					{
						_items[i] = 0; // Обнуляем, вещь не будет передана
						_itemQ[i] = 0L;
						continue;
					}
					slotsleft--; // если слот есть то его уже нет
				}

				if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
					adenaDeposit = _itemQ[i];

				items++;
			}

			// Сообщаем о том, что слоты кончились
			if(slotsleft <= 0)
				activeChar.sendPacket(SystemMsg.YOUR_WAREHOUSE_IS_FULL);

			if(items == 0)
			{
				activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}

			// Проверяем, хватит ли у нас денег на уплату налога
			long fee = SafeMath.mulAndCheck(items, _WAREHOUSE_FEE);

			if(fee + adenaDeposit > activeChar.getAdena())
			{
				activeChar.sendPacket(SystemMsg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
				return;
			}

			if(!activeChar.reduceAdena(fee, true))
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}

			for(int i = 0; i < _count; i++)
			{
				if(_items[i] == 0)
					continue;
				
				
				ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i]);
				String log = activeChar.getObjectId() + "|add|" + item.getItemId() + "|" + item.getObjectId() + "|" + item.getCount();
				if (activeChar.getUsingWarehouseType() != WarehouseType.CLAN)
					Log.addGame("PcWarehouse|" + log, "items");
				else
					Log.addGame("ClanWarehouse|" + log, "items");
				
				warehouse.addItem(item);
			}
		}
		catch(ArithmeticException ae)
		{
			// TODO audit
			activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			warehouse.writeUnlock();
			inventory.writeUnlock();
		}

		// Обновляем параметры персонажа
		activeChar.sendChanges();
		activeChar.sendPacket(SystemMsg.THE_TRANSACTION_IS_COMPLETE);
	}
}