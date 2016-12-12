package events.TvT_New;

import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.actor.player.OnPlayerExitListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.GameEvent;
import l2r.gameserver.model.entity.events.GameEventManager;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.Residence;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ChangeWaitType;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.stats.Env;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.utils.GArray;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastTable;
import javolution.util.FastMap;

import org.apache.commons.lang3.ArrayUtils;

public class TvT_New extends GameEvent implements ScriptFile, OnPlayerExitListener
{
	private int _state = 0;
	private static TvT_New _instance;
	private static Logger _log = Logger.getLogger(TvT_New.class.getName());

	private FastMap<Player, Integer> _participants = new FastMap<Player, Integer>();
	private List<String> _registeredHWIDs = new FastTable<String>();
	
	private FastMap<Player, List<Effect>> returnBuffs = new FastMap<Player, List<Effect>>();
	private List<Effect> _effects;
	
	private HashMap<Player, Integer> live_list = new HashMap<Player, Integer>();
	private int[] _score;
	private int curr_round = 1;

	public long startBattle = 0;
	private Reflection _ref;
	private InstantZone _instantZone;

	public TvT_New()
	{
		_instance = this;
	}

	public static TvT_New getInstance()
	{
		if (_instance == null)
			_instance = new TvT_New();
		return _instance;
	}

	@Override
	public int getState()
	{
		return _state;
	}

	@Override
	public String getName()
	{
		return "Team Vs Team";
	}

	public long getNextTime()
	{
		long next_time = getConfigs().START_TIME;

		while (next_time <= System.currentTimeMillis() / 1000)
		{
			getConfigs().START_TIME += 86400;
			setNextEvent();
			next_time = getConfigs().START_TIME;
		}

		return next_time;
	}

	public void setNextEvent()
	{
		if (TvTConfig._configs != null && TvTConfig._configs.size() > 1)
		{
			TvTConfig._configs.sort();
		}
	}

	public Configs getConfigs()
	{
		return (TvTConfig._configs == null || TvTConfig._configs.isEmpty()) ? null : TvTConfig._configs.get(0);
	}

	@Override
	public boolean canUseItem(Player actor, ItemInstance item)
	{
		if(_state == 2)
		{
			if((item.isHeroWeapon() && !getConfigs().ALLOW_HERO_WEAPONS) || ArrayUtils.contains(getConfigs().getRestictId(), item.getItemId()))
			{
				actor.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.NotUseable", actor));
				return false;
			}
		}
		return true;
	}

	@Override
	public void onLoad()
	{
		if (!Config.ENABLE_NEW_TVT)
			return;
		
		TvTConfig.load();
		GameEventManager.getInstance().registerEvent(getInstance());
		_log.info("Loaded Event: Team Vs Team");
		_state = 0;
	}

	@Override
	public void onReload()
	{
		if (_ref != null)
			_ref.clearReflection(1, false);
		if (TvTConfig._configs.size() > 0)
			TvTConfig._configs.clear();
		if (_state != 0)
			finish();
		onLoad();
	}

	@Override
	public void onShutdown()
	{
		if (!Config.ENABLE_NEW_TVT)
			return;
		
		_state = 0;
	}

	@Override
	public boolean register(Player player)
	{
		if (!canRegister(player, true))
			return false;

		player.setPvPTeam(TeamWithMinPlayers());
		_participants.put(player, Integer.valueOf(0));
		if (player.hasHWID())
			_registeredHWIDs.add(player.getHWID());
		player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouRegistred", player, new Object[0]));
		player._event = this;
		return true;
	}

	public void addPlayer()
	{
		registerPlayer();
	}

	public void removePlayer()
	{
		unregisterPlayer();
	}
	
	public void registerPlayer()
	{
		Player player = getSelf();
		GameEvent event = GameEventManager.getInstance().findEvent("Team Vs Team");
		event.register(player);
	}

	public void unregisterPlayer()
	{
		Player player = getSelf();
		GameEvent event = GameEventManager.getInstance().findEvent("Team Vs Team");
		event.unreg(player);
	}
	
