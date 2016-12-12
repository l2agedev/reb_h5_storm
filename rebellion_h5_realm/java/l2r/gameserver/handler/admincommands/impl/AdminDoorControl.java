package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class AdminDoorControl implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_open,
		admin_close,
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		GameObject target;

		switch(command)
		{
			case admin_open:
				if(wordList.length > 1)
					target = World.getAroundObjectById(activeChar, Integer.parseInt(wordList[1]));
				else
					target = activeChar.getTarget();

				if(target != null && target.isDoor())
					((DoorInstance) target).openMe();
				else
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);

				break;
			case admin_close:
				if(wordList.length > 1)
					target = World.getAroundObjectById(activeChar, Integer.parseInt(wordList[1]));
				else
					target = activeChar.getTarget();
				if(target != null && target.isDoor())
					((DoorInstance) target).closeMe();
				else
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}