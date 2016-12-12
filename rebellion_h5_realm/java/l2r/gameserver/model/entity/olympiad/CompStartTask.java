package l2r.gameserver.model.entity.olympiad;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CompStartTask extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(CompStartTask.class);

	@Override
	public void runImpl() throws Exception
	{
		if(Olympiad.isOlympiadEnd())
			return;

		Olympiad._manager = new OlympiadManager();
		Olympiad._inCompPeriod = true;

		new Thread(Olympiad._manager).start();

		ThreadPoolManager.getInstance().schedule(new CompEndTask(), Olympiad.getMillisToCompEnd());

		Announcements.getInstance().announceToAll(new SystemMessage2(SystemMsg.SHARPEN_YOUR_SWORDS_TIGHTEN_THE_STITCHING_IN_YOUR_ARMOR_AND_MAKE_HASTE_TO_A_GRAND_OLYMPIAD_MANAGER__BATTLES_IN_THE_GRAND_OLYMPIAD_GAMES_ARE_NOW_TAKING_PLACE));
		_log.info("Olympiad System: Olympiad Game Started");
		
		// Safe check to not have double start of olympiad.
		Olympiad._scheduledComeptitionStartTask = null;
	}
}