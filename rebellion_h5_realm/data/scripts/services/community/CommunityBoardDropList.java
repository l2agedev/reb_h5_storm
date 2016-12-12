package services.community;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.instances.BossInstance;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.PetInstance;
import l2r.gameserver.model.instances.RaidBossInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestEventType;
import l2r.gameserver.model.reward.RewardGroup;
import l2r.gameserver.model.reward.RewardItem;
import l2r.gameserver.model.reward.RewardList;
import l2r.gameserver.model.reward.RewardType;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.mapregion.RestartArea;
import l2r.gameserver.templates.mapregion.RestartPoint;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Infern0
 *
 */
public class CommunityBoardDropList implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityBoardDropList.class);
	
	private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
	

	static
	{
		pf.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(2);
	}
	
	private static Map<NpcTemplate, RewardItem> _npcs = new HashMap<NpcTemplate, RewardItem>();
	
	public static final CommunityBoardDropList getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityBoardDropList _instance = new CommunityBoardDropList();
	}

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.COMMUNITY_DROP_LIST)
		{
			_log.info("CommunityBoard: DropList service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.COMMUNITY_DROP_LIST)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbsdroplist", "_bbsdroploc", "_bbsdropquests", "_bbsdropNpcInfo", "_bbsdropnpcskills", "_bbsrewardspage", "_bbsdropnpcnamepage", "_bbsnpcnameid", "_bbsnpcnameidpage", "_bbsdroppage", "_bbsdropnameid", "_bbsdropnpcid", "_bbsdropnpcname", "_bbsrewards", "_bbsrewardradar", "_bbsdropteleport"  };
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, " ");
		String cmd = st.nextToken();

		if (cmd.equalsIgnoreCase("_bbsdroplist"))
		{
			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/search.htm", player);

			ShowBoard.separateAndSend(content, player);
		}
		else if (cmd.equalsIgnoreCase("_bbsdropnpcskills"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 4)
				generateNpcSkills(player, Integer.parseInt(cm[1]), Integer.parseInt(cm[2]));
		}
		else if (cmd.equalsIgnoreCase("_bbsdroploc"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 3)
				setMapLocation(player, Integer.parseInt(cm[1]));
		}
		else if (cmd.equalsIgnoreCase("_bbsdropquests"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 3)
				getNpcQuests(player, Integer.parseInt(cm[1]));
		}
		else if (cmd.equalsIgnoreCase("_bbsdropNpcInfo"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 3)
				generatenpcInfo(player, Integer.parseInt(cm[1]));
		}
		else if (cmd.equalsIgnoreCase("_bbsdropnameidpage"))
		{
			StringTokenizer list = new StringTokenizer(bypass, ":");
			list.nextToken();
			int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
			String name = list.hasMoreTokens() ? list.nextToken() : "";
			
			getItemsbyNameId(player, page, name.trim());
		}
		else if (cmd.equalsIgnoreCase("_bbsnpcnameidpage"))
		{
			StringTokenizer list = new StringTokenizer(bypass, ":");
			list.nextToken();
			int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
			String name = list.hasMoreTokens() ? list.nextToken() : "";
			
			getNPCbyNameId(player, page, name.trim());
		}
		else if (cmd.equalsIgnoreCase("_bbsdropnameid"))
		{
			StringTokenizer list = new StringTokenizer(bypass, ":");
			list.nextToken();
			String name = list.hasMoreTokens() ? list.nextToken() : "";
			
			if (Util.isDigit(name))
				getItemsbyNameId(player, 1, name.trim());
			else
				getItemsbyNameId(player, 1, name.trim());
		}
		else if (cmd.equalsIgnoreCase("_bbsnpcnameid"))
		{
			StringTokenizer list = new StringTokenizer(bypass, ":");
			list.nextToken();
			String name = list.hasMoreTokens() ? list.nextToken() : "";
			
			if (Util.isDigit(name))
				getNPCbyNameId(player, 1, name.trim());
			else
				getNPCbyNameId(player, 1, name.trim());
		}
		else if (cmd.equalsIgnoreCase("_bbsrewards"))
		{
			StringTokenizer list = new StringTokenizer(bypass, ":");
			list.nextToken();
			int npcid = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : null;
			String type = list.hasMoreTokens() ? list.nextToken() : "";
			int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
			
			generateDropListAllTypes(npcid, type, player, page);
		}
		else if (cmd.equalsIgnoreCase("_bbsdropteleport"))
		{
			StringTokenizer list = new StringTokenizer(bypass, ":");
			list.nextToken();
			int npcid = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : null;
			
			teleportToNpcs(player, npcid);
		}
	}

	private static void generateDatabyItemId(int itemId)
	{
		for (NpcTemplate npc : NpcHolder.getInstance().getAll())
			if (npc != null)
				if (npc.getRewards() != null && !npc.getRewards().isEmpty() && npc.getRewards().size() != 0)
					for (Map.Entry<RewardType, RewardList> entry : npc.getRewards().entrySet())
						for (RewardGroup group : entry.getValue())
							if (group != null)
								for (RewardItem dat : group.getItems())
									if (dat != null && dat.getItem() != null && dat.getItemId() == itemId)
									{
										_npcs.put(npc, dat);
										break;
									}
	}

	private static void generateDatabyName(String name)
	{
		for (NpcTemplate npc : NpcHolder.getInstance().getAll())
			if (npc != null)
				if (npc.getRewards() != null && !npc.getRewards().isEmpty() && npc.getRewards().size() != 0)
					for (Map.Entry<RewardType, RewardList> entry : npc.getRewards().entrySet())
						for (RewardGroup group : entry.getValue())
							if (group != null)
								for (RewardItem dat : group.getItems())
									if (dat != null && dat.getItem() != null && dat.getItem().getName().toLowerCase().contains(name.toLowerCase()))
									{
										_npcs.put(npc, dat);
										break;
									}
	}
	
	public static void getItemsbyNameId(Player pl, int page, String name)
	{
		_npcs.clear();
		if (Util.isDigit(name))
			generateDatabyItemId(Integer.valueOf(name));
		else
			generateDatabyName(name);
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/rewards.htm", pl);
		
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = _npcs.size() / 12 + 1;
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsdropnameidpage :" + (page + 1) + ":" + name + " \" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsdropnameidpage :" + (page - 1) + ":" + name + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsdropnameidpage :" + (page + 1) + ":" + name + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsdropnameidpage :" + (page - 1) + ":" + name + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for(Entry<NpcTemplate, RewardItem> data : _npcs.entrySet())
		{
			all++;
			if(page == 1 && clansvisual > 12)
				continue;
			if(!pagereached && all > page * 12)
				continue;
			if(!pagereached && all <= (page - 1) * 12)
				continue;
			
			clansvisual++;

			RewardItem rwrditem = data.getValue();
			ItemTemplate item = rwrditem.getItem();
			NpcTemplate npc = data.getKey();
			
			String itemName = item.getName();
			itemName = resizeNames(itemName);
			if (itemName.length() > 30)
			{
				itemName = itemName.substring(0, itemName.length() - (itemName.length() - 30));
				itemName += "...";
			}
			
			String npcName = npc.getName();
			if (npcName.length() > 23)
			{
				npcName = npcName.substring(0, npcName.length() - (npcName.length() - 23));
				npcName += "...";
			}
			
			String iteminfo;
			if (item.isWeapon())
				iteminfo = "bypass _bbsweaponinfoid";
			else if (item.isArmor())
				iteminfo = "bypass _bbsarmorinfoid";
			else if (item.isAccessory())
				iteminfo = "bypass _bbsarmorinfoid";
			else
				iteminfo = "bypass _bbsiteminfoid";
			
			long min = rwrditem.getMinDrop();
			long max = rwrditem.getMaxDrop();
			
			if (rwrditem.getItem().isAdena())
			{
				if (Config.ADENA_DROP_RATE_BY_LEVEL.get(pl.getLevel()) > 0)
					max *= Config.ADENA_DROP_RATE_BY_LEVEL.get(pl.getLevel());
				else
					max *= Config.RATE_DROP_ADENA;
			}
			else if (rwrditem.getItem().isAncientAdena())
				max *= Config.RATE_DROP_AA_ADENA;
			else
			{
				if (!rwrditem.notRate())
				{
					if (Config.RATE_DROP_ITEMS_ID.containsKey(item.getItemId()))
					{
						min *= Config.RATE_DROP_ITEMS_ID.get(item.getItemId());
						max *= Config.RATE_DROP_ITEMS_ID.get(item.getItemId());
					}
					else
						max *= Config.RATE_DROP_ITEMS;
				}
			}
			
			htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + item.getIcon());
			htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"ad9d46\">" + itemName + "</font>");
			htmltosend = htmltosend.replaceAll("%minmaxdrop" + clansvisual + "%", "<font color=\"ad9d46\">[" + min + ".." + max +  "]</font>");
			htmltosend = htmltosend.replaceAll("%itemdropchance" + clansvisual + "%", "<font color=\"009900\">" + pf.format(rwrditem.getChance() / RewardList.MAX_CHANCE) + "</font>");
			htmltosend = htmltosend.replaceAll("%npcname" + clansvisual + "%", "NPC: <font color=\"0099FF\">" + npcName + " (" + npc.getLevel() + ")</font>");
			htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "237");
			htmltosend = htmltosend.replaceAll("%npcinfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":RATED_GROUPED:1\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\">");
			htmltosend = htmltosend.replaceAll("%iteminfo" + clansvisual + "%", "<button value=\"\" action=\"" + iteminfo + " " + item.getItemId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");	
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%minmaxdrop" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemdropchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "273");
				htmltosend = htmltosend.replaceAll("%npcname" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%npcinfo" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%iteminfo" + d + "%", "&nbsp;");
			}
		}

		if (Util.isDigit(name))
		{
			ItemTemplate tempitem = ItemHolder.getInstance().getTemplate(Integer.valueOf(name));
			if(tempitem != null)
				name = tempitem.getName();
		}
		
		htmltosend = htmltosend.replaceAll("%searchfor%", "" + name);
		htmltosend = htmltosend.replaceAll("%totalresults%", "" + _npcs.size());
		ShowBoard.separateAndSend(htmltosend, pl);
	}
	
	private static void getNpcQuests(Player player, int npcId)
	{
		NpcTemplate npc = NpcHolder.getInstance().getTemplate(npcId);
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><title>Quest Information</title><center><font color=\"LEVEL\">");
		dialog.append(npc.getName()).append("<br></font></center><br>");

		int count = 0;
		Map<QuestEventType, Quest[]> list = npc.getQuestEvents();
		
		if (list.isEmpty())
			dialog.append("<font color=\"LEVEL\">- No Quests has been found for this NPC.</font>");
		else
		{
			for (Map.Entry<QuestEventType, Quest[]> entry : list.entrySet())
			{
				for (Quest quest : entry.getValue())
				{
					count++;
					String questname = quest.getName().startsWith("_") ? quest.getName().split("_")[2] : quest.getName();
					String[] r = questname.split("(?=\\p{Upper})");
					
					dialog.append(count + ". ");
					for (int i = 0; i < r.length; i++)
						dialog.append(r[i] + " ");
					
					dialog.append("<br1>");
				}
			}
		}

		dialog.append("</body></html>");
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(dialog.toString());
		
		player.sendPacket(msg);
	}
	
	private static void generateNPCDatabyId(int npcId)
	{
		for (NpcTemplate npc : NpcHolder.getInstance().getAll())
			if (npc != null)
				if (npc.getNpcId() == npcId)
				{
					if (npc.getRewards() == null || npc.getRewards().isEmpty() || npc.getRewards().size() == 0)
						_npcs.put(npc, null);
					else
					{
						for (Map.Entry<RewardType, RewardList> entry : npc.getRewards().entrySet())
							for (RewardGroup group : entry.getValue())
								if (group != null)
									for (RewardItem dat : group.getItems())
										if (dat != null && dat.getItem() != null)
										{
											_npcs.put(npc, dat);
											break;
									}
					}
				}
	}

	private static void generateNPCDatabyName(String name)
	{
		for (NpcTemplate npc : NpcHolder.getInstance().getAll())
			if (npc != null)
				if (!npc.getName().isEmpty() && npc.getName().toLowerCase().contains(name.toLowerCase()))
				{
					if (npc.getRewards() == null || npc.getRewards().isEmpty() || npc.getRewards().size() == 0)
						_npcs.put(npc, null);
					else
					{
						for (Map.Entry<RewardType, RewardList> entry : npc.getRewards().entrySet())
							for (RewardGroup group : entry.getValue())
								if (group != null)
									for (RewardItem dat : group.getItems())
										if (dat != null && dat.getItem() != null)
										{
											_npcs.put(npc, dat);
											break;
									}
					}
				}
	}
	
	public static void getNPCbyNameId(Player pl, int page, String name)
	{
		_npcs.clear();
		if (Util.isDigit(name))
			generateNPCDatabyId(Integer.valueOf(name));
		else
			generateNPCDatabyName(name);
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/rewardsnpc.htm", pl);
		
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = _npcs.size() / 12 + 1;
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsnpcnameidpage :" + (page + 1) + ":" + name + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsnpcnameidpage :" + (page - 1) + ":" + name + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsnpcnameidpage :" + (page + 1) + ":" + name + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsnpcnameidpage :" + (page - 1) + ":" + name + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for(Entry<NpcTemplate, RewardItem> data : _npcs.entrySet())
		{
			NpcTemplate npc = data.getKey();
			
			if (npc.isInstanceOf(PetInstance.class))
				continue;
			
			all++;
			if(page == 1 && clansvisual > 12)
				continue;
			if(!pagereached && all > page * 12)
				continue;
			if(!pagereached && all <= (page - 1) * 12)
				continue;
			
			clansvisual++;
			
			String npcName = npc.getName();
			if (npcName.length() > 23)
			{
				npcName = npcName.substring(0, npcName.length() - (npcName.length() - 23));
				npcName += "...";
			}
			
			//String npcNearString = getNearestLocation(npc.getNpcId());
			
			htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", getIconByRace(npc.getRace()));
			htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"ad9d46\">" +  npcName + " (" + npc.getLevel() + ")</font>");
			htmltosend = htmltosend.replaceAll("%itemdropchance" + clansvisual + "%", "&nbsp;");
			htmltosend = htmltosend.replaceAll("%npcType" + clansvisual + "%", "Type: <font color=\"0099FF\">" + getTypeofNpc(npc) + "</font>");
			htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "283");
			htmltosend = htmltosend.replaceAll("%npcinfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbsdropNpcInfo " + npc.getNpcId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
			htmltosend = htmltosend.replaceAll("%npcloc" + clansvisual + "%", "&nbsp;");
			htmltosend = htmltosend.replaceAll("%npcId" + clansvisual + "%", "<font color=\"504A4B\">ID: " + npc.getNpcId() + "</font>");
			// MORE CORRECT RESPAWN STRING htmltosend = htmltosend.replaceAll("%nearLoc" + clansvisual + "%", npcNearString == StringUtil.EMPTY ? "" : "Near: <font color=\"C80000\">" + npcNearString + "</font>");
			
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemdropchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "287");
				htmltosend = htmltosend.replaceAll("%npcType" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%npcinfo" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%npcloc" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%npcId" + d + "%", "&nbsp;");
			}
		}
		
		htmltosend = htmltosend.replaceAll("%searchfor%", "" + name);
		htmltosend = htmltosend.replaceAll("%totalNpcs%", "" + _npcs.size());
		ShowBoard.separateAndSend(htmltosend, pl);
	}
	
	private void generatenpcInfo(Player player, int npcId)
	{
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/npcinfo.htm", player);
		
		NpcTemplate npc = NpcHolder.getInstance().getTemplate(npcId);
		if (npc != null)
		{
			if (npc.getInstanceConstructor().isAccessible())
				return;
			
			NpcInstance npc2 = npc.getNewInstance();
			
			if (npc2 == null)
				return;
			
			htmltosend = htmltosend.replaceAll("%icon%", getIconByRace(npc.getRace()));
			htmltosend = htmltosend.replaceAll("%name%", npc.getName());
			htmltosend = htmltosend.replaceAll("%id%", String.valueOf(npc.getNpcId()));
			htmltosend = htmltosend.replaceAll("%race%", getNpcRaceById(npc.getRace()));
			htmltosend = htmltosend.replaceAll("%level%", String.valueOf(npc.getLevel()));
			htmltosend = htmltosend.replaceAll("%respawn%", String.valueOf(npc2.getSpawn() != null ? Util.formatTime(npc2.getSpawn().getRespawnDelay()) : "0"));
			htmltosend = htmltosend.replaceAll("%factionId%", String.valueOf(npc.getFaction()));
			htmltosend = htmltosend.replaceAll("%aggro%", String.valueOf(npc2.getAggroRange()));
			htmltosend = htmltosend.replaceAll("%maxHp%", String.valueOf(npc2.getMaxHp()));
			htmltosend = htmltosend.replaceAll("%maxMp%", String.valueOf(npc2.getMaxMp()));
			htmltosend = htmltosend.replaceAll("%pDef%", String.valueOf(npc2.getPDef(null)));
			htmltosend = htmltosend.replaceAll("%mDef%", String.valueOf(npc2.getMDef(null, null)));
			htmltosend = htmltosend.replaceAll("%pAtk%", String.valueOf(npc2.getPAtk(null)));
			htmltosend = htmltosend.replaceAll("%mAtk%", String.valueOf(npc2.getMAtk(null, null)));
			htmltosend = htmltosend.replaceAll("%expReward%", String.valueOf(npc2.getExpReward()));
			htmltosend = htmltosend.replaceAll("%spReward%", String.valueOf(npc2.getSpReward()));
			htmltosend = htmltosend.replaceAll("%runSpeed%", String.valueOf(npc2.getRunSpeed()));
			htmltosend = htmltosend.replaceAll("%class%", getTypeofNpc(npc));
			htmltosend = htmltosend.replaceAll("%walkSpeed%", String.valueOf(npc2.getWalkSpeed()));
			htmltosend = htmltosend.replaceAll("%evs%", String.valueOf(npc2.getEvasionRate(null)));
			htmltosend = htmltosend.replaceAll("%acc%", String.valueOf(npc2.getAccuracy()));
			htmltosend = htmltosend.replaceAll("%aspd%", String.valueOf(npc2.getPAtkSpd()));
			htmltosend = htmltosend.replaceAll("%cspd%", String.valueOf(npc2.getMAtkSpd()));
			
			htmltosend = htmltosend.replaceAll("%defFire%", getResist(npc2.calcStat(Stats.DEFENCE_FIRE, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defWind%", getResist(npc2.calcStat(Stats.DEFENCE_WIND, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defWater%", getResist(npc2.calcStat(Stats.DEFENCE_WATER, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defEarth%", getResist(npc2.calcStat(Stats.DEFENCE_EARTH, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defHoly%", getResist(npc2.calcStat(Stats.DEFENCE_HOLY, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defDark%", getResist(npc2.calcStat(Stats.DEFENCE_UNHOLY, 0, null, null)));
			
			htmltosend = htmltosend.replaceAll("%vulSword%", getResist(100 - npc2.calcStat(Stats.SWORD_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulDual%", getResist(100 - npc2.calcStat(Stats.DUAL_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulBlunt%", getResist(100 - npc2.calcStat(Stats.BLUNT_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulDagger%", getResist(100 - npc2.calcStat(Stats.DAGGER_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulBow%", getResist(100 - npc2.calcStat(Stats.BOW_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulCrossbow%", getResist(100 - npc2.calcStat(Stats.CROSSBOW_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulPole%", getResist(100 - npc2.calcStat(Stats.POLE_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulFist%", getResist(100 - npc2.calcStat(Stats.FIST_WPN_VULNERABILITY, null, null)));
			
			htmltosend = htmltosend.replaceAll("%resStun%", getResist(npc2.calcStat(Stats.STUN_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resRoot%", getResist(npc2.calcStat(Stats.ROOT_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resSleep%", getResist(npc2.calcStat(Stats.SLEEP_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resPara%", getResist(npc2.calcStat(Stats.PARALYZE_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resBleed%", getResist(npc2.calcStat(Stats.BLEED_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resPoison%", getResist(npc2.calcStat(Stats.POISON_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resCancel%", getResist(npc2.calcStat(Stats.CANCEL_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resMental%", getResist(npc2.calcStat(Stats.MENTAL_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resDebuff%", getResist(npc2.calcStat(Stats.DEBUFF_RESIST, 0, null, null)));
			
			
			htmltosend = htmltosend.replaceAll("%npcImage%", "%image:" + npc.getNpcId() + ".jpg%");
			
			npc2.deleteMe(); //delete existing temp npc forstats, to avoid bugs
			ShowBoard.separateAndSend(htmltosend, player);
		}
	}
	
	private static String getResist(double val)
	{
		if (val == 0)
			return "-";

		if (val == Double.POSITIVE_INFINITY)
			return "<font color=\"F80000\">Max</font>"; // max value color red
		else if (val == Double.NEGATIVE_INFINITY)
			return "<font color=\"FFFF33\">Min</font>"; // min value color yellow
		else if (val < 0)
			return "<font color=\"787878\">" + (int)val + "</font>"; // negative color grey
		
		return "<font color=\"66CC33\">" + (int)val + "</font>"; // positive color green
	}
	
	private void generateNpcSkills(Player player, int npcId, int page)
	{
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		NpcTemplate npc = NpcHolder.getInstance().getTemplate(npcId);
		
		if (npc == null)
			return;
		
		NpcInstance npc2 = npc.getNewInstance();
		
		Collection<Skill> list = npc2.getAllSkills();
		
		int totalpages = list.size() / 12 + 1;
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/npcSkills.htm", player);
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsdropnpcskills " + npc.getNpcId() + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsdropnpcskills " + npc.getNpcId() + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsdropnpcskills " + npc.getNpcId() + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsdropnpcskills " + npc.getNpcId() + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		if (npc != null)
		{
			if(list != null && !list.isEmpty())
			{
				for (Skill s : list)
				{
					all++;
					if (page == 1 && clansvisual > 12)
						continue;
					if (!pagereached && all > page * 12)
						continue;
					if (!pagereached && all <= (page - 1) * 12)
						continue;
					
					clansvisual++;
					
					String skillName = s.getName();
					if (skillName.length() > 32)
					{
						skillName = skillName.substring(0, skillName.length() - (skillName.length() - 32));
						skillName += "...";
					}
					
					htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + s.getIcon());
					htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"ad9d46\">" + skillName + "</font>");
					htmltosend = htmltosend.replaceAll("%skillType" + clansvisual + "%", s.isActive() ? "Active" : "Passive");
					htmltosend = htmltosend.replaceAll("%skillId" + clansvisual + "%", "<font color=\"0099FF\">Skill Id: " + s.getId() + " Level: " + s.getLevel() + " </font>");
					htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "100");
				}
			}
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%skillType" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%skillId" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "50");
			}
		}

		htmltosend = htmltosend.replaceAll("%npcId%", "" + npc.getNpcId());
		htmltosend = htmltosend.replaceAll("%nameofnpc%", "" + npc.getName());
		htmltosend = htmltosend.replaceAll("%totalSkills%", "" + list.size());
		
		npc2.deleteMe(); // delete the temp npc to prevent bugs.
		ShowBoard.separateAndSend(htmltosend, player);
	}
	
	private int getTotalDropCount(NpcTemplate npc, RewardType group)
	{
		int totaldrop = 0;
		if (npc != null)
		{
			List<RewardGroup> list = npc.getRewardList(group);
			if (list == null)
				return 0;
			
			for (RewardGroup g : list)
			{
				if (g == null)
					break;
				
				for (RewardItem d : g.getItems())
				{
					if (d != null)
						totaldrop++;
				}
			}
		}
		
		return totaldrop;
	}
	
	private void generateDropListAllTypes(int npcId, String type, Player player, int page)
	{
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/rewardspage.htm", player);
		
		NpcTemplate npc = NpcHolder.getInstance().getTemplate(npcId);
		if (npc != null)
		{
			if (npc.getRewards().isEmpty())
			{
				int clansvisual = 0;
				for (int d = clansvisual + 1; d != 13; d++)
				{
					htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
					htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
					htmltosend = htmltosend.replaceAll("%minmaxdrop" + d + "%", "&nbsp;");
					htmltosend = htmltosend.replaceAll("%itemdropchance" + d + "%", "&nbsp;");
					htmltosend = htmltosend.replaceAll("%width" + d + "%", "236");
					htmltosend = htmltosend.replaceAll("%groupchance" + d + "%", "&nbsp;");
					htmltosend = htmltosend.replaceAll("%npcinfo" + d + "%", "&nbsp;");
					htmltosend = htmltosend.replaceAll("%iteminfo" + d + "%", "&nbsp;");
					clansvisual++;
				}
				
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
				
				htmltosend = htmltosend.replaceAll("%buttonRatedGroup%", "<button value=\"Rated Group\" action=\"\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Large\" fore=\"L2UI_CT1.Button_DF_Large\">");
				htmltosend = htmltosend.replaceAll("%buttonNotRated%", "<button value=\"Not Rated\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
				htmltosend = htmltosend.replaceAll("%buttonNotRatedNotGrouped%", "<button value=\"Not Grouped\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_NOT_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
				htmltosend = htmltosend.replaceAll("%buttonSweep%", "<button value=\"Sweep\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":SWEEP:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
				
				htmltosend = htmltosend.replaceAll("%npcId%", "" + npc.getNpcId());
				htmltosend = htmltosend.replaceAll("%nameofnpc%", "" + npc.getName());
				htmltosend = htmltosend.replaceAll("%totalItems%", "0");
				
				ShowBoard.separateAndSend(htmltosend, player);
				return;
			}
				
			switch (type)
			{
				case "RATED_GROUPED":
					generateDropListGrouped(player, npc, page);
					break;
				case "NOT_RATED_NOT_GROUPED":
					generateDropListNotRated(player, npc, page);
					break;
				case "NOT_RATED_GROUPED":
					generateDropListNotGrouped(player, npc, page);
					break;
				case "SWEEP":
					generateDropListSpoil(player, npc, page);
					break;
			}
		}
	}
	
	private void generateDropListGrouped(Player player, NpcTemplate npc, int page)
	{
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totaldrop = getTotalDropCount(npc, RewardType.RATED_GROUPED);
		
		int totalpages = totaldrop / 12 + 1;
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/rewardspage.htm", player);
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":RATED_GROUPED:" + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":RATED_GROUPED:" + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":RATED_GROUPED:" + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":RATED_GROUPED:" + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		if (npc != null && npc.getRewardList(RewardType.RATED_GROUPED) != null)
		{
			for (RewardGroup g : npc.getRewardList(RewardType.RATED_GROUPED))
			{
				List<RewardItem> items = g.getItems();
				
				for (RewardItem d : items)
				{
					all++;
					if (page == 1 && clansvisual > 12)
						continue;
					if (!pagereached && all > page * 12)
						continue;
					if (!pagereached && all <= (page - 1) * 12)
						continue;
					
					clansvisual++;
					
					ItemTemplate item = d.getItem();
					String itemName = item.getName();
					itemName = resizeNames(itemName);
					if (itemName.length() > 40)
					{
						itemName = itemName.substring(0, itemName.length() - (itemName.length() - 40));
						itemName += "...";
					}
					
					String iteminfo;
					if (item.isWeapon())
						iteminfo = "bypass _bbsweaponinfoid";
					else if (item.isArmor())
						iteminfo = "bypass _bbsarmorinfoid";
					else if (item.isAccessory())
						iteminfo = "bypass _bbsarmorinfoid";
					else
						iteminfo = "bypass _bbsiteminfoid";
					
					long min = d.getMinDrop();
					long max = d.getMaxDrop();
					
					if (d.getItem().isAdena())
					{
						if (Config.ADENA_DROP_RATE_BY_LEVEL.get(player.getLevel()) > 0)
							max *= Config.ADENA_DROP_RATE_BY_LEVEL.get(player.getLevel());
						else
							max *= Config.RATE_DROP_ADENA;
					}
					else if (d.getItem().isAncientAdena())
						max *= Config.RATE_DROP_AA_ADENA;
					else
					{
						if (!d.notRate())
						{
							if (Config.RATE_DROP_ITEMS_ID.containsKey(item.getItemId()))
							{
								min *= Config.RATE_DROP_ITEMS_ID.get(item.getItemId());
								max *= Config.RATE_DROP_ITEMS_ID.get(item.getItemId());
							}
							else
								max *= Config.RATE_DROP_ITEMS;
						}
					}
					
					htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + item.getIcon());
					htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"ad9d46\">" + itemName + "</font>");
					htmltosend = htmltosend.replaceAll("%minmaxdrop" + clansvisual + "%", "<font color=\"ad9d46\">[" + min + ".." + max + "]</font>");
					htmltosend = htmltosend.replaceAll("%itemdropchance" + clansvisual + "%", "<font color=\"0099FF\">" + pf.format(d.getChance() / RewardList.MAX_CHANCE) + "</font>");
					htmltosend = htmltosend.replaceAll("%groupchance" + clansvisual + "%", "<font color=\"b09979\">Group Chance: " + pf.format(g.getChance() / RewardList.MAX_CHANCE) + "</font>");
					htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "200");
					htmltosend = htmltosend.replaceAll("%npcinfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbsdropnameid : " + item.getItemId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\">");
					htmltosend = htmltosend.replaceAll("%iteminfo" + clansvisual + "%", "<button value=\"\" action=\"" + iteminfo + " " + item.getItemId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
				}
			}
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%minmaxdrop" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemdropchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "200");
				htmltosend = htmltosend.replaceAll("%groupchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%npcinfo" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%iteminfo" + d + "%", "&nbsp;");
			}
		}

		// Buttons
		htmltosend = htmltosend.replaceAll("%buttonRatedGroup%", "<button value=\"Rated Group\" action=\"\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Large\" fore=\"L2UI_CT1.Button_DF_Large\">");
		htmltosend = htmltosend.replaceAll("%buttonNotRated%", "<button value=\"Not Rated\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		htmltosend = htmltosend.replaceAll("%buttonNotRatedNotGrouped%", "<button value=\"Not Grouped\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_NOT_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		htmltosend = htmltosend.replaceAll("%buttonSweep%", "<button value=\"Sweep\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":SWEEP:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");

		htmltosend = htmltosend.replaceAll("%npcId%", "" + npc.getNpcId());
		htmltosend = htmltosend.replaceAll("%nameofnpc%", "" + npc.getName());
		htmltosend = htmltosend.replaceAll("%totalItems%", "" + totaldrop);
		
		ShowBoard.separateAndSend(htmltosend, player);
	}

	private void generateDropListNotGrouped(Player player, NpcTemplate npc, int page)
	{
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totaldrop = getTotalDropCount(npc, RewardType.NOT_RATED_GROUPED);
		
		int totalpages = totaldrop / 12 + 1;
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/rewardspage.htm", player);
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_NOT_GROUPED:" + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_NOT_GROUPED:" + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_NOT_GROUPED:" + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_NOT_GROUPED:" + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		if (npc != null && npc.getRewardList(RewardType.NOT_RATED_GROUPED) != null)
		{
			for (RewardGroup g : npc.getRewardList(RewardType.NOT_RATED_GROUPED))
			{
				List<RewardItem> items = g.getItems();
				
				for (RewardItem d : items)
				{
					all++;
					if (page == 1 && clansvisual > 12)
						continue;
					if (!pagereached && all > page * 12)
						continue;
					if (!pagereached && all <= (page - 1) * 12)
						continue;
					
					clansvisual++;
					
					ItemTemplate item = d.getItem();
					String itemName = item.getName();
					itemName = resizeNames(itemName);
					if (itemName.length() > 40)
					{
						itemName = itemName.substring(0, itemName.length() - (itemName.length() - 40));
						itemName += "...";
					}
					
					String iteminfo;
					if (item.isWeapon())
						iteminfo = "bypass _bbsweaponinfoid";
					else if (item.isArmor())
						iteminfo = "bypass _bbsarmorinfoid";
					else if (item.isAccessory())
						iteminfo = "bypass _bbsarmorinfoid";
					else
						iteminfo = "bypass _bbsiteminfoid";
					
					long min = d.getMinDrop();
					long max = d.getMaxDrop();
					
					if (d.getItem().isAdena())
					{
						if (Config.ADENA_DROP_RATE_BY_LEVEL.get(player.getLevel()) > 0)
							max *= Config.ADENA_DROP_RATE_BY_LEVEL.get(player.getLevel());
						else
							max *= Config.RATE_DROP_ADENA;
					}
					else if (d.getItem().isAncientAdena())
						max *= Config.RATE_DROP_AA_ADENA;
					else
					{
						if (!d.notRate())
						{
							if (Config.RATE_DROP_ITEMS_ID.containsKey(item.getItemId()))
							{
								min *= Config.RATE_DROP_ITEMS_ID.get(item.getItemId());
								max *= Config.RATE_DROP_ITEMS_ID.get(item.getItemId());
							}
							else
								max *= Config.RATE_DROP_ITEMS;
						}
					}
					
					htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + item.getIcon());
					htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"ad9d46\">" + itemName + "</font>");
					htmltosend = htmltosend.replaceAll("%minmaxdrop" + clansvisual + "%", "<font color=\"ad9d46\">[" + min + ".." + max + "]</font>");
					htmltosend = htmltosend.replaceAll("%itemdropchance" + clansvisual + "%", "<font color=\"0099FF\">" + pf.format(d.getChance() / RewardList.MAX_CHANCE) + "</font>");
					htmltosend = htmltosend.replaceAll("%groupchance" + clansvisual + "%", "<font color=\"b09979\">Group Chance: " + pf.format(g.getChance() / RewardList.MAX_CHANCE) + "</font>");
					htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "200");
					htmltosend = htmltosend.replaceAll("%npcinfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbsdropnameid : " + item.getItemId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\">");
					htmltosend = htmltosend.replaceAll("%iteminfo" + clansvisual + "%", "<button value=\"\" action=\"" + iteminfo + " " + item.getItemId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
				}
			}
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%minmaxdrop" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemdropchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "200");
				htmltosend = htmltosend.replaceAll("%groupchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%npcinfo" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%iteminfo" + d + "%", "&nbsp;");
			}
		}

		// Buttons
		htmltosend = htmltosend.replaceAll("%buttonRatedGroup%", "<button value=\"Rated Group\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":RATED_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		htmltosend = htmltosend.replaceAll("%buttonNotRated%", "<button value=\"Not Rated\" action=\"\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Large\" fore=\"L2UI_CT1.Button_DF_Large\">");
		htmltosend = htmltosend.replaceAll("%buttonNotRatedNotGrouped%", "<button value=\"Not Grouped\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_NOT_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		htmltosend = htmltosend.replaceAll("%buttonSweep%", "<button value=\"Sweep\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":SWEEP:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		
		htmltosend = htmltosend.replaceAll("%npcId%", "" + npc.getNpcId());
		htmltosend = htmltosend.replaceAll("%nameofnpc%", "" + npc.getName());
		htmltosend = htmltosend.replaceAll("%totalItems%", "" + totaldrop);
		
		ShowBoard.separateAndSend(htmltosend, player);
	}
	
	private void generateDropListNotRated(Player player, NpcTemplate npc, int page)
	{
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totaldrop = getTotalDropCount(npc, RewardType.NOT_RATED_NOT_GROUPED);
		
		int totalpages = totaldrop / 12 + 1;
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/rewardspage.htm", player);
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_GROUPED:" + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_GROUPED:" + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_GROUPED:" + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_GROUPED:" + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		if (npc != null && npc.getRewardList(RewardType.NOT_RATED_NOT_GROUPED) != null)
		{
			for (RewardGroup g : npc.getRewardList(RewardType.NOT_RATED_NOT_GROUPED))
			{
				List<RewardItem> items = g.getItems();
				
				for (RewardItem d : items)
				{
					all++;
					if (page == 1 && clansvisual > 12)
						continue;
					if (!pagereached && all > page * 12)
						continue;
					if (!pagereached && all <= (page - 1) * 12)
						continue;
					
					clansvisual++;
					
					ItemTemplate item = d.getItem();
					String itemName = item.getName();
					itemName = resizeNames(itemName);
					if (itemName.length() > 40)
					{
						itemName = itemName.substring(0, itemName.length() - (itemName.length() - 40));
						itemName += "...";
					}
					
					String iteminfo;
					if (item.isWeapon())
						iteminfo = "bypass _bbsweaponinfoid";
					else if (item.isArmor())
						iteminfo = "bypass _bbsarmorinfoid";
					else if (item.isAccessory())
						iteminfo = "bypass _bbsarmorinfoid";
					else
						iteminfo = "bypass _bbsiteminfoid";
					
					long min = d.getMinDrop();
					long max = d.getMaxDrop();
					
					if (d.getItem().isAdena())
					{
						if (Config.ADENA_DROP_RATE_BY_LEVEL.get(player.getLevel()) > 0)
							max *= Config.ADENA_DROP_RATE_BY_LEVEL.get(player.getLevel());
						else
							max *= Config.RATE_DROP_ADENA;
					}
					else if (d.getItem().isAncientAdena())
						max *= Config.RATE_DROP_AA_ADENA;
					else
					{
						if (!d.notRate())
						{
							if (Config.RATE_DROP_ITEMS_ID.containsKey(item.getItemId()))
							{
								min *= Config.RATE_DROP_ITEMS_ID.get(item.getItemId());
								max *= Config.RATE_DROP_ITEMS_ID.get(item.getItemId());
							}
							else
								max *= Config.RATE_DROP_ITEMS;
						}
					}
					
					htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + item.getIcon());
					htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"ad9d46\">" + itemName + "</font>");
					htmltosend = htmltosend.replaceAll("%minmaxdrop" + clansvisual + "%", "<font color=\"ad9d46\">[" + min + ".." + max + "]</font>");
					htmltosend = htmltosend.replaceAll("%itemdropchance" + clansvisual + "%", "<font color=\"0099FF\">" + pf.format(d.getChance() / RewardList.MAX_CHANCE) + "</font>");
					htmltosend = htmltosend.replaceAll("%groupchance" + clansvisual + "%", "<font color=\"b09979\">Group Chance: " + pf.format(g.getChance() / RewardList.MAX_CHANCE) + "</font>");
					htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "200");
					htmltosend = htmltosend.replaceAll("%npcinfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbsdropnameid : " + item.getItemId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\">");
					htmltosend = htmltosend.replaceAll("%iteminfo" + clansvisual + "%", "<button value=\"\" action=\"" + iteminfo + " " + item.getItemId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
				}
			}
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%minmaxdrop" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemdropchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "200");
				htmltosend = htmltosend.replaceAll("%groupchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%npcinfo" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%iteminfo" + d + "%", "&nbsp;");
			}
		}

		// Buttons
		htmltosend = htmltosend.replaceAll("%buttonRatedGroup%", "<button value=\"Rated Group\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":RATED_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		htmltosend = htmltosend.replaceAll("%buttonNotRated%", "<button value=\"Not Rated\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		htmltosend = htmltosend.replaceAll("%buttonNotRatedNotGrouped%", "<button value=\"Not Grouped\" action=\"\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Large\" fore=\"L2UI_CT1.Button_DF_Large\">");
		htmltosend = htmltosend.replaceAll("%buttonSweep%", "<button value=\"Sweep\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":SWEEP:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
	
		htmltosend = htmltosend.replaceAll("%npcId%", "" + npc.getNpcId());
		htmltosend = htmltosend.replaceAll("%nameofnpc%", "" + npc.getName());
		htmltosend = htmltosend.replaceAll("%totalItems%", "" + totaldrop);
		
		ShowBoard.separateAndSend(htmltosend, player);
	}
	
	private void generateDropListSpoil(Player player, NpcTemplate npc, int page)
	{
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totaldrop = getTotalDropCount(npc, RewardType.SWEEP);
		
		int totalpages = totaldrop / 12 + 1;
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/rewardspage.htm", player);
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":SWEEP:" + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":SWEEP:" + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":SWEEP:" + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":SWEEP:" + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		if (npc != null && npc.getRewardList(RewardType.SWEEP) != null)
		{
			for (RewardGroup g : npc.getRewardList(RewardType.SWEEP))
			{
				List<RewardItem> items = g.getItems();
				
				for (RewardItem d : items)
				{
					all++;
					if (page == 1 && clansvisual > 12)
						continue;
					if (!pagereached && all > page * 12)
						continue;
					if (!pagereached && all <= (page - 1) * 12)
						continue;
					
					clansvisual++;
					
					ItemTemplate item = d.getItem();
					String itemName = item.getName();
					itemName = resizeNames(itemName);
					if (itemName.length() > 40)
					{
						itemName = itemName.substring(0, itemName.length() - (itemName.length() - 40));
						itemName += "...";
					}
					
					String iteminfo;
					if (item.isWeapon())
						iteminfo = "bypass _bbsweaponinfoid";
					else if (item.isArmor())
						iteminfo = "bypass _bbsarmorinfoid";
					else if (item.isAccessory())
						iteminfo = "bypass _bbsarmorinfoid";
					else
						iteminfo = "bypass _bbsiteminfoid";
					
					long min = d.getMinDrop();
					long max = d.getMaxDrop();
					
					if (d.getItem().isAdena())
					{
						if (Config.ADENA_DROP_RATE_BY_LEVEL.get(player.getLevel()) > 0)
							max *= Config.ADENA_DROP_RATE_BY_LEVEL.get(player.getLevel());
						else
							max *= Config.RATE_DROP_ADENA;
					}
					else if (d.getItem().isAncientAdena())
						max *= Config.RATE_DROP_AA_ADENA;
					else
					{
						if (!d.notRate())
						{
							if (Config.RATE_DROP_SPOIL_ITEMS_ID.containsKey(item.getItemId()))
							{
								min *= Config.RATE_DROP_SPOIL_ITEMS_ID.get(item.getItemId());
								max *= Config.RATE_DROP_SPOIL_ITEMS_ID.get(item.getItemId());
							}
							else
								max *= Config.RATE_DROP_SPOIL;
						}
					}
					
					htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + item.getIcon());
					htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"ad9d46\">" + itemName + "</font>");
					htmltosend = htmltosend.replaceAll("%minmaxdrop" + clansvisual + "%", "<font color=\"ad9d46\">[" + min + ".." + max + "]</font>");
					htmltosend = htmltosend.replaceAll("%itemdropchance" + clansvisual + "%", "<font color=\"0099FF\">" + pf.format(d.getChance() / RewardList.MAX_CHANCE) + "</font>");
					htmltosend = htmltosend.replaceAll("%groupchance" + clansvisual + "%", "<font color=\"b09979\">Group Chance: " + pf.format(g.getChance() / RewardList.MAX_CHANCE) + "</font>");
					htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "200");
					htmltosend = htmltosend.replaceAll("%npcinfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbsdropnameid : " + item.getItemId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\">");
					htmltosend = htmltosend.replaceAll("%iteminfo" + clansvisual + "%", "<button value=\"\" action=\"" + iteminfo + " " + item.getItemId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
				}
			}
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%minmaxdrop" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemdropchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "200");
				htmltosend = htmltosend.replaceAll("%groupchance" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%npcinfo" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%iteminfo" + d + "%", "&nbsp;");
			}
		}

		// Buttons
		htmltosend = htmltosend.replaceAll("%buttonRatedGroup%", "<button value=\"Rated Group\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":RATED_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		htmltosend = htmltosend.replaceAll("%buttonNotRated%", "<button value=\"Not Rated\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		htmltosend = htmltosend.replaceAll("%buttonNotRatedNotGrouped%", "<button value=\"Not Grouped\" action=\"bypass _bbsrewards :" + npc.getNpcId() + ":NOT_RATED_NOT_GROUPED:1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Long\" fore=\"L2UI_CT1.Button_DF_Calculator_Long\">");
		htmltosend = htmltosend.replaceAll("%buttonSweep%", "<button value=\"Sweep\" action=\"\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Large\" fore=\"L2UI_CT1.Button_DF_Large\">");

		htmltosend = htmltosend.replaceAll("%npcId%", "" + npc.getNpcId());
		htmltosend = htmltosend.replaceAll("%nameofnpc%", "" + npc.getName());
		htmltosend = htmltosend.replaceAll("%totalItems%", "" + totaldrop);
		
		ShowBoard.separateAndSend(htmltosend, player);
	}
	
	public static String resizeNames(String s)
	{
		return s.replaceFirst("Recipe:", "R:").replaceFirst("Common Item - ", "Common ")
			.replaceFirst("Scroll: Enchant", "Enchant").replaceFirst("Compressed Package", "CP")
			.replaceFirst("Forgotten Scroll - ", "FS ").replaceFirst("Ancient Book - ", "Book ");
	}
	
	private static String getIconByRace(int race)
	{
		switch (race)
		{
			case 1:
				return "icon.skill4290";
			case 2:
				return "icon.skill4291";
			case 3:
				return "icon.skill4292";
			case 4:
				return "icon.skill4293";
			case 5:
				return "icon.skill4294";
			case 6:
				return "icon.skill4295";
			case 7:
				return "icon.skill4296";
			case 8:
				return "icon.skill4297";
			case 9:
				return "icon.skill4298";
			case 10:
				return "icon.skill4299";
			case 11:
				return "icon.skill4300";
			case 12:
				return "icon.skill4301";
			case 13:
				return "icon.skill4302";
			case 14:
				return "icon.skill4416_human";
			case 15:
				return "icon.skill4416_elf";
			case 16:
				return "icon.skill4416_darkelf";
			case 17:
				return "icon.skill4416_orc";
			case 18:
				return "icon.skill4416_dwarf";
			case 19:
				return "icon.skill4416_etc";
			case 20:
				return "icon.skill4416_none";
			case 21:
				return "icon.skill4416_siegeweapon";
			case 22:
				return "icon.skill4416_castleguard";
			case 23:
				return "icon.skill4416_mercenary";
			case 24:
				return "icon.skill4286";
			case 25:
				return "icon.skill4416_kamael";
		}
		
		return "L2UI.TutorialHelp";
	}
	
	private static String getTypeofNpc(NpcTemplate npc)
	{
		if (npc.isInstanceOf(BossInstance.class))
			return "GrandBoss";
		else if (npc.isInstanceOf(RaidBossInstance.class))
			return "RaidBoss";
		else if (npc.isInstanceOf(MonsterInstance.class))
			return "Monster";
		else if (npc.isInstanceOf(NpcInstance.class))
			return "Npc";
		
		return "Unknown";
	}
	
	@SuppressWarnings("unused")
	private static String getQuestEventType(QuestEventType type)
	{
		switch(type)
		{
			case MOB_TARGETED_BY_SKILL:
				return "Action triggered when a character uses a skill on the creature.";
			case ATTACKED_WITH_QUEST:
				return "Action triggered when a creature is attacked.";
			case MOB_KILLED_WITH_QUEST:
				return "Action triggered when the creature is killed.";
			case QUEST_START:
			case NPC_FIRST_TALK:
			case QUEST_TALK:
				return "Talk with the NPC to start the quest.";
			default:
				return "Not defined";
		}
	}
	
	private static String getNpcRaceById(int raceId)
	{
		switch(raceId)
		{
			case 1:
				return "Undead";
			case 2:
				return "Magic Creatures";
			case 3:
				return "Beasts";
			case 4:
				return "Animals";
			case 5:
				return "Plants";
			case 6:
				return "Humanoids";
			case 7:
				return "Spirits";
			case 8:
				return "Angels";
			case 9:
				return "Demons";
			case 10:
				return "Dragons";
			case 11:
				return "Giants";
			case 12:
				return "Bugs";
			case 13:
				return "Fairies";
			case 14:
				return "Humans";
			case 15:
				return "Elves";
			case 16:
				return "Dark Elves";
			case 17:
				return "Orcs";
			case 18:
				return "Dwarves";
			case 19:
				return "Others";
			case 20:
				return "Non-living Beings";
			case 21:
				return "Siege Weapons";
			case 22:
				return "Defending Army";
			case 23:
				return "Mercenaries";
			case 24:
				return "Unknown Creature";
			case 25:
				return "Kamael";
			default:
				return "Not defined";
		}
	}
	
	private static void setMapLocation(Player player, int npcId)
	{
		NpcTemplate temp = NpcHolder.getInstance().getTemplate(npcId);
		if (temp == null || player == null)
			return;
		
		NpcInstance npc = GameObjectsStorage.getNpc(temp.getName());
		if (npc != null)
		{
			Location loc = npc.getSpawnedLoc();
			if (loc != null)
			{
				player.addRadar(loc.getX(), loc.getY(), loc.getZ());
				player.addRadarWithMap(loc.getX(), loc.getY(), loc.getZ(), true);
			}
			else
				player.sendMessage("I did not find any spawn's for this NPC!");
		}
		else
			player.sendMessage("I did not find any spawn's for this NPC!");
	}
	
	@SuppressWarnings("unused")
	private static String getNearestLocation(int npcId)
	{
		String nearestTown = "";
		NpcInstance npc = GameObjectsStorage.getByNpcId(npcId);
		if (npc != null)
		{
			RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, npc.getLoc());
			
			if(ra != null)
			{
				RestartPoint rp = ra.getRestartPoint().get(Race.elf);
				nearestTown = rp.getNameLoc();
			}
		}
		return nearestTown;
	}
	
	private static void teleportToNpcs(Player player, int npcId)
	{
		NpcInstance npc = GameObjectsStorage.getByNpcId(npcId);
		if (npc != null)
		{
			Location loc = npc.getLoc().coordsRandomize(100, 250);
			player.teleToLocation(loc);
		}
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
}