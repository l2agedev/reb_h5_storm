package l2r.gameserver.skills.effects;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;

public final class EffectRemoveTarget extends Effect
{
	private boolean _doStopTarget;

	public EffectRemoveTarget(Env env, EffectTemplate template)
	{
		super(env, template);
		_doStopTarget = template.getParam().getBool("doStopTarget", false);
	}

	@Override
	public boolean checkCondition()
	{
		return Rnd.chance(_template.chance(100));
	}

	@Override
	public void onStart()
	{
		if(getEffected().getAI().isDefaultAI())
			((DefaultAI) getEffected().getAI()).setGlobalAggro(System.currentTimeMillis() + 3000L);

		getEffected().setTarget(null);
		if(_doStopTarget)
			getEffected().stopMove();
		
		if (!getEffected().isRaid() || !getEffected().isBoss())
		{
			getEffected().abortAttack(true, true);
			getEffected().abortCast(true, true);
		}
		
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, getEffector());
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}