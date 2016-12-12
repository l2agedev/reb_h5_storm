package l2r.gameserver.nexus_interface.delegate;


import l2r.gameserver.model.Skill;
import l2r.gameserver.nexus_engine.l2r.delegate.ISkillData;
import l2r.gameserver.tables.SkillTable;

/**
 * @author hNoke
 *
 */
public class SkillData implements ISkillData
{
	private Skill _skill;
	
	public SkillData(Skill cha)
	{
		_skill = cha;
	}
	
	public SkillData(int skillId, int level)
	{
		_skill = SkillTable.getInstance().getInfo(skillId, level);
	}
	
	public Skill getOwner()
	{
		return _skill;
	}
	
	@Override
	public String getName()
	{
		return _skill.getName();
	}
	
	@Override
	public int getLevel()
	{
		return _skill.getLevel();
	}
	
	@Override
	public boolean exists()
	{
		return _skill != null;
	}
	
	@Override
	public String getSkillType()
	{
		return _skill.getSkillType().toString();
	}
	
	@Override
	public boolean isHealSkill()
	{
		if(getSkillType().equals("BALANCE_LIFE")
				|| getSkillType().equals("CPHEAL_PERCENT")
				|| getSkillType().equals("COMBATPOINTHEAL")
				|| getSkillType().equals("CPHOT")
				|| getSkillType().equals("HEAL")
				|| getSkillType().equals("HEAL_PERCENT")
				|| getSkillType().equals("HEAL_STATIC")
				|| getSkillType().equals("HOT")
				|| getSkillType().equals("MANAHEAL")
				|| getSkillType().equals("MANAHEAL_PERCENT")
				|| getSkillType().equals("MANARECHARGE")
				|| getSkillType().equals("MPHOT")
				|| getSkillType().equals("MANA_BY_LEVEL")
				)
		{
			return true;
		}
		else 
			return false;
	}
	
	@Override
	public boolean isResSkill()
	{
		if(getSkillType().equals("RESURRECT"))
			return true;
		return false;
	}
	
	@Override
	public int getHitTime()
	{
		return _skill.getHitTime();
	}
	
	@Override
	public long getReuseDelay()
	{
		return _skill.getReuseDelay();
	}
	
	@Override
	public int getId()
	{
		return _skill.getId();
	}
}
