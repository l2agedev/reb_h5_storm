package ai.dragonvalley;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;

public class BloodyMonster extends Fighter
{
	public BloodyMonster(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		if(Rnd.get(30) < 1) // Infern0 original 15
		{
			for(int i = 0; i < Rnd.get(3, 5); i++)
			{
				NpcInstance n = SpawnTable.spawnSingle(getActor().getNpcId(), getActor().getLoc().coordsRandomize(100, 250));
				n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 1000);
			}
		}
	}
}