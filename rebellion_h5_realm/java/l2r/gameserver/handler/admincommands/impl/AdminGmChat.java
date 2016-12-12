package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.tables.AdminTable;

public class AdminGmChat implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_gmchat,
		admin_snoop,
		admin_unsnoop
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_gmchat:
				try
				{
					String text = fullString.replaceFirst(Commands.admin_gmchat.name(), "");
					Say2 cs = new Say2(0, ChatType.ALLIANCE, activeChar.getName(), text);
					AdminTable.broadcastToGMs(cs);
				}
				catch(StringIndexOutOfBoundsException e)
				{}
				break;
			case admin_snoop:
			{
				GameObject target = activeChar.getTarget();
				if(target == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message1", activeChar));
					return false;
				}
				if(!target.isPlayer())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message2", activeChar));
					return false;
				}
				
				Player player = (Player) target;
				
				if (player.getAccessLevel().getLevel() > activeChar.getAccessLevel().getLevel())
				{
					activeChar.sendMessage("You cannot snoop player with higher access than you...");
					return false;
				}
				
				player.addSnooper(activeChar);
				activeChar.addSnooped(player);
				break;
			}
			case admin_unsnoop:
			{
				GameObject target = activeChar.getTarget();
				if(target == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message3", activeChar));
					return false;
				}
				if(!target.isPlayer())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message4", activeChar));
					return false;
				}
				
				Player player = (Player) target;
				activeChar.removeSnooped(player);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message5", activeChar, target.getName()));
				break;
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