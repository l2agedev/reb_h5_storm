package l2r.gameserver.stats;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Skill.SkillMagicType;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.model.base.BaseStats;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.base.SkillTrait;
import l2r.gameserver.model.instances.ReflectionBossInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.templates.item.WeaponTemplate;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

public class Formulas
{
	public static double calcHpRegen(Creature cha)
	{
		double init;
		if(cha.isPlayer())
			init = (cha.getLevel() <= 10 ? 1.5 + cha.getLevel() / 20. : 1.4 + cha.getLevel() / 10.) * cha.getLevelMod();
		else
			init = cha.getTemplate().getBaseHpReg();

		if(cha.isPlayable())
		{
			init *= BaseStats.CON.calcBonus(cha);
			if(cha.isSummon())
				init *= 2;
		}
		else if (cha.getChampionTemplate() != null)
			init *= cha.getChampionTemplate().hpRegenMultiplier;

		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
	}

	public static double calcMpRegen(Creature cha)
	{
		double init;
		if(cha.isPlayer())
			init = (.87 + cha.getLevel() * .03) * cha.getLevelMod();
		else
			init = cha.getTemplate().getBaseMpReg();

		if(cha.isPlayable())
		{
			init *= BaseStats.MEN.calcBonus(cha);
			if(cha.isSummon())
				init *= 2;
		}

		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null);
	}

	public static double calcCpRegen(Creature cha)
	{
		double init = (1.5 + cha.getLevel() / 10) * cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		double cpRegenMultiplier = 1;
		if (cha.isPlayer())
		{
			Player player = cha.getPlayer();
			
			// Calculate Movement bonus
			if (player.isSitting())
			{
				cpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}
		else
		{
			// Calculate Movement bonus
			if (!cha.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if (cha.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}
		
		return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier;
	}

	public static class AttackInfo
	{
		public double damage = 0;
		public double defence = 0;
		public double crit_static = 0;
		public double death_rcpt = 0;
		public double lethal1 = 0;
		public double lethal2 = 0;
		public double lethal_dmg = 0;
		public boolean crit = false;
		public boolean shld = false;
		public boolean lethal = false;
		public boolean miss = false;
	}

	/**
	 * Для простых ударов
	 * patk = patk
	 * При крите простым ударом:
	 * patk = patk * (1 + crit_damage_rcpt) * crit_damage_mod + crit_damage_static
	 * Для blow скиллов
	 * TODO
	 * Для скилловых критов, повреждения просто удваиваются, бафы не влияют (кроме blow, для них выше)
	 * patk = (1 + crit_damage_rcpt) * (patk + skill_power)
	 * Для обычных атак
	 * damage = patk * ss_bonus * 70 / pdef
	 */
	public static AttackInfo calcPhysDam(Creature attacker, Creature target, Skill skill, boolean dual, boolean blow, boolean ss, boolean onCrit)
	{
		if (!Config.PDAM_OLD_FORMULA)
			return calcPhysDamNew(attacker, target, skill, dual, blow, ss, onCrit);
		
		AttackInfo info = new AttackInfo();

		info.damage = attacker.getPAtk(target);
		info.defence = target.getPDef(attacker);
		info.crit_static = attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
		info.death_rcpt = 0.01 * target.calcStat(Stats.DEATH_VULNERABILITY, attacker, skill);
		info.lethal1 = skill == null ? 0 : skill.getLethal1() * info.death_rcpt;
		info.lethal2 = skill == null ? 0 : skill.getLethal2() * info.death_rcpt;
		info.crit = Rnd.chance(calcCrit(attacker, target, skill, blow));
		info.shld = (skill == null || !skill.getShieldIgnore()) && Formulas.calcShldUse(attacker, target);
		info.lethal = false;
		info.miss = false;
		
		// lets not give to gladiators
		if (skill != null && !skill.isChargeBoost())
			info.damage = Math.pow(info.damage, Config.PHYS_SKILLS_DAMAGE_POW);
		
		if (attacker.isSummon())
			info.damage *= Config.ALT_PET_PVP_DAMAGE_MODIFIER;
		
		boolean isPvP = attacker.isPlayable() && target.isPlayable();

		if(info.shld)
			info.defence += target.getShldDef();

		info.defence = Math.max(info.defence, 1);

		if(skill != null)
		{
			if(!blow && !target.isLethalImmune() && target.getLevel() - skill.getMagicLevel() <= 5) // TODO Infern0 confirm
				if(Rnd.chance(info.lethal1))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentCp();
						target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
					}
					else
						info.lethal_dmg = target.getCurrentHp() / 2;
					attacker.sendPacket(SystemMsg.CP_SIPHON);
				}
				else if(Rnd.chance(info.lethal2))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentHp() + target.getCurrentCp() - 1.1; // Oly\Duel хак установки не точно 1 ХП, а чуть больше для предотвращения псевдосмерти
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					}
					else
						info.lethal_dmg = target.getCurrentHp() - 1;
					attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}

			// если скилл не имеет своей силы дальше идти бесполезно, можно сразу вернуть дамаг от летала
			if(skill.getPower(target) == 0)
			{
				info.damage = 0; // обычного дамага в этом случае не наносится
				return info;
			}

			if (blow && !skill.isBehind() && ss) // Для обычных blow не влияет на power
				info.damage *= Config.NON_BACK_BLOW_MULTIPLIER; // 2.04
			
			info.damage += Math.max(0., skill.getPower(target) * attacker.calcStat(Stats.SKILL_POWER, 1, null, null));

			if (blow && skill.isBehind() && ss) // Для обычных blow не влияет на power
				info.damage *= Config.BACK_BLOW_MULTIPLIER; // 1.5
			
			//Заряжаемые скилы имеют постоянный урон
			if(!skill.isChargeBoost())
				info.damage *= 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;

			if(blow)
			{
				Functions.sendDebugMessage(target, "- Blow dmg w/o modification  " + info.damage);
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				
				Functions.sendDebugMessage(target, "- Blow critical dmg modifier  " + 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill));
				info.damage = target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				
				Functions.sendDebugMessage(target, "- Dmg w/o crit static dmg modification  " + info.damage + " crit static dmg modifier " + info.crit_static);
				info.damage += 6.1 * info.crit_static;
				
				Functions.sendDebugMessage(target, "- Dmg with critical static dmg modifier modification " + info.damage + " crit static modifier " + info.crit_static);
				
				/*
				// custom frontal/side damage nerf to balance daggers
				switch(Location.getDirectionTo(target, attacker))
				{
					case SIDE:
						info.damage *= 0.9;
						break;
					case FRONT:
						info.damage *= 0.7;
						break;
				}
				*/ 
				Functions.sendDebugMessage(target, "- Blow dmg after modification  " + info.damage);
			}

			if(skill.isChargeBoost())
				info.damage *= Config.SKILL_FORCE_H5_FORMULA ? (1.0 + 0.25 * attacker.getIncreasedForce()) : (0.8 + 0.2 * attacker.getIncreasedForce());
			else if(skill.isSoulBoost())
				info.damage *= 1.0 + 0.06 * Math.min(attacker.getConsumedSouls(), 5);
			
			// Gracia Physical Skill Damage Bonus
			info.damage *= 1.10113;

			if (info.crit)
			{
				info.damage *= 2.;
				Functions.sendDebugMessage(target, "- Dmg after crit modification  " + info.damage);
			}
		}
		else
		{
			info.damage *= 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;

			if(dual)
				info.damage /= 2.;

			if(info.crit)
			{
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage = 2 * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				info.damage += info.crit_static;
			}
		}

		if(info.crit)
		{
			// шанс абсорбации души (без анимации) при крите, если Soul Mastery 4го уровня или более
			int chance = attacker.getSkillLevel(Skill.SKILL_SOUL_MASTERY);
			if(chance > 0)
			{
				if(chance >= 21)
					chance = 30;
				else if(chance >= 15)
					chance = 25;
				else if(chance >= 9)
					chance = 20;
				else if(chance >= 4)
					chance = 15;
				if(Rnd.chance(chance))
					attacker.setConsumedSouls(attacker.getConsumedSouls() + 1, null);
			}
		}

		// Do not multiply behind or side dmg for chargeBoost skills...
		if (skill == null || !skill.isChargeBoost()) // TODO Infern0 confirm
			switch (Location.getDirectionTo(target, attacker))
			{
				case BEHIND:
					info.damage *= 1.2;
					break;
				case SIDE:
					info.damage *= 1.1;
					break;
			}

		if(ss && !blow)
			info.damage *= 2.0;

		info.damage *= 70. / info.defence;
		info.damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, info.damage, target, skill);

		if(info.shld && Rnd.chance(5))
			info.damage = 1;

		if(isPvP)
		{
			if(skill == null)
			{
				info.damage *= attacker.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1, null, null);
				info.damage /= target.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1, null, null);
			}
			else
			{
				info.damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1, null, null);
				info.damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1, null, null);
			}
		}

		// Тут проверяем только если skill != null, т.к. L2Character.onHitTimer не обсчитывает дамаг.
		if(skill != null)
		{
			if(info.shld)
				if(info.damage == 1)
					target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				else
					target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);

			// Уворот от физ скилов уводит атаку в 0
			// TODO: confirm if(info.damage > 1 && !skill.hasEffects() && Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0, attacker, skill)))
			if(info.damage > 1 && !skill.hasNotSelfEffects() && Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0, attacker, skill)))
			{
				attacker.sendPacket(new SystemMessage2(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
				target.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(attacker));
				info.damage = 0;
			}

			if(info.damage > 1 && skill.isDeathlink())
				info.damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());

			if(onCrit && !calcBlow(attacker, target, skill))
			{
				info.miss = true;
				info.damage = 0;
				attacker.sendPacket(new SystemMessage2(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
			}

			if(blow)
				if(info.lethal1 > 0 && Rnd.chance(info.lethal1))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentCp();
						target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
					}
					else if(target.isLethalImmune())
						info.damage *= 2;
					else
						info.lethal_dmg = target.getCurrentHp() / 2;
					attacker.sendPacket(SystemMsg.CP_SIPHON);
				}
				else if(info.lethal2 > 0 && Rnd.chance(info.lethal2))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentHp() + target.getCurrentCp() - 1.1;
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					}
					else if(target.isLethalImmune())
						info.damage *= 3;
					else
						info.lethal_dmg = target.getCurrentHp() - 1;
					attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}

			if(info.damage > 0)
				attacker.displayGiveDamageMessage(target, (int) info.damage, info.crit || blow, false, false, false);

			if(target.isStunned() && calcStunBreak(info.crit))
				target.getEffectList().stopEffects(EffectType.Stun);

			if(target.getEffectList().getEffectsBySkillId(522) != null && calcRealTargetBreak(info.crit))
				target.getEffectList().stopEffect(522);
			
			if(calcCastBreak(target, info.crit))
				target.abortCast(false, true);
		}

		return info;
	}
	
	/**
	 * Under Reconstruction until tested and confirmed.
	 */
	private static AttackInfo calcPhysDamNew(Creature attacker, Creature target, Skill skill, boolean dual, boolean blow, boolean ss, boolean onCrit)
	{
		final boolean isPvP = attacker.isPlayable() && target.isPlayable();
		final double skillPower = skill == null ? 0 : Math.max(0, skill.getPower(target));
		final double randomDamage = 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;
		// TODO Infern0 confirm
		final boolean skillEvaded = skill == null ? false : (!skill.hasNotSelfEffects() && Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0, attacker, skill)));
		// final boolean skillEvaded = skill == null ? false : (!skill.hasEffects() && Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0, attacker, skill)));
		AttackInfo info = new AttackInfo();
		info.shld = (skill == null || !skill.getShieldIgnore()) && Formulas.calcShldUse(attacker, target);
		info.damage = attacker.getPAtk(target) + skillPower;
		info.defence = Math.max(info.shld ? (target.getPDef(attacker) + target.getShldDef()) : target.getPDef(attacker), 1);
		info.crit_static = attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
		info.death_rcpt = 0.01 * target.calcStat(Stats.DEATH_VULNERABILITY, attacker, skill);
		info.lethal1 = skill == null ? 0 : skill.getLethal1() * info.death_rcpt;
		info.lethal2 = skill == null ? 0 : skill.getLethal2() * info.death_rcpt;
		final boolean lethal1 = info.lethal1 > 0 && Rnd.chance(info.lethal1);
		final boolean lethal2 = info.lethal2 > 0 && Rnd.chance(info.lethal2);
		info.lethal = lethal1 || lethal2;
		info.crit = Rnd.chance(calcCrit(attacker, target, skill, blow));
		info.miss = (blow && !calcBlow(attacker, target, skill)) || skillEvaded; // Miss for blow skills
		
		if (skill != null && !skill.isChargeBoost() && !blow) // Custom p.atk for skill damage boost.
			info.damage = Math.pow(info.damage, Config.PHYS_SKILLS_DAMAGE_POW);
		
		if(ss)
		{
			if (blow)
			{			
				if (skill != null && skill.isBehind()) info.damage *= Config.BACK_BLOW_MULTIPLIER; // 1.5
				else info.damage *= Config.NON_BACK_BLOW_MULTIPLIER; // 2.04
			}
			else
				info.damage *= 2.0;
		}
			
		
		// Lethal calculation first. Even skills with 0 power can calc lethal. Even perfect shield def cant stop lethal.
		if(info.lethal)
		{
			if (blow && target.isLethalImmune())
			{
				if (lethal1)
					info.damage *= 2;
				else
					info.damage *= 3;
				
				attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
			}
			else if (!target.isLethalImmune())
			{
				if (target.isPlayer())
				{
					info.lethal_dmg = lethal1 ? target.getCurrentCp() : (target.getCurrentHp() + target.getCurrentCp() - 1.1);
					if (lethal1)
						target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
					else
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					
					attacker.sendPacket(SystemMsg.CP_SIPHON);
				}
				else
				{
					info.lethal_dmg = lethal1 ? (target.getCurrentHp() / 2) : (target.getCurrentHp() - 1);
					attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
			}
		}
		
		// Blow skill failed or skill evaded. Nothing more to calculate.
		if(info.miss)
		{
			info.damage = 0;
			attacker.sendPacket(new SystemMessage2(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
			if (skillEvaded)
				target.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(attacker));
			return info;
		}
		
		// If perfect shield block, nothing more to calculate.
		if(info.shld && Rnd.chance(5))
		{
			info.damage = 1;
			target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
			return info;
		}
		
		if (attacker.isSummon())
			info.damage *= Config.ALT_PET_PVP_DAMAGE_MODIFIER;
		
		if(skill != null) // Skill damage
		{
			if(skillPower == 0)
			{
				info.damage = 0;
				return info;
			}
			
			info.damage *= 1.10113; // Gracia Physical Skill Damage Bonus
			if (!blow) // Copyleft said that tests on rpgclub showed that skill power boost skills do not boost dagger skills (Shining Edge + Final Secret)
			{
				info.damage *= attacker.calcStat(Stats.SKILL_POWER, 1, null, null);
				if (isPvP)
				{
					info.damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1, null, null);
					info.damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1, null, null);
				}
			}
			
			if(skill.isChargeBoost())
				info.damage *= Config.SKILL_FORCE_H5_FORMULA ? (1.0 + 0.25 * attacker.getIncreasedForce()) : (0.8 + 0.2 * attacker.getIncreasedForce());
			if(skill.isSoulBoost())
				info.damage *= 1.0 + 0.06 * Math.min(attacker.getConsumedSouls(), 5);
			if(skill.isDeathlink())
				info.damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());

			if(blow)
			{
				Functions.sendDebugMessage(target, "- Blow dmg w/o modification  " + info.damage);
				info.damage *= attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill) / 100;
				Functions.sendDebugMessage(target, "- Blow critical dmg modifier  " + attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill) / 100);
				info.damage = target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				Functions.sendDebugMessage(target, "- Dmg w/o crit static dmg modification  " + info.damage + " crit static dmg modifier " + info.crit_static);
				info.damage += 6.1 * info.crit_static; // TODO: Verify if is affected by ssboost
				Functions.sendDebugMessage(target, "- Dmg with critical static dmg modifier modification " + info.damage + " crit static modifier " + info.crit_static);
			}

			if (info.crit)
				info.damage *= 2.;
		}
		else // Normal attack damage.
		{
			if(dual)
				info.damage /= 2.;

			if(info.crit) // Crit formula: 2 * criticalPowerPercentMod / criticalResistPercentMod
			{
				info.damage *= attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill) / 100;
				info.damage = 2 * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				info.damage += info.crit_static;
			}
			
			if (isPvP)
			{
				info.damage *= attacker.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1, null, null);
				info.damage /= target.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1, null, null);
			}
		}
		
		// For autoattacks only until confirmed that works for skills.
		if (skill == null || !skill.isChargeBoost()) // TODO Infern0 confirm
			switch (Location.getDirectionTo(target, attacker))
			{
				case BEHIND:
					info.damage *= 1.2;
					break;
				case SIDE:
					info.damage *= 1.1;
					break;
			}
					
		info.damage *= 70. / info.defence;

		// Random damage comes at the end. Russians say that charge skills dont calc random... I dunno.
		if(skill == null || !skill.isChargeBoost())
			info.damage *= randomDamage;
		
		// Apply weapon and elemental resists.
		info.damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, info.damage, target, skill);

		if(info.damage > 0 && skill != null)
			attacker.displayGiveDamageMessage(target, (int) info.damage, info.crit || blow, false, info.shld, false);
		
		if(target.isStunned() && skill != null && calcStunBreak(info.crit)) // Stun break for normal attacks is handled elsewhere.
			target.getEffectList().stopEffects(EffectType.Stun);
		
		if(target.getEffectList().getEffectsBySkillId(522) != null && calcRealTargetBreak(info.crit))
			target.getEffectList().stopEffect(522);
		
		if(calcCastBreak(target, info.crit))
			target.abortCast(false, true);
		
		if(info.crit)
		{
			int chance = attacker.getSkillLevel(Skill.SKILL_SOUL_MASTERY);
			if(chance > 0)
			{
				if(chance >= 21)
					chance = 30;
				else if(chance >= 15)
					chance = 25;
				else if(chance >= 9)
					chance = 20;
				else if(chance >= 4)
					chance = 15;
				if(Rnd.chance(chance))
					attacker.setConsumedSouls(attacker.getConsumedSouls() + 1, null);
			}
		}

		return info;
	}

	public static double calcMagicDam(Creature attacker, Creature target, Skill skill, int sps)
	{
		if (attacker == null || target == null)
			return 0;
		
		boolean isPvP = attacker.isPlayable() && target.isPlayable();
		// Параметр ShieldIgnore для магических скиллов инвертирован
		boolean shield = skill.getShieldIgnore() && calcShldUse(attacker, target);

		int levelDiff = target.getLevel() - attacker.getLevel();

		double mAtk = attacker.getMAtk(target, skill);

		if(sps == 2)
			mAtk *= 4;
		else if(sps == 1)
			mAtk *= 2;

		double mdef = target.getMDef(null, skill);

		if(shield)
			mdef += target.getShldDef();
		if(mdef == 0)
			mdef = 1;

		double power = skill.getPower(target);
		boolean gradePenalty = attacker.isPlayer() && ((Player)attacker).getWeaponsExpertisePenalty() > 0;

		final SkillTrait trait = skill.getTraitType();
		if (trait != null)
		{
			final Env env = new Env(attacker, target, skill);
			double traitMul = 1. + (trait.calcProf(env) - trait.calcVuln(env)) / 100.;
			if (traitMul == Double.NEGATIVE_INFINITY) // invul
				return 0;
			else if (traitMul > 2.)
				traitMul = 2.;
			else if (traitMul < 0.05)
				traitMul = 0.05;

			power *= traitMul;
		}
		
		double lethalDamage = 0;

		if (!gradePenalty)
		{
			if(skill.getLethal1() > 0 && Rnd.chance(skill.getLethal1()))
			{
				if(target.isPlayer())
				{
					lethalDamage = target.getCurrentCp();
					target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
				}
				else if(!target.isLethalImmune())
					lethalDamage = target.getCurrentHp() / 2;
				else
					power *= 2;
					attacker.sendPacket(SystemMsg.CP_SIPHON);
			}
			else if(skill.getLethal2() > 0 && Rnd.chance(skill.getLethal2()))
			{
				if(levelDiff <= 9)
				{
					if(target.isPlayer())
					{
						lethalDamage = target.getCurrentHp() + target.getCurrentCp() - 1.1;
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					}
					else if(!target.isLethalImmune())
					{
						lethalDamage = target.getCurrentHp() - 1;
						attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
					else if (power > 0)
					{
						power *= 3;
						attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
					
				}
				else
				{
					lethalDamage = 0;
					power = 0;
				}
			}
		}

		if(power == 0)
		{
			if(lethalDamage > 0)
				attacker.displayGiveDamageMessage(target, (int) lethalDamage, false, false, false, false);
			return lethalDamage;
		}

		if(skill.isSoulBoost())
			power *= 1.0 + 0.06 * Math.min(attacker.getConsumedSouls(), 5);

		double damage = 91 * power * Math.sqrt(mAtk) / mdef;

		damage *= 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;

		boolean crit = calcMCrit(attacker.getMagicCriticalRate(target, skill));

		if(crit)
			damage *= attacker.calcStat(Stats.MCRITICAL_DAMAGE, attacker.isPlayable() && target.isPlayable() ? 2.5 : 3., target, skill);

		damage = attacker.calcStat(Stats.MAGIC_DAMAGE, damage, target, skill);

		if(shield)
		{
			if(Rnd.chance(5))
			{
				damage = 0;
				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				attacker.sendPacket(new SystemMessage2(SystemMsg.C1_RESISTED_C2S_MAGIC).addName(target).addName(attacker));
			}
			else
			{
				target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				attacker.sendPacket(new SystemMessage2(SystemMsg.YOUR_OPPONENT_HAS_RESISTANCE_TO_MAGIC_THE_DAMAGE_WAS_DECREASED));
			}
		}

		if(damage > 1 && skill.isDeathlink())
			damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());

		if(damage > 1 && skill.isBasedOnTargetDebuff())
			damage *= 1 + 0.035 * target.getEffectList().getAllEffects().size();

		damage += lethalDamage;

		if(skill.getSkillType() == SkillType.MANADAM)
			damage = Math.max(1, damage / 4.);

		if(isPvP && damage > 1)
		{
			damage *= attacker.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1, null, null);
			damage /= target.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1, null, null);
		}
		double magic_rcpt = target.calcStat(Stats.MAGIC_RESIST, attacker, skill) - attacker.calcStat(Stats.MAGIC_POWER, target, skill);
		double failChance = 4. * Math.max(1., levelDiff) * (1. + magic_rcpt / 100.);
		if(Rnd.chance(failChance))
		{
			if(levelDiff > 9)
			{
				damage = 0;
				SystemMessage msg = new SystemMessage(SystemMessage.C1_RESISTED_C2S_MAGIC).addName(target).addName(attacker);
				attacker.sendPacket(msg);
				target.sendPacket(msg);
			}
			else
			{
				damage /= 2;
				SystemMessage msg = new SystemMessage(SystemMessage.DAMAGE_IS_DECREASED_BECAUSE_C1_RESISTED_AGAINST_C2S_MAGIC).addName(target).addName(attacker);
				attacker.sendPacket(msg);
				target.sendPacket(msg);
			}
		}
		
		if(damage > 0)
			attacker.displayGiveDamageMessage(target, (int) damage, crit, false, false, true);

		if(calcCastBreak(target, crit))
			target.abortCast(false, true);
		
		if(target.isAfraid() && calcFearBreak(crit))
			target.getEffectList().stopEffects(EffectType.Fear);

		return damage;
	}

	public static boolean calcStunBreak(boolean crit)
	{
		return Rnd.chance(crit ? 55 : 10);
	}
	
	public static boolean calcRealTargetBreak(boolean crit)
	{
		return Rnd.chance(crit ? 5 : 2);
	}
	
	public static boolean calcFearBreak(boolean crit)
	{
		return Rnd.chance(crit ? 30 : 5);
	}

	/** Returns true in case of fatal blow success */
	public static boolean calcBlow(Creature activeChar, Creature target, Skill skill)
	{
		WeaponTemplate weapon = activeChar.getActiveWeaponItem();

		double base_weapon_crit = weapon == null ? 4. : weapon.getCritical();
		double crit_height_bonus = 0.008 * Math.min(25, Math.max(-25, target.getZ() - activeChar.getZ())) + 1.1;
		double buffs_mult = activeChar.calcStat(Stats.FATALBLOW_RATE, target, skill);
		double skill_mod = skill.isBehind() ? 5 : 4; // CT 2.3 blowrate increase

		double chance = base_weapon_crit * buffs_mult * crit_height_bonus * skill_mod;

		if(!target.isInCombat())
			chance *= 1.1;

		switch(Location.getDirectionTo(target, activeChar))
		{
			case BEHIND:
				chance *= 1.3;
				break;
			case SIDE:
				chance *= 1.1;
				break;
			case FRONT:
				if(skill.isBehind())
					chance = 3.0;
				break;
		}
		chance = Math.min(skill.isBehind() ? 100 : 80, chance);
		
		if ((Config.SKILLS_CHANCE_SHOW) && (activeChar.isPlayer()) && (((Player)activeChar).getVarB("SkillsHideChance"))) 
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.skills.Formulas.Chance", (Player)activeChar, new Object[0]).addString(skill.getName()).addNumber((long) chance));
		
		return Rnd.chance(chance);
	}

	/** Возвращает шанс крита в процентах */
	public static double calcCrit(Creature attacker, Creature target, Skill skill, boolean blow)
	{
		if(attacker.isPlayer() && attacker.getActiveWeaponItem() == null)
			return 0;
		if(skill != null)
			return skill.getCriticalRate() * (blow ? BaseStats.DEX.calcBonus(attacker) : BaseStats.STR.calcBonus(attacker)) * 0.01 * attacker.calcStat(Stats.SKILL_CRIT_CHANCE_MOD, target, skill);

		double rate = attacker.getCriticalHit(target, null) * 0.01 * target.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, attacker, skill);

		switch(Location.getDirectionTo(target, attacker))
		{
			case BEHIND:
				rate *= 1.4;
				break;
			case SIDE:
				rate *= 1.2;
				break;
		}

		return rate / 10;
	}

	public static boolean calcMCrit(double mRate)
	{
		// floating point random gives more accuracy calculation, because argument also floating point
		return Rnd.get() * 100 <= mRate;
	}

	public static boolean calcCastBreak(Creature target, boolean crit)
	{
		if(target == null || target.isDamageBlocked() || target.isInvul() || target.isRaid() || !target.isCastingNow())
			return false;
		Skill skill = target.getCastingSkill();
		if (skill == null)
			return false;
		if(skill.getSkillType() == SkillType.TAKECASTLE || skill.getSkillType() == SkillType.TAKEFORTRESS || skill.getSkillType() == SkillType.TAKEFLAG)
			return false;
		if(skill.getMagicType() == SkillMagicType.PHYSIC || skill.getMagicType() == SkillMagicType.MUSIC)
			return false;
		return Rnd.chance(target.calcStat(Stats.CAST_INTERRUPT, crit ? 75 : 10, null, skill));
	}

	/** Calculate delay (in milliseconds) before next ATTACK */
	public static int calcPAtkSpd(double rate)
	{
		return (int) (500000.0 / rate); // в миллисекундах поэтому 500*1000
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public static int calcMAtkSpd(Creature attacker, Skill skill, double skillTime)
	{
		if(skill.isMagic())
			return (int) (skillTime * 333 / Math.max(attacker.getMAtkSpd(), 1));
		return (int) (skillTime * 333 / Math.max(attacker.getPAtkSpd(), 1));
	}

	/** Calculate reuse delay (in milliseconds) for skills */
	public static long calcSkillReuseDelay(Creature actor, Skill skill)
	{
		long reuseDelay = skill.getReuseDelay();
		if(actor.isMonster())
			reuseDelay = skill.getReuseForMonsters();
		if(skill.isReuseDelayPermanent() || skill.isHandler() || skill.isItemSkill())
			return reuseDelay;
		if(actor.getSkillMastery(skill.getId()) == 1)
		{
			actor.removeSkillMastery(skill.getId());
			return 0;
		}
		if(skill.isMagic() || skill.isMusic())
			return (long) actor.calcStat(Stats.MAGIC_REUSE_RATE, reuseDelay, null, skill);
		return (long) actor.calcStat(Stats.PHYSIC_REUSE_RATE, reuseDelay, null, skill);
	}

	/** Returns true if hit missed (target evaded) */
	public static boolean calcHitMiss(Creature attacker, Creature target)
	{
		int chanceToHit = 88 + 2 * (attacker.getAccuracy() - target.getEvasionRate(attacker));

		chanceToHit = Math.max(chanceToHit, 28);
		chanceToHit = Math.min(chanceToHit, 98);

		Location.TargetDirection direction = Location.getDirectionTo(attacker, target);
		switch(direction)
		{
			case BEHIND:
				chanceToHit *= 1.2;
				break;
			case SIDE:
				chanceToHit *= 1.1;
				break;
		}
		return !Rnd.chance(chanceToHit);
	}

	/** Returns true if shield defence successfull */
	public static boolean calcShldUse(Creature attacker, Creature target)
	{
		WeaponTemplate template = target.getSecondaryWeaponItem();
		if(template == null || template.getItemType() != WeaponTemplate.WeaponType.NONE)
			return false;
		int angle = (int) target.calcStat(Stats.SHIELD_ANGLE, attacker, null);
		if(!Location.isInFront(target, attacker, angle))
			return false;
		return Rnd.chance((int)target.calcStat(Stats.SHIELD_RATE, attacker, null));
	}
	
	public static double calcSkillSuccessChance(Env env)
	{
		if(env.value == -1)
			return -1;

		final Skill skill = env.skill;
		if(!skill.isOffensive())
			return env.value;

		final Creature caster = env.character;
		final Creature target = env.target;
		final boolean isDebuff = skill.isOffensive();
		
		if (isDebuff && target.isDebuffImmune())
			return -1;
		
		int magicLevel = skill.getMagicLevel();
		if (magicLevel <= 0)
			magicLevel = target.getLevel() + 3;
		
		int targetBaseStat = 0;
		if (skill.getSaveVs() == BaseStats.NONE)
			targetBaseStat = 30;
		
		switch (skill.getSaveVs())
		{
			case STR:
				targetBaseStat = target.getSTR();
				break;
			case DEX:
				targetBaseStat = target.getDEX();
				break;
			case CON:
				targetBaseStat = target.getCON();
				break;
			case INT:
				targetBaseStat = target.getINT();
				break;
			case MEN:
				targetBaseStat = target.getMEN();
				break;
			case WIT:
				targetBaseStat = target.getWIT();
				break;
		}
		
		final double activateRate = env.value; // skill.getActivateRate()
		final double baseMod = ((((((magicLevel - target.getLevel()) + 3) * /*skill.getLvlBonusRate()*/1) + activateRate) + 30.0) - targetBaseStat);
		final double elementMod = calcElementBonus(caster, target, skill);
		final double traitMod = calcGeneralTraitBonus(env, skill.getTraitType(), skill.isIgnoreResists());
		final double buffDebuffMod = isDebuff ? 1 - (target.calcStat(Stats.DEBUFF_RESIST, 0) / 100) : 1; //todo change to isDebuff, 0 to BUFF_RESIST
		double mAtkMod = 1.;
		
		if (traitMod == Double.NEGATIVE_INFINITY || buffDebuffMod == Double.NEGATIVE_INFINITY)
		{
			Functions.sendDebugMessage(caster, "Full immunity.");
			return Double.NEGATIVE_INFINITY;
		}
		else if (traitMod == Double.POSITIVE_INFINITY || buffDebuffMod == Double.POSITIVE_INFINITY)
		{
			Functions.sendDebugMessage(caster, "Full vulnerability.");
			return Double.POSITIVE_INFINITY;
		}
		
		if(skill.isMagic()) // Calc M.Atk Mod if skill is magic.
		{
			double mAtk = caster.getMAtk(null, null);
			int mdef = Math.max(1, target.getMDef(null, null));
			double val = 0;
			if(skill.isSSPossible() && env.chargedShot == ItemInstance.CHARGED_BLESSED_SPIRITSHOT) // Only blessed spiritshots increase chance.
				val = mAtk * 3;
			
			val += mAtk;
			
			if (caster.isPlayer())
			{
				if (caster.getPlayer().getClassId().isMage())
					val = (Math.sqrt(val) / mdef) * Config.SKILLS_CHANCE_MOD_MAGE;
				else if (caster.getPlayer().getClassId().getId() >= 123 && caster.getPlayer().getClassId().getId() <= 136) // Very, very bad balance hack for kamaels
					val = (Math.sqrt(val) / mdef) * (Config.SKILLS_CHANCE_MOD_MAGE + Config.SKILLS_CHANCE_MOD_FIGHTER)/2D;
				else
					val = (Math.sqrt(val) / mdef) * Config.SKILLS_CHANCE_MOD_FIGHTER;
			}	
			else
				val = (Math.sqrt(val) / mdef) * 11.0;
			
			mAtkMod = val;
		}
		
		if (Config.SKILLS_CHANCE_CAP_ONLY_PLAYERS && !target.isPlayer())
			env.maxChance = Config.SKILLS_CHANCE_CAP;
		
		Functions.sendDebugMessage(caster, "Skill:           " + skill);
		Functions.sendDebugMessage(caster, "Activate Rate:   " + env.value);
		Functions.sendDebugMessage(caster, "Stat Depend:     " + skill.getSaveVs());
		Functions.sendDebugMessage(caster, "BaseMod:         " + baseMod);
		Functions.sendDebugMessage(caster, "Element Mod:     " + elementMod);
		Functions.sendDebugMessage(caster, "Trait Mod:       " + traitMod);
		Functions.sendDebugMessage(caster, "Buff/Debuff Mod: " + buffDebuffMod);
		Functions.sendDebugMessage(caster, "M.Atk Mod:       " + mAtkMod);

		double customBonusRate = 1.0;
		if(caster.isMonster())
			customBonusRate *= Config.SKILLS_MOB_CHANCE;
		
		final double rate = baseMod * elementMod * traitMod * mAtkMod * buffDebuffMod * customBonusRate;
		final double finalRate = traitMod > 0 ? (env.maxChance > 0 ? Util.constrain(rate, env.minChance, env.maxChance) : Math.max(rate, env.minChance)) : 0;
		
		Functions.sendDebugMessage(caster, "Rate:            " + rate);
		Functions.sendDebugMessage(caster, "Final Rate:      " + finalRate);
		
		if (Config.SKILLS_CHANCE_SHOW || env.character.isPlayer() && env.character.getPlayer().isGM())
		{
			Player player = env.character.getPlayer();
			if (player != null && player.getVarB("SkillsHideChance"))
				player.sendMessage(new CustomMessage("l2r.gameserver.skills.Formulas.Chance", player, new Object[0]).addString(env.skill.getName()).addNumber(Math.round(finalRate)));
		}
		
		return finalRate;
	}
	
	public static double calcSkillSuccessChance(Creature caster, Creature target, Skill skill)
	{
		Env env = new Env(caster, target, skill);
		env.value = skill.getActivateRate();
		env.minChance = skill.getMinChance();
		env.maxChance = skill.getMaxChance();
		env.chargedShot = caster.getChargedSpiritShot();
		return Formulas.calcSkillSuccessChance(env);
	}
	
	public static boolean calcSkillSuccess(Env env)
	{
		int chance = (int) calcSkillSuccessChance(env);
		return Rnd.chance(chance);
	}

	public static boolean calcSkillSuccess(Creature caster, Creature target, Skill skill)
	{
		int chance = (int)calcSkillSuccessChance(caster, target, skill);
		return Rnd.chance(chance);
	}

	public static void calcSkillMastery(Skill skill, Creature activeChar)
	{
		if(skill.isHandler())
			return;

		//Skill id 330 for fighters, 331 for mages
		//Actually only GM can have 2 skill masteries, so let's make them more lucky ^^
		if(activeChar.getSkillLevel(331) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getINT(), null, skill) >= Rnd.get(Config.SKILL_MASTERY_TRIGGER_CHANSE) || activeChar.getSkillLevel(330) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getSTR(), null, skill) >= Rnd.get(Config.SKILL_MASTERY_TRIGGER_CHANSE))
		{
			//byte mastery level, 0 = no skill mastery, 1 = no reuseTime, 2 = buff duration*2, 3 = power*3
			int masteryLevel;
			Skill.SkillType type = skill.getSkillType();
			if(skill.isMusic() || type == Skill.SkillType.BUFF || type == Skill.SkillType.HOT || type == Skill.SkillType.HEAL_PERCENT) //Hope i didn't forget skills to multiply their time
				masteryLevel = 2;
			else if(type == Skill.SkillType.HEAL)
				masteryLevel = 3;
			else
				masteryLevel = 1;
			
			if(masteryLevel > 0)
				activeChar.setSkillMastery(skill.getId(), masteryLevel);
		}
	}

	/**
	 * @return the Env instance with overriden value.
	 */
	public static double calcDamageResists(Env env)
	{
		//if(env.character == env.target) // это дамаг от местности вроде ожога в лаве, наносится от своего имени
		//	return env.value; // TODO: по хорошему надо учитывать защиту, но поскольку эти скиллы немагические то надо делать отдельный механизм

		if(env.character.isBoss())
			env.value *= Config.RATE_EPIC_ATTACK;
		else if(env.character.isRaid() || env.character instanceof ReflectionBossInstance)
			env.value *= Config.RATE_RAID_ATTACK;

		if(env.target.isBoss())
			env.value /= Config.RATE_EPIC_DEFENSE;
		else if(env.target.isRaid() || env.target instanceof ReflectionBossInstance)
			env.value /= Config.RATE_RAID_DEFENSE;

		Player pAttacker = env.character.getPlayer();

		// если уровень игрока ниже чем на 2 и более уровней моба 78+, то его урон по мобу снижается
		int diff = env.target.getLevel() - (pAttacker != null ? pAttacker.getLevel() : env.character.getLevel());
		if(env.character.isPlayable() && env.target.isMonster() && env.target.getLevel() >= 78 && diff > 2)
			env.value *= .7 / Math.pow(diff - 2, .25);

		double elementMod = calcElementBonus(env.character, env.target, env.skill);
		Element element = env.character.getAttackElement(env.target);

		Functions.sendDebugMessage(pAttacker, "Element: " + element.name());
		if (element != Element.NONE)
		{
			Functions.sendDebugMessage(pAttacker, "Attack: " + env.character.calcStat(element.getAttack(), env.skill == null ? 0 : env.skill.getElementPower()));
			Functions.sendDebugMessage(pAttacker, "Defence: " + env.target.calcStat(element.getDefence(), 0.));
		}
		Functions.sendDebugMessage(pAttacker, "Modifier: " + elementMod);

		return env.value * elementMod;
	}
	
	public static double calcElementBonus(Creature attacker, Creature target, Skill skill)
	{
		if (attacker == null || target == null)
			return 1.0;
		
		int attack_attribute;
		int defence_attribute;
		
		if (skill != null)
		{
			if (skill.getElement() == Element.NONE)
			{
				attack_attribute = 0;
				defence_attribute = 0;
			}
			else
			{
				attack_attribute = attacker.getAttack(skill.getElement()) + skill.getElementPower();
				defence_attribute = target.getDefence(skill.getElement());
			}
		}
		else
		{
			Element element = attacker.getAttackElement();
			attack_attribute = attacker.getAttack(element);
			defence_attribute = target.getDefence(element);
		}
		
		if (attack_attribute == 0 && defence_attribute == 0)
			return 1.0;
		
		if (Config.ALT_ELEMENT_FORMULA)
		{	
			double attack_attribute_mod = 0;
			double defence_attribute_mod = 0;
			
			if (attack_attribute >= 450)
			{
				if (defence_attribute >= 450)
				{
					attack_attribute_mod = 0.06909;
					defence_attribute_mod = 0.078;
				}
				else if (defence_attribute >= 350)
				{
					attack_attribute_mod = 0.0887;
					defence_attribute_mod = 0.1007;
				}
				else
				{
					attack_attribute_mod = 0.129;
					defence_attribute_mod = 0.1473;
				}
			}
			else if (attack_attribute >= 300)
			{
				if (defence_attribute >= 300)
				{
					attack_attribute_mod = 0.0887;
					defence_attribute_mod = 0.1007;
				}
				else if (defence_attribute >= 150)
				{
					attack_attribute_mod = 0.129;
					defence_attribute_mod = 0.1473;
				}
				else
				{
					attack_attribute_mod = 0.25;
					defence_attribute_mod = 0.2894;
				}
			}
			else if (attack_attribute >= 150)
			{
				if (defence_attribute >= 150)
				{
					attack_attribute_mod = 0.129;
					defence_attribute_mod = 0.1473;
				}
				else if (defence_attribute >= 0)
				{
					attack_attribute_mod = 0.25;
					defence_attribute_mod = 0.2894;
				}
				else
				{
					attack_attribute_mod = 0.4;
					defence_attribute_mod = 0.55;
				}
			}
			else if (attack_attribute >= -99)
			{
				if (defence_attribute >= 0)
				{
					attack_attribute_mod = 0.25;
					defence_attribute_mod = 0.2894;
				}
				else
				{
					attack_attribute_mod = 0.4;
					defence_attribute_mod = 0.55;
				}
			}
			else
			{
				if (defence_attribute >= 450)
				{
					attack_attribute_mod = 0.06909;
					defence_attribute_mod = 0.078;
				}
				else if (defence_attribute >= 350)
				{
					attack_attribute_mod = 0.0887;
					defence_attribute_mod = 0.1007;
				}
				else
				{
					attack_attribute_mod = 0.129;
					defence_attribute_mod = 0.1473;
				}
			}
			
			int attribute_diff = attack_attribute - defence_attribute;
			double min;
			double max;
			if (attribute_diff >= 300)
			{
				max = 100.0;
				min = -50;
			}
			else if (attribute_diff >= 150)
			{
				max = 70.0;
				min = -50;
			}
			else if (attribute_diff >= -150)
			{
				max = 40.0;
				min = -50;
			}
			else if (attribute_diff >= -300)
			{
				max = 40.0;
				min = -60;
			}
			else
			{
				max = 40.0;
				min = -80;
			}
			
			attack_attribute += 100;
			attack_attribute *= attack_attribute;
			
			attack_attribute_mod = (attack_attribute / 144.0) * attack_attribute_mod;
			
			defence_attribute += 100;
			defence_attribute *= defence_attribute;
			
			defence_attribute_mod = (defence_attribute / 169.0) * defence_attribute_mod;
			
			double attribute_mod_diff = attack_attribute_mod - defence_attribute_mod;
			
			attribute_mod_diff = Util.constrain(attribute_mod_diff, min, max);
			
			double result = (attribute_mod_diff / 100.0) + 1;
			
			if (attacker.isPlayer() && target.isPlayer() && (result < 1.0))
				result = 1.0;
			
			return result;
		}
		else
		{
			double diff = attack_attribute - defence_attribute;
			if (diff <= 0)
				return 1.0;
			else if (diff < 50)
				return 1.0 + diff * 0.003948;
			else if (diff < 150)
				return 1.2;
			else if (diff < 300)
				return 1.4;
			else
				return 1.7;
		}
	}
	
	public static double calcGeneralTraitBonus(Env env, SkillTrait trait, boolean ignoreResistance)
	{
		if (ignoreResistance)
			return 1.0;
		
		if (trait == SkillTrait.NONE)
		{
			// Support for slow skills resisted by Spring + Freedom which gives slow resist.
			if (env.skill != null)
				for (EffectTemplate e : env.skill.getEffectTemplates())
					if (e != null && e._stackType.equalsIgnoreCase("RunSpeedDown"))
						trait = SkillTrait.SLOW;
			
			if (trait == SkillTrait.NONE)
				return 1.0;
		}
		
		double prof = trait.calcProf(env);
		double vuln = trait.calcVuln(env);
		double diff = prof - vuln;
		
		if (diff == 0)
		{
			return 1.0;
		}
		else if (diff == Double.NEGATIVE_INFINITY) // Full Resist
		{
			return Double.NEGATIVE_INFINITY;
		}
		else if (diff == Double.POSITIVE_INFINITY) // Full Vulnerability
		{
			return Double.POSITIVE_INFINITY;
		}
		
		final double result = diff/100 + 1.0;
		return Util.constrain(result, 0.05, 2.0);
	}
}