package ai.hellbound;

import l2r.gameserver.ai.Fighter;
import l2r.gameserver.instancemanager.naia.NaiaCoreManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;

public class Epidos extends Fighter
{

	public Epidos(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NaiaCoreManager.removeSporesAndSpawnCube();
		super.onEvtDead(killer);
	}
}