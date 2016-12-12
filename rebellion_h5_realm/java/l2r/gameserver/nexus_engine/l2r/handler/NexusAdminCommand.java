/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r.handler;

import l2r.gameserver.nexus_interface.PlayerEventInfo;

/**
 * @author hNoke
 *
 */
public interface NexusAdminCommand
{
	public abstract boolean useAdminCommand(Enum comm, String[] wordList, String fullString, PlayerEventInfo pi);
}
