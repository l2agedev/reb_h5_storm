/**
 * 
 */
package l2r.gameserver.nexus_interface.callback;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.team.EventTeam;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.Collection;
import java.util.List;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public class CallbackManager implements ICallback
{
	public List<ICallback> _list = new FastTable<ICallback>();
	
	public CallbackManager()
	{
		
	}
	
	public void registerCallback(ICallback c)
	{
		_list.add(c);
	}

	@Override
	public void eventStarts(int instance, EventType event, Collection<? extends EventTeam> teams)
	{
//		System.out.println("achievments hook: event started for instance " + instance);
		
		for(ICallback cb : _list)
		{
			try
			{
				cb.eventStarts(instance, event, teams);
			}
			catch (Exception e)
			{
			}
		}
	}

	@Override
	public void playerKills(EventType event, PlayerEventInfo player, PlayerEventInfo target)
	{
		for(ICallback cb : _list)
		{
			try
			{
				cb.playerKills(event, player, target);
			}
			catch (Exception e)
			{
			}
		}
	}

	@Override
	public void playerScores(EventType event, PlayerEventInfo player, int count)
	{
//		System.out.println("achievments hook: player scored");
		
		for(ICallback cb : _list)
		{
			try
			{
				cb.playerScores(event, player, count);
			}
			catch (Exception e)
			{
			}
		}
	}

	@Override
	public void playerFlagScores(EventType event, PlayerEventInfo player)
	{
//		System.out.println("achievments hook: player scored w a flag ");
		
		for(ICallback cb : _list)
		{
			try
			{
				cb.playerFlagScores(event, player);
			}
			catch (Exception e)
			{
			}
		}
	}

	@Override
	public void playerKillsVip(EventType event, PlayerEventInfo player, PlayerEventInfo vip)
	{
//		System.out.println("achievments hook: player vip killed ");
		
		for(ICallback cb : _list)
		{
			try
			{
				cb.playerKillsVip(event, player, vip);
			}
			catch (Exception e)
			{
			}
		}
	}

	@Override
	public void eventEnded(int instance, EventType event, Collection<? extends EventTeam> teams)
	{
//		System.out.println("achievments hook: event ended for instance " + instance);
		
		for(ICallback cb : _list)
		{
			try
			{
				cb.eventEnded(instance, event, teams);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	public static final CallbackManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CallbackManager _instance = new CallbackManager();
	}
}
