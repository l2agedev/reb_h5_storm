package l2r.gameserver.dao;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restrict quests by HWID for time.
 * @author Infern0
 */
public class QuestHWIDRestriction
{
	private static final Logger _log = LoggerFactory.getLogger(QuestHWIDRestriction.class);
	private static List<HWIDData> _data = new ArrayList<HWIDData>();

	private static QuestHWIDRestriction _instance = new QuestHWIDRestriction();
	
	public class HWIDData
	{
		private String _hwid;
		private String _questName;
		private long _restrictUntil;
		
		public HWIDData(String hwid, String questname, long date)
		{
			_hwid = hwid;
			_questName = questname;
			_restrictUntil = date;
		}
		
		public String getHwid()
		{
			return _hwid;
		}
		
		public String getQuestName()
		{
			return _questName;
		}
		
		public long getDate()
		{
			return _restrictUntil;
		}
	}
	
	public void loadQuestData()
	{
		_data.clear();
		
		String hwid;
		String questname;
		long date;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM quest_hwid_restriction");
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				hwid = rset.getString("hwid");
				questname = rset.getString("questname");
				date = rset.getLong("restricUntil");
				
				HWIDData data = new HWIDData(hwid, questname, date);
				
				_data.add(data);
			}
		}
		catch (Exception e)
		{
			_log.error("QuestHWIDRestriction:loadQuestData: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
			_log.info("Quests HWID Restriction: Loaded successfully.");
		}
	}
	
	public void insertQuestData(Player player, String questname, long date)
	{
		if (!player.hasHWID())
		{
			_log.error("QuestHWIDRestriction:insertQuestData(): error with HWID grep...  for Player " + player.getName());
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO quest_hwid_restriction(hwid, questname, restricUntil) VALUES (?,?,?)");
			statement.setString(1, player.getHWID());
			statement.setString(2, questname);
			statement.setLong(3, date);
			statement.execute();
			
			HWIDData data = new HWIDData(player.getHWID(), questname, date);
			
			_data.add(data);
		}
		catch (Exception e)
		{
			_log.error("QuestHWIDRestriction:insertQuestData(String hwid, String questname, long date): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void deleteQuestData(String hwid, String questName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM quest_hwid_restriction WHERE hwid=? AND questname=?");
			statement.setString(1, hwid);
			statement.setString(2, questName);;
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("QuestHWIDRestriction:deleteQuestData(String hwid, String questName): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public long canRepeatQuest(Player player, String questname)
	{
		if (!player.hasHWID())
			return 0;
		
		for (HWIDData data : _data)
		{
			if (data == null)
				continue;
			
			if (player.getHWID().equals(data.getHwid()))
			{
				if (data.getQuestName().equalsIgnoreCase(questname) && data.getDate() > System.currentTimeMillis())
					return data.getDate();
			}
		}
		
		return 0;
	}
	
	public void setHWIDData(String hwid, String questName, long date)
	{
		HWIDData data = new HWIDData(hwid, questName, date);
		
		_data.add(data);
	}
	
	public void deleteHWIDData(String hwid)
	{
		_data.remove(hwid);
	}
	
	public static QuestHWIDRestriction getInstance()
	{
		return _instance;
	}
}
