package services.community;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.utils.ValueSortMap;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;

public class NewClanCommunity
{
	private static TIntObjectHashMap<SortBy> _playerSortBy = new TIntObjectHashMap<>();
	private static TIntObjectHashMap<String> _playerSearch = new TIntObjectHashMap<>();
	
	public static void usebypass(Player player, String Bypass)
	{
		if (!Config.ENABLE_NEW_CLAN_CB)
			return;
		
		if (player == null)
			return;
		
		StringTokenizer st = new StringTokenizer(Bypass, "_");
		
		if(Bypass.startsWith("_bbsclan:commClan"))
		{
			String[] cm = Bypass.split(" ");
			showclanInfo(player, Integer.parseInt(cm[1]));
		}

		else if(Bypass.startsWith("_bbsclan:ClanPages"))
		{
			String[] cm = Bypass.split(" ");
			showClans(player, Integer.parseInt(cm[1]));
		}
		else if(Bypass.startsWith("_bbsclan:showClanWars"))
		{
			String[] cm = Bypass.split(" ");
			
			showClanWars(Integer.parseInt(cm[1]), Integer.parseInt(cm[2]), player);
		}
		else if(Bypass.startsWith("_bbsclan:showClanSkills"))
		{
			String[] cm = Bypass.split(" ");
			
			showClanSkills(Integer.parseInt(cm[1]), Integer.parseInt(cm[2]), player);
		}
		else if(Bypass.startsWith("_bbsclan:sortby"))
		{
			String[] cm = Bypass.split(" ");
			if(cm.length == 2)
				_playerSortBy.put(player.getObjectId(), SortBy.getEnum(cm[1]));
			
			showClans(player, 1);
		}
		else if(Bypass.startsWith("_bbsclan:search"))
		{
			String[] cm = Bypass.split(" ");
			if(cm.length == 2)
				_playerSearch.put(player.getObjectId(), cm[1]);
			
			showClans(player, 1);
		}
		else if(Bypass.startsWith("_bbsclan:resetsearch"))
		{
			_playerSearch.remove(player.getObjectId());
			
			showClans(player, 1);
		}
		else if(Bypass.startsWith("_bbsclan:myClan"))
		{
			showMyClan(player);
		}
		else if(Bypass.startsWith("_bbsclan:setClanNotice"))
		{
			if (st.countTokens() != 3)
			{
				player.sendMessage("There was an error while setting clan notice.");
				return;
			}
			
			st.nextToken();
			String notice = st.nextToken();
			int type = Integer.parseInt(st.nextToken());

			setClanNotice(player, notice, type);
		}
		else if(Bypass.startsWith("_bbsclan:deleteClanNotice"))
		{
			st.nextToken();
			int type = Integer.parseInt(st.nextToken());
			
			deleteClanNotice(player, type);
		}
		else if(Bypass.startsWith("_bbsclan:sendClanMail"))
		{
			st.nextToken();
			String title = st.hasMoreTokens() ? st.nextToken() : null;
			String message = st.hasMoreTokens() ? st.nextToken() : null;
			
			sendClanMail(player, title, message);
		}
		else
			showClans(player, 1);
	}

