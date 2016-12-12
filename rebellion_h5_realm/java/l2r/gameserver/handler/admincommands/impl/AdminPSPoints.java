package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

/**
 * @author KilRoy
 * AddPoints Manipulation //addpoints count <target>
 * DelPoints Manipulation //delpoints count <target>
 */
public class AdminPSPoints implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_addpoints,
		admin_delpoints
	}
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		switch(command)
		{
			case admin_addpoints:
				try
				{
					String targetName = wordList[1];
					Player obj = World.getPlayer(targetName);
					if(obj != null && obj.isPlayer())
					{
						int add = (obj.getClient().getPointG() + Integer.parseInt(wordList[2]));
						obj.getClient().setPointG(add);
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpspoints.message1", activeChar, add));
					}
					else
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpspoints.message2", activeChar, targetName));
				}
				catch(IndexOutOfBoundsException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpspoints.message3", activeChar));
				}
				break;
			case admin_delpoints:
				try
				{
					String targetName = wordList[1];
					Player obj = World.getPlayer(targetName);
					if(obj != null && obj.isPlayer())
					{
						int reduce = (obj.getClient().getPointG() - Integer.parseInt(wordList[2]));
						obj.getClient().setPointG(reduce);
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpspoints.message4", activeChar, reduce));
					}
					else
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpspoints.message5", activeChar, targetName));
				}
				catch(IndexOutOfBoundsException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpspoints.message6", activeChar));
				}
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