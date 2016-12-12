package npc.model;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.instances.BossInstance;
import l2r.gameserver.model.instances.MinionInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;


public class QueenAntInstance extends BossInstance
{
	private static final int QUEN_ANT = 29001;
	private static final int QUUEN_ANT_LARVA = 29002;
	
	private List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();
	private NpcInstance Larva = null;

	public QueenAntInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	public NpcInstance getLarva()
	{
		if(Larva == null)
			Larva = SpawnNPC(QUUEN_ANT_LARVA, new Location(-21600, 179482, -5846, Rnd.get(0, 65535)));
		return Larva;
	}

	@Override
	protected int getKilledInterval(MinionInstance minion)
	{
		return minion.getNpcId() == 29003 ? 10000 : 280000 + Rnd.get(40000);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		ServerVariables.set("QueenAntDeath", System.currentTimeMillis());
		
		if (Config.ENABLE_PLAYER_COUNTERS && getNpcId() == QUEN_ANT)
		{
			if (killer != null && killer.getPlayer() != null)
			{
				for (Player member : killer.getPlayer().getPlayerGroup())
				{
					if (member != null && Location.checkIfInRange(3000, member, killer, false))
						member.getCounters().addPoint("_Ant_Queen_Killed");
				}
			}
		}
		
		broadcastPacketToOthers(new PlaySound(PlaySound.Type.MUSIC, "BS02_D", 1, 0, getLoc()));
		Functions.deSpawnNPCs(_spawns);
		Larva = null;
		super.onDeath(killer);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		getLarva();
		broadcastPacketToOthers(new PlaySound(PlaySound.Type.MUSIC, "BS01_A", 1, 0, getLoc()));
	}

	private NpcInstance SpawnNPC(int npcId, Location loc)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! template is null for npc: " + npcId);
			Thread.dumpStack();
			return null;
		}
		try
		{
			SimpleSpawner sp = new SimpleSpawner(template);
			sp.setLoc(loc);
			sp.setAmount(1);
			sp.setRespawnDelay(0);
			_spawns.add(sp);
			return sp.spawnOne();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}