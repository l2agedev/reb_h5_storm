package ai;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SkillTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bosses.AntharasManager;

public class Antharas extends DefaultAI
{
	// debuffs
	final Skill s_fear = getSkill(4108, 1), s_fear2 = getSkill(5092, 1), s_curse = getSkill(4109, 1), s_paralyze = getSkill(4111, 1);
	// damage skills
	final Skill s_shock = getSkill(4106, 1), s_shock2 = getSkill(4107, 1), s_antharas_ordinary_attack = getSkill(4112, 1), s_antharas_ordinary_attack2 = getSkill(4113, 1), s_meteor = getSkill(5093, 1), s_breath = getSkill(4110, 1);
	// regen skills
	final Skill s_regen1 = getSkill(4239, 1), s_regen2 = getSkill(4240, 1), s_regen3 = getSkill(4241, 1);
	
	private static final int FWA_INTERVAL_MINION_WEAK = 12 * 60000; // default 8
	private static final int FWA_INTERVAL_MINION_NORMAL = 9 * 60000; // default 5
	private static final int FWA_INTERVAL_MINION_STRONG = 7 * 60000; // default 3
	
	// Vars
	private int _hpStage = 0;
	private static long _minionsSpawnDelay = 0;
	private List<NpcInstance> minions = new ArrayList<NpcInstance>();
	private int DAMAGE_COUNTER = 0;
	
	public Antharas(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if (DAMAGE_COUNTER == 0)
			actor.getAI().startAITask();
		
		AntharasManager.setLastAttackTime();
		for(Playable p : AntharasManager.getZone().getInsidePlayables())
			notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 1);
		DAMAGE_COUNTER++;
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		setNextMinionSpawnDelay();
	}

	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		Creature target;
		
		NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return false;
		
		//possible to return here...
		if((target = prepareTarget()) == null)
		{
			target = actor.getAggroList().getRandomHated();
			if (target == null)
				return false;
			
			/* TODO: test if this work...
			if (target.isPlayer() && target.getPlayer().isGM())
				return false;
			
			if(target.isDead() && !target.isInRangeZ(actor, 10000))
				return false;
				*/
		}

		double distance = actor.getDistance(target.getLoc());

		// Buffs and stats
		double chp = actor.getCurrentHpPercents();
		if(_hpStage == 0)
		{
			actor.altOnMagicUseTimer(actor, s_regen1);
			_hpStage = 1;
		}
		else if(chp < 75 && _hpStage == 1)
		{
			actor.altOnMagicUseTimer(actor, s_regen2);
			_hpStage = 2;
		}
		else if(chp < 50 && _hpStage == 2)
		{
			actor.altOnMagicUseTimer(actor, s_regen3);
			_hpStage = 3;
		}
		else if(chp < 30 && _hpStage == 3)
		{
			actor.altOnMagicUseTimer(actor, s_regen3);
			_hpStage = 4;
		}

		// Minions spawn
		if(_minionsSpawnDelay < System.currentTimeMillis() && getAliveMinionsCount() < Config.ANTHARAS_MINIONS_NUMBER && Rnd.chance(5))
		{
			NpcInstance minion = Functions.spawn(actor.getLoc().findPointToStay(400, 700), Rnd.chance(50) ? 29190 : 29069);  // Antharas Minions
			minions.add(minion);
			AntharasManager.addSpawnedMinion(minion);
			setNextMinionSpawnDelay();
		}

		// Basic Attack
		if(Rnd.chance(50))
			return chooseTaskAndTargets(Rnd.chance(50) ? s_antharas_ordinary_attack : s_antharas_ordinary_attack2, target, distance);

		// Stage based skill attacks
		Map<Skill, Integer> d_skill = new HashMap<Skill, Integer>();
		switch(_hpStage)
		{
			case 1:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				break;
			case 2:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				break;
			case 3:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				addDesiredSkill(d_skill, target, distance, s_shock2);
				addDesiredSkill(d_skill, target, distance, s_breath);
				break;
			case 4:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				addDesiredSkill(d_skill, target, distance, s_shock2);
				addDesiredSkill(d_skill, target, distance, s_fear);
				addDesiredSkill(d_skill, target, distance, s_shock);
				addDesiredSkill(d_skill, target, distance, s_breath);
				break;
			default:
				break;
		}

		Skill r_skill = selectTopSkill(d_skill);
		if(r_skill != null && !r_skill.isOffensive())
			target = actor;

		return chooseTaskAndTargets(r_skill, target, distance);
	}
	
	private void setNextMinionSpawnDelay()
	{
		switch (AntharasManager.getTypeAntharas())
		{
			case 1:
				_minionsSpawnDelay = System.currentTimeMillis() + FWA_INTERVAL_MINION_NORMAL;
				break;
			case 2:
				_minionsSpawnDelay = System.currentTimeMillis() + FWA_INTERVAL_MINION_STRONG;
				break;
			default:
				_minionsSpawnDelay = System.currentTimeMillis() + FWA_INTERVAL_MINION_WEAK;
				break;
		}
	}
	
	private int getAliveMinionsCount()
	{
		int i = 0;
		for(NpcInstance n : minions)
			if(n != null && !n.isDead())
				i++;
		return i;
	}

	private Skill getSkill(int id, int level)
	{
		return SkillTable.getInstance().getInfo(id, level);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(minions != null && !minions.isEmpty())
			for(NpcInstance n : minions)
				n.deleteMe();
		
		super.onEvtDead(killer);
	}
}