	@Override
	public void unreg(Player player)
	{
		if (player == null) 
			return;

		if (_state == 2 || !isParticipant(player))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouCancelRegistration", player, new Object[0]));
			player.setPvPTeam(0);
			player.allowPvPTeam();
			player._event = null;
			return;
		}
		_participants.remove(player);
		_registeredHWIDs.remove(player.getHWID());
		
		player.setPvPTeam(0);
		player.allowPvPTeam();
		player._event = null;
		player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouRegistrationCancelled", player, new Object[0]));
	}

	@Override
	public void remove(Player player)
	{
		if (player == null)
			return;

		try
		{
			if (_participants.containsKey(player))
				_participants.remove(player);
			if (live_list.containsKey(player))
				live_list.remove(player);
			if (player.hasHWID() && _registeredHWIDs.contains(player.getHWID()))
				_registeredHWIDs.remove(player.getHWID());
			player.setPvPTeam(0);
			player.allowPvPTeam();
			player._event = null;
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouDisqualified", player, new Object[0]));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean canRegister(Player player, boolean first)
	{
		if (getConfigs().ALLOW_TAKE_ITEM)
		{
			long take_item_count = getItemCount(player, getConfigs().TAKE_ITEM_ID);
			String name_take_items = ItemHolder.getInstance().getTemplate(getConfigs().TAKE_ITEM_ID).getName();
			if(take_item_count > 0)
			{
				if((int)take_item_count < getConfigs().TAKE_COUNT)
				{
					player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.NotEnoughItems", player, name_take_items));
					return false;
				}
			}
			else
			{
				player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.NotEnoughItems2", player, name_take_items));
				return false;
			}
		}
		if (first && _state != 1)
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.registration_disabled", player));
			return false;
		}
		if (first && isParticipant(player))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.already_member", player));
			return false;
		}
		if (player.isMounted())
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.unsummon_pet", player));
			return false;
		}
		if (player.isInDuel())
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.mustfinishduel", player));
			return false;
		}
		if ((player.getLevel() < getConfigs().MIN_LEVEL) || (player.getLevel() > getConfigs().MAX_LEVEL))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.notenoughlevel", player));
			return false;
		}
		if ((first) && (player.getPvPTeam() != 0))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.another_event", player));
			return false;
		}
		if(first && (player.isInOlympiadMode() || Olympiad.isRegistered(player) || Olympiad.isRegisteredInComp(player)))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.olympiad", player));
			return false;
		}
		if ((player.isInParty()) && (player.getParty().isInDimensionalRift()))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.DimensionalRift", player));
			return false;
		}
		if (player.isTeleporting())
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.teleporting", player));
			return false;
		}
		if ((first) && (_participants.size() >= getConfigs().MAX_PARTICIPANTS))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.max_participiants", player));
			return false;
		}
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.no_cursed", player));
			return false;
		}
		if (player.getKarma() > 0)
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.no_pk", player));
			return false;
		}
		if (player.hasHWID() && first && _registeredHWIDs.contains(player.getHWID()))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.already_registered", player));
			return false;
		}
		return true;
	}

	public void question()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player != null && ((player.getLevel() >= getConfigs().MIN_LEVEL && player.getLevel() <= getConfigs().MAX_LEVEL) || player.getReflection().getId() <= 0 || !player.isInOlympiadMode() || !Olympiad.isRegistered(player) || !player.isInOfflineMode()))
			{
				player.scriptRequest(new CustomMessage("scripts.events.TvT_New.TvT_New.AskPlayer", player).toString(), "events.TvT_New.TvT_New:registerPlayer", new Object[0]);
			}
		}
	}

	public int getCountPlayers()
	{
		return _participants.size();
	}

	public void canRegisters()
	{
		if (_participants != null)
		{
			for (Player player : _participants.keySet())
			{
				if (!canRegister(player, false))
				{
					player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.conditions.NoConditionsDisqualified", player));
				}
			}
		}
	}

	@Override
	public boolean isParticipant(Player player)
	{
		return _participants.containsKey(player);
	}

	public int TeamWithMinPlayers()
	{
		int[] count = new int[getConfigs().TEAM_COUNTS + 1];

		for (Player player : _participants.keySet())
		{
			count[player.getPvPTeam()] += 1;
		}
		int min = count[1];

		for (int i = 1; i < count.length; i++)
			min = Math.min(min, count[i]);
		for (int i = 1; i < count.length; i++)
		{
			if (count[i] != min) 
				continue;
			min = i;
		}
		return min;
	}

	public void sayToAll(String adress, String[] replacements, boolean all)
	{
		if (all)
			Announcements.getInstance().announceByCustomMessage(adress, replacements, ChatType.CRITICAL_ANNOUNCE);
		else
			for (Player player : _participants.keySet())
				Announcements.getInstance().announceToPlayerByCustomMessage(player, adress, replacements, ChatType.CRITICAL_ANNOUNCE);
	}

	public void startRegistration()
	{
		_state = 1;
		sayToAll("scripts.events.TvT_New.TvT_New.AnnounceRegistrationStarted", new String[] { getName() }, true);
		//question();
		
		_score = new int[getConfigs().TEAM_COUNTS + 1];

		if (getConfigs().TIME_TO_START_BATTLE >= 30)
			ThreadPoolManager.getInstance().schedule(new StartMessages("scripts.events.TvT_New.TvT_New.EventStartOver", new String[] { "30" }), (getConfigs().TIME_TO_START_BATTLE - 30) * 1000);
		if (getConfigs().TIME_TO_START_BATTLE >= 10)
			ThreadPoolManager.getInstance().schedule(new StartMessages("scripts.events.TvT_New.TvT_New.EventStartOver", new String[] { "10" }), (getConfigs().TIME_TO_START_BATTLE - 10) * 1000);
		for (int i = 5; i >= 1; i--)
		{
			if (getConfigs().TIME_TO_START_BATTLE - i >= i)
				ThreadPoolManager.getInstance().schedule(new StartMessages("scripts.events.TvT_New.TvT_New.EventStartOver", new String[] { Integer.toString(i) }), (getConfigs().TIME_TO_START_BATTLE - i) * 1000);
		}
		ThreadPoolManager.getInstance().schedule(new TaskVoid("canRegisters", null), (getConfigs().TIME_TO_START_BATTLE - 10) * 1000);
		ThreadPoolManager.getInstance().schedule(new TaskVoid("start", null), getConfigs().TIME_TO_START_BATTLE * 1000);
	}

	private void initRef()
	{
		_ref = new Reflection();
		_instantZone = InstantZoneHolder.getInstance().getInstantZone(605);
		_ref.init(_instantZone);
	}
	
	@Override
	public void start()
	{
		
		for(Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress()) 
			{
				GameEventManager.getInstance().nextEvent();
				_state = 0;
				_log.info("TvT not started: CastleSiege in progress");
				return;
			}
		 
		initRef();
		
		if (_state == 0)
		{
			startRegistration();
		}
		else if (_state == 1)
		{
			if (getCountPlayers() >= getConfigs().MIN_PARTICIPANTS)
			{
				ThreadPoolManager.getInstance().schedule(new go(), getConfigs().PAUSE_TIME * 1000);
				sayToAll("scripts.events.TvT_New.TvT_New.AnnounceTeleportToColiseum", new String[0], true);
				_state = 2;
				teleportPlayersToColiseum();
			}
			else
			{
				sayToAll("scripts.events.TvT_New.TvT_New.AnnounceEventCancelled", new String[] { getName() }, true);
				for(Player player : _participants.keySet())
				{
					player.setPvPTeam(0);
					player.allowPvPTeam();
					player._event = null;
				}
				
				_participants.clear();
				_registeredHWIDs.clear();
				_state = 0;
				abort();
			}
		}
		else
		{
			sayToAll("scripts.events.TvT_New.TvT_New.AnnounceStartError", new String[0], true);
		}
	}

	@Override
	public void finish()
	{
		sayToAll("scripts.events.TvT_New.TvT_New.AnnounceEventEnd", new String[0], false);
		
		if (_state == 2)
		{
			int WinTeam = -1;
			int count = 0;
			int max = 0;
			
			for (int i = 0; i < _score.length - 1; i++)
			{
				max = Math.max(_score[i], _score[(i + 1)]);
			}
			for (int i = 0; i < _score.length; i++)
			{
				if (_score[i] != max)
					continue;
				WinTeam = i;
				count++;
			}
			if (count != 1 || WinTeam == -1 || _score[WinTeam] == 0)
			{
				sayToAll("scripts.events.TvT_New.TvT_New.EventDraw", new String[0], false);
			}
			else
			{
				rewardToWinTeam(WinTeam);	
			}
			
			ThreadPoolManager.getInstance().schedule(new TaskVoid("restorePlayers", null), 1000);
			ThreadPoolManager.getInstance().schedule(new TaskVoid("teleportPlayersToSavedCoords", null), 2000);
		}
		
		ThreadPoolManager.getInstance().schedule(new TaskVoid("clearAll", null), 3500);
		GameEventManager.getInstance().nextEvent();
		_state = 0;
	}

	@Override
	public void abort()
	{
		finish();
		if (_state > 0)
			sayToAll("scripts.events.TvT_New.TvT_New.EventCompletedManually", new String[] { getName() }, true);
	}

	@Override
	public void onLogout(Player player)
	{
		if ((player == null) || (player.getPvPTeam() < 1))
		{
			return;
		}

		if (_state == 1 && _participants.containsKey(player))
		{
			unreg(player);
			player.setPvPTeam(0);
			player.allowPvPTeam();
			player._event = null;
			return;
		}

		if (_state == 2 && _participants.containsKey(player))
		{
			remove(player);
			try
			{
				if(player != null && player.getStablePoint() != null)
					player.teleToLocation(player.getStablePoint(), 0);
				remove(player);
				player.setPvPTeam(0);
				player.allowPvPTeam();
				player._event = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void teleportPlayersToSavedCoords()
	{
		for (Player player : _participants.keySet())
		{
			teleportPlayerToSavedCoords(player);
		}
	}

	public void teleportPlayerToSavedCoords(Player player)
	{
		try
		{
			if(player == null)
				return;
			
			if (player.getStablePoint() == null) // игрока не портнуло на стадион
				return;
			
			player.getEffectList().stopAllEffects();
			if(player.getPet() != null)
			{
				Summon summon = player.getPet();
				summon.unSummon();
			}
			player.teleToLocation(player.getStablePoint(), ReflectionManager.DEFAULT);
			if (getConfigs().STOP_ALL_EFFECTS)
				ThreadPoolManager.getInstance().schedule(new TaskVoid("returnBuffsToPlayers", player), 500);
			player.setStablePoint(null);
			player.setPvPTeam(0);
			player.allowPvPTeam();
			player._event = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void doDie(Creature killer, Creature self)
	{
		if (self == null || killer == null)
			return;
		if ((self instanceof Player) && (killer instanceof Player))
		{
			Player player = (Player) self;
			Player kill = (Player) killer;
			if (_participants.containsKey(player))
			{
				_participants.put(player, _participants.get(player) + 1);
			}
			if (getConfigs().ALLOW_KILL_BONUS && _participants.containsKey(kill))
			{
				addItem(kill, getConfigs().KILL_BONUS_ID, getConfigs().KILL_BONUS_COUNT);
			}
			if (_state == 2 && player.getPvPTeam() > 0 && kill.getPvPTeam() > 0 && _participants.containsKey(player) && _participants.containsKey(kill) && getConfigs().RESURRECTION_TIME == 0)
			{
				if (player != null)
				{
					player.setFakeDeath(true);
					player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
					player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_START_FAKEDEATH));
					player.broadcastCharInfo();
					player.abortCast(true, false);
					player.abortAttack(true, false);
					show(new CustomMessage("scripts.events.TvT_New.TvT_New.YouDead", player), player);
					live_list.remove(player);
				}
			}
			else
			{
				if (killer != null)
				{
					_score[kill.getPvPTeam()] += 1;
				}
				if (player != null)
				{
					player.setFakeDeath(true);
					player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
					player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_START_FAKEDEATH));
					player.broadcastCharInfo();
					player.abortCast(true, false);
					player.abortAttack(true, false);
					player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouWillBeRessuction", player, new Object[0]).add(new Object[]
					{
						getConfigs().RESURRECTION_TIME
					}));
					ThreadPoolManager.getInstance().schedule(new TaskVoid("ResurrectionPlayer", player), getConfigs().RESURRECTION_TIME * 1000);
				}
			}
		}
	}

	public boolean checkRound(boolean finish)
	{
		if (!finish)
		{
			int liveTeamCount = 0;
			int team = 0;
			for (int i = 1; i <= getConfigs().TEAM_COUNTS; i++)
			{
				if (!live_list.containsValue(Integer.valueOf(i)))
					continue;
				liveTeamCount++;
				team = i;
				if (liveTeamCount > 1)
					return false;
			}

			_score[team] += 1;
		}

		if (curr_round >= getConfigs().NUMBER_OF_ROUNDS)
			finish();
		else
			nextRound();

		return true;
	}

	public void nextRound()
	{
		for (Player player : _participants.keySet())
		{
			restorePlayer(player);
			Reflection ref = _ref;
			InstantZone instantZone = ref.getInstancedZone();
			Location tele = instantZone.getTeleportCoords().get(player.getPvPTeam() - 1).setR(ref).findPointToStay(50, 50);
			player.teleToLocation(tele, ref);
			if(getConfigs().NUMBER_OF_ROUNDS > 0)
				live_list.put(player, player.getPvPTeam());
			player.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.StartBattle", player, new Object[0]).toString(), getConfigs().PAUSE_TIME * 700, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
		}
		curr_round += 1;
		paralyzePlayers();
		ThreadPoolManager.getInstance().schedule(new go(), getConfigs().PAUSE_TIME * 1000);
	}

	public void teleportPlayersToColiseum()
	{
		for (Player player : _participants.keySet())
		{
			if (!canRegister(player, false))
			{
				remove(player);
				player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouDisqualified", player, new Object[0]));
				continue;
			}
			if(getConfigs().ALLOW_TAKE_ITEM)
				removeItem(player, getConfigs().TAKE_ITEM_ID, getConfigs().TAKE_COUNT);
			ItemInstance wpn = player.getActiveWeaponInstance();
			if(wpn != null && wpn.isHeroWeapon() && !getConfigs().ALLOW_HERO_WEAPONS)
			{
				player.getInventory().unEquipItem(wpn);
				player.abortAttack(true, true);
			}
			unRide(player);
			//unSummonPet(player, true);
			if (getConfigs().STOP_ALL_EFFECTS) 
				removeBuff(player);
			if(player.getParty() != null)
				player.leaveParty();
			playersBuff();
			player.allowPvPTeam();
			player.setStablePoint(player.getStablePoint() == null ? player.getReflection().getReturnLoc() == null ? player.getLoc() : player.getReflection().getReturnLoc() : player.getStablePoint());
			Reflection ref = _ref;
			InstantZone instantZone = ref.getInstancedZone();
			Location tele = instantZone.getTeleportCoords().get(player.getPvPTeam() - 1).setR(ref).findPointToStay(50, 50);
			player.teleToLocation(tele, ref);
			restorePlayer(player);
			if (getConfigs().NUMBER_OF_ROUNDS > 0) 
				live_list.put(player, Integer.valueOf(player.getPvPTeam()));
			player.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.StartBattle", player, new Object[0]).toString(), getConfigs().PAUSE_TIME * 700, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
		}

		paralyzePlayers();
	}

	public void removeBuff(Player player)
	{
		if(player != null)
		{
			List<Effect> effectList = player.getEffectList().getAllEffects();
			_effects = new ArrayList<Effect>(effectList.size());

			if (player.isCastingNow())
			{
				player.abortCast(true, true);
			}
			for(Effect $effect : effectList)
			{
				Effect effect = $effect.getTemplate().getEffect(new Env($effect.getEffector(), $effect.getEffected(), $effect.getSkill()));
				effect.setCount($effect.getCount());
				effect.setPeriod($effect.getCount() == 1 ? $effect.getPeriod() - $effect.getTime() : $effect.getPeriod());
				_effects.add(effect);
			}
			if(player.getPet() != null)
			{
				Summon summon = player.getPet();
				summon.getEffectList().stopAllEffects();
			}
			returnBuffs.put(player, _effects);
			player.getEffectList().stopAllEffects();
		}
	}
	
	public void returnBuffsToPlayers(Player player)
	{
		for(Effect e : returnBuffs.get(player))
			player.getEffectList().addEffect(e);
	}
	
	public void paralyzePlayers()
	{
		Skill revengeSkill = SkillTable.getInstance().getInfo(4515, 1);
		for (Player player : _participants.keySet())
		{
			player.getEffectList().stopEffect(1411);
			revengeSkill.getEffects(player, player, false, false);
			if (player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
	}

	public void unParalyzePlayers()
	{
		for (Player player : _participants.keySet())
		{
			player.getEffectList().stopEffect(4515);
			if (player.getPet() != null)
				player.getPet().getEffectList().stopEffect(4515);
			if(player.isInParty())
				player.leaveParty();
		}
	}

	public void restorePlayer(Player player)
	{
		ClassId nclassId = ClassId.VALUES[player.getClassId().getId()];
		if(player.isFakeDeath())
		{
			player.setFakeDeath(false);
			player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_STOP_FAKEDEATH));
			player.broadcastPacket(new Revive(player));
			player.broadcastCharInfo();
		}
		if(nclassId.isMage())
			playerBuff(player, getConfigs().LIST_MAGE_MAG_SUPPORT);
		else
			playerBuff(player, getConfigs().LIST_MAGE_FAITER_SUPPORT);
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
	}

	public void restorePlayers()
	{
		for (Player player : _participants.keySet())
		{
			if(player.isFakeDeath())
			{
				player.setFakeDeath(false);
				player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_STOP_FAKEDEATH));
				player.broadcastPacket(new Revive(player));
				player.broadcastCharInfo();
			}
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public void ResurrectionPlayer(Player player)
	{
		if ((player._event == null) || (_state != 2) || (!_participants.containsKey(player)))
			return;
		Reflection ref = _ref;
		InstantZone instantZone = ref.getInstancedZone();
		Location tele = instantZone.getTeleportCoords().get(player.getPvPTeam() - 1).findPointToStay(50, 50);
		player.teleToLocation(tele, ref);
		restorePlayer(player);
	}

	private void clearAll()
	{
		for(Player player : _participants.keySet())
		{
			player.setPvPTeam(0);
			player.allowPvPTeam();
			player._event = null;
		}
		
		_participants.clear();
		live_list.clear();
		_registeredHWIDs.clear();
		returnBuffs.clear();
		
		if (_ref != null)
			_ref.clearReflection(1, false);
	}

	public void rewardToWinTeam(int WinTeam)
	{
		int count = 0;
		for (Player player : _participants.keySet())
		{
			if (player != null && player.getPvPTeam() == WinTeam)
				count++;
		}
		if (count < 1)
			return;

		for (Player player : _participants.keySet())
		{
			if (player != null && player.getPvPTeam() == WinTeam)// && _participants.get(player) >= getConfigs().REWARD_FOR_KILL)
			{
				for(int i = 0; i < getConfigs().getRewardId().length; i++)
					addItem(player, getConfigs().getRewardId()[i], getConfigs().getRewardCount()[i]);
			}
		}
		sayToAll("scripts.events.TvT_New.TvT_New.EventWin", new String[] { getConfigs().TEAM_NAME.get(WinTeam - 1), getConfigs().NUMBER_OF_ROUNDS > 0 ? "Points" : "Kills", Integer.toString(_score[WinTeam]) }, false);
	}

	public void getInfo()
	{
		Player player = getSelf();
		
		if (player == null)
			return;
		
		getInformation(player);
	}
	
	public void getInformation(Player player)
	{
		if (!Config.ENABLE_NEW_TVT)
			return;
		
		int rounds = getConfigs().NUMBER_OF_ROUNDS;
		long min = (getConfigs().START_TIME - System.currentTimeMillis() / 1000L) / 60L;
		String time = min + " minute";
		String reward = getConfigs().ST_REWARD_COUNT + " " + ItemHolder.getInstance().getTemplate(Integer.parseInt(getConfigs().ST_REWARD_ITEM_ID)).getName();
		long timeToEnd = (getConfigs().TIME_TO_END_BATTLE - System.currentTimeMillis() / 1000L) / 60L;

		StringBuffer content = new StringBuffer("<html><body>");
		content.append("<table width=280 cellspacing=0>");
		content.append("<tr><td align=center>Event: <font color=LEVEL>").append(getName()).append("</font></td></tr>");
		content.append("<tr><td align=center>Type: <font color=LEVEL>").append(rounds > 0 ? "By Rounds" : "By Time").append("</font></td></tr>");
		content.append("<tr><td align=center>Prize: <font color=LEVEL>").append(reward).append("</font></td></tr>");
		content.append("<tr><td align=center>Teams: <font color=LEVEL>").append(getConfigs().TEAM_COUNTS).append("</font></td></tr>");
		content.append("<tr><td align=center>Min/Max participants: <font color=LEVEL>").append(getConfigs().MIN_PARTICIPANTS).append("/").append(getConfigs().MAX_PARTICIPANTS).append("</font></td></tr>");
		if (_state == 0)
		{
			content.append("<tr><td align=center>Start in: <font color=LEVEL>").append(time).append("</font></td></tr>");
		}
		else if (_state == 1)
		{
			content.append("<tr><td align=center>");
			if (_participants == null || !_participants.containsKey(player))
				content.append(Strings.htmlButton("Registration", "bypass -h scripts_events.TvT_New.TvT_New:addPlayer", 120, 25));
			else 
				content.append(Strings.htmlButton("Unregister", "bypass -h scripts_events.TvT_New.TvT_New:removePlayer", 120, 25));
			
			content.append("</td></tr>");
		}
		else if (_state == 2)
		{
			content.append("<tr><td align=center>Ends in: <font color=LEVEL>").append(timeToEnd).append("</font></td></tr>");
		}
		
		content.append("</table>");
		content.append("</body></html>");

		show(content.toString(), player, getNpc());
	}

	class TaskVoid implements Runnable
	{
		String _name;
		Player _player;

		TaskVoid(String name, Player player)
		{
			_name = name;
			_player = player;
		}

		public void run()
		{
			if (_name.equals("canRegisters"))
				canRegisters();
			else if (_name.equals("start"))
				start();
			else if (_name.equals("restorePlayers"))
				restorePlayers();
			else if (_name.equals("returnBuffsToPlayers"))
				returnBuffsToPlayers(_player);
			else if (_name.equals("teleportPlayersToSavedCoords"))
				teleportPlayersToSavedCoords();
			else if (_name.equals("clearAll"))
				clearAll();
			else if (_name.equals("ResurrectionPlayer"))
				ResurrectionPlayer(_player);
		}
	}

	class StartMessages implements Runnable
	{
		String _adress;
		String[] _replacing;

		StartMessages(String adress, String[] replacing)
		{
			_adress = adress;
			_replacing = replacing;
		}

		public void run()
		{
			if (_state == 1)
				sayToAll(_adress, _replacing, true);
		}
	}

	public class go implements Runnable
	{
		public go()
		{
		}

		public void run()
		{
			openDoors();
			unParalyzePlayers();
			int time = getConfigs().TIME_TO_END_BATTLE;

			sayToAll("scripts.events.TvT_New.TvT_New.RoundStarted", null, false);

			while (time >= 0 && _state == 2)
			{
				int sec = time - time / 60 * 60;

				for (Player player : _participants.keySet())
				{
					String message;
					if (getConfigs().NUMBER_OF_ROUNDS > 0)
					{
						message = "Round: " + curr_round + " from " + getConfigs().NUMBER_OF_ROUNDS;
						message = message + "\nTeam: " + getConfigs().TEAM_NAME.get(player.getPvPTeam() - 1);
					}
					else
					{
						message = "\nTeam: " + getConfigs().TEAM_NAME.get(player.getPvPTeam() - 1);
					}

					if (sec < 10)
						message = message + "\nTime left: " + time / 60 + ":0" + sec;
					else
					{
						message = message + "\nTime left: " + time / 60 + ":" + sec;
					}
					player.sendPacket(new ExShowScreenMessage(message, 1000, ExShowScreenMessage.ScreenMessageAlign.BOTTOM_RIGHT, false));
				}
				if(getCountPlayers() <= 1)
					finish();
				
				if (getConfigs().NUMBER_OF_ROUNDS > 0 && checkRound(false))
					return;
				try
				{
					Thread.sleep(1000);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				time--;
			}

			if (getConfigs().NUMBER_OF_ROUNDS > 0)
				checkRound(true);
			else
				finish();
		}
	}

	public void openDoors()
	{
		for(DoorInstance door : _ref.getDoors())
			door.openMe();
	}

	private void playersBuff()
	{
		for(Player player : _participants.keySet())
		{
			ClassId nclassId = ClassId.VALUES[player.getClassId().getId()];
			if(nclassId.isMage())
				playerBuff(player, getConfigs().LIST_MAGE_MAG_SUPPORT);
			else
				playerBuff(player, getConfigs().LIST_MAGE_FAITER_SUPPORT);
		}
	}

	private void playerBuff(Player player, GArray<Integer> list)
	{
		int time = getConfigs().TIME_MAGE_SUPPORT;
		Summon pet = player.getPet();
		Skill skill = null;

		for(int i : list)
		{
			int lvl = SkillTable.getInstance().getBaseLevel(i);
			
			skill = SkillTable.getInstance().getInfo(i, lvl);
			if(pet != null)
				for(EffectTemplate et : skill.getEffectTemplates())
				{	
					Env env = new Env(pet, pet, skill);
					Effect effect = et.getEffect(env);
					effect.setPeriod(time * 60000);
					pet.getEffectList().addEffect(effect);
					pet.updateEffectIcons();
				}
			else
				for(EffectTemplate et : skill.getEffectTemplates())
				{	
					Env env = new Env(player, player, skill);
					Effect effect = et.getEffect(env);
					effect.setPeriod(time * 60000);
					player.getEffectList().addEffect(effect);
					player.sendChanges();
					player.updateEffectIcons();
				}
		}
	}

	@Override
	public boolean canAttack(Creature attacker, Creature target)
	{
		if (_state == 2)
		{
			if (attacker.isPlayer() && target.isPlayer() && attacker.getPlayer().getPvPTeam() == target.getPlayer().getPvPTeam())
				return false;
			if (target.isFakeDeath())
				return false;
		}
		return true;
	}

	@Override
	public boolean canUseSkill(Creature caster, Creature target, Skill skill)
	{
		if (_state == 2)
		{
			if (caster.isPlayer() && target.isPlayer() && caster.getPlayer().getPvPTeam() == target.getPlayer().getPvPTeam() && skill.isOffensive())
				return false;
			if(skill.isHeroic() && !getConfigs().ALLOW_HERO_WEAPONS)
			{
				//TODO: fix the 2 "caster.sendMessage" (creature caster)
				caster.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.NotUseable", caster.getPlayer()));
				return false;
			}
			if (ArrayUtils.contains(getConfigs().getRestictSkillId(), skill.getId()))
			{
				caster.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.NotUseable", caster.getPlayer()));
				return false;
			}
			if (target.isFakeDeath())
				return false;
		}
		return true;
	}
	
	@Override
	public String minLvl()
	{
		return "" + getConfigs().MIN_LEVEL;
	}

	@Override
	public String maxLvl()
	{
		return "" + getConfigs().MAX_LEVEL;
	}

	@Override
	public void onPlayerExit(Player player)
	{
		if(player.getPvPTeam() == 0)
			return;

		if(_state > 1 && player != null && player.getPvPTeam() != 0 && _participants.containsKey(player))
		{
			try
			{
				if(player != null)	
					player.teleToLocation(player.getStablePoint(), ReflectionManager.DEFAULT);
				remove(player);
				player.setPvPTeam(0);
				player.allowPvPTeam();
				player._event = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}