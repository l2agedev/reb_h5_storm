package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;

public final class EffectBuffImmunity extends Effect
{
	public EffectBuffImmunity(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startBuffImmunity();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopBuffImmunity();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
