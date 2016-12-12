package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.SummonInstance;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class Drain extends Skill
{
	private double _absorbAbs;

	public Drain(StatsSet set)
	{
		super(set);
		_absorbAbs = set.getDouble("absorbAbs", 0.f);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		boolean ss = isSSPossible() && activeChar.getChargedSoulShot();
		Creature realTarget;
		boolean reflected;
		final boolean corpseSkill = _targetType == SkillTargetType.TARGET_CORPSE;

		for(Creature target : targets)
			if(target != null)
			{
				reflected = !corpseSkill && target.checkReflectSkill(activeChar, this);
				realTarget = reflected ? activeChar : target;

				if(getPower() > 0 || _absorbAbs > 0) // Если == 0 значит скилл "отключен"
				{
					if(realTarget.isDead() && !corpseSkill)
						continue;

					double hp = 0.;
					double targetHp = realTarget.getCurrentHp();

					if(!corpseSkill)
					{
						double damage = isMagic() ? Formulas.calcMagicDam(activeChar, realTarget, this, sps) : Formulas.calcPhysDam(activeChar, realTarget, this, false, false, ss, false).damage;
						double targetCP = realTarget.getCurrentCp();

						// Нельзя восстанавливать HP из CP
						if(damage > targetCP || !realTarget.isPlayer())
							hp = (damage - targetCP) * _absorbPart;

						realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
						if(!reflected)
							realTarget.doCounterAttack(this, activeChar, false);
					}

					if(_absorbAbs == 0 && _absorbPart == 0)
						continue;

					hp += _absorbAbs;

					// Нельзя восстановить больше hp, чем есть у цели.
					if(hp > targetHp && !corpseSkill)
						hp = targetHp;

					double addToHp = Math.max(0, Math.min(hp, activeChar.calcStat(Stats.HP_LIMIT, null, null) * activeChar.getMaxHp() / 100. - activeChar.getCurrentHp()));

					if(addToHp > 0 && !target.isDoor() && !activeChar.isHealBlocked())
						activeChar.setCurrentHp(activeChar.getCurrentHp() + addToHp, false);

					if(realTarget.isDead() && corpseSkill)
					{
						if (realTarget.isNpc())
							((NpcInstance) realTarget).endDecayTask();
						else if (realTarget.isSummon())
							((SummonInstance) realTarget).endDecayTask();
						activeChar.getAI().setAttackTarget(null);
					}
				}

				getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}

		if(isMagic() ? sps != 0 : ss)
			activeChar.unChargeShots(isMagic());
	}
}