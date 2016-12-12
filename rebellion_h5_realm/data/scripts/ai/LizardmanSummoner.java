package ai;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Mystic;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.utils.Location;

/** Author: Bonux
	При ударе монстра спавнятся 2 х Tanta Lizardman Scout и они агрятся на игрока.
**/
public class LizardmanSummoner extends Mystic
{
	private final int TANTA_LIZARDMAN_SCOUT = 22768;
	private final int SPAWN_COUNT = 2;
	private boolean spawnedMobs = false;

	public LizardmanSummoner(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		spawnedMobs = false;
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(!spawnedMobs && attacker.isPlayable())
		{
			NpcInstance actor = getActor();
			for(int i = 0; i < SPAWN_COUNT; i++)
			{
				try
				{
					NpcInstance npc = SpawnTable.spawnSingle(TANTA_LIZARDMAN_SCOUT, actor.getLoc());
					npc.setHeading(Location.calculateHeadingFrom(npc, attacker));
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 1000);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			spawnedMobs = true;
		}
		super.onEvtAttacked(attacker, damage);
	}
}
