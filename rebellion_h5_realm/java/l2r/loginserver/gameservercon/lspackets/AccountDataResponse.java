package l2r.loginserver.gameservercon.lspackets;

import l2r.commons.dbutils.DbUtils;
import l2r.loginserver.database.L2DatabaseFactory;
import l2r.loginserver.gameservercon.SendablePacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountDataResponse extends SendablePacket
{
	private static final Logger _log = LoggerFactory.getLogger(SendablePacket.class);
	
	private String _account;
	private int _accessLevel;
	private int _banExpire;
	private String _allowedIps;
	private String _allowedHwids;
	private double _bonus;
	private int _bonusExpire;
	private int _lastServer;
	private String _lastIp;
	private int _lastAccess;
	private int _botReportPoints;
	private int _points;

	public AccountDataResponse(String account)
	{
		_account = account;
		_accessLevel = 0;
		_banExpire = 0;
		_allowedIps = "";
		_allowedHwids = "";
		_bonus = 0;
		_bonusExpire = 0;
		_lastServer = 0;
		_lastIp = "0.0.0.0";
		_lastAccess = 0;
		_botReportPoints = 0;
		_points = 0;
		
		loadData();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x07);
		writeS(_account);
		writeD(_accessLevel);
		writeD(_banExpire);
		writeS(_allowedIps);
		writeS(_allowedHwids);
		writeF(_bonus);
		writeD(_bonusExpire);
		writeD(_lastServer);
		writeS(_lastIp);
		writeD(_lastAccess);
		writeD(_botReportPoints);
		writeD(_points);
		
	}
	
	private void loadData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM accounts WHERE login=?");
			statement.setString(1, _account);
			rset = statement.executeQuery();
			if(rset.next())
			{
				_accessLevel = rset.getInt("access_level");
				_banExpire = rset.getInt("ban_expire");
				_allowedIps = rset.getString("allow_ip");
				_allowedHwids = rset.getString("allow_hwid");
				_bonus = rset.getDouble("bonus");
				_bonusExpire = rset.getInt("bonus_expire");
				_lastServer = rset.getInt("last_server");
				_lastIp = rset.getString("last_ip");
				_lastAccess = rset.getInt("last_access");
				_botReportPoints = rset.getInt("bot_report_points");
				_points = rset.getInt("points");
			}
		}
		catch(Exception e)
		{
			_log.info("AccountDataResponse.loadData(): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
}
