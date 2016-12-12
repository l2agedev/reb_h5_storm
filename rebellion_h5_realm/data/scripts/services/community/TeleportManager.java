package services.community;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.network.serverpackets.HideBoard;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.mapregion.DomainArea;
import l2r.gameserver.utils.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;

public class TeleportManager implements ScriptFile, ICommunityBoardHandler
{
	
	private static final Logger _log = LoggerFactory.getLogger(TeleportManager.class);
	
	@Override
	public void onLoad()
	{
		if (Config.COMMUNITYBOARD_ENABLED && Config.COMMUNITY_TELEPORT_ENABLED)
		{
			_log.info("CommunityBoard: Teleport service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if (Config.COMMUNITYBOARD_ENABLED && Config.COMMUNITY_TELEPORT_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbsteleport;",
			"_bbsteleport;delete;",
			"_bbsteleport;save; ",
			"_bbsteleport;teleport;"
		};
	}
	
	public class CBteleport
	{
		public int _teleportId = 0;
		public String _teleportName = "";
		public int _objId = 0;
		public int _x = 0;
		public int _y = 0;
		public int _z = 0;
	}
	
	@Override
	public void onBypassCommand(Player player, String command)
	{
		
		if (!Config.COMMUNITY_TELEPORT_ENABLED)
			return;
		
		player.setSessionVar("add_fav", null);
		
		if (command.equals("_bbsteleport;"))
		{
			showBookmarkMenu(player);
		}
		else if (command.startsWith("_bbsteleport;save; "))
		{
			StringTokenizer sb = new StringTokenizer(command, ";");
			sb.nextToken();
			sb.nextToken();
			String teleportName = sb.nextToken();
			
			addBookmark(player, teleportName);
			showBookmarkMenu(player);
		}
		else if (command.startsWith("_bbsteleport;delete;"))
		{
			StringTokenizer sb = new StringTokenizer(command, ";");
			sb.nextToken();
			sb.nextToken();
			int name = Integer.parseInt(sb.nextToken());
			
			deleteBookmark(player, name);
			showBookmarkMenu(player);
		}
		else if (command.startsWith("_bbsteleport;teleport;"))
		{
			StringTokenizer sb = new StringTokenizer(command, " ");
			sb.nextToken();
			int x = Integer.parseInt(sb.nextToken());
			int y = Integer.parseInt(sb.nextToken());
			int z = Integer.parseInt(sb.nextToken());
			int price = Integer.parseInt(sb.nextToken());
			
			teleportTo(player, x, y, z, price);
		}
	}
	
	private void teleportTo(Player player, int x, int y, int z, int price)
	{
		if (!player.isGM())
		{
			if (player.isInJail() || player.isCursedWeaponEquipped() || NexusEvents.isInEvent(player) || player.getReflectionId() != ReflectionManager.DEFAULT_ID/* || player.getPvpFlag() != 0*/ || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped() || player.isInZone(ZoneType.no_escape) || /*player.isInZone(ZoneType.SIEGE) ||*/ player.isInZone(ZoneType.epic))
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Teleporter", (player.isLangRus() ? "Телепортация невозможна!" : "Teleportation is not possible!"));
				return;
			}
			
			if (player.getPvpFlag() > 0 || player.getKarma() > 0)
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Teleporter", "You cannot use this function while pvp flagged or karma.");
				return;
			}
			
			if (PremiumAccountsTable.getGatekeeperOutsidePeace(player))
			{
				if (player.getPvpFlag() > 0 || player.getKarma() > 0)
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Teleporter", "You cannot use this function while pvp flagged or karma.");
					return;
				}
			}
			else
			{	
				if (!player.isInZone(ZoneType.peace_zone) && !player.isInZone(ZoneType.RESIDENCE))
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Teleporter", (player.isLangRus() ? "Вы должны быть в зону мира, чтобы использовать эту функцию." : "You must be inside peace zone to use this function."));
					return;
				}
			}
			
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0)
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Teleporter", "You cannot use this while having karma! Go Away!");
				return;
			}
			
			Location pos = Location.findPointToStay(x, y, z, 50, 100, player.getGeoIndex());
			
			if(player.getReflection().isDefault())
			{
				DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, pos);
				if(domain != null)
				{
					Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId());
					if (!Config.COMMUNITY_TELEPORT_DURING_SIEGES && castle != null && castle.getSiegeEvent().isInProgress())
					{
						player.sendPacket(Msg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
						return;
					}
				}
			}
			
			if(player.isTerritoryFlagEquipped() || player.isCombatFlagEquipped())
			{
				player.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
				return;
			}

			// Global teleport price.
			if (Config.COMMUNITY_TELEPORT_GLOBAL_PRICE > 0)
				price = Config.COMMUNITY_TELEPORT_GLOBAL_PRICE;
			
			if (price > 0 && player.getAdena() < price)
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
				return;
			}
			
			if (price > 0)
				player.reduceAdena(price, true);
		}
		
		player.setVar("EnterUrban", 0, -1);
		if (Config.HELLBOUND_ENTER_NO_QUEST)
			player.setVar("EnterHellbound", 1, -1);
		else
			player.setVar("EnterHellbound", 0, -1);
		player.setVar("EnterBaium", 0, -1);
		player.setVar("EnterAntharas", 0, -1);
		player.setVar("EnterValakas", 0, -1);
		
		player.teleToLocation(x, y, z);
		player.sendPacket(new HideBoard());
	}
	
	private void showBookmarkMenu(Player player)
	{
		CBteleport tp;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM community_bookmark WHERE objId=?;");
			st.setLong(1, player.getObjectId());
			ResultSet rs = st.executeQuery();
			TextBuilder html = new TextBuilder();
			html.append("<table width=220>");
			while (rs.next())
			{
				tp = new CBteleport();
				tp._teleportId = rs.getInt("teleportId");
				tp._teleportName = rs.getString("teleportName");
				tp._objId = rs.getInt("objId");
				tp._x = rs.getInt("x");
				tp._y = rs.getInt("y");
				tp._z = rs.getInt("z");
				
				html.append("<tr>");
				html.append("<td>");
				html.append("<button value=\"" + tp._teleportName + "\" action=\"bypass _bbsteleport;teleport; " + tp._x + " " + tp._y + " " + tp._z + " " + Config.COMMUNITY_TELEPORT_BOOKMARK_PRICE + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("<td>");
				html.append("<button value=\"Delete\" action=\"bypass _bbsteleport;delete;" + tp._teleportId + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			DbUtils.closeQuietly(st, rs);
			
			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/teleport/teleport.htm", player);
			content = content.replace("%tp%", html.toString());
			ShowBoard.separateAndSend(content, player);
			return;
			
		}
		catch (Exception e)
		{
			_log.warn("CommunityGatekeeper: Data error on showing Teleport bookmark menu: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		
	}
	
	private void addBookmark(Player player, String teleportName)
	{
		if (player.isCursedWeaponEquipped() || player.isInJail() || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isAttackingNow() || player.isInZone(ZoneType.no_escape) || player.isInZone(ZoneType.SIEGE) || player.isOlympiadGameStart() || player.isInZone(ZoneType.epic))
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Teleporter", (player.isLangRus() ? "Сохранить закладку в вашем состоянии нельзя!" : "Bookmark in your condition can not be!"));
			return;
		}
		
		if (player.isInCombat() || player.getPvpFlag() != 0)
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Teleporter", (player.isLangRus() ? "Сохранение закладок в бою невозможно!" : "Bookmark in combat can not be!"));
			return;
		}
		
		if (teleportName.equals("") || teleportName.equals(null))
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Teleporter", (player.isLangRus() ? "Вы не ввели имя закладки!" : "You have not entered the name of a bookmark!"));
			return;
		}
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM community_bookmark WHERE objId=?;");
			st.setLong(1, player.getObjectId());
			ResultSet rs = st.executeQuery();
			rs.next();
			if (rs.getInt(1) <= Config.COMMUNITY_BOOKMARK_MAX - 1)
			{
				PreparedStatement st1 = con.prepareStatement("SELECT COUNT(*) FROM community_bookmark WHERE objId=? AND teleportName=?;");
				st1.setLong(1, player.getObjectId());
				st1.setString(2, teleportName);
				ResultSet rs1 = st1.executeQuery();
				rs1.next();
				if (rs1.getInt(1) == 0)
				{
					PreparedStatement stAdd = con.prepareStatement("INSERT INTO community_bookmark (objId,teleportName,x,y,z) VALUES(?,?,?,?,?)");
					stAdd.setInt(1, player.getObjectId());
					stAdd.setInt(2, player.getX());
					stAdd.setInt(3, player.getY());
					stAdd.setInt(4, player.getZ());
					stAdd.setString(5, teleportName);
					stAdd.execute();
					DbUtils.closeQuietly(stAdd);
				}
				else
				{
					PreparedStatement stAdd = con.prepareStatement("UPDATE community_bookmark SET x=?, y=?, z=? WHERE objId=? AND teleportName=?;");
					stAdd.setInt(1, player.getObjectId());
					stAdd.setInt(2, player.getX());
					stAdd.setInt(3, player.getY());
					stAdd.setInt(4, player.getZ());
					stAdd.setString(5, teleportName);
					stAdd.execute();
					DbUtils.closeQuietly(stAdd);
				}
			}
			else
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Teleporter", (player.isLangRus() ? "Вы не можете сохранить более " + Config.COMMUNITY_BOOKMARK_MAX + " закладок." : "You can not save more than " + Config.COMMUNITY_BOOKMARK_MAX + " bookmarks."));
				return;
			}
			DbUtils.closeQuietly(st, rs);
			
		}
		catch (Exception e)
		{
			_log.warn("CommunityGatekeeper: Data error on Adding Teleport bookmark: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}
	
	private void deleteBookmark(Player player, int teleportName)
	{
		Connection conDel = null;
		try
		{
			conDel = DatabaseFactory.getInstance().getConnection();
			PreparedStatement stDel = conDel.prepareStatement("DELETE FROM community_bookmark WHERE objId=? AND teleportId=?;");
			stDel.setInt(1, player.getObjectId());
			stDel.setInt(2, teleportName);
			stDel.execute();
			DbUtils.closeQuietly(stDel);
		}
		catch (Exception e)
		{
			_log.warn("CommunityGatekeeper: Data error on Delete Teleport: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conDel);
		}
		
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
}