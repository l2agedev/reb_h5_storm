package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class AdminIP implements IAdminCommandHandler
{
	private enum Commands
	{
		admin_charip
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_charip:
				if(wordList.length != 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminip.message1", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminip.message2", activeChar));
					break;
				}

				Player pl = World.getPlayer(wordList[1]);

				if(pl == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminip.message3", activeChar, wordList[1]));
					break;
				}

				String ip_adr = pl.getIP();
				if(ip_adr.equalsIgnoreCase("<not connected>"))
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminip.message4", activeChar, wordList[1]));
					break;
				}

				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminip.message5", activeChar, ip_adr));
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