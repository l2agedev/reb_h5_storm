package l2r.gameserver.handler.admincommands.impl;


import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.HellboundManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

import org.apache.commons.lang3.math.NumberUtils;

public class AdminHellbound implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_hbadd,
		admin_hbsub,
		admin_hbset,
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_hbadd:
				HellboundManager.addConfidence(Long.parseLong(wordList[1]));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminhellbound.message1", activeChar, NumberUtils.toInt(wordList[1], 1)));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminhellbound.message2", activeChar, HellboundManager.getConfidence()));
				break;
			case admin_hbsub:
				HellboundManager.reduceConfidence(Long.parseLong(wordList[1]));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminhellbound.message3", activeChar, NumberUtils.toInt(wordList[1], 1)));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminhellbound.message4", activeChar, HellboundManager.getConfidence()));
				break;
			case admin_hbset:
				HellboundManager.setConfidence(Long.parseLong(wordList[1]));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminhellbound.message5", activeChar, HellboundManager.getConfidence()));
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