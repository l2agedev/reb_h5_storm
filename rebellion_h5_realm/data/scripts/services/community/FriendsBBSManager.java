package services.community;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.actor.instances.player.Friend;
import l2r.gameserver.network.serverpackets.FriendAddRequest;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.TimeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendsBBSManager implements ScriptFile, ICommunityBoardHandler
{
	private static Logger _log = LoggerFactory.getLogger(FriendsBBSManager.class);

	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.ENABLE_NEW_FRIENDS_BOARD)
		{
			_log.info("CommunityBoard: New Friends Manger loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.ENABLE_NEW_FRIENDS_BOARD)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	public void onShutdown()
	{}

	public static void pagr(Player pl, int page)
	{
		if(pl == null)
			return;

		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "bbs_friends.htm", pl);
		int friendforvisual = 0;
		int blockforvisual = 0;
		int all = 0;
		boolean pagereached = false;
		
		int onlineFriends = 0;
		for (Friend fr : pl.getFriendList().getList().values())
		{
			if (fr.isOnline())
				onlineFriends++;
		}

		html = html.replaceAll("%fonline%", "" + onlineFriends);
		html = html.replaceAll("%fall%", "" + pl.getFriendList().getList().size());
		html = html.replaceAll("%blocked%", "" + pl.getBlockList().size());

		int totalpages = 0;

		int maxfpages = (int) Math.round(pl.getFriendList().getList().size() / 12.0 + 1);
		int maxbpages = (int) Math.round(pl.getBlockList().size() / 6.0 + 1);

		if(maxfpages > maxbpages)
			totalpages = maxfpages;
		else if(maxfpages < maxbpages)
			totalpages = maxbpages;
		else
			totalpages = maxfpages;

		if(page == 1)
		{
			html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _friendlist_0_:go " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			html = html.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
			if(totalpages == page)
			{
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _friendlist_0_:go " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				html = html.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _friendlist_0_:go " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _friendlist_0_:go " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}

		if(page <= maxfpages)
		{
			for(Friend fr : getSortedFriendList(pl))
			{
				if (fr == null)
					continue;
				
				String freindName = fr.getName();
				all++;
				if(page == 1 && friendforvisual > 12)
					continue;
				if(!pagereached && all > page * 12)
					continue;
				if(!pagereached && all <= (page - 1) * 12)
					continue;
				friendforvisual++;

				html = html.replaceAll("%charName" + friendforvisual + "%", freindName);
				html = html.replaceAll("%charImage" + friendforvisual + "%", "icon.action011");

				if (pl.isLangRus())
				{
					if(fr.isOnline())
						html = html.replaceAll("%charLoginDate" + friendforvisual + "%", "Друг <font color=\"00CC33\">Онлайн</font>");
					else
						html = html.replaceAll("%charLoginDate" + friendforvisual + "%", "Друг был на сайте <font color=\"5b574c\">" + getLastAccessDate(fr.getObjectId()) + "</font>");
				}
				else
				{
					if(fr.isOnline())
						html = html.replaceAll("%charLoginDate" + friendforvisual + "%", "Friend is <font color=\"00CC33\">Online</font>");
					else
						html = html.replaceAll("%charLoginDate" + friendforvisual + "%", "Friend was online at <font color=\"5b574c\">" + getLastAccessDate(fr.getObjectId()) + "</font>");
				}
				
				html = html.replaceAll("%charwidth" + friendforvisual + "%", "100");
				html = html.replaceAll("%btn" + friendforvisual + "%", "<button value=\"\" action=\"bypass _friendlist_0_:remove " + freindName + " " + page + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Over\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\">");
			}
		}

		if(page <= maxbpages)
		{
			all = 0;
			pagereached = false;

			for(String blockedName : pl.getBlockList())
			{
				all++;
				if(page == 1 && blockforvisual > 6)
					continue;
				if(!pagereached && all > page * 6)
					continue;
				if(!pagereached && all <= (page - 1) * 6)
					continue;
				blockforvisual++;
				
				html = html.replaceAll("%bcharName" + blockforvisual + "%", blockedName);
				html = html.replaceAll("%bcharImage" + blockforvisual + "%", "icon.skill4269");
				html = html.replaceAll("%bcharwidth" + blockforvisual + "%", "100");
				
				if(pl.isLangRus())
					html = html.replaceAll("%bchar" + blockforvisual + "%", "Заблокир. игрок");
				else
					html = html.replaceAll("%bchar" + blockforvisual + "%", "Blocked player.");
					
				html = html.replaceAll("%bbtn" + blockforvisual + "%", "<button value=\"\" action=\"bypass _friendlist_0_:block " + blockedName + " " + page + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Over\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\">");

			}
		}

		if(friendforvisual < 12)
		{
			for(int d = friendforvisual + 1; d != 13; d++)
			{
				html = html.replaceAll("%charName" + d + "%", "&nbsp;");
				html = html.replaceAll("%charImage" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				html = html.replaceAll("%charLoginDate" + d + "%", "&nbsp;");
				html = html.replaceAll("%charwidth" + d + "%", "121");
				html = html.replaceAll("%btn" + d + "%", "&nbsp;");
			}
		}
		
		if(blockforvisual < 6)
		{
			for(int d = blockforvisual + 1; d != 7; d++)
			{
				html = html.replaceAll("%bcharName" + d + "%", "&nbsp;");
				html = html.replaceAll("%bcharImage" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				html = html.replaceAll("%bcharwidth" + d + "%", "121");
				html = html.replaceAll("%bchar" + d + "%", "&nbsp;");
				html = html.replaceAll("%bbtn" + d + "%", "&nbsp;");
			}
		}

		ShowBoard.separateAndSend(html, pl);
	}

	public static void removeFriend(Player player, String name, int page)
	{
		SystemMessage sm;
		if (player == null)
			return;
		
		int id = CharacterDAO.getInstance().getObjectIdByName(name);
		
		if (id == -1)
		{
			sm = new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST);
			sm.addString(name);
			player.sendPacket(sm);
			return;
		}
		
		if (!player.getFriendList().getList().containsKey(id))
		{
			sm = new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST);
			sm.addString(name);
			player.sendPacket(sm);
			return;
		}
		
		// Player deleted from your friendlist
		sm = new SystemMessage(SystemMessage.S1__HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
		sm.addString(name);
		player.sendPacket(sm);
		
		player.getFriendList().removeFriend(name);
		//player.sendPacket(new FriendPacket(false, id));
		
		Player removedFriend = World.getPlayer(name);
		if (removedFriend != null)
		{
			removedFriend.getFriendList().removeFriend(player.getName());
			//removedFriend.sendPacket(new FriendPacket(false, player.getObjectId()));
		}
		
		pagr(player, page);
	}

	public static void addToBlockList(Player player, String charName, int page)
	{
		player.addToBlockList(charName);
		pagr(player, page);
	}

	public static void removeFromBlockList(Player activeChar, String targetName, int page)
	{
		if(activeChar == null)
			return;
		
		activeChar.removeFromBlockList(targetName);
		pagr(activeChar, page);
	}

	public static boolean tryFriendInvite(Player activeChar, String friendName)
	{
		if (activeChar == null)
			return false;
		
		Player friend = World.getPlayer(friendName);
		
		SystemMessage sm;
		// can't use friend invite for locating invisible characters
		if (friend == null || !friend.isOnline() || friend.isInvisible())
		{
			//Target is not found in the game.
			sm = new SystemMessage(SystemMessage.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME);
			activeChar.sendPacket(sm);
			return false;
		}
		else if (friend == activeChar)
		{
			//You cannot add yourself to your own friend list.
			sm = new SystemMessage(SystemMessage.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST);
			activeChar.sendPacket(sm);
			return false;
		}
		else if (activeChar.isInBlockList(friend))
		{
			sm = new SystemMessage(SystemMessage.YOU_HAVE_BLOCKED_C1);
			sm.addName(friend);
			activeChar.sendPacket(sm);
			return false;
		}
		else if (friend.isInBlockList(activeChar))
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.community.friendsbbsmanager.blocked", activeChar));
			return false;
		}
		else if (activeChar.isInOlympiadMode() || friend.isInOlympiadMode())
		{
			sm = new SystemMessage(SystemMessage.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (activeChar.getFriendList().getList().containsKey(friend.getObjectId()))
		{
			// Player already is in your friendlist
			sm = new SystemMessage(SystemMessage.ALREADY_REGISTERED_ON_THE_FRIENDS_LIST);
			sm.addString(friendName);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (!friend.isProcessingRequest())
		{
			// requets to become friend
			//activeChar.onTransactionRequest(friend);
			sm = new SystemMessage(SystemMessage.YOU_VE_REQUESTED_C1_TO_BE_ON_YOUR_FRIENDS_LIST);
			sm.addString(friendName);
			FriendAddRequest ajf = new FriendAddRequest(activeChar.getName());
			friend.sendPacket(ajf);
		}
		else
		{
			sm = new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER);
			sm.addString(friendName);
		}
		activeChar.sendPacket(sm);
		
		return true;
	}
	
	private static List<Friend> getSortedFriendList(Player player)
	{
		List<Friend> result = new ArrayList<Friend>();
		List<Friend> resultOnline = new ArrayList<Friend>();
		List<Friend> resultOffline = new ArrayList<Friend>();
		for(Friend fr : player.getFriendList().getList().values())
		{
			if(player.isInBlockList(fr.getObjectId()))
				continue;

			if(fr.isOnline())
				resultOnline.add(fr);
			else
				resultOffline.add(fr);
		}
		result.addAll(resultOnline);
		result.addAll(resultOffline);
		
		return result;
	}
	
	public static String getLastAccessDate(int charId)
	{
		long lastAccess = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT lastAccess FROM characters WHERE obj_Id=?");
			statement.setInt(1, charId);
			rset = statement.executeQuery();
			while (rset.next())
				lastAccess = rset.getLong(1);
		}
		catch (Exception e)
		{
			_log.warn("Error while getting last access for characterId: " + charId + ". Error msg: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return TimeUtils.convertDateToString(lastAccess * 1000);
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		if (bypass.equals("_friendlist_0_"))
			pagr(player, 1);
		else if(bypass.startsWith("_friendlist_0_:invite"))
		{
			String[] cm = bypass.split(" ");
			
			String sub = bypass.substring(21);
			
			if (sub.isEmpty())
			{
				player.sendMessage(new CustomMessage("scripts.services.community.friendsbbsmanager.empty_input", player));
				return;
			}
			
			if(cm.length > 0 && cm[1] != null && cm[1].length() < 16)
				tryFriendInvite(player, cm[1]);
		}
		else if(bypass.startsWith("_friendlist_0_:go"))
		{
			String[] cm = bypass.split(" ");
			
			String sub = bypass.substring(17);
			
			if (sub.isEmpty())
			{
				player.sendMessage(new CustomMessage("scripts.services.community.friendsbbsmanager.empty_input", player));
				return;
			}
			
			if(cm.length > 0 && cm[1].length() < 2)
				pagr(player, Integer.parseInt(cm[1]));
		}
		else if(bypass.startsWith("_friendlist_0_:block"))
		{
			String[] cm = bypass.split(" ");

			String sub = bypass.substring(20);
			
			if (sub.isEmpty())
			{
				player.sendMessage(new CustomMessage("scripts.services.community.friendsbbsmanager.empty_input", player));
				return;
			}
			
			int blockedId = CharacterDAO.getInstance().getObjectIdByName(cm[1]);
			if(cm.length > 0 && cm[1].length() < 16)
				
				if(player.isInBlockList(blockedId))
					removeFromBlockList(player, cm[1], Integer.parseInt(cm[2]));
				else
					addToBlockList(player, cm[1], 1);
		}
		else if(bypass.startsWith("_friendlist_0_:remove"))
		{
			String[] cm = bypass.split(" ");
			
			String sub = bypass.substring(21);
			
			if (sub.isEmpty())
			{
				player.sendMessage(new CustomMessage("scripts.services.community.friendsbbsmanager.empty_input", player));
				return;
			}
			if(cm.length > 0 && cm[1].length() < 16)
				removeFriend(player, cm[1], Integer.parseInt(cm[2]));
		}
		else
			pagr(player, 1);
		
	}

	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_friendlist_0_",
			"_friendlist_0_:invite",
			"_friendlist_0_:go",
			"_friendlist_0_:block",
			"_friendlist_0_:remove"
		};
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{	
	}
}