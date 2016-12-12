package services;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;

public class ServiceClanReputation extends Functions
{
	public void giveclanrept()
	{
		Player player = getSelf();
		if (player == null)
			return;
		
		if (!Config.SERVICES_CLAN_REP_POINTS)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		if (player.getClan() == null)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You must have a clan to use this function.");
			return;
		}
		
		if (player.getClan().getLevel() < 5)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Your clan must be atleast level 5 to use this function.");
			return;
		}
		
		if (getItemCount(player, Config.SERVICE_CLAN_REP_ITEM) < Config.SERVICE_CLAN_REP_COST)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}
		
		removeItem(player, Config.SERVICE_CLAN_REP_ITEM, Config.SERVICE_CLAN_REP_COST);
		
		int reputationtoAdd = Config.SERVICE_CLAN_REP_ADD;
		int currentClanRep = player.getClan().getReputationScore();
		if (player.getClan().getLevel() >= 5)
			player.getClan().incReputation(reputationtoAdd, false, "service_clanreputation");
		
		player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Your clan has recived " + Config.SERVICE_CLAN_REP_ADD + " reputation points.");
		
		Log.addDonation("Character " + player + " increased his clan " + player.getClan().getName() + " reputation score from " + (currentClanRep - reputationtoAdd) + " to " + currentClanRep + " .", "clan_reputation");
		
		showclanrepthtml();
	}
	
	public void showclanrepthtml()
	{
		Player player = getSelf();
		if (player == null)
			return;
		
		if (!Config.SERVICES_CLAN_REP_POINTS)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		String clanReput = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/clanreputation.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICE_CLAN_REP_ITEM).getName();
		int cost = Config.SERVICE_CLAN_REP_COST;
		String clanRept = "";
		
		if (player.getClan() != null)
			clanRept = String.valueOf(player.getClan().getReputationScore());
		else
			clanRept = "Not in Clan";
		
		clanReput = clanReput.replace("%item%", itemName);
		clanReput = clanReput.replace("%cost%", "" + cost);
		clanReput = clanReput.replace("%serviceAddReput%", "" + Config.SERVICE_CLAN_REP_ADD);
		clanReput = clanReput.replace("%playerName%", player.getName());
		clanReput = clanReput.replace("%currentReput%", "" + clanRept);
		
		show(clanReput, player);
	}
}