package l2r.gameserver.stats.conditions;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.stats.Env;

public class ConditionTargetPlayerRace extends Condition
{
	private final Race _race;

	public ConditionTargetPlayerRace(String race)
	{
		_race = Race.valueOf(race.toLowerCase());
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		return target != null && target.isPlayer() && _race == ((Player) target).getRace();
	}
}