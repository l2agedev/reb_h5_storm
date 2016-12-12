/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r.delegate;

import l2r.gameserver.nexus_interface.delegate.CharacterData;
import l2r.gameserver.nexus_interface.delegate.ObjectData;

/**
 * @author hNoke
 *
 */
public interface INpcData
{
	public ObjectData getObjectData();
	
	public void setName(String name);
	public void setTitle(String t);
	
	public int getNpcId();
	
	public void setEventTeam(int team);
	public int getEventTeam();
	
	public void broadcastNpcInfo();
	public void broadcastSkillUse(CharacterData owner, CharacterData target, int skillId, int level);
	
	public void deleteMe();
}
