package l2r.gameserver.dao;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromotionCheckerDAO
{
	private static final PromotionCheckerDAO _instance = new PromotionCheckerDAO();
	private static final Logger _log = LoggerFactory.getLogger(PromotionCheckerDAO.class);
	
	public static final String SELECT_SQL_QUERY = "SELECT * FROM promotion_hwid";
	public static final String INSERT_SQL_QUERY = "INSERT INTO promotion_hwid(name,hwid) VALUES (?,?)";
	public static final String DELETE_SQL_QUERY = "DELETE FROM promotion_hwid WHERE hwid=? AND name=?";
	
	private static Map<String, String> _list = new ConcurrentHashMap<String, String>();
	
	public static PromotionCheckerDAO getInstance()
	{
		return _instance;
	}
	
	public void loadHwids()
	{
		_list.clear();
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			rset = statement.executeQuery();
			
			while (rset.next())
				_list.put(rset.getString("name"), rset.getString("hwid"));
		}
		catch (Exception e)
		{
			_log.error("PromotionCheck:loadHwids(): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public void insert(String name, String hwid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setString(1, name);
			statement.setString(2, hwid);
			statement.execute();
			
			_list.put(name, hwid);
		}
		catch (Exception e)
		{
			_log.error("PromotionCheck:insert(String name, String hwid): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void delete(String name, String hwid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setString(1, name);
			statement.setString(2, hwid);;
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("PromotionCheck:delete(String name, String hwid): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public int getCountOfHWIDs()
	{
		return _list.size();
	}
	
	public boolean containsHwid(String name, String hwid)
	{
		if (hwid == null || name == null || name.isEmpty())
			return true;
		
		for (Entry<String, String> list : _list.entrySet())
		{
			if (list == null)
				continue;
			
			if (list.getKey().equalsIgnoreCase(name))
			{
				if (list.getValue().contains(hwid))
					return true;
			}
		}
		
		return false;
	}
}
