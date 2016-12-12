/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r;

import l2r.gameserver.nexus_interface.PlayerEventInfo;

import javolution.util.FastMap;

/**
 * @author hNoke
 *
 */
public interface IPlayerBase
{
	public PlayerEventInfo addInfo(PlayerEventInfo player);
	
	public PlayerEventInfo getPlayer(int id);
	
	public FastMap<Integer, PlayerEventInfo> getPs();
	
	public void eventEnd(PlayerEventInfo player);
	public void playerDisconnected(PlayerEventInfo player);
	
	public void deleteInfo(int player);
	
	// TODO:
	
	// NO REFERENCES FROM EventEngine's l2j package to L2j server! use genericity in some form instead - exmaple above
}
