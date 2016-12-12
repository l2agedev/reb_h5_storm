package services.community;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.npc.NpcTemplate;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunityRaidBoss implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityRaidBoss.class);
	

	@Override
	public void onLoad()
	{
		if (Config.ALLOW_BSS_RAIDBOSS)
		{
			_log.info("CommunityBoard: RaidBoss Status loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if (Config.ALLOW_BSS_RAIDBOSS)
			CommunityBoardManager.getInstance().removeHandler(this);
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbsraidboss",
		};
	}
	
	private static final int BOSSES_PER_PAGE = 15;
	private static final int[] BOSSES_TO_NOT_SHOW = Config.BOSSES_TO_NOT_SHOW;
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/RaidStatus.htm", player);
		
		if (!checkCondition(player))
			return;
		
		if ("bbsraidboss".equals(cmd))
		{
			int sort = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "3");
			int page = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");
			String search = st.hasMoreTokens() ? st.nextToken().trim() : "";
			
			html = getBosses(player, html, sort, page, search);
		}
		
		ShowBoard.separateAndSend(html, player);
		
	}
	
	/**
	 * @return newHtml
	 */
	private String getBosses(Player visitor, String html, int sort, int page, String search)
	{
		StringBuilder builder = new StringBuilder();
		Map<Integer, StatsSet> allBosses = getSearchedBosses(sort, search);
		int i = 0;
		
		for (Entry<Integer, StatsSet> entry : allBosses.entrySet())
		{
			if (i < page * BOSSES_PER_PAGE)
			{
				i++;
				continue;
			}
			
			StatsSet boss = entry.getValue();
			NpcTemplate temp = NpcHolder.getInstance().getTemplate(entry.getKey());
			if (boss == null || temp == null)
				continue;
			
			boolean isAlive = boss.getInteger("respawn_delay", 0) < System.currentTimeMillis() / 1000;
			String killer = boss.getString("last_killer", "<br>");
			if (killer.equals("0"))
				killer = "<br>";
			
			builder.append("<table bgcolor=").append(getLineColor(i)).append(" width=760 height=26 border=0 cellpadding=0 cellspacing=0><tr>");
			builder.append("<td width=285 height=20><center><font color=").append(getTextColor(isAlive)).append(">").append(temp.getName()).append("</font></center></td>");
			builder.append("<td width=70 height=20><center><font color=").append(getTextColor(isAlive)).append(">").append(temp.getLevel()).append("</font></center></td>");
			builder.append("<td width=85 height=20><center><font color=").append(getTextColor(isAlive)).append(">").append(isAlive ? "<font color=61F300>Alive</font>" : "<font color=F30000>Dead</font>").append("</font></center></td>");
			builder.append("<td width=170 height=20><center><font color=").append(getTextColor(isAlive)).append(">").append(getConvertedDateOfDeath(boss.getLong("date_of_death", 0))).append("</font></center></td>");
			builder.append("<td width=150 height=20><center><font color=").append(getTextColor(isAlive)).append(">").append(killer).append("</font></center></td>");
			builder.append("</tr></table>");
			
			i++;
			if (i >= (page * BOSSES_PER_PAGE + BOSSES_PER_PAGE - 1))
			{
				break;
			}
		}
		
		builder.append("<img src=\"L2UI_CT1.Gauge_DF_CP_Center\" width=760 height=2>");
		// Prev
		builder.append("<center><table width=760><tr>");
		if (page > 0)
			builder.append("<td width=380 align=right><button value=\"Prev\" action=\"bypass _bbsraidboss_%sort%_").append(page - 1).append("_%search%\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		else
			builder.append("<td width=380 align=right><br></td>");
		// Next
		if (allBosses.size() > i)
			builder.append("<td width=380 align=left><button value=\"Next\" action=\"bypass _bbsraidboss_%sort%_").append(page + 1).append("_%search%\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		else
			builder.append("<td width=380 align=right><br></td>");
		
		builder.append("</tr></table></center>");
		
		
		html = html.replace("%bosses%", builder.toString());
		html = html.replace("%page%", String.valueOf(page));
		html = html.replace("%sort%", String.valueOf(sort));
		html = html.replace("%search%", search);
		for (i = 1; i <= 6; i++)
			if (Math.abs(sort) == i)
				html = html.replace("%sort" + i + "%", String.valueOf(-sort));
			else
				html = html.replace("%sort" + i + "%", String.valueOf(i));
		
		return html;
	}
	
	private Map<Integer, StatsSet> getSearchedBosses(int sort, String search)
	{
		Map<Integer, StatsSet> result = new HashMap<Integer, StatsSet>();
		
		if (!search.isEmpty())
		{
			for (Entry<Integer, StatsSet> entry : RaidBossSpawnManager.getInstance().getAllBosses().entrySet())
			{
				NpcTemplate temp = NpcHolder.getInstance().getTemplate(entry.getKey());
				if (StringUtils.containsIgnoreCase(temp.getName(), search))
					result.put(entry.getKey(), entry.getValue());
			}
		}
		else
		{
			result = RaidBossSpawnManager.getInstance().getAllBosses();
		}
		
		for (int id : BOSSES_TO_NOT_SHOW)
			result.remove(id);
		
		result = sort(result, sort);
		
		return result;
	}
	
	private String getConvertedDateOfDeath(long time)
	{
		if (time == 0)
			return "";
		
		Date anotherDate = new Date(time);
		String DATE_FORMAT = "HH:** dd/MM/yy";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
		
		return sdf.format(anotherDate);
	}
	
	private String getLineColor(int i)
	{
		if (i % 2 == 0)
			return "890202";
		else
			return "211618";
	}
	
	private String getTextColor(boolean alive)
	{
		if (alive)
			return "D5D5D5";
		else
			return "746767";
	}
	
	private Map<Integer, StatsSet> sort(Map<Integer, StatsSet> result, int sort)
	{
		ValueComparator bvc = new ValueComparator(result, sort);
		Map<Integer, StatsSet> sorted_map = new TreeMap<Integer, StatsSet>(bvc);
		sorted_map.putAll(result);
		return sorted_map;
	}
	
	class ValueComparator implements Comparator<Integer>
	{
		
		private Map<Integer, StatsSet> base;
		private int _sort;
		
		public ValueComparator(Map<Integer, StatsSet> base, int sort)
		{
			this.base = base;
			_sort = sort;
		}
		
		public int compare(Integer a, Integer b)
		{
			int sortResult = sortById(a, b, _sort);
			if (sortResult == 0 && Math.abs(_sort) != 3)
				sortResult = sortById(a, b, 3);
			if (sortResult == 0 && Math.abs(_sort) != 1)
				sortResult = sortById(a, b, 1);
			if (sortResult == 0 && Math.abs(_sort) != 2)
				sortResult = sortById(a, b, 2);
			return sortResult;
		}
		
		private int sortById(Integer a, Integer b, int sortId)
		{
			NpcTemplate temp1 = NpcHolder.getInstance().getTemplate(a);
			NpcTemplate temp2 = NpcHolder.getInstance().getTemplate(b);
			StatsSet set1 = base.get(a);
			StatsSet set2 = base.get(b);
			switch (sortId)
			{
				case 1:// Name ASC
					return temp1.getName().compareTo(temp2.getName());
				case -1:// Name DESC
					return temp2.getName().compareTo(temp1.getName());
				case 2:// Level ASC
					return Integer.compare(temp1.level, temp2.level);
				case -2:// Level DESC
					return Integer.compare(temp2.level, temp1.level);
				case 4:// Status ASC
					return Integer.compare(set1.getInteger("respawn_delay", 0), set2.getInteger("respawn_delay", 0));
				case -4:// Status DESC
					return Integer.compare(set2.getInteger("respawn_delay", 0), set1.getInteger("respawn_delay", 0));
				case 5:// Date of Death ASC
					return Integer.compare(set1.getInteger("date_of_death", 0), set2.getInteger("date_of_death", 0));
				case -5:// Date of Death DESC
					return Integer.compare(set2.getInteger("date_of_death", 0), set1.getInteger("date_of_death", 0));
				case 6:// Last Killer ASC
					return set1.getString("last_killer", "").compareTo(set2.getString("last_killer", ""));
				case -6:// Last Killer DESC
					return set2.getString("last_killer", "").compareTo(set1.getString("last_killer", ""));
			}
			return 0;
		}
	}
	
	private static boolean checkCondition(Player player)
	{
		if (!Config.ALLOW_BSS_RAIDBOSS || player == null)
			return false;
		
		return true;
	}
	
	public static final CommunityRaidBoss getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityRaidBoss _instance = new CommunityRaidBoss();
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
}
