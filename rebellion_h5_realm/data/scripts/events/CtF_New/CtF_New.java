package events.CtF_New;

import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.data.xml.holder.ZoneHolder;
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
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ChangeWaitType;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.stats.Env;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.utils.GArray;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastTable;
import javolution.util.FastMap;

import org.apache.commons.lang3.ArrayUtils;

public class CtF_New extends GameEvent implements ScriptFile, OnPlayerExitListener
{
	private Logger _log = Logger.getLogger(CtF_New.class.getName());

	private int _state = 0;
	private static CtF_New _instance;
	private FastMap<Player, Integer> _participants = new FastMap<Player, Integer>();
	private List<String> _registeredHWIDs = new FastTable<String>();
	private FastMap<Player, List<Effect>> returnBuffs = new FastMap<Player, List<Effect>>();
	private List<Effect> _effects;
	
	private int[] _score;
	public long startBattle = 0L;
	private Reflection _ref;
	private InstantZone _instantZone;
	private NpcInstance redFlag = null;
	private NpcInstance blueFlag = null;

	public CtF_New()
	{
		_instance = this;
	}

	public static CtF_New getInstance()
	{
		if (_instance == null)
			_instance = new CtF_New();
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
		return "Capture The Flag";
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
		if (CTFConfig._configs != null && CTFConfig._configs.size() > 1)
			CTFConfig._configs.sort();
	}

	public Configs getConfigs()
	{
		return (CTFConfig._configs != null && !CTFConfig._configs.isEmpty()) ? CTFConfig._configs.get(0) : null;
	}

	@Override
	public boolean canUseItem(Player actor, ItemInstance item)
	{
		if(_state == 2)
		{
			if((item.isHeroWeapon() && !getConfigs().ALLOW_HERO_WEAPONS) || ArrayUtils.contains(getConfigs().getRestictId(), item.getItemId()))
			{
				actor.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NotUseable", actor));
				return false;
			}
		}
		return true;
	}
	public void onLoad()
	{
		if (!Config.ENABLE_NEW_CTF)
			return;
		
		CTFConfig.load();
		GameEventManager.getInstance().registerEvent(getInstance());
		_log.info("Loaded Event: CTF");
		_state = 0;
	}

	@Override
	public void onReload()
	{
		if (!Config.ENABLE_NEW_CTF)
			return;
		
		if (_ref != null)
			_ref.clearReflection(1, false);
		if (CTFConfig._configs.size() > 0)
			CTFConfig._configs.clear();
		if (_state != 0)
			finish();
		onLoad();
	}

	@Override
	public void onShutdown()
	{
		if (!Config.ENABLE_NEW_CTF)
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
		player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.YouRegistred", player, new Object[0]));
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
		GameEvent event = GameEventManager.getInstance().findEvent("Capture The Flag");
		event.register(player);
	}
	
	public void unregisterPlayer()
	{
		Player player = getSelf();
		GameEvent event = GameEventManager.getInstance().findEvent("Capture The Flag");
		event.unreg(player);
	}
	
	@Override
	public void unreg(Player player)
	{
		if (player == null) 
			return;

		if (_state == 2 || !isParticipant(player))
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.YouCancelRegistration", player, new Object[0]));
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
		player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.YouRegistrationCancelled", player, new Object[0]));
	}

