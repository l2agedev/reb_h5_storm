package l2r.gameserver.instancemanager;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pchayka
 */
public class BloodAltarManager
{
	private static final Logger _log = LoggerFactory.getLogger(BloodAltarManager.class);
	private static BloodAltarManager _instance;
	private static final long DELAY = 30 * 60 * 1000L;
	private static long _bossRespawnTimer = 0;
	private static boolean _bossesSpawned = false;
	private static final String[] BOSSGROUPS =
	{
		"bloodaltar_boss_aden",
		"bloodaltar_boss_darkelf",
		"bloodaltar_boss_dion",
		"bloodaltar_boss_dwarw",
		"bloodaltar_boss_giran",
		"bloodaltar_boss_gludin",
		"bloodaltar_boss_gludio",
		"bloodaltar_boss_goddart",
		"bloodaltar_boss_heine",
		"bloodaltar_boss_orc",
		"bloodaltar_boss_oren",
		"bloodaltar_boss_schutgart"
	};
	
	public static BloodAltarManager getInstance()
	{
		if (_instance == null)
			_instance = new BloodAltarManager();
		return _instance;
	}
	
	public BloodAltarManager()
	{
		_log.info("Blood Altar Manager: Initializing...");
		manageNpcs(true);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				if (Rnd.chance(30) && _bossRespawnTimer < System.currentTimeMillis())
					if (!_bossesSpawned)
					{
						manageNpcs(false);
						manageBosses(true);
						_bossesSpawned = true;
					}
					else
					{
						manageBosses(false);
						manageNpcs(true);
						_bossesSpawned = false;
					}
			}
		}, DELAY, DELAY);
	}
	
	private static void manageNpcs(boolean spawnAlive)
	{
		if (spawnAlive)
		{
			SpawnManager.getInstance().despawn("bloodaltar_dead_npc");
			SpawnManager.getInstance().spawn("bloodaltar_alive_npc");
		}
		else
		{
			SpawnManager.getInstance().despawn("bloodaltar_alive_npc");
			SpawnManager.getInstance().spawn("bloodaltar_dead_npc");
		}
	}
	
	private static void manageBosses(boolean spawn)
	{
		if (spawn)
			for (String s : BOSSGROUPS)
				SpawnManager.getInstance().spawn(s);
		else
		{
			_bossRespawnTimer = System.currentTimeMillis() + 4 * 3600 * 1000L;
			for (String s : BOSSGROUPS)
				SpawnManager.getInstance().despawn(s);
		}
	}
}
