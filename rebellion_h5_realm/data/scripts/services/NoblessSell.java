package services;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

import quests._234_FatesWhisper;

public class NoblessSell extends Functions
{
	
	public void noble_page()
	{
		Player player = getSelf();
		
		if(player == null)
			return;
		
		if (!Config.SERVICES_NOBLESS_SELL_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		if(player.isNoble())
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You are already noble.");
			return;
		}
		
		if(player.getSubLevel() < 75)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You must have a subclass level 75 first.");
			return;
		}
		
		String htmlnoble = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/getnobless.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_NOBLESS_SELL_ITEM).getName();
		String cost = Util.formatAdena(Config.SERVICES_NOBLESS_SELL_PRICE);
		
		htmlnoble = htmlnoble.replace("%item%", itemName);
		htmlnoble = htmlnoble.replace("%cost%", cost);
		htmlnoble = htmlnoble.replace("%playerName%", player.getName());
		htmlnoble = htmlnoble.replace("%noblestatus%", player.isNoble() == true ? "Noble" : "Not Noble");
		
		show(htmlnoble, player);
	}
	
	public void make_noble()
	{
		Player player = getSelf();

		if (!Config.SERVICES_NOBLESS_SELL_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		if(player.isNoble())
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You are already noble.");
			return;
		}
		
		if(player.getSubLevel() < 75)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You must have a subclass level 75 first.");
			return;
		}
		
		if(getItemCount(player, Config.SERVICES_NOBLESS_SELL_ITEM) < Config.SERVICES_NOBLESS_SELL_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}
		
		makeSubQuests();
		becomeNoble();
		player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Congratulations, you have earned noble status!");
		removeItem(player, Config.SERVICES_NOBLESS_SELL_ITEM, Config.SERVICES_NOBLESS_SELL_PRICE);
		Log.addDonation("Character " + player + " earned noble status trough service", "noblestatus");
	}

	public void makeSubQuests()
	{
		Player player = getSelf();
		if(player == null)
			return;
		Quest q = QuestManager.getQuest(_234_FatesWhisper.class);
		QuestState qs = player.getQuestState(q.getClass());
		if(qs != null)
			qs.exitCurrentQuest(true);
		q.newQuestState(player, Quest.COMPLETED);

		if(player.getRace() == Race.kamael)
		{
			q = QuestManager.getQuest("_236_SeedsOfChaos");
			qs = player.getQuestState(q.getClass());
			if(qs != null)
				qs.exitCurrentQuest(true);
			q.newQuestState(player, Quest.COMPLETED);
		}
		else
		{
			q = QuestManager.getQuest("_235_MimirsElixir");
			qs = player.getQuestState(q.getClass());
			if(qs != null)
				qs.exitCurrentQuest(true);
			q.newQuestState(player, Quest.COMPLETED);
		}
	}

	public void becomeNoble()
	{
		Player player = getSelf();
		if(player == null || player.isNoble())
			return;

		Olympiad.addNoble(player);
		player.setNoble(true);
		player.updatePledgeClass();
		player.updateNobleSkills();
		player.sendPacket(new SkillList(player));
		player.broadcastUserInfo(true);
	}
}