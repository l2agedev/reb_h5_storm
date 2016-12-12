package l2r.gameserver.nexus_engine.events.engine;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.events.engine.lang.LanguageEngine;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;

/**
 * @author hNoke
 * - handles EventWarnings system
 */
public class EventWarnings 
{
	private Map<Integer, Integer> _warnings;
	private ScheduledFuture<?> _decTask;
	
	@SuppressWarnings("unused")
	private SaveScheduler _saveScheduler;
	
	public static int MAX_WARNINGS = 3;
	
	public EventWarnings()
	{
		_warnings = new FastMap<Integer, Integer>();
		_decTask = null;
		_saveScheduler = new SaveScheduler();
		
		loadData();
		
		decreasePointsTask();
		
		NexusLoader.debug("Nexus Engine: Loaded EventWarnings engine.");
	}
	
	private class SaveScheduler implements Runnable
	{
		public SaveScheduler()
		{
			schedule();
		}
		
		private void schedule()
		{
			CallBack.getInstance().getOut().scheduleGeneral(this, 1800000);
		}

		@Override
		public void run()
		{
			saveData();
			schedule();
		}
	}
	
	private void decreasePointsTask()
	{
		if(_decTask != null)
			_decTask.cancel(false);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 30);
		
		long delay = cal.getTimeInMillis() - System.currentTimeMillis();
		
		_decTask = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				PlayerEventInfo pi;
				for(int id : _warnings.keySet())
				{
					decreasePoints(id, 1);
					
					pi = CallBack.getInstance().getOut().getPlayer(id);
					
					if(pi != null)
					{
						pi.sendMessage(LanguageEngine.getMsg("system_warningsDecreased", getPoints(id)));
					}
				}
				
				saveData();
				decreasePointsTask();
			}
		}, delay);
	}
	
	public int getPoints(PlayerEventInfo player)
	{
		if(player == null)
			return -1;
		
		return _warnings.containsKey(player.getPlayersId()) ? _warnings.get(player.getPlayersId()) : 0;
	}
	
	public int getPoints(int player)
	{
		return _warnings.containsKey(player) ? _warnings.get(player) : 0;
	}
	
	public void addWarning(PlayerEventInfo player, int ammount)
	{
		if(player == null)
			return;
		
		addPoints(player.getPlayersId(), ammount);
		
		if(ammount > 0)
			player.sendMessage(LanguageEngine.getMsg("system_warning", (MAX_WARNINGS - getPoints(player))));
	}
	
	public void addPoints(int player, int ammount)
	{
		int points = 0;
		if(_warnings.containsKey(player))
			points = _warnings.get(player);
		
		points += ammount;
		
		if(points < 0)
			points = 0;
		
		if(points > 0)
			_warnings.put(player, points);
		else
			_warnings.remove(player);
	}
	
	public void removeWarning(PlayerEventInfo player, int ammount)
	{
		addWarning(player, -ammount);
	}
	
	public void decreasePoints(int player, int ammount)
	{
		addPoints(player, -ammount);
	}
	
	private void loadData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			statement = con.prepareStatement("SELECT id, points FROM nexus_warnings");
			rset = statement.executeQuery();
			while (rset.next())
			{
				_warnings.put(rset.getInt("id"), rset.getInt("points"));
			}
		}

		catch (SQLException e)
		{
			e.printStackTrace();
		}

		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public void saveData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			statement = con.prepareStatement("DELETE FROM nexus_warnings");
			statement.execute();
			statement.close();
			
			for(Entry<Integer, Integer> e : _warnings.entrySet())
			{
				statement = con.prepareStatement("INSERT INTO nexus_warnings VALUES (" + e.getKey() + "," + e.getValue() + ")");
				statement.execute();
			}
		}

		catch (SQLException e)
		{
			e.printStackTrace();
		}

		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static final EventWarnings getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final EventWarnings _instance = new EventWarnings();
	}
}
