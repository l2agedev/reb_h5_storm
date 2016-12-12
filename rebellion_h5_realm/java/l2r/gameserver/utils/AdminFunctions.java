package l2r.gameserver.utils;

import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.ChangeAccessLevel;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;

import ru.akumu.smartguard.manager.bans.BanManager;
import ru.akumu.smartguard.manager.bans.model.Ban;
import ru.akumu.smartguard.manager.session.model.HWID;

public final class AdminFunctions
{
	public final static Location JAIL_SPAWN = new Location(-114648, -249384, -2984);
	
	private AdminFunctions() {}
	
	/**
	 * 
	 * @param playerName
	 * @param time : 0 is infinite.
	 * @param reason
	 * @param bannerName
	 * @return
	 */
	public static String ban(String playerName, int time, String reason, String bannerName)
	{
		if (playerName == null || playerName.isEmpty())
			return "PlayerName is null or empty.";
		Player gm = World.getPlayer(bannerName);
		try
		{
			Player plyr = World.getPlayer(playerName);
			
			if(plyr != null)
			{
				plyr.sendMessage(new CustomMessage("admincommandhandlers.YoureBannedByGM", plyr));
				plyr.setAccessLevel(-100);
				AutoBan.Banned(plyr, time, reason, bannerName);
				plyr.kick();
				if (gm != null)
					gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message17", gm).addString(plyr.getName()));
				
				return "Player found ingame. Kicked and banned!";
			}
			else if(AutoBan.Banned(playerName, -100, time, reason, bannerName))
			{
				if (gm != null)
					gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message17", gm).addString(playerName));
				
				return "Player not found ingame. Banned offline!";
			}
			else
			{
				 if (gm != null)
					 gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message13", gm).addString(playerName));
				 
				 return "Unable to ban player " + playerName;
			}
		}
		catch(Exception e)
		{
			if (gm != null)
				gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message18", gm));
			
			return "Exception while banning: " + e.getMessage();
		}
	}
	
	/**
	 * 
	 * @param playerName
	 * @param bannerName
	 * @return
	 */
	public static String unban(String playerName, String unbannerName)
	{
		if (playerName == null || playerName.isEmpty())
			return "PlayerName is null or empty.";
		Player gm = World.getPlayer(unbannerName);
		try
		{
			if (AutoBan.Banned(playerName, 0, 0, "", "Unban"))
			{
				if (gm != null)
					gm.sendMessage("Character " + playerName + " has been unbanned.");
				
				return "Player " + playerName + " has been unbanned!";
			}
			else
			{
				if (gm != null)
					gm.sendMessage("No such character!");
				
				return "No such character!";
			}
		}
		catch(Exception e)
		{
			if (gm != null)
				gm.sendMessage("Error while unbanning");
			
			return "Exception while unbanning: " + e.getMessage();
		}
	}
	
	/**
	 * 
	 * @param playerNane
	 * @param hours : -1 is infinite.
	 * @param bannerName
	 * @return
	 */
	public static String tradeBan(String playerName, int hours, String bannerName)
	{
		if (hours > 0)
			hours *= 60 * 60000;
		
		Player banned = World.getPlayer(playerName);
		Player gm = World.getPlayer(bannerName);
		if (banned != null)
		{
			banned.setVar("tradeBan", 1, hours < 0 ? -1 : (System.currentTimeMillis() + hours));
			
			if (gm != null)
				gm.sendMessage(playerName + " has been trade banned for " + hours + " hours(s).");
			
			if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
				Announcements.getInstance().announceToAll(playerName + " has been trade banned for " + hours + " hour(s).");

			Log.addGame(bannerName + ": trade banned character " + playerName + " for " + hours + " hour(s).", "tradeBan", gm);
			
			if(banned.isInOfflineMode())
			{
				banned.setOfflineMode(false);
				banned.kick();
			}
			else if(banned.isInStoreMode())
			{
				banned.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
				banned.standUp();
				banned.broadcastCharInfo();
				banned.getBuyList().clear();
			}
			
			return playerName + " has been trade banned by " + bannerName + " for " + hours + " hours(s).";
		}
		
		int objId = CharacterDAO.getInstance().getObjectIdByName(playerName);
		if (objId != 0)
		{
			Player.setVarOffline(objId, "tradeBan", 1, System.currentTimeMillis() + hours);
			
			if (gm != null)
				gm.sendMessage(playerName + " has been trade banned for " + hours + " hours(s).");
			
			Log.addGame(bannerName + ": trade banned character " + playerName + " for " + hours + " hour(s).", "tradeBan", gm);
			
			return playerName + " has been trade banned by " + bannerName + " for " + hours + " hours(s).";
		}
		
		return "Trade ban has failed because the character " + playerName + " cannot be found in the server.";
	}
	
