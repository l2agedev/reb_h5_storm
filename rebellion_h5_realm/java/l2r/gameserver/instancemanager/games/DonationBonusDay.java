package l2r.gameserver.instancemanager.games;

import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.utils.Util;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DonationBonusDay
{
	private static final Logger _log = LoggerFactory.getLogger(DonationBonusDay.class);
	private static DonationBonusDay _instance = new DonationBonusDay();
	
	private Future<?> _taskannounce = null;
	private Future<?> _taskstoppromotion = null;
	
	private long _timeOfDonationPromotion;

	public static DonationBonusDay getInstance()
	{
		return _instance;
	}

	private DonationBonusDay()
	{
	}

	public void startPromotion(int hours)
	{
		_timeOfDonationPromotion = System.currentTimeMillis() + hours * 60 * 60000; // time of promotion that will be active
		ServerVariables.set("DonationBonusActive", true);
		ServerVariables.set("DonationBonusTime", _timeOfDonationPromotion);
		startPromotionDayTask(5); // 5 sec first announce...
		_log.info("Donation Bonus Day: Activated for " + hours + " hours");
	}
	
	public void stopPromotion()
	{
		ServerVariables.set("DonationBonusActive", false);
		ServerVariables.unset("DonationBonusTime");
		stopPromotionDayTask();
		Announcements.getInstance().announceToAll("Bonus Day: Promotion for donations has ended.");
		_log.info("Donation Bonus Day: Deactivated");
	}
	
	public void continuePormotion()
	{
		_timeOfDonationPromotion = ServerVariables.getLong("DonationBonusTime"); // when it should end...
		startPromotionDayTask(360); // lets put some delay.... 360 sec = 5 minutes.
		_log.info("Donation Bonus Day: Re-Activated...");
	}
	
	public void clearPromotionDay()
	{
		ServerVariables.set("DonationBonusActive", false);
		ServerVariables.unset("DonationBonusTime");
		_log.info("Donation Bonus Day: Cleared ... time was finished.");
	}
	
	public String getTimeLeft()
	{
		int timeRemaning = -1;
		if (ServerVariables.getBool("DonationBonusActive", true))
			timeRemaning = (int) ((ServerVariables.getLong("DonationBonusTime") - System.currentTimeMillis()) / 1000);
		
		return Util.formatTime(timeRemaning);
	}
	
	private void startPromotionDayTask(int delayedAnnounceSeconds)
	{
		_taskannounce = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			public void run()
			{
				Announcements.getInstance().announceToAll("Bonus Day: All donations made next " + getTimeLeft()  +  " will recive " + ServerVariables.getInt("DonationBonusPercent") + "% extra coins bonus!");
			}
		}, delayedAnnounceSeconds * 1000, 60 * 60000); // spam every 1 hour.
		
		_taskstoppromotion = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			public void run()
			{
				if (System.currentTimeMillis() >= _timeOfDonationPromotion)
				{
					stopPromotion();
					_log.info("Donation Bonus Day: Time of promotion has finished, task are stoped.");
				}
			}
		}, 5000, 5000); // check every 5 sec if time to end promotion has come.
	}
	
	private void stopPromotionDayTask()
	{
		// task for check.
		if (_taskstoppromotion != null)
			_taskstoppromotion.cancel(true);
		
		_taskstoppromotion = null;
		
		// announce task
		if (_taskannounce != null)
			_taskannounce.cancel(true);
		
		_taskannounce = null;
	}
}
