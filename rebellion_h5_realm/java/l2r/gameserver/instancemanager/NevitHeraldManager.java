package l2r.gameserver.instancemanager;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.components.NpcString;

/**
 * @author pchayka
 */
public class NevitHeraldManager
{
	private static final String spawngroup = "nevitt_herald_group";

	private static NevitHeraldManager _instance = new NevitHeraldManager();
	private static boolean _spawned = false;

	public static NevitHeraldManager getInstance()
	{
		return _instance;
	}

	public void doSpawn(int bossId)
	{
		NpcString ns = null;
		if (bossId == 29068)
			ns = NpcString.ANTHARAS_THE_EVIL_LAND_DRAGON_ANTHARAS_DEFEATED;
		else if (bossId == 29028)
			ns = NpcString.VALAKAS_THE_EVIL_FIRE_DRAGON_VALAKAS_DEFEATED;
		
		if (ns != null)
			for (Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				if (player == null || player.isInOfflineMode())
					continue;
				
				player.sendPacket(new ExShowScreenMessage(ns, 8000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
			}

		if(_spawned)
			return;
		
		_spawned = true;
		SpawnManager.getInstance().spawn(spawngroup);
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				SpawnManager.getInstance().despawn(spawngroup);
				_spawned = false;
			}
		}, 3 * 3600 * 1000L); // 3 hours
	}

}
