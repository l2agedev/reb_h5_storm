package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.Func;

public class EffectServitorShare extends Effect
{
	public static final Stats[] STATS_USED = {Stats.POWER_ATTACK, Stats.POWER_DEFENCE, Stats.MAGIC_ATTACK, Stats.MAGIC_DEFENCE, Stats.MAX_HP, Stats.MAX_MP, Stats.CRITICAL_BASE, Stats.POWER_ATTACK_SPEED, Stats.MAGIC_ATTACK_SPEED};
	
	public class FuncShare extends Func
	{
		public FuncShare(Stats stat, int order, Object owner, double value)
		{
			super(stat, order, owner, value);
		}

		@Override
		public void calc(Env env)
		{
			env.value += env.character.getPlayer().calcStat(stat, stat.getInit()) * value;
		}
	}

	public EffectServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		onActionTime();
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}
	
	public double getBonusStatValue(Stats stat)
	{
		
		/*for (Func func : getStatFuncs()) Untested
		{
			if (func.stat == stat)
				return getEffected().calcStat(stat, 0) * (1 + func.value) * (1 - func.value);
		}*/
		
		switch (stat)
		{
			case POWER_ATTACK:
				return getEffected().getPAtk(null) * 0.5D;
			case POWER_DEFENCE:
				return getEffected().getPDef(null) * 0.5D;
			case MAGIC_ATTACK:
				return getEffected().getMAtk(null, null) * 0.25D;
			case MAGIC_DEFENCE:
				return getEffected().getMDef(null, null) *0.25D;
			case MAX_HP:
				return getEffected().getMaxHp() * 0.1D;
			case MAX_MP:
				return getEffected().getMaxHp()* 0.1D;
			case CRITICAL_BASE:
				return getEffected().getCriticalHit(null, null)* 0.2D;
			case POWER_ATTACK_SPEED:
				return getEffected().getPAtkSpd()* 0.1D;
			case MAGIC_ATTACK_SPEED:
				return getEffected().getMAtkSpd()* 0.03D;
			default:
				return 0;
		}
	}

/*	@Override
	public Func[] getStatFuncs()
	{
		return new Func[] 
		{ new Func(Stats.POWER_ATTACK, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getPAtk((Creature)((target != null) && (target.isPet()) ? target : null)) * 0.5D;
					}
				}
			}
			, new Func(Stats.POWER_DEFENCE, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getPDef((Creature)((target != null) && (target.isPet()) ? target : null)) * 0.5D;
					}
				}
			}
			, new Func(Stats.MAGIC_ATTACK, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getMAtk((Creature)((target != null) && (target.isPet()) ? target : null), env.skill) * 0.25D;
					}
				}
			}
			, new Func(Stats.MAGIC_DEFENCE, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getMDef((Creature)((target != null) && (target.isPet()) ? target : null), env.skill) * 0.25D;
					}
				}
			}
			, new Func(Stats.MAX_HP, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
						env.value += pc.getMaxHp() * 0.1D;
				}
			}
			, new Func(Stats.MAX_HP, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
						env.value += pc.getMaxMp() * 0.1D;
				}
			}
			, new Func(Stats.CRITICAL_BASE, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getCriticalHit((Creature)((target != null) && (target.isPet()) ? target : null), env.skill) * 0.2D;
					}
				}
			}
			, new Func(Stats.POWER_ATTACK_SPEED, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
						env.value += pc.getPAtkSpd() * 0.1D;
				}
			}
			, new Func(Stats.MAGIC_ATTACK_SPEED, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
						env.value += pc.getMAtkSpd() * 0.03D;
				}
			}
		};



		//FuncTemplate[] funcTemplates = getTemplate().getAttachedFuncs();
		//Func[] funcs = new Func[funcTemplates.length];
		//if(_effected.getEffectList().getEffectByType(EffectType.ServitorShare) == null)
		//{
		//for(int i = 0; i < funcs.length; i++)
		//{
		//	funcs[i] = new FuncShare(funcTemplates[i]._stat, funcTemplates[i]._order, this, funcTemplates[i]._value);
		//}
		//}
		//return funcs;
	}*/

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}