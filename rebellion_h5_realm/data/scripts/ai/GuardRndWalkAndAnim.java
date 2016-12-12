package ai;

import l2r.gameserver.ai.Guard;
import l2r.gameserver.model.instances.NpcInstance;

public class GuardRndWalkAndAnim extends Guard
{
	public GuardRndWalkAndAnim(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		if(super.thinkActive())
			return true;

		if(randomAnimation())
			return true;

		if(randomWalk())
			return true;

		return false;
	}
}