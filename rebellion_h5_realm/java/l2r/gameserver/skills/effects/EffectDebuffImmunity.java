package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;

public final class EffectDebuffImmunity extends Effect
{
	public EffectDebuffImmunity(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startDebuffImmunity();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopDebuffImmunity();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}