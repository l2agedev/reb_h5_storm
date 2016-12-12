package quests;

import l2r.gameserver.Config;
import l2r.gameserver.dao.RankSystemTable;
import l2r.gameserver.dao.RankSystemTable.RankInformation;
import l2r.gameserver.data.xml.holder.CharTemplateHolder;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripts.ScriptFile;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TreeSet;


public class _5001_RankNPC extends Quest implements ScriptFile
{
	private final static int NPC = Config.RANK_NPC_ID;
	private final static String NAME_QUEST = _5001_RankNPC.class.getSimpleName();
	private final static DecimalFormat f = new DecimalFormat(",##0,000");
	private final static RankSystemTable rst = RankSystemTable.getInstance();
	
	public _5001_RankNPC()
	{
		super(false);
		addStartNpc(NPC);
		addTalkId(NPC);
		addFirstTalkId(NPC);
	}
	
	private String title(Player player)
	{
		String htmltext = NpcHtmlMessage.title("Rank Manager", "<br>Rank Information");
		htmltext += "<center>Hello, " + player.getName() + "!</center><br>";
		return htmltext;
	}
	
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "<html><body><br><center>Nothing to say..</center></body></html>";
		
		if (!Config.ENABLE_RANK_MANAGER)
			return htmltext;
		
		return page(st.getPlayer(), "");
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		String htmltext = "<html><body><br><center>Nothing to say..</center></body></html>";
		
		if (!Config.ENABLE_RANK_MANAGER)
			return htmltext;
		
