package services.community;

import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.ItemsDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.CharTemplateHolder;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.ItemInstance.ItemLocation;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.stats.triggers.TriggerInfo;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.templates.PlayerTemplate;
import l2r.gameserver.templates.item.ItemTemplate.Grade;
import l2r.gameserver.utils.Util;
import l2r.gameserver.utils.ValueSortMap;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Infern0
 *
 */
public class CommunityBoardCharacteItems implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityBoardCharacteItems.class);
	private static List<ItemInstance> _playerItems = new ArrayList<ItemInstance>();
	private static TIntObjectHashMap<SortBy> _playerSortBy = new TIntObjectHashMap<>();
	
	public static final CommunityBoardCharacteItems getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityBoardCharacteItems _instance = new CommunityBoardCharacteItems();
	}

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: CharacterItemInfo service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbschar", "_bbscharSearch", "_bbscharData", "_bbscharItemInfo" };
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, " ");
		String cmd = st.nextToken();

		// initial page of search
		if (cmd.equalsIgnoreCase("_bbschar"))
		{
			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/characterItems/search.htm", player);

			ShowBoard.separateAndSend(content, player);
		}
		// search for player name or player objectId
		else if (cmd.equalsIgnoreCase("_bbscharSearch"))
		{
			StringTokenizer list = new StringTokenizer(bypass, " ");
			list.nextToken();
			String name = list.hasMoreTokens() ? list.nextToken().trim() : "";
			int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
			
			generateCharacters(player, name, page);
		}
		else if (cmd.equalsIgnoreCase("_bbscharData"))
		{
			StringTokenizer list = new StringTokenizer(bypass, ":");
			list.nextToken();
			int charObjId = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : -1;
			int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
			String findItem = list.hasMoreTokens() ? list.nextToken() : "";
			
			generateCharItems(player, charObjId, page, findItem);
		}
		else if (cmd.equalsIgnoreCase("_bbscharItemInfo"))
		{
			StringTokenizer list = new StringTokenizer(bypass, ":");
			list.nextToken();
			int itemObj = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : -1;
			int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
			String findItem = list.hasMoreTokens() ? list.nextToken() : "";
			
			sendItemInfo(player, itemObj, page, findItem);
		}
		else if(cmd.equalsIgnoreCase("_bbscharSortBy"))
		{
			StringTokenizer list = new StringTokenizer(bypass, " ");
			list.nextToken();
			String sorname = list.hasMoreTokens() ? list.nextToken() : "Id";
			int charObjId = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : -1;
			
			_playerSortBy.put(player.getObjectId(), SortBy.getEnum(sorname));
			
			generateCharItems(player, charObjId, 1, "");
		}
		else if(cmd.equalsIgnoreCase("_bbscharItemsById"))
		{
			StringTokenizer list = new StringTokenizer(bypass, " ");
			list.nextToken();
			int itemId = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : -1;
			int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
			
			findItemsById(player, page, itemId);
		}
	}

	private static void getCharacterItems(int charid, String findItem)
	{
		Collection<ItemInstance> items;
		Player plr = World.getPlayer(charid);
		
		if (charid == -55) // usable only for send in this script.
			items = ItemsDAO.getInstance().getItemsByItemId(Integer.valueOf(findItem));
		else if (plr != null)
			items = plr.getInventory().getItemsList();
		else
			items = ItemsDAO.getInstance().getItemsByOwnerId(charid);
		
		if (charid == -55) // usable only for send in this script.
			items = ItemsDAO.getInstance().getItemsByItemId(Integer.valueOf(findItem));
		
		for (ItemInstance item : items)
		{
			if (item == null)
				continue;
			
			if (!findItem.isEmpty() && !Util.isDigit(findItem))
			{
				findItem.trim();
				while (findItem.startsWith(" "))
					findItem = findItem.replaceFirst(" ", "");
				
				if (item.getName().toLowerCase().equalsIgnoreCase(findItem.toLowerCase()) || item.getName().toLowerCase().contains(findItem.toLowerCase()))
					_playerItems.add(item);
			}
			else
				_playerItems.add(item);
		}
	}
	
	public static void generateCharacters(Player pl, String name, int page)
	{
		List<String> players = new ArrayList<String>();
		
		if (Util.isDigit(name))
			name = CharacterDAO.getInstance().getNameByObjectId(Integer.parseInt(name));
		
		players = CharacterDAO.getInstance().getCharactersByName(name);
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/characterItems/chars.htm", pl);
		
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = players.size() / 12 + 1;
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbscharSearch " + name + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbscharSearch :" + name + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbscharSearch " + name + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbscharSearch " + name + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for(String plrName : players)
		{
			if (plrName == null || plrName.isEmpty())
				continue;
			
			all++;
			if(page == 1 && clansvisual > 12)
				continue;
			if(!pagereached && all > page * 12)
				continue;
			if(!pagereached && all <= (page - 1) * 12)
				continue;
			
			clansvisual++;

			String clan = CharacterDAO.getInstance().generateCharDataBy(plrName, "clanId");
			Clan cl = ClanTable.getInstance().getClan(Integer.parseInt(clan));
			if (cl != null)
				clan = cl.getName();
			else
				clan = "No Clan";
			
			int playerObjId = CharacterDAO.getInstance().getObjectIdByName(plrName);
			
			long level = CharacterDAO.getInstance().getClassData(playerObjId, "level");
			
			int activeClass = CharacterDAO.getInstance().getClassIdByObjectId(playerObjId, false);
			int baseClass = CharacterDAO.getInstance().getClassIdByObjectId(playerObjId, true);
			
			String classname = Util.getFullClassName(activeClass);
			
			String classIcon;
			PlayerTemplate templ = CharTemplateHolder.getInstance().getTemplate(baseClass, false);
			if (templ == null)
				classIcon = "L2UI_CT1.Inventory_DF_CloakSlot_Disable";
			else
				classIcon = "icon.skill" + templ.race.name();
			
			String onlineColor;
			Player plr = World.getPlayer(playerObjId);
			if (plr != null)
				onlineColor = "0BE830";
			else
				onlineColor = "0BE830";
			
			htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", classIcon);
			htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"" + onlineColor + "\">" + plrName + "</font> [" + level + "]");
			htmltosend = htmltosend.replaceAll("%clan" + clansvisual + "%", "<font color=\"F5E500\">Clan - " + clan +  "</font>");
			htmltosend = htmltosend.replaceAll("%class" + clansvisual + "%", "<font color=\"79C5D4\">Class - " + classname + "</font>");
			htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "257");
			htmltosend = htmltosend.replaceAll("%playerinfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbscharData :" + playerObjId + ":1\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\">");
			htmltosend = htmltosend.replaceAll("%nnotused" + clansvisual + "%", "&nbsp;");
			htmltosend = htmltosend.replaceAll("%notused" + clansvisual + "%", "&nbsp;");
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%clan" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%class" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "273");
				htmltosend = htmltosend.replaceAll("%playerinfo" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%nnotused" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%notused" + d + "%", "&nbsp;");
			}
		}
		
		htmltosend = htmltosend.replaceAll("%searchfor%", "" + name);
		htmltosend = htmltosend.replaceAll("%totalresults%", "" + all);
		
		ShowBoard.separateAndSend(htmltosend, pl);
	}
	
	public static void generateCharItems(Player pl, int charObjectId, int page, String findItem)
	{
		if (charObjectId == -1)
			return;
		
		_playerItems.clear();
		getCharacterItems(charObjectId, findItem);
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/characterItems/charitems.htm", pl);
		
		StringBuilder sb = new StringBuilder();
		SortBy sortBy = _playerSortBy.get(pl.getObjectId());
		
		if (sortBy == null)
			sortBy = SortBy.ID;
		
		List<ItemInstance> items = getSortedItems(_playerSortBy.get(pl.getObjectId()), _playerItems);
		
		String nameOfCurSortBy = sortBy.toString() + ";";
		sb.append(nameOfCurSortBy);
		
		for (SortBy s : SortBy.values())
		{
			String str = s + ";";
			if (!str.toString().equalsIgnoreCase(nameOfCurSortBy))
				sb.append(str);
		}
		
		htmltosend = htmltosend.replaceAll("%sortbylist%", sb.toString());
		
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = items.size() / 12 + 1;
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbscharData :" + charObjectId + ":" + (page + 1) + ":" + findItem + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbscharData :" + charObjectId + ":" + (page - 1) + ":" + findItem + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbscharData :" + charObjectId + ":" + (page + 1) + ":" + findItem + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbscharData :" + charObjectId + ":" + (page - 1) + ":" + findItem + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for(ItemInstance item : items)
		{
			if (item == null)
				continue;
			all++;
			if(page == 1 && clansvisual > 12)
				continue;
			if(!pagereached && all > page * 12)
				continue;
			if(!pagereached && all <= (page - 1) * 12)
				continue;
			
			clansvisual++;
			
			String itemName = item.getName();
			itemName = resizeNames(itemName);
			if (itemName.length() > 30)
			{
				itemName = itemName.substring(0, itemName.length() - (itemName.length() - 30));
				itemName += "...";
			}
			
			long itemsInDatabse = ItemsDAO.getInstance().getItemCount(item.getItemId());
			
			String itemInDB = "Items In DB: " + itemsInDatabse;
			
			sb = new StringBuilder();
			
			for (TriggerInfo trg : item.getAugmentTriggerList())
			{
				if (trg == null)
					continue;
				
				sb.append(trg.getSkill().getName() + " ");
			}
			
			for (Skill skill : item.getAugmentSkills())
			{
				if (skill == null)
					continue;
				
				sb.append(skill.getName() + " ");
			}
			
			String augment = "Augment: " + (sb.toString().isEmpty() ? "No" : sb.toString());
			String enchant = "Enchant: " + item.getEnchantLevel();
			
			String location;
			
			switch(item.getLocation().name())
			{
				case "VOID":
				case "LEASE":
				case "FREIGHT":
					location = "FF5A68";
					break;
				case "INVENTORY":
					location = "01EA08";
					break;
				case "Warehouse":
					location = "04AAF2";
					break;
				case "CLANWH":
					location = "0847DA";
					break;
				case "PAPERDOLL":
					location = "B69800";
					break;
				case "MAIL":
					location = "DAFF01";
					break;
				default:
					location = "E0DFDA";
					break;
			}
			
			htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + item.getTemplate().getIcon());
			htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"FF8A21\">" + itemName + "</font>" + (item.getCount() > 1 ? "&nbsp;&nbsp;<font color=\"E12E3A\">x" + item.getCount() + "</font>" : ""));
			htmltosend = htmltosend.replaceAll("%augment" + clansvisual + "%", augment);
			htmltosend = htmltosend.replaceAll("%enchant" + clansvisual + "%", enchant);
			htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "283");
			htmltosend = htmltosend.replaceAll("%itemLoc" + clansvisual + "%", "<font color=\"D6FFB7\">ID: (" + item.getItemId() + ") | </font><font color=\"" + location + " name=hs10\">" + item.getLocation().name() + "</font> | <font color=\"05A005\">Grade: " + String.valueOf(item.getCrystalType()) + "</font>");
			htmltosend = htmltosend.replaceAll("%itemInDB" + clansvisual + "%", "" + itemInDB);
			htmltosend = htmltosend.replaceAll("%itemInfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbscharItemInfo :" + item.getObjectId() + ":" + page + ":" + findItem + "\" width=16 height=16 back=\"L2UI_CH3.aboutotpicon\" fore=\"L2UI_CH3.aboutotpicon_over\">");
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%augment" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "287");
				htmltosend = htmltosend.replaceAll("%enchant" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemLoc" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemInDB" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemInfo" + d + "%", "&nbsp;");
			}
		}
		
		htmltosend = htmltosend.replaceAll("%type%", "" + sortBy.name());
		htmltosend = htmltosend.replaceAll("%searchfor%", "<font color=\"188288\">" + CharacterDAO.getInstance().getNameByObjectId(charObjectId) + "</font>" + (!findItem.isEmpty() ? " | <font color=\"02ABFF\">Search for: " + findItem.trim() + "</font>" : ""));
		htmltosend = htmltosend.replaceAll("%charId%", "" + charObjectId);
		htmltosend = htmltosend.replaceAll("%totalNpcs%", "" + all);
		ShowBoard.separateAndSend(htmltosend, pl);
	}
	
	public static void findItemsById(Player pl, int page, int itemId)
	{
		if (itemId == -1)
			return;
		
		_playerItems.clear();
		getCharacterItems(-55, String.valueOf(itemId));
		
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/characterItems/itemsbyid.htm", pl);
		
		StringBuilder sb = new StringBuilder();
		SortBy sortBy = _playerSortBy.get(pl.getObjectId());
		
		if (sortBy == null)
			sortBy = SortBy.ID;
		
		List<ItemInstance> items = getSortedItems(_playerSortBy.get(pl.getObjectId()), _playerItems);
		
		String nameOfCurSortBy = sortBy.toString() + ";";
		sb.append(nameOfCurSortBy);
		
		for (SortBy s : SortBy.values())
		{
			String str = s + ";";
			if (!str.toString().equalsIgnoreCase(nameOfCurSortBy))
				sb.append(str);
		}
		
		htmltosend = htmltosend.replaceAll("%sortbylist%", sb.toString());
		
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = items.size() / 12 + 1;
		
		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbscharItemsById  " + itemId + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbscharItemsById " + itemId + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbscharItemsById " + itemId + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbscharItemsById " + itemId + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for(ItemInstance item : items)
		{
			if (item == null)
				continue;
			all++;
			if(page == 1 && clansvisual > 12)
				continue;
			if(!pagereached && all > page * 12)
				continue;
			if(!pagereached && all <= (page - 1) * 12)
				continue;
			
			clansvisual++;
			
			String itemName = item.getName();
			itemName = resizeNames(itemName);
			if (itemName.length() > 30)
			{
				itemName = itemName.substring(0, itemName.length() - (itemName.length() - 30));
				itemName += "...";
			}
			
			//long itemsInDatabse = ItemsDAO.getInstance().getItemCount(item.getItemId());
			
			String owner = "<font name=hs10 color=\"D6FFB7\">Owner: (" + CharacterDAO.getInstance().getNameByObjectId(item.getOwnerId()) + ")</font>";
			
			sb = new StringBuilder();
			
			for (TriggerInfo trg : item.getAugmentTriggerList())
			{
				if (trg == null)
					continue;
				
				sb.append(trg.getSkill().getName() + " ");
			}
			
			for (Skill skill : item.getAugmentSkills())
			{
				if (skill == null)
					continue;
				
				sb.append(skill.getName() + " ");
			}
			
			String augment = "Augment: " + (sb.toString().isEmpty() ? "No" : sb.toString());
			String enchant = "Enchant: " + item.getEnchantLevel();
			
			String location;
			
			switch(item.getLocation().name())
			{
				case "VOID":
				case "LEASE":
				case "FREIGHT":
					location = "FF5A68";
					break;
				case "INVENTORY":
					location = "01EA08";
					break;
				case "Warehouse":
					location = "04AAF2";
					break;
				case "CLANWH":
					location = "0847DA";
					break;
				case "PAPERDOLL":
					location = "B69800";
					break;
				case "MAIL":
					location = "DAFF01";
					break;
				default:
					location = "E0DFDA";
					break;
			}
			
			htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + item.getTemplate().getIcon());
			htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"FF8A21\">" + itemName + "</font>" + (item.getCount() > 1 ? "&nbsp;&nbsp;<font color=\"E12E3A\">x" + item.getCount() + "</font>" : ""));
			htmltosend = htmltosend.replaceAll("%augment" + clansvisual + "%", augment);
			htmltosend = htmltosend.replaceAll("%enchant" + clansvisual + "%", enchant);
			htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "283");
			htmltosend = htmltosend.replaceAll("%itemLoc" + clansvisual + "%", "<font color=\"" + location + " name=hs10\">" + item.getLocation().name() + "</font> | <font color=\"05A005\">Grade: " + String.valueOf(item.getCrystalType()) + "</font>");
			htmltosend = htmltosend.replaceAll("%itemInDB" + clansvisual + "%", "<a action=\"bypass _bbscharData :" + item.getOwnerId() + ":1\">" + owner + "</a>");
			htmltosend = htmltosend.replaceAll("%itemInfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbscharItemInfo :" + item.getObjectId() + ":" + page + ":" + item.getName() + "\" width=16 height=16 back=\"L2UI_CH3.aboutotpicon\" fore=\"L2UI_CH3.aboutotpicon_over\">");
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%augment" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "287");
				htmltosend = htmltosend.replaceAll("%enchant" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemLoc" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemInDB" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemInfo" + d + "%", "&nbsp;");
			}
		}
		
		htmltosend = htmltosend.replaceAll("%type%", "" + sortBy.name());
		htmltosend = htmltosend.replaceAll("%searchfor%", "<font color=\"188288\">" + ItemHolder.getInstance().getTemplateName(itemId) + "</font>");
		htmltosend = htmltosend.replaceAll("%charId%", "" + itemId);
		htmltosend = htmltosend.replaceAll("%totalNpcs%", "" + all);
		ShowBoard.separateAndSend(htmltosend, pl);
	}
	
	private static void sendItemInfo(Player player, int itemObjectid, int page, String finditem)
	{
		ItemInstance item = GameObjectsStorage.getAsItem(itemObjectid);
		if (item == null)
			item = ItemsDAO.getInstance().load(itemObjectid);
		
		if (item == null)
			return;
		
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/characterItems/iteminfo.htm", player);
		
		html = html.replaceAll("%owner%", String.valueOf(CharacterDAO.getInstance().getNameByObjectId(item.getOwnerId())));
		html = html.replaceAll("%icon%", "icon." + item.getTemplate().getIcon());
		
		html = html.replaceAll("%name%", String.valueOf(item.getTemplate().getName()));
		html = html.replaceAll("%objId%", String.valueOf(item.getObjectId()));
		html = html.replaceAll("%itemId%", String.valueOf(item.getItemId()));
		html = html.replaceAll("%grade%", String.valueOf(item.getCrystalType()));
		html = html.replaceAll("%count%", String.valueOf(item.getCount()));
		
		for(Element e : Element.VALUES)
			html = html.replaceAll("%" + e.name().toLowerCase() + "Val%", String.valueOf(item.getAttributeElementValue(e, true)));

		html = html.replaceAll("%attrElement%", String.valueOf(item.getAttributeElement()));
		html = html.replaceAll("%attrValue%", String.valueOf(item.getAttributeElementValue()));

		html = html.replaceAll("%enchLevel%", String.valueOf(item.getEnchantLevel()));
		html = html.replaceAll("%type%", String.valueOf(item.getItemType()));
		
		NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
		npcHtml.setHtml(html);
		player.sendPacket(npcHtml);
		
		generateCharItems(player, item.getOwnerId(), page, finditem);
	}
	
	public static String resizeNames(String s)
	{
		return s.replaceFirst("Recipe:", "R:").replaceFirst("Common Item - ", "Common ")
			.replaceFirst("Scroll: Enchant", "Enchant").replaceFirst("Compressed Package", "CP")
			.replaceFirst("Forgotten Scroll - ", "FS ").replaceFirst("Ancient Book - ", "Book ");
	}
	
	@SuppressWarnings("unchecked")
	public static List<ItemInstance> getSortedItems(SortBy sort, List<ItemInstance> items)
	{
		if (sort == null)
			sort = SortBy.ID;
		
		List<ItemInstance> sorted = new ArrayList<>();

		switch(sort)
		{
			default:
			case ID:
				List<ItemInstance> notSortedValues = new ArrayList<>();
				notSortedValues.addAll(items);
				ItemInstance storedid = null;
				int lastpoints = 0;
				
				while(notSortedValues.size() > 0)
				{
					if(sorted.size() == items.size())
						break;

					for(ItemInstance item : notSortedValues)
						if(item.getItemId() >= lastpoints)
						{
							storedid = item;
							lastpoints = item.getItemId();
						}

					if(storedid != null)
					{
						notSortedValues.remove(storedid);
						sorted.add(storedid);
						storedid = null;
						lastpoints = 0;
					}
				}
				
				return sorted;
			case COUNT_ASC:
				Map<ItemInstance, Long> tmp = new FastMap<>();
				for (ItemInstance item : items)
					tmp.put(item, item.getCount());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp, true).keySet());
				return sorted;
			case COUNT_DSC:
				Map<ItemInstance, Long> tmp2 = new FastMap<>();
				for (ItemInstance item : items)
					tmp2.put(item, item.getCount());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp2, false).keySet());
				return sorted;
			case NAME_ASC:
				Map<ItemInstance, String> tmp3 = new FastMap<>();
				for (ItemInstance item : items)
					tmp3.put(item, item.getName());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp3, true).keySet());
				return sorted;
			case NAME_DSC:
				Map<ItemInstance, String> tmp4 = new FastMap<>();
				for (ItemInstance item : items)
					tmp4.put(item, item.getName());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp4, false).keySet());
				return sorted;
			case GRADE_ASC:
				Map<ItemInstance, Grade> tmp5 = new FastMap<>();
				for (ItemInstance item : items)
					tmp5.put(item, item.getCrystalType());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp5, true).keySet());
				return sorted;
			case GRADE_DSC:
				Map<ItemInstance, Grade> tmp6 = new FastMap<>();
				for (ItemInstance item : items)
					tmp6.put(item, item.getCrystalType());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp6, false).keySet());
				return sorted;
			case PRICE_ASC:
				Map<ItemInstance, Integer> tmp7 = new FastMap<>();
				for (ItemInstance item : items)
					tmp7.put(item, item.getReferencePrice());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp7, true).keySet());
				return sorted;
			case PRICE_DSC:
				Map<ItemInstance, Integer> tmp8 = new FastMap<>();
				for (ItemInstance item : items)
					tmp8.put(item, item.getReferencePrice());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp8, false).keySet());
				return sorted;
			case INVENTORY:
				Map<ItemInstance, Integer> tmp9 = new FastMap<>();
				for (ItemInstance item : items)
				{
					if (item.getLocation() != ItemLocation.INVENTORY) 
						continue;
					
					tmp9.put(item, item.getItemId());
				}
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp9, false).keySet());
				return sorted;
			case EQUIPED:
				Map<ItemInstance, Integer> tmp10 = new FastMap<>();
				for (ItemInstance item : items)
				{
					if (item.getLocation() != ItemLocation.PAPERDOLL) 
						continue;
					
					tmp10.put(item, item.getItemId());
				}
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp10, false).keySet());
				return sorted;
			case WAREHOUSE:
				Map<ItemInstance, Integer> tmp11 = new FastMap<>();
				for (ItemInstance item : items)
				{
					if (item.getLocation() != ItemLocation.WAREHOUSE) 
						continue;
					
					tmp11.put(item, item.getItemId());
				}
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp11, false).keySet());
				return sorted;
			case VOID:
				Map<ItemInstance, Integer> tmp12 = new FastMap<>();
				for (ItemInstance item : items)
				{
					if (item.getLocation() != ItemLocation.VOID) 
						continue;
					
					tmp12.put(item, item.getItemId());
				}
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp12, false).keySet());
				return sorted;
			case MAIL:
				Map<ItemInstance, Integer> tmp13 = new FastMap<>();
				for (ItemInstance item : items)
				{
					if (item.getLocation() != ItemLocation.MAIL) 
						continue;
					
					tmp13.put(item, item.getItemId());
				}
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp13, false).keySet());
				return sorted;
		}
	}
	
	private enum SortBy
	{
		ID("Id"),
		INVENTORY("Inventory"),
		EQUIPED("Equiped"),
		WAREHOUSE("Warehouse"),
		VOID("Void"),
		MAIL("Mail"),
		COUNT_ASC("Count(Ascending)"),
		COUNT_DSC("Count(Descending)"),
		NAME_ASC("Name(Ascending)"),
		NAME_DSC("Name(Descending)"),
		GRADE_ASC("Grade(Ascending)"),
		GRADE_DSC("Grade(Descending)"),
		PRICE_ASC("Price(Ascending)"),
		PRICE_DSC("Price(Descending)");
		
		private String _sortName;
		
		private SortBy(String sortName)
		{
			_sortName = sortName;
		}
		
		@Override
		public String toString()
		{
			return _sortName;
		}
		
		public static SortBy getEnum(String sortName)
		{
			for (SortBy sb : values())
			{
				if (sb.toString().equals(sortName))
					return sb;
			}
			
			return ID;
		}
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
}