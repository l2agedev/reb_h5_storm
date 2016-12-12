package l2r.gameserver.nexus_engine.events.engine.main;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.Earthquake;
import l2r.gameserver.network.serverpackets.ExRedSky;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.events.engine.EventBuffer;
import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.EventMapSystem;
import l2r.gameserver.nexus_engine.events.engine.EventWarnings;
import l2r.gameserver.nexus_engine.events.engine.base.EventMap;
import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.lang.LanguageEngine;
import l2r.gameserver.nexus_engine.events.engine.main.events.AbstractMainEvent;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.GlobalEventManager;
import l2r.gameserver.nexus_engine.events.engine.main.pvpzone.PvpZoneManager;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.NpcData;
import l2r.gameserver.nexus_interface.delegate.NpcTemplateData;
import l2r.gameserver.nexus_interface.delegate.SkillData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javolution.util.FastTable;
import javolution.util.FastMap;

/**
 * @author hNoke
 * - manages everything related to the running of MainEvents
 */
public class MainEventManager
{
	EventManager _manager;
	
	// active main events
	public enum State
	{
		IDLE, REGISTERING, RUNNING, TELE_BACK, END
	}
	
	private EventTaskScheduler _task;
	
	private AbstractMainEvent current;
	private EventMap activeMap;
	
	private List<PlayerEventInfo> _players;
	
	private State _state;
	private int _counter;
	
	private long lastEvent;
	
	private RegistrationCountdown _regCountdown;
	
	private ScheduledFuture<?> _regCountdownFuture;
	private ScheduledFuture<?> taskFuture;

	public Map<Integer, RegNpcLoc> regNpcLocs;
	
	private RegNpcLoc regNpc;
	private NpcData regNpcInstance;
	private int eventRunTime;
	
	private boolean autoScheduler = false;
	private double pausedTimeLeft;
	
	private EventScheduler scheduler;
	
	// new Event order system
	private List<EventScheduleData> _eventScheduleData = new FastTable<EventScheduleData>();
	private EventType _lastEvent = null;
	
	private PvpZoneManager _pvpZoneManager;
	
	public PvpZoneManager getPvpZoneManager()
	{
		if(_pvpZoneManager == null)
			_pvpZoneManager = new PvpZoneManager();
		
		return _pvpZoneManager;
	}

	private GlobalEventManager _globalManager;

	public GlobalEventManager getGlobalEventManager()
	{
		if(_globalManager == null)
			_globalManager = new GlobalEventManager();

		return _globalManager;
	}
	
	public MainEventManager()
	{
		_manager = EventManager.getInstance();
		
		_state = State.IDLE;
		
		_task = new EventTaskScheduler();
		_regCountdown = new RegistrationCountdown();
		
		_counter = 0;
		
		activeMap = null;
		eventRunTime = 0;
		
		_players = new FastTable<PlayerEventInfo>();
		
		initRegNpcLocs();
		
		scheduler = new EventScheduler();
		scheduler.schedule(-1, true);
		
		_pvpZoneManager = new PvpZoneManager();
		_globalManager = getGlobalEventManager();
	}
	
	private void initRegNpcLocs()
	{
		regNpcLocs = new FastMap<Integer, RegNpcLoc>();
		
		regNpcLocs.put(1, new RegNpcLoc("Your cords", null));
		regNpcLocs.put(2, new RegNpcLoc("Hunters Village", new int[] {116541,76077,-2730,0}));
		regNpcLocs.put(3, new RegNpcLoc("Goddard Town", new int[] {147726,-56323,-2781,0}));
		regNpcLocs.put(4, new RegNpcLoc("Ketra/Varka", new int[] {125176,-69204,-3260,0}));
		regNpcLocs.put(5, new RegNpcLoc("Cemetery", new int[] {182297,19407,-3174,0}));
		regNpcLocs.put(6, new RegNpcLoc("Aden Town", new int[] {148083,26983,-2205,0}));
	}
	
	public synchronized void startEvent(PlayerEventInfo gm, EventType type, int regTime, String mapName, String npcLoc, int runTime, boolean newbieOnly)
	{
		AbstractMainEvent event = EventManager.getInstance().getMainEvent(type);
		if(event == null)
		{
			if(gm != null)
				gm.sendMessage("This event is not finished yet (most likely cause it is being reworked to be a mini event).");
			
			NexusLoader.debug("An unfinished event is chosen to be run. Skipping to the next one...", Level.WARNING);
			
			scheduler.run();
			return;
		}

		event.setNewbieOnly(newbieOnly);
		
		EventMap map = EventMapSystem.getInstance().getMap(type, mapName);
		if(map == null)
		{
			if(gm != null)
				gm.sendMessage("Map " + mapName + " doesn't exist or is not allowed for this event.");
			else
				NexusLoader.debug("Map " + mapName + " doesn't exist for event " + type.getAltTitle(), Level.WARNING);
			
			return;
		}
		
		RegNpcLoc npc = null;
		if(npcLoc != null)
		{
			for(Entry<Integer, RegNpcLoc> e : regNpcLocs.entrySet())
			{
				if(e.getValue().name.equalsIgnoreCase(npcLoc))
				{
					npc = e.getValue();
					break;
				}
			}
		}
		
		if(npc == null && gm != null)
		{
			gm.sendMessage("Reg NPC location " + npcLoc + " is not registered in the engine.");
			return;
		}
		else if(npc == null)
		{
			String configsCords = EventConfig.getInstance().getGlobalConfigValue("spawnRegNpcCords");
			
			int x = Integer.parseInt(configsCords.split(";")[0]);
			int y = Integer.parseInt(configsCords.split(";")[1]);
			int z = Integer.parseInt(configsCords.split(";")[2]);
			
			npc = new RegNpcLoc("From Configs", new int[]{x,y,z,0});
		}
		
		if(regTime <= 0 || regTime >= 1439)
		{
			if(gm != null)gm.sendMessage("The minutes for registration must be within interval 1-1439 minutes.");
			else NexusLoader.debug("Can't start main event (automatic scheduler) - regTime is too high or too low (" + regTime + ").", Level.SEVERE);
			return;
		}
		
		int eventsRunTime = event.getInt("runTime");
		
		if(gm == null && eventsRunTime > 0) // launched automatically - use configs for runtime value
			runTime = eventsRunTime;
		
		if(runTime <= 0 || runTime >= 120) // max event runTime is 120 minutes
		{
			if(gm != null)gm.sendMessage("RunTime must be at least 1 minute and max. 120 minutes.");
			else NexusLoader.debug("Can't start main event (automatic scheduler) - runTime is too high or too low (" + runTime + ").", Level.SEVERE);
			return;
		}
		
		eventRunTime = runTime * 60;
		
		regNpc = npc;
		
		_state = State.REGISTERING;
		current = event;
		current.startRegistration();
		
		activeMap = map;
		
		_counter = (regTime) * 60;
		_regCountdownFuture = CallBack.getInstance().getOut().scheduleGeneral(_regCountdown, 1);
		
		spawnRegNpc(gm);

		announce(LanguageEngine.getMsg("announce_eventStarted", type.getHtmlTitle()), (newbieOnly ? ("Newbie " + type.getAltTitle()) : type.getHtmlTitle()));
		
		String announce = EventConfig.getInstance().getGlobalConfigValue("announceRegNpcPos");
		if(announce.equals("-"))
			return;
		else
		{
			if(gm != null)
			{
				if(!npc.name.equals("Your cords") && !npc.name.equals("From Configs"))
				{
					announce(LanguageEngine.getMsg("announce_npcPos", npc.name), (newbieOnly ? ("Newbie " + type.getAltTitle()) : type.getHtmlTitle()));
				}
				else
					/**/ if(NexusLoader.detailedDebug) print("not announcing registration cords (either Your Cords or From Configs chosen)");
			}
			else
			{
				announce(LanguageEngine.getMsg("announce_npcPos", announce), (newbieOnly ? ("Newbie " + type.getAltTitle()) : type.getHtmlTitle()));
			}
		}
		

		/*if(EventConfig.getInstance().getGlobalConfigBoolean("announce_moreInfoInCb"))
		{
			announce(LanguageEngine.getMsg("announce_moreInfoInCb"));
		}*/

		if(newbieOnly)
			announce("Only items up to S80-grade are allowed on this event.", ("Newbie " + type.getAltTitle()));

		NexusLoader.debug("Started registration for event " + current.getEventName());
		if(gm != null)
			gm.sendMessage("The event has been started.");
	}
	
