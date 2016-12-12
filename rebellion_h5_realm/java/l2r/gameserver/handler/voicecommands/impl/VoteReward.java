package l2r.gameserver.handler.voicecommands.impl;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.TimeUtils;
import l2r.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

public class VoteReward extends Functions implements IVoicedCommandHandler
{
	
	private static final String[] _voicedCommands =
	{
		"votereward",
		"getreward",
		"givereward",
		"rewards"
	};
	
	// Usage if the login server and game server sql is splited.
	private final boolean useExternalLoginserverDB = false;
	private final String LOGINSERVER_DATABASE_URL = "jdbc:mysql://";
	private final String LOGINSERVER_DATABASE_LOGIN = "";
	private final String LOGINSERVER_DATABASE_PASSWORD = "";

	private static String _rewardsHtml = "<html><body>Error 404. Page Not Found </body></html>";
	static
	{
		for (String rewards : Config.VOTE_REWARDS)
		{
			String[] reward = rewards.split(",");
			
			int id = Integer.parseInt(reward[0]);
			long count = Long.parseLong(reward[1]);
			int chance = Integer.parseInt(reward[2]);
			
			String icon = ItemHolder.getInstance().getTemplate(id).getIcon();
			if (icon == null || icon.equals(StringUtils.EMPTY))
				icon = "icon.etc_question_mark_i00";
			
			_rewardsHtml += "<tr><td width=40 height=35><img src=" + icon + " width=32 height=32></td></tr>";
			_rewardsHtml += "<tr><td width=150 height=15><font color=LEVEL>" + ItemHolder.getInstance().getTemplate(id).getName() + "</font></td></tr>";
			_rewardsHtml += "<tr><td width=150 height=15><font color=7FFFD4>Count:</font> " + count + ", <font color=7FFF00>Chance:</font> " + chance + "%</td></tr>";
		}
	}
	
