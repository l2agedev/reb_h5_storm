package ai.dragonvalley;

import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.instances.NpcInstance;

public class DragonRaid extends Fighter
{
	private long lastAttackTime = 0;
	private NpcInstance actor = getActor();

	public DragonRaid(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		lastAttackTime = System.currentTimeMillis();
	}

	@Override
	protected boolean thinkActive()
	{
		super.thinkActive();
		if(lastAttackTime != 0)
		{
			if(lastAttackTime + 5 * 60 * 1000L < System.currentTimeMillis())
				if(actor.getAggroRange() == 0)
					actor.setAggroRange(400);
			if(lastAttackTime + 30 * 60 * 1000L < System.currentTimeMillis())
				getActor().deleteMe();
		}
		return true;
	}
}