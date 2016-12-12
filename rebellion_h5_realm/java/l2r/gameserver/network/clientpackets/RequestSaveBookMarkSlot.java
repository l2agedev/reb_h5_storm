package l2r.gameserver.network.clientpackets;

import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.network.serverpackets.ExGetBookMarkInfo;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

/**
 * SdS
 */
public class RequestSaveBookMarkSlot extends L2GameClientPacket
{
	private String _name, _acronym;
	private int _icon;

	@Override
	protected void readImpl()
	{
		_name = readS(32);
		_icon = readD();
		_acronym = readS(4);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (activeChar.isActionBlocked(Zone.BLOCKED_ACTION_SAVE_BOOKMARK) || activeChar.isInBoat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA);
			return;
		}
		
		if (activeChar.getActiveWeaponFlagAttachment() != null)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
			return;
		}
		
		if (activeChar.getReflection() != ReflectionManager.DEFAULT)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE);
			return;
		}
		
		if (activeChar.isInDuel())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
			return;
		}
		
		if (activeChar.isInCombat() || activeChar.getPvpFlag() != 0)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
			return;
		}
		
		if (activeChar.isOnSiegeField() || activeChar.isInZoneBattle())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_A_LARGESCALE_BATTLE_SUCH_AS_A_CASTLE_SIEGE_FORTRESS_SIEGE_OR_HIDEOUT_SIEGE);
			return;
		}
		
		if (activeChar.isFlying())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
			return;
		}
		
		if (activeChar.isInWater())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
			return;
		}
		
		if(activeChar != null && activeChar.getTeleportBookmarks().add(_name, _acronym, _icon))
			activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
	}
}