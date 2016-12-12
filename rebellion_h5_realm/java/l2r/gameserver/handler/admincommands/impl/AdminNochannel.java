package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.utils.AdminFunctions;
import l2r.gameserver.utils.Util;

public class AdminNochannel implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_nochannel,
		admin_nc
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_nochannel:
			case admin_nc:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminnochannel.message1", activeChar));
					return false;
				}
				int timeval = 30; // if no args, then 30 min default.
				if(wordList.length > 2)
					try
				{
						timeval = Integer.parseInt(wordList[2]);
				}
				catch(Exception E)
				{
					timeval = 30;
				}

				String msg = AdminFunctions.banChat(activeChar, null, wordList[1], timeval, wordList.length > 3 ? Util.joinStrings(" ", wordList, 3) : null);
				activeChar.sendMessage(msg);
			}
		}
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}