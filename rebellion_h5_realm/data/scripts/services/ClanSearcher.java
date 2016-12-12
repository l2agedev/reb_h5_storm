/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package services;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.ClanTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastMap;

public class ClanSearcher extends Functions
{
	private static final int UPDATE_TIME = 60;
	
	public void sendEvent(String[] param)
	{
		Player player = getSelf();
		
		if (player == null)
			return;
		/*
		if (Config.ENABLE_CLAN_SEARCHER)
		{
			show(HtmCache.getInstance().getNotNull("mods/clansearch/notenabled.htm", player), player);
			return;	
		}
		*/
		
		String bypass = param[0];
		
		if (bypass.startsWith("show_list"))
			loadClansAndShowWindow(player, 1, 1);
		else if (bypass.startsWith("add"))
			saveClanPresentation(param, player);
		else if (bypass.startsWith("moreinfo_"))
			showMoreClanInfo(bypass, player);
		else if (bypass.startsWith("requestjoin_"))
			sendClanJoinRequest(bypass, player);
		else if (bypass.startsWith("remove"))
			removeClanPresentation(player);
		else if (bypass.startsWith("newclan"))
		{
			if (player.isClanLeader())
			{
				String html = HtmCache.getInstance().getNotNull("mods/clansearch/addnewclan.htm", player);
				html = html.replaceAll("%clanname%", player.getClan().getName());
				html = html.replaceAll("%clanlevel%", "" + player.getClan().getLevel());
				html = html.replaceAll("%clanleader%", player.getClan().getLeaderName());
				ShowBoard.separateAndSend(html, player);
			}
			else
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "ClanSearch", "This feature is available only for Clan Leader.");
				return;
			}	
		}
		else if (bypass.startsWith("deleteclan"))
		{
			String htmldeleteclan = HtmCache.getInstance().getNotNull("mods/clansearch/deleteclan.htm", player);
			ShowBoard.separateAndSend(htmldeleteclan, player);
		}
	}
	
	public void showIntroWindow()
	{
		Player player = getSelf();
		
		if (player == null)
			return;
		/*
		if (Config.ENABLE_CLAN_SEARCHER)
		{
			show(HtmCache.getInstance().getNotNull("mods/clansearch/notenabled.htm", player), player);
			return;	
		}
		*/
		String html = HtmCache.getInstance().getNotNull("mods/clansearch/present.htm", player);
		html = html.replaceAll("%player%", player.getName());
		
		if (player.getClan() == null)
			html = html.replaceAll("%text%", "I am here to help you find a clan. Good luck!");
		else if (player.isClanLeader())
			html = html.replaceAll("%text%", "Do you wish to present your clan?");
		else
			html = html.replaceAll("%text%", "Do you want see your concurence?");
		
		if (player.isClanLeader())
		{
			html = html.replaceAll("%add%", "<button value=\"Edit your clan's presentation\" action=\"bypass -h scripts_services.ClanSearcher:sendEvent newclan\" width=255 height=27 back=\"L2UI_CT1.Button_DF.Gauge_DF_Attribute_Divine\" fore=\"L2UI_CT1.Button_DF.Gauge_DF_Attribute_Divine\">");
			html = html.replaceAll("%remove%", "<button value=\"Remove your clan's presentation\" action=\"bypass -h scripts_services.ClanSearcher:sendEvent deleteclan\" width=255 height=27 back=\"L2UI_CT1.Button_DF.Gauge_DF_Attribute_Divine\" fore=\"L2UI_CT1.Button_DF.Gauge_DF_Attribute_Divine\">");
		}
		else
		{
			html = html.replaceAll("%add%", "");
			html = html.replaceAll("%remove%", "");
		}
		ShowBoard.separateAndSend(html, player);
	}
	
	private void sendClanJoinRequest(String event, Player requester)
	{
		Clan clan = ClanTable.getInstance().getClan(Integer.parseInt(event.split("_")[1]));
		int success = 0;
		
		FastMap<Player, Long> _requestsend = new FastMap<Player, Long>();
		
		if (requester == null || clan == null)
			return;
		
		//TODO: why this dont work ???
		for (Entry<Player, Long> check : _requestsend.entrySet())
		{
			if (check.getKey() == requester && check.getValue() < System.currentTimeMillis() - 60 * 60 * 1000)
			{
				requester.sendChatMessage(requester.getObjectId(), ChatType.TELL.ordinal(), "ClanSearch", "Request has not been send. You can make request every 1 hour.");
				return;
			}
		}
		
		for (UnitMember member : clan.getAllMembers())
		{
			if (member.isOnline())
			{
				Player player = member.getPlayer();
				if (player != null && (player.getClanPrivileges() & Clan.CP_CL_INVITE_CLAN) == Clan.CP_CL_INVITE_CLAN)
				{
					Mail letter = new Mail();
					letter.setSenderId(requester.getObjectId());
					letter.setSenderName(requester.getName());
					letter.setReceiverId(player.getObjectId());
					letter.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(player.getObjectId()));
					letter.setTopic("Request to join in the Clan!");
					letter.setBody("Clan Search System: player " + requester.getName() + " has made request to join in your clan. Class: " + requester.getClassId().name() + " Level: " + requester.getLevel() + " To accept him in the clan type : .claninvite " + requester.getName() + " <subpledgename> ");
					letter.setType(Mail.SenderType.NORMAL);
					letter.setUnread(true);
					letter.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					letter.save();
					
					if (player != null)
					{
						player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
						player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
					}
					
					success++;
				}
			}
		}
		if (success > 0)
		{
			requester.sendChatMessage(requester.getObjectId(), ChatType.TELL.ordinal(), "ClanSearch", "Your request was successfully send.");
			_requestsend.put(requester, System.currentTimeMillis());
			loadClansAndShowWindow(requester, 1, 1);
		}
		else
		{
			requester.sendChatMessage(requester.getObjectId(), ChatType.TELL.ordinal(), "ClanSearch", "Nobody was able to see your request, please try again later.");
			loadClansAndShowWindow(requester, 1, 1);
		}
	}
	
	private String loadClansAndShowWindow(Player player, int mode, int page)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		String htmltosend = HtmCache.getInstance().getNotNull("mods/clansearch/intro.htm", player);
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		Clan clan = null;
		Map<Clan, String> clans = new FastMap<Clan, String>();
		try
		{
			
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clan_search ORDER BY adenas DESC");
			rset = statement.executeQuery();
			while (rset.next())
			{
				clan = ClanTable.getInstance().getClan(rset.getInt("clanId"));
				if (clan == null)
					continue;
				if (rset.getInt("visible") > 0 || mode == 2)
					clans.put(clan, rset.getString("message"));
			}
			
			if (mode == 2)
				return clans.get(player.getClan());
			
			int totalpages = (int) Math.round(clans.entrySet().size() / 18.0 + 1);
			
			if(page == 1)
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsclan:ClanPages " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
			}
			else if(page > 1)
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
			
			for (Map.Entry<Clan, String> entry : clans.entrySet())
			{
				if (entry.getKey() == null)
					continue;
				
				all++;
				if(page == 1 && clansvisual > 18)
					continue;
				if(!pagereached && all > page * 18)
					continue;
				if(!pagereached && all <= (page - 1) * 18)
					continue;
				else
					clansvisual++;

				Clan claninfo = entry.getKey();
				
				String fullMsg = clans.get(entry.getKey());
				String smallClanInfo = fullMsg.substring(0, Math.min(fullMsg.length(), 96));
				smallClanInfo = smallClanInfo.replaceAll("<br>", " ");
				smallClanInfo = smallClanInfo.replaceAll("<", "");
				smallClanInfo = smallClanInfo.replaceAll(">", "");
				smallClanInfo = smallClanInfo.replaceAll("bypass", "");
				
				htmltosend = htmltosend.replaceAll("%clanlevel" + clansvisual + "%", "Crest.crest_1_500" + claninfo.getLevel());
				htmltosend = htmltosend.replaceAll("%clanName" + clansvisual + "%", claninfo.getName());
				htmltosend = htmltosend.replaceAll("%leaderName" + clansvisual + "%", "Leader <font color=\"d4c039\"> " + claninfo.getLeaderName() + "</font><br1>with " + claninfo.getAllMembers().size() + " clan members.");
				htmltosend = htmltosend.replaceAll("%button" + clansvisual + "%", "<button value=\"\" action=\"bypass -h scripts_services.ClanSearcher:sendEvent moreinfo_" + claninfo.getClanId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
				htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "100");
				htmltosend = htmltosend.replaceAll("%smallInfo" + clansvisual + "%", smallClanInfo);
			}
			
			if(clansvisual < 18)
				for(int d = clansvisual + 1; d != 19; d++)
				{
					htmltosend = htmltosend.replaceAll("%clanlevel" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
					htmltosend = htmltosend.replaceAll("%clanName" + d + "%", "&nbsp;");
					htmltosend = htmltosend.replaceAll("%leaderName" + d + "%", "&nbsp;");
					htmltosend = htmltosend.replaceAll("%button" + d + "%", "&nbsp;");
					htmltosend = htmltosend.replaceAll("%width" + d + "%", "121");
				}
			
			ShowBoard.separateAndSend(htmltosend, player);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return "";
	}
	
	private void showMoreClanInfo(String event, Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		String fullhtm = HtmCache.getInstance().getNotNull("mods/clansearch/claninfo.htm", player);
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clan_search WHERE clanId=?");
			statement.setInt(1, Integer.parseInt(event.split("_")[1]));
			rset = statement.executeQuery();
			
			if (rset.next())
			{
				Clan clan = ClanTable.getInstance().getClan(rset.getInt("clanId"));
				String message = rset.getString("message");
				
				int onlinecount = 0;
				for (UnitMember member : clan.getAllMembers())
				{
					if (member.isOnline())
						onlinecount++;
				}
				
				fullhtm = fullhtm.replace("%clanname%", clan.getName());
				fullhtm = fullhtm.replace("%clanleader%", clan.getLeaderName());
				fullhtm = fullhtm.replace("%clanlevel%", "" + clan.getLevel());
				fullhtm = fullhtm.replace("%clanmembersonline%", "" + onlinecount);
				fullhtm = fullhtm.replace("%clanmemberssize%", "" + clan.getAllMembers().size());
				fullhtm = fullhtm.replace("%message%", "" + message);
				fullhtm = fullhtm.replace("%clanId%", "" + clan.getClanId());
				
				ShowBoard.separateAndSend(fullhtm, player);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	private void saveClanPresentation(String[] param, Player player)
	{
		if (param.length < 3)
			return;
		
		long adena = Integer.valueOf(param[1]);

		String message = "";
		
		for (String s : param)
		{
			s.split(" ");
			message = message + " " + s;
		}
		
		message = message.replaceFirst("add", "");
		message = message.replaceFirst("  ", "");
		
		if (message.length() < 5)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "ClanSearch", "Failed to submit clan info, description is too short!");
			return;
		}
		else if (message.length() > 7800)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "ClanSearch", "Failed to submit clan info, description cannot exceed 7800 chars.");
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO clan_search (visible,clanId,message,timeleft,adenas) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE visible=?,clanId=?,message=?,timeleft=?,adenas=?");
			
			if (player.getInventory().getAdena() > adena)
				player.reduceAdena(adena, true);
			else
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "ClanSearch", "You dont have enough adena to submit presentation.");
				return;
			}
			message = message.replaceFirst("" + adena, "");
			message = message.replaceAll("\n", "<br>");
			statement.setInt(1, 1);
			statement.setInt(2, player.getClanId());
			statement.setString(3, message);
			statement.setInt(4, 604800000);
			statement.setLong(5, adena);
			statement.setInt(6, 1);
			statement.setInt(7, player.getClanId());
			statement.setString(8, message);
			statement.setInt(9, 604800000);
			statement.setLong(10, adena);
			statement.execute();
			
			player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "ClanSearch", "Your presentation has been successfuly submited.");
			loadClansAndShowWindow(player, 1, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	private void removeClanPresentation(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_search WHERE clanId=?");
			statement.setInt(1, player.getClanId());
			statement.execute();
			
			player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "ClanSearch", "Your presentation has been deleted.");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	protected static void updateClans()
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null;
		try
		{
			Map<Clan, Integer> clans = new FastMap<Clan, Integer>();
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clan_search");
			rset = statement.executeQuery();
			while (rset.next())
			{
				Clan clan = ClanTable.getInstance().getClan(rset.getInt("clanId"));
				if (clan != null)
					clans.put(clan, rset.getInt("timeleft"));
			}
			for (Map.Entry<Clan, Integer> entry : clans.entrySet())
			{
				if (entry.getValue() - (UPDATE_TIME * 60000) < 1)
				{
					statement2 = con.prepareStatement("UPDATE clan_search SET timeleft=0,visible=0 WHERE clanId=?");
					statement2.setInt(1, entry.getKey().getClanId());
					statement2.execute();
				}
				else
				{
					statement2 = con.prepareStatement("UPDATE clan_search SET timeleft=? WHERE clanId=?");
					statement2.setInt(1, entry.getValue() - (UPDATE_TIME * 60000));
					statement2.setInt(2, entry.getKey().getClanId());
					statement2.execute();
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(statement2);
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public static void main(String[] args)
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			public void run()
			{
				updateClans();
			}
			
		}, UPDATE_TIME * 60000, UPDATE_TIME * 60000);
	}
}
