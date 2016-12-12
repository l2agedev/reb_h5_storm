package l2r.gameserver.model;

import static l2r.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_ALTERED_FLAG;
import static l2r.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_PEACE_FLAG;
import static l2r.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_PVP_FLAG;
import static l2r.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_SIEGE_FLAG;
import static l2r.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_SSQ_FLAG;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.dbutils.DbUtils;
import l2r.commons.lang.reference.HardReference;
import l2r.commons.lang.reference.HardReferences;
import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.achievements.Achievements;
import l2r.gameserver.achievements.PlayerCounters;
import l2r.gameserver.achievements.iAchievement;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.ai.PhantomPlayerAI;
import l2r.gameserver.ai.PlayableAI.nextAction;
import l2r.gameserver.ai.PlayerAI;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.dao.AccountBonusDAO;
import l2r.gameserver.dao.AccountReportDAO;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.CharacterGroupReuseDAO;
import l2r.gameserver.dao.CharacterPostFriendDAO;
import l2r.gameserver.dao.EffectsDAO;
import l2r.gameserver.dao.OlympiadNobleDAO;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.CharTemplateHolder;
import l2r.gameserver.data.xml.holder.EventHolder;
import l2r.gameserver.data.xml.holder.HennaHolder;
import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.data.xml.holder.RecipeHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.database.mysql;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.handler.items.IItemHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.AutoHuntingManager;
import l2r.gameserver.instancemanager.BypassManager;
import l2r.gameserver.instancemanager.BypassManager.BypassType;
import l2r.gameserver.instancemanager.BypassManager.DecodedBypass;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.MatchingRoomManager;
import l2r.gameserver.instancemanager.OfflineBufferManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.instancemanager.SchemeBufferManager.PlayerBuffProfile;
import l2r.gameserver.instancemanager.games.HandysBlockCheckerManager;
import l2r.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2r.gameserver.listener.actor.player.impl.ScriptAnswerListener;
import l2r.gameserver.listener.actor.player.impl.SummonAnswerListener;
import l2r.gameserver.model.Effect.EffectsComparator;
import l2r.gameserver.model.GameObjectTasks.ChargeCountDownTask;
import l2r.gameserver.model.GameObjectTasks.EndSitDownTask;
import l2r.gameserver.model.GameObjectTasks.EndStandUpTask;
import l2r.gameserver.model.GameObjectTasks.HourlyTask;
import l2r.gameserver.model.GameObjectTasks.AutoPotionTask;
import l2r.gameserver.model.GameObjectTasks.KickTask;
import l2r.gameserver.model.GameObjectTasks.PvPFlagTask;
import l2r.gameserver.model.GameObjectTasks.RecomBonusTask;
import l2r.gameserver.model.GameObjectTasks.UnJailTask;
import l2r.gameserver.model.GameObjectTasks.WaterTask;
import l2r.gameserver.model.Request.L2RequestType;
import l2r.gameserver.model.Skill.AddedSkill;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.actor.instances.player.Bonus;
import l2r.gameserver.model.actor.instances.player.BookMarkList;
import l2r.gameserver.model.actor.instances.player.FriendList;
import l2r.gameserver.model.actor.instances.player.Macro;
import l2r.gameserver.model.actor.instances.player.MacroList;
import l2r.gameserver.model.actor.instances.player.NevitSystem;
import l2r.gameserver.model.actor.instances.player.RecomBonus;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.actor.instances.player.ShortCutList;
import l2r.gameserver.model.actor.listener.PlayerListenerList;
import l2r.gameserver.model.actor.recorder.PlayerStatsChangeRecorder;
import l2r.gameserver.model.base.AccessLevel;
import l2r.gameserver.model.base.AcquireType;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.ClassLevel;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.model.base.RestartType;
import l2r.gameserver.model.base.Supersnoop;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.DimensionalRift;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import l2r.gameserver.model.entity.boat.Boat;
import l2r.gameserver.model.entity.boat.ClanAirShip;
import l2r.gameserver.model.entity.events.GameEvent;
import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2r.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2r.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2r.gameserver.model.entity.events.impl.DuelEvent;
import l2r.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2r.gameserver.model.entity.events.impl.SiegeEvent;
import l2r.gameserver.model.entity.olympiad.CompType;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.olympiad.OlympiadGame;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.entity.residence.Residence;
import l2r.gameserver.model.instances.DecoyInstance;
import l2r.gameserver.model.instances.FestivalMonsterInstance;
import l2r.gameserver.model.instances.GuardInstance;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.PetBabyInstance;
import l2r.gameserver.model.instances.PetInstance;
import l2r.gameserver.model.instances.ReflectionBossInstance;
import l2r.gameserver.model.instances.StaticObjectInstance;
import l2r.gameserver.model.instances.TamedBeastInstance;
import l2r.gameserver.model.instances.TrapInstance;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemContainer;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.LockType;
import l2r.gameserver.model.items.ManufactureItem;
import l2r.gameserver.model.items.PcFreight;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.items.PcRefund;
import l2r.gameserver.model.items.PcWarehouse;
import l2r.gameserver.model.items.TradeItem;
import l2r.gameserver.model.items.Warehouse;
import l2r.gameserver.model.items.Warehouse.WarehouseType;
import l2r.gameserver.model.items.attachment.FlagItemAttachment;
import l2r.gameserver.model.items.attachment.PickableAttachment;
import l2r.gameserver.model.matching.MatchingRoom;
import l2r.gameserver.model.petition.PetitionMainGroup;
import l2r.gameserver.model.pledge.Alliance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.Privilege;
import l2r.gameserver.model.pledge.RankPrivs;
import l2r.gameserver.model.pledge.SubUnit;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestEventType;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.ChangeAccessLevel;
import l2r.gameserver.network.serverpackets.AbnormalStatusUpdate;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.AutoAttackStart;
import l2r.gameserver.network.serverpackets.ChairSit;
import l2r.gameserver.network.serverpackets.ChangeWaitType;
import l2r.gameserver.network.serverpackets.CharInfo;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.EtcStatusUpdate;
import l2r.gameserver.network.serverpackets.ExAutoSoulShot;
import l2r.gameserver.network.serverpackets.ExBR_AgathionEnergyInfo;
import l2r.gameserver.network.serverpackets.ExBR_ExtraUserInfo;
import l2r.gameserver.network.serverpackets.ExBasicActionList;
import l2r.gameserver.network.serverpackets.ExDominionWarStart;
import l2r.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import l2r.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import l2r.gameserver.network.serverpackets.ExOlympiadMode;
import l2r.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import l2r.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2r.gameserver.network.serverpackets.ExQuestItemList;
import l2r.gameserver.network.serverpackets.ExSetCompassZoneCode;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.ExStartScenePlayer;
import l2r.gameserver.network.serverpackets.ExStorageMaxCount;
import l2r.gameserver.network.serverpackets.ExVitalityPointInfo;
import l2r.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2r.gameserver.network.serverpackets.GetItem;
import l2r.gameserver.network.serverpackets.HennaInfo;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.ItemList;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.LeaveWorld;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.MyTargetSelected;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.NpcInfoPoly;
import l2r.gameserver.network.serverpackets.ObserverEnd;
import l2r.gameserver.network.serverpackets.ObserverStart;
import l2r.gameserver.network.serverpackets.PartySmallWindowUpdate;
import l2r.gameserver.network.serverpackets.PartySpelled;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2r.gameserver.network.serverpackets.PrivateStoreListBuy;
import l2r.gameserver.network.serverpackets.PrivateStoreListSell;
import l2r.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import l2r.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2r.gameserver.network.serverpackets.QuestList;
import l2r.gameserver.network.serverpackets.RadarControl;
import l2r.gameserver.network.serverpackets.RecipeShopMsg;
import l2r.gameserver.network.serverpackets.RecipeShopSellList;
import l2r.gameserver.network.serverpackets.RelationChanged;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.Ride;
import l2r.gameserver.network.serverpackets.SendTradeDone;
import l2r.gameserver.network.serverpackets.ServerClose;
import l2r.gameserver.network.serverpackets.SetupGauge;
import l2r.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import l2r.gameserver.network.serverpackets.ShortCutInit;
import l2r.gameserver.network.serverpackets.ShortCutRegister;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.Snoop;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.SpawnEmitter;
import l2r.gameserver.network.serverpackets.SpecialCamera;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.TargetSelected;
import l2r.gameserver.network.serverpackets.TargetUnselected;
import l2r.gameserver.network.serverpackets.TeleportToLocation;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.network.serverpackets.ValidateLocation;
import l2r.gameserver.network.serverpackets.WareHouseDepositList;
import l2r.gameserver.network.serverpackets.WareHouseWithdrawList;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.IStaticPacket;
import l2r.gameserver.network.serverpackets.components.SceneMovie;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.randoms.CharacterEmails;
import l2r.gameserver.randoms.PlayerKillsLogManager;
import l2r.gameserver.randoms.PlayerKillsLogManager.KillLog;
import l2r.gameserver.randoms.Radar;
import l2r.gameserver.scripts.Events;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.skills.TimeStamp;
import l2r.gameserver.skills.effects.EffectCubic;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.skills.skillclasses.Charge;
import l2r.gameserver.skills.skillclasses.Transformation;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.FuncTemplate;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.tables.PetDataTable;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SkillTreeTable;
import l2r.gameserver.taskmanager.AutoSaveManager;
import l2r.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2r.gameserver.templates.Henna;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.templates.PlayerTemplate;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.item.ArmorTemplate;
import l2r.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.item.ItemTemplate.ItemClass;
import l2r.gameserver.templates.item.WeaponTemplate;
import l2r.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2r.gameserver.templates.item.support.FishTemplate;
import l2r.gameserver.templates.item.support.LureTemplate;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.AntiFlood;
import l2r.gameserver.utils.AutoBan;
import l2r.gameserver.utils.AutoHuntingPunish;
import l2r.gameserver.utils.GArray;
import l2r.gameserver.utils.GameStats;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Language;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.PlayerEventStatus;
import l2r.gameserver.utils.SqlBatch;
import l2r.gameserver.utils.Strings;
import l2r.gameserver.utils.TimeUtils;
import l2r.gameserver.utils.Util;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Player extends Playable implements PlayerGroup
{
	private PlayerEventInfo _eventInfo = new PlayerEventInfo(this);
	
	public static final int DEFAULT_TITLE_COLOR = 0xFFFF77;
	public static final int MAX_POST_FRIEND_SIZE = 100;
	public static final int MAX_FRIEND_SIZE = 128;

	private static final Logger _log = LoggerFactory.getLogger(Player.class);
	
	public static final String NO_TRADERS_VAR = "notraders";
	public static final String NO_ANIMATION_OF_CAST_VAR = "notShowBuffAnim";
	public static final String MY_BIRTHDAY_RECEIVE_YEAR = "MyBirthdayReceiveYear";
	public static final String NOT_CONNECTED = "<not connected>";

	public Map<Integer, SubClass> _classlist = new HashMap<Integer, SubClass>(4);

	public final static int OBSERVER_NONE = 0;
	public final static int OBSERVER_STARTING = 1;
	public final static int OBSERVER_STARTED = 3;
	public final static int OBSERVER_LEAVING = 2;

	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_OBSERVING_GAMES = 7;
	public static final int STORE_PRIVATE_SELL_PACKAGE = 8;

	/** The Constant STORE_SELLBUFF. */
	public static final int STORE_SELLBUFF = 12;
	
	/** If player is selling buffs value = true. */
	private boolean _sellbuff = false;
	
	public static final int RANK_VAGABOND = 0;
	public static final int RANK_VASSAL = 1;
	public static final int RANK_HEIR = 2;
	public static final int RANK_KNIGHT = 3;
	public static final int RANK_WISEMAN = 4;
	public static final int RANK_BARON = 5;
	public static final int RANK_VISCOUNT = 6;
	public static final int RANK_COUNT = 7;
	public static final int RANK_MARQUIS = 8;
	public static final int RANK_DUKE = 9;
	public static final int RANK_GRAND_DUKE = 10;
	public static final int RANK_DISTINGUISHED_KING = 11;
	public static final int RANK_EMPEROR = 12; // unused

	public static final int LANG_ENG = 0;
	public static final int LANG_RUS = 1;
	public static final int LANG_UNK = -1;

	public static final int[] EXPERTISE_LEVELS =
	{
			0,
			20,
			40,
			52,
			61,
			76,
			80,
			84,
			Integer.MAX_VALUE
	};

	private GameClient _connection;
	private String _login;
	private double _collision_radius;
	private double _collision_height;

	private int _karma, _pkKills, _pvpKills;
	private int _face, _hairStyle, _hairColor;
	private int _recomHave, _recomLeftToday, _fame;
	private int _recomLeft = 20;
	private int _recomBonusTime = 3600;
	private boolean _isHourglassEffected, _isRecomTimerActive;
	private int _deleteTimer;

	private long _createTime, _onlineTime, _leaveClanTime, _deleteClanTime, _NoChannel, _NoChannelBegin;
	private long _uptime;
	
	private FastMap<Integer, Long> _partyRequests = new FastMap<Integer, Long>();
	private long _lookingForPartyMembers = 0;
	
	 private boolean _isVoting = false;
	 
	/**
	 * Time on login in game
	 */
	private long _lastAccess;

	/**
	 * The Color of players name / title (white is 0xFFFFFF)
	 */
	private int _nameColor, _titlecolor;

	private int _vitalityLevel = -1;
	private double _vitality = Config.VITALITY_LEVELS[4];

	boolean sittingTaskLaunched;

	private boolean _relax;
	
	/**
	 * Time counter when L2Player is sitting
	 */
	private int _waitTimeWhenSit;

	private boolean _autoLoot = Config.AUTO_LOOT, _autoLootHerbs = Config.AUTO_LOOT_HERBS, _autoLootOnlyAdena = Config.AUTO_LOOT_ONLY_ADENA;

	private final PcInventory _inventory = new PcInventory(this);
	private final Warehouse _warehouse = new PcWarehouse(this);
	private final ItemContainer _refund = new PcRefund(this);
	private final PcFreight _freight = new PcFreight(this);

	public final BookMarkList _teleportBookmarks = new BookMarkList(this, 0);

	public final AntiFlood antiFlood = new AntiFlood();

	protected boolean _inventoryDisable = false;
	
	/**
	 * The table containing all L2RecipeList of the L2Player
	 */
	private final Map<Integer, Recipe> _recipebook = new TreeMap<Integer, Recipe>();
	private final Map<Integer, Recipe> _commonrecipebook = new TreeMap<Integer, Recipe>();

	/**
	 * Premium Items
	 */
	private Map<Integer, PremiumItem> _premiumItems = new TreeMap<Integer, PremiumItem>();

	/**
	 * The table containing all Quests began by the L2Player
	 */
	private final Map<String, QuestState> _quests = new HashMap<String, QuestState>();

	/**
	 * The list containing all shortCuts of this L2Player
	 */
	private final ShortCutList _shortCuts = new ShortCutList(this);

	/**
	 * The list containing all macroses of this L2Player
	 */
	private final MacroList _macroses = new MacroList(this);

	/**
	 * The Private Store type of the L2Player (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5)
	 */
	private int _privatestore;
	/**
	 * Данные для магазина рецептов
	 */
	private String _manufactureName;
	private List<ManufactureItem> _createList = Collections.emptyList();
	/**
	 * Данные для магазина продажи
	 */
	private String _sellStoreName;
	private List<TradeItem> _sellList = Collections.emptyList();
	private List<TradeItem> _packageSellList = Collections.emptyList();
	/**
	 * Данные для магазина покупки
	 */
	private String _buyStoreName;
	private List<TradeItem> _buyList = Collections.emptyList();
	/**
	 * Данные для обмена
	 */
	private List<TradeItem> _tradeList = Collections.emptyList();

	/**
	 * hennas
	 */
	private final Henna[] _henna = new Henna[3];
	private int _hennaSTR, _hennaINT, _hennaDEX, _hennaMEN, _hennaWIT, _hennaCON;

	private Party _party;
	private Location _lastPartyPosition;

	private Clan _clan;
	private int _pledgeClass = RANK_VAGABOND, _pledgeType = Clan.SUBUNIT_NONE, _powerGrade = 0, _lvlJoinedAcademy = 0, _apprentice = 0;
	private int _pledgeItemId = 0;
	private long _pledgePrice = 0;
	private boolean _isInAcademyList = false;
	  
	/**
	 * GM Stuff
	 */
	private AccessLevel _accessLevel;

	private int _serverId = 0;
	
	private boolean _messageRefusal = false, _tradeRefusal = false, _blockAll = false;

	/**
	 * The L2Summon of the L2Player
	 */
	private Summon _summon = null;
	private boolean _riding;

	private DecoyInstance _decoy = null;

	private Map<Integer, EffectCubic> _cubics = null;
	private int _agathionId = 0;

	private Request _request;

	private ItemInstance _arrowItem;

	/**
	 * The fists L2Weapon of the L2Player (used when no weapon is equipped)
	 */
	private WeaponTemplate _fistsWeaponItem;

	private Map<Integer, String> _chars = new HashMap<Integer, String>(8);
	
	private int _usedInventoryPercents = 0;
	private int _weightPercents = 0;
	private int _weightPenaltyLevel = 0;
	private boolean _overloaded = false;

	/**
	 * The current higher Expertise of the L2Player (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)
	 */
	private volatile int _expertiseIndex = 0;
	private volatile int _weaponExpertisePenaltyLevel = 0;
	private volatile int _armorExpertisePenaltyLevel = 0;

	private ItemInstance _enchantScroll = null;
	private int _enchantScrollValue = 0;
	private int _enchantCatalyst = 0;

	private WarehouseType _usingWHType;

	private boolean _isOnline = false;

	private AtomicBoolean _isLogout = new AtomicBoolean();

	/**
	 * The L2NpcInstance corresponding to the last Folk which one the player talked.
	 */
	private HardReference<NpcInstance> _lastNpc = HardReferences.emptyRef();
	/**
	 * тут храним мультиселл с которым работаем
	 */
	private MultiSellListContainer _multisell = null;

	private Set<Integer> _activeSoulShots = new CopyOnWriteArraySet<Integer>();
	private Set<Integer> _autoPotions = new CopyOnWriteArraySet<Integer>();

	private WorldRegion _observerRegion;
	private AtomicInteger _observerMode = new AtomicInteger(0);

	public int _telemode = 0;

	private int _handysBlockCheckerEventArena = -1;

	public boolean entering = true;

	/**
	 * Эта точка проверяется при нештатном выходе чара, и если не равна null чар возвращается в нее
	 * Используется например для возвращения при падении с виверны
	 * Поле heading используется для хранения денег возвращаемых при сбое
	 */
	private Location _stablePoint = null;

	/**
	 * new loto ticket *
	 */
	public int _loto[] = new int[5];
	/**
	 * new race ticket *
	 */
	public int _race[] = new int[2];

	private final Map<Integer, String> _blockList = new ConcurrentSkipListMap<Integer, String>(); // characters blocked with '/block <charname>' cmd
	private final FriendList _friendList = new FriendList(this);

	private boolean _hero = false;

	/**
	 * True if the L2Player is in a boat
	 */
	private Boat _boat;
	private Location _inBoatPosition;

	protected int _baseClass = -1;
	protected SubClass _activeClass = null;

	private Bonus _bonus = new Bonus();
	private Future<?> _bonusExpiration;

	private boolean _isSitting;
	private StaticObjectInstance _sittingObject;

	private boolean _noble = false;

	private boolean _inOlympiadMode;
	private OlympiadGame _olympiadGame;
	private OlympiadGame _olympiadObserveGame;

	private int _olympiadSide = -1;

	/**
	 * ally with ketra or varka related wars
	 */
	private int _varka = 0;
	private int _ketra = 0;
	private int _ram = 0;

	private byte[] _keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;

	private int _cursedWeaponEquippedId = 0;

	private final Fishing _fishing = new Fishing(this);
	private boolean _isFishing;

	private Future<?> _taskWater;
	private Future<?> _autoSaveTask;
	private Future<?> _kickTask;

	private Future<?> _vitalityTask;
	private Future<?> _pcCafePointsTask;
	private Future<?> _unjailTask;
	
	private Future<?> _autoCPTask;
	private Future<?> _autoHPTask;
	private Future<?> _autoMPTask;

	private final Lock _storeLock = new ReentrantLock();

	private int _zoneMask;

	private boolean _offline = false;

	private int _transformationId;
	private int _transformationTemplate;
	private String _transformationName;

	private int _pcBangPoints;

	private Map<Integer, Skill> _transformationSkills = new HashMap<Integer, Skill>();

	private int _expandInventory = 0;
	private int _expandWarehouse = 0;
	private int _battlefieldChatId;
	private int _lectureMark;

	private List<String> bypasses = null, bypasses_bbs = null;
	private IntObjectMap<String> _postFriends = Containers.emptyIntObjectMap();

	private List<String> _blockedActions = new ArrayList<String>();

	private boolean _notShowBuffAnim = false;
	private boolean _notShowTraders = false;
	private boolean _disableFogAndRain = false;
	private boolean _debug = false;

	private long _dropDisabled;
	private long _lastItemAuctionInfoRequest;
	
	private IntObjectMap<TimeStamp> _sharedGroupReuses = new CHashIntObjectMap<TimeStamp>();
	private Pair<Integer, OnAnswerListener> _askDialog = null;

	// High Five: Navit's Bonus System
	private NevitSystem _nevitSystem = new NevitSystem(this);

	private MatchingRoom _matchingRoom;
	private boolean _matchingRoomWindowOpened = false;
	private PetitionMainGroup _petitionGroup;
	private final Map<Integer, Long> _instancesReuses = new ConcurrentHashMap<Integer, Long>();
	private Supersnoop _supersnoop = new Supersnoop(this);
	
	public GameEvent _event = null;

	// Ping
	private int _ping = -1;
	private int _mtu = -1;
	
	// not used ?private boolean _isStuning, _isParalyzed = false;
	
	private List<Integer> _acceptedPMs = new ArrayList<>();
	
	private static ScheduledFuture<?> _startTaskMessage;
	private static ScheduledFuture<?> _startTaskPunishWyvern;
	
	/**
	 * Конструктор для L2Player. Напрямую не вызывается, для создания игрока используется PlayerManager.create
	 */
	public Player(final int objectId, final PlayerTemplate template, final String accountName)
	{
		super(objectId, template);

		_login = accountName;
		_collision_radius = template.getCollisionRadius();
		_collision_height = template.getCollisionHeight();
		_nameColor = 0xFFFFFF;
		_titlecolor = 0xFFFF77;
		_baseClass = getClassId().getId();
	}

	/**
	 * Constructor<?> of L2Player (use L2Character constructor).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2Player </li>
	 * <li>Create a L2Radar object</li>
	 * <li>Retrieve from the database all items of this L2Player and add them to _inventory </li>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SET the account name of the L2Player</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PlayerTemplate to apply to the L2Player
	 */
	private Player(final int objectId, final PlayerTemplate template)
	{
		this(objectId, template, null);

		_ai = new PlayerAI(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<Player> getRef()
	{
		return (HardReference<Player>) super.getRef();
	}

	public String getAccountName()
	{
		if(_connection == null)
			return _login;
		return _connection.getLogin();
	}

	public String getIP()
	{
		if(_connection == null)
			return NOT_CONNECTED;
		return _connection.getIpAddr();
	}

	public String getHWID()
	{
		if(_connection == null)
			return NOT_CONNECTED;
		
		return _connection.getHWID();
	}

	public boolean hasHWID()
	{
		if (Config.DISABLE_HWID_SUPPORT)
			return false;
		
		if (getPlayer() == null || getHWID() == null || getHWID().equalsIgnoreCase(NOT_CONNECTED))
			return false;
		
		return true;
	}
	
	public int getInstanceCount()
	{
		if(_connection == null)
			return 0;
		
		return _connection.getInstanceCount();
	}
	
	public void setInstanceCount(int count)
	{
		if(_connection == null)
			return;
		
		_connection.setInstanceCount(count);
	}
	
	/**
	 * Возвращает список персонажей на аккаунте, за исключением текущего
	 *
	 * @return Список персонажей
	 */
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}

	@Override
	public final PlayerTemplate getTemplate()
	{
		return (PlayerTemplate) _template;
	}

	@Override
	public PlayerTemplate getBaseTemplate()
	{
		return (PlayerTemplate) _baseTemplate;
	}

	public void changeSex()
	{
		_template = CharTemplateHolder.getInstance().getTemplate(getClassId(), (getSex() == 0));
	}

	public boolean isSameRace(Player initiator)
	{
		if (!initiator.isPlayer() || !isPlayer())
			return false;
		
		if (initiator.getRace() != getRace() && getRace() != Race.kamael && initiator.getRace() != Race.kamael && initiator.getRace() != Race.dwarf && getRace() != Race.dwarf)
			return false;
		
		return true;
	}
	
	@Override
	public PlayerAI getAI()
	{
		return (PlayerAI) _ai;
	}

	@Override
	public void doCast(final Skill skill, final Creature target, boolean forceUse)
	{
		if(skill == null)
			return;

		// Infern0 - used for ctf/tvt datapack events.
		if(_event != null && (!_event.canUseSkill(this, target, skill) && !canOverrideCond(PcCondOverride.SKILL_CONDITIONS)))
		{
			sendActionFailed();
			return;
		}
		
		super.doCast(skill, target, forceUse);

		//if(getUseSeed() != 0 && skill.getSkillType() == SkillType.SOWING)
		//	sendPacket(new ExUseSharedGroupItem(getUseSeed(), getUseSeed(), 5000, 5000));
	}

	@Override
	public void doAttack(Creature target)
	{
		if(_event != null && !_event.canAttack(this, target) && !canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
		{
			sendActionFailed();
			return;
		}
		
		super.doAttack(target);
	}

	@Override
	public void sendReuseMessage(Skill skill)
	{
		if(isCastingNow())
			return;
		TimeStamp sts = getSkillReuse(skill);
		if(sts == null || !sts.hasNotPassed())
			return;
		long timeleft = sts.getReuseCurrent();
		if(!Config.ALT_SHOW_REUSE_MSG && timeleft < 10000 || timeleft < 500)
			return;
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.);
		if(hours > 0)
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(hours).addNumber(minutes).addNumber(seconds));
		else if(minutes > 0)
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(minutes).addNumber(seconds));
		else
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(seconds));
	}

	@Override
	public final int getLevel()
	{
		return _activeClass == null ? 1 : _activeClass.getLevel();
	}
	
	/**
	 * 
	 * @return 0-No, 1-D, 2-C, 3-B, 4-A, 5-S, 6-S80, 7-S84
	 */
	public int getGrade()
	{
		switch (getSkillLevel(239))
		{
			case -1: return 0; // No-Grade
			case 1: return 1; // D-Grade
			case 2: return 2; // C-Grade
			case 3: return 3; // B-Grade
			case 4: return 4; // A-Grade
			case 5: return 5; // S-Grade
			case 6: return 6; // S80-Grade
			case 7: return 7; // S84-Grade
			default: return 0; // No-Grade
		}
	}

	public int getSex()
	{
		return getTemplate().isMale ? 0 : 1;
	}

	public int getFace()
	{
		return _face;
	}

	public void setFace(int face)
	{
		_face = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public void offline()
	{
		if (Config.TRANSFORM_ON_OFFLINE_TRADE)
		{
			if (getTransformation() == 0)
			{
				if(!getTemplate().isMale)
					setTransformation(Config.TRANSFORMATION_ID_FEMALE);
				else
					setTransformation(Config.TRANSFORMATION_ID_MALE);
				broadcastUserInfo(true);
			}
		}
		
		setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
		setOnlineTime(getOnlineTime()); // сохраняем текущее общее время онлайна
		setUptime(0); // сбрасываем аптайм
		setOfflineMode(true);

		setVar("offline", String.valueOf(System.currentTimeMillis() / 1000L), -1);

		if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
			startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000L);

		if (isInSearchOfAcademy())
		{
			setSearchforAcademy(false);
			AcademyList.deleteFromAcdemyList(this);
		}
		
		Party party = getParty();
		if(party != null)
		{
			if(isFestivalParticipant())
				party.sendMessage(getName() + " has been removed from the upcoming festival.");
			leaveParty();
		}

		if(getPet() != null)
		{
			getPet().saveEffects();
			getPet().unSummon();
		}

		if (this != null)
			EffectsDAO.getInstance().insert(this);
		
		CursedWeaponsManager.getInstance().doLogout(this);

		if(isInOlympiadMode() || getOlympiadGame() != null)
			Olympiad.logoutPlayer(this);

		if (Olympiad.isRegistered(this))
			Olympiad.unRegisterNoble(this);
		
        if(isInObserverMode())
        {
            if(getOlympiadObserveGame() == null)
                leaveObserverMode();
			else
				leaveOlympiadObserverMode(true);
			_observerMode.set(OBSERVER_NONE);
        }

		broadcastCharInfo();
		stopWaterTask();
		stopBonusTask();
		stopHourlyTask();
		stopAutoPotionTask(-1);
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		stopQuestTimers();
		getNevitSystem().stopTasksOnLogout();
		
		try
		{
			getSupersnoop().onDelete();
		}
		catch(Throwable t)
		{
			_log.error("Unable to delete supersnoop: ", t);
		}

		try
		{
			getInventory().store();
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
		
		if (isGM())
			AdminTable.getInstance().deleteGm(this);
		
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			_connection.close(ServerClose.STATIC);
			setClient(null);
		}
	}

	public void offlineBuffer()
	{
		setNameColor(Config.OFFLINE_BUFFER_NAME_COLOR);
		setTitleColor(Config.OFFLINE_BUFFER_TITLE_COLOR);
		setOfflineMode(true);

		setVar("offlinebuffer", String.valueOf(System.currentTimeMillis() / 1000L), -1);
		setVar("offlinebufferprice", OfflineBufferManager.getInstance().getBuffsAsString(this, false), -1);
		setVar("offlinebufferschemeprice", OfflineBufferManager.getInstance().getBuffsAsString(this, true), -1);
		setVar("offlinebuffrestoretitle", getTitle(), -1);
		setVar("offlinebuffertitle", "SELL " + getShortClassName(getClassId().getId()).toUpperCase() + " BUFFS", -1);
		
		if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
			startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000L);

		Party party = getParty();
		if(party != null)
		{
			if(isFestivalParticipant())
				party.sendMessage(getName() + " has been removed from the upcoming festival.");
			leaveParty();
		}

		if(getPet() != null)
		{
			getPet().saveEffects();
			getPet().unSummon();
		}

		if (this != null)
			EffectsDAO.getInstance().insert(this);
		
		CursedWeaponsManager.getInstance().doLogout(this);

		if(isInOlympiadMode() || getOlympiadGame() != null)
			Olympiad.logoutPlayer(this);

		if (Olympiad.isRegistered(this))
			Olympiad.unRegisterNoble(this);
		
        if(isInObserverMode())
        {
            if(getOlympiadObserveGame() == null)
                leaveObserverMode();
			else
				leaveOlympiadObserverMode(true);
			_observerMode.set(OBSERVER_NONE);
        }

		broadcastCharInfo();
		stopWaterTask();
		stopBonusTask();
		stopAutoPotionTask(-1);
		stopHourlyTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		stopQuestTimers();
		getNevitSystem().stopTasksOnLogout();

		try
		{
			getInventory().store();
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
		
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			_connection.close(ServerClose.STATIC);
			setClient(null);
		}
	}
	
	/**
	 * Соединение закрывается, клиент закрывается, персонаж сохраняется и удаляется из игры
	 * Не пишем надпись NO CARRIER, не ждем
	 */
	public void kick()
	{
		try
		{
			if (Config.ALLOW_CURSED_WEAPONS && Config.DROP_CURSED_WEAPONS_ON_KICK)
				if (isCursedWeaponEquipped())
				{
					_pvpFlag = 0;
					CursedWeaponsManager.getInstance().dropPlayer(this);
				}
			
			prepareToLogout();
			deleteMe();
			
			if (_connection != null)
			{
				_connection.close(LeaveWorld.STATIC);
				setClient(null);
			}
		}
		catch (Exception e)
		{
			_log.error("kick() error: ", e);
		}
	}

	/**
	 * Соединение не закрывается, клиент не закрывается, персонаж сохраняется и удаляется из игры
	 */
	public void restart(boolean noCarrier)
	{
		try
		{
			noCarrier &= Config.SERVICES_ENABLE_NO_CARRIER;
			noCarrier &= !NexusEvents.isInEvent(this);
			noCarrier &= getVar("noCarrier") != null;
			
			if (isInSearchOfAcademy())
			{
				setSearchforAcademy(false);
				AcademyList.deleteFromAcdemyList(this);
			}
			
			if (Config.ALLOW_CURSED_WEAPONS && Config.DROP_CURSED_WEAPONS_ON_KICK && isCursedWeaponEquipped())
			{
				_pvpFlag = 0;
				CursedWeaponsManager.getInstance().dropPlayer(this);
			}
			
			if (noCarrier) // noCarrier system.
			{
				scheduleDelete();
			}
			else
			{
				prepareToLogout();
				deleteMe();
			}
			
			if (_connection != null)
			{
				_connection.setActiveChar(null);
				setClient(null);
			}
		}
		catch (Exception e)
		{
			_log.error("restart() error: ", e);
		}
	}
	
	public void logout()
	{
		logout(false);
	}

	/**
	 * Соединение закрывается, клиент не закрывается, персонаж сохраняется и удаляется из игры
	 * Пишем надпись NO CARRIER
	 */
	public void logout(boolean noCarrier)
	{
		try
		{
			noCarrier &= Config.SERVICES_ENABLE_NO_CARRIER;
			noCarrier &= !NexusEvents.isInEvent(this);

			if(Config.ALLOW_CURSED_WEAPONS && Config.DROP_CURSED_WEAPONS_ON_KICK)
			{
				if(isCursedWeaponEquipped())
				{
					_pvpFlag = 0;
					CursedWeaponsManager.getInstance().dropPlayer(this);
				}
			}
			
			if (isInSearchOfAcademy())
			{
				setSearchforAcademy(false);
				AcademyList.deleteFromAcdemyList(this);
			}
			
			if (noCarrier)
			{
				scheduleDelete();
			}
			else
			{
				prepareToLogout();
				deleteMe();
			}
			
			if(_connection != null)
			{
				_connection.close(LeaveWorld.STATIC);
				setClient(null);
			}
			
		}
		catch (Exception e)
		{
			_log.error("logout() error: ", e);
		}
		
	}

	private void prepareToLogout()
	{
		if(_isLogout.getAndSet(true))
			return;

		setIsOnline(false);

		getListeners().onExit();

		if(isFlying() && !checkLandingState())
			_stablePoint = Location.getRestartLocation(this, RestartType.TO_VILLAGE);

		if(isCastingNow())
			abortCast(true, true);

		Party party = getParty();

		if(party != null || NexusEvents.isInEvent(this))
		{
			if(isFestivalParticipant())
				party.sendMessage(getName() + " has been removed from the upcoming festival.");
			leaveParty();
		}

		CursedWeaponsManager.getInstance().doLogout(this);

		if(_olympiadObserveGame != null)
			_olympiadObserveGame.removeSpectator(this);

		if(isInOlympiadMode() || getOlympiadGame() != null)
			Olympiad.logoutPlayer(this);

		if (Olympiad.isRegistered(this))
			Olympiad.unRegisterNoble(this);
		
		stopFishing();

        if(isInObserverMode())
        {
            if(getOlympiadObserveGame() == null)
                leaveObserverMode();
			else
				leaveOlympiadObserverMode(true);
			_observerMode.set(OBSERVER_NONE);
        }
        
		if(_stablePoint != null)
			teleToLocation(_stablePoint);

		Summon pet = getPet();
		if(pet != null)
		{
			pet.saveEffects();
			pet.unSummon();
		}

		if (this != null)
			EffectsDAO.getInstance().insert(this);
		
		_friendList.notifyFriends(false);

		if(isProcessingRequest())
			getRequest().cancel();

		stopAllTimers();

		if(isInBoat())
			getBoat().removePlayer(this);

		SubUnit unit = getSubUnit();
		UnitMember member = unit == null ? null : unit.getUnitMember(getObjectId());
		if(member != null)
		{
			int sponsor = member.getSponsor();
			int apprentice = getApprentice();
			PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
			for(Player clanMember : _clan.getOnlineMembers(getObjectId()))
			{
				clanMember.sendPacket(memberUpdate);
				if(clanMember.getObjectId() == sponsor)
					clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_APPRENTICE_HAS_LOGGED_OUT).addString(_name));
				else if(clanMember.getObjectId() == apprentice)
					clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_SPONSOR_HAS_LOGGED_OUT).addString(_name));
			}
			member.setPlayerInstance(this, true);
		}

		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
			attachment.onLogout(this);

		if(CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()) != null)
			CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).setPlayer(null);

		MatchingRoom room = getMatchingRoom();
		if(room != null)
		{
			if(room.getLeader() == this)
				room.disband();
			else
				room.removeMember(this, false);
		}
		setMatchingRoom(null);

		MatchingRoomManager.getInstance().removeFromWaitingList(this);

		destroyAllTraps();

		if(_decoy != null)
		{
			_decoy.unSummon();
			_decoy = null;
		}

		stopPvPFlag();

		if (_event != null)
			_event.onLogout(this);

		Reflection ref = getReflection();

		if(ref != ReflectionManager.DEFAULT)
		{
			if(ref.getReturnLoc() != null)
				_stablePoint = ref.getReturnLoc();

			ref.removeObject(this);
		}

		// Bot punishment
		if(Config.ENABLE_AUTO_HUNTING_REPORT)
		{
			// Save punish
			if(isBeingPunished())
			{
				try
				{
					AutoHuntingManager.getInstance().savePlayerPunish(this);
				}
				catch(Exception e)
				{
					_log.warn("deleteMe()", e);
				}
			}
			// Save report points left
			if(_account != null)
			{
				try
				{
					_account.updatePoints(_login);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		try
		{
			getInventory().store();
			getRefund().clear();
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
		try
		{
			NexusEvents.onLogout(this);
		}
		catch (Exception e)
		{
			_log.error("deleteMe() - nexus events", e);
		}
		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
	}

	/**
	 * @return a table containing all L2RecipeList of the L2Player.<BR><BR>
	 */
	public Collection<Recipe> getDwarvenRecipeBook()
	{
		return _recipebook.values();
	}

	public Collection<Recipe> getCommonRecipeBook()
	{
		return _commonrecipebook.values();
	}

	public int recipesCount()
	{
		return _commonrecipebook.size() + _recipebook.size();
	}

	public boolean hasRecipe(final Recipe id)
	{
		return _recipebook.containsValue(id) || _commonrecipebook.containsValue(id);
	}

	public boolean findRecipe(final int id)
	{
		return _recipebook.containsKey(id) || _commonrecipebook.containsKey(id);
	}

	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void registerRecipe(final Recipe recipe, boolean saveDB)
	{
		if(recipe == null)
			return;
		if(recipe.isDwarvenRecipe())
			_recipebook.put(recipe.getId(), recipe);
		else
			_commonrecipebook.put(recipe.getId(), recipe);
		if(saveDB)
			mysql.set("REPLACE INTO character_recipebook (char_id, id) VALUES(?,?)", getObjectId(), recipe.getId());
	}

	/**
	 * Remove a L2RecipList from the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void unregisterRecipe(final int RecipeID)
	{
		if(_recipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_recipebook.remove(RecipeID);
		}
		else if(_commonrecipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_commonrecipebook.remove(RecipeID);
		}
		else
			_log.warn("Attempted to remove unknown RecipeList" + RecipeID);
	}

	// ------------------- Quest Engine ----------------------

	public QuestState getQuestState(String quest)
	{
		questRead.lock();
		try
		{
			return _quests.get(quest);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public QuestState getQuestState(Class<?> quest)
	{
		return getQuestState(quest.getSimpleName());
	}

	public boolean isQuestCompleted(String quest)
	{
		QuestState q = getQuestState(quest);
		return q != null && q.isCompleted();
	}

	public boolean isQuestCompleted(Class<?> quest)
	{
		QuestState q = getQuestState(quest);
		return q != null && q.isCompleted();
	}

	public void setQuestState(QuestState qs)
	{
		questWrite.lock();
		try
		{
			_quests.put(qs.getQuest().getName(), qs);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public void removeQuestState(String quest)
	{
		questWrite.lock();
		try
		{
			_quests.remove(quest);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public Quest[] getAllActiveQuests()
	{
		List<Quest> quests = new ArrayList<Quest>(_quests.size());
		questRead.lock();
		try
		{
			for(final QuestState qs : _quests.values())
				if(qs.isStarted())
					quests.add(qs.getQuest());
		}
		finally
		{
			questRead.unlock();
		}
		return quests.toArray(new Quest[quests.size()]);
	}

	public QuestState[] getAllQuestsStates()
	{
		questRead.lock();
		try
		{
			return _quests.values().toArray(new QuestState[_quests.size()]);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public List<QuestState> getQuestsForEvent(NpcInstance npc, QuestEventType event)
	{
		List<QuestState> states = new ArrayList<QuestState>();
		Quest[] quests = npc.getTemplate().getEventQuests(event);
		QuestState qs;
		if(quests != null)
			for(Quest quest : quests)
			{
				qs = getQuestState(quest.getName());
				if(qs != null && !qs.isCompleted())
					states.add(getQuestState(quest.getName()));
			}
		return states;
	}

	public void processQuestEvent(String quest, String event, NpcInstance npc)
	{
		if(event == null)
			event = "";
		QuestState qs = getQuestState(quest);
		if(qs == null)
		{
			Quest q = QuestManager.getQuest(quest);
			if(q == null)
			{
				_log.warn("Quest " + quest + " not found!");
				return;
			}
			qs = q.newQuestState(this, Quest.CREATED);
		}
		if(qs == null || qs.isCompleted())
			return;
		qs.getQuest().notifyEvent(event, qs, npc);
		sendPacket(new QuestList(this));
	}

	/**
	 * Проверка на переполнение инвентаря и перебор в весе для квестов и эвентов
	 *
	 * @return true если ве проверки прошли успешно
	 */
	public boolean isQuestContinuationPossible(boolean msg)
	{
		if(getWeightPercents() >= 80 || getUsedInventoryPercents() >= 90 || Config.QUEST_INVENTORY_MAXIMUM * 0.9 < getInventory().getQuestSize())
		{
			if(msg)
				sendPacket(Msg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
			return false;
		}
		return true;
	}

	/**
	 * Останавливаем и запоминаем все квестовые таймеры
	 */
	public void stopQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
			if(qs.isStarted())
				qs.pauseQuestTimers();
			else
				qs.stopQuestTimers();
	}

	/**
	 * Восстанавливаем все квестовые таймеры
	 */
	public void resumeQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
			qs.resumeQuestTimers();
	}

	// ----------------- End of Quest Engine -------------------

	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}

	public ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}

	public void registerShortCut(ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}

	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}

	public void registerMacro(Macro macro)
	{
		_macroses.registerMacro(macro);
	}

	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}

	public MacroList getMacroses()
	{
		return _macroses;
	}

	public boolean isCastleLord(int castleId)
	{
		return _clan != null && isClanLeader() && _clan.getCastle() == castleId;
	}

	/**
	 * Проверяет является ли этот персонаж владельцем крепости
	 *
	 * @param fortressId
	 * @return true если владелец
	 */
	public boolean isFortressLord(int fortressId)
	{
		return _clan != null && isClanLeader() && _clan.getHasFortress() == fortressId;
	}

	public int getPkKills()
	{
		return _pkKills;
	}

	public void setPkKills(final int pkKills)
	{
		_pkKills = pkKills;
	}

	public long getCreateTime()
	{
		return _createTime;
	}

	public void setCreateTime(final long createTime)
	{
		_createTime = createTime;
	}

	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(final int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	public int getCurrentLoad()
	{
		return getInventory().getTotalWeight();
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public void setLastAccess(long value)
	{
		_lastAccess = value;
	}

	public int getRecomHave()
	{
		return _recomHave;
	}

	public void setRecomHave(int value)
	{
		if(value > 255)
			_recomHave = 255;
		else if(value < 0)
			_recomHave = 0;
		else
			_recomHave = value;
	}

	public int getRecomBonusTime()
	{
		if(_recomBonusTask != null)
			return (int) Math.max(0, _recomBonusTask.getDelay(TimeUnit.SECONDS));
		return _recomBonusTime;
	}

	public void setRecomBonusTime(int val)
	{
		_recomBonusTime = val;
	}

	public int getRecomLeft()
	{
		return _recomLeft;
	}

	public void setRecomLeft(final int value)
	{
		_recomLeft = value;
	}

	public boolean isHourglassEffected()
	{
		return _isHourglassEffected;
	}

	public void setHourlassEffected(boolean val)
	{
		_isHourglassEffected = val;
	}

	public void startHourglassEffect()
	{
		setHourlassEffected(true);
		stopRecomBonusTask(true);
		sendVoteSystemInfo();
	}

	public void stopHourglassEffect()
	{
		setHourlassEffected(false);
		startRecomBonusTask();
		sendVoteSystemInfo();
	}

	public int addRecomLeft()
	{
		int recoms = 0;
		if(getRecomLeftToday() < 20)
			recoms = 10;
		else
			recoms = 1;
		setRecomLeft(getRecomLeft() + recoms);
		setRecomLeftToday(getRecomLeftToday() + recoms);
		sendUserInfo(true);
		return recoms;
	}

	public int getRecomLeftToday()
	{
		return _recomLeftToday;
	}

	public void setRecomLeftToday(final int value)
	{
		_recomLeftToday = value;
		setVar("recLeftToday", String.valueOf(_recomLeftToday), -1);
	}

	public void giveRecom(final Player target)
	{
		int targetRecom = target.getRecomHave();
		if(targetRecom < 255)
			target.addRecomHave(1);
		if(getRecomLeft() > 0)
			setRecomLeft(getRecomLeft() - 1);

		sendUserInfo(true);
	}

	public void addRecomHave(final int val)
	{
		setRecomHave(getRecomHave() + val);
		broadcastUserInfo(true);
		sendVoteSystemInfo();
	}

	public int getRecomBonus()
	{
		if(getRecomBonusTime() > 0 || isHourglassEffected())
			return RecomBonus.getRecoBonus(this);
		return 0;
	}

	public double getRecomBonusMul()
	{
		if(getRecomBonusTime() > 0 || isHourglassEffected())
			return RecomBonus.getRecoMultiplier(this);
		return 1;
	}

	public void sendVoteSystemInfo()
	{
		sendPacket(new ExVoteSystemInfo(this));
	}

	public boolean isRecomTimerActive()
	{
		return _isRecomTimerActive;
	}

	public void setRecomTimerActive(boolean val)
	{
		if(_isRecomTimerActive == val)
			return;

		_isRecomTimerActive = val;

		if(val)
			startRecomBonusTask();
		else
			stopRecomBonusTask(true);

		sendVoteSystemInfo();
	}

	private ScheduledFuture<?> _recomBonusTask;

	public void startRecomBonusTask()
	{
		if(_recomBonusTask == null && getRecomBonusTime() > 0 && isRecomTimerActive() && !isHourglassEffected())
			_recomBonusTask = ThreadPoolManager.getInstance().schedule(new RecomBonusTask(this), getRecomBonusTime() * 1000);
	}

	public void stopRecomBonusTask(boolean saveTime)
	{
		if(_recomBonusTask != null)
		{
			if(saveTime)
				setRecomBonusTime((int) Math.max(0, _recomBonusTask.getDelay(TimeUnit.SECONDS)));
			_recomBonusTask.cancel(false);
			_recomBonusTask = null;
		}
	}

	// TODO: Infern0 test it..
	private ScheduledFuture<?> _chargeCountDownTask;

	public void stopChargeForceCountDown()
	{
		if (_chargeCountDownTask != null)
		{
			_chargeCountDownTask.cancel(false);
			_chargeCountDownTask = null;
		}
	}
	
	public void startChargeForceCountDown()
	{
		if (_chargeCountDownTask == null)
			_chargeCountDownTask = ThreadPoolManager.getInstance().schedule(new ChargeCountDownTask(this), 600000L);
	}
	
	@Override
	public int getKarma()
	{
		return _karma;
	}

	public void setKarma(int karma)
	{
		if (NexusEvents.isInEvent(this))
			return;

		if(karma < 0)
			karma = 0;

		if(_karma == karma)
			return;

		_karma = karma;

		sendChanges();

		if(getPet() != null)
			getPet().broadcastCharInfo();
	}

	@Override
	public int getMaxLoad()
	{
		// Weight Limit = (CON Modifier*69000)*Skills
		// Source http://l2r.bravehost.com/weightlimit.html (May 2007)
		// Fitted exponential curve to the data
		int con = getCON();
		if(con < 1)
			return (int) (31000 * Config.MAXLOAD_MODIFIER);
		else if(con > 59)
			return (int) (176000 * Config.MAXLOAD_MODIFIER);
		else
			return (int) calcStat(Stats.MAX_LOAD, Math.pow(1.029993928, con) * 30495.627366 * Config.MAXLOAD_MODIFIER, this, null);
	}

	private Future<?> _updateEffectIconsTask;

	private class UpdateEffectIcons extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}

	@Override
	public void updateEffectIcons()
	{
		if(entering || isLogoutStarted())
			return;

		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateEffectIconsTask != null)
			{
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}

		if(_updateEffectIconsTask != null)
			return;

		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	private void updateEffectIconsImpl()
	{
		Effect[] effects = getEffectList().getAllFirstEffects();
		Arrays.sort(effects, EffectsComparator.getInstance());

		PartySpelled ps = new PartySpelled(this, false);
		AbnormalStatusUpdate mi = new AbnormalStatusUpdate();

		for(Effect effect : effects)
			if(effect.isInUse())
			{
				if(effect.getStackType().equals(EffectTemplate.HP_RECOVER_CAST))
					sendPacket(new ShortBuffStatusUpdate(effect));
				else
					effect.addIcon(mi);
				if(_party != null)
					effect.addPartySpelledIcon(ps);
			}

		sendPacket(mi);
		if(_party != null)
			_party.sendPacket(ps);

		if(isInOlympiadMode() && isOlympiadCompStart())
		{
			OlympiadGame olymp_game = _olympiadGame;
			if(olymp_game != null)
			{
				ExOlympiadSpelledInfo olympiadSpelledInfo = new ExOlympiadSpelledInfo();

				for(Effect effect : effects)
					if(effect != null && effect.isInUse())
						effect.addOlympiadSpelledIcon(this, olympiadSpelledInfo);

				if(olymp_game.getType() == CompType.CLASSED || olymp_game.getType() == CompType.NON_CLASSED)
					for(Player member : olymp_game.getTeamMembers(this))
						member.sendPacket(olympiadSpelledInfo);

				for(Player member : olymp_game.getSpectators())
					member.sendPacket(olympiadSpelledInfo);
			}
		}
	}

	public int getUsedInventoryPercents()
	{
		return _usedInventoryPercents;
	}

	public int getWeightPercents()
	{
		return _weightPercents;
	}

	public int getWeightPenalty()
	{
		return _weightPenaltyLevel;
	}

	public void refreshOverloaded()
	{
		if(isLogoutStarted())
			return;

		if (canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
			return;
		
		final int maxLoad = getMaxLoad();
		if (maxLoad <= 0)
			return;

		int inventoryLimit = getInventoryLimit();
		if (inventoryLimit <= 0)
			inventoryLimit = 1;
		_usedInventoryPercents = (int)(100. * (double)getInventory().getSize() / inventoryLimit);

		int weightPercents = (int)(100. * (getCurrentLoad() - calcStat(Stats.MAX_NO_PENALTY_LOAD, 0, this, null)) / maxLoad);
		if (weightPercents < 0)
			weightPercents = 0;
		_weightPercents = weightPercents;

		int newWeightPenalty = 0;
		if (weightPercents > 100)
		{
			setOverloaded(true);
			newWeightPenalty = 4;
		}
		else
		{
			setOverloaded(false);
			if(weightPercents < 50)
				newWeightPenalty = 0;
			else if(weightPercents < 67)
				newWeightPenalty = 1;
			else if(weightPercents < 80)
				newWeightPenalty = 2;
			else
				newWeightPenalty = 3;
		}

		if (_weightPenaltyLevel == newWeightPenalty)
			return;
		_weightPenaltyLevel = newWeightPenalty;

		if(newWeightPenalty > 0)
			super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
		else
			super.removeSkill(getKnownSkill(4270));

		sendPacket(new SkillList(this));
		sendEtcStatusUpdate();
		updateStats();
	}

	public int getExpertiseIndex()
	{
		return _expertiseIndex;
	}

	public int getArmorsExpertisePenalty()
	{
		return _armorExpertisePenaltyLevel;
	}

	public int getWeaponsExpertisePenalty()
	{
		return _weaponExpertisePenaltyLevel;
	}

	public int getExpertisePenalty(ItemInstance item)
	{
		if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
			return getWeaponsExpertisePenalty();
		else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
			return getArmorsExpertisePenalty();
		return 0;
	}

	public void refreshExpertisePenalty()
	{
		if(isLogoutStarted())
			return;

		// Calculate the current higher Expertise of the L2Player
		int level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null);
		int i = 0;
		for(i = 0; i < EXPERTISE_LEVELS.length; i++)
			if(level < EXPERTISE_LEVELS[i + 1])
				break;

		boolean skillUpdate = false; // Для того, чтобы лишний раз не посылать пакеты
		// Add the Expertise skill corresponding to its Expertise level
		if(_expertiseIndex != i)
		{
			_expertiseIndex = i;
			if(_expertiseIndex > 0)
			{
				addSkill(SkillTable.getInstance().getInfo(239, _expertiseIndex), false);
				skillUpdate = true;
			}
		}

		int newWeaponPenalty = 0;
		int newArmorPenalty = 0;
		ItemInstance[] items = getInventory().getPaperdollItems();
		for(ItemInstance item : items)
			if(item != null)
			{
				int penalty = item.getCrystalType().ordinal() - _expertiseIndex;
				if (penalty <= 0)
					continue;

				switch (item.getTemplate().getType2())
				{
					case ItemTemplate.TYPE2_WEAPON:
						if(penalty > newWeaponPenalty)
							newWeaponPenalty = penalty;
						break;
					case ItemTemplate.TYPE2_SHIELD_ARMOR:
					case ItemTemplate.TYPE2_ACCESSORY:
						newArmorPenalty++;
						break;
				}
			}

		if(newWeaponPenalty > 4)
			newWeaponPenalty = 4;

		if(newArmorPenalty > 4)
			newArmorPenalty = 4;

		if(_weaponExpertisePenaltyLevel != newWeaponPenalty)
		{
			_weaponExpertisePenaltyLevel = newWeaponPenalty;
			if(newWeaponPenalty > 0 && Config.EXPERTISE_PENALTY)
				super.addSkill(SkillTable.getInstance().getInfo(6209, _weaponExpertisePenaltyLevel));
			else
				super.removeSkill(getKnownSkill(6209));
			skillUpdate = true;
		}
		if(_armorExpertisePenaltyLevel != newArmorPenalty)
		{
			_armorExpertisePenaltyLevel = newArmorPenalty;
			if(newArmorPenalty > 0 && Config.EXPERTISE_PENALTY)
				super.addSkill(SkillTable.getInstance().getInfo(6213, _armorExpertisePenaltyLevel));
			else
				super.removeSkill(getKnownSkill(6213));
			skillUpdate = true;
		}

		if(skillUpdate)
		{
			getInventory().validateItemsSkills();

			sendPacket(new SkillList(this));
			sendEtcStatusUpdate();
			updateStats();
		}
	}

	public int getPvpKills()
	{
		return _pvpKills;
	}

	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	public ClassId getClassId()
	{
		return getTemplate().classId;
	}

	public void addClanPointsOnProfession(final int id)
	{
		if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 2)
			_clan.incReputation(100, true, "Academy");
		else if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 3)
		{
			int earnedPoints = 0;
			if(getLvlJoinedAcademy() <= 16)
				earnedPoints = Config.MAX_ACADEM_POINT;
			else if(getLvlJoinedAcademy() >= 39)
				earnedPoints = Config.MIN_ACADEM_POINT;
			else
				earnedPoints = Config.MAX_ACADEM_POINT - (getLvlJoinedAcademy() - 16) * 20;

			_clan.removeClanMember(getObjectId());

			SystemMessage sm = new SystemMessage(SystemMessage.CLAN_ACADEMY_MEMBER_S1_HAS_SUCCESSFULLY_COMPLETED_THE_2ND_CLASS_TRANSFER_AND_OBTAINED_S2_CLAN_REPUTATION_POINTS);
			sm.addString(getName());
			sm.addNumber(_clan.incReputation(earnedPoints, true, "Academy"));
			_clan.broadcastToOnlineMembers(sm);
			_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListDelete(getName()), this);

			AcademyList.removeAcademyFromDB(_clan, getObjectId(), true, false);
			
			setClan(null);
			setTitle("");
			sendPacket(Msg.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES);
			setLeaveClanTime(0);

			broadcastCharInfo();

			sendPacket(PledgeShowMemberListDeleteAll.STATIC);

			ItemFunctions.addItem(this, 8181, 1, true);
		}
	}

	/**
	 * Set the template of the L2Player.
	 *
	 * @param id The Identifier of the L2PlayerTemplate to set to the L2Player
	 */
	@SuppressWarnings("incomplete-switch")
	public synchronized void setClassId(final int id, boolean noban, boolean fromQuest)
	{
		ClassId curClassId = ClassId.VALUES[getActiveClassId()];
		if(!noban && !ClassId.VALUES[id].equalsOrChildOf(curClassId) && !isGM())
		{
			sendMessage(new CustomMessage("l2r.gameserver.model.Player.message1", this));
			Thread.dumpStack();
			return;
		}

		// If the new ID does not belong to existing classes means a new Prof
		if(!getSubClasses().containsKey(id))
		{
			final SubClass cclass = getActiveClass();
			getSubClasses().remove(getActiveClassId());
			changeClassInDb(cclass.getClassId(), id);
			if(cclass.isBase())
			{
				setBaseClass(id);
				addClanPointsOnProfession(id);
				ItemInstance coupons = null;
				if(ClassId.VALUES[id].getLevel() == 2)
				{
					if(fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemFunctions.createItem(8869);
					unsetVar("newbieweapon");
					unsetVar("p1q2");
					unsetVar("p1q3");
					unsetVar("p1q4");
					unsetVar("prof1");
					unsetVar("ng1");
					unsetVar("ng2");
					unsetVar("ng3");
					unsetVar("ng4");
				}
				else if(ClassId.VALUES[id].getLevel() == 3)
				{
					if(fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemFunctions.createItem(8870);
					unsetVar("newbiearmor");
					unsetVar("dd1"); // удаляем отметки о выдаче дименшен даймондов
					unsetVar("dd2");
					unsetVar("dd3");
					unsetVar("prof2.1");
					unsetVar("prof2.2");
					unsetVar("prof2.3");
				}
				else if (ClassId.VALUES[id].getLevel() == 4) // Third prof
				{
					// When third class is changed by uncommon methods, change olympiad data if neccessary.
					// I had to do this when a Titan hero is actually listen as a GK hero. He was GK before being changed to Titan by a GM.
					if (Olympiad.isNoble(getObjectId()))
					{
						StatsSet statDat = Olympiad._nobles.get(getObjectId());
						statDat.set(Olympiad.CLASS_ID, id);
						OlympiadNobleDAO.getInstance().replace(getObjectId());
					}
				}

				if(coupons != null)
				{
					coupons.setCount(15);
					sendPacket(SystemMessage2.obtainItems(coupons));
					getInventory().addItem(coupons);
				}
			}

			if (Config.CUSTOM_CLASS_TRANSFER_SKILLS)
			{
				// custom for pvp server.
				switch(ClassId.VALUES[id])
				{
					case cardinal:
						ItemFunctions.addItem(this, 15307, 4, true);
						break;
					case evaSaint:
						ItemFunctions.addItem(this, 15308, 4, true);
						break;
					case shillienSaint:
						ItemFunctions.addItem(this, 15309, 4, true);
						break;
					case spectralDancer:
						ItemFunctions.addItem(this, 15309, 5, true);
						break;
					case swordMuse:
						ItemFunctions.addItem(this, 15308, 5, true);
						break;
					case hierophant:
						ItemFunctions.addItem(this, 15307, 5, true);
						break;
				}
			}
			else
			{
				// retail give of Pomander
				switch(ClassId.VALUES[id])
				{
					case cardinal:
						ItemFunctions.addItem(this, 15307, 1, true);
						break;
					case evaSaint:
						ItemFunctions.addItem(this, 15308, 1, true);
						break;
					case shillienSaint:
						ItemFunctions.addItem(this, 15309, 4, true);
						break;
					default:
						break;
				}
			}

			cclass.setClassId(id);
			getSubClasses().put(id, cclass);
			rewardSkills(true);
			storeCharSubClasses();

			if(fromQuest)
			{
				// Социалка при получении профы
				broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0));
				sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
			}
			broadcastCharInfo();
		}

		PlayerTemplate t = CharTemplateHolder.getInstance().getTemplate(id, getSex() == 1);
		if(t == null)
		{
			_log.error("Missing template for classId: " + id);
			// do not throw error - only print error
			return;
		}

		// Set the template of the L2Player
		_template = t;

		// Update class icon in party and clan
		if(isInParty())
			getParty().sendPacket(new PartySmallWindowUpdate(this));
		if(getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		if(_matchingRoom != null)
			_matchingRoom.broadcastPlayerUpdate(this);
	}

	public long getExp()
	{
		return _activeClass == null ? 0 : _activeClass.getExp();
	}

	public long getMaxExp()
	{
		return _activeClass == null ? Experience.LEVEL[Experience.getMaxLevel() + 1] : _activeClass.getMaxExp();
	}

	public void setEnchantScroll(final ItemInstance scroll)
	{
		setEnchantScroll(scroll, 0);
	}

	public void setEnchantScroll(final ItemInstance scroll, final int value)
	{
		_enchantScroll = scroll;
		_enchantScrollValue = value;
	}

	public ItemInstance getEnchantScroll()
	{
		return _enchantScroll;
	}
	
	public int getEnchantScrollValue()
	{
		return _enchantScrollValue;
	}
	
	public void setEnchantCatalyst(int itemId)
	{
		_enchantCatalyst = itemId;
	}

	public int getEnchantCatalyst()
	{
		return _enchantCatalyst;
	}

	public void setFistsWeaponItem(final WeaponTemplate weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}

	public WeaponTemplate getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}

	public WeaponTemplate findFistsWeaponItem(final int classId)
	{
		//human fighter fists
		if(classId >= 0x00 && classId <= 0x09)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(246);

		//human mage fists
		if(classId >= 0x0a && classId <= 0x11)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(251);

		//elven fighter fists
		if(classId >= 0x12 && classId <= 0x18)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(244);

		//elven mage fists
		if(classId >= 0x19 && classId <= 0x1e)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(249);

		//dark elven fighter fists
		if(classId >= 0x1f && classId <= 0x25)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(245);

		//dark elven mage fists
		if(classId >= 0x26 && classId <= 0x2b)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(250);

		//orc fighter fists
		if(classId >= 0x2c && classId <= 0x30)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(248);

		//orc mage fists
		if(classId >= 0x31 && classId <= 0x34)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(252);

		//dwarven fists
		if(classId >= 0x35 && classId <= 0x39)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(247);

		return null;
	}

	private boolean _isVitalityStop = false;

	public void VitalityStop(boolean stop)
	{
		_isVitalityStop = stop;
	}

	private boolean isVitalityStop()
	{
		return _isVitalityStop;
	}
	
	public void addExpAndCheckBonus(MonsterInstance mob, final double noRateExp, double noRateSp, double partyVitalityMod)
	{
		if(_activeClass == null)
			return;

		// Начисление душ камаэлям
		double neededExp = calcStat(Stats.SOULS_CONSUME_EXP, 0, mob, null);
		if(neededExp > 0 && noRateExp > neededExp)
		{
			mob.broadcastPacket(new SpawnEmitter(mob, this));
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.SoulConsumeTask(this), 1000);
		}

		double vitalityBonus = 1.0;
		int npcLevel = mob.getLevel();
		if(Config.ALT_VITALITY_ENABLED)
		{
			boolean blessActive = getNevitSystem().isBlessingActive();
			// Retail formula for vitality. 
			vitalityBonus *= (mob.isRaid() || getVitalityLevel(blessActive) <= 0) ? 1.0 : ((double)Config.ALT_VITALITY_RATE[getVitalityLevel(blessActive) - 1] / 100);
			if(noRateExp > 0)
			{
				if(!isVitalityStop())
				{
					if(!mob.isRaid())
					{
						// TODO: Разобратся, нельзя предметы использовать, или предметы не будут давать эффекта?
						// (Все предметы для восполнения или поддержания энергии не действуют во время действия Нисхождения Невитта)
						if(!blessActive && !(getVarB("NoExp") && getExp() == Experience.LEVEL[getLevel() + 1] - 1))
						{
							double points = ((noRateExp / (npcLevel * npcLevel)) * 100) / 9;
							points *= Config.ALT_VITALITY_CONSUME_RATE;

							if(getEffectList().getEffectByType(EffectType.Vitality) != null)
								points *= -1;

							setVitality(getVitality() - points * partyVitalityMod);
						}
					}
					else
						setVitality(getVitality() + Config.ALT_VITALITY_RAID_BONUS);
				}
				else
					setVitality(getVitality());
			}
		}

		//При первом вызове, активируем таймеры бонусов.
		if(!isInPeaceZone())
		{
			setRecomTimerActive(true);
			getNevitSystem().startAdventTask();
			if((getLevel() - npcLevel) <= 9)
			{
				int nevitPoints = (int) Math.round(((noRateExp / (npcLevel * npcLevel)) * 100) / 20);
				getNevitSystem().addPoints(nevitPoints);
			}
		}

		// Block exp.
		if (getVarB("NoExp"))
			return;
		double rateXP = Config.RATE_XP;
		double rateSP = Config.RATE_SP;
		
		if (!Config.EXP_RATE_BY_LEVEL.isEmpty())
		{
			if (Config.EXP_RATE_BY_LEVEL.get(getLevel()) > 0)
				rateXP = Config.EXP_RATE_BY_LEVEL.get(getLevel());
		}
		
		if (!Config.SP_RATE_BY_LEVEL.isEmpty())
		{
			if (Config.SP_RATE_BY_LEVEL.get(getLevel()) > 0)
				rateSP = Config.SP_RATE_BY_LEVEL.get(getLevel());
		}
		
		long normalExp = (long)  (noRateExp * ((rateXP * getRateExp() * (vitalityBonus > 0 ? vitalityBonus : 1.0)) * getRecomBonusMul()));
		long normalSp = (long)  (noRateSp * (rateSP * getRateSp() * (vitalityBonus > 0 ? vitalityBonus : 1.0)));

		long expWithoutBonus = (long)  (noRateExp * rateXP * getRateExp());
		long spWithoutBonus = (long)  (noRateSp * rateSP * getRateSp());

		addExpAndSp(normalExp, normalSp, normalExp - expWithoutBonus, normalSp - spWithoutBonus, false, true);
	}

	@Override
	public void addExpAndSp(long exp, long sp)
	{
		addExpAndSp(exp, sp, 0, 0, false, false);
	}

	public void addExpAndSp(long addToExp, long addToSp, long bonusAddExp, long bonusAddSp, boolean applyRate, boolean applyToPet)
	{
		if(_activeClass == null)
			return;

		if(applyRate)
		{
			addToExp *= Config.RATE_XP * getRateExp();
			addToSp *= Config.RATE_SP * getRateSp();
		}

		Summon pet = getPet();
		if(addToExp > 0)
		{
			if(applyToPet)
			{
				if(pet != null && !pet.isDead() && !PetDataTable.isVitaminPet(pet.getNpcId()))
					// Sin Eater забирает всю экспу у персонажа
					if(pet.getNpcId() == PetDataTable.SIN_EATER_ID)
					{
						pet.addExpAndSp(addToExp, 0);
						addToExp = 0;
					}
					else if(pet.isPet() && pet.getExpPenalty() > 0f)
						if(pet.getLevel() > getLevel() - 20 && pet.getLevel() < getLevel() + 5)
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty()), 0);
							addToExp *= 1. - pet.getExpPenalty();
						}
						else
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty() / 5.), 0);
							addToExp *= 1. - pet.getExpPenalty() / 5.;
						}
					else if(pet.isSummon())
						addToExp *= 1. - pet.getExpPenalty();
			}

			// Remove Karma when the player kills L2MonsterInstance
			if(!isCursedWeaponEquipped() && addToSp > 0 && _karma > 0)
				_karma -= addToSp / (Config.KARMA_SP_DIVIDER * Config.RATE_SP);

			if(_karma < 0)
				_karma = 0;

			// Premium System
			addToExp *= PremiumAccountsTable.getExpBonus(this);
			addToSp  *= PremiumAccountsTable.getSpBonus(this);
			
			long max_xp = getVarB("NoExp") ? Experience.LEVEL[getLevel() + 1] - 1 : getMaxExp();
			addToExp = Math.min(addToExp, max_xp - getExp());
		}
				
		int oldLvl = _activeClass.getLevel();

		_activeClass.addExp(addToExp);
		_activeClass.addSp(addToSp);

		if(addToExp > 0 && addToSp > 0 && (bonusAddExp > 0 || bonusAddSp > 0))
			sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_ACQUIRED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4).addLong(addToExp).addLong(bonusAddExp).addInteger(addToSp).addInteger((int)bonusAddSp));
		else if(addToSp > 0 && addToExp == 0)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_SP).addNumber(addToSp));
		else if(addToSp > 0 && addToExp > 0)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP).addNumber(addToExp).addNumber(addToSp));
		else if(addToSp == 0 && addToExp > 0)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE).addNumber(addToExp));

		int level = _activeClass.getLevel();
		if(level != oldLvl)
		{
			int levels = level - oldLvl;
			if(levels > 0)
				getNevitSystem().addPoints(1950);
			levelSet(levels);
			getListeners().onLevelChange(oldLvl, level);
		}

		if(pet != null && pet.isPet() && PetDataTable.isVitaminPet(pet.getNpcId()))
		{
			PetInstance _pet = (PetInstance) pet;
			_pet.setLevel(getLevel());
			_pet.setExp(_pet.getExpForNextLevel());
			_pet.broadcastStatusUpdate();
		}
		
		if(getNevitSystem().isBlessingActive())
			addVitality(Config.ALT_VITALITY_NEVIT_POINT);

		updateStats();
		
		// Referral system
		if (_activeClass.isBase() && oldLvl < _activeClass.getLevel() && Config.ENABLE_REFERRAL_SYSTEM)
		{
			int levelPassed = 0;
			if (oldLvl < 20 && _activeClass.getLevel() >= 20)
				levelPassed = 20;
			else if (oldLvl < 40 && _activeClass.getLevel() >= 40)
				levelPassed = 40;
			else if (oldLvl < 60 && _activeClass.getLevel() >= 60)
				levelPassed = 60;
			else if (oldLvl < 80 && _activeClass.getLevel() >= 80)
				levelPassed = 80;
			else if (oldLvl < 85 && _activeClass.getLevel() >= 85)
				levelPassed = 85;
			
			switch (levelPassed)
			{
				case 20:
				case 40:
				case 60:
				case 80:
					CharacterEmails.newcomersReward(this, 250, levelPassed);
			}
			
			// Referral System
			if (_activeClass.getLevel() == 85)
				CharacterEmails.referralReward(this);
			
			// Revita-Pop reward
			if (_activeClass.getLevel() >= 40 && Rnd.get(3) == 0) // 33% for lv40 or above.
			{
				sendMessage("Here is small gift for you!");
				Functions.addItem(this, 20034, 1, true);
			}
		}
	}


	/**
	 * Give Expertise skill of this level.<BR><BR>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Level of the L2Player </li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the L2Player</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR><BR>
	 * @param send
	 */
	private void rewardSkills(boolean send)
	{
		boolean update = false;
		if(Config.AUTO_LEARN_SKILLS)
		{
			int unLearnable = 0;
			Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
			while(skills.size() > unLearnable)
			{
				unLearnable = 0;
				for(SkillLearn s : skills)
				{
					Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
					if(sk == null || !sk.getCanLearn(getClassId()) || (!Config.AUTO_LEARN_FORGOTTEN_SKILLS && s.isClicked()) || (!Config.AUTO_LEARN_DIVINE_INSPIRATION && sk.getId() == 1405))
					{
						unLearnable++;
						continue;
					}
					addSkill(sk, true);
					// Update shortcuts for skills on Level up.
					if(getAllShortCuts().size() > 0 && sk.getLevel() > 1)
						for(ShortCut sc : getAllShortCuts())
							if(sc.getId() == sk.getId() && sc.getType() == ShortCut.TYPE_SKILL)
							{
								ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sk.getLevel(), 1);
								sendPacket(new ShortCutRegister(this, newsc));
								registerShortCut(newsc);
							}
				}
				skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
			}
			update = true;
		}
		else
			// Скиллы дающиеся бесплатно не требуют изучения
			for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL))
				if(skill.getCost() == 0 && skill.getItemId() == 0)
				{
					Skill sk = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
					addSkill(sk, true);
					// Update shortcuts for skills on Level up.
					if(getAllShortCuts().size() > 0 && sk.getLevel() > 1)
						for(ShortCut sc : getAllShortCuts())
							if(sc.getId() == sk.getId() && sc.getType() == ShortCut.TYPE_SKILL)
							{
								ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sk.getLevel(), 1);
								sendPacket(new ShortCutRegister(this, newsc));
								registerShortCut(newsc);
							}
					update = true;
				}

		if(send && update)
			sendPacket(new SkillList(this));

		updateStats();
	}

	public Race getRace()
	{
		return getBaseTemplate().race;
	}

	public int getIntSp()
	{
		return (int) getSp();
	}

	public long getSp()
	{
		return _activeClass == null ? 0 : _activeClass.getSp();
	}

	public void setSp(long sp)
	{
		if(_activeClass != null)
			_activeClass.setSp(sp);
	}

	public int getClanId()
	{
		return _clan == null ? 0 : _clan.getClanId();
	}

	public long getLeaveClanTime()
	{
		return _leaveClanTime;
	}

	public void setLeaveClanTime(final long time)
	{
		_leaveClanTime = time;
	}
	
	public long getDeleteClanTime()
	{
		return _deleteClanTime;
	}

	public void setDeleteClanTime(final long time)
	{
		_deleteClanTime = time;
	}

	public void setOnlineTime(final long time)
	{
		_onlineTime = time;
	}

	public long getOnlineTime()
	{
		return (_onlineTime + getUptime()) / 1000L;
	}

	public void setUptime(final long time)
	{
		_uptime = time;
	}

	public long getUptime()
	{
		return _uptime == 0 ? 0 : System.currentTimeMillis() - _uptime;
	}

	public void setNoChannel(final long time)
	{
		_NoChannel = time;
		if(_NoChannel > 2145909600000L || _NoChannel < 0)
			_NoChannel = -1;

		if(_NoChannel > 0)
			_NoChannelBegin = System.currentTimeMillis();
		else
			_NoChannelBegin = 0;
	}

	public long getNoChannel()
	{
		return _NoChannel;
	}

	public long getNoChannelRemained()
	{
		if(_NoChannel == 0)
			return 0;
		else if(_NoChannel < 0)
			return -1;
		else
		{
			long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
			if(remained < 0)
				return 0;

			return remained;
		}
	}

	public boolean canTalkWith(Player player)
	{
		return _NoChannel >= 0 || player == this;
	}
	
	public void setLeaveClanCurTime()
	{
		_leaveClanTime = System.currentTimeMillis();
	}

	public void setDeleteClanCurTime()
	{
		_deleteClanTime = System.currentTimeMillis();
	}

	public boolean canJoinClan()
	{
		if(_leaveClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _leaveClanTime >= Clan.JOIN_PLEDGE_PENALTY)
		{
			_leaveClanTime = 0;
			return true;
		}
		return false;
	}

	public boolean canCreateClan()
	{
		if(_deleteClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _deleteClanTime >= Clan.DISBAND_PENALTY)
		{
			_deleteClanTime = 0;
			return true;
		}
		return false;
	}

	public IStaticPacket canJoinParty(Player inviter)
	{
		Request request = getRequest();
		if(request != null && request.isInProgress() && request.getOtherPlayer(this) != inviter)
			return SystemMsg.WAITING_FOR_ANOTHER_REPLY.packet(inviter); // занят
		// lets cheat for gm silence mode :)
		if (canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && getMessageRefusal())
			return SystemMsg.INVALID_TARGET.packet(inviter);
		if(isBlockAll() || getMessageRefusal()) // всех нафиг
			return SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE.packet(inviter);
		if(isInParty()) // уже
			return new SystemMessage2(SystemMsg.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addName(this);
		if(inviter.getReflection() != getReflection()) // в разных инстантах
			if(inviter.getReflection() != ReflectionManager.DEFAULT && getReflection() != ReflectionManager.DEFAULT)
				return SystemMsg.INVALID_TARGET.packet(inviter);
		if(isCursedWeaponEquipped() || inviter.isCursedWeaponEquipped()) // зарич
			return SystemMsg.INVALID_TARGET.packet(inviter);
		if(inviter.isInOlympiadMode() || isInOlympiadMode()) // олимпиада
			return SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS.packet(inviter);
		if(getTeam() != TeamType.NONE) // участник пвп эвента или дуэли
			return SystemMsg.INVALID_TARGET.packet(inviter);
		return null;
	}

	@Override
	public PcInventory getInventory()
	{
		return _inventory;
	}

	@Override
	public long getWearedMask()
	{
		return _inventory.getWearedMask();
	}

	public PcFreight getFreight()
	{
		return _freight;
	}

	public void removeItemFromShortCut(final int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}

	public void removeSkillFromShortCut(final int skillId)
	{
		_shortCuts.deleteShortCutBySkillId(skillId);
	}

	public boolean isSitting()
	{
		return _isSitting;
	}

	public void setSitting(boolean val)
	{
		_isSitting = val;
	}

	public boolean getSittingTask()
	{
		return sittingTaskLaunched;
	}

	@Override
	public void sitDown(StaticObjectInstance throne)
	{
		if(isSitting() || sittingTaskLaunched || isAlikeDead())
			return;

		if(isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isMoving())
		{
			getAI().setNextAction(nextAction.REST, null, null, false, false);
			return;
		}

		resetWaitSitTime();
		getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);

		if(throne == null)
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
		else
			broadcastPacket(new ChairSit(this, throne));

		_sittingObject = throne;
		setSitting(true);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndSitDownTask(this), 2500);
	}

	@Override
	public void standUp()
	{
		if(!isSitting() || sittingTaskLaunched || isInStoreMode() || isAlikeDead() || isSellBuff())
			return;
		
		if (NexusEvents.isInEvent(this) && getEventStatus().eventSitForced)
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
			return;
		}
		
		if(_relax)
		{
			setRelax(false);
			getEffectList().stopAllSkillEffects(EffectType.Relax);
		}

		getAI().clearNextAction();
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));

		_sittingObject = null;
		setSitting(false);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndStandUpTask(this), 2500);
	}

	public void setRelax(final boolean val)
	{
		_relax = val;
	}
	
	public void updateWaitSitTime()
	{
		if(_waitTimeWhenSit < 200)
			_waitTimeWhenSit += 2;
	}

	public int getWaitSitTime()
	{
		return _waitTimeWhenSit;
	}

	public void resetWaitSitTime()
	{
		_waitTimeWhenSit = 0;
	}

	public Warehouse getWarehouse()
	{
		return _warehouse;
	}

	public ItemContainer getRefund()
	{
		return _refund;
	}

	public long getAdena()
	{
		return getInventory().getAdena();
	}

	public boolean reduceAdena(long adena)
	{
		return reduceAdena(adena, false);
	}

	/**
	 * Забирает адену у игрока.<BR><BR>
	 *
	 * @param adena  - сколько адены забрать
	 * @param notify - отображать системное сообщение
	 * @return true если сняли
	 */
	public boolean reduceAdena(long adena, boolean notify)
	{
		if(adena < 0)
			return false;
		if(adena == 0)
			return true;
		boolean result = getInventory().reduceAdena(adena);
		if(notify && result)
			sendPacket(SystemMessage2.removeItems(ItemTemplate.ITEM_ID_ADENA, adena));
		return result;
	}

	public ItemInstance addAdena(long adena)
	{
		return addAdena(adena, false);
	}

	/**
	 * Добавляет адену игроку.<BR><BR>
	 *
	 * @param adena  - сколько адены дать
	 * @param notify - отображать системное сообщение
	 * @return L2ItemInstance - новое количество адены
	 */
	public ItemInstance addAdena(long adena, boolean notify)
	{
		if(adena < 1)
			return null;
		
		//if (Config.ENABLE_ACHIEVEMENTS)
		//	getCounters().setPoints("_most_adena_had", getAdena());
		
		ItemInstance item = getInventory().addAdena(adena);
		if(item != null && notify)
			sendPacket(SystemMessage2.obtainItems(ItemTemplate.ITEM_ID_ADENA, adena, 0));
		return item;
	}

	public GameClient getClient()
	{
		return _connection;
	}

	public int getRevision()
	{
		return _connection == null ? 0 : _connection.getRevision();
	}

	public void setClient(final GameClient connection)
	{
		_connection = connection;
	}

	public boolean isConnected()
	{
		return _connection != null && _connection.isConnected();
	}

	@Override
	public void onAction(final Player player, boolean shift)
	{
		if(isFrozen())
		{
			player.sendPacket(ActionFail.STATIC);
			return;
		}

		if(Events.onAction(player, this, shift))
		{
			player.sendPacket(ActionFail.STATIC);
			return;
		}
		
		// Check if the other player already target this L2Player
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
				player.sendPacket(new MyTargetSelected(getObjectId(), 0)); // The color to display in the select window is White
			else
				player.sendPacket(ActionFail.STATIC);
		}
		else if(getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			if(getDistance(player) > INTERACTION_DISTANCE && player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				else
					player.sendPacket(ActionFail.STATIC);
			}
			else
				player.doInteract(this);
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else if(player != this)
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
				else
					player.sendPacket(ActionFail.STATIC);
			}
			else
				player.sendPacket(ActionFail.STATIC);
		}
		else
			player.sendPacket(ActionFail.STATIC);
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate()) //По идее еше должно срезать траффик. Будут глюки с отображением - убрать это условие.
			return;

		StatusUpdate su = makeStatusUpdate(StatusUpdate.MAX_HP, StatusUpdate.MAX_MP, StatusUpdate.MAX_CP, StatusUpdate.CUR_HP, StatusUpdate.CUR_MP, StatusUpdate.CUR_CP);
		sendPacket(su);

		// Check if a party is in progress
		if(isInParty())
			// Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2Player of the Party
			getParty().sendPacket(this, new PartySmallWindowUpdate(this));

		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null)
			duelEvent.sendPacket(new ExDuelUpdateUserInfo(this), getTeam().revert().name());

		if(isInOlympiadMode() && isOlympiadCompStart())
		{
			if(_olympiadGame != null)
				_olympiadGame.broadcastInfo(this, null, false);
		}
	}

	private ScheduledFuture<?> _broadcastCharInfoTask;

	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	@Override
	public void broadcastCharInfo()
	{
		broadcastUserInfo(false);
	}

	/**
	 * Отправляет UserInfo даному игроку и CharInfo всем окружающим.<BR><BR>
	 * <p/>
	 * <B><U> Концепт</U> :</B><BR><BR>
	 * Сервер шлет игроку UserInfo.
	 * Сервер вызывает метод {@link Creature#broadcastPacketToOthers(l2r.gameserver.network.serverpackets.L2GameServerPacket...)} для рассылки CharInfo<BR><BR>
	 * <p/>
	 * <B><U> Действия</U> :</B><BR><BR>
	 * <li>Отсылка игроку UserInfo(личные и общие данные)</li>
	 * <li>Отсылка другим игрокам CharInfo(Public data only)</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Внимание</U> : НЕ ПОСЫЛАЙТЕ UserInfo другим игрокам либо CharInfo даному игроку.<BR>
	 * НЕ ВЫЗЫВАЕЙТЕ ЭТОТ МЕТОД КРОМЕ ОСОБЫХ ОБСТОЯТЕЛЬСТВ(смена сабкласса к примеру)!!! Траффик дико кушается у игроков и начинаются лаги.<br>
	 * Используйте метод {@link Player#sendChanges()}</B></FONT><BR><BR>
	 */
	public void broadcastUserInfo(boolean force)
	{
		sendUserInfo(force);

		if(!isVisible())
			return;

		if(Config.BROADCAST_CHAR_INFO_INTERVAL == 0)
			force = true;

		if(force)
		{
			if(_broadcastCharInfoTask != null)
			{
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			broadcastCharInfoImpl();
			return;
		}

		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	private int _polyNpcId;

	public void setPolyId(int polyid)
	{
		_polyNpcId = polyid;

		teleToLocation(getLoc());
		broadcastUserInfo(true);
	}

	public boolean isPolymorphed()
	{
		return _polyNpcId != 0;
	}

	public int getPolyId()
	{
		return _polyNpcId;
	}

	private void broadcastCharInfoImpl()
	{
		if(!isVisible())
			return;

		L2GameServerPacket ci = isPolymorphed() ? new NpcInfoPoly(this).setInvisible(isInvisible()) : new CharInfo(this).setInvisible(isInvisible());
		L2GameServerPacket exCi = new ExBR_ExtraUserInfo(this).setInvisible(isInvisible());
		
		L2GameServerPacket dominion = null;
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null && (siegeEvent.isInProgress() || siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(getObjectId())))
			dominion = new ExDominionWarStart(this).setInvisible(isInvisible());
		
		for(Player player : World.getAroundPlayers(this))
		{
			player.sendPacket(ci, exCi);
			player.sendPacket(RelationChanged.update(player, this, player));
			if(dominion != null)
				player.sendPacket(dominion);
		}
	}

	public void broadcastRelationChanged()
	{
		if(!isVisible())
			return;

		for(Player player : World.getAroundPlayers(this))
			player.sendPacket(RelationChanged.update(player, this, player).setInvisible(isInvisible()));
	}

	public void sendEtcStatusUpdate()
	{
		if(!isVisible())
			return;

		sendPacket(new EtcStatusUpdate(this));
	}

	private Future<?> _userInfoTask;

	private class UserInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sendUserInfoImpl();
			_userInfoTask = null;
		}
	}

	private void sendUserInfoImpl()
	{
		sendPacket(new UserInfo(this), new ExBR_ExtraUserInfo(this));
		DominionSiegeEvent dominionSiegeEvent = getEvent(DominionSiegeEvent.class);
		if(dominionSiegeEvent != null && (dominionSiegeEvent.isInProgress() || dominionSiegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(getObjectId())))
			sendPacket(new ExDominionWarStart(this));
	}

	public void sendUserInfo()
	{
		sendUserInfo(false);
	}

	public void sendUserInfo(boolean force)
	{
		if(!isVisible() || entering || isLogoutStarted())
			return;

		if(Config.USER_INFO_INTERVAL == 0 || force)
		{
			if(_userInfoTask != null)
			{
				_userInfoTask.cancel(false);
				_userInfoTask = null;
			}
			sendUserInfoImpl();
			return;
		}

		if(_userInfoTask != null)
			return;

		_userInfoTask = ThreadPoolManager.getInstance().schedule(new UserInfoTask(), Config.USER_INFO_INTERVAL);
	}

	@Override
	public StatusUpdate makeStatusUpdate(int... fields)
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		for(int field : fields)
			switch(field)
			{
				case StatusUpdate.CUR_HP:
					su.addAttribute(field, (int) getCurrentHp());
					break;
				case StatusUpdate.MAX_HP:
					su.addAttribute(field, getMaxHp());
					break;
				case StatusUpdate.CUR_MP:
					su.addAttribute(field, (int) getCurrentMp());
					break;
				case StatusUpdate.MAX_MP:
					su.addAttribute(field, getMaxMp());
					break;
				case StatusUpdate.CUR_LOAD:
					su.addAttribute(field, getCurrentLoad());
					break;
				case StatusUpdate.MAX_LOAD:
					su.addAttribute(field, getMaxLoad());
					break;
				case StatusUpdate.PVP_FLAG:
					su.addAttribute(field, _pvpFlag);
					break;
				case StatusUpdate.KARMA:
					su.addAttribute(field, getKarma());
					break;
				case StatusUpdate.CUR_CP:
					su.addAttribute(field, (int) getCurrentCp());
					break;
				case StatusUpdate.MAX_CP:
					su.addAttribute(field, getMaxCp());
					break;
			}
		return su;
	}

	public void sendStatusUpdate(boolean broadCast, boolean withPet, int... fields)
	{
		if(fields.length == 0 || entering && !broadCast)
			return;

		StatusUpdate su = makeStatusUpdate(fields);
		if(!su.hasAttributes())
			return;

		List<L2GameServerPacket> packets = new ArrayList<L2GameServerPacket>(withPet ? 2 : 1);
		if(withPet && getPet() != null)
			packets.add(getPet().makeStatusUpdate(fields));

		packets.add(su);

		if(!broadCast)
			sendPacket(packets);
		else if(entering)
			broadcastPacketToOthers(packets);
		else
			broadcastPacket(packets);
	}

	/**
	 * @return the Alliance Identifier of the L2Player.<BR><BR>
	 */
	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}

	@Override
	public void sendPacket(IStaticPacket p)
	{	
		if(p == null || !isConnected() && (getAI() == null || !getAI().isPhantomPlayerAI()))
			return;

		if(isPacketIgnored(p.packet(this)))
			return;

		if (_connection != null)
			_connection.sendPacket(p.packet(this));
	}

	@Override
	public void sendPacket(IStaticPacket... packets)
	{
		if(packets == null || !isConnected() && (getAI() == null || !getAI().isPhantomPlayerAI()))
			return;

		for(IStaticPacket p : packets)
		{
			if(isPacketIgnored(p))
				continue;

			if (_connection != null)
				_connection.sendPacket(p.packet(this));
		}
	}

	private boolean isPacketIgnored(IStaticPacket p)
	{
		if(p == null)
			return true;
		
		if(_notShowBuffAnim && (p.getClass() == MagicSkillUse.class || p.getClass() == MagicSkillLaunched.class))
			return true;
		
		if (!getListeners().onPacketReceived(p))
			return true;

		//if(_notShowTraders && (p.getClass() == PrivateStoreMsgBuy.class || p.getClass() == PrivateStoreMsgSell.class || p.getClass() == RecipeShopMsg.class))
		//		return true;

		return false;
	}

	@Override
	public void sendPacket(List<? extends IStaticPacket> packets)
	{
		if(packets == null || !isConnected() && (getAI() == null || !getAI().isPhantomPlayerAI()))
			return;

		for(IStaticPacket p : packets)
		{
			if(isPacketIgnored(p))
				continue;
			
			if (_connection != null)
				_connection.sendPacket(p.packet(this));
		}
	}

	public void doInteract(GameObject target)
	{
		if(target == null || isActionsDisabled())
		{
			sendActionFailed();
			return;
		}
		if(target.isPlayer())
		{
			if(target.getDistance(this) <= INTERACTION_DISTANCE)
			{
				Player temp = (Player) target;

				if(temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
				{
					sendPacket(new PrivateStoreListSell(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				{
					sendPacket(new PrivateStoreListBuy(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				{
					sendPacket(new RecipeShopSellList(this, temp));
					sendActionFailed();
				}
				sendActionFailed();
			}
			else if(getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
		}
		else
			target.onAction(this, false);
	}

	public void doAutoLootOrDrop(ItemInstance item, NpcInstance fromNpc)
	{
		boolean forceAutoloot = fromNpc.isFlying() || getReflection().isAutolootForced();

		// temp hack for lindvior boss
		if (fromNpc.getNpcId() == 70013)
		{
			item.dropToTheGround(this, fromNpc, false);
			return;
		}
		
		if((fromNpc.isRaid() || fromNpc instanceof ReflectionBossInstance) && !Config.AUTO_LOOT_FROM_RAIDS && !item.isHerb() && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc, true);
			return;
		}

		if(!item.isAdena())
		{
			// Herbs
			if(item.isHerb())
			{
				if(fromNpc.isChampion() && !Config.ALT_CHAMPION_DROP_HERBS)
				{
					item.deleteMe();
					return;
				}
					
				if(!_autoLootHerbs && !forceAutoloot)
				{
					item.dropToTheGround(this, fromNpc, true);
					return;
				}
				
				// Auto loot the herb
				Skill[] skills = item.getTemplate().getAttachedSkills();
				if(skills.length > 0)
				{
					for(Skill skill : skills)
					{
						altUseSkill(skill, this);
						if(getPet() != null && getPet().isSummon() && !getPet().isDead())
							getPet().altUseSkill(skill, getPet());
					}
				}
				
				item.deleteMe();
				return;
			}
			
			//Champion mob drop
			if(fromNpc.isChampion() && Config.CHAMPION_DROP_ONLY_ADENA)
			{
				item.deleteMe();
				return;
			}
			
			// Certainly not herbs. Regular items
			if(!_autoLoot && !forceAutoloot)
			{
				item.dropToTheGround(this, fromNpc, true);
				return;
			}
		}
		else // Adena
		{
			if(!_autoLoot && !_autoLootOnlyAdena && !forceAutoloot)
			{
				item.dropToTheGround(this, fromNpc, true);
				return;
			}
		}
		
		if (Config.AUTO_LOOT_PA)
		{
			if (!(_bonusExpiration !=null))
			{
				item.dropToTheGround(this, fromNpc, true);
				sendMessage(isLangRus() ? "Вам нужно купить премиум аккаунт." : "You need to buy Premium Account.");
				return;
			}
		}
		
		// Check if the L2Player is in a Party
		if(!isInParty())
		{
			if(!pickupItem(item, Log.Pickup))
			{
				item.dropToTheGround(this, fromNpc, true);
				return;
			}
		}
		else
			getParty().distributeItem(this, item, fromNpc);

		broadcastPickUpMsg(item);
	}

	@Override
	public void doPickupItem(final GameObject object)
	{
		// Check if the L2Object to pick up is a L2ItemInstance
		if(!object.isItem())
		{
			_log.warn("trying to pickup wrong target." + getTarget());
			return;
		}

		sendActionFailed();
		stopMove();

		ItemInstance item = (ItemInstance) object;

		synchronized(item)
		{
			if(!item.isVisible())
				return;

			// Check if me not owner of item and, if in party, not in owner party and nonowner pick up delay still active
			if(!ItemFunctions.checkIfCanPickup(this, item))
			{
				SystemMessage sm;
				if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
				return;
			}

			// Herbs
			if(item.isHerb())
			{
				Skill[] skills = item.getTemplate().getAttachedSkills();
				if(skills.length > 0)
					for(Skill skill : skills)
					{
						altUseSkill(skill, this);
						if(getPet() != null && getPet().isSummon() && !getPet().isDead())
							getPet().altUseSkill(skill, getPet());
					}

				broadcastPacket(new GetItem(item, getObjectId()));
				item.deleteMe();
				return;
			}

			FlagItemAttachment attachment = item.getAttachment() instanceof FlagItemAttachment ? (FlagItemAttachment) item.getAttachment() : null;

			if(!isInParty() || attachment != null)
			{
				if(pickupItem(item, Log.Pickup))
				{
					broadcastPacket(new GetItem(item, getObjectId()));
					broadcastPickUpMsg(item);
					item.pickupMe();
				}
			}
			else
				getParty().distributeItem(this, item, null);
		}
	}

	public boolean pickupItem(ItemInstance item, String log)
	{
		PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;

		if(!ItemFunctions.canAddItem(this, item))
			return false;

		if(!Config.DISABLE_TUTORIAL && item.getItemId() == ItemTemplate.ITEM_ID_ADENA || item.getItemId() == 6353)
		{
			Quest q = QuestManager.getQuest(255);
			if(q != null)
				processQuestEvent(q.getName(), "CE" + item.getItemId(), null);
		}

		// TODO: Infern0 new log system
		//Log.LogEvent(getName(), getIP(), "PickUpItem", new String[] { "player pickup item " + log + ": " + getName() + " item: " + item.getCount() + " of " + item.getItemId() + "" });
		
		Log.LogItem(this, log, item);
		sendPacket(SystemMessage2.obtainItems(item));
		getInventory().addItem(item);

		if(attachment != null)
			attachment.pickUp(this);

		sendChanges();
		return true;
	}

	public void setObjectTarget(GameObject target)
	{
		setTarget(target);
		if(target == null)
			return;

		if(target == getTarget())
		{
			if(target.isNpc())
			{
				NpcInstance npc = (NpcInstance) target;
				sendPacket(new MyTargetSelected(npc.getObjectId(), getLevel() - npc.getLevel()));
				sendPacket(npc.makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
				sendPacket(new ValidateLocation(npc), ActionFail.STATIC);
			}
			else
				sendPacket(new MyTargetSelected(target.getObjectId(), 0));
		}
	}

	@Override
	public void setTarget(GameObject newTarget)
	{
		// Check if the new target is visible
		if(newTarget != null && !newTarget.isVisible())
			newTarget = null;

		// Can't target and attack festival monsters if not participant
		if(newTarget instanceof FestivalMonsterInstance && !isFestivalParticipant())
			newTarget = null;

		Party party = getParty();

		// Can't target and attack rift invaders if not in the same room
		if(party != null && party.isInDimensionalRift())
		{
			int riftType = party.getDimensionalRift().getType();
			int riftRoom = party.getDimensionalRift().getCurrentRoom();
			if(newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
				newTarget = null;
		}

		GameObject oldTarget = getTarget();

		if(oldTarget != null)
		{
			if(oldTarget.equals(newTarget))
				return;

			// Remove the L2Player from the _statusListener of the old target if it was a L2Character
			if(oldTarget.isCreature())
				((Creature) oldTarget).removeStatusListener(this);

			broadcastPacket(new TargetUnselected(this));
		}

		if(newTarget != null)
		{
			// Add the L2Player to the _statusListener of the new target if it's a L2Character
			if(newTarget.isCreature())
				((Creature) newTarget).addStatusListener(this);

			broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getLoc()));
		}

		super.setTarget(newTarget);
		
		if (Config.ENABLE_OFFLINE_BUFFERS)
		{
			if(newTarget != null)
			{
				if (newTarget.isPlayer() && ((Player) newTarget).isSellBuff())
				{
					Player seller = (Player) newTarget;
					OfflineBufferManager.getInstance().processBypass(this, "showBuyerMenu " + seller.getObjectId());
				}
			}
			
		}
	}

	/**
	 * @return the active weapon instance (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * @return the active weapon item (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		final ItemInstance weapon = getActiveWeaponInstance();

		if(weapon == null)
			return getFistsWeaponItem();

		return (WeaponTemplate) weapon.getTemplate();
	}

	/**
	 * @return the secondary weapon instance (always equipped in the left hand).<BR><BR>
	 */
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * @return the secondary weapon item (always equipped in the left hand) or the fists weapon.<BR><BR>
	 */
	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		final ItemInstance weapon = getSecondaryWeaponInstance();

		if(weapon == null)
			return getFistsWeaponItem();

		final ItemTemplate item = weapon.getTemplate();

		if(item instanceof WeaponTemplate)
			return (WeaponTemplate) item;

		return null;
	}

	public boolean isWearingArmor(final ArmorType armorType)
	{
		final ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);

		if(chest == null)
			return armorType == ArmorType.NONE;

		if(chest.getItemType() != armorType)
			return false;

		if(chest.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
			return true;

		final ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);

		return legs == null ? armorType == ArmorType.NONE : legs.getItemType() == armorType;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || (attacker.isDead() && !isDot))
			return;

		// 5182 = Blessing of protection, работает если разница уровней больше 10 и не в зоне осады
		if(attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ПК не может нанести урон чару с блессингом
			if(attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.SIEGE))
				return;
			// чар с блессингом не может нанести урон ПК
			if(getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.SIEGE))
				return;
		}
		
		if (Config.ALT_DAMAGE_INVIS && !GeoEngine.canSeeTarget(this, attacker, false))
			damage = 0;

		// Reduce the current HP of the L2Player
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	private Creature _lastAttacker = null;
	
	private long _lastAttackDate = 0;
	
	public Creature getLastAttacker()
	{
		return _lastAttacker;
	}
	
	public long getLastAttackDate()
	{
		return _lastAttackDate;
	}
	
	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(standUp)
		{
			standUp();
			if(isFakeDeath() && attacker.isPlayable()) // FIXME: Its not totally retail, but still makes fake death usable, other than using it and in 0.5sec getting gone (thats not retail like)
				breakFakeDeath();
		}

		_lastAttacker = attacker;
		_lastAttackDate = System.currentTimeMillis();
		
		if(attacker.isPlayable())
		{
			if(!directHp && getCurrentCp() > 0)
			{
				final double cp = getCurrentCp();
				if(isInOlympiadMode())
					addDamageOnOlympiad(attacker, skill, damage, cp);

				if(cp >= damage)
				{
					setCurrentCp(cp - damage);
					damage = 0;
				}
				else
				{
					damage -= cp;
					setCurrentCp(0);
				}
			}
		}

		double hp = directHp ? getCurrentHp() : getCurrentHp() + getCurrentCp(); 

		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null)
		{
			if(damage >= hp)
			{
				damage = 0;
				setCurrentHp(1, false);
				duelEvent.onDie(this);
				return;
			}
		}

		if(getPvPTeam() != 0 && _event != null && _event.getState() == 2)
		{
			if(hp <= damage)
			{
				damage = 0;
				setCurrentHp(1, true);
				_event.doDie(attacker, this);
				return;
			}
		}

		if(isInOlympiadMode())
		{
			addDamageOnOlympiad(attacker, skill, damage, hp);
			
			if(hp <= damage)
			{
				damage = 0;
				if(_olympiadGame.getType() != CompType.TEAM)
				{
					setCurrentHp(1, true);
					_olympiadGame.setWinner(getOlympiadSide() == 1 ? 2 : 1);
					_olympiadGame.endGame(20000, false, false);
					attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					attacker.sendActionFailed();
					
					if (attacker != null)
					{
						if (attacker.isPlayer())
							attacker.getPlayer().setPendingOlyEnd(true);
						
						for (Effect e : attacker.getEffectList().getAllEffects())
							if (e.getEffectType() != EffectType.Cubic && !e.getSkill().isToggle())
								e.exit();
						
					}
					
					if (getPlayer() != null)
					{
						setPendingOlyEnd(true);
						for (Effect e : attacker.getEffectList().getAllEffects())
							if (e.getEffectType() != EffectType.Cubic && !e.getSkill().isToggle())
								e.exit();
						
						if (isDead())
							broadcastPacket(new Revive(this));
					}
					
					return;
				}
				else if(_olympiadGame.doDie(this)) // Все умерли
				{
					_olympiadGame.setWinner(getOlympiadSide() == 1 ? 2 : 1);
					_olympiadGame.endGame(20000, false, false);
				}
			}
		}

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	public void addDamageOnOlympiad(Creature attacker, Skill skill, double damage, double hpcp)
	{
		OlympiadGame game = getOlympiadGame();
		if (game == null)
			return;
		
		if(this != attacker && (skill == null || skill.isOffensive())) // считаем дамаг от простых ударов и атакующих скиллов
			_olympiadGame.addDamage(this, Math.min(hpcp, damage));
	}

	private void altDeathPenalty(final Creature killer)
	{
		if (canOverrideCond(PcCondOverride.DEATH_PENALTY))
			return;
		
		// Reduce the Experience of the L2Player in function of the calculated Death Penalty
		if(!Config.ALT_GAME_DELEVEL)
			return;
		if(isInZoneBattle())
			return;
		if(getNevitSystem().isBlessingActive())
			return;
		
		deathPenalty(killer);
	}

	public final boolean atWarWith(final Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId());
	}

	public boolean atMutualWarWith(Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId()) && player.getClan().isAtWarWith(_clan.getClanId());
	}

	public final void doPurePk(final Player killer)
	{
		
		// Antifeed protection
		if (killer.checkIfKillIsFeed(this))
			return;
				
		// Check if the attacker has a PK counter greater than 0
		final int pkCountMulti = Math.max(killer.getPkKills() / 2, 1);

		// Calculate the level difference Multiplier between attacker and killed L2Player
		//final int lvlDiffMulti = Math.max(killer.getLevel() / _level, 1);

		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		// Add karma to attacker and increase its PK counter
		killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti); // * lvlDiffMulti);
		killer.setPkKills(killer.getPkKills() + 1);
		
		if (Config.ENABLE_PLAYER_COUNTERS)
			killer.getCounters().addPoint("_PK");;
	}

	public final void doKillInPeace(final Player killer) // Check if the L2Player killed haven't Karma
	{
		if(_karma <= 0  && (_event == null || _event.checkPvP(killer, this)))
		{
			if(Config.SERVICES_PK_PVP_KILL_ENABLE)
			{
				if(Config.SERVICES_PK_PVP_TIE_IF_SAME_IP)
				{
					if (getClient().getAccountData().accessLevel >= 1 || getIP() != killer.getIP())
						ItemFunctions.addItem(killer, Config.SERVICES_PK_KILL_REWARD_ITEM, Config.SERVICES_PK_KILL_REWARD_COUNT, true);
				}
				else
					ItemFunctions.addItem(killer, Config.SERVICES_PK_KILL_REWARD_ITEM, Config.SERVICES_PK_KILL_REWARD_COUNT, true);
			}
			doPurePk(killer);
		}
		else
		{
			if(Config.SERVICES_PK_PVP_KILL_ENABLE)
			{
				if(Config.SERVICES_PK_PVP_TIE_IF_SAME_IP)
				{
					if (getClient().getAccountData().accessLevel >= 1 || getIP() != killer.getIP())
						ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, Config.SERVICES_PVP_KILL_REWARD_COUNT, true);
				}
				else
					ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, Config.SERVICES_PVP_KILL_REWARD_COUNT, true);
			}
			
			// Anti feed protection
			if (killer.checkIfKillIsFeed(this))
				killer.setPvpKills(killer.getPvpKills() + 1);
		}
	}

	public void checkAddItemToDrop(List<ItemInstance> array, List<ItemInstance> items, int maxCount)
	{
		for(int i = 0; i < maxCount && !items.isEmpty(); i++)
			array.add(items.remove(Rnd.get(items.size())));
	}

	public FlagItemAttachment getActiveWeaponFlagAttachment()
	{
		ItemInstance item = getActiveWeaponInstance();
		if(item == null || !(item.getAttachment() instanceof FlagItemAttachment))
			return null;
		return (FlagItemAttachment) item.getAttachment();
	}

	protected void doPKPVPManage(Creature killer)
	{
		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
			attachment.onDeath(this, killer);

		if(killer == null || killer == _summon || killer == this)
			return;

		if((isInZoneBattle() || killer.isInZoneBattle()) && !Config.ZONE_PVP_COUNT)
			return;

		if(killer instanceof Summon && (killer = killer.getPlayer()) == null)
			return;

		// Processing Karma/PKCount/PvPCount for killer
		if(killer.isPlayer())
		{
			final Player pk = (Player) killer;
			final int repValue = getLevel() - pk.getLevel() >= 20 ? 2 : 1;
			boolean war = atMutualWarWith(pk);
			
			// Support for pk clan member and reduce clan points.
			if(war)
			{
				if(pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
				{
					_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
					pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
				}
			}

			// Manage clan reputation deduction.
			if (_clan != null)
			{
				CastleSiegeEvent siegeEvent = getEvent(CastleSiegeEvent.class);
				CastleSiegeEvent siegeEventPk = pk.getEvent(CastleSiegeEvent.class);
				if(siegeEvent != null && siegeEvent != null && siegeEventPk != null && siegeEvent == siegeEventPk && (((siegeEventPk.getSiegeClan(SiegeEvent.DEFENDERS, pk.getClan())) != (siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, getClan()))) || ((siegeEventPk.getSiegeClan(SiegeEvent.ATTACKERS, pk.getClan())) != (siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, getClan())))))
				{
					pk.getClan().incSiegeKills();
					
					if(pk.getClan() != null && pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
					{
						_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
						pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
					}
				}
				
				DominionSiegeEvent dominionEvent = getEvent(DominionSiegeEvent.class);
				DominionSiegeEvent dominionEventPk = pk.getEvent(DominionSiegeEvent.class);
				if (dominionEvent != null && dominionEventPk != null)
					pk.getClan().incSiegeKills();
				
				FortressSiegeEvent fsiegeEvent = getEvent(FortressSiegeEvent.class);
				FortressSiegeEvent fsiegeEventPk = pk.getEvent(FortressSiegeEvent.class);
				if(fsiegeEvent != null && fsiegeEvent != null && fsiegeEventPk != null && fsiegeEvent == fsiegeEventPk && (((fsiegeEventPk.getSiegeClan(SiegeEvent.DEFENDERS, pk.getClan())) != (fsiegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, getClan()))) || ((fsiegeEventPk.getSiegeClan(SiegeEvent.ATTACKERS, pk.getClan())) != (fsiegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, getClan())))))
				{
					if(pk.getClan() != null && pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
					{
						_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
						pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
					}
				}
				ClanHallSiegeEvent chsiegeEvent = getEvent(ClanHallSiegeEvent.class);
				ClanHallSiegeEvent chsiegeEventPk = pk.getEvent(ClanHallSiegeEvent.class);
				if(chsiegeEvent != null && chsiegeEvent != null && chsiegeEventPk != null && chsiegeEvent == chsiegeEventPk && (((chsiegeEventPk.getSiegeClan(SiegeEvent.DEFENDERS, pk.getClan())) != (chsiegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, getClan()))) || ((chsiegeEventPk.getSiegeClan(SiegeEvent.ATTACKERS, pk.getClan())) != (chsiegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, getClan())))))
				{
					if(pk.getClan() != null && pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
					{
						_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
						pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
					}
				}
			}

			if(isOnSiegeField() && !Config.SIEGE_PVP_COUNT)
				return;
			
			if (NexusEvents.isInEvent(this) && NexusEvents.canAttack(this, pk) && !NexusEvents.gainPvpPointsOnEvents())
				return;
			
			if (_pvpFlag > 0 || war || Config.SIEGE_PVP_COUNT || Config.ZONE_PVP_COUNT || (NexusEvents.isInEvent(this) && NexusEvents.canAttack(this, pk) && NexusEvents.gainPvpPointsOnEvents()))
				pk.setPvpKills(pk.getPvpKills() + 1);
			else
				doKillInPeace(pk);

			if (!Config.PVP_KILLS_COLOR.isEmpty())
				checkForPvPColor(pk.getPvpKills());
			
			if (Config.ENABLE_PLAYER_COUNTERS && !pk.checkIfKillIsFeed(this))
				pk.getCounters().addPoint("_PvP");
			
			pk.sendChanges();
		}

		int karma = _karma;
		decreaseKarma(Config.KARMA_LOST_BASE);

		// в нормальных условиях вещи теряются только при смерти от гварда или игрока
		// кроме того, альт на потерю вещей при сметри позволяет терять вещи при смтери от монстра
		boolean isPvP = killer.isPlayable() || killer instanceof GuardInstance;

		if(killer.isMonster() && !Config.DROP_ITEMS_ON_DIE // если убил монстр и альт выключен
				|| isPvP // если убил игрок или гвард и
				&& (_pkKills < Config.MIN_PK_TO_ITEMS_DROP // количество пк слишком мало
				|| karma == 0 && Config.KARMA_NEEDED_TO_DROP) // кармы нет
				|| isFestivalParticipant() // в фестивале вещи не теряются
				|| !killer.isMonster() && !isPvP) // в прочих случаях тоже
			return;
		
		if (NexusEvents.isInEvent(this))
			return;

		// No drop from GM's
		if(!Config.KARMA_DROP_GM && isGM())
			return;

		final int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;

		double dropRate; // базовый шанс в процентах
		if(isPvP)
			dropRate = _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE;
		else
			dropRate = Config.NORMAL_DROPCHANCE_BASE;

		// Premium System
		dropRate *= PremiumAccountsTable.getKarmaDrop(this);
		
		int dropEquipCount = 0, dropWeaponCount = 0, dropItemCount = 0;

		for(int i = 0; i < Math.ceil(dropRate / 100) && i < max_drop_count; i++)
			if(Rnd.chance(dropRate))
			{
				int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
				if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT)
					dropItemCount++;
				else if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON)
					dropEquipCount++;
				else
					dropWeaponCount++;
			}

		List<ItemInstance> drop = new ArrayList<ItemInstance>(), // общий массив с результатами выбора
				dropItem = new ArrayList<ItemInstance>(), dropEquip = new ArrayList<ItemInstance>(), dropWeapon = new ArrayList<ItemInstance>(); // временные

		getInventory().writeLock();
		try
		{
			for(ItemInstance item : getInventory().getItems())
			{
				if(!item.canBeDropped(this, true) || Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
					continue;

				if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
					dropWeapon.add(item);
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
					dropEquip.add(item);
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_OTHER)
					dropItem.add(item);
			}

			checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
			checkAddItemToDrop(drop, dropEquip, dropEquipCount);
			checkAddItemToDrop(drop, dropItem, dropItemCount);

			// Dropping items, if present
			if(drop.isEmpty())
				return;

			for(ItemInstance item : drop)
			{
				if(item.isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
					item.setAugmentationId(0);

				item = getInventory().removeItem(item);
				Log.LogItem(this, Log.PvPDrop, item);

				if(item.getEnchantLevel() > 0)
					sendPacket(new SystemMessage(SystemMessage.DROPPED__S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				else
					sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));

				if(killer.isPlayable() && ((Config.AUTO_LOOT && Config.AUTO_LOOT_PK) || isInFlyingTransform()))
				{
					killer.getPlayer().getInventory().addItem(item);
					Log.LogItem(this, Log.Pickup, item);

					killer.getPlayer().sendPacket(SystemMessage2.obtainItems(item));
				}
				else
					item.dropToTheGround(this, Location.findAroundPosition(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT));
			}
		}
		finally
		{
			getInventory().writeUnlock();
		}
	}

	@Override
	protected void onDeath(Creature killer)
	{
		Player player = getPlayer();
		
		// Check for active charm of luck for death penalty
		if (player != null && !player.isPhantom())
			getDeathPenalty().checkCharmOfLuck();

		if(isInStoreMode())
			setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		if(isProcessingRequest())
		{
			Request request = getRequest();
			if(isInTrade())
			{
				Player parthner = request.getOtherPlayer(this);
				sendPacket(SendTradeDone.FAIL);
				parthner.sendPacket(SendTradeDone.FAIL);
			}
			request.cancel();
		}

		if(_cubics != null)
			getEffectList().stopAllSkillEffects(EffectType.Cubic);

		setAgathion(0);

		boolean checkPvp = true;
		if(Config.ALLOW_CURSED_WEAPONS)
		{
			if(isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().dropPlayer(this);
				checkPvp = false;
			}
			else if(killer != null && killer.isPlayer() && killer.isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().increaseKills(((Player) killer).getCursedWeaponEquippedId());
				checkPvp = false;
			}
		}
		
		if (NexusEvents.isInEvent(this))
			NexusEvents.onDie(this, killer);
		
		if(checkPvp)
		{
			doPKPVPManage(killer);
			altDeathPenalty(killer);
		}

		if (Config.ENABLE_PLAYER_COUNTERS)
			getCounters().addPoint("_Times_Died");
		
		// playerkill - for the anti-feed system
		if (Config.ENABLE_PVP_PK_LOG && getPlayer() != null && getPlayer().getHWID() != null && killer != null && killer.isPlayer() && killer.getPlayer().getHWID() != null)
		{
			Player pk = (Player) killer;
			boolean isPvP = true;
			
			if (killer.getKarma() > 0)
				isPvP = false;
			
			long lastKillTime = _killedHWIDs.get(getPlayer().getHWID()) == null ? 0 : _killedHWIDs.get(getPlayer().getHWID());
			pk._killedHWIDs.put(getPlayer().getHWID(), System.currentTimeMillis());
			KillLog kl = PlayerKillsLogManager.getInstance().new KillLog(pk, getPlayer(), System.currentTimeMillis(), lastKillTime, pk.checkIfKillIsFeed(this), NexusEvents.isInEvent(this), isPvP);
			PlayerKillsLogManager.getInstance().addLog(getObjectId(), kl);
		}
				
		if (!player.isPhantom())
			getDeathPenalty().notifyDead(killer);

		if (_event != null)
			_event.doDie(killer, this);

		setIncreasedForce(0);

		if(isInParty() && getParty().isInReflection() && getParty().getReflection() instanceof DimensionalRift)
			((DimensionalRift) getParty().getReflection()).memberDead(this);

		stopWaterTask();

		if(!isSalvation() && isOnSiegeField() && isCharmOfCourage())
		{
			ask(new ConfirmDlg(SystemMsg.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU, 60000), new ReviveAnswerListener(this, 100, false, 60000));
			setCharmOfCourage(false);
		}

		if(!Config.DISABLE_TUTORIAL && getLevel() < 6)
		{
			Quest q = QuestManager.getQuest(255);
			if(q != null)
				processQuestEvent(q.getName(), "CE30", null);
		}
		
		super.onDeath(killer);
	}

	public void restoreExp()
	{
		restoreExp(100.);
	}

	public void restoreExp(double percent)
	{
		if(percent == 0)
			return;

		int lostexp = 0;

		String lostexps = getVar("lostexp");
		if(lostexps != null)
		{
			lostexp = Integer.parseInt(lostexps);
			unsetVar("lostexp");
		}

		if(lostexp != 0)
			addExpAndSp((long) (lostexp * percent / 100), 0);
	}

	public void deathPenalty(Creature killer)
	{
		if (canOverrideCond(PcCondOverride.DEATH_PENALTY))
			return;
		
		if(killer == null)
			return;
		
		if (isPhantom())
			return;
		
		final boolean atwar = killer.getPlayer() != null && atWarWith(killer.getPlayer());

		double deathPenaltyBonus = getDeathPenalty().getLevel() * Config.ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
		if(deathPenaltyBonus < 2)
			deathPenaltyBonus = 1;
		else
			deathPenaltyBonus = deathPenaltyBonus / 2;

		// The death steal you some Exp: 10-40 lvl 8% loose
		double percentLost = 8.0;

		int level = getLevel();
		if(level >= 79)
			percentLost = 1.0;
		else if(level >= 78)
			percentLost = 1.5;
		else if(level >= 76)
			percentLost = 2.0;
		else if(level >= 40)
			percentLost = 4.0;

		if(Config.ALT_DEATH_PENALTY)
			percentLost = percentLost * Config.RATE_XP + _pkKills * Config.ALT_PK_DEATH_RATE;

		if(isFestivalParticipant() || atwar)
			percentLost = percentLost / 4.0;

		// Calculate the Experience loss
		int lostexp = (int) Math.round((Experience.LEVEL[level + 1] - Experience.LEVEL[level]) * percentLost / 100);
		lostexp *= deathPenaltyBonus;

		lostexp = (int) calcStat(Stats.EXP_LOST, lostexp, killer, null);

		// На зарегистрированной осаде нет потери опыта, на чужой осаде - как при обычной смерти от *моба*
		if(isOnSiegeField())
		{
			SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
			if(siegeEvent != null)
				lostexp = 0;

			if(siegeEvent != null)
			{
				List<Effect> effect = getEffectList().getEffectsBySkillId(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
				if(effect != null)
				{
					int syndromeLvl = effect.get(0).getSkill().getLevel();
					if(syndromeLvl < 5)
					{
						getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
						Skill skill = SkillTable.getInstance().getInfo(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, syndromeLvl + 1);
						skill.getEffects(this, this, false, false);
					}
					else if(syndromeLvl == 5)
					{
						getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
						Skill skill = SkillTable.getInstance().getInfo(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 5);
						skill.getEffects(this, this, false, false);
					}
				}
				else
				{
					Skill skill = SkillTable.getInstance().getInfo(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 1);
					if(skill != null)
						skill.getEffects(this, this, false, false);
				}
			}
		}

	
		
		if(getNevitSystem().isBlessingActive())
			return;
		
		if (_event != null && !_event.canLostExpOnDie())
			return;
		
		if (NexusEvents.isInEvent(this))
			return;
		
		long before = getExp();
		addExpAndSp(-lostexp, 0);
		long lost = before - getExp();

		if(lost > 0)
			setVar("lostexp", String.valueOf(lost), -1);
	}

	public void setRequest(Request transaction)
	{
		_request = transaction;
	}

	public Request getRequest()
	{
		return _request;
	}

	/**
	 * Проверка, занят ли игрок для ответа на зарос
	 *
	 * @return true, если игрок не может ответить на запрос
	 */
	public boolean isBusy()
	{
		return isProcessingRequest() || isOutOfControl() || isInOlympiadMode() || getTeam() != TeamType.NONE || isInStoreMode() || isInDuel() || getMessageRefusal() || isBlockAll() || isInvisible();
	}

	public boolean isProcessingRequest()
	{
		if(_request == null)
			return false;
		if(!_request.isInProgress())
			return false;
		return true;
	}

	public boolean isInTrade()
	{
		return isProcessingRequest() && getRequest().isTypeOf(L2RequestType.TRADE);
	}

	public boolean isInPost()
	{
		return isProcessingRequest() && getRequest().isTypeOf(L2RequestType.POST);
	}

	public List<L2GameServerPacket> addVisibleObject(GameObject object)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || (object.isInvisible() && !canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)))
			return Collections.emptyList();

		return object.addPacketList(this);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer)
	{
		//TODO: using the cb, when he find a target, remove all others and start a timer to restore all shops for his view
		boolean isInvisible = isInvisible() && forPlayer.getObjectId() != getObjectId() && !forPlayer.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS);
		if(isInvisible)
			return Collections.emptyList();

		if(isInStoreMode() && !isInBuffStoreMode() && forPlayer.isNotShowTraders())
			return Collections.emptyList();

		// Если это фэйк обсервера - не показывать.
		if(isInObserverMode() && getCurrentRegion() != getObserverRegion() && getObserverRegion() == forPlayer.getCurrentRegion())
			return Collections.emptyList();

		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		if(forPlayer.getObjectId() != getObjectId())
			list.add(isPolymorphed() ? new NpcInfoPoly(this).setInvisible(isInvisible) : new CharInfo(this, forPlayer).setInvisible(isInvisible));

		list.add(new ExBR_ExtraUserInfo(this).setInvisible(isInvisible));

		if(isSitting() && _sittingObject != null)
			list.add(new ChairSit(this, _sittingObject).setInvisible(isInvisible));

		if(isInStoreMode())
		{
			if(getPrivateStoreType() == STORE_PRIVATE_BUY)
				list.add(new PrivateStoreMsgBuy(this).setInvisible(isInvisible));
			else if(getPrivateStoreType() == STORE_PRIVATE_SELL || getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
				list.add(new PrivateStoreMsgSell(this).setInvisible(isInvisible));
			else if(getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				list.add(new RecipeShopMsg(this).setInvisible(isInvisible));
			if(forPlayer.isInZonePeace()) // Мирным торговцам не нужно посылать больше пакетов, для экономии траффика
				return list;
		}

		if(isCastingNow())
		{
			Creature castingTarget = getCastingTarget();
			Skill castingSkill = getCastingSkill();
			long animationEndTime = getAnimationEndTime();
			if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && getAnimationEndTime() > 0)
				list.add(new MagicSkillUse(this, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0).setInvisible(isInvisible));
		}

		if(isInCombat())
			list.add(new AutoAttackStart(getObjectId()));

		list.add(RelationChanged.update(forPlayer, this, forPlayer));
		
		DominionSiegeEvent dominionSiegeEvent = getEvent(DominionSiegeEvent.class);
		if(dominionSiegeEvent != null && (dominionSiegeEvent.isInProgress() || dominionSiegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(getObjectId())))
			list.add(new ExDominionWarStart(this).setInvisible(isInvisible));
		
		if(isInBoat())
			list.add(getBoat().getOnPacket(this, getInBoatPosition()).setInvisible(isInvisible));
		else
		{
			if(isMoving() || isFollow())
				list.add(movePacket());
		}
		
		if (isInMountTransform() || (isInStoreMode() && entering))
		{
			list.add(new CharInfo(this));
			list.add(new ExBR_ExtraUserInfo(this));
		}
				
		return list;
	}

	public List<L2GameServerPacket> removeVisibleObject(GameObject object, List<L2GameServerPacket> list)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId())
			return null;

		List<L2GameServerPacket> result = list == null ? object.deletePacketList() : list;

		getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		return result;
	}

	private void levelSet(int levels)
	{
		if(levels > 0)
		{
			sendPacket(Msg.YOU_HAVE_INCREASED_YOUR_LEVEL);
			broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));

			setCurrentHpMp(getMaxHp(), getMaxMp());
			setCurrentCp(getMaxCp());

			if (!Config.DISABLE_TUTORIAL)
			{
				Quest q = QuestManager.getQuest(255);
				if(q != null)
					processQuestEvent(q.getName(), "CE40", null);
			}
		}
		else if(levels < 0)
			if(Config.ALT_REMOVE_SKILLS_ON_DELEVEL)
				checkSkills();

		// Recalculate the party level
		if(isInParty())
			getParty().recalculatePartyData();

		if(_clan != null)
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));

		if(_matchingRoom != null)
			_matchingRoom.broadcastPlayerUpdate(this);

		// Give Expertise skill of this level
		rewardSkills(true);
	}

	/**
	 * Удаляет все скиллы, которые учатся на уровне большем, чем текущий+maxDiff
	 */
	public void checkSkills()
	{
		for(Skill sk : getAllSkillsArray())
			SkillTreeTable.checkSkill(this, sk);
	}

	public void startTimers()
	{
		startAutoSaveTask();
		startPcBangPointsTask();
		startBonusTask();
		getInventory().startTimers();
		resumeQuestTimers();
	}

	public void stopAllTimers()
	{
		setAgathion(0);
		stopWaterTask();
		stopBonusTask();
		stopHourlyTask();
		stopAutoPotionTask(-1);
		stopKickTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		getInventory().stopAllTimers();
		stopQuestTimers();
		getNevitSystem().stopTasksOnLogout();
	}

	@Override
	public Summon getPet()
	{
		return _summon;
	}

	public void setPet(Summon summon)
	{
		boolean isPet = false;
		if(_summon != null && _summon.isPet())
			isPet = true;
		unsetVar("pet");
		_summon = summon;
		autoShot();
		if(summon == null)
		{
			if(isPet)
			{
				if(isLogoutStarted())
					if(getPetControlItem() != null)
						setVar("pet", String.valueOf(getPetControlItem().getObjectId()), -1);
				setPetControlItem(null);
			}
			getEffectList().stopEffect(4140);
		}
	}

	public void scheduleDelete()
	{
		long time = 0;

		if(Config.SERVICES_ENABLE_NO_CARRIER)
			time = NumberUtils.toInt(getVar("noCarrier"), Config.SERVICES_NO_CARRIER_TIME);

		scheduleDelete(time * 1000);
	}

	/**
	 * Удалит персонажа из мира через указанное время, если на момент истечения времени он не будет присоединен.<br>
	 * @param time время в миллисекундах
	 */
	public void scheduleDelete(long time)
	{
		if(isLogoutStarted() || isInOfflineMode())
			return;
		
		// No carrier, lets save the title, title color on delete and restore it on re-enter.
		setVar("NoCarrierTitle", getTitle(), -1);
		setVar("NoCarrierTitleColor", getTitleColor(), -1);
		
		setEnchantCatalyst(0);
		setEnchantScroll(null);
		
		broadcastCharInfo();

		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				if (!isConnected())
				{
					Party party = getParty();

					if(party != null)
					{
						if(isFestivalParticipant())
							party.sendMessage(getName() + " has been removed from the upcoming festival.");
						leaveParty();
					}
					
					// Infern0 lets not unset var, its stupid player all the time to enable no-carrier system from .cfg
					//unsetVar("noCarrier");
					prepareToLogout();
					deleteMe();
				}
				
			}
		}, time);
	}

	@Override
	protected void onDelete()
	{
		super.onDelete();

		// Убираем фэйк в точке наблюдения
		WorldRegion observerRegion = getObserverRegion();
		if(observerRegion != null)
			observerRegion.removeObject(this);

		//Send friendlists to friends that this player has logged off
		_friendList.notifyFriends(false);

		_teleportBookmarks.clear();

		_inventory.clear();
		_warehouse.clear();
		_summon = null;
		_arrowItem = null;
		_fistsWeaponItem = null;
		_chars = null;
		_enchantScroll = null;
		_lastNpc = HardReferences.emptyRef();
		_observerRegion = null;
	}

	public void setTradeList(List<TradeItem> list)
	{
		_tradeList = list;
	}

	public List<TradeItem> getTradeList()
	{
		return _tradeList;
	}

	public String getSellStoreName()
	{
		return _sellStoreName;
	}

	public void setSellStoreName(String name)
	{
		_sellStoreName = Strings.stripToSingleLine(name);
	}

	public void setSellList(boolean packageSell, List<TradeItem> list)
	{
		if (packageSell)
			_packageSellList = list;
		else
			_sellList = list;
	}

	public List<TradeItem> getSellList()
	{
		return getSellList(_privatestore == STORE_PRIVATE_SELL_PACKAGE);
	}

	public List<TradeItem> getSellList(boolean packageSell)
	{
		return packageSell ? _packageSellList : _sellList;
	}

	public String getBuyStoreName()
	{
		return _buyStoreName;
	}

	public void setBuyStoreName(String name)
	{
		_buyStoreName = Strings.stripToSingleLine(name);
	}

	public void setBuyList(List<TradeItem> list)
	{
		_buyList = list;
	}

	public List<TradeItem> getBuyList()
	{
		return _buyList;
	}

	public void setManufactureName(String name)
	{
		_manufactureName = Strings.stripToSingleLine(name);
	}

	public String getManufactureName()
	{
		return _manufactureName;
	}

	public List<ManufactureItem> getCreateList()
	{
		return _createList;
	}

	public void setCreateList(List<ManufactureItem> list)
	{
		_createList = list;
	}

	public void setPrivateStoreType(final int type)
	{
		_privatestore = type;
		if(type != STORE_PRIVATE_NONE)
			setVar("storemode", String.valueOf(type), -1);
		else
			unsetVar("storemode");
	}

	public boolean isInStoreMode()
	{
		return _privatestore != STORE_PRIVATE_NONE;
	}

	public boolean isInBuffStoreMode()
	{
		return _privatestore == STORE_SELLBUFF;
	}
	
	public int getPrivateStoreType()
	{
		return _privatestore;
	}
	
	public boolean canOpenPrivateStore(int storeType)
	{
		if(!getAccessLevel().allowTransaction())
		{
			sendPacket(SystemMsg.SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_____);
			return false;
		}
		
		if (canOverrideCond(PcCondOverride.ITEM_TRADE_CONDITIONS))
			return true;

		if(getLevel() < Config.SERVICES_TRADE_MIN_LEVEL)
		{
			sendMessage(new CustomMessage("trade.NotHavePermission", this).addNumber(Config.SERVICES_TRADE_MIN_LEVEL));
			return false;
		}

		String tradeBan = getVar("tradeBan");
		if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
		{
			sendPacket(SystemMsg.YOU_ARE_CURRENTLY_BLOCKED_FROM_USING_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP);
			return false;
		}

		String BLOCK_ZONE = storeType == Player.STORE_PRIVATE_MANUFACTURE ? Zone.BLOCKED_ACTION_PRIVATE_WORKSHOP : Zone.BLOCKED_ACTION_PRIVATE_STORE;
		if(isActionBlocked(BLOCK_ZONE))
			if(!Config.SERVICES_NO_TRADE_ONLY_OFFLINE || Config.SERVICES_NO_TRADE_ONLY_OFFLINE && isInOfflineMode())
			{
				sendPacket(storeType == Player.STORE_PRIVATE_MANUFACTURE ? SystemMsg.YOU_CANNOT_OPEN_A_PRIVATE_WORKSHOP_HERE : SystemMsg.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
				return false;
			}

		if(isCastingNow())
		{
			sendPacket(SystemMsg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
			return false;
		}

		if(isInCombat())
		{
			sendPacket(SystemMsg.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return false;
		}

		if(isActionsDisabled() || isMounted() || isInOlympiadMode() || isInDuel() || isProcessingRequest())
			return false;

		if(Config.SERVICES_TRADE_ONLY_FAR)
		{
			boolean tradenear = false;
			for(Player p : World.getAroundPlayers(this, Config.SERVICES_TRADE_RADIUS, 200))
				if(p.isInStoreMode())
				{
					tradenear = true;
					break;
				}

			if(World.getAroundNpc(this, Config.SERVICES_TRADE_RADIUS + 100, 200).size() > 0)
				tradenear = true;

			if(tradenear)
			{
				sendMessage(new CustomMessage("trade.OtherTradersNear", this));
				return false;
			}
		}

		return true;
	}
	
	public void cancelPrivateStore()
	{
		setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		if(isInOfflineMode())
		{
			setOfflineMode(false);
			kick();
		}
		else
			broadcastCharInfo();
	}
	
	public long getTax(long price)
	{
		long tax = (long) (price * Config.SERVICES_TRADE_TAX / 100);
		if(isInZone(Zone.ZoneType.offshore))
			tax = (long) (price * Config.SERVICES_OFFSHORE_TRADE_TAX / 100);
		if(Config.SERVICES_TRADE_TAX_ONLY_OFFLINE && !isInOfflineMode())
			tax = 0;
		if(Config.SERVICES_PARNASSUS_NOTAX && getReflection() == ReflectionManager.PARNASSUS)
			tax = 0;

		return tax;
	}
	
	public void purchaseItem(Player purchasedFrom, TradeItem item)
	{
		long price = item.getCount() * item.getOwnersPrice();
		if(!item.getItem().isStackable())
		{
			if(item.getEnchantLevel() > 0)
			{
				purchasedFrom.sendPacket(new SystemMessage2(SystemMsg.S2S3_HAS_BEEN_SOLD_TO_C1_AT_THE_PRICE_OF_S4_ADENA).addString(getName()).addInteger(item.getEnchantLevel()).addItemName(item.getItemId()).addLong(price));
				sendPacket(new SystemMessage2(SystemMsg.S2S3_HAS_BEEN_PURCHASED_FROM_C1_AT_THE_PRICE_OF_S4_ADENA).addString(purchasedFrom.getName()).addInteger(item.getEnchantLevel()).addItemName(item.getItemId()).addLong(price));
			}
			else
			{
				purchasedFrom.sendPacket(new SystemMessage2(SystemMsg.S2_IS_SOLD_TO_C1_FOR_THE_PRICE_OF_S3_ADENA).addString(getName()).addItemName(item.getItemId()).addLong(price));
				sendPacket(new SystemMessage2(SystemMsg.S2_HAS_BEEN_PURCHASED_FROM_C1_AT_THE_PRICE_OF_S3_ADENA).addString(purchasedFrom.getName()).addItemName(item.getItemId()).addLong(price));
			}
		}
		else
		{
			purchasedFrom.sendPacket(new SystemMessage2(SystemMsg.S2_S3_HAVE_BEEN_SOLD_TO_C1_FOR_S4_ADENA).addString(getName()).addItemName(item.getItemId()).addLong(item.getCount()).addLong(price));
			sendPacket(new SystemMessage2(SystemMsg.S3_S2_HAS_BEEN_PURCHASED_FROM_C1_FOR_S4_ADENA).addString(purchasedFrom.getName()).addItemName(item.getItemId()).addLong(item.getCount()).addLong(price));
		}
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2Player.<BR><BR>
	 *
	 * @param clan the clat to set
	 */
	public void setClan(Clan clan)
	{
		if(_clan != clan && _clan != null)
			unsetVar("canWhWithdraw");

		Clan oldClan = _clan;
		if(oldClan != null && clan == null)
			for(Skill skill : oldClan.getAllSkills())
				removeSkill(skill, false);

		_clan = clan;

		if(clan == null)
		{
			_pledgeType = Clan.SUBUNIT_NONE;
			_pledgeClass = RANK_VAGABOND;
			_powerGrade = 0;
			_apprentice = 0;
			getInventory().validateItems();
			return;
		}

		if(!clan.isAnyMember(getObjectId()))
		{
			setClan(null);
			if(!isNoble())
				setTitle("");
		}
	}

	@Override
	public Clan getClan()
	{
		return _clan;
	}

	public SubUnit getSubUnit()
	{
		return _clan == null ? null : _clan.getSubUnit(_pledgeType);
	}

	public ClanHall getClanHall()
	{
		int id = _clan != null ? _clan.getHasHideout() : 0;
		return ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
	}

	public Castle getCastle()
	{
		int id = _clan != null ? _clan.getCastle() : 0;
		return ResidenceHolder.getInstance().getResidence(Castle.class, id);
	}

	public Fortress getFortress()
	{
		int id = _clan != null ? _clan.getHasFortress() : 0;
		return ResidenceHolder.getInstance().getResidence(Fortress.class, id);
	}

	public Alliance getAlliance()
	{
		return _clan == null ? null : _clan.getAlliance();
	}

	public boolean isClanLeader()
	{
		return _clan != null && getObjectId() == _clan.getLeaderId();
	}

	public boolean isAllyLeader()
	{
		return getAlliance() != null && getAlliance().getLeader().getLeaderId() == getObjectId();
	}

	@Override
	public void reduceArrowCount()
	{
		sendPacket(SystemMsg.YOU_CAREFULLY_NOCK_AN_ARROW);
		if(_arrowItem != null && (!Config.ALLOW_ARROW_INFINITELY || !PremiumAccountsTable.getPlayerInfinityShotsArrows(this)))
		{
			if(!getInventory().destroyItemByObjectId(getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1L))
			{
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, null);
				_arrowItem = null;
			}
		}
	}

	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2Player then return True.
	 */
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equipped in left hand
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			ItemInstance activeWeapon = getActiveWeaponInstance();
			if(activeWeapon != null)
			{
				if(activeWeapon.getItemType() == WeaponType.BOW)
					_arrowItem = getInventory().findArrowForBow(activeWeapon.getTemplate());
				else if(activeWeapon.getItemType() == WeaponType.CROSSBOW)
					_arrowItem = getInventory().findArrowForCrossbow(activeWeapon.getTemplate());
			}

			// Equip arrows needed in left hand
			if(_arrowItem != null)
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
		}
		else
			// Get the L2ItemInstance of arrows equipped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		return _arrowItem != null;
	}
	
	public boolean isInParty()
	{
		return _party != null;
	}

	public void setParty(final Party party)
	{
		_party = party;
	}

	public void joinParty(final Party party)
	{
		if(party != null)
			party.addPartyMember(this);
	}

	public void leaveParty()
	{
		if(isInParty())
			_party.removePartyMember(this, false, false);
	}

	public Party getParty()
	{
		return _party;
	}

	public void setLastPartyPosition(Location loc)
	{
		_lastPartyPosition = loc;
	}

	public Location getLastPartyPosition()
	{
		return _lastPartyPosition;
	}

	public boolean isPartyRequestValid(int i)
	{
		if(!_partyRequests.containsKey(i))
			return false;

		if(System.currentTimeMillis() - _partyRequests.get(i) < 30000)
			return true;

		_partyRequests.remove(i);
		return false;
	}

	public synchronized void addPartyRequest(int i)
	{
		_partyRequests.put(i, System.currentTimeMillis());
	}

	public synchronized void removePartyRequest(int i)
	{
		_partyRequests.remove(i);
	}

	public boolean isPartyFindValid()
	{
		if(System.currentTimeMillis() - _lookingForPartyMembers < 30000)
			return true;
		return false;
	}

	public void setPartyFindValid(boolean a)
	{
		_lookingForPartyMembers = a ? System.currentTimeMillis() : 0;
	}
	
	public boolean isGM()
	{
		return getAccessLevel().isGm();
	}
	
	public void setAccessLevel(int level)
	{
		_accessLevel = AdminTable.getInstance().getAccessLevel(level);
		
		setNameColor(_accessLevel.getNameColor());
		setTitleColor(_accessLevel.getTitleColor());
		broadcastUserInfo(true);
		
		if (!AdminTable.getInstance().hasAccessLevel(level))
			_log.warn("Tryed to set unregistered access level " + level + " for " + toString() + ". Setting access level without privileges!");
		else if (level > 0)
			_log.warn(_accessLevel.getName() + " access level set for character " + getName() + "! Just a warning to be careful ;)");
	}
	
	public AccessLevel getAccessLevel()
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			return AdminTable.getInstance().getMasterAccessLevel();
		else if (_accessLevel == null) /* This is here because inventory etc. is loaded before access level on login, so it is not null */
			setAccessLevel(0);
		
		return _accessLevel;
	}

	@Override
	public double getLevelMod()
	{
		return (89. + getLevel()) / 100.0;
	}

	/**
	 * Update Stats of the Player client side by sending Server->Client packet UserInfo/StatusUpdate to this L2Player and CharInfo/StatusUpdate to all players around (broadcast).<BR><BR>
	 */
	@Override
	public void updateStats()
	{
		if(entering || isLogoutStarted())
			return;

		refreshOverloaded();
		if(Config.EXPERTISE_PENALTY)
			refreshExpertisePenalty();
		super.updateStats();
	}

	@Override
	public void sendChanges()
	{
		if(!isPhantom() && entering || isLogoutStarted())
			return;
		
		super.sendChanges();
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2Player and all L2Player to inform (broadcast).
	 */
	public void updateKarma(boolean flagChanged)
	{
		sendStatusUpdate(true, true, StatusUpdate.KARMA);
		if(flagChanged)
			broadcastRelationChanged();
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	public void setIsOnline(boolean isOnline)
	{
		_isOnline = isOnline;
	}

	public void setOnlineStatus(boolean isOnline)
	{
		_isOnline = isOnline;
		updateOnlineStatus();
	}

	public void updateOnlineStatus()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, (isOnline() && !isInOfflineMode()) || (isInOfflineMode() && Config.SHOW_OFFLINE_MODE_IN_ONLINE) ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis() / 1000L);
			statement.setInt(3, getObjectId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).
	 */
	public void increaseKarma(final int add_karma)
	{
		boolean flagChanged = _karma == 0;
		int new_karma = _karma + add_karma;

		if(new_karma > Integer.MAX_VALUE)
			new_karma = Integer.MAX_VALUE;

		if(_karma == 0 && new_karma > 0)
		{
			if(_pvpFlag > 0)
			{
				_pvpFlag = 0;
				if(_PvPRegTask != null)
				{
					_PvPRegTask.cancel(true);
					_PvPRegTask = null;
				}
				sendStatusUpdate(true, true, StatusUpdate.PVP_FLAG);
			}

			_karma = new_karma;
		}
		else
			_karma = new_karma;

		if (Config.ENABLE_PLAYER_COUNTERS && getCounters().getPoints("_Highest_Karma") < new_karma)
			getCounters().setPoints("_Highest_Karma", new_karma);
		
		updateKarma(flagChanged);
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).
	 */
	public void decreaseKarma(final int i)
	{
		boolean flagChanged = _karma > 0;
		_karma -= i;
		if(_karma <= 0)
		{
			_karma = 0;
			updateKarma(flagChanged);
		}
		else
			updateKarma(false);
	}

	/**
	 * Create a new L2Player and add it in the characters table of the database.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a new L2Player with an account name </li>
	 * <li>Set the name, the Hair Style, the Hair Color and	the Face type of the L2Player</li>
	 * <li>Add the player in the characters table of the database</li><BR><BR>
	 *
	 * @param accountName The name of the L2Player
	 * @param name		The name of the L2Player
	 * @param hairStyle   The hair style Identifier of the L2Player
	 * @param hairColor   The hair color Identifier of the L2Player
	 * @param face		The face type Identifier of the L2Player
	 * @return The L2Player added to the database or null
	 */
	public static Player create(int classId, int sex, String accountName, final String name, final int hairStyle, final int hairColor, final int face)
	{
		PlayerTemplate template = CharTemplateHolder.getInstance().getTemplate(classId, sex != 0);

		if (template == null)
			return null;
		
		// Create a new L2Player with an account name
		Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName);

		player.setName(name);
		player.setTitle("");
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		player.setCreateTime(System.currentTimeMillis());

		// Add the player in the characters table of the database
		if(!CharacterDAO.getInstance().insert(player))
			return null;

		return player;
	}
	
	/**
	 * Retrieve a Player from the characters table of the database and add it in _allObjects of the World
	 *
	 * @return The Player loaded from the database
	 */
	public static Player restore(final int objectId)
	{
		Player player = null;
		Connection con = null;
		Statement statement = null;
		PreparedStatement statement3 = null;
		ResultSet rset = null;
		ResultSet rset3 = null;
		try
		{
			// Retrieve the L2Player from the characters table of the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");

			if(rset.next())
			{
				final int classId = CharacterDAO.getInstance().getClassIdByObjectId(objectId, true);
				if (classId < 0)
					return null;
				
				final boolean female = rset.getInt("sex") == 1;
				final PlayerTemplate template = CharTemplateHolder.getInstance().getTemplate(classId, female);

				player = new Player(objectId, template);

				player.setIsPhantom(false, false);
				
				player.loadVariables();
				player.loadInstanceReuses();
				player.loadPremiumItemList();
				player._teleportBookmarks.setCapacity(rset.getInt("bookmarks"));
				player._teleportBookmarks.restore();
				player._friendList.restore();
				player._postFriends = CharacterPostFriendDAO.getInstance().select(player);
				CharacterGroupReuseDAO.getInstance().select(player);

				player.setBaseClass(classId);
				player._login = rset.getString("account_name");
				player.setName(rset.getString("char_name"));
				player.setAccessLevel(rset.getInt("accessLevel"));

				player.setFace(rset.getInt("face"));
				player.setHairStyle(rset.getInt("hairStyle"));
				player.setHairColor(rset.getInt("hairColor"));
				player.setHeading(0);

				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setLeaveClanTime(rset.getLong("leaveclan") * 1000L);
				if(player.getLeaveClanTime() > 0 && player.canJoinClan())
					player.setLeaveClanTime(0);
				player.setDeleteClanTime(rset.getLong("deleteclan") * 1000L);
				if(player.getDeleteClanTime() > 0 && player.canCreateClan())
					player.setDeleteClanTime(0);

				player.setNoChannel(rset.getLong("nochannel") * 1000L);
				if(player.getNoChannel() > 0 && player.getNoChannelRemained() < 0)
					player.setNoChannel(0);

				player.setOnlineTime(rset.getLong("onlinetime") * 1000L);

				final int clanId = rset.getInt("clanid");
				if(clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
					player.setPledgeType(rset.getInt("pledge_type"));
					player.setPowerGrade(rset.getInt("pledge_rank"));
					player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
					player.setApprentice(rset.getInt("apprentice"));
				}

				player.setCreateTime(rset.getLong("createtime") * 1000L);
				player.setDeleteTimer(rset.getInt("deletetime"));

				if (player.getVar("offlinebuffrestoretitle") != null)
					player.setTitle(player.getVar("offlinebuffrestoretitle"));
				else
					player.setTitle(rset.getString("title"));

				if(player.getVar("titlecolor") != null)
					player.setTitleColor(Integer.decode("0x" + player.getVar("titlecolor")));

				if(player.getVar("namecolor") == null)
					if(player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId())
						player.setNameColor(Config.CLANLEADER_NAME_COLOUR);
					else
						player.setNameColor(Config.NORMAL_NAME_COLOUR);
				else
					player.setNameColor(Integer.decode("0x" + player.getVar("namecolor")));

				if(Config.AUTO_LOOT_INDIVIDUAL)
				{
					player._autoLoot = player.getVarB("AutoLoot", Config.AUTO_LOOT);
					player._autoLootHerbs = player.getVarB("AutoLootHerbs", Config.AUTO_LOOT_HERBS);
					player._autoLootOnlyAdena = player.getVarB("AutoLootOnlyAdena", Config.AUTO_LOOT_ONLY_ADENA);
				}

				player.setFistsWeaponItem(player.findFistsWeaponItem(classId));
				player.setLastAccess(rset.getLong("lastAccess"));
				
				player.setRecomHave(rset.getInt("rec_have"));
				player.setRecomLeft(rset.getInt("rec_left"));
				player.setRecomBonusTime(rset.getInt("rec_bonus_time"));

				if(player.getVar("recLeftToday") != null)
					player.setRecomLeftToday(Integer.parseInt(player.getVar("recLeftToday")));
				else
					player.setRecomLeftToday(0);

				player.getNevitSystem().setPoints(rset.getInt("hunt_points"), rset.getInt("hunt_time"));

				player.setKeyBindings(rset.getBytes("key_bindings"));
				player.setPcBangPoints(rset.getInt("pcBangPoints"));

				player.setFame(rset.getInt("fame"), null);

				player.restoreRecipeBook();

				if(Config.ENABLE_OLYMPIAD)
				{
					player.setHero(Hero.getInstance().isHero(player.getObjectId()));
					player.setNoble(Olympiad.isNoble(player.getObjectId()));
				}

				player.updatePledgeClass();
				
				player.setDisableFogAndRain(Boolean.parseBoolean("disableFogAndRain"));
				
				int reflection = 0;
				int tempRef = 0;

				if (player.getVar("jailed") != null && player.getVarTimeToExpire("jailed") > 0)
				{
					// рандомный спаун по тюрьме
					player.setXYZ(Rnd.get(-114936, -114136), Rnd.get(-249768, -248952), -2984);

					long period = player.getVarTimeToExpire("jailed");

					if(period != -1)
						player._unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player, true), period);
				}
				else if(player.getVar("jailedFrom") != null)
				{
					String[] re = player.getVar("jailedFrom").split(";");

					player.setXYZ(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
					player.setReflection(re.length > 3 ? Integer.parseInt(re[3]) : 0);

					player.unsetVar("jailedFrom");
				}
				else if (player.getVar("noCarrier") != null)
				{
					player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
					String ref = player.getVar("reflection");
					if(ref != null)
					{
						tempRef = Integer.parseInt(ref);
						if(tempRef > 0)
						{
							if (ReflectionManager.getInstance().get(tempRef) != null)
								reflection = tempRef;
							
							player.unsetVar("noCarrier");
						}
					}
				}
				else
				{
					player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
					String ref = player.getVar("reflection");
					if(ref != null)
					{
						reflection = Integer.parseInt(ref);
						if(reflection > 0) // не портаем назад из ГХ, парнаса, джайла
						{
							String back = player.getVar("backCoords");
							if(back != null)
							{
								player.setLoc(Location.parseLoc(back));
								player.unsetVar("backCoords");
							}
							reflection = 0;
						}
					}
				}

				player.setReflection(reflection);

				EventHolder.getInstance().findEvent(player);
				
				Quest.restoreQuestStates(player);

				player.getInventory().restore();

				restoreCharSubClasses(player);

				if (Config.ENABLE_ACHIEVEMENTS)
				{
					player.getCounters().load();
					player.loadAchivementLevel();
				}
				
				// 4 очка в минуту оффлайна
				player.setVitality(rset.getInt("vitality") + (int) ((System.currentTimeMillis() / 1000L - rset.getLong("lastAccess")) / 15.));

				try
				{
					String var = player.getVar("ExpandInventory");
					if(var != null)
						player.setExpandInventory(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar("ExpandWarehouse");
					if(var != null)
						player.setExpandWarehouse(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar(NO_ANIMATION_OF_CAST_VAR);
					if(var != null)
						player.setNotShowBuffAnim(Boolean.parseBoolean(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					player.setNotShowTraders(player.getVarB(NO_TRADERS_VAR, false));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar("pet");
					if(var != null)
						player.setPetControlItem(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar("isPvPevents");
					if(var != null)
						player.unsetVar("isPvPevents");
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				statement3 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id!=?");
				statement3.setString(1, player._login);
				statement3.setInt(2, objectId);
				rset3 = statement3.executeQuery();
				while(rset3.next())
				{
					final Integer charId = rset3.getInt("obj_Id");
					final String charName = rset3.getString("char_name");
					if (player._chars != null)
						player._chars.put(charId, charName);
				}

				DbUtils.close(statement3, rset3);

				//if(!player.isGM())
				{
					ArrayList<Zone> zones = new ArrayList<>();

					World.getZones(zones, player.getLoc(), player.getReflection());

					if(!zones.isEmpty())
						for(Zone zone : zones)
							if(zone.getType() == ZoneType.no_restart)
							{
								if(System.currentTimeMillis() / 1000L - player.getLastAccess() > zone.getRestartTime())
								{
									player.sendMessage(new CustomMessage("l2r.gameserver.clientpackets.EnterWorld.TeleportedReasonNoRestart", player));
									player.setLoc(Location.getRestartLocation(player, RestartType.TO_VILLAGE));
								}
							}
							else if(zone.getType() == ZoneType.SIEGE)
							{
								SiegeEvent siegeEvent = player.getEvent(SiegeEvent.class);
								if(siegeEvent != null)
									player.setLoc(siegeEvent.getEnterLoc(player));
								else
								{
									Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
									player.setLoc(r.getNotOwnerRestartPoint(player));
								}
							}


					if(DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getLoc(), false))
						player.setLoc(DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords());
				}

				player.restoreBlockList();
				player._macroses.restore();
				
				player.refreshExpertisePenalty();
				player.refreshOverloaded();

				player.getWarehouse().restore();
				player.getFreight().restore();

				player.restoreTradeList();
				if(player.getVar("storemode") != null)
				{
					player.setPrivateStoreType(Integer.parseInt(player.getVar("storemode")));
					player.setSitting(true);
				}

				if(player.getVar("offlinebufferprice") != null)
				{
					OfflineBufferManager.getInstance().restoreOfflineBuffer(player, false);
					player.setSitting(true);
				}
				
				if(player.getVar("offlinebufferschemeprice") != null)
				{
					OfflineBufferManager.getInstance().restoreOfflineBuffer(player, true);
					player.setSitting(true);
				}
				
				try
				{
					String var = player.getVar("FightClubRate");
					if(var != null)
						RestoreFightClub(player);
				}
				catch(Exception e)
				{
					_log.error("", e);
				}
				
				player.updateKetraVarka();
				player.updateRam();
				player.checkRecom();
				if(player.isCursedWeaponEquipped())
					player.restoreCursedWeapon();
			}
		}
		catch(final Exception e)
		{
			_log.error("Could not restore char data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(statement3, rset3);
			DbUtils.closeQuietly(con, statement, rset);
		}
		return player;
	}

	private void loadPremiumItemList()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				int itemNum = rs.getInt("itemNum");
				int itemId = rs.getInt("itemId");
				long itemCount = rs.getLong("itemCount");
				String itemSender = rs.getString("itemSender");
				PremiumItem item = new PremiumItem(itemId, itemCount, itemSender);
				_premiumItems.put(itemNum, item);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public void updatePremiumItem(int itemNum, long newcount)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=?");
			statement.setLong(1, newcount);
			statement.setInt(2, getObjectId());
			statement.setInt(3, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deletePremiumItem(int itemNum)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public Map<Integer, PremiumItem> getPremiumItemList()
	{
		return _premiumItems;
	}

	/**
	 * Update L2Player stats in the characters table of the database.
	 */
	public void store(boolean fast)
	{
		if(!_storeLock.tryLock())
			return;
		
		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(//
						"UPDATE characters SET face=?,hairStyle=?,hairColor=?,x=?,y=?,z=?" + //
								",karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,rec_bonus_time=?,hunt_points=?,hunt_time=?,clanid=?,deletetime=?," + //
								"title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?," + //
								"onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,vitality=?,fame=?,bookmarks=? WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, getFace());
				statement.setInt(2, getHairStyle());
				statement.setInt(3, getHairColor());
				if(_stablePoint == null) // если игрок находится в точке в которой его сохранять не стоит (например на виверне) то сохраняются последние координаты
				{
					statement.setInt(4, getX());
					statement.setInt(5, getY());
					statement.setInt(6, getZ());
				}
				else
				{
					statement.setInt(4, _stablePoint.x);
					statement.setInt(5, _stablePoint.y);
					statement.setInt(6, _stablePoint.z);
				}
				statement.setInt(7, getKarma());
				statement.setInt(8, getPvpKills());
				statement.setInt(9, getPkKills());
				statement.setInt(10, getRecomHave());
				try
				{
					if (getRecomLeft() > 20)
				          setRecomLeft(20);
					
					statement.setInt(11, getRecomLeft());
				}
				catch(Exception e)
				{
					statement.setInt(11, 0);
				}
				statement.setInt(12, getRecomBonusTime());
				statement.setInt(13, getNevitSystem().getPoints());
				statement.setInt(14, getNevitSystem().getTime());
				statement.setInt(15, getClanId());
				statement.setInt(16, getDeleteTimer());
				statement.setString(17, _title);
				statement.setInt(18, _accessLevel.getLevel());
				statement.setInt(19, (isOnline() && !isInOfflineMode()) || (isInOfflineMode() && Config.SHOW_OFFLINE_MODE_IN_ONLINE) ? 1 : 0);
				statement.setLong(20, getLeaveClanTime() / 1000L);
				statement.setLong(21, getDeleteClanTime() / 1000L);
				statement.setLong(22, _NoChannel > 0 ? getNoChannelRemained() / 1000 : _NoChannel);
				statement.setInt(23, (int) getOnlineTime());
				statement.setInt(24, getPledgeType());
				statement.setInt(25, getPowerGrade());
				statement.setInt(26, getLvlJoinedAcademy());
				statement.setInt(27, getApprentice());
				statement.setBytes(28, getKeyBindings());
				statement.setInt(29, getPcBangPoints());
				statement.setString(30, getName());
				statement.setInt(31, (int) getVitality());
				statement.setInt(32, getFame());
				statement.setInt(33, _teleportBookmarks.getCapacity());
				statement.setInt(34, getObjectId());

				statement.executeUpdate();
				
				if (!isPhantom())
				{
					if (Config.RATE_DROP_ADENA < 20)
						GameStats.increaseUpdatePlayerBase();
					
					if (!fast)
					{
						CharacterGroupReuseDAO.getInstance().insert(this);
						storeDisableSkills();
						storeBlockList();
					}
					
					if (Config.ENABLE_ACHIEVEMENTS)
					{
						getCounters().save();
						saveAchivementsLevels();
					}
					
					for (PlayerBuffProfile pbp : _profiles.values())
						pbp.save(this);
					
					storeCharSubClasses();
					_teleportBookmarks.store();
				}
			}
			catch(Exception e)
			{
				_log.error("Could not store char data: " + this + "!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		finally
		{
			_storeLock.unlock();
		}
	}

	/**
	 * Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player and save update in the character_skills table of the database.
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public Skill addSkill(final Skill newSkill, final boolean store)
	{
		if(newSkill == null)
			return null;

		// Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player
		Skill oldSkill = super.addSkill(newSkill);

		if(newSkill.equals(oldSkill))
			return oldSkill;

		// Add or update a L2Player skill in the character_skills table of the database
		if(store)
			storeSkill(newSkill, oldSkill);

		return oldSkill;
	}
	
	public void addSiegeSkills()
	{
		addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		addSkill(SkillTable.getInstance().getInfo(247, 1), false);
		if(isNoble())
			addSkill(SkillTable.getInstance().getInfo(326, 1), false);

		if(getClan() != null && getClan().getCastle() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(844, 1), false);
			addSkill(SkillTable.getInstance().getInfo(845, 1), false);
		}
	}

	public Skill removeSkill(Skill skill, boolean fromDB)
	{
		if(skill == null)
			return null;
		return removeSkill(skill.getId(), fromDB);
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.
	 *
	 * @return The L2Skill removed
	 */
	public Skill removeSkill(int id, boolean fromDB)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		Skill oldSkill = super.removeSkillById(id);

		if(!fromDB)
			return oldSkill;

		if(oldSkill != null)
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				// Remove or update a L2Player skill from the character_skills table of the database
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getActiveClassId());
				statement.execute();
			}
			catch(final Exception e)
			{
				_log.error("Could not delete skill!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}

		return oldSkill;
	}
	
	public void removeSiegeSkills()
	{
		removeSkill(SkillTable.getInstance().getInfo(246, 1), false);
		removeSkill(SkillTable.getInstance().getInfo(247, 1), false);
		removeSkill(SkillTable.getInstance().getInfo(326, 1), false);

		if(getClan() != null && getClan().getCastle() > 0)
		{
			removeSkill(SkillTable.getInstance().getInfo(844, 1), false);
			removeSkill(SkillTable.getInstance().getInfo(845, 1), false);
		}
	}

	/**
	 * Add or update a L2Player skill in the character_skills table of the database.
	 */
	private void storeSkill(final Skill newSkill, final Skill oldSkill)
	{
		if(newSkill == null) // вообще-то невозможно
		{
			_log.warn("could not store new skill. its NULL");
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("REPLACE INTO character_skills (char_obj_id,skill_id,skill_level,class_index) values(?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newSkill.getId());
			statement.setInt(3, newSkill.getLevel());
			statement.setInt(4, getActiveClassId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("Error could not store skills!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Retrieve from the database all skills of this L2Player and add them to _skills.
	 */
	private void restoreSkills()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all skills of this L2Player from the database
			// Send the SQL query : SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? to the database
			con = DatabaseFactory.getInstance().getConnection();
			if (Config.ALT_ENABLE_MULTI_PROFA)
			{
				statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=?");
				statement.setInt(1, getObjectId());
			}
			else
			{
				statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");
				statement.setInt(1, getObjectId());
				statement.setInt(2, getActiveClassId());
			}
			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				final int id = rset.getInt("skill_id");
				final int level = rset.getInt("skill_level");

				// Create a L2Skill object for each record
				final Skill skill = SkillTable.getInstance().getInfo(id, level);

				if(skill == null)
					continue;
				
				super.addSkill(skill);
				
				// Remove skill if not possible
				if(!isGM() && !skill.isCommon() && !SkillAcquireHolder.getInstance().isSkillPossible(this, skill))
				{
					//int ReturnSP = SkillTreeTable.getInstance().getSkillCost(this, skill);
					//if(ReturnSP == Integer.MAX_VALUE || ReturnSP < 0)
					//		ReturnSP = 0;
					removeSkill(skill, true);
					removeSkillFromShortCut(skill.getId());
					//if(ReturnSP > 0)
					//		setSp(getSp() + ReturnSP);

					Log.IllegalPlayerAction(getPlayer(), "has skill " + skill.getName() +" that should not have. (REMOVED)", 0);
					continue;
				}
			}

			// Restore noble skills
			if(isNoble())
				updateNobleSkills();

			// Restore Hero skills at main class only
			if(_hero && getBaseClassId() == getActiveClassId())
				Hero.addSkills(this);

			// Restore clan skills
			if (_clan != null)
			{
				_clan.addSkillsQuietly(this);

				// Restore clan leader siege skills
				if(_clan.getLeaderId() == getObjectId() && _clan.getLevel() >= 5)
					addSiegeSkills();
			}

			// Give dwarven craft skill
			if(getActiveClassId() >= 53 && getActiveClassId() <= 57 || getActiveClassId() == 117 || getActiveClassId() == 118)
				super.addSkill(SkillTable.getInstance().getInfo(1321, 1));

			super.addSkill(SkillTable.getInstance().getInfo(1322, 1));

			if(Config.UNSTUCK_SKILL && getSkillLevel(1050) < 0)
				super.addSkill(SkillTable.getInstance().getInfo(2099, 1));
		}
		catch(final Exception e)
		{
			_log.warn("Could not restore skills for player objId: " + getObjectId());
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void storeDisableSkills()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());

			if(_skillReuses.isEmpty())
				return;

			SqlBatch b = new SqlBatch("REPLACE INTO `character_skills_save` (`char_obj_id`,`skill_id`,`skill_level`,`class_index`,`end_time`,`reuse_delay_org`) VALUES");
			synchronized(_skillReuses)
			{
				StringBuilder sb;
				for(TimeStamp timeStamp : _skillReuses.values())
				{
					if(timeStamp.hasNotPassed())
					{
						sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(timeStamp.getId()).append(",");
						sb.append(timeStamp.getLevel()).append(",");
						sb.append(getActiveClassId()).append(",");
						sb.append(timeStamp.getEndTime()).append(",");
						sb.append(timeStamp.getReuseBasic()).append(")");
						b.write(sb.toString());
					}
				}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(final Exception e)
		{
			_log.warn("Could not store disable skills data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void restoreDisableSkills()
	{
		_skillReuses.clear();

		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT skill_id,skill_level,end_time,reuse_delay_org FROM character_skills_save WHERE char_obj_id=" + getObjectId() + " AND class_index=" + getActiveClassId());
			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLevel = rset.getInt("skill_level");
				long endTime = rset.getLong("end_time");
				long rDelayOrg = rset.getLong("reuse_delay_org");
				long curTime = System.currentTimeMillis();

				Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

				if(skill != null && endTime - curTime > 500)
					_skillReuses.put(skill.hashCode(), new TimeStamp(skill, endTime, rDelayOrg));
			}
			DbUtils.close(statement);

			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());
		}
		catch(Exception e)
		{
			_log.error("Could not restore active skills data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	/**
	 * Retrieve from the database all Henna of this L2Player, add them to _henna and calculate stats of the L2Player.<BR><BR>
	 */
	private void restoreHenna()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();

			for(int i = 0; i < 3; i++)
				_henna[i] = null;

			while(rset.next())
			{
				final int slot = rset.getInt("slot");
				if(slot < 1 || slot > 3)
					continue;

				final int symbol_id = rset.getInt("symbol_id");

				if(symbol_id != 0)
				{
					final Henna tpl = HennaHolder.getInstance().getHenna(symbol_id);
					if(tpl != null)
					{
						_henna[slot - 1] = tpl;
					}
				}
			}
		}
		catch(final Exception e)
		{
			_log.warn("could not restore henna: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		// Calculate Henna modifiers of this L2Player
		recalcHennaStats();

	}

	public int getHennaEmptySlots()
	{
		int totalSlots = getClassId().level();
		for(int i = 0; i < 3; i++)
			if(_henna[i] != null)
				totalSlots--;

		if(totalSlots <= 0)
			return 0;

		return totalSlots;

	}

	/**
	 * Remove a Henna of the L2Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2Player.<BR><BR>
	 */
	public boolean removeHenna(int slot)
	{
		if(slot < 1 || slot > 3)
			return false;

		slot--;

		if(_henna[slot] == null)
			return false;

		final Henna henna = _henna[slot];
		final int dyeID = henna.getDyeId();

		_henna[slot] = null;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=? and class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getActiveClassId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warn("could not remove char henna: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		// Calculate Henna modifiers of this L2Player
		recalcHennaStats();

		// Send Server->Client HennaInfo packet to this L2Player
		sendPacket(new HennaInfo(this));
		// Send Server->Client UserInfo packet to this L2Player
		sendUserInfo(true);

		// Add the recovered dyes to the player's inventory and notify them.
		ItemFunctions.addItem(this, dyeID, henna.getDrawCount() / 2, true);

		return true;
	}

	/**
	 * Add a Henna to the L2Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2Player.<BR><BR>
	 *
	 * @param henna L2Henna РґР»СЏ РґРѕР±Р°РІР»РµРЅРёСЏ
	 */
	public boolean addHenna(Henna henna)
	{
		if(getHennaEmptySlots() == 0)
		{
			sendPacket(SystemMsg.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
			return false;
		}

		// int slot = 0;
		for(int i = 0; i < 3; i++)
			if(_henna[i] == null)
			{
				_henna[i] = henna;

				// Calculate Henna modifiers of this L2Player
				recalcHennaStats();

				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, slot, class_index) VALUES (?,?,?,?)");
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getActiveClassId());
					statement.execute();
				}
				catch(Exception e)
				{
					_log.warn("could not save char henna: " + e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}

				sendPacket(new HennaInfo(this));
				sendUserInfo(true);

				return true;
			}

		return false;
	}

	/**
	 * Calculate Henna modifiers of this L2Player.
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;

		for(int i = 0; i < 3; i++)
		{
			Henna henna = _henna[i];
			if(henna == null)
				continue;
			if(!henna.isForThisClass(this))
				continue;

			_hennaINT += henna.getStatINT();
			_hennaSTR += henna.getStatSTR();
			_hennaMEN += henna.getStatMEN();
			_hennaCON += henna.getStatCON();
			_hennaWIT += henna.getStatWIT();
			_hennaDEX += henna.getStatDEX();
		}

		if(_hennaINT > Config.HENNA_MAX_VALUE)
			_hennaINT = Config.HENNA_MAX_VALUE;
		if(_hennaSTR > Config.HENNA_MAX_VALUE)
			_hennaSTR = Config.HENNA_MAX_VALUE;
		if(_hennaMEN > Config.HENNA_MAX_VALUE)
			_hennaMEN = Config.HENNA_MAX_VALUE;
		if(_hennaCON > Config.HENNA_MAX_VALUE)
			_hennaCON = Config.HENNA_MAX_VALUE;
		if(_hennaWIT > Config.HENNA_MAX_VALUE)
			_hennaWIT = Config.HENNA_MAX_VALUE;
		if(_hennaDEX > Config.HENNA_MAX_VALUE)
			_hennaDEX = Config.HENNA_MAX_VALUE;
	}

	/**
	 * @param slot id слота у перса
	 * @return the Henna of this L2Player corresponding to the selected slot.<BR><BR>
	 */
	public Henna getHenna(final int slot)
	{
		if(slot < 1 || slot > 3)
			return null;
		return _henna[slot - 1];
	}

	public int getHennaStatINT()
	{
		return _hennaINT;
	}

	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}

	public int getHennaStatCON()
	{
		return _hennaCON;
	}

	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}

	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}

	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}

	@Override
	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		if (getInventory().destroyItemByItemId(itemConsumeId, itemCount))
		{
			if (!ItemHolder.getInstance().getTemplate(itemConsumeId).isHideConsumeMessage())
				sendPacket(SystemMessage2.removeItems(itemConsumeId, itemCount));
			return true;
		}
		return false;
	}

	@Override
	public boolean consumeItemMp(int itemId, int mp)
	{
		for(ItemInstance item : getInventory().getPaperdollItems())
			if(item != null && item.getItemId() == itemId)
			{
				final int newMp = item.getLifeTime() - mp;
				if(newMp >= 0)
				{
					item.setLifeTime(newMp);
					sendPacket(new InventoryUpdate().addModifiedItem(item));
					return true;
				}
				break;
			}
		return false;
	}

	/**
	 * @return True if the L2Player is a Mage.<BR><BR>
	 */
	@Override
	public boolean isMageClass()
	{
		return _template.getBaseMAtk() > 3;
	}

	public boolean isMounted()
	{
		return _mountNpcId > 0;
	}

	public final boolean isRiding()
	{
		return _riding;
	}

	public final void setRiding(boolean mode)
	{
		_riding = mode;
	}

	/**
	 * Проверяет, можно ли приземлиться в этой зоне.
	 *
	 * @return можно ли приземлится
	 */
	public boolean checkLandingState()
	{
		if(isInZone(ZoneType.no_landing))
			return false;

		SiegeEvent siege = getEvent(SiegeEvent.class);
		if(siege != null)
		{
			Residence unit = siege.getResidence();
			if(unit != null && getClan() != null && isClanLeader() && (getClan().getCastle() == unit.getId() || getClan().getHasFortress() == unit.getId()))
				return true;
			return false;
		}

		return true;
	}

	public void setMount(int npcId, int obj_id, int level)
	{
		final PetData info = PetDataTable.getInstance().getInfo(npcId, level);
		if (info == null)
			return;
		
		if(isCursedWeaponEquipped())
			return;

		if (isActionBlocked(Zone.BLOCKED_ACTION_MOUNT))
		{
			sendMessage("You are not allowed to mount in this zone.");
			return;
		}
				
		switch(npcId)
		{
			case 0: // Dismount
				setFlying(false);
				setRiding(false);
				if(getTransformation() > 0)
					setTransformation(0);
				removeSkillById(Skill.SKILL_STRIDER_ASSAULT);
				removeSkillById(Skill.SKILL_WYVERN_BREATH);
				getEffectList().stopEffect(Skill.SKILL_HINDER_STRIDER);
				break;
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
			case PetDataTable.GUARDIANS_STRIDER_ID:
				setRiding(true);
				if(isNoble())
					addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_STRIDER_ASSAULT, 1), false);
				break;
			case PetDataTable.WYVERN_ID:
				setFlying(true);
				setLoc(getLoc().changeZ(32));
				addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_WYVERN_BREATH, 1), false);
				break;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
			case PetDataTable.LIGHT_PURPLE_MANED_HORSE_ID:
				setRiding(true);
				break;
			case PetDataTable.TAWNY_MANED_LION_ID:
				setRiding(true);
				break;
			case PetDataTable.STEAM_BEATLE_ID:
				setRiding(true);
				break;
			case PetDataTable.AURA_BIRD_FALCON_ID:
				setLoc(getLoc().changeZ(32));
				setFlying(true);
				setTransformation(8);
				break;
			case PetDataTable.AURA_BIRD_OWL_ID:
				setLoc(getLoc().changeZ(32));
				setFlying(true);
				setTransformation(9);
				break;
		}

		if(npcId > 0)
			unEquipWeapon();

		_mountNpcId = npcId;
		_mountObjId = obj_id;
		_mountLevel = level;

		broadcastUserInfo(true); // нужно послать пакет перед Ride для корректного снятия оружия с заточкой
		broadcastPacket(new Ride(this));
		broadcastUserInfo(true); // нужно послать пакет после Ride для корректного отображения скорости

		sendPacket(new SkillList(this));
	}

	public void dismount()
	{
		if (!isMounted())
			return;

		setFlying(false);
		setRiding(false);
		if(getTransformation() > 0)
			setTransformation(0);

		removeSkillById(Skill.SKILL_STRIDER_ASSAULT);
		removeSkillById(Skill.SKILL_WYVERN_BREATH);
		getEffectList().stopEffect(Skill.SKILL_HINDER_STRIDER);

		_mountNpcId = 0;
		_mountObjId = 0;
		_mountLevel = 0;

		broadcastPacket(new Ride(this));
		broadcastUserInfo(true);

		sendPacket(new SkillList(this));
	}
	
	public void unEquipWeapon()
	{
		if (NexusEvents.isInEvent(this))
			if (!NexusEvents.canBeDisarmed(this))
				return;
		
		ItemInstance wpn = getSecondaryWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		wpn = getActiveWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		abortAttack(true, true);
		abortCast(true, true);
	}

	@Override
	public int getSpeed(int baseSpeed)
	{
		if(isMounted())
		{
			PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
			int speed = 187;
			if(petData != null)
				speed = petData.getSpeed();
			double mod = 1.;
			int level = getLevel();
			if(_mountLevel > level && level - _mountLevel > 10)
				mod = 0.5; // Штраф на разницу уровней между игроком и петом
			baseSpeed = (int) (mod * speed);
		}
		return super.getSpeed(baseSpeed);
	}

	private int _mountNpcId;
	private int _mountObjId;
	private int _mountLevel;

	public int getMountNpcId()
	{
		return _mountNpcId;
	}

	public int getMountObjId()
	{
		return _mountObjId;
	}

	public int getMountLevel()
	{
		return _mountLevel;
	}

	public void sendDisarmMessage(ItemInstance wpn)
	{
		SystemMessage sm;
		if(wpn.getEnchantLevel() > 0)
		{
			sm = new SystemMessage(SystemMessage.EQUIPMENT_OF__S1_S2_HAS_BEEN_REMOVED);
			sm.addNumber(wpn.getEnchantLevel());
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
		else
		{
			sm = new SystemMessage(SystemMessage.S1__HAS_BEEN_DISARMED);
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
	}

	/**
	 * Устанавливает тип используемого склада.
	 *
	 * @param type тип склада:<BR>
	 *             <ul>
	 *             <li>WarehouseType.PRIVATE
	 *             <li>WarehouseType.CLAN
	 *             <li>WarehouseType.CASTLE
	 *             </ul>
	 */
	public void setUsingWarehouseType(final WarehouseType type)
	{
		_usingWHType = type;
	}

	/**
	 * Р’РѕР·РІСЂР°С‰Р°РµС‚ С‚РёРї РёСЃРїРѕР»СЊР·СѓРµРјРѕРіРѕ СЃРєР»Р°РґР°.
	 *
	 * @return null РёР»Рё С‚РёРї СЃРєР»Р°РґР°:<br>
	 *         <ul>
	 *         <li>WarehouseType.PRIVATE
	 *         <li>WarehouseType.CLAN
	 *         <li>WarehouseType.CASTLE
	 *         </ul>
	 */
	public WarehouseType getUsingWarehouseType()
	{
		return _usingWHType;
	}

	public Collection<EffectCubic> getCubics()
	{
		return _cubics == null ? Collections.<EffectCubic>emptyList() : _cubics.values();
	}

	public void addCubic(EffectCubic cubic)
	{
		if(_cubics == null)
			_cubics = new ConcurrentHashMap<Integer, EffectCubic>(3);
		_cubics.put(cubic.getId(), cubic);
	}

	public void removeCubic(int id)
	{
		if(_cubics != null)
			_cubics.remove(id);
	}

	public EffectCubic getCubic(int id)
	{
		return _cubics == null ? null : _cubics.get(id);
	}

	@Override
	public String toString()
	{
		return getName() + "[" + getObjectId() + "]";
	}

	/**
	 * @return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR><BR>
	 */
	public int getEnchantEffect()
	{
		final ItemInstance wpn = getActiveWeaponInstance();

		if(wpn == null)
			return 0;

		return Math.min(127, wpn.getOlyEnchantLevel());
	}

	/**
	 * Set the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public void setLastNpc(final NpcInstance npc)
	{
		if(npc == null)
			_lastNpc = HardReferences.emptyRef();
		else
			_lastNpc = npc.getRef();
	}

	/**
	 * @return the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public NpcInstance getLastNpc()
	{
		return _lastNpc.get();
	}

	public void setMultisell(MultiSellListContainer multisell)
	{
		_multisell = multisell;
	}

	public MultiSellListContainer getMultisell()
	{
		return _multisell;
	}

	/**
	 * @return True if L2Player is a participant in the Festival of Darkness.<BR><BR>
	 */
	public boolean isFestivalParticipant()
	{
		return getReflection() instanceof DarknessFestival;
	}

	@Override
	public boolean unChargeShots(boolean spirit)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;

		if(spirit)
			weapon.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
		else
			weapon.setChargedSoulshot(ItemInstance.CHARGED_NONE);

		autoShot();
		return true;
	}

	public boolean unChargeFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if (weapon == null)
			return false;
		weapon.setChargedFishshot(false);
		autoShot();
		return true;
	}

	public void autoShot()
	{
		for(Integer shotId : _activeSoulShots)
		{
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if(item == null)
			{
				removeAutoSoulShot(shotId);
				continue;
			}
			
			IItemHandler handler = item.getTemplate().getHandler();
			if(handler == null)
				continue;
			handler.useItem(this, item, false);
		}
	}
	
	public void autoPotions()
	{
		for(Integer potionId : _autoPotions)
		{
			ItemInstance item = getInventory().getItemByItemId(potionId);
			if(item == null)
			{
				removeAutoPotion(potionId);
				stopAutoPotionTask(potionId);
				continue;
			}
			
			int realId = item.getdisplayId();
			String itemName = ItemHolder.getInstance().getTemplateName(potionId);
			sendPacket(new ExAutoSoulShot(realId, true));
			startAutoPotionTask(potionId);
			sendMessage(itemName + " has been Enabled.");
		}
	}

	public void startAutoPotionTask(int itemId)
	{
		stopAutoPotionTask(itemId);
		switch(itemId)
		{
			case 22410:
			case 22411:
				_autoCPTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoPotionTask(this, itemId), 2000, (long) (Config.AUTO_CP_POTION_DELAY * 1000));
				break;
			case 22412:
			case 22413:
				_autoHPTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoPotionTask(this, itemId), 2000, (long) (Config.AUTO_HP_POTION_DELAY * 1000));
				break;
			case 22414:
			case 22415:
				_autoMPTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoPotionTask(this, itemId), 2000, (long) (Config.AUTO_MP_POTION_DELAY * 1000));
				break;
		}
	}
	
	public void stopAutoPotionTask(int itemId)
	{
		switch(itemId)
		{
			case -1:
			{
				if(_autoCPTask != null)
				{
					_autoCPTask.cancel(false);
					_autoCPTask = null;
				}
				if(_autoHPTask != null)
				{
					_autoHPTask.cancel(false);
					_autoHPTask = null;
				}
				if(_autoMPTask != null)
				{
					_autoMPTask.cancel(false);
					_autoMPTask = null;
				}
				break;
			}
			case 22410:
			case 22411:
				if(_autoCPTask != null)
				{
					_autoCPTask.cancel(false);
					_autoCPTask = null;
				}
				break;
			case 22412:
			case 22413:
				if(_autoHPTask != null)
				{
					_autoHPTask.cancel(false);
					_autoHPTask = null;
				}
				break;
			case 22414:
			case 22415:
				if(_autoMPTask != null)
				{
					_autoMPTask.cancel(false);
					_autoMPTask = null;
				}
				break;
		}
	}
	
	public boolean getChargedFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedFishshot();
	}

	@Override
	public boolean getChargedSoulShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedSoulshot() == ItemInstance.CHARGED_SOULSHOT;
	}

	@Override
	public int getChargedSpiritShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return 0;
		return weapon.getChargedSpiritshot();
	}

	public void addAutoSoulShot(Integer itemId)
	{
		_activeSoulShots.add(itemId);
	}

	public void removeAutoSoulShot(Integer itemId)
	{
		_activeSoulShots.remove(itemId);
	}

	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}

	public void setAutoPotion(Integer itemId)
	{
		_autoPotions.add(itemId);
	}

	public void removeAutoPotion(Integer itemId)
	{
		_autoPotions.remove(itemId);
	}

	public Set<Integer> getAutoPotion()
	{
		return _autoPotions;
	}
	
	public int getClanPrivileges()
	{
		if(_clan == null)
			return 0;
		if(isClanLeader())
			return Clan.CP_ALL;
		if(_powerGrade < 1 || _powerGrade > 9)
			return 0;
		RankPrivs privs = _clan.getRankPrivs(_powerGrade);
		if(privs != null)
			return privs.getPrivs();
		return 0;
	}

	public void teleToClosestTown()
	{
		teleToLocation(Location.getRestartLocation(this, RestartType.TO_VILLAGE), ReflectionManager.DEFAULT);
	}

	public void teleToCastle()
	{
		teleToLocation(Location.getRestartLocation(this, RestartType.TO_CASTLE), ReflectionManager.DEFAULT);
	}

	public void teleToFortress()
	{
		teleToLocation(Location.getRestartLocation(this, RestartType.TO_FORTRESS), ReflectionManager.DEFAULT);
	}

	public void teleToClanhall()
	{
		teleToLocation(Location.getRestartLocation(this, RestartType.TO_CLANHALL), ReflectionManager.DEFAULT);
	}

	@Override
	public void sendMessage(CustomMessage message)
	{
		sendMessage(message.toString());
	}

	@Override
	public void sendChatMessage(int objectId, int messageType, String charName, String text)
	{
		sendPacket(new CreatureSay(objectId, messageType, charName, text));
	}
	
	@Override
	public void teleToLocation(int x, int y, int z, int refId)
	{
		if(isDeleted())
			return;

		super.teleToLocation(x, y, z, refId);
	}

	@Override
	public void teleToLocation(Location loc)
	{
		teleToLocation(loc.x, loc.y, loc.z, getReflection());
	}

	@Override
	public void teleToLocation(Location loc, int refId)
	{
		teleToLocation(loc.x, loc.y, loc.z, refId);
	}

	@Override
	public void teleToLocation(Location loc, Reflection r)
	{
		teleToLocation(loc.x, loc.y, loc.z, r);
	}

	@Override
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getReflection());
	}
	
	@Override
	public boolean onTeleported()
	{
		if (!super.onTeleported())
			return false;
		
		if(isFakeDeath())
			breakFakeDeath();

		if(isInBoat())
			setLoc(getBoat().getLoc());

		// 15 секунд после телепорта на персонажа не агрятся мобы
		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);

		spawnMe();

		setLastClientPosition(getLoc());
		setLastServerPosition(getLoc());
		
		if(isPendingRevive())
			doRevive();

		sendActionFailed();

		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);

		if(isLockedTarget() && getTarget() != null)
			sendPacket(new MyTargetSelected(getTarget().getObjectId(), 0));

		sendUserInfo(true);
		if(getPet() != null)
			getPet().teleportToOwner();

		return true;
	}

	public boolean enterObserverMode(Location loc)
	{
		WorldRegion observerRegion = World.getRegion(loc);
		if(observerRegion == null)
			return false;
		if (!_observerMode.compareAndSet(OBSERVER_NONE, OBSERVER_STARTING))
			return false;

		setTarget(null);
		stopMove();
		sitDown(null);
		setFlying(true);

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this);

		setObserverRegion(observerRegion);

		// Отображаем надпись над головой
		broadcastCharInfo();

		// Переходим в режим обсервинга
		sendPacket(new ObserverStart(loc));

		return true;
	}

	public void appearObserverMode()
	{
		if (!_observerMode.compareAndSet(OBSERVER_STARTING, OBSERVER_STARTED))
				return;

		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();

		// Добавляем фэйк в точку наблюдения
		if(!observerRegion.equals(currentRegion))
			observerRegion.addObject(this);

		World.showObjectsToPlayer(this);

		OlympiadGame game = getOlympiadObserveGame();
		if(game != null)
		{
			game.addSpectator(this);
			game.broadcastInfo(null, this, true);
		}
	}

	public void leaveObserverMode()
	{
		if (!_observerMode.compareAndSet(OBSERVER_STARTED, OBSERVER_LEAVING))
			return;

		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();
		// Убираем фэйк в точке наблюдения
		if(!observerRegion.equals(currentRegion))
			observerRegion.removeObject(this);

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this);

		setObserverRegion(null);

		setTarget(null);
		stopMove();

		// Выходим из режима обсервинга
		sendPacket(new ObserverEnd(getLoc()));
	}

	public void returnFromObserverMode()
	{
		if (!_observerMode.compareAndSet(OBSERVER_LEAVING, OBSERVER_NONE))
		{
			_log.warn("Return from observ mode " + getName() + ". ObservMode ID: " + _observerMode.get());
			//Thread.dumpStack();
			return;
		}

		// Нужно при телепорте с более высокой точки на более низкую, иначе наносится вред от "падения"
		setLastClientPosition(null);
		setLastServerPosition(null);

		unblock();
		standUp();
		setFlying(false);

		broadcastCharInfo();

		World.showObjectsToPlayer(this);
	}

	public void enterOlympiadObserverMode(Location loc, OlympiadGame game, Reflection reflect)
	{
		WorldRegion observerRegion = World.getRegion(loc);
		WorldRegion oldObserver = getObserverRegion();

		if(observerRegion == null)
			return;

		OlympiadGame oldGame = getOlympiadObserveGame();
		if (!_observerMode.compareAndSet(oldGame != null ? OBSERVER_STARTED : OBSERVER_NONE, OBSERVER_STARTING))
		{
			return;
		}

		setTarget(null);
		stopMove();

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this);
		setObserverRegion(observerRegion);

		if (oldGame != null)
		{
			if (isInObserverMode() && oldObserver != null)
				oldObserver.removeObject(this);
			
			oldGame.removeSpectator(this);
			sendPacket(ExOlympiadMatchEnd.STATIC);
		}
		else
		{
			block();

			// Отображаем надпись над головой
			broadcastCharInfo();

			// Меняем интерфейс
			sendPacket(new ExOlympiadMode(3));
		}

		setOlympiadObserveGame(game);

		// "Телепортируемся"
		setReflection(reflect);
		sendPacket(new TeleportToLocation(this, loc));
	}

	public void leaveOlympiadObserverMode(boolean removeFromGame)
	{
		OlympiadGame game = getOlympiadObserveGame();
		
		if (game == null)
			return;
		
		if (!_observerMode.compareAndSet(OBSERVER_STARTED, OBSERVER_LEAVING))
		{
			_log.warn("Leave observ mode " + getName() + ". ObservMode ID: " + _observerMode.get());
			//Thread.dumpStack();
			return;
		}

		if (removeFromGame)
			game.removeSpectator(this);
		setOlympiadObserveGame(null);

		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();

		// Убираем фэйк в точке наблюдения
		if(observerRegion != null && currentRegion != null && !observerRegion.equals(currentRegion))
			observerRegion.removeObject(this);

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this);

		setObserverRegion(null);

		setTarget(null);
		stopMove();

		// Меняем интерфейс
		sendPacket(new ExOlympiadMode(0));
		sendPacket(ExOlympiadMatchEnd.STATIC);

		setReflection(ReflectionManager.DEFAULT);
		// "Телепортируемся"
		sendPacket(new TeleportToLocation(this, getLoc()));
	}

	public void setOlympiadSide(final int i)
	{
		_olympiadSide = i;
	}

	public int getOlympiadSide()
	{
		return _olympiadSide;
	}

	@Override
	public boolean isInObserverMode()
	{
		return _observerMode.get() > 0;
	}

	public int getObserverMode()
	{
		return _observerMode.get();
	}

	public WorldRegion getObserverRegion()
	{
		return _observerRegion;
	}

	public void setObserverRegion(WorldRegion region)
	{
		_observerRegion = region;
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(final int mode)
	{
		_telemode = mode;
	}

	public void setLoto(final int i, final int val)
	{
		_loto[i] = val;
	}

	public int getLoto(final int i)
	{
		return _loto[i];
	}

	public void setRace(final int i, final int val)
	{
		_race[i] = val;
	}

	public int getRace(final int i)
	{
		return _race[i];
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(final boolean mode)
	{
		_messageRefusal = mode;
		sendPacket(new EtcStatusUpdate(this));
		
		if (mode)
			_acceptedPMs.clear();
	}

	public void setTradeRefusal(final boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public void addToBlockList(final String charName)
	{
		if(charName == null || charName.equalsIgnoreCase(getName()) || isInBlockList(charName))
		{
			// уже в списке
			sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}

		Player block_target = World.getPlayer(charName);

		if(block_target != null)
		{
			if(block_target.isGM())
			{
				sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
				return;
			}
			_blockList.put(block_target.getObjectId(), block_target.getName());
			sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST).addString(block_target.getName()));
			block_target.sendPacket(new SystemMessage(SystemMessage.S1__HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST).addString(getName()));
			return;
		}

		int charId = CharacterDAO.getInstance().getObjectIdByName(charName);

		if(charId == 0)
		{
			// чар не существует
			sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}

		/*if(Config.gmlist.containsKey() && Config.gmlist.get(charId).IsGM) //TODO FIXXX
		{
			sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
			return;
		}*/
		_blockList.put(charId, charName);
		sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST).addString(charName));
	}

	public void removeFromBlockList(final String charName)
	{
		int charId = 0;
		for(int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
			{
				charId = blockId;
				break;
			}
		if(charId == 0)
		{
			sendPacket(Msg.YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_FROM_IGNORE_LIST);
			return;
		}
		sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST).addString(_blockList.remove(charId)));
		Player block_target = GameObjectsStorage.getPlayer(charId);
		if(block_target != null)
			block_target.sendMessage(block_target.isLangRus() ? getName() + " удалил вас из его/ее черный список." : getName() + " has removed you from his/her Ignore List."); //В системных(619 == 620) мессагах ошибка ;)
	}

	public boolean isInBlockList(final Player player)
	{
		return isInBlockList(player.getObjectId());
	}

	public boolean isInBlockList(final int charId)
	{
		return _blockList != null && _blockList.containsKey(charId);
	}

	public boolean isInBlockList(final String charName)
	{
		for(int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
				return true;
		return false;
	}

	private void restoreBlockList()
	{
		_blockList.clear();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT target_Id, char_name FROM character_blocklist LEFT JOIN characters ON ( character_blocklist.target_Id = characters.obj_Id ) WHERE character_blocklist.obj_Id = ?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				int targetId = rs.getInt("target_Id");
				String name = rs.getString("char_name");
				if(name == null)
					continue;
				_blockList.put(targetId, name);
			}
		}
		catch(SQLException e)
		{
			_log.warn("Can't restore player blocklist " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	private void storeBlockList()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_blocklist WHERE obj_Id=" + getObjectId());

			if(_blockList.isEmpty())
				return;

			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_blocklist` (`obj_Id`,`target_Id`) VALUES");

			synchronized(_blockList)
			{
				StringBuilder sb;
				for(Entry<Integer, String> e : _blockList.entrySet())
				{
					sb = new StringBuilder("(");
					sb.append(getObjectId()).append(",");
					sb.append(e.getKey()).append(")");
					b.write(sb.toString());
				}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			_log.warn("Can't store player blocklist " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isBlockAll()
	{
		return _blockAll;
	}

	public void setBlockAll(final boolean state)
	{
		_blockAll = state;
		if (state)
			_acceptedPMs.clear();
	}

	public Collection<String> getBlockList()
	{
		return _blockList.values();
	}

	public Map<Integer, String> getBlockListMap()
	{
		return _blockList;
	}

	public void setHero(final boolean hero)
	{
		_hero = hero;
	}

	@Override
	public boolean isHero()
	{
		return _hero;
	}

	public void setHero(Player player)
	{
		StatsSet hero = new StatsSet();
		hero.set(Olympiad.CLASS_ID, player.getBaseClassId());
		hero.set(Olympiad.CHAR_ID, player.getObjectId());
		hero.set(Olympiad.CHAR_NAME, player.getName());
		hero.set(Hero.ACTIVE, 1);

		List<StatsSet> heroesToBe = new ArrayList<StatsSet>();
		heroesToBe.add(hero);

		Hero.getInstance().computeNewHeroes(heroesToBe);
		player.setHero(true);
		Hero.addSkills(player);
		player.updatePledgeClass();
		if(player.isHero())
			player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
		player.broadcastUserInfo(true);
	}
	
	public void setIsInOlympiadMode(final boolean b)
	{
		_inOlympiadMode = b;
	}

	@Override
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public boolean isOlympiadGameStart()
	{
		return _olympiadGame != null && _olympiadGame.getState() == 1;
	}

	public boolean isOlympiadCompStart()
	{
		return _olympiadGame != null && _olympiadGame.getState() == 2;
	}

	public void updateNobleSkills()
	{
		if(isNoble())
		{
			if(isClanLeader() && getClan().getCastle() > 0)
				super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_WYVERN_AEGIS, 1));
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_NOBLESSE_BLESSING, 1));
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_SUMMON_CP_POTION, 1));
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_FORTUNE_OF_NOBLESSE, 1));
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_HARMONY_OF_NOBLESSE, 1));
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_SYMPHONY_OF_NOBLESSE, 1));
		}
		else
		{
			super.removeSkillById(Skill.SKILL_WYVERN_AEGIS);
			super.removeSkillById(Skill.SKILL_NOBLESSE_BLESSING);
			super.removeSkillById(Skill.SKILL_SUMMON_CP_POTION);
			super.removeSkillById(Skill.SKILL_FORTUNE_OF_NOBLESSE);
			super.removeSkillById(Skill.SKILL_HARMONY_OF_NOBLESSE);
			super.removeSkillById(Skill.SKILL_SYMPHONY_OF_NOBLESSE);
		}
	}

	public void setNoble(boolean noble)
	{
		if(noble)
			broadcastPacket(new MagicSkillUse(this, this, 6673, 1, 1000, 0));
		_noble = noble;
	}

	public void setNoble(boolean noble, boolean animation)
	{
		if(animation) // without broadcast of animation.
			broadcastPacket(new MagicSkillUse(this, this, 6673, 1, 1000, 0));
		
		_noble = noble;
	}
	
	public boolean isNoble()
	{
		return _noble;
	}

	public int getSubLevel()
	{
		return isSubClassActive() ? getLevel() : 0;
	}

	/* varka silenos and ketra orc quests related functions */
	public void updateKetraVarka()
	{
		if(ItemFunctions.getItemCount(this, 7215) > 0)
			_ketra = 5;
		else if(ItemFunctions.getItemCount(this, 7214) > 0)
			_ketra = 4;
		else if(ItemFunctions.getItemCount(this, 7213) > 0)
			_ketra = 3;
		else if(ItemFunctions.getItemCount(this, 7212) > 0)
			_ketra = 2;
		else if(ItemFunctions.getItemCount(this, 7211) > 0)
			_ketra = 1;
		else if(ItemFunctions.getItemCount(this, 7225) > 0)
			_varka = 5;
		else if(ItemFunctions.getItemCount(this, 7224) > 0)
			_varka = 4;
		else if(ItemFunctions.getItemCount(this, 7223) > 0)
			_varka = 3;
		else if(ItemFunctions.getItemCount(this, 7222) > 0)
			_varka = 2;
		else if(ItemFunctions.getItemCount(this, 7221) > 0)
			_varka = 1;
		else
		{
			_varka = 0;
			_ketra = 0;
		}
	}

	public int getVarka()
	{
		return _varka;
	}

	public int getKetra()
	{
		return _ketra;
	}

	public void updateRam()
	{
		if(ItemFunctions.getItemCount(this, 7247) > 0)
			_ram = 2;
		else if(ItemFunctions.getItemCount(this, 7246) > 0)
			_ram = 1;
		else
			_ram = 0;
	}

	public int getRam()
	{
		return _ram;
	}

	public void setPledgeType(final int typeId)
	{
		_pledgeType = typeId;
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}

	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}

	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}

	public int getPledgeClass()
	{
		return _pledgeClass;
	}

	public int getPledgeItemId()
	{
		return _pledgeItemId;
	}
	
	public void setPledgeItemId(int itemId)
	{
		_pledgeItemId = itemId;
	}
	
	public void setPledgePrice(long price)
	{
		_pledgePrice = price;
	}
	
	public long getPledgePrice()
	{
		return _pledgePrice;
	}
	
	public void setSearchforAcademy(boolean search)
	{
		_isInAcademyList = search;
	}
	
	public boolean isInSearchOfAcademy()
	{
		return _isInAcademyList;
	}
	  
	public void updatePledgeClass()
	{
		int clanLevel = _clan == null ? -1 : _clan.getLevel();
		boolean inAcademy = _clan != null && Clan.isAcademy(_pledgeType);
		boolean isGuard = _clan != null && Clan.isRoyalGuard(_pledgeType);
		boolean isKnight = _clan != null && Clan.isOrderOfKnights(_pledgeType);

		boolean isGuardCaptain = false, isKnightCommander = false, isLeader = false;

		SubUnit unit = getSubUnit();
		if(unit != null)
		{
			UnitMember unitMember = unit.getUnitMember(getObjectId());
			if(unitMember == null)
			{
				_log.warn("Player: unitMember null, clan: " + _clan.getClanId() + "; pledgeType: " + unit.getType());
				return;
			}
			isGuardCaptain = Clan.isRoyalGuard(unitMember.getLeaderOf());
			isKnightCommander = Clan.isOrderOfKnights(unitMember.getLeaderOf());
			isLeader = unitMember.getLeaderOf() == Clan.SUBUNIT_MAIN_CLAN;
		}

		switch (clanLevel)
		{
			case -1:
				_pledgeClass = RANK_VAGABOND;
				break;
			case 0:
			case 1:
			case 2:
			case 3:
				if (isLeader)
					_pledgeClass = RANK_HEIR;
				else
					_pledgeClass = RANK_VASSAL;
				break;
			case 4:
				if (isLeader)
					_pledgeClass = RANK_KNIGHT;
				else
					_pledgeClass = RANK_HEIR;
				break;
			case 5:
				if (isLeader)
					_pledgeClass = RANK_WISEMAN;
				else if (inAcademy)
					_pledgeClass = RANK_VASSAL;
				else
					_pledgeClass = RANK_HEIR;
				break;
			case 6:
				if (isLeader)
					_pledgeClass = RANK_BARON;
				else if (inAcademy)
					_pledgeClass = RANK_VASSAL;
				else if (isGuardCaptain)
					_pledgeClass = RANK_WISEMAN;
				else if (isGuard)
					_pledgeClass = RANK_HEIR;
				else
					_pledgeClass = RANK_KNIGHT;
				break;
			case 7:
				if (isLeader)
					_pledgeClass = RANK_COUNT;
				else if (inAcademy)
					_pledgeClass = RANK_VASSAL;
				else if (isGuardCaptain)
					_pledgeClass = RANK_VISCOUNT;
				else if (isGuard)
					_pledgeClass = RANK_KNIGHT;
				else if (isKnightCommander)
					_pledgeClass = RANK_BARON;
				else if (isKnight)
					_pledgeClass = RANK_HEIR;
				else
					_pledgeClass = RANK_WISEMAN;
				break;
			case 8:
				if (isLeader)
					_pledgeClass = RANK_MARQUIS;
				else if (inAcademy)
					_pledgeClass = RANK_VASSAL;
				else if (isGuardCaptain)
					_pledgeClass = RANK_COUNT;
				else if (isGuard)
					_pledgeClass = RANK_WISEMAN;
				else if (isKnightCommander)
					_pledgeClass = RANK_VISCOUNT;
				else if (isKnight)
					_pledgeClass = RANK_KNIGHT;
				else
					_pledgeClass = RANK_BARON;
				break;
			case 9:
				if (isLeader)
					_pledgeClass = RANK_DUKE;
				else if (inAcademy)
					_pledgeClass = RANK_VASSAL;
				else if (isGuardCaptain)
					_pledgeClass = RANK_MARQUIS;
				else if (isGuard)
					_pledgeClass = RANK_BARON;
				else if (isKnightCommander)
					_pledgeClass = RANK_COUNT;
				else if (isKnight)
					_pledgeClass = RANK_WISEMAN;
				else
					_pledgeClass = RANK_VISCOUNT;
				break;
			case 10:
				if (isLeader)
					_pledgeClass = RANK_GRAND_DUKE;
				else if (inAcademy)
					_pledgeClass = RANK_VASSAL;
				else if (isGuard)
					_pledgeClass = RANK_VISCOUNT;
				else if (isKnight)
					_pledgeClass = RANK_BARON;
				else if (isGuardCaptain)
					_pledgeClass = RANK_DUKE;
				else if (isKnightCommander)
					_pledgeClass = RANK_MARQUIS;
				else
					_pledgeClass = RANK_COUNT;
				break;
			case 11:
				if (isLeader)
					_pledgeClass = RANK_DISTINGUISHED_KING;
				else if (inAcademy)
					_pledgeClass = RANK_VASSAL;
				else if (isGuard)
					_pledgeClass = RANK_COUNT;
				else if (isKnight)
					_pledgeClass = RANK_VISCOUNT;
				else if (isGuardCaptain)
					_pledgeClass = RANK_GRAND_DUKE;
				else if (isKnightCommander)
					_pledgeClass = RANK_DUKE;
				else
					_pledgeClass = RANK_MARQUIS;
				break;
		}
		
		if (_hero && _pledgeClass < RANK_MARQUIS)
			_pledgeClass = RANK_MARQUIS;
		else if (_noble && _pledgeClass < RANK_BARON)
			_pledgeClass = RANK_BARON;
	}

	public void setPowerGrade(final int grade)
	{
		_powerGrade = grade;
	}

	public int getPowerGrade()
	{
		return _powerGrade;
	}

	public void setApprentice(final int apprentice)
	{
		_apprentice = apprentice;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public int getSponsor()
	{
		return _clan == null ? 0 : _clan.getAnyMember(getObjectId()).getSponsor();
	}

	public int getNameColor()
	{
		if(isInObserverMode())
			return Color.black.getRGB();

		return _nameColor;
	}

	public void setNameColor(final int nameColor, boolean store)
	{
		if(nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR && nameColor != Config.OFFLINE_BUFFER_NAME_COLOR && store)
			setVar("namecolor", Integer.toHexString(nameColor), -1);
		else if(nameColor == Config.NORMAL_NAME_COLOUR)
			unsetVar("namecolor");
		_nameColor = nameColor;
	}
	public void setNameColor(final int nameColor)
	{
		if(nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR && nameColor != Config.OFFLINE_BUFFER_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(nameColor), -1);
		else if(nameColor == Config.NORMAL_NAME_COLOUR)
			unsetVar("namecolor");
		_nameColor = nameColor;
	}
	

	public void setNameColor(final int red, final int green, final int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
		if(_nameColor != Config.NORMAL_NAME_COLOUR && _nameColor != Config.CLANLEADER_NAME_COLOUR && _nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR && _nameColor != Config.OFFLINE_BUFFER_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(_nameColor), -1);
		else
			unsetVar("namecolor");
	}

	/**
	 * Меняет цвет ника.
	 * @param RGB - цвет в виде RGB (rrggbb).
	 */
	public void setNameColor(String RGB)
	{
		if (RGB.length()==6)
		{
			RGB = RGB.substring(4,6) + RGB.substring(2,4) + RGB.substring(0,2);
		}
		setNameColor(Integer.decode("0x" + RGB));
	}

	
	public double getCollisionRadius()
	{
		return _collision_radius;
	}
	
	public double getCollisionHeight()
	{
		return _collision_height;
	}
	
	private final Map<String, PlayerVar> user_variables = new ConcurrentHashMap<String, PlayerVar>();

	public static void setVarOffline(int playerObjId, String name, String value, long expireDate)
	{
		Player player = World.getPlayer(playerObjId);
		if (player != null)
			player.setVar(name, value, expireDate);
		else
			mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var',?,?,?)", playerObjId, name, value, expireDate);
	}
	
	public static void setVarOffline(int playerObjId, String name, int value, long expireDate)
	{
		setVarOffline(playerObjId, name, String.valueOf(value), expireDate);
	}
	
	public static void setVarOffline(int playerObjId, String name, int value)
	{
		setVarOffline(playerObjId, name, String.valueOf(value), -1);
	}
	
	public static void setVarOffline(int playerObjId, String name, long value, long expireDate)
	{
		setVarOffline(playerObjId, name, String.valueOf(value), expireDate);
	}
	
	public static void setVarOffline(int playerObjId, String name, long value)
	{
		setVarOffline(playerObjId, name, String.valueOf(value), -1);
	}
	
	public static void unsetVarOffline(int playerObjId, String name)
	{
		Player player = World.getPlayer(playerObjId);
		if (player != null)
			player.unsetVar(name);
		else
			mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", playerObjId, name);
	}
	
	public void setVar(String name, String value, long expirationTimeMilis)
	{
		if(user_variables.containsKey(name))
		{
			getVarObject(name).stopExpireTask();
		}

		user_variables.put(name, new PlayerVar(this, name, value, expirationTimeMilis));
		mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var',?,?,?)", getObjectId(), name, value, expirationTimeMilis);
	}
	
	public void setVar(String name, String value)
	{
		setVar(name, value, -1);
	}

	public void setVar(String name, int value, long expirationTimeMilis)
	{
		setVar(name, String.valueOf(value), expirationTimeMilis);
	}
	
	public void setVar(String name, int value)
	{
		setVar(name, String.valueOf(value), -1);
	}

	public void setVar(String name, long value, long expirationTimeMilis)
	{
		setVar(name, String.valueOf(value), expirationTimeMilis);
	}
	
	public void setVar(String name, long value)
	{
		setVar(name, String.valueOf(value), -1);
	}

	public void unsetVar(String name)
	{
		if(name == null)
			return;

		if (isPhantom())
			return;
		
		PlayerVar pv = user_variables.remove(name);

		if(pv != null)
		{
			pv.stopExpireTask();
			if (name.equalsIgnoreCase("OVRD_COND_" + getObjectId()))
				setOverrideCond(0);
			
			mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", getObjectId(), name);
		}
	}

	public String getVar(String name)
	{
		PlayerVar pv = getVarObject(name);

		if(pv == null)
			return null;

		return pv.getValue();
	}

	public long getVarTimeToExpire(String name)
	{
		try
		{
			return getVarObject(name).getTimeToExpire();
		}
		catch (NullPointerException npe)
		{}

		return 0;
	}
	
	public PlayerVar getVarObject(String name)
	{
		return user_variables.get(name);
	}
	
	public boolean getVarB(String name, boolean defaultVal)
	{
		PlayerVar pv = getVarObject(name);

		if(pv == null)
		{
			return defaultVal;
		}

		return pv.getValueBoolean();
	}
	
	public boolean getVarB(String name)
	{
		return getVarB(name, false);
	}

	public long getVarLong(String name)
	{
		return getVarLong(name, 0L);
	}

	public long getVarLong(String name, long defaultVal)
	{
		long result = defaultVal;
		String var = getVar(name);
		if(var != null)
			result = Long.parseLong(var);
		return result;
	}

	public int getVarInt(String name)
	{
		return getVarInt(name, 0);
	}

	public int getVarInt(String name, int defaultVal)
	{
		int result = defaultVal;
		String var = getVar(name);
		if(var != null)
			result = Integer.parseInt(var);
		return result;
	}

	public String getVar(String name, String defaultVal)
	{
		String var = getVar(name);
		if(var == null)
			return defaultVal;
		
		return var;
	}
	
	public Map<String, PlayerVar> getVars()
	{
		return user_variables;
	}

	private void loadVariables()
	{
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM character_variables WHERE obj_id = ?");
			offline.setInt(1, getObjectId());
			rs = offline.executeQuery();

			while(rs.next())
			{
				String name = rs.getString("name");
				String value = Strings.stripSlashes(rs.getString("value"));
				long expire_time = rs.getLong("expire_time");
				long curtime = System.currentTimeMillis();

				if(expire_time <= curtime && expire_time > 0)
				{
					continue;
				}

				user_variables.put(name, new PlayerVar(this, name, value, expire_time));
			}
			
			long pcCondOverride = getVarLong("OVRD_COND_" + getObjectId(), 0);
			setOverrideCond(pcCondOverride);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
	}

	public static String getVarFromPlayer(int objId, String var)
	{
		String value = null;
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT value FROM character_variables WHERE obj_id = ? AND name = ?");
			offline.setInt(1, objId);
			offline.setString(2, var);
			rs = offline.executeQuery();
			if(rs.next())
				value = Strings.stripSlashes(rs.getString("value"));
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
		return value;
	}

	public void setLanguage(Language lang)
	{
		if (lang == null)
			lang = Config.DEFAULT_LANG;
		
		setVar("lang@", lang.toString(), -1);
	}
	
	public Language getLanguage()
	{
		Player player = getPlayer();
		
		if (player == null)
			return Language.ENGLISH;
		
		if (player != null && player.isPhantom())
			return Language.ENGLISH;
		
		String lang = getVar("lang@");
		
		if (Config.USE_CLIENT_LANG && getClient() != null)
			lang = getClient().getLanguage().toString();
		else if (lang == null)
		{
			lang = Config.DEFAULT_LANG.toString();
			setVar("lang@", Config.DEFAULT_LANG.toString(), -1);
			return Config.DEFAULT_LANG;
		}
		
		if (lang.equalsIgnoreCase("ENGLISH") || lang.equalsIgnoreCase("en"))
			return Language.ENGLISH;
		
		if (lang.equalsIgnoreCase("RUSSIAN") || lang.equalsIgnoreCase("ru"))
			return Language.RUSSIAN;
		
		return Language.ENGLISH;
	}

	public boolean isLangRus()
	{
		return getLanguage() == Language.RUSSIAN;
	}

	public boolean isLangEng()
	{
		return getLanguage() == Language.ENGLISH;
	}
	
	public boolean isAtWarWith(final int id)
	{
		return _clan != null && _clan.isAtWarWith(id);
	}

	public void stopWaterTask()
	{
		if(_taskWater != null)
		{
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGauge(this, SetupGauge.CYAN, 0));
			sendChanges();
		}
	}

	public void startWaterTask()
	{
		if(isDead())
			stopWaterTask();
		else if(Config.ALLOW_WATER && _taskWater == null)
		{
			int timeinwater = (int) (calcStat(Stats.BREATH, 86, null, null) * 1000L);
			sendPacket(new SetupGauge(this, SetupGauge.CYAN, timeinwater));
			if(getTransformation() > 0 && getTransformationTemplate() > 0 && !isCursedWeaponEquipped())
				setTransformation(0);
			_taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WaterTask(this), timeinwater, 1000L);
			sendChanges();
		}
	}
	
	public void doRevive(double percent)
	{
		restoreExp(percent);
		doRevive();
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		
		setAgathionRes(false);
		unsetVar("lostexp");
		updateEffectIcons();
		autoShot();
	}

	public void reviveRequest(Player reviver, double percent, boolean pet)
	{
		ReviveAnswerListener reviveAsk = _askDialog != null && _askDialog.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener)_askDialog.getValue() : null;
		if(reviveAsk != null)
		{
			if(reviveAsk.isForPet() == pet && reviveAsk.getPower() >= percent)
			{
				reviver.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
				return;
			}
			if(pet && !reviveAsk.isForPet())
			{
				reviver.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
				return;
			}
			if(pet && isDead())
			{
				reviver.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
				return;
			}
		}

		if(pet && getPet() != null && getPet().isDead() || !pet && isDead())
		{
			ConfirmDlg pkt = new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0);
			pkt.addName(reviver).addString(Math.round(percent) + " percent");

			ask(pkt, new ReviveAnswerListener(this, percent, pet, 0));
		}
		
		checkAndAddRessurectionStack();
		
		if (Config.ENABLE_PLAYER_COUNTERS && !isDualbox(reviver))
			reviver.getCounters().addPoint("_Players_Ressurected");
	}

	public void summonCharacterRequest(final Player summoner, final Location loc, final int summonConsumeCrystal)
	{
		ConfirmDlg cd = new ConfirmDlg(SystemMsg.C1_WISHES_TO_SUMMON_YOU_FROM_S2, 30000);
		cd.addName(summoner).addZoneName(loc);

		ask(cd, new SummonAnswerListener(summoner, this, loc, summonConsumeCrystal, 30000));
	}

	public void scriptRequest(String text, String scriptName, Object[] args)
	{
		ask(new ConfirmDlg(SystemMsg.S1, 30000).addString(text), new ScriptAnswerListener(this, scriptName, args));
	}

	public void updateNoChannel(final long time)
	{
		setNoChannel(time);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			final String stmt = "UPDATE characters SET nochannel = ? WHERE obj_Id=?";
			statement = con.prepareStatement(stmt);
			statement.setLong(1, _NoChannel > 0 ? _NoChannel / 1000 : _NoChannel);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.warn("Could not activate nochannel:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		sendPacket(new EtcStatusUpdate(this));
	}

	private void checkRecom()
	{
		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.HOUR_OF_DAY, 6);
		temp.set(Calendar.MINUTE, 30);
		temp.set(Calendar.SECOND, 0);
		temp.set(Calendar.MILLISECOND, 0);
		long count = Math.round((System.currentTimeMillis() / 1000 - _lastAccess) / 86400);
		if(count == 0 && _lastAccess < temp.getTimeInMillis() / 1000 && System.currentTimeMillis() > temp.getTimeInMillis())
			count++;

		for(int i = 1; i < count; i++)
			setRecomHave(getRecomHave() - 20);

		if(count > 0)
			restartRecom();
	}

	public void restartRecom()
	{
		setRecomBonusTime(3600);
		setRecomLeftToday(0);
		setRecomLeft(20);
		setRecomHave(getRecomHave() - 20);
		stopRecomBonusTask(false);
		startRecomBonusTask();
		sendUserInfo(true);
		sendVoteSystemInfo();
	}

	@Override
	public boolean isInBoat()
	{
		return _boat != null;
	}

	public Boat getBoat()
	{
		return _boat;
	}

	public void setBoat(Boat boat)
	{
		_boat = boat;
	}

	public Location getInBoatPosition()
	{
		return _inBoatPosition;
	}

	public void setInBoatPosition(Location loc)
	{
		_inBoatPosition = loc;
	}

	public Map<Integer, SubClass> getSubClasses()
	{
		return _classlist;
	}

	public void setBaseClass(final int baseClass)
	{
		_baseClass = baseClass;
	}

	public int getBaseClassId()
	{
		return _baseClass;
	}

	public void setActiveClass(SubClass activeClass)
	{
		_activeClass = activeClass;
	}

	public SubClass getActiveClass()
	{
		return _activeClass;
	}

	public int getActiveClassId()
	{
		return getActiveClass().getClassId();
	}

	/**
	 * Changing index of class in DB, used for changing class when finished professional quests
	 *
	 * @param oldclass
	 * @param newclass
	 */
	public synchronized void changeClassInDb(final int oldclass, final int newclass)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE object_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_effects_save SET id=? WHERE object_id=? AND id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
		}
		catch(final SQLException e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Сохраняет информацию о классах в БД
	 */
	public void storeCharSubClasses()
	{
		SubClass main = getActiveClass();
		if(main != null)
		{
			main.setCp(getCurrentCp());
			//main.setExp(getExp());
			//main.setLevel(getLevel());
			//main.setSp(getSp());
			main.setHp(getCurrentHp());
			main.setMp(getCurrentMp());
			main.setActive(true);
			getSubClasses().put(getActiveClassId(), main);
		}
		else
			_log.warn("Could not store char sub data, main class " + getActiveClassId() + " not found for " + this);

		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();

			StringBuilder sb;
			for(SubClass subClass : getSubClasses().values())
			{
				sb = new StringBuilder("UPDATE character_subclasses SET ");
				sb.append("exp=").append(subClass.getExp()).append(",");
				sb.append("sp=").append(subClass.getSp()).append(",");
				sb.append("curHp=").append(subClass.getHp()).append(",");
				sb.append("curMp=").append(subClass.getMp()).append(",");
				sb.append("curCp=").append(subClass.getCp()).append(",");
				sb.append("level=").append(subClass.getLevel()).append(",");
				sb.append("active=").append(subClass.isActive() ? 1 : 0).append(",");
				sb.append("isBase=").append(subClass.isBase() ? 1 : 0).append(",");
				sb.append("death_penalty=").append(subClass.getDeathPenalty(this).getLevelOnSaveDB()).append(",");
				sb.append("certification='").append(subClass.getCertification()).append("'");
				sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND class_id=").append(subClass.getClassId()).append(" LIMIT 1");
				statement.executeUpdate(sb.toString());
			}

			sb = new StringBuilder("UPDATE character_subclasses SET ");
			sb.append("maxHp=").append(getMaxHp()).append(",");
			sb.append("maxMp=").append(getMaxMp()).append(",");
			sb.append("maxCp=").append(getMaxCp());
			sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND active=1 LIMIT 1");
			statement.executeUpdate(sb.toString());
		}
		catch(final Exception e)
		{
			_log.warn("Could not store char sub data: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Restore list of character professions and set up active proof
	 * Used when character is loading
	 */
	public static void restoreCharSubClasses(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id,exp,sp,curHp,curCp,curMp,active,isBase,death_penalty,certification FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();

			SubClass activeSubclass = null;
			while(rset.next())
			{
				final SubClass subClass = new SubClass();
				subClass.setBase(rset.getInt("isBase") != 0);
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setHp(rset.getDouble("curHp"));
				subClass.setMp(rset.getDouble("curMp"));
				subClass.setCp(rset.getDouble("curCp"));
				subClass.setDeathPenalty(new DeathPenalty(player, rset.getInt("death_penalty")));
				subClass.setCertification(rset.getInt("certification"));

				boolean active = rset.getInt("active") != 0;
				if(active)
					activeSubclass = subClass;
				player.getSubClasses().put(subClass.getClassId(), subClass);
			}

			if(player.getSubClasses().size() == 0)
				throw new Exception("There are no one subclass for player: " + player);

			int BaseClassId = player.getBaseClassId();
			if(BaseClassId == -1)
				throw new Exception("There are no base subclass for player: " + player);

			if(activeSubclass != null)
				player.setActiveSubClass(activeSubclass.getClassId(), false);

			if(player.getActiveClass() == null)
			{
				//если из-за какого-либо сбоя ни один из сабкласов не отмечен как активный помечаем базовый как активный
				final SubClass subClass = player.getSubClasses().get(BaseClassId);
				subClass.setActive(true);
				player.setActiveSubClass(subClass.getClassId(), false);
			}
		}
		catch(final Exception e)
		{
			_log.warn("Could not restore char sub-classes: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	/**
	 * Добавить класс, используется только для сабклассов
	 *
	 * @param storeOld
	 * @param certification
	 */
	public boolean addSubClass(final int classId, boolean storeOld, int certification)
	{
		if(_classlist.size() >= (4 + Config.ALT_GAME_SUB_ADD))
			return false;

		final ClassId newId = ClassId.VALUES[classId];

		final SubClass newClass = new SubClass();
		newClass.setBase(false);
		if(newId.getRace() == null)
			return false;

		newClass.setClassId(classId);
		newClass.setCertification(certification);

		_classlist.put(classId, newClass);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			// Store the basic info about this new sub-class.
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty, certification) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, Experience.LEVEL[40]);
			statement.setInt(4, 0);
			statement.setDouble(5, getCurrentHp());
			statement.setDouble(6, getCurrentMp());
			statement.setDouble(7, getCurrentCp());
			statement.setDouble(8, getCurrentHp());
			statement.setDouble(9, getCurrentMp());
			statement.setDouble(10, getCurrentCp());
			statement.setInt(11, 40);
			statement.setInt(12, 0);
			statement.setInt(13, 0);
			statement.setInt(14, 0);
			statement.setInt(15, certification);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warn("Could not add character sub-class: " + e, e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		setActiveSubClass(classId, storeOld);

		boolean countUnlearnable = true;
		int unLearnable = 0;

		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		while(skills.size() > unLearnable)
		{
			for(final SkillLearn s : skills)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || !sk.getCanLearn(newId))
				{
					if(countUnlearnable)
						unLearnable++;
					continue;
				}
				addSkill(sk, true);
			}
			countUnlearnable = false;
			skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		}

		sendPacket(new SkillList(this));
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setCurrentCp(getMaxCp());
		return true;
	}

	/**
	 * Удаляет всю информацию о классе и добавляет новую, только для сабклассов
	 */
	public boolean modifySubClass(final int oldClassId, final int newClassId)
	{
		final SubClass originalClass = _classlist.get(oldClassId);
		if(originalClass == null || originalClass.isBase())
			return false;

		final int certification = originalClass.getCertification();

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND isBase = 0");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved skills info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved effects stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all henna info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
		}
		catch(final Exception e)
		{
			_log.warn("Could not delete char sub-class: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		_classlist.remove(oldClassId);

		return newClassId <= 0 || addSubClass(newClassId, false, certification);
	}

	public void setActiveSubClass(final int subId, final boolean store)
	{
		final SubClass sub = getSubClasses().get(subId);
		final SubClass oldsub = getActiveClass();
		if(sub == null)
			return;

		if(isInDuel())
		{
			sendMessage(isLangRus() ? "Невозможно выполнить во время дуэли!" : "Unable to perform during a duel!");
			return;
		}
		
		if (NexusEvents.isRegistered(this))
			return;

		if(getActiveClass() != null)
		{
			EffectsDAO.getInstance().insert(this);
			storeDisableSkills();

			if(QuestManager.getQuest(422) != null)
			{
				String qn = QuestManager.getQuest(422).getName();
				if(qn != null)
				{
					QuestState qs = getQuestState(qn);
					if(qs != null)
						qs.exitCurrentQuest(true);
				}
			}
		}

		if(store)
		{
			oldsub.setCp(getCurrentCp());
			//oldsub.setExp(getExp());
			//oldsub.setLevel(getLevel());
			//oldsub.setSp(getSp());
			oldsub.setHp(getCurrentHp());
			oldsub.setMp(getCurrentMp());
			oldsub.setActive(false);
			getSubClasses().put(oldsub.getClassId(), oldsub);
		}

		sub.setActive(true);
		setActiveClass(sub);
		getSubClasses().put(getActiveClassId(), sub);

		setClassId(subId, false, false);

		removeAllSkills();

		getEffectList().stopAllEffects();

		if(getPet() != null && (getPet().isSummon() || Config.ALT_IMPROVED_PETS_LIMITED_USE && (getPet().getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID && !isMageClass() || getPet().getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID && isMageClass())))
		{
			getPet().saveEffects();
			getPet().unSummon();
		}

		setAgathion(0);

		restoreSkills();
		rewardSkills(false);
		checkSkills();
		sendPacket(new ExStorageMaxCount(this));

		refreshExpertisePenalty();

		sendPacket(new SkillList(this));

		getInventory().refreshEquip();
		getInventory().validateItems();

		for(int i = 0; i < 3; i++)
			_henna[i] = null;

		restoreHenna();
		sendPacket(new HennaInfo(this));

		EffectsDAO.getInstance().restoreEffects(this);
		restoreDisableSkills();

		setCurrentHpMp(sub.getHp(), sub.getMp());
		setCurrentCp(sub.getCp());

		_shortCuts.restore();
		sendPacket(new ShortCutInit(this));
		for(int shotId : getAutoSoulShot())
			sendPacket(new ExAutoSoulShot(shotId, true));
		sendPacket(new SkillCoolTime(this));

		broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));

		getDeathPenalty().restore(this);

		setIncreasedForce(0);

		startHourlyTask();

		broadcastCharInfo();
		updateEffectIcons();
		updateStats();
	}

	/**
	 * Через delay миллисекунд выбросит игрока из игры
	 */
	public void startKickTask(long delayMillis)
	{
		stopKickTask();
		_kickTask = ThreadPoolManager.getInstance().schedule(new KickTask(this), delayMillis);
	}

	public void stopKickTask()
	{
		if(_kickTask != null)
		{
			_kickTask.cancel(false);
			_kickTask = null;
		}
	}

	public void startBonusTask()
	{
		if (this == null || getClient() == null)
			return;

		if(Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS)
		{
			int bonusExpire = getClient() == null ? 0 : getClient().getBonusExpire();
			double bonus = getClient() == null ? 0 : getClient().getBonus();
			if(bonusExpire > System.currentTimeMillis() / 1000L)
			{
				getBonus().setRateXp(Config.SERVICES_BONUS_XP * bonus);
				getBonus().setRateSp(Config.SERVICES_BONUS_SP * bonus);
				getBonus().setDropAdena(Config.SERVICES_BONUS_ADENA * bonus);
				getBonus().setDropItems(Config.SERVICES_BONUS_ITEMS * bonus);
				getBonus().setDropSpoil(Config.SERVICES_BONUS_SPOIL * bonus);
				getBonus().setBonusExpire(bonusExpire);

				if(_bonusExpiration == null)
					_bonusExpiration = LazyPrecisionTaskManager.getInstance().startBonusExpirationTask(this);
			}
			else if(bonus > 0 && Config.SERVICES_RATE_TYPE == Bonus.BONUS_GLOBAL_ON_GAMESERVER)
				AccountBonusDAO.getInstance().delete(getAccountName());
		}
	}

	public void stopBonusTask()
	{
		if(_bonusExpiration != null)
		{
			_bonusExpiration.cancel(false);
			_bonusExpiration = null;
		}
	}

	@Override
	public int getInventoryLimit()
	{
		return (int) calcStat(Stats.INVENTORY_LIMIT, 0, null, null);
	}

	public int getWarehouseLimit()
	{
		return (int) calcStat(Stats.STORAGE_LIMIT, 0, null, null);
	}

	public int getTradeLimit()
	{
		return (int) calcStat(Stats.TRADE_LIMIT, 0, null, null);
	}

	public int getDwarvenRecipeLimit()
	{
		return (int) calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	public int getCommonRecipeLimit()
	{
		return (int) calcStat(Stats.COMMON_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	public boolean getAndSetLastItemAuctionRequest()
	{
		if(_lastItemAuctionInfoRequest + 2000L < System.currentTimeMillis())
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return true;
		}
		
		_lastItemAuctionInfoRequest = System.currentTimeMillis();
		return false;
	}
	
	@Override
	public int getNpcId()
	{
		return -2;
	}

	public GameObject getVisibleObject(int id)
	{
		if(getObjectId() == id)
			return this;

		GameObject target = null;

		if(getTargetId() == id)
			target = getTarget();

		if (target == null)
			target = getPlayerGroup().getPlayer(id);

		if(target == null)
			target = World.getAroundObjectById(this, id);

		return target == null || (target.isInvisible() && !canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)) ? null : target;
	}

	@Override
	public int getPAtk(final Creature target)
	{
		double init = getActiveWeaponInstance() == null ? (isMageClass() ? 3 : 4) : 0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getPDef(final Creature target)
	{
		double init = 4.; //empty cloak and underwear slots

		final ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chest == null)
			init += isMageClass() ? ArmorTemplate.EMPTY_BODY_MYSTIC : ArmorTemplate.EMPTY_BODY_FIGHTER;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) == null && (chest == null || chest.getBodyPart() != ItemTemplate.SLOT_FULL_ARMOR))
			init += isMageClass() ? ArmorTemplate.EMPTY_LEGS_MYSTIC : ArmorTemplate.EMPTY_LEGS_FIGHTER;

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) == null)
			init += ArmorTemplate.EMPTY_HELMET;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) == null)
			init += ArmorTemplate.EMPTY_GLOVES;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) == null)
			init += ArmorTemplate.EMPTY_BOOTS;

		return (int) calcStat(Stats.POWER_DEFENCE, init, target, null);
	}

	@Override
	public int getMDef(final Creature target, final Skill skill)
	{
		double init = 0.;

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) == null)
			init += ArmorTemplate.EMPTY_EARRING;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) == null)
			init += ArmorTemplate.EMPTY_EARRING;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) == null)
			init += ArmorTemplate.EMPTY_NECKLACE;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) == null)
			init += ArmorTemplate.EMPTY_RING;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) == null)
			init += ArmorTemplate.EMPTY_RING;

		return (int) calcStat(Stats.MAGIC_DEFENCE, init, target, skill);
	}

	public boolean isSubClassActive()
	{
		return getBaseClassId() != getActiveClassId();
	}

	@Override
	public String getTitle()
	{
		return super.getTitle();
	}

	public int getTitleColor()
	{
		return _titlecolor;
	}

	public void setTitleColor(final int titlecolor, boolean store)
	{
		if(titlecolor != DEFAULT_TITLE_COLOR && store)
			setVar("titlecolor", Integer.toHexString(titlecolor), -1);
		else
			unsetVar("titlecolor");
		_titlecolor = titlecolor;
	}

	public void setTitleColor(final int titlecolor)
	{
		if(titlecolor != DEFAULT_TITLE_COLOR)
			setVar("titlecolor", Integer.toHexString(titlecolor), -1);
		else
			unsetVar("titlecolor");
		_titlecolor = titlecolor;
	}
	
	@Override
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}

	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;
	}

	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}

	@Override
	public boolean isImmobilized()
	{
		return super.isImmobilized() || isOverloaded() || isSitting() || isFishing();
	}

	@Override
	public boolean isBlocked()
	{
		return super.isBlocked() || isInMovie() || isInObserverMode() || isTeleporting() || isLogoutStarted();
	}

	@Override
	public boolean isInvul()
	{
		return super.isInvul() || isInMovie();
	}

	/**
	 * if True, the L2Player can't take more item
	 */
	public void setOverloaded(boolean overloaded)
	{
		_overloaded = overloaded;
	}

	public boolean isOverloaded()
	{
		return _overloaded;
	}

	public boolean isFishing()
	{
		return _isFishing;
	}

	public Fishing getFishing()
	{
		return _fishing;
	}

	public void setFishing(boolean value)
	{
		_isFishing = value;
	}

	public void startFishing(FishTemplate fish, LureTemplate lure)
	{
		_fishing.setFish(fish);
		_fishing.setLureId(lure);
		_fishing.startFishing();
	}

	public void stopFishing()
	{
		_fishing.stopFishing();
	}

	public Location getFishLoc()
	{
		return _fishing.getFishLoc();
	}

	public Bonus getBonus()
	{
		return _bonus;
	}

	public boolean hasBonus()
	{
		return _bonus.getBonusExpire() > System.currentTimeMillis() / 1000L;
	}

	@Override
	public double getRateAdena()
	{
		if (PremiumAccountsTable.isPremium(this))
			return PremiumAccountsTable.getDropBonus(this, 57);
		
		return _party == null ? _bonus.getDropAdena() : _party._rateAdena;
	}

	@Override
	public double getRateItems()
	{
		if (PremiumAccountsTable.isPremium(this))
				return PremiumAccountsTable.getDropBonus(this, 0);
		
		return _party == null ? _bonus.getDropItems() : _party._rateDrop;
	}

	@Override
	public double getRateExp()
	{
		return calcStat(Stats.EXP, (_party == null ? _bonus.getRateXp() : _party._rateExp), null, null);
	}

	@Override
	public double getRateSp()
	{
		return calcStat(Stats.SP, (_party == null ? _bonus.getRateSp() : _party._rateSp), null, null);
	}

	@Override
	public double getRateSpoil()
	{
		return _party == null ? _bonus.getDropSpoil() : _party._rateSpoil;
	}

	private boolean _maried = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _maryrequest = false;
	private boolean _maryaccepted = false;

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(boolean state)
	{
		_maried = state;
	}

	public void setMaryRequest(boolean state)
	{
		_maryrequest = state;
	}

	public boolean isMaryRequest()
	{
		return _maryrequest;
	}

	public void setMaryAccepted(boolean state)
	{
		_maryaccepted = state;
	}

	public boolean isMaryAccepted()
	{
		return _maryaccepted;
	}

	public int getPartnerId()
	{
		return _partnerId;
	}

	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}

	private GArray<Player> _snoopListener = new GArray<Player>();
	private GArray<Player> _snoopedPlayer = new GArray<Player>();

	public void broadcastSnoop(int type, String name, String _text)
	{
		if(_snoopListener.size() > 0)
		{
			Snoop sn = new Snoop(getObjectId(), getName(), type, name, _text);
			for(Player pci : _snoopListener)
				if(pci != null)
					pci.sendPacket(sn);
		}
	}

	public void addSnooper(Player pci)
	{
		if(!_snoopListener.contains(pci))
			_snoopListener.add(pci);
	}

	public void removeSnooper(Player pci)
	{
		_snoopListener.remove(pci);
	}

	public void addSnooped(Player pci)
	{
		if(!_snoopedPlayer.contains(pci))
			_snoopedPlayer.add(pci);
	}

	public void removeSnooped(Player pci)
	{
		_snoopedPlayer.remove(pci);
	}

	/**
	 * Сброс реюза всех скилов персонажа.
	 */
	public void resetReuse()
	{
		_skillReuses.clear();
		_sharedGroupReuses.clear();
	}

	public DeathPenalty getDeathPenalty()
	{
		return _activeClass == null ? null : _activeClass.getDeathPenalty(this);
	}

	private boolean _charmOfCourage = false;

	public boolean isCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;

		if(!val)
			getEffectList().stopEffect(Skill.SKILL_CHARM_OF_COURAGE);

		sendEtcStatusUpdate();
	}

	private int _increasedForce = 0;
	private int _consumedSouls = 0;

	@Override
	public int getIncreasedForce()
	{
		return _increasedForce;
	}

	@Override
	public int getConsumedSouls()
	{
		return _consumedSouls;
	}

	@Override
	public void setConsumedSouls(int i, NpcInstance monster)
	{
		if(i == _consumedSouls)
			return;

		int max = (int) calcStat(Stats.SOULS_LIMIT, 0, monster, null);

		if(i > max)
			i = max;

		if(i <= 0)
		{
			_consumedSouls = 0;
			sendEtcStatusUpdate();
			return;
		}

		if(_consumedSouls != i)
		{
			int diff = i - _consumedSouls;
			if(diff > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
				sm.addNumber(diff);
				sm.addNumber(i);
				sendPacket(sm);
			}
		}
		else if(max == i)
		{
			sendPacket(Msg.SOUL_CANNOT_BE_ABSORBED_ANY_MORE);
			return;
		}

		_consumedSouls = i;
		sendPacket(new EtcStatusUpdate(this));
	}

	@Override
	public void setIncreasedForce(int i)
	{
		if (_increasedForce == i)
			return;
		
		i = Math.min(i, Charge.MAX_CHARGE);
		i = Math.max(i, 0);

		if(i != 0 && i > _increasedForce)
			sendPacket(new SystemMessage(SystemMessage.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL).addNumber(i));

		_increasedForce = i;
		sendEtcStatusUpdate();
	}

	private long _lastFalling;

	public boolean isFalling()
	{
		return System.currentTimeMillis() - _lastFalling < 5000;
	}

	public void falling(int height)
	{
		if(!Config.DAMAGE_FROM_FALLING || isDead() || isFlying() || isInWater() || isInBoat())
			return;
		_lastFalling = System.currentTimeMillis();
		int damage = (int) calcStat(Stats.FALL, getMaxHp() / 2000 * height, null, null);
		if(damage > 0)
		{
			int curHp = (int) getCurrentHp();
			if(curHp - damage < 1)
				setCurrentHp(1, false);
			else
				setCurrentHp(curHp - damage, false);
			sendPacket(new SystemMessage(SystemMessage.YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL).addNumber(damage));
		}
	}

	/**
	 * Системные сообщения о текущем состоянии хп
	 */
	@Override
	public void checkHpMessages(double curHp, double newHp)
	{
		//сюда пасивные скиллы
		final int[] _hp = {
				30,
				30
		};
		final int[] skills = {
				290,
				291
		};

		//сюда активные эффекты
		final int[] _effects_skills_id = {
				139,
				176,
				292,
				292,
				420
		};
		final int[] _effects_hp = {
				30,
				30,
				30,
				60,
				30
		};

		double percent = getMaxHp() / 100;
		double _curHpPercent = curHp / percent;
		double _newHpPercent = newHp / percent;
		boolean needsUpdate = false;

		//check for passive skills
		for(int i = 0; i < skills.length; i++)
		{
			int level = getSkillLevel(skills[i]);
			if(level > 0)
				if(_curHpPercent > _hp[i] && _newHpPercent <= _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(skills[i], level));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _hp[i] && _newHpPercent > _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(skills[i], level));
					needsUpdate = true;
				}
		}

		//check for active effects
		for(Integer i = 0; i < _effects_skills_id.length; i++)
			if(getEffectList().getEffectsBySkillId(_effects_skills_id[i]) != null)
				if(_curHpPercent > _effects_hp[i] && _newHpPercent <= _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _effects_hp[i] && _newHpPercent > _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}

		if(needsUpdate)
			sendChanges();
	}

	/**
	 * Системные сообщения для темных эльфов о вкл/выкл ShadowSence (skill id = 294)
	 */
	public void checkDayNightMessages()
	{
		int level = getSkillLevel(294);
		if(level > 0)
			if(GameTimeController.getInstance().isNowNight())
				sendPacket(new SystemMessage(SystemMessage.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(294, level));
			else
				sendPacket(new SystemMessage(SystemMessage.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR).addSkillName(294, level));
		
		sendChanges();
	}

	public int getZoneMask()
	{
		return _zoneMask;
	}

	//TODO [G1ta0] переработать в лисенер?
	@Override
	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		super.onUpdateZones(leaving, entering);

		if((leaving == null || leaving.isEmpty()) && (entering == null || entering.isEmpty()))
			return;

		boolean lastInCombatZone = (_zoneMask & ZONE_PVP_FLAG) == ZONE_PVP_FLAG;
		boolean lastInDangerArea = (_zoneMask & ZONE_ALTERED_FLAG) == ZONE_ALTERED_FLAG;
		boolean lastOnSiegeField = (_zoneMask & ZONE_SIEGE_FLAG) == ZONE_SIEGE_FLAG;
		boolean lastInPeaceZone = (_zoneMask & ZONE_PEACE_FLAG) == ZONE_PEACE_FLAG;
		//FIXME G1ta0 boolean lastInSSQZone = (_zoneMask & ZONE_SSQ_FLAG) == ZONE_SSQ_FLAG;

		boolean isInCombatZone = isInCombatZone();
		boolean isInDangerArea = isInDangerArea();
		boolean isOnSiegeField = isOnSiegeField();
		boolean isInPeaceZone = isInPeaceZone();
		boolean isInSSQZone = isInSSQZone();

		// обновляем компас, только если персонаж в мире
		int lastZoneMask = _zoneMask;
		_zoneMask = 0;

		if(isInCombatZone)
			_zoneMask |= ZONE_PVP_FLAG;
		if(isInDangerArea)
			_zoneMask |= ZONE_ALTERED_FLAG;
		if(isOnSiegeField)
			_zoneMask |= ZONE_SIEGE_FLAG;
		if(isInPeaceZone)
			_zoneMask |= ZONE_PEACE_FLAG;
		if(isInSSQZone)
			_zoneMask |= ZONE_SSQ_FLAG;

		if(lastZoneMask != _zoneMask)
			sendPacket(new ExSetCompassZoneCode(this));

		if(lastInCombatZone != isInCombatZone)
			broadcastRelationChanged();

		if(lastInDangerArea != isInDangerArea)
			sendPacket(new EtcStatusUpdate(this));

		if(lastOnSiegeField != isOnSiegeField)
		{
			broadcastRelationChanged();
			if(isOnSiegeField)
			{
				sendPacket(Msg.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
				if (!Config.ALLOW_WYVERN_DURING_SIEGE && getPlayer().getMountType() == 2)
				{
					if (Config.PUNISHMENT_FOR_WYVERN_INSIDE_SIEGE != null && getPlayer() != null)
					{
						if(isMessageTaskCancelled())
							startMessageTask("Its not allowed to ride wyvern inside siege zone! You have 15 seconds to leave!", 15);
						
						startPunishWyvern(this, 15);
					}
					stopMove();
					sendPacket(SystemMsg.THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN);
					dismount();
				}
			}
			else
			{
				endMessageTask();
				endPunishWyvernTask();
				FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
				if(attachment != null)
					attachment.onOutTerritory(this);
				startPvPFlagSiege();
				sendPacket(Msg.YOU_HAVE_LEFT_A_COMBAT_ZONE);
			}
		}

		if(lastInPeaceZone != isInPeaceZone)
		{
			if (isInPeaceZone)
			{
				setRecomTimerActive(false);
				if (getNevitSystem().isActive())
					getNevitSystem().stopAdventTask(true);
				startVitalityTask();
				
				DuelEvent duelEvent = getEvent(DuelEvent.class);
				if (duelEvent != null)
					duelEvent.abortDuel(this);
			}
			else
				stopVitalityTask();
		}
		
		if (isInWater())
			startWaterTask();
		else
			stopWaterTask();
	}

	public void startAutoSaveTask()
	{
		if(!Config.AUTOSAVE)
			return;
		if(_autoSaveTask == null)
			_autoSaveTask = AutoSaveManager.getInstance().addAutoSaveTask(this);
	}

	public void stopAutoSaveTask()
	{
		if(_autoSaveTask != null)
			_autoSaveTask.cancel(false);
		_autoSaveTask = null;
	}

	public void startVitalityTask()
	{
		if(!Config.ALT_VITALITY_ENABLED)
			return;
		if(_vitalityTask == null)
			_vitalityTask = LazyPrecisionTaskManager.getInstance().addVitalityRegenTask(this);
	}

	public void stopVitalityTask()
	{
		if(_vitalityTask != null)
			_vitalityTask.cancel(false);
		_vitalityTask = null;
	}

	public void startPcBangPointsTask()
	{
		if(!Config.PCBANG_POINTS_ENABLED)
			return;
		if(_pcCafePointsTask == null)
			_pcCafePointsTask = LazyPrecisionTaskManager.getInstance().addPCCafePointsTask(this);
	}

	public void stopPcBangPointsTask()
	{
		if(_pcCafePointsTask != null)
			_pcCafePointsTask.cancel(false);
		_pcCafePointsTask = null;
	}

	public final boolean isInJail()
	{
		return getVarB("jailed");
	}

	@Override
	public void sendMessage(String message)
	{
		sendPacket(new SystemMessage(message));
	}

	private Location _lastClientPosition;
	private Location _lastServerPosition;

	public void setLastClientPosition(Location position)
	{
		_lastClientPosition = position;
	}

	public Location getLastClientPosition()
	{
		return _lastClientPosition;
	}

	public void setLastServerPosition(Location position)
	{
		_lastServerPosition = position;
	}

	public Location getLastServerPosition()
	{
		return _lastServerPosition;
	}

	private int _useSeed = 0;

	public void setUseSeed(int id)
	{
		_useSeed = id;
	}

	public int getUseSeed()
	{
		return _useSeed;
	}

	public int getRelation(Player target)
	{
		int result = 0;

		if(getClan() != null)
		{
			result |= RelationChanged.RELATION_CLAN_MEMBER;
			if(getClan() == target.getClan())
				result |= RelationChanged.RELATION_CLAN_MATE;
			if(getClan().getAllyId() != 0)
				result |= RelationChanged.RELATION_ALLY_MEMBER;
		}

		if(isClanLeader())
			result |= RelationChanged.RELATION_LEADER;

		Party party = getParty();
		if(party != null && party == target.getParty())
		{
			result |= RelationChanged.RELATION_HAS_PARTY;

			switch(party.getMembers().indexOf(this))
			{
				case 0:
					result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
					break;
				case 1:
					result |= RelationChanged.RELATION_PARTY4; // 0x8
					break;
				case 2:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x7
					break;
				case 3:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2; // 0x6
					break;
				case 4:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY1; // 0x5
					break;
				case 5:
					result |= RelationChanged.RELATION_PARTY3; // 0x4
					break;
				case 6:
					result |= RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x3
					break;
				case 7:
					result |= RelationChanged.RELATION_PARTY2; // 0x2
					break;
				case 8:
					result |= RelationChanged.RELATION_PARTY1; // 0x1
					break;
			}
		}

		Clan clan1 = getClan();
		Clan clan2 = target.getClan();
		if(clan1 != null && clan2 != null)
		{
			if(target.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPledgeType() != Clan.SUBUNIT_ACADEMY)
			{
				if(clan2.isAtWarWith(clan1.getClanId()))
				{
					result |= RelationChanged.RELATION_1SIDED_WAR;
					if(clan1.isAtWarWith(clan2.getClanId()))
						result |= RelationChanged.RELATION_MUTUAL_WAR;
				}
			}
			if(getBlockCheckerArena() != -1)
			{
				result |= RelationChanged.RELATION_INSIEGE;
				ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
				if(holder.getPlayerTeam(this) == 0)
					result |= RelationChanged.RELATION_ENEMY;
				else
					result |= RelationChanged.RELATION_ALLY;
				result |= RelationChanged.RELATION_ATTACKER;
			}
			else if (NexusEvents.isInEvent(this) && NexusEvents.isInEvent(target))
			{
				result |= RelationChanged.RELATION_INSIEGE;
				if (NexusEvents.canAttack(this, target))
					result |= RelationChanged.RELATION_ENEMY;
				else
					result |= RelationChanged.RELATION_ALLY;
				result |= RelationChanged.RELATION_ATTACKER;
			}
		}

		for(GlobalEvent e : getEvents())
			result =  e.getRelation(this, target, result);

		return result;
	}

	/**
	 * 0=White, 1=Purple, 2=PurpleBlink
	 */
	protected int _pvpFlag;

	private Future<?> _PvPRegTask;
	private long _lastPvpAttack;

	public long getlastPvpAttack()
	{
		return _lastPvpAttack;
	}

	private void startPvPFlagSiege()
	{
		if(_karma > 0)
			return;
		// Не флагаемся если в осадной зоне
		if(isOnSiegeField())
			return;

		long startTime = System.currentTimeMillis();
		startTime -= Config.PVP_TIME / 2;
		if(_pvpFlag != 0 && _lastPvpAttack > startTime)
			return;

		_lastPvpAttack = startTime;

		updatePvPFlag(1);

		if(_PvPRegTask == null)
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PvPFlagTask(this), 1000, 1000);
	}
	
	@Override
	public void startPvPFlag(Creature target)
	{
		if(isOnSiegeField())
			return;
		
		if(_karma > 0)
			return;

		long startTime = System.currentTimeMillis();
		if(target != null && (target.getPvpFlag() != 0 || target.isMonster()))
			startTime -= Config.PVP_TIME / 2;
		if(_pvpFlag != 0 && _lastPvpAttack > startTime)
			return;
				
		_lastPvpAttack = startTime;

		updatePvPFlag(1);

		if(_PvPRegTask == null)
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PvPFlagTask(this), 1000, 1000);
	}

	public void stopPvPFlag()
	{
		if(_PvPRegTask != null)
		{
			_PvPRegTask.cancel(false);
			_PvPRegTask = null;
		}
		updatePvPFlag(0);
	}

	public void updatePvPFlag(int value)
	{
		if(_handysBlockCheckerEventArena != -1)
			return;
		if(_pvpFlag == value)
			return;
				
		setPvpFlag(value);

		sendStatusUpdate(true, true, StatusUpdate.PVP_FLAG);

		broadcastRelationChanged();
	}

	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}

	@Override
	public int getPvpFlag()
	{
		return _pvpFlag;
	}

	public boolean isInDuel()
	{
		return getEvent(DuelEvent.class) != null;
	}

	private Map<Integer, TamedBeastInstance> _tamedBeasts = new ConcurrentHashMap<Integer, TamedBeastInstance>();

	public Map<Integer, TamedBeastInstance> getTrainedBeasts()
	{
		return _tamedBeasts;
	}

	public void addTrainedBeast(TamedBeastInstance tamedBeast)
	{
		_tamedBeasts.put(tamedBeast.getObjectId(), tamedBeast);
	}

	public void removeTrainedBeast(int npcId)
	{
		_tamedBeasts.remove(npcId);
	}

	private long _lastAttackPacket = 0;

	public long getLastAttackPacket()
	{
		return _lastAttackPacket;
	}

	public void setLastAttackPacket()
	{
		_lastAttackPacket = System.currentTimeMillis();
	}

	private long _lastMovePacket = 0;

	public long getLastMovePacket()
	{
		return _lastMovePacket;
	}

	public void setLastMovePacket()
	{
		_lastMovePacket = System.currentTimeMillis();
	}

	public byte[] getKeyBindings()
	{
		return _keyBindings;
	}

	public void setKeyBindings(byte[] keyBindings)
	{
		if(keyBindings == null)
			keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
		_keyBindings = keyBindings;
	}

	/**
	 * Устанавливает режим трансформаии<BR>
	 *
	 * @param transformationId идентификатор трансформации
	 *                         Известные режимы:<BR>
	 *                         <li>0 - стандартный вид чара
	 *                         <li>1 - Onyx Beast
	 *                         <li>2 - Death Blader
	 *                         <li>etc.
	 */
	public void setTransformation(int transformationId)
	{
		if(transformationId == _transformationId || _transformationId != 0 && transformationId != 0)
			return;
		
		if (isActionBlocked(Zone.BLOCKED_ACTION_TRANSFORMATION))
		{
			sendMessage("You are not allowed to transform in this zone.");
			return;
		}
		
		// For each transformation different set of skills
		if(transformationId == 0) // Normal untransformed state
		{
			// Stop the current transformation effect
			for(Effect effect : getEffectList().getAllEffects())
				if(effect != null && effect.getEffectType() == EffectType.Transformation)
				{
					if (effect.calc() == 0) // Dont remove Dispel
						continue;
					effect.exit();
					preparateToTransform(effect.getSkill());
					break;
				}

			// Remove transform skills
			if(!_transformationSkills.isEmpty())
			{
				for(Skill s : _transformationSkills.values().toArray(new Skill[_transformationSkills.size()]))
				{
					if(!s.isCommon() && !SkillAcquireHolder.getInstance().isSkillPossible(this, s) && !s.isHeroic())
						super.removeSkill(s);
				}
				
				_transformationSkills.clear();
			}
		}
		else
		{
			int id = 0;
			int level = 1;
			switch(getBaseClassId())
			{
				case 97://Cardinal
					id = 24001;
					break;
				case 98://Hierophant
					id = 24002;
					break;
				case 100://SwordMuse
					id = 24003;
					break;
				case 105://EvaSaint
					id = 24004;
					break;
				case 107://SpectralDancer
					id = 24005;
					break;
				case 112://ShillienSaint
					id = 24006;
					break;
				case 115://Dominator
					id = 24007;
					break;
				case 116://Doomcryer
					id = 24008;
					break;
			}

			Skill skill = SkillTable.getInstance().getInfo(id, level);
			if(skill != null)
			{
				super.removeSkill(skill);
				removeSkillFromShortCut(skill.getId());
			}
			
			if(!isCursedWeaponEquipped())
			{
				// Добавляем скилы трансформации
				for(Effect effect : getEffectList().getAllEffects())
					if(effect != null && effect.getEffectType() == EffectType.Transformation)
					{
						if(effect.getSkill() instanceof Transformation && ((Transformation) effect.getSkill()).isDisguise)
						{
							for(Skill s : getAllSkills())
								if(s != null && (s.isActive() || s.isToggle()))
									_transformationSkills.put(s.getId(), s);
						}
						else
							for(AddedSkill s : effect.getSkill().getAddedSkills())
								if(s.level == 0) // трансформация позволяет пользоваться обычным скиллом
								{
									int s2 = getSkillLevel(s.id);
									if(s2 > 0)
										_transformationSkills.put(s.id, SkillTable.getInstance().getInfo(s.id, s2));
								}
								else if(s.level == -2) // XXX: дикий изжоп для скиллов зависящих от уровня игрока
								{
									int learnLevel = Math.max(effect.getSkill().getMagicLevel(), 40);
									int maxLevel = SkillTable.getInstance().getBaseLevel(s.id);
									int curSkillLevel = 1;
									if(maxLevel > 3)
										curSkillLevel += getLevel() - learnLevel;
									else
										curSkillLevel += (getLevel() - learnLevel) / ((76 - learnLevel) / maxLevel); // не спрашивайте меня что это такое
									curSkillLevel = Util.constrain(curSkillLevel, 1, maxLevel);
									_transformationSkills.put(s.id, SkillTable.getInstance().getInfo(s.id, curSkillLevel));
								}
								else
									_transformationSkills.put(s.id, s.getSkill());
						preparateToTransform(effect.getSkill());
						break;
					}
			}
			else
				preparateToTransform(null);

			if(!isInOlympiadMode() && !isCursedWeaponEquipped() && _hero && getBaseClassId() == getActiveClassId())
			{
				// Добавляем хиро скиллы проклятому трансформу
				_transformationSkills.put(395, SkillTable.getInstance().getInfo(395, 1));
				_transformationSkills.put(396, SkillTable.getInstance().getInfo(396, 1));
				_transformationSkills.put(1374, SkillTable.getInstance().getInfo(1374, 1));
				_transformationSkills.put(1375, SkillTable.getInstance().getInfo(1375, 1));
				_transformationSkills.put(1376, SkillTable.getInstance().getInfo(1376, 1));
			}

			for(Skill s : _transformationSkills.values())
				addSkill(s, false);
		}

		_transformationId = transformationId;

		sendPacket(new ExBasicActionList(this));
		sendPacket(new SkillList(this));
		sendPacket(new ShortCutInit(this));
		for(int shotId : getAutoSoulShot())
			sendPacket(new ExAutoSoulShot(shotId, true));
		broadcastUserInfo(true);
	}

	private void preparateToTransform(Skill transSkill)
	{
		if(transSkill == null || !transSkill.isBaseTransformation())
		{
			// Останавливаем тугл скиллы
			for(Effect effect : getEffectList().getAllEffects())
				if(effect != null && effect.getSkill().isToggle())
					effect.exit();
		}
	}

	public boolean isInFlyingTransform()
	{
		return _transformationId == 8 || _transformationId == 9 || _transformationId == 260;
	}

	public boolean isInMountTransform()
	{
		return _transformationId == 106 || _transformationId == 109 || _transformationId == 110 || _transformationId == 20001;
	}

	/**
	 * Возвращает режим трансформации
	 *
	 * @return ID режима трансформации
	 */
	public int getTransformation()
	{
		return _transformationId;
	}

	/**
	 * Возвращает имя трансформации
	 *
	 * @return String
	 */
	public String getTransformationName()
	{
		return _transformationName;
	}

	/**
	 * Устанавливает имя трансформаии
	 *
	 * @param name имя трансформации
	 */
	public void setTransformationName(String name)
	{
		_transformationName = name;
	}

	/**
	 * Устанавливает шаблон трансформации, используется для определения коллизий
	 *
	 * @param template ID шаблона
	 */
	public void setTransformationTemplate(int template)
	{
		_transformationTemplate = template;
	}

	/**
	 * Возвращает шаблон трансформации, используется для определения коллизий
	 *
	 * @return NPC ID
	 */
	public int getTransformationTemplate()
	{
		return _transformationTemplate;
	}

	/**
	 * Returns a collection of skills, taking into account the current transformation
	 */
	@Override
	public final Collection<Skill> getAllSkills()
	{
		// Transformation inactive
		if(_transformationId == 0)
			return super.getAllSkills();
		else if (canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			List<Skill> skills = new ArrayList<Skill>();
			skills.addAll(super.getAllSkills());
			skills.addAll(_transformationSkills.values());
			return skills;
		}

		// Transformation active
		Map<Integer, Skill> tempSkills = new HashMap<Integer, Skill>();
		for(Skill s : super.getAllSkills())
			if(s != null && !s.isActive() && !s.isToggle())
				tempSkills.put(s.getId(), s);
		tempSkills.putAll(_transformationSkills);
		
		return tempSkills.values();
	}

	public void setAgathion(int id)
	{
		if(_agathionId == id)
			return;

		_agathionId = id;
		broadcastCharInfo();
	}

	public int getAgathionId()
	{
		return _agathionId;
	}

	/**
	 * Возвращает количество PcBangPoint'ов даного игрока
	 *
	 * @return количество PcCafe Bang Points
	 */
	public int getPcBangPoints()
	{
		return _pcBangPoints;
	}

	/**
	 * Устанавливает количество Pc Cafe Bang Points для даного игрока
	 *
	 * @param val новое количество PcCafeBangPoints
	 */
	public void setPcBangPoints(int val)
	{
		_pcBangPoints = val;
	}

	public void addPcBangPoints(int count, boolean doublePoints)
	{
		if(doublePoints)
			count *= 2;

		_pcBangPoints += count;

		if (_pcBangPoints > Config.MAX_PC_BANG_POINTS)
		{
			_pcBangPoints = Config.MAX_PC_BANG_POINTS;
			count = 0;
			sendMessage("You have reached the max PC Bang Points that can be acquired!");
		}
		
		sendPacket(new SystemMessage(doublePoints ? SystemMessage.DOUBLE_POINTS_YOU_AQUIRED_S1_PC_BANG_POINT : SystemMessage.YOU_ACQUIRED_S1_PC_BANG_POINT).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, count, 1, 2, 12));
	}

	public boolean reducePcBangPoints(int count)
	{
		if(_pcBangPoints < count)
			return false;

		_pcBangPoints -= count;
		sendPacket(new SystemMessage(SystemMessage.YOU_ARE_USING_S1_POINT).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, 0, 1, 2, 12));
		return true;
	}

	private Location _groundSkillLoc;

	public void setGroundSkillLoc(Location location)
	{
		_groundSkillLoc = location;
	}

	public Location getGroundSkillLoc()
	{
		return _groundSkillLoc;
	}

	/**
	 * Персонаж в процессе выхода из игры
	 *
	 * @return возвращает true если процесс выхода уже начался
	 */
	public boolean isLogoutStarted()
	{
		return _isLogout.get();
	}

	public void setOfflineMode(boolean val)
	{
		if(!val)
		{
			unsetVar("offline");
			unsetVar("offlinebuffer");
			unsetVar("offlinebuffrestoretitle");
			unsetVar("offlinebufferprice");
			unsetVar("offlinebufferschemeprice");
			unsetVar("offlinebuffertitle");
			setSellBuff(false);
		}
			
		_offline = val;
	}

	public boolean isOfflineBuffer()
	{
		return getVarB("offlinebuffer");
	}
	
	public boolean isInOfflineMode()
	{
		return _offline;
	}

	public void saveTradeList()
	{
		String val = "";

		if(_sellList == null || _sellList.isEmpty())
			unsetVar("selllist");
		else
		{
			for(TradeItem i : _sellList)
				val += i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("selllist", val, -1);
			val = "";
			if(_tradeList != null && getSellStoreName() != null)
				setVar("sellstorename", getSellStoreName(), -1);
		}

		if(_packageSellList == null || _packageSellList.isEmpty())
			unsetVar("packageselllist");
		else
		{
			for(TradeItem i : _packageSellList)
				val += i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("packageselllist", val, -1);
			val = "";
			if(_tradeList != null && getSellStoreName() != null)
				setVar("sellstorename", getSellStoreName(), -1);
		}

		if(_buyList == null || _buyList.isEmpty())
			unsetVar("buylist");
		else
		{
			for(TradeItem i : _buyList)
				val += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("buylist", val, -1);
			val = "";
			if(_tradeList != null && getBuyStoreName() != null)
				setVar("buystorename", getBuyStoreName(), -1);
		}

		if(_createList == null || _createList.isEmpty())
			unsetVar("createlist");
		else
		{
			for(ManufactureItem i : _createList)
				val += i.getRecipeId() + ";" + i.getCost() + ":";
			setVar("createlist", val, -1);
			if(getManufactureName() != null)
				setVar("manufacturename", getManufactureName(), -1);
		}
	}

	public void restoreTradeList()
	{
		String var;
		var = getVar("selllist");
		if(var != null)
		{
			_sellList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;

				int oId = Integer.parseInt(values[0]);
				long count = Long.parseLong(values[1]);
				long price = Long.parseLong(values[2]);

				ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

				if(count < 1 || itemToSell == null)
					continue;

				if(count > itemToSell.getCount())
					count = itemToSell.getCount();

				TradeItem i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);

				_sellList.add(i);
			}
			var = getVar("sellstorename");
			if(var != null)
			{
				if (Config.containsAbuseWord(var))
					var = "Sell";
				
				setSellStoreName(var);
			}
				
		}
		var = getVar("packageselllist");
		if(var != null)
		{
			_packageSellList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;

				int oId = Integer.parseInt(values[0]);
				long count = Long.parseLong(values[1]);
				long price = Long.parseLong(values[2]);

				ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

				if(count < 1 || itemToSell == null)
					continue;

				if(count > itemToSell.getCount())
					count = itemToSell.getCount();

				TradeItem i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);

				_packageSellList.add(i);
			}
			var = getVar("sellstorename");
			if(var != null)
			{
				if (Config.containsAbuseWord(var))
					var = "Sell Package";
				
				setSellStoreName(var);
			}
		}
		var = getVar("buylist");
		if(var != null)
		{
			_buyList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;
				TradeItem i = new TradeItem();
				i.setItemId(Integer.parseInt(values[0]));
				i.setCount(Long.parseLong(values[1]));
				i.setOwnersPrice(Long.parseLong(values[2]));
				_buyList.add(i);
			}
			var = getVar("buystorename");
			if(var != null)
			{
				if (Config.containsAbuseWord(var))
					var = "Buy";
				
				setBuyStoreName(var);
			}
		}
		var = getVar("createlist");
		if(var != null)
		{
			_createList = new CopyOnWriteArrayList<ManufactureItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 2)
					continue;
				int recId = Integer.parseInt(values[0]);
				long price = Long.parseLong(values[1]);
				if(findRecipe(recId))
					_createList.add(new ManufactureItem(recId, price));
			}
			var = getVar("manufacturename");
			if(var != null)
			{
				if (Config.containsAbuseWord(var))
					var = "Manufacture";
				
				setManufactureName(var);
			}
		}
	}

	public void restoreRecipeBook()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				int id = rset.getInt("id");
				Recipe recipe = RecipeHolder.getInstance().getRecipeByRecipeId(id);
				registerRecipe(recipe, false);
			}
		}
		catch(Exception e)
		{
			_log.warn("count not recipe skills:" + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public DecoyInstance getDecoy()
	{
		return _decoy;
	}

	public void setDecoy(DecoyInstance decoy)
	{
		_decoy = decoy;
	}

	public int getMountType()
	{
		switch(getMountNpcId())
		{
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
			case PetDataTable.GUARDIANS_STRIDER_ID:
				return 1;
			case PetDataTable.WYVERN_ID:
				return 2;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				return 3;
			case 32: // Jet Bike
			case 13130: // Light Purple Maned Horse
			case 13146: // Tawny-Maned Lion
			case 13147: // Steam Sledge
				return 4;
		}
		return 0; // Dismount
	}

	@Override
	public double getColRadius()
	{
		if (getTransformation() != 0)
		{
			if(getTransformationTemplate() == 32)
				setTransformation(0);
			else
			{
				final int template = getTransformationTemplate();
				if (template != 0)
				{
					final NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
					if (npcTemplate != null)
						return npcTemplate.getCollisionRadius();
				}
			}
		}
		else if (isMounted())
		{
			final int mountTemplate = getMountNpcId();
			if (mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if (mountNpcTemplate != null)
					return mountNpcTemplate.getCollisionRadius();
			}
		}
		return getCollisionRadius();
	}

	@Override
	public double getColHeight()
	{
		if (getTransformation() != 0)
		{
			if(getTransformationTemplate() == 32)
				setTransformation(0);
			else
			{
				final int template = getTransformationTemplate();
				if (template != 0)
				{
					final NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
					if (npcTemplate != null)
						return npcTemplate.getCollisionHeight();
				}
			}
		}
		else if (isMounted())
		{
			final int mountTemplate = getMountNpcId();
			if (mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if (mountNpcTemplate != null)
					return mountNpcTemplate.getCollisionHeight();
			}
		}
		return getCollisionHeight();
	}

	@Override
	public void setReflection(Reflection reflection)
	{
		if(getReflection() == reflection)
			return;

		super.setReflection(reflection);

		if(_summon != null && !_summon.isDead())
			_summon.setReflection(reflection);

		if(reflection != ReflectionManager.DEFAULT)
		{
			String var = getVar("reflection");
			if(var == null || !var.equals(String.valueOf(reflection.getId())))
				setVar("reflection", String.valueOf(reflection.getId()), -1);
		}
		else
			unsetVar("reflection");

		if(getActiveClass() != null)
		{
			getInventory().validateItems();
			// Для квеста _129_PailakaDevilsLegacy
			if(getPet() != null && (getPet().getNpcId() == 14916 || getPet().getNpcId() == 14917))
			{
				getPet().saveEffects();
				getPet().unSummon();
			}
		}
	}

	public boolean isTerritoryFlagEquipped()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getTemplate().isTerritoryFlag();
	}

	public boolean isCombatFlagEquipped()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getTemplate().isCombatFlag();
	}
	
	public boolean isFlagEquipped()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getTemplate().isFlagEquiped();
	}
	
	private int _buyListId;

	public void setBuyListId(int listId)
	{
		_buyListId = listId;
	}

	public int getBuyListId()
	{
		return _buyListId;
	}

	public int getFame()
	{
		return _fame;
	}

	public void setFame(int fame, String log)
	{
		fame = Math.min(Config.getLimit(Config.LIMIT_FAME, this), fame);
		if(log != null && !log.isEmpty())
			Log.addGame(_name + "|" + (fame - _fame) + "|" + fame + "|" + log, "fame");
		if(fame > _fame)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_REPUTATION_SCORE).addNumber(fame - _fame));
		_fame = fame;
		sendChanges();
	}

	public void setFame(int fame)
	{
		fame = Math.min(Config.getLimit(Config.LIMIT_FAME, this), fame);
		// Premium system.
		
		if(fame > _fame)
		{
			int added = fame - _fame;
			added *= PremiumAccountsTable.getFameBonus(this);
			fame = added + _fame;
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_REPUTATION_SCORE).addNumber(added));
		}
		_fame = fame;
		sendChanges();
	}
	
	public int getVitalityLevel(boolean blessActive)
	{
		return Config.ALT_VITALITY_ENABLED ? (blessActive ? 4 : _vitalityLevel) : 0;
	}

	public double getVitality()
	{
		return Config.ALT_VITALITY_ENABLED ? _vitality : 0;
	}

	public void addVitality(double val)
	{
		setVitality(getVitality() + val);
	}

	public void setVitality(double newVitality)
	{
		if(!Config.ALT_VITALITY_ENABLED)
			return;

		newVitality = Math.max(Math.min(newVitality, Config.VITALITY_LEVELS[4]), 0);

		if(newVitality >= _vitality || getLevel() >= 10)
		{
			if(newVitality != _vitality)
				if(newVitality == 0)
					sendPacket(Msg.VITALITY_IS_FULLY_EXHAUSTED);
				else if(newVitality == Config.VITALITY_LEVELS[4])
					sendPacket(Msg.YOUR_VITALITY_IS_AT_MAXIMUM);

			_vitality = newVitality;
		}

		int newLevel = 0;
		if(_vitality >= Config.VITALITY_LEVELS[3])
			newLevel = 4;
		else if(_vitality >= Config.VITALITY_LEVELS[2])
			newLevel = 3;
		else if(_vitality >= Config.VITALITY_LEVELS[1])
			newLevel = 2;
		else if(_vitality >= Config.VITALITY_LEVELS[0])
			newLevel = 1;

		if(_vitalityLevel > newLevel)
			getNevitSystem().addPoints(1500);

		if(_vitalityLevel != newLevel)
		{
			if(_vitalityLevel != -1) // при ините чара сообщения не шлём
				sendPacket(newLevel < _vitalityLevel ? Msg.VITALITY_HAS_DECREASED : Msg.VITALITY_HAS_INCREASED);
			_vitalityLevel = newLevel;
		}

		sendPacket(new ExVitalityPointInfo((int) _vitality));
	}

	private final int _incorrectValidateCount = 0;

	public int getIncorrectValidateCount()
	{
		return _incorrectValidateCount;
	}

	public int setIncorrectValidateCount(int count)
	{
		return _incorrectValidateCount;
	}

	public int getExpandInventory()
	{
		return _expandInventory;
	}

	public void setExpandInventory(int inventory)
	{
		_expandInventory = inventory;
	}

	public int getExpandWarehouse()
	{
		return _expandWarehouse;
	}

	public void setExpandWarehouse(int warehouse)
	{
		_expandWarehouse = warehouse;
	}
	
	public void showFreightWindow()
	{
		if(!canShowWarehouseWithdrawList(WarehouseType.FREIGHT))
		{
			sendActionFailed();
			return;
		}

		setUsingWarehouseType(WarehouseType.FREIGHT);
		sendPacket(new WareHouseWithdrawList(this, WarehouseType.FREIGHT, ItemClass.ALL));
	}

	public void showRetrieveWindow(int val)
	{
		if(!canShowWarehouseWithdrawList(WarehouseType.PRIVATE))
		{
			sendActionFailed();
			return;
		}

		setUsingWarehouseType(WarehouseType.PRIVATE);
		sendPacket(new WareHouseWithdrawList(this, WarehouseType.PRIVATE, ItemClass.values()[val]));
	}

	public void showDepositWindow()
	{
		if(!canShowWarehouseDepositList(WarehouseType.PRIVATE))
		{
			sendActionFailed();
			return;
		}

		setUsingWarehouseType(WarehouseType.PRIVATE);
		sendPacket(new WareHouseDepositList(this, WarehouseType.PRIVATE));
	}

	public void showDepositWindowClan()
	{
		if(!canShowWarehouseDepositList(WarehouseType.CLAN))
		{
			sendActionFailed();
			return;
		}

		if(!(isClanLeader() || (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || getVarB("canWhWithdraw")) && (getClanPrivileges() & Clan.CP_CL_WAREHOUSE_SEARCH) == Clan.CP_CL_WAREHOUSE_SEARCH))
			sendPacket(SystemMsg.ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE);

		setUsingWarehouseType(WarehouseType.CLAN);
		sendPacket(new WareHouseDepositList(this, WarehouseType.CLAN));
	}

	public void showWithdrawWindowClan(int val)
	{
		if(!canShowWarehouseWithdrawList(WarehouseType.CLAN))
		{
			sendActionFailed();
			return;
		}

		setUsingWarehouseType(WarehouseType.CLAN);
		sendPacket(new WareHouseWithdrawList(this, WarehouseType.CLAN, ItemClass.values()[val]));
	}

	public boolean canShowWarehouseWithdrawList(WarehouseType type)
	{
		Warehouse warehouse = null;
		switch(type)
		{
			case PRIVATE:
				warehouse = getWarehouse();
				break;
			case FREIGHT:
				warehouse = getFreight();
				break;
			case CLAN:
			case CASTLE:

				if(getClan() == null || getClan().getLevel() == 0)
				{
					sendPacket(SystemMsg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
					return false;
				}

				boolean canWithdrawCWH = false;
				if(getClan() != null)
					if((getClanPrivileges() & Clan.CP_CL_WAREHOUSE_SEARCH) == Clan.CP_CL_WAREHOUSE_SEARCH)
						canWithdrawCWH = true;
				if(!canWithdrawCWH)
				{
					sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
					return false;
				}
				warehouse = getClan().getWarehouse();
				break;
			default:
				return false;
		}

		if(warehouse.getSize() == 0)
		{
			sendPacket(type == WarehouseType.FREIGHT ? SystemMsg.NO_PACKAGES_HAVE_ARRIVED : SystemMsg.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
			return false;
		}

		return true;
	}

	public boolean canShowWarehouseDepositList(WarehouseType type)
	{
		switch(type)
		{
			case PRIVATE:
				return true;
			case CLAN:
			case CASTLE:

				if(getClan() == null || getClan().getLevel() == 0)
				{
					sendPacket(SystemMsg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
					return false;
				}

				boolean canWithdrawCWH = false;
				if(getClan() != null)
					if((getClanPrivileges() & Clan.CP_CL_WAREHOUSE_SEARCH) == Clan.CP_CL_WAREHOUSE_SEARCH)
						canWithdrawCWH = true;
				if(!canWithdrawCWH)
				{
					sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
					return false;
				}
				return true;
			default:
				return false;
		}
	}

	public boolean isNotShowBuffAnim()
	{
		return _notShowBuffAnim;
	}

	public void setNotShowBuffAnim(boolean value)
	{
		_notShowBuffAnim = value;
	}

	public void enterMovieMode()
	{
		if(isInMovie()) //already in movie
			return;
		
		setTarget(null);
		stopMove();
		setIsInMovie(true);
		//sendPacket(new CameraMode(1));
		sendPacket(new Revive(this));
	}

	public void leaveMovieMode()
	{
		setIsInMovie(false);
		//sendPacket(new CameraMode(0));
		broadcastCharInfo();
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen ,unk));
	}

	private int _movieId = 0;
	private boolean _isInMovie;

	public void setMovieId(int id)
	{
		_movieId = id;
	}

	public int getMovieId()
	{
		return _movieId;
	}

	public boolean isInMovie()
	{
		return _isInMovie;
	}

	public void setIsInMovie(boolean state)
	{
		_isInMovie = state;
	}

	public void showQuestMovie(SceneMovie movie)
	{
		if(isInMovie()) //already in movie
			return;

		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movie.getId());
		setIsInMovie(true);
		sendPacket(movie.packet(this));
	}

	public void showQuestMovie(int movieId)
	{
		if(isInMovie()) //already in movie
			return;

		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movieId);
		setIsInMovie(true);
		sendPacket(new ExStartScenePlayer(movieId));
	}

	public void setAutoLoot(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			_autoLoot = enable;
			setVar("AutoLoot", String.valueOf(enable), -1);
		}
	}

	public void setAutoLootHerbs(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			_autoLootHerbs = enable;
			setVar("AutoLootHerbs", String.valueOf(enable), -1);
		}
	}

	public void setAutoLootOnlyAdena(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			_autoLootOnlyAdena = enable;
			setVar("AutoLootOnlyAdena", String.valueOf(enable), -1);
		}
	}

	public boolean isAutoLootEnabled()
	{
		return _autoLoot;
	}

	public boolean isAutoLootHerbsEnabled()
	{
		return _autoLootHerbs;
	}

	public boolean isAutoLootOnlyAdenaEnabled()
	{
		return _autoLootOnlyAdena;
	}

	public final void reName(String name, boolean saveToDB)
	{
		setName(name);
		if(saveToDB)
			saveNameToDB();
		Olympiad.changeNobleName(getObjectId(), name);
		broadcastCharInfo();
		
		if(_clan != null)
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
	}

	public final void reName(String name)
	{
		reName(name, false);
	}

	public final void saveNameToDB()
	{
		Connection con = null;
		PreparedStatement st = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ?");
			st.setString(1, getName());
			st.setInt(2, getObjectId());
			st.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, st);
		}
	}

	@Override
	public Player getPlayer()
	{
		return this;
	}

	private List<String> getStoredBypasses(boolean bbs)
	{
		if(bbs)
		{
			if(bypasses_bbs == null)
				bypasses_bbs = new ArrayList<String>();
			return bypasses_bbs;
		}
		if(bypasses == null)
			bypasses = new ArrayList<String>();
		return bypasses;
	}

	public void cleanBypasses(boolean bbs)
	{
		List<String> bypassStorage = getStoredBypasses(bbs);
		synchronized(bypassStorage)
		{
			bypassStorage.clear();
		}
	}

	public String encodeBypasses(String htmlCode, boolean bbs)
	{
		List<String> bypassStorage = getStoredBypasses(bbs);
		synchronized(bypassStorage)
		{
			return BypassManager.encode(htmlCode, bypassStorage, bbs);
		}
	}

	public DecodedBypass decodeBypass(String bypass)
	{
		BypassType bpType = BypassManager.getBypassType(bypass);
		boolean bbs = bpType == BypassType.ENCODED_BBS || bpType == BypassType.SIMPLE_BBS;
		List<String> bypassStorage = getStoredBypasses(bbs);
		if(bpType == BypassType.ENCODED || bpType == BypassType.ENCODED_BBS)
			return BypassManager.decode(bypass, bypassStorage, bbs, this);
		if(bpType == BypassType.SIMPLE || bpType == BypassType.SIMPLE_BBS)
			return new DecodedBypass(bypass).trim();
		_log.warn("Direct access to bypass: " + bypass + " / Player: " + getName());
		return null;
	}

	public int getTalismanCount()
	{
		return (int) calcStat(Stats.TALISMANS_LIMIT, 0, null, null);
	}

	public boolean getOpenCloak()
	{
		if(Config.ALT_OPEN_CLOAK_SLOT)
			return true;
		return (int) calcStat(Stats.CLOAK_SLOT, 0, null, null) > 0;
	}

	public final void disableDrop(int time)
	{
		_dropDisabled = System.currentTimeMillis() + time;
	}

	public final boolean isDropDisabled()
	{
		return _dropDisabled > System.currentTimeMillis();
	}

	private ItemInstance _petControlItem = null;

	public void setPetControlItem(int itemObjId)
	{
		setPetControlItem(getInventory().getItemByObjectId(itemObjId));
	}

	public void setPetControlItem(ItemInstance item)
	{
		_petControlItem = item;
	}

	public ItemInstance getPetControlItem()
	{
		return _petControlItem;
	}

	private AtomicBoolean isActive = new AtomicBoolean();

	public boolean isActive()
	{
		return isActive.get();
	}

	public void setActive()
	{
		setNonAggroTime(0);

		if(isActive.getAndSet(true))
			return;

		onActive();
	}

	private void onActive()
	{
		setNonAggroTime(0);
		sendPacket(Msg.YOU_ARE_PROTECTED_AGGRESSIVE_MONSTERS);
		if(getPetControlItem() != null)
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				@Override
				public void runImpl()
				{
					if(getPetControlItem() != null)
						summonPet();
				}

			});
	}

	public void summonPet()
	{
		if(getPet() != null)
			return;

		ItemInstance controlItem = getPetControlItem();
		if(controlItem == null)
			return;

		int npcId = PetDataTable.getSummonId(controlItem);
		if(npcId == 0)
			return;

		NpcTemplate petTemplate = NpcHolder.getInstance().getTemplate(npcId);
		if(petTemplate == null)
			return;

		PetInstance pet = PetInstance.restore(controlItem, petTemplate, this);
		if(pet == null)
			return;

		setPet(pet);
		pet.setTitle(getName());

		if(!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp(), false);
			pet.setCurrentMp(pet.getMaxMp());
			pet.setCurrentFed(pet.getMaxFed());
			pet.updateControlItem();
			pet.store();
		}

		pet.getInventory().restore();

		EffectsDAO.getInstance().restoreEffects(pet);
		
		pet.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		pet.setReflection(getReflection());
		pet.spawnMe(Location.findPointToStay(this, 50, 70));
		pet.setRunning();
		pet.setFollowMode(true);
		pet.getInventory().validateItems();

		if(pet instanceof PetBabyInstance)
			((PetBabyInstance) pet).startBuffTask();
	}

	private Map<Integer, Long> _traps;

	public Collection<TrapInstance> getTraps()
	{
		if(_traps == null)
			return null;
		Collection<TrapInstance> result = new ArrayList<TrapInstance>(getTrapsCount());
		TrapInstance trap;
		for(Integer trapId : _traps.keySet())
			if((trap = (TrapInstance) GameObjectsStorage.get(_traps.get(trapId))) != null)
				result.add(trap);
			else
				_traps.remove(trapId);
		return result;
	}

	public int getTrapsCount()
	{
		return _traps == null ? 0 : _traps.size();
	}

	public void addTrap(TrapInstance trap)
	{
		if(_traps == null)
			_traps = new HashMap<Integer, Long>();
		_traps.put(trap.getObjectId(), trap.getStoredId());
	}

	public void removeTrap(TrapInstance trap)
	{
		Map<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		traps.remove(trap.getObjectId());
	}

	public void destroyFirstTrap()
	{
		Map<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		TrapInstance trap;
		for(Integer trapId : traps.keySet())
		{
			if((trap = (TrapInstance) GameObjectsStorage.get(traps.get(trapId))) != null)
			{
				trap.deleteMe();
				return;
			}
			return;
		}
	}

	public void destroyAllTraps()
	{
		Map<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		List<TrapInstance> toRemove = new ArrayList<TrapInstance>();
		for(Integer trapId : traps.keySet())
			toRemove.add((TrapInstance) GameObjectsStorage.get(traps.get(trapId)));
		for(TrapInstance t : toRemove)
			if(t != null)
				t.deleteMe();
	}

	public void setBlockCheckerArena(byte arena)
	{
		_handysBlockCheckerEventArena = arena;
	}

	public int getBlockCheckerArena()
	{
		return _handysBlockCheckerEventArena;
	}

	@Override
	public PlayerListenerList getListeners()
	{
		if(listeners == null)
			synchronized(this)
			{
				if(listeners == null)
					listeners = new PlayerListenerList(this);
			}
		return (PlayerListenerList) listeners;
	}

	@Override
	public PlayerStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized(this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new PlayerStatsChangeRecorder(this);
			}
		return (PlayerStatsChangeRecorder) _statsRecorder;
	}

	private Future<?> _hourlyTask;
	private int _hoursInGame = 0;

	public int getHoursInGame()
	{
		_hoursInGame++;
		return _hoursInGame;
	}

	public void startHourlyTask()
	{
		_hourlyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HourlyTask(this), 3600000L, 3600000L);
	}

	public void stopHourlyTask()
	{
		if(_hourlyTask != null)
		{
			_hourlyTask.cancel(false);
			_hourlyTask = null;
		}
	}

	public long getPremiumPoints()
	{
		if(Config.GAME_POINT_ITEM_ID != -1)
			return ItemFunctions.getItemCount(this, Config.GAME_POINT_ITEM_ID);
		else
			return getClient().getPointG();
	}

	public void reducePremiumPoints(final int val)
	{
		int reduce = (getClient().getPointG() - (val));
		if(Config.GAME_POINT_ITEM_ID != -1)
			ItemFunctions.removeItem(this, Config.GAME_POINT_ITEM_ID, val, true);
		else
			getClient().setPointG(reduce);
	}

	private boolean _agathionResAvailable = false;

	public boolean isAgathionResAvailable()
	{
		return _agathionResAvailable;
	}

	public void setAgathionRes(boolean val)
	{
		_agathionResAvailable = val;
	}

	public boolean isClanAirShipDriver()
	{
		return isInBoat() && getBoat().isClanAirShip() && ((ClanAirShip) getBoat()).getDriver() == this;
	}

	/**
	 * _userSession - испольюзуется для хранения временных переменных.
	 */
	private Map<String, String> _userSession;

	public String getSessionVar(String key)
	{
		if(_userSession == null)
			return null;
		return _userSession.get(key);
	}

	public void setSessionVar(String key, String val)
	{
		if(_userSession == null)
			_userSession = new ConcurrentHashMap<String, String>();

		if(val == null || val.isEmpty())
			_userSession.remove(key);
		else
			_userSession.put(key, val);
	}

	public FriendList getFriendList()
	{
		return _friendList;
	}

	public boolean isNotShowTraders()
	{
		return _notShowTraders;
	}

	public void setNotShowTraders(boolean notShowTraders)
	{
		_notShowTraders = notShowTraders;
	}

	public boolean disableFogAndRain()
	{
		return _disableFogAndRain;
	}

	public void setDisableFogAndRain(boolean b)
	{
		_disableFogAndRain = b;
	}
	
	public boolean isDebug()
	{
		return _debug;
	}

	public void setDebug(boolean b)
	{
		_debug = b;
	}

	public void sendItemList(boolean show)
	{
		ItemInstance[] items = getInventory().getItems();
		LockType lockType = getInventory().getLockType();
		int[] lockItems = getInventory().getLockItems();

		int allSize = items.length;
		int questItemsSize = 0;
		int agathionItemsSize = 0;
		for(ItemInstance item : items)
		{
			if(item.getTemplate().isQuest())
				questItemsSize++;
			if(item.getTemplate().getAgathionEnergy() > 0)
				agathionItemsSize ++;
		}

		sendPacket(new ItemList(allSize - questItemsSize, items, show, lockType, lockItems));
		if(questItemsSize > 0)
			sendPacket(new ExQuestItemList(questItemsSize, items, lockType, lockItems));
		if(agathionItemsSize > 0)
			sendPacket(new ExBR_AgathionEnergyInfo(agathionItemsSize, items));
	}

	public int getBeltInventoryIncrease()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_BELT);
		if(item != null && item.getTemplate().getAttachedSkills() != null)
			for(Skill skill : item.getTemplate().getAttachedSkills())
				for(FuncTemplate func : skill.getAttachedFuncs())
					if(func._stat == Stats.INVENTORY_LIMIT)
						return (int) func._value;
		return 0;
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	public boolean checkCoupleAction(Player target)
	{
		if(target.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IN_PRIVATE_STORE).addName(target));
			return false;
		}
		if(target.isFishing())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_FISHING).addName(target));
			return false;
		}
		if(target.isInCombat())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_COMBAT).addName(target));
			return false;
		}
		if(target.isCursedWeaponEquipped())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_CURSED_WEAPON_EQUIPED).addName(target));
			return false;
		}
		if(target.isInOlympiadMode())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_OLYMPIAD).addName(target));
			return false;
		}
		if(target.isOnSiegeField())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_SIEGE).addName(target));
			return false;
		}
		if(target.isInBoat() || target.getMountNpcId() != 0)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_VEHICLE_MOUNT_OTHER).addName(target));
			return false;
		}
		if(target.isTeleporting())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_TELEPORTING).addName(target));
			return false;
		}
		if(target.getTransformation() != 0)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_TRANSFORM).addName(target));
			return false;
		}
		if(target.isDead())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_DEAD).addName(target));
			return false;
		}
		return true;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		Summon summon = getPet();
		if(summon != null)
			summon.startAttackStanceTask0();
	}

	@Override
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		super.displayGiveDamageMessage(target, damage, crit, miss, shld, magic);
		
		if (Config.ALT_DAMAGE_INVIS && !GeoEngine.canSeeTarget(this, target, false))
		{
			sendMessage(target.getName() + (isLangRus() ? " укрылся от Вашей атаки." : " steal away from your attack."));
			return;
		}
		
		if(crit)
		{
			if(magic)
			{
				if (Config.ENABLE_PLAYER_COUNTERS)
					getCounters().addPoint("_Mcrits_Done");
				
				sendPacket(new SystemMessage(SystemMessage.MAGIC_CRITICAL_HIT).addName(this));
			}
			else
			{
				if (Config.ENABLE_PLAYER_COUNTERS)
					getCounters().addPoint("_Crits_Done");
				
				sendPacket(new SystemMessage(SystemMessage.C1_HAD_A_CRITICAL_HIT).addName(this));
			}
		}
		if(miss)
			sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(this));
		else if(!target.isDamageBlocked())
			sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(this).addName(target).addNumber(damage));

		if(target.isPlayer())
		{
			if(shld && damage > 1)
				target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
			else if(shld && damage == 1)
				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
		}
	}

	@Override
	public void displayReceiveDamageMessage(Creature attacker, int damage)
	{
		if (Config.ALT_DAMAGE_INVIS && !GeoEngine.canSeeTarget(this, attacker, false))
		{
			sendMessage(isLangRus() ? "Вы укрылись от атаки " : "You fled from the attack " + attacker.getName() + ".");
			return;
		}
		
		if(attacker != this)
			sendPacket(new SystemMessage(SystemMessage.C1_HAS_RECEIVED_DAMAGE_OF_S3_FROM_C2).addName(this).addName(attacker).addNumber((long) damage));
	}

	public IntObjectMap<String> getPostFriends()
	{
		return _postFriends;
	}

	public boolean isSharedGroupDisabled(int groupId)
	{
		TimeStamp sts = _sharedGroupReuses.get(groupId);
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_sharedGroupReuses.remove(groupId);
		return false;
	}

	public TimeStamp getSharedGroupReuse(int groupId)
	{
		return _sharedGroupReuses.get(groupId);
	}

	public void addSharedGroupReuse(int group, TimeStamp stamp)
	{
		_sharedGroupReuses.put(group, stamp);
	}

	public Collection<IntObjectMap.Entry<TimeStamp>> getSharedGroupReuses()
	{
		return _sharedGroupReuses.entrySet();
	}

	public void sendReuseMessage(ItemInstance item)
	{
		TimeStamp sts = getSharedGroupReuse(item.getTemplate().getReuseGroup());
		if(sts == null || !sts.hasNotPassed())
			return;

		long timeleft = sts.getReuseCurrent();
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.);

		if(hours > 0)
			sendPacket(new SystemMessage2(item.getTemplate().getReuseType().getMessages()[2]).addItemName(item.getTemplate().getItemId()).addInteger(hours).addInteger(minutes).addInteger(seconds));
		else if(minutes > 0)
			sendPacket(new SystemMessage2(item.getTemplate().getReuseType().getMessages()[1]).addItemName(item.getTemplate().getItemId()).addInteger(minutes).addInteger(seconds));
		else
			sendPacket(new SystemMessage2(item.getTemplate().getReuseType().getMessages()[0]).addItemName(item.getTemplate().getItemId()).addInteger(seconds));
	}

	public NevitSystem getNevitSystem()
	{
		return _nevitSystem;
	}

	public void ask(ConfirmDlg dlg, OnAnswerListener listener)
	{
		if(_askDialog != null)
			return;
		int rnd = Rnd.nextInt();
		_askDialog = new ImmutablePair<Integer, OnAnswerListener>(rnd, listener);
		dlg.setRequestId(rnd);
		sendPacket(dlg);
	}

	public Pair<Integer, OnAnswerListener> getAskListener(boolean clear)
	{
		if(!clear)
			return _askDialog;
		else
		{
			Pair<Integer, OnAnswerListener> ask = _askDialog;
			_askDialog = null;
			return ask;
		}
	}

	@Override
	public boolean isDead()
	{
		return (isInOlympiadMode() || isInDuel()) ? getCurrentHp() <= 1. : super.isDead();
	}

	@Override
	public int getAgathionEnergy()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		return item == null ? 0 : item.getAgathionEnergy();
	}

	@Override
	public void setAgathionEnergy(int val)
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		if(item == null)
			return;
		item.setAgathionEnergy(val);
		item.setJdbcState(JdbcEntityState.UPDATED);

		sendPacket(new ExBR_AgathionEnergyInfo(1, item));
	}

	public boolean hasPrivilege(Privilege privilege)
	{
		return _clan != null && (getClanPrivileges() & privilege.mask()) == privilege.mask();
	}

	public MatchingRoom getMatchingRoom()
	{
		return _matchingRoom;
	}
	
	public void setMatchingRoom(MatchingRoom matchingRoom)
	{
		_matchingRoom = matchingRoom;
		if (matchingRoom == null)
			_matchingRoomWindowOpened = false;
	}

	public boolean isMatchingRoomWindowOpened()
	{
		return _matchingRoomWindowOpened;
	}

	public void setMatchingRoomWindowOpened(boolean b)
	{
		_matchingRoomWindowOpened = b;
	}
	
	public void dispelBuffs()
	{
		for(Effect e : getEffectList().getAllEffects())
			if(!e.getSkill().isOffensive() && !e.getSkill().isNewbie() && e.isCancelable() && !e.getSkill().isPreservedOnDeath())
			{
				sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
				e.exit();
			}
		if(getPet() != null)
			for(Effect e : getPet().getEffectList().getAllEffects())
				if(!e.getSkill().isOffensive() && !e.getSkill().isNewbie() && e.isCancelable() && !e.getSkill().isPreservedOnDeath())
					e.exit();
	}

	public void setInstanceReuse(int id, long time)
	{
		final SystemMessage msg = new SystemMessage(SystemMessage.INSTANT_ZONE_FROM_HERE__S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_ENTRY_POSSIBLE).addString(getName());
		sendPacket(msg);
		_instancesReuses.put(id, time);
		mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", getObjectId(), id, time);
	}

	public void removeInstanceReuse(int id)
	{
		if(_instancesReuses.remove(id) != null)
			mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=? AND `id`=? LIMIT 1", getObjectId(), id);
	}

	public void removeAllInstanceReuses()
	{
		_instancesReuses.clear();
		mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=?", getObjectId());
	}

	public void removeInstanceReusesByGroupId(int groupId)
	{
		for(int i : InstantZoneHolder.getInstance().getSharedReuseInstanceIdsByGroup(groupId))
			if(getInstanceReuse(i) != null)
				removeInstanceReuse(i);
	}

	public Long getInstanceReuse(int id)
	{
		return _instancesReuses.get(id);
	}

	public Map<Integer, Long> getInstanceReuses()
	{
		return _instancesReuses;
	}

	private void loadInstanceReuses()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM character_instances WHERE obj_id = ?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				int id = rs.getInt("id");
				long reuse = rs.getLong("reuse");
				_instancesReuses.put(id, reuse);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public Reflection getActiveReflection()
	{
		for(Reflection r : ReflectionManager.getInstance().getAll())
			if(r != null && ArrayUtils.contains(r.getVisitors(), getObjectId()))
				return r;
		return null;
	}

	public boolean canEnterInstance(int instancedZoneId)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);

		if(isDead())
			return false;

		if(ReflectionManager.getInstance().size() > Config.MAX_REFLECTIONS_COUNT)
		{
			sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
			return false;
		}

		if(iz == null)
		{
			sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}
		
		if (canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
			return true;

		if(ReflectionManager.getInstance().getCountByIzId(instancedZoneId) >= iz.getMaxChannels())
		{
			sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
			return false;
		}
		
		if (isTerritoryFlagEquipped())
		{
			sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}

		return iz.getEntryType().canEnter(this, iz);
	}

	public boolean canReenterInstance(int instancedZoneId)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if(getActiveReflection() != null && getActiveReflection().getInstancedZoneId() != instancedZoneId)
		{
			sendPacket(SystemMsg.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
			return false;
		}
		
		if (canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
			return true;
		
		if(iz.isDispelBuffs())
			dispelBuffs();
		
		return iz.getEntryType().canReEnter(this, iz);
	}

	public int getBattlefieldChatId()
	{
		return _battlefieldChatId;
	}

	public void setBattlefieldChatId(int battlefieldChatId)
	{
		_battlefieldChatId = battlefieldChatId;
	}

	@Override
	public Iterator<Player> iterator()
	{
		return Collections.singleton(this).iterator();
	}
	
	@Override
	public int size()
	{
		return 1;
	}

	@Override
	public Player getLeader()
	{
		return this;
	}
	
	@Override
	public boolean isLeader(Player player)
	{
		return true;
	}

	@Override
	public List<Player> getMembers(Player ... excluded)
	{
		if (excluded != null && Util.contains(excluded, this))
			return Collections.emptyList();
		
		return Arrays.asList(this);
	}

	@Override
	public boolean containsMember(Player player)
	{
		return this == player;
	}

	public PlayerGroup getPlayerGroup()
	{
		if(getParty() != null)
		{
			if(getParty().getCommandChannel() != null)
				return getParty().getCommandChannel();
			else
				return getParty();
		}
		else
			return this;
	}

	public boolean isActionBlocked(String action)
	{
		return _blockedActions.contains(action);
	}

	public void blockActions(String... actions)
	{
		Collections.addAll(_blockedActions, actions);
	}

	public void unblockActions(String... actions)
	{
		for(String action : actions)
			_blockedActions.remove(action);
	}

	public OlympiadGame getOlympiadGame()
	{
		return _olympiadGame;
	}

	public void setOlympiadGame(OlympiadGame olympiadGame)
	{
		_olympiadGame = olympiadGame;
	}

	public OlympiadGame getOlympiadObserveGame()
	{
		return _olympiadObserveGame;
	}

	public void setOlympiadObserveGame(OlympiadGame olympiadObserveGame)
	{
		_olympiadObserveGame = olympiadObserveGame;
	}
	
	public Supersnoop getSupersnoop()
	{
		if (_supersnoop == null)
			_supersnoop = new Supersnoop(this);
		
		return _supersnoop;
	}

	public void addRadar(int x, int y, int z)
	{
		sendPacket(new RadarControl(2, 2, x, y, z));
		sendPacket(new RadarControl(0, 1, x, y, z));
	}

	public void addRadar(Location loc)
	{
		addRadar(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public void addRadarWithMap(int x, int y, int z, boolean message)
	{
		sendPacket(new RadarControl(2, 2, x, y, z));
		sendPacket(new RadarControl(0, 2, x, y, z));
		if (message)
			sendMessage(new CustomMessage("scripts.services.community.communityboarddroplist.radar", this));
	}

	public void addRadarWithMap(Location loc)
	{
		addRadarWithMap(loc.getX(), loc.getY(), loc.getZ(), true);
	}
	
	public PetitionMainGroup getPetitionGroup()
	{
		return _petitionGroup;
	}

	public void setPetitionGroup(PetitionMainGroup petitionGroup)
	{
		_petitionGroup = petitionGroup;
	}

	public int getLectureMark()
	{
		return _lectureMark;
	}

	public void setLectureMark(int lectureMark)
	{
		_lectureMark = lectureMark;
	}
	
	public BookMarkList getTeleportBookmarks()
	{
		return _teleportBookmarks;
	}

	public void setLastHeroTrue(boolean value)
	{
		setHero(value);
	}
	
	public void setAccountAccesslevel(final int level, final String comments, int banTime)
	{
		AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(getAccountName(), level, banTime));
	}

	private static void RestoreFightClub(Player player)
	{
		String[] values = player.getVar("FightClubRate").split(";");
		int id = Integer.parseInt(values[0]);
		int count = Integer.parseInt(values[1]);
		ItemFunctions.addItem(player, id, count, true);
		player.unsetVar("FightClubRate");
		player.unsetVar("isPvPevents");
	}

	private void restoreCursedWeapon()
	{
		for(ItemInstance item : getInventory().getItems())
			if(item.isCursed())
			{
				int skillLvl = CursedWeaponsManager.getInstance().getLevel(item.getItemId());
				if(item.getItemId() == 8190)
					addSkill(SkillTable.getInstance().getInfo(3603, skillLvl), false);
				else if(item.getItemId() == 8689)
					addSkill(SkillTable.getInstance().getInfo(3629, skillLvl), false);
			}
		updateStats();
	}

	private int _pvp_team = 0;

	public void setPvPTeam(final int team)
	{
		if(_pvp_team != team)
		{
			_pvp_team = team;
			broadcastUserInfo(true);
			if(getPet() != null)
				getPet().broadcastCharInfo();
		}
	}

	public void allowPvPTeam()
	{
		if(_pvp_team == 0)
			setTeam(TeamType.NONE);
		else if(_pvp_team == 2)
			setTeam(TeamType.RED);
		else if(_pvp_team == 1)
			setTeam(TeamType.BLUE);
	}

	@Override
	public void setTeam(TeamType t)
	{
		super.setTeam(t);
		sendChanges();
		Summon pet = getPet();
		if(pet != null)
			pet.sendChanges();
	}
	
	@Override
	public int getPvPTeam()
	{
		return _pvp_team;
	}

	public Map<Integer, Skill> getTransformationSkills()
	{
		return _transformationSkills;
	}
	
	public void startUnjailTask(Player player, long timeInSeconds, boolean msg)
	{
		if(timeInSeconds < 1)
			return;

		if(_unjailTask != null)
			_unjailTask.cancel(false);

		_unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player, msg), timeInSeconds * 1000);
	}

	public void stopUnjailTask()
	{
		if(_unjailTask != null)
			_unjailTask.cancel(false);
		_unjailTask = null;
	}
	private AutoHuntingPunish _AutoHuntingPunish = null;
	
	/**
	* Initializes his _botPunish object with the specified punish
	* and for the specified time
	* @param punishType
	* @param minsOfPunish
	*/
	public synchronized void setPunishDueBotting(AutoHuntingPunish.Punish punishType, int minsOfPunish)
	{
		if(_AutoHuntingPunish == null)
			_AutoHuntingPunish = new AutoHuntingPunish(punishType, minsOfPunish);
	}
	
	/**
	* Returns the current object-representative player punish
	* @return
	*/
	public AutoHuntingPunish getPlayerPunish()
	{
		return _AutoHuntingPunish;
	}
	
	/**
	* Returns the type of punish being applied
	* @return
	*/
	public AutoHuntingPunish.Punish getBotPunishType()
	{
		return _AutoHuntingPunish.getBotPunishType();
	}
	
	/**
	* Will return true if the player has any bot punishment
	* active
	* @return
	*/
	public boolean isBeingPunished()
	{
		return _AutoHuntingPunish != null;
	}
	
	private AccountReportDAO _account = null;
	
	public AccountReportDAO getReportedAccount()
	{
		return _account;
	}
	
	public void setReportedAccount(String accountName)
	{
		_account = new AccountReportDAO(accountName);
	}
	
	/**
	* Will end the punishment once a player attempt to
	* perform any forbid action and his punishment has
	* expired
	*/
	public void endPunishment()
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM bot_reported_punish WHERE charId = ?");
			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(SQLException sqle) { }
		}
		_AutoHuntingPunish = null;
		sendMessage(isLangRus() ? "Ваша наказания истек. Не бот снова!" : "Your punishment has expired. Do not bot again!");
	}

	private boolean _inEvent = false;

	public boolean isInEvent()
	{
		return _inEvent;
	}
  
	public void setInEvent(boolean param)
	{
		_inEvent = param;
	}
	
	private FastMap<Integer, Integer> _achievementLevels = new FastMap<>();
	private boolean _achievementRewarding = false;

	public boolean achievement_nf_open;

	public boolean isRewardingAchievement()
	{
		return _achievementRewarding;
	}

	public void setIsRewardingAchievement(boolean val)
	{
		_achievementRewarding = val;
	}

	public int getAchievementLevelbyId(int id)
	{
		for(Entry<Integer, Integer> a : _achievementLevels.entrySet())
		{
			if(a.getKey() == id)
				return a.getValue();
		}
		return 0;
	}

	public boolean getAchievementLevel(int id, int level)
	{
		for(Entry<Integer, Integer> a : _achievementLevels.entrySet())
		{
			if(a.getKey() == id && a.getValue() == level)
				return true;
		}
		return false;
	}

	public FastMap<Integer, Integer> getAchievements(int category)
	{
		FastMap<Integer, Integer> temp = new FastMap<>();
		for(Entry<Integer, Integer> lol : _achievementLevels.entrySet())
		{
			if(Achievements.getInstance().GetAchievement(lol.getKey(), lol.getValue()).getCat() == category)
				temp.put(lol.getKey(), lol.getValue());
		}
		return temp;
	}

	public int getAchievementsLevel(int category)
	{
		int i = 0;
		for(Entry<Integer, Integer> lol : _achievementLevels.entrySet())
		{
			if(Achievements.getInstance().GetAchievement(lol.getKey(), lol.getValue()).getCat() == category)
				i += lol.getValue();
		}
		return i;
	}

	public FastMap<Integer, Integer> getAchievements()
	{
		return _achievementLevels;
	}

	public iAchievement getAchievementByType(String type)
	{
		for(Entry<Integer, Integer> lol : _achievementLevels.entrySet())
		{
			iAchievement aaaa = Achievements.getInstance().GetAchievement(lol.getKey(), lol.getValue() + 1);
			if(aaaa.getType().equals(type))
				return aaaa;
		}
		return null;
	}

	private void loadAchivementLevel()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM character_achievement_levels WHERE char_id = " + getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				String achievements = rset.getString("achievement_levels");

				if(achievements.isEmpty())
					continue;

				String[] levels = achievements.split(";");

				for(String ach : levels)
				{
					String[] lvl = ach.split(",");
					for(Entry<Integer, Integer> o : Achievements.getInstance()._achievementsLevels.entrySet())
					{
						if (o.getKey().intValue() != Integer.parseInt(lvl[0]))
							continue;
								
						_achievementLevels.put(Integer.parseInt(lvl[0]), Integer.parseInt(lvl[1]));
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		for(Entry<Integer, Integer> o : Achievements.getInstance()._achievementsLevels.entrySet())
			if(!_achievementLevels.containsKey(o.getKey()))
				_achievementLevels.put(o.getKey(), 0);
	}

	private void saveAchivementsLevels()
	{
		String AchievementString = "";

		for(Entry<Integer, Integer> a : _achievementLevels.entrySet())
			AchievementString += a.getKey() + "," + a.getValue() + ";";
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("replace into character_achievement_levels (char_id,achievement_levels) values(?,?)");
			statement.setInt(1, getObjectId());
			statement.setString(2, AchievementString);
			statement.executeUpdate();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warn("store: could not store char achievements data: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	private PlayerCounters _playerCountersExtension = null;

	public PlayerCounters getCounters()
	{
		if (!Config.ENABLE_ACHIEVEMENTS) // Return Dummy counters when achievements arent enabled.
			return PlayerCounters.DUMMY_COUNTER;
		
		if(_playerCountersExtension == null)
			_playerCountersExtension = new PlayerCounters(this);
		return _playerCountersExtension;
	}
	
	private boolean _isSecured = true;
	private String _securityPassword = null;
	private byte _securityRemainingTries = 0;
	
	public boolean getSecurity()
	{
		return (!Config.SECURITY_FORCE && _securityPassword == null ? false : _isSecured);
	}
	
	public void setSecurity(boolean val)
	{
		_isSecured = val;
	}
	
	public void setSecurityRemainingTries(byte val)
	{
		_securityRemainingTries = val;
		updateSecurityTries();
	}
	
	public byte getSecurityRemainingTries()
	{
		return _securityRemainingTries;
	}
	
	public String getSecurityPassword()
	{
		return _securityPassword;
	}
	
	public void setSecurityPassword(String val)
	{
		_securityPassword = val;
	}
	
	public void loadSecurity()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT password, remainingTries FROM character_security WHERE charId = ?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();
			if (rset.next())
			{
				if (rset.getString("password") == null || rset.getString("password").length() < 1)
				{
					sendChatMessage(getPlayer().getObjectId(), ChatType.TELL.ordinal(), "SECURITY", (getPlayer().isLangRus() ? "Ваша безопасность отключена. Пожалуйста, включите его, набрав .security" : "Your security is disabled. Please enable it by typing .security"));
				}
				else
				{
					_securityPassword = rset.getString("password");
					_securityRemainingTries = rset.getByte("remainingTries");
					_isSecured = true;
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not restore security password: " + e.getMessage() + " for " + getName(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public void updateSecurityTries()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("UPDATE `character_security` SET `remainingTries`=? WHERE `charId`=?");
			statement.setInt(1, _securityRemainingTries);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("Could not store security password: " + e.getMessage() + " for " + getName(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void saveSecurity()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if (getSecurityPassword() == null)
			{
				statement = con.prepareStatement("UPDATE `character_security` SET `password`=?, `changeDate`=?, `changeHWID`=?, `remainingTries`=? WHERE `charId`=?");
				statement.setString(1, null);
				statement.setLong(2, System.currentTimeMillis());
				statement.setString(3, getHWID());
				statement.setInt(4, 3); // 3 tries left on successful change
				statement.setInt(5, getObjectId());
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO character_security (charId, password, activationDate, activationHWID) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE password=?, changeDate=?, changeHWID=?");
				statement.setInt(1, getObjectId());
				statement.setString(2, getSecurityPassword());
				statement.setLong(3, System.currentTimeMillis());
				statement.setString(4, getHWID());
				
				// On duplicate key - the char already has security set
				statement.setString(5, getSecurityPassword());
				statement.setLong(6, System.currentTimeMillis());
				statement.setString(7, getHWID());
			}
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("Could not store security password: " + e.getMessage() + " for " + getName(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	// Ping
	public int getPing()
	{
		return _ping;
	}

	public void setPing(int ping)
	{
		_ping = ping;
	}
	
	// MTU
	public int getMTU()
	{
		return _mtu;
	}

	public void setMTU(int mtu)
	{
		_mtu = mtu;
	}
	
	public void sendMessageS(String text, int timeonscreenins)
	{
		sendPacket(new ExShowScreenMessage(text, timeonscreenins * 1000, ScreenMessageAlign.TOP_CENTER, text.length() > 30 ? false : true));
	}
	
	public void acceptPM(int acceptFromObjId)
	{
		_acceptedPMs.add(acceptFromObjId);
	}
	
	public boolean canAcceptPM(int acceptFromObjId)
	{
		return _acceptedPMs.contains(acceptFromObjId);
	}
	
	public final Map<String, PlayerBuffProfile> _profiles = new HashMap<>(Config.MAX_SCHEME_PROFILES);
	
	public void addProfile(PlayerBuffProfile profile)
	{
		_profiles.put(profile.profileName, profile);
	}
	
	public void removeProfile(PlayerBuffProfile profile)
	{
		_profiles.remove(profile.profileName);
	}
	
	public void removeProfile(String profileName)
	{
		_profiles.remove(profileName);
	}
	
	public PlayerBuffProfile getProfile(String profileName)
	{
		return _profiles.get(profileName);
	}
	
	public Map<String, PlayerBuffProfile> getProfiles()
	{
		return _profiles;
	}
	
	public void delOlympiadIpHWID()
	{
		Olympiad.getOlyIPList().remove(getIP());
		Olympiad.getOlyHwidList().remove(getHWID());
	}
	
	public GameEvent getGameEvent()
	{
		return _event;
	}
	
	private PlayerEventStatus eventStatus = null;
	
	public void setEventStatus()
	{
		eventStatus = new PlayerEventStatus(this);
	}
	
	public void setEventStatus(PlayerEventStatus pes)
	{
		eventStatus = pes;
	}
	
	public PlayerEventStatus getEventStatus()
	{
		return eventStatus;
	}
	
	// Selfbuff method.
	public boolean isSellBuff()
	{
		return _sellbuff;
	}
	
	public void setSellBuff(boolean sellingbuffs)
	{
		_sellbuff = sellingbuffs;
		if (sellingbuffs)
			_privatestore = STORE_SELLBUFF;
		else
			_privatestore = STORE_PRIVATE_NONE;
	}
	
	public boolean isInSiege()
	{
		return getEvent(SiegeEvent.class) != null;
	}
	
	public static String getShortClassName(int classId)
	{
		switch (classId)
		{
			case 12:
			case 94:
				return "Sorcer";
			case 13:
			case 95:
				return "Necro";
			case 14:
			case 96:
				return "Warlock";
			case 16:
			case 97:
				return "Bishop";
			case 17:
			case 98:
				return "Prophet";
			case 21:
			case 100:
				return "SwordSinger";
			case 27:
			case 103:
				return "SpellSinger";
			case 28:
			case 104:
				return "E.Summoner";
			case 30:
			case 105:
				return "Eva Saint";
			case 34:
			case 107:
				return "BladeDancer";
			case 40:
			case 110:
				return "SpellHowler";
			case 41:
			case 111:
				return "P.Summoner";
			case 43:
			case 112:
				return "ShillienElder";
			case 51:
			case 115:
				return "Overlord";
			case 52:
			case 116:
				return "Warcryer";
			case 55:
			case 117:
				return "BountyHunter";
			case 57:
			case 118:
				return "Warsmith";
			default:
				return "Random";
		}
	}
	
	Radar _radar = new Radar(this);
	public Radar getRadar()
	{
		return _radar;
	}
	
	public PlayerEventInfo getEventInfo()
	{
		return _eventInfo;
	}

	public void registerShortCut(ShortCut shortcut, boolean storeToDb)
	{
		_shortCuts.registerShortCut(shortcut, storeToDb);
	}
	
	public void deleteShortCut(int slot, int page, boolean fromDb)
	{
		_shortCuts.deleteShortCut(slot, page, fromDb);
	}
	
	public void restoreShortCuts()
	{
		_shortCuts.restore();
	}
	
	public void removeAllShortcuts()
	{
		_shortCuts.tempRemoveAll();
	}
	
	// Nexus Events antifeed protection
	private PlayerTemplate _antifeedTemplate = null;
	private boolean _antifeedSex;
	
	private PlayerTemplate createRandomAntifeedTemplate()
	{
		Race race = null;
		
		while (race == null)
		{
			race = Race.values()[Rnd.get(Race.values().length)];
			if (race == getRace() || race == Race.kamael)
				race = null;
		}
		
		PlayerClass p;
		for (ClassId c : ClassId.values())
		{
			p = PlayerClass.values()[c.getId()];
			if (p.isOfRace(race) && p.isOfLevel(ClassLevel.Fourth))
			{
				_antifeedTemplate = CharTemplateHolder.getInstance().getTemplate(c, false);
				break;
			}
		}
		
		if (getRace() == Race.kamael)
			_antifeedSex = getSex() == 1 ? true : false;
		
		_antifeedSex = Rnd.get(2) == 0 ? true : false;
		
		return _antifeedTemplate;
	}
	
	public void startAntifeedProtection(boolean start, boolean broadcast)
	{
		if (!start)
			_antifeedTemplate = null;
		else
			createRandomAntifeedTemplate();
	}

	public PlayerTemplate getAntifeedTemplate()
	{
		return _antifeedTemplate;
	}
	
	public boolean getAntifeedSex()
	{
		return _antifeedSex;
	}
	// Nexus Events antifeed protection end

	private int _killsInRow = 0;
	
	public int getKillsInRow()
	{
		return _killsInRow;
	}
	
	public void setKillsInRow(int val)
	{
		_killsInRow = val;
		
		if (Config.ENABLE_PLAYER_COUNTERS && getCounters().getPoints("_Longest_Kill_Spree") < _killsInRow)
			getCounters().setPoints("_Longest_Kill_Spree", _killsInRow);
	}
	
	private final Map<String, Long> _killedHWIDs = new FastMap<String, Long>();
	
	public final boolean checkIfKillIsFeed(Player target)
	{
		try
		{
			if (!Config.ANTIFEED_ENABLE)
				return false;
			
			if (target == null)
				return false;
			
			if (Config.ANTIFEED_DUALBOX && isDualbox(target))
				return true;
			
			// If there are many players around him, surely its not feed
			int i = 0;
			for (Player plr : World.getAroundPlayers(target, 3000, 200))
			{
				if (GeoEngine.canSeeTarget(this, plr, false))
					i++;
			}
			
			if (i > 6)
				return false;
			
			if (target.hasHWID() && target.hasTooManyRessurectionStacks())
			{
				// 20 min ban to this char for killing this target.
				_killedHWIDs.put(target.getHWID(), System.currentTimeMillis() + 20 * 60000);
				return true;
			}
			
			if (Config.ANTIFEED_INTERVAL > 0 && target.hasHWID() && _killedHWIDs.containsKey(target.getHWID()))
			{
				if ((System.currentTimeMillis() - _killedHWIDs.get(target.getHWID())) < Config.ANTIFEED_INTERVAL) //multiplier
					return true;
			}
		}
		catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
		
		return false;
	}
	
	private long lastTimeRessurected = 0;
	private int lastTimeRessurectedStack = 0;
	
	private void checkAndAddRessurectionStack()
	{
		if (lastTimeRessurected + 300000 > System.currentTimeMillis()) // If 5 mins havent passed since last res, add stack
			lastTimeRessurectedStack++;
		else
			// If 5 mins has passed since last res, set stack to 1
			lastTimeRessurectedStack = 1;
		
		lastTimeRessurected = System.currentTimeMillis();
	}
	
	private boolean hasTooManyRessurectionStacks()
	{
		if (lastTimeRessurectedStack >= 5)
		{
			lastTimeRessurectedStack = 0;
			return true;
		}
		
		return false;
	}
	
	public Location getStablePoint()
	{
		return _stablePoint;
	}
	
	public void setStablePoint(Location point)
	{
		_stablePoint = point;
	}
	
	public int getGearScore()
	{
		return Util.getGearPoints(this);
	}
	
	private int[] _visualitems;

	public void setVisualItems(int[] items)
	{
		_visualitems = items;
	}

	public int[] getVisualItems()
	{
		return _visualitems;
	}
	
	int[] _visualOthers;

	public void setVisualOthers(int[] others)
	{
		_visualOthers = others;
	}

	public int[] getVisualOthers()
	{
		return _visualOthers;
	}
	
	public void disableVisuals()
	{
		Player player = getPlayer();
		
		if (player == null)
			return;

		if (player.getVar("visualItems") != null)
		{
			if (player.getVisualItems() != null)
			{
				player.setVisualItems(null);
				broadcastCharInfo();
				sendPacket(new UserInfo(player));
				sendPacket(new UserInfo(player));
			}
		}
	}
	
	public void enableVisuals()
	{
		Player player = getPlayer();
		
		if (player == null)
			return;
		
		if (player.getVar("visualItems") != null)
		{
			String[] var = player.getVar("visualItems").split(";");
			
			int[] items =
			{
				Integer.parseInt(var[0]),
				Integer.parseInt(var[1]),
				Integer.parseInt(var[2]),
				Integer.parseInt(var[3]),
				Integer.parseInt(var[4]),
				Integer.parseInt(var[5]),
				Integer.parseInt(var[6]),
				Integer.parseInt(var[7]),
				Integer.parseInt(var[8]),
				Integer.parseInt(var[9]),
				Integer.parseInt(var[10])
			};
			
			player.setVisualItems(items);
			broadcastCharInfo();
			sendUserInfo();
		}
	}
	
	public void setServerId(int id)
	{
		_serverId = id;
	}
	
	public int getServerId()
	{
		if (_serverId == 0)
			_serverId = CharacterDAO.getInstance().getLastServerId(getAccountName());
		
		return _serverId;
	}
	
	public void showClanNotice()
	{
		Clan clan = getClan();
		if(getPlayer() == null || clan == null || clan.getLevel() < 2 && getSecurity())
			return;

		if(clan.getNotice() == null)
		{
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			String notice = "";
			long date = 0;
			int type = 0;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT * FROM `clan_notice` WHERE `clan_id` = ? and type != 2");
				statement.setInt(1, clan.getClanId());
				rset = statement.executeQuery();
				if(rset.next())
				{
					notice = rset.getString("notice");
					type = rset.getInt("type");
					date = rset.getLong("lastUpdated");
				}
			}
			catch(Exception e)
			{}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}

			clan.setNotice(type == 1 ? notice.replace("\n", "<br1>\n") : "");
			clan.setNoticeLastUpdated(date);
		}

		if(clan.getNotice() != null && !clan.getNotice().isEmpty())
		{
			String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "clan_popup.htm", this);
			html = html.replace("%pledge_name%", clan.getName());
			html = html.replace("%content%", clan.getNotice());
			html = html.replace("%date%", TimeUtils.convertDateToString(clan.getNoticeUpdated()));

			sendPacket(new NpcHtmlMessage(0).setHtml(html));
		}
	}
	
	public boolean isDualbox(Player ... players)
	{
		return isDualbox(0, false, Arrays.asList(players));
	}
	
	public boolean isDualbox(Iterable<Player> players)
	{
		return isDualbox(0, false, players);
	}
	
	public boolean isDualbox(int maxBoxesAllowed, boolean hwidOnly, Player ... players)
	{
		if (maxBoxesAllowed < 0)
			return false;
		
		return isDualbox(maxBoxesAllowed, hwidOnly, Arrays.asList(players));
	}
	
	public boolean isDualbox(int maxBoxesAllowed, boolean hwidOnly, Iterable<Player> players)
	{
		if (maxBoxesAllowed < 0)
			return false;
		
		for (Player player : players)
		{
			if (player == null || player.getClient() == null || player == this)
				continue;
			
			if (!hwidOnly && getClient().getAccountData() == null || getClient().getAccountData().accessLevel < 1 && getIP().equalsIgnoreCase(player.getIP())) // Account access 1 allows IP dualbox bypass
			{
				maxBoxesAllowed--;
			}
			else if (player.hasHWID() && getClient().getAccountData() == null || getClient().getAccountData().accessLevel < 2 && getHWID().equalsIgnoreCase(player.getHWID())) // Account access 2 allows HWID dualbox bypass
			{
				maxBoxesAllowed--;
			}
			
			if (maxBoxesAllowed < 0)
				return true;
		}
		
		return false;
	}
	
	private boolean _partyMatchingVisible = false;
	
	public void setPartyMatchingVisible()
	{
		_partyMatchingVisible = !_partyMatchingVisible;
	}
	
	public boolean isPartyMatchingVisible()
	{
		return _partyMatchingVisible;
	}
	
	private boolean _isPendingOlyEnd = false;
	
	public boolean isPendingOlyEnd()
	{
		return _isPendingOlyEnd;
	}
	
	public void setPendingOlyEnd(boolean val)
	{
		_isPendingOlyEnd = val;
	}
	
	private int _communityAugmentStat = 0;
	
	public void setCommunityAugmentStat(int id)
	{
		_communityAugmentStat = id;
	}
	
	public int getCommnityAugmentStat()
	{
		return _communityAugmentStat;
	}
	
	private ItemInstance _communityAugmentItem = null;
	
	public void setCommunityAugmentItem(ItemInstance item)
	{
		_communityAugmentItem = item;
	}
	
	public ItemInstance getCommnityAugmentItem()
	{
		return _communityAugmentItem;
	}
	
	@Override
	public void sendHtml(String file)
	{
		sendPacket(new NpcHtmlMessage(0).setFile(file));
	}
	
	@Override
	public void sendHtml(NpcInstance npc, String file)
	{
		sendPacket(new NpcHtmlMessage(this, npc).setFile(file));
	}
	
	@Override
	public void sendHtmlMessage(String message)
	{
		sendPacket(new NpcHtmlMessage(0).setHtml(message));
	}
	
	private class startMessageTimer implements Runnable
	{
		Player _activeChar;
		String _message;
		int _time;
		
		public startMessageTimer(Player player, String message, int time)
		{
			_activeChar = player;
			_message = message;
			_time = time;
		}
		
		@Override
		public void run()
		{
			while (_time > 0)
			{
				if (_activeChar == null || !_activeChar.isOnline())
				{
					endMessageTask();
					break;
				}
				
				if (_startTaskMessage == null || _startTaskMessage.isCancelled())
					break;
				
				int sec = _time - _time / 60 * 60;
				
				String timeMsg = "";
				
				if (sec < 10)
					timeMsg = timeMsg + "\n(" + _time / 60 + ":0" + sec + ")";
				else
					timeMsg = timeMsg + "\n(" + _time / 60 + ":" + sec + ")";
				
				_activeChar.sendPacket(new ExShowScreenMessage(timeMsg + " " + _message, 1000, ScreenMessageAlign.TOP_CENTER, false));
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{

				}
				
				_time--;
			}
			if (_activeChar != null)
				_activeChar.endMessageTask();
		}
	}
	
	public void startMessageTask(String message, int time)
	{
		endMessageTask();
		_startTaskMessage = ThreadPoolManager.getInstance().schedule(new startMessageTimer(this, message, time), 1000);
	}
	public boolean isMessageTaskCancelled()
	{
		return _startTaskMessage == null;
	}
	
	public void endMessageTask()
	{
		if (_startTaskMessage != null)
		{
			_startTaskMessage.cancel(false);
			_startTaskMessage = null;
		}
	}
	
	private static class startPunishWyvernTask implements Runnable
	{
		Player _activeChar;
		
		public startPunishWyvernTask(Player player)
		{
			_activeChar = player;
		}
		
		@Override
		public void run()
		{
			_activeChar.stopMove();
			_activeChar.dismount();
			
			if ("kick".equals(Config.PUNISHMENT_FOR_WYVERN_INSIDE_SIEGE))
			{
				_log.info(_activeChar.getName() + " Kicked Using wyvern inside siege zone.");
				
				if (_activeChar != null)
					_activeChar.kick();
				else
					_activeChar.getClient().getConnection().getClient().closeNow(false);
			}
			else if ("jail".equals(Config.PUNISHMENT_FOR_WYVERN_INSIDE_SIEGE))
			{
				if (_activeChar != null)
				{
					AutoBan.Jail(_activeChar.getName(), Config.PINISHMENT_TIME_FOR_WYVERN, "Enter Inside siege with wyvern.", null);
					
					_log.info(_activeChar.getName() + " Jailed for " + Config.PINISHMENT_TIME_FOR_WYVERN + " minutes. Using wyvern inside siege zone.");
				}
				else
				{
					_log.warn("Wyvern Punishment: unable to jail: no active player");
				}
			}
		}
	}
	
	public static void startPunishWyvern(Player player, int timeInSeconds)
	{
		endPunishWyvernTask();
		_startTaskPunishWyvern = ThreadPoolManager.getInstance().schedule(new startPunishWyvernTask(player), timeInSeconds * 1000);
	}
	
	public static void endPunishWyvernTask()
	{
		if (_startTaskPunishWyvern != null)
		{
			_startTaskPunishWyvern.cancel(false);
			_startTaskPunishWyvern = null;
		}
	}
	
	@Override
	public void sendPacket(Player exclude, IStaticPacket... packets)
	{
		for (Player plr : getMembers(exclude))
		{
			if (plr != null)
				plr.sendPacket(packets);
		}
	}
	
	@Override
	public void sendPacketInRange(GameObject obj, int range, IStaticPacket... packets)
	{
		for (Player plr : getMembers())
		{
			if (plr != null && plr.isInRangeZ(obj, range))
				plr.sendPacket(packets);
		}
	}
	
	@Override
	public List<Player> getMembersInRange(GameObject obj, int range)
	{
		List<Player> result = new ArrayList<Player>();
		for(Player member : getMembers())
		{
			if (member != null && obj.isPlayer() && member.isInRange(obj, range))
				result.add(member);
		}
		
		return result;
	}
	
	@Override
	public int getMemberCountInRange(GameObject obj, int range)
	{
		int count = 0;
		
		for (Player plr : getMembers())
		{
			if (plr != null && plr.isInRangeZ(obj, range))
				count++;
		}
		
		return count;
	}
	
	@Override
	public List<Integer> getMembersObjIds(Player... excluded)
	{
		List<Integer> result = new ArrayList<Integer>();
		for (Player member : getMembers(excluded))
		{
			if (member != null)
				result.add(member.getObjectId());
		}
		
		return result;
		
	}
	
	@Override
	public List<Playable> getMembersWithPets(Player ... excluded)
	{
		List<Playable> result = new ArrayList<Playable>();
		for(Player member : getMembers(excluded))
		{
			if (member == null)
				continue;
			
			result.add(member);
			
			if(member.getPet() != null)
				result.add(member.getPet());
		}
		
		return result;
	}
	
	@Override
	public Player getPlayerByName(String name)
	{
		if (name == null)
			return null;
		
		for (Player member : getMembers())
		{
			if (name.equalsIgnoreCase(member.getName()))
				return member;
		}
		
		return null;
	}
	
	@Override
	public Player getPlayer(int objId)
	{
		if (getLeader() != null && getLeader().getObjectId() == objId)
			return getLeader();
		
		for (Player member : getMembers())
		{
			if (member != null && member.getObjectId() == objId)
				return member;
		}
		
		return null;
	}
	
	public void startAcademyTask()
	{
		Clan clan = getClan();
		if (clan != null)
		{
			if (clan.getLeader().isOnline())
				AcademyList.removeAcademyFromDB(clan, getObjectId(), false, true);
			
			clan.removeClanMember(getObjectId());
			setLvlJoinedAcademy(0);
			setClan(null);
			setTitle("");
			broadcastUserInfo(true);
		}
	}
	
	public void checkForPvPColor(int pkKills)
	{
		if (Config.PVP_KILLS_COLOR.containsKey(pkKills))
		{
			String color = Config.PVP_KILLS_COLOR.get(pkKills);
			
			setNameColor(Integer.decode("0x" + color));
			broadcastUserInfo(true);
		}
	}
	
	public void setIsVoting(boolean value)
	{
		_isVoting = value;
	}
	
	public boolean isVoting()
	{
		return _isVoting;
	}
	
	/*
	public void requestBotCheck()
	{
		BotCheckQuestion question = BotCheckerManager.generateRandomQuestion();
		int qId = question.getId();
		String qDescr = question.getDescr(isLangRus());
		
		ask(new ConfirmDlg(SystemMsg.S1, 60000).addString("[BotChecker] Anaswer on the question: \n" + qDescr), new BotCheckerAnswerListener(this, qId));
	}*/
	
	public void fullHeal()
	{
		setCurrentCp(getMaxCp());
		setCurrentHp(getMaxHp(), false);
		setCurrentMp(getMaxMp());
	}
	
	private boolean _IsPhantom = false;
	
	public void setIsPhantom(boolean isPhantom, boolean hasAi)
	{
		_IsPhantom = isPhantom;
		if (hasAi)
			setAI(new PhantomPlayerAI(this));
		else
			setAI(new PlayerAI(this));
	}
	
	public boolean isPhantom()
	{
		return _IsPhantom;
	}
}