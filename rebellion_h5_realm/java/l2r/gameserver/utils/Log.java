package l2r.gameserver.utils;

import l2r.commons.text.PrintfFormat;
import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.tables.AdminTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log
{
	public static final PrintfFormat LOG_BOSS_KILLED = new PrintfFormat("%s: %s[%d] killed by %s at Loc(%d %d %d) in %s");
	public static final PrintfFormat LOG_BOSS_RESPAWN = new PrintfFormat("%s: %s[%d] scheduled for respawn in %s at %s");

	private static final Logger _log = LoggerFactory.getLogger(Log.class);
	
	private static final Logger _logChat = LoggerFactory.getLogger("chat");
	private static final Logger _logEvents = LoggerFactory.getLogger("events");
	private static final Logger _logGm = LoggerFactory.getLogger("gmactions");
	private static final Logger _logItems = LoggerFactory.getLogger("item");
	private static final Logger _logGame = LoggerFactory.getLogger("game");
	private static final Logger _logDebug = LoggerFactory.getLogger("debug");
	private static final Logger _logDonations = LoggerFactory.getLogger("donations");
	private static final Logger _LogVoteReward = LoggerFactory.getLogger("votereward");
	private static final Logger _logAuctionAchievements = LoggerFactory.getLogger("auctionachievements");
	private static final Logger _logOlympiad = LoggerFactory.getLogger("olympiad");
	private static final Logger _logEnchant = LoggerFactory.getLogger("enchant");
	private static final Logger _logBots = LoggerFactory.getLogger("bot");
	private static final Logger _logBans = LoggerFactory.getLogger("secondarybans");

	public static final String Create = "Create";
	public static final String Delete = "Delete";
	public static final String Modifiy = "Modify";
	public static final String Drop = "Drop";
	public static final String PvPDrop = "PvPDrop";
	public static final String Crystalize = "Crystalize";
	public static final String EnchantFail = "EnchantFail";
	public static final String EnchantSuccess = "EnchantSuccess";
	public static final String AttributeFail = "AttributeFail";
	public static final String AttributeSuccess = "AttributeSuccess";
	public static final String AttributeRemove = "AttributeRemove";
	public static final String Pickup = "Pickup";
	public static final String PartyPickup = "PartyPickup";
	public static final String PrivateStoreBuy = "PrivateStoreBuy";
	public static final String PrivateStoreSell = "PrivateStoreSell";
	public static final String TradeBuy = "TradeBuy";
	public static final String TradeSell = "TradeSell";
	public static final String PostRecieve = "PostRecieve";
	public static final String PostSend = "PostSend";
	public static final String PostCancel = "PostCancel";
	public static final String PostExpire = "PostExpire";
	public static final String RefundSell = "RefundSell";
	public static final String RefundReturn = "RefundReturn";
	public static final String WarehouseDeposit = "WarehouseDeposit";
	public static final String WarehouseWithdraw = "WarehouseWithdraw";
	public static final String FreightWithdraw = "FreightWithdraw";
	public static final String FreightDeposit = "FreightDeposit";
	public static final String ClanWarehouseDeposit = "ClanWarehouseDeposit";
	public static final String ClanWarehouseWithdraw = "ClanWarehouseWithdraw";
	public static final String AdminCreateItem = "AdminCreateItem";
	public static final String NpcCreateItem = "NpcCreateItem";
	public static final String GiveItemToPet = "GiveItemToPet";
	public static final String GetItemFromPet = "GetItemFromPet";
	public static final String MailRecive = "MailRecive";
	public static final String MailSend = "MailSend";
	public static final String MailCancel = "MailCancel";
	public static final String VoteReward = "VoteReward";
	public static final String Auction = "Auction";
	public static final String Achievements = "Achievements";

	public static void addGame(PrintfFormat fmt, Object[] o, String cat)
	{
		addGame(fmt.sprintf(o), cat);
	}

	public static void addGame(String fmt, Object[] o, String cat)
	{
		addGame(new PrintfFormat(fmt).sprintf(o), cat);
	}

	public static void addGame(String text, String cat, Player player)
	{
		StringBuilder output = new StringBuilder();

		output.append(cat);
		if(player != null)
		{
			output.append(' ');
			output.append(player);
		}
		output.append(' ');
		output.append(text);

		_logGame.info(output.toString());
	}

	public static void addGame(String text, String cat)
	{
		addGame(text, cat, null);
	}

	public static void debug(String text)
	{
		_logDebug.debug(text);
	}

	public static void debug(String text, Throwable t)
	{
		_logDebug.debug(text, t);
	}

	public static void olympiad(String text)
	{
		_logOlympiad.info(text);
	}
	
	public static void enchant(String text)
	{
		_logEnchant.info(text);
	}
	
	public static void bots(String text)
	{
		_logBots.info(text);
	}
	
	public static void hwidBan(String text)
	{
		_logBans.info(text);
	}
	
	public static void LogChat(String type, String player, String target, String text, int identifier)
	{
		if(!Config.LOG_CHAT)
			return;

		StringBuilder output = new StringBuilder();
		output.append(type);
		if(identifier > 0)
		{
			output.append(' ');
			output.append(identifier);
		}
		output.append(' ');
		output.append('[');
		output.append(player);
		if(target != null)
		{
			output.append(" -> ");
			output.append(target);
		}
		output.append(']');
		output.append(' ');
		output.append(text);

		_logChat.info(output.toString());
	}

	public static void LogEvents(String name, String action, String player, String target, String text)
	{
		StringBuilder output = new StringBuilder();
		output.append(name);
		output.append(": ");
		output.append(action);
		output.append(' ');
		output.append('[');
		output.append(player);
		if(target != null)
		{
			output.append(" -> ");
			output.append(target);
		}
		output.append(']');
		output.append(' ');
		output.append(text);

		_logEvents.info(output.toString());
	}

	public static void LogCommand(Player player, GameObject target, String command, boolean success)
	{
		StringBuilder output = new StringBuilder();

		if(success)
			output.append("SUCCESS");
		else
			output.append("FAIL   ");

		output.append(' ');
		output.append(player);
		if(target != null)
		{
			output.append(" -> ");
			output.append(target);
		}
		output.append(' ');
		output.append(command);

		_logGm.info(output.toString());
	}

	public static void LogItem(Creature activeChar, String process, ItemInstance item)
	{
		LogItem(activeChar, process, item, item.getCount());
	}

	public static void LogItem(Creature activeChar, String process, ItemInstance item, long count)
	{
		StringBuilder output = new StringBuilder();
		output.append(process);
		output.append(' ');
		output.append(item);
		output.append(' ');
		output.append(activeChar);
		output.append(' ');
		output.append(count);

		_logItems.info(output.toString());
		
		if (item != null && item.getItemId() == 13693)
		{
			StringBuilder donate = new StringBuilder();
			donate.append(process);
			donate.append(' ');
			donate.append(item);
			donate.append(' ');
			donate.append(activeChar);
			donate.append(' ');
			donate.append(count);

			_logDonations.info(donate.toString());
		}
	}
	
	public static void addDonation(PrintfFormat fmt, Object[] o, String cat)
	{
		addDonation(fmt.sprintf(o), cat);
	}

	public static void addDonation(String fmt, Object[] o, String cat)
	{
		addDonation(new PrintfFormat(fmt).sprintf(o), cat);
	}

	public static void addDonation(String text, String cat)
	{
		addDonation(text, cat, null);
	}
	
	public static void addDonation(String text, String cat, Player player)
	{
		StringBuilder output = new StringBuilder();

		output.append(cat);
		if(player != null)
		{
			output.append(' ');
			output.append(player);
		}
		output.append(' ');
		output.append(text);

		_logDonations.info(output.toString());
	}
	
	public static void voteReward(String text)
	{
		_LogVoteReward.info(text);
	}
	
	public static void auction(String text)
	{
		_logAuctionAchievements.info(text);
	}
	
	public static void achievements(String text)
	{
		_logAuctionAchievements.info(text);
	}
	
	public static void IllegalPlayerAction(Player player, String msg, int jailItems)
	{
		if (player == null)
			return;
		
		msg = "Illegal " + player.toString() + " action :" + msg;
		
		_log.warn(msg);
		
		AdminTable.broadcastMessageToGMs(msg);
		
		// TODO реализовать тюрьму :)
	}
}