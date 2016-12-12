package l2r.gameserver.dao;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.templates.StatsSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 20:00/02.05.2011
 */
public class OlympiadNobleDAO
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadNobleDAO.class);
	private static final OlympiadNobleDAO _instance = new OlympiadNobleDAO();

	public static final String SELECT_SQL_QUERY = "SELECT char_id, characters.char_name as char_name, class_id, olympiad_points, olympiad_points_past, olympiad_points_past_static, olympiad_competitions_win_past_static, olympiad_competitions_done_past_static, competitions_done, competitions_loose, competitions_win, game_classes_count, game_noclasses_count, game_team_count FROM olympiad_nobles LEFT JOIN characters ON characters.obj_Id = olympiad_nobles.char_id";
	public static final String REPLACE_SQL_QUERY = "REPLACE INTO `olympiad_nobles` (`char_id`, `class_id`, `olympiad_points`, `olympiad_points_past`, `olympiad_points_past_static`, `olympiad_competitions_win_past_static`, `olympiad_competitions_done_past_static`, `competitions_done`, `competitions_win`, `competitions_loose`, game_classes_count, game_noclasses_count, game_team_count) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String OLYMPIAD_GET_HEROS = "SELECT `char_id`, characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON char_id=characters.obj_Id WHERE `class_id` = ? AND `competitions_done` >= ? AND `competitions_win` > 0 ORDER BY `olympiad_points` DESC, `competitions_win` DESC, `competitions_done` DESC";
	public static final String OLYMPIAD_GET_HEROS_SOULHOUND = "SELECT `char_id`, characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON char_id=characters.obj_Id WHERE `class_id` IN (?, 133) AND `competitions_done` >= ? AND `competitions_win` > 0 ORDER BY `olympiad_points` DESC, `competitions_win` DESC, `competitions_done` DESC";
	public static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON char_id=characters.obj_Id WHERE `class_id` = ? AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC, `olympiad_competitions_win_past_static` DESC, `olympiad_competitions_done_past_static` DESC LIMIT 10";
	public static final String GET_EACH_CLASS_LEADER_SOULHOUND = "SELECT characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON characters.obj_Id=olympiad_nobles.char_id WHERE `class_id` IN (?, 133) AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC, `olympiad_competitions_win_past_static` DESC, `olympiad_competitions_done_past_static` DESC LIMIT 10";
	public static final String GET_ALL_CLASSIFIED_NOBLESS = "SELECT `char_id` FROM `olympiad_nobles` ORDER BY olympiad_points_past_static DESC, olympiad_competitions_win_past_static DESC, olympiad_competitions_done_past_static DESC";
	public static final String OLYMPIAD_CALCULATE_LAST_PERIOD = "UPDATE `olympiad_nobles` SET `olympiad_points_past` = `olympiad_points`, `olympiad_points_past_static` = `olympiad_points`, `olympiad_competitions_win_past_static` = `competitions_win`, `olympiad_competitions_done_past_static` = `competitions_done` WHERE `competitions_done` >= ?";
	public static final String OLYMPIAD_CLEANUP_NOBLES = "UPDATE `olympiad_nobles` SET `olympiad_points` = ?, `competitions_done` = 0, `competitions_win` = 0, `competitions_loose` = 0, game_classes_count=0, game_noclasses_count=0, game_team_count=0";
	public static final String DELETE_SQL_QUERY = "DELETE FROM `olympiad_nobles` WHERE `char_id` = ?";
	
	public static OlympiadNobleDAO getInstance()
	{
		return _instance;
	}

	public void select()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			rset = statement.executeQuery();
			while(rset.next())
			{
				// If a player is 1st or 2nd class:
				// Example: kamael trooper, it set him to first that he find 4nd class aka doombringer.
				ClassId classId = ClassId.VALUES[rset.getInt(Olympiad.CLASS_ID)];
				
				if(classId.getLevel() < 4)
				{
					for(ClassId id : ClassId.VALUES)
					{
						if(id.getLevel() == 4 && id.childOf(classId))
						{
							classId = id;
							break;
						}
					}
				}

				StatsSet statDat = new StatsSet();
				int charId = rset.getInt(Olympiad.CHAR_ID);
				statDat.set(Olympiad.CLASS_ID, classId.getId());
				statDat.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));
				statDat.set(Olympiad.POINTS, rset.getInt(Olympiad.POINTS));
				statDat.set(Olympiad.POINTS_PAST, rset.getInt(Olympiad.POINTS_PAST));
				statDat.set(Olympiad.POINTS_PAST_STATIC, rset.getInt(Olympiad.POINTS_PAST_STATIC));
				statDat.set(Olympiad.COMP_WIN_PAST_STATIC, rset.getInt(Olympiad.COMP_WIN_PAST_STATIC));
				statDat.set(Olympiad.COMP_DONE_PAST_STATIC, rset.getInt(Olympiad.COMP_DONE_PAST_STATIC));
				statDat.set(Olympiad.COMP_DONE, rset.getInt(Olympiad.COMP_DONE));
				statDat.set(Olympiad.COMP_WIN, rset.getInt(Olympiad.COMP_WIN));
				statDat.set(Olympiad.COMP_LOOSE, rset.getInt(Olympiad.COMP_LOOSE));
				statDat.set(Olympiad.GAME_CLASSES_COUNT, rset.getInt(Olympiad.GAME_CLASSES_COUNT));
				statDat.set(Olympiad.GAME_NOCLASSES_COUNT, rset.getInt(Olympiad.GAME_NOCLASSES_COUNT));
				statDat.set(Olympiad.GAME_TEAM_COUNT, rset.getInt(Olympiad.GAME_TEAM_COUNT));

				Olympiad._nobles.put(charId, statDat);
			}
		}
		catch(Exception e)
		{
			_log.error("OlympiadNobleDAO: select():", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void replace(int nobleId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			StatsSet nobleInfo = Olympiad._nobles.get(nobleId);

			statement = con.prepareStatement(REPLACE_SQL_QUERY);
			statement.setInt(1, nobleId);
			statement.setInt(2, nobleInfo.getInteger(Olympiad.CLASS_ID));
			statement.setInt(3, nobleInfo.getInteger(Olympiad.POINTS));
			statement.setInt(4, nobleInfo.getInteger(Olympiad.POINTS_PAST));
			statement.setInt(5, nobleInfo.getInteger(Olympiad.POINTS_PAST_STATIC));
			statement.setInt(6, nobleInfo.getInteger(Olympiad.COMP_WIN_PAST_STATIC));
			statement.setInt(7, nobleInfo.getInteger(Olympiad.COMP_DONE_PAST_STATIC));
			statement.setInt(8, nobleInfo.getInteger(Olympiad.COMP_DONE));
			statement.setInt(9, nobleInfo.getInteger(Olympiad.COMP_WIN));
			statement.setInt(10, nobleInfo.getInteger(Olympiad.COMP_LOOSE));
			statement.setInt(11, nobleInfo.getInteger(Olympiad.GAME_CLASSES_COUNT));
			statement.setInt(12, nobleInfo.getInteger(Olympiad.GAME_NOCLASSES_COUNT));
			statement.setInt(13, nobleInfo.getInteger(Olympiad.GAME_TEAM_COUNT));
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("OlympiadNobleDAO: replace(int): " + nobleId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public boolean delete(int nobleId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setInt(1, nobleId);
			return statement.execute();
		}
		catch (Exception e)
		{
	      _log.error("Can't delete noble " + nobleId + " from `olympiad_nobles`!", e);
	    }
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		
		return false;
	}
}
