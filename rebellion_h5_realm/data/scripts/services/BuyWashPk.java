package services;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

public class BuyWashPk extends Functions
{
	public void list()
	{
		Player player = getSelf();
		
		if (player == null)
			return;
		
		if(!Config.SERVICES_WASH_PK_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		String htmlpk = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/reducepkcount.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_WASH_PK_ITEM).getName();
		String cost = Util.formatAdena(Config.SERVICES_WASH_PK_PRICE);
		
		// How many pk to remove ?
		int pktoremove = 1;
				
		htmlpk = htmlpk.replace("%item%", itemName);
		htmlpk = htmlpk.replace("%cost%", cost);
		htmlpk = htmlpk.replace("%playerName%", player.getName());
		htmlpk = htmlpk.replace("%pkcount%", "" + player.getPkKills());
		htmlpk = htmlpk.replace("%pktoremove%", "" + pktoremove);
		
		show(htmlpk, player);
	}

	public void get()
	{
		Player player = getSelf();
		if (player == null)
			return;
		
		if(!Config.SERVICES_WASH_PK_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		// How many pk to remove ?
		int pktoremove = 1;
		int kills = player.getPkKills();
		
		if(player.getPkKills() == 0)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You dont have any PK.");
			return;
		}

		if(getItemCount(player, Config.SERVICES_WASH_PK_ITEM) < Config.SERVICES_WASH_PK_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}
		
		player.setPkKills(kills - pktoremove);
		player.broadcastCharInfo();
		player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Now you have " + player.getPkKills() + " PK points.");
		removeItem(player, Config.SERVICES_WASH_PK_ITEM, Config.SERVICES_WASH_PK_PRICE);
		Log.addDonation("Character " + player + " reduced his PK count with " + pktoremove + " from services", "removepk");
	}
}