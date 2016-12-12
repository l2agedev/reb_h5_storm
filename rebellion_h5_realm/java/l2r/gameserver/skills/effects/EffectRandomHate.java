package l2r.gameserver.skills.effects;

import l2r.commons.util.Rnd;
import l2r.gameserver.model.AggroList;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.stats.Env;

import java.util.List;

public class EffectRandomHate extends Effect
{
	public EffectRandomHate(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		return getEffected().isMonster() && Rnd.chance(_template.chance(100));
	}

	@Override
	public void onStart()
	{
		MonsterInstance monster = (MonsterInstance)getEffected();
		Creature mostHated = monster.getAggroList().getMostHated();
		if(mostHated == null)
			return;

		AggroList.AggroInfo mostAggroInfo = monster.getAggroList().get(mostHated);
		List<Creature> hateList = monster.getAggroList().getHateList();
		hateList.remove(mostHated);

		if(!hateList.isEmpty())
		{
			AggroList.AggroInfo newAggroInfo = monster.getAggroList().get(hateList.get(Rnd.get(hateList.size())));
			final int oldHate = newAggroInfo.hate;

			newAggroInfo.hate = mostAggroInfo.hate;
			mostAggroInfo.hate = oldHate;
		}
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}
