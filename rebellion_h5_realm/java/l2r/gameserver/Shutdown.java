package l2r.gameserver;

import l2r.commons.net.nio.impl.SelectorThread;
import l2r.commons.time.cron.SchedulingPattern;
import l2r.commons.time.cron.SchedulingPattern.InvalidPatternException;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.instancemanager.CoupleManager;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.games.FishingChampionShipManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.SevenSigns;
import l2r.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2r.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.utils.Util;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shutdown extends Thread
{
	private static final Logger _log = LoggerFactory.getLogger(Shutdown.class);

	public static final int SHUTDOWN = 0;
	public static final int RESTART = 2;
	public static final int NONE = -1;

	private static final Shutdown _instance = new Shutdown();

	public static final Shutdown getInstance()
	{
		return _instance;
	}

	private Timer counter;
	
	private int shutdownMode;
	private int shutdownCounter;

	private class ShutdownCounter extends TimerTask
	{
		@Override
		public void run()
		{
			String mode = shutdownMode == SHUTDOWN ? "Shutdown" : "Restarted";
			switch(shutdownCounter)
			{
				case 1800:
				case 900:
				case 600:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
					Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_S1_IN_S2_MINUTES", new String[] { mode, String.valueOf(shutdownCounter / 60) }, ScreenMessageAlign.BOTTOM_RIGHT, 3000, false);
					Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_S1_IN_S2_MINUTES", new String[] { mode, String.valueOf(shutdownCounter / 60) });
					break;
				case 30:
				case 20:
				case 10:
					Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_S1_IN_S2_SECONDS", new String[] { mode, String.valueOf(shutdownCounter) }, ScreenMessageAlign.TOP_CENTER, 2000, true);
					Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_S1_IN_S2_SECONDS", new String[] { mode, String.valueOf(shutdownCounter) });
					break;
				case 5:
					Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_S1_IN_S2_SECONDS", new String[] { mode, String.valueOf(shutdownCounter) }, ScreenMessageAlign.TOP_CENTER, 1000, true);
					Announcements.getInstance().announceToAll(new SystemMessage2(SystemMsg.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS__PLEASE_FIND_A_SAFE_PLACE_TO_LOG_OUT).addInteger(shutdownCounter));
					break;
				case 4:
					Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_S1_IN_S2_SECONDS", new String[] { mode, String.valueOf(shutdownCounter) }, ScreenMessageAlign.TOP_CENTER, 1000, true);
					break;
				case 3:
					Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_S1_IN_S2_SECONDS", new String[] { mode, String.valueOf(shutdownCounter) }, ScreenMessageAlign.TOP_CENTER, 1000, true);
					break;
				case 2:
					Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_S1_IN_S2_SECONDS", new String[] { mode, String.valueOf(shutdownCounter) }, ScreenMessageAlign.TOP_CENTER, 1000, true);
					break;
				case 1:
					Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_S1_IN_S2_SECONDS", new String[] { mode, String.valueOf(shutdownCounter) }, ScreenMessageAlign.TOP_CENTER, 1000, true);
					break;
				case 0:
					switch(shutdownMode)
					{
						case SHUTDOWN:
							Runtime.getRuntime().exit(SHUTDOWN);
							break;
						case RESTART:
							Runtime.getRuntime().exit(RESTART);
							break;
					}
					cancel();
					return;
			}

			shutdownCounter--;
		}
	}

	private Shutdown()
	{
		setName(getClass().getSimpleName());
		setDaemon(true);

		shutdownMode = NONE;
	}

	/**
	 * Время в секундах до отключения.
	 * 
	 * @return время в секундах до отключения сервера, -1 если отключение не запланировано
	 */
	public int getSeconds()
	{
		return shutdownMode == NONE ? -1 : shutdownCounter;
	}

	/**
	 * Режим отключения.
	 * 
	 * @return <code>SHUTDOWN</code> или <code>RESTART</code>, либо <code>NONE</code>, если отключение не запланировано.
	 */
	public int getMode()
	{
		return shutdownMode;
	}

	/**
	 * Запланировать отключение сервера через определенный промежуток времени.
	 * 
	 * @param time время в формате <code>hh:mm</code>
	 * @param shutdownMode  <code>SHUTDOWN</code> или <code>RESTART</code>
	 */
	public synchronized void schedule(String nameofgm, int seconds, int shutdownMode)
	{
		if(seconds < 0)
			return;

		if (nameofgm == null || nameofgm.isEmpty())
			nameofgm = "Missing Name";
			
		if(counter != null)
			counter.cancel();

		this.shutdownMode = shutdownMode;
		this.shutdownCounter = seconds;

		_log.info("GM: " + nameofgm + " scheduled server " + (shutdownMode == SHUTDOWN ? "shutdown" : "restart") + " in " + Util.formatTime(seconds) + ".");

		counter = new Timer("ShutdownCounter", true);
		counter.scheduleAtFixedRate(new ShutdownCounter(), 0, 1000L);
	}

	/**
	 * Запланировать отключение сервера через определенный промежуток времени.
	 * 
	 * @param time время в формате <code>hh:mm</code>
	 * @param shutdownMode  <code>SHUTDOWN</code> или <code>RESTART</code>
	 */
	public synchronized void Telnetschedule(int seconds, int shutdownMode)
	{
		if(seconds < 0)
			return;

		if(counter != null)
			counter.cancel();

		this.shutdownMode = shutdownMode;
		this.shutdownCounter = seconds;

		_log.info("Telnet scheduled server " + (shutdownMode == SHUTDOWN ? "shutdown" : "restart") + " in " + Util.formatTime(seconds) + ".");

		counter = new Timer("ShutdownCounter", true);
		counter.scheduleAtFixedRate(new ShutdownCounter(), 0, 1000L);
	}
	
	/**
	 * Запланировать отключение сервера на определенное время.
	 * 
	 * @param time время в формате cron
	 * @param shutdownMode <code>SHUTDOWN</code> или <code>RESTART</code>
	 */
	public void schedule(String time, int shutdownMode)
	{
		SchedulingPattern cronTime;
		try 
		{
			cronTime = new SchedulingPattern(time);
		} 
		catch (InvalidPatternException e) 
		{
			return;
		}

		int seconds = (int)(cronTime.next(System.currentTimeMillis()) / 1000L - System.currentTimeMillis() / 1000L);
		schedule("AutoRestart", seconds, shutdownMode);
		_log.info("AutoRestart scheduled through " + Util.formatTime(seconds));
	}

	/**
	 * Отменить запланированное отключение сервера.
	 */
	public synchronized void cancel(String nameOfGm)
	{
		if (nameOfGm == null || nameOfGm.isEmpty())
			nameOfGm = "Missing Name";
		
		Announcements.getInstance().announceToAll("Server aborts " + (shutdownMode == SHUTDOWN ? "shutdown" : "restart") + " and continues normal operation!");
		_log.warn("GM: " + nameOfGm + " issued shutdown  " + (shutdownMode == SHUTDOWN ? "shutdown" : "restart") + " has been ABORTED!");
		shutdownMode = NONE;
		if(counter != null)
			counter.cancel();
		counter = null;
	}

	public synchronized void telnetCancel()
	{
		Announcements.getInstance().announceToAll("Server aborts " + (shutdownMode == SHUTDOWN ? "shutdown" : "restart") + " and continues normal operation!");
		_log.warn("Telnet issued shutdown  " + (shutdownMode == SHUTDOWN ? "shutdown" : "restart") + " has been ABORTED!");
		shutdownMode = NONE;
		if(counter != null)
			counter.cancel();
		counter = null;
	}
	
	@Override
	public void run()
	{
		System.out.println("Shutting down LS/GS communication...");
		AuthServerCommunication.getInstance().shutdown();

		System.out.println("Shutting down scripts...");
		Scripts.getInstance().shutdown();

		System.out.println("Disconnecting players...");
		disconnectAllPlayers();

		System.out.println("Saving data...");
		saveData();

		TimeCounter tc = new TimeCounter();
		
		try
		{
			System.out.println("Shutting down thread pool...");
			ThreadPoolManager.getInstance().shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Shutting down selector...");
		if(GameServer.getInstance() != null)
			for(SelectorThread<GameClient> st : GameServer.getInstance().getSelectorThreads())
				try
				{
					st.shutdown();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

		try
		{
			System.out.println("Shutting down database communication...");
			DatabaseFactory.getInstance().shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("The server has been successfully shut down in " + (tc.getEstimatedTime() / 1000) + "seconds.");
	}

	private void saveData()
	{
		try
		{
			// Seven Signs data is now saved along with Festival data.
			if(!SevenSigns.getInstance().isSealValidationPeriod())
			{
				SevenSignsFestival.getInstance().saveFestivalData(false);
				System.out.println("SevenSignsFestival: Data saved.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			SevenSigns.getInstance().saveSevenSignsData(0, true);
			System.out.println("SevenSigns: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if(Config.ENABLE_OLYMPIAD)
			try
			{
				OlympiadDatabase.save();
				System.out.println("Olympiad: Data saved.");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		if(Config.ALLOW_WEDDING)
			try
			{
				CoupleManager.getInstance().store();
				System.out.println("CoupleManager: Data saved.");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		try
		{
			FishingChampionShipManager.getInstance().shutdown();
			System.out.println("FishingChampionShipManager: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Hero.getInstance().shutdown();
			System.out.println("Hero: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if(Config.ALLOW_CURSED_WEAPONS)
			try
			{
				CursedWeaponsManager.getInstance().saveData();
				System.out.println("CursedWeaponsManager: Data saved,");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		
		try
		{
			NexusEvents.serverShutDown();
			System.out.println("NexusEngine: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void disconnectAllPlayers()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			try
			{
				player.logout();
			}
			catch(Exception e)
			{
				System.out.println("Error while disconnecting: " + player + "!");
				e.printStackTrace();
			}
	}
	
	/**
	 * A simple class used to track down the estimated time of method executions.<br>
	 * Once this class is created, it saves the start time, and when you want to get the estimated time, use the getEstimatedTime() method.
	 */
	private static final class TimeCounter
	{
		private long _startTime;
		
		protected TimeCounter()
		{
			restartCounter();
		}
		
		protected void restartCounter()
		{
			_startTime = System.currentTimeMillis();
		}
		
		@SuppressWarnings("unused")
		protected long getEstimatedTimeAndRestartCounter()
		{
			final long toReturn = System.currentTimeMillis() - _startTime;
			restartCounter();
			return toReturn;
		}
		
		protected long getEstimatedTime()
		{
			return System.currentTimeMillis() - _startTime;
		}
	}
}