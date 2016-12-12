package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ShowCalc;
import l2r.gameserver.scripts.Functions;

public class Calculator extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[]
	{
		"calc",
		"calculator"
	};
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("calc") || command.equals("calculator"))
			activeChar.sendPacket(new ShowCalc(4393));
		return false;
	}
}
