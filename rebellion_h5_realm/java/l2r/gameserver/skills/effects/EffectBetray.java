package l2r.gameserver.skills.effects;

import static l2r.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Summon;
import l2r.gameserver.stats.Env;

public class EffectBetray extends Effect
{
	public EffectBetray(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected != null && _effected.isSummon())
		{
			Summon summon = (Summon) _effected;
			summon.setDepressed(true);
			summon.getAI().Attack(summon.getPlayer(), true, false);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected != null && _effected.isSummon())
		{
			Summon summon = (Summon) _effected;
			summon.setDepressed(false);
			summon.getAI().setIntention(AI_INTENTION_ACTIVE);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}