package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Summon;
import l2r.gameserver.stats.Env;

public final class EffectDestroySummon extends Effect
{
	public EffectDestroySummon(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(!_effected.isSummon())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if (_effected.isSummon())
			((Summon) _effected).unSummon();
	}

	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}