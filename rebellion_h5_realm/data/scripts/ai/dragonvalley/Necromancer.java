package ai.dragonvalley;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Mystic;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;

public class Necromancer extends Mystic
{
	public Necromancer(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		if(Rnd.chance(15)) // Infern0 original 30
		{
			NpcInstance n = SpawnTable.spawnSingle(Rnd.chance(50) ? 22818 : 22819, getActor().getLoc());
			n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 2);
		}
	}
}