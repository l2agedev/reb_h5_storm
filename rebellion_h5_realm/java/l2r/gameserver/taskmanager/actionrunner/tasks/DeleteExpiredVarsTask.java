package l2r.gameserver.taskmanager.actionrunner.tasks;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.database.mysql;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.utils.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DeleteExpiredVarsTask extends AutomaticTask
{
	private static final Logger _log = Logger.getLogger(DeleteExpiredVarsTask.class);

	public DeleteExpiredVarsTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		long time = System.currentTimeMillis();
		
		Connection con = null;
		PreparedStatement query = null;
		Map<Integer, String> varMap = new HashMap<Integer, String>();
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			query = con.prepareStatement("SELECT obj_id, name FROM character_variables WHERE expire_time > 0 AND expire_time < ?");
			query.setLong(1, time);
			rs = query.executeQuery();
			while(rs.next())
			{
				String name = rs.getString("name");
				String obj_id = Strings.stripSlashes(rs.getString("obj_id"));
				varMap.put(Integer.parseInt(obj_id), name);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, query, rs);
		}
		
		if (!varMap.isEmpty())
			for (Map.Entry<Integer, String> entry : varMap.entrySet())
			{
				Player player = GameObjectsStorage.getPlayer(entry.getKey());
				if (player != null && player.isOnline())
					player.unsetVar(entry.getValue());
				else
					mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", entry.getKey(), entry.getValue());
			}
	}
	
	@Override
	public long reCalcTime(boolean start)
	{
		return System.currentTimeMillis() + 600000L;
	}
}
