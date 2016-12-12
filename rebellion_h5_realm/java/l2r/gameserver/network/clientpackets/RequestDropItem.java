package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.utils.Location;

public class RequestDropItem extends L2GameClientPacket
{
	private int _objectId;
	private long _count;
	private Location _loc;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
		_loc = new Location(readD(), readD(), readD());
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!activeChar.getAccessLevel().allowTransaction())
			return;
		
		if(_count < 1 || _loc.isNull())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if (Config.SECURITY_ENABLED && Config.SECURITY_ITEM_DESTROY_ENABLED && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		if(!Config.ALLOW_DISCARDITEM)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.clientpackets.RequestDropItem.Disallowed", activeChar));
			return;
		}

		if (activeChar.isInJail())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestDropItem.message1", activeChar));
			return;
		}
		
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isSitting() || activeChar.isDropDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(activeChar.isActionBlocked(Zone.BLOCKED_ACTION_DROP_ITEM))
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DISCARD_THOSE_ITEMS_HERE);
			return;
		}
		
		if(!activeChar.isInRangeSq(_loc, 22500) || Math.abs(_loc.z - activeChar.getZ()) > 50)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DISCARD_SOMETHING_THAT_FAR_AWAY_FROM_YOU);
			return;
		}

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if(activeChar.getPet() != null && activeChar.getPet().getControlItemObjId() == item.getObjectId())
		{
			activeChar.sendPacket(SystemMsg.THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_DELETED);
			return;
		}
		
		if(!item.canBeDropped(activeChar, false))
		{
			activeChar.sendPacket(SystemMsg.THAT_ITEM_CANNOT_BE_DISCARDED);
			return;
		}

		if(NexusEvents.isInEvent(activeChar))
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestDropItem.message2", activeChar));
			return;
		}
		
		item.getTemplate().getHandler().dropItem(activeChar, item, _count, _loc);
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}