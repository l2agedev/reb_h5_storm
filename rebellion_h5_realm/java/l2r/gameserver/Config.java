package l2r.gameserver;

import l2r.commons.configuration.ExProperties;
import l2r.commons.net.AdvIP;
import l2r.commons.net.nio.impl.SelectorConfig;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.actor.instances.player.Bonus;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.loginservercon.ServerType;
import l2r.gameserver.utils.GArray;
import l2r.gameserver.utils.Language;
import l2r.gameserver.utils.Strings;
import l2r.gameserver.utils.Util;

import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config
{
	private static final Logger _log = LoggerFactory.getLogger(Config.class);
	
	public static final int NCPUS = Runtime.getRuntime().availableProcessors();
	/** Configuration files */
	public static final String RESIDENCE_CONFIG_FILE = "config/residence.properties";
	public static final String SPOIL_CONFIG_FILE = "config/spoil.properties";
	public static final String ALT_SETTINGS_FILE = "config/altsettings.properties";
	public static final String FORMULAS_CONFIGURATION_FILE = "config/formulas.properties";
	public static final String PVP_CONFIG_FILE = "config/pvp.properties";
	public static final String TELNET_CONFIGURATION_FILE = "config/telnet.properties";
	public static final String CONFIGURATION_FILE = "config/server.properties";
	public static final String AI_CONFIG_FILE = "config/ai.properties";
	public static final String GEODATA_CONFIG_FILE = "config/geodata.properties";
	public static final String GM_CONFIG_FILE = "config/gm.properties";
	public static final String OLYMPIAD = "config/olympiad.properties";
	public static final String DEVELOP_FILE = "config/develop.properties";
	public static final String EXT_FILE = "config/ext.properties";
	public static final String RATES_FILE = "config/rates.properties";
	public static final String CHAT_FILE = "config/chat.properties";
	public static final String BOSS_FILE = "config/boss.properties";
	public static final String EPIC_BOSS_FILE = "config/epic.properties";
	public static final String PAYMENT_FILE = "config/services/payment.properties";
	public static final String ITEM_USE_FILE = "config/UseItems.properties";
	public static final String INSTANCES_FILE = "config/instances.properties";
	public static final String ITEMS_FILE = "config/items.properties";
	public static final String ANUSEWORDS_CONFIG_FILE = "config/abusewords.txt";
	public static final String ADV_IP_FILE = "config/advipsystem.properties";
	public static final String GM_PERSONAL_ACCESS_FILE = "config/GMAccess.xml";
	public static final String CUSTOM_CONFIG_FILE = "config/custom.properties";
	public static final String CUSTOM_SECURITY_FILE = "config/CustomSecurity.properties";
	public static final String SMTP_CONFIG = "config/SMTP.properties";
	public static final String COMMANDS_CONFIG_FILE = "config/commands.properties";
	public static final String OTHER_CONFIG_FILE = "config/other.properties";
	public static final String OFFLINE_CONFIG_FILE = "config/offline.properties";
	
	/** services */
	public static final String ACC_MOVE_FILE = "config/services/CharMove.properties";
	public static final String TOP_FILE = "config/services/tops.properties";
	public static final String PREMIUM_FILE = "config/services/premium.properties";
	public static final String WEDDING_FILE = "config/services/Wedding.properties";
	public static final String SERVICES_FILE = "config/services/services.properties";
	
	/** events */
	public static final String EVENTS_CONFIG_FILE = "config/events/events.properties";
	public static final String EVENT_APRIL_FOOLS_CONFIG_FILE = "config/events/AprilFools.properties";
	public static final String EVENT_CAPTURE_THE_FLAG_CONFIG_FILE = "config/events/CaptureTheFlag.properties";
	public static final String EVENT_CHANGE_OF_HEART_CONFIG_FILE = "config/events/ChangeOfHeart.properties";
	public static final String EVENT_COFFER_OF_SHADOWS_CONFIG_FILE = "config/events/CofferOfShadows.properties";
	public static final String EVENT_FIGHT_CLUB_FILE = "config/events/FightClub.properties";
	public static final String EVENT_GLITTERING_MEDAL_CONFIG_FILE = "config/events/GlitteringMedal.properties";
	public static final String EVENT_L2_DAY_CONFIG_FILE = "config/events/L2Day.properties";
	public static final String EVENT_LAST_HERO_CONFIG_FILE = "config/events/LastHero.properties";
	public static final String EVENT_MARCH_8_CONFIG_FILE = "config/events/March8.properties";
	public static final String EVENT_MASTER_OF_ENCHANING_CONFIG_FILE = "config/events/MasterOfEnchaning.properties";
	public static final String EVENT_OTHER_EVENTS_CONFIG_FILE = "config/events/OtherEvents.properties";
	public static final String EVENT_SAVING_SNOWMAN_CONFIG_FILE = "config/events/SavingSnowman.properties";
	public static final String EVENT_TEAM_VS_TEAM_CONFIG_FILE = "config/events/TeamVSTeam.properties";
	public static final String EVENT_THE_FALL_HARVEST_CONFIG_FILE = "config/events/TheFallHarvest.properties";
	public static final String EVENT_TRICK_OF_TRANSMUTATION_CONFIG_FILE = "config/events/TrickOfTransmutation.properties";
	public static final String PC_CONFIG_FILE = "config/events/PcBangPoints.properties";
	public static final String DEFENSE_TOWNS_CONFIG_FILE = "config/events/DefenseTowns.properties";
	public static final String TRIVIA_CONFIG_FILE = "config/events/Trivia.properties";
	
	/** community */
	public static final String BOARD_MANAGER_CONFIG_FILE = "config/community/board_manager.properties";
	public static final String CB_COMISSION_CONFIG_FILE = "config/community/comission.properties";
	public static final String BUFFER_MANAGER_CONFIG_FILE = "config/community/buffer_manager.properties";
	public static final String CLASS_MASTER_CONFIG_FILE = "config/community/class_master.properties";
	public static final String TELEPORT_MANAGER_CONFIG_FILE = "config/community/teleport_manager.properties";
	public static final String ENCHANT_CB_CONFIG_FILE = "config/community/enchanter_manager.properties";
	public static final String DONATE_REWARD_CONFIG_FILE = "config/DonateReward.properties";
	
	public static boolean ENABLE_HWID_CHECKER;
	
	// donation system
	public static String MAIL_USER;
	public static String MAIL_PASS;
	public static String MAIL_SUBJECT;
	public static String MAIL_MESSAGE;
	
	/** Phantom players */
	public static final String PHANTOM_FILE = "config/phantom/Phantoms.properties";
	
	 /* --------------------------------------------------------- */
	// Phantom players
	public static boolean PHANTOM_PLAYERS_ENABLED;
	public static String PHANTOM_PLAYERS_ACCOUNT;
	public static int PHANTOM_MAX_PLAYERS;
	public static int[] PHANTOM_BANNED_CLASSID;
	public static int[] PHANTOM_BANNED_SETID;
	public static int PHANTOM_MAX_WEAPON_GRADE;
	public static int PHANTOM_MAX_ARMOR_GRADE;
	public static int PHANTOM_MAX_JEWEL_GRADE;
	public static int PHANTOM_SPAWN_MAX;
	public static int PHANTOM_SPAWN_DELAY;
	public static int PHANTOM_MAX_LIFETIME;
	public static int CHANCE_TO_ENCHANT_WEAP;
	public static int MAX_ENCH_PHANTOM_WEAP;
	public static int PHANTOM_MAX_DRIFT_RANGE;
	public static boolean ALLOW_PHANTOM_CUSTOM_TITLES;
	public static int PHANTOM_CHANCE_SET_NOBLE_TITLE;
	public static boolean DISABLE_PHANTOM_ACTIONS;
	public static int[] PHANTOM_ALLOWED_NPC_TO_WALK;
	public static int PHANTOM_ROAMING_MAX_WH_CHECKS;
	public static int PHANTOM_ROAMING_MAX_WH_CHECKS_DWARF;
	public static int PHANTOM_ROAMING_MAX_SHOP_CHECKS;
	public static int PHANTOM_ROAMING_MAX_SHOP_CHECKS_DWARF;
	public static int PHANTOM_ROAMING_MAX_NPC_CHECKS;
	public static int PHANTOM_ROAMING_MIN_WH_DELAY;
	public static int PHANTOM_ROAMING_MAX_WH_DELAY;
	public static int PHANTOM_ROAMING_MIN_SHOP_DELAY;
	public static int PHANTOM_ROAMING_MAX_SHOP_DELAY;
	public static int PHANTOM_ROAMING_MIN_NPC_DELAY;
	public static int PHANTOM_ROAMING_MAX_NPC_DELAY;
	public static int PHANTOM_ROAMING_MIN_PRIVATESTORE_DELAY;
	public static int PHANTOM_ROAMING_MAX_PRIVATESTORE_DELAY;
	public static int PHANTOM_ROAMING_MIN_FREEROAM_DELAY;
	public static int PHANTOM_ROAMING_MAX_FREEROAM_DELAY;
	public static boolean DISABLE_PHANTOM_RESPAWN;
	public static boolean DEBUG_PHANTOMS;
	public static int[] PHANTOM_CLANS;
	
	// Automatic potions
	public static boolean ENABLE_AUTO_POTIONS;
	public static double AUTO_CP_POTION_DELAY;
	public static double AUTO_HP_POTION_DELAY;
	public static double AUTO_MP_POTION_DELAY;
	public static int LASER_MP_POTION_RESTORE;
	public static int GREATER_MP_POTION_RESTORE;
	public static int LASER_CP_POTION_RESTORE;
	public static int GREATER_CP_POTION_RESTORE;
	public static int LASER_HP_POTION_RESTORE;
	public static int GREATER_HP_POTION_RESTORE;
	
	public static boolean COMMUNITY_FORGE_ENABLED;
	public static int BBS_ENCHANT_ITEM;
	public static int[] BBS_ENCHANT_MAX;
	public static int[] BBS_WEAPON_ENCHANT_LVL;
	public static int[] BBS_ARMOR_ENCHANT_LVL;
	public static int[] BBS_JEWELS_ENCHANT_LVL;
	public static int[] BBS_ENCHANT_PRICE_WEAPON;
	public static int[] BBS_ENCHANT_PRICE_ARMOR;
	public static int[] BBS_ENCHANT_PRICE_JEWELS;

	public static int BBS_ENCHANT_WEAPON_ATTRIBUTE_MAX;
	public static int BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX;
	public static int[] BBS_ENCHANT_ATRIBUTE_LVL_WEAPON;
	public static int[] BBS_ENCHANT_ATRIBUTE_LVL_ARMOR;
	public static int[] BBS_ENCHANT_ATRIBUTE_PRICE_ARMOR;
	public static int[] BBS_ENCHANT_ATRIBUTE_PRICE_WEAPON;
	public static boolean BBS_ENCHANT_ATRIBUTE_PVP;

	public static boolean BBS_ENCHANT_HEAD_ATTRIBUTE;
	public static boolean BBS_ENCHANT_CHEST_ATTRIBUTE;
	public static boolean BBS_ENCHANT_LEGS_ATTRIBUTE;
	public static boolean BBS_ENCHANT_GLOVES_ATTRIBUTE;
	public static boolean BBS_ENCHANT_FEET_ATTRIBUTE;
	public static String[] BBS_ENCHANT_GRADE_ATTRIBUTE;

	public static boolean BBS_ENCHANT_WEAPON_ATTRIBUTE;
	public static boolean BBS_ENCHANT_SHIELD_ATTRIBUTE;
	
	public static boolean BBS_RECRUITMENT_ALLOW;
	public static int BBS_RECRUITMENT_CLAN_DESCRIPTION_MIN;
	public static int[] BBS_RECRUITMENT_TIME;
	public static int[] BBS_RECRUITMENT_ITEMS;
	
	// Lottery
	public static boolean BBS_GAME_LOTTERY_ALLOW;
	public static int[] BBS_GAME_LOTTERY_BET;
	public static double BBS_GAME_LOTTERY_WIN_CHANCE;
	public static double BBS_GAME_LOTTERY_JACKPOT_CHANCE;
	public static int BBS_GAME_LOTTERY_INITIAL_JACKPOT;
	public static int BBS_GAME_LOTTERY_AMOUNT_PERCENT_TO_JACKPOT;
	public static int BBS_GAME_LOTTERY_ITEM;
	public static int BBS_GAME_LOTTERY_REWARD_MULTIPLER;
	public static int BBS_GAME_LOTTERY_STORE_DATA;
	
	// Alternative Vote reward
	public static boolean ENABLE_ALT_VOTE_REWARD;
	public static boolean ENABLE_HOPZONE_VOTING;
	public static boolean ENABLE_TOPZONE_VOTING;
	public static String TOPZONE_SERVER_LINK;
	public static String HOPZONE_SERVER_LINK;
	public static int TIME_TO_VOTE_SEC;
	public static String[] ALT_VOTE_REWARDS;
	
	// Community Academy
	public static boolean ENABLE_COMMUNITY_ACADEMY;
	public static String SERVICES_ACADEMY_REWARD;
	public static long ACADEMY_MIN_ADENA_AMOUNT;
	public static long ACADEMY_MAX_ADENA_AMOUNT;
	public static long MAX_TIME_IN_ACADEMY;
	public static int ACADEMY_INVITE_DELAY;
	
	// Randoms...
	public static boolean DISABLE_HWID_SUPPORT;
	public static Map<Integer, String> PVP_KILLS_COLOR;
	public static boolean LOG_ACCOUNT_INFO;
	public static int LEVEL_UP_CRY_EXTRA_CHANCE;
	public static int STEAL_DIVINITY_SUCCESS;
	public static boolean CHECK_PRIVATE_SHOPS;
	public static boolean ENABLE_FAKEPC;
	public static int NEVIT_BONUS_TIME;
	public static int ALT_SELL_PRICE_DIV;
	public static int[] CLANHALL_NPC_IDS;
	public static int[] TALISMAN_IDS;
	
	// Lucky Pigs
	public static boolean ENABLE_LUCKY_PIGS;
	public static int MAX_ADENA_TO_EAT;
	public static int ADENA_TO_EAT;
	public static int TIME_IF_NOT_FEED;
	  
	// Visual System
	public static boolean ENABLE_VISUAL_SYSTEM;
	public static String VISUALS_TITLE_COLOR;
	public static String VISUALS_NAME_COLOR;
	public static int VISUAL_NPC_DELETE_TIME;
	
	// Clan Hall cleanup...
	public static boolean CLEAN_CLAN_HALLS_ON_TIME;
	public static int MIN_PLAYERS_IN_CLAN_TO_KEEP_CH;
	public static int DAYS_TO_CHECK_FOR_CH_DELETE;
	
	
	// Community commision shop
	public static boolean COMMUNITY_COMMISSION_ALLOW;
	public static int[] COMMUNITY_COMMISSION_ARMOR_PRICE;
	public static int[] COMMUNITY_COMMISSION_WEAPON_PRICE;
	public static int[] COMMUNITY_COMMISSION_JEWERLY_PRICE;
	public static int[] COMMUNITY_COMMISSION_OTHER_PRICE;
	public static int[] COMMUNITY_COMMISSION_ALLOW_ITEMS;
	public static int COMMUNITY_COMMISSION_MAX_ENCHANT;
	public static int[] COMMUNITY_COMMISSION_NOT_ALLOW_ITEMS;
	public static boolean COMMUNITY_COMMISSION_ALLOW_UNDERWEAR;
	public static boolean COMMUNITY_COMMISSION_ALLOW_CLOAK;
	public static boolean COMMUNITY_COMMISSION_ALLOW_BRACELET;
	public static boolean COMMUNITY_COMMISSION_ALLOW_AUGMENTED;
	public static boolean COMMUNITY_COMMISSION_ALLOW_EQUIPPED;
	public static int COMMUNITY_COMMISSION_COUNT_TO_PAGE;
	public static int[] COMMUNITY_COMMISSION_MONETS;
	public static int COMMUNITY_COMMISSION_SAVE_DAYS;
	
	// DropList
	public static boolean COMMUNITY_DROP_LIST;
	public static boolean COMMUNITY_ITEM_INFO;
	
	// Others
	public static int OLYMPIAD_TEAM_MATCH_SIZE;
	public static boolean ENABLE_TRADELIST_VOICE;
	public static int LEVEL_REQUIRED_TO_SEND_MAIL;
	public static boolean SHOW_BAN_INFO_IN_CHARACTER_SELECT;
	public static boolean ENABLE_VOTE_REWARDS;
	
	// Allow player to change his lang via .cfg
	public static boolean ALLOW_PLAYER_CHANGE_LANGUAGE;
	
	// Party distribute of items on premium account
	public static boolean PREMIUM_ACCOUNT_FOR_PARTY;
	
	// Retail macro use bug
	public static boolean ALLOW_MACROS_REUSE_BUG;
	
	// Referral system
	public static boolean ENABLE_REFERRAL_SYSTEM;
	
	// Allow custom class-transfer-skills for pvp server
	public static boolean CUSTOM_CLASS_TRANSFER_SKILLS;
		
	// Playerkill log manager
	public static boolean ANTIFEED_ENABLE;
	public static boolean ANTIFEED_DUALBOX;
	public static int ANTIFEED_INTERVAL;
	public static boolean ENABLE_PVP_PK_LOG;
	
	// Custom player kill system for pvp
	public static boolean ENABLE_PLAYER_KILL_SYSTEM;
	public static boolean PLAYER_KILL_SPAWN_UNIQUE_CHEST;
	public static boolean PLAYER_KILL_INCREASE_ATTRIBUTE;
	public static boolean PLAYER_KILL_GIVE_ENCHANTS;
	public static boolean PLAYER_KILL_GIVE_LIFE_STONE;
	public static boolean PLAYER_KILL_GIVE_MANTRAS;
	public static boolean PLAYER_KILL_AQUIRE_FAME;
	public static boolean PLAYER_KILL_ALLOW_CUSTOM_PVP_ZONES;
	
	// Emoticons on say.
	public static boolean ENABLE_EMOTIONS;
	
	// Gm Rank manager.
	public static boolean ENABLE_RANK_MANAGER;
	public static int RANK_NPC_ID;
	public static int RANK_NPC_MIN_LEVEL;
	public static String[] RANK_NPC_DISABLE_PAGE;
	public static Integer[] RANK_NPC_LIST_ITEM;
	public static int RANK_NPC_ITEMS_RECORDS;
	public static Integer[] RANK_NPC_LIST_CLASS;
	public static int RANK_NPC_OLY_RECORDS;
	public static int RANK_NPC_PVP_RECORDS;
	public static int RANK_NPC_PK_RECORDS;
	public static String RANK_NPC_COLOR_A;
	public static String RANK_NPC_COLOR_B;
	public static int RANK_NPC_RELOAD;
	
	// GmHunter event
	public static String[] GM_HUNTER_EVENT_REWARDS;
	public static int GM_HUNTER_EVENT_SET_SPEED;
	public static int GM_HUNTER_EVENT_SET_PDEFENCE;
	public static int GM_HUNTER_EVENT_SET_MDEFENCE;
	public static int GM_HUNTER_EVENT_SET_HP;
	public static int GM_HUNTER_EVENT_SET_CP;
	
	public static String[] ITEM_COST_1_ADENA;
	
	public static boolean ALT_SELL_FROM_EVERYWHERE;
	
	// RRD tool
	public static boolean RRD_ENABLED;
	public static boolean RRD_EXTENDED;
	public static String RRD_PATH;
	public static String RRD_EXT_PATH;
	public static String RRD_GRAPH_PATH;
	public static String RRD_AREA_COLOR;
	public static String RRD_LINE_COLOR;
	public static long RRD_UPDATE_TIME;
	public static int RRD_GRAPH_HEIGHT;
	public static int RRD_GRAPH_WIDTH;
	public static int RRD_LINE_WIDTH;
	
	// Clan Promotion npc
	public static boolean SERVICES_CLAN_PROMOTION_ENABLE;
	public static int SERVICES_CLAN_PROMOTION_MAX_LEVEL;
	public static int SERVICES_CLAN_PROMOTION_MIN_ONLINE;
	public static int SERVICES_CLAN_PROMOTION_ITEM;
	public static int SERVICES_CLAN_PROMOTION_ITEM_COUNT;
	public static int SERVICES_CLAN_PROMOTION_SET_LEVEL;
	public static int SERVICES_CLAN_PROMOTION_ADD_REP;
	public static boolean SERVICE_CLAN_PRMOTION_ADD_EGGS;
	public static String[] CLAN_PROMOTION_CLAN_EGGS;
	
	// Captcha system
	public static boolean ENABLE_CAPTCHA;
	public static int CAPTCHA_MIN_MONSTERS;
	public static int CAPTCHA_MAX_MONSTERS;
	public static int CAPTCHA_ATTEMPTS;
	public static int CAPTCHA_SAME_LOCATION_DELAY;
	public static int CAPTCHA_SAME_LOCATION_MIN_KILLS;
	public static String CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE;
	public static int CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME;
	public static int CAPTCHA_TIME_BETWEEN_TESTED_SECONDS;
	public static int CAPTCHA_TIME_BETWEEN_REPORTS_SECONDS;
	public static int CAPTCHA_MIN_LEVEL;
	
	public static boolean ALLOW_BOARD_NEWS_LEECH;
	public static String FORUM_URL_TO_LEECH_CHANGELOG;
	public static String  FORUM_URL_TO_LEECH_ANNOUNCE;
	
	public static boolean ENABLE_POLL_SYSTEM;
	
	// Vote rewards
	public static String[] VOTE_REWARDS;
	
	/* Player offline buff */
	public static boolean RESTORE_OFFLINE_BUFFERS_ON_RESTART;
	public static int OFFLINE_BUFFER_NAME_COLOR;
	public static int OFFLINE_BUFFER_TITLE_COLOR;
	
	public static boolean ENABLE_OFFLINE_BUFFERS;
	public static List<String> SELL_BUFF_CLASS_LIST = new ArrayList<String>();
	public static Map<Integer, Integer> SELL_BUFF_SKILL_LIST;
	public static boolean ALLOW_PARTY_BUFFS;
	public static boolean ALLOW_CLAN_BUFFS;
	public static int SELL_BUFF_PUNISHED_PRICE;
	public static int MINIMUM_PRICE_FOR_OFFLINE_BUFF;
	public static boolean SELL_BUFF_FILTER_ENABLED;
	public static boolean OFFLINE_SELLBUFF_ONLY_IN_ZONE;
	
	public static int SELL_BUFF_MIN_LVL;
	
	public static boolean OFFLINE_SELLBUFF_ENABLED;
	public static boolean SELL_BUFF_SKILL_MP_ENABLED;
	public static double SELL_BUFF_SKILL_MP_MULTIPLIER;
	public static boolean SELL_BUFF_SKILL_ITEM_CONSUME_ENABLED;
	
	public static int DEADLOCKCHECK_INTERVAL;
	public static int CANCEL_SYSTEM_RESTORE_DELAY;
	public static boolean CANCEL_SYSTEM_KEEP_TICKING;
	public static boolean AUTO_SHOTS_ON_LOGIN;
	
	/* smtp configs */
	public static boolean ENABLE_PASSWORD_RECOVERY;
	public static boolean ENABLE_ON_PASSWORD_CHANGE;
	public static boolean ENABLE_ON_SECURITY_PASSWORD_CHANGE;
	public static String SMTP_SERVER;
	public static int SMTP_SERVER_PORT;
	public static String SMTP_USERNAME;
	public static String SMTP_PASSWORD;
	public static boolean SMTP_SERVER_AUTH;
	public static String SMTP_SERVER_SECUIRTY;
	public static String SMTP_EMAIL_ADDR_SENDER;
	public static int SMTP_SERVER_TIMEOUT;
	public static int SMTP_SERVER_CONNECTION_TIMEOUT;
	
	/** CUSTOM CONFIGS */
	// Character intro
	public static boolean ENABLE_CHARACTER_INTRO;
	
	// Achievement system
	public static boolean ENABLE_ACHIEVEMENTS;
	public static boolean DISABLE_ACHIEVEMENTS_FAME_REWARD;
	
	// Custom Auction system
	public static boolean ENABLE_CUSTOM_AUCTION;
	
	public static boolean ENABLE_COMMUNITY_RANKING;
	public static boolean ENABLE_PLAYER_COUNTERS;
	public static int PLAYER_COUNTERS_REFRESH;
	public static boolean PLAYER_TOP_SORT_BY_LASTACCESS;
	public static boolean PLAYER_TOP_MONTHLY_RANKING;
	public static boolean ENABLE_EMAIL_VALIDATION;
	public static boolean CHARACTER_NAME_COLORIZATION;
	public static String[] FORBIDDEN_CHAR_NAMES;
	
	// Adena drop by level
	public static TIntIntHashMap ADENA_DROP_RATE_BY_LEVEL;
	
	// Wyvern settings
	public static boolean ALLOW_WYVERN_DURING_SIEGE;
	public static String PUNISHMENT_FOR_WYVERN_INSIDE_SIEGE;
	public static int PINISHMENT_TIME_FOR_WYVERN;
	
	// Custom things
	public static boolean CUSTOM_SKILLS_LOAD;
	public static boolean CUSTOM_ITEMS_LOAD;
	public static boolean CUSTOM_MULTISELL_LOAD;
	
	public static boolean DISABLE_TUTORIAL;
	
	public static boolean ENABLE_DONATE_PAGE;
	
	public static int CB_NPC_GATEKEEPER_ID;
	public static int CB_NPC_GMSHOP_ID;
	public static int CB_NPC_BUFFER_ID;
	public static int CB_NPC_AUCTION_ID;
	public static int CB_NPC_RBSTATUS_ID;
	public static int CB_NPC_CLASS_MASTER_ID;
	
	public static boolean ALLOW_MAMMON_FOR_ALL;
	
	public static boolean ALLOW_FARM_IN_SEVENSIGN_IF_NOT_REGGED;
	
	public static boolean SEVEN_SIGN_DISABLE_BUFF_DEBUFF;
	
	public static boolean SEVEN_SIGN_NON_STOP_ALL_SPAWN;
	public static int SEVEN_SIGN_SET_PERIOD;
	
	public static boolean PREMIUM_FOR_NEW_ACC;
	public static int PREMIUM_TEMPLATE_NEW_ACC;
	public static long PREMIUM_TIME_FOR_NEW_ACC;
	
	// Online table configs
	public static boolean ALLOW_ONLINE_PARSE;
	public static int FIRST_UPDATE;
	public static int DELAY_UPDATE;
	
	// Buffer
	public static boolean ENABLE_SCHEME_BUFFER;
	public static int MAX_SCHEME_PROFILES;
	public static int MAX_BUFFS_PER_PROFILE;
	public static int PRICE_PER_BUFF;
	public static int BUFFS_TIME;
	public static int BUFFER_MIN_LEVEL;
	public static int BUFFER_MAX_LEVEL;
	public static boolean ALLOW_BUFF_FOR_MY_CLASS;
	public static boolean ALLOW_BUFFER_HEAL;
	public static boolean ALLOW_CANCEL_BUFFS;
	public static boolean ALLOW_BUFFER_NOBLE;
	public static boolean ALLOW_BUFFER_UP_BUFF;
	
	// --------------------------------------------------
	// Security
	// --------------------------------------------------
	public static boolean SECURITY_ENABLED;
	public static boolean SECURITY_CHANGE_PASSWORD;
	public static boolean SECURITY_HERO_HEROVOICE;
	public static boolean SECURITY_ON_STARTUP_WHEN_SECURED;
	public static boolean SECURITY_CANT_PVP_ENABLED;
	public static boolean SECURITY_FORCE;
	public static boolean SECURITY_TRADE_ENABLED;
	public static boolean SECURITY_CFG_ENABLED;
	public static boolean SECURITY_ENCHANT_SKILL_ENABLED;
	public static boolean SECURITY_ENCHANT_ITEM_ENABLED;
	public static boolean SECURITY_ENCHANT_ITEM_REMOVE_ENABLED;
	public static boolean SECURITY_ENCHANT_ITEM_ELEMENT_REMOVE_ENABLED;
	public static boolean SECURITY_ITEM_AUGMENT;
	public static boolean SECURITY_ITEM_UNEQUIP;
	public static boolean SECURITY_ITEM_ATTRIBUTE_REMOVE_ENABLED;
	public static boolean SECURITY_ITEM_DESTROY_ENABLED;
	public static boolean SECURITY_ITEM_DROP_ENABLED;
	public static boolean SECURITY_ITEM_GIVE_TO_PET_ENABLED;
	public static boolean SECURITY_ITEM_REMOVE_AUGUMENT_ENABLED;
	public static boolean SECURITY_ITEM_CRYSTALIZE_ENABLED;
	public static boolean SECURITY_SENDING_MAIL_ENABLED;
	public static boolean SECURITY_DELETE_RECIEVED_MAILS;
	public static boolean SECURITY_DELETE_SENT_MAILS;
	public static boolean SECURITY_READ_OWN_MAILS;
	public static boolean SECURITY_DELETE_MACRO;
	public static boolean SECURITY_ADD_MACRO;
	public static boolean SECURITY_DELETE_BOOKMARK_SLOT;
	public static boolean SECURITY_CLAN_ALLY_ALL;
	
	public static int HTM_CACHE_MODE;
	
	public static boolean ALLOW_QUETS_ADDONS_CONFIG;
	
	public static boolean ALLOW_IP_LOCK;
	public static boolean ALLOW_HWID_LOCK;
	public static int HWID_LOCK_MASK;
	
	/** GameServer ports */
	public static int[] PORTS_GAME;
	public static boolean ADVIPSYSTEM;
	public static List<AdvIP> GAMEIPS = new ArrayList<AdvIP>();
	public static String DATABASE_DRIVER;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIMEOUT;
	public static int DATABASE_IDLE_TEST_PERIOD;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static String LOGINSERVER_DB_NAME;
	
	// Database additional options
	public static boolean AUTOSAVE;
	
	public static long USER_INFO_INTERVAL;
	public static boolean BROADCAST_STATS_INTERVAL;
	public static long BROADCAST_CHAR_INFO_INTERVAL;
	
	public static int EFFECT_TASK_MANAGER_COUNT;
	
	public static int MAXIMUM_ONLINE_USERS;
	public static int ONLINE_PLUS;
	
	public static boolean DONTLOADSPAWN;
	public static boolean DONTLOADQUEST;
	public static boolean DONTLOADEVENTS;
	public static boolean DONTLOADOPTIONDATA;
	public static boolean DONTLOADNPCDROP;
	public static boolean DONTLOADNEXUS;
	public static boolean DONTLOADMULTISELLS;
	public static boolean DONTAUTOANNOUNCE;
	public static int MAX_REFLECTIONS_COUNT;
	
	public static int SHIFT_BY;
	public static int SHIFT_BY_Z;
	public static int MAP_MIN_Z;
	public static int MAP_MAX_Z;
	
	/** ChatBan */
	public static int CHAT_MESSAGE_MAX_LEN;
	public static boolean ABUSEWORD_BANCHAT;
	public static int[] BAN_CHANNEL_LIST = new int[18];
	public static boolean ABUSEWORD_REPLACE;
	public static String ABUSEWORD_REPLACE_STRING;
	public static int ABUSEWORD_BANTIME;
	public static Pattern[] ABUSEWORD_LIST = {};
	public static boolean BANCHAT_ANNOUNCE;
	public static boolean BANCHAT_ANNOUNCE_FOR_ALL_WORLD;
	public static boolean BANCHAT_ANNOUNCE_NICK;
	
	public static int[] CHATFILTER_CHANNELS = new int[18];
	public static int CHATFILTER_MIN_LEVEL = 0;
	public static int CHATFILTER_WORK_TYPE = 1;
	
	public static boolean SAVING_SPS;
	public static boolean MANAHEAL_SPS_BONUS;
	
	public static int ALT_ADD_RECIPES;
	public static int ALT_MAX_ALLY_SIZE;
	
	public static int MAX_DISTRIBUTE_MEMBER_LEVEL_PARTY;
	public static int ALT_PARTY_DISTRIBUTION_RANGE;
	public static double[] ALT_PARTY_BONUS;
	public static double ALT_ABSORB_DAMAGE_MODIFIER;
	public static boolean ALT_ALL_PHYS_SKILLS_OVERHIT;
	
	public static double ALT_POLE_DAMAGE_MODIFIER;
	public static double ALT_BOW_PVP_DAMAGE_MODIFIER;
	public static double ALT_BOW_PVE_DAMAGE_MODIFIER;
	public static double ALT_PET_PVP_DAMAGE_MODIFIER;
	
	public static int FORMULA_LETHAL_MAX_HP;
	public static boolean ALT_REMOVE_SKILLS_ON_DELEVEL;
	public static boolean ALT_USE_BOW_REUSE_MODIFIER;
	
	public static boolean ALT_VITALITY_ENABLED;
	public static int[] ALT_VITALITY_RATE;
	public static double ALT_VITALITY_CONSUME_RATE;
	public static int ALT_VITALITY_RAID_BONUS;
	public static final int[] VITALITY_LEVELS =
	{
		240,
		2000,
		13000,
		17000,
		20000
	};
	
	public static boolean ALLOW_BBS_WAREHOUSE;
	public static boolean BBS_WAREHOUSE_ALLOW_PK;
	/** Community Board */
	public static boolean BBS_PVP_SUB_MANAGER_ALLOW;
	public static boolean BBS_PVP_SUB_MANAGER_PEACE_ZONE;
	
	public static boolean COMMUNITY_TELEPORT_ENABLED;
	public static int COMMUNITY_TELEPORT_GLOBAL_PRICE;
	public static int COMMUNITY_TELEPORT_BOOKMARK_PRICE;
	public static int COMMUNITY_BOOKMARK_MAX;
	public static boolean COMMUNITY_TELEPORT_DURING_SIEGES;
	
	public static boolean COMMUNITY_ALLOW_SELL;
	public static boolean COMUMNITY_ALLOW_BUY;
	public static boolean COMMUNITY_ALLOW_AUGMENT;
	
	/** Settings of CommunityBoard Buffer */
	
	public static boolean SERVICES_BBSMERCENARIES;
	public static int MERCENARIES_ITEM;
	public static int MERCENARIES_ITEM_COUNT;
	public static int MERCENARIES_ITEM_HIDE_COUNT;
	public static boolean MERCENARIES_ANNOUNCE;
	
	public static boolean ENABLE_NEW_CLAN_CB;
	public static boolean ENABLE_OLD_CLAN_BOARD;
	
	public static boolean BOARD_ENABLE_CLASS_MASTER;
	public static boolean ENABLE_NEW_FRIENDS_BOARD;
	public static boolean ENABLE_RETAIL_FRIENDS_BOARD;
	public static boolean ENABLE_MEMO_BOARD;
	public static boolean ENABLE_NEW_MAIL_MANAGER;
	public static boolean ENABLE_OLD_MAIL_MANAGER;
	
	public static int EVENT_TvTTime;
	public static String[] EVENT_TvTRewards;
	public static boolean EVENT_TvT_rate;
	public static String[] EVENT_TvTStartTime;
	public static boolean EVENT_TvTCategories;
	public static int EVENT_TvTMaxPlayerInTeam;
	public static int EVENT_TvTMinPlayerInTeam;
	public static boolean EVENT_TvTAllowSummons;
	public static boolean EVENT_TvTAllowBuffs;
	public static boolean EVENT_TvTAllowMultiReg;
	public static String EVENT_TvTCheckWindowMethod;
	public static int EVENT_TvTEventRunningTime;
	public static String[] EVENT_TvTFighterBuffs;
	public static String[] EVENT_TvTMageBuffs;
	public static boolean EVENT_TvTBuffPlayers;
	public static boolean EVENT_TvTrate;
	public static int[] EVENT_TvTOpenCloseDoors;
	public static String[] EVENT_TvT_DISALLOWED_SKILLS;
	
	public static int EVENT_CtfTime;
	public static boolean EVENT_CtFrate;
	public static String[] EVENT_CtFStartTime;
	public static boolean EVENT_CtFCategories;
	public static int EVENT_CtFMaxPlayerInTeam;
	public static int EVENT_CtFMinPlayerInTeam;
	public static boolean EVENT_CtFAllowSummons;
	public static boolean EVENT_CtFAllowBuffs;
	public static boolean EVENT_CtFAllowMultiReg;
	public static String EVENT_CtFCheckWindowMethod;
	public static String[] EVENT_CtFFighterBuffs;
	public static String[] EVENT_CtFMageBuffs;
	public static boolean EVENT_CtFBuffPlayers;
	public static String[] EVENT_CtFRewards;
	public static int[] EVENT_CtFOpenCloseDoors;
	public static String[] EVENT_CtF_DISALLOWED_SKILLS;
	
	public static final boolean BBS_COMMISSION_ALLOW = false;
	public static final int[] BBS_COMMISSION_ARMOR_PRICE = null;
	public static final int[] BBS_COMMISSION_WEAPON_PRICE = null;
	public static final int[] BBS_COMMISSION_JEWERLY_PRICE = null;
	public static final int[] BBS_COMMISSION_OTHER_PRICE = null;
	public static final Object[] BBS_COMMISSION_ALLOW_ITEMS = null;
	public static final int BBS_COMMISSION_MAX_ENCHANT = 0;
	public static final Object[] BBS_COMMISSION_NOT_ALLOW_ITEMS = null;
	public static final boolean BBS_COMMISSION_ALLOW_UNDERWEAR = false;
	public static final boolean BBS_COMMISSION_ALLOW_CLOAK = false;
	public static final boolean BBS_COMMISSION_ALLOW_BRACELET = false;
	public static final boolean BBS_COMMISSION_ALLOW_AUGMENTED = false;
	public static final int BBS_COMMISSION_COUNT_TO_PAGE = 0;
	public static final int[] BBS_COMMISSION_MONETS = null;
	
	public static final boolean ENABLE_AUTO_HUNTING_REPORT = true;

	public static boolean AllowBBSSubManager;
	
	public static int TalkGuardChance;
	public static int TalkNormalChance;
	public static int TalkNormalPeriod;
	public static int TalkAggroPeriod;
	
	public static boolean SERVICES_RIDE_HIRE_ENABLED;
	public static boolean CLASS_MASTER_NPC;
	public static boolean SERVICES_DELEVEL_ENABLED;
	public static int SERVICES_DELEVEL_ITEM;
	public static int SERVICES_DELEVEL_COUNT;
	public static int SERVICES_DELEVEL_MIN_LEVEL;
	
	public static double BBS_BUFF_TIME_MOD_SPECIAL;
	public static double BBS_BUFF_TIME_MOD_MUSIC;
	public static double BBS_BUFF_TIME_MOD;
	public static int BBS_BUFF_TIME;
	public static int BBS_BUFF_TIME_SPECIAL;
	public static int BBS_BUFF_TIME_MUSIC;
	public static int BBS_BUFF_ITEM_ID;
	public static int BUFF_PAGE_ROWS;
	public static int MAX_BUFF_PER_SET;
	public static int BBS_BUFF_FREE_LVL;
	public static int BBS_BUFF_ITEM_COUNT;
	public static int MAX_SETS_PER_CHAR;
	public static boolean BUFF_MANUAL_EDIT_SETS;
	public static boolean BBS_BUFF_ALLOW_HEAL;
	public static boolean BBS_BUFF_ALLOW_CANCEL;
	public static int[] BBS_BUFF_IDs;
	public static boolean BBS_BUFF_CURSED;
	public static boolean BBS_BUFF_PK;
	public static boolean BBS_BUFF_LEADER;
	public static boolean BBS_BUFF_NOBLE;
	public static boolean BBS_BUFF_TERITORY;
	public static boolean BBS_BUFF_PEACEZONE_ONLY;
	public static boolean BBS_BUFF_DUEL;
	public static boolean BBS_BUFF_TEMP_ACTION;
	public static boolean BBS_BUFF_CANT_MOVE;
	public static boolean BBS_BUFF_STORE_MODE;
	public static boolean BBS_BUFF_FISHING;
	public static boolean BBS_BUFF_MOUNTED;
	public static boolean BBS_BUFF_VEICHLE;
	public static boolean BBS_BUFF_FLY;
	public static boolean BBS_BUFF_OLY;
	public static boolean BBS_BUFF_ACTION;
	public static boolean BBS_BUFF_DEATH;
	public static boolean BBS_BUFFER_ENABLED;
	
	public static boolean SERVICES_CHANGE_TITLE_COLOR_ENABLED;
	public static int SERVICES_CHANGE_TITLE_COLOR_PRICE;
	public static int SERVICES_CHANGE_TITLE_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_TITLE_COLOR_LIST;
	
	public static boolean CASTLE_GENERATE_TIME_ALTERNATIVE;
	public static int CASTLE_GENERATE_TIME_LOW;
	public static int CASTLE_GENERATE_TIME_HIGH;
	public static Calendar CASTLE_VALIDATION_DATE;
	public static int[] CASTLE_SELECT_HOURS;
	public static int PERIOD_CASTLE_SIEGE;
	
	public static Calendar TW_VALIDATION_DATE;
	public static int TW_SELECT_HOURS;
	public static int DOMINION_INTERVAL_WEEKS;
	public static boolean RETURN_WARDS_WHEN_TW_STARTS;
	public static boolean PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE;
	public static int INTERVAL_FLAG_DROP;
	public static double DOMINION_BADGES_MOD_MULTI;
	public static boolean DOMINION_REMOVE_FLAG_ON_LEAVE_ZONE;
	
	public static boolean PCBANG_POINTS_ENABLED;
	public static int MAX_PC_BANG_POINTS;
	public static double PCBANG_POINTS_BONUS_DOUBLE_CHANCE;
	public static int PCBANG_POINTS_BONUS;
	public static int PCBANG_POINTS_DELAY;
	public static int PCBANG_POINTS_MIN_LVL;
	
	public static boolean DEV_UNDERGROUND_COLISEUM;
	public static int DEV_UNDERGROUND_COLISEUM_MEMBER_COUNT;
	
	public static boolean ALT_DEBUG_ENABLED;
	public static boolean ALT_DEBUG_PVP_ENABLED;
	public static boolean ALT_DEBUG_PVP_DUEL_ONLY;
	public static boolean ALT_DEBUG_PVE_ENABLED;
	
	public static int CLAN_HALL_AUCTION_LENGTH;
	
	public static double CRAFT_MASTERWORK_CHANCE;
	public static double CRAFT_DOUBLECRAFT_CHANCE;
	
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_RARE_XPSP_RATE;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	
	/** Thread pools size */
	public static int SCHEDULED_THREAD_POOL_SIZE;
	public static int EXECUTOR_THREAD_POOL_SIZE;
	
	public static int THREAD_P_MOVE;
	public static int NPC_AI_MAX_THREAD;
	public static int PLAYER_AI_MAX_THREAD;
	public static int THREAD_P_PATHFIND;
	
	public static boolean NEW_THREAD_FOR_AI;
	
	public static boolean ENABLE_RUNNABLE_STATS;
	
	/** Network settings */
	public static SelectorConfig SELECTOR_CONFIG = new SelectorConfig();
	
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_ONLY_ADENA;
	public static boolean AUTO_LOOT_INDIVIDUAL;
	public static boolean AUTO_LOOT_FROM_RAIDS;
	
	/** Auto-loot for/from players with karma also? */
	public static boolean AUTO_LOOT_PK;
	
	/** Character name template */
	public static String CNAME_TEMPLATE;
	
	public static int CNAME_MAXLEN = 32;
	
	/** Clan name template */
	public static String CLAN_NAME_TEMPLATE;
	
	/** Clan title template */
	public static String CLAN_TITLE_TEMPLATE;
	
	/** Ally name template */
	public static String ALLY_NAME_TEMPLATE;
	
	/** Global chat state */
	public static boolean GLOBAL_SHOUT;
	public static int PVP_COUNT_SHOUT;
	public static int ONLINE_TIME_SHOUT;
	public static int LEVEL_FOR_SHOUT;
	
	public static boolean GLOBAL_TRADE_CHAT;
	public static int ONLINE_TIME_TRADE;
	public static int PVP_COUNT_TRADE;
	public static int LEVEL_FOR_TRADE;
	
	public static int CHAT_RANGE;
	public static int SHOUT_OFFSET;
	
	public static int CHAT_SHOUT_TIME_DELAY;
	public static int CHAT_TRADE_TIME_DELAY;
	public static int CHAT_HERO_TIME_DELAY;
	
	public static boolean USE_TRADE_WORDS_ON_GLOBAL_CHAT;
	
	public static GArray<String> TRADE_WORDS;
	public static boolean TRADE_CHATS_REPLACE_FROM_ALL;
	public static boolean TRADE_CHATS_REPLACE_FROM_SHOUT;
	public static boolean TRADE_CHATS_REPLACE_FROM_TRADE;
	
	/** For test servers - evrybody has admin rights */
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	
	public static double RAID_RESPAWN_MULTIPLIER;
	
	public static boolean ALT_ALLOW_AUGMENT_ALL;
	public static boolean ALT_ALLOW_DROP_AUGMENTED;
	
	public static boolean ALT_GAME_UNREGISTER_RECIPE;
	
	/** Delay for announce SS period (in minutes) */
	public static int SS_ANNOUNCE_PERIOD;
	
	/** Petition manager */
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	
	/** Show mob stats/droplist to players? */
	public static boolean ALT_GAME_SHOW_DROPLIST;
	public static boolean ALT_FULL_NPC_STATS_PAGE;
	public static boolean ALLOW_NPC_SHIFTCLICK;
	public static boolean ALT_PLAYER_SHIFTCLICK;
	public static boolean ALT_BYPASS_SHIFT_CLICK_NPC_TO_CB;
	
	public static boolean ALT_ALLOW_SELL_COMMON;
	public static boolean ALT_ALLOW_SHADOW_WEAPONS;
	public static int[] ALT_DISABLED_MULTISELL;
	public static int[] ALT_SHOP_PRICE_LIMITS;
	public static int[] ALT_SHOP_FORBIDDEN_ITEMS;
	
	public static int[] ALT_ALLOWED_PET_POTIONS;
	
	public static boolean SKILL_CHANCE_CALCULATED_BY_ENCHANT_LEVEL;
	public static double BACK_BLOW_MULTIPLIER;
	public static double NON_BACK_BLOW_MULTIPLIER;
	public static boolean PDAM_OLD_FORMULA;
	public static boolean SKILL_FORCE_H5_FORMULA;
	public static boolean SKILLS_CHANCE_SHOW;
	public static int SKILLS_CHANCE_MIN;
	public static double SKILLS_CHANCE_MOD_MAGE;
	public static double SKILLS_CHANCE_POW_MAGE;
	public static double SKILLS_CHANCE_MOD_FIGHTER;
	public static double SKILLS_CHANCE_POW_FIGHTER;
	public static int SKILLS_CHANCE_CAP;
	public static boolean SKILLS_CHANCE_CAP_ONLY_PLAYERS;
	public static double SKILLS_MOB_CHANCE;
	public static double SKILLS_DEBUFF_MOB_CHANCE;
	public static boolean SHIELD_SLAM_BLOCK_IS_MUSIC;
	public static boolean ALT_SAVE_UNSAVEABLE;
	public static int ALT_SAVE_EFFECTS_REMAINING_TIME;
	public static boolean ALT_SHOW_REUSE_MSG;
	public static boolean ALT_DELETE_SA_BUFFS;
	public static int SKILLS_CAST_TIME_MIN;
	public static double PHYS_SKILLS_DAMAGE_POW;
	public static double SKILL_PROF_MULTIPLIER;
	public static double BASE_MAGICAL_CRIT_RATE;
	
	/** Конфигурация использования итемов по умолчанию поушены */
	public static int[] ITEM_USE_LIST_ID;
	public static boolean ITEM_USE_IS_COMBAT_FLAG;
	public static boolean ITEM_USE_IS_ATTACK;
	public static boolean ITEM_USE_IS_EVENTS;
	
	/** Настройки для евента Файт Клуб */
	public static boolean FIGHT_CLUB_ENABLED;
	public static int MINIMUM_LEVEL_TO_PARRICIPATION;
	public static int MAXIMUM_LEVEL_TO_PARRICIPATION;
	public static int MAXIMUM_LEVEL_DIFFERENCE;
	public static String[] ALLOWED_RATE_ITEMS;
	public static int PLAYERS_PER_PAGE;
	public static int ARENA_TELEPORT_DELAY;
	public static boolean CANCEL_BUFF_BEFORE_FIGHT;
	public static boolean UNSUMMON_PETS;
	public static boolean UNSUMMON_SUMMONS;
	public static boolean REMOVE_CLAN_SKILLS;
	public static boolean REMOVE_HERO_SKILLS;
	public static int TIME_TO_PREPARATION;
	public static int FIGHT_TIME;
	public static boolean ALLOW_DRAW;
	public static int TIME_TELEPORT_BACK;
	public static boolean FIGHT_CLUB_ANNOUNCE_RATE;
	public static boolean FIGHT_CLUB_ANNOUNCE_RATE_TO_SCREEN;
	public static boolean FIGHT_CLUB_ANNOUNCE_START_TO_SCREEN;
	
	/** Титул при создании чара */
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	
	/** Таймаут на использование social action */
	public static boolean ALT_SOCIAL_ACTION_REUSE;
	
	/** Отключение книг для изучения скилов */
	public static boolean ALT_DISABLE_SPELLBOOKS;
	
	/** Alternative gameing - loss of XP on death */
	public static boolean ALT_GAME_DELEVEL;
	
	/** Разрешать ли на арене бои за опыт */
	public static boolean ALT_ARENA_EXP;
	
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM;
	public static int ALT_GAME_START_LEVEL_TO_SUBCLASS;
	public static int ALT_GAME_LEVEL_TO_GET_SUBCLASS;
	public static int ALT_MAX_LEVEL;
	public static int ALT_MAX_SUB_LEVEL;
	public static int ALT_GAME_SUB_ADD;
	public static boolean ALL_SUBCLASSES_AVAILABLE;
	public static boolean ALT_GAME_SUB_BOOK;
	public static boolean ALT_NO_LASTHIT;
	public static boolean ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY;
	public static boolean ALT_KAMALOKA_NIGHTMARE_REENTER;
	public static boolean ALT_KAMALOKA_ABYSS_REENTER;
	public static boolean ALT_KAMALOKA_LAB_REENTER;
	public static boolean ALT_PET_HEAL_BATTLE_ONLY;
	
	public static boolean ALT_SIMPLE_SIGNS;
	public static boolean ALT_TELE_TO_CATACOMBS;
	public static boolean ALT_BS_CRYSTALLIZE;
	public static int ALT_MAMMON_EXCHANGE;
	public static int ALT_MAMMON_UPGRADE;
	
	public static int ALT_BUFF_LIMIT;
	
	public static int MULTISELL_SIZE;
	
	public static boolean SERVICES_CHANGE_NICK_ENABLED;
	public static boolean SERVICES_CHANGE_NICK_ALLOW_SYMBOL;
	public static int SERVICES_CHANGE_NICK_PRICE;
	public static int SERVICES_CHANGE_NICK_ITEM;
	
	public static boolean SERVICES_CHANGE_CLAN_NAME_ENABLED;
	public static int SERVICES_CHANGE_CLAN_NAME_PRICE;
	public static int SERVICES_CHANGE_CLAN_NAME_ITEM;
	
	public static boolean SERVICES_CHANGE_PET_NAME_ENABLED;
	public static int SERVICES_CHANGE_PET_NAME_PRICE;
	public static int SERVICES_CHANGE_PET_NAME_ITEM;
	
	public static boolean SERVICES_EXCHANGE_BABY_PET_ENABLED;
	public static int SERVICES_EXCHANGE_BABY_PET_PRICE;
	public static int SERVICES_EXCHANGE_BABY_PET_ITEM;
	
	public static boolean SERVICES_CHANGE_SEX_ENABLED;
	public static int SERVICES_CHANGE_SEX_PRICE;
	public static int SERVICES_CHANGE_SEX_ITEM;
	
	public static boolean SERVICES_CHANGE_BASE_ENABLED;
	public static int SERVICES_CHANGE_BASE_PRICE;
	public static int SERVICES_CHANGE_BASE_ITEM;
	
	public static boolean SERVICES_SEPARATE_SUB_ENABLED;
	public static int SERVICES_SEPARATE_SUB_PRICE;
	public static int SERVICES_SEPARATE_SUB_ITEM;
	
	public static boolean SERVICES_CHANGE_NICK_COLOR_ENABLED;
	public static int SERVICES_CHANGE_NICK_COLOR_PRICE;
	public static int SERVICES_CHANGE_NICK_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_NICK_COLOR_LIST;
	
	public static boolean SERVICES_BASH_ENABLED;
	public static boolean SERVICES_BASH_SKIP_DOWNLOAD;
	public static int SERVICES_BASH_RELOAD_TIME;
	
	public static boolean SERVICES_NOBLESS_SELL_ENABLED;
	public static int SERVICES_NOBLESS_SELL_PRICE;
	public static int SERVICES_NOBLESS_SELL_ITEM;
	
	public static boolean SERVICES_HERO_SELL_ENABLED;
	public static int[] SERVICES_HERO_SELL_DAY;
	public static int[] SERVICES_HERO_SELL_PRICE;
	public static int[] SERVICES_HERO_SELL_ITEM;
	
	public static boolean SERVICES_WASH_PK_ENABLED;
	public static int SERVICES_WASH_PK_ITEM;
	public static int SERVICES_WASH_PK_PRICE;
	
	public static boolean SERVICES_EXPAND_INVENTORY_ENABLED;
	public static int SERVICES_EXPAND_INVENTORY_PRICE;
	public static int SERVICES_EXPAND_INVENTORY_ITEM;
	public static int SERVICES_EXPAND_INVENTORY_MAX;
	
	public static boolean SERVICES_EXPAND_WAREHOUSE_ENABLED;
	public static int SERVICES_EXPAND_WAREHOUSE_PRICE;
	public static int SERVICES_EXPAND_WAREHOUSE_ITEM;
	public static int SERVICES_EXPAND_WAREHOUSE_MAX;
	
	public static boolean SERVICES_EXPAND_CWH_ENABLED;
	public static int SERVICES_EXPAND_CWH_PRICE;
	public static int SERVICES_EXPAND_CWH_ITEM;
	
	public static boolean SERVICES_CLAN_REP_POINTS;
	public static int SERVICE_CLAN_REP_ITEM;
	public static int SERVICE_CLAN_REP_COST;
	public static int SERVICE_CLAN_REP_ADD;
	
	public static boolean SERVICES_OFFLINE_TRADE_ALLOW;
	public static boolean SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE;
	public static int SERVICES_OFFLINE_TRADE_MIN_LEVEL;
	public static int SERVICES_OFFLINE_TRADE_NAME_COLOR;
	public static int SERVICES_OFFLINE_TRADE_PRICE;
	public static int SERVICES_OFFLINE_TRADE_PRICE_ITEM;
	public static long SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK;
	public static boolean SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART;
	public static boolean TRANSFORM_ON_OFFLINE_TRADE;
	public static int TRANSFORMATION_ID_MALE;
	public static int TRANSFORMATION_ID_FEMALE;
	
	public static boolean SERVICES_GIRAN_HARBOR_ENABLED;
	public static boolean SERVICES_PARNASSUS_ENABLED;
	public static boolean SERVICES_PARNASSUS_NOTAX;
	public static long SERVICES_PARNASSUS_PRICE;
	
	public static boolean SERVICES_ALLOW_LOTTERY;
	public static int SERVICES_LOTTERY_PRIZE;
	public static int SERVICES_LOTTERY_TICKET_PRICE;
	public static double SERVICES_LOTTERY_5_NUMBER_RATE;
	public static double SERVICES_LOTTERY_4_NUMBER_RATE;
	public static double SERVICES_LOTTERY_3_NUMBER_RATE;
	public static double SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static int[] SERVICES_LOTTERY_STARTING_DATE;
	
	public static boolean SERVICES_ALLOW_ROULETTE;
	public static long SERVICES_ROULETTE_MIN_BET;
	public static long SERVICES_ROULETTE_MAX_BET;
	
	public static int EXPELLED_MEMBER_PENALTY;
	public static int LEAVED_ALLY_PENALTY;
	public static int DISSOLVED_ALLY_PENALTY;
	public static int DISSOLVED_CLAN_PENALTY;
	public static int CLAN_DISBAND_TIME;
	public static int LEAVE_CLAN_PENALTY;
	
	public static boolean ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE;
	public static boolean ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_ALLOW_ADENA_DAWN;

	// -------------------------------------------------------------------------------------------------------
	public static int ATTRIBUTE_ARMOR;
	public static int ATTRIBUTE_WEAPON;
	public static int ATTRIBUTE_FIRST_WEAPON;
	
	public static int MAX_ATTRIBUTE_ARMOR;
	public static int MAX_ATTRIBUTE_WEAPON;
	
	public static boolean SPAWN_CITIES_TREE;
	public static boolean SPAWN_NPC_CLASS_MASTER;
	
	public static int MAX_PARTY_SIZE;
	public static boolean ALLOW_SPAWN_CUSTOM_HALL_NPC;
	public static boolean ADEPT_ENABLE;
	// By SmokiMo
	public static int HENNA_MAX_VALUE;
	public static boolean ENEBLE_TITLE_COLOR_MOD;
	public static String TYPE_TITLE_COLOR_MOD;
	public static int COUNT_TITLE_1;
	public static int TITLE_COLOR_1;
	public static int COUNT_TITLE_2;
	public static int TITLE_COLOR_2;
	public static int COUNT_TITLE_3;
	public static int TITLE_COLOR_3;
	public static int COUNT_TITLE_4;
	public static int TITLE_COLOR_4;
	public static int COUNT_TITLE_5;
	public static int TITLE_COLOR_5;
	public static boolean ENEBLE_NAME_COLOR_MOD;
	public static String TYPE_NAME_COLOR_MOD;
	public static int COUNT_NAME_1;
	public static int NAME_COLOR_1;
	public static int COUNT_NAME_2;
	public static int NAME_COLOR_2;
	public static int COUNT_NAME_3;
	public static int NAME_COLOR_3;
	public static int COUNT_NAME_4;
	public static int NAME_COLOR_4;
	public static int COUNT_NAME_5;
	public static int NAME_COLOR_5;
	// add by 4ipolino
	public static boolean NEW_CHAR_IS_NOBLE;
	public static boolean NEW_CHAR_IS_HERO;
	public static boolean ANNOUNCE_SPAWN_RB;
	public static boolean ANNOUNCE_SPAWN_RB_REGION;
	// Перенос персонажей между аккаунтами
	public static boolean ACC_MOVE_ENABLED;
	public static int ACC_MOVE_ITEM;
	public static int ACC_MOVE_PRICE;
	
	/** voiced commands **/
	public static boolean DISABLE_VOICED_COMMANDS;
	public static boolean ENABLE_KM_ALL_TO_ME;
	public static boolean ALLOW_ONLINE_COMMAND;
	public static boolean ENABLE_CASTLE_COMMAND;
	public static boolean ENABLE_CFG_COMMAND;
	public static boolean ENABLE_CLAN_WAREHOUSE_COMMAND;
	public static boolean ENABLE_HELLBOUND_VOICED;
	public static boolean ENABLE_HELP_COMMAND;
	public static boolean ENABLE_PASSWORD_COMMAND;
	public static boolean ENABLE_PING_COMMAND;
	public static boolean ENABLE_SC_INFO_COMMAND;
	public static boolean PARTY_SEARCH_COMMANDS;
	public static boolean ENABLE_NPCSPAWN_COMMAND;
	public static boolean ENABLE_WHEREIS_COMMAND;
	public static boolean ENABLE_COMBINE_TALISMAN_COMMAND;
	public static boolean ENABLE_OPENATOD_COMMAND;
	public static boolean ENABLE_TRADELIST_VOICED;
	public static boolean ENABLE_EXP_COMMAND;
	public static boolean ENABLE_REPAIR_COMMAND;
	public static boolean ALLOW_LOCK_COMMAND;
	public static boolean ALLOW_WHOAMI_COMMAND;
	
	// CustomSpawnNewChar
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	
	/** Olympiad Compitition Starting time */
	public static int ALT_OLY_START_TIME;
	/** Olympiad Compition Min */
	public static int ALT_OLY_MIN;
	/** Olympaid Comptetition Period */
	public static long ALT_OLY_CPERIOD;
	/** Olympaid Weekly Period */
	public static long ALT_OLY_WPERIOD;
	/** Olympaid Validation Period */
	public static long ALT_OLY_VPERIOD;
	public static int[] ALT_OLY_DATE_END;
	
	public static boolean ENABLE_OLYMPIAD;
	public static boolean ENABLE_OLYMPIAD_SPECTATING;
	
	public static int CLASS_GAME_MIN;
	public static int NONCLASS_GAME_MIN;
	public static int TEAM_GAME_MIN;
	
	public static int GAME_MAX_LIMIT;
	public static int GAME_CLASSES_COUNT_LIMIT;
	public static int GAME_NOCLASSES_COUNT_LIMIT;
	public static int GAME_TEAM_COUNT_LIMIT;
	
	public static int ALT_OLY_REG_DISPLAY;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_TEAM_RITEM_C;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int OLYMPIAD_STADIAS_COUNT;
	public static int OLYMPIAD_BATTLES_FOR_REWARD;
	public static int OLYMPIAD_POINTS_DEFAULT;
	public static int OLYMPIAD_POINTS_WEEKLY;
	public static boolean OLYMPIAD_OLDSTYLE_STAT;
	public static boolean OLYMPIAD_PLAYER_IP;
	public static boolean OLYMPIAD_PLAYER_HWID;
	public static int OLYMPIAD_BEGIN_TIME;
	public static boolean OLYMPIAD_BAD_ENCHANT_ITEMS_ALLOW;
	
	public static boolean OLY_ENCH_LIMIT_ENABLE;
	public static int OLY_ENCHANT_LIMIT_WEAPON;
	public static int OLY_ENCHANT_LIMIT_ARMOR;
	public static int OLY_ENCHANT_LIMIT_JEWEL;
	
	public static long NONOWNER_ITEM_PICKUP_DELAY;
	
	public static int[] HERO_DIARY_EXCLUDED_BOSSES;
	
	/** Logging Chat Window */
	public static boolean LOG_CHAT;
	
	/** Rate control */
	public static double RATE_XP;
	public static double RATE_SP;
	public static TIntIntHashMap EXP_RATE_BY_LEVEL;
	public static TIntIntHashMap SP_RATE_BY_LEVEL;
	public static double RATE_QUESTS_REWARD;
	public static double RATE_QUESTS_REWARD_EXPSP;
	public static double QUEST_REWARD_ADENA;
	public static double RATE_QUESTS_DROP;
	public static double RATE_CLAN_REP_SCORE;
	public static int RATE_CLAN_REP_SCORE_MAX_AFFECTED;
	public static int MAX_CLAN_REPUTATIONS_POINTS;
	public static double RATE_DROP_ADENA;
	public static double RATE_DROP_AA_ADENA;
	public static double RATE_DROP_CHAMPION;
	public static double RATE_CHAMPION_DROP_ADENA;
	public static double RATE_DROP_SPOIL_CHAMPION;
	public static double RATE_DROP_ITEMS;
	public static double RATE_CHANCE_GROUP_DROP_ITEMS;
	public static double RATE_CHANCE_DROP_ITEMS;
	public static double RATE_CHANCE_DROP_HERBS;
	public static double RATE_CHANCE_SPOIL;
	public static double RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_EPOLET;
	public static boolean NO_RATE_ENCHANT_SCROLL;
	public static double RATE_ENCHANT_SCROLL;
	public static boolean CHAMPION_DROP_ONLY_ADENA;
	public static boolean NO_RATE_HERBS;
	public static double RATE_DROP_HERBS;
	public static boolean NO_RATE_ATT;
	public static double RATE_DROP_ATT;
	public static boolean NO_RATE_LIFE_STONE;
	public static boolean NO_RATE_CODEX_BOOK;
	public static boolean NO_RATE_FORGOTTEN_SCROLL;
	public static double RATE_DROP_LIFE_STONE;
	public static boolean NO_RATE_KEY_MATERIAL;
	public static double RATE_DROP_KEY_MATERIAL;
	public static boolean NO_RATE_RECIPES;
	public static double RATE_DROP_RECIPES;
	public static double RATE_DROP_COMMON_ITEMS;
	public static boolean NO_RATE_RAIDBOSS;
	public static double RATE_DROP_RAIDBOSS;
	public static double RATE_DROP_SPOIL;
	public static int[] NO_RATE_ITEMS;
	public static boolean NO_RATE_SIEGE_GUARD;
	public static double RATE_DROP_SIEGE_GUARD;
	public static double RATE_MANOR;
	public static double RATE_FISH_DROP_COUNT;
	public static boolean RATE_PARTY_MIN;
	public static double RATE_HELLBOUND_CONFIDENCE;
	public static boolean NO_RATE_EQUIPMENT;
	public static Map<Integer, Float> RATE_DROP_ITEMS_ID;
	public static Map<Integer, Float> RATE_DROP_SPOIL_ITEMS_ID;
	public static Map<Integer, Float> RATE_CHANCE_DROP_ITEMS_ID;
	public static Map<Integer, Float> RATE_CHANCE_DROP_SPOIL_ITEMS_ID;
	
	public static int RATE_MOB_SPAWN;
	public static int RATE_MOB_SPAWN_MIN_LEVEL;
	public static int RATE_MOB_SPAWN_MAX_LEVEL;
	
	/** Player Drop Rate control */
	public static boolean KARMA_NEEDED_TO_DROP;
	
	public static int KARMA_DROP_ITEM_LIMIT;
	
	public static int KARMA_RANDOM_DROP_LOCATION_LIMIT;
	
	public static double KARMA_DROPCHANCE_BASE;
	public static double KARMA_DROPCHANCE_MOD;
	public static double NORMAL_DROPCHANCE_BASE;
	public static int DROPCHANCE_EQUIPMENT;
	public static int DROPCHANCE_EQUIPPED_WEAPON;
	public static int DROPCHANCE_ITEM;
	
	public static int AUTODESTROY_ITEM_AFTER;
	public static int AUTODESTROY_PLAYER_ITEM_AFTER;
	
	public static int DELETE_DAYS;
	
	/** Datapack root directory */
	public static File DATAPACK_ROOT;
	
	public static int CLANHALL_BUFFTIME_MODIFIER;
	public static int SONGDANCETIME_MODIFIER;
	
	public static double MAXLOAD_MODIFIER;
	public static double GATEKEEPER_MODIFIER;
	public static boolean ALT_IMPROVED_PETS_LIMITED_USE;
	public static int GATEKEEPER_FREE;
	public static int CRUMA_GATEKEEPER_LVL;
	
	public static double ALT_CHAMPION_CHANCE1;
	public static double ALT_CHAMPION_CHANCE2;
	public static boolean ALT_CHAMPION_CAN_BE_AGGRO;
	public static boolean ALT_CHAMPION_CAN_BE_SOCIAL;
	public static boolean ALT_CHAMPION_DROP_HERBS;
	public static boolean ALT_SHOW_MONSTERS_LVL;
	public static boolean ALT_SHOW_MONSTERS_AGRESSION;
	public static int ALT_CHAMPION_TOP_LEVEL;
	public static int ALT_CHAMPION_MIN_LEVEL;
	
	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean DROP_CURSED_WEAPONS_ON_KICK;
	public static boolean ALLOW_NOBLE_TP_TO_ALL;
	
	public static boolean SELL_ALL_ITEMS_FREE;
	/** Pets */
	public static int SWIMING_SPEED;
	public static boolean SAVE_PET_EFFECT;
	
	/** protocol revision */
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;
	
	/** random animation interval */
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	
	public static boolean USE_CLIENT_LANG;
	public static Language DEFAULT_LANG;
	
	/** Время запланированного на определенное время суток рестарта */
	public static String RESTART_AT_TIME;
	
	public static int GAME_SERVER_LOGIN_PORT;
	public static boolean GAME_SERVER_LOGIN_CRYPT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String INTERNAL_HOSTNAME;
	public static String[] EXTERNAL_HOSTNAME;
	public static String[] EXTERNAL_LOGIN_HOSTNAME;
	
	public static boolean SECOND_AUTH_ENABLED;
	public static boolean SECOND_AUTH_BAN_ACC;
	public static boolean SECOND_AUTH_STRONG_PASS;
	public static int SECOND_AUTH_MAX_ATTEMPTS;
	public static long SECOND_AUTH_BAN_TIME;
	public static String SECOND_AUTH_REC_LINK;
	
	public static boolean SERVER_SIDE_NPC_NAME;
	public static boolean SERVER_SIDE_NPC_TITLE;
	public static boolean SERVER_SIDE_NPC_TITLE_ETC;
	public static boolean NPC_ALLOW_HIT;
	
	public static String CLASS_MASTERS_PRICE;
	public static String CLASS_MASTERS_PRICE_ITEM;
	public static List<Integer> ALLOW_CLASS_MASTERS_LIST = new ArrayList<Integer>();
	public static int[] CLASS_MASTERS_PRICE_LIST = new int[4];
	public static int[] CLASS_MASTERS_PRICE_ITEM_LIST = new int[4];
	public static int CLASS_MASTERS_SUB_ITEM;
	public static int CLASS_MASTERS_SUB_PRICE;
	
	public static boolean ALLOW_EVENT_GATEKEEPER;
	
	public static boolean ITEM_BROKER_ITEM_SEARCH;
	
	/** Inventory slots limits */
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int QUEST_INVENTORY_MAXIMUM;
	
	/** Warehouse slots limits */
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	
	public static int FREIGHT_SLOTS;
	
	/** Spoil Rates */
	public static double BASE_SPOIL_RATE;
	public static double MINIMUM_SPOIL_RATE;
	public static boolean ALT_SPOIL_FORMULA;
	
	/** Manor Config */
	public static double MANOR_SOWING_BASIC_SUCCESS;
	public static double MANOR_SOWING_ALT_BASIC_SUCCESS;
	public static double MANOR_HARVESTING_BASIC_SUCCESS;
	public static int MANOR_DIFF_PLAYER_TARGET;
	public static double MANOR_DIFF_PLAYER_TARGET_PENALTY;
	public static int MANOR_DIFF_SEED_TARGET;
	public static double MANOR_DIFF_SEED_TARGET_PENALTY;
	
	/** Karma System Variables */
	public static int KARMA_MIN_KARMA;
	public static int KARMA_SP_DIVIDER;
	public static int KARMA_LOST_BASE;
	
	public static int MIN_PK_TO_ITEMS_DROP;
	public static boolean DROP_ITEMS_ON_DIE;
	public static boolean DROP_ITEMS_AUGMENTED;
	
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();
	
	public static int PVP_TIME;
	
	/** Karma Punishment */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	
	
	public static int ENCHANT_ATTRIBUTE_STONE_CHANCE;
	public static int ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE;
	
	public static int FAME_REWARD_FORTRESS;
	public static int FAME_REWARD_CASTLE;
	
	public static boolean FORTRESS_REMOVE_FLAG_ON_LEAVE_ZONE;
	
	public static int RATE_SIEGE_FAME_MIN;
	public static int RATE_SIEGE_FAME_MAX;
	
	public static int RATE_DOMINION_SIEGE_FAME_MIN;
	public static int RATE_DOMINION_SIEGE_FAME_MAX;
	
	public static boolean ALT_NOT_ALLOW_TW_WARDS_IN_CLANHALLS;
	
	public static int MAX_SIEGE_CLANS;
	public static int MAX_CLAN_WARS_DECLARATION;
	
	public static boolean REGEN_SIT_WAIT;
	
	public static double RATE_RAID_REGEN;
	public static double RATE_RAID_DEFENSE;
	public static double RATE_RAID_ATTACK;
	public static double RATE_EPIC_DEFENSE;
	public static double RATE_EPIC_ATTACK;
	public static int RAID_MAX_LEVEL_DIFF;
	public static boolean PARALIZE_ON_RAID_DIFF;
	
	public static double ALT_PK_DEATH_RATE;
	public static int STARTING_ADENA;
	
	public static int STARTING_LVL;
	/** Deep Blue Mobs' Drop Rules Enabled */
	public static boolean DEEPBLUE_DROP_RULES;
	public static int DEEPBLUE_DROP_MAXDIFF;
	public static int DEEPBLUE_DROP_RAID_MAXDIFF;
	public static boolean UNSTUCK_SKILL;
	
	/** telnet enabled */
	public static boolean IS_TELNET_ENABLED;
	public static String TELNET_DEFAULT_ENCODING;
	public static String TELNET_PASSWORD;
	public static String TELNET_HOSTNAME;
	public static int TELNET_PORT;
	
	/** Percent CP is restore on respawn */
	public static double RESPAWN_RESTORE_CP;
	/** Percent HP is restore on respawn */
	public static double RESPAWN_RESTORE_HP;
	/** Percent MP is restore on respawn */
	public static double RESPAWN_RESTORE_MP;
	
	/** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	/** Maximum number of available slots for pvt stores (sell/buy) - Others */
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static int MAX_PVTCRAFT_SLOTS;
	
	public static boolean SENDSTATUS_TRADE_JUST_OFFLINE;
	public static double SENDSTATUS_TRADE_MOD;
	public static boolean SHOW_OFFLINE_MODE_IN_ONLINE;
	
	public static boolean ALLOW_CH_DOOR_OPEN_ON_CLICK;
	public static boolean ALT_CH_ALL_BUFFS;
	public static boolean ALT_CH_ALLOW_1H_BUFFS;
	public static boolean ALT_CH_SIMPLE_DIALOG;
	public static double CLANHALL_LEASE_MULTIPLIER;
	
	public static int CH_BID_GRADE1_MINCLANLEVEL;
	public static int CH_BID_GRADE1_MINCLANMEMBERS;
	public static int CH_BID_GRADE1_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE2_MINCLANLEVEL;
	public static int CH_BID_GRADE2_MINCLANMEMBERS;
	public static int CH_BID_GRADE2_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE3_MINCLANLEVEL;
	public static int CH_BID_GRADE3_MINCLANMEMBERS;
	public static int CH_BID_GRADE3_MINCLANMEMBERSLEVEL;
	
	public static double RESIDENCE_LEASE_FUNC_MULTIPLIER;
	public static double RESIDENCE_LEASE_MULTIPLIER;
	
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;
	public static int[] REQUEST_IDS;
	
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	
	public static int NORMAL_NAME_COLOUR;
	public static int CLANLEADER_NAME_COLOUR;
	
	public static boolean TRIVIA_ENABLED;
	public static boolean TRIVIA_REMOVE_QUESTION;
	public static boolean TRIVIA_REMOVE_QUESTION_NO_ANSWER;
	public static int TRIVIA_START_TIME_HOUR;
	public static int TRIVIA_START_TIME_MIN;
	public static int TRIVIA_WORK_TIME;
	public static int TRIVIA_TIME_ANSER;
	public static int TRIVIA_TIME_PAUSE;
	public static String TRIVIA_REWARD_FIRST;
	public static String TRIVIA_REWARD_REST;
	
	public static void loadTriviaSettings()
	{
		ExProperties TriviaSettings = load(TRIVIA_CONFIG_FILE);
		
		TRIVIA_ENABLED = TriviaSettings.getProperty("Trivia_Enabled", false);
		TRIVIA_REMOVE_QUESTION = TriviaSettings.getProperty("Trivia_Remove_Question", false);
		TRIVIA_REMOVE_QUESTION_NO_ANSWER = TriviaSettings.getProperty("Trivia_Remove_Question_No_Answer", false);
		TRIVIA_START_TIME_HOUR = TriviaSettings.getProperty("Trivia_Start_Time_Hour", 16);
		TRIVIA_START_TIME_MIN = TriviaSettings.getProperty("Trivia_Start_Time_Minute", 16);
		TRIVIA_WORK_TIME = TriviaSettings.getProperty("Trivia_Work_Time", 2);
		TRIVIA_TIME_ANSER = TriviaSettings.getProperty("Trivia_Time_Answer", 1);
		TRIVIA_TIME_PAUSE = TriviaSettings.getProperty("Trivia_Time_Pause", 1);
		TRIVIA_REWARD_FIRST = TriviaSettings.getProperty("Trivia_Reward_First", "57,1,100;57,2,100;");
		TRIVIA_REWARD_REST = TriviaSettings.getProperty("Trivia_Reward_Other", "57,1,100;57,2,100;");
		
	}
	
	/** AI */
	public static int AI_TASK_MANAGER_COUNT;
	public static long AI_TASK_ATTACK_DELAY;
	public static long AI_TASK_ACTIVE_DELAY;
	public static boolean BLOCK_ACTIVE_TASKS;
	public static boolean ALWAYS_TELEPORT_HOME;
	public static boolean RND_WALK;
	public static int RND_WALK_RATE;
	public static int RND_ANIMATION_RATE;
	
	public static int AGGRO_CHECK_INTERVAL;
	public static long NONAGGRO_TIME_ONTELEPORT;
	
	/** Maximum range mobs can randomly go from spawn point */
	public static int MAX_DRIFT_RANGE;
	
	/** Maximum range mobs can pursue agressor from spawn point */
	public static int MAX_PURSUE_RANGE;
	public static int MAX_PURSUE_UNDERGROUND_RANGE;
	public static int MAX_PURSUE_RANGE_RAID;
	
	public static boolean ALT_DEATH_PENALTY;
	public static boolean ALLOW_DEATH_PENALTY_C5;
	public static int ALT_DEATH_PENALTY_C5_CHANCE;
	public static boolean ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY;
	public static int ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
	public static int ALT_DEATH_PENALTY_C5_KARMA_PENALTY;
	
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_FORGOTTEN_SKILLS;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	
	public static int MOVE_PACKET_DELAY;
	public static int ATTACK_PACKET_DELAY;
	public static int ATTACK_END_DELAY;
	
	public static boolean DAMAGE_FROM_FALLING;
	
	/** Community Board */
	public static boolean USE_BBS_BUFER_IS_COMBAT;
	public static boolean USE_BBS_BUFER_IS_CURSE_WEAPON;
	public static boolean USE_BBS_BUFER_IS_EVENTS;
	public static boolean USE_BBS_TELEPORT_IS_COMBAT;
	public static boolean USE_BBS_TELEPORT_IS_EVENTS;
	public static boolean USE_BBS_PROF_IS_COMBAT;
	public static boolean USE_BBS_PROF_IS_EVENTS;
	public static boolean SAVE_BBS_TELEPORT_IS_EPIC;
	public static boolean SAVE_BBS_TELEPORT_IS_BZ;
	public static boolean COMMUNITYBOARD_ENABLED;
	public static boolean ALLOW_COMMUNITYBOARD_IN_COMBAT;
	public static boolean ALLOW_COMMUNITYBOARD_IS_IN_SIEGE;
	public static boolean COMMUNITYBOARD_BUFFER_ENABLED;
	public static boolean COMMUNITYBOARD_BUFFER_MAX_LVL_ALLOW;
	public static boolean COMMUNITYBOARD_BUFFER_SIEGE_ENABLED;
	public static boolean COMMUNITYBOARD_BUFFER_NO_IS_IN_PEACE_ENABLED;
	public static boolean COMMUNITYBOARD_SELL_ENABLED;
	public static boolean COMMUNITYBOARD_SHOP_ENABLED;
	public static boolean COMMUNITYBOARD_SHOP_NO_IS_IN_PEACE_ENABLED;
	public static boolean COMMUNITYBOARD_BUFFER_PET_ENABLED;
	public static boolean COMMUNITYBOARD_BUFFER_SAVE_ENABLED;
	public static boolean COMMUNITYBOARD_ABNORMAL_ENABLED;
	public static boolean COMMUNITYBOARD_INSTANCE_ENABLED;
	public static boolean COMMUNITYBOARD_EVENTS_ENABLED;
	public static int COMMUNITYBOARD_BUFF_TIME;
	public static int COMMUNITYBOARD_BUFFER_MAX_LVL;
	public static int COMMUNITYBOARD_BUFF_PETS_TIME;
	public static int COMMUNITYBOARD_BUFF_COMBO_TIME;
	public static int COMMUNITYBOARD_BUFF_SONGDANCE_TIME;
	public static int COMMUNITYBOARD_BUFF_PICE;
	public static int COMMUNITYBOARD_BUFF_SAVE_PICE;
	public static List<Integer> COMMUNITYBOARD_BUFF_ALLOW = new ArrayList<Integer>();
	public static List<Integer> COMMUNITI_LIST_MAGE_SUPPORT = new ArrayList<Integer>();
	public static List<Integer> COMMUNITI_LIST_FIGHTER_SUPPORT = new ArrayList<Integer>();
	public static List<String> COMMUNITYBOARD_MULTISELL_ALLOW = new ArrayList<String>();
	public static String BBS_DEFAULT;
	public static String BBS_HOME_DIR;
	public static boolean COMMUNITYBOARD_TELEPORT_ENABLED;
	public static int COMMUNITYBOARD_TELE_PICE;
	public static int COMMUNITYBOARD_SAVE_TELE_PICE;
	public static boolean COMMUNITYBOARD_TELEPORT_SIEGE_ENABLED;
	
	/** Wedding Options */
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_INTERVAL;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;
	public static boolean ON_WEDDING_GIVE_GIFT;
	public static boolean ANNOUNCE_WEDDING;
	
	/** Augmentations **/
	public static int AUGMENTATION_NG_SKILL_CHANCE; // Chance to get a skill while using a NoGrade Life Stone
	public static int AUGMENTATION_NG_GLOW_CHANCE; // Chance to get a Glow effect while using a NoGrade Life Stone(only if you get a skill)
	public static int AUGMENTATION_MID_SKILL_CHANCE; // Chance to get a skill while using a MidGrade Life Stone
	public static int AUGMENTATION_MID_GLOW_CHANCE; // Chance to get a Glow effect while using a MidGrade Life Stone(only if you get a skill)
	public static int AUGMENTATION_HIGH_SKILL_CHANCE; // Chance to get a skill while using a HighGrade Life Stone
	public static int AUGMENTATION_HIGH_GLOW_CHANCE; // Chance to get a Glow effect while using a HighGrade Life Stone
	public static int AUGMENTATION_TOP_SKILL_CHANCE; // Chance to get a skill while using a TopGrade Life Stone
	public static int AUGMENTATION_TOP_GLOW_CHANCE; // Chance to get a Glow effect while using a TopGrade Life Stone
	public static int AUGMENTATION_BASESTAT_CHANCE; // Chance to get a BaseStatModifier in the augmentation process
	public static int AUGMENTATION_ACC_SKILL_CHANCE;
	public static boolean DISABLE_ACCESSORY_AUGMENTATION;
	
	public static int FOLLOW_RANGE;
	
	public static boolean ALT_ENABLE_MULTI_PROFA;
	
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static boolean ALT_ITEM_AUCTION_CAN_REBID;
	public static boolean ALT_ITEM_AUCTION_START_ANNOUNCE;
	public static int ALT_ITEM_AUCTION_BID_ITEM_ID;
	public static long ALT_ITEM_AUCTION_MAX_BID;
	public static int ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS;
	
	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
	
	public static boolean ALT_ENABLE_BLOCK_CHECKER_EVENT;
	public static int ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static double ALT_RATE_COINS_REWARD_BLOCK_CHECKER;
	public static boolean ALT_HBCE_FAIR_PLAY;
	public static int ALT_PET_INVENTORY_LIMIT;
	public static int ALT_CLAN_LEVEL_CREATE;
	
	/** limits of stats **/
	public static TIntIntHashMap LIMIT_PATK;
	public static TIntIntHashMap LIMIT_MATK;
	public static TIntIntHashMap LIMIT_PDEF;
	public static TIntIntHashMap LIMIT_MDEF;
	public static TIntIntHashMap LIMIT_MATK_SPD;
	public static TIntIntHashMap LIMIT_PATK_SPD;
	public static TIntIntHashMap LIMIT_CRIT_DAM;
	public static TIntIntHashMap LIMIT_CRIT;
	public static TIntIntHashMap LIMIT_MCRIT;
	public static TIntIntHashMap LIMIT_ACCURACY;
	public static TIntIntHashMap LIMIT_EVASION;
	public static TIntIntHashMap LIMIT_MOVE;
	public static TIntIntHashMap LIMIT_REFLECT;
	public static TIntIntHashMap LIMIT_FAME;
	
	public static boolean ALT_ELEMENT_FORMULA;
	
	public static int SKILL_MASTERY_TRIGGER_CHANSE;
	
	public static double ALT_NPC_PATK_MODIFIER;
	public static double ALT_NPC_MATK_MODIFIER;
	public static double ALT_NPC_MAXHP_MODIFIER;
	public static double ALT_NPC_MAXMP_MODIFIER;
	
	public static int FESTIVAL_MIN_PARTY_SIZE;
	public static double FESTIVAL_RATE_PRICE;
	
	/** DimensionalRift Config **/
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY; // Time in ms the party has to wait until the mobs spawn
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME;
	public static int RIFT_AUTO_JUMPS_TIME_RAND;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	
	public static boolean ALLOW_TALK_WHILE_SITTING;
	
	public static boolean PARTY_LEADER_ONLY_CAN_INVITE;
	
	/** Разрешены ли клановые скилы? **/
	public static boolean ALLOW_CLANSKILLS;
	
	/** Разрешено ли изучение скилов трансформации и саб классов без наличия выполненного квеста */
	public static boolean ALLOW_LEARN_TRANS_SKILLS_WO_QUEST;
	
	/** Allow Manor system */
	public static boolean ALLOW_MANOR;
	
	/** Manor Refresh Starting time */
	public static int MANOR_REFRESH_TIME;
	
	/** Manor Refresh Min */
	public static int MANOR_REFRESH_MIN;
	
	/** Manor Next Period Approve Starting time */
	public static int MANOR_APPROVE_TIME;
	
	/** Manor Next Period Approve Min */
	public static int MANOR_APPROVE_MIN;
	
	/** Manor Maintenance Time */
	public static int MANOR_MAINTENANCE_PERIOD;
	
	public static boolean ENABLE_OLD_TVT;
	public static boolean ENABLE_OLD_CTF;
	
	public static boolean ENABLE_NEW_TVT;
	public static boolean ENABLE_NEW_CTF;
	
	public static double EVENT_CofferOfShadowsPriceRate;
	public static double EVENT_CofferOfShadowsRewardRate;
	
	public static double EVENT_APIL_FOOLS_DROP_CHANCE;
	
	/** Master Yogi event enchant config */
	public static int ENCHANT_CHANCE_MASTER_YOGI_STAFF;
	public static int ENCHANT_MAX_MASTER_YOGI_STAFF;
	public static int SAFE_ENCHANT_MASTER_YOGI_STAFF;
	
	public static boolean AllowCustomDropItems;
	public static String[] CUSTOM_DROP_ITEMS;
	public static boolean CDItemsAllowMinMaxPlayerLvl;
	public static int CDItemsMinPlayerLvl;
	public static int CDItemsMaxPlayerLvl;
	public static boolean CDItemsAllowMinMaxMobLvl;
	public static int CDItemsMinMobLvl;
	public static int CDItemsMaxMobLvl;
	public static boolean CDItemsAllowOnlyRbDrops;
	
	public static boolean ACTIVITY_REWARD_ENABLED;
	public static int ACTIVITY_REWARD_TIME;
	public static String[] ACTIVITY_REWARD_ITEMS;
	
	public static boolean ENABLE_GVG_EVENT;
	public static boolean EVENT_GvGDisableEffect;
	public static int GvG_POINTS_FOR_BOX;
	public static int GvG_POINTS_FOR_BOSS;
	public static int GvG_POINTS_FOR_KILL;
	public static int GvG_POINTS_FOR_DEATH;
	public static int GvG_EVENT_TIME;
	public static long GvG_BOSS_SPAWN_TIME;
	public static int GvG_FAME_REWARD;
	public static int GvG_REWARD;
	public static long GvG_REWARD_COUNT;
	public static int GvG_ADD_IF_WITHDRAW;
	public static int GvG_HOUR_START;
	public static int GvG_MINUTE_START;
	public static int GVG_MIN_LEVEL;
	public static int GVG_MAX_LEVEL;
	public static int GVG_MAX_GROUPS;
	public static int GVG_MIN_PARTY_MEMBERS;
	public static long GVG_TIME_TO_REGISTER;
	
	public static double EVENT_TFH_POLLEN_CHANCE;
	public static double EVENT_GLITTMEDAL_NORMAL_CHANCE;
	public static double EVENT_GLITTMEDAL_GLIT_CHANCE;
	public static double EVENT_L2DAY_LETTER_CHANCE;
	public static String[] L2_DAY_CUSTOM_DROP;
	public static double EVENT_CHANGE_OF_HEART_CHANCE;
	
	public static double EVENT_TRICK_OF_TRANS_CHANCE;
	
	public static double EVENT_MARCH8_DROP_CHANCE;
	public static double EVENT_MARCH8_PRICE_RATE;
	
	public static boolean EVENT_BOUNTY_HUNTERS_ENABLED;
	
	public static long EVENT_SAVING_SNOWMAN_LOTERY_PRICE;
	public static int EVENT_SAVING_SNOWMAN_REWARDER_CHANCE;
	
	public static boolean SERVICES_NO_TRADE_ONLY_OFFLINE;
	public static boolean SERVICES_NO_TRADE_BLOCK_ZONE;
	public static double SERVICES_TRADE_TAX;
	public static double SERVICES_OFFSHORE_TRADE_TAX;
	public static boolean SERVICES_OFFSHORE_NO_CASTLE_TAX;
	public static boolean SERVICES_TRADE_TAX_ONLY_OFFLINE;
	public static boolean SERVICES_TRADE_ONLY_FAR;
	public static int SERVICES_TRADE_RADIUS;
	public static int SERVICES_TRADE_MIN_LEVEL;
	
	public static boolean SERVICES_ENABLE_NO_CARRIER;
	public static int SERVICES_NO_CARRIER_TIME;
	
	public static boolean SERVICES_PK_PVP_KILL_ENABLE;
	public static int SERVICES_PVP_KILL_REWARD_ITEM;
	public static long SERVICES_PVP_KILL_REWARD_COUNT;
	public static int SERVICES_PK_KILL_REWARD_ITEM;
	public static long SERVICES_PK_KILL_REWARD_COUNT;
	public static boolean SERVICES_PK_PVP_TIE_IF_SAME_IP;
	
	public static boolean ALT_OPEN_CLOAK_SLOT;
	
	public static boolean ALT_SHOW_SERVER_TIME;
	
	/** Geodata config */
	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;
	public static String GEOFILES_PATTERN;
	public static boolean ALLOW_GEODATA;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static boolean ALLOW_KEYBOARD_MOVE;
	public static boolean COMPACT_GEO;
	public static int CLIENT_Z_SHIFT;
	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;
	public static boolean ALT_DAMAGE_INVIS;
	public static int REGION_EDGE_MAX_Z_DIFF;
	
	/** Geodata (Pathfind) config */
	public static int PATHFIND_BOOST;
	public static int PATHFIND_MAP_MUL;
	public static boolean PATHFIND_DIAGONAL;
	public static boolean PATH_CLEAN;
	public static int PATHFIND_MAX_Z_DIFF;
	public static long PATHFIND_MAX_TIME;
	public static String PATHFIND_BUFFERS;
	public static int GEODATA_SKILL_CHECK_TASK_INTERVAL;
	
	public static boolean DEBUG;
	
	/**
	 * GM Config
	 */
	public static boolean KARMA_DROP_GM;
	public static int INVENTORY_MAXIMUM_GM;
	public static boolean GM_LOGIN_INVUL;
	public static boolean GM_LOGIN_IMMORTAL;
	public static boolean GM_LOGIN_INVIS;
	public static boolean GM_LOGIN_SILENCE;
	public static boolean GM_LOGIN_TRADEOFF;
	public static boolean HIDE_GM_STATUS;
	public static boolean ANNOUNCE_GM_LOGIN;
	public static boolean SAVE_GM_EFFECTS; // Silence, gmspeed, etc...
	public static boolean GM_PM_COMMANDS;
	
	/* Item-Mall Configs */
	public static int GAME_POINT_ITEM_ID;
	
	public static int WEAR_DELAY;
	
	public static boolean GOODS_INVENTORY_ENABLED = false;
	public static boolean EX_NEW_PETITION_SYSTEM;
	public static boolean EX_JAPAN_MINIGAME;
	public static boolean EX_LECTURE_MARK;
	
	/* Top's Config */
	public static boolean L2_TOP_MANAGER_ENABLED;
	public static int L2_TOP_MANAGER_INTERVAL;
	public static String L2_TOP_WEB_ADDRESS;
	public static String L2_TOP_SMS_ADDRESS;
	public static String L2_TOP_SERVER_ADDRESS;
	public static int L2_TOP_SAVE_DAYS;
	public static int[] L2_TOP_REWARD;
	public static String L2_TOP_NAME_PREFIX;
	
	public static boolean MMO_TOP_MANAGER_ENABLED;
	public static int MMO_TOP_MANAGER_INTERVAL;
	public static String MMO_TOP_WEB_ADDRESS;
	public static String MMO_TOP_SERVER_ADDRESS;
	public static int MMO_TOP_SAVE_DAYS;
	public static int[] MMO_TOP_REWARD;
	
	public static boolean SMS_PAYMENT_MANAGER_ENABLED;
	public static String SMS_PAYMENT_WEB_ADDRESS;
	public static int SMS_PAYMENT_MANAGER_INTERVAL;
	public static int SMS_PAYMENT_SAVE_DAYS;
	public static String SMS_PAYMENT_SERVER_ADDRESS;
	public static int[] SMS_PAYMENT_REWARD;
	
	public static boolean AUTH_SERVER_GM_ONLY;
	public static boolean AUTH_SERVER_BRACKETS;
	public static boolean AUTH_SERVER_IS_PVP;
	public static int AUTH_SERVER_AGE_LIMIT;
	public static int AUTH_SERVER_SERVER_TYPE;
	
	/* Version Configs */
	public static String SERVER_VERSION;
	public static String SERVER_BUILD_DATE;
	
	/* Конфиг для ПА */
	public static int SERVICES_RATE_TYPE;
	public static int SERVICES_RATE_CREATE_PA;
	public static int[] SERVICES_RATE_BONUS_PRICE;
	public static int[] SERVICES_RATE_BONUS_ITEM;
	public static double[] SERVICES_RATE_BONUS_VALUE;
	public static int[] SERVICES_RATE_BONUS_DAYS;
	public static int ENCHANT_CHANCE_WEAPON_PA;
	public static int ENCHANT_CHANCE_ARMOR_PA;
	public static int ENCHANT_CHANCE_ACCESSORY_PA;
	public static int ENCHANT_CHANCE_WEAPON_BLESS_PA;
	public static int ENCHANT_CHANCE_ARMOR_BLESS_PA;
	public static int ENCHANT_CHANCE_ACCESSORY_BLESS_PA;
	public static int ENCHANT_CHANCE_CRYSTAL_WEAPON_PA;
	public static int ENCHANT_CHANCE_CRYSTAL_ARMOR_PA;
	public static int ENCHANT_CHANCE_CRYSTAL_ACCESSORY_PA;
	
	public static double SERVICES_BONUS_XP;
	public static double SERVICES_BONUS_SP;
	public static double SERVICES_BONUS_ADENA;
	public static double SERVICES_BONUS_ITEMS;
	public static double SERVICES_BONUS_SPOIL;
	
	public static long MAX_PLAYER_CONTRIBUTION;
	public static boolean AUTO_LOOT_PA;
	
	/* Configration of epic's */
	
	public static boolean ANTHARAS_DIABLE_CC_ENTER;
	public static int SPAWN_CUSTOM_ANTHARAS;
	public static int FIXINTERVALOFANTHARAS_HOUR;
	public static int RANDOM_TIME_OF_ANTHARAS;
	public static int ANTHARAS_MINIONS_NUMBER;
	
	public static int FIXINTERVALOFBAIUM_HOUR;
	public static int RANDOMINTERVALOFBAIUM;
	
	public static int FIXINTERVALOFBAYLORSPAWN_HOUR;
	public static int RANDOMINTERVALOFBAYLORSPAWN;
	
	public static int FIXINTERVALOFBELETHSPAWN_HOUR;
	public static int BOSS_BELETH_MIN_COUNT;
	
	public static int FIXINTERVALOFSAILRENSPAWN_HOUR;
	public static int RANDOMINTERVALOFSAILRENSPAWN;
	
	public static int FIXINTERVALOFVALAKAS;
	public static int RANDOM_TIME_OF_VALAKAS;
	public static boolean VALAKAS_DISABLE_CC_ENTER;
	public static int MAX_VALAKAS_MINIONS;
	
	public static int DV_RB_DESPAWN;
	
	/* Количество очков репутации необходимое для поднятия уровня клану. */
	public static int CLAN_LEVEL_6_COST;
	public static int CLAN_LEVEL_7_COST;
	public static int CLAN_LEVEL_8_COST;
	public static int CLAN_LEVEL_9_COST;
	public static int CLAN_LEVEL_10_COST;
	public static int CLAN_LEVEL_11_COST;
	
	/* Количество человек в клане необходимое для поднятия уровня клану. */
	public static int CLAN_LEVEL_6_REQUIREMEN;
	public static int CLAN_LEVEL_7_REQUIREMEN;
	public static int CLAN_LEVEL_8_REQUIREMEN;
	public static int CLAN_LEVEL_9_REQUIREMEN;
	public static int CLAN_LEVEL_10_REQUIREMEN;
	public static int CLAN_LEVEL_11_REQUIREMEN;
	
	public static int BLOOD_OATHS;
	public static int BLOOD_PLEDGES;
	public static int MIN_ACADEM_POINT;
	public static int MAX_ACADEM_POINT;
	
	public static int VITAMIN_PETS_FOOD_ID;
	
	public static boolean ZONE_PVP_COUNT;
	public static boolean SIEGE_PVP_COUNT;
	public static boolean EPIC_EXPERTISE_PENALTY;
	public static boolean EXPERTISE_PENALTY;
	public static boolean ALT_DISPEL_MUSIC;
	
	public static int ALT_MUSIC_LIMIT;
	public static int ALT_DEBUFF_LIMIT;
	public static int ALT_TRIGGER_LIMIT;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static TIntIntHashMap SKILL_DURATION_LIST;
	
	public static boolean COMMUNITYBOARD_BOARD_ALT_ENABLED;
	public static int COMMUNITYBOARD_BUFF_PICE_NG;
	public static int COMMUNITYBOARD_BUFF_PICE_D;
	public static int COMMUNITYBOARD_BUFF_PICE_C;
	public static int COMMUNITYBOARD_BUFF_PICE_B;
	public static int COMMUNITYBOARD_BUFF_PICE_A;
	public static int COMMUNITYBOARD_BUFF_PICE_S;
	public static int COMMUNITYBOARD_BUFF_PICE_S80;
	public static int COMMUNITYBOARD_BUFF_PICE_S84;
	public static int COMMUNITYBOARD_BUFF_PICE_NG_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_D_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_C_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_B_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_A_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_S_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_S80_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_S84_GR;
	public static int COMMUNITYBOARD_TELEPORT_PICE_NG;
	public static int INSTANCES_MAX_BOXES;
	public static int COMMUNITYBOARD_TELEPORT_PICE_D;
	public static int COMMUNITYBOARD_TELEPORT_PICE_C;
	public static int COMMUNITYBOARD_TELEPORT_PICE_B;
	public static int COMMUNITYBOARD_TELEPORT_PICE_A;
	public static int COMMUNITYBOARD_TELEPORT_PICE_S;
	public static int COMMUNITYBOARD_TELEPORT_PICE_S80;
	public static int COMMUNITYBOARD_TELEPORT_PICE_S84;
	
	public static double ALT_VITALITY_NEVIT_UP_POINT;
	public static double ALT_VITALITY_NEVIT_POINT;
	
	public static boolean SERVICES_LVL_ENABLED;
	public static int SERVICES_LVL_UP_MAX;
	public static int SERVICES_LVL_UP_ITEM;
	
	public static int SERVICES_LVL_79_85_PRICE;
	public static int SERVICES_LVL_1_85_PRICE;
	
	public static boolean ALLOW_INSTANCES_LEVEL_MANUAL;
	public static boolean ALLOW_INSTANCES_PARTY_MANUAL;
	public static int INSTANCES_LEVEL_MIN;
	public static int INSTANCES_LEVEL_MAX;
	public static int INSTANCES_PARTY_MIN;
	public static int INSTANCES_PARTY_MAX;
	
	// Items setting
	public static boolean CAN_BE_TRADED_NO_TARADEABLE;
	public static boolean CAN_BE_TRADED_NO_SELLABLE;
	public static boolean CAN_BE_TRADED_NO_STOREABLE;
	public static boolean CAN_BE_TRADED_SHADOW_ITEM;
	public static boolean CAN_BE_TRADED_HERO_WEAPON;
	public static boolean CAN_BE_WH_NO_TARADEABLE;
	public static boolean CAN_BE_CWH_NO_TARADEABLE;
	public static boolean CAN_BE_CWH_IS_AUGMENTED;
	public static boolean CAN_BE_WH_IS_AUGMENTED;
	public static boolean ALLOW_SOUL_SPIRIT_SHOT_INFINITELY;
	public static boolean ALLOW_ARROW_INFINITELY;
	
	public static boolean ALLOW_START_ITEMS;
	public static boolean BIND_NEWBIE_START_ITEMS_TO_CHAR;
	public static int[] START_ITEMS_MAGE;
	public static int[] START_ITEMS_MAGE_COUNT;
	public static int[] START_ITEMS_FITHER;
	public static int[] START_ITEMS_FITHER_COUNT;
	
	public static int[] START_ITEMS_MAGE_BIND_TO_CHAR;
	public static int[] START_ITEMS_MAGE_COUNT_BIND_TO_CHAR;
	public static int[] START_ITEMS_FITHER_BIND_TO_CHAR;
	public static int[] START_ITEMS_FITHER_COUNT_BIND_TO_CHAR;
	
	public static int HELLBOUND_LEVEL;
	public static boolean HELLBOUND_ENTER_NO_QUEST;
	
	public static boolean ALLOW_BSS_RAIDBOSS;
	public static int[] BOSSES_TO_NOT_SHOW;
	
	public static boolean ALLOW_COMMUNITY_CLAN_SKILLS_SELLER;
	public static int COMMUNITY_CLAN_SKILL_SELLER_ITEM;
	
	public static boolean ALLOW_CB_AUGMENTATION;
	public static int COMMUNITY_AUGMENTATION_MIN_LEVEL;
	public static boolean COMMUNITY_AUGMENTATION_ALLOW_JEWELRY;
	
	public static boolean COMMUNITYBOARD_ENCHANT_ENABLED;
	public static boolean ALLOW_BBS_ENCHANT_ELEMENTAR;
	public static boolean ALLOW_BBS_ENCHANT_ATT;
	public static int COMMUNITYBOARD_ENCHANT_ITEM;
	public static int COMMUNITYBOARD_MAX_ENCHANT;
	public static int[] COMMUNITYBOARD_ENCHANT_LVL;
	public static int[] COMMUNITYBOARD_ENCHANT_PRICE_WEAPON;
	public static int[] COMMUNITYBOARD_ENCHANT_PRICE_ARMOR;
	public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON;
	public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_WEAPON;
	public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR;
	public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_ARMOR;
	public static boolean COMMUNITYBOARD_ENCHANT_ATRIBUTE_PVP;
	
	public static boolean USE_ALT_ENCHANT_PA;
	public static ArrayList<Integer> ENCHANT_WEAPON_FIGHT_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_WEAPON_FIGHT_BLESSED_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_WEAPON_FIGHT_CRYSTAL_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_CRYSTAL_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_BLESSED_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_JEWELRY_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_JEWELRY_CRYSTAL_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_JEWELRY_BLESSED_PA = new ArrayList<Integer>();
	
	public static boolean EVENT_ENABLE_LAST_HERO;
	public static int EVENT_LastHeroItemID;
	public static double EVENT_LastHeroItemCOUNT;
	public static int EVENT_LastHeroTime;
	public static boolean EVENT_LastHeroRate;
	public static double EVENT_LastHeroItemCOUNTFinal;
	public static boolean EVENT_LastHeroRateFinal;
	public static int EVENT_LastHeroChanceToStart;
	
	public static int EVENT_TvTItemID;
	public static double EVENT_TvTItemCOUNT;
	public static int EVENT_TvTChanceToStart;
	
	public static boolean LOAD_CUSTOM_SPAWN;
	public static boolean SAVE_GM_SPAWN;
	
	public static boolean TMEnabled;
	public static int TMStartHour;
	public static int TMStartMin;
	public static int TMEventInterval;
	public static int TMMobLife;
	public static int BossLifeTime;
	public static int TMTime1;
	public static int TMTime2;
	public static int TMTime3;
	public static int TMTime4;
	public static int TMTime5;
	public static int TMTime6;
	public static int TMWave1;
	public static int TMWave2;
	public static int TMWave3;
	public static int TMWave4;
	public static int TMWave5;
	public static int TMWave6;
	public static int TMWave1Count;
	public static int TMWave2Count;
	public static int TMWave3Count;
	public static int TMWave4Count;
	public static int TMWave5Count;
	public static int TMWave6Count;
	public static int TMBoss;
	public static int[] TMItem;
	public static int[] TMItemCol;
	public static int[] TMItemColBoss;
	public static int[] TMItemChance;
	public static int[] TMItemChanceBoss;
	  
	public static void loadServerConfig()
	{
		ExProperties serverSettings = load(CONFIGURATION_FILE);
		
		GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
		GAME_SERVER_LOGIN_PORT = serverSettings.getProperty("LoginPort", 9014);
		GAME_SERVER_LOGIN_CRYPT = serverSettings.getProperty("LoginUseCrypt", true);
		
		AUTH_SERVER_AGE_LIMIT = serverSettings.getProperty("ServerAgeLimit", 0);
		AUTH_SERVER_GM_ONLY = serverSettings.getProperty("ServerGMOnly", false);
		AUTH_SERVER_BRACKETS = serverSettings.getProperty("ServerBrackets", false);
		AUTH_SERVER_IS_PVP = serverSettings.getProperty("PvPServer", false);
		for (String a : serverSettings.getProperty("ServerType", ArrayUtils.EMPTY_STRING_ARRAY))
		{
			if (a.trim().isEmpty())
				continue;
			
			ServerType t = ServerType.valueOf(a.toUpperCase());
			AUTH_SERVER_SERVER_TYPE |= t.getMask();
		}
		
		DISABLE_HWID_SUPPORT = serverSettings.getProperty("DisableHWIDSupport", false);
		
		SECOND_AUTH_ENABLED = serverSettings.getProperty("SAEnabled", false);
		SECOND_AUTH_BAN_ACC = serverSettings.getProperty("SABanAccEnabled", false);
		SECOND_AUTH_STRONG_PASS = serverSettings.getProperty("SAStrongPass", false);
		SECOND_AUTH_MAX_ATTEMPTS = serverSettings.getProperty("SAMaxAttemps", 5);
		SECOND_AUTH_BAN_TIME = serverSettings.getProperty("SABanTime", 480);
		SECOND_AUTH_REC_LINK = serverSettings.getProperty("SARecoveryLink", "http://www.my-domain.com/charPassRec.php");
		
		INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "*");
		EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", new String[]{"*"});
		EXTERNAL_LOGIN_HOSTNAME = serverSettings.getProperty("ExternalLoginHostname", new String[]{"*"});
		
		ADVIPSYSTEM = serverSettings.getProperty("AdvIPSystem", false);
		REQUEST_IDS = serverSettings.getProperty("RequestServerID", new int[]{0});
		REQUEST_ID = REQUEST_IDS[0];
		ACCEPT_ALTERNATE_ID = serverSettings.getProperty("AcceptAlternateID", true);
		
		PORTS_GAME = serverSettings.getProperty("GameserverPort", new int[]
		{
			7777
		});
		
		CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");
		CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		CLAN_TITLE_TEMPLATE = serverSettings.getProperty("ClanTitleTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f \\p{Punct}]{1,16}");
		ALLY_NAME_TEMPLATE = serverSettings.getProperty("AllyNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		
		PARALIZE_ON_RAID_DIFF = serverSettings.getProperty("ParalizeOnRaidLevelDiff", true);
		
		AUTODESTROY_ITEM_AFTER = serverSettings.getProperty("AutoDestroyDroppedItemAfter", 0);
		AUTODESTROY_PLAYER_ITEM_AFTER = serverSettings.getProperty("AutoDestroyPlayerDroppedItemAfter", 0);
		DELETE_DAYS = serverSettings.getProperty("DeleteCharAfterDays", 7);
		
		try
		{
			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
		}
		catch (IOException e)
		{
			_log.error("", e);
		}
		
		ALLOW_DISCARDITEM = serverSettings.getProperty("AllowDiscardItem", true);
		ALLOW_MAIL = serverSettings.getProperty("AllowMail", true);
		ALLOW_WAREHOUSE = serverSettings.getProperty("AllowWarehouse", true);
		
		ALLOW_CURSED_WEAPONS = serverSettings.getProperty("AllowCursedWeapons", false);
		DROP_CURSED_WEAPONS_ON_KICK = serverSettings.getProperty("DropCursedWeaponsOnKick", false);
		
		MIN_PROTOCOL_REVISION = serverSettings.getProperty("MinProtocolRevision", 267);
		MAX_PROTOCOL_REVISION = serverSettings.getProperty("MaxProtocolRevision", 271);
		
		AUTOSAVE = serverSettings.getProperty("Autosave", true);
		
		MAXIMUM_ONLINE_USERS = serverSettings.getProperty("MaximumOnlineUsers", 3000);
		ONLINE_PLUS = serverSettings.getProperty("OnlineUsersPlus", 1);
		
		ALLOW_ONLINE_PARSE = serverSettings.getProperty("AllowParsTotalOnline", false);
		FIRST_UPDATE = serverSettings.getProperty("FirstOnlineUpdate", 1);
		DELAY_UPDATE = serverSettings.getProperty("OnlineUpdate", 5);
		
		DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
		DATABASE_MAX_CONNECTIONS = serverSettings.getProperty("MaximumDbConnections", 10);
		DATABASE_MAX_IDLE_TIMEOUT = serverSettings.getProperty("MaxIdleConnectionTimeout", 600);
		DATABASE_IDLE_TEST_PERIOD = serverSettings.getProperty("IdleConnectionTestPeriod", 60);
		
		DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
		DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
		DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
		
		LOGINSERVER_DB_NAME = serverSettings.getProperty("LoginServerDBName", "l2jdb");
		
		USER_INFO_INTERVAL = serverSettings.getProperty("UserInfoInterval", 100L);
		BROADCAST_STATS_INTERVAL = serverSettings.getProperty("BroadcastStatsInterval", true);
		BROADCAST_CHAR_INFO_INTERVAL = serverSettings.getProperty("BroadcastCharInfoInterval", 100L);
		
		EFFECT_TASK_MANAGER_COUNT = serverSettings.getProperty("EffectTaskManagers", 2);
		
		SCHEDULED_THREAD_POOL_SIZE = serverSettings.getProperty("ScheduledThreadPoolSize", NCPUS * 4);
		EXECUTOR_THREAD_POOL_SIZE = serverSettings.getProperty("ExecutorThreadPoolSize", NCPUS * 2);
		
		ENABLE_RUNNABLE_STATS = serverSettings.getProperty("EnableRunnableStats", false);
		
		THREAD_P_MOVE = serverSettings.getProperty("ThreadPoolSizeMove", 25);
		THREAD_P_PATHFIND = serverSettings.getProperty("ThreadPoolSizePathfind", 10);
		NPC_AI_MAX_THREAD = serverSettings.getProperty("NpcAiMaxThread", 10);
		PLAYER_AI_MAX_THREAD = serverSettings.getProperty("PlayerAiMaxThread", 20);
		
		NEW_THREAD_FOR_AI = serverSettings.getProperty("IgnoreUsageOfAiTaskManager", false);
		
		SELECTOR_CONFIG.SLEEP_TIME = serverSettings.getProperty("SelectorSleepTime", 10L);
		SELECTOR_CONFIG.INTEREST_DELAY = serverSettings.getProperty("InterestDelay", 30L);
		SELECTOR_CONFIG.MAX_SEND_PER_PASS = serverSettings.getProperty("MaxSendPerPass", 32);
		SELECTOR_CONFIG.READ_BUFFER_SIZE = serverSettings.getProperty("ReadBufferSize", 65536);
		SELECTOR_CONFIG.WRITE_BUFFER_SIZE = serverSettings.getProperty("WriteBufferSize", 131072);
		SELECTOR_CONFIG.HELPER_BUFFER_COUNT = serverSettings.getProperty("BufferPoolSize", 64);
		
		USE_CLIENT_LANG = serverSettings.getProperty("UseClientLang", false);
		DEFAULT_LANG = Language.valueOf(serverSettings.getProperty("DefaultLang", "ENGLISH"));
		
		RESTART_AT_TIME = serverSettings.getProperty("AutoRestartAt", "0 5 * * *");
		SHIFT_BY = serverSettings.getProperty("HShift", 12);
		SHIFT_BY_Z = serverSettings.getProperty("VShift", 11);
		MAP_MIN_Z = serverSettings.getProperty("MapMinZ", -32768);
		MAP_MAX_Z = serverSettings.getProperty("MapMaxZ", 32767);
		
		MOVE_PACKET_DELAY = serverSettings.getProperty("MovePacketDelay", 100);
		ATTACK_PACKET_DELAY = serverSettings.getProperty("AttackPacketDelay", 200);
		ATTACK_END_DELAY = serverSettings.getProperty("AttackEndDelay", 50);
		
		MAX_REFLECTIONS_COUNT = serverSettings.getProperty("MaxReflectionsCount", 300);
		
		WEAR_DELAY = serverSettings.getProperty("WearDelay", 5);
		
		HTM_CACHE_MODE = serverSettings.getProperty("HtmCacheMode", HtmCache.LAZY);
		
		ALLOW_QUETS_ADDONS_CONFIG = serverSettings.getProperty("AllowQuestAddons", false);
		
		ALLOW_IP_LOCK = serverSettings.getProperty("AllowLockIP", false);
		ALLOW_HWID_LOCK = serverSettings.getProperty("AllowLockHwid", false);
		HWID_LOCK_MASK = serverSettings.getProperty("HwidLockMask", 10);
		
		RRD_ENABLED = serverSettings.getProperty("UseRRD", true);
		RRD_EXTENDED = serverSettings.getProperty("UseExtendedRRD", false);
		RRD_PATH = serverSettings.getProperty("RRDPath", "./serverstats/");
		RRD_GRAPH_PATH = serverSettings.getProperty("GraphPath", "./serverstats/");
		RRD_UPDATE_TIME = serverSettings.getProperty("UpdateDelay", 30);
		RRD_GRAPH_HEIGHT = serverSettings.getProperty("GraphHeight", 378);
		RRD_GRAPH_WIDTH = serverSettings.getProperty("GraphWidth", 580);
		RRD_LINE_WIDTH = serverSettings.getProperty("LineWidth", 1);
		RRD_AREA_COLOR = serverSettings.getProperty("GraphAreaColor", "ORANGE");
		RRD_LINE_COLOR = serverSettings.getProperty("GraphLineColor", "RED");
		
		MIN_NPC_ANIMATION = serverSettings.getProperty("MinNPCAnimation", 5);
		MAX_NPC_ANIMATION = serverSettings.getProperty("MaxNPCAnimation", 90);
		SERVER_SIDE_NPC_NAME = serverSettings.getProperty("ServerSideNpcName", false);
		SERVER_SIDE_NPC_TITLE = serverSettings.getProperty("ServerSideNpcTitle", false);
		SERVER_SIDE_NPC_TITLE_ETC = serverSettings.getProperty("ServerSideNpcTitleEtc", false);
		NPC_ALLOW_HIT = serverSettings.getProperty("AllowHitOnNpcs", true);
		LOG_ACCOUNT_INFO = serverSettings.getProperty("LogAccountInfo", true);
		
		MAIL_USER = serverSettings.getProperty("MailUser", "");
		MAIL_PASS = serverSettings.getProperty("MailPass", "");
		MAIL_SUBJECT = serverSettings.getProperty("MailSubject", "");
		MAIL_MESSAGE = serverSettings.getProperty("MailMessage", "");
		
		ENABLE_HWID_CHECKER = serverSettings.getProperty("EnableSecondaryHwidCheck", false);
		
	}
	
	public static int DONATION_REWARD_ITEM_ID;
	public static int DONATION_REWARD_MULTIPLIER_PER_EURO;
	public static Map<Integer, Integer> DONATION_REWARD_BONUSES = new LinkedHashMap<>();

	public static void loadDonateRewardSettings()
	{
		ExProperties donate = load(DONATE_REWARD_CONFIG_FILE);

		DONATION_REWARD_ITEM_ID = donate.getProperty("DonationRewardItemId", 57);
		DONATION_REWARD_MULTIPLIER_PER_EURO = donate.getProperty("DonationMultiplierPerEuro", 1);
		final String donationBonus = donate.getProperty("DonationBonusRewards", "300,35;200,25;100,20;25,15;10,10;0,1");
		for (String bonus : donationBonus.split(";"))
		{
			final String[] split = bonus.split(",");
			DONATION_REWARD_BONUSES.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		}
	}
	
	public static void loadChatConfig()
	{
		ExProperties chatSettings = load(CHAT_FILE);
		
		GLOBAL_SHOUT = chatSettings.getProperty("GlobalShoutChat", false);
		ONLINE_TIME_SHOUT = chatSettings.getProperty("OnlineTimeForShoutChat", 0);
		PVP_COUNT_SHOUT = chatSettings.getProperty("PvPCountForShoutChat", 0);
		LEVEL_FOR_SHOUT = chatSettings.getProperty("LevelForShoutChat", 0);
		
		GLOBAL_TRADE_CHAT = chatSettings.getProperty("GlobalTradeChat", false);
		PVP_COUNT_TRADE = chatSettings.getProperty("PvPCountForTradeChat", 0);
		PVP_COUNT_TRADE = chatSettings.getProperty("OnlineTimeForTradeChat", 0);
		LEVEL_FOR_TRADE = chatSettings.getProperty("LevelForTradeChat", 0);
		
		CHAT_RANGE = chatSettings.getProperty("ChatRange", 1250);
		SHOUT_OFFSET = chatSettings.getProperty("ShoutOffset", 0);
		
		CHAT_SHOUT_TIME_DELAY = chatSettings.getProperty("ShoutChatTimeDelay", 5);
		CHAT_TRADE_TIME_DELAY = chatSettings.getProperty("TradeChatTimeDelay", 5);
		CHAT_HERO_TIME_DELAY = chatSettings.getProperty("HeroChatTimeDelay", 10);
		
		USE_TRADE_WORDS_ON_GLOBAL_CHAT = chatSettings.getProperty("GlobalChatTradeWords", false);
		
		TRADE_WORDS = new GArray<String>();
		
		String T_WORLD = chatSettings.getProperty("TradeWords", "продаю,проgаю,пр0даю,продам,проgам,пр0дам,покупаю,куплю,кyплю,обменяю,выменяю,ВТТ,ВТС,ВТБ,WTB,WTT,WTS");
		String[] T_WORLDS = T_WORLD.split(",", -1);
		for (String w : T_WORLDS)
			TRADE_WORDS.add(w);
		
		_log.info("Trade: Loaded " + TRADE_WORDS.size() + " trade words.");
		
		TRADE_CHATS_REPLACE_FROM_ALL = chatSettings.getProperty("TradeChatsReplaceFromAll", false);
		TRADE_CHATS_REPLACE_FROM_SHOUT = chatSettings.getProperty("TradeChatsReplaceFromShout", false);
		TRADE_CHATS_REPLACE_FROM_TRADE = chatSettings.getProperty("TradeChatsReplaceFromTrade", false);
		
		LOG_CHAT = chatSettings.getProperty("LogChat", false);
		CHAT_MESSAGE_MAX_LEN = chatSettings.getProperty("ChatMessageLimit", 1000);
		ABUSEWORD_BANCHAT = chatSettings.getProperty("ABUSEWORD_BANCHAT", false);
		int counter = 0;
		for (int id : chatSettings.getProperty("ABUSEWORD_BAN_CHANNEL", new int[]
		{
			0
		}))
		{
			BAN_CHANNEL_LIST[counter] = id;
			counter++;
		}
		ABUSEWORD_REPLACE = chatSettings.getProperty("ABUSEWORD_REPLACE", false);
		ABUSEWORD_REPLACE_STRING = chatSettings.getProperty("ABUSEWORD_REPLACE_STRING", "[censored]");
		BANCHAT_ANNOUNCE = chatSettings.getProperty("BANCHAT_ANNOUNCE", true);
		BANCHAT_ANNOUNCE_FOR_ALL_WORLD = chatSettings.getProperty("BANCHAT_ANNOUNCE_FOR_ALL_WORLD", true);
		BANCHAT_ANNOUNCE_NICK = chatSettings.getProperty("BANCHAT_ANNOUNCE_NICK", true);
		ABUSEWORD_BANTIME = chatSettings.getProperty("ABUSEWORD_UNBAN_TIMER", 30);
		CHATFILTER_MIN_LEVEL = chatSettings.getProperty("ChatFilterMinLevel", 0);
		counter = 0;
		for (int id : chatSettings.getProperty("ChatFilterChannels", new int[]
		{
			1,
			8
		}))
		{
			CHATFILTER_CHANNELS[counter] = id;
			counter++;
		}
		CHATFILTER_WORK_TYPE = chatSettings.getProperty("ChatFilterWorkType", 1);
	}
	
	public static void loadTelnetConfig()
	{
		ExProperties telnetSettings = load(TELNET_CONFIGURATION_FILE);
		
		IS_TELNET_ENABLED = telnetSettings.getProperty("EnableTelnet", false);
		TELNET_DEFAULT_ENCODING = telnetSettings.getProperty("TelnetEncoding", "UTF-8");
		TELNET_PORT = telnetSettings.getProperty("Port", 7000);
		TELNET_HOSTNAME = telnetSettings.getProperty("BindAddress", "127.0.0.1");
		TELNET_PASSWORD = telnetSettings.getProperty("Password", "");
	}
	
	public static void loadWeddingConfig()
	{
		ExProperties weddingSettings = load(WEDDING_FILE);
		
		ALLOW_WEDDING = weddingSettings.getProperty("AllowWedding", false);
		WEDDING_PRICE = weddingSettings.getProperty("WeddingPrice", 500000);
		WEDDING_PUNISH_INFIDELITY = weddingSettings.getProperty("WeddingPunishInfidelity", true);
		WEDDING_TELEPORT = weddingSettings.getProperty("WeddingTeleport", true);
		WEDDING_TELEPORT_PRICE = weddingSettings.getProperty("WeddingTeleportPrice", 500000);
		WEDDING_TELEPORT_INTERVAL = weddingSettings.getProperty("WeddingTeleportInterval", 120);
		WEDDING_SAMESEX = weddingSettings.getProperty("WeddingAllowSameSex", true);
		WEDDING_FORMALWEAR = weddingSettings.getProperty("WeddingFormalWear", true);
		WEDDING_DIVORCE_COSTS = weddingSettings.getProperty("WeddingDivorceCosts", 20);
		ON_WEDDING_GIVE_GIFT = weddingSettings.getProperty("GiveItemsOnWedding", true);
		ANNOUNCE_WEDDING = weddingSettings.getProperty("AnnounceWedding", false);
	}
	
	public static void loadResidenceConfig()
	{
		ExProperties residenceSettings = load(RESIDENCE_CONFIG_FILE);
		
		CH_BID_GRADE1_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanLevel", 2);
		CH_BID_GRADE1_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembers", 1);
		CH_BID_GRADE1_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE2_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanLevel", 2);
		CH_BID_GRADE2_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembers", 1);
		CH_BID_GRADE2_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE3_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanLevel", 2);
		CH_BID_GRADE3_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembers", 1);
		CH_BID_GRADE3_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembersAvgLevel", 1);
		
		CLAN_HALL_AUCTION_LENGTH = residenceSettings.getProperty("ClanHallAuctionLength", 7);
		
		CLANHALL_LEASE_MULTIPLIER = residenceSettings.getProperty("ClanHallLeaseMultiplier", 1.);
		
		RESIDENCE_LEASE_FUNC_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseFuncMultiplier", 1.);
		RESIDENCE_LEASE_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseMultiplier", 1.);
		
		CASTLE_GENERATE_TIME_ALTERNATIVE = residenceSettings.getProperty("CastleGenerateAlternativeTime", false);
		CASTLE_GENERATE_TIME_LOW = residenceSettings.getProperty("CastleGenerateTimeLow", 46800000);
		CASTLE_GENERATE_TIME_HIGH = residenceSettings.getProperty("CastleGenerateTimeHigh", 61200000);
		
		PERIOD_CASTLE_SIEGE = residenceSettings.getProperty("CastleSiegeIntervalWeeks", 2);
		
		CASTLE_SELECT_HOURS = residenceSettings.getProperty("CastleSelectHours", new int[]
		{
			16,
			20
		});
		int[] tempCastleValidatonTime = residenceSettings.getProperty("CastleValidationDate", new int[]
		{
			2,
			4,
			2003
		});
		CASTLE_VALIDATION_DATE = Calendar.getInstance();
		CASTLE_VALIDATION_DATE.set(Calendar.DAY_OF_MONTH, tempCastleValidatonTime[0]);
		CASTLE_VALIDATION_DATE.set(Calendar.MONTH, tempCastleValidatonTime[1] - 1);
		CASTLE_VALIDATION_DATE.set(Calendar.YEAR, tempCastleValidatonTime[2]);
		CASTLE_VALIDATION_DATE.set(Calendar.HOUR_OF_DAY, 0);
		CASTLE_VALIDATION_DATE.set(Calendar.MINUTE, 0);
		CASTLE_VALIDATION_DATE.set(Calendar.SECOND, 0);
		CASTLE_VALIDATION_DATE.set(Calendar.MILLISECOND, 0);
		
		DOMINION_INTERVAL_WEEKS = residenceSettings.getProperty("DominionIntervalWeeks", 2);
		
		TW_SELECT_HOURS = residenceSettings.getProperty("TwSelectHours", 20);
		int[] tempTwValidatonTime = residenceSettings.getProperty("TwValidationDate", new int[]
		{
			2,
			4,
			2003
		});
		TW_VALIDATION_DATE = Calendar.getInstance();
		TW_VALIDATION_DATE.set(Calendar.DAY_OF_MONTH, tempTwValidatonTime[0]);
		TW_VALIDATION_DATE.set(Calendar.MONTH, tempTwValidatonTime[1] - 1);
		TW_VALIDATION_DATE.set(Calendar.YEAR, tempTwValidatonTime[2]);
		TW_VALIDATION_DATE.set(Calendar.HOUR_OF_DAY, 0);
		TW_VALIDATION_DATE.set(Calendar.MINUTE, 0);
		TW_VALIDATION_DATE.set(Calendar.SECOND, 0);
		TW_VALIDATION_DATE.set(Calendar.MILLISECOND, 0);
		
		RETURN_WARDS_WHEN_TW_STARTS = residenceSettings.getProperty("ReturnWardsWhenTWStarts", false);
		PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE = residenceSettings.getProperty("PlayerWithWardCanBeKilledInPeaceZone", false);
		INTERVAL_FLAG_DROP = residenceSettings.getProperty("MinutesUntillFlagDissapearIfOut", 5);
		
		DOMINION_BADGES_MOD_MULTI = residenceSettings.getProperty("BadgesRewardMultiplier", 1.);
		
		FAME_REWARD_FORTRESS = residenceSettings.getProperty("FameRewardFortress", 31);
		FAME_REWARD_CASTLE = residenceSettings.getProperty("FameRewardCastle", 125);
		
		FORTRESS_REMOVE_FLAG_ON_LEAVE_ZONE = residenceSettings.getProperty("FortFlagReturnOnLeaveZone", false);
		
		DOMINION_REMOVE_FLAG_ON_LEAVE_ZONE = residenceSettings.getProperty("ReturnFlagOnSiegeZoneLeave", false);
		
		RATE_SIEGE_FAME_MIN = residenceSettings.getProperty("RateSiegeFameMin", 10);
		RATE_SIEGE_FAME_MAX = residenceSettings.getProperty("RateSiegeFameMax", 20);
		
		RATE_DOMINION_SIEGE_FAME_MIN = residenceSettings.getProperty("RateDominionSiegeFameMin", 10);
		RATE_DOMINION_SIEGE_FAME_MAX = residenceSettings.getProperty("RateDominionSiegeFameMax", 20);
		
		ALT_NOT_ALLOW_TW_WARDS_IN_CLANHALLS = residenceSettings.getProperty("NotAllowTWWardsInClanHalls", false);
		
		MAX_SIEGE_CLANS = residenceSettings.getProperty("MaxSiegeClans", 20);
		
		CLEAN_CLAN_HALLS_ON_TIME = residenceSettings.getProperty("CleanClanHalls", false);
		MIN_PLAYERS_IN_CLAN_TO_KEEP_CH = residenceSettings.getProperty("MinActiveMemberstoNOTDELETE", 11);
		DAYS_TO_CHECK_FOR_CH_DELETE = residenceSettings.getProperty("DaysToCheckForClanHallDelete", 7);
	}
	
	public static void loadItemsUseConfig()
	{
		ExProperties itemsUseSettings = load(ITEM_USE_FILE);
		
		ITEM_USE_LIST_ID = itemsUseSettings.getProperty("ItemUseListId", new int[]
		{
			725,
			726,
			727,
			728
		});
		ITEM_USE_IS_COMBAT_FLAG = itemsUseSettings.getProperty("ItemUseIsCombatFlag", true);
		ITEM_USE_IS_ATTACK = itemsUseSettings.getProperty("ItemUseIsAttack", true);
		ITEM_USE_IS_EVENTS = itemsUseSettings.getProperty("ItemUseIsEvents", true);
	}
	
	public static void loadFightClubSettings()
	{
		ExProperties eventFightClubSettings = load(EVENT_FIGHT_CLUB_FILE);
		
		FIGHT_CLUB_ENABLED = eventFightClubSettings.getProperty("FightClubEnabled", false);
		MINIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MinimumLevel", 1);
		MAXIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MaximumLevel", 85);
		MAXIMUM_LEVEL_DIFFERENCE = eventFightClubSettings.getProperty("MaximumLevelDifference", 10);
		ALLOWED_RATE_ITEMS = eventFightClubSettings.getProperty("AllowedItems", "").trim().replaceAll(" ", "").split(",");
		PLAYERS_PER_PAGE = eventFightClubSettings.getProperty("RatesOnPage", 10);
		ARENA_TELEPORT_DELAY = eventFightClubSettings.getProperty("ArenaTeleportDelay", 5);
		CANCEL_BUFF_BEFORE_FIGHT = eventFightClubSettings.getProperty("CancelBuffs", true);
		UNSUMMON_PETS = eventFightClubSettings.getProperty("UnsummonPets", true);
		UNSUMMON_SUMMONS = eventFightClubSettings.getProperty("UnsummonSummons", true);
		REMOVE_CLAN_SKILLS = eventFightClubSettings.getProperty("RemoveClanSkills", false);
		REMOVE_HERO_SKILLS = eventFightClubSettings.getProperty("RemoveHeroSkills", false);
		TIME_TO_PREPARATION = eventFightClubSettings.getProperty("TimeToPreparation", 10);
		FIGHT_TIME = eventFightClubSettings.getProperty("TimeToDraw", 300);
		ALLOW_DRAW = eventFightClubSettings.getProperty("AllowDraw", true);
		TIME_TELEPORT_BACK = eventFightClubSettings.getProperty("TimeToBack", 10);
		FIGHT_CLUB_ANNOUNCE_RATE = eventFightClubSettings.getProperty("AnnounceRate", false);
		FIGHT_CLUB_ANNOUNCE_RATE_TO_SCREEN = eventFightClubSettings.getProperty("AnnounceRateToAllScreen", false);
		FIGHT_CLUB_ANNOUNCE_START_TO_SCREEN = eventFightClubSettings.getProperty("AnnounceStartBatleToAllScreen", false);
	}
	
	public static void loadRatesConfig()
	{
		ExProperties ratesSettings = load(RATES_FILE);
		
		RATE_XP = ratesSettings.getProperty("RateXp", 1.);
		RATE_SP = ratesSettings.getProperty("RateSp", 1.);
		
		String[] propertySplit = ratesSettings.getProperty("RateExpPerLevel", "").split(";");
		EXP_RATE_BY_LEVEL = new TIntIntHashMap(propertySplit.length);
		for (String ps : propertySplit)
		{
			if (ps.isEmpty())
				continue;
			
			String[] skillSplit = ps.split(",");
			if (skillSplit.length != 2)
			{
				_log.warn(Strings.concat("[RateExpPerLevel]: invalid config property -> AdenaDropRateByLevel \"", ps, "\""));
			}
			else
			{
				try
				{
					EXP_RATE_BY_LEVEL.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!ps.isEmpty())
					{
						_log.warn(Strings.concat("[RateExpPerLevel]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
					}
				}
			}
		}
		
		propertySplit = ratesSettings.getProperty("RateSpPerLevel", "").split(";");
		SP_RATE_BY_LEVEL = new TIntIntHashMap(propertySplit.length);
		for (String ps : propertySplit)
		{
			if (ps.isEmpty())
				continue;
			
			String[] skillSplit = ps.split(",");
			if (skillSplit.length != 2)
			{
				_log.warn(Strings.concat("[RateSpPerLevel]: invalid config property -> AdenaDropRateByLevel \"", ps, "\""));
			}
			else
			{
				try
				{
					SP_RATE_BY_LEVEL.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!ps.isEmpty())
					{
						_log.warn(Strings.concat("[RateSpPerLevel]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
					}
				}
			}
		}
		
		RATE_QUESTS_REWARD = ratesSettings.getProperty("RateQuestsReward", 1.);
		RATE_QUESTS_REWARD_EXPSP = ratesSettings.getProperty("RateQuestsRewardExpSp", 1.);
		QUEST_REWARD_ADENA = ratesSettings.getProperty("QuestRewardAdena", 1.);
		RATE_QUESTS_DROP = ratesSettings.getProperty("RateQuestsDrop", 1.);
		RATE_DROP_CHAMPION = ratesSettings.getProperty("RateDropChampion", 1.);
		RATE_CLAN_REP_SCORE = ratesSettings.getProperty("RateClanRepScore", 1.);
		RATE_CLAN_REP_SCORE_MAX_AFFECTED = ratesSettings.getProperty("RateClanRepScoreMaxAffected", 2);
		MAX_CLAN_REPUTATIONS_POINTS = ratesSettings.getProperty("MaxClanReputationPoints", 2147483647);
		RATE_DROP_ADENA = ratesSettings.getProperty("RateDropAdena", 1.);
		RATE_DROP_AA_ADENA = ratesSettings.getProperty("RateDropAncientAdena", 1.);
		RATE_CHAMPION_DROP_ADENA = ratesSettings.getProperty("RateChampionDropAdena", 1.);
		RATE_DROP_SPOIL_CHAMPION = ratesSettings.getProperty("RateSpoilChampion", 1.);
		RATE_DROP_ITEMS = ratesSettings.getProperty("RateDropItems", 1.);
		RATE_CHANCE_GROUP_DROP_ITEMS = ratesSettings.getProperty("RateChanceGroupDropItems", 1.);
		RATE_CHANCE_DROP_ITEMS = ratesSettings.getProperty("RateChanceDropItems", 1.);
		RATE_CHANCE_DROP_HERBS = ratesSettings.getProperty("RateChanceDropHerbs", 1.);
		RATE_CHANCE_SPOIL = ratesSettings.getProperty("RateChanceSpoil", 1.);
		RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY = ratesSettings.getProperty("RateChanceSpoilWAA", 1.);
		RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY = ratesSettings.getProperty("RateChanceDropWAA", 1.);
		RATE_CHANCE_DROP_EPOLET = ratesSettings.getProperty("RateChanceDropEpolets", 1.);
		NO_RATE_ENCHANT_SCROLL = ratesSettings.getProperty("NoRateEnchantScroll", true);
		CHAMPION_DROP_ONLY_ADENA = ratesSettings.getProperty("ChampionDropOnlyAdena", false);
		RATE_ENCHANT_SCROLL = ratesSettings.getProperty("RateDropEnchantScroll", 1.);
		NO_RATE_HERBS = ratesSettings.getProperty("NoRateHerbs", true);
		RATE_DROP_HERBS = ratesSettings.getProperty("RateDropHerbs", 1.);
		NO_RATE_ATT = ratesSettings.getProperty("NoRateAtt", true);
		RATE_DROP_ATT = ratesSettings.getProperty("RateDropAtt", 1.);
		NO_RATE_LIFE_STONE = ratesSettings.getProperty("NoRateLifeStone", true);
		NO_RATE_CODEX_BOOK = ratesSettings.getProperty("NoRateCodex", true);
		NO_RATE_FORGOTTEN_SCROLL = ratesSettings.getProperty("NoRateForgottenScroll", true);
		RATE_DROP_LIFE_STONE = ratesSettings.getProperty("RateDropLifeStone", 1.);
		NO_RATE_KEY_MATERIAL = ratesSettings.getProperty("NoRateKeyMaterial", true);
		RATE_DROP_KEY_MATERIAL = ratesSettings.getProperty("RateDropKeyMaterial", 1.);
		NO_RATE_RECIPES = ratesSettings.getProperty("NoRateRecipes", true);
		RATE_DROP_RECIPES = ratesSettings.getProperty("RateDropRecipes", 1.);
		RATE_DROP_COMMON_ITEMS = ratesSettings.getProperty("RateDropCommonItems", 1.);
		NO_RATE_RAIDBOSS = ratesSettings.getProperty("NoRateRaidBoss", false);
		RATE_DROP_RAIDBOSS = ratesSettings.getProperty("RateRaidBoss", 1.);
		RATE_DROP_SPOIL = ratesSettings.getProperty("RateDropSpoil", 1.);
		NO_RATE_ITEMS = ratesSettings.getProperty("NoRateItemIds", new int[]
		{
			6660,
			6662,
			6661,
			6659,
			6656,
			6658,
			8191,
			6657,
			10170,
			10314,
			16025,
			16026
		});
		NO_RATE_EQUIPMENT = ratesSettings.getProperty("NoRateEquipment", true);
		NO_RATE_SIEGE_GUARD = ratesSettings.getProperty("NoRateSiegeGuard", false);
		RATE_DROP_SIEGE_GUARD = ratesSettings.getProperty("RateSiegeGuard", 1.);
		RATE_MANOR = ratesSettings.getProperty("RateManor", 1.);
		RATE_FISH_DROP_COUNT = ratesSettings.getProperty("RateFishDropCount", 1.);
		RATE_PARTY_MIN = ratesSettings.getProperty("RatePartyMin", false);
		RATE_HELLBOUND_CONFIDENCE = ratesSettings.getProperty("RateHellboundConfidence", 1.);
		
		RATE_MOB_SPAWN = ratesSettings.getProperty("RateMobSpawn", 1);
		RATE_MOB_SPAWN_MIN_LEVEL = ratesSettings.getProperty("RateMobMinLevel", 1);
		RATE_MOB_SPAWN_MAX_LEVEL = ratesSettings.getProperty("RateMobMaxLevel", 100);
		
		String[] rateDropItemsById = ratesSettings.getProperty("RateDropItemsById", "").split(";");
		RATE_DROP_ITEMS_ID = new HashMap<>(rateDropItemsById.length);
		if (!rateDropItemsById[0].isEmpty())
		{
			for (String item : rateDropItemsById)
			{
				String[] itemSplit = item.split(",");
				if (itemSplit.length != 2)
				{
					_log.warn(Strings.concat("Config.load(): invalid config property -> RateDropItemsById \"", item, "\""));
				}
				else
				{
					try
					{
						RATE_DROP_ITEMS_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!item.isEmpty())
						{
							_log.warn(Strings.concat("Config.load(): invalid config property -> RateDropItemsById \"", item, "\""));
						}
					}
				}
			}
		}
		
		String[] rateSpoilItemsById = ratesSettings.getProperty("RateDropSpoilById", "").split(";");
		RATE_DROP_SPOIL_ITEMS_ID = new HashMap<>(rateSpoilItemsById.length);
		if (!rateSpoilItemsById[0].isEmpty())
		{
			for (String item : rateSpoilItemsById)
			{
				String[] itemSplit = item.split(",");
				if (itemSplit.length != 2)
				{
					_log.warn(Strings.concat("Config.load(): invalid config property -> RateDropSpoilById \"", item, "\""));
				}
				else
				{
					try
					{
						RATE_DROP_SPOIL_ITEMS_ID.put(Integer.parseInt(itemSplit[0]), Float.parseFloat(itemSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!item.isEmpty())
						{
							_log.warn(Strings.concat("Config.load(): invalid config property -> RateDropSpoilById \"", item, "\""));
						}
					}
				}
			}
		}
		
		String[] rateChanceDropItemsById = ratesSettings.getProperty("RateChanceDropItemsById", "").split(";");
		RATE_CHANCE_DROP_ITEMS_ID = new HashMap<>(rateChanceDropItemsById.length);
		if (!rateChanceDropItemsById[0].isEmpty())
		{
			for (String item : rateChanceDropItemsById)
			{
				String[] itemSplit = item.split(",");
				if (itemSplit.length != 2)
				{
					_log.warn(Strings.concat("Config.load(): invalid config property -> RateChanceDropItemsById \"", item, "\""));
				}
				else
				{
					try
					{
						RATE_CHANCE_DROP_ITEMS_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!item.isEmpty())
						{
							_log.warn(Strings.concat("Config.load(): invalid config property -> RateChanceDropItemsById \"", item, "\""));
						}
					}
				}
			}
		}
		
		String[] rateChanceSpoilItemsById = ratesSettings.getProperty("RateChanceDropSpoilById", "").split(";");
		RATE_CHANCE_DROP_SPOIL_ITEMS_ID = new HashMap<>(rateChanceSpoilItemsById.length);
		if (!rateChanceSpoilItemsById[0].isEmpty())
		{
			for (String item : rateChanceSpoilItemsById)
			{
				String[] itemSplit = item.split(",");
				if (itemSplit.length != 2)
				{
					_log.warn(Strings.concat("Config.load(): invalid config property -> RateChanceDropSpoilById \"", item, "\""));
				}
				else
				{
					try
					{
						RATE_CHANCE_DROP_SPOIL_ITEMS_ID.put(Integer.parseInt(itemSplit[0]), Float.parseFloat(itemSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!item.isEmpty())
						{
							_log.warn(Strings.concat("Config.load(): invalid config property -> RateChanceDropSpoilById \"", item, "\""));
						}
					}
				}
			}
		}
	}
	
	public static void loadBossConfig()
	{
		ExProperties bossSettings = load(BOSS_FILE);
		
		RATE_RAID_REGEN = bossSettings.getProperty("RateRaidRegen", 1.);
		RATE_RAID_DEFENSE = bossSettings.getProperty("RateRaidDefense", 1.);
		RATE_RAID_ATTACK = bossSettings.getProperty("RateRaidAttack", 1.);
		RATE_EPIC_DEFENSE = bossSettings.getProperty("RateEpicDefense", RATE_RAID_DEFENSE);
		RATE_EPIC_ATTACK = bossSettings.getProperty("RateEpicAttack", RATE_RAID_ATTACK);
		RAID_MAX_LEVEL_DIFF = bossSettings.getProperty("RaidMaxLevelDiff", 8);
		
		ANNOUNCE_SPAWN_RB = bossSettings.getProperty("AnnounceToSpawnRb", false);
		ANNOUNCE_SPAWN_RB_REGION = bossSettings.getProperty("AnnounceSpawnRbForRegion", false);
		
		RAID_RESPAWN_MULTIPLIER = bossSettings.getProperty("RaidRespawnMultiplier", 1.0);
		
		ALT_NO_LASTHIT = bossSettings.getProperty("NoLasthitOnRaid", false);
	}
	
	public static void loadOtherConfig()
	{
		ExProperties otherSettings = load(OTHER_CONFIG_FILE);
		
		DEEPBLUE_DROP_RULES = otherSettings.getProperty("UseDeepBlueDropRules", true);
		DEEPBLUE_DROP_MAXDIFF = otherSettings.getProperty("DeepBlueDropMaxDiff", 8);
		DEEPBLUE_DROP_RAID_MAXDIFF = otherSettings.getProperty("DeepBlueDropRaidMaxDiff", 2);
		
		SWIMING_SPEED = otherSettings.getProperty("SwimingSpeedTemplate", 50);
		
		/* All item price 1 adena */
		SELL_ALL_ITEMS_FREE = otherSettings.getProperty("SellAllItemsFree", false);
		/* Inventory slots limits */
		INVENTORY_MAXIMUM_NO_DWARF = otherSettings.getProperty("MaximumSlotsForNoDwarf", 80);
		INVENTORY_MAXIMUM_DWARF = otherSettings.getProperty("MaximumSlotsForDwarf", 100);
		QUEST_INVENTORY_MAXIMUM = otherSettings.getProperty("MaximumSlotsForQuests", 100);
		
		MULTISELL_SIZE = otherSettings.getProperty("MultisellPageSize", 10);
		
		/* Warehouse slots limits */
		WAREHOUSE_SLOTS_NO_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForNoDwarf", 100);
		WAREHOUSE_SLOTS_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForDwarf", 120);
		WAREHOUSE_SLOTS_CLAN = otherSettings.getProperty("MaximumWarehouseSlotsForClan", 200);
		FREIGHT_SLOTS = otherSettings.getProperty("MaximumFreightSlots", 10);
		
		ENCHANT_ATTRIBUTE_STONE_CHANCE = otherSettings.getProperty("EnchantAttributeChance", 50);
		ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE = otherSettings.getProperty("EnchantAttributeCrystalChance", 30);
		
		REGEN_SIT_WAIT = otherSettings.getProperty("RegenSitWait", false);
		
		STARTING_ADENA = otherSettings.getProperty("StartingAdena", 0);
		UNSTUCK_SKILL = otherSettings.getProperty("UnstuckSkill", true);
		
		/* Amount of HP, MP, and CP is restored */
		RESPAWN_RESTORE_CP = otherSettings.getProperty("RespawnRestoreCP", 0.) / 100;
		RESPAWN_RESTORE_HP = otherSettings.getProperty("RespawnRestoreHP", 65.) / 100;
		RESPAWN_RESTORE_MP = otherSettings.getProperty("RespawnRestoreMP", 0.) / 100;
		
		/* Maximum number of available slots for pvt stores */
		MAX_PVTSTORE_SLOTS_DWARF = otherSettings.getProperty("MaxPvtStoreSlotsDwarf", 5);
		MAX_PVTSTORE_SLOTS_OTHER = otherSettings.getProperty("MaxPvtStoreSlotsOther", 4);
		MAX_PVTCRAFT_SLOTS = otherSettings.getProperty("MaxPvtManufactureSlots", 20);
		
		NORMAL_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("NormalNameColour", "FFFFFF"));
		CLANLEADER_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("ClanleaderNameColour", "FFFFFF"));
		
		GAME_POINT_ITEM_ID = otherSettings.getProperty("GamePointItemId", -1);
		STARTING_LVL = otherSettings.getProperty("StartingLvL", 0);
		
		TalkGuardChance = otherSettings.getProperty("TalkGuardChance", 90);
		TalkNormalChance = otherSettings.getProperty("TalkNormalChance", 50);
		TalkNormalPeriod = otherSettings.getProperty("TalkNormalPeriod", 20);
		TalkAggroPeriod = otherSettings.getProperty("TalkAggroPeriod", 2);
		
		ATTRIBUTE_ARMOR = otherSettings.getProperty("AttributeAddArmor", 6);
		ATTRIBUTE_WEAPON = otherSettings.getProperty("AttributeAddWeapon", 5);
		ATTRIBUTE_FIRST_WEAPON = otherSettings.getProperty("AttributeForCleanWeap", 20);
		
		MAX_ATTRIBUTE_ARMOR = otherSettings.getProperty("MaxAttributeArmor", 60);
		MAX_ATTRIBUTE_WEAPON = otherSettings.getProperty("MaxAttributeWeapon", 150);
		
		// By SmokiMo
		HENNA_MAX_VALUE = otherSettings.getProperty("HennaMaxValue", 5);
		
		ENEBLE_TITLE_COLOR_MOD = otherSettings.getProperty("EnebleTitleColorMod", false);
		TYPE_TITLE_COLOR_MOD = otherSettings.getProperty("TypeTitleColorMod", "PvP");
		COUNT_TITLE_1 = otherSettings.getProperty("CountTitle_1", 50);
		TITLE_COLOR_1 = Integer.decode("0x" + otherSettings.getProperty("TitleColor_1", "FFFFFF"));
		COUNT_TITLE_2 = otherSettings.getProperty("CountTitle_2", 100);
		TITLE_COLOR_2 = Integer.decode("0x" + otherSettings.getProperty("TitleColor_2", "FFFFFF"));
		COUNT_TITLE_3 = otherSettings.getProperty("CountTitle_3", 250);
		TITLE_COLOR_3 = Integer.decode("0x" + otherSettings.getProperty("TitleColor_3", "FFFFFF"));
		COUNT_TITLE_4 = otherSettings.getProperty("CountTitle_4", 500);
		TITLE_COLOR_4 = Integer.decode("0x" + otherSettings.getProperty("TitleColor_4", "FFFFFF"));
		COUNT_TITLE_5 = otherSettings.getProperty("CountTitle_5", 1000);
		TITLE_COLOR_5 = Integer.decode("0x" + otherSettings.getProperty("TitleColor_5", "FFFFFF"));
		ENEBLE_NAME_COLOR_MOD = otherSettings.getProperty("EnebleNameColorMod", false);
		TYPE_NAME_COLOR_MOD = otherSettings.getProperty("TypeNameColorMod", "Pk");
		COUNT_NAME_1 = otherSettings.getProperty("CountName_1", 50);
		NAME_COLOR_1 = Integer.decode("0x" + otherSettings.getProperty("NameColor_1", "FFFFFF"));
		COUNT_NAME_2 = otherSettings.getProperty("CountName_2", 100);
		NAME_COLOR_2 = Integer.decode("0x" + otherSettings.getProperty("NameColor_2", "FFFFFF"));
		COUNT_NAME_3 = otherSettings.getProperty("CountName_3", 250);
		NAME_COLOR_3 = Integer.decode("0x" + otherSettings.getProperty("NameColor_3", "FFFFFF"));
		COUNT_NAME_4 = otherSettings.getProperty("CountName_4", 500);
		NAME_COLOR_4 = Integer.decode("0x" + otherSettings.getProperty("NameColor_4", "FFFFFF"));
		COUNT_NAME_5 = otherSettings.getProperty("CountName_5", 1000);
		NAME_COLOR_5 = Integer.decode("0x" + otherSettings.getProperty("NameColor_5", "FFFFFF"));
		
		NEW_CHAR_IS_NOBLE = otherSettings.getProperty("NewCharIsNoble", false);
		NEW_CHAR_IS_HERO = otherSettings.getProperty("NewCharIsHero", false);
		
		SPAWN_CHAR = Boolean.parseBoolean(otherSettings.getProperty("CustomSpawn", "false"));
		SPAWN_X = Integer.parseInt(otherSettings.getProperty("SpawnX", ""));
		SPAWN_Y = Integer.parseInt(otherSettings.getProperty("SpawnY", ""));
		SPAWN_Z = Integer.parseInt(otherSettings.getProperty("SpawnZ", ""));
		
		ADEPT_ENABLE = otherSettings.getProperty("EnableTalkingAdept", true);
		
		SPAWN_CITIES_TREE = otherSettings.getProperty("SpawnCityTrees", false);
		SPAWN_NPC_CLASS_MASTER = otherSettings.getProperty("SpawnClassMaster", true);
		ALLOW_EVENT_GATEKEEPER = otherSettings.getProperty("SpawnEventGateKeeper", false);
		ALLOW_SPAWN_CUSTOM_HALL_NPC = otherSettings.getProperty("SpawnCustomHallNPC", true);
	}
	
	public static void loadSpoilConfig()
	{
		ExProperties spoilSettings = load(SPOIL_CONFIG_FILE);
		
		BASE_SPOIL_RATE = spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", 78.);
		MINIMUM_SPOIL_RATE = spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", 1.);
		ALT_SPOIL_FORMULA = spoilSettings.getProperty("AltFormula", false);
		MANOR_SOWING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingSuccess", 100.);
		MANOR_SOWING_ALT_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingAltSuccess", 10.);
		MANOR_HARVESTING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfHarvestingSuccess", 90.);
		MANOR_DIFF_PLAYER_TARGET = spoilSettings.getProperty("MinDiffPlayerMob", 5);
		MANOR_DIFF_PLAYER_TARGET_PENALTY = spoilSettings.getProperty("DiffPlayerMobPenalty", 5.);
		MANOR_DIFF_SEED_TARGET = spoilSettings.getProperty("MinDiffSeedMob", 5);
		MANOR_DIFF_SEED_TARGET_PENALTY = spoilSettings.getProperty("DiffSeedMobPenalty", 5.);
		ALLOW_MANOR = spoilSettings.getProperty("AllowManor", true);
		MANOR_REFRESH_TIME = spoilSettings.getProperty("AltManorRefreshTime", 20);
		MANOR_REFRESH_MIN = spoilSettings.getProperty("AltManorRefreshMin", 00);
		MANOR_APPROVE_TIME = spoilSettings.getProperty("AltManorApproveTime", 6);
		MANOR_APPROVE_MIN = spoilSettings.getProperty("AltManorApproveMin", 00);
		MANOR_MAINTENANCE_PERIOD = spoilSettings.getProperty("AltManorMaintenancePeriod", 360000);
	}
	
	public static void loadInstancesConfig()
	{
		ExProperties instancesSettings = load(INSTANCES_FILE);
		
		ALLOW_INSTANCES_LEVEL_MANUAL = instancesSettings.getProperty("AllowInstancesLevelManual", false);
		ALLOW_INSTANCES_PARTY_MANUAL = instancesSettings.getProperty("AllowInstancesPartyManual", false);
		INSTANCES_LEVEL_MIN = instancesSettings.getProperty("InstancesLevelMin", 1);
		INSTANCES_LEVEL_MAX = instancesSettings.getProperty("InstancesLevelMax", 85);
		INSTANCES_PARTY_MIN = instancesSettings.getProperty("InstancesPartyMin", 2);
		INSTANCES_PARTY_MAX = instancesSettings.getProperty("InstancesPartyMax", 100);
		INSTANCES_MAX_BOXES = instancesSettings.getProperty("InstancesMaxBoxes", -1);
	}
	
	public static void loadEpicBossConfig()
	{
		ExProperties epicBossSettings = load(EPIC_BOSS_FILE);
		
		ANTHARAS_DIABLE_CC_ENTER = epicBossSettings.getProperty("AntharasDisableCCenter", false);
		VALAKAS_DISABLE_CC_ENTER = epicBossSettings.getProperty("ValakasDisableCCenter", false);
		
		FIXINTERVALOFANTHARAS_HOUR = epicBossSettings.getProperty("AntharasRespawn", 264);
		RANDOM_TIME_OF_ANTHARAS = epicBossSettings.getProperty("AntharasRandom", 6);
		SPAWN_CUSTOM_ANTHARAS = epicBossSettings.getProperty("SpawnCustomAntharas", 3);
		ANTHARAS_MINIONS_NUMBER = epicBossSettings.getProperty("AntharasMaxMinions", 30);
		
		FIXINTERVALOFBAIUM_HOUR = epicBossSettings.getProperty("BaiumRespawn", 120);
		RANDOMINTERVALOFBAIUM = epicBossSettings.getProperty("BaiumRandom", 8);
		
		FIXINTERVALOFBAYLORSPAWN_HOUR = epicBossSettings.getProperty("BaylorRespawn", 24);
		RANDOMINTERVALOFBAYLORSPAWN = epicBossSettings.getProperty("BaylorRandom", 24);
		
		FIXINTERVALOFBELETHSPAWN_HOUR = epicBossSettings.getProperty("BelethRespawn", 48);
		BOSS_BELETH_MIN_COUNT = epicBossSettings.getProperty("BossBelethMinCount", 50);
		
		FIXINTERVALOFSAILRENSPAWN_HOUR = epicBossSettings.getProperty("SailrenRespawn", 24);
		RANDOMINTERVALOFSAILRENSPAWN = epicBossSettings.getProperty("SailrenRandom", 24);
		
		FIXINTERVALOFVALAKAS = epicBossSettings.getProperty("ValakasRespawn", 264);
		RANDOM_TIME_OF_VALAKAS = epicBossSettings.getProperty("ValakasRandom", 12);
		DV_RB_DESPAWN = epicBossSettings.getProperty("DVRaidDespawn", 30);
		
		MAX_VALAKAS_MINIONS = epicBossSettings.getProperty("ValakasMaxMinions", 36);
	}
	
	public static void loadFormulasConfig()
	{
		ExProperties formulasSettings = load(FORMULAS_CONFIGURATION_FILE);
		
		SKILL_CHANCE_CALCULATED_BY_ENCHANT_LEVEL = formulasSettings.getProperty("CalculateSkillSuccessByEnchantLevel", false);
		
		NON_BACK_BLOW_MULTIPLIER = formulasSettings.getProperty("NonBackSkillModifier", 2.04);
		BACK_BLOW_MULTIPLIER = formulasSettings.getProperty("BackSkillsModifier", 1.5);
		SKILL_FORCE_H5_FORMULA = formulasSettings.getProperty("SkillForceH5Formula", true);
		PDAM_OLD_FORMULA = formulasSettings.getProperty("PDamOldFormula", false);
		SKILLS_CHANCE_SHOW = formulasSettings.getProperty("SkillsShowChance", true);
		SKILLS_CHANCE_MOD_MAGE = formulasSettings.getProperty("SkillsChanceModMage", 11.);
		SKILLS_CHANCE_POW_MAGE = formulasSettings.getProperty("SkillsChancePowMage", 0.5);
		SKILLS_CHANCE_MOD_FIGHTER = formulasSettings.getProperty("SkillsChanceModFighter", 11.);
		SKILLS_CHANCE_POW_FIGHTER = formulasSettings.getProperty("SkillsChancePowFighter", 0.5);
		SKILLS_CHANCE_MIN = formulasSettings.getProperty("SkillsChanceMin", 10);
		SKILLS_CHANCE_CAP = formulasSettings.getProperty("SkillsChanceCap", 90);
		SKILLS_CHANCE_CAP_ONLY_PLAYERS = formulasSettings.getProperty("SkillsChanceCapOnlyPlayers", false);
		SKILLS_MOB_CHANCE = formulasSettings.getProperty("SkillsMobChance", 0.5);
		SKILLS_DEBUFF_MOB_CHANCE = formulasSettings.getProperty("SkillsDebuffMobChance", 0.5);
		SKILLS_CAST_TIME_MIN = formulasSettings.getProperty("SkillsCastTimeMin", 333);
		PHYS_SKILLS_DAMAGE_POW = formulasSettings.getProperty("PhysSkillsDamagePow", 1.0);
		SKILL_PROF_MULTIPLIER = formulasSettings.getProperty("SkillProfMultiplier", 0.85);
		
		BASE_MAGICAL_CRIT_RATE = formulasSettings.getProperty("BaseMcritRate", 10.);
		
		ALT_ABSORB_DAMAGE_MODIFIER = formulasSettings.getProperty("AbsorbDamageModifier", 1.0);
		
		String[] propertySplit;
		propertySplit = formulasSettings.getProperty("LimitPatk", "-1,25000").split(";");
		LIMIT_PATK = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_PATK]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_PATK.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_PATK]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitMAtk", "-1,25000").split(";");
		LIMIT_MATK = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_MATK]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_MATK.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_MATK]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitPDef", "-1,15000").split(";");
		LIMIT_PDEF = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_PDEF]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_PDEF.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_PDEF]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitMDef", "-1,15000").split(";");
		LIMIT_MDEF = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_MDEF]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_MDEF.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_MDEF]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitPatkSpd", "-1,1500").split(";");
		LIMIT_PATK_SPD = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_PATK_SPD]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_PATK_SPD.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_PATK_SPD]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitMatkSpd", "-1,1999").split(";");
		LIMIT_MATK_SPD = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_MATK_SPD]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_MATK_SPD.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_MATK_SPD]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitCriticalDamage", "-1,2000").split(";");
		LIMIT_CRIT_DAM = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_CRIT_DAM]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_CRIT_DAM.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_CRIT_DAM]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		
		propertySplit = formulasSettings.getProperty("LimitCritical", "-1,500").split(";");
		LIMIT_CRIT = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_CRIT]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_CRIT.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_CRIT]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitMCritical", "-1,20").split(";");
		LIMIT_MCRIT = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_MCRIT]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_MCRIT.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_MCRIT]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitAccuracy", "-1,200").split(";");
		LIMIT_ACCURACY = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_ACCURACY]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_ACCURACY.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_ACCURACY]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitEvasion", "-1,200").split(";");
		LIMIT_EVASION = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_EVASION]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_EVASION.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_EVASION]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitMove", "-1,250").split(";");
		LIMIT_MOVE = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_MOVE]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_MOVE.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_MOVE]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitReflect", "-1,10000").split(";");
		LIMIT_REFLECT = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_REFLECT]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_REFLECT.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_REFLECT]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		propertySplit = formulasSettings.getProperty("LimitFame", "-1,50000").split(";");
		LIMIT_FAME = new TIntIntHashMap(propertySplit.length);
		for (String limit : propertySplit)
		{
			String[] s = limit.split(",");
			if (s.length != 2)
				_log.warn("[LIMIT_FAME]: invalid config property ->  \"" + limit + "\"");
			else
			{
				try
				{
					LIMIT_FAME.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!limit.isEmpty())
					{
						_log.warn("[LIMIT_FAME]: invalid config property ->  \"" + s[0] + "\"" + s[1]);
					}
				}
			}
		}
		
		ALT_ELEMENT_FORMULA = formulasSettings.getProperty("AltElementFormula", false);
		
		SKILL_MASTERY_TRIGGER_CHANSE = formulasSettings.getProperty("SkillMasteryActivationChance", 5000);
		
		ALT_NPC_PATK_MODIFIER = formulasSettings.getProperty("NpcPAtkModifier", 1.0);
		ALT_NPC_MATK_MODIFIER = formulasSettings.getProperty("NpcMAtkModifier", 1.0);
		ALT_NPC_MAXHP_MODIFIER = formulasSettings.getProperty("NpcMaxHpModifier", 1.00);
		ALT_NPC_MAXMP_MODIFIER = formulasSettings.getProperty("NpcMapMpModifier", 1.00);
		
		ALT_POLE_DAMAGE_MODIFIER = formulasSettings.getProperty("PoleDamageModifier", 1.0);
		ALT_BOW_PVP_DAMAGE_MODIFIER = formulasSettings.getProperty("BowPvpDamageModifier", 1.0);
		ALT_BOW_PVE_DAMAGE_MODIFIER = formulasSettings.getProperty("BowPveDamageModifier", 1.0);
		ALT_PET_PVP_DAMAGE_MODIFIER = formulasSettings.getProperty("PetPvpDamageModifier", 1.0);
		
		FORMULA_LETHAL_MAX_HP = formulasSettings.getProperty("LethalImmuneHp", 50000);
	}
	
	public static void loadDevelopSettings()
	{
		ExProperties DevelopSettings = load(DEVELOP_FILE);
		
		DEV_UNDERGROUND_COLISEUM = DevelopSettings.getProperty("UndergroundColiseum", false);
		DEV_UNDERGROUND_COLISEUM_MEMBER_COUNT = DevelopSettings.getProperty("UndergroundColiseumMemberCount", 7);
		
		ALT_DEBUG_ENABLED = DevelopSettings.getProperty("AltDebugEnabled", false);
		ALT_DEBUG_PVP_ENABLED = DevelopSettings.getProperty("AltDebugPvPEnabled", false);
		ALT_DEBUG_PVP_DUEL_ONLY = DevelopSettings.getProperty("AltDebugPvPDuelOnly", true);
		ALT_DEBUG_PVE_ENABLED = DevelopSettings.getProperty("AltDebugPvEEnabled", false);
		
		DONTLOADSPAWN = DevelopSettings.getProperty("StartWithoutSpawn", false);
		DONTLOADQUEST = DevelopSettings.getProperty("StartWithoutQuest", false);
		DONTLOADEVENTS = DevelopSettings.getProperty("StartWithoutEvents", false);
		DONTLOADOPTIONDATA = DevelopSettings.getProperty("DontLoadOptionData", false);
		DONTLOADNPCDROP = DevelopSettings.getProperty("DontLoadNpcDrop", false);
		DONTLOADNEXUS = DevelopSettings.getProperty("DontLoadNexus", false);
		DONTLOADMULTISELLS = DevelopSettings.getProperty("DontLoadMultisells", false);
		DONTAUTOANNOUNCE = DevelopSettings.getProperty("DontAutoAnnounce", false);
		
		LOAD_CUSTOM_SPAWN = DevelopSettings.getProperty("LoadAddGmSpawn", false);
		SAVE_GM_SPAWN = DevelopSettings.getProperty("SaveGmSpawn", false);
	}
	
	public static void loadExtSettings()
	{
		ExProperties properties = load(EXT_FILE);
		
		EX_NEW_PETITION_SYSTEM = properties.getProperty("NewPetitionSystem", false);
		EX_JAPAN_MINIGAME = properties.getProperty("JapanMinigame", false);
		EX_LECTURE_MARK = properties.getProperty("LectureMark", false);
		
		// Emulation OFF Core (packet SendStatus)
		Random ppc = new Random();
		int z = ppc.nextInt(6);
		if (z == 0)
		{
			z += 2;
		}
		for (int x = 0; x < 8; x++)
		{
			if (x == 4)
			{
				RWHO_ARRAY[x] = 44;
			}
			else
			{
				RWHO_ARRAY[x] = 51 + ppc.nextInt(z);
			}
		}
		RWHO_ARRAY[11] = 37265 + ppc.nextInt(z * 2 + 3);
		RWHO_ARRAY[8] = 51 + ppc.nextInt(z);
		z = 36224 + ppc.nextInt(z * 2);
		RWHO_ARRAY[9] = z;
		RWHO_ARRAY[10] = z;
		RWHO_ARRAY[12] = 1;
		RWHO_LOG = properties.getProperty("RemoteWhoLog", false);
		RWHO_SEND_TRASH = properties.getProperty("RemoteWhoSendTrash", false);
		RWHO_MAX_ONLINE = properties.getProperty("RemoteWhoMaxOnline", 0);
		RWHO_KEEP_STAT = properties.getProperty("RemoteOnlineKeepStat", 5);
		RWHO_ONLINE_INCREMENT = properties.getProperty("RemoteOnlineIncrement", 0);
		RWHO_PRIV_STORE_FACTOR = properties.getProperty("RemotePrivStoreFactor", 0);
		RWHO_FORCE_INC = properties.getProperty("RemoteWhoForceInc", 0);
	}
	
	public static void loadItemsSettings()
	{
		ExProperties itemsProperties = load(ITEMS_FILE);
		
		CAN_BE_TRADED_NO_TARADEABLE = itemsProperties.getProperty("CanBeTradedNoTradeable", false);
		CAN_BE_TRADED_NO_SELLABLE = itemsProperties.getProperty("CanBeTradedNoSellable", false);
		CAN_BE_TRADED_NO_STOREABLE = itemsProperties.getProperty("CanBeTradedNoStoreable", false);
		CAN_BE_TRADED_SHADOW_ITEM = itemsProperties.getProperty("CanBeTradedShadowItem", false);
		CAN_BE_TRADED_HERO_WEAPON = itemsProperties.getProperty("CanBeTradedHeroWeapon", false);
		CAN_BE_WH_NO_TARADEABLE = itemsProperties.getProperty("CanBeWhNoTradeable", false);
		CAN_BE_CWH_NO_TARADEABLE = itemsProperties.getProperty("CanBeCwhNoTradeable", false);
		CAN_BE_CWH_IS_AUGMENTED = itemsProperties.getProperty("CanBeCwhIsAugmented", false);
		CAN_BE_WH_IS_AUGMENTED = itemsProperties.getProperty("CanBeWhIsAugmented", false);
		ALLOW_SOUL_SPIRIT_SHOT_INFINITELY = itemsProperties.getProperty("AllowSoulSpiritShotInfinitely", false);
		ALLOW_ARROW_INFINITELY = itemsProperties.getProperty("AllowArrowInfinitely", false);
		ALLOW_START_ITEMS = itemsProperties.getProperty("AllowStartItems", false);
		BIND_NEWBIE_START_ITEMS_TO_CHAR = itemsProperties.getProperty("BindItemsToCharacter", false);
		START_ITEMS_MAGE = itemsProperties.getProperty("StartItemsMageIds", new int[]
		{
			57
		});
		START_ITEMS_MAGE_COUNT = itemsProperties.getProperty("StartItemsMageCount", new int[]
		{
			1
		});
		START_ITEMS_FITHER = itemsProperties.getProperty("StartItemsFigtherIds", new int[]
		{
			57
		});
		START_ITEMS_FITHER_COUNT = itemsProperties.getProperty("StartItemsFigtherCount", new int[]
		{
			1
		});
		
		// bind to acc
		START_ITEMS_MAGE_BIND_TO_CHAR = itemsProperties.getProperty("BindCharStartItemsMageIds", new int[]
		{
			57
		});
		START_ITEMS_MAGE_COUNT_BIND_TO_CHAR = itemsProperties.getProperty("BindCharStartItemsMageCount", new int[]
		{
			1
		});
		START_ITEMS_FITHER_BIND_TO_CHAR = itemsProperties.getProperty("BindCharStartItemsFigtherIds", new int[]
		{
			57
		});
		START_ITEMS_FITHER_COUNT_BIND_TO_CHAR = itemsProperties.getProperty("BindCharStartItemsFigtherCount", new int[]
		{
			1
		});
	}
	
	public static void loadTopSettings()
	{
		ExProperties topSetting = load(TOP_FILE);
		
		L2_TOP_MANAGER_ENABLED = topSetting.getProperty("L2TopManagerEnabled", false);
		L2_TOP_MANAGER_INTERVAL = topSetting.getProperty("L2TopManagerInterval", 300000);
		L2_TOP_WEB_ADDRESS = topSetting.getProperty("L2TopWebAddress", "");
		L2_TOP_SMS_ADDRESS = topSetting.getProperty("L2TopSmsAddress", "");
		L2_TOP_SERVER_ADDRESS = topSetting.getProperty("L2TopServerAddress", "first-team.ru");
		L2_TOP_SAVE_DAYS = topSetting.getProperty("L2TopSaveDays", 30);
		L2_TOP_REWARD = topSetting.getProperty("L2TopReward", new int[0]);
		L2_TOP_NAME_PREFIX = topSetting.getProperty("L2TopServerPrefix", "");
		
		MMO_TOP_MANAGER_ENABLED = topSetting.getProperty("MMOTopEnable", false);
		MMO_TOP_MANAGER_INTERVAL = topSetting.getProperty("MMOTopManagerInterval", 300000);
		MMO_TOP_WEB_ADDRESS = topSetting.getProperty("MMOTopUrl", "");
		MMO_TOP_SERVER_ADDRESS = topSetting.getProperty("MMOTopServerAddress", "first-team.ru");
		MMO_TOP_SAVE_DAYS = topSetting.getProperty("MMOTopSaveDays", 30);
		MMO_TOP_REWARD = topSetting.getProperty("MMOTopReward", new int[0]);
	}
	
	public static void loadPaymentSettings()
	{
		ExProperties paymentSettings = load(PAYMENT_FILE);
		
		SMS_PAYMENT_MANAGER_ENABLED = paymentSettings.getProperty("SMSPaymentEnabled", false);
		SMS_PAYMENT_WEB_ADDRESS = paymentSettings.getProperty("SMSPaymentWebAddress", "");
		SMS_PAYMENT_MANAGER_INTERVAL = paymentSettings.getProperty("SMSPaymentManagerInterval", 300000);
		SMS_PAYMENT_SAVE_DAYS = paymentSettings.getProperty("SMSPaymentSaveDays", 30);
		SMS_PAYMENT_SERVER_ADDRESS = paymentSettings.getProperty("SMSPaymentServerAddress", "revolt-team.com");
		SMS_PAYMENT_REWARD = paymentSettings.getProperty("SMSPaymentReward", new int[0]);
	}
	
	public static void loadAltSettings()
	{
		ExProperties altSettings = load(ALT_SETTINGS_FILE);
		
		ALT_ARENA_EXP = altSettings.getProperty("ArenaExp", true);
		ALT_GAME_DELEVEL = altSettings.getProperty("Delevel", true);
		ALT_SAVE_UNSAVEABLE = altSettings.getProperty("AltSaveUnsaveable", false);
		SHIELD_SLAM_BLOCK_IS_MUSIC = altSettings.getProperty("ShieldSlamBlockIsMusic", false);
		STEAL_DIVINITY_SUCCESS = altSettings.getProperty("StealDivinitySuccess", 50);
		ALT_SAVE_EFFECTS_REMAINING_TIME = altSettings.getProperty("AltSaveEffectsRemainingTime", 5);
		ALT_SHOW_REUSE_MSG = altSettings.getProperty("AltShowSkillReuseMessage", true);
		ALT_DELETE_SA_BUFFS = altSettings.getProperty("AltDeleteSABuffs", false);
		AUTO_LOOT = altSettings.getProperty("AutoLoot", false);
		AUTO_LOOT_ONLY_ADENA = altSettings.getProperty("AutoLootOnlyAdena", false);
		AUTO_LOOT_HERBS = altSettings.getProperty("AutoLootHerbs", false);
		AUTO_LOOT_INDIVIDUAL = altSettings.getProperty("AutoLootIndividual", false);
		AUTO_LOOT_FROM_RAIDS = altSettings.getProperty("AutoLootFromRaids", false);
		AUTO_LOOT_PK = altSettings.getProperty("AutoLootPK", false);
		
		SERVICES_ENABLE_NO_CARRIER = altSettings.getProperty("EnableNoCarrier", false);
		SERVICES_NO_CARRIER_TIME = altSettings.getProperty("NoCarrierTime", 90);
		
		ALT_GAME_KARMA_PLAYER_CAN_SHOP = altSettings.getProperty("AltKarmaPlayerCanShop", true);
		ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = altSettings.getProperty("AltKarmaPlayerCanTeleport", true);
		ALT_GAME_KARMA_PLAYER_CAN_USE_GK = altSettings.getProperty("AltKarmaPlayerCanUseGK", false);
		ALT_GAME_KARMA_PLAYER_CAN_TRADE = altSettings.getProperty("AltKarmaPlayerCanTrade", true);
		ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = altSettings.getProperty("AltKarmaPlayerCanUseWareHouse", true);
		
		SAVING_SPS = altSettings.getProperty("SavingSpS", false);
		MANAHEAL_SPS_BONUS = altSettings.getProperty("ManahealSpSBonus", false);
		
		CRAFT_MASTERWORK_CHANCE = altSettings.getProperty("CraftMasterworkChance", 3.);
		CRAFT_DOUBLECRAFT_CHANCE = altSettings.getProperty("CraftDoubleCraftChance", 3.);
		
		ALT_GAME_CREATION = altSettings.getProperty("AllowAltCreationRate", false);
		ALT_GAME_CREATION_RARE_XPSP_RATE = altSettings.getProperty("AltCreationRateXpSp", 1.0);
		ALT_GAME_CREATION_XP_RATE = altSettings.getProperty("AltCreationRateXp", 1.0);
		ALT_GAME_CREATION_SP_RATE = altSettings.getProperty("AltCreationRateSp", 1.0);
		
		ALT_ALLOW_AUGMENT_ALL = altSettings.getProperty("AugmentAll", false);
		ALT_ALLOW_DROP_AUGMENTED = altSettings.getProperty("AlowDropAugmented", false);
		ALT_GAME_UNREGISTER_RECIPE = altSettings.getProperty("AltUnregisterRecipe", true);
		ALT_GAME_SHOW_DROPLIST = altSettings.getProperty("AltShowDroplist", true);
		ALLOW_NPC_SHIFTCLICK = altSettings.getProperty("AllowShiftClick", true);
		ALT_PLAYER_SHIFTCLICK = altSettings.getProperty("AllowPlayerShiftClick", false);
		ALT_BYPASS_SHIFT_CLICK_NPC_TO_CB = altSettings.getProperty("BypassShiftClickToCB", false);
		ALT_FULL_NPC_STATS_PAGE = altSettings.getProperty("AltFullStatsPage", false);
		ALT_GAME_SUBCLASS_WITHOUT_QUESTS = altSettings.getProperty("AltAllowSubClassWithoutQuest", false);
		ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM = altSettings.getProperty("AltAllowSubClassWithoutBaium", true);
		ALT_GAME_LEVEL_TO_GET_SUBCLASS = altSettings.getProperty("AltLevelToGetSubclass", 75);
		ALT_GAME_START_LEVEL_TO_SUBCLASS = altSettings.getProperty("AltStartLevelToSubclass", 40);
		ALT_GAME_SUB_ADD = altSettings.getProperty("AltSubAdd", 0);
		ALL_SUBCLASSES_AVAILABLE = altSettings.getProperty("AllSubclassesAvailable", false);
		ALT_GAME_SUB_BOOK = altSettings.getProperty("AltSubBook", false);
		ALT_MAX_LEVEL = Math.min(altSettings.getProperty("AltMaxLevel", 85), Experience.LEVEL.length - 1);
		ALT_MAX_SUB_LEVEL = Math.min(altSettings.getProperty("AltMaxSubLevel", 80), Experience.LEVEL.length - 1);
		ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE = altSettings.getProperty("AltAllowOthersWithdrawFromClanWarehouse", false);
		ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER = altSettings.getProperty("AltAllowClanCommandOnlyForClanLeader", true);
		EXPELLED_MEMBER_PENALTY = altSettings.getProperty("ExpelledMemberPenalty", 24);
		LEAVED_ALLY_PENALTY = altSettings.getProperty("LeavedAllyPenalty", 24);
		DISSOLVED_ALLY_PENALTY = altSettings.getProperty("DissolvedAllyPenalty", 24);
		DISSOLVED_CLAN_PENALTY = altSettings.getProperty("DissolvedClanPenalty", 24);
		CLAN_DISBAND_TIME = altSettings.getProperty("DisbanClanTime", 48);
		LEAVE_CLAN_PENALTY = altSettings.getProperty("LeaveClanPenalty", 24);
		
		MAX_CLAN_WARS_DECLARATION = altSettings.getProperty("MaxClanWars", 30);
		
		ALT_GAME_REQUIRE_CLAN_CASTLE = altSettings.getProperty("AltRequireClanCastle", false);
		ALT_GAME_REQUIRE_CASTLE_DAWN = altSettings.getProperty("AltRequireCastleDawn", true);
		ALT_GAME_ALLOW_ADENA_DAWN = altSettings.getProperty("AltAllowAdenaDawn", true);
		ALT_ADD_RECIPES = altSettings.getProperty("AltAddRecipes", 0);
		SS_ANNOUNCE_PERIOD = altSettings.getProperty("SSAnnouncePeriod", 0);
		PETITIONING_ALLOWED = altSettings.getProperty("PetitioningAllowed", true);
		MAX_PETITIONS_PER_PLAYER = altSettings.getProperty("MaxPetitionsPerPlayer", 5);
		MAX_PETITIONS_PENDING = altSettings.getProperty("MaxPetitionsPending", 25);
		AUTO_LEARN_SKILLS = altSettings.getProperty("AutoLearnSkills", false);
		AUTO_LEARN_FORGOTTEN_SKILLS = altSettings.getProperty("AutoLearnForgottenSkills", false);
		AUTO_LEARN_DIVINE_INSPIRATION = altSettings.getProperty("AutoLearnDivineInspiration", false);
		ALT_SOCIAL_ACTION_REUSE = altSettings.getProperty("AltSocialActionReuse", false);
		ALT_DISABLE_SPELLBOOKS = altSettings.getProperty("AltDisableSpellbooks", false);
		ALT_SIMPLE_SIGNS = altSettings.getProperty("PushkinSignsOptions", false);
		ALT_TELE_TO_CATACOMBS = altSettings.getProperty("TeleToCatacombs", false);
		ALT_BS_CRYSTALLIZE = altSettings.getProperty("BSCrystallize", false);
		ALT_MAMMON_UPGRADE = altSettings.getProperty("MammonUpgrade", 6680500);
		ALT_MAMMON_EXCHANGE = altSettings.getProperty("MammonExchange", 10091400);
		ALT_BUFF_LIMIT = altSettings.getProperty("BuffLimit", 20);
		ALT_DEATH_PENALTY = altSettings.getProperty("EnableAltDeathPenalty", false);
		ALLOW_DEATH_PENALTY_C5 = altSettings.getProperty("EnableDeathPenaltyC5", true);
		ALT_DEATH_PENALTY_C5_CHANCE = altSettings.getProperty("DeathPenaltyC5Chance", 10);
		ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY = altSettings.getProperty("ChaoticCanUseScrollOfRecovery", false);
		ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY = altSettings.getProperty("DeathPenaltyC5RateExpPenalty", 1);
		ALT_DEATH_PENALTY_C5_KARMA_PENALTY = altSettings.getProperty("DeathPenaltyC5RateKarma", 1);
		ALT_PK_DEATH_RATE = altSettings.getProperty("AltPKDeathRate", 0.);
		NONOWNER_ITEM_PICKUP_DELAY = altSettings.getProperty("NonOwnerItemPickupDelay", 15L) * 1000L;
		
		ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY = altSettings.getProperty("KamalokaNightmaresPremiumOnly", false);
		ALT_KAMALOKA_NIGHTMARE_REENTER = altSettings.getProperty("SellReenterNightmaresTicket", true);
		ALT_KAMALOKA_ABYSS_REENTER = altSettings.getProperty("SellReenterAbyssTicket", true);
		ALT_KAMALOKA_LAB_REENTER = altSettings.getProperty("SellReenterLabyrinthTicket", true);
		ALT_PET_HEAL_BATTLE_ONLY = altSettings.getProperty("PetsHealOnlyInBattle", true);
		SAVE_PET_EFFECT = altSettings.getProperty("SavePetEffect", true);
		CHAR_TITLE = altSettings.getProperty("CharTitle", false);
		ADD_CHAR_TITLE = altSettings.getProperty("CharAddTitle", "");
		
		ALT_ALLOW_SELL_COMMON = altSettings.getProperty("AllowSellCommon", true);
		ALT_ALLOW_SHADOW_WEAPONS = altSettings.getProperty("AllowShadowWeapons", true);
		ALT_DISABLED_MULTISELL = altSettings.getProperty("DisabledMultisells", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_PRICE_LIMITS = altSettings.getProperty("ShopPriceLimits", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_FORBIDDEN_ITEMS = altSettings.getProperty("ShopForbiddenItems", ArrayUtils.EMPTY_INT_ARRAY);
		
		ALT_ALLOWED_PET_POTIONS = altSettings.getProperty("AllowedPetPotions", new int[]
		{
			735,
			1060,
			1061,
			1062,
			1374,
			1375,
			1539,
			1540,
			6035,
			6036
		});
		
		FESTIVAL_MIN_PARTY_SIZE = altSettings.getProperty("FestivalMinPartySize", 5);
		FESTIVAL_RATE_PRICE = altSettings.getProperty("FestivalRatePrice", 1.0);
		
		RIFT_MIN_PARTY_SIZE = altSettings.getProperty("RiftMinPartySize", 5);
		RIFT_SPAWN_DELAY = altSettings.getProperty("RiftSpawnDelay", 10000);
		RIFT_MAX_JUMPS = altSettings.getProperty("MaxRiftJumps", 4);
		RIFT_AUTO_JUMPS_TIME = altSettings.getProperty("AutoJumpsDelay", 8);
		RIFT_AUTO_JUMPS_TIME_RAND = altSettings.getProperty("AutoJumpsDelayRandom", 120000);
		
		RIFT_ENTER_COST_RECRUIT = altSettings.getProperty("RecruitFC", 18);
		RIFT_ENTER_COST_SOLDIER = altSettings.getProperty("SoldierFC", 21);
		RIFT_ENTER_COST_OFFICER = altSettings.getProperty("OfficerFC", 24);
		RIFT_ENTER_COST_CAPTAIN = altSettings.getProperty("CaptainFC", 27);
		RIFT_ENTER_COST_COMMANDER = altSettings.getProperty("CommanderFC", 30);
		RIFT_ENTER_COST_HERO = altSettings.getProperty("HeroFC", 33);
		ALLOW_CLANSKILLS = altSettings.getProperty("AllowClanSkills", true);
		ALLOW_LEARN_TRANS_SKILLS_WO_QUEST = altSettings.getProperty("AllowLearnTransSkillsWOQuest", false);
		PARTY_LEADER_ONLY_CAN_INVITE = altSettings.getProperty("PartyLeaderOnlyCanInvite", true);
		MAX_PARTY_SIZE = altSettings.getProperty("MaxPartySize", 9);
		ALLOW_TALK_WHILE_SITTING = altSettings.getProperty("AllowTalkWhileSitting", true);
		ALLOW_NOBLE_TP_TO_ALL = altSettings.getProperty("AllowNobleTPToAll", false);
		
		CLANHALL_BUFFTIME_MODIFIER = altSettings.getProperty("ClanHallBuffTimeModifier", 1);
		SONGDANCETIME_MODIFIER = altSettings.getProperty("SongDanceTimeModifier", 1);
		MAXLOAD_MODIFIER = altSettings.getProperty("MaxLoadModifier", 1.0);
		GATEKEEPER_MODIFIER = altSettings.getProperty("GkCostMultiplier", 1.0);
		GATEKEEPER_FREE = altSettings.getProperty("GkFree", 40);
		CRUMA_GATEKEEPER_LVL = altSettings.getProperty("GkCruma", 65);
		ALT_IMPROVED_PETS_LIMITED_USE = altSettings.getProperty("ImprovedPetsLimitedUse", false);
		
		ALT_CHAMPION_CHANCE1 = altSettings.getProperty("AltChampionChance1", 0.);
		ALT_CHAMPION_CHANCE2 = altSettings.getProperty("AltChampionChance2", 0.);
		ALT_CHAMPION_CAN_BE_AGGRO = altSettings.getProperty("AltChampionAggro", false);
		ALT_CHAMPION_CAN_BE_SOCIAL = altSettings.getProperty("AltChampionSocial", false);
		ALT_CHAMPION_DROP_HERBS = altSettings.getProperty("AltChampionDropHerbs", false);
		ALT_SHOW_MONSTERS_AGRESSION = altSettings.getProperty("AltShowMonstersAgression", false);
		ALT_SHOW_MONSTERS_LVL = altSettings.getProperty("AltShowMonstersLvL", false);
		ALT_CHAMPION_TOP_LEVEL = altSettings.getProperty("AltChampionTopLevel", 75);
		ALT_CHAMPION_MIN_LEVEL = altSettings.getProperty("AltChampionMinLevel", 20);
		
		ALT_VITALITY_ENABLED = altSettings.getProperty("AltVitalityEnabled", true);
		ALT_VITALITY_RATE = altSettings.getProperty("AltVitalityRate", new int[]
		{
			150,
			200,
			250,
			300
		});
		ALT_VITALITY_CONSUME_RATE = altSettings.getProperty("AltVitalityConsumeRate", 1.);
		ALT_VITALITY_RAID_BONUS = altSettings.getProperty("AltVitalityRaidBonus", 2000);
		
		PCBANG_POINTS_ENABLED = altSettings.getProperty("PcBangPointsEnabled", false);
		MAX_PC_BANG_POINTS = altSettings.getProperty("PcBangMaxPoints", 200000);
		PCBANG_POINTS_BONUS_DOUBLE_CHANCE = altSettings.getProperty("PcBangPointsDoubleChance", 10.);
		PCBANG_POINTS_BONUS = altSettings.getProperty("PcBangPointsBonus", 0);
		PCBANG_POINTS_DELAY = altSettings.getProperty("PcBangPointsDelay", 20);
		PCBANG_POINTS_MIN_LVL = altSettings.getProperty("PcBangPointsMinLvl", 1);
		
		ALT_MAX_ALLY_SIZE = altSettings.getProperty("AltMaxAllySize", 3);
		ALT_PARTY_DISTRIBUTION_RANGE = altSettings.getProperty("AltPartyDistributionRange", 1500);
		MAX_DISTRIBUTE_MEMBER_LEVEL_PARTY = altSettings.getProperty("MaxDiffLevelInParty", 20);
		ALT_PARTY_BONUS = altSettings.getProperty("AltPartyBonus", new double[]
		{
			1.00,
			1.10,
			1.20,
			1.30,
			1.40,
			1.50,
			2.00,
			2.10,
			2.20
		});
		
		ALT_VITALITY_NEVIT_UP_POINT = altSettings.getProperty("AltVitalityNevitUpPoint", 100);
		ALT_VITALITY_NEVIT_POINT = altSettings.getProperty("AltVitalityNevitPoint", 100);
		
		ALT_ALL_PHYS_SKILLS_OVERHIT = altSettings.getProperty("AltAllPhysSkillsOverhit", true);
		ALT_REMOVE_SKILLS_ON_DELEVEL = altSettings.getProperty("AltRemoveSkillsOnDelevel", true);
		ALT_USE_BOW_REUSE_MODIFIER = altSettings.getProperty("AltUseBowReuseModifier", true);
		ALLOW_CH_DOOR_OPEN_ON_CLICK = altSettings.getProperty("AllowChDoorOpenOnClick", true);
		ALT_CH_ALL_BUFFS = altSettings.getProperty("AltChAllBuffs", false);
		ALT_CH_ALLOW_1H_BUFFS = altSettings.getProperty("AltChAllowHourBuff", false);
		ALT_CH_SIMPLE_DIALOG = altSettings.getProperty("AltChSimpleDialog", false);
		
		AUGMENTATION_NG_SKILL_CHANCE = altSettings.getProperty("AugmentationNGSkillChance", 15);
		AUGMENTATION_NG_GLOW_CHANCE = altSettings.getProperty("AugmentationNGGlowChance", 0);
		AUGMENTATION_MID_SKILL_CHANCE = altSettings.getProperty("AugmentationMidSkillChance", 30);
		AUGMENTATION_MID_GLOW_CHANCE = altSettings.getProperty("AugmentationMidGlowChance", 40);
		AUGMENTATION_HIGH_SKILL_CHANCE = altSettings.getProperty("AugmentationHighSkillChance", 45);
		AUGMENTATION_HIGH_GLOW_CHANCE = altSettings.getProperty("AugmentationHighGlowChance", 70);
		AUGMENTATION_TOP_SKILL_CHANCE = altSettings.getProperty("AugmentationTopSkillChance", 60);
		AUGMENTATION_TOP_GLOW_CHANCE = altSettings.getProperty("AugmentationTopGlowChance", 100);
		AUGMENTATION_BASESTAT_CHANCE = altSettings.getProperty("AugmentationBaseStatChance", 1);
		AUGMENTATION_ACC_SKILL_CHANCE = altSettings.getProperty("AugmentationAccSkillChance", 10);
		DISABLE_ACCESSORY_AUGMENTATION = altSettings.getProperty("DisableAccesssoryAugment", false);
		
		ALT_OPEN_CLOAK_SLOT = altSettings.getProperty("OpenCloakSlot", false);
		
		FOLLOW_RANGE = altSettings.getProperty("FollowRange", 100);
		
		ALT_ENABLE_MULTI_PROFA = altSettings.getProperty("AltEnableMultiProfa", false);
		
		ALT_ITEM_AUCTION_ENABLED = altSettings.getProperty("AltItemAuctionEnabled", true);
		ALT_ITEM_AUCTION_CAN_REBID = altSettings.getProperty("AltItemAuctionCanRebid", false);
		ALT_ITEM_AUCTION_START_ANNOUNCE = altSettings.getProperty("AltItemAuctionAnnounce", true);
		ALT_ITEM_AUCTION_BID_ITEM_ID = altSettings.getProperty("AltItemAuctionBidItemId", 57);
		ALT_ITEM_AUCTION_MAX_BID = altSettings.getProperty("AltItemAuctionMaxBid", 1000000L);
		ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS = altSettings.getProperty("AltItemAuctionMaxCancelTimeInMillis", 604800000);
		
		ALT_FISH_CHAMPIONSHIP_ENABLED = altSettings.getProperty("AltFishChampionshipEnabled", true);
		ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = altSettings.getProperty("AltFishChampionshipRewardItemId", 57);
		ALT_FISH_CHAMPIONSHIP_REWARD_1 = altSettings.getProperty("AltFishChampionshipReward1", 800000);
		ALT_FISH_CHAMPIONSHIP_REWARD_2 = altSettings.getProperty("AltFishChampionshipReward2", 500000);
		ALT_FISH_CHAMPIONSHIP_REWARD_3 = altSettings.getProperty("AltFishChampionshipReward3", 300000);
		ALT_FISH_CHAMPIONSHIP_REWARD_4 = altSettings.getProperty("AltFishChampionshipReward4", 200000);
		ALT_FISH_CHAMPIONSHIP_REWARD_5 = altSettings.getProperty("AltFishChampionshipReward5", 100000);
		
		ALT_ENABLE_BLOCK_CHECKER_EVENT = altSettings.getProperty("EnableBlockCheckerEvent", true);
		ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS = Util.constrain(altSettings.getProperty("BlockCheckerMinTeamMembers", 1), 1, 6);
		ALT_RATE_COINS_REWARD_BLOCK_CHECKER = altSettings.getProperty("BlockCheckerRateCoinReward", 1.);
		
		ALT_HBCE_FAIR_PLAY = altSettings.getProperty("HBCEFairPlay", false);
		
		ALT_PET_INVENTORY_LIMIT = altSettings.getProperty("AltPetInventoryLimit", 12);
		ALT_CLAN_LEVEL_CREATE = altSettings.getProperty("ClanLevelCreate", 0);
		CLAN_LEVEL_6_COST = altSettings.getProperty("ClanLevel6Cost", 5000);
		CLAN_LEVEL_7_COST = altSettings.getProperty("ClanLevel7Cost", 10000);
		CLAN_LEVEL_8_COST = altSettings.getProperty("ClanLevel8Cost", 20000);
		CLAN_LEVEL_9_COST = altSettings.getProperty("ClanLevel9Cost", 40000);
		CLAN_LEVEL_10_COST = altSettings.getProperty("ClanLevel10Cost", 40000);
		CLAN_LEVEL_11_COST = altSettings.getProperty("ClanLevel11Cost", 75000);
		CLAN_LEVEL_6_REQUIREMEN = altSettings.getProperty("ClanLevel6Requirement", 30);
		CLAN_LEVEL_7_REQUIREMEN = altSettings.getProperty("ClanLevel7Requirement", 50);
		CLAN_LEVEL_8_REQUIREMEN = altSettings.getProperty("ClanLevel8Requirement", 80);
		CLAN_LEVEL_9_REQUIREMEN = altSettings.getProperty("ClanLevel9Requirement", 120);
		CLAN_LEVEL_10_REQUIREMEN = altSettings.getProperty("ClanLevel10Requirement", 140);
		CLAN_LEVEL_11_REQUIREMEN = altSettings.getProperty("ClanLevel11Requirement", 170);
		BLOOD_OATHS = altSettings.getProperty("BloodOaths", 150);
		BLOOD_PLEDGES = altSettings.getProperty("BloodPledges", 5);
		MIN_ACADEM_POINT = altSettings.getProperty("MinAcademPoint", 190);
		MAX_ACADEM_POINT = altSettings.getProperty("MaxAcademPoint", 650);
		
		VITAMIN_PETS_FOOD_ID = altSettings.getProperty("AltVitaminPetsFoodId", -1);
		
		HELLBOUND_LEVEL = altSettings.getProperty("HellboundLevel", 0);
		HELLBOUND_ENTER_NO_QUEST = altSettings.getProperty("EnterHellboundWithoutQuest", true);
		
		NEVIT_BONUS_TIME = altSettings.getProperty("AltNevitBonusTime", 180);
		
		SIEGE_PVP_COUNT = altSettings.getProperty("SiegePvpCount", false);
		ZONE_PVP_COUNT = altSettings.getProperty("ZonePvpCount", false);
		EPIC_EXPERTISE_PENALTY = altSettings.getProperty("EpicExpertisePenalty", true);
		EXPERTISE_PENALTY = altSettings.getProperty("ExpertisePenalty", true);
		ALT_DISPEL_MUSIC = altSettings.getProperty("AltDispelDanceSong", false);
		ALT_MUSIC_LIMIT = altSettings.getProperty("MusicLimit", 12);
		ALT_DEBUFF_LIMIT = altSettings.getProperty("DebuffLimit", 8);
		ALT_TRIGGER_LIMIT = altSettings.getProperty("TriggerLimit", 12);
		ENABLE_MODIFY_SKILL_DURATION = altSettings.getProperty("EnableSkillDuration", false);
		if (ENABLE_MODIFY_SKILL_DURATION)
		{
			String[] propertySplit = altSettings.getProperty("SkillDurationList", "").split(";");
			SKILL_DURATION_LIST = new TIntIntHashMap(propertySplit.length);
			for (String skill : propertySplit)
			{
				String[] skillSplit = skill.split(",");
				if (skillSplit.length != 2)
					_log.warn("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
				else
				{
					try
					{
						SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!skill.isEmpty())
						{
							_log.warn("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
						}
					}
				}
			}
		}
	}
	
	public static void loadServicesSettings()
	{
		ExProperties servicesSettings = load(SERVICES_FILE);
		
		SERVICES_DELEVEL_ENABLED = servicesSettings.getProperty("AllowDelevel", false);
		SERVICES_DELEVEL_ITEM = servicesSettings.getProperty("DelevelItem", 57);
		SERVICES_DELEVEL_COUNT = servicesSettings.getProperty("DelevelCount", 1000);
		SERVICES_DELEVEL_MIN_LEVEL = servicesSettings.getProperty("DelevelMinLevel", 1);
		
		SERVICES_RIDE_HIRE_ENABLED = servicesSettings.getProperty("RideHireEnabled", false);
		
		SERVICES_CHANGE_NICK_ALLOW_SYMBOL = servicesSettings.getProperty("NickChangeAllowSimbol", false);
		SERVICES_CHANGE_NICK_ENABLED = servicesSettings.getProperty("NickChangeEnabled", false);
		SERVICES_CHANGE_NICK_PRICE = servicesSettings.getProperty("NickChangePrice", 100);
		SERVICES_CHANGE_NICK_ITEM = servicesSettings.getProperty("NickChangeItem", 4037);
		
		SERVICES_CHANGE_CLAN_NAME_ENABLED = servicesSettings.getProperty("ClanNameChangeEnabled", false);
		SERVICES_CHANGE_CLAN_NAME_PRICE = servicesSettings.getProperty("ClanNameChangePrice", 100);
		SERVICES_CHANGE_CLAN_NAME_ITEM = servicesSettings.getProperty("ClanNameChangeItem", 4037);
		
		SERVICES_CHANGE_PET_NAME_ENABLED = servicesSettings.getProperty("PetNameChangeEnabled", false);
		SERVICES_CHANGE_PET_NAME_PRICE = servicesSettings.getProperty("PetNameChangePrice", 100);
		SERVICES_CHANGE_PET_NAME_ITEM = servicesSettings.getProperty("PetNameChangeItem", 4037);
		
		SERVICES_EXCHANGE_BABY_PET_ENABLED = servicesSettings.getProperty("BabyPetExchangeEnabled", false);
		SERVICES_EXCHANGE_BABY_PET_PRICE = servicesSettings.getProperty("BabyPetExchangePrice", 100);
		SERVICES_EXCHANGE_BABY_PET_ITEM = servicesSettings.getProperty("BabyPetExchangeItem", 4037);
		
		SERVICES_CHANGE_SEX_ENABLED = servicesSettings.getProperty("SexChangeEnabled", false);
		SERVICES_CHANGE_SEX_PRICE = servicesSettings.getProperty("SexChangePrice", 100);
		SERVICES_CHANGE_SEX_ITEM = servicesSettings.getProperty("SexChangeItem", 4037);
		
		SERVICES_CHANGE_BASE_ENABLED = servicesSettings.getProperty("BaseChangeEnabled", false);
		SERVICES_CHANGE_BASE_PRICE = servicesSettings.getProperty("BaseChangePrice", 100);
		SERVICES_CHANGE_BASE_ITEM = servicesSettings.getProperty("BaseChangeItem", 4037);
		
		SERVICES_SEPARATE_SUB_ENABLED = servicesSettings.getProperty("SeparateSubEnabled", false);
		SERVICES_SEPARATE_SUB_PRICE = servicesSettings.getProperty("SeparateSubPrice", 100);
		SERVICES_SEPARATE_SUB_ITEM = servicesSettings.getProperty("SeparateSubItem", 4037);
		
		SERVICES_CHANGE_NICK_COLOR_ENABLED = servicesSettings.getProperty("NickColorChangeEnabled", false);
		SERVICES_CHANGE_NICK_COLOR_PRICE = servicesSettings.getProperty("NickColorChangePrice", 100);
		SERVICES_CHANGE_NICK_COLOR_ITEM = servicesSettings.getProperty("NickColorChangeItem", 4037);
		
		SERVICES_CHANGE_TITLE_COLOR_ENABLED = servicesSettings.getProperty("TitleColorChangeEnabled", false);
		SERVICES_CHANGE_TITLE_COLOR_PRICE = servicesSettings.getProperty("TitleColorChangePrice", 100);
		SERVICES_CHANGE_TITLE_COLOR_ITEM = servicesSettings.getProperty("TitleColorChangeItem", 4037);
		
		SERVICES_BASH_ENABLED = servicesSettings.getProperty("BashEnabled", false);
		SERVICES_BASH_SKIP_DOWNLOAD = servicesSettings.getProperty("BashSkipDownload", false);
		SERVICES_BASH_RELOAD_TIME = servicesSettings.getProperty("BashReloadTime", 24);
		
		SERVICES_NOBLESS_SELL_ENABLED = servicesSettings.getProperty("NoblessSellEnabled", false);
		SERVICES_NOBLESS_SELL_PRICE = servicesSettings.getProperty("NoblessSellPrice", 1000);
		SERVICES_NOBLESS_SELL_ITEM = servicesSettings.getProperty("NoblessSellItem", 4037);
		
		SERVICES_HERO_SELL_ENABLED = servicesSettings.getProperty("HeroSellEnabled", false);
		SERVICES_HERO_SELL_DAY = servicesSettings.getProperty("HeroSellDay", new int[]
		{
			30
		});
		SERVICES_HERO_SELL_PRICE = servicesSettings.getProperty("HeroSellPrice", new int[]
		{
			30
		});
		SERVICES_HERO_SELL_ITEM = servicesSettings.getProperty("HeroSellItem", new int[]
		{
			4037
		});
		
		SERVICES_WASH_PK_ENABLED = servicesSettings.getProperty("WashPkEnabled", false);
		SERVICES_WASH_PK_ITEM = servicesSettings.getProperty("WashPkItem", 4037);
		SERVICES_WASH_PK_PRICE = servicesSettings.getProperty("WashPkPrice", 5);
		
		SERVICES_EXPAND_INVENTORY_ENABLED = servicesSettings.getProperty("ExpandInventoryEnabled", false);
		SERVICES_EXPAND_INVENTORY_PRICE = servicesSettings.getProperty("ExpandInventoryPrice", 1000);
		SERVICES_EXPAND_INVENTORY_ITEM = servicesSettings.getProperty("ExpandInventoryItem", 4037);
		SERVICES_EXPAND_INVENTORY_MAX = servicesSettings.getProperty("ExpandInventoryMax", 250);
		
		SERVICES_EXPAND_WAREHOUSE_ENABLED = servicesSettings.getProperty("ExpandWarehouseEnabled", false);
		SERVICES_EXPAND_WAREHOUSE_PRICE = servicesSettings.getProperty("ExpandWarehousePrice", 1000);
		SERVICES_EXPAND_WAREHOUSE_ITEM = servicesSettings.getProperty("ExpandWarehouseItem", 4037);
		SERVICES_EXPAND_WAREHOUSE_MAX = servicesSettings.getProperty("ExpandWarehouseMax", 250);
		
		SERVICES_EXPAND_CWH_ENABLED = servicesSettings.getProperty("ExpandCWHEnabled", false);
		SERVICES_EXPAND_CWH_PRICE = servicesSettings.getProperty("ExpandCWHPrice", 1000);
		SERVICES_EXPAND_CWH_ITEM = servicesSettings.getProperty("ExpandCWHItem", 4037);
		
		SERVICES_ALLOW_LOTTERY = servicesSettings.getProperty("AllowLottery", false);
		SERVICES_LOTTERY_PRIZE = servicesSettings.getProperty("LotteryPrize", 50000);
		SERVICES_LOTTERY_TICKET_PRICE = servicesSettings.getProperty("LotteryTicketPrice", 2000);
		SERVICES_LOTTERY_5_NUMBER_RATE = servicesSettings.getProperty("Lottery5NumberRate", 1.1);
		SERVICES_LOTTERY_4_NUMBER_RATE = servicesSettings.getProperty("Lottery4NumberRate", 4.);
		SERVICES_LOTTERY_3_NUMBER_RATE = servicesSettings.getProperty("Lottery3NumberRate", 6.);
		SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE = servicesSettings.getProperty("Lottery2and1NumberPrize", 20.);
		
		SERVICES_LOTTERY_STARTING_DATE = servicesSettings.getProperty("LotteryDate", new int[]
		{
			1,
			19
		});
		
		SERVICES_ALLOW_ROULETTE = servicesSettings.getProperty("AllowRoulette", false);
		SERVICES_ROULETTE_MIN_BET = servicesSettings.getProperty("RouletteMinBet", 1L);
		SERVICES_ROULETTE_MAX_BET = servicesSettings.getProperty("RouletteMaxBet", Long.MAX_VALUE);
		
		SERVICES_PK_PVP_KILL_ENABLE = servicesSettings.getProperty("PkPvPKillEnable", false);
		SERVICES_PVP_KILL_REWARD_ITEM = servicesSettings.getProperty("PvPkillRewardItem", 4037);
		SERVICES_PVP_KILL_REWARD_COUNT = servicesSettings.getProperty("PvPKillRewardCount", 1L);
		SERVICES_PK_KILL_REWARD_ITEM = servicesSettings.getProperty("PkkillRewardItem", 4037);
		SERVICES_PK_KILL_REWARD_COUNT = servicesSettings.getProperty("PkKillRewardCount", 1L);
		SERVICES_PK_PVP_TIE_IF_SAME_IP = servicesSettings.getProperty("PkPvPTieifSameIP", true);
		
		ITEM_BROKER_ITEM_SEARCH = servicesSettings.getProperty("UseItemBrokerItemSearch", false);
		
		SERVICES_LVL_ENABLED = servicesSettings.getProperty("LevelChangeEnabled", false);
		SERVICES_LVL_UP_MAX = servicesSettings.getProperty("LevelUPChangeMax", 85);
		SERVICES_LVL_UP_ITEM = servicesSettings.getProperty("LevelUPChangeItem", 4037);
		
		SERVICES_LVL_79_85_PRICE = servicesSettings.getProperty("Levelfrom79to85", 1000);
		SERVICES_LVL_1_85_PRICE = servicesSettings.getProperty("Levelfrom1to85", 1500);
		
		SERVICES_CLAN_REP_POINTS = servicesSettings.getProperty("EnableClanRepService", false);
		SERVICE_CLAN_REP_ITEM = servicesSettings.getProperty("ClanRepItem", 13693);
		SERVICE_CLAN_REP_COST= servicesSettings.getProperty("ClanReptCost", 1000);
		SERVICE_CLAN_REP_ADD = servicesSettings.getProperty("AddReputation", 1);
	}
	
	public static void loadCommandssettings()
	{
		ExProperties CommandsSettings = load(COMMANDS_CONFIG_FILE);
		
		DISABLE_VOICED_COMMANDS = CommandsSettings.getProperty("DisableVoicedCommands", false);
		ENABLE_KM_ALL_TO_ME = CommandsSettings.getProperty("EnableKmAllToMe", false);
		ALLOW_ONLINE_COMMAND = CommandsSettings.getProperty("AllowVoiceCommandOnline", false);
		ALT_SHOW_SERVER_TIME = CommandsSettings.getProperty("ShowServerTime", false);
		ENABLE_CASTLE_COMMAND = CommandsSettings.getProperty("EnableCastleInfo", false);
		ENABLE_CFG_COMMAND = CommandsSettings.getProperty("EnableCfg", false);
		ENABLE_CLAN_WAREHOUSE_COMMAND = CommandsSettings.getProperty("EnableClanWH", false);
		ENABLE_HELLBOUND_VOICED = CommandsSettings.getProperty("AllowHellboundStatus", false);
		ENABLE_HELP_COMMAND = CommandsSettings.getProperty("EnableHelp", false);
		ENABLE_PASSWORD_COMMAND = CommandsSettings.getProperty("AllowPasswordChange", false);
		ENABLE_PING_COMMAND = CommandsSettings.getProperty("EnablePing", false);
		ENABLE_SC_INFO_COMMAND = CommandsSettings.getProperty("AllowSCInfo", false);
		PARTY_SEARCH_COMMANDS = CommandsSettings.getProperty("AllowPartyFind", false);
		ENABLE_NPCSPAWN_COMMAND = CommandsSettings.getProperty("EnableNpcspawn", false);
		ENABLE_WHEREIS_COMMAND = CommandsSettings.getProperty("EnableWhereis", false);
		ENABLE_COMBINE_TALISMAN_COMMAND = CommandsSettings.getProperty("AllowCombineTalisman", false);
		ENABLE_OPENATOD_COMMAND = CommandsSettings.getProperty("EnableOpenATOD", false);
		ENABLE_TRADELIST_VOICE = CommandsSettings.getProperty("EnableTradelistVoice", false);
		ENABLE_EXP_COMMAND = CommandsSettings.getProperty("EnableExpCheck", false);
		ENABLE_REPAIR_COMMAND = CommandsSettings.getProperty("EnableRepair", false);
		ALLOW_LOCK_COMMAND = CommandsSettings.getProperty("AllowLockService", false);
		ALLOW_WHOAMI_COMMAND = CommandsSettings.getProperty("AllowWhoami", false);
	}
	
	public static void loadCommunityboardsettings()
	{
		ExProperties CommunityboardSettings = load(BOARD_MANAGER_CONFIG_FILE);
		
		ENABLE_NEW_CLAN_CB = CommunityboardSettings.getProperty("EnableNewClanBoard", false);
		ENABLE_OLD_CLAN_BOARD = CommunityboardSettings.getProperty("EnableOLDClanBoard", true);
		
		ENABLE_NEW_FRIENDS_BOARD = CommunityboardSettings.getProperty("EnableNewFriendsBoard", false);
		ENABLE_RETAIL_FRIENDS_BOARD = CommunityboardSettings.getProperty("EnableRetailFriendsBoard", false);
		ENABLE_MEMO_BOARD = CommunityboardSettings.getProperty("EnableMemoBoard", false);
		ENABLE_NEW_MAIL_MANAGER = CommunityboardSettings.getProperty("EnableBoardNewMailManager", false);
		ENABLE_OLD_MAIL_MANAGER = CommunityboardSettings.getProperty("EnableBoardOldMailManager", false);
		
		COMMUNITYBOARD_ENABLED = CommunityboardSettings.getProperty("AllowCommunityBoard", true);
		BBS_DEFAULT = CommunityboardSettings.getProperty("BBSDefault", "_bbshome");
		BBS_HOME_DIR = CommunityboardSettings.getProperty("BBSHomeDir", "scripts/services/community/");
		ALLOW_BBS_WAREHOUSE = CommunityboardSettings.getProperty("AllowBBSWarehouse", true);
		BBS_WAREHOUSE_ALLOW_PK = CommunityboardSettings.getProperty("BBSWarehouseAllowPK", false);
		
		COMMUNITY_DROP_LIST = CommunityboardSettings.getProperty("EnableCommunityDropList", false);
		COMMUNITY_ITEM_INFO = CommunityboardSettings.getProperty("EnableCommunityItemInfo", false);
		
		SERVICES_BBSMERCENARIES = CommunityboardSettings.getProperty("BBSMercenaries", false);
		MERCENARIES_ITEM = CommunityboardSettings.getProperty("MercenariesItem", 57);
		MERCENARIES_ITEM_COUNT = CommunityboardSettings.getProperty("MercenariesItemCount", 300000);
		MERCENARIES_ITEM_HIDE_COUNT = CommunityboardSettings.getProperty("MercenariesItemHideCount", 900000);
		MERCENARIES_ANNOUNCE = CommunityboardSettings.getProperty("MercenariesAnnounce", false);
		
		ALLOW_BSS_RAIDBOSS = CommunityboardSettings.getProperty("EnableBossStatus", false);
		BOSSES_TO_NOT_SHOW = CommunityboardSettings.getProperty("BossIdNotBeShow", new int[]
		{
			29006, 29001
		});
		
		ALLOW_COMMUNITY_CLAN_SKILLS_SELLER = CommunityboardSettings.getProperty("EnableClanSkillsSeller", false);
		COMMUNITY_CLAN_SKILL_SELLER_ITEM = CommunityboardSettings.getProperty("ClanSKillsItemId", 57);
		
		ALLOW_CB_AUGMENTATION = CommunityboardSettings.getProperty("EnableCommunityAugmentation", false);
		COMMUNITY_AUGMENTATION_MIN_LEVEL = CommunityboardSettings.getProperty("MinLevelToAugment", 46);
		COMMUNITY_AUGMENTATION_ALLOW_JEWELRY = CommunityboardSettings.getProperty("AllowJewelryyAugmentation", false);
		
		COMUMNITY_ALLOW_BUY = CommunityboardSettings.getProperty("CommunityShopEnable", false);
		COMMUNITY_ALLOW_SELL = CommunityboardSettings.getProperty("CommunitySellEnable", false);
		COMMUNITY_ALLOW_AUGMENT = CommunityboardSettings.getProperty("CommunityAugmentEnable", false);
		
		ENABLE_CUSTOM_AUCTION = CommunityboardSettings.getProperty("EnableCustomAuction", false);
		
		ENABLE_COMMUNITY_RANKING = CommunityboardSettings.getProperty("EnableCommunityRanking", true);
		ENABLE_PLAYER_COUNTERS = CommunityboardSettings.getProperty("EnablePlayerCounters", true);
		PLAYER_COUNTERS_REFRESH = CommunityboardSettings.getProperty("PlayerRankingRefresh", 5);
		PLAYER_TOP_SORT_BY_LASTACCESS = CommunityboardSettings.getProperty("SortRankingByLastAccess", false);
		PLAYER_TOP_MONTHLY_RANKING = CommunityboardSettings.getProperty("SeasonRanking", false);
		
		ENABLE_DONATE_PAGE = CommunityboardSettings.getProperty("EnableDonationPage", true);
		
		CB_NPC_GATEKEEPER_ID = CommunityboardSettings.getProperty("CommunityGateKeeperNPC", 402);
		CB_NPC_GMSHOP_ID = CommunityboardSettings.getProperty("CommunityShopNPC", 403);
		CB_NPC_BUFFER_ID = CommunityboardSettings.getProperty("CommunityBufferNPC", 404);
		CB_NPC_AUCTION_ID = CommunityboardSettings.getProperty("CommunityAuctionNPC", 405);
		CB_NPC_RBSTATUS_ID = CommunityboardSettings.getProperty("CommunityRBStatusNPC", 406);
		CB_NPC_CLASS_MASTER_ID = CommunityboardSettings.getProperty("CommunityClassMasterNPC", 31860);
		
		ENABLE_COMMUNITY_ACADEMY = CommunityboardSettings.getProperty("EnableAcademyBoard", false);
		SERVICES_ACADEMY_REWARD = CommunityboardSettings.getProperty("AcademyRewards", "57");
		ACADEMY_MIN_ADENA_AMOUNT = CommunityboardSettings.getProperty("MinAcademyPrice", 1);
		ACADEMY_MAX_ADENA_AMOUNT = CommunityboardSettings.getProperty("MaxAcademyPrice", 1000000000);
		MAX_TIME_IN_ACADEMY = CommunityboardSettings.getProperty("KickAcademyAfter", 259200000);
		ACADEMY_INVITE_DELAY = CommunityboardSettings.getProperty("InviteDelay", 5);
		
		BBS_RECRUITMENT_ALLOW = CommunityboardSettings.getProperty("Allow", false);
		BBS_RECRUITMENT_CLAN_DESCRIPTION_MIN = CommunityboardSettings.getProperty("Description", 10);
		BBS_RECRUITMENT_TIME = CommunityboardSettings.getProperty("Time", new int[] { 2, 3, 5, 6 });
		BBS_RECRUITMENT_ITEMS = CommunityboardSettings.getProperty("Items", new int[] { 57, 4036, 4356 });
		
		BBS_GAME_LOTTERY_ALLOW = CommunityboardSettings.getProperty("AllowCommunityLottery", false);
		BBS_GAME_LOTTERY_ITEM = CommunityboardSettings.getProperty("ItemId", 57);
		BBS_GAME_LOTTERY_BET = CommunityboardSettings.getProperty("Bets", new int[] { 1000, 4000, 50000, 250000, 1000000, 5000000 });
		BBS_GAME_LOTTERY_WIN_CHANCE = CommunityboardSettings.getProperty("LotteryWinChance", 20.1);
		BBS_GAME_LOTTERY_JACKPOT_CHANCE = CommunityboardSettings.getProperty("JackpotWinChance", 0.01);
		BBS_GAME_LOTTERY_INITIAL_JACKPOT = CommunityboardSettings.getProperty("JackpotReset", 10000);
		BBS_GAME_LOTTERY_AMOUNT_PERCENT_TO_JACKPOT = CommunityboardSettings.getProperty("PercentToJackpot", 10);
		BBS_GAME_LOTTERY_REWARD_MULTIPLER = CommunityboardSettings.getProperty("RewardMultiplier", 4);
		BBS_GAME_LOTTERY_STORE_DATA = CommunityboardSettings.getProperty("LotterySaveData", 1) * 60000;
		
		COMMUNITY_FORGE_ENABLED = CommunityboardSettings.getProperty("EnableCommunityForge", false);
		BBS_ENCHANT_ITEM = CommunityboardSettings.getProperty("Item", 4356);
		BBS_ENCHANT_MAX = CommunityboardSettings.getProperty("MaxEnchant", new int[] { 25 });
		BBS_WEAPON_ENCHANT_LVL = CommunityboardSettings.getProperty("WValue", new int[] { 5 });
		BBS_ARMOR_ENCHANT_LVL = CommunityboardSettings.getProperty("AValue", new int[] { 5 });
		BBS_JEWELS_ENCHANT_LVL = CommunityboardSettings.getProperty("JValue", new int[] { 5 });
		BBS_ENCHANT_PRICE_WEAPON = CommunityboardSettings.getProperty("WPrice", new int[] { 5 });
		BBS_ENCHANT_PRICE_ARMOR = CommunityboardSettings.getProperty("APrice", new int[] { 5 });
		BBS_ENCHANT_PRICE_JEWELS = CommunityboardSettings.getProperty("JPrice", new int[] { 5 });
		BBS_ENCHANT_ATRIBUTE_LVL_WEAPON = CommunityboardSettings.getProperty("AtributeWeaponValue", new int[] { 25 });
		BBS_ENCHANT_ATRIBUTE_PRICE_WEAPON = CommunityboardSettings.getProperty("PriceForAtributeWeapon", new int[] { 25 });
		BBS_ENCHANT_ATRIBUTE_LVL_ARMOR = CommunityboardSettings.getProperty("AtributeArmorValue", new int[] { 25 });
		BBS_ENCHANT_ATRIBUTE_PRICE_ARMOR = CommunityboardSettings.getProperty("PriceForAtributeArmor", new int[] { 25 });
		BBS_ENCHANT_ATRIBUTE_PVP = CommunityboardSettings.getProperty("AtributePvP", true);
		BBS_ENCHANT_WEAPON_ATTRIBUTE_MAX = CommunityboardSettings.getProperty("MaxWAttribute", 25);
		BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX = CommunityboardSettings.getProperty("MaxAAttribute", 25);

		BBS_ENCHANT_HEAD_ATTRIBUTE = CommunityboardSettings.getProperty("AtributeHead", true);
		BBS_ENCHANT_CHEST_ATTRIBUTE = CommunityboardSettings.getProperty("AtributeChest", true);
		BBS_ENCHANT_LEGS_ATTRIBUTE = CommunityboardSettings.getProperty("AtributeLegs", true);
		BBS_ENCHANT_GLOVES_ATTRIBUTE = CommunityboardSettings.getProperty("AtributeGloves", true);
		BBS_ENCHANT_FEET_ATTRIBUTE = CommunityboardSettings.getProperty("AtributeFeet", true);

		BBS_ENCHANT_WEAPON_ATTRIBUTE = CommunityboardSettings.getProperty("AtributeWeapon", true);
		BBS_ENCHANT_SHIELD_ATTRIBUTE = CommunityboardSettings.getProperty("AtributeShield", false);
		BBS_ENCHANT_GRADE_ATTRIBUTE = CommunityboardSettings.getProperty("AtributeGrade", "NG:NO;D:NO;C:NO;B:NO;A:ON;S:ON;S80:ON;S84:ON").trim().replaceAll(" ", "").split(";");
	}
	
	public static void loadCommunityboardComission()
	{
		ExProperties CommunityboardComission = load(CB_COMISSION_CONFIG_FILE);
		
		COMMUNITY_COMMISSION_ALLOW = CommunityboardComission.getProperty("CommunityCommissionAllow", false);
		COMMUNITY_COMMISSION_ARMOR_PRICE = CommunityboardComission.getProperty("CommunityCommissionArmorPrice", new int[] { 1 });
		COMMUNITY_COMMISSION_WEAPON_PRICE = CommunityboardComission.getProperty("CommunityCommissionWeaponPrice", new int[] { 1 });
		COMMUNITY_COMMISSION_JEWERLY_PRICE = CommunityboardComission.getProperty("CommunityCommissionJewerlyPrice", new int[] { 1 });
		COMMUNITY_COMMISSION_OTHER_PRICE = CommunityboardComission.getProperty("CommunityCommissionOtherPrice", new int[] { 1 });
		COMMUNITY_COMMISSION_ALLOW_ITEMS = CommunityboardComission.getProperty("CommunityCommissionAllowItems", new int[] { 1 });
		COMMUNITY_COMMISSION_MAX_ENCHANT = CommunityboardComission.getProperty("CommunityCommissionMaxEnchant", 24);
		COMMUNITY_COMMISSION_NOT_ALLOW_ITEMS = CommunityboardComission.getProperty("CommunityCommissionNotAllowItems", new int[] { 1 });
		COMMUNITY_COMMISSION_COUNT_TO_PAGE = CommunityboardComission.getProperty("CommunityCommissionCountToPage", 24);
		COMMUNITY_COMMISSION_MONETS = CommunityboardComission.getProperty("CommunityCommissionMonets", new int[] { 1 });
		COMMUNITY_COMMISSION_ALLOW_UNDERWEAR = CommunityboardComission.getProperty("CommunityCommissionAllowUnderwear", false);
		COMMUNITY_COMMISSION_ALLOW_CLOAK = CommunityboardComission.getProperty("CommunityCommissionAllowCloak", false);
		COMMUNITY_COMMISSION_ALLOW_BRACELET = CommunityboardComission.getProperty("CommunityCommissionAllowBracelet", false);
		COMMUNITY_COMMISSION_ALLOW_AUGMENTED = CommunityboardComission.getProperty("CommunityCommissionAllowAugmented", false);
		COMMUNITY_COMMISSION_ALLOW_EQUIPPED = CommunityboardComission.getProperty("CommunityCommissionAllowEquipped", false);
		COMMUNITY_COMMISSION_SAVE_DAYS = CommunityboardComission.getProperty("CommunityCommissionSaveDays", 30);
	}
	
	public static void loadCommunitybuffersettings()
	{
		ExProperties CommunitybufferSettings = load(BUFFER_MANAGER_CONFIG_FILE);
		
		ENABLE_SCHEME_BUFFER = CommunitybufferSettings.getProperty("EnableSchemeBuffer", false);
		MAX_SCHEME_PROFILES = CommunitybufferSettings.getProperty("MaxSchemeProfiles", 4);
		MAX_BUFFS_PER_PROFILE = CommunitybufferSettings.getProperty("MaxSchemeBuffsPerProfile", 24);
		PRICE_PER_BUFF = CommunitybufferSettings.getProperty("BuffPrice", 0);
		BUFFS_TIME = CommunitybufferSettings.getProperty("BuffDuration", 180);
		BUFFER_MIN_LEVEL = CommunitybufferSettings.getProperty("BufferMinLevel", 1);
		BUFFER_MAX_LEVEL = CommunitybufferSettings.getProperty("BufferMaxLevel", 99);
		ALLOW_BUFF_FOR_MY_CLASS = CommunitybufferSettings.getProperty("AllowBuffForMyClass", true);
		ALLOW_BUFFER_HEAL = CommunitybufferSettings.getProperty("AllowHeal", true);
		ALLOW_CANCEL_BUFFS = CommunitybufferSettings.getProperty("AllowCancel", true);
		ALLOW_BUFFER_NOBLE = CommunitybufferSettings.getProperty("AllowNobless", true);
		ALLOW_BUFFER_UP_BUFF = CommunitybufferSettings.getProperty("AllowUltimatePetBuff", true);
		
		BBS_BUFFER_ENABLED = CommunitybufferSettings.getProperty("AllowBBSBuffer", false);
		BBS_BUFF_DEATH = CommunitybufferSettings.getProperty("AllowWhenDead", false);
		BBS_BUFF_ACTION = CommunitybufferSettings.getProperty("AllowWhenInAction", false);
		BBS_BUFF_OLY = CommunitybufferSettings.getProperty("AllowWhenInOlly", false);
		BBS_BUFF_FLY = CommunitybufferSettings.getProperty("AllowWhenInFly", false);
		BBS_BUFF_VEICHLE = CommunitybufferSettings.getProperty("AllowWhenInVeichle", false);
		BBS_BUFF_MOUNTED = CommunitybufferSettings.getProperty("AllowWhenMounted", false);
		BBS_BUFF_CANT_MOVE = CommunitybufferSettings.getProperty("AllowWhenCantMove", false);
		BBS_BUFF_STORE_MODE = CommunitybufferSettings.getProperty("AllowWhenInTrade", false);
		BBS_BUFF_FISHING = CommunitybufferSettings.getProperty("AllowWhenFishing", false);
		BBS_BUFF_TEMP_ACTION = CommunitybufferSettings.getProperty("AllowWhenInTemp", false);
		BBS_BUFF_DUEL = CommunitybufferSettings.getProperty("AllowWhenInDuel", false);
		BBS_BUFF_CURSED = CommunitybufferSettings.getProperty("AllowWhenUseCursed", false);
		BBS_BUFF_PK = CommunitybufferSettings.getProperty("AllowWhenIsPk", false);
		BBS_BUFF_LEADER = CommunitybufferSettings.getProperty("AllowOnlyToClanLeader", false);
		BBS_BUFF_NOBLE = CommunitybufferSettings.getProperty("AllowOnlyToNoble", false);
		BBS_BUFF_TERITORY = CommunitybufferSettings.getProperty("AllowUseInTWPlayer", false);
		BBS_BUFF_PEACEZONE_ONLY = CommunitybufferSettings.getProperty("AllowUseOnlyInPeace", false);
		BBS_BUFF_IDs = CommunitybufferSettings.getProperty("BuffIDs", ArrayUtils.EMPTY_INT_ARRAY);
		BBS_BUFF_ALLOW_CANCEL = CommunitybufferSettings.getProperty("BuffAllowCancel", false);
		BBS_BUFF_ALLOW_HEAL = CommunitybufferSettings.getProperty("BuffAllowHeal", false);
		BUFF_MANUAL_EDIT_SETS = CommunitybufferSettings.getProperty("BuffManualEditSets", false);
		MAX_SETS_PER_CHAR = CommunitybufferSettings.getProperty("MaximumSetsPerChar", 8);
		BBS_BUFF_ITEM_COUNT = CommunitybufferSettings.getProperty("BuffItemCount", 8);
		BBS_BUFF_FREE_LVL = CommunitybufferSettings.getProperty("FreeBuffLevel", 8);
		MAX_BUFF_PER_SET = CommunitybufferSettings.getProperty("MaxBuffsPerSet", 8);
		BUFF_PAGE_ROWS = CommunitybufferSettings.getProperty("BuffsPageRows", 8);
		BBS_BUFF_ITEM_ID = CommunitybufferSettings.getProperty("BuffItemId", 8);
		BBS_BUFF_TIME_MUSIC = CommunitybufferSettings.getProperty("BuffTimeMusic", 8);
		BBS_BUFF_TIME_SPECIAL = CommunitybufferSettings.getProperty("BuffTimeSpecial", 8);
		BBS_BUFF_TIME = CommunitybufferSettings.getProperty("BuffTime", 8);
		BBS_BUFF_TIME_MOD = CommunitybufferSettings.getProperty("BuffTimeMod", 8);
		BBS_BUFF_TIME_MOD_MUSIC = CommunitybufferSettings.getProperty("BuffTimeModMusic", 8);
		BBS_BUFF_TIME_MOD_SPECIAL = CommunitybufferSettings.getProperty("BuffTimeModSpecial", 8);
		
	}
	
	public static void loadCommunityclasssettings()
	{
		ExProperties CommunityClassSettings = load(CLASS_MASTER_CONFIG_FILE);
		
		BOARD_ENABLE_CLASS_MASTER = CommunityClassSettings.getProperty("EnableBoardClassMaster", false);
		
		CLASS_MASTER_NPC = CommunityClassSettings.getProperty("ClassMasterNpc", false);
		
		for (int id : CommunityClassSettings.getProperty("AllowClassMasters", ArrayUtils.EMPTY_INT_ARRAY))
			if (id != 0)
				ALLOW_CLASS_MASTERS_LIST.add(id);
		
		CLASS_MASTERS_PRICE = CommunityClassSettings.getProperty("ClassMastersPrice", "1000,10000,100000");
		if (CLASS_MASTERS_PRICE.length() >= 5)
		{
			int level = 1;
			for (String id : CLASS_MASTERS_PRICE.split(","))
			{
				CLASS_MASTERS_PRICE_LIST[level] = Integer.parseInt(id);
				level++;
			}
		}
		
		CLASS_MASTERS_PRICE_ITEM = CommunityClassSettings.getProperty("ClassMastersPriceItem", "57,57,57");
		if (CLASS_MASTERS_PRICE_ITEM.length() >= 5)
		{
			int level = 1;
			for (String id : CLASS_MASTERS_PRICE_ITEM.split(","))
			{
				CLASS_MASTERS_PRICE_ITEM_LIST[level] = Integer.parseInt(id);
				level++;
			}
		}
		
		CLASS_MASTERS_SUB_ITEM = CommunityClassSettings.getProperty("ClassMasterSubItem", 57);
		CLASS_MASTERS_SUB_PRICE = CommunityClassSettings.getProperty("ClassMasterSubPrice", 1000);
		
		BBS_PVP_SUB_MANAGER_ALLOW = CommunityClassSettings.getProperty("AllowBBSSubManager", false);
		BBS_PVP_SUB_MANAGER_PEACE_ZONE = CommunityClassSettings.getProperty("AllowBBSSubManagerPiace", false);
	}
	
	public static void loadCommunityteleportsettings()
	{
		ExProperties Communityteleportsettings = load(TELEPORT_MANAGER_CONFIG_FILE);
		
		COMMUNITY_TELEPORT_ENABLED = Communityteleportsettings.getProperty("EnableCommunityGateKeeper", false);
		COMMUNITY_TELEPORT_GLOBAL_PRICE = Communityteleportsettings.getProperty("GlobalTeleportPrice", 200000);
		COMMUNITY_TELEPORT_BOOKMARK_PRICE = Communityteleportsettings.getProperty("CommunityBookmarkCost", 200000);
		COMMUNITY_BOOKMARK_MAX = Communityteleportsettings.getProperty("CommunityMaxBookmarks", 10);
		COMMUNITY_TELEPORT_DURING_SIEGES = Communityteleportsettings.getProperty("TeleportDuringSieges", false);
	}
	
	public static void loadPvPSettings()
	{
		ExProperties pvpSettings = load(PVP_CONFIG_FILE);
		
		ANTIFEED_ENABLE = pvpSettings.getProperty("AntiFeedEnable", true);
		ANTIFEED_DUALBOX = pvpSettings.getProperty("AntiFeedDualbox", true);
		ANTIFEED_INTERVAL = pvpSettings.getProperty("AntiFeedInterval", 120);
		
		ENABLE_PVP_PK_LOG = pvpSettings.getProperty("EnablePvPpkLog", false);
		
		/* KARMA SYSTEM */
		KARMA_MIN_KARMA = pvpSettings.getProperty("MinKarma", 240);
		KARMA_SP_DIVIDER = pvpSettings.getProperty("SPDivider", 7);
		KARMA_LOST_BASE = pvpSettings.getProperty("BaseKarmaLost", 0);
		
		KARMA_NEEDED_TO_DROP = pvpSettings.getProperty("KarmaNeededToDrop", true);
		DROP_ITEMS_ON_DIE = pvpSettings.getProperty("DropOnDie", false);
		DROP_ITEMS_AUGMENTED = pvpSettings.getProperty("DropAugmented", false);
		
		KARMA_DROP_ITEM_LIMIT = pvpSettings.getProperty("MaxItemsDroppable", 10);
		MIN_PK_TO_ITEMS_DROP = pvpSettings.getProperty("MinPKToDropItems", 5);
		
		KARMA_RANDOM_DROP_LOCATION_LIMIT = pvpSettings.getProperty("MaxDropThrowDistance", 70);
		
		KARMA_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfPKDropBase", 20.);
		KARMA_DROPCHANCE_MOD = pvpSettings.getProperty("ChanceOfPKsDropMod", 1.);
		NORMAL_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfNormalDropBase", 1.);
		DROPCHANCE_EQUIPPED_WEAPON = pvpSettings.getProperty("ChanceOfDropWeapon", 3);
		DROPCHANCE_EQUIPMENT = pvpSettings.getProperty("ChanceOfDropEquippment", 17);
		DROPCHANCE_ITEM = pvpSettings.getProperty("ChanceOfDropOther", 80);
		
		KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();
		for (int id : pvpSettings.getProperty("ListOfNonDroppableItems", new int[]
		{
			57,
			1147,
			425,
			1146,
			461,
			10,
			2368,
			7,
			6,
			2370,
			2369,
			3500,
			3501,
			3502,
			4422,
			4423,
			4424,
			2375,
			6648,
			6649,
			6650,
			6842,
			6834,
			6835,
			6836,
			6837,
			6838,
			6839,
			6840,
			5575,
			7694,
			6841,
			8181
		}))
			KARMA_LIST_NONDROPPABLE_ITEMS.add(id);
		
		PVP_TIME = pvpSettings.getProperty("PvPTime", 120000);
	}
	
	public static void loadAISettings()
	{
		ExProperties aiSettings = load(AI_CONFIG_FILE);
		
		AI_TASK_MANAGER_COUNT = aiSettings.getProperty("AiTaskManagers", 1);
		AI_TASK_ATTACK_DELAY = aiSettings.getProperty("AiTaskDelay", 1000);
		AI_TASK_ACTIVE_DELAY = aiSettings.getProperty("AiTaskActiveDelay", 1000);
		BLOCK_ACTIVE_TASKS = aiSettings.getProperty("BlockActiveTasks", false);
		ALWAYS_TELEPORT_HOME = aiSettings.getProperty("AlwaysTeleportHome", false);
		
		RND_WALK = aiSettings.getProperty("RndWalk", true);
		RND_WALK_RATE = aiSettings.getProperty("RndWalkRate", 1);
		RND_ANIMATION_RATE = aiSettings.getProperty("RndAnimationRate", 2);
		
		AGGRO_CHECK_INTERVAL = aiSettings.getProperty("AggroCheckInterval", 250);
		NONAGGRO_TIME_ONTELEPORT = aiSettings.getProperty("NonAggroTimeOnTeleport", 15000);
		MAX_DRIFT_RANGE = aiSettings.getProperty("MaxDriftRange", 100);
		MAX_PURSUE_RANGE = aiSettings.getProperty("MaxPursueRange", 4000);
		MAX_PURSUE_UNDERGROUND_RANGE = aiSettings.getProperty("MaxPursueUndergoundRange", 2000);
		MAX_PURSUE_RANGE_RAID = aiSettings.getProperty("MaxPursueRangeRaid", 5000);
	}
	
	public static void loadGeodataSettings()
	{
		ExProperties geodataSettings = load(GEODATA_CONFIG_FILE);
		
		GEO_X_FIRST = geodataSettings.getProperty("GeoFirstX", 11);
		GEO_Y_FIRST = geodataSettings.getProperty("GeoFirstY", 10);
		GEO_X_LAST = geodataSettings.getProperty("GeoLastX", 26);
		GEO_Y_LAST = geodataSettings.getProperty("GeoLastY", 26);
		
		GEOFILES_PATTERN = geodataSettings.getProperty("GeoFilesPattern", "(\\d{2}_\\d{2})\\.l2j");
		ALLOW_GEODATA = geodataSettings.getProperty("AllowGeodata", true);
		ALLOW_WATER = geodataSettings.getProperty("AllowWater", true);
		DAMAGE_FROM_FALLING = geodataSettings.getProperty("DamageFromFalling", true);
		
		ALLOW_FALL_FROM_WALLS = geodataSettings.getProperty("AllowFallFromWalls", false);
		ALLOW_KEYBOARD_MOVE = geodataSettings.getProperty("AllowMoveWithKeyboard", true);
		COMPACT_GEO = geodataSettings.getProperty("CompactGeoData", false);
		CLIENT_Z_SHIFT = geodataSettings.getProperty("ClientZShift", 16);
		PATHFIND_BOOST = geodataSettings.getProperty("PathFindBoost", 2);
		PATHFIND_DIAGONAL = geodataSettings.getProperty("PathFindDiagonal", true);
		PATHFIND_MAP_MUL = geodataSettings.getProperty("PathFindMapMul", 2);
		PATH_CLEAN = geodataSettings.getProperty("PathClean", true);
		PATHFIND_MAX_Z_DIFF = geodataSettings.getProperty("PathFindMaxZDiff", 32);
		MAX_Z_DIFF = geodataSettings.getProperty("MaxZDiff", 64);
		MIN_LAYER_HEIGHT = geodataSettings.getProperty("MinLayerHeight", 64);
		PATHFIND_MAX_TIME = geodataSettings.getProperty("PathFindMaxTime", 10000000);
		PATHFIND_BUFFERS = geodataSettings.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");
		GEODATA_SKILL_CHECK_TASK_INTERVAL = geodataSettings.getProperty("GeodataSkillCheckTaskInterval", 200);
		REGION_EDGE_MAX_Z_DIFF = geodataSettings.getProperty("RegionEdgeMaxZDiff", 128);
		ALT_DAMAGE_INVIS = geodataSettings.getProperty("GeoEngineHPReduceCheck", false);
	}
	
	public static void loadGMSettings()
	{
		ExProperties gmSettings = load(GM_CONFIG_FILE);
		
		EVERYBODY_HAS_ADMIN_RIGHTS = gmSettings.getProperty("EverybodyHasAdminRights", false);
		INVENTORY_MAXIMUM_GM = gmSettings.getProperty("MaximumSlotsForGMPlayer", 250);
		KARMA_DROP_GM = gmSettings.getProperty("CanGMDropEquipment", false);
		
		GM_LOGIN_INVUL = gmSettings.getProperty("GMLoginInvul", false);
		GM_LOGIN_IMMORTAL = gmSettings.getProperty("GMLoginImmortal", false);
		GM_LOGIN_INVIS = gmSettings.getProperty("GMLoginInvis", false);
		GM_LOGIN_SILENCE = gmSettings.getProperty("GMLoginSilence", false);
		GM_LOGIN_TRADEOFF = gmSettings.getProperty("GMLoginTradeOff", false);
		HIDE_GM_STATUS = gmSettings.getProperty("HideGMStatus", false);
		ANNOUNCE_GM_LOGIN = gmSettings.getProperty("AnnounceGMLogin", false);
		SAVE_GM_EFFECTS = gmSettings.getProperty("SaveGMEffects", false);
		GM_PM_COMMANDS = gmSettings.getProperty("GmPmCommands", true);
	}
	
	public static void loadEventsSettings()
	{
		ExProperties eventSettings = load(EVENTS_CONFIG_FILE);
		
		ENABLE_NEW_CTF = eventSettings.getProperty("EnableNewCTF", false);
		ENABLE_NEW_TVT = eventSettings.getProperty("EnableNewTVT", false);
		
		EVENT_CofferOfShadowsPriceRate = eventSettings.getProperty("CofferOfShadowsPriceRate", 1.);
		EVENT_CofferOfShadowsRewardRate = eventSettings.getProperty("CofferOfShadowsRewardRate", 1.);
		
		EVENT_ENABLE_LAST_HERO = eventSettings.getProperty("Enable_last_hero", false);
		EVENT_LastHeroItemID = eventSettings.getProperty("LastHero_bonus_id", 57);
		EVENT_LastHeroItemCOUNT = eventSettings.getProperty("LastHero_bonus_count", 5000.);
		EVENT_LastHeroTime = eventSettings.getProperty("LastHero_time", 3);
		EVENT_LastHeroRate = eventSettings.getProperty("LastHero_rate", true);
		EVENT_LastHeroChanceToStart = eventSettings.getProperty("LastHero_ChanceToStart", 5);
		EVENT_LastHeroItemCOUNTFinal = eventSettings.getProperty("LastHero_bonus_count_final", 10000.);
		EVENT_LastHeroRateFinal = eventSettings.getProperty("LastHero_rate_final", true);
		
		EVENT_TvTItemID = eventSettings.getProperty("TvT_bonus_id", 57);
		EVENT_TvTItemCOUNT = eventSettings.getProperty("TvT_bonus_count", 5000.);
		EVENT_TvTTime = eventSettings.getProperty("TvT_time", 3);
		EVENT_TvT_rate = eventSettings.getProperty("TvT_rate", true);
		EVENT_TvTChanceToStart = eventSettings.getProperty("TvT_ChanceToStart", 5);
		
		ENABLE_GVG_EVENT = eventSettings.getProperty("EnableGVGEvent", false);
		EVENT_GvGDisableEffect = eventSettings.getProperty("GvGDisableEffect", false);
		GvG_POINTS_FOR_BOX = eventSettings.getProperty("GvGPointsKillBox", 20);
		GvG_POINTS_FOR_BOSS = eventSettings.getProperty("GvGPointsKillBoss", 50);
		GvG_POINTS_FOR_KILL = eventSettings.getProperty("GvGPointsKillPlayer", 5);
		GvG_POINTS_FOR_DEATH = eventSettings.getProperty("GvGPointsIfDead", 3);
		GvG_EVENT_TIME = eventSettings.getProperty("GvGEventTime", 10);
		GvG_BOSS_SPAWN_TIME = eventSettings.getProperty("GvGBossSpawnTime", 10);
		GvG_FAME_REWARD = eventSettings.getProperty("GvGRewardFame", 200);
		GvG_REWARD = eventSettings.getProperty("GvGRewardStatic", 57);
		GvG_REWARD_COUNT = eventSettings.getProperty("GvGRewardCountStatic", 10000);
		GvG_ADD_IF_WITHDRAW = eventSettings.getProperty("GvGAddPointsIfPartyWithdraw", 200);
		GvG_HOUR_START = eventSettings.getProperty("GvGHourStart", 20);
		GvG_MINUTE_START = eventSettings.getProperty("GvGMinuteStart", 0);
		GVG_MIN_LEVEL = eventSettings.getProperty("GvGMinLevel", 1);
		GVG_MAX_LEVEL = eventSettings.getProperty("GvGMaxLevel", 85);
		GVG_MAX_GROUPS = eventSettings.getProperty("GvGMaxGroupsInEvent", 100);
		GVG_MIN_PARTY_MEMBERS = eventSettings.getProperty("GvGMinPlayersInParty", 6);
		GVG_TIME_TO_REGISTER = eventSettings.getProperty("GvGTimeToRegister", 10);
		
		TMEnabled = eventSettings.getProperty("DefenseTownsEnabled", false);
	    TMStartHour = eventSettings.getProperty("DefenseTownsStartHour", 19);
	    TMStartMin = eventSettings.getProperty("DefenseTownsStartMin", 0);
	    
	    TMEventInterval = eventSettings.getProperty("DefenseTownsEventInterval", 0);
	    
	    TMMobLife = eventSettings.getProperty("DefenseTownsMobLife", 10) * 60000;
	    
	    BossLifeTime = eventSettings.getProperty("BossLifeTime", 25) * 60000;
	    
	    TMTime1 = eventSettings.getProperty("DefenseTownsTime1", 2) * 60000;
	    TMTime2 = eventSettings.getProperty("DefenseTownsTime2", 5) * 60000;
	    TMTime3 = eventSettings.getProperty("DefenseTownsTime3", 5) * 60000;
	    TMTime4 = eventSettings.getProperty("DefenseTownsTime4", 5) * 60000;
	    TMTime5 = eventSettings.getProperty("DefenseTownsTime5", 5) * 60000;
	    TMTime6 = eventSettings.getProperty("DefenseTownsTime6", 5) * 60000;
	    
	    TMWave1 = eventSettings.getProperty("DefenseTownsWave1", 18855);
	    TMWave2 = eventSettings.getProperty("DefenseTownsWave2", 18855);
	    TMWave3 = eventSettings.getProperty("DefenseTownsWave3", 25699);
	    TMWave4 = eventSettings.getProperty("DefenseTownsWave4", 18855);
	    TMWave5 = eventSettings.getProperty("DefenseTownsWave5", 18855);
	    TMWave6 = eventSettings.getProperty("DefenseTownsWave6", 25699);
	    
	    TMWave1Count = eventSettings.getProperty("DefenseTownsWave1Count", 3);
	    TMWave2Count = eventSettings.getProperty("DefenseTownsWave2Count", 2);
	    TMWave3Count = eventSettings.getProperty("DefenseTownsWave3Count", 2);
	    TMWave4Count = eventSettings.getProperty("DefenseTownsWave4Count", 2);
	    TMWave5Count = eventSettings.getProperty("DefenseTownsWave5Count", 2);
	    TMWave6Count = eventSettings.getProperty("DefenseTownsWave6Count", 2);
	    
	    TMBoss = eventSettings.getProperty("DefenseTownsBoss", 25700);
	    
	    TMItem = eventSettings.getProperty("DefenseTownsItem", new int[] { 4037, 57, 9552, 9553, 9554, 9555, 9556, 9557, 6577, 6578 });
	    TMItemCol = eventSettings.getProperty("DefenseTownsItemCol", new int[] { 1, 77700000, 1, 1, 1, 1, 1, 1, 1, 1 });
	    TMItemColBoss = eventSettings.getProperty("DefenseTownsItemColBoss", new int[] { 5, 77700000, 10, 10, 10, 10, 10, 10, 2, 2 });
	    TMItemChance = eventSettings.getProperty("DefenseTownsItemChance", new int[] { 20, 40, 10, 10, 10, 10, 10, 10, 20, 20 });
	    TMItemChanceBoss = eventSettings.getProperty("DefenseTownsItemChanceBoss", new int[] { 50, 40, 50, 50, 50, 50, 50, 50, 20, 20 });
	    
		EVENT_TFH_POLLEN_CHANCE = eventSettings.getProperty("TFH_POLLEN_CHANCE", 5.);
		
		EVENT_GLITTMEDAL_NORMAL_CHANCE = eventSettings.getProperty("MEDAL_CHANCE", 10.);
		EVENT_GLITTMEDAL_GLIT_CHANCE = eventSettings.getProperty("GLITTMEDAL_CHANCE", 0.1);
		
		EVENT_L2DAY_LETTER_CHANCE = eventSettings.getProperty("L2DAY_LETTER_CHANCE", 1.);
		L2_DAY_CUSTOM_DROP = eventSettings.getProperty("L2DAY_CUSTOM_LETTER_CHANCE", "3875,1").replaceAll(" ", "").split(";");
		EVENT_CHANGE_OF_HEART_CHANCE = eventSettings.getProperty("EVENT_CHANGE_OF_HEART_CHANCE", 5.);
		
		EVENT_APIL_FOOLS_DROP_CHANCE = eventSettings.getProperty("AprilFollsDropChance", 50.);
		
		EVENT_BOUNTY_HUNTERS_ENABLED = eventSettings.getProperty("BountyHuntersEnabled", true);
		
		EVENT_SAVING_SNOWMAN_LOTERY_PRICE = eventSettings.getProperty("SavingSnowmanLoteryPrice", 50000);
		EVENT_SAVING_SNOWMAN_REWARDER_CHANCE = eventSettings.getProperty("SavingSnowmanRewarderChance", 2);
		
		EVENT_TRICK_OF_TRANS_CHANCE = eventSettings.getProperty("TRICK_OF_TRANS_CHANCE", 10.);
		
		EVENT_MARCH8_DROP_CHANCE = eventSettings.getProperty("March8DropChance", 10.);
		EVENT_MARCH8_PRICE_RATE = eventSettings.getProperty("March8PriceRate", 1.);
		
		ENCHANT_CHANCE_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiEnchantChance", 66);
		ENCHANT_MAX_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiEnchantMaxWeapon", 28);
		SAFE_ENCHANT_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiSafeEnchant", 3);
		
		AllowCustomDropItems = eventSettings.getProperty("AllowCustomDropItems", true);
		CDItemsAllowMinMaxPlayerLvl = eventSettings.getProperty("CDItemsAllowMinMaxPlayerLvl", false);
		CDItemsAllowMinMaxMobLvl = eventSettings.getProperty("CDItemsAllowMinMaxMobLvl", false);
		CDItemsAllowOnlyRbDrops = eventSettings.getProperty("CDItemsAllowOnlyRbDrops", false);
		CUSTOM_DROP_ITEMS = eventSettings.getProperty("CustomDropItems", "57,1,2,100").replaceAll(" ", "").split(";");
		CDItemsMinPlayerLvl = eventSettings.getProperty("CDItemsMinPlayerLvl", 20);
		CDItemsMaxPlayerLvl = eventSettings.getProperty("CDItemsMaxPlayerLvl", 85);
		CDItemsMinMobLvl = eventSettings.getProperty("CDItemsMinMobLvl", 20);
		CDItemsMaxMobLvl = eventSettings.getProperty("CDItemsMaxMobLvl", 80);
		
		ACTIVITY_REWARD_ENABLED = eventSettings.getProperty("ActivityRewardEnabled", false);
		ACTIVITY_REWARD_TIME = eventSettings.getProperty("ActivityRewardTime", 21600);
		ACTIVITY_REWARD_ITEMS = eventSettings.getProperty("ActivityRewardItems", "57,1,2,100").replaceAll(" ", "").split(";");
		
		
		GM_HUNTER_EVENT_REWARDS = eventSettings.getProperty("GmHunterRewards",  "57,1,100;").replaceAll(" ", "").split(";");
		GM_HUNTER_EVENT_SET_SPEED = eventSettings.getProperty("GmHunterSetSpeed", 200);
		GM_HUNTER_EVENT_SET_PDEFENCE = eventSettings.getProperty("GmHunterSetPdefence", 2000);
		GM_HUNTER_EVENT_SET_MDEFENCE = eventSettings.getProperty("GmHunterSetMdefence", 2000);
		GM_HUNTER_EVENT_SET_HP = eventSettings.getProperty("GmHunterSetHP", 500000);
		GM_HUNTER_EVENT_SET_CP = eventSettings.getProperty("GmHunterSetCP", 500000);
	}
	
	public static void loadOlympiadSettings()
	{
		ExProperties olympSettings = load(OLYMPIAD);
		
		ENABLE_OLYMPIAD = olympSettings.getProperty("EnableOlympiad", true);
		ENABLE_OLYMPIAD_SPECTATING = olympSettings.getProperty("EnableOlympiadSpectating", true);
		ALT_OLY_START_TIME = olympSettings.getProperty("AltOlyStartTime", 18);
		ALT_OLY_MIN = olympSettings.getProperty("AltOlyMin", 0);
		ALT_OLY_CPERIOD = olympSettings.getProperty("AltOlyCPeriod", 21600000);
		ALT_OLY_WPERIOD = olympSettings.getProperty("AltOlyWPeriod", 604800000);
		ALT_OLY_VPERIOD = olympSettings.getProperty("AltOlyVPeriod", 43200000);
		ALT_OLY_DATE_END = olympSettings.getProperty("AltOlyDateEnd", new int[]
		{
			1
		});
		CLASS_GAME_MIN = olympSettings.getProperty("ClassGameMin", 5);
		NONCLASS_GAME_MIN = olympSettings.getProperty("NonClassGameMin", 9);
		TEAM_GAME_MIN = olympSettings.getProperty("TeamGameMin", 4);
		
		GAME_MAX_LIMIT = olympSettings.getProperty("GameMaxLimit", 70);
		GAME_CLASSES_COUNT_LIMIT = olympSettings.getProperty("GameClassesCountLimit", 30);
		GAME_NOCLASSES_COUNT_LIMIT = olympSettings.getProperty("GameNoClassesCountLimit", 60);
		GAME_TEAM_COUNT_LIMIT = olympSettings.getProperty("GameTeamCountLimit", 10);
		
		ALT_OLY_REG_DISPLAY = olympSettings.getProperty("AltOlyRegistrationDisplayNumber", 100);
		ALT_OLY_BATTLE_REWARD_ITEM = olympSettings.getProperty("AltOlyBattleRewItem", 13722);
		ALT_OLY_CLASSED_RITEM_C = olympSettings.getProperty("AltOlyClassedRewItemCount", 50);
		ALT_OLY_NONCLASSED_RITEM_C = olympSettings.getProperty("AltOlyNonClassedRewItemCount", 40);
		ALT_OLY_TEAM_RITEM_C = olympSettings.getProperty("AltOlyTeamRewItemCount", 50);
		ALT_OLY_COMP_RITEM = olympSettings.getProperty("AltOlyCompRewItem", 13722);
		ALT_OLY_GP_PER_POINT = olympSettings.getProperty("AltOlyGPPerPoint", 1000);
		ALT_OLY_HERO_POINTS = olympSettings.getProperty("AltOlyHeroPoints", 180);
		ALT_OLY_RANK1_POINTS = olympSettings.getProperty("AltOlyRank1Points", 120);
		ALT_OLY_RANK2_POINTS = olympSettings.getProperty("AltOlyRank2Points", 80);
		ALT_OLY_RANK3_POINTS = olympSettings.getProperty("AltOlyRank3Points", 55);
		ALT_OLY_RANK4_POINTS = olympSettings.getProperty("AltOlyRank4Points", 35);
		ALT_OLY_RANK5_POINTS = olympSettings.getProperty("AltOlyRank5Points", 20);
		OLYMPIAD_STADIAS_COUNT = olympSettings.getProperty("OlympiadStadiasCount", 160);
		OLYMPIAD_BEGIN_TIME = olympSettings.getProperty("OlympiadBeginTime", 120);
		OLYMPIAD_BATTLES_FOR_REWARD = olympSettings.getProperty("OlympiadBattlesForReward", 15);
		OLYMPIAD_POINTS_DEFAULT = olympSettings.getProperty("OlympiadPointsDefault", 18);
		OLYMPIAD_POINTS_WEEKLY = olympSettings.getProperty("OlympiadPointsWeekly", 3);
		OLYMPIAD_OLDSTYLE_STAT = olympSettings.getProperty("OlympiadOldStyleStat", false);
		OLYMPIAD_PLAYER_IP = olympSettings.getProperty("OlympiadPlayerIp", false);
		OLYMPIAD_PLAYER_HWID = olympSettings.getProperty("OlympiadPlayerHWID", false);
		OLYMPIAD_BAD_ENCHANT_ITEMS_ALLOW = olympSettings.getProperty("OlympiadUnEquipBadEnchantItem", false);
		
		OLY_ENCH_LIMIT_ENABLE = olympSettings.getProperty("OlyEnchantLimit", false);
		OLY_ENCHANT_LIMIT_WEAPON = olympSettings.getProperty("OlyEnchantLimitWeapon", 0);
		OLY_ENCHANT_LIMIT_ARMOR = olympSettings.getProperty("OlyEnchantLimitArmor", 0);
		OLY_ENCHANT_LIMIT_JEWEL = olympSettings.getProperty("OlyEnchantLimitJewel", 0);
		OLYMPIAD_TEAM_MATCH_SIZE = olympSettings.getProperty("OlympiadTeamMatchSize", 3);
		HERO_DIARY_EXCLUDED_BOSSES = olympSettings.getProperty("ExcludedBossesFromDairy", new int[] { 29177 });
	}
	
	public static void loadEnchantCBConfig()
	{
		ExProperties EnchantCBSetting = load(ENCHANT_CB_CONFIG_FILE);
		
		COMMUNITYBOARD_ENCHANT_ENABLED = EnchantCBSetting.getProperty("AllowCBEnchant", false);
		ALLOW_BBS_ENCHANT_ELEMENTAR = EnchantCBSetting.getProperty("AllowEnchantElementar", false);
		ALLOW_BBS_ENCHANT_ATT = EnchantCBSetting.getProperty("AllowEnchantAtt", false);
        COMMUNITYBOARD_ENCHANT_ITEM = EnchantCBSetting.getProperty("CBEnchantItem", 4356);
        COMMUNITYBOARD_MAX_ENCHANT = EnchantCBSetting.getProperty("CBMaxEnchant", 25);
        COMMUNITYBOARD_ENCHANT_LVL = EnchantCBSetting.getProperty("CBEnchantLvl", new int[0]);
        COMMUNITYBOARD_ENCHANT_PRICE_WEAPON = EnchantCBSetting.getProperty("CBEnchantPriceWeapon", new int[0]);
        COMMUNITYBOARD_ENCHANT_PRICE_ARMOR = EnchantCBSetting.getProperty("CBEnchantPriceArmor", new int[0]);
        COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON = EnchantCBSetting.getProperty("CBEnchantAtributeLvlWeapon", new int[0]);
        COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_WEAPON = EnchantCBSetting.getProperty("CBEnchantAtributePriceWeapon", new int[0]);
        COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR = EnchantCBSetting.getProperty("CBEnchantAtributeLvlArmor", new int[0]);
        COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_ARMOR = EnchantCBSetting.getProperty("CBEnchantAtributePriceArmor", new int[0]);
        COMMUNITYBOARD_ENCHANT_ATRIBUTE_PVP = EnchantCBSetting.getProperty("CBEnchantAtributePvP", false);	
	}
	
	public static void loadPhantomsConfig()
	{
		ExProperties settings = load(PHANTOM_FILE);
		
		PHANTOM_PLAYERS_ENABLED = settings.getProperty("PhantomPlayersEnabled", false);
		PHANTOM_PLAYERS_ACCOUNT = settings.getProperty("PhantomPlayersAccount", "PhantomPlayerAI");
		PHANTOM_MAX_PLAYERS = settings.getProperty("PhantomMaxPlayers", 1);
		PHANTOM_BANNED_CLASSID = settings.getProperty("PhantomBannedClassIds", new int[]{});
		PHANTOM_BANNED_SETID = settings.getProperty("PhantomBannedSetIds", new int[]{});
		PHANTOM_MAX_WEAPON_GRADE = settings.getProperty("PhantomMaxWeaponGrade", 5);
		PHANTOM_MAX_ARMOR_GRADE = settings.getProperty("PhantomMaxArmorGrade", 5);
		PHANTOM_MAX_JEWEL_GRADE = settings.getProperty("PhantomMaxJewelGrade", 5);
		PHANTOM_SPAWN_MAX = settings.getProperty("PhantomSpawnMax", 1);
		PHANTOM_SPAWN_DELAY = settings.getProperty("PhantomSpawnDelay", 60);
		PHANTOM_MAX_LIFETIME = settings.getProperty("PhantomMaxLifetime", 120);
		
		CHANCE_TO_ENCHANT_WEAP = settings.getProperty("PhantomChanceEnchantWeap", 0);
		MAX_ENCH_PHANTOM_WEAP = settings.getProperty("PhantomMaxEnchantWeap", 4);
		
		PHANTOM_MAX_DRIFT_RANGE  = settings.getProperty("MaxDriftRangeForNpc", 1000);
		
		ALLOW_PHANTOM_CUSTOM_TITLES = settings.getProperty("AllowSetupCustomTitles", false);
		PHANTOM_CHANCE_SET_NOBLE_TITLE = settings.getProperty("ChanceToSetTitle", 30);
		
		DISABLE_PHANTOM_ACTIONS = settings.getProperty("DisablePhantomActions", false);
		
		PHANTOM_ALLOWED_NPC_TO_WALK = settings.getProperty("PhantomRoamingNpcs", new int[]{});
		PHANTOM_ROAMING_MAX_WH_CHECKS = settings.getProperty("PhantomRoamingMaxWhChecks", 2);
		PHANTOM_ROAMING_MAX_WH_CHECKS_DWARF = settings.getProperty("PhantomRoamingMaxWhChecksDwarf", 8);
		PHANTOM_ROAMING_MAX_SHOP_CHECKS = settings.getProperty("PhantomRoamingMaxShopChecks", 2);
		PHANTOM_ROAMING_MAX_SHOP_CHECKS_DWARF = settings.getProperty("PhantomRoamingMaxShopChecksDwarf", 5);
		PHANTOM_ROAMING_MAX_NPC_CHECKS = settings.getProperty("PhantomRoamingMaxNpcChecks", 6);
		PHANTOM_ROAMING_MIN_WH_DELAY = settings.getProperty("PhantomRoamingMinWhDelay", 60);
		PHANTOM_ROAMING_MAX_WH_DELAY = settings.getProperty("PhantomRoamingMaxWhDelay", 300);
		PHANTOM_ROAMING_MIN_SHOP_DELAY = settings.getProperty("PhantomRoamingMinShopDelay", 30);
		PHANTOM_ROAMING_MAX_SHOP_DELAY = settings.getProperty("PhantomRoamingMaxShopDelay", 120);
		PHANTOM_ROAMING_MIN_NPC_DELAY = settings.getProperty("PhantomRoamingMinNpcDelay", 45);
		PHANTOM_ROAMING_MAX_NPC_DELAY = settings.getProperty("PhantomRoamingMaxNpcDelay", 120);
		PHANTOM_ROAMING_MIN_PRIVATESTORE_DELAY = settings.getProperty("PhantomRoamingMinPrivatestoreDelay", 2);
		PHANTOM_ROAMING_MAX_PRIVATESTORE_DELAY = settings.getProperty("PhantomRoamingMaxPrivatestoreDelay", 7);
		PHANTOM_ROAMING_MIN_FREEROAM_DELAY = settings.getProperty("PhantomRoamingMinFreeroamDelay", 10);
		PHANTOM_ROAMING_MAX_FREEROAM_DELAY = settings.getProperty("PhantomRoamingMaxFreeroamDelay", 60);
		DISABLE_PHANTOM_RESPAWN = settings.getProperty("DisablePhantomRespawn", false);
		DEBUG_PHANTOMS = settings.getProperty("DebugPhantoms", false);
		PHANTOM_CLANS = settings.getProperty("PhantomClans", new int[]{});
	}
	
	public static void loadPremiumConfig()
	{
		ExProperties premiumConf = load(PREMIUM_FILE);
		
		SERVICES_RATE_TYPE = premiumConf.getProperty("RateBonusType", Bonus.NO_BONUS);
		SERVICES_RATE_CREATE_PA = premiumConf.getProperty("RateBonusCreateChar", 0);
		SERVICES_RATE_BONUS_PRICE = premiumConf.getProperty("RateBonusPrice", new int[]
		{
			1500
		});
		SERVICES_RATE_BONUS_ITEM = premiumConf.getProperty("RateBonusItem", new int[]
		{
			4037
		});
		SERVICES_RATE_BONUS_VALUE = premiumConf.getProperty("RateBonusValue", new double[]
		{
			2.
		});
		SERVICES_RATE_BONUS_DAYS = premiumConf.getProperty("RateBonusTime", new int[]
		{
			30
		});
		AUTO_LOOT_PA = premiumConf.getProperty("AutoLootPA", false);
		ENCHANT_CHANCE_WEAPON_PA = premiumConf.getProperty("EnchantChancePA", 66);
		ENCHANT_CHANCE_ARMOR_PA = premiumConf.getProperty("EnchantChanceArmorPA", 66);
		ENCHANT_CHANCE_ACCESSORY_PA = premiumConf.getProperty("EnchantChanceAccessoryPA", 66);
		ENCHANT_CHANCE_WEAPON_BLESS_PA = premiumConf.getProperty("EnchantChanceBlessPA", 66);
		ENCHANT_CHANCE_ARMOR_BLESS_PA = premiumConf.getProperty("EnchantChanceArmorBlessPA", 66);
		ENCHANT_CHANCE_ACCESSORY_BLESS_PA = premiumConf.getProperty("EnchantChanceAccessoryBlessPA", 66);
		ENCHANT_CHANCE_CRYSTAL_WEAPON_PA = premiumConf.getProperty("EnchantChanceCrystalPA", 66);
		ENCHANT_CHANCE_CRYSTAL_ARMOR_PA = premiumConf.getProperty("EnchantChanceCrystalArmorPA", 66);
		ENCHANT_CHANCE_CRYSTAL_ACCESSORY_PA = premiumConf.getProperty("EnchantChanceCrystalAccessory", 66);
		
		SERVICES_BONUS_XP = premiumConf.getProperty("RateBonusXp", 1.);
		SERVICES_BONUS_SP = premiumConf.getProperty("RateBonusSp", 1.);
		SERVICES_BONUS_ADENA = premiumConf.getProperty("RateBonusAdena", 1.);
		SERVICES_BONUS_ITEMS = premiumConf.getProperty("RateBonusItems", 1.);
		SERVICES_BONUS_SPOIL = premiumConf.getProperty("RateBonusSpoil", 1.);
		
		USE_ALT_ENCHANT_PA = Boolean.parseBoolean(premiumConf.getProperty("UseAltEnchantPA", "False"));
		for (String prop : premiumConf.getProperty("EnchantWeaponFighterPA", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
			ENCHANT_WEAPON_FIGHT_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantWeaponFighterCrystalPA", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
			ENCHANT_WEAPON_FIGHT_BLESSED_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantWeaponFighterBlessedPA", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
			ENCHANT_WEAPON_FIGHT_CRYSTAL_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantArmorPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantArmorCrystalPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_CRYSTAL_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantArmorBlessedPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_BLESSED_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantJewelryPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_JEWELRY_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantJewelryCrystalPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_JEWELRY_CRYSTAL_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantJewelryBlessedPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_JEWELRY_BLESSED_PA.add(Integer.parseInt(prop));
	}
	
	public static void LoadCustom_Config()
	{
		ExProperties custom_Config = load(CUSTOM_CONFIG_FILE);
		
		ITEM_COST_1_ADENA = custom_Config.getProperty("ItemsCost1Adena", "7575,7576").split(",");
		
		ENABLE_RANK_MANAGER = custom_Config.getProperty("EnableRankNpcManager", false);
		RANK_NPC_ID = custom_Config.getProperty("RankNpcID", 70027);
		RANK_NPC_MIN_LEVEL = custom_Config.getProperty("RankNpcMinLevel", 40);
		RANK_NPC_DISABLE_PAGE = custom_Config.getProperty("RankNpcDisablePage", "0").split("\\,");
		String[] listSplit = custom_Config.getProperty("RankNpcListItem", "57;5575;6673").split("\\;");
		RANK_NPC_LIST_ITEM = new Integer[listSplit.length];
		for (int i = 0; i < listSplit.length; i++)
		{
			try
			{
				RANK_NPC_LIST_ITEM[i] = Integer.parseInt(listSplit[i]);
			}
			catch (Exception e)
			{
				_log.warn("Error creating list of items, enter only numbers: " + e.getMessage());
			}
		}
		RANK_NPC_ITEMS_RECORDS = custom_Config.getProperty("RankNpcItemsRecords", 20);
		listSplit = custom_Config.getProperty("RankNpcListClass", "88;89;90;91;92;93;94;95;96;97;98;99;100;101;102;103;104;105;106;107;108;109;110;111;112;113;114;115;116;117;118;131;132;133;134;136").split("\\;");
		RANK_NPC_LIST_CLASS = new Integer[listSplit.length];
		for (int i = 0; i < listSplit.length; i++)
		{
			try
			{
				RANK_NPC_LIST_CLASS[i] = Integer.parseInt(listSplit[i]);
			}
			catch (Exception e)
			{
				_log.warn("Error creating list of class, enter only numbers: " + e.getMessage());
			}
		}
		RANK_NPC_OLY_RECORDS = custom_Config.getProperty("RankNpcOlyRecords", 20);
		RANK_NPC_PVP_RECORDS = custom_Config.getProperty("RankNpcPvPRecords", 20);
		RANK_NPC_PK_RECORDS = custom_Config.getProperty("RankNpcPKRecords", 20);
		RANK_NPC_COLOR_A = custom_Config.getProperty("RankNpcColorA", "D9CC46");
		RANK_NPC_COLOR_B = custom_Config.getProperty("RankNpcColorB", "FFFFFF");
		RANK_NPC_RELOAD = custom_Config.getProperty("RankNpcReload", 15);
		
		ENABLE_CHARACTER_INTRO = custom_Config.getProperty("EnableCharacterIntro", false);
		FORBIDDEN_CHAR_NAMES = custom_Config.getProperty("ForbiddenCharNames", "").split(",");
		
		ENABLE_ACHIEVEMENTS = custom_Config.getProperty("EnableAchievements", false);
		DISABLE_ACHIEVEMENTS_FAME_REWARD = custom_Config.getProperty("DisableFameRewards", false);
		
		ENABLE_EMAIL_VALIDATION = custom_Config.getProperty("EnableEmailValidation", false);
		
		CHARACTER_NAME_COLORIZATION = custom_Config.getProperty("CustomCharacterColorization", false);
		
		ALLOW_WYVERN_DURING_SIEGE = custom_Config.getProperty("AllowRideWyvernDuringSiege", false);
		PUNISHMENT_FOR_WYVERN_INSIDE_SIEGE = custom_Config.getProperty("PunishmentForWyvern", "");
		PINISHMENT_TIME_FOR_WYVERN = custom_Config.getProperty("PunishmentTimeForWyvern", 120);
		
		String[] propertySplit = custom_Config.getProperty("AdenaDropRateByLevel", "").split(";");
		ADENA_DROP_RATE_BY_LEVEL = new TIntIntHashMap(propertySplit.length);
		for (String ps : propertySplit)
		{
			String[] skillSplit = ps.split(",");
			if (skillSplit.length != 2)
			{
				_log.warn(Strings.concat("[AdenaDropRateByLevel]: invalid config property -> AdenaDropRateByLevel \"", ps, "\""));
			}
			else
			{
				try
				{
					ADENA_DROP_RATE_BY_LEVEL.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!ps.isEmpty())
					{
						_log.warn(Strings.concat("[AdenaDropRateByLevel]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
					}
				}
			}
		}
		
		CUSTOM_SKILLS_LOAD = custom_Config.getProperty("CustomSkillsLoad", false);
		CUSTOM_ITEMS_LOAD = custom_Config.getProperty("CustomItemsLoad", false);
		CUSTOM_MULTISELL_LOAD = custom_Config.getProperty("CustomMultisellLoad", false);
		DISABLE_TUTORIAL = custom_Config.getProperty("DisableTutorialQuestOnStart", true);
		ALLOW_MAMMON_FOR_ALL = custom_Config.getProperty("EnableMammonsForAll", true);
		ALLOW_FARM_IN_SEVENSIGN_IF_NOT_REGGED = custom_Config.getProperty("AllowPlayersToFarmIntoSevenSignIfNotRegged", true);
		SEVEN_SIGN_DISABLE_BUFF_DEBUFF = custom_Config.getProperty("DisableDebuffBuffFromSevenSign", false);
		SEVEN_SIGN_NON_STOP_ALL_SPAWN = custom_Config.getProperty("AllowToSetCustomSevenSignMode", false);
		SEVEN_SIGN_SET_PERIOD = custom_Config.getProperty("CustomModeForSevenSign", 0);
		ANNOUNCE_MAMMON_SPAWN = custom_Config.getProperty("AnnounceMammonSpawn", true);
		MAX_PLAYER_CONTRIBUTION = custom_Config.getProperty("MaxPlayerContribution", 1000000);
		PREMIUM_FOR_NEW_ACC = custom_Config.getProperty("PremiumForNewAcc", false);
		PREMIUM_TEMPLATE_NEW_ACC = custom_Config.getProperty("TemplateForPremium", 10);
		PREMIUM_TIME_FOR_NEW_ACC = custom_Config.getProperty("TimeForPremium", 172800000L);
		
		VOTE_REWARDS = custom_Config.getProperty("VoteRewards", "6673,5,100;6673,10,50;17168,1,100;17168,2,75;17168,3,50;13693,25,50;13279,1,15;13015,1,5;17061,1,10;17057,1,25;959,1,40;960,1,100;").replaceAll(" ", "").split(";");
		
		ALLOW_BOARD_NEWS_LEECH = custom_Config.getProperty("ForumBoardLeech", false);
		FORUM_URL_TO_LEECH_CHANGELOG = custom_Config.getProperty("ChangeLogURL", "http://l2age.com/forum/index.php?/5-changelog/");
		FORUM_URL_TO_LEECH_ANNOUNCE = custom_Config.getProperty("AnnounceURL", "http://l2age.com/forum/index.php?/4-news/");
		
		ENABLE_POLL_SYSTEM = custom_Config.getProperty("ActivatePollSystem", false);
		
		ALT_SELL_FROM_EVERYWHERE = custom_Config.getProperty("SellItemsEverywhere", false);
		
		// Captcha system
		ENABLE_CAPTCHA = custom_Config.getProperty("EnableCaptchaSystem", false);
		CAPTCHA_MIN_MONSTERS = custom_Config.getProperty("CaptchaMinMonstertokill", 1000);
		CAPTCHA_MAX_MONSTERS = custom_Config.getProperty("CaptchaMaxMonstertokill", 2000);
		CAPTCHA_ATTEMPTS = custom_Config.getProperty("CaptchaAttempts", 3);
		CAPTCHA_SAME_LOCATION_DELAY = custom_Config.getProperty("CaptchaSameLocationDelay", 60);
		CAPTCHA_SAME_LOCATION_MIN_KILLS = custom_Config.getProperty("CaptchaSameLocationMinKills", 5);
		CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE = custom_Config.getProperty("CaptchaPunishmentType", "BANCHAR");
		CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME = custom_Config.getProperty("CaptchaPunishmentTime", -1);
		CAPTCHA_TIME_BETWEEN_TESTED_SECONDS = custom_Config.getProperty("CaptchaDelayBetweenTests", 1800);
		CAPTCHA_TIME_BETWEEN_REPORTS_SECONDS = custom_Config.getProperty("CaptchaReportDelay", 7200);
		CAPTCHA_MIN_LEVEL = custom_Config.getProperty("CaptchaMinLevel", 40);
		
		// Clan promotion
		SERVICES_CLAN_PROMOTION_ENABLE = custom_Config.getProperty("EnableClanPromotion", false);
		SERVICES_CLAN_PROMOTION_MAX_LEVEL = custom_Config.getProperty("MaxClanLevel", 6);
		SERVICES_CLAN_PROMOTION_MIN_ONLINE = custom_Config.getProperty("MinOnlineMembers", 10);
		SERVICES_CLAN_PROMOTION_ITEM = custom_Config.getProperty("ClanPromotionItemId", 57);
		SERVICES_CLAN_PROMOTION_ITEM_COUNT = custom_Config.getProperty("ClanPromotionItemCOunt", 1000);
		SERVICES_CLAN_PROMOTION_SET_LEVEL = custom_Config.getProperty("ClanPromotionSetLevel", 5);
		SERVICES_CLAN_PROMOTION_ADD_REP = custom_Config.getProperty("ClanPromotionAddrep", 0);
		SERVICE_CLAN_PRMOTION_ADD_EGGS = custom_Config.getProperty("GiveEggsToNewClans", false);
		CLAN_PROMOTION_CLAN_EGGS = custom_Config.getProperty("ClanEggsToReward", "").replaceAll(" ", "").split(";");
		
		CANCEL_SYSTEM_RESTORE_DELAY = custom_Config.getProperty("CancelSystemRestoreDelay", 120);
		CANCEL_SYSTEM_KEEP_TICKING = custom_Config.getProperty("CancelSystemKeepTicking", false);
		DEADLOCKCHECK_INTERVAL = custom_Config.getProperty("DeadLockCheckerInterval", -1);
		
		AUTO_SHOTS_ON_LOGIN = custom_Config.getProperty("AutoShotsOnLogin", false);
		
		ENABLE_EMOTIONS = custom_Config.getProperty("EnableEmotions", false);
		
		ENABLE_PLAYER_KILL_SYSTEM = custom_Config.getProperty("EnableCustomPlayerKillSystem", false);
		PLAYER_KILL_SPAWN_UNIQUE_CHEST = custom_Config.getProperty("SpawnChestsOnKill", false);
		PLAYER_KILL_INCREASE_ATTRIBUTE = custom_Config.getProperty("AllowRandomAttribute", false);
		PLAYER_KILL_GIVE_ENCHANTS = custom_Config.getProperty("GiveEnchantsonKill", false);
		PLAYER_KILL_GIVE_LIFE_STONE = custom_Config.getProperty("GiveLifeStoneonKill", false);
		PLAYER_KILL_GIVE_MANTRAS = custom_Config.getProperty("GiveMantrasonKill", false);
		PLAYER_KILL_AQUIRE_FAME = custom_Config.getProperty("IncreaseFameonKill", false);
		PLAYER_KILL_ALLOW_CUSTOM_PVP_ZONES = custom_Config.getProperty("AllowCustomZones", false);
		
		CUSTOM_CLASS_TRANSFER_SKILLS = custom_Config.getProperty("AllowCustomTransferSkills", false);
		
		ENABLE_REFERRAL_SYSTEM = custom_Config.getProperty("EnableReferralSystem", false);
		
		ALLOW_MACROS_REUSE_BUG = custom_Config.getProperty("AllowMacrosReuseBug", true);
		PREMIUM_ACCOUNT_FOR_PARTY = custom_Config.getProperty("PremiumDistributeDropToAllParty", false);
		
		ALLOW_PLAYER_CHANGE_LANGUAGE = custom_Config.getProperty("AllowPlayerToChangeLang", false);
		
		ENABLE_VOTE_REWARDS = custom_Config.getProperty("EnableVoteReward", false);
		SHOW_BAN_INFO_IN_CHARACTER_SELECT = custom_Config.getProperty("ShowBanInfoOnCharSelect", false);
		LEVEL_REQUIRED_TO_SEND_MAIL = custom_Config.getProperty("MinLevelToSendMails", 0);
		
		ALT_SELL_PRICE_DIV = custom_Config.getProperty("SellPriceDiv", 2);
		
		ENABLE_FAKEPC = custom_Config.getProperty("EneableFakePCs", false);
		
		ENABLE_VISUAL_SYSTEM = custom_Config.getProperty("EnableVisualSystem", false);
		VISUALS_TITLE_COLOR = custom_Config.getProperty("VisualsTitleColor", "0xa4e598");
		VISUALS_NAME_COLOR = custom_Config.getProperty("VisualsNameColor", "0xFFFFFF");
		VISUAL_NPC_DELETE_TIME = custom_Config.getProperty("VisualNpcDeleteTime", 5);
		
		MAX_ADENA_TO_EAT = custom_Config.getProperty("MaxAdenaLakfiEat", 99);
		ADENA_TO_EAT = custom_Config.getProperty("AdenaLakfiEat", 50);
		TIME_IF_NOT_FEED = custom_Config.getProperty("TimeIfNotFeedDissapear", 10);
		ENABLE_LUCKY_PIGS = custom_Config.getProperty("EnableLuckyPigs", false);
		
		LEVEL_UP_CRY_EXTRA_CHANCE = custom_Config.getProperty("LevelUpCrystalExtraChance", 0);
		
		ENABLE_ALT_VOTE_REWARD = custom_Config.getProperty("AltEnalbeVoteReward", false);
		ENABLE_HOPZONE_VOTING = custom_Config.getProperty("AltEnableHopzoneVote", true);
		ENABLE_TOPZONE_VOTING = custom_Config.getProperty("AltEnableTopzoneVote", true);
		TOPZONE_SERVER_LINK = custom_Config.getProperty("AltTopzoneLink", "");
		HOPZONE_SERVER_LINK = custom_Config.getProperty("AltHopzoneLink", "");
		TIME_TO_VOTE_SEC = custom_Config.getProperty("AltVoteTime", 60);
		ALT_VOTE_REWARDS = custom_Config.getProperty("AltVoteRewards", "57,5,100;,57,1000,100;").replaceAll(" ", "").split(";");
		
		PVP_KILLS_COLOR = new HashMap<Integer, String>();
		
		String[] split = custom_Config.getProperty("PvPColorSystem", "").split(";");
		
		if (!split[0].isEmpty())
		{
			for (String ps : split)
			{
				String[] pvp = ps.split(",");
				if (pvp.length != 2)
					_log.error("[PvPColorSystem]: invalid config property -> PvPColorSystem \"" + ps + "\"");
				else
				{
					try
					{
						PVP_KILLS_COLOR.put(Integer.parseInt(pvp[0]), pvp[1]);
					}
					catch (NumberFormatException nfe)
					{
						nfe.printStackTrace();
						
						if (!ps.equals(""))
						{
							_log.error("[PvPColorSystem]: invalid config property -> PvPColorSystem \"" + Integer.parseInt(pvp[0]) + "\"" + pvp[1]);
						}
					}
				}
			}
		}
		
		CLANHALL_NPC_IDS = custom_Config.getProperty("NPCcanBeSpawnedinClanHall", new int[]
		{
			402,
			403,
			30300,
			30120,
			30086,
			404
		});
		TALISMAN_IDS = custom_Config.getProperty("TalismansCanBeCombined", new int[]
		{
			9914,
			9915,
			9916,
			9917,
			9918,
			9919,
			9920,
			9921,
			9922,
			9923,
			9924,
			9925,
			9926,
			9927,
			9928,
			9929,
			9930,
			9931,
			9932,
			9933,
			9934,
			9935,
			9936,
			9937,
			9938,
			9939,
			9940,
			9941,
			9942,
			9943,
			9944,
			9945,
			9946,
			9947,
			9948,
			9949,
			9950,
			9951,
			9952,
			9953,
			9954,
			9955,
			9956,
			9957,
			9958,
			9959,
			9960,
			9961,
			9962,
			9963,
			9964,
			9965,
			10141,
			10142,
			10168,
			10416,
			10417,
			10418,
			10419,
			10420,
			10421,
			10422,
			10423,
			10424,
			10518,
			10519,
			10520,
			10521,
			10522,
			10523,
			10524,
			10525,
			10526,
			10527,
			10528,
			10529,
			10530,
			10531,
			10532,
			10533,
			10534,
			10535,
			10536,
			10537,
			10538,
			10539,
			10540,
			10541,
			10542,
			10543,
			12815,
			12816,
			12817,
			12818,
			14604,
			14605,
			14810,
			14811,
			14812,
			14813,
			14814
		});
		
		ENABLE_AUTO_POTIONS = custom_Config.getProperty("EnableAutoPotions", false);
		AUTO_CP_POTION_DELAY = custom_Config.getProperty("CPPotionDelay", 1.);
		AUTO_HP_POTION_DELAY= custom_Config.getProperty("HPPotionDelay", 2.);
		AUTO_MP_POTION_DELAY= custom_Config.getProperty("MPPotionDelay", 1.);
		
		LASER_CP_POTION_RESTORE = custom_Config.getProperty("LowCPrestore", 200);
		GREATER_CP_POTION_RESTORE = custom_Config.getProperty("GreaterCPrestore", 400);
		
		LASER_MP_POTION_RESTORE = custom_Config.getProperty("LowMPrestore", 200);
		GREATER_MP_POTION_RESTORE = custom_Config.getProperty("GreaterMPrestore", 400);
		
		LASER_HP_POTION_RESTORE = custom_Config.getProperty("LowHPrestore", 200);
		GREATER_HP_POTION_RESTORE = custom_Config.getProperty("GreaterHPrestore", 400);
	}

	public static void LoadCustomSecurity_Config()
	{
		ExProperties custom_security = load(CUSTOM_SECURITY_FILE);
		
		SECURITY_ENABLED = custom_security.getProperty("EnableSecurity", true);
		SECURITY_CHANGE_PASSWORD = custom_security.getProperty("SecurityRequestOnChangePassword", false);
		SECURITY_HERO_HEROVOICE = custom_security.getProperty("EnableSecurityHeroVoice", false);
		SECURITY_ON_STARTUP_WHEN_SECURED = custom_security.getProperty("EnableOnStartupWhenSecured", true);
		SECURITY_CFG_ENABLED = custom_security.getProperty("EnableSecurityCfg", false);
		SECURITY_CANT_PVP_ENABLED = custom_security.getProperty("EnableSecurityCantPvP", false);
		SECURITY_FORCE = custom_security.getProperty("EnableSecurityForced", false);
		SECURITY_TRADE_ENABLED = custom_security.getProperty("EnableSecurityTrade", false);
		SECURITY_ENCHANT_SKILL_ENABLED = custom_security.getProperty("EnableSecurityEnchantSkills", false);
		SECURITY_ENCHANT_ITEM_ENABLED = custom_security.getProperty("EnableSecurityItemEnchant", false);
		SECURITY_ENCHANT_ITEM_REMOVE_ENABLED = custom_security.getProperty("EnableSecurityItemEnchantRemove", false);
		SECURITY_ENCHANT_ITEM_ELEMENT_REMOVE_ENABLED = custom_security.getProperty("EnableSecurityItemElementRemove", false);
		SECURITY_ITEM_AUGMENT = custom_security.getProperty("EnableSecurityItemAugment", false);
		SECURITY_ITEM_UNEQUIP = custom_security.getProperty("EnableSecurityUnEquipItem", false);
		SECURITY_ITEM_ATTRIBUTE_REMOVE_ENABLED = custom_security.getProperty("EnableSecurityItemAttributeRemove", false);
		SECURITY_ITEM_DESTROY_ENABLED = custom_security.getProperty("EnableSecurityItemDestroy", false);
		SECURITY_ITEM_DROP_ENABLED = custom_security.getProperty("EnableSecurityItemDrop", false);
		SECURITY_ITEM_GIVE_TO_PET_ENABLED = custom_security.getProperty("EnableSecurityItemGiveToPet", false);
		SECURITY_ITEM_REMOVE_AUGUMENT_ENABLED = custom_security.getProperty("EnableSecurityItemRemoveAugument", false);
		SECURITY_ITEM_CRYSTALIZE_ENABLED = custom_security.getProperty("EnableSecurityItemCrystalize", false);
		SECURITY_SENDING_MAIL_ENABLED = custom_security.getProperty("EnableSecuritySendMail", false);
		SECURITY_DELETE_RECIEVED_MAILS = custom_security.getProperty("EnableSecurityDeleteRecievedMails", false);
		SECURITY_DELETE_SENT_MAILS = custom_security.getProperty("EnableSecurityDeleteSentMails", false);
		SECURITY_READ_OWN_MAILS = custom_security.getProperty("EnableSecurityReadOwnMails", false);
		SECURITY_DELETE_MACRO = custom_security.getProperty("EnableSecurityDeleteMarco", false);
		SECURITY_ADD_MACRO = custom_security.getProperty("EnableSecurityAddMarco", false);
		SECURITY_DELETE_BOOKMARK_SLOT = custom_security.getProperty("EnableSecurityDeleteBookmark", false);
		SECURITY_CLAN_ALLY_ALL = custom_security.getProperty("EnableSecurityClanAllyAll", false);
	}
	
	public static void LoadSMTP_Config()
	{
		ExProperties stmp_server = load(SMTP_CONFIG);
		
		ENABLE_PASSWORD_RECOVERY = stmp_server.getProperty("EnablePasswordRecovery", false);
		ENABLE_ON_PASSWORD_CHANGE = stmp_server.getProperty("EnableOnChangePassword", false);
		ENABLE_ON_SECURITY_PASSWORD_CHANGE = stmp_server.getProperty("EnableOnChangeSecurity", false);
		SMTP_SERVER = stmp_server.getProperty("SMTPServerAddress", "smtp.gmail.com");
		SMTP_SERVER_PORT = stmp_server.getProperty("SMTPServerPort", 465);
		SMTP_USERNAME = stmp_server.getProperty("SMTPUsername", "username");
		SMTP_PASSWORD = stmp_server.getProperty("SMTPPassword", "password");
		SMTP_SERVER_AUTH = stmp_server.getProperty("SMTPAuth", false);
		SMTP_SERVER_SECUIRTY = stmp_server.getProperty("SMTPSecurity", "TLS");
		SMTP_EMAIL_ADDR_SENDER = stmp_server.getProperty("SMTPEmailAdressSender", "sender@something.com");
		SMTP_SERVER_TIMEOUT = stmp_server.getProperty("STMPServerTimeout", 5000);
		SMTP_SERVER_CONNECTION_TIMEOUT = stmp_server.getProperty("STMPServerConnectionTimeout", 5000);
		
	}
	
	public static void loadAcc_moveConfig()
	{
		ExProperties Acc_moveConfig = load(ACC_MOVE_FILE);
		
		ACC_MOVE_ENABLED = Acc_moveConfig.getProperty("EnableAccountTransfer", false);
		ACC_MOVE_ITEM = Acc_moveConfig.getProperty("TransferItem", 57);
		ACC_MOVE_PRICE = Acc_moveConfig.getProperty("TranferCost", 57);
		
	}
	
	// RWHO system (off emulation)
	public static boolean RWHO_LOG;
	public static int RWHO_FORCE_INC;
	public static int RWHO_KEEP_STAT;
	public static int RWHO_MAX_ONLINE;
	public static boolean RWHO_SEND_TRASH;
	public static int RWHO_ONLINE_INCREMENT;
	public static float RWHO_PRIV_STORE_FACTOR;
	public static int RWHO_ARRAY[] = new int[13];
	
	public static void loadTeamVSTeamSettings()
	{
		ExProperties eventTeamVSTeamSettings = load(EVENT_TEAM_VS_TEAM_CONFIG_FILE);
		
		ENABLE_OLD_TVT = eventTeamVSTeamSettings.getProperty("EnableOldTVT", false);
		EVENT_TvTRewards = eventTeamVSTeamSettings.getProperty("TvT_Rewards", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTTime = eventTeamVSTeamSettings.getProperty("TvT_time", 3);
		EVENT_TvTStartTime = eventTeamVSTeamSettings.getProperty("TvT_StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_TvTCategories = eventTeamVSTeamSettings.getProperty("TvT_Categories", false);
		EVENT_TvTMaxPlayerInTeam = eventTeamVSTeamSettings.getProperty("TvT_MaxPlayerInTeam", 20);
		EVENT_TvTMinPlayerInTeam = eventTeamVSTeamSettings.getProperty("TvT_MinPlayerInTeam", 2);
		EVENT_TvTAllowSummons = eventTeamVSTeamSettings.getProperty("TvT_AllowSummons", false);
		EVENT_TvTAllowBuffs = eventTeamVSTeamSettings.getProperty("TvT_AllowBuffs", false);
		EVENT_TvTAllowMultiReg = eventTeamVSTeamSettings.getProperty("TvT_AllowMultiReg", false);
		EVENT_TvTCheckWindowMethod = eventTeamVSTeamSettings.getProperty("TvT_CheckWindowMethod", "IP");
		EVENT_TvTEventRunningTime = eventTeamVSTeamSettings.getProperty("TvT_EventRunningTime", 20);
		EVENT_TvTFighterBuffs = eventTeamVSTeamSettings.getProperty("TvT_FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTMageBuffs = eventTeamVSTeamSettings.getProperty("TvT_MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTBuffPlayers = eventTeamVSTeamSettings.getProperty("TvT_BuffPlayers", false);
		EVENT_TvTrate = eventTeamVSTeamSettings.getProperty("TvT_rate", true);
		EVENT_TvTOpenCloseDoors = eventTeamVSTeamSettings.getProperty("TvT_OpenCloseDoors", new int[]
		{
			24190001,
			24190002,
			24190003,
			24190004
		});
		EVENT_TvT_DISALLOWED_SKILLS = eventTeamVSTeamSettings.getProperty("TvT_DisallowedSkills", "").trim().replaceAll(" ", "").split(";");
		
	}
	
	public static void loadCaptureTheFlagSettings()
	{
		ExProperties eventCaptureTheFlagSettings = load(EVENT_CAPTURE_THE_FLAG_CONFIG_FILE);
		
		ENABLE_OLD_CTF = eventCaptureTheFlagSettings.getProperty("EnableOldCTF", false);
		
		EVENT_CtFRewards = eventCaptureTheFlagSettings.getProperty("CtF_Rewards", "").trim().replaceAll(" ", "").split(";");
		EVENT_CtfTime = eventCaptureTheFlagSettings.getProperty("CtF_time", 3);
		EVENT_CtFrate = eventCaptureTheFlagSettings.getProperty("CtF_rate", true);
		EVENT_CtFStartTime = eventCaptureTheFlagSettings.getProperty("CtF_StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_CtFCategories = eventCaptureTheFlagSettings.getProperty("CtF_Categories", false);
		EVENT_CtFMaxPlayerInTeam = eventCaptureTheFlagSettings.getProperty("CtF_MaxPlayerInTeam", 20);
		EVENT_CtFMinPlayerInTeam = eventCaptureTheFlagSettings.getProperty("CtF_MinPlayerInTeam", 2);
		EVENT_CtFAllowSummons = eventCaptureTheFlagSettings.getProperty("CtF_AllowSummons", false);
		EVENT_CtFAllowBuffs = eventCaptureTheFlagSettings.getProperty("CtF_AllowBuffs", false);
		EVENT_CtFAllowMultiReg = eventCaptureTheFlagSettings.getProperty("CtF_AllowMultiReg", false);
		EVENT_CtFCheckWindowMethod = eventCaptureTheFlagSettings.getProperty("CtF_CheckWindowMethod", "IP");
		EVENT_CtFFighterBuffs = eventCaptureTheFlagSettings.getProperty("CtF_FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_CtFMageBuffs = eventCaptureTheFlagSettings.getProperty("CtF_MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_CtFBuffPlayers = eventCaptureTheFlagSettings.getProperty("CtF_BuffPlayers", false);
		EVENT_CtFOpenCloseDoors = eventCaptureTheFlagSettings.getProperty("CtF_OpenCloseDoors", new int[]
		{
			24190001,
			24190002,
			24190003,
			24190004
		});
		EVENT_CtF_DISALLOWED_SKILLS = eventCaptureTheFlagSettings.getProperty("CtF_DisallowedSkills", "").trim().replaceAll(" ", "").split(";");
		
	}
	
	public static void loadOfflineConfig()
	{
		ExProperties offlineConfig = load(OFFLINE_CONFIG_FILE);
		
		SERVICES_OFFLINE_TRADE_ALLOW = offlineConfig.getProperty("AllowOfflineTrade", false);
		SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE = offlineConfig.getProperty("AllowOfflineTradeOnlyOffshore", true);
		SERVICES_OFFLINE_TRADE_MIN_LEVEL = offlineConfig.getProperty("OfflineMinLevel", 0);
		SERVICES_OFFLINE_TRADE_NAME_COLOR = Integer.decode("0x" + offlineConfig.getProperty("OfflineTradeNameColor", "B0FFFF"));
		SERVICES_OFFLINE_TRADE_PRICE_ITEM = offlineConfig.getProperty("OfflineTradePriceItem", 0);
		SERVICES_OFFLINE_TRADE_PRICE = offlineConfig.getProperty("OfflineTradePrice", 0);
		SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK = offlineConfig.getProperty("OfflineTradeDaysToKick", 14) * 86400L;
		SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART = offlineConfig.getProperty("OfflineRestoreAfterRestart", true);
		TRANSFORMATION_ID_MALE = offlineConfig.getProperty("TransformationIdMale", 20005);
		TRANSFORMATION_ID_FEMALE = offlineConfig.getProperty("TransformationIdFemale", 20006);
		TRANSFORM_ON_OFFLINE_TRADE = offlineConfig.getProperty("TransformOnOfflineTrade", false);
		

		SENDSTATUS_TRADE_JUST_OFFLINE = offlineConfig.getProperty("SendStatusTradeJustOffline", false);
		SENDSTATUS_TRADE_MOD = offlineConfig.getProperty("SendStatusTradeMod", 1.);
		SHOW_OFFLINE_MODE_IN_ONLINE = offlineConfig.getProperty("ShowOfflineTradeInOnline", false);
		
		SERVICES_NO_TRADE_ONLY_OFFLINE = offlineConfig.getProperty("NoTradeOnlyOffline", false);
		SERVICES_NO_TRADE_BLOCK_ZONE = offlineConfig.getProperty("NoTradeBlockZone", false);
		SERVICES_TRADE_TAX = offlineConfig.getProperty("TradeTax", 0.0);
		SERVICES_OFFSHORE_TRADE_TAX = offlineConfig.getProperty("OffshoreTradeTax", 0.0);
		SERVICES_TRADE_TAX_ONLY_OFFLINE = offlineConfig.getProperty("TradeTaxOnlyOffline", false);
		SERVICES_OFFSHORE_NO_CASTLE_TAX = offlineConfig.getProperty("NoCastleTaxInOffshore", false);
		SERVICES_TRADE_ONLY_FAR = offlineConfig.getProperty("TradeOnlyFar", false);
		SERVICES_TRADE_MIN_LEVEL = offlineConfig.getProperty("MinLevelForTrade", 0);
		SERVICES_TRADE_RADIUS = offlineConfig.getProperty("TradeRadius", 30);
		
		SERVICES_GIRAN_HARBOR_ENABLED = offlineConfig.getProperty("GiranHarborZone", false);
		SERVICES_PARNASSUS_ENABLED = offlineConfig.getProperty("ParnassusZone", false);
		SERVICES_PARNASSUS_NOTAX = offlineConfig.getProperty("ParnassusNoTax", false);
		SERVICES_PARNASSUS_PRICE = offlineConfig.getProperty("ParnassusPrice", 500000);
		
		RESTORE_OFFLINE_BUFFERS_ON_RESTART = offlineConfig.getProperty("RestoreOfflineBuffers", false);
		
		OFFLINE_BUFFER_NAME_COLOR = Integer.decode("0x" + offlineConfig.getProperty("OfflineBuffNameColor", "FBB117"));
		OFFLINE_BUFFER_TITLE_COLOR = Integer.decode("0x" + offlineConfig.getProperty("OfflineBuffTitleColor", "F9966B"));
		
		ENABLE_OFFLINE_BUFFERS = Boolean.parseBoolean(offlineConfig.getProperty("EnableOfflineBuffers", "False"));
		if (ENABLE_OFFLINE_BUFFERS) // create map if system is enabled
		{
			SELL_BUFF_SKILL_LIST = new HashMap<Integer, Integer>();
			
			String[] skillsplit;
			skillsplit = offlineConfig.getProperty("SellBuffSkillList", "").split(";");
			
			for (String skill : skillsplit)
			{
				String[] skillSplit = skill.split(",");
				if (skillSplit.length != 2)
				{
					_log.error("[SellBuffSkillList]: invalid config property -> SellBuffSkillList \"" + skill + "\"");
				}
				else
				{
					try
					{
						SELL_BUFF_SKILL_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						nfe.printStackTrace();
						
						if (!skill.equals(""))
						{
							_log.error("[SellBuffSkillList]: invalid config property -> SellBuffSkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
						}
					}
				}
			}
		}
		if (ENABLE_OFFLINE_BUFFERS) // create map if system is enabled
		{
			String SELL_BUFF_CLASS_STRING = offlineConfig.getProperty("SellBuffClassList");
			int i = 0;
			for (String classId : SELL_BUFF_CLASS_STRING.split(","))
			{
				SELL_BUFF_CLASS_LIST.add(i, classId);
				i++;
			}
		}
		ALLOW_PARTY_BUFFS = Boolean.parseBoolean(offlineConfig.getProperty("AllowPartyBuffs", "True"));
		ALLOW_CLAN_BUFFS = Boolean.parseBoolean(offlineConfig.getProperty("AllowClanBuffs", "True"));
		SELL_BUFF_PUNISHED_PRICE = Integer.parseInt(offlineConfig.getProperty("SellBuffPunishedPrice", "10000"));
		MINIMUM_PRICE_FOR_OFFLINE_BUFF = Integer.parseInt(offlineConfig.getProperty("MinimumPriceForOfflineBuffs", "10000"));
		SELL_BUFF_FILTER_ENABLED = Boolean.parseBoolean(offlineConfig.getProperty("SellBuffFilterEnabled", "True"));
		OFFLINE_SELLBUFF_ONLY_IN_ZONE = Boolean.parseBoolean(offlineConfig.getProperty("SetOfflineBufferOnlyInSpecificZone", "True"));
		
		SELL_BUFF_MIN_LVL = Integer.parseInt(offlineConfig.getProperty("SellBuffMinLvl", "20"));
		
		OFFLINE_SELLBUFF_ENABLED = Boolean.parseBoolean(offlineConfig.getProperty("OfflineSellbuffEnabled", "True"));
		SELL_BUFF_SKILL_MP_ENABLED = Boolean.parseBoolean(offlineConfig.getProperty("SellBuffSkillMpEnabled", "False"));
		SELL_BUFF_SKILL_MP_MULTIPLIER = Double.parseDouble(offlineConfig.getProperty("SellBuffSkillMpMultiplier", "1."));
		SELL_BUFF_SKILL_ITEM_CONSUME_ENABLED = Boolean.parseBoolean(offlineConfig.getProperty("SellBuffSkillItemConsumeEnabled", "True"));
		
		CHECK_PRIVATE_SHOPS = offlineConfig.getProperty("CheckPrivateStoreShops", false);
		
	}
	public static void load()
	{
		loadServerConfig();
		loadTelnetConfig();
		loadResidenceConfig();
		loadOtherConfig();
		loadSpoilConfig();
		loadFormulasConfig();
		loadAltSettings();
		loadServicesSettings();
		loadPvPSettings();
		loadAISettings();
		loadGeodataSettings();
		loadGMSettings();
		loadEventsSettings();
		loadOlympiadSettings();
		loadDevelopSettings();
		loadExtSettings();
		loadTopSettings();
		loadRatesConfig();
		loadFightClubSettings();
		loadItemsUseConfig();
		loadChatConfig();
		loadBossConfig();
		loadEpicBossConfig();
		loadWeddingConfig();
		loadInstancesConfig();
		loadItemsSettings();
		abuseLoad();
		loadPremiumConfig();
		loadTriviaSettings();
		if (ADVIPSYSTEM)
			ipsLoad();
		if (ALLOW_QUETS_ADDONS_CONFIG)
			loadQuestAddon();
		// комюнити
		loadCommunityboardsettings();
		loadCommunitybuffersettings();
		loadCommunityboardComission();
		loadCommunityclasssettings();
		loadCommunityteleportsettings();
		loadEnchantCBConfig();
		loadCommandssettings();
		loadAcc_moveConfig();
		loadTeamVSTeamSettings();
		loadCaptureTheFlagSettings();
		LoadCustom_Config();
		LoadCustomSecurity_Config();
		LoadSMTP_Config();
		loadOfflineConfig();
		loadPhantomsConfig();
		loadDonateRewardSettings();
		
		_log.info("All Config files has been loaded!");
	}
	
	private Config()
	{
	}
	
	public static void abuseLoad()
	{
		List<Pattern> tmp = new ArrayList<Pattern>();
		
		LineNumberReader lnr = null;
		try
		{
			String line;
			
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(getFile(ANUSEWORDS_CONFIG_FILE)), "UTF-8"));
			
			while ((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
					tmp.add(Pattern.compile(".*" + st.nextToken() + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
			}
			
			ABUSEWORD_LIST = tmp.toArray(new Pattern[tmp.size()]);
			tmp.clear();
			_log.info("Abuse: Loaded " + ABUSEWORD_LIST.length + " abuse words.");
		}
		catch (IOException e1)
		{
			_log.warn("Error reading abuse: " + e1);
		}
		finally
		{
			try
			{
				if (lnr != null)
					lnr.close();
			}
			catch (Exception e2)
			{
				// nothing
			}
		}
	}
	
	public static String getField(String fieldName)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);
		
		if (field == null)
			return "Config " + fieldName + " not found!"; 
		
		try
		{
			return String.valueOf(field.get(null));
		}
		catch (IllegalArgumentException e)
		{
			
		}
		catch (IllegalAccessException e)
		{
			
		}
		
		return null;
	}
	
	public static boolean setField(String fieldName, String value)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);
		
		if (field == null)
			return false;
		
		try
		{
			if (field.getType() == boolean.class)
				field.setBoolean(null, BooleanUtils.toBoolean(value));
			else if (field.getType() == int.class)
				field.setInt(null, NumberUtils.toInt(value));
			else if (field.getType() == long.class)
				field.setLong(null, NumberUtils.toLong(value));
			else if (field.getType() == double.class)
				field.setDouble(null, NumberUtils.toDouble(value));
			else if (field.getType() == String.class)
				field.set(null, value);
			else
				return false;
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}
		catch (IllegalAccessException e)
		{
			return false;
		}
		
		return true;
	}
	
	public static ExProperties load(String filename)
	{
		return load(getFile(filename));
	}
	
	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();
		
		try
		{
			result.load(file);
		}
		catch (IOException e)
		{
			_log.error("Error loading config : " + file.getName() + "!");
		}
		
		return result;
	}
	
    private static ConcurrentHashMap<String, String> _properties = new ConcurrentHashMap<String, String>();
    private static ConcurrentHashMap<String, Double> _questRewardRates = new ConcurrentHashMap<String, Double>();
    private static ConcurrentHashMap<String, Double> _questDropRates = new ConcurrentHashMap<String, Double>();
    private static ConcurrentHashMap<String, Double> _questRewardExpSp = new ConcurrentHashMap<String, Double>();    
	public static TIntIntHashMap SKILL_REUSE_LIST;

	public static void loadQuestAddon()
	{
		File files = Config.getFile("./config/quests");
		if (!files.exists())
			_log.warn("WARNING! " + files.getPath() + " not exists! Config not loaded!");
		else
		{
			synchronized (_properties)
			{
				synchronized (_questRewardRates)
				{
					synchronized (_questDropRates)
					{
						_properties = new ConcurrentHashMap<String, String>();
						_questRewardRates = new ConcurrentHashMap<String, Double>();
						_questDropRates = new ConcurrentHashMap<String, Double>();
						parseAddonFile(files.listFiles());
					}
				}
			}
		}
	}
	
	private static void parseAddonFile(File ... files)
	{
		for (File file : files)
		{
			if (file.isHidden())
				continue;
			if (file.isDirectory() && !file.getName().contains("defaults"))
				parseAddonFile(file.listFiles());
			try
			{
				Properties props = new Properties();
				props.load(new FileInputStream(file));
				
				if (file.getName().startsWith("quest_drop_rates"))
				{
					for (String name : props.stringPropertyNames())
					{
						//int id = Integer.parseInt(name);
						if (props.getProperty(name) == null)
							_log.info("Null property for quest name " + name);
						else if (_questRewardRates.replace(name, Double.parseDouble(props.getProperty(name).trim())) != null)
							_log.info("Duplicate quest name \"" + name + "\". Replaced.");
						else
							_questRewardRates.put(name, Double.parseDouble(props.getProperty(name).trim()));
					}
				}
				else if (file.getName().startsWith("quest_reward_rates"))
				{
					for (String name : props.stringPropertyNames())
					{
						//int id = Integer.parseInt(name);
						if (props.getProperty(name) == null)
							_log.info("Null property for quest name " + name);
						else if (_questRewardRates.replace(name, Double.parseDouble(props.getProperty(name).trim())) != null)
							_log.info("Duplicate quest name \"" + name + "\". Replaced.");
						else
							_questRewardRates.put(name, Double.parseDouble(props.getProperty(name).trim()));
					}
				}
				else if (file.getName().startsWith("quest_reward_expsp"))
				{
					for (String name : props.stringPropertyNames())
					{
						//int id = Integer.parseInt(name);
						if (props.getProperty(name) == null)
							_log.info("Null property for quest name " + name);
						else if (_questRewardRates.replace(name, Double.parseDouble(props.getProperty(name).trim())) != null)
							_log.info("Duplicate quest name \"" + name + "\". Replaced.");
						else
							_questRewardExpSp.put(name, Double.parseDouble(props.getProperty(name).trim()));
					}
				}
				else if (file.getName().endsWith(".properties"))
				{
					for (String name : props.stringPropertyNames())
					{
						if (props.getProperty(name) == null)
							_log.info("Null property for key " + name);
						else if (_properties.replace(name, props.getProperty(name).trim()) != null)
							_log.info("Duplicate properties name \"" + name + "\" replaced with new value.");
						else
							_properties.put(name, props.getProperty(name).trim());
					}
				}
				
				props.clear();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (NumberFormatException nfe)
			{
				_log.warn("Skipped Quest Addon property due to wrong value: ", nfe);
			}
		}
	}
	
	public static double getQuestRewardExSp(Quest q)
	{
		return _questRewardRates.containsKey(q.getClass().getSimpleName()) ? _questRewardRates.get(q.getClass().getSimpleName()) : -1;
	}
	
	public static double getQuestRewardRates(Quest q)
	{
		return _questRewardRates.containsKey(q.getClass().getSimpleName()) ? _questRewardRates.get(q.getClass().getSimpleName()) : -1;
	}
	
	public static double getQuestDropRates(Quest q)
	{
		return _questDropRates.containsKey(q.getClass().getSimpleName()) ? _questDropRates.get(q.getClass().getSimpleName()) : -1;
	}
	
	public static String get(String name)
	{
		if (_properties.get(name) == null)
			_log.warn("Config: quests - Null value for key: " + name);
		return _properties.get(name);
	}
	
	public static float getFloat(String name)
	{
		return getFloat(name, Float.MAX_VALUE);
	}
	
	public static boolean getBoolean(String name)
	{
		return getBoolean(name, false);
	}
	
	public static int getInt(String name)
	{
		return getInt(name, Integer.MAX_VALUE);
	}
	
	public static int[] getIntArray(String name)
	{
		return getIntArray(name, new int[0]);
	}
	
	public static int getIntHex(String name)
	{
		return getIntHex(name, Integer.decode("0xFFFFFF"));
	}
	
	public static byte getByte(String name)
	{
		return getByte(name, Byte.MAX_VALUE);
	}
	
	public static long getLong(String name)
	{
		return getLong(name, Long.MAX_VALUE);
	}
	
	public static double getDouble(String name)
	{
		return getDouble(name, Double.MAX_VALUE);
	}
	
	public static String get(String name, String def)
	{
		return get(name) == null ? def : get(name);
	}
	
	public static float getFloat(String name, float def)
	{
		return Float.parseFloat(get(name, String.valueOf(def)));
	}
	
	public static boolean getBoolean(String name, boolean def)
	{
		return Boolean.parseBoolean(get(name, String.valueOf(def)));
	}
	
	public static int getInt(String name, int def)
	{
		return Integer.parseInt(get(name, String.valueOf(def)));
	}
	
	public static int[] getIntArray(String name, int[] def)
	{
		return get(name, null) == null ? def : Util.parseCommaSeparatedIntegerArray(get(name, null));
	}
	
	public static int getIntHex(String name, int def)
	{
		if (!get(name, String.valueOf(def)).trim().startsWith("0x"))
			return Integer.decode("0x" + get(name, String.valueOf(def)));
		else
			return Integer.decode(get(name, String.valueOf(def)));
	}
	
	public static byte getByte(String name, byte def)
	{
		return Byte.parseByte(get(name, String.valueOf(def)));
	}
	
	public static double getDouble(String name, double def)
	{
		return Double.parseDouble(get(name, String.valueOf(def)));
	}
	
	public static long getLong(String name, long def)
	{
		return Long.parseLong(get(name, String.valueOf(def)));
	}
	
	public static void set(String name, String param)
	{
		_properties.replace(name, param);
	}
	
	public static void set(String name, Object obj)
	{
		set(name, String.valueOf(obj));
	}
	
	public static void loadSkillDurationList()
	{
		if (getBoolean("EnableModifySkillDuration"))
		{
			String[] propertySplit = get("SkillDurationList").split(";");
			SKILL_DURATION_LIST = new TIntIntHashMap(propertySplit.length);
			for (String skill : propertySplit)
			{
				String[] skillSplit = skill.split(",");
				if (skillSplit.length != 2)
					_log.warn(concat("[SkillDurationList]: invalid config property -> SkillDurationList \"", skill, "\""));
				else
				{
					try
					{
						SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!skill.isEmpty())
						{
							_log.warn(concat("[SkillDurationList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
						}
					}
				}
			}
		}
	}
	
	public static void loadSkillReuseList()
	{
		if (getBoolean("EnableModifySkillReuse"))
		{
			String[] propertySplit = get("SkillReuseList").split(";");
			SKILL_REUSE_LIST = new TIntIntHashMap(propertySplit.length);
			for (String skill : propertySplit)
			{
				String[] skillSplit = skill.split(",");
				if (skillSplit.length != 2)
					_log.warn(concat("[SkillReuseList]: invalid config property -> SkillReuseList \"", skill, "\""));
				else
				{
					try
					{
						SKILL_REUSE_LIST.put(Integer.valueOf(skillSplit[0]), Integer.valueOf(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!skill.isEmpty())
							_log.warn(concat("[SkillReuseList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
					}
				}
			}
		}
	}
	
	public static String concat(final String... strings)
	{
		final StringBuilder sbString = new StringBuilder(getLength(strings));
		for (final String string : strings)
		{
			sbString.append(string);
		}
		return sbString.toString();
	}

	private static int getLength(final String[] strings)
	{
		int length = 0;
		for (final String string : strings)
		{
			length += string.length();
		}
		return length;
	}
	
	public static boolean containsAbuseWord(String s)
	{
		for (Pattern pattern : ABUSEWORD_LIST)
			if (pattern.matcher(s).matches())
				return true;
		return false;
	}
	
	private static void ipsLoad()
	{
		ExProperties ipsSettings = load(ADV_IP_FILE);
		
		String NetMask;
		String ip;
		for (int i = 0; i < ipsSettings.size() / 2; i++)
		{
			NetMask = ipsSettings.getProperty("NetMask" + (i + 1));
			ip = ipsSettings.getProperty("IPAdress" + (i + 1));
			for (String mask : NetMask.split(","))
			{
				AdvIP advip = new AdvIP();
				advip.ipadress = ip;
				advip.ipmask = mask.split("/")[0];
				advip.bitmask = mask.split("/")[1];
				GAMEIPS.add(advip);
			}
		}
	}
	
	public static int getLimit(TIntIntHashMap configMap, Creature creature)
	{
		if (creature.isPlayer())
		{
			ClassId classId = creature.getPlayer().getClassId();
			if (configMap.containsKey(classId.getId()))
				return configMap.get(classId.getId());
			else
			{
				for (ClassId parent = classId.getParent(creature.getPlayer().getSex()); parent != null; parent = parent.getParent(creature.getPlayer().getSex()))
				{
					if (configMap.containsKey(parent.getId()))
					{
						int limit = configMap.get(parent.getId());
						configMap.put(classId.getId(), limit); // Cache here so we don't search like that next time.
						return limit;
					}
				}
			}
		}
		
		return configMap.get(-1);
	}
	
	public static File getFile(String filename)
	{
		if (Files.isReadable(Paths.get(GameServer.getArgumentValue("configdir", "") + filename)))
			return new File(GameServer.getArgumentValue("configdir", "") + filename);
		
		_log.warn("File not found or is not readable: " + GameServer.getArgumentValue("configdir", "") + filename);
		return null;
	}
	
	public static String getFilebyName(String filename)
	{
		if (Files.isReadable(Paths.get(GameServer.getArgumentValue("configdir", "") + filename)))
			return GameServer.getArgumentValue("configdir", "") + filename;
		
		return "";
	}
}