	public static String tradeUnban(String playerName, String unbannerName)
	{
		Player banned = World.getPlayer(playerName);
		Player gm = World.getPlayer(unbannerName);
		if (banned != null)
		{
			banned.unsetVar("tradeBan");
			
			if (gm != null)
				gm.sendMessage(playerName + " has been trade unbanned.");
			
			if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
				Announcements.getInstance().announceToAll(playerName + " has been trade unbanned.");

			Log.addGame(unbannerName+ ": trade unban for character " + playerName + ".", "tradeBan", gm);
			
			return playerName + " has been trade unbanned by " + unbannerName + ".";
		}
		
		int objId = CharacterDAO.getInstance().getObjectIdByName(playerName);
		if (objId != 0)
		{
			Player.unsetVarOffline(objId, "tradeBan");
			
			if (gm != null)
				gm.sendMessage(playerName + " has been trade unbanned.");

			Log.addGame(unbannerName+ ": trade unlocked for character " + playerName + ".", "tradeBan", gm);
			
			return playerName + " has been successfully unbanned from trading by " + unbannerName;
		}
		
		return "Trade unban has failed because the character " + playerName + " cannot be found in the server.";
	}
	
	/**
	 * 
	 * @param playerName can be empty, used just to kick player
	 * @param playerAccountName
	 * @param banExpire
	 * @param bannerName
	 * @return
	 */
	public static String accountBan(String playerName, String playerAccountName, int banExpire, String bannerName)
	{
		Player banned = World.getPlayer(playerName);
		if (banned != null)
		{
			if (banned.isInOfflineMode())
				banned.setOfflineMode(false);
			banned.kick();
		}
		
		AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(playerAccountName, banExpire == 0 ? -100 : 0, banExpire));

