/**
 * 
 */
package l2r.gameserver.nexus_interface.callback;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.team.EventTeam;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.Collection;

/**
 * @author hNoke
 *
 */
public interface ICallback
{
	public void eventStarts(int instance, EventType event, Collection<? extends EventTeam> teams);
	public void playerKills(EventType event, PlayerEventInfo player, PlayerEventInfo target);
	public void playerScores(EventType event, PlayerEventInfo player, int count);
	public void playerFlagScores(EventType event, PlayerEventInfo player);
	public void playerKillsVip(EventType event, PlayerEventInfo player, PlayerEventInfo vip);
	public void eventEnded(int instance, EventType event, Collection<? extends EventTeam> teams);
}
