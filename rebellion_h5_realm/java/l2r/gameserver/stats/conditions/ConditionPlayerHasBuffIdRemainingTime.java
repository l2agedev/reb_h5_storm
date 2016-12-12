package l2r.gameserver.stats.conditions;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;

import java.util.List;


public class ConditionPlayerHasBuffIdRemainingTime extends Condition
{
	private final int _id;
	private final int _level;
	private final int _remainingTime;

	public ConditionPlayerHasBuffIdRemainingTime(int id, int level, int remainingTimeSeconds)
	{
		_id = id;
		_level = level;
		_remainingTime = remainingTimeSeconds;
	}
	
	public ConditionPlayerHasBuffIdRemainingTime(int id, int remainingTimeSeconds)
	{
		_id = id;
		_level = 1;
		_remainingTime = remainingTimeSeconds;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature character = env.character;
		if(character == null)
			return false;
		if(_level == -1)
			return character.getEffectList().getEffectsBySkillId(_id) != null;
		List<Effect> el = character.getEffectList().getEffectsBySkillId(_id);
		if(el == null)
			return false;
		for(Effect effect : el)
			if(effect != null && effect.getSkill().getLevel() >= _level && effect.getTimeLeft() >= _remainingTime)
				return true;
		return false;
	}
}