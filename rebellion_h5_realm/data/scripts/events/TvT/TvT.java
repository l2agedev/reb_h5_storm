package events.TvT;

import l2r.commons.geometry.Polygon;
import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.listener.actor.OnMagicUseListener;
import l2r.gameserver.listener.actor.player.OnPlayerExitListener;
import l2r.gameserver.listener.actor.player.OnTeleportListener;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Territory;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.impl.DuelEvent;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.Residence;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.DoorTemplate;
import l2r.gameserver.templates.ZoneTemplate;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.LoggerFactory;

public class TvT extends Functions implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener, OnMagicUseListener
{
	
	private static final org.slf4j.Logger _log = LoggerFactory.getLogger(TvT.class);
	
	private static ScheduledFuture<?> _startTask;
	
	private static final int[] doors = Config.EVENT_TvTOpenCloseDoors;
	
	private static List<Long> players_list1 = new CopyOnWriteArrayList<Long>();
	private static List<Long> players_list2 = new CopyOnWriteArrayList<Long>();
	private static List<Long> live_list1 = new CopyOnWriteArrayList<Long>();
	private static List<Long> live_list2 = new CopyOnWriteArrayList<Long>();
	
	private static int[][] mage_buffs = new int[Config.EVENT_TvTMageBuffs.length][2];
	private static int[][] fighter_buffs = new int[Config.EVENT_TvTFighterBuffs.length][2];
	
	private static int[][] rewards = new int[Config.EVENT_TvTRewards.length][2];
	
	private static Map<Long, Location> playerRestoreCoord = new LinkedHashMap<Long, Location>();
	
	private static Map<Long, String> boxes = new LinkedHashMap<Long, String>();
	
	private static boolean _isRegistrationActive = false;
	private static int _status = 0;
	private static int _time_to_start;
	private static int _category;
	private static int _minLevel;
	private static int _maxLevel;
	private static int _autoContinue = 0;
	private static boolean _active = false;
	private static Skill buff;
	
	private static Reflection reflection = ReflectionManager.TVT_EVENT;
	
	private static ScheduledFuture<?> _endTask;
	
	private static Zone _zone;
	private static Map<String, ZoneTemplate> _zones = new HashMap<String, ZoneTemplate>();
	private static IntObjectMap<DoorTemplate> _doors = new HashIntObjectMap<DoorTemplate>();
	private static ZoneListener _zoneListener = new ZoneListener();
	
	private static Territory team1spawn = new Territory().add(new Polygon().add(149878, 47505).add(150262, 47513).add(150502, 47233).add(150507, 46300).add(150256, 46002).add(149903, 46005).setZmin(-3408).setZmax(-3308));
	
	private static Territory team2spawn = new Territory().add(new Polygon().add(149027, 46005).add(148686, 46003).add(148448, 46302).add(148449, 47231).add(148712, 47516).add(149014, 47527).setZmin(-3408).setZmax(-3308));
	
	@Override
	public void onLoad()
	{
		if (!Config.ENABLE_OLD_TVT)
			return;
		
		CharListenerList.addGlobal(this);
		
		_zones.put("[colosseum_battle]", ZoneHolder.getZone("[colosseum_battle]").getTemplate());
		for (final int doorId : doors)
			_doors.put(doorId, ZoneHolder.getDoor(doorId).getTemplate());
		reflection.init(_doors, _zones);
		
		_zone = reflection.getZone("[colosseum_battle]");
		
		_active = ServerVariables.getString("TvT", "off").equalsIgnoreCase("on");
		if (isActive())
			scheduleEventStart();
		
		_zone.addListener(_zoneListener);
		
		int i = 0;
		
		if (Config.EVENT_TvTBuffPlayers && Config.EVENT_TvTMageBuffs.length != 0)
			for (String skill : Config.EVENT_TvTMageBuffs)
			{
				String[] splitSkill = skill.split(",");
				mage_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				mage_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}
		
		i = 0;
		
		if (Config.EVENT_TvTBuffPlayers && Config.EVENT_TvTMageBuffs.length != 0)
			for (String skill : Config.EVENT_TvTFighterBuffs)
			{
				String[] splitSkill = skill.split(",");
				fighter_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				fighter_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}
		
		i = 0;
		if (Config.EVENT_TvTRewards.length != 0)
			for (String reward : Config.EVENT_TvTRewards)
			{
				String[] splitReward = reward.split(",");
				rewards[i][0] = Integer.parseInt(splitReward[0]);
				rewards[i][1] = Integer.parseInt(splitReward[1]);
				i++;
			}
		
		_log.info("Loaded Event: TvT");
	}
	
