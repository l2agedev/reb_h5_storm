package l2r.gameserver.nexus_interface.handlers;


import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.nexus_engine.l2r.handler.NexusAdminCommand;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

/**
 * @author hNoke
 *
 */
public abstract class AdminCommandHandlerInstance implements IAdminCommandHandler, NexusAdminCommand
{
	//public abstract Enum[] getAdminCommandEnum();
	
	@Override
	public abstract boolean useAdminCommand(Enum comm, String[] wordList, String fullString, PlayerEventInfo pi);

	@Override
	public final boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player player) 
	{
		return useAdminCommand(comm, wordList, fullString, player.getEventInfo());
	}
}
