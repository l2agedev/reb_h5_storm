package l2r.gameserver.achievements;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author Nik, Infern0
 *
 */
public class PlayerCounters
{
	public static PlayerCounters DUMMY_COUNTER = new PlayerCounters(null);
	
	/*
	 * Start of row's
	 */
	public int _PvP = 0;
	public int _PK = 0;
	public int _Highest_Karma = 0;
	
	public int _Times_Died = 0;
	public int _Players_Ressurected = 0;
	public int _Duels_Won = 0;

	public int _Monsters_Killed = 0;
	public int _Raids_Killed = 0;
	public int _Champions_Killed = 0;

	public int _Crafted_Recipes = 0;
	public int _Failed_Crafting = 0;
	public int _Olympiad_Games_Won = 0;

	public int _Longest_Kill_Spree = 0;
	public int _Kill_Sprees_Ended = 0;
	public int _Enchant_Item = 0;

	public int _Crits_Done = 0;
	public int _Mcrits_Done = 0;
	public int _Get_Married = 0;

	public int _Castle_Sieges_Won = 0;	
	public int _Dominion_Sieges_Won = 0;
	public int _Fortress_Sieges_Won = 0;
	
	public int _Players_Killed_In_Siege = 0;
	public int _Players_Killed_In_Dominion = 0;
	public int _Players_Killed_At_Fortress = 0;
	
	public int _Enchanted_Normal_Scroll = 0;
	public int _Enchanted_Blessed_Scroll = 0;
	public int _Became_Hero = 0;

	public int _Manor_Seeds_Sow = 0;
	public int _Fish_Caught = 0;
	public int _Treasure_Boxes_Opened = 0;
	
	public int _Antharas_Killed = 0;
	public int _Baium_Killed = 0; 
	public int _Valakas_Killed = 0;
	
	public int _Orfen_Killed = 0;
	public int _Ant_Queen_Killed = 0;
	public int _Core_Killed = 0;
	
	public int _Beleth_Killed = 0;
	public int _Sailren_Killed = 0;
	public int _Baylor_Killed = 0;
	
	public int _Main_Events = 0;
	public int _Mini_Events = 0;
	public int _Captured_Flags = 0;
	
	public int _Returned_Flags = 0;
	public int _First_Blood = 0;
	public int _Chests_Opened = 0;
	
	public int _Killed_Mutants = 0;
	public int _Killed_Zombies = 0;
	public int _Domination_Points = 0;
	
	public int _Times_Voted = 0;
	public int _Achievements_Done = 0;
	public int _Summons_Killed = 0;
	/*
	 * END
	 */
	
	// Here comes the code...
	private Player _activeChar = null;
	private int _playerObjId = 0;

	public PlayerCounters(Player activeChar)
	{
		_activeChar = activeChar;
	    _playerObjId = activeChar == null ? 0 : activeChar.getObjectId();
	}
	
	public PlayerCounters(int playerObjId)
	{
		_activeChar = World.getPlayer(playerObjId);
		_playerObjId = playerObjId;
	}

	protected Player getChar()
	{
		return _activeChar;
	}
	
	public void addPoint(String fieldName)
	{
		if (_activeChar == null)
			return;
		
		addPoints(fieldName, 1);
	}
	
	public void addPoints(String fieldName, int points)
	{	
		if (_activeChar == null)
			return;
		
		switch (fieldName)
		{
			case "_Achievements_Done":
				setPoints(fieldName, (getPoints(fieldName) + points));
				AchievementNotification.update(_activeChar, 0);
				break;
			default:
				setPoints(fieldName, (getPoints(fieldName) + points));
		}
		
		if (getChar() != null)
			Achievements.getInstance().checkArchievements(_activeChar);
	}
	
	public int getPoints(String fieldName)
	{
		if (_activeChar == null)
			return 0;
		
		try
		{
			return getClass().getField(fieldName).getInt(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public long getPointsLong(String fieldName)
	{
		if (_activeChar == null)
			return 0;
		
		try
		{
			return getClass().getField(fieldName).getLong(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public void setPoints(String fieldName, int points)
	{
		if (_activeChar == null)
			return;
		
		try
		{
			getClass().getField(fieldName).setInt(this, points);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setPointsLong(String fieldName, long points)
	{
		if (_activeChar == null)
			return;
		
		try
		{
			getClass().getField(fieldName).setLong(this, points);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void save()
	{
		if (_activeChar == null)
			return;
		
		Connection con = null;
		Connection con2 = null;
		PreparedStatement statement2 = null;
		PreparedStatement statement3 = null;
		ResultSet rs = null;
		try
		{
			con2 = DatabaseFactory.getInstance().getConnection();
			statement2 = con2.prepareStatement("SELECT char_id FROM character_counters WHERE char_id = " + _playerObjId + ";");
			rs = statement2.executeQuery();
			if (!rs.next())
			{
				statement3 = con2.prepareStatement("INSERT INTO character_counters (char_id) values (?);");
				statement3.setInt(1, _playerObjId);
				statement3.execute();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con2, statement2, rs);
		}
		
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE character_counters SET ");
			boolean firstPassed = false;
			for (Field field : getClass().getFields())
			{
				switch (field.getName()) // Fields that we wont save.
				{
					case "_activeChar":
					case "_playerObjId":
					case "DUMMY_COUNTER":
						continue;
				}
				
				if (firstPassed)
					sb.append(",");
				sb.append(field.getName());
				sb.append("=");
				
				try
				{
					sb.append(field.getInt(this));
				}
				catch (IllegalArgumentException | IllegalAccessException | SecurityException e)
				{
					sb.append(field.getLong(this));
				}
				
				firstPassed = true;
			}
			sb.append(" WHERE char_id=" + _playerObjId + ";");
			statement = con.prepareStatement(sb.toString());
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
    
	public void load()
	{
		if (_activeChar == null)
			return;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM character_counters WHERE char_id = ?");
			statement.setInt(1, getChar().getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				for (Field field : getClass().getFields())
				{
					switch (field.getName()) // Fields that we dont use here.
					{
						case "_activeChar":
						case "_playerObjId":
						case "DUMMY_COUNTER":
							continue;
					}
					
					try
					{
						setPoints(field.getName(), rs.getInt(field.getName()));
					}
					catch (SQLException sqle)
					{
						setPointsLong(field.getName(), rs.getLong(field.getName()));
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}
}
