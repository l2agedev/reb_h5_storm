package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.instancemanager.HellboundManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;

public class Hellbound extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "hellbound" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.equals("hellbound"))
		{
			if (!Config.ENABLE_HELLBOUND_VOICED)
				return false;
			
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.hellbound.message1", activeChar, HellboundManager.getHellboundLevel()));
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.hellbound.message2", activeChar, HellboundManager.getConfidence()));
		}
		return false;
	}
}
