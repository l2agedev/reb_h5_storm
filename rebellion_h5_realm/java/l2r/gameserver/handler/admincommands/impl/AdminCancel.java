package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class AdminCancel implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_cancel
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_cancel:
				handleCancel(activeChar, wordList.length > 1 ? wordList[1] : null);
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleCancel(Player activeChar, String targetName)
	{
		GameObject obj = activeChar.getTarget();
		if(targetName != null)
		{
			Player plyr = World.getPlayer(targetName);
			if(plyr != null)
				obj = plyr;
			else
				try
			{
					int radius = Math.max(Integer.parseInt(targetName), 100);
					for(Creature character : activeChar.getAroundCharacters(radius, 200))
						character.getEffectList().stopAllEffects();
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincancel.message1", activeChar, radius));
					return;
			}
			catch(NumberFormatException e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincancel.message2", activeChar));
				return;
			}
		}

		if(obj == null)
			obj = activeChar;
		if(obj.isCreature())
			((Creature) obj).getEffectList().stopAllEffects();
		else
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}
}