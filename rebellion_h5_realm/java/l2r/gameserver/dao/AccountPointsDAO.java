package l2r.gameserver.dao;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountPointsDAO
{
	private static final Logger _log = LoggerFactory.getLogger(AccountPointsDAO.class);
	private static final AccountPointsDAO _instance = new AccountPointsDAO();

	public static final String SELECT_SQL_QUERY = "SELECT points FROM " + Config.LOGINSERVER_DB_NAME + ".accounts WHERE login=?";
	public static final String UPDATE_SQL_QUERY = "UPDATE " + Config.LOGINSERVER_DB_NAME + ".accounts SET points=? WHERE login=?";

	public static AccountPointsDAO getInstance()
	{
		return _instance;
	}

	public int getPoint(String account)
	{
		int points = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setString(1, account);
			rset = statement.executeQuery();
			if(rset.next())
				points = rset.getInt("points");
		}
		catch(Exception e)
		{
			_log.info("AccountPointsDAO.select(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return points;
	}

	public void setPoint(String account, int points)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_SQL_QUERY);
			statement.setInt(1, points);
			statement.setString(2, account);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.info("AccountPointsDAO.delete(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
