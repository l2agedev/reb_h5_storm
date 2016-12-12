package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;

public final class EffectSalvation extends Effect
{
	public EffectSalvation(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isBoss() || _effected.isRaid())
			return false;
			
		return getEffected().isPlayer() && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		getEffected().setIsSalvation(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().setIsSalvation(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}