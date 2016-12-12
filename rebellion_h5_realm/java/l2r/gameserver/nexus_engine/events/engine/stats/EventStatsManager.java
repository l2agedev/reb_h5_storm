/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.stats;

import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.html.EventHtmlManager;
import l2r.gameserver.nexus_interface.PlayerEventInfo;


/**
 * @author hNoke
 *
 */
public class EventStatsManager
{
	private GlobalStats _globalStats;
	private EventSpecificStats _eventStats;
	
	public EventStatsManager()
	{
		_globalStats = new GlobalStats();
		_eventStats = new EventSpecificStats();
		
		_globalStats.load();
		_eventStats.load();
	}
	
	public GlobalStats getGlobalStats()
	{
		return _globalStats;
	}
	
	public EventSpecificStats getEventStats()
	{
		return _eventStats;
	}
	
	public void onBypass(PlayerEventInfo player, String command)
	{
		if(command.startsWith("global_"))
		{
			_globalStats.onCommand(player, command.substring(7));
		}
		else if(command.startsWith("eventstats_"))
		{
			_eventStats.onCommand(player, command.substring(11));
		}
		else if(command.startsWith("cbmenu"))
		{
			if(EventHtmlManager.BBS_COMMAND == null)
				EventHtmlManager.BBS_COMMAND = EventConfig.getInstance().getGlobalConfigValue("cbPage");
			
			EventManager.getInstance().getHtmlManager().onCbBypass(player, EventHtmlManager.BBS_COMMAND);
		}
	}
	
	public void onLogin(PlayerEventInfo player)
	{
		_globalStats.onLogin(player);
		_eventStats.onLogin(player);
	}
	
	public void onDisconnect(PlayerEventInfo player)
	{
		_globalStats.onDisconnect(player);
		_eventStats.onDisconnect(player);
	}
	
	public void reload()
	{
		_globalStats.loadGlobalStats();
	}
	
	public static EventStatsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		private static final EventStatsManager _instance = new EventStatsManager();
	}
}
