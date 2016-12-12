package l2r.gameserver.stats.conditions;

import l2r.gameserver.model.Creature;
import l2r.gameserver.stats.Env;

import gnu.trove.set.hash.TIntHashSet;

public class ConditionTargetForbiddenClassId extends Condition
{
	private TIntHashSet _classIds = new TIntHashSet();

	public ConditionTargetForbiddenClassId(String[] ids)
	{
		for(String id : ids)
			_classIds.add(Integer.parseInt(id));
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		if(!target.isPlayable())
			return false;
		return !target.isPlayer() || !_classIds.contains(target.getPlayer().getActiveClassId());
	}
}