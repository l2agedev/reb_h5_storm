package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class AdminGlobalEvent implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_list_events
	}
	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands c = (Commands)comm;
		switch(c)
		{
			case admin_list_events:
				GameObject object = activeChar.getTarget();
				if(object == null)
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				else
				{
					for(GlobalEvent e : object.getEvents())
						activeChar.sendMessage("- " + e.toString());
				}
				break;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
