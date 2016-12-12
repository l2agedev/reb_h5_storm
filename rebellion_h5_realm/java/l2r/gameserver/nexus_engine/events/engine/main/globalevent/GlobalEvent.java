/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.main.globalevent;

import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.NpcData;


/**
 * @author hNoke
 *
 */
public abstract class GlobalEvent
{
	public abstract String getName();
	public abstract boolean canStart(String param);
	public abstract void start(String param);
	public abstract void end();
	
	public abstract boolean canRegister(PlayerEventInfo player);
	public abstract void addPlayer(PlayerEventInfo player);

	public abstract void reload();
	
	public abstract void monsterDies(NpcData npc);
	
	public abstract String getStateNameForHtml();
	
	public void announce(String message)
	{
		announce(message, false);
	}
	
	public void announce(String message, boolean special)
	{
		for(PlayerEventInfo player : CallBack.getInstance().getOut().getAllPlayers())
		{
			player.screenMessage(message, getName(), special);
		}
	}
}
