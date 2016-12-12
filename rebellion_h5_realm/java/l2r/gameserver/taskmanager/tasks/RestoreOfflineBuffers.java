package l2r.gameserver.taskmanager.tasks;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestoreOfflineBuffers extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(RestoreOfflineBuffers.class);

	@Override
	public void runImpl() throws Exception
	{
		int count = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			//Убираем просроченных
			if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
			{
				int expireTimeSecs = (int) (System.currentTimeMillis() / 1000L - Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);

				statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offlinebuffer' AND value < ?");
				statement.setLong(1, expireTimeSecs);
				statement.executeUpdate();

				DbUtils.close(statement);
			}

			//Убираем забаненных
			statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offlinebuffer' AND obj_id IN (SELECT obj_id FROM characters WHERE accessLevel < 0)");
			statement.executeUpdate();

			DbUtils.close(statement);

			statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offlinebuffer'");
			rset = statement.executeQuery();

			int objectId;
			int expireTimeSecs;
			Player p;

			while(rset.next())
			{
				objectId = rset.getInt("obj_id");
				expireTimeSecs = rset.getInt("value");

				p = Player.restore(objectId);
				if(p == null)
					continue;

				if(p.isDead())
				{
					p.kick();
					continue;
				}
				
				p.setNameColor(Config.OFFLINE_BUFFER_NAME_COLOR);
				p.setTitleColor(Config.OFFLINE_BUFFER_TITLE_COLOR);
				p.setTitle(p.getVar("offlinebuffertitle"));
				p.setOfflineMode(true);
				p.setSellBuff(true);
				p.setIsOnline(true);
				p.updateOnlineStatus();

				p.spawnMe();

				if(p.getClan() != null && p.getClan().getAnyMember(p.getObjectId()) != null)
					p.getClan().getAnyMember(p.getObjectId()).setPlayerInstance(p, false);

				if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
					p.startKickTask((Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK + expireTimeSecs  - System.currentTimeMillis() / 1000L) * 1000L);

				count++;
			}
		}
		catch(Exception e)
		{
			_log.error("Error while restoring offline buffers!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		_log.info("Restored " + count + " offline buffers!");
	}
}
