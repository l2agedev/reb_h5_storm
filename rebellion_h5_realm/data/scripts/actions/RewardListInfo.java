package actions;

import l2r.gameserver.dao.ChampionTemplateTable.ChampionTemplate;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.reward.RewardGroup;
import l2r.gameserver.model.reward.RewardItem;
import l2r.gameserver.model.reward.RewardList;
import l2r.gameserver.model.reward.RewardType;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.utils.HtmlUtils;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public abstract class RewardListInfo
{
	private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);

	static
	{
		pf.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(2);
	}

	public static void showInfo(Player player, NpcInstance npc, int page)
	{
		ChampionTemplate ct = npc.getChampionTemplate();
		final int diff = npc.calculateLevelDiffForDrop(player.isInParty() ? player.getParty().getLevel() : player.getLevel());
		double mod = npc.calcStat(Stats.REWARD_MULTIPLIER, 1.0, player, null);
		mod *= Experience.penaltyModifier(diff, 9);

		NpcHtmlMessage htmlMessage = new NpcHtmlMessage(5);
		htmlMessage.replace("%npc_name%", HtmlUtils.htmlNpcName(npc.getNpcId()));

		if(mod <= 0)
		{
			htmlMessage.setFile("scripts/actions/rewardlist_to_weak.htm");
			player.sendPacket(htmlMessage);
			return;
		}

		if(npc.getTemplate().getRewards().isEmpty())
		{
			htmlMessage.setFile("scripts/actions/rewardlist_empty.htm");
			player.sendPacket(htmlMessage);
			return;
		}

		htmlMessage.setFile("scripts/actions/rewardlist_info.htm");

		StringBuilder sb = new StringBuilder();
		sb.append(generateFromListSimple(npc, player, mod * (ct == null ? 1 : ct.itemDropMultiplier), page));
		
		htmlMessage.replace("%info%", sb.toString());
		player.sendPacket(htmlMessage);
	}

	public static String generateFromList(NpcInstance npc, RewardList list, Player player, double mod)
	{
		StringBuilder sb = new StringBuilder();
		boolean icons = true;//player != null ? player.getVarB("DroplistIcons") : true;
		
		sb.append("<table width=270 border=0>");
		sb.append("<tr><td><table width=270 border=0><tr><td><font color=\"aaccff\">").append(fixGroupName(list.getType())).append("</font></td></tr></table></td></tr>");
		sb.append("<tr><td><img src=\"L2UI.SquareWhite\" width=270 height=1> </td></tr>");
		sb.append("<tr><td><img src=\"L2UI.SquareBlank\" width=270 height=10> </td></tr>");

		for(RewardGroup group : list)
		{
			sb.append("<tr><td><img src=\"L2UI.SquareBlank\" width=270 height=10> </td></tr>");
			sb.append("<tr><td>");
			sb.append("<table width=270 border=0 bgcolor=333333>");
			sb.append("<tr><td width=170><font color=\"a2a0a2\">Group Chance: </font><font color=\"b09979\">").append(pf.format(group.getChance() / RewardList.MAX_CHANCE)).append("</font></td>");
			sb.append("<td width=100 align=right>");
			sb.append("</td></tr>");
			sb.append("</table>").append("</td></tr>");

			sb.append("<tr><td><table>");
			for(RewardItem item : group.getItems())
			{
				String icon = item.getItem().getIcon();
				if(icon == null || icon.equals(StringUtils.EMPTY))
					icon = "icon.etc_question_mark_i00";
				
				if (icons)
				{
					sb.append("<tr><td width=32><img src=").append(icon).append(" width=32 height=32></td><td width=238>").append(resizeNames(HtmlUtils.htmlItemName(item.getItemId()))).append("<br1>");
					sb.append("<font color=\"b09979\">[").append(item.getMinDrop()).append("~").append(item.getMaxDrop()).append("]&nbsp;");
					sb.append(pf.format(item.getChance() / RewardList.MAX_CHANCE)).append("</font></td></tr>");
				}
				else
				{
					sb.append("<tr><td width=238>").append(resizeNames(HtmlUtils.htmlItemName(item.getItemId()))).append("<br1>");
					sb.append("<font color=\"b09979\">[").append(item.getMinDrop()).append("~").append(item.getMaxDrop()).append("]&nbsp;");
					sb.append(pf.format(item.getChance() / RewardList.MAX_CHANCE)).append("</font></td></tr>");
				}
			}
			sb.append("</table></td></tr>");
		}

		sb.append("</table>");
		
		return sb.toString();
	}
	
	public static String generateFromListSimple(NpcInstance npc, Player player, double mod, int page)
	{
		StringBuilder sb = new StringBuilder();
		boolean icons = true;//player != null ? player.getVarB("DroplistIcons") : true;
		
		int all = 0;
		int pageloop = 0;
		boolean pagereached = false;
		int totalpages = 0;
		
		for(RewardList rewardList : npc.getTemplate().getRewards().values())
		{
			for(RewardGroup group : rewardList)
			{
				for (RewardItem item : group.getItems())
				{
					double gchance = group.getChance();
					
					all++;
					if(page == 1 && pageloop > 15)
						continue;
					if(!pagereached && all > page * 15)
						continue;
					if(!pagereached && all <= (page - 1) * 15)
						continue;
					pageloop++;
					
					sb.append("<table width=270 border=0>");
					sb.append("<tr><td><table width=270 border=0><tr><td><font color=\"aaccff\">").append(fixGroupName(rewardList.getType())).append("</font></td></tr></table></td></tr>");
					sb.append("<tr><td><img src=\"L2UI.SquareWhite\" width=270 height=1> </td></tr>");
					sb.append("<tr><td><img src=\"L2UI.SquareBlank\" width=270 height=10> </td></tr>");
					sb.append("</table>");
					
					double simpleChanceMod = Math.min(group.getChance(), RewardList.MAX_CHANCE) / RewardList.MAX_CHANCE;
					sb.append("<tr><td><img src=\"L2UI.SquareBlank\" width=270 height=10> </td></tr>");
					sb.append("<tr><td>");
					sb.append("<table width=270 border=0 bgcolor=333333>");
					sb.append("<tr><td width=170><font color=\"a2a0a2\">Group Chance: </font><font color=\"b09979\">").append(pf.format(gchance / RewardList.MAX_CHANCE)).append("</font></td>");
					sb.append("<td width=100 align=right>");
					sb.append("</td></tr>");
					sb.append("</table>").append("</td></tr>");
					
					sb.append("<tr><td><table>");
					
					String icon = item.getItem().getIcon();
					if (icon == null || icon.equals(StringUtils.EMPTY))
						icon = "icon.etc_question_mark_i00";
					
					if (icons)
					{
						sb.append("<tr><td width=32><img src=").append(icon).append(" width=32 height=32></td><td width=238>").append(resizeNames(HtmlUtils.htmlItemName(item.getItemId()))).append("<br1>");
						sb.append("<font color=\"b09979\">[").append(item.getMinDrop()).append("~").append(item.getMaxDrop()).append("]&nbsp;");
						sb.append(pf.format(item.getChance() * simpleChanceMod / RewardList.MAX_CHANCE)).append("</font></td></tr>");
					}
					else
					{
						sb.append("<tr><td width=238>").append(resizeNames(HtmlUtils.htmlItemName(item.getItemId()))).append("<br1>");
						sb.append("<font color=\"b09979\">[").append(item.getMinDrop()).append("~").append(item.getMaxDrop()).append("]&nbsp;");
						sb.append(pf.format(item.getChance() * simpleChanceMod / RewardList.MAX_CHANCE)).append("</font></td></tr>");
					}
					
					sb.append("</table></td></tr>");
				}
			}
		}

		sb.append("<table><tr>");
		totalpages = all / 15 + 1;
		if(page == 1)
		{
			if(totalpages == 1)
				sb.append("<td>&nbsp;</td>");
			else
				sb.append("<td><button value=\"[NEXT]\" action=\"bypass -h scripts_actions.OnActionShift:droplist " + (page + 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				sb.append("<td><button value=\"[PREV]\" action=\"bypass -h scripts_actions.OnActionShift:droplist " + (page - 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			}
			else
			{
				
				sb.append("<td><button value=\"[PREV]\" action=\"bypass -h scripts_actions.OnActionShift:droplist " + (page - 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				sb.append("<td><button value=\"[NEXT]\" action=\"bypass -h scripts_actions.OnActionShift:droplist " + (page + 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			}
		}
		sb.append("</tr></table>");
		return sb.toString();
	}
	
	public static String fixGroupName(RewardType type)
	{
		String name = "";
		
		switch(type)
		{
			case RATED_GROUPED:
				name = "Rated Group";
				break;
			case NOT_RATED_GROUPED:
				name = "Not Rated Group";
				break;
			case SWEEP:
				name = "Spoil";
				break;
			case NOT_RATED_NOT_GROUPED:
				name = "Not Rated Not Grouped";
				break;
		}
		
		return name;
	}
	public static String resizeNames(String s)
	{
		return s.replaceFirst("Recipe:", "R:").replaceFirst("Common Item - ", "Common ").replaceFirst("Scroll: Enchant", "Enchant").replaceFirst("Compressed Package", "CP");
	}
}