		return page(player, "");
	}
	
	private String footer()
	{
		return NpcHtmlMessage.footer("NPC Rank");
	}
	
	private String button(String value, String event, int w, int h, int type, boolean revert)
	{
		return NpcHtmlMessage.questButton(NAME_QUEST, value, event, w, h, type, revert);
	}
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String paramEvent[] = event.split(" ");
		String action = "page", param1 = "", param2 = "", param3 = "", param4 = "";
		int size = paramEvent.length;
		
		if (size >= 1)
			action = paramEvent[0];
		if (size >= 2)
			param1 = paramEvent[1];
		if (size >= 3)
			param2 = paramEvent[2];
		if (size >= 4)
			param3 = paramEvent[3];
		if (size >= 5)
			param4 = paramEvent[4];
		
		String htmltext = "";
		if (action.equals("page"))
			htmltext = page(st.getPlayer(), "");
		else if (action.equalsIgnoreCase("generatePage"))
			htmltext = generatePage(Integer.valueOf(param1), st.getPlayer());
		else if (action.equalsIgnoreCase("viewPage"))
		{
			switch (Integer.valueOf(param1))
			{
				case 1:
					htmltext = viewItem(st.getPlayer(), RankSystemTable.ITEM, Integer.valueOf(param2), Integer.valueOf(param3));
					break;
				case 2:
					htmltext = viewItem(st.getPlayer(), RankSystemTable.OLYMPIAD, Integer.valueOf(param2), Integer.valueOf(param3));
					break;
				case 3:
					htmltext = viewItem(st.getPlayer(), RankSystemTable.PVP, Integer.valueOf(param2), Integer.valueOf(param3));
					break;
				case 4:
					htmltext = viewItem(st.getPlayer(), RankSystemTable.PK, Integer.valueOf(param2), Integer.valueOf(param3));
					break;
				default:
					htmltext = page(st.getPlayer(), "Invalid option!");
					break;
			}
		}
		else if (action.equalsIgnoreCase("generateInfo"))
			htmltext = viewInfoChar(st.getPlayer(), Integer.parseInt(param1), Integer.parseInt(param2), Integer.parseInt(param3), Integer.parseInt(param4));
		return htmltext;
	}
	
	private Boolean disablePage(int page)
	{
		for (int i = 0; i < Config.RANK_NPC_DISABLE_PAGE.length; i++)
			if (Integer.valueOf(Config.RANK_NPC_DISABLE_PAGE[i]) == page)
				return true;
		return false;
	}
	
	private String getNameItem(int item_id)
	{
		return ItemHolder.getInstance().getTemplate(item_id).getName();
	}
	
	private String getClassName(int class_id)
	{
		return CharTemplateHolder.getInstance().getTemplate(class_id, false).className;
	}
	
	private String page(Player player, String msg)
	{
		String htmltext = "";
		
		htmltext += title(player);
		if (player.getLevel() < Config.RANK_NPC_MIN_LEVEL)
			htmltext += "Minimum Level is: " + String.valueOf(Config.RANK_NPC_MIN_LEVEL);
		else
		{
			SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
			htmltext += "<br1><center>Next Update after: <font color=\"LEVEL\">" + sdf.format(rst.getNextUpdatingTime()) + " minutes</font></center><br1>";
			if (msg != "")
				htmltext += "<br1><font color=\"LEVEL\">" + msg + "</font>";
			htmltext += "<center><br1>";
			htmltext += "<table width=\"260\" align=\"center\">";
			if (!disablePage(1) && Config.RANK_NPC_LIST_ITEM.length != 0)
				htmltext += "<tr><td width=\"260\" align=\"center\">" + button("Items", "generatePage 1", 180, 21, 1, false) + "</td></tr>";
			if (!disablePage(2) && Config.RANK_NPC_LIST_CLASS.length != 0)
				htmltext += "<tr><td width=\"260\" align=\"center\">" + button("Olympics", "generatePage 2", 180, 21, 1, false) + "</td></tr>";
			if (!disablePage(3))
				htmltext += "<tr><td width=\"260\" align=\"center\">" + button("PvP/PK", "generatePage 3", 180, 21, 1, false) + "</td></tr>";
			htmltext += "</table>";
			htmltext += "</center>";
		}
		htmltext += footer();
		return htmltext;
	}
	
	private String generatePage(int option, Player player)
	{
		String htmltext = "";
		int pag = 1, top = 1;
		String event = "viewPage " + option;
		htmltext += title(player);
		htmltext += "<center>";
		htmltext += "<table width=\"260\" align=\"center\">";
		switch (option)
		{
			case 1:				
				for (Integer item_id : Config.RANK_NPC_LIST_ITEM)
					htmltext += "<tr><td width=\"260\" align=\"center\">" + button(getNameItem(item_id), event + " " + item_id + " " + pag + " " + top, 180, 21, 1, false) + "</td></tr>";
				break;
			case 2:
				for (Integer class_id : Config.RANK_NPC_LIST_CLASS)
					htmltext += "<tr><td width=\"260\" align=\"center\">" + button(getClassName(class_id), event + " " + class_id + " " + pag + " " + top, 180, 21, 1, false) + "</td></tr>";
				break;
			case 3:
				htmltext += "<tr><td width=\"260\" align=\"center\">" + button("PvP", "viewPage 3" + " 0" + " " + pag + " " + top, 180, 21, 1, false) + "</td></tr>";
				htmltext += "<tr><td width=\"260\" align=\"center\">" + button("PK", "viewPage 4" + " 1" + " " + pag + " " + top, 180, 21, 1, false) + "</td></tr>";
				break;
			default:
				htmltext = page(player, "Invalid option!");
				break;
		}
		htmltext += "</table><br>";
		htmltext += button("Back", "page", 100, 21, 4, false);
		htmltext += "</center>";
		htmltext += footer();
		return htmltext;
	}
	
	private String getTextTitle(int id, int type)
	{
		switch (type)
		{
			case RankSystemTable.ITEM: return String.valueOf(Config.RANK_NPC_ITEMS_RECORDS + " - " + getNameItem(id));
			case RankSystemTable.OLYMPIAD: return String.valueOf(Config.RANK_NPC_OLY_RECORDS + " - " + getClassName(id));
			case RankSystemTable.PVP: return String.valueOf(Config.RANK_NPC_PVP_RECORDS + " - PvP");
			case RankSystemTable.PK: return String.valueOf(Config.RANK_NPC_PK_RECORDS + " - PK");
			default: return String.valueOf("");
		}
	}
	
	private String viewItem(Player player, int type, int id, int pag)
	{
		String htmltext = "";
		htmltext += title(player);
		htmltext += "<center>";
		htmltext += "<table>";
		htmltext += "<tr><td align=\"center\">Rank TOP " + getTextTitle(id, type) + "</td></tr>";
		htmltext += "<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>";
		htmltext += "<tr><td>";
		
		htmltext += generateRankHtml(rst.getRankInformatioValues(type, id), type, id, pag);
		
		htmltext += "</td></tr>";
		htmltext += "<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>";
		htmltext += "</table>";
		htmltext += "<br>" + button("Back", "generatePage " + (type == 4 ? 3 : type), 100, 21, 4, false);
		htmltext += "</center>";
		htmltext += footer();
		return htmltext;
	}

	private String generateRankHtml(TreeSet<RankInformation> values, int type, int id, int pag)
	{
		String c1 = Config.RANK_NPC_COLOR_A;
		String c2 = Config.RANK_NPC_COLOR_B;
		Boolean c = true;
		int maxPerPage = 20;
		Integer top = (maxPerPage * (pag - 1)) + 1;
		String htmltext = "";
		
		if (!values.isEmpty())
		{
			int countPag = (int)Math.ceil((double)values.size() / (double) maxPerPage);
			int startRow = (maxPerPage * (pag - 1));
			int stopRow = (startRow + maxPerPage);
			int countReg = 0;
			String pages = generateButtonPage(countPag, type, id, pag);
			htmltext += pages;
			
			htmltext += "<table bgcolor=\"000000\">";
			htmltext += "<tr>";
			htmltext += "<td width=\"35\" align=\"center\">TOP</td>";
			htmltext += "<td width=\"100\" align=\"left\"><font color=\"F2FEBF\">Name</font></td>";
			htmltext += "<td width=\"125\" align=\"right\"><font color=\"00CC00\">Total</font></td>";
			htmltext += "</tr>";
			
			for (RankInformation ri : values)
			{
				if (ri == null) break;
				if (countReg >= stopRow) break;
				if (countReg >= startRow && countReg < stopRow)
				{
					String color = (c ? c1 : c2);
					String online = String.valueOf(top);
					Player activeChar = World.getPlayer(ri.getObjectId());
					if (activeChar != null)
						online = button(online, "generateInfo " + ri.getObjectId() + " " + type + " " + id + " " + pag, 30, 20, 6, c);
					Long amount = ri.getAmount();
					String total = (amount > 999 ? f.format(amount) : String.valueOf(amount));
					
					htmltext += "<tr>";
					htmltext += "<td width=\"35\" align=\"center\"><font color=\"" + color + "\">" + online + "</font></td>";
					htmltext += "<td width=\"100\" align=\"left\"><font color=\"" + color + "\">" + ri.getPlayerName() + "</font></td>";
					htmltext += "<td width=\"125\" align=\"right\"><font color=\"" + color + "\">" + total + "</font></td>";
					htmltext += "</tr>";

					c = !c;
					top++;
				}
				countReg++;
			}
			
			htmltext += "<tr>";
			htmltext += "<td height=\"3\" align=\"center\"> </td>";
			htmltext += "<td height=\"3\" align=\"center\"> </td>";
			htmltext += "<td height=\"3\" align=\"center\"> </td>";
			htmltext += "</tr>";
			htmltext += "</table>";
			
			htmltext += pages;
		}
		else
			htmltext += "<center>Has no information for that option.</center>";
		
		return htmltext;
	}

	private String generateButtonPage(int countPag, int type, int id, int pag)
	{
		if (countPag == 1) return "";
		String text = "";
		
		text += "<center><table><tr>";
		for (int i = 1; i <= countPag; i++)
		{
			text += "<td>" + button("P" + i, "viewPage " + type + " " + id + " " + i, 35, 20, 5, (i == pag ? true : false)) + "</td>";
			text += (i % 8 == 0 ? "</tr><tr>" : "");
		}
		text += "</tr></table></center>";
		return text;
	}
	
	public String viewInfoChar(Player player, int objectId, int type, int id, int pag)
	{
		Player activeChar = World.getPlayer(objectId);
		String htmltext = "", title = "", footer = "";
		
		title += "<table bgcolor=\"000000\">";
		title += "<tr>";
		title += "<td width=\"125\" align=\"left\"><font color=\"F2FEBF\">Desc.</font></td>";
		title += "<td width=\"125\" align=\"right\"><font color=\"00CC00\">Total</font></td>";
		title += "</tr>";
		
		footer += "<tr>";
		footer += "<td height=\"3\" align=\"center\"> </td>";
		footer += "<td height=\"3\" align=\"center\"> </td>";
		footer += "</tr>";
		footer += "</table><br>";
		
		htmltext += title(player);
		htmltext += "<center>";
		htmltext += "<table>";
		htmltext += "<tr><td align=\"center\">Character Information</td></tr>";
		htmltext += "<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>";
		htmltext += "<tr><td>";
		
		if (activeChar == null)
			htmltext += "<center>No Information! Sorry!</center>";
		else
		{	
			
			Boolean c;
			String c1 = Config.RANK_NPC_COLOR_A;
			String c2 = Config.RANK_NPC_COLOR_B;
			String yes = "<font color=\"79FF43\">Yes!</font>";
			String no = "<font color=\"FF0B00\">No!</font>";
			//MapRegionManager map = MapRegionManager.getInstance();
			PcInventory inv = activeChar.getInventory();
			
			c = true;
			htmltext += "Name: <font color=\""+c1+"\">" + activeChar.getName() + "</font><br1>";
			htmltext += "Level: <font color=\""+c1+"\">" + String.valueOf(activeChar.getLevel()) + "</font><br1>";
			if (activeChar.getClanId() != 0)
				htmltext += "Clan: <font color=\""+c1+"\">" + activeChar.getClan().getName() + "</font><br1>";
			if (activeChar.getAllyId() != 0)
				htmltext += "Ally: <font color=\""+c1+"\">" + activeChar.getClan().getAlliance().getAllyName() + "</font><br1>";
			//htmltext += "Last Town: <font color=\""+c1+"\">" + activeChar.getLastTownName() + "</font><br1>";
			//htmltext += "Current Known Location: <font color=\""+c1+"\">" + map.getClosestTownName(activeChar) + " Teritory</font>";
			
			htmltext += "<table align=\"center\" width=\"255\"><tr>";
			htmltext += "<td width=\"85\" align=\"center\">Is Noble?</td>";
			htmltext += "<td width=\"85\" align=\"center\">Is Hero?</td>";
			htmltext += "</tr><tr>";
			htmltext += "<td width=\"85\" align=\"center\">" + (activeChar.isNoble() ? yes : no) + "</td>";
			htmltext += "<td width=\"85\" align=\"center\">" + (activeChar.isHero() ? yes : no) + "</td>";
			htmltext += "</tr></table>";			
			
			c = true;
			htmltext += "<br1><b><font color=\"LEVEL\">ITEMS</font></b>";
			htmltext += title;
			for (Integer item_id : Config.RANK_NPC_LIST_ITEM)
			{
				String color = (c ? c1 : c2);		
				Long amount = inv.getItemByItemId(item_id).getCount();
				String total = (amount > 999 ? f.format(amount) : String.valueOf(amount));
				htmltext += "<tr>";
				htmltext += "<td align=\"left\"><font color=\"" + color + "\">" + getNameItem(item_id)+ "</font></td>";			
				htmltext += "<td align=\"right\"><font color=\"" + color + "\">" + total + "</font></td>";
				htmltext += "</tr>";
				c = !c;
			}
			
			htmltext += footer;
			
			c = true;
			htmltext += "<br1><b><font color=\"LEVEL\">PVP/PK</font></b>";
			htmltext += title;
			String color = (c ? c1 : c2);		
			int amount = activeChar.getPvpKills();
			String total = (amount > 999 ? f.format(amount) : String.valueOf(amount));
			htmltext += "<tr>";
			htmltext += "<td align=\"left\"><font color=\"" + color + "\">PvP</font></td>";			
			htmltext += "<td align=\"right\"><font color=\"" + color + "\">" + total + "</font></td>";
			htmltext += "</tr>";
			c = !c;
			color = (c ? c1 : c2);
			amount = activeChar.getPkKills();
			total = (amount > 999 ? f.format(amount) : String.valueOf(amount));
			htmltext += "<tr>";
			htmltext += "<td align=\"left\"><font color=\"" + color + "\">PK</font></td>";			
			htmltext += "<td align=\"right\"><font color=\"" + color + "\">" + total + "</font></td>";
			htmltext += "</tr>";
			htmltext += footer;			
		}
		htmltext += "</td></tr>";
		htmltext += "<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>";
		htmltext += "</table>";
		htmltext += "<br>" + button("Back", "viewPage " + type + " " + id + " " + pag, 100, 21, 4, false);
		htmltext += "</center>";
		htmltext += footer();
		return htmltext;
	}

	@Override
	public void onLoad()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReload()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShutdown()
	{
		// TODO Auto-generated method stub
		
	}
}