	private static void showClans(Player pl, int page)
	{
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/clan/clancommunity.htm", pl);
		String filterStr = _playerSearch.get(pl.getObjectId());
		SortBy sortBy = _playerSortBy.get(pl.getObjectId());
		List<Clan> clans = new ArrayList<>();
		
		Collections.addAll(clans, ClanTable.getInstance().getClans());
		
		if (sortBy == null)
			sortBy = SortBy.CLAN_LEVEL;
		if (filterStr != null)
		{
			clans.clear();
			for (Clan clan : ClanTable.getInstance().getClans())
			{
				if (clan != null && (clan.getName().toLowerCase().contains(filterStr.toLowerCase()) || clan.getLeader().getName().contains(filterStr.toLowerCase())))
					clans.add(clan);
			}
		}

		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = (int) Math.round(clans.size() / 18.0);
		
		StringBuilder sb = new StringBuilder();
		
		String nameOfCurSortBy = sortBy.toString() + ";";
		sb.append(nameOfCurSortBy);
		
		for (SortBy s : SortBy.values())
		{
			String str = s + ";";
			if (!str.toString().equalsIgnoreCase(nameOfCurSortBy))
				sb.append(str);
		}
		
		if(pl.getClan() != null && pl.getClan().getLevel() > 2 && pl.isClanLeader())
		{
			if (pl.isLangRus())
				htmltosend = htmltosend.replaceAll("%myClan%", "<button value=\"Мой клану\" action=\"bypass _bbsclan:myClan\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			else
				htmltosend = htmltosend.replaceAll("%myClan%", "<button value=\"My Clan\" action=\"bypass _bbsclan:myClan\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		}
		else
		{
			htmltosend = htmltosend.replaceAll("%myClan%", "&nbsp;");
		}
		
		htmltosend = htmltosend.replaceAll("%sortbylist%", sb.toString());
		
		if(page == 1)
		{
			if (totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsclan:ClanPages " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsclan:ClanPages " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsclan:ClanPages " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsclan:ClanPages " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for(Clan uu : getSorttedClans(_playerSortBy.get(pl.getObjectId()), clans))
		{
			all++;
			if(page == 1 && clansvisual > 18)
				continue;
			else if(!pagereached && all > page * 18)
				continue;
			else if(!pagereached && all <= (page - 1) * 18)
				continue;
			else
				clansvisual++;

			int crestId = 0;
			if (uu.getCrestId() > 0)
				crestId = uu.getCrestId();
			
			if (pl.isLangRus())
			{
				htmltosend = htmltosend.replaceAll("%clanlevel" + clansvisual + "%", "%image:500" + uu.getLevel() + ".png%");
				htmltosend = htmltosend.replaceAll("%clanName" + clansvisual + "%", uu.getName());
				htmltosend = htmltosend.replaceAll("%leaderName" + clansvisual + "%", "Лидер: <font color=\"33CC66\"> " + uu.getLeaderName() + "</font><br1>Члены: <font color=\"FF0033\">" + uu.getAllMembers().size() + "</font> ");
				htmltosend = htmltosend.replaceAll("%button" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbsclan:commClan " + uu.getClanId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
				htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "110");
				htmltosend = htmltosend.replaceAll("%clanCrest" + clansvisual + "%", crestId > 0 ? "<img src=\"Crest.crest_" + pl.getServerId() + "_" + crestId + "\" width=16 height=16 align=top />" : "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%clanlevel" + clansvisual + "%", "%image:500" + uu.getLevel() + ".png%");
				htmltosend = htmltosend.replaceAll("%clanName" + clansvisual + "%", uu.getName());
				htmltosend = htmltosend.replaceAll("%leaderName" + clansvisual + "%", "Leader: <font color=\"33CC66\"> " + uu.getLeaderName() + "</font><br1>Members: <font color=\"FF0033\">" + uu.getAllMembers().size() + "</font> ");
				htmltosend = htmltosend.replaceAll("%button" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbsclan:commClan " + uu.getClanId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
				htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "110");
				htmltosend = htmltosend.replaceAll("%clanCrest" + clansvisual + "%", crestId > 0 ? "<img src=\"Crest.crest_" + pl.getServerId() + "_" + crestId + "\" width=16 height=16 align=top />" : "&nbsp;");
			}
		}

		if(clansvisual < 18)
		{
			for(int d = clansvisual + 1; d != 19; d++)
			{
				htmltosend = htmltosend.replaceAll("%clanlevel" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%clanName" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%leaderName" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%button" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "121");
				htmltosend = htmltosend.replaceAll("%clanCrest" + d + "%","&nbsp;");
			}
		}
		
		htmltosend = htmltosend.replaceAll("%totalclans%", "" + clans.size());
		
		ShowBoard.separateAndSend(htmltosend, pl);
	}

	private static void showMyClan(Player player)
	{
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/clan/myclan.htm", player);
		
		Clan clan = player.getClan();
		if(clan == null || clan.getLevel() < 2 || !player.isClanLeader())
		{
			showClans(player, 1);
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		String intro = "";
		int type = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `clan_notice` WHERE `clan_id` = ? and type != 2");
			statement.setInt(1, clan.getClanId());
			rset = statement.executeQuery();
			if(rset.next())
			{
				intro = rset.getString("notice");
				type  = rset.getInt("type");
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
		
		if(type == 0)
			htmltosend = htmltosend.replace("%intro%", "<button value=\"Enable notice\" action=\"bypass _bbsclan:deleteClanNotice_1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		else
			htmltosend = htmltosend.replace("%intro%", "<button value=\"Disable notice\" action=\"bypass _bbsclan:deleteClanNotice_0\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		
		htmltosend = htmltosend.replaceAll("%currentStatus%", "" + type);
		
		htmltosend = htmltosend.replaceAll("%clanName%", clan.getName());
		
		ShowBoard.separateAndSend(htmltosend, player, intro);
	}
	
	private static void deleteClanNotice(Player player, int status)
	{
		Clan clan = player.getClan();
		if (clan == null || clan.getLevel() < 2 || !player.isClanLeader())
		{
			showMyClan(player);
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE `clan_notice` SET type = ? WHERE `clan_id` = ?");
			statement.setInt(1, status);
			statement.setInt(2, clan.getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		clan.setNotice(status == 0 ? "" : null);
		
		if (status == 0)
			player.sendMessage(new CustomMessage("scripts.services.community.newclancommunity.notice_disabled", player));
		else
			player.sendMessage(new CustomMessage("scripts.services.community.newclancommunity.notice_enabled", player));
		
		showMyClan(player);
	}
	
	@SuppressWarnings("unused")
	private static boolean hasClanNotice(int clanId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		String notice = "";
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `clan_notice` WHERE `clan_id` = ?");
			statement.setInt(1, clanId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				notice = rset.getString("notice");
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
		
		if (!notice.isEmpty())
			return true;
		
		return false;
		
	}
	private static void setClanNotice(Player player, String notice, int currentStatus)
	{
		Clan clan = player.getClan();
		if (clan == null || clan.getLevel() < 2 || !player.isClanLeader())
		{
			showMyClan(player);
			return;
		}
		if (notice == null || notice.isEmpty())
		{
			showMyClan(player);
			return;
		}
		
		notice = notice.replace("<", "");
		notice = notice.replace(">", "");
		notice = notice.replace("&", "");
		notice = notice.replace("$", "");
		
		if (notice.isEmpty())
		{
			showMyClan(player);
			return;
		}
		
		if (notice.length() > 3000)
			notice = notice.substring(0, 3000);
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO `clan_notice`(clan_id, type, notice, lastUpdated) VALUES(?, ?, ?, ?)");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, currentStatus);
			statement.setString(3, notice);
			statement.setLong(4, System.currentTimeMillis());
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		
		clan.setNotice(notice.replace("\n", "<br1>"));
		clan.setNoticeLastUpdated(System.currentTimeMillis());
		
		player.sendPacket(Msg.NOTICE_HAS_BEEN_SAVED);
		showMyClan(player);
	}
	
	private static void sendClanMail(Player player, String title, String body)
	{
		Clan clan = player.getClan();
		
		if(clan == null || clan.getLevel() < 2 || !player.isClanLeader())
		{
			showClans(player, 1);
			return;
		}
		
		
		if (!player.antiFlood.canSendClanEmail())
		{
			player.sendMessageS("You can send one mail message per hour!", 5);
			return;
		}
		
		if(title == null || body == null || title.isEmpty() || body.isEmpty())
		{
			player.sendPacket(Msg.THE_MESSAGE_WAS_NOT_SENT);
			showMyClan(player);
			return;
		}

		title = title.replace("<", "");
		title = title.replace(">", "");
		title = title.replace("&", "");
		title = title.replace("$", "");

		body = body.replace("<", "");
		body = body.replace(">", "");
		body = body.replace("&", "");
		body = body.replace("$", "");

		if(title.isEmpty() || body.isEmpty())
		{
			player.sendPacket(Msg.THE_MESSAGE_WAS_NOT_SENT);
			showMyClan(player);
			return;
		}

		if(title.length() > 128)
			title = title.substring(0, 128);

		if(body.length() > 3000)
			body = body.substring(0, 3000);

	
		try
		{
			for (UnitMember clm : clan)
			{

				if (clm == null || clm.getObjectId() == player.getObjectId())
					continue;
				
				Mail mail = new Mail();
				mail.setSenderId(player.getObjectId());
				mail.setSenderName(player.getName());
				mail.setReceiverId(clm.getObjectId());
				mail.setReceiverName(clm.getName());
				mail.setTopic(title);
				mail.setBody(body);
				
				
				mail.setType(Mail.SenderType.NORMAL);
				mail.setUnread(true);
				mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
				mail.save();

				if (clm.getPlayer() != null)
				{
					clm.getPlayer().sendPacket(ExNoticePostArrived.STATIC_TRUE);
					clm.getPlayer().sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
				}
			}
		}
		catch(Exception e)
		{
			player.sendPacket(Msg.THE_MESSAGE_WAS_NOT_SENT);
			showMyClan(player);
			return;
		}
		
		player.sendPacket(Msg.YOUVE_SENT_MAIL);
		
		showMyClan(player);
	}
	
	private static void showclanInfo(Player player, int clanid)
	{
		Clan c = ClanTable.getInstance().getClan(clanid);
		if(c == null)
			return;
		
		boolean hasFort = false;
		boolean hasCastle = false;
		boolean hasClanHall = false;
		
		String fortName = "";
		String castleName = "";
		String hallName = "";
		
		String fullhtm = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/clan/clanpage.htm", player);

		int crestId = 0;
		if (c.getCrestId() > 0)
			crestId = c.getCrestId();
		
		fullhtm = fullhtm.replace("%clanname%", c.getName());
		fullhtm = fullhtm.replace("%clanleader%", c.getLeaderName());
		fullhtm = fullhtm.replace("%clanlevel%", "" + c.getLevel());
		fullhtm = fullhtm.replace("%clanmemberssize%", "" + c.getAllMembers().size());
		fullhtm = fullhtm.replace("%clanwarscount%", "" + c.getWarsCount());
		fullhtm = fullhtm.replace("%clanskillscount%", "" + c.getSkills().size());
		fullhtm = fullhtm.replace("%clanCrest%", crestId > 0 ? "<img src=\"Crest.crest_" + player.getServerId() + "_" + crestId + "\" width=16 height=16 align=top />" : "");
		fullhtm = fullhtm.replace("%clanReputation%", "" + c.getReputationScore());
		
		if (c.getHasFortress() > 0)
		{
			fortName = ResidenceHolder.getInstance().getResidence(Fortress.class, c.getHasFortress()).getName();
			hasFort = true;
		}
		
		if (c.getHasHideout() > 0)
		{
			hallName = ResidenceHolder.getInstance().getResidence(ClanHall.class, c.getHasHideout()).getName();
			hasClanHall = true;
		}
		
		if (c.getCastle() > 0)
		{
			castleName = ResidenceHolder.getInstance().getResidence(Castle.class, c.getCastle()).getName();
			hasCastle = true;
		}
		
		fullhtm = fullhtm.replace("%hasFort%", hasFort ? fortName : "No");
		fullhtm = fullhtm.replace("%hasClanHall%", hasClanHall ? hallName : "No");
		fullhtm = fullhtm.replace("%hasCastle%", hasCastle ? castleName : "No");
		
		fullhtm = fullhtm.replace("%clanId%", "" + c.getClanId());
		
		//Functions.show(fullhtm, player, null);
		ShowBoard.separateAndSend(fullhtm, player);
	}
	
	private static void showClanWars(int clanId, int page, Player player)
	{
		if(player == null)
			return;

		Clan cl = ClanTable.getInstance().getClan(clanId);
		if(cl == null)
			return;
		
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = (int) Math.round(cl.getEnemyClans().size() / 10.0 + 1);
		
		String result = "";
		String fullhtm = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/clan/clanwars.htm", player);
		String one = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/clan/clanwarsTemplate.htm", player);

		if(page == 1)
		{
			fullhtm = fullhtm.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsclan:showClanWars " + cl.getClanId() + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			fullhtm = fullhtm.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				fullhtm = fullhtm.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsclan:showClanWars " + cl.getClanId() + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				fullhtm = fullhtm.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				fullhtm = fullhtm.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsclan:showClanWars " + cl.getClanId() + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				fullhtm = fullhtm.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsclan:showClanWars " + cl.getClanId() + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for(Clan wars : cl.getEnemyClans())
		{
			all++;
			if(page == 1 && clansvisual > 10)
				continue;
			if(!pagereached && all > page * 10)
				continue;
			if(!pagereached && all <= (page - 1) * 10)
				continue;
			else
				clansvisual++;
			
			result += one.replaceAll("%warname%", wars.getName()).replaceAll("%totalwars%","" + all).replaceAll("%warnumber%", "" + clansvisual).replaceAll("%warlink%", "<button value=\"\" action=\"bypass _bbsclan:commClan " + wars.getClanId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">").replaceAll("%bg%", clansvisual % 2 == 0 ? "121618" : "070e13");
		}

		fullhtm = fullhtm.replace("%clanName%", cl.getName());
		fullhtm = fullhtm.replace("%wars%", result);

		if (cl.getEnemyClans().size() > 0)
			fullhtm = fullhtm.replace("%totalwars%", "Total Wars <font color=\"LEVEL\"> " + cl.getEnemyClans().size() + " </font>");
		else
			fullhtm = fullhtm.replace("%totalwars%", "That clan have no clan-wars!");
		
		showclanInfo(player, cl.getClanId());
		
		Functions.show(fullhtm, player, null);
	}
	
	private static void showClanSkills(int clanId, int page, Player player)
	{
		if(player == null)
			return;

		Clan cl = ClanTable.getInstance().getClan(clanId);
		if(cl == null)
			return;
		
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = (int) Math.round(cl.getSkills().size() / 10.0 + 1);
		
		String result = "";
		String fullhtm = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/clan/clanskills.htm", player);
		String one = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/clan/clanskillsTemplate.htm", player);

		if(page == 1)
		{
			fullhtm = fullhtm.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsclan:showClanSkills " + cl.getClanId() + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			fullhtm = fullhtm.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				fullhtm = fullhtm.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsclan:showClanSkills " + cl.getClanId() + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				fullhtm = fullhtm.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				fullhtm = fullhtm.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsclan:showClanSkills " + cl.getClanId() + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				fullhtm = fullhtm.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsclan:showClanSkills " + cl.getClanId() + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for(Skill skill : cl.getSkills())
		{
			all++;
			if(page == 1 && clansvisual > 10)
				continue;
			else if(!pagereached && all > page * 10)
				continue;
			else if(!pagereached && all <= (page - 1) * 10)
				continue;
			else
				clansvisual++;
			
			result += one.replaceAll("%skillname%", skill.getName()).replaceAll("%skillLevel%", "" + skill.getLevel()).replaceAll("%totalSkills%","" + all).replaceAll("%skillNumber%", "" + clansvisual).replaceAll("%skillIncon%", "<img src=icon." + skill.getIcon() + " width=32 height=32>").replaceAll("%bg%", clansvisual % 2 == 0 ? "121618" : "070e13");
		}

		fullhtm = fullhtm.replace("%clanName%", cl.getName());
		fullhtm = fullhtm.replace("%skills%", result);

		if (cl.getSkills().size() > 0)
			fullhtm = fullhtm.replace("%totalskills%", "Total Skills <font color=\"LEVEL\"> " + cl.getSkills().size() + " </font>");
		else
			fullhtm = fullhtm.replace("%totalskills%", "There is not clan skills to show...");
		
		showclanInfo(player, cl.getClanId());
		
		Functions.show(fullhtm, player, null);
	}
	
	private enum SortBy
	{
		CLAN_LEVEL("ClanLevel"),
		CLAN_NAME_ASC("ClanName(Ascending)"),
		CLAN_NAME_DSC("ClanName(Descending)"),
		REPUTATION_SCORE_ASC("ReputationScore(Ascending)"),
		REPUTATION_SCORE_DSC("ReputationScore(Descending)"),
		ClAN_MEMMBERS_ASC("MembersCount(Ascending)"),
		ClAN_MEMMBERS_DSC("MembersCount(Descending)");
		
		private String _sortName;
		
		private SortBy(String sortName)
		{
			_sortName = sortName;
		}
		
		@Override
		public String toString()
		{
			return _sortName;
		}
		
		public static SortBy getEnum(String sortName)
		{
			for (SortBy sb : values())
			{
				if (sb.toString().equals(sortName))
					return sb;
			}
			
			return CLAN_LEVEL;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<Clan> getSorttedClans(SortBy sort, List<Clan> clans)
	{
		if (sort == null)
			sort = SortBy.CLAN_LEVEL;
		
		List<Clan> sorted = new ArrayList<>();
		
		switch(sort)
		{
			default:
			case CLAN_LEVEL:
				List<Clan> notSortedValues = new ArrayList<>();
				notSortedValues.addAll(clans);
				Clan storedid = null;
				int lastpoints = 0;
				
				while(notSortedValues.size() > 0)
				{
					if(sorted.size() == clans.size())
						break;

					for(Clan cplayer : notSortedValues)
						if(cplayer.getLevel() >= lastpoints)
						{
							storedid = cplayer;
							lastpoints = cplayer.getLevel();
						}

					if(storedid != null)
					{
						notSortedValues.remove(storedid);
						sorted.add(storedid);
						storedid = null;
						lastpoints = 0;
					}
				}
				
				return sorted;
			case CLAN_NAME_ASC:
				Map<Clan, String> tmp = new FastMap<>();
				for (Clan clan: clans)
					tmp.put(clan, clan.getName());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp, true).keySet());
				return sorted;
			case CLAN_NAME_DSC:
				Map<Clan, String> tmp2 = new FastMap<>();
				for (Clan clan : clans)
					tmp2.put(clan, clan.getName());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp2, false).keySet());
				return sorted;
			case REPUTATION_SCORE_ASC:
				Map<Clan, Integer> tmp3 = new FastMap<>();
				for (Clan clan : clans)
					tmp3.put(clan, clan.getReputationScore());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp3, true).keySet());
				return sorted;
			case REPUTATION_SCORE_DSC:
				Map<Clan, Integer> tmp4 = new FastMap<>();
				for (Clan clan : clans)
					tmp4.put(clan, clan.getReputationScore());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp4, false).keySet());
				return sorted;
			case ClAN_MEMMBERS_ASC:
				Map<Clan, Integer> tmp5 = new FastMap<>();
				for (Clan clan : clans)
					tmp5.put(clan, clan.getAllSize());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp5, true).keySet());
				return sorted;
			case ClAN_MEMMBERS_DSC:
				Map<Clan, Integer> tmp6 = new FastMap<>();
				for (Clan clan : clans)
					tmp6.put(clan, clan.getAllSize());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp6, false).keySet());
				return sorted;
		}
	}
}
