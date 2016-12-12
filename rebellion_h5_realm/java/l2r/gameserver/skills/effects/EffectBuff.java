package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;

public final class EffectBuff extends Effect
{
	public EffectBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}