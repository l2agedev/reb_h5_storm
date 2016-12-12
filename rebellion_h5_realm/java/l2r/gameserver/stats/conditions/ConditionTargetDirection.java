package l2r.gameserver.stats.conditions;

import l2r.gameserver.stats.Env;
import l2r.gameserver.utils.Location;

public class ConditionTargetDirection extends Condition
{
	private final Location.TargetDirection _dir;

	public ConditionTargetDirection(Location.TargetDirection direction)
	{
		_dir = direction;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return Location.getDirectionTo(env.target, env.character) == _dir;
	}
}