	@SuppressWarnings("resource")
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (!Config.ENABLE_VOTE_REWARDS)
		{
			String htmContent = HtmCache.getInstance().getNotNull("mods/votereward/Disabled.htm", activeChar);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		
		if (command.equalsIgnoreCase("getreward") || command.equalsIgnoreCase("votereward"))
		{
			String htmContent = HtmCache.getInstance().getNotNull("mods/votereward/Intro.htm", activeChar);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			activeChar.sendPacket(npcHtmlMessage);
		}
		else if (command.equalsIgnoreCase("givereward"))
		// Vote reward script:
		// 1. the .php script gets results for voted ppl - ip, host(ip), timestamp and inserts them in hz_votes
		// where rewardsLeft (table makes auto 1 = user has reward). NO NEED TO DELETE THE RECORD FROM TABLE:
		// primary key is ID and IP with timestamp, so if user votes every 12 hours, he has more than 1 record (if with same ip)
		// and each of those records has 1 vote. User then can get ALL rewards from ALL votes with .getreward
		// if we delete the record AFTER user gets reward, the .php script will automatically add his IP again with vote 1 !!!!
		// so user will have reward WITHOUT vote.
		// we can make .php script to insert in multiple sql tables (for x1000 & x20), so users with chars @ both servers
		// will get reward IN both servers. (which isnt bad)
		// we can periodicly clean the table from old votes, that's not a problem - we can make a script that will delete
		// automatially records with timestamp > 1 month & rewardsLeft = 0 :)
		{
			String ip = activeChar.getIP();
			String[] ips = ip.split("\\.");
			
			if (!activeChar.hasHWID())
				return false;
			
			String hwidcheck = Util.getCPU_BIOS_HWID(activeChar.getHWID());
			String iphwidcheck = ips[0] + "" + ips[1] + "" + Util.getCPU_BIOS_HWID(activeChar.getHWID());
			
			int reward = 0;
			long timeLeftForReward = 0;
			int found = 0;
			
			PreparedStatement ps = null;
			ResultSet rs = null;
			try (Connection con = getLoginserverDatabase())
			{
				if (activeChar.getHWID() != null && activeChar.getIP() != null)
				{
					if (ips.length != 4)
						return false;
					
					// First check
					ps = con.prepareStatement("SELECT COUNT(hwid) FROM " + Config.LOGINSERVER_DB_NAME + ".hz_votes_hwids WHERE `hwid` LIKE ?");
					ps.setString(1, "%" + hwidcheck + "%");
					rs = ps.executeQuery();
					
					while (rs.next())
						found = rs.getInt(1);
					
					if (found >= 1)
					{
						ps = con.prepareStatement("SELECT * FROM " + Config.LOGINSERVER_DB_NAME + ".hz_votes_hwids WHERE `hwid` = ?");
						ps.setString(1, iphwidcheck);
						rs = ps.executeQuery();
						
						while (rs.next())
							timeLeftForReward = (rs.getLong("lastRewardTime") + /* 86400000 - 24h */43200000) - System.currentTimeMillis();
						
						if (timeLeftForReward == 0)
						{
							ps = con.prepareStatement("INSERT INTO " + Config.LOGINSERVER_DB_NAME + ".hz_votes_hwids VALUES (?,?)");
							ps.setString(1, iphwidcheck);
							ps.setLong(2, 1);
							ps.executeUpdate();
							
							timeLeftForReward = 1;
						}
						
						rs.close();
						ps.close();
					}
					else
					{
						ps = con.prepareStatement("INSERT INTO " + Config.LOGINSERVER_DB_NAME + ".hz_votes_hwids VALUES (?,?)");
						ps.setString(1, iphwidcheck);
						ps.setLong(2, 1);
						ps.executeUpdate();
						ps.close();
						
						timeLeftForReward = 1;
					}
					
					// CLose the "count" (first check) query.
					rs.close();
					ps.close();
				}
				
				if (timeLeftForReward < 60000)
				{
					ps = con.prepareStatement("SELECT rewardsLeft FROM " + Config.LOGINSERVER_DB_NAME + ".hz_votes WHERE `ip` = ? AND rewardsLeft > 0;");
					ps.setString(1, activeChar.getIP());
					rs = ps.executeQuery();
					
					while (rs.next())
						reward += rs.getInt("rewardsLeft");
					
					rs.close();
					ps.close();
					
					if (reward > 0)
					{
						// Set the last time this guy got a reward
						ps = con.prepareStatement("UPDATE " + Config.LOGINSERVER_DB_NAME + ".hz_votes_hwids SET `lastRewardTime` = ? WHERE `hwid` = ?");
						ps.setLong(1, System.currentTimeMillis());
						ps.setString(2, iphwidcheck);
						ps.executeUpdate();
						ps.close();
						
						// The guy has taken a reward, set rewardsLeft to 0
						ps = con.prepareStatement("UPDATE " + Config.LOGINSERVER_DB_NAME + ".hz_votes SET `rewardsLeft` = '0' WHERE `ip` = ?");
						ps.setString(1, activeChar.getIP());
						ps.executeUpdate();
						ps.close();
						
						// Give the reward
						Collections.shuffle(Arrays.asList(Config.VOTE_REWARDS));
						for (String rewards : Config.VOTE_REWARDS)
						{
							String[] reward2 = rewards.split(",");
							
							int id = Integer.parseInt(reward2[0]);
							long count = Long.parseLong(reward2[1]);
							int chance = Integer.parseInt(reward2[2]);
							
							if (Rnd.get(100) < chance)
							{
								String icon = ItemHolder.getInstance().getTemplate(id).getIcon();
								if (icon == null || icon.equals(StringUtils.EMPTY))
									icon = "icon.etc_question_mark_i00";
								
								String reciveReward = HtmCache.getInstance().getNotNull("mods/votereward/Recivereward.htm", activeChar);
								NpcHtmlMessage htmlIntro = new NpcHtmlMessage(5);
								htmlIntro.replace("%itemId%", ItemHolder.getInstance().getTemplate(id).getName());
								htmlIntro.replace("%icon%", "" + icon);
								htmlIntro.replace("%count%", "" + count);
								htmlIntro.setHtml(reciveReward);
								activeChar.sendPacket(htmlIntro);
								
								if (Config.ENABLE_PLAYER_COUNTERS)
									activeChar.getCounters().addPoint("_Times_Voted");
								
								Functions.addItem(activeChar, id, count, true);
								Log.voteReward("[" + TimeUtils.convertDateToString(System.currentTimeMillis()) + "] Acc: " + activeChar.getAccountName() + " - Char: " + activeChar.getName() + " - IP: " + activeChar.getIP() + " - HWID: " + Util.getCPU_BIOS_HWID(activeChar.getHWID()) + " ");
								break;
							}
						}
						
					}
					else
					{
						String htmContent = HtmCache.getInstance().getNotNull("mods/votereward/NotVoted.htm", activeChar);
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setHtml(htmContent);
						activeChar.sendPacket(npcHtmlMessage);
					}
				}
				else
				{
					String htmContent = HtmCache.getInstance().getNotNull("mods/votereward/Voted.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%timeLeftForReward%", "" + Util.formatTime((int) timeLeftForReward / 1000));
					activeChar.sendPacket(npcHtmlMessage);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (command.equalsIgnoreCase("rewards"))
		{
			String htmContent = HtmCache.getInstance().getNotNull("mods/votereward/Rewards.htm", activeChar);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			npcHtmlMessage.replace("%rewards%", _rewardsHtml);
			activeChar.sendPacket(npcHtmlMessage);
		}
		return true;
	}
	
	private Connection getLoginserverDatabase()
	{
		Connection conn = null;
		if (useExternalLoginserverDB)
		{
			try
			{
				conn = DriverManager.getConnection(LOGINSERVER_DATABASE_URL + "?" + "user=" + LOGINSERVER_DATABASE_LOGIN + "&password=" + LOGINSERVER_DATABASE_PASSWORD);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				conn = DatabaseFactory.getInstance().getConnection();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		
		return conn;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}