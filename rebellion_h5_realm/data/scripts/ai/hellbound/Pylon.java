package ai.hellbound;

import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.utils.Location;

public class Pylon extends Fighter
{
	public Pylon(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		NpcInstance actor = getActor();
		for(int i = 0; i < 7; i++)
			SpawnTable.spawnSingle(22422, Location.findPointToStay(actor, 150, 550));
	}
}