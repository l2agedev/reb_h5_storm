package services;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlineUpdate extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(OnlineUpdate.class);
	public void onLoad()
	{
		_log.info("Loaded Service: Parse Online [" + (Config.ALLOW_ONLINE_PARSE ? "enabled]" : "disabled]"));
		if (Config.ALLOW_ONLINE_PARSE)
		{
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new updateOnline(), Config.FIRST_UPDATE * 60000, Config.DELAY_UPDATE * 60000);
			
		}
	}
	
	private class updateOnline implements Runnable
	{
		public void run()
		{
			int members = getOnlineMembers();
			int offMembers = getOfflineMembers();
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE online SET totalOnline = ?, totalOffline = ? WHERE 'index' = 0");
				statement.setInt(1, members);
				statement.setInt(2, offMembers);
				statement.execute();
				DbUtils.closeQuietly(statement);
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}
	
	private int getOnlineMembers()
	{
		if (Config.AUTH_SERVER_GM_ONLY)
			return 0;
		
		return GameObjectsStorage.getAllPlayersCount();
	}
	
	private int getOfflineMembers()
	{
		if (Config.AUTH_SERVER_GM_ONLY)
			return 0;
		
		int i = 0;
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if (player.isInOfflineMode())
				i++;
		}
		
		return i;
	}
	
	public void onReload()
	{
	}
	
	public void onShutdown()
	{
	}
}