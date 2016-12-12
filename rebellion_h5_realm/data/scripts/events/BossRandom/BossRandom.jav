package events.BossRandom;

import l2p.gameserver.Announcements;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.instancemanager.SpawnManager;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.utils.Location;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class BossRandom extends Functions implements ScriptFile {

    private static boolean isActiveBossRandom = true;
    private static final int BossId = 91107;
    private static NpcInstance _boss;
    private static final int BossEventInterval = 7200000; // 60*60*1000
    private static ArrayList<SpawnManager> _spawns = new ArrayList<SpawnManager>();
	public static Logger _log = Logger.getLogger(BossRandom.class);

	private static void spawnBoss()
	{
        _boss = spawn(new Location(20168, -15336, -3109, -3109), BossId);
    }

    public static void OnDie(Creature self, Creature killer)
	{
        if (self.getNpcId() == getBossId()) {
            Announcements.getInstance().announceToAll(self.getName() + " defeated, player " + killer.getName() + " made final hit!");
            Announcements.getInstance().announceToAll("Peace zone in the island cancelled.");
            ThreadPoolManager.getInstance().schedule(new spawnBossShedule(), BossEventInterval);
            ZoneManager.getInstance().getZoneById(Zone.ZoneType.peace_zone, 520100, false).setActive(false);
        }
    }

    private static class spawnBossShedule implements Runnable
	{
        @Override
        public void run()
		{

            spawnBoss();
            String nearestTown = TownManager.getInstance().getClosestTownName(_boss);
            Announcements.getInstance().announceToAll(_boss.getName() + " appeared in " + nearestTown + "!");
            Announcements.getInstance().announceToAll("Part of the land on the island was peaceful.");
            ZoneManager.getInstance().getZoneById(Zone.ZoneType.peace_zone, 520100, false).setActive(true);

        }
    }

    public static int getBossId()
	{
        return BossId;
    }

    public void onLoad()
	{
        if (NpcHolder.getInstance().getTemplate(BossId) == null)
		{
            isActiveBossRandom = false;
        }
        if (isActiveBossRandom)
		{
            spawnBoss();
            _log.info("Loaded Event: Отшельник Вася");
        }
    }

    @Override
    public void onReload()
	{
    }

    @Override
    public void onShutdown()
	{
    }
}
