package l2r.loginserver.utils;

import l2r.commons.dbutils.DbUtils;
import l2r.loginserver.Config;
import l2r.loginserver.accounts.Account;
import l2r.loginserver.database.L2DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Log
{
	private final static Logger _log = LoggerFactory.getLogger(Log.class);
	
	public static void LogAccount(Account account)
	{
		if(!Config.LOGIN_LOG)
			return;
			
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO account_log (time, login, ip) VALUES(?,?,?)");
			statement.setInt(1, account.getLastAccess());
			statement.setString(2, account.getLogin());
			statement.setString(3, account.getLastIP());			
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static StringBuilder getAccountLog(String accountName)
	{
		long date = 0;
		String ip = "";
		
		int count = 0;
		
		StringBuilder sb = new StringBuilder();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM account_log WHERE login = ? ORDER BY time DESC LIMIT 50");
			statement.setString(1, accountName);
			rs = statement.executeQuery();
			while(rs.next())
			{
				count++;
				ip = rs.getString("ip");
				date = rs.getLong("time");
				
				sb.append("<tr>");
				sb.append("<td width=20>" + count + ".</td>");
				sb.append("<td width=100><font color=D7DF01>IP</font>: " + ip + "</td>");
				sb.append("<td width=150><font color=31B404>Date</font>: " + convertDateToString(date * 1000) + "</td>");
				sb.append("</tr>");
				
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
		
		return sb;
	}
	
	private static String convertDateToString(long time)
	{
		SimpleDateFormat DATE_HOUR_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");
		
		Date dt = new Date(time);
		String stringDate = DATE_HOUR_FORMAT.format(dt);
		return stringDate;
	}
}
