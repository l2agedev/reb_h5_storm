package l2r.gameserver.model.base;

import l2r.gameserver.Config;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.Stats;

public enum SkillTrait
{
	NONE,
	BLEED
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.BLEED_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.BLEED_POWER, env.target, env.skill);
		}
		
		@Override
		public BaseStats getSaveVs()
		{
			return BaseStats.CON;
		}
	},
	BOSS,
	DEATH,
	DERANGEMENT
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.MENTAL_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			if (Config.SKILL_CHANCE_CALCULATED_BY_ENCHANT_LEVEL)
				return Math.min(40., env.character.calcStat(Stats.MENTAL_POWER, env.target, env.skill) + calcEnchantMod(env));
			
			return env.character.calcStat(Stats.MENTAL_POWER, env.target, env.skill);
		}
		
		@Override
		public BaseStats getSaveVs()
		{
			return BaseStats.MEN;
		}
	},
	ETC,
	GUST,
	HOLD
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.ROOT_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.ROOT_POWER, env.target, env.skill);
		}
		
		@Override
		public BaseStats getSaveVs()
		{
			return BaseStats.MEN;
		}
	},
	SLOW // NOT RETAIL, used for skills that give resistance to slows.
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.SLOW_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.SLOW_POWER, env.target, env.skill);
		}
		
		@Override
		public BaseStats getSaveVs()
		{
			return BaseStats.MEN;
		}
	},
	PARALYZE
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.PARALYZE_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.PARALYZE_POWER, env.target, env.skill);
		}
		
		@Override
		public BaseStats getSaveVs()
		{
			return BaseStats.MEN;
		}
	},
	PHYSICAL_BLOCKADE,
	POISON
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.POISON_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.POISON_POWER, env.target, env.skill);
		}
		
		@Override
		public BaseStats getSaveVs()
		{
			return BaseStats.MEN;
		}
	},
	SHOCK
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.STUN_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			if (Config.SKILL_CHANCE_CALCULATED_BY_ENCHANT_LEVEL)
				return Math.min(40., env.character.calcStat(Stats.STUN_POWER, env.target, env.skill) + calcEnchantMod(env));
			
			return env.character.calcStat(Stats.STUN_POWER, env.target, env.skill);
		}
		
		@Override
		public BaseStats getSaveVs()
		{
			return BaseStats.CON;
		}
	},
	SLEEP
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.SLEEP_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.SLEEP_POWER, env.target, env.skill);
		}
		
		@Override
		public BaseStats getSaveVs()
		{
			return BaseStats.MEN;
		}
	},
	VALAKAS;

	public double calcVuln(Env env)
	{
		return 0;
	}

	public double calcProf(Env env)
	{
		return 0;
	}
	
	public BaseStats getSaveVs()
	{
		return BaseStats.NONE;
	}
/*
	public double calcResistMod(Env env)
	{
		final double vulnMod = calcVuln(env);
		final double profMod = calcProf(env);
		final double maxResist = 90. + Math.max(calcEnchantMod(env), profMod * 0.85);
		return (maxResist - vulnMod) / 60.;
	}
*/
	public static double calcEnchantMod(Env env)
	{
		int enchantLevel = env.skill.getDisplayLevel();
		if (enchantLevel <= 100)
			return 0;
		enchantLevel = enchantLevel % 100;
		return env.skill.getEnchantLevelCount() == 15 ? enchantLevel * 2 : enchantLevel;
	}
}