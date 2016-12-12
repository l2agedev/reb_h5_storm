package npc.model;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.templates.npc.NpcTemplate;

public class PrisonBossInstance extends MonsterInstance
{
	
	public PrisonBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		int attackerLevel = attacker.getLevel();
		int bossLevel = getLevel();
		int leveldiff = attackerLevel - bossLevel;
		
		// If level difference is higher then 9 cancel all buffs of attacker.
		if (leveldiff >= 9)
			attacker.getEffectList().stopAllEffects();
		
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
	
	@Override
	public boolean isRaid()
	{
		return false;
	}
	
	@Override
	public boolean isFearImmune()
	{
		return true;
	}
	
	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
	
	@Override
	public boolean isLethalImmune()
	{
		return true;
	}
	
	@Override
	public boolean canChampion()
	{
		return false;
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		getMinionList().deleteMinions();
		
		super.onDeath(killer);
	}
}
