package l2r.gameserver.skills.skillclasses;
/*
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.stats.conditions.ConditionTargetRelation;
import l2r.gameserver.templates.StatsSet;

public class SelfSacrifice extends Skill
{
	private final int _effRadius;
	
	public SelfSacrifice(StatsSet set)
	{
		super(set);
		_effRadius = set.getInteger("effRadius", 1000);
		_lethal1 = set.getInteger("lethal1", 0);
		_lethal2 = set.getInteger("lethal2", 0);
	}
	
	// TODO: conditions find correct one..
	@Override
	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		List<Creature> result = new ArrayList<Creature>();
		List<Creature> targets = activeChar.getAroundCharacters(_effRadius, _effRadius);
		if (targets == null || targets.isEmpty() && ((Player) activeChar).getParty() == null)
			return result;
		
		for (Creature target : targets)
		{
			if (target != null && target.isPlayer() && !target.isAutoAttackable(activeChar))
			{
				if (target.isPlayer())
				{
					Player activeCharTarget = (Player) target;
					Player activeCharPlayer = (Player) activeChar;
					if ((activeCharTarget.isInDuel()) || activeCharTarget.isCursedWeaponEquipped()))
					{
					}
				}
				else
				{
					result.add(target);
				}
			}
		}
		return result;
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for (Creature target : targets)
		{
			if (target != null)
			{
				if (getSkillType() != Skill.SkillType.BUFF || target == activeChar || !target.isCursedWeaponEquipped() && !activeChar.isCursedWeaponEquipped())
				{
					boolean reflected = target.checkReflectSkill(activeChar, this);
					getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
				}
			}
		}
		
		if (isSSPossible() && !Config.SAVING_SPS || _skillType != Skill.SkillType.SELF_SACRIFICE)
			activeChar.unChargeShots(isMagic());
	}
}
*/