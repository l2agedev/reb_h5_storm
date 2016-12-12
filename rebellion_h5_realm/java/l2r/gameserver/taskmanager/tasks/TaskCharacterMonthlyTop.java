package l2r.gameserver.taskmanager.tasks;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.achievements.PlayerCounters;
import l2r.gameserver.achievements.PlayerTops;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.taskmanager.Task;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2r.gameserver.taskmanager.TaskTypes;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task update monthly ranking.
 * @author Infern0
 */
public class TaskCharacterMonthlyTop extends Task
{
	private static final Logger _log = LoggerFactory.getLogger(TaskCharacterMonthlyTop.class);
	private static final String NAME = "TaskCharacterMonthlyTop";
	
	private static int LIMIT_QUERY_RESULTS = 10;
	
	private HashMap<String, List<MonthlyTopScore>> _monthlytop = new HashMap<String, List<MonthlyTopScore>>();
	
	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		if (!Config.PLAYER_TOP_MONTHLY_RANKING)
		{
			_log.info("Task: Monthly Ranking is Disabled!");
			return;
		}
		
		_log.info("Task: Monthly Ranking Cleanup initiliazed.");
		List<String> fieldNames = PlayerTops.getInstance().getFieldList();
		
		int seasonId = ServerVariables.getInt("SeasonRanking", 0) + 1;
		
		for (String field : fieldNames)
			generateDataBy(field, LIMIT_QUERY_RESULTS);

		StringBuilder sb;
		
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			for (String type : fieldNames)
			{
				if (_monthlytop.get(type) != null)
				{
					sb = new StringBuilder();
					for (MonthlyTopScore pl : _monthlytop.get(type))
					{
						if (pl.getPlace() == 11)
							break;
						
						sb.append(pl.getObjectId() + "," + pl.getPlace() + "," + pl.getTop() + ";");
					}
					
					// Insert type and data for the current season.
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO character_counters_monthly (season,type,data) values (?,?,?);");
					statement.setInt(1, seasonId);
					statement.setString(2, type);
					statement.setString(3, sb.toString());
					statement.execute();
					
					DbUtils.closeQuietly(con, statement);
				}
				else
				{
					// If there is no data, make simple insert.
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO character_counters_monthly (season,type,data) values (?,?,?);");
					statement.setInt(1, seasonId);
					statement.setString(2, type);
					statement.setString(1, "");
					statement.execute();
					
					DbUtils.closeQuietly(con, statement);
				}
				
				PlayerTops.getInstance().generateSeasonData();
			}
			
			// Now lets delete the old data from character_counters.
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("TRUNCATE TABLE character_counters;");
			statement.execute();
			
			// Refresh index page of ranking, coz of truncate.
			for (Field field : PlayerCounters.class.getFields())
			{
				switch (field.getName())
				{
					case "_activeChar":
					case "_playerObjId":
					case "DUMMY_COUNTER":
						continue;
					default:
						PlayerTops.getInstance().generateDataFromAcsBy(field.getName());
				}
			}
			
			// Set new seeason ID.
			ServerVariables.set("SeasonRanking", seasonId);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		
		_log.info("Task: Monthly TOPs has been generated.");
		_log.info("Task: Season " + seasonId + " has finished.");
	}

	private void generateDataBy(String type, int limit)
	{
		List<MonthlyTopScore> _temp = new ArrayList<MonthlyTopScore>();
		
		int rank = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_id, " + type + " FROM character_counters WHERE " + type + " > 0 ORDER BY " + type + " DESC LIMIT " + limit + "");
			rset = statement.executeQuery();
			while(rset.next())
			{
				rank++;
				int objId = rset.getInt("char_id");
				long value = rset.getLong(type);
				
				_temp.add(new MonthlyTopScore(objId, rank, value));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_monthlytop.put(type, _temp);
	}
	
	public class MonthlyTopScore
	{
		int _objId = 0;
		long _topValue = 0;
		int _place = 0;
		
		public MonthlyTopScore(int objId, int place, long value)
		{
			_objId = objId;
			_place = place;
			_topValue = value;
		}
		
		public int getObjectId()
		{
			return _objId;
		}
		
		public long getTop()
		{
			return _topValue;
		}
		
		public int getPlace()
		{
			return _place;
		}
	}
	
	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "30", "7:00:00", "");
	}
}