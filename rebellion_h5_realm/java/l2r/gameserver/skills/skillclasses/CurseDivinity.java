package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Skill;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class CurseDivinity extends Skill
{
	public CurseDivinity(StatsSet set)
	{
		super(set);
		_power = set.getInteger("power", 1);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int sps = isSSPossible() ? (isMagic() ? activeChar.getChargedSpiritShot() : activeChar.getChargedSoulShot() ? 2 : 0) : 0;

		Creature realTarget;
		boolean reflected;

		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;

				//if(target.getPlayer() == null)
				//	continue;
				
				reflected = target.checkReflectSkill(activeChar, this);
				realTarget = reflected ? activeChar : target;

				List<Effect> effectsList = target.getEffectList().getAllEffects();
				int buffCount = effectsList.size();
				double damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
				if(damage >= 1)
				{
					damage = damage + (_power * 0.1 + _power * 0.127 * (buffCount / 3.5)); // (buffCount / 3.5) is custom balance.
					realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
				}
					

				getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}

		if(isSuicideAttack())
			activeChar.doDie(null);
		else if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}