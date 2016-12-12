package ai.hellbound;

import l2r.gameserver.ai.Mystic;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.instances.NpcInstance;

public class BelethClone extends Mystic
{
	public BelethClone(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	public boolean canSeeInSilentMove(Playable target)
	{
		return true;
	}

	@Override
	public boolean canSeeInHide(Playable target)
	{
		return true;
	}

	@Override
	public void addTaskAttack(Creature target)
	{
		return;
	}

}