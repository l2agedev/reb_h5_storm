package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

/**
 * This class handles following admin commands: - gm = turns gm mode on/off
 */
public class AdminGm implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_gm
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		// TODO зачем отключено?
		if(Boolean.TRUE)
			return false;

		switch(command)
		{
			case admin_gm:
				handleGm(activeChar);
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleGm(Player activeChar)
	{
		if(activeChar.isGM())
		{
			//activeChar.getPlayerAccess().IsGM = false;
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingm.message1", activeChar));
		}
		else
		{
			//activeChar.getPlayerAccess().IsGM = true;
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingm.message2", activeChar));
		}
	}
}