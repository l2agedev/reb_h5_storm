package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class AdminHeal implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_heal,
		admin_healthy
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_heal:
			case admin_healthy:
				if(wordList.length == 1)
					handleRes(activeChar);
				else
					handleRes(activeChar, wordList[1]);
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleRes(Player activeChar)
	{
		handleRes(activeChar, null);
	}

	private void handleRes(Player activeChar, String player)
	{

		GameObject obj = activeChar.getTarget();
		if(player != null)
		{
			Player plyr = World.getPlayer(player);

			if(plyr != null)
				obj = plyr;
			else
			{
				int radius = Math.max(Integer.parseInt(player), 100);
				for(Creature character : activeChar.getAroundCharacters(radius, 200))
				{
					character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
					if(character.isPlayer())
						character.setCurrentCp(character.getMaxCp());
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminheal.message1", activeChar));
				return;
			}
		}

		if(obj == null)
			obj = activeChar;

		if(obj instanceof Creature)
		{
			Creature target = (Creature) obj;
			target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
			if(target.isPlayer())
				target.setCurrentCp(target.getMaxCp());
		}
		else
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}
}