	@Override
	public void remove(Player player)
	{
		try
		{
			if (player == null)
				return;
			if (_participants.containsKey(player)) 
				_participants.remove(player);
			if (player.hasHWID() &&_registeredHWIDs.contains(player.getHWID()))
				_registeredHWIDs.remove(player.getHWID());	
			player.setPvPTeam(0);
			player.allowPvPTeam();
			player._event = null;
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.YouDisqualified", player, new Object[0]));
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
					player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NotEnoughItems", player, name_take_items));
					return false;
				}
			}
			else
			{
				player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NotEnoughItems2", player, name_take_items));
				return false;
			}
		}
		if (first && _state != 1)
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.regnotactive", player));
			return false;
		}
		if (first && isParticipant(player))
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.AlreadyParticipant", player));
			return false;
		}
		if (player.isMounted())
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.UnSummonPet", player));
			return false;
		}
		if (player.isInDuel())
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.MustFinishDuel", player));
			return false;
		}
		if (player.getLevel() < getConfigs().MIN_LEVEL || player.getLevel() > getConfigs().MAX_LEVEL)
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NotEnoughLevel", player));
			return false;
		}
		if(first && player._event != null)
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.RegisteredOnAnotherEvent", player));
			return false;
		}
		if(first && (player.isInOlympiadMode() || Olympiad.isRegistered(player) || Olympiad.isRegisteredInComp(player)))
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.RegisteredOnOly", player));
			return false;
		}
		if (player.isInParty() && player.getParty().isInDimensionalRift())
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.InDimensionalRift", player));
			return false;
		}
		if (player.isTeleporting())
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.CurrentlyTeleporting", player));
			return false;
		}
		if (first && _participants.size() >= getConfigs().MAX_PARTICIPANTS)
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.MaxParticipiantsReached", player));
			return false;
		}
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NoCursedWeapons", player));
			return false;
		}
		if (player.getKarma() > 0)
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NoPk", player));
			return false;
		}
		if (player.hasHWID() && first && _registeredHWIDs.contains(player.getHWID()))
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.AlreadyRegistered", player));
			return false;
		}
		
		return true;
	}

	public boolean canTeleport(Player player)
	{
		if(player.isCursedWeaponEquipped())
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NoCursedWeapon2", player));
			return false;
		}
		if(player.getKarma() > 0)
		{
			player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NoKarma", player));
			return false;
		}
		return true;
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
					player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NoConditionsDisqualified", player));
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
		{
			min = Math.min(min, count[i]);
		}
		for (int i = 1; i < count.length; i++)
		{
			if (count[i] != min) continue; min = i;
		}
		return min;
	}

	public void sayToAll(String adress, String[] replacements, boolean all)
	{
		if (all)
		{
			Announcements.getInstance().announceByCustomMessage(adress, replacements);
		}
		else
			for(Player player : _participants.keySet())
				Announcements.getInstance().announceToPlayerByCustomMessage(player, adress, replacements, ChatType.CRITICAL_ANNOUNCE);
	}

	public void question()
	{
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player != null && ((player.getLevel() >= getConfigs().MIN_LEVEL && player.getLevel() <= getConfigs().MAX_LEVEL) || player.getReflection().getId() <= 0 || !player.isInOlympiadMode() || !Olympiad.isRegistered(player) || !player.isInOfflineMode()))
			{
				player.scriptRequest(new CustomMessage("scripts.events.CtF_New.CtF_New.AskPlayer", player).toString(), "events.CtF_New.CtF_New:registerPlayer", new Object[0]);
			}
		}
	}
	
	public void startRegistration()
	{
		_state = 1;
		sayToAll("scripts.events.CtF_New.CtF_New.AnnounceRegistrationStarted", new String[] { getName() }, true);
		//question();		
		
		_score = new int[getConfigs().TEAM_COUNTS];

		if (getConfigs().TIME_TO_START_BATTLE >= 30)
			ThreadPoolManager.getInstance().schedule(new StartMessages("scripts.events.CtF_New.CtF_New.EventStartOver", new String[] { "30" }), (getConfigs().TIME_TO_START_BATTLE - 30) * 1000);
		if (getConfigs().TIME_TO_START_BATTLE >= 10)
			ThreadPoolManager.getInstance().schedule(new StartMessages("scripts.events.CtF_New.CtF_New.EventStartOver", new String[] { "10" }), (getConfigs().TIME_TO_START_BATTLE - 10) * 1000);
		for (int i = 5; i >= 1; i--) 
		{
			if (getConfigs().TIME_TO_START_BATTLE - i >= i)
				ThreadPoolManager.getInstance().schedule(new StartMessages("scripts.events.CtF_New.CtF_New.EventStartOver", new String[] { Integer.toString(i) }), (getConfigs().TIME_TO_START_BATTLE - i) * 1000);
		}
		ThreadPoolManager.getInstance().schedule(new TaskVoid("canRegisters", null), (getConfigs().TIME_TO_START_BATTLE - 10) * 1000);
		ThreadPoolManager.getInstance().schedule(new TaskVoid("start", null), getConfigs().TIME_TO_START_BATTLE * 1000);
	}

	private void initRef()
	{
		_ref = new Reflection();
		_instantZone = InstantZoneHolder.getInstance().getInstantZone(602);
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
				_log.info("CTF not started: CastleSiege in progress");
				return;
			}
		
		initRef();

		if(_state == 0)
		{
			startRegistration();
		}
		else if (_state == 1)
		{
			if (getCountPlayers() >= getConfigs().MIN_PARTICIPANTS)
			{
				ZoneHolder.getDoor(24190002).closeMe();
				ZoneHolder.getDoor(24190003).closeMe();
				//checkPlayers();
				ThreadPoolManager.getInstance().schedule(new go(), getConfigs().PAUSE_TIME * 1000);
				sayToAll("scripts.events.CtF_New.CtF_New.AnnounceTeleportToColiseum", new String[0], true);
				_state = 2;
				teleportPlayersToColiseum();

				if (redFlag != null)
					redFlag.deleteMe();
				if (blueFlag != null) {
					blueFlag.deleteMe();
				}
				blueFlag = Functions.spawn(getConfigs().FLAG_COORDS.get(0), 35426, _ref);
				redFlag = Functions.spawn(getConfigs().FLAG_COORDS.get(1), 35423, _ref);
			}
			else
			{
				sayToAll("scripts.events.CtF_New.CtF_New.AnnounceEventCancelled", new String[] { getName() }, true);
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
			sayToAll("scripts.events.CtF_New.CtF_New.AnnounceStartError", new String[0], true);
		}
	}

	@Override
	public void finish()
	{
		sayToAll("scripts.events.CtF_New.CtF_New.AnnounceEventEnd", new String[0], false);

		if (_state == 2)
		{
			int WinTeam = -1;
			int max = 0;
			int count = 0;

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

			if(count != 1 || WinTeam == -1 || _score[WinTeam] == 0)
			{
				sayToAll("scripts.events.CtF_New.CtF_New.EventDraw", new String[0], false);
			}
			else
			{
				rewardToWinTeam(WinTeam);
			}

			ThreadPoolManager.getInstance().schedule(new TaskVoid("restorePlayers", null), 1000);
			ThreadPoolManager.getInstance().schedule(new TaskVoid("teleportPlayersToSavedCoords", null), 3000);
		}
		
		ZoneHolder.getDoor(24190002).openMe();
		ZoneHolder.getDoor(24190003).openMe();
		ThreadPoolManager.getInstance().schedule(new TaskVoid("clearAll", null), 3500);
		GameEventManager.getInstance().nextEvent();
		_state = 0;
	}

	@Override
	public void abort()
	{
		finish();
		if (_state > 0)
			sayToAll("scripts.events.CtF_New.CtF_New.EventCompletedManually", new String[] { getName() }, true);
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

		if ((_state == 2) && (_participants.containsKey(player)))
		{
			remove(player);
			try
			{
				if(player != null)
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
			if(player.getStablePoint() == null)
				return;

			player.getEffectList().stopAllEffects();

			ItemInstance[] arr = player.getInventory().getItems();
			int len = arr.length;
			for (int i = 0; i < len; i++)
			{
				ItemInstance _item = arr[i];
				if(_item.getItemId() == 13560 || _item.getItemId() == 13561)
				{
					player.unsetVar("CtF_Flag");
					player.getInventory().destroyItem(_item, 1);
					player.broadcastUserInfo(true);
				}
			}

			player.unsetVar("CtF_Flag");
			player.setPvPTeam(0);
			player.allowPvPTeam();
			removeFlag(player);
			player._event = null;
			if(player.getPet() != null)
			{
				Summon summon = player.getPet();
				summon.unSummon();
			}
			if (getConfigs().STOP_ALL_EFFECTS)
				ThreadPoolManager.getInstance().schedule(new TaskVoid("returnBuffsToPlayers", player), 500L);
			player.teleToLocation(player.getStablePoint(), ReflectionManager.DEFAULT);
			player.setStablePoint(null);
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
			Player player = (Player)self;
			Player kill = (Player)killer;
			if (_participants.containsKey(player))
				_participants.put(player, _participants.get(player) + 1);
			if(getConfigs().ALLOW_KILL_BONUS && _participants.containsKey(kill))
				addItem(kill, getConfigs().KILL_BONUS_ID, getConfigs().KILL_BONUS_COUNT);
			if (_state == 2 && player.getPvPTeam() > 0 && kill.getPvPTeam() > 0 && _participants.containsKey(player) && _participants.containsKey(kill))
			{
				if (player != null)
				{
					player.setFakeDeath(true);
					player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
					player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_START_FAKEDEATH));
					player.broadcastCharInfo();
					player.abortCast(true, false);
					player.abortAttack(true, false);
					player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.YouDead", player, new Object[0]).add(new Object[]
					{
						getConfigs().RESURRECTION_TIME
					}));
					ThreadPoolManager.getInstance().schedule(new TaskVoid("ResurrectionPlayer", player), getConfigs().RESURRECTION_TIME * 1000);
				}
			}
			else
			{
				if (player != null)
				{
					player.setFakeDeath(true);
					player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
					player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_START_FAKEDEATH));
					player.broadcastCharInfo();
					player.abortCast(true, false);
					player.abortAttack(true, false);
					player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.YouDead", player, new Object[0]).add(new Object[]
					{
						getConfigs().RESURRECTION_TIME
					}));
					ThreadPoolManager.getInstance().schedule(new TaskVoid("ResurrectionPlayer", player), getConfigs().RESURRECTION_TIME * 1000);
				}
			}
			player.unsetVar("CtF_Flag");
			dropFlag(player, true);
		}
	}

	public void teleportPlayersToColiseum()
	{
		for (Player player : _participants.keySet())
		{
			if (!canRegister(player, false))
			{
				remove(player);
				player.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.YouDisqualified", player, new Object[0]));
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
			Location tele = instantZone.getTeleportCoords().get(player.getPvPTeam() - 1).setR(ref).findPointToStay(150, 200);
			player.teleToLocation(tele, ref);
			restorePlayer(player);
			player.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.StartBattle", player, new Object[0]).toString(), getConfigs().PAUSE_TIME * 700, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
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
		if (player._event == null || _state != 2 || !_participants.containsKey(player))
			return;
		Reflection ref = _ref;
		InstantZone instantZone = ref.getInstancedZone();
		Location tele = instantZone.getTeleportCoords().get(player.getPvPTeam() - 1).setR(ref).findPointToStay(150, 200);
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
		_registeredHWIDs.clear();
		returnBuffs.clear();
		
		if (redFlag != null)
			redFlag.deleteMe();
		if (blueFlag != null)
			blueFlag.deleteMe();
		
		if (_ref != null)
			_ref.clearReflection(1, false);
	}

	public void rewardToWinTeam(int WinTeam)
	{
		WinTeam++;
		for (Player player : _participants.keySet())
		{
			if ((player != null) && (player.getPvPTeam() == WinTeam))
				for(int i = 0; i < getConfigs().getRewardId().length; i++)
					addItem(player, getConfigs().getRewardId()[i], getConfigs().getRewardCount()[i]);
		}
		sayToAll("scripts.events.CtF_New.CtF_New.EventWin", new String[] { getConfigs().TEAM_NAME.get(WinTeam - 1), "Flags", Integer.toString(_score[(WinTeam - 1)]) }, false);
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
		if (!Config.ENABLE_NEW_CTF)
			return;
		
		int need_score = getConfigs().NEED_SCORE;
		long min = (getConfigs().START_TIME - System.currentTimeMillis() / 1000L) / 60L;
		String time = min + " minute";
		String reward = getConfigs().ST_REWARD_COUNT + " " + ItemHolder.getInstance().getTemplate(Integer.parseInt(getConfigs().ST_REWARD_ITEM_ID)).getName();
		long timeToEnd = (getConfigs().TIME_TO_END_BATTLE - System.currentTimeMillis() / 1000L) / 60L;
		
		StringBuffer content = new StringBuffer("<html><body>");
		content.append("<table width=280 cellspacing=0>");
		content.append("<tr><td align=center>Event: <font color=LEVEL>").append(getName()).append("</font></td></tr>");
		content.append("<tr><td align=center>Goal: <font color=LEVEL>").append(need_score).append(" captured flags.</font></td></tr>");
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
				content.append(Strings.htmlButton("Registration", "bypass -h scripts_events.CtF_New.CtF_New:addPlayer", 120, 25));
			else 
				content.append(Strings.htmlButton("Unregister", "bypass -h scripts_events.CtF_New.CtF_New:removePlayer", 120, 25));
			
			content.append("</td></tr>");
		}
		else if (_state == 2)
		{
			content.append("<tr><td align=center>Ends in: <font color=LEVEL>").append(timeToEnd).append("</font></td></tr>");
		}
		
		content.append("</table>");
		content.append("</body></html>");
		
		show(content.toString(), player, getNpc());
		//player.sendPacket(new NpcHtmlMessage(0).setHtml(content.toString()));
	}

	private void addFlag(Player player)
	{
		int flagId = player.getPvPTeam() == 1 ? 13560 : 13561;
		ItemInstance item = ItemFunctions.createItem(flagId);
		item.setCustomType1(77);
		item.setCustomFlags(354);
		player.getInventory().addItem(item);
		player.getInventory().equipItem(item);
		player.sendChanges();
		player.setVar("CtF_Flag", "1", -1);
		if (flagId == 13561)
			blueFlag.decayMe();
		else redFlag.decayMe();
	}

	private void removeFlag(Player player)
	{
		if (player != null && player.isTerritoryFlagEquipped())
		{
			ItemInstance[] arr = player.getInventory().getItems();
			int len = arr.length;
			for (int i = 0; i < len; i++)
			{
				ItemInstance _item = arr[i];
				if((_item.getItemId() == 13560 || _item.getItemId() == 13561) && _item.getCustomType1() == 77)
				{
					player.unsetVar("CtF_Flag");
					_item.setCustomFlags(0);
					player.getInventory().destroyItem(_item, 1);
					player.broadcastUserInfo(true);
				}
			}
		}
	}

	private void dropFlag(Player player, boolean onBase)
	{
		if (player != null && player.isTerritoryFlagEquipped())
		{
			ItemInstance flag = player.getActiveWeaponInstance();
			if (flag != null && flag.getCustomType1() == 77)
			{
				removeFlag(player);

				if(flag.getItemId() == 13561)
				{
					blueFlag.setXYZInvisible(onBase ? getConfigs().FLAG_COORDS.get(0) : player.getLoc());
					blueFlag.spawnMe();
				}
				else if(flag.getItemId() == 13560)
				{
					redFlag.setXYZInvisible(onBase ? getConfigs().FLAG_COORDS.get(1) : player.getLoc());
					redFlag.spawnMe();
				}
			}
		}
	}

	@Override
	public boolean talkWithNpc(Player player, NpcInstance npc)
	{
		if (_state > 0 && player != null && _participants.containsKey(player))
		{
			if (npc.getNpcId() == 35426)
			{
				if (player.isTerritoryFlagEquipped() && player.getPvPTeam() == 1)
				{
					flagToBase(player);
					player.unsetVar("CtF_Flag");
				}
				else if (!player.isTerritoryFlagEquipped() && player.getPvPTeam() == 2 && npc.isVisible())
					addFlag(player);
				return true;
			}

			if (npc.getNpcId() == 35423)
			{
				if (player.isTerritoryFlagEquipped() && player.getPvPTeam() == 2)
				{
					flagToBase(player);
					player.unsetVar("CtF_Flag");
				}
				else if (!player.isTerritoryFlagEquipped() && player.getPvPTeam() == 1 && npc.isVisible())
					addFlag(player);
				return true;
			}
		}
		return false;
	}

	public void flagToBase(Player player)
	{
		dropFlag(player, true);
		player.unsetVar("CtF_Flag");
		_score[(player.getPvPTeam() - 1)] += 1;
		sayToAll("scripts.events.CtF_New.CtF_New.FlagToBase", new String[] { player.getName(), getConfigs().TEAM_NAME.get(player.getPvPTeam() - 1) }, false);
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

	class StartMessages	implements Runnable
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
		{}

		public void run()
		{
			unParalyzePlayers();
			int time = getConfigs().TIME_TO_END_BATTLE;

			sayToAll("scripts.events.CtF_New.CtF_New.RoundStarted", new String[0], false);

			while (time >= 0 && _state == 2)
			{
				int sec = time - time / 60 * 60;

				for (Player player : _participants.keySet())
				{
					String message = "Points: " + _score[(player.getPvPTeam() - 1)] + " from " + getConfigs().NEED_SCORE;
					message = message + "\nTeam: " + getConfigs().TEAM_NAME.get(player.getPvPTeam() - 1);

					if (sec < 10)
						message = message + "\nTime left: " + time / 60 + ":0" + sec;
					else {
						message = message + "\nTime left: " + time / 60 + ":" + sec;
					}
					player.sendPacket(new ExShowScreenMessage(message, 1000, ExShowScreenMessage.ScreenMessageAlign.BOTTOM_RIGHT, false));
					if(_score[(player.getPvPTeam() - 1)] == getConfigs().NEED_SCORE)
						finish();
				}
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

			finish();
    	}
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
		if(_state == 2)
		{
			if(attacker.isPlayer() && target.isPlayer() && attacker.getPlayer().getPvPTeam() == target.getPlayer().getPvPTeam())
				return false;
			if(target.isFakeDeath())
				return false;
		}
		return true;
	}

	@Override
	public boolean canUseSkill(Creature caster, Creature target, Skill skill)
	{
		if(_state == 2)
		{
			if(caster.isPlayer() && target.isPlayer() && caster.getPlayer().getPvPTeam() == target.getPlayer().getPvPTeam() && skill.isOffensive())
				return false;
			if(skill.isHeroic() && !getConfigs().ALLOW_HERO_WEAPONS)
			{
				//TODO: Check if this message will actually appear, cause we use here Creature ...
				caster.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NotUseable", caster.getPlayer()));
				return false;
			}
			if (ArrayUtils.contains(getConfigs().getRestictSkillId(), skill.getId()))
			{
				//TODO: Check if this message will actually appear, cause we use here Creature ...
				caster.sendMessage(new CustomMessage("scripts.events.CtF_New.CtF_New.NotUseable", caster.getPlayer()));
				return false;
			}
			if(target.isFakeDeath())
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