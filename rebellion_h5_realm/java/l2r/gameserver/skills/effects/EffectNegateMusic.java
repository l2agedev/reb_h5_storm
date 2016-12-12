package l2r.gameserver.skills.effects;

import l2r.gameserver.model.CancelSystem;
import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;

public class EffectNegateMusic extends Effect
{
	public EffectNegateMusic(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
		for(Effect e : _effected.getEffectList().getAllEffects())
			if(e.getSkill().isMusic())
			{
				CancelSystem.getInstance().onCancel(_effected, e);
				e.exit();
			}
		return false;
	}
}