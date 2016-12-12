package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.PetInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.items.PetInventory;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestGetItemFromPet extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestGetItemFromPet.class);

	int _objectId;
	long _amount;
	int _unknown;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readQ();
		_unknown = readD(); // = 0 for most trades
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _amount < 1)
			return;

		PetInstance pet = (PetInstance) activeChar.getPet();
		if(pet == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade() || activeChar.isProcessingRequest())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(NexusEvents.isInEvent(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}
		
		PetInventory petInventory = pet.getInventory();
		PcInventory playerInventory = activeChar.getInventory();

		ItemInstance item = petInventory.getItemByObjectId(_objectId);
		
		if(item == null)
		{
			_log.warn(activeChar.getName() + " requested item obj_id: " + _objectId + " from pet, but its not there.");
			return;
		}
		
		if(item == null || item.getCount() < _amount || item.isEquipped())
		{
			activeChar.sendActionFailed();
			return;
		}

		
		
		if(item.isHeroWeapon())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Животное не может носить оружие героев!" : "The pet can not carry weapons hero!");
			activeChar.sendActionFailed();
			return;
		}

		int slots = 0;
		long weight = item.getTemplate().getWeight() * _amount;
		if(!item.getTemplate().isStackable() || activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
			slots = 1;

		if(!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}

		if(!activeChar.getInventory().validateCapacity(slots))
		{
			activeChar.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
			return;
		}

		playerInventory.addItem(petInventory.removeItemByObjectId(_objectId, _amount));

		pet.sendChanges();
		activeChar.sendChanges();
	}
}