package l2r.gameserver.stats.conditions;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.stats.Env;

public final class ConditionTargetHasBuffRemainingTime extends Condition
{
	private final EffectType _effectType;
	private final int _level;
	private final int _remainingTime;

	public ConditionTargetHasBuffRemainingTime(EffectType effectType, int level, int remainingTimeSeconds)
	{
		_effectType = effectType;
		_level = level;
		_remainingTime = remainingTimeSeconds;
	}
	
	public ConditionTargetHasBuffRemainingTime(EffectType effectType, int remainingTimeSeconds)
	{
		_effectType = effectType;
		_level = 1;
		_remainingTime = remainingTimeSeconds;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		if(target == null)
			return false;
		Effect effect = target.getEffectList().getEffectByType(_effectType);
		if(effect == null)
			return false;
		if((_level == -1 || effect.getSkill().getLevel() >= _level) && effect.getTimeLeft() >= _remainingTime)
			return true;
		return false;
	}
}
