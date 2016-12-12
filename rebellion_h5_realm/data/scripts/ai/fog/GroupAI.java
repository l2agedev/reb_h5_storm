package ai.fog;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;

import org.apache.commons.lang3.ArrayUtils;

public class GroupAI extends Fighter
{
	private static final int[] RANDOM_SPAWN_MOBS =
	{
		18799,
		18800,
		18801,
		18802,
		18803
	};
	private static final int[] FOG_MOBS =
	{
		22634,
		22635,
		22636,
		22637,
		22638,
		22639,
		22640,
		22641,
		22642,
		22643,
		22644,
		22645,
		22646,
		22647,
		22648,
		22649
	};
	
	public GroupAI(NpcInstance actor)
	{
		super(actor);
		
		if (ArrayUtils.contains(RANDOM_SPAWN_MOBS, actor.getNpcId()))
			actor.startImmobilized();
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		
		if (ArrayUtils.contains(FOG_MOBS, actor.getNpcId()) && Rnd.chance(20))
			try
			{
				NpcInstance npc = SpawnTable.spawnSingle(RANDOM_SPAWN_MOBS[Rnd.get(RANDOM_SPAWN_MOBS.length)], actor.getLoc(), actor.getReflection(), 300000L);
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Rnd.get(1, 100));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		
		super.onEvtDead(killer);
	}
	
}