	@Override
	public void onReload()
	{
		if (!Config.ENABLE_OLD_CTF)
			return;
		
		if (_startTask != null)
		{
			_startTask.cancel(false);
			_startTask = null;
		}
	}
	
	@Override
	public void onShutdown()
	{
		onReload();
	}
	
	private static boolean isActive()
	{
		return _active;
	}
	
	public void activateEvent()
	{
		Player player = getSelf();
		
		if (!isActive())
		{
			if (_startTask == null)
				scheduleEventStart();
			ServerVariables.set("TvT", "on");
			_log.info("Event 'TvT' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TvT.AnnounceEventStarted", null);
		}
		else
			player.sendMessage(new CustomMessage("scripts.events.TvT.eventstart", player));
		
		_active = true;
		
		show("admin/events/events.htm", player);
	}
	
	public void deactivateEvent()
	{
		Player player = getSelf();
		
		if (isActive())
		{
			if (_startTask != null)
			{
				_startTask.cancel(false);
				_startTask = null;
			}
			ServerVariables.unset("TvT");
			_log.info("Event 'TvT' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TvT.AnnounceEventStoped", null);
		}
		else
			player.sendMessage(new CustomMessage("scripts.events.TvT.eventstop", player));
		
		_active = false;
		
		show("admin/events/events.htm", player);
	}
	
	public static boolean isRunned()
	{
		return _isRegistrationActive || _status > 0;
	}
	
	public static int getMinLevelForCategory(int category)
	{
		switch (category)
		{
			case 1:
				return 20;
			case 2:
				return 30;
			case 3:
				return 40;
			case 4:
				return 52;
			case 5:
				return 62;
			case 6:
				return 76;
		}
		return 0;
	}
	
	public static int getMaxLevelForCategory(int category)
	{
		switch (category)
		{
			case 1:
				return 29;
			case 2:
				return 39;
			case 3:
				return 51;
			case 4:
				return 61;
			case 5:
				return 75;
			case 6:
				return 85;
		}
		return 0;
	}
	
	public static int getCategory(int level)
	{
		if (level >= 20 && level <= 29)
			return 1;
		else if (level >= 30 && level <= 39)
			return 2;
		else if (level >= 40 && level <= 51)
			return 3;
		else if (level >= 52 && level <= 61)
			return 4;
		else if (level >= 62 && level <= 75)
			return 5;
		else if (level >= 76)
			return 6;
		return 0;
	}
	
	public void start(String[] var)
	{
		Player player = getSelf();
		if (var.length != 2)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}
		
