package ai.dragonvalley;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;

public class DragonKnight extends Fighter
{
	public DragonKnight(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		switch(getActor().getNpcId())
		{
			case 22844:
				if(Rnd.chance(35)) // Infern0 original 50
				{
					NpcInstance n = SpawnTable.spawnSingle(22845, getActor().getLoc());
					n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 2);
				}
				break;
			case 22845:
				if(Rnd.chance(35)) // Infern0 original 50
				{
					NpcInstance n = SpawnTable.spawnSingle(22846, getActor().getLoc());
					n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 2);
				}
				break;
		}

	}
}