package l2r.gameserver.achievements;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.TutorialCloseHtml;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.utils.Log;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastTable;
import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Achievements
{
	// id-max
	public FastMap<Integer, Integer> _achievementsLevels = new FastMap<>();
	public List<IAchievementCategory> _achievementsCat = new FastTable<>();
	private static Achievements _instance;
	
	private static final Logger _log = LoggerFactory.getLogger(Achievements.class);
	
	public Achievements()
	{
		load();
	}
	
	public void usebypass(Player player, String bypass, String[] cm)
	{
		if (bypass.startsWith("_bbs_achievements_cat"))
			generatePage(player, Integer.parseInt(cm[1]), Integer.parseInt(cm[2]));
		else if (bypass.equals("_bbs_achievements_close"))
			player.sendPacket(TutorialCloseHtml.STATIC);
		else
			generatePagr(player);
	}
	
	public void generatePagr(Player player)
	{
		if (player == null)
			return;
		
		String achievements = HtmCache.getInstance().getNotNull("achievements/Achievements.htm", player);
		
		String ac = "";
		
		for (IAchievementCategory a : _achievementsCat)
			ac += a.getHtml(player.getAchievementsLevel(a.getId()));
		
		achievements = achievements.replace("%categories%", ac);
		
		//player.sendPacket(html);
		player.sendPacket(new TutorialShowHtml(achievements));
	}
	
	public void generatePage(Player player, int category, int page)
	{
		if (player == null)
			return;
		
		String FULL_PAGE = HtmCache.getInstance().getNotNull("achievements/inAchievements.htm", player);
		
		boolean done;
		String achievementsNotDone = "";
		String achievementsDone = "";
		String html;
		
		int playerPoints = 0;
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = (int) (Math.round(player.getAchievements(category).entrySet().size()) / 5.0 + 1);
		
		if(page == 1)
		{
			if(totalpages == 1)
				FULL_PAGE = FULL_PAGE.replaceAll("%more%", "&nbsp;");
			else
				FULL_PAGE = FULL_PAGE.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbs_achievements_cat " + category + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			FULL_PAGE = FULL_PAGE.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				FULL_PAGE = FULL_PAGE.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbs_achievements_cat " + category + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				FULL_PAGE = FULL_PAGE.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				FULL_PAGE = FULL_PAGE.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbs_achievements_cat " + category + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				FULL_PAGE = FULL_PAGE.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbs_achievements_cat " + category + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		IAchievementCategory cat = getCatById(category);

		if (cat == null)
		{
			_log.warn("Achievements: getCatById - cat - is null, return. for " + player.getName());
			return;
		}
		
		for (Entry<Integer, Integer> arc : player.getAchievements(category).entrySet())
		{
			int aId = arc.getKey();
			int aLevel = arc.getValue();
			iAchievement a = GetAchievement(aId, aLevel + 1);
			
			if (a == null)
			{
				_log.warn("Achievements: GetAchievement - a - is null, return. for " + player.getName());
				return;
			}
			
			playerPoints = getRealPoints(player.getCounters().getPoints(a.getType()), aId, aLevel);
			
			all++;
			if(page == 1 && clansvisual > 5)
				continue;
			if(!pagereached && all > page * 5)
				continue;
			if(!pagereached && all <= (page - 1) * 5)
				continue;
			
			clansvisual++;
			
			if (getMaxLevel(aId) != aLevel)
			{
				done = false;
				
				String notDoneAchievement = HtmCache.getInstance().getNullable("achievements/oneAchievement.htm", null);

				int needpoints = a.getNeedPoints();
				int greenbar = 24 * (playerPoints * 100 / needpoints) / 100;
				if(greenbar < 0)
					greenbar = 0;
				
				if(greenbar >= 24)
					greenbar = 24;
				
				notDoneAchievement = notDoneAchievement.replaceFirst("%fame%", "" + a.getFame());
				notDoneAchievement = notDoneAchievement.replaceAll("%bar1%", "" + greenbar);
				notDoneAchievement = notDoneAchievement.replaceAll("%bar2%", "" + (24 - greenbar));

				notDoneAchievement = notDoneAchievement.replaceFirst("%cap1%", greenbar > 0 ? "Gauge_DF_Food_Left" : "Gauge_DF_Exp_bg_Left");
				notDoneAchievement = notDoneAchievement.replaceFirst("%cap2%", "Gauge_DF_Exp_bg_Right");

				notDoneAchievement = notDoneAchievement.replaceFirst("%desc%", a.getDesc().replaceAll("%need%", "" + (needpoints - playerPoints)));

				notDoneAchievement = notDoneAchievement.replaceFirst("%bg%", a.getId() % 2 == 0 ? "090908" : "0f100f");
				notDoneAchievement = notDoneAchievement.replaceFirst("%icon%", a.getIcon());
				notDoneAchievement = notDoneAchievement.replaceFirst("%name%", a.getName() + " " + a.getLevel() + "lvl");
				
				html = notDoneAchievement;
			}
			else
			{
				done = true;
				
				String doneAchievement = HtmCache.getInstance().getNullable("achievements/oneAchievement.htm", null);

				doneAchievement = doneAchievement.replaceFirst("%fame%", "" + a.getFame());
				doneAchievement = doneAchievement.replaceAll("%bar1%", "24");
				doneAchievement = doneAchievement.replaceAll("%bar2%", "0");

				doneAchievement = doneAchievement.replaceFirst("%cap1%", "Gauge_DF_Food_Left");
				doneAchievement = doneAchievement.replaceFirst("%cap2%", "Gauge_DF_Food_Right");

				doneAchievement = doneAchievement.replaceFirst("%desc%", "Done.");

				doneAchievement = doneAchievement.replaceFirst("%bg%", a.getId() % 2 == 0 ? "090908" : "0f100f");
				doneAchievement = doneAchievement.replaceFirst("%icon%", a.getIcon());
				doneAchievement = doneAchievement.replaceFirst("%name%", a.getName() + " " + a.getLevel() + "lvl");
				
				html = doneAchievement;
			}
			
			if(clansvisual < 5)
			{
				for(int d = clansvisual + 1; d != 5; d++)
				{
					html = html.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
					html = html.replaceAll("%bar1" + d + "%", "0");
					html = html.replaceAll("%bar2" + d + "%", "0");
					html = html.replaceAll("%cap1" + d + "%", "&nbsp;");
					html = html.replaceAll("%cap2" + d + "%", "&nbsp");
					html = html.replaceAll("%desc" + d + "%", "&nbsp");
					html = html.replaceAll("%bg" + d + "%", "0f100f");
					html = html.replaceAll("%name" + d + "%", "&nbsp");
				}
			}
			
			if (!done)
				achievementsNotDone += html;
			else
				achievementsDone += html;
		}
		
		
		int barup = cat.calculteBar(248, player.getAchievementsLevel(category));
		String fp = FULL_PAGE;
		fp = fp.replaceAll("%bar1up%", "" + barup);
		fp = fp.replaceAll("%bar2up%", "" + (248 - barup));
		
		fp = fp.replaceFirst("%caps1%", barup > 0 ? "Gauge_DF_Large_Food_Left" : "Gauge_DF_Large_Exp_bg_Left");
		
		fp = fp.replaceFirst("%caps2%", barup >= 248 ? "Gauge_DF_Large_Food_Right" : "Gauge_DF_Large_Exp_bg_Right");
		
		fp = fp.replaceFirst("%achievementsNotDone%", achievementsNotDone);
		fp = fp.replaceFirst("%achievementsDone%", achievementsDone);
		fp = fp.replaceFirst("%catname%", cat.getName());
		fp = fp.replaceFirst("%catDesc%", cat.getDesc());
		fp = fp.replaceFirst("%catIcon%", cat.getIcon());
		
		player.sendPacket(new TutorialShowHtml(fp));
	}
	
	@SuppressWarnings("unused")
	private void fix(Player pl, int playerPoints, int aid)
	{
		iAchievement arc = Achievements.getInstance().GetAchievement(aid, pl.getAchievementLevelbyId(aid) + 1);
		if(arc != null && playerPoints >= arc.getNeedPoints())
		{
			Achievements.getInstance().reward(pl, arc);
			fix(pl, playerPoints, aid);
		}
	}
	
	public void checkArchievements(Player player)
	{
		if (player.isRewardingAchievement())
			return;
		
		for (Entry<Integer, Integer> arco : player.getAchievements().entrySet())
		{
			if (getMaxLevel(arco.getKey()) == arco.getValue())
				continue;
			
			iAchievement arc = GetAchievement(arco.getKey(), arco.getValue() + 1);
			if (arc.maybeDone(getRealPoints(player.getCounters().getPoints(arc.getType()), arco.getKey(), arco.getValue())))
				reward(player, arc);
		}
	}
	
	public void reward(Player player, iAchievement arc)
	{
		player.setIsRewardingAchievement(true);
		player.sendChatMessage(player.getObjectId(), ChatType.BATTLEFIELD.ordinal(), "Achievements", "You've completed '" + arc.getName() + "' achievement.");
		player.getAchievements().put(arc.getId(), arc.getLevel());
		
		if (!Config.DISABLE_ACHIEVEMENTS_FAME_REWARD)
		{
			player.setFame(player.getFame() + arc.getFame());
			Log.achievements("Achievements: Player " + player.getName() + " recived " + arc.getFame() + " fame from achievement " + arc.getName());
		}
		
		player.broadcastPacket(new MagicSkillUse(player, player, 2527, 1, 0, 500));
		player.getCounters().addPoint("_Achievements_Done");
		player.setIsRewardingAchievement(false);
	}
	
	public int getRealPoints(int allpoints, int id, int level)
	{
		if (allpoints == 0)
			return 0;
		
		int result = 0;
		for (int i = level; i > 0; i--)
		{
			iAchievement a = GetAchievement(id, i);
			if (a != null)
				result += a.getNeedPoints();
		}
		
		return allpoints - result;
	}
	
	public IAchievementCategory getCatById(int id)
	{
		for (IAchievementCategory ach : _achievementsCat)
		{
			if (ach.getId() == id)
				return ach;
		}
		return null;
	}
	
	public iAchievement GetAchievement(int id, int level)
	{
		for (IAchievementCategory cat : _achievementsCat)
		{
			for (iAchievement ipo : cat.getAchievments())
			{
				if (ipo.getId() == id && ipo.getLevel() == level)
					return ipo;
			}
		}
		return GetAchievement(id, getMaxLevel(id));
	}
	
	public iAchievement GetAchievement(int id)
	{
		for (IAchievementCategory cat : _achievementsCat)
		{
			for (iAchievement ipo : cat.getAchievments())
			{
				if (ipo.getId() == id)
					return ipo;
			}
		}
		return GetAchievement(id, getMaxLevel(id));
	}
	
	public iAchievement GetAchievementWithNull(int id, int level)
	{
		for (IAchievementCategory cat : _achievementsCat)
		{
			for (iAchievement ipo : cat.getAchievments())
			{
				if (ipo.getId() == id && ipo.getLevel() == level)
					return ipo;
			}
		}
		return null;
	}
	
	public int getMaxLevel(int id)
	{
		return _achievementsLevels.get(id);
	}
	
	public void load()
	{
		_achievementsLevels.clear();
		_achievementsCat.clear();
		try
		{
			List<iAchievement> _tempachievements = new FastTable<>();
			
			IAchievementCategory cat = new IAchievementCategory();
			int lastlevel = 0;
			int lastid = 0;
			int lastcat = 0;
			
			File file = new File(Config.DATAPACK_ROOT, "config/achievements.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			Document doc = factory.newDocumentBuilder().parse(file);
			
			for (Node g = doc.getFirstChild(); g != null; g = g.getNextSibling())
			{
				for (Node z = g.getFirstChild(); z != null; z = z.getNextSibling())
				{
					if (z.getNodeName().equals("categories"))
					{
						for (Node i = z.getFirstChild(); i != null; i = i.getNextSibling())
						{
							for (Node o = i.getFirstChild(); o != null; o = o.getNextSibling())
							{
								if ("kitekat".equalsIgnoreCase(i.getNodeName()))
								{
									if ("cat".equalsIgnoreCase(o.getNodeName()))
									{
										int id = Integer.valueOf(o.getAttributes().getNamedItem("id").getNodeValue());
										String name = String.valueOf(o.getAttributes().getNamedItem("name").getNodeValue());
										String icon = String.valueOf(o.getAttributes().getNamedItem("icon").getNodeValue());
										String desc = String.valueOf(o.getAttributes().getNamedItem("desc").getNodeValue());
										
										cat.setId(id);
										cat.setName(name);
										cat.setIcon(icon);
										cat.setDesc(desc);
										_achievementsCat.add(cat);
									}
								}
								cat = new IAchievementCategory();
							}
						}
					}
					else if (z.getNodeName().equals("achievement"))
					{
						for (Node i = z.getFirstChild(); i != null; i = i.getNextSibling())
						{
							int id = Integer.valueOf(z.getAttributes().getNamedItem("id").getNodeValue());
							int category = Integer.valueOf(z.getAttributes().getNamedItem("cat").getNodeValue());
							String desc = String.valueOf(z.getAttributes().getNamedItem("desc").getNodeValue());
							String type = String.valueOf(z.getAttributes().getNamedItem("type").getNodeValue());
							
							for (Node o = i.getFirstChild(); o != null; o = o.getNextSibling())
							{
								if ("levels".equalsIgnoreCase(i.getNodeName()))
								{
									if ("lvl".equalsIgnoreCase(o.getNodeName()))
									{
										int level = Integer.valueOf(o.getAttributes().getNamedItem("id").getNodeValue());
										int points = Integer.valueOf(o.getAttributes().getNamedItem("need").getNodeValue());
										int fame = Integer.valueOf(o.getAttributes().getNamedItem("fame").getNodeValue());
										String name = String.valueOf(o.getAttributes().getNamedItem("name").getNodeValue());
										String icon = String.valueOf(o.getAttributes().getNamedItem("icon").getNodeValue());
										
										iAchievement achievement = new iAchievement();
										
										achievement.setId(id);
										achievement.setCategory(category);
										achievement.setType(type);
										achievement.setDesc(desc);
										achievement.setLevel(level);
										achievement.setNeededPoints(points);
										achievement.setFame(fame);
										achievement.setName(name);
										achievement.setIcon(icon);
										
										_tempachievements.add(achievement);
										
										lastlevel = achievement.getLevel();
										lastid = achievement.getId();
										lastcat = achievement.getCat();
										
									}
								}
								if ("rewards".equalsIgnoreCase(i.getNodeName()))
								{
									if ("item".equalsIgnoreCase(o.getNodeName()))
									{
										int Itemid = Integer.valueOf(o.getAttributes().getNamedItem("id").getNodeValue());
										int Itemcount = Integer.valueOf(o.getAttributes().getNamedItem("count").getNodeValue());
										for (iAchievement hui : _tempachievements)
											hui.addReward(Itemid, Itemcount);
									}
								}
							}
						}
						
						getCatById(lastcat).getAchievments().addAll(_tempachievements);
						_tempachievements.clear();
						_achievementsLevels.put(lastid, lastlevel);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static Achievements getInstance()
	{
		if (_instance == null)
			_instance = new Achievements();
		return _instance;
	}
}
