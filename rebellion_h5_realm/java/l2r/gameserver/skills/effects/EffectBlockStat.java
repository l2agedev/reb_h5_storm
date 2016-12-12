package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.skills.skillclasses.NegateStats;
import l2r.gameserver.stats.Env;

public class EffectBlockStat extends Effect
{
	public EffectBlockStat(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.addBlockStats(((NegateStats) _skill).getNegateStats());
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.removeBlockStats(((NegateStats) _skill).getNegateStats());
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}