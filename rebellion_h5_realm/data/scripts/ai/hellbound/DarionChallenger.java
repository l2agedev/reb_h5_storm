package ai.hellbound;

import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.utils.Location;

public class DarionChallenger extends Fighter
{
	private static final int TeleportCube = 32467;

	public DarionChallenger(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(checkAllDestroyed())
			SpawnTable.spawnSingle(TeleportCube, new Location(-12527, 279714, -11622, 16384), 600000L);

		super.onEvtDead(killer);
	}

	private static boolean checkAllDestroyed()
	{
		if(!GameObjectsStorage.getAllByNpcId(25600, true).isEmpty())
			return false;
		if(!GameObjectsStorage.getAllByNpcId(25601, true).isEmpty())
			return false;
		if(!GameObjectsStorage.getAllByNpcId(25602, true).isEmpty())
			return false;

		return true;
	}
} 