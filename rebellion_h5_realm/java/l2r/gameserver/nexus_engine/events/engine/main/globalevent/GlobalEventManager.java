/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.main.globalevent;

import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.EventWarnings;
import l2r.gameserver.nexus_engine.events.engine.lang.LanguageEngine;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.RaidbossEvent;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.townrush.TownRushEvent;
import l2r.gameserver.nexus_engine.events.engine.scheduler.EventScheduler;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javolution.util.FastTable;


/**
 * @author hNoke
 *
 */
public class GlobalEventManager
{
	private GlobalEvent _activeGlobalEvent;
	private String selectedEvent;
	
	private long timeStarted;
	private int lastEventIndex;
	
	private GlobalEvent _raidbossEvent;
	private GlobalEvent _townrushEvent;

	public static List<GlobalEvent> _events = new FastTable<GlobalEvent>();
	
	ScheduledFuture<?> _timeLimit;
	
	public GlobalEventManager()
	{
		load();
	}
	
	private void load()
	{
		_activeGlobalEvent = null;
		lastEventIndex = -1;

		_raidbossEvent = new RaidbossEvent();
		_events.add(_raidbossEvent);

		_townrushEvent = new TownRushEvent();
		_events.add(_townrushEvent);
	}
	
	public void selectEvent()
	{
		if(isActive())
			return;
		
		lastEventIndex ++;
		if(lastEventIndex >= _events.size() || _events.get(lastEventIndex) == null)
		{
			lastEventIndex = 0;
			selectedEvent = _events.get(0).getName();
		}
		else
		{
			selectedEvent = _events.get(lastEventIndex).getName();
		}
	}
	
	public boolean start(String event, String param)
	{
		if(_activeGlobalEvent != null)
			return false;

		if(event == null)
			event = selectedEvent;

		if(event == null)
		{
			selectEvent();
			event = selectedEvent;
		}

		if(event.equalsIgnoreCase("RaidBoss"))
		{
			if(_raidbossEvent.canStart(param))
			{
				_activeGlobalEvent = _raidbossEvent;
				_activeGlobalEvent.start(param);
			}
		}

		if(event.equalsIgnoreCase("TownRush"))
		{
			if(_townrushEvent.canStart(param))
			{
				_activeGlobalEvent = _townrushEvent;
				_activeGlobalEvent.start(param);
			}
		}

		// an event has been started successfully
		if(_activeGlobalEvent != null)
		{
			timeStarted = System.currentTimeMillis();
			scheduleAutoEnd(timeStarted);
		}

		return _activeGlobalEvent != null;
	}

	public void reload()
	{
		for(GlobalEvent event : _events)
		{
			event.reload();
		}
	}

	private void scheduleAutoEnd(final long timeStartedOrig)
	{
		_timeLimit = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if(isActive() && timeStartedOrig == timeStarted)
				{
					stopByGm();
				}
			}
		}, 1000*60*60);
	}

	public boolean stopByGm()
	{
		if(_activeGlobalEvent != null)
		{
			_activeGlobalEvent.end();
			_activeGlobalEvent = null;

			timeStarted = 0;

			return true;
		}
		return false;
	}
	
	public void stop()
	{
		if(_activeGlobalEvent != null)
		{
			_activeGlobalEvent = null;
			
			timeStarted = 0;
		}
	}
	
	public void registerPlayer(PlayerEventInfo player)
	{
		if(_activeGlobalEvent != null)
		{
			if (player.isRegistered())
			{
				player.sendMessage(LanguageEngine.getMsg("registering_alreadyRegistered"));
				return;
			}
			
			int i = EventWarnings.getInstance().getPoints(player);
			if(i >= EventWarnings.MAX_WARNINGS && !player.isGM())
			{
				player.sendMessage(LanguageEngine.getMsg("registering_warningPoints", EventWarnings.MAX_WARNINGS, i));
				return;
			}
			
			if (EventManager.getInstance().canRegister(player) && _activeGlobalEvent.canRegister(player))
			{
				_activeGlobalEvent.addPlayer(player);
			}
			else
			{
				player.sendMessage("You cannot register in this state.");
			}
		}
	}
	
	public boolean isActive()
	{
		return _activeGlobalEvent != null;
	}
	
	public GlobalEvent getActiveEvent()
	{
		return _activeGlobalEvent;
	}
	
	public String timeActive()
	{
		if(timeStarted == 0)
			return "N/A";
		
		long currTime = System.currentTimeMillis();
		return Math.max(0, (currTime - timeStarted) / 60000) + " min";
	}
	
	public String getTimeLimit()
	{
		if(EventScheduler.getInstance()._isActive)
		{
			ScheduledFuture<?> next = _timeLimit;
			
			double d = 0;
			if(next != null && !next.isDone())
				d = next.getDelay(TimeUnit.SECONDS);

			if(d == 0)
				return "N/A";
			else
			{
				if(d >= 60)
					return (int)d / 60 + " min";
				else
					return (int)d + " sec";
			}
		}
		else
			return "N/A";
	}
	
	public String getEventName()
	{
		if(isActive())
		{
			return _activeGlobalEvent.getName();
		}
		else
			return selectedEvent == null ? "N/A" : selectedEvent;
	}
	
	public String getState()
	{
		if(_activeGlobalEvent != null) return _activeGlobalEvent.getStateNameForHtml();
		return "N/A";
	}
}