		Functions.sendDebugMessage(World.getPlayer(bannerName), "Player account " + playerAccountName + " is banned, player " + playerName + " kicked.");
		Log.addGame(bannerName+ ": account " + playerAccountName + " banned by " + bannerName + " for " + banExpire + ".", "Account Ban");
		return "Account ban request for " + playerAccountName + " sent to loginserver by " + bannerName + ".";
	}
	
	public static String accountUnban(String playerAccountName, String unbannerName)
	{
		AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(playerAccountName, 0, 0));

		Functions.sendDebugMessage(World.getPlayer(unbannerName), "Player account " + playerAccountName + " unbanned");
		Log.addGame(unbannerName+ ": account " + playerAccountName + " unbanned by " + unbannerName + ".", "Account Unban");
		return "Account unban request for " + playerAccountName + " sent to loginserver by " + unbannerName + ".";
	}
	
	/**
	 * 
	 * @param playerName
	 * @param periodInMins : -1 is Infinite.
	 * @param jailerName
	 * @param reason
	 * @return
	 */
	public static String jail(String playerName, int periodInSecs, String jailerName, String reason)
	{
		Player target = World.getPlayer(playerName);
		Player gm = World.getPlayer(jailerName);
		if (target != null)
		{
			target.setVar("jailedFrom", target.getX() + ";" + target.getY() + ";" + target.getZ() + ";" + target.getReflectionId(), -1);
			target.setVar("jailed", 1, periodInSecs < 0 ? -1 : (System.currentTimeMillis() + periodInSecs * 1000));
			target.startUnjailTask(target, periodInSecs, false);
			target.teleToLocation(JAIL_SPAWN.findPointToStay(50, 200));
			if (target.isInStoreMode())
				target.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			
			String htmlban = HtmCache.getInstance().getNotNull("jailed_in.htm", target);
			
			if (reason.isEmpty() || reason == null)
				reason = "Missing Reason";
			
			String endDate = periodInSecs < 0 ? "never!" : "in " + (periodInSecs / 60) + " minute(s).";
			
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml(htmlban);
			html.replace("%jailedby%", jailerName);
			html.replace("%endDate%", endDate);
			html.replace("%reason%", reason);
			target.sendPacket(html);
			
			//target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message8", target).addNumber(periodInSecs / 60).addString(reason));
			
			if (gm != null)
				gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message9", gm).addString(playerName).addNumber(periodInSecs / 60).addString(reason));
			
			return playerName + " has been jailed " + (periodInSecs < 0 ? "forever" : "for " + (periodInSecs / 60)) + " minute(s) by " + jailerName + ". Reason: " + reason;
		}
		
		int objId = CharacterDAO.getInstance().getObjectIdByName(playerName);
		if (objId != 0)
		{
			Player.setVarOffline(objId, "jailed", 1, System.currentTimeMillis() + periodInSecs * 1000);
			
			if (gm != null)
				gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message9", gm).addString(playerName).addNumber(periodInSecs).addString(reason));
			
			return playerName + " has been offline jailed " + (periodInSecs < 0 ? "forever" : "for " + (periodInSecs / 60)) + " minute(s) by " + jailerName + ". Reason: " + reason;
		}
		
		if (gm != null)
			gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message10", gm).addString(playerName));
		
		return "Unable to jail " + playerName + ". Player not found in the server.";
	}
	
	public static String unJail(String playerName, String unjailerName)
	{
		Player target = World.getPlayer(playerName);
		Player gm = World.getPlayer(unjailerName);
		if (target != null)
		{
			if (target.getVar("jailed") == null)
			{
				if (gm != null)
					gm.sendMessage("Unjailing " + playerName + " had no effect because he is not jailed.");
				
				return "Unjailing " + playerName + " had no effect because he is not jailed.";
			}
			
			String[] re = target.getVar("jailedFrom").split(";");
			target.unsetVar("jailedFrom");
			target.unsetVar("jailed");
			target.teleToLocation(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
			target.setReflection(re.length > 3 ? Integer.parseInt(re[3]) : 0);
			target.stopUnjailTask();
			
			target.sendPacket(new NpcHtmlMessage(0).setFile("jailed_out.htm"));
				
			if (gm != null)
				gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message12", gm).addString(playerName));
			
			return playerName + " has been unjailed by " + unjailerName + ".";
		}
		
		int objId = CharacterDAO.getInstance().getObjectIdByName(playerName);
		if (objId != 0)
		{
			String[] re = Player.getVarFromPlayer(objId, "jailedFrom").split(";");
			CharacterDAO.getInstance().setDbLocatio(objId, Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
			
			Player.unsetVarOffline(objId, "jailed");
			Player.unsetVarOffline(objId, "jailedFrom");
			
			if (gm != null)
				gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message12", gm).addString(playerName));
			
			return playerName + " has been offline unjailed by " + unjailerName + ".";
		}
		
		if (gm != null)
			gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message13", gm).addString(playerName));
		
		return "Unable to unjail " + playerName + " he is offline.";
	}
	
	public static String hwidBan(String playerName, String bannerName, String reason)
	{
		Player gm = World.getPlayer(bannerName);
		try
		{
			Player plr = World.getPlayer(playerName);
			
			if (plr != null)
			{
				if (!plr.hasHWID())
				{
					Functions.sendDebugMessage(gm, "Player " + plr.getName() + " does not have HWID, cannot be banned.");
					return "HWID for player " + playerName + " not found.";
				}
				
				String rawhwid = plr.getHWID();
				HWID hwid = HWID.fromString(rawhwid);
				if (hwid == null)
				{
					plr.sendMessage(String.format("Hwid '%s' has bad format.", new Object[] { rawhwid }));
					return "HWID for player " + playerName + " not found.";
				}
				
				Ban ban = new Ban(hwid, reason);
				BanManager.addBan(ban);
				
				Functions.sendDebugMessage(gm, "Player " + plr.getName() + " is banned by hwid.");
				return "Player " + playerName + " has been successfully HWID banned on " + hwid + " by " + bannerName + ". Reason: " + reason;
			}
			
			/*String accountName = CharacterDAO.getInstance().getAccountName(playerName);
			if (accountName != null && !accountName.isEmpty())
			{
				// Get offline hwid somehow
			}*/
			if (gm != null)
				gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message15", gm));
		}
		catch (Exception e)
		{
			if (gm != null)
				gm.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message16", gm));
			
			return "Unable to HWID ban " + playerName + " by " + bannerName + " due to an Exception: " +e.getMessage();
		}
		
		return "Player " + playerName + " not found.";
	}

	public static boolean kick(String player, String reason)
	{
		Player plyr = World.getPlayer(player);
		if (plyr == null)
			return false;

		if(Config.ALLOW_CURSED_WEAPONS && Config.DROP_CURSED_WEAPONS_ON_KICK)
			if(plyr.isCursedWeaponEquipped())
			{
				plyr.setPvpFlag(0);
				CursedWeaponsManager.getInstance().dropPlayer(plyr);
			}

		if(plyr.isInOfflineMode())
			plyr.setOfflineMode(false);
		
		plyr.kick();
		
		return true;
	}

	public static String banChat(Player adminChar, String adminName, String charName, int val, String reason)
	{
		Player player = World.getPlayer(charName);

		if(player != null)
			charName = player.getName();
		else if(CharacterDAO.getInstance().getObjectIdByName(charName) == 0)
			return "Игрок " + charName + " не найден.";

		if((adminName == null || adminName.isEmpty()) && adminChar != null)
			adminName = adminChar.getName();

		if(reason == null || reason.isEmpty())
			reason = "не указана"; // if no args, then "не указана" default.

		String result, announce = null;
		if(val == 0) //unban
		{
			if(adminChar != null)
				return "Вы не имеете прав на снятие бана чата.";
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " снял бан чата с игрока " + charName + "." : "С игрока " + charName + " снят бан чата.";
			Log.addGame(adminName + " снял бан чата с игрока " + charName + ".", "banchat", adminChar);
			result = "Вы сняли бан чата с игрока " + charName + ".";
		}
		else if(val < 0)
		{
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " забанил чат игроку " + charName + " на бессрочный период, причина: " + reason + "." : "Забанен чат игроку " + charName + " на бессрочный период, причина: " + reason + ".";
			Log.addGame(adminName + " забанил чат игроку " + charName + " на бессрочный период, причина: " + reason + ".", "banchat", adminChar);
			result = "Вы забанили чат игроку " + charName + " на бессрочный период.";
		}
		else
		{
			if(adminChar != null && (player == null || player.getNoChannel() != 0))
				return "Вы не имеете права изменять время бана.";
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " забанил чат игроку " + charName + " на " + val + " минут, причина: " + reason + "." : "Забанен чат игроку " + charName + " на " + val + " минут, причина: " + reason + ".";
			Log.addGame(adminName + " забанил чат игроку " + charName + " на " + val + " минут, причина: " + reason + ".", "banchat", adminChar);
			result = "Вы забанили чат игроку " + charName + " на " + val + " минут.";
		}

		if(player != null)
			updateNoChannel(player, val, reason);
		else
			AutoBan.ChatBan(charName, val, reason, adminName);

		if(announce != null)
			if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
				Announcements.getInstance().announceToAll(announce);
			else
				Announcements.shout(adminChar, announce, ChatType.CRITICAL_ANNOUNCE);

		return result;
	}

	private static void updateNoChannel(Player player, int time, String reason)
	{
		player.updateNoChannel(time * 60000);
		if(time == 0)
			player.sendMessage(new CustomMessage("common.ChatUnBanned", player));
		else if(time > 0)
		{
			if(reason == null || reason.isEmpty())
				player.sendMessage(new CustomMessage("common.ChatBanned", player).addNumber(time));
			else
				player.sendMessage(new CustomMessage("common.ChatBannedWithReason", player).addNumber(time).addString(reason));
		}
		else if(reason == null || reason.isEmpty())
			player.sendMessage(new CustomMessage("common.ChatBannedPermanently", player));
		else
			player.sendMessage(new CustomMessage("common.ChatBannedPermanentlyWithReason", player).addString(reason));
	}
}
