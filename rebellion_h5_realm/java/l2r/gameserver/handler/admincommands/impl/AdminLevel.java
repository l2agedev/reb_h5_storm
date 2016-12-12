package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.instances.PetInstance;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.PetDataTable;

public class AdminLevel implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_add_level,
		admin_addLevel,
		admin_set_level,
		admin_setLevel,
	}

	private void setLevel(Player activeChar, GameObject target, int level)
	{
		if(target == null || !(target.isPlayer() || target.isPet()))
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}
		if(level < 1 || level > Experience.getMaxLevel() + 1) // +1 because if maxLevel is 85 and you type 86, you will set level 85 to 100% Exp.
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminlevel.message1", activeChar, Experience.getMaxLevel()));
			return;
		}
		if(target.isPlayer())
		{
			Long exp_add = Experience.LEVEL[level] - ((Player) target).getExp();
			((Player) target).addExpAndSp(exp_add, 0);
			return;
		}
		if(target.isPet())
		{
			Long exp_add = PetDataTable.getInstance().getInfo(((PetInstance) target).getNpcId(), level).getExp() - ((PetInstance) target).getExp();
			((PetInstance) target).addExpAndSp(exp_add, 0);
		}
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		GameObject target = activeChar.getTarget();
		if(target == null || !(target.isPlayer() || target.isPet()))
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		int level;

		switch(command)
		{
			case admin_add_level:
			case admin_addLevel:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminlevel.message2", activeChar));
					return false;
				}
				try
				{
					level = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminlevel.message3", activeChar));
					return false;
				}
				setLevel(activeChar, target, level + ((Creature) target).getLevel());
				break;
			case admin_set_level:
			case admin_setLevel:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminlevel.message4", activeChar));
					return false;
				}
				try
				{
					level = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminlevel.message5", activeChar));
					return false;
				}
				setLevel(activeChar, target, level);
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