package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Time extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "date", "time" };

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.equals("date") || command.equals("time") && Config.ALT_SHOW_SERVER_TIME)
		{
			activeChar.sendMessage(DATE_FORMAT.format(new Date(System.currentTimeMillis())));
			return true;
		}

		return false;
	}
}
