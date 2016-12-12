package l2r.gameserver.dao;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.network.AccountData;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.AccountDataRequest;
import l2r.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountsDAO
{
	private static final Logger _log = LoggerFactory.getLogger(AccountsDAO.class);
	private static Map<String, AccountData> _accountData = new ConcurrentHashMap<String, AccountData>();
	
	/**
	 * Checks the server if it holds data for the given account, if not, it returns the default (AccountData.DUMMY).
	 * Only accounts who are logged in at least once in the server since the last restart are stored here.
	 */
	public static AccountData getAccountData(String accountName)
	{
		AccountData data = _accountData.get(accountName);
		if (data == null)
			data = AccountData.DUMMY;
		
		return data;
	}
	
	/**
	 * Checks the server if it holds data for the given account, if not, it waits the given time in miliseconds 
	 * for the loginserver to answer with the account data packet. It usually happens in <10ms if the loginserver is local.
	 * Suggested usage for accounts who haven't logged in.
	 */
	public static AccountData getAccountData(String accountName, int timeoutInMilis)
	{
		AccountData data = _accountData.get(accountName);
		if (data == null)
		{
			AuthServerCommunication.getInstance().sendPacket(new AccountDataRequest(accountName));
			
			// Make at least 1 iteration happen and do not wait more than 5secs LOL!
			Util.constrain(timeoutInMilis, 1, 5000);
			
			// Wait until the request answer comes. The packet should automatically update the map via setAccountData.
			while (timeoutInMilis > 0 && !_accountData.containsKey(accountName))
			{
				try {Thread.sleep(10);} // Sleep 10 milisec
				catch (InterruptedException e) {_log.error("AccountsDAO: ",e);}
				timeoutInMilis -= 10;
			}
			
			data = _accountData.get(accountName);
		}
		
		if (data == null)
			data = AccountData.DUMMY;
		
		return data;
	}
	
	public static void setAccountData(String accountName, AccountData data)
	{
		_accountData.put(accountName, data);
	}
	
	public static void deleteAccountData(String accountName)
	{
		_accountData.remove(accountName);
	}
	
	public static void logAccount(GameClient client)
	{
		if (client == null)
			return;
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO account_log (time, login, ip, hwid) VALUES(?,?,?,?)");
			statement.setInt(1, (int) (System.currentTimeMillis() / 1000L));
			statement.setString(2, client.getLogin());
			statement.setString(3, client.getIpAddr());
			statement.setString(4, client.getHWID() == null ? "Missing hwid data." : client.getHWID());
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
		String hwid = "";
		
		int count = 0;
		
		StringBuilder sb = new StringBuilder();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM " + Config.LOGINSERVER_DB_NAME + ".account_log WHERE login = ? ORDER BY time DESC LIMIT 50");
			statement.setString(1, accountName);
			rs = statement.executeQuery();
			while(rs.next())
			{
				count++;
				ip = rs.getString("ip");
				hwid = rs.getString("hwid");
				date = rs.getLong("time");
				
				sb.append("<tr>");
				sb.append("<td width=20>" + count + ".</td>");
				sb.append("<td width=100><font name=hs12 color=D7DF01>" + ip + "</font></td>");
				sb.append("<td width=150><font name=hs12 color=01A9DB>" + hwid + "</font></td>");
				sb.append("<td width=150><font name=hs12 color=31B404>" + convertDateToString(date * 1000) + "</font></td>");
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
		SimpleDateFormat DATE_HOUR_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy");
		
		Date dt = new Date(time);
		String stringDate = DATE_HOUR_FORMAT.format(dt);
		return stringDate;
	}
}
