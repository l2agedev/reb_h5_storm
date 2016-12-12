/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r.delegate;

import l2r.gameserver.nexus_interface.PlayerEventInfo;

/**
 * @author hNoke
 *
 */
public interface IPartyData
{
	public void addPartyMember(PlayerEventInfo player);
	public void removePartyMember(PlayerEventInfo player);
	
	public PlayerEventInfo getLeader();
	public int getLeadersId();
	
	public PlayerEventInfo[] getPartyMembers();
	public int size();
}