		Integer category;
		Integer autoContinue;
		try
		{
			category = Integer.valueOf(var[0]);
			autoContinue = Integer.valueOf(var[1]);
		}
		catch (Exception e)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}
		
		_category = category;
		_autoContinue = autoContinue;
		
		if (_category == -1)
		{
			_minLevel = 1;
			_maxLevel = 85;
		}
		else
		{
			_minLevel = getMinLevelForCategory(_category);
			_maxLevel = getMaxLevelForCategory(_category);
		}
		
		if (_endTask != null)
		{
			show(new CustomMessage("common.TryLater", player), player);
			return;
		}
		
		_status = 0;
		_isRegistrationActive = true;
		_time_to_start = Config.EVENT_TvTTime;
		
		players_list1 = new CopyOnWriteArrayList<Long>();
		players_list2 = new CopyOnWriteArrayList<Long>();
		live_list1 = new CopyOnWriteArrayList<Long>();
		live_list2 = new CopyOnWriteArrayList<Long>();
		
		playerRestoreCoord = new LinkedHashMap<Long, Location>();
		
		String[] param =
		{
			String.valueOf(_time_to_start),
			String.valueOf(_minLevel),
			String.valueOf(_maxLevel)
		};
		sayToAll("scripts.events.TvT.AnnouncePreStart", param);
		
		executeTask("events.TvT.TvT", "question", new Object[0], 10000);
		executeTask("events.TvT.TvT", "announce", new Object[0], 60000);
	}
	
	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}
	
	public static void question()
	{
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
			if (player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isInOlympiadMode() && !player.isInObserverMode())
				player.scriptRequest(new CustomMessage("scripts.events.TvT.AskPlayer", player).toString(), "events.TvT.TvT:addPlayer", new Object[0]);
	}
	
	public static void announce()
	{
		if (_time_to_start > 1)
		{
			_time_to_start--;
			String[] param =
			{
				String.valueOf(_time_to_start),
				String.valueOf(_minLevel),
				String.valueOf(_maxLevel)
			};
			sayToAll("scripts.events.TvT.AnnouncePreStart", param);
			executeTask("events.TvT.TvT", "announce", new Object[0], 60000);
		}
		else
		{
			if (players_list1.isEmpty() || players_list2.isEmpty() || players_list1.size() < Config.EVENT_TvTMinPlayerInTeam || players_list2.size() < Config.EVENT_TvTMinPlayerInTeam)
			{
				sayToAll("scripts.events.TvT.AnnounceEventCancelled", null);
				_isRegistrationActive = false;
				_status = 0;
				executeTask("events.TvT.TvT", "autoContinue", new Object[0], 10000);
				return;
			}
			else
			{
				_status = 1;
				_isRegistrationActive = false;
				sayToAll("scripts.events.TvT.AnnounceEventStarting", null);
				executeTask("events.TvT.TvT", "prepare", new Object[0], 5000);
			}
		}
	}
	
	public void addPlayer()
	{
		Player player = getSelf();
		if (player == null || !checkPlayer(player, true) || !checkDualBox(player))
			return;
		
		int team = 0, size1 = players_list1.size(), size2 = players_list2.size();
		
		if (size1 == Config.EVENT_TvTMaxPlayerInTeam && size2 == Config.EVENT_TvTMaxPlayerInTeam)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledCount", player), player);
			_isRegistrationActive = false;
			return;
		}
		
		if (!Config.EVENT_TvTAllowMultiReg)
		{
			if ("IP".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod))
				boxes.put(player.getStoredId(), player.getIP());
		}
		
		if (size1 > size2)
			team = 2;
		else if (size1 < size2)
			team = 1;
		else
			team = Rnd.get(1, 2);
		
		if (team == 1)
		{
			players_list1.add(player.getStoredId());
			live_list1.add(player.getStoredId());
			player.setInEvent(true);
			show(new CustomMessage("scripts.events.TvT.Registered", player), player);
		}
		else if (team == 2)
		{
			players_list2.add(player.getStoredId());
			live_list2.add(player.getStoredId());
			player.setInEvent(true);
			show(new CustomMessage("scripts.events.TvT.Registered", player), player);
		}
		else
			_log.info("WTF??? Command id 0 in TvT...");
	}
	
	public static boolean checkPlayer(Player player, boolean first)
	{
		
		if (first && (!_isRegistrationActive || player.isDead()))
		{
			show(new CustomMessage("scripts.events.Late", player), player);
			return false;
		}
		
		if (first && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			show(new CustomMessage("scripts.events.TvT.Cancelled", player), player);
			if (players_list1.contains(player.getStoredId()))
				players_list1.remove(player.getStoredId());
			if (players_list2.contains(player.getStoredId()))
				players_list2.remove(player.getStoredId());
			if (live_list1.contains(player.getStoredId()))
				live_list1.remove(player.getStoredId());
			if (live_list2.contains(player.getStoredId()))
				live_list2.remove(player.getStoredId());
			if (boxes.containsKey(player.getStoredId()))
				boxes.remove(player.getStoredId());
			return false;
		}
		
		if (player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledLevel", player), player);
			return false;
		}
		
		if (player.isMounted())
		{
			show(new CustomMessage("scripts.events.TvT.Cancelled", player), player);
			return false;
		}
		
		if (player.isCursedWeaponEquipped())
		{
			show(new CustomMessage("scripts.events.CtF.Cancelled", player), player);
			return false;
		}
		
		if (player.isInDuel())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledDuel", player), player);
			return false;
		}
		
		if (player.getTeam() != TeamType.NONE)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player), player);
			return false;
		}
		
		if (player.getOlympiadGame() != null || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOlympiad", player), player);
			return false;
		}
		
		if (player.isInParty() && player.getParty().isInDimensionalRift())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player), player);
			return false;
		}
		
		if (player.isInObserverMode())
		{
			show(new CustomMessage("scripts.event.TvT.CancelledObserver", player), player);
			return false;
		}
		
		if (player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledTeleport", player), player);
			return false;
		}
		return true;
	}
	
	public static void prepare()
	{
		ZoneHolder.getDoor(24190002).closeMe();
		ZoneHolder.getDoor(24190003).closeMe();
		
		for (Zone z : reflection.getZones())
			z.setType(ZoneType.peace_zone);
		
		cleanPlayers();
		clearArena();
		executeTask("events.TvT.TvT", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.TvT.TvT", "healPlayers", new Object[0], 2000);
		executeTask("events.TvT.TvT", "teleportPlayersToColiseum", new Object[0], 3000);
		executeTask("events.TvT.TvT", "paralyzePlayers", new Object[0], 4000);
		executeTask("events.TvT.TvT", "buffPlayers", new Object[0], 5000);
		executeTask("events.TvT.TvT", "go", new Object[0], 60000);
		
		sayToAll("scripts.events.TvT.AnnounceFinalCountdown", null);
	}
	
	public static void go()
	{
		_status = 2;
		upParalyzePlayers();
		checkLive();
		clearArena();
		sayToAll("scripts.events.TvT.AnnounceFight", null);
		for (Zone z : reflection.getZones())
			z.setType(ZoneType.battle_zone);
		_endTask = executeTask("events.TvT.TvT", "endBattle", new Object[0], 300000);
	}
	
	public static void endBattle()
	{
		_status = 0;
		removeAura();
		for (Zone z : reflection.getZones())
			z.setType(ZoneType.peace_zone);
		boxes.clear();
		if (live_list1.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins", null);
			giveItemsToWinner(false, true, 1);
		}
		else if (live_list2.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedRedWins", null);
			giveItemsToWinner(true, false, 1);
		}
		else if (live_list1.size() < live_list2.size())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins", null);
			giveItemsToWinner(false, true, 1);
		}
		else if (live_list1.size() > live_list2.size())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedRedWins", null);
			giveItemsToWinner(true, false, 1);
		}
		else if (live_list1.size() == live_list2.size())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedDraw", null);
			giveItemsToWinner(true, true, 0.5);
		}
		
		sayToAll("scripts.events.TvT.AnnounceEnd", null);
		executeTask("events.TvT.TvT", "end", new Object[0], 30000);
		_isRegistrationActive = false;
		if (_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}
	}
	
	public static void end()
	{
		executeTask("events.TvT.TvT", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.TvT.TvT", "healPlayers", new Object[0], 2000);
		executeTask("events.TvT.TvT", "teleportPlayers", new Object[0], 3000);
		executeTask("events.TvT.TvT", "autoContinue", new Object[0], 10000);
	}
	
	public void autoContinue()
	{
		live_list1.clear();
		live_list2.clear();
		players_list1.clear();
		players_list2.clear();
		
		if (_autoContinue > 0)
		{
			if (_autoContinue >= 6)
			{
				_autoContinue = 0;
				return;
			}
			start(new String[]
			{
				"" + (_autoContinue + 1),
				"" + (_autoContinue + 1)
			});
		}
		else
			scheduleEventStart();
	}
	
	public static void giveItemsToWinner(boolean team1, boolean team2, double rate)
	{
		if (team1)
			for (Player player : getPlayers(players_list1))
				for (int i = 0; i < rewards.length; i++)
					addItem(player, rewards[i][0], Math.round((Config.EVENT_TvTrate ? player.getLevel() : 1) * rewards[i][1] * rate));
		if (team2)
			for (Player player : getPlayers(players_list2))
				for (int i = 0; i < rewards.length; i++)
					addItem(player, rewards[i][0], Math.round((Config.EVENT_TvTrate ? player.getLevel() : 1) * rewards[i][1] * rate));
	}
	
	public static void teleportPlayersToColiseum()
	{
		for (Player player : getPlayers(players_list1))
		{
			unRide(player);
			
			if (!Config.EVENT_TvTAllowSummons)
				unSummonPet(player, true);
			
			DuelEvent duel = player.getEvent(DuelEvent.class);
			if (duel != null)
				duel.abortDuel(player);
			
			playerRestoreCoord.put(player.getStoredId(), new Location(player.getX(), player.getY(), player.getZ()));
			
			player.teleToLocation(Territory.getRandomLoc(team1spawn), reflection);
			
			if (!Config.EVENT_TvTAllowBuffs)
			{
				player.getEffectList().stopAllEffects();
				if (player.getPet() != null)
					player.getPet().getEffectList().stopAllEffects();
			}
		}
		
		for (Player player : getPlayers(players_list2))
		{
			unRide(player);
			
			if (!Config.EVENT_TvTAllowSummons)
				unSummonPet(player, true);
			
			playerRestoreCoord.put(player.getStoredId(), new Location(player.getX(), player.getY(), player.getZ()));
			
			player.teleToLocation(Territory.getRandomLoc(team2spawn), reflection);
			
			if (!Config.EVENT_TvTAllowBuffs)
			{
				player.getEffectList().stopAllEffects();
				if (player.getPet() != null)
					player.getPet().getEffectList().stopAllEffects();
			}
		}
	}
	
	public static void teleportPlayers()
	{
		for (Player player : getPlayers(players_list1))
		{
			if (player == null || !playerRestoreCoord.containsKey(player.getStoredId()))
				continue;
			player.teleToLocation(playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
		}
		
		for (Player player : getPlayers(players_list2))
		{
			if (player == null || !playerRestoreCoord.containsKey(player.getStoredId()))
				continue;
			player.teleToLocation(playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
		}
		
		playerRestoreCoord.clear();
		ZoneHolder.getDoor(24190002).openMe();
		ZoneHolder.getDoor(24190003).openMe();
	}
	
	public static void paralyzePlayers()
	{
		for (Player player : getPlayers(players_list1))
		{
			if (player == null)
				continue;
			
			if (!player.isParalyzed())
			{
				player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
				player.startParalyzed();
				player.startAbnormalEffect(AbnormalEffect.ROOT);
			}
			
			if (player.getPet() != null && !player.getPet().isParalyzed())
			{
				player.getPet().startParalyzed();
				player.getPet().startAbnormalEffect(AbnormalEffect.ROOT);
			}
		}
		for (Player player : getPlayers(players_list2))
		{
			
			if (!player.isParalyzed())
			{
				player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
				player.startParalyzed();
				player.startAbnormalEffect(AbnormalEffect.ROOT);
			}
			
			if (player.getPet() != null && !player.getPet().isParalyzed())
			{
				player.getPet().startParalyzed();
				player.getPet().startAbnormalEffect(AbnormalEffect.ROOT);
			}
		}
	}
	
	public static void upParalyzePlayers()
	{
		for (Player player : getPlayers(players_list1))
		{
			if (player.isParalyzed())
			{
				player.stopParalyzed();
				player.stopAbnormalEffect(AbnormalEffect.ROOT);
			}
			
			if (player.getPet() != null && player.getPet().isParalyzed())
			{
				player.getPet().stopParalyzed();
				player.getPet().stopAbnormalEffect(AbnormalEffect.ROOT);
			}
			
		}
		for (Player player : getPlayers(players_list2))
		{
			if (player.isParalyzed())
			{
				player.stopParalyzed();
				player.stopAbnormalEffect(AbnormalEffect.ROOT);
			}
			if (player.getPet() != null && player.getPet().isParalyzed())
			{
				player.getPet().stopParalyzed();
				player.getPet().stopAbnormalEffect(AbnormalEffect.ROOT);
			}
		}
	}
	
	public static void ressurectPlayers()
	{
		for (Player player : getPlayers(players_list1))
			if (player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
		for (Player player : getPlayers(players_list2))
			if (player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
	}
	
	public static void healPlayers()
	{
		for (Player player : getPlayers(players_list1))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
		for (Player player : getPlayers(players_list2))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}
	
	public static void cleanPlayers()
	{
		for (Player player : getPlayers(players_list1))
			if (!checkPlayer(player, false))
				removePlayer(player);
		for (Player player : getPlayers(players_list2))
			if (!checkPlayer(player, false))
				removePlayer(player);
	}
	
	public static void checkLive()
	{
		List<Long> new_live_list1 = new CopyOnWriteArrayList<Long>();
		List<Long> new_live_list2 = new CopyOnWriteArrayList<Long>();
		
		for (Long storeId : live_list1)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if (player != null)
				new_live_list1.add(storeId);
		}
		
		for (Long storeId : live_list2)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if (player != null)
				new_live_list2.add(storeId);
		}
		
		live_list1 = new_live_list1;
		live_list2 = new_live_list2;
		
		for (Player player : getPlayers(live_list1))
			if (player.isInZone(_zone) && !player.isDead() && !player.isLogoutStarted())
				player.setTeam(TeamType.RED);
			else
				loosePlayer(player);
		
		for (Player player : getPlayers(live_list2))
			if (player.isInZone(_zone) && !player.isDead() && !player.isLogoutStarted())
				player.setTeam(TeamType.BLUE);
			else
				loosePlayer(player);
		
		if (live_list1.size() < 1 || live_list2.size() < 1)
			endBattle();
	}
	
	public static void removeAura()
	{
		for (Player player : getPlayers(live_list1))
		{
			player.setTeam(TeamType.NONE);
			if (player.getPet() != null)
				player.getPet().setTeam(TeamType.NONE);
		}
		for (Player player : getPlayers(live_list2))
		{
			player.setTeam(TeamType.NONE);
			if (player.getPet() != null)
				player.getPet().setTeam(TeamType.NONE);
		}
	}
	
	public static void clearArena()
	{
		for (GameObject obj : _zone.getObjects())
			if (obj != null)
			{
				Player player = obj.getPlayer();
				if (player != null && !live_list1.contains(player.getStoredId()) && !live_list2.contains(player.getStoredId()))
					player.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);
			}
	}
	
	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if (_status > 1 && self.isPlayer() && self.getTeam() != TeamType.NONE && (live_list1.contains(self.getStoredId()) || live_list2.contains(self.getStoredId())))
		{
			loosePlayer((Player) self);
			checkLive();
		}
		
	}
	
	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
	{
		if (_zone.checkIfInZone(x, y, z, reflection))
			return;
		
		if (_status > 1 && player != null && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			checkLive();
		}
	}
	
	@Override
	public void onPlayerExit(Player player)
	{
		if (player.getTeam() == TeamType.NONE)
			return;
		
		if (_status == 0 && _isRegistrationActive && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			return;
		}
		
		if (_status == 1 && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			player.teleToLocation(playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
			removePlayer(player);
			return;
		}
		
		if (_status > 1 && player != null && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			checkLive();
		}
	}
	
	private static class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if (cha == null)
				return;
			Player player = cha.getPlayer();
			if (_status > 0 && player != null && !live_list1.contains(player.getStoredId()) && !live_list2.contains(player.getStoredId()))
				player.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if (cha == null)
				return;
			Player player = cha.getPlayer();
			if (_status > 1 && player != null && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
			{
				double angle = Location.convertHeadingToDegree(cha.getHeading()); // СѓРіРѕР» РІ РіСЂР°РґСѓСЃР°С…
				double radian = Math.toRadians(angle - 90); // СѓРіРѕР» РІ СЂР°РґРёР°РЅР°С…
				int x = (int) (cha.getX() + 250 * Math.sin(radian));
				int y = (int) (cha.getY() - 250 * Math.cos(radian));
				int z = cha.getZ();
				player.teleToLocation(x, y, z, reflection);
			}
		}
	}
	
	private static void loosePlayer(Player player)
	{
		if (player != null)
		{
			live_list1.remove(player.getStoredId());
			live_list2.remove(player.getStoredId());
			player.setTeam(TeamType.NONE);
			show(new CustomMessage("scripts.events.TvT.YouLose", player), player);
		}
	}
	
	private static void removePlayer(Player player)
	{
		if (player != null)
		{
			live_list1.remove(player.getStoredId());
			live_list2.remove(player.getStoredId());
			players_list1.remove(player.getStoredId());
			players_list2.remove(player.getStoredId());
			playerRestoreCoord.remove(player.getStoredId());
			player.setInEvent(false);
			
			if (!Config.EVENT_TvTAllowMultiReg)
				boxes.remove(player.getStoredId());
			
			player.setTeam(TeamType.NONE);
		}
	}
	
	private static List<Player> getPlayers(List<Long> list)
	{
		List<Player> result = new ArrayList<Player>();
		for (Long storeId : list)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if (player != null)
				result.add(player);
		}
		return result;
	}
	
	public static void buffPlayers()
	{
		
		for (Player player : getPlayers(players_list1))
		{
			if (player.isMageClass())
				mageBuff(player);
			else
				fighterBuff(player);
		}
		
		for (Player player : getPlayers(players_list2))
		{
			if (player.isMageClass())
				mageBuff(player);
			else
				fighterBuff(player);
		}
	}
	
	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;
			
			for (String timeOfDay : Config.EVENT_TvTStartTime)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				
				String[] splitTimeOfDay = timeOfDay.split(":");
				
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				
				if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
					nextStartTime = testStartTime;
				
				if (_startTask != null)
				{
					_startTask.cancel(false);
					_startTask = null;
				}
				_startTask = ThreadPoolManager.getInstance().schedule(new StartTask(), nextStartTime.getTimeInMillis() - System.currentTimeMillis());
				
			}
			
			currentTime = null;
			nextStartTime = null;
			testStartTime = null;
			
		}
		catch (Exception e)
		{
			_log.warn("TvT: Error figuring out a start time. Check TvTEventInterval in config file.");
		}
	}
	
	public static void mageBuff(Player player)
	{
		for (int i = 0; i < mage_buffs.length; i++)
		{
			buff = SkillTable.getInstance().getInfo(mage_buffs[i][0], mage_buffs[i][1]);
			buff.getEffects(player, player, false, false);
		}
	}
	
	public static void fighterBuff(Player player)
	{
		for (int i = 0; i < fighter_buffs.length; i++)
		{
			buff = SkillTable.getInstance().getInfo(fighter_buffs[i][0], fighter_buffs[i][1]);
			buff.getEffects(player, player, false, false);
		}
	}
	
	private static boolean checkDualBox(Player player)
	{
		if (!Config.EVENT_TvTAllowMultiReg)
		{
			if ("IP".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod))
			{
				if (boxes.containsValue(player.getIP()))
				{
					show(new CustomMessage("scripts.events.TvT.CancelledBox", player), player);
					return false;
				}
			}
		}
		return true;
	}
	
	public class StartTask extends RunnableImpl
	{
		
		@Override
		public void runImpl()
		{
			if (!_active)
				return;
			
			if (isPvPEventStarted())
			{
				_log.info("TvT not started: another event is already running");
				return;
			}
			
			for (Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
				if (c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress())
				{
					_log.debug("TvT not started: CastleSiege in progress");
					return;
				}
			
			if (Config.EVENT_TvTCategories)
				start(new String[]
				{
					"1",
					"1"
				});
			else
				start(new String[]
				{
					"-1",
					"-1"
				});
		}
	}

	@Override
	public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt)
	{
		if(actor.isPlayer() && (getPlayers(players_list1).contains(actor.getStoredId()) || getPlayers(players_list2).contains(actor.getStoredId())))
		{
			for(String skillId : Config.EVENT_TvT_DISALLOWED_SKILLS)
			{
				if(skill.getId() == Integer.parseInt(skillId))
				{
					//TODO: does not support custom message, string already exists: scripts.events.TvT.notallowed
					actor.sendMessage("Action is not allowed.");
					//actor.sendMessage(new CustomMessage("scripts.events.TvT.notallowed", actor));
					return;
				}
			}
		}
		return;
	}
}