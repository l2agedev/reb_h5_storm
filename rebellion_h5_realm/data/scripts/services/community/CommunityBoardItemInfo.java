package services.community;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.Func;
import l2r.gameserver.stats.funcs.FuncAdd;
import l2r.gameserver.stats.funcs.FuncDiv;
import l2r.gameserver.stats.funcs.FuncEnchant;
import l2r.gameserver.stats.funcs.FuncMul;
import l2r.gameserver.stats.funcs.FuncSet;
import l2r.gameserver.stats.funcs.FuncSub;
import l2r.gameserver.stats.funcs.FuncTemplate;
import l2r.gameserver.templates.item.ArmorTemplate;
import l2r.gameserver.templates.item.EtcItemTemplate;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.item.WeaponTemplate;
import l2r.gameserver.utils.HtmlUtils;
import l2r.gameserver.utils.Language;
import l2r.gameserver.utils.Util;

public class CommunityBoardItemInfo implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityBoardItemInfo.class);
	
	private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
	
	
	static
	{
		pf.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(2);
	}
	
	private String val1 = "";
	private String val2 = "";
	private String val3 = "";
	private String val4 = "";
	
	public static final CommunityBoardItemInfo getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityBoardItemInfo _instance = new CommunityBoardItemInfo();
	}
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.COMMUNITY_ITEM_INFO)
		{
			_log.info("CommunityBoard: ItemInfo service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.COMMUNITY_ITEM_INFO)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbsitemlist", "_bbsitematributes", "_bbsitemstats", "_bbsitemskills", "_bbsarmorinfoid", "_bbsarmorinfoname", "_bbsweaponinfoid", "_bbsweaponinfoname", "_bbsiteminfoid", "_bbsiteminfoname" };
	}
	
	@Override
	public void onBypassCommand(Player activeChar, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();
		val1 = "";
		val2 = "";
		val3 = "";
		val4 = "";
		
		if (st.countTokens() == 1)
		{
			val1 = st.nextToken();
		}
		else if (st.countTokens() == 2)
		{
			val1 = st.nextToken();
			val2 = st.nextToken();
		}
		else if (st.countTokens() == 3)
		{
			val1 = st.nextToken();
			val2 = st.nextToken();
			val3 = st.nextToken();
		}
		else if (st.countTokens() == 4)
		{
			val1 = st.nextToken();
			val2 = st.nextToken();
			val3 = st.nextToken();
			val4 = st.nextToken();
		}
		
		if (cmd.equalsIgnoreCase("_bbsitemlist"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/list.htm").toString(), activeChar);
			
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsiteminfolist"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/itemlist.htm").toString(), activeChar);
			
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsarmorinfoid"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/iteminfo.htm").toString(), activeChar);
			
			content = content.replace("%iteminfo%", generateArmorInfo(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsarmorinfoname"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/iteminfo.htm").toString(), activeChar);
			
			
			String str = null;
			if (!val1.equals(""))
			{
				str = val1;
			}
			
			if (!val2.equals(""))
			{
				str = new StringBuilder().append(val1).append(" ").append(val2).toString();
			}
			
			if (!val3.equals(""))
			{
				str = new StringBuilder().append(val1).append(" ").append(val2).append(" ").append(val3).toString();
			}
			
			if (!val4.equals(""))
			{
				str = new StringBuilder().append(val1).append(" ").append(val2).append(" ").append(val3).append(" ").append(val4).toString();
			}
			
			content = content.replace("%iteminfo%", generateArmorInfo(activeChar, str));
			
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsweaponinfoid"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/iteminfo.htm").toString(), activeChar);
			
			content = content.replace("%iteminfo%", generateWeaponInfo(activeChar, Integer.parseInt(val1)));
			
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsweaponinfoname"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/iteminfo.htm").toString(), activeChar);
			
			
			String str = null;
			
			if (!val1.equals(""))
			{
				str = val1;
			}
			
			if (!val2.equals(""))
			{
				str = new StringBuilder().append(val1).append(" ").append(val2).toString();
			}
			
			if (!val3.equals(""))
			{
				str = new StringBuilder().append(val1).append(" ").append(val2).append(" ").append(val3).toString();
			}
			
			if (!val4.equals(""))
			{
				str = new StringBuilder().append(val1).append(" ").append(val2).append(" ").append(val3).append(" ").append(val4).toString();
			}
			
			content = content.replace("%iteminfo%", generateWeaponInfo(activeChar, str));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsiteminfoid"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/iteminfo.htm").toString(), activeChar);
			
			content = content.replace("%iteminfo%", generateItemInfo(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsiteminfoname"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/iteminfo.htm").toString(), activeChar);
			
			String str = null;
			
			if (!val1.equals(""))
			{
				str = val1;
			}
			
			if (!val2.equals(""))
			{
				str = new StringBuilder().append(val1).append(" ").append(val2).toString();
			}
			
			if (!val3.equals(""))
			{
				str = new StringBuilder().append(val1).append(" ").append(val2).append(" ").append(val3).toString();
			}
			
			if (!val4.equals(""))
			{
				str = new StringBuilder().append(val1).append(" ").append(val2).append(" ").append(val3).append(" ").append(val4).toString();
			}
			
			content = content.replace("%iteminfo%", generateItemInfo(activeChar, str));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsitemskills"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/iteminfo.htm").toString(), activeChar);
			
			content = content.replace("%iteminfo%", generateItemSkills(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsitemstats"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/iteminfo.htm").toString(), activeChar);
			
			content = content.replace("%iteminfo%", generateItemStats(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsitematributes"))
		{
			String content = HtmCache.getInstance().getNullable(new StringBuilder().append(Config.BBS_HOME_DIR).append("pages/iteminfo/iteminfo.htm").toString(), activeChar);
			
			content = content.replace("%iteminfo%", generateItemAttribute(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
	}
	
	private String generateItemSkills(Player player, int id)
	{
		StringBuilder result = new StringBuilder();
		
		result.append("<table width=400 border=0>");
		
		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);
		String str;
		if (temp.isWeapon())
		{
			str = "_bbsweaponinfoid";
		}
		else
		{
			if ((temp.isArmor()) || (temp.isAccessory()))
				str = "_bbsarmorinfoid";
			else
			{
				str = "_bbsiteminfoid";
			}
		}
		for (Skill skill : temp.getAttachedSkills())
		{
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"").append(skill.getIcon()).append("\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Название скила: " : "Skill name: ").append(new StringBuilder().append("</font>").append(skill.getName()).append("<br1>").toString()).append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "ID скила: " : "ID: ").append("</font>").append(skill.getId());
			result.append("</td>");
			result.append("</tr>");
		}
		
		result.append("</table>");
		result.append("<center><table width=690>");
		result.append("<tr>");
		result.append("<td WIDTH=690 align=center valign=top>");
		result.append("<center><br><br><button value=\"");
		result.append(player.getLanguage() == Language.RUSSIAN ? "Информация о предмете" : "Item information");
		result.append("\" action=\"bypass ").append(str).append(" ").append(temp.getItemId()).append("\" width=200 height=29  back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table></center>");
		
		return result.toString();
	}
	
	private String generateItemStats(Player player, int id)
	{
		StringBuilder result = new StringBuilder();
		
		result.append("<table width=400 border=0>");
		
		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);
		String str;
		if (temp.isWeapon())
		{
			str = "_bbsweaponinfoid";
		}
		else
		{
			if ((temp.isArmor()) || (temp.isAccessory()))
				str = "_bbsarmorinfoid";
			else
			{
				str = "_bbsiteminfoid";
			}
		}
		for (FuncTemplate func : temp.getAttachedFuncs())
		{
			if (getFunc(player, func) != null)
			{
				result.append("<tr><td>› <font color=\"b09979\">").append(getFunc(player, func)).append("</font></td></tr><br>");
			}
		}
		
		result.append("</table>");
		
		result.append("<center><table width=690>");
		result.append("<tr>");
		result.append("<td WIDTH=690 align=center valign=top>");
		result.append("<center><br><br><button value=\"");
		result.append(player.getLanguage() == Language.RUSSIAN ? "Информация о предмете" : "Item information ");
		result.append("\" action=\"bypass ").append(str).append(" ").append(temp.getItemId()).append("\" width=200 height=29  back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table></center>");
		return result.toString();
	}
	
	private String generateItemAttribute(Player player, int id)
	{
		StringBuilder result = new StringBuilder();
		
		String str;
		
		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);
		if (temp.isWeapon())
		{
			str = "_bbsweaponinfoid";
		}
		else
		{
			if ((temp.isArmor()) || (temp.isAccessory()))
				str = "_bbsarmorinfoid";
			else
			{
				str = "_bbsiteminfoid";
			}
		}
		if (temp.getBaseAttributeValue(Element.FIRE) > 0)
		{
			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"icon.etc_fire_stone_i00\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Атрибут Огня</font> " : "Attributes Fire</font>").append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "Бонус атрибута:</font> " : "Attributes bonuses:</font> ").append(temp.getBaseAttributeValue(Element.FIRE)).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");
		}
		
		if (temp.getBaseAttributeValue(Element.WATER) > 0)
		{
			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"icon.etc_water_stone_i00\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Атрибут Воды</font> " : "Attributes Water</font>").append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "Бонус атрибута:</font> " : "Attributes bonuses:</font> ").append(temp.getBaseAttributeValue(Element.WATER)).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");
		}
		
		if (temp.getBaseAttributeValue(Element.WIND) > 0)
		{
			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"icon.etc_wind_stone_i00\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Атрибут Ветра</font> " : "Attributes Wind</font>").append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "Бонус атрибута:</font> " : "Attributes bonuses:</font> ").append(temp.getBaseAttributeValue(Element.WIND)).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");
		}
		
		if (temp.getBaseAttributeValue(Element.EARTH) > 0)
		{
			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"icon.etc_earth_stone_i00\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Атрибут Земли</font> " : "Attributes Earth</font>").append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "Бонус атрибута:</font> " : "Attributes bonuses:</font> ").append(temp.getBaseAttributeValue(Element.EARTH)).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");
		}
		
		if (temp.getBaseAttributeValue(Element.HOLY) > 0)
		{
			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"icon.etc_holy_stone_i00\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Атрибут Света</font> " : "Attributes Light</font>").append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "Бонус атрибута:</font> " : "Attributes bonuses:</font> ").append(temp.getBaseAttributeValue(Element.HOLY)).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");
		}
		
		if (temp.getBaseAttributeValue(Element.UNHOLY) > 0)
		{
			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"icon.etc_unholy_stone_i00\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Атрибут Темноты</font> " : "Attributes Dark</font>").append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "Бонус атрибута:</font> " : "Attributes bonuses:</font> ").append(temp.getBaseAttributeValue(Element.UNHOLY)).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");
		}
		
		result.append("<center><table width=690>");
		result.append("<tr>");
		result.append("<td WIDTH=690 align=center valign=top>");
		result.append("<center><br><br><button value=\"");
		result.append(player.getLanguage() == Language.RUSSIAN ? "Информация о предмете" : "Item information ");
		result.append("\" action=\"bypass ").append(str).append(" ").append(temp.getItemId()).append("\" width=200 height=29  back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table></center>");
		
		return result.toString();
	}
	
	private String generateItemInfo(Player player, String name)
	{
		StringBuilder result = new StringBuilder();
		
		for (ItemTemplate temp : ItemHolder.getInstance().getAllTemplates())
		{
			if ((temp != null) && (!temp.isArmor()) && (!temp.isWeapon()) && (!temp.isAccessory()) && ((temp.getName() == name) || (val2.equals("")) ? !temp.getName().startsWith(name) : (temp.getName().contains(name)) || (temp.getName().equals(name)) || (temp.getName().equalsIgnoreCase(name))))
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(temp.getName());
				result.append("\" action=\"bypass _bbsiteminfoid ").append(temp.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
		}
		
		return result.toString();
	}
	
	private String generateItemInfo(Player player, int id)
	{
		StringBuilder result = new StringBuilder();
		
		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);
		if ((temp != null) && (!temp.isArmor()) && (!temp.isWeapon()) && (!temp.isAccessory()))
		{
			EtcItemTemplate etcitem = (EtcItemTemplate) temp;
			String icon = etcitem.getIcon();
			if ((icon == null) || (icon.equals("")))
			{
				icon = "icon.etc_question_mark_i00";
			}
			
			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"").append(icon).append("\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Название предмета:</font> " : "Name:</font> ").append(HtmlUtils.htmlItemName(etcitem.getItemId())).append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "ID предмета:</font> " : "ID:</font> ").append(etcitem.getItemId()).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<br><table width=690>");
			result.append("<tr>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Тип предмета: " : "Item type: ").append(etcitem.getItemType().toString()).append("&nbsp;").append("</font><br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Вес: " : "Weight: ").append(etcitem.getWeight()).append("&nbsp;").append("</font><br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Цена продажи в магазин: " : "Sale price to the store: ").append(etcitem.getReferencePrice() / 2).append("&nbsp;").append("</font><br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Будет стыковаться: " : "It will be docked: ").append(player.getLanguage() == Language.RUSSIAN ? "нет" : etcitem.isStackable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("</font><br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Временный предмет: " : "A temporary items: ").append(player.getLanguage() == Language.RUSSIAN ? "нет" : etcitem.getDurability() > 0 ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("</font><br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно выбросить: " : "You can throw: ").append(player.getLanguage() == Language.RUSSIAN ? "нет" : etcitem.isDropable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("</font><br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно продать: " : "Can be sold: ").append(player.getLanguage() == Language.RUSSIAN ? "нет" : etcitem.isSellable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("</font><br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно обменять: " : "Can be exchanged: ").append(player.getLanguage() == Language.RUSSIAN ? "нет" : etcitem.isStoreable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("</font><br>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");
			
			if (etcitem.getAttachedSkills().length > 0)
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(player.getLanguage() == Language.RUSSIAN ? "Список скиллов" : "List skills");
				result.append("\" action=\"bypass _bbsitemskills ").append(etcitem.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
		}
		else
		{
			result.append(player.getLanguage() == Language.RUSSIAN ? "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Предмет не найден</font></center></td></tr></table><br>" : "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Item not found</font></center></td></tr></table><br>");
		}
		
		return result.toString();
	}
	
	private String generateWeaponInfo(Player player, String name)
	{
		StringBuilder result = new StringBuilder();
		
		for (ItemTemplate temp : ItemHolder.getInstance().getAllTemplates())
		{
			if ((temp != null) && (temp.isWeapon()) && ((temp.getName() == name) || (val2.equals("")) ? !temp.getName().startsWith(name) : (temp.getName().contains(name)) || (temp.getName().equals(name)) || (temp.getName().equalsIgnoreCase(name))))
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(temp.getName());
				result.append("\" action=\"bypass _bbsweaponinfoid ").append(temp.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
		}
		
		return result.toString();
	}
	
	private String generateWeaponInfo(Player player, int id)
	{
		StringBuilder result = new StringBuilder();
		
		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);
		if ((temp != null) && (temp.isWeapon()))
		{
			WeaponTemplate weapon = (WeaponTemplate) temp;
			String icon = weapon.getIcon();
			if ((icon == null) || (icon.equals("")))
			{
				icon = "icon.etc_question_mark_i00";
			}
			
			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"").append(icon).append("\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Название предмета:</font> " : "Name:</font> ").append(HtmlUtils.htmlItemName(weapon.getItemId())).append(" (<font color=\"b09979\">").append(weapon.getItemType().toString()).append("</font>)<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "ID предмета:</font> " : "ID:</font> ").append(weapon.getItemId()).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<br><table width=690>");
			result.append("<tr>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Грейд оружия: " : "Grade: ").append("</font>").append(weapon.getCrystalType()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Слот: " : "Slot: ").append("</font>").append(getBodyPart(player, weapon)).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Разбивается на кристаллы: " : "Crystallize: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : weapon.isCrystallizable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			if (weapon.isCrystallizable())
				result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Количество кристаллов: " : "Number of crystals: ").append("</font>").append(weapon.getCrystalCount()).append("&nbsp;").append("<br>");
			else
			{
				result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Количество кристаллов:</font> 0" : "Number of crystals:</font> 0").append("&nbsp;").append("<br>");
			}
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Потребление спиритов: " : "Consume spiritshot: ").append("</font>").append(weapon.getSpiritShotCount()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Оружие камаелей: " : "Kamael weapons: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : weapon.getKamaelConvert() > 0 ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Вес: " : "Weight: ").append("</font>").append(weapon.getWeight()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Цена продажи: " : "Sale price: ").append("</font>").append(Util.formatAdena(weapon.getReferencePrice() / 2)).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Будет стыковаться: " : "Stackable: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : weapon.isStackable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Временный предмет: " : "Temporary: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : weapon.getDurability() > 0 ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно выбросить: " : "Droppable: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : weapon.isDropable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Реюз Атаки: " : "Attack reuse: ").append("</font>").append(weapon.getAttackReuseDelay() / 1000).append(" sec.").append("&nbsp;").append("</font><br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно продать: " : "Can be sold: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : weapon.isSellable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно вставить аугментацию: " : "Augmentable: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : weapon.isAugmentable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно вставить атрибут: " : "Can be attributed: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : weapon.isAttributable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно обменять: " : "Tradable: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : weapon.isStoreable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Потребление сосок: " : "Consume soulshot: ").append("</font>").append(weapon.getSoulShotCount()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Потребление МП: " : "Consume Mp: ").append("</font>").append(weapon.getMpConsume()).append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");
			
			if (weapon.getAttachedSkills().length > 0)
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(player.getLanguage() == Language.RUSSIAN ? "Список скиллов" : "List skills");
				result.append("\" action=\"bypass _bbsitemskills ").append(weapon.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
			
			if (weapon.getAttachedFuncs().length > 0)
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(player.getLanguage() == Language.RUSSIAN ? "Список бонусов" : "List bonuses");
				result.append("\" action=\"bypass _bbsitemstats ").append(weapon.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
			
			if ((weapon.getBaseAttributeValue(Element.FIRE) > 0) || (weapon.getBaseAttributeValue(Element.WATER) > 0) || (weapon.getBaseAttributeValue(Element.WIND) > 0) || (weapon.getBaseAttributeValue(Element.EARTH) > 0) || (weapon.getBaseAttributeValue(Element.HOLY) > 0) || (weapon.getBaseAttributeValue(Element.UNHOLY) > 0))
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(player.getLanguage() == Language.RUSSIAN ? "Список Атрибутов" : "List attributes");
				result.append("\" action=\"bypass _bbsitematributes ").append(weapon.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
		}
		else
		{
			result.append(player.getLanguage() == Language.RUSSIAN ? "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Предмет не найден</font></center></td></tr></table><br>" : "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Item not found</font></center></td></tr></table><br>");
		}
		
		return result.toString();
	}
	
	private String generateArmorInfo(Player player, String name)
	{
		StringBuilder result = new StringBuilder();
		
		for (ItemTemplate temp : ItemHolder.getInstance().getAllTemplates())
		{
			if ((temp != null) && ((temp.isArmor()) || (temp.isAccessory())) && ((temp.getName() == name) || (val2.equals("")) ? !temp.getName().startsWith(name) : (temp.getName().contains(name)) || (temp.getName().startsWith(name)) || (temp.getName().equals(name)) || (temp.getName().equalsIgnoreCase(name))))
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(temp.getName());
				result.append("\" action=\"bypass _bbsarmorinfoid ").append(temp.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
		}
		
		return result.toString();
	}
	
	private String generateArmorInfo(Player player, int id)
	{
		StringBuilder result = new StringBuilder();
		
		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);
		if ((temp != null) && ((temp.isArmor()) || (temp.isAccessory())))
		{
			ItemTemplate armor;
			if (temp.getClass().equals(WeaponTemplate.class))
				armor = (WeaponTemplate) temp;
			else
				armor = (ArmorTemplate) temp;
			 
			String icon = armor.getIcon();
			if ((icon == null) || (icon.equals("")))
			{
				icon = "icon.etc_question_mark_i00";
			}
			
			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"").append(icon).append("\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Название предмета:</font> " : "Name:</font> ").append(HtmlUtils.htmlItemName(armor.getItemId())).append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "ID предмета:</font> " : "ID:</font> ").append(armor.getItemId()).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<br><table width=690>");
			result.append("<tr>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Тип доспехов: " : "Type: ").append("</font>").append(armor.getItemType().toString()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Грейд доспехов: " : "Grade: ").append("</font>").append(armor.getCrystalType()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Слот: " : "Slot: ").append("</font>").append(getBodyPart(player, armor)).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Разбивается на кристаллы: " : "Crystallize: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : armor.isCrystallizable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			if (armor.isCrystallizable())
				result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Количество кристаллов: " : "Number of crystals: ").append("</font>").append(armor.getCrystalCount()).append("&nbsp;").append("<br>");
			else
			{
				result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Количество кристаллов:</font> 0" : "Number of crystals:</font> 0").append("&nbsp;").append("<br>");
			}
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Вес: " : "Weight: ").append("</font>").append(armor.getWeight()).append("&nbsp;").append("</font><br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Цена продажи: " : "Sale price: ").append("</font>").append(Util.formatAdena(armor.getReferencePrice() / 2)).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Будет стыковаться: " : "Stackable: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : armor.isStackable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Временный предмет: " : "Temporary: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : armor.getDurability() > 0 ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно выбросить: " : "Droppable: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : armor.isDropable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно продать: " : "Can be sold: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : armor.isSellable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно вставить аугментацию: " : "Augmentable: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : armor.isAugmentable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно вставить атрибут: " : "Can be attributed: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : armor.isAttributable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно обменять: " : "Tradable: ").append("</font>").append(player.getLanguage() == Language.RUSSIAN ? "нет" : armor.isStoreable() ? "yes" : player.getLanguage() == Language.RUSSIAN ? "да" : "no").append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");
			
			if (armor.getAttachedSkills().length > 0)
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(player.getLanguage() == Language.RUSSIAN ? "Список скиллов" : "List skills");
				result.append("\" action=\"bypass _bbsitemskills ").append(armor.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
			
			if (armor.getAttachedFuncs().length > 0)
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(player.getLanguage() == Language.RUSSIAN ? "Список бонусов" : "List bonuses");
				result.append("\" action=\"bypass _bbsitemstats ").append(armor.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
			
			if ((armor.getBaseAttributeValue(Element.FIRE) > 0) || (armor.getBaseAttributeValue(Element.WATER) > 0) || (armor.getBaseAttributeValue(Element.WIND) > 0) || (armor.getBaseAttributeValue(Element.EARTH) > 0) || (armor.getBaseAttributeValue(Element.HOLY) > 0) || (armor.getBaseAttributeValue(Element.UNHOLY) > 0))
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(player.getLanguage() == Language.RUSSIAN ? "Список Атрибутов" : "List attributes");
				result.append("\" action=\"bypass _bbsitematributes ").append(armor.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}
		}
		else
		{
			result.append(player.getLanguage() == Language.RUSSIAN ? "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Предмет не найден</font></center></td></tr></table><br>" : "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Item not found</font></center></td></tr></table><br>");
		}
		
		return result.toString();
	}
	
	private String getFunc(Player player, FuncTemplate func)
	{
		if (func.getFunc(null) != null)
		{
			Func f = func.getFunc(null);
			if (getStats(player, f) != null)
			{
				if ((f instanceof FuncAdd))
				{
					String str = player.getLanguage() == Language.RUSSIAN ? "Увеличивает " : "Increases ";
					return new StringBuilder().append(str).append(getStats(player, f)).append(" to ").append(f.value).toString();
				}
				if ((f instanceof FuncSet))
				{
					String str = player.getLanguage() == Language.RUSSIAN ? "Увеличивает " : "Sets ";
					return new StringBuilder().append(str).append(getStats(player, f)).append(" to ").append(f.value).toString();
				}
				if ((f instanceof FuncSub))
				{
					String str = player.getLanguage() == Language.RUSSIAN ? "Увеличивает " : "Decreases ";
					return new StringBuilder().append(str).append(getStats(player, f)).append(" to ").append(f.value).toString();
				}
				if ((f instanceof FuncMul))
				{
					String str = player.getLanguage() == Language.RUSSIAN ? "Увеличивает " : "Multiplies ";
					return new StringBuilder().append(str).append(getStats(player, f)).append(" to ").append(f.value).toString();
				}
				if ((f instanceof FuncDiv))
				{
					String str = player.getLanguage() == Language.RUSSIAN ? "Увеличивает " : "Divides ";
					return new StringBuilder().append(str).append(getStats(player, f)).append(" to ").append(f.value).toString();
				}
				if ((f instanceof FuncEnchant))
				{
					String str = player.getLanguage() == Language.RUSSIAN ? "Увеличивает " : "Increases in the sharpening ";
					return new StringBuilder().append(str).append(getStats(player, f)).append(" to ").append(f.value).toString();
				}
			}
		}
		
		return "Не опознано";
	}
	
	private String getStats(Player player, Func f)
	{
		if (f.stat == Stats.MAX_HP)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "максимальное ХП" : "max HP";
			return str;
		}
		if (f.stat == Stats.MAX_MP)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "максимальное МП" : "max MP";
			return str;
		}
		if (f.stat == Stats.MAX_CP)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "максимальное СП" : " max CP";
			return str;
		}
		if (f.stat == Stats.REGENERATE_HP_RATE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "регенерация ХП" : "regeneration HP";
			return str;
		}
		if (f.stat == Stats.REGENERATE_CP_RATE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "регенерация СП" : "regeneration CP";
			return str;
		}
		if (f.stat == Stats.REGENERATE_MP_RATE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "регенерация МП" : "regeneration MP";
			return str;
		}
		if (f.stat == Stats.RUN_SPEED)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "скорость" : "speed";
			return str;
		}
		if (f.stat == Stats.POWER_DEFENCE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "физическую защиту" : "physical defence";
			return str;
		}
		if (f.stat == Stats.MAGIC_DEFENCE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "магическую защиту" : "magical defence";
			return str;
		}
		if (f.stat == Stats.POWER_ATTACK)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "физическую атаку" : "physical attack";
			return str;
		}
		if (f.stat == Stats.MAGIC_ATTACK)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "магическую атаку" : "magical attack";
			return str;
		}
		if ((f.stat == Stats.ATK_REUSE) || (f.stat == Stats.ATK_BASE))
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "реюз атаку" : "reuse attack";
			return str;
		}
		if (f.stat == Stats.EVASION_RATE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "точность" : "evasion";
			return str;
		}
		if (f.stat == Stats.ACCURACY_COMBAT)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "уклонение" : "accuarity";
			return str;
		}
		if (f.stat == Stats.CRITICAL_BASE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "шанс критического удара" : "crit";
			return str;
		}
		if (f.stat == Stats.SHIELD_DEFENCE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "защиту щитом" : "defense shield";
			return str;
		}
		if (f.stat == Stats.SHIELD_RATE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "шанс уклониться щитом" : "chance to avoid a shield";
			return str;
		}
		if (f.stat == Stats.POWER_ATTACK_RANGE)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "радиус физической атаки" : "reuse physical attack";
			return str;
		}
		if (f.stat == Stats.STAT_STR)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "СТР" : "STR";
			return str;
		}
		if (f.stat == Stats.STAT_CON)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "КОН" : "CON";
			return str;
		}
		if (f.stat == Stats.STAT_DEX)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "ДЕХ" : "DEX";
			return str;
		}
		if (f.stat == Stats.STAT_INT)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "ИНТ" : "INT";
			return str;
		}
		if (f.stat == Stats.STAT_WIT)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "ВИТ" : "WIT";
			return str;
		}
		if (f.stat == Stats.STAT_MEN)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "МЕН" : "MEN";
			return str;
		}
		if (f.stat == Stats.MP_PHYSICAL_SKILL_CONSUME)
		{
			String str = player.getLanguage() == Language.RUSSIAN ? "потребление мп физических скилов" : "mp consume physical skill";
			return str;
		}
		return player.getLanguage() == Language.RUSSIAN ? "Не опознано" : "Not recognized";
	}
	
	private String getBodyPart(Player player, ItemTemplate item)
	{
		if ((item.getBodyPart() == 2) || (item.getBodyPart() == 4))
			return player.getLanguage() == Language.RUSSIAN ? "Серьга" : "Earring";
		if (item.getBodyPart() == 8)
			return player.getLanguage() == Language.RUSSIAN ? "Ожерелье" : "Necklace";
		if ((item.getBodyPart() == 16) || (item.getBodyPart() == 32))
			return player.getLanguage() == Language.RUSSIAN ? "Кольцо" : "Ring";
		if (item.getBodyPart() == 64)
			return player.getLanguage() == Language.RUSSIAN ? "Шлем" : "Helmet";
		if (item.getBodyPart() == 256)
			return player.getLanguage() == Language.RUSSIAN ? "Щит" : "Shield";
		if ((item.getBodyPart() == 128) || (item.getBodyPart() == 16384))
			return player.getLanguage() == Language.RUSSIAN ? "Оружие" : "Weapon";
		if (item.getBodyPart() == 512)
			return player.getLanguage() == Language.RUSSIAN ? "Перчатки" : "Gloves";
		if (item.getBodyPart() == 1024)
			return player.getLanguage() == Language.RUSSIAN ? "Куртка" : "Armor";
		if (item.getBodyPart() == 2048)
			return player.getLanguage() == Language.RUSSIAN ? "Штаны" : "Pants";
		if (item.getBodyPart() == 4096)
			return player.getLanguage() == Language.RUSSIAN ? "Ботинки" : "Boots";
		if (item.getBodyPart() == 8192)
			return player.getLanguage() == Language.RUSSIAN ? "Плащ" : "Cloak";
		if (item.getBodyPart() == 32768)
			return player.getLanguage() == Language.RUSSIAN ? "Полная броня" : "Full armor";
		if (item.getBodyPart() == 65536)
			return player.getLanguage() == Language.RUSSIAN ? "Украшение" : "Decoration";
		if (item.getBodyPart() == 131072)
			return player.getLanguage() == Language.RUSSIAN ? "Костюм" : "Body";
		if (item.getBodyPart() == 131072)
			return player.getLanguage() == Language.RUSSIAN ? "Свадебное платье" : "Wedding Dress";
		if (item.isUnderwear())
			return player.getLanguage() == Language.RUSSIAN ? "Бельё" : "Underwear";
		if (item.isBracelet())
			return player.getLanguage() == Language.RUSSIAN ? "Браслет" : "Bracelet";
		if (item.isTalisman())
			return player.getLanguage() == Language.RUSSIAN ? "Талисман" : "Talisman";
		if (item.isBelt())
			return player.getLanguage() == Language.RUSSIAN ? "Пояс" : "Belt";
		
		return player.getLanguage() == Language.RUSSIAN ? "Не опознано" : "Not recognized";
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}

}