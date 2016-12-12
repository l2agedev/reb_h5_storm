package ai.freya;

import l2r.commons.util.Rnd;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;

import java.util.HashMap;
import java.util.Map;

public class EnergySeed extends DefaultAI
{
	private static final Map<String, Integer> zoneNpc = new HashMap<String, Integer>();
	static
	{
		zoneNpc.put("[13_23_cocracon]", 22761);
		zoneNpc.put("[14_23_raptilicon]", 22755);
		zoneNpc.put("[14_23_beastacon]", 22747);
	}

	public EnergySeed(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();

		// In the SoA gathered seed can spawn a mob
		for(String s : zoneNpc.keySet())
			if(actor.isInZone(s) && Rnd.chance(30))
			{
				NpcInstance npc = SpawnTable.spawnSingle(zoneNpc.get(s), actor.getLoc(), getActor().getReflection());
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 2000);
			}
		super.onEvtDead(killer);
	}
}