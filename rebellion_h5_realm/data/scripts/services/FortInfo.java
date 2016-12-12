package services;

import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Util;

import java.util.List;

public class FortInfo extends Functions
{
	public void list(String[] param)
	{
		Player player = getSelf();
		
		if (player == null)
			return;
		
		if (!Util.isNumber(param[0]))
			return;
		
		int page = Integer.parseInt(param[0]);
		NpcHtmlMessage html = new NpcHtmlMessage(0).setFile("scripts/services/FortInfo/list.htm");
		String list = "";
		if (page == 1)
		{
			list = list + getButton(101, player);
			list = list + getButton(102, player);
			list = list + getButton(103, player);
			list = list + getButton(104, player);
			list = list + getButton(105, player);
			list = list + getButton(106, player);
		}
		else if (page == 2)
		{
			list = list + getButton(107, player);
			list = list + getButton(108, player);
			list = list + getButton(109, player);
			list = list + getButton(110, player);
			list = list + getButton(111, player);
			list = list + getButton(112, player);
		}
		else if (page == 3)
		{
			list = list + getButton(113, player);
			list = list + getButton(114, player);
			list = list + getButton(115, player);
			list = list + getButton(116, player);
			list = list + getButton(117, player);
			list = list + getButton(118, player);
		}
		else if (page == 4)
		{
			list = list + getButton(119, player);
			list = list + getButton(120, player);
			list = list + getButton(121, player);
		}
		html = html.replace("%list%", list);
		
		player.sendPacket(html);
	}
	
	private String getButton(int id, Player player)
	{
		String name;
		Fortress res = ResidenceHolder.getInstance().getResidence(id);
		if (res != null)
			name = res.getName();
		else
			name = "";
		
		return "<button action=\"bypass -h scripts_services.FortInfo:fort " + id + "\" value=" + name + " width=200 height=26 back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\">";
	}
	
	public void fort(String[] param)
	{
		Player player = getSelf();
		
		if (player == null)
			return;
		
		if (!Util.isNumber(param[0]))
			return;
		
		int id = Integer.parseInt(param[0]);
		
		String name;
		Fortress res = ResidenceHolder.getInstance().getResidence(id);
		if (res != null)
			name = res.getName();
		else
			name = "";
		
		NpcHtmlMessage html = new NpcHtmlMessage(0).setFile("scripts/services/FortInfo/fort.htm");
		html.replace("%name%", name);
		html.replace("%info%", Fort(id));
		Fortress fortress = (Fortress) ResidenceHolder.getInstance().getResidence(Fortress.class, id);
		
		html.replace("%skill%", skills(fortress.getSkills()));
		player.sendPacket(html);
	}
	
	private String Fort(int id)
	{
		String Big = "Big.";
		String Small = "Small.";
		String Territorial = "Territorial.";
		String Border = "Border.";
		
		String type = "...";
		String size = "...";
		switch (id)
		{
			case 101:
				size = Small;
				type = Territorial;
				break;
			case 102:
				size = Big;
				type = Territorial;
				break;
			case 103:
				size = Small;
				type = Territorial;
				break;
			case 104:
				size = Big;
				type = Territorial;
				break;
			case 105:
				size = Small;
				type = Territorial;
				break;
			case 106:
				size = Small;
				type = Territorial;
				break;
			case 107:
				size = Big;
				type = Territorial;
				break;
			case 108:
				size = Small;
				type = Territorial;
				break;
			case 109:
				size = Big;
				type = Territorial;
				break;
			case 110:
				size = Big;
				type = Territorial;
				break;
			case 111:
				size = Small;
				type = Territorial;
				break;
			case 112:
				size = Big;
				type = Border;
				break;
			case 113:
				size = Big;
				type = Border;
				break;
			case 114:
				size = Small;
				type = Border;
				break;
			case 115:
				size = Small;
				type = Border;
				break;
			case 116:
				size = Big;
				type = Border;
				break;
			case 117:
				size = Big;
				type = Border;
				break;
			case 118:
				size = Big;
				type = Border;
				break;
			case 119:
				size = Small;
				type = Border;
				break;
			case 120:
				size = Small;
				type = Border;
				break;
			case 121:
				size = Small;
				type = Border;
				break;
		}
		StringBuilder html = new StringBuilder();
		
		Fortress fortress = ResidenceHolder.getInstance().getResidence(Fortress.class, id);
		html.append("<table border=0 width=290>");
		html.append("<tr>");
		html.append("<td width=54 align=center valign=top height=20>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=30 height=20>");
		html.append("<tr>");
		html.append("<td width=32 height=45 align=center valign=top>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"icon.weapon_fort_flag_i00\">");
		html.append("<tr>");
		html.append("<td width=32 align=center valign=top>");
		html.append("<img src=\"icon.castle_tab\" width=32 height=32>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td>");
		html.append("<td FIXWIDTH=230 align=left valign=top>");
		
		Clan owner = fortress.getOwnerId() == 0 ? null : ClanTable.getInstance().getClan(fortress.getOwnerId());
		String fort_owner;
		if (owner == null)
			fort_owner = "NPC";
		else
			fort_owner = owner.getName();
		
		html.append("Owner: <font color=\"FFFF00\">" + fort_owner + "</font>");
		html.append("<br1>Size: <font color=\"AAAAAA\">" + size + "</font>");
		html.append("<br1>Type: <font color=\"AAAAAA\">" + type + "</font>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		return html.toString();
	}
	
	private String skills(List<Skill> list)
	{
		String html = "";
		for (Skill sk : list)
			html = html + skillHtml(sk.getId());
		return html;
	}

	private String skillHtml(int id)
	{
		Skill skill = SkillTable.getInstance().getInfo(id, 1);
		return FortSkillsBlock(skill);
	}
	
	private String FortSkillsBlock(Skill skill)
	{
		StringBuilder html = new StringBuilder();
		
		html.append("<br>");
		html.append("<table border=0 width=290 height=30>");
		html.append("<tr>");
		html.append("<td width=54 align=center valign=top height=20>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=30 height=20>");
		html.append("<tr>");
		html.append("<td width=32 height=45 align=center valign=top>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"icon.skill" + skill.getIcon() + "\">");
		html.append("<tr>");
		html.append("<td width=32 align=center valign=top>");
		html.append("<img src=\"icon.panel_2\" width=32 height=32>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td>");
		html.append("<td FIXWIDTH=230 align=left valign=top>");
		html.append("<font color=\"CCFF33\">" + skill.getName() + "</font>");
		html.append("<br1>Level: <font color=\"AAAAAA\">" + skill.getLevel() + "</font>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		
		return html.toString();
	}
}
