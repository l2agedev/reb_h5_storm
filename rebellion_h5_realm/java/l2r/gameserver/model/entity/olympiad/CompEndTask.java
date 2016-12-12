package l2r.gameserver.model.entity.olympiad;


import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CompEndTask extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(CompEndTask.class);

	@Override
	public void runImpl() throws Exception
	{
		if(Olympiad.isOlympiadEnd())
			return;

		Olympiad._inCompPeriod = false;

		try
		{
			OlympiadManager manager = Olympiad._manager;

			// Если остались игры, ждем их завершения еще одну минуту
			if(manager != null && !manager.getOlympiadGames().isEmpty())
			{
				ThreadPoolManager.getInstance().schedule(new CompEndTask(), 60000);
				return;
			}

			// Every oly end cleanup the list just in case.
			Olympiad.getOlyHwidList().clear();
			Olympiad.getOlyIPList().clear();
			
			Announcements.getInstance().announceToAll(new SystemMessage2(SystemMsg.THE_OLYMPIAD_GAME_HAS_ENDED));
			_log.info("Olympiad System: Olympiad Game Ended");
			OlympiadDatabase.save();
		}
		catch(Exception e)
		{
			_log.warn("Olympiad System: Failed to save Olympiad configuration:");
			_log.error("", e);
		}
		Olympiad.init();
	}
}