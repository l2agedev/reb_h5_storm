package l2r.gameserver.handler.usercommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.handler.usercommands.IUserCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Support for /time command
 */
public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 77 };

	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
	private static final SimpleDateFormat sf = new SimpleDateFormat("H:mm");
	static
	{
		df.setMinimumIntegerDigits(2);
	}

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
			return false;

		int h = GameTimeController.getInstance().getGameHour();
		int m = GameTimeController.getInstance().getGameMin();

		SystemMessage2 sm;
		if(GameTimeController.getInstance().isNowNight())
			sm = new SystemMessage2(SystemMsg.THE_CURRENT_TIME_IS_S1S2_);
		else
			sm = new SystemMessage2(SystemMsg.THE_CURRENT_TIME_IS_S1S2);
		sm.addString(df.format(h)).addString(df.format(m));

		activeChar.sendPacket(sm);

		if (Config.ALT_SHOW_SERVER_TIME)
			activeChar.sendMessage(new CustomMessage("usercommandhandlers.Time.ServerTime", activeChar, sf.format(new Date(System.currentTimeMillis()))));

		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
