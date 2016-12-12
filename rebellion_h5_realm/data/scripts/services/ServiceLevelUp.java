package services;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.network.serverpackets.ShortCutInit;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;

public class ServiceLevelUp extends Functions
{
	public void levelupPage()
	{
		Player player = getSelf();
		if (player == null)
			return;
		
		if (!Config.SERVICES_LVL_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		String html = null;
		
		html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/levelup.htm", player);
		String add = "";
		int cost = 0;
		
		if (player.getLevel() >= 85)
		{
			add += "<font color=\"FFFF00\">You have reached the max level!</font><br>";
		}
		else if (player.getLevel() >= 79) // if level 79 set him to 85.
		{
			add += "<button value=\"Raise your level to " + player.getLevel() + " - 85\" action=\"bypass -h scripts_services.ServiceLevelUp:upfrom79to85" + "\" width=250 height=25 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">" + "</a><br>";
			cost = Config.SERVICES_LVL_79_85_PRICE;
		}
		else
		{
			add += "<button value=\"Raise your level to " + player.getLevel() + " - 85\" action=\"bypass -h scripts_services.ServiceLevelUp:upfrom1to85" + "\" width=250 height=25 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">" + "</a><br>";
			cost = Config.SERVICES_LVL_1_85_PRICE;
		}
			
		html = html.replaceAll("%plrLevel%", "" + player.getLevel());
		html = html.replaceAll("%itemName%", "" + ItemHolder.getInstance().getTemplate(Config.SERVICES_LVL_UP_ITEM).getName());
		html = html.replaceAll("%cost%", "" + cost);
		html = html.replaceFirst("%toreplace%", add);
		
		show(html, player);
	}
	
	public void upfrom1to85()
	{
		Player player = getSelf();
		if (player == null)
			return;
		
		if (!Config.SERVICES_LVL_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		if (getItemCount(player, Config.SERVICES_LVL_UP_ITEM) < Config.SERVICES_LVL_1_85_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}
		
		if (player.getLevel() == Config.SERVICES_LVL_UP_MAX)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Level requirements are not meet.");
			return;
		}
		
		int level = 85;
		removeItem(player, Config.SERVICES_LVL_UP_ITEM, Config.SERVICES_LVL_1_85_PRICE);
		setLevel(player, level);
		player.sendPacket(new ShortCutInit(player), new SkillList(player), new SkillCoolTime(player));
		player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You have increased your character to Level: " + player.getLevel());
		Log.addDonation("Character " + player + " level himself to " + player.getLevel() + " using service: 1-85", "levelup");
	}
	
	public void upfrom79to85()
	{
		Player player = getSelf();
		if (player == null)
			return;
		
		if (!Config.SERVICES_LVL_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		if (getItemCount(player, Config.SERVICES_LVL_UP_ITEM) < Config.SERVICES_LVL_79_85_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}
		
		if (player.getLevel() < 79 && player.getLevel() == Config.SERVICES_LVL_UP_MAX)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Level requirements are not meet.");
			return;
		}
			
		int level = 85;
		removeItem(player, Config.SERVICES_LVL_UP_ITEM, Config.SERVICES_LVL_79_85_PRICE);
		setLevel(player, level);
		player.sendPacket(new ShortCutInit(player), new SkillList(player), new SkillCoolTime(player));
		player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You have increased your character to Level: " + player.getLevel());
		Log.addDonation("Character " + player + " level himself to " + player.getLevel() + " using service: 79-85", "levelup");
	}
	
	private void setLevel(Player player, int level)
	{
		Long exp_add = Experience.LEVEL[level] - player.getExp();
		player.addExpAndSp(exp_add, 0, 0, 0, false, false);
	}
}