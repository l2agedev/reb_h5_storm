package services;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Util;

/**
 * 
 * @author Infern0
 * Color picker: http://www.lineage2.es/misc/l2-user_name_color.php
 */
public class NickColor extends Functions
{
	public void list()
	{
		Player player = getSelf();
		if (player == null)
			return;
		
		if (!Config.SERVICES_CHANGE_NICK_COLOR_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		String nicknameColor = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/nicknamecolor.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_NICK_COLOR_ITEM).getName();
		String cost = Util.formatAdena(Config.SERVICES_CHANGE_NICK_COLOR_PRICE);
		
		
		nicknameColor = nicknameColor.replace("%item%", itemName);
		nicknameColor = nicknameColor.replace("%cost%", cost);
		nicknameColor = nicknameColor.replace("%playerName%", player.getName());
		
		show(nicknameColor, player);
	}
	
	public void change(String[] param)
	{
		Player player = getSelf();
		if(player == null || param == null || param.length == 0)
			return;
		
		if (!Config.SERVICES_CHANGE_NICK_COLOR_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		if (param[0].equalsIgnoreCase("FFFFFF"))
		{
			if (player.getNameColor() == Integer.decode("0xFFFFFF"))
			{
				player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You have already default name color.");
				list();
				return;
			}
			
			player.setNameColor(Integer.decode("0xFFFFFF"));
			player.broadcastUserInfo(true);
			return;
		}
		
		if (getItemCount(player, Config.SERVICES_CHANGE_NICK_COLOR_ITEM) < Config.SERVICES_CHANGE_NICK_COLOR_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You dont have enough items to change color.");
			return;
		}
		
		removeItem(player, Config.SERVICES_CHANGE_NICK_COLOR_ITEM, Config.SERVICES_CHANGE_NICK_COLOR_PRICE);
		
		player.setNameColor(Integer.parseInt(param[0]));
		player.broadcastUserInfo(true);
		player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Your Nickname color has been changed!");
		list();
	}
}