	private void spawnRegNpc(PlayerEventInfo gm)
	{
		if(gm == null && !EventConfig.getInstance().getGlobalConfigBoolean("allowSpawnRegNpc"))
		{
			print("configs permitted spawning regNpc");
			return;
		}
		
		if(regNpc != null)
		{
			final int id = EventConfig.getInstance().getGlobalConfigInt("mainEventManagerId");
			final NpcTemplateData template = new NpcTemplateData(id);
			
			print("spawning npc id " + id + ", template exists = " + template.exists());
			
			try
			{
				NpcData data;
				
				if(regNpc.cords == null)
				{
					data = template.doSpawn(gm.getX(), gm.getY(), gm.getZ(), 1, gm.getHeading(), 0);
				}
				else
				{
					data = template.doSpawn(regNpc.cords[0], regNpc.cords[1], regNpc.cords[2], 1, regNpc.cords[3], 0);
				}
				
				regNpcInstance = data;
				
				regNpcInstance.setTitle(current.getEventType().getHtmlTitle());
				regNpcInstance.broadcastNpcInfo();
				
				L2GameServerPacket redSky = new ExRedSky(5);
				L2GameServerPacket eq = new Earthquake(regNpcInstance.getLoc(), 15, 5);
				
				for (Player aPlayer : GameObjectsStorage.getAllPlayersForIterate())
					aPlayer.sendPacket(redSky, eq);
				
				SkillData skill = new SkillData(6176, 1);
				if(skill.exists())
					regNpcInstance.getOwner().broadcastPacketToOthers(new MagicSkillUse(regNpcInstance.getOwner(), regNpcInstance.getOwner(), 6176, 1, 0, 500));
				
				print("NPC spawned to cords " + data.getLoc().getX() + ", " + data.getLoc().getY() + ", " + data.getLoc().getZ() + "; objId = " + data.getObjectId());
			}
			catch (Exception e)
			{
				e.printStackTrace();
				print("error spawning NPC, " + NexusLoader.getTraceString(e.getStackTrace()));
			}
		}
	}
	
	public void unspawnRegNpc()
	{
		/**/ if(NexusLoader.detailedDebug) print("unspawnRegNpc()");
		
		if(regNpcInstance != null)
		{
			/**/ if(NexusLoader.detailedDebug) print("regNpcInstance is not null, unspawning it...");
			
			regNpcInstance.deleteMe();
			regNpcInstance = null;
		}
		else
			/**/ if(NexusLoader.detailedDebug) print("regNpcInstance is NULL!");
		
		regNpc = null;
	}
	
	public synchronized void skipDelay(PlayerEventInfo gm)
	{
		/**/ if(NexusLoader.detailedDebug) print("skipping event delay... ");
		
		if(_state == State.IDLE)
		{
			/**/ if(NexusLoader.detailedDebug) print("state is idle, can't skip delay");
			
			gm.sendMessage("There's no active event atm.");
			return;
		}
		else if(_state == State.REGISTERING)
		{
			/**/ if(NexusLoader.detailedDebug) print("state is registering, skipping delay...");
			
			if(_regCountdownFuture != null)
				_regCountdownFuture.cancel(false);
			
			if(taskFuture != null)
				taskFuture.cancel(false);
			
			_counter = 0;
			_regCountdownFuture = CallBack.getInstance().getOut().scheduleGeneral(_regCountdown, 1);
			
			/**/ if(NexusLoader.detailedDebug) print("delay successfully skipped");
		}
		else
		{
			gm.sendMessage("The event can skip waiting delay only when it's in the registration state.");
			
			/**/ if(NexusLoader.detailedDebug) print("can't skip delay, state is " + _state.toString());
		}
	}
	
