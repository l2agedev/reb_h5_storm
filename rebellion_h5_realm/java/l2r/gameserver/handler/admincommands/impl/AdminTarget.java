package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

@SuppressWarnings("unused")
public class AdminTarget implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_target
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		try
		{
			String targetName = wordList[1];
			GameObject obj = World.getPlayer(targetName);
			if(obj != null && obj.isPlayer())
				obj.onAction(activeChar, false);
			else
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admintarget.message1", activeChar, targetName));
		}
		catch(IndexOutOfBoundsException e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admintarget.message2", activeChar));
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}