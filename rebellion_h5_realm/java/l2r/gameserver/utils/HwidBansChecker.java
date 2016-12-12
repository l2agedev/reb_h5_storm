package l2r.gameserver.utils;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.GameClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Infern0
 */
public final class HwidBansChecker
{
	protected static final Logger _log = LoggerFactory.getLogger(HwidBansChecker.class);
	
	private static List<String> _bans = new ArrayList<>();
	
	public HwidBansChecker()
	{
		if (Config.ENABLE_HWID_CHECKER)
			startUpdateTask();
	}
	
	private static void startUpdateTask()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new ScheduleTimerTask(), 5000, 5000); // every 5 sec will check sql.
		_log.info("HwidBansChecker: Task for check has been started.");
	}
	
	static class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				getAllHwidBans();
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	public boolean isClientBanned(GameClient client)
	{
		if (!Config.ENABLE_HWID_CHECKER)
			return false;
		
		if (client == null)
			return false;
		
		String hwid = client.getHWID();
		if (hwid == null || hwid.isEmpty())
			return false;
		
		String ip = client.getIpAddr();
		
		if (ip == null || ip.equals(Player.NOT_CONNECTED) || ip.equals("?.?.?.?"))
			return false;
		
		String[] ips = ip.split("\\.");
		
		String hwidcheck = getCPU(hwid);
		String iphwidcheck = ips[0] + "" + ips[1] + "" + getCPU(hwid);
		
		for (String ban : _bans)
		{
			if (ban == null)
				continue;
			
			if (ban.equals(hwidcheck))
			{
				Log.hwidBan("Client: " + client.getLogin() + " IP: " + ip + " has matched the [HWID CHECK - CPU/HDD] and has not been allowed to enter ingame.");
				Log.hwidBan("Client: " + client.getLogin() + " hwidcheck: " + hwidcheck);
				return true;
			}
			
			if (ban.equals(iphwidcheck))
			{
				Log.hwidBan("Client: " + client.getLogin() + " IP: " + ip + " has matched the [HWID CHECK - (IP[1] + IP[2] + CPU/HDD)] and has not been allowed to enter ingame.");
				Log.hwidBan("Client: " + client.getLogin() + " iphwidcheck: " + iphwidcheck);
				return true;
			}
		}
		
		return false;
	}
	
	static List<String> getAllHwidBans()
	{
		_bans.clear();
		
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM hwid_bans");
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				String hwid = result.getString("hwid");
				if (hwid == null)
					continue;
				
				_bans.add(hwid);
			}
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		
		return _bans;
	}
	
	public void addHwidBan(Player requester, Player target)
	{
		if (!Config.ENABLE_HWID_CHECKER)
		{
			requester.sendMessage("This system is disabled...");
			return;
		}
		
		if (!target.hasHWID())
		{
			requester.sendMessage("Your target does not have hwid...");
			return;
		}
		
		String ip = target.getIP();
		
		if (ip.equals(Player.NOT_CONNECTED) || ip.equals("?.?.?.?"))
		{
			requester.sendMessage("Your target does not have IP....");
			return;
		}
		
		String[] ips = ip.split("\\.");
		String hwidtoban = ips[0] + "" + ips[1] + "" + getCPU(target.getHWID());
		insertBan(hwidtoban);
		
	}
	// smartguard use 48 byte of hwid, 
	// lets take the 8 bytes which is CPU
	private static String getCPU(String hwid)
	{
		byte[] hwidBytes = Util.asByteArray(hwid);
		return Util.asHex(new byte[]
		{
			hwidBytes[0],
			hwidBytes[1],
			hwidBytes[2],
			hwidBytes[3],
			hwidBytes[4],
			hwidBytes[5],
			hwidBytes[6],
			hwidBytes[7]
		});
	}
	
	// smartguard use 48 byte of hwid,
	// lets take the 8 bytes which is HDD
	@SuppressWarnings("unused")
	private static String getHDD(String hwid)
	{
		byte[] hwidBytes = Util.asByteArray(hwid);
		return Util.asHex(new byte[]
		{
			hwidBytes[8],
			hwidBytes[9],
			hwidBytes[10],
			hwidBytes[11],
			hwidBytes[12],
			hwidBytes[13],
			hwidBytes[14],
			hwidBytes[15]
		});
	}
	
	// smartguard use 48 byte of hwid,
	// lets take the 8 bytes which is MASK
	@SuppressWarnings("unused")
	private static String getMask(String hwid)
	{
		byte[] hwidBytes = Util.asByteArray(hwid);
		return Util.asHex(new byte[]
		{
			hwidBytes[16],
			hwidBytes[17],
			hwidBytes[18],
			hwidBytes[19],
			hwidBytes[20],
			hwidBytes[21],
			hwidBytes[22],
			hwidBytes[23]
		});
	}
	
	private void insertBan(String ban)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO hwid_bans (hwid) VALUES (?)");
			statement.setString(1, ban);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("Could not insert ban : " + e.getMessage() + " for " + ban, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static final HwidBansChecker getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HwidBansChecker _instance = new HwidBansChecker();
	}
}