	public void watchEvent(PlayerEventInfo gm, int instanceId)
	{
		AbstractMainEvent event = current;
		if(event == null)
		{
			gm.sendMessage("No event is available now.");
			return;
		}
		
		try
		{
			event.addSpectator(gm, instanceId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			gm.sendMessage("Event cannot be spectated now. Please try it again later.");
		}
	}
	
	public void stopWatching(PlayerEventInfo gm)
	{
		AbstractMainEvent event = current;
		if(event == null)
		{
			gm.sendMessage("No event is available now.");
			return;
		}
		
		event.removeSpectator(gm);
	}
	
	public synchronized void abort(PlayerEventInfo gm, boolean error)
	{
		/**/ if(NexusLoader.detailedDebug) print("MainEventManager.abort(), error = " + error);
		
		if(error)
		{
			/**/ if(NexusLoader.detailedDebug) print("aborting due to error...");
			
			unspawnRegNpc();
			
			try
			{
				current.clearEvent();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				clean(null);
				
				/**/ if(NexusLoader.detailedDebug) print("error while aborting - " + NexusLoader.getTraceString(e.getStackTrace()));
			}
		}
		else
		{
			/**/ if(NexusLoader.detailedDebug) print("aborting due to GM... - _state = " + _state.toString());
			
			if(_state == State.REGISTERING)
			{
				/**/ if(NexusLoader.detailedDebug) print("aborting while in registering state");
				
				NexusLoader.debug("Event aborted by GM");
				
				unspawnRegNpc();
				
				current.clearEvent();
				announce(LanguageEngine.getMsg("announce_regAborted"));
				
				_regCountdown.abort();
				
				/**/ if(NexusLoader.detailedDebug) print("event (in registration) successfully aborted");
			}
			else if(_state == State.RUNNING)
			{
				/**/ if(NexusLoader.detailedDebug) print("aborting while in running state");
				
				unspawnRegNpc();
				
				if(current != null)
					current.clearEvent();
				else
					clean("in RUNNING state after current was null!!!");
				
				announce(LanguageEngine.getMsg("announce_eventAborted"));
				
				/**/ if(NexusLoader.detailedDebug) print("event (in runtime) successfully aborted");
			}
			else 
			{
				/**/ if(NexusLoader.detailedDebug) print("can't abort event now!");
				
				if (gm != null)
				{
					gm.sendMessage("Event cannot be aborted now.");
					return;
				}
			}
		}
		
		/**/ if(NexusLoader.detailedDebug) print("MainEventManager.abort() finished");
		
		if(!autoSchedulerPaused() && autoSchedulerEnabled())
		{
			scheduler.schedule(-1, false);
			
			/**/ if(NexusLoader.detailedDebug) print("scheduler enabled, scheduling next event...");
		}
	}
	
	public void endDueToError(String text)
	{
		/**/ if(NexusLoader.detailedDebug) print("starting MainEventManager.endDueToError(): " + text);
		
		announce(text);
		
		abort(null, true);
		
		/**/ if(NexusLoader.detailedDebug) print("finished MainEventManager.endDueToError()");
	}
	
	public void end()
	{
		/**/ if(NexusLoader.detailedDebug) print("started MainEventManager.end()");
		
		_state = State.TELE_BACK;
		schedule(1);
		
		/**/ if(NexusLoader.detailedDebug) print("finished MainEventManager.end()");
	}
	
	private void schedule(int time)
	{
		/**/ if(NexusLoader.detailedDebug) print("MainEventManager.schedule(): " + time);
		
		taskFuture = CallBack.getInstance().getOut().scheduleGeneral(_task, time);
	}
	
	public void announce(String text)
	{
		String announcer = "Event Engine";
		
		if(current != null)
			announcer = current.getEventType().getAltTitle();
		
		/**/ if(NexusLoader.detailedDebug) print("MainEventManager.announce(): '" + text + "' announcer = " + announcer);
		
		CallBack.getInstance().getOut().announceToAllScreenMessage(text, announcer);
	}

	public void announce(String text, String announcer)
	{
		if(announcer == null)
			announcer = "Event Engine";

		CallBack.getInstance().getOut().announceToAllScreenMessage(text, announcer);
	}

	public List<PlayerEventInfo> getPlayers()
	{
		return _players;
	}
	
	public int getCounter()
	{
		return _counter;
	}
	
	public String getTimeLeft(boolean digitalClockFormat)
	{
		try
		{
			if(_state == State.REGISTERING)
			{
				if(digitalClockFormat)
					return _regCountdown.getTimeAdmin();
				else 
					return _regCountdown.getTime();
			}
			else if(_state == State.RUNNING)
			{
				return current.getEstimatedTimeLeft();
			}
			else
				return "N/A";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "<font color=AE0000>Event error</font>";
		}
	}
	
	public String getMapName()
	{
		if(activeMap == null)
			return "N/A";
		else
			return activeMap.getMapName();
	}
	
	public String getMapDesc()
	{
		if(activeMap == null)
			return "N/A";
		else
			if(activeMap.getMapDesc() == null || activeMap.getMapDesc().length() == 0)
			{
				return "This map has no description.";
			}
			else
				return activeMap.getMapDesc();
	}
	
	public EventMap getMap()
	{
		return activeMap;
	}
	
	public int getRunTime()
	{
		return eventRunTime == 0 ? 120 : eventRunTime;
	}
	
	public State getState()
	{
		return _state;
	}
	
	private void msgToAll(String text)
	{
		/**/ if(NexusLoader.detailedDebug) print("MainEventManager.msgToAll(): " + text);
		
		for (PlayerEventInfo player : _players)
			player.sendMessage(text);
	}
	
	public void paralizeAll(boolean para)
	{
		try
		{
			/**/ if(NexusLoader.detailedDebug) print("paralyze all called, para = " + para);
			
			for(PlayerEventInfo player : _players)
			{
				if(player.isOnline())
				{
					if (para)
						player.block();
					else
						player.unblock();
					
					player.setIsParalyzed(para);
					player.setIsInvul(para);
					
					player.paralizeEffect(para);
				}
			}
		}
		catch (NullPointerException e)
		{
			/**/ if(NexusLoader.detailedDebug) print("error while paralyzing, " + NexusLoader.getTraceString(e.getStackTrace()));
		}
	}
	
	public boolean canRegister(PlayerEventInfo player, boolean start)
	{
		if (player.getLevel() > current.getInt("maxLvl"))
		{
			player.sendMessage(LanguageEngine.getMsg("registering_highLevel"));
			return false;
		}
		
		if (player.getLevel() < current.getInt("minLvl"))
		{
			player.sendMessage(LanguageEngine.getMsg("registering_lowLevel"));
			return false;
		}
		
		if (!player.isGM() && start && current.getBoolean("dualboxCheck") && dualboxDetected(player, current.getInt("maxPlayersPerIp")))
		{
			player.sendMessage(LanguageEngine.getMsg("registering_sameIp"));
			return false;
		}
		else
		{
			if (!player.isGM() && start && current.getBoolean("dualboxCheckHwidSupport") && dualboxDetectedHWID(player, current.getInt("maxHwidAllowed")))
			{
				player.sendMessage(LanguageEngine.getMsg("registering_sameHWID"));
				return false;
			}
		}
		
		if(!EventManager.getInstance().canRegister(player))
		{
			player.sendMessage(LanguageEngine.getMsg("registering_status"));
			return false;
		}
		
		return true;
	}
	
	public boolean registerPlayer(PlayerEventInfo player)
	{
		/**/ if(NexusLoader.detailedDebug) print(". starting registerPlayer() for " + player.getPlayersName());
		
		if (_state != State.REGISTERING)
		{
			player.sendMessage(LanguageEngine.getMsg("registering_notRegState"));
			return false;
		}
		
		if (player.isRegistered())
		{
			player.sendMessage(LanguageEngine.getMsg("registering_alreadyRegistered"));
			return false;
		}
		
		int i = EventWarnings.getInstance().getPoints(player);
		if(i >= EventWarnings.MAX_WARNINGS && !player.isGM())
		{
			player.sendMessage(LanguageEngine.getMsg("registering_warningPoints", EventWarnings.MAX_WARNINGS, i));
			
			/**/ if(NexusLoader.detailedDebug) print("... registerPlayer() for " + player.getPlayersName() + ", player has too many warnings! (" + i + ")");
			return false;
		}
		
		if (canRegister(player, true))
		{
			if(!getCurrent().canRegister(player))
			{
				/**/ if(NexusLoader.detailedDebug) print("... registerPlayer() for " + player.getPlayersName() + ", player failed to register on event, event itself didn't allow so!");
				
				player.sendMessage(LanguageEngine.getMsg("registering_notAllowed"));
				return false;
			}
			
			if(EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer"))
			{
				if (!EventBuffer.getInstance().hasBuffs(player))
				{
					player.sendMessage(LanguageEngine.getMsg("registering_buffs"));
				}
				
				EventManager.getInstance().getHtmlManager().showSelectSchemeForEventWindow(player, "main", getCurrent().getEventType().getAltTitle());
			}
			
			player.sendMessage(LanguageEngine.getMsg("registering_registered"));
			
			PlayerEventInfo pi = CallBack.getInstance().getPlayerBase().addInfo(player);
			pi.setIsRegisteredToMainEvent(true, current.getEventType(), (current.getMaxGearScore() > 0));

			synchronized(_players)
			{
				_players.add(pi);
			}
			
			/**/ if(NexusLoader.detailedDebug) print("... registerPlayer() for " + player.getPlayersName() + ", player has been registered!");
			
			return true;
		}
		else
		{
			/**/ if(NexusLoader.detailedDebug) print("... registerPlayer() for " + player.getPlayersName() + ", player failed to register on event, manager didn't allow so!");
			
			player.sendMessage(LanguageEngine.getMsg("registering_fail"));
			return false;
		}
	}
	
	public boolean unregisterPlayer(PlayerEventInfo player, boolean force)
	{
		if(player == null)
			return false;
		
		/**/ if(NexusLoader.detailedDebug) print(". starting unregisterPlayer() for " + player.getPlayersName() + ", force = " + force);
		
		if(!EventConfig.getInstance().getGlobalConfigBoolean("enableUnregistrations"))
		{
			/**/ if(NexusLoader.detailedDebug) print("... unregisterPlayer()  - unregistrations are not allowed here!");
			
			if(!force) player.sendMessage(LanguageEngine.getMsg("unregistering_cantUnregister"));
			return false;
		}
		
		if (!_players.contains(player))
		{
			/**/ if(NexusLoader.detailedDebug) print("... unregisterPlayer() for " + player.getPlayersName() + " player is not registered");
			
			if(!force) player.sendMessage(LanguageEngine.getMsg("unregistering_notRegistered"));
			return false;
		}
		
		if (_state != State.REGISTERING && !force)
		{
			/**/ if(NexusLoader.detailedDebug) print("... unregisterPlayer() for " + player.getPlayersName() + " player can't unregister now, becuase _state = " + _state.toString());
			
			player.sendMessage(LanguageEngine.getMsg("unregistering_cant"));
			return false;
		}
		else
		{
			player.sendMessage(LanguageEngine.getMsg("unregistering_unregistered"));
			
			player.setIsRegisteredToMainEvent(false, null, (current.getMaxGearScore() > 0));
			CallBack.getInstance().getPlayerBase().eventEnd(player);
			
			synchronized(_players)
			{
				_players.remove(player);
			}
			
			/**/ if(NexusLoader.detailedDebug) print("... unregisterPlayer() for " + player.getPlayersName() + " player has been unregistered");
			
			if(current != null)
				current.playerUnregistered(player);
			
			return true;
		}
	}
	
	public class RegNpcLoc
	{
		public String name;
		public int[] cords;
		
		public RegNpcLoc(String name, int[] cords)
		{
			this.name = name;
			this.cords = cords;
		}
	}
	
	public boolean dualboxDetected(PlayerEventInfo player)
	{
		if (player == null)
			return false;
		
		if(!player.isOnline(true))
			return false;
		
		if (player.getIpDualboxAllowed())
			return true;
		
		String ip1 = player.getIp();
		
		if(ip1 == null)
			return false;
		
        String ip2;
        
        for(PlayerEventInfo p : _players)
        {
        	ip2 = p.getIp();
        	
        	if(ip2 != null && ip1.equals(ip2))
        	{
				/**/ if(NexusLoader.detailedDebug) print("... MainEventManager.dualboxDetected() for " + player.getPlayersName() + ", found dualbox for IP " + player.getIp());
        		return true;
        	}
        }
        
        return false;
	}
	
	public boolean dualboxDetectedHWID(PlayerEventInfo player)
	{
		if (player == null)
			return false;
		
		if(!player.isOnline(true))
			return false;
		
		if (player.getHWIDDualboxAllowed())
			return true;
		
		String hwid1 = player.getHWID();
		
		if(hwid1 == null)
			return false;
		
        String hwid2;
        
        for(PlayerEventInfo p : _players)
        {
        	hwid2 = p.getHWID();
        	
        	if(hwid2 != null && hwid1.equalsIgnoreCase(hwid2))
        	{
				/**/ if(NexusLoader.detailedDebug) print("... MainEventManager.dualboxDetectedHWID() for " + player.getPlayersName());
        		return true;
        	}
        }
        
        return false;
	}
	
	public boolean dualboxDetectedHWID(PlayerEventInfo player, int maxHwid)
	{
		if (player == null)
			return false;
		
		if(!player.isOnline(true))
			return false;
		
		if (player.getHWIDDualboxAllowed())
			return true;
		
		int occurences = 0;
		String hwid1 = player.getHWID();
		
		if(hwid1 == null)
			return false;
		
		String hwid2;
		for(PlayerEventInfo p : _players)
		{
			hwid2 = p.getHWID();
			
			if(hwid2 != null && hwid1.equalsIgnoreCase(hwid2))
			{
				occurences ++;
			}
		}
		
		if(occurences >= maxHwid)
		{
			/**/ if(NexusLoader.detailedDebug) print("... MainEventManager.dualboxDetected() for " + player.getPlayersName() + ", found dualbox for HWID (method 2) " + player.getHWID() + " maxHwid " + maxHwid + " occurences = " + occurences);
			return true;
		}
		
		return false;
	}
	
	public boolean dualboxDetected(PlayerEventInfo player, int maxPerIp)
	{
		if (player == null)
			return false;
		
		if(!player.isOnline(true))
			return false;
		
		if (player.getIpDualboxAllowed())
			return true;
		
		int occurences = 0;
		String ip1 = player.getIp();
		
		if(ip1 == null)
			return false;
		
		String ip2;
		for(PlayerEventInfo p : _players)
		{
			ip2 = p.getIp();
			
			if(ip2 != null && ip1.equals(ip2))
			{
				occurences ++;
			}
		}
		
		if(occurences >= maxPerIp)
		{
			/**/ if(NexusLoader.detailedDebug) print("... MainEventManager.dualboxDetected() for " + player.getPlayersName() + ", found dualbox for IP (method 2) " + player.getIp() + " maxPerIp " + maxPerIp + " occurences = " + occurences);
			return true;
		}
		
		return false;
	}
	
	public AbstractMainEvent getCurrent()
	{
		return current;
	}
	
	public int getPlayersCount()
	{
		return _players.size();
	}

	private int _counterTillNewbieEvent;
	public static int NEWBIEEVENT_AFTER = 3;
	
	public class EventScheduler implements Runnable
	{
		private ScheduledFuture<?> _future;
		
		@Override
		public void run()
		{
			try
			{
				boolean selected = false;
				
				for(int i = 0; i < EventType.values().length; i++)
				{
					NexusLoader.debug("Trying to find an event to be started:", Level.INFO);

					boolean newbieOnlyEvent = false;

					if(_counterTillNewbieEvent >= NEWBIEEVENT_AFTER)
					{
						_counterTillNewbieEvent = 0;
						newbieOnlyEvent = true;
					}
					else
						_counterTillNewbieEvent++;

					EventType next = null;
					if(newbieOnlyEvent)
					{
						// 25% chance for CTF
						if(CallBack.getInstance().getOut().random(100) < 25)
						{
							next = EventType.CTF;
						}
						// 15% chance for DM
						else if(CallBack.getInstance().getOut().random(100) < 40)
						{
							next = EventType.DM;
						}
						// 60% for TvT
						else
							next = EventType.TvT;
					}
					else
						next = EventType.getNextRegularEvent();
					
					if(next == null)
					{
						/**/ if(NexusLoader.detailedDebug) print("no next event is available. stopping it here, pausing scheduler");
						
						NexusLoader.debug("No next event is aviaible!", Level.INFO);
						if(!autoSchedulerPaused())
							schedule(-1, false);
						
						break;
					}
					
					EventMap nextMap = null;
					
					AbstractMainEvent event = EventManager.getInstance().getMainEvent(next);
					
					List<EventMap> maps = new FastTable<EventMap>();
					maps.addAll(EventMapSystem.getInstance().getMaps(next).values());
					Collections.shuffle(maps);
					
					for(EventMap map : maps)
					{
						if(event.canRun(map))
						{
							nextMap = map;
							break;
						}
					}
					
					if(nextMap == null) // start another event
					{
						continue;
					}
					
					selected = true;

					int runTime = EventConfig.getInstance().getGlobalConfigInt("defaultRunTime");
					
					if(next == EventType.Zombies || next == EventType.TreasureHunt || next == EventType.TreasureHuntPvp)
						runTime = 10;
					
					startEvent(null, next, EventConfig.getInstance().getGlobalConfigInt("defaultRegTime"), nextMap.getMapName(), null, runTime, newbieOnlyEvent);
					break;
				}
				
				if(!selected)
				{
					NexusLoader.debug("No event could be started. Check if you have any maps for them and if they are configured properly.");
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		public boolean abort()
		{
			/**/ if(NexusLoader.detailedDebug) print("aborting event scheduler");
			
			if(_future != null)
			{
				_future.cancel(false);
				_future = null;
				return true;
			}
			return false;
		}
		
		public void schedule(double delay, boolean firstStart)
		{
			if(!EventConfig.getInstance().getGlobalConfigBoolean("enableAutomaticScheduler"))
				return;
			
			
			if(_future != null)
			{
				_future.cancel(false);
				_future = null;
			}
			
			autoScheduler = true;
			
			if(current == null)
			{
				if(firstStart)
				{
					delay = EventConfig.getInstance().getGlobalConfigInt("firstEventDelay") * 60000;
					_future = CallBack.getInstance().getOut().scheduleGeneral(this, (long)delay);
				}
				else
				{
					if(delay > -1)
					{
						_future = CallBack.getInstance().getOut().scheduleGeneral(this, (long)delay * 1000);
					}
					else
					{
						delay = EventConfig.getInstance().getGlobalConfigInt("delayBetweenEvents") * 60000;
						_future = CallBack.getInstance().getOut().scheduleGeneral(this, (long)delay);
					}
				}
				
				/**/ if(NexusLoader.detailedDebug) print("scheduling next event in " + Math.round(delay / 60000) + " minutes.");
				
				NexusLoader.debug("Next event in " + Math.round(delay / 60000) + " minutes.", Level.INFO);
			}
			else
			{
				NexusLoader.debug("Automatic scheduler reeanbled.");
				
				/**/ if(NexusLoader.detailedDebug) print("reenabling automatic scheduler");				
			}
		}
	}
	
	
	public void abortAutoScheduler(PlayerEventInfo gm)
	{
		if(autoSchedulerPaused())
			unpauseAutoScheduler(gm, false);
		
		if(scheduler.abort())
		{
			if(gm != null)
				gm.sendMessage("Automatic event scheduling has been disabled");
			
			NexusLoader.debug("Automatic scheduler disabled" + (gm != null ? " by a GM." : "."), Level.INFO);
		}
		
		/**/ if(NexusLoader.detailedDebug) print("aborting auto scheduler, gm is null? " + (gm == null));
		
		autoScheduler = false;
	}
	
	public void pauseAutoScheduler(PlayerEventInfo gm)
	{
		if(!EventConfig.getInstance().getGlobalConfigBoolean("enableAutomaticScheduler"))
		{
			gm.sendMessage("The automatic event scheduler has been disabled in configs.");
			return;
		}
		
		if(scheduler == null)
			return;
		
		if(getCurrent() != null)
		{
			gm.sendMessage("There's no pausable delay. Wait till the event ends.");
			return;
		}
		
		if(!autoSchedulerPaused() && autoSchedulerEnabled())
		{
			if(scheduler._future == null)
			{
				gm.sendMessage("Cannot pause the scheduler now.");
				return;
			}
			
			if(scheduler._future.getDelay(TimeUnit.SECONDS) < 2)
			{
				gm.sendMessage("Cannot pause now. Event starts in less than 2 seconds.");
				return;
			}
			
			pausedTimeLeft = scheduler._future.getDelay(TimeUnit.SECONDS);
			scheduler.abort();
			
			NexusLoader.debug("Automatic scheduler paused" + (gm != null ? " by a GM." : "."), Level.INFO);
		}
		else
			gm.sendMessage("The scheduler must be enabled.");
	}
	
	public void unpauseAutoScheduler(PlayerEventInfo gm, boolean run)
	{
		if(!EventConfig.getInstance().getGlobalConfigBoolean("enableAutomaticScheduler"))
		{
			gm.sendMessage("The automatic event scheduler has been disabled in configs.");
			return;
		}
		
		if(scheduler == null)
			return;
		
		if(getCurrent() != null)
		{
			gm.sendMessage("An event is already running.");
			return;
		}
		
		if(autoSchedulerPaused())
		{
			if(run)
			{
				scheduler.schedule(pausedTimeLeft, false);
				NexusLoader.debug("Automatic scheduler continues (event in " + pausedTimeLeft + " seconds) again after being paused" + (gm != null ? " by a GM." : "."), Level.INFO);
			}
			else
				NexusLoader.debug("Automatic scheduler unpaused " + (gm != null ? " by a GM." : "."), Level.INFO);
			
			pausedTimeLeft = 0;
		}
		else if(gm != null)
			gm.sendMessage("The scheduler is not paused.");
	}
	
	public void restartAutoScheduler(PlayerEventInfo gm)
	{
		if(!EventConfig.getInstance().getGlobalConfigBoolean("enableAutomaticScheduler"))
		{
			gm.sendMessage("The automatic event scheduler has been disabled in configs.");
			return;
		}
		
		if(autoSchedulerPaused())
		{
			unpauseAutoScheduler(gm, true);
		}
		else
		{
			NexusLoader.debug("Automatic scheduler enabled" + (gm != null ? " by a GM." : "."), Level.INFO);
			scheduler.schedule(-1, false);
		}
		
		if(gm != null && current == null)
			gm.sendMessage("Automatic event scheduling has been enabled. Next event in " + EventConfig.getInstance().getGlobalConfigInt("delayBetweenEvents") + " minutes.");
	}
	
	public EventScheduler getScheduler()
	{
		return scheduler;
	}
	
	public boolean autoSchedulerEnabled()
	{
		return autoScheduler;
	}
	
	public boolean autoSchedulerPaused()
	{
		return pausedTimeLeft > 0;
	}
	
	public String getAutoSchedulerDelay()
	{
		double d = 0;
		if(scheduler._future != null && !scheduler._future.isDone())
			d = scheduler._future.getDelay(TimeUnit.SECONDS);

		if(autoSchedulerPaused())
			d = pausedTimeLeft;
		
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
	
	public String getLastEventTime()
	{
		if(lastEvent == 0)
			return "N/A";
		
		long time = System.currentTimeMillis();
		
		long diff = time - lastEvent;
		if(diff > 1000)
			diff /= 1000;
		else return "< 1 sec ago";
		
		if(diff > 60)
		{
			diff /= 60;
			if(diff > 60)
				diff /= 60;
			else
				return (diff + " min ago");
		}
		else 
			return (diff + " sec ago");
		
		return (diff + " hours ago");
	}
	
	public List<EventScheduleData> getEventScheduleData()
	{
		return _eventScheduleData;
	}
	
	public EventType nextAvailableEvent(boolean testOnly)
	{
		EventType event = null;
		int lastOrder = 0;
		
		if(_lastEvent != null)
		{
			for(EventScheduleData d : _eventScheduleData)
			{
				if(d.getEvent() == _lastEvent)
				{
					lastOrder = d.getOrder();
				}
			}
		}
		
		int limit = _eventScheduleData.size()*2;
		
		if(_eventScheduleData.isEmpty())
			return null;
		
		while(event == null)
		{
			for(EventScheduleData d : _eventScheduleData)
			{
				if(d.getOrder() == lastOrder+1)
				{
					// check if event can run (has maps, isn't disabled, etc.
					if(d.getEvent().isRegularEvent() && EventConfig.getInstance().isEventAllowed(d.getEvent()) && EventManager.getInstance().getMainEvent(d.getEvent()) != null && EventMapSystem.getInstance().getMapsCount(d.getEvent()) > 0)
					{
						// chance check
						if(testOnly || CallBack.getInstance().getOut().random(100) < d.getChance())
						{
							event = d.getEvent();
							
							if(!testOnly)
								_lastEvent = event;
							break;
						}
					}
				}
			}
			
			if(--limit <= 0)
				break;
			else
			{
				if(lastOrder > _eventScheduleData.size())
					lastOrder = 0; // go again from start
				else
					lastOrder ++;
			}
		}
		
		return event;
	}
	
	public EventScheduleData getScheduleData(EventType type)
	{
		for(EventScheduleData d : _eventScheduleData)
		{
			if(d.getEvent().equals(type))
				return d;
		}
		return null;
	}
	
	public EventType getLastEventOrder()
	{
		return _lastEvent;
	}
	
	public EventType getGuessedNextEvent()
	{
		return nextAvailableEvent(true);
	}
	
	private void addScheduleData(EventType type, int order, int chance, boolean updateInDb)
	{
		if(type == null)
			return;
		
		boolean selectOrder = false;
		
		if(order == -1 || order > _eventScheduleData.size())
			selectOrder = true;
		else
		{
			for(EventScheduleData d : _eventScheduleData)
			{
				if(d.getOrder() == order)
				{
					selectOrder = true;
					break;
				}
			}
		}
		
		if(selectOrder)
		{
			int freeOrder = -1;
			boolean found;
			
			for(int i = 0; i < _eventScheduleData.size(); i++)
			{
				found = false;
				for(EventScheduleData d : _eventScheduleData)
				{
					if(d.getOrder() == i+1)
					{
						found = true;
						break;
					}
				}
				
				if(!found)
				{
					freeOrder = i+1;
					break;
				}
			}
			
			if(freeOrder == -1)
			{
				int highest = 0;
				for(EventScheduleData d : _eventScheduleData)
				{
					if(d.getOrder() > highest)
						highest = d.getOrder();
				}
				order = highest + 1;
			}
			else
			{
				order = freeOrder;
			}
		}
		
		boolean add = true;
		for(EventScheduleData d : _eventScheduleData)
		{
			if(d.getEvent() == type)
			{
				add = false;
				break;
			}
		}
		
		if(add)
		{
			EventScheduleData data = new EventScheduleData(type, order, chance);
			_eventScheduleData.add(data);
		}
		
		if(selectOrder)
		{
			saveScheduleData(type);
			
			if(updateInDb)
			{
				if(order != -1)
					NexusLoader.debug("Adding wrong-configured/missing " + type.getAltTitle() + " event to EventOrder system with order " + order);
				else
					NexusLoader.debug("Error adding " + type.getAltTitle() + " event to EventOrder system");
			}
		}
	}
	
	public void loadScheduleData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			
			statement = con.prepareStatement("SELECT * FROM nexus_eventorder ORDER BY eventOrder ASC");
			rset = statement.executeQuery();
			
			while(rset.next())
			{
				String event = rset.getString("event");
				int order = rset.getInt("eventOrder");
				int chance = rset.getInt("chance");
				
				for(EventScheduleData d : _eventScheduleData)
				{
					if(d.getOrder() == order)
					{
						NexusLoader.debug("Duplicate order in EventOrder system for event " + event, Level.WARNING);
						order = -1;
					}
				}
				
				addScheduleData(EventType.getType(event), order, chance, false);
			}
		}
		
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		for(EventType type : EventType.values())
		{
			if(type.isRegularEvent() && type != EventType.Unassigned && EventManager.getInstance().getEvent(type) != null)
			{
				if(getScheduleData(type) == null)
				{
					addScheduleData(type, -1, 100, true);
				}
			}
		}
	}
	
	public int saveScheduleData(EventType event)
	{
		Connection con = null;
		PreparedStatement statement = null;

		EventScheduleData data = getScheduleData(event);
		
		if(data == null)
			return -1;
		
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			
			statement = con.prepareStatement("REPLACE INTO nexus_eventorder VALUES (?,?,?)");
			statement.setString(1, data.getEvent().getAltTitle());
			statement.setInt(2, data.getOrder());
			statement.setInt(3, data.getChance());
			
			statement.execute();
		}
		
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		
		return data._order;
	}
	
	public class EventScheduleData
	{
		private final EventType _event;
		private int _order;
		private int _chance;
		
		private EventScheduleData(EventType event, int order, int chance)
		{
			_event = event;
			_order = order;
			_chance = chance;
		}
		
		public EventType getEvent()
		{
			return _event;
		}
		
		public int getOrder()
		{
			return _order;
		}
		
		public void setOrder(int c)
		{
			_order = c;
		}
		
		public int getChance()
		{
			return _chance;
		}
		
		public void setChance(int c)
		{
			_chance = c;
		}
		
		public boolean decreaseOrder()
		{
			boolean done = false;
			for(EventScheduleData d : _eventScheduleData)
			{
				if(d.getEvent() != getEvent() && d.getOrder() == _order+1)
				{
					d.setOrder(_order);
					_order ++;
					
					saveScheduleData(d.getEvent());
					saveScheduleData(getEvent());
					
					done = true;
					break;
				}
			}
			
			return done;
		}
		
		public boolean raiseOrder()
		{
			boolean done = false;
			for(EventScheduleData d : _eventScheduleData)
			{
				if(d.getEvent() != getEvent() && d.getOrder() == _order-1)
				{
					d.setOrder(_order);
					_order --;
					
					saveScheduleData(d.getEvent());
					saveScheduleData(getEvent());
					
					done = true;
					break;
				}
			}
			
			return done;
		}
	}
	
	private class RegistrationCountdown implements Runnable
	{
		private String getTimeAdmin()
		{
			String mins = "" + _counter / 60;
			String secs = (_counter % 60 < 10 ? "0" + _counter % 60 : "" + _counter % 60);
			return "" + mins + ":" + secs + "";
		}
		
		private String getTime()
		{
			if(_counter > 60)
			{
				int min = _counter/60;
				if(min < 1) min = 1;
				return min + " minutes";
			}
			else
			{
				return _counter + " seconds";
			}
		}

		@Override
		@SuppressWarnings("synthetic-access")
		public void run()
		{
			if (_state == State.REGISTERING)
			{
				switch (_counter)
				{
					case 1800:
					case 1200:
					case 600:
					case 300:
					case 60:
						announce(LanguageEngine.getMsg("announce_timeleft_min", _counter/60));
						break;
					case 30:
					case 10:
					case 5:
						announce(LanguageEngine.getMsg("announce_timeleft_sec", _counter));
						break;
				}
			}

			if (_counter == 0)
			{
				/**/ if(NexusLoader.detailedDebug) print("registration coutndown counter 0, scheduling next action");
				
				schedule(1);
			}
			else
			{
				_counter--;
				_regCountdownFuture = CallBack.getInstance().getOut().scheduleGeneral(_regCountdown, 1000);
			}
		}
		
		private void abort()
		{
			/**/ if(NexusLoader.detailedDebug) print("aborting regcoutndown... ");
			
			if(_regCountdownFuture != null)
			{
				/**/ if(NexusLoader.detailedDebug) print("... regCount is not null");
				
				_regCountdownFuture.cancel(false);
				_regCountdownFuture = null;
			}
			else /**/ if(NexusLoader.detailedDebug) print("... regCount is NULL!");
			
			_counter = 0;
		}
	}
	
	private void abortCast()
	{
		/**/ if(NexusLoader.detailedDebug) print("aborting cast of all players on the event");
		
		for(PlayerEventInfo p : _players)
		{
			p.abortCasting();
		}
	}
	
	private class EventTaskScheduler implements Runnable
	{
		@Override
		@SuppressWarnings("synthetic-access")
		public void run()
		{
			switch (_state)
			{
				case REGISTERING:
				{
					/**/ if(NexusLoader.detailedDebug) print("eventtask - ending registration");
					
					announce(LanguageEngine.getMsg("announce_regClosed"));
					NexusLoader.debug("Registration phase ended.");
					
					for(PlayerEventInfo p : _players)
					{
						if(!canRegister(p, false))
							unregisterPlayer(p, true);
					}
					
					/**/ if(NexusLoader.detailedDebug) print("eventtask - players that can't participate were unregistered");

					if (!current.canStart())
					{
						/**/ if(NexusLoader.detailedDebug) print("eventtask - can't start - not enought players - " + _players.size());
						
						NexusLoader.debug("Not enought participants.");
						
						unspawnRegNpc();
						
						current.clearEvent();
						announce(LanguageEngine.getMsg("announce_lackOfParticipants"));
						
						if(!autoSchedulerPaused() && autoSchedulerEnabled())
							scheduler.schedule(-1, false);
					}
					else
					{
						/**/ if(NexusLoader.detailedDebug) print("eventtask - event started");
						
						NexusLoader.debug("Event starts.");
						announce(LanguageEngine.getMsg("announce_started"));
						current.initEvent();
						
						_state = State.RUNNING;
					
						msgToAll(LanguageEngine.getMsg("announce_teleport10sec"));
						msgToAll("Type .respawn if you get stuck to respawn.");
						
						int delay = EventConfig.getInstance().getGlobalConfigInt("teleToEventDelay");
						if(delay <= 0 || delay > 60000)
							delay = 10000;
						
						// Achievement system.
						if (Config.ENABLE_PLAYER_COUNTERS)
						{
							for(PlayerEventInfo pl : _players)
							{
								if (pl == null || pl.getOwner() == null)
									continue;
								
								pl.getOwner().getCounters().addPoint("_Main_Events");
							}
						}
						
						/**/ if(NexusLoader.detailedDebug) print("eventtask - event started, teletoevent delay " + delay);
						
						if(EventConfig.getInstance().getGlobalConfigBoolean("antistuckProtection"))
						{
							/**/ if(NexusLoader.detailedDebug) print("eventtask - anti stuck protection ON");
							
							abortCast();
							
							/**/ if(NexusLoader.detailedDebug) print("eventtask - aborted cast...");
							
							final int fDelay = delay;
							CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
							{
								@Override
								public void run()
								{
									paralizeAll(true);
									
									schedule(fDelay-1000);
								}
							}, 1000);
						}
						else
						{
							/**/ if(NexusLoader.detailedDebug) print("eventtask - anti stuck protection OFF");
							
							paralizeAll(true);
							
							schedule(delay);
							
							/**/ if(NexusLoader.detailedDebug) print("eventtask - scheduled for next state in " + delay);
						}
					}
					break;
				}
				case RUNNING:
				{
					/**/ if(NexusLoader.detailedDebug) print("eventtask - event started, players teleported");
					
					paralizeAll(false);
					
					/**/ if(NexusLoader.detailedDebug) print("eventtask - event started, players unparalyzed");
					
					current.runEvent();
					
					if(current != null)
						current.initMap();
					
					/**/ if(NexusLoader.detailedDebug) print("eventtask - event started, event runned");
					
					/**/ if(NexusLoader.detailedDebug) print("eventtask - event started, map initialized");

					/**/ if(NexusLoader.detailedDebug) print("eventtask - event started, stats given");

					break;
				}
				case TELE_BACK:
				{
					/**/ if(NexusLoader.detailedDebug) print("eventtask - event ending, teleporting back in 10 sec");
					
					// rewards
					current.onEventEnd();
					
					/**/ if(NexusLoader.detailedDebug) print("eventtask - on event end");
					
					msgToAll(LanguageEngine.getMsg("announce_teleportBack10sec"));
					
					_state = State.END;
					NexusLoader.debug("Teleporting back.");
					schedule(10000);
					break;
				}
				case END:
				{	
					/* Infern0 this stuck the server... TODO: why ?
					if(EventConfig.getInstance().getGlobalConfigBoolean("statTrackingEnabled"))
					{
						OldStats.getInstance().applyChanges();
						OldStats.getInstance().tempTable.clear();
						OldStats.getInstance().updateSQL(current.getPlayers(0), current.getEventType().getMainEventId());
					}
					*/ 
					
					/**/ if(NexusLoader.detailedDebug) print("eventtask - event ended, teleporting back NOW!");
					
					unspawnRegNpc();
					
					current.clearEvent(); 
					
					/**/ if(NexusLoader.detailedDebug) print("eventtask - event ended, event cleared!");
					
					announce(LanguageEngine.getMsg("announce_end"));
					
					CallBack.getInstance().getOut().purge();
					
					/**/ if(NexusLoader.detailedDebug) print("eventtask - event ended, after purge");
					
					if(!autoSchedulerPaused() && autoSchedulerEnabled())
						scheduler.schedule(-1, false);
					
					// TODO: Infern0 , vote for next event.
					NexusLoader.debug("Event ended.");
					break;
				}
			}
		}
	}
	
	/**
	 * @param message - null if no announce
	 */
	public void clean(String message)
	{
		/**/ if(NexusLoader.detailedDebug) print("MainEventManager() clean: " + message);
		
		current = null;
		activeMap = null;
		eventRunTime = 0;
		
		_players.clear();
		
		if(message != null)
			announce(message);
		
		_state = State.IDLE;
		
		if(regNpcInstance != null)
		{
			regNpcInstance.deleteMe();
			regNpcInstance = null;
		}
		
		regNpc = null;
		
		lastEvent = System.currentTimeMillis();
		
		//EventManager.getInstance().endAutoevent(true);
		l2r.gameserver.nexus_engine.events.engine.scheduler.EventScheduler.getInstance().startNext(true);
	}
	
	/** detailed debug */
	protected void print(String msg)
	{
		NexusLoader.detailedDebug(msg);
	}
}
