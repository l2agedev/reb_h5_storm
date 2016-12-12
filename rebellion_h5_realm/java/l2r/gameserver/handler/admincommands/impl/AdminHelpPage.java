package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class AdminHelpPage implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_showhtml
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_showhtml:
				if(wordList.length != 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminhelppage.message1", activeChar));
					return false;
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/" + wordList[1]));
				break;
		}

		return true;
	}

	public static void showHelpHtml(Player targetChar, String content)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		targetChar.sendPacket(adminReply);
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}