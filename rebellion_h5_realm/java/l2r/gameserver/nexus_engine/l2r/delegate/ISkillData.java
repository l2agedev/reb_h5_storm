/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r.delegate;

/**
 * @author hNoke
 *
 */
public interface ISkillData
{
	public int getId();
	
	public String getName();
	public int getLevel();
	
	public boolean exists();
	
	public String getSkillType();
	
	public boolean isHealSkill();
	public boolean isResSkill();
	
	public int getHitTime();
	public long getReuseDelay();
}
