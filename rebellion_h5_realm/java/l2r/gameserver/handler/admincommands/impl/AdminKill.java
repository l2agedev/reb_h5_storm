package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

import org.apache.commons.lang3.math.NumberUtils;

public class AdminKill implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_kill,
		admin_killall,
		admin_damage,
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		switch(command)
		{
			case admin_kill:
				if(wordList.length == 1)
					handleKill(activeChar);
				else
					handleKill(activeChar, wordList[1], false);
				break;
			case admin_killall:
				if(wordList.length > 1)
					handleKill(activeChar, wordList[1], true);
			case admin_damage:
				handleDamage(activeChar, NumberUtils.toInt(wordList[1], 1));
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleKill(Player activeChar)
	{
		handleKill(activeChar, null, false);
	}

	private void handleKill(Player activeChar, String player, boolean everything)
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
				for(Creature character : everything ? activeChar.getAroundCharacters(radius, 200) : activeChar.getAroundNpc(radius, 200))
					if(!character.isDoor())
						character.doDie(activeChar);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminkill.message1", activeChar, radius));
				return;
			}
		}

		if(obj != null && obj.isCreature())
		{
			Creature target = (Creature) obj;
			target.doDie(activeChar);
		}
		else
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private void handleDamage(Player activeChar, int damage)
	{
		GameObject obj = activeChar.getTarget();

		if(obj == null)
		{
			activeChar.sendPacket(SystemMsg.SELECT_TARGET);
			return;
		}

		if(!obj.isCreature())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		Creature cha = (Creature) obj;
		cha.reduceCurrentHp(damage, activeChar, null, true, true, false, false, false, false, true);
		activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminkill.message2", activeChar, damage, cha.getName()));
	}
}