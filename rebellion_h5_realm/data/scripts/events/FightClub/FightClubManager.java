package events.FightClub;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.actor.player.OnPlayerExitListener;
import l2r.gameserver.listener.actor.player.OnTeleportListener;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.base.InvisibleType;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import npc.model.events.FightClubManagerInstance.Rate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FightClubManager extends Functions implements ScriptFile, OnPlayerExitListener, OnTeleportListener
{

	private static Logger _log = LoggerFactory.getLogger(FightClubManager.class);

	private static Map<Long, Rate> _ratesMap;
	private static List<FightClubArena> _fights;
	protected static List<Long> _inBattle;
	private static Map<Long, Location> _restoreCoord;
	private static List<Long> _inList;
	private static StringBuilder _itemsList;
	private static Map<String, Integer> _allowedItems;

	private static boolean _isCreateBattle = false;

	private static final ArrayList<SimpleSpawner> _spawns_fight_club_manager = new ArrayList<SimpleSpawner>();

	private static int FIGHT_CLUB_MANAGER = 112;

	private void spawnFightClub()
	{
		final int FIGHT_CLUB_MANAGER_SPAWN[][] = {

		{ 82042, 149711, -3356, 58312 }, // Giran
		{ 146408, 28536, -2255, 33600 }, // Aden 1
		{ 148504, 28536, -2255, 33600 }, // Aden 2
		{ 145800, -57112, -2966, 32500 }, //Goddard 1
		{ 150120, -56504, -2966, 32500 }, //Goddard 2
		{ 43656, -46792, -784, 17000 }, //Rune
		{ 19448, 145048, -3094, 16500 }, //Dion 1
		{ 17832, 144312, -3037, 16500 }, //Dion 2
		{ 82888, 55304, -1511, 16500 }, //Oren 1
		{ 80104, 53608, -1547, 16500 }, //Oren 2
		{ -15064, 124296, -3104, 16500 }, //Gludio 1
		{ -12184, 122552, -3086, 16500 }, //Gludio 2
		{ -82648, 149896, -3115, 33600 }, //Gludin 1
		{ -81800, 155368, -3163, 58312 }, //Gludin 2
		{ 89272, -141592, -1525, 32500 }, //Shuttgart 1
		{ 87672, -140776, -1525, 32500 }, //Shuttgart 2
		{ 115496, 218728, -3648, 16500 }, //Heine 1
		{ 107384, 217704, -3661, 16500 }, //Heine 2
		{ 116808, 75448, -2748, 16500 }, //Hunter's Village 
		};

		SpawnNPCs(FIGHT_CLUB_MANAGER, FIGHT_CLUB_MANAGER_SPAWN, _spawns_fight_club_manager);
	}
	
	@Override
	public void onLoad()
	{
		if(!Config.FIGHT_CLUB_ENABLED)
			return;

		spawnFightClub();
		CharListenerList.addGlobal(this);

		_ratesMap = new HashMap<Long, Rate>();
		_fights = new ArrayList<FightClubArena>();
		_restoreCoord = new HashMap<Long, Location>();
		_inBattle = new ArrayList<Long>();
		_inList = new ArrayList<Long>();
		_itemsList = new StringBuilder();
		_allowedItems = new HashMap<String, Integer>();

		for(int i = 0; i < Config.ALLOWED_RATE_ITEMS.length; i++)
		{
			String itemName = ItemFunctions.createItem(Integer.parseInt(Config.ALLOWED_RATE_ITEMS[i])).getTemplate().getName();
			_itemsList.append(itemName).append(";");
			_allowedItems.put(itemName, Integer.parseInt(Config.ALLOWED_RATE_ITEMS[i]));
		}

		_log.info("Loaded Event: Fight Club");

	}

	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
	{
		if(player.getTeam() != TeamType.NONE && _inBattle.contains(player.getStoredId()))
			removePlayer(player);
	}

	/**
	 * Удаляет всю информацию об игроке
	 * @param player - ссылка на удаляемого игрока
	 */
	private static void removePlayer(Player player)
	{
		if(player != null)
		{
			player.setTeam(TeamType.NONE);
			if(_inBattle.contains(player.getStoredId()))
				_inBattle.remove(player.getStoredId());
			if(_inList.contains(player.getStoredId()))
			{
				_ratesMap.remove(player.getStoredId());
				_inList.remove(player.getStoredId());
			}
			if(_restoreCoord.containsKey(player.getStoredId()))
				_restoreCoord.remove(player.getStoredId());
		}
	}

	public static Location getRestoreLocation(Player player)
	{
		return _restoreCoord.get(player.getStoredId());
	}

	public static Player getPlayer(long playerStoredI)
	{
		return GameObjectsStorage.getAsPlayer(playerStoredI);
	}

	@Override
	public void onPlayerExit(Player player)
	{
		removePlayer(player);
	}

	@Override
	public void onReload()
	{
		_isCreateBattle = false;
		_fights.clear();
		_ratesMap.clear();
		_inBattle.clear();
		_inList.clear();
		onLoad();
	}

	@Override
	public void onShutdown()
	{
		if(!Config.FIGHT_CLUB_ENABLED)
			return;
		
		_isCreateBattle = false;
		_fights.clear();
		_ratesMap.clear();
		_inBattle.clear();
		_inList.clear();
	}

	public static String addApplication(Player player, String item, int count)
	{
		if(player == null)
			return null;
		
		if(!checkPlayer(player, true))
			return null;
		if(isRegistered(player))
			return "reg";
		if(getItemCount(player, _allowedItems.get(item)) < count)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledItems", player), player);
			return "NoItems";
		}
		final Rate rate = new Rate(player, _allowedItems.get(item), count);
		final StringBuilder stRate = new StringBuilder();
		stRate.append(_allowedItems.get(item)).append(";").append(count).append(";");
		_ratesMap.put(player.getStoredId(), rate);
		_inList.add(0, player.getStoredId());
		if(Config.FIGHT_CLUB_ANNOUNCE_RATE)
		{
			final String[] args = { String.valueOf(player.getName()), String.valueOf(player.getLevel()), String.valueOf(rate.getItemCount()), String.valueOf(item) };
			sayToAll("scripts.events.fightclub.Announce", args);
			if(Config.FIGHT_CLUB_ANNOUNCE_RATE_TO_SCREEN)
				sayToAllPlayers("scripts.events.fightclub.Announce", player, item, count, true);
		}
		player.setVar("FightClubRate", stRate.toString(), -1);

		return "OK";
	}

	public static void backItemsPlayers()
	{
		for(Player player : getPlayers(_inList))
		{
			final int itemId = _ratesMap.get(player.getStoredId()).getItemId();
			final int itemCount = _ratesMap.get(player.getStoredId()).getItemCount();
			if(player.isOnline())
				addItem(player, itemId, itemCount);
		}
	}

	public static void backItemsPlayer(Player player)
	{
		final int itemId = _ratesMap.get(player.getStoredId()).getItemId();
		final int itemCount = _ratesMap.get(player.getStoredId()).getItemCount();
		addItem(player, itemId, itemCount);
	}
	
	/**
	 * Отправляет ConfirmDlg
	 * @param requested - игрок, выставивший заявку. <b>Ему</b> отправляется запрос
	 * @param requester - игрок, выбравший из списка соперника. <b>От него</b> отправляется запрос
	 */
	public static boolean requestConfirmation(Player requested, Player requester)
	{
		if(!checkPlayer(requested, true))
			return false;
		
		if(!checkPlayer(requester, true))
			return false;

		if((requested.getLevel() - requester.getLevel()) > Config.MAXIMUM_LEVEL_DIFFERENCE || (requester.getLevel() - requested.getLevel()) > Config.MAXIMUM_LEVEL_DIFFERENCE)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledLevel", requester, Config.MINIMUM_LEVEL_TO_PARRICIPATION, Config.MAXIMUM_LEVEL_TO_PARRICIPATION, Config.MAXIMUM_LEVEL_DIFFERENCE), requester);
			return false;
		}
		Object[] duelists = { requested, requester };
		requested.scriptRequest(new CustomMessage("scripts.events.fightclub.AskPlayer", requested, requester.getName(), requester.getLevel()).toString(), "events.FightClub.FightClubManager:doStart", duelists);
		final StringBuilder stRate = new StringBuilder();
		final int itemId = _ratesMap.get(requested.getStoredId()).getItemId();
		final int itemCount = _ratesMap.get(requested.getStoredId()).getItemCount();
		stRate.append(itemId).append(";").append(itemCount).append(";");
		requester.setVar("FightClubRate", stRate.toString(), -1);
		return true;
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}
	
	/**
	 * Проверка игроков для последующего создания арены для них
	 * @param requested - игрок, выставивший заявку. <b>Ему</b> отправляется запрос
	 * @param requester - игрок, выбравший из списка соперника. <b>От него</b> отправляется запрос
	 */
	public static void doStart(Player requested, Player requester)
	{
		final int itemId = _ratesMap.get(requested.getStoredId()).getItemId();
		final int itemCount = _ratesMap.get(requested.getStoredId()).getItemCount();
		if(!checkPrepare(requested, requester, itemId, itemCount))
			return;

		if(!checkPlayer(requested, false))
			return;

		if(!checkPlayer(requester, true))
			return;

		removeItem(requested, itemId, itemCount);
		removeItem(requester, itemId, itemCount);
		_inList.remove(requested.getStoredId());
		_ratesMap.remove(requested.getStoredId());
		_restoreCoord.put(requested.getStoredId(), new Location(requested.getX(), requested.getY(), requested.getZ()));
		_restoreCoord.put(requester.getStoredId(), new Location(requester.getX(), requester.getY(), requester.getZ()));
		removeItem(requested, itemId, itemCount);
		removeItem(requester, itemId, itemCount);
		_isCreateBattle = true;
		
		createBattle(requested, requester, itemId, itemCount);
	}

	private static boolean checkPrepare(Player requested, Player requester, int itemId, int itemCount)
	{

		if(requested.getVar("FightClubRate") == null)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledItems", requested), requested);
			show(new CustomMessage("scripts.events.fightclub.CancelledOpponent", requester), requester);
			return false;
		}

		if(getItemCount(requester, itemId) < itemCount)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledItems", requester), requester);
			return false;
		}

		if(getItemCount(requested, itemId) < itemCount)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledItems", requested), requested);
			return false;
		}

		if(requester.getVar("FightClubRate") == null)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledItems", requester), requester);
			return false;
		}

		if(_inBattle.contains(requested.getStoredId()))
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOpponent", requester), requested);
			return false;
		}

		if(requester.isInOfflineMode())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOffline", requested), requested);
			return false;
		}

		if(requested.isInOfflineMode())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOffline", requester), requester);
			return false;
		}
		
		return true;
	}

	private static void createBattle(Player player1, Player player2, int itemId, int itemCount)
	{
		_inBattle.add(player1.getStoredId());
		_inBattle.add(player2.getStoredId());
		final FightClubArena _arena = new FightClubArena(player1, player2, itemId, itemCount, new Reflection());
		_fights.add(_arena);
	}

	public static void deleteArena(FightClubArena arena)
	{
		removePlayer(arena.getPlayer1());
		removePlayer(arena.getPlayer2());
		arena.getReflection().collapse();
		_fights.remove(arena);
	}

	public static boolean checkPlayer(Player player, boolean first)
	{

		if(first && player.isDead())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledDead", player), player);
			return false;
		}

		if(first && player.getTeam() != TeamType.NONE)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.getLevel() < Config.MINIMUM_LEVEL_TO_PARRICIPATION || player.getLevel() > Config.MAXIMUM_LEVEL_TO_PARRICIPATION)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledLevel", player), player);
			return false;
		}

		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.fightclub.Cancelled", player), player);
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			show(new CustomMessage("scripts.events.fightclub.Cancelled", player), player);
			return false;
		}

		if(player.isInDuel())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledDuel", player), player);
			return false;
		}

		if(player.getOlympiadGame() != null || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOlympiad", player), player);
			return false;
		}

		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.isInObserverMode())
		{
			show(new CustomMessage("scripts.event.fightclub.CancelledObserver", player), player);
			return false;
		}

		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledTeleport", player), player);
			return false;
		}
		return true;
	}

	/**
	 * Приватный метод. Возващает true, если игрок зарегистрировал ставку
	 * @param player - ссылка на проверяемого игрока
	 * @return - true, если зарегистрирован
	 */
	private static boolean isRegistered(Player player)
	{
		if(_inList.contains(player.getStoredId()))
			return true;
		return false;
	}

	/**
	 * Возвращает объект класса {@link Rate}, содержащий параметры "заявки"
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @param index
	 * @return объект, содержащий заявку
	 */
	public static Rate getRateByIndex(int index)
	{
		return _ratesMap.get(_inList.get(index));
	}

	/**
	 * Возвращает объект класса {@link Rate}, содержащий параметры "заявки"
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @param index
	 * @return объект, содержащий заявку
	 */
	public static Rate getRateByStoredId(long storedId)
	{
		return _ratesMap.get(storedId);
	}

	/**
	 * Возвращает через ; имена предметов,
	 * разрешенных в качестве ставки.
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @return список предметов через ";"
	 */
	public static String getItemsList()
	{
		return _itemsList.toString();
	}

	/**
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @param playerObject - ссылка на игрока
	 * @return true, если игрок зарегистрировал ставку
	 */
	public static boolean isRegistered(Object playerObject)
	{
		if(_ratesMap.containsKey(((Player) playerObject).getStoredId()))
			return true;
		return false;
	}

	/**
	 * Удаляет регистрацию игрока в списке через метод {@link #removePlayer(Player)}
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @param playerObject ссылка на игрока
	 */
	public static void deleteRegistration(Player player)
	{
		player.unsetVar("FightClubRate");
		removePlayer(player);
	}

	public static boolean isCreateBattle(Object playerObject)
	{
		return _isCreateBattle;
	}
	
	/**
	 * Возвращает количеств игроков, сделавших свои ставки
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @return - количество игроков, сделавших ставки
	 */
	public static int getRatesCount()
	{
		return _inList.size();
	}

	/**
	 * Ставит в root игрока
	 * @param player
	 */
	private static void rootPlayer(Player player)
	{
		player.startRooted();
		player.startAbnormalEffect(AbnormalEffect.ROOT);
		if(player.getPet() != null)
		{
			player.getPet().startRooted();
			player.getPet().startAbnormalEffect(AbnormalEffect.ROOT);
		}
	}

	/**
	 * Снимает root с игрока
	 * @param player
	 */
	private static void unrootPlayers(Player player)
	{
		player.stopRooted();
		player.stopAbnormalEffect(AbnormalEffect.ROOT);
		if(player.getPet() != null)
		{
			player.getPet().stopRooted();
			player.getPet().stopAbnormalEffect(AbnormalEffect.ROOT);
		}
	}

	/**
	 * Телепортирует игроков на сохраненные координаты
	 * @param player1
	 * @param player2
	 */
	public static void teleportPlayersBack(Player player1, Player player2, Object obj)
	{
		teleportPlayerBack(player1);
		teleportPlayerBack(player2);
	}

	@SuppressWarnings("static-access")
	private static void teleportPlayerBack(Player player)
	{
		if (player == null)
			return;

		if (player.getPet() != null)
			player.getPet().getEffectList().stopAllEffects();

		player.setCurrentCp(player.getMaxCp());
		player.setCurrentMp(player.getMaxMp());

		if(player.isDead())
		{
			player.setCurrentHp(player.getMaxHp(), true);
			player.broadcastPacket(new Revive(player));
		}
		else
		{
			player.setCurrentHp(player.getMaxHp(), false);
		}

		if(Config.REMOVE_CLAN_SKILLS && player.getClan() != null)
		{
			for(final Skill skill : player.getClan().getAllSkills())
				player.addSkill(skill);
		}

		if(Config.REMOVE_HERO_SKILLS && player.isHero())
			Hero.getInstance().addSkills(player);

		if ((player != null) && (_restoreCoord.containsKey(player.getStoredId())))
			player.teleToLocation(_restoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
	}

	/**
	 * Выводит текст по центру экрана. Выводит нескольким игрокам.
	 * Положение - TOP_CENTER
	 * @param address - адрес текста
	 * @param arg - параметр замены (один)
	 * @param bigFont - большой шрифт
	 * @param players - список игроков
	 */
	protected static void sayToPlayers(String address, Object arg, boolean bigFont, Player... players)
	{
		for(Player player : players)
		{
			final CustomMessage sm = new CustomMessage(address, player, arg);
			player.sendPacket(new ExShowScreenMessage(sm.toString(), 3000, ScreenMessageAlign.TOP_CENTER, bigFont));
		}
	}

	/**
	 * Выводит текст по центру экрана. Выводит нескольким игрокам.
	 * Положение - TOP_CENTER
	 * @param address - адрес текста
	 * @param bigFont - большой шрифт
	 * @param players - список игроков
	 */
	protected static void sayToPlayers(String address, boolean bigFont, Player... players)
	{
		for(Player player : players)
		{
			final CustomMessage sm = new CustomMessage(address, player);
			player.sendPacket(new ExShowScreenMessage(sm.toString(), 3000, ScreenMessageAlign.TOP_CENTER, bigFont));
		}
	}

	/**
	 * Выводит текст по центру экрана. Положение - TOP_CENTER
	 * @param player - целевой игрок
	 * @param address - адрес текста
	 * @param bigFont - большой шрифт
	 * @param args - параметры замены текста
	 */
	protected static void sayToPlayer(Player player, String address, boolean bigFont, Object... args)
	{
		player.sendPacket(new ExShowScreenMessage(new CustomMessage(address, player, args).toString(), 3000, ScreenMessageAlign.TOP_CENTER, bigFont));
	}

	protected static void sayToAllPlayers(String address, Player player, String item, int count, boolean bigFont)
	{
		for(Player _player : GameObjectsStorage.getAllPlayersForIterate())
		{
			final CustomMessage sm = new CustomMessage(address, _player);
			final Object[] args = { String.valueOf(player.getName()), String.valueOf(player.getLevel()), String.valueOf(count), String.valueOf(item) };
			_player.sendPacket(new ExShowScreenMessage(sm.add(args).toString(), 3000, ScreenMessageAlign.TOP_CENTER, bigFont));
		}
	}

	protected static void sayStartToAllPlayers(String address, Player player1, Player player2, String item, int count, boolean bigFont)
	{
		for(Player _player : GameObjectsStorage.getAllPlayersForIterate())
		{
			final CustomMessage sm = new CustomMessage(address, _player);
			final Object[] args = { String.valueOf(player1.getName()), String.valueOf(player2.getName()), String.valueOf(count * 2), String.valueOf(item) };
			_player.sendPacket(new ExShowScreenMessage(sm.add(args).toString(), 3000, ScreenMessageAlign.TOP_CENTER, bigFont));
		}
	}
	
	/**
	 * Возрождает мёртвых игроков
	 */
	public static void resurrectPlayers(Player player1, Player player2, Object obj)
	{
		if(player1.isDead())
		{
			player1.restoreExp();
			player1.setCurrentCp(player1.getMaxCp());
			player1.setCurrentHp(player1.getMaxHp(), true);
			player1.setCurrentMp(player1.getMaxMp());
			player1.broadcastPacket(new Revive(player1));
		}
		if(player2.isDead())
		{
			player2.restoreExp();
			player2.setCurrentCp(player2.getMaxCp());
			player2.setCurrentHp(player2.getMaxHp(), true);
			player2.setCurrentMp(player2.getMaxMp());
			player2.broadcastPacket(new Revive(player2));
		}
	}

	/**
	 * Восстанавливает HP/MP/CP участникам
	 */
	public void healPlayers(Player player1, Player player2, Object obj)
	{
		player1.setCurrentCp(player1.getMaxCp());
		player1.setCurrentHpMp(player1.getMaxHp(), player1.getMaxMp());
		player2.setCurrentCp(player2.getMaxCp());
		player2.setCurrentHpMp(player2.getMaxHp(), player2.getMaxMp());
	}

	/**
	 * Запускает битву между игроками.
	 * @param player1
	 * @param player2
	 */
	protected static void startBattle(Player player1, Player player2)
	{
		unrootPlayers(player1);
		player1.setTeam(TeamType.BLUE);
		unrootPlayers(player2);
		player2.setTeam(TeamType.RED);
		sayToPlayers("scripts.events.fightclub.Start", true, player1, player2);
	}

	/**
	 * Телепортирует игроков в коллизей в заданное отражение
	 * @param player1 - первый игрок
	 * @param player2 - втрой игрок
	 * @param reflection - отражение
	 */
	@SuppressWarnings("static-access")
	public static void teleportPlayersToColliseum(Player player1, Player player2, Reflection reflection)
	{
		player1.block();
		unRide(player1);

		if(Config.UNSUMMON_PETS)
			unSummonPet(player1, true);

		if(Config.UNSUMMON_SUMMONS)
			unSummonPet(player1, false);

		if(player1.isInvisible())
			player1.setInvisibleType(InvisibleType.NONE);

		if(Config.REMOVE_CLAN_SKILLS && player1.getClan() != null)
		{
			for(final Skill skill : player1.getClan().getAllSkills())
				player1.removeSkill(skill);
		}

		if(Config.REMOVE_HERO_SKILLS && player1.isHero())
			Hero.getInstance().removeSkills(player1);
		if(Config.CANCEL_BUFF_BEFORE_FIGHT)
		{
			player1.getEffectList().stopAllEffects();
			if(player1.getPet() != null)
				player1.getPet().getEffectList().stopAllEffects();
		}

		player1.teleToLocation(reflection.getInstancedZone().getTeleportCoords().get(0).setR(reflection).findPointToStay(50, 50), reflection);
		player1.unblock();
		rootPlayer(player1);

		player2.block();
		unRide(player2);

		if(Config.UNSUMMON_PETS)
			unSummonPet(player2, true);

		if(Config.UNSUMMON_SUMMONS)
			unSummonPet(player2, false);

		if(player2.isInvisible())
			player2.setInvisibleType(InvisibleType.NONE);

		if(Config.REMOVE_CLAN_SKILLS && player2.getClan() != null)
		{
			for(final Skill skill : player2.getClan().getAllSkills())
				player2.removeSkill(skill);
		}

		if(Config.REMOVE_HERO_SKILLS && player2.isHero())
			Hero.getInstance().removeSkills(player2);

		if(Config.CANCEL_BUFF_BEFORE_FIGHT)
		{
			player2.getEffectList().stopAllEffects();
			if(player2.getPet() != null)
				player2.getPet().getEffectList().stopAllEffects();
		}

		player2.teleToLocation(reflection.getInstancedZone().getTeleportCoords().get(1).setR(reflection).findPointToStay(50, 50), reflection);
		player2.unblock();
		rootPlayer(player2);
	}

	protected static class TeleportTask extends RunnableImpl
	{

		private Player player;
		private Location location;

		public TeleportTask(Player player, Location location)
		{
			this.player = player;
			this.location = location;
			player.block();
		}

		@Override
		public void runImpl() throws Exception
		{
			player.teleToLocation(location);
			player.unblock();
		}

	}
	
	private static List<Player> getPlayers(List<Long> list)
	{
		List<Player> result = new ArrayList<Player>();
		for(Long storeId : list)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				result.add(player);
		}
		return result;
	}
}