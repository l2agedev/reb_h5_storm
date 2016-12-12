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
public class TitleColor extends Functions
{
	public void list()
	{
		Player player = getSelf();
		if (player == null)
			return;
		if (!Config.SERVICES_CHANGE_TITLE_COLOR_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		String titleColor = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/titlecolor.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_TITLE_COLOR_ITEM).getName();
		String cost = Util.formatAdena(Config.SERVICES_CHANGE_TITLE_COLOR_PRICE);
		
		titleColor = titleColor.replace("%item%", itemName);
		titleColor = titleColor.replace("%cost%", cost);
		titleColor = titleColor.replace("%playerName%", player.getName());
		
		show(titleColor, player);
	}
	
	public void change(String[] param)
	{
		Player player = getSelf();
		if(player == null || param == null || param.length == 0)
			return;
		
		if (!Config.SERVICES_CHANGE_TITLE_COLOR_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		if (param[0].equalsIgnoreCase("FFFF77"))
		{
			if (player.getNameColor() == Integer.decode("0xFFFF77"))
			{
				player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You have already default title color.");
				list();
				return;
			}
			
			player.setTitleColor(Integer.decode("0xFFFF77"));
			player.broadcastUserInfo(true);
			return;
		}
		
		if (getItemCount(player, Config.SERVICES_CHANGE_TITLE_COLOR_ITEM) < Config.SERVICES_CHANGE_TITLE_COLOR_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You dont have enough items to change color.");
			return;
		}
		
		removeItem(player, Config.SERVICES_CHANGE_TITLE_COLOR_ITEM, Config.SERVICES_CHANGE_TITLE_COLOR_PRICE);
		
		player.setTitleColor(Integer.parseInt(param[0]));
		player.broadcastUserInfo(true);
		list();
	}
}