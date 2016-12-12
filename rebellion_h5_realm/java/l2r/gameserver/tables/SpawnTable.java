package l2r.gameserver.tables;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.data.xml.holder.SpawnHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.GameObjectTasks;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.templates.spawn.PeriodOfDay;
import l2r.gameserver.templates.spawn.SpawnNpcInfo;
import l2r.gameserver.templates.spawn.SpawnTemplate;
import l2r.gameserver.utils.Location;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnTable
{
	private static final Logger _log = LoggerFactory.getLogger(SpawnTable.class);
	private static SpawnTable _instance;

	public static SpawnTable getInstance()
	{
		if (_instance == null)
			new SpawnTable();
		return _instance;
	}

	private SpawnTable()
	{
		_instance = this;
		if (Config.LOAD_CUSTOM_SPAWN)
			fillCustomSpawnTable();
	}

	private void fillCustomSpawnTable()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM add_spawnlist ORDER by npc_templateid");
			rset = statement.executeQuery();

			while (rset.next())
			{
				int count = rset.getInt("count");
				int delay = rset.getInt("respawn_delay");
				int delay_rnd = rset.getInt("respawn_delay_rnd");
				int npcId = rset.getInt("npc_templateid");
				int x = rset.getInt("locx");
				int y = rset.getInt("locy");
				int z = rset.getInt("locz");
				int h = rset.getInt("heading");

				SpawnTemplate template = new SpawnTemplate(PeriodOfDay.NONE, count, delay, delay_rnd);
				template.addNpc(new SpawnNpcInfo(npcId, 1, StatsSet.EMPTY));
				template.addSpawnRange(new Location(x, y, z, h));
				SpawnHolder.getInstance().addSpawn(PeriodOfDay.NONE.name(), template);
			}
		}
		catch (Exception e1)
		{
			_log.warn("custom_spawnlist couldnt be initialized:" + e1);
			e1.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void addNewSpawn(SimpleSpawner spawn)
	{
		/*
		String location = "";
		
		List<Zone> zones = new ArrayList<Zone>();
		World.getZones(zones, spawn.getLoc(), spawn.getReflection());
		for (Zone zone : zones)
			location += zone.getName() + ";";

		if (location.length() > 200)
		{
			location = location.substring(0, location.length() - (location.length() - 200));
			location += "...";
		}
		*/
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `add_spawnlist` (date_spawned,count,npc_templateid,locx,locy,locz,heading,respawn_delay) values(?,?,?,?,?,?,?,?)");
			statement.setDate(1, new Date(System.currentTimeMillis()));
			statement.setInt(2, spawn.getAmount());
			statement.setInt(3, spawn.getCurrentNpcId());
			statement.setInt(4, spawn.getLocx());
			statement.setInt(5, spawn.getLocy());
			statement.setInt(6, spawn.getLocz());
			statement.setInt(7, spawn.getHeading());
			statement.setInt(8, spawn.getRespawnDelay());
			statement.execute();
		}
		catch (Exception e1)
		{
			_log.warn("spawn couldnt be stored in db:" + e1);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deleteSpawn(Location loc, int template)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM add_spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?");
			statement.setInt(1, loc.x);
			statement.setInt(2, loc.y);
			statement.setInt(3, loc.z);
			statement.setInt(4, template);
			statement.setInt(5, loc.h);
			statement.execute();
		}
		catch (Exception e1)
		{
			_log.warn("spawn couldnt be deleted in db:" + e1);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static NpcInstance spawnSingle(int npcId, int x, int y, int z)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, despawnTime);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, int h, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, h), ReflectionManager.DEFAULT, despawnTime);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, 0);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, long despawnTime)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, despawnTime);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection)
	{
		return spawnSingle(npcId, loc, reflection, 0);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection, long despawnTime)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
			throw new NullPointerException("Npc template id : " + npcId + " not found!");

		NpcInstance npc = template.getNewInstance();
		npc.setHeading(loc.h < 0 ? Rnd.get(0xFFFF) : loc.h);
		npc.setSpawnedLoc(loc);
		npc.setReflection(reflection);
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);

		npc.spawnMe(npc.getSpawnedLoc());
		if(despawnTime > 0)
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(npc), despawnTime);
		return npc;
	}
}