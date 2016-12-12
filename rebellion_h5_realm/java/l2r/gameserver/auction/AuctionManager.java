package l2r.gameserver.auction;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.ItemsDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SkillLearn;
import l2r.gameserver.model.base.AcquireType;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.ItemInstance.ItemLocation;
import l2r.gameserver.network.serverpackets.ExItemAuctionInfo;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PackageSendableList;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.item.CreateItem;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.TimeUtils;
import l2r.gameserver.utils.Util;
import l2r.gameserver.utils.ValueSortMap;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastTable;
import javolution.util.FastMap;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Midnex
 * @author Promo(htmls)
 * @reworked by Infern0, Nik
 *
 */
public class AuctionManager
{
	private static final Logger _log = LoggerFactory.getLogger(AuctionManager.class);
	
	static List<Auction> _auctions = new FastTable<>();
	private static TIntObjectHashMap<SortBy> _playerSortBy = new TIntObjectHashMap<>();
	private static TIntObjectHashMap<String> _playerSearch = new TIntObjectHashMap<>();
	static NumberFormat _nf = NumberFormat.getNumberInstance(Locale.US);
	
	private enum SortBy
	{
		LAST_BID("LastBid"),
		BUY_OUT_ASC("BuyOut(Ascending)"),
		BUY_OUT_DSC("BuyOut(Descending)"),
		ITEM_NAME_ASC("ItemName(Ascending)"),
		ITEM_NAME_DSC("ItemName(Descending)"),
		TIME_REMANING_ASC("TimeRemaning(Ascending)"),
		TIME_REMANING_DSC("TimeRemaning(Descending)"),
		BID_PRICE_ASC("BidPrice(Ascending)"),
		BID_PRICE_DSC("BidPrice(Descending)");
		
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
			
			return LAST_BID;
		}
	}

	public static void init() throws Exception
	{
		int auctionerObjId = ServerVariables.getInt("auctionCharacter", 0);
		
		if (auctionerObjId == 0)
		{
			_log.info("Auction: Missing dummy character for auction... creating it...");
			String name = RandomStringUtils.random(10, true, true);
			
			if (CharacterDAO.getInstance().accountExists(name))
			{
				_log.info("Auction: already existing account " + name + " another loop...");
				init();
				return;
			}
			
			if (CharacterDAO.getInstance().getObjectIdByName(name) != 0)
			{
				_log.info("Auction: already existing character name " + name + " another loop...");
				init();
				return;
			}
			
			createDummyAuctionCharacter(name);
		}
		
		_log.info("Auction: Dummy character exists.. continue loading data from sql.");
		restoreAuctions();
	}

	private static String encrypt(String password) throws Exception
	{
		AbstractChecksum checksum = JacksumAPI.getChecksumInstance("whirlpool2");
		checksum.setEncoding("BASE64");
		checksum.update(password.getBytes());
		return checksum.format("#CHECKSUM");
	}
	
	private static void createDummyAuctionCharacter(String name) throws Exception
	{
		String password = RandomStringUtils.random(7, true, true);
		
		String passwordHash = encrypt(password);
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO " + Config.LOGINSERVER_DB_NAME + ".accounts (login, password, access_level, allow_ip) VALUES(?,?,?,?)");
			statement.setString(1, name);
			statement.setString(2, passwordHash);
			statement.setInt(3, -100);
			statement.setString(4, "127.0.0.1");
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		
		Player plr = createNewCharcater(0, 0, name, name, 0, 0, 0);
		if (plr == null)
			init();
		
		ServerVariables.set("auctionCharacter", plr.getObjectId());
		_log.info("Auction: Dummy Character Created - " + plr.getName() + " [" + plr.getObjectId() + "]");
	}
	
	private static Player createNewCharcater(int classId, int sex, String accountName, String chrName, int hairStyle, int hairColor, int face)
	{
		Player newChar = Player.create(classId, sex, accountName, chrName, hairStyle, hairColor, face);
		if(newChar == null)
			return null;
		
		newChar.setCreateTime(1337); // Thats out special number :)
		
		Player.restoreCharSubClasses(newChar);

		if(Config.STARTING_ADENA > 0)
			newChar.addAdena(Config.STARTING_ADENA);
		
		if(Config.STARTING_LVL != 0)
			newChar.addExpAndSp(Experience.LEVEL[Config.STARTING_LVL] - newChar.getExp(), 0, 0, 0, false, false);

        if (Config.SPAWN_CHAR)
            newChar.teleToLocation(Config.SPAWN_X, Config.SPAWN_Y, Config.SPAWN_Z);
           else
           	newChar.setLoc(newChar.getTemplate().spawnLoc);

		if(Config.CHAR_TITLE)
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		else
			newChar.setTitle("");

		
		for(CreateItem i : newChar.getTemplate().getItems())
		{
			ItemInstance item = ItemFunctions.createItem(i.getItemId());
			newChar.getInventory().addItem(item);

			if(i.isEquipable() && item.isEquipable() && (newChar.getActiveWeaponItem() == null || item.getTemplate().getType2() != ItemTemplate.TYPE2_WEAPON))
				newChar.getInventory().equipItem(item);
		}

		for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(newChar, AcquireType.NORMAL))
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);

		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0);
		newChar.setOnlineStatus(false);

		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();
		
		return newChar;
	}
	
	public static void usebypass(Player player, String bypass)
	{
		if(bypass.equals("_bbs_Auction:getPlayerItems"))
		{
			if(player.isInJail())
			{
				player.sendMessage(new CustomMessage("l2r.gameserver.auction.auctionmanager.sealed", player));
				return;
			}
			
			if (Config.SECURITY_ENABLED && Config.SECURITY_TRADE_ENABLED && player.getSecurity())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "SECURITY", (player.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
			}
			
			getPlayerItemsForAuction(player);
		}
		else if(bypass.startsWith("_bbs_Auction:addItemToAuction"))
		{
			String[] cm = bypass.split(" ");
			
			if (Config.SECURITY_ENABLED && Config.SECURITY_TRADE_ENABLED && player.getSecurity())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "SECURITY", (player.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
			}
			
			if(cm.length < 6)
			{
				player.sendMessage(new CustomMessage("l2r.gameserver.auction.auctionmanager.cheat", player));
				return;
			}
			addItemToAuction(player, Integer.parseInt(cm[1]), Long.parseLong(cm[2]), Long.parseLong(cm[3]), Long.parseLong(cm[4]), Integer.parseInt(cm[5]));
		}
		else if(bypass.startsWith("_bbs_Auction:showAuction"))
		{
			String[] cm = bypass.split(" ");
			
			if (Config.SECURITY_ENABLED && Config.SECURITY_TRADE_ENABLED && player.getSecurity())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "SECURITY", (player.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
			}
			
			showItemAuction(player, Integer.parseInt(cm[1]));
		}
		else if(bypass.startsWith("_bbs_Auction:goto"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 3)
				getAllAuctions(player, Integer.parseInt(cm[1]));
		}
		else if(bypass.startsWith("_bbs_Auction:sortby"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length == 2)
				_playerSortBy.put(player.getObjectId(), SortBy.getEnum(cm[1]));
			
			getAllAuctions(player, 1);
		}
		else if(bypass.startsWith("_bbs_Auction:search"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length == 2)
				_playerSearch.put(player.getObjectId(), cm[1]);
			
			getAllAuctions(player, 1);
		}
		else if(bypass.startsWith("_bbs_Auction:resetsearch"))
		{
			_playerSearch.remove(player.getObjectId());
			
			getAllAuctions(player, 1);
		}
		else if(bypass.startsWith("_bbs_Auction "))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 3)
				getAllAuctions(player, Integer.parseInt(cm[1]));
		}
		else if(bypass.startsWith("_bbs_Auction:myauction"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 3)
				playerAuctionItems(player, Integer.parseInt(cm[1]));
		}
		else if(bypass.startsWith("_bbs_Auction:itemInfo"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 3)
				sendItemInfo(player, Integer.parseInt(cm[1]));
		}
		else if(bypass.startsWith("_bbs_Auction:removeAuction"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 4)
				removeAuction(player, Integer.parseInt(cm[1]), Integer.parseInt(cm[2]), false);
		}
		/*
		else if(bypass.startsWith("_bbs_Auction:itemlink"))
		{
			String[] cm = bypass.split(" ");
			if(cm.length < 3)
				sendItemLink(player, Integer.parseInt(cm[1]));
		}
		*/
		else
			getAllAuctions(player, 1);
	}

	public static void getAllAuctions(Player pl, int page)
	{
		String htmltosend = HtmCache.getInstance().getNotNull("CustomAuction/auctions.htm", pl);
		List<Auction> auctions = getFilteredAuctions(_playerSearch.get(pl.getObjectId()));
		
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = (int) Math.round(auctions.size() / 12.0 + 1);
		
		StringBuilder sb = new StringBuilder();
		SortBy sortBy = _playerSortBy.get(pl.getObjectId());
		
		String tmp = "";
		if (sortBy != null)
			sb.append(tmp = (sortBy.toString() + ";"));
		
		for (String s : new String[]{"LastBid;", "ItemName(Ascending);", "ItemName(Descending);", "TimeRemaning(Ascending);", "TimeRemaning(Descending);", "BidPrice(Ascending);", "BidPrice(Descending);"})
		{
			if (!s.equalsIgnoreCase(tmp))
				sb.append(s);
		}
		
		htmltosend = htmltosend.replaceAll("%sortbylist%", sb.toString());

		if(page == 1)
		{
			if(totalpages == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbs_Auction:goto " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbs_Auction:goto " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbs_Auction:goto " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbs_Auction:goto " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for(Auction auc : getSorttedAuctions(_playerSortBy.get(pl.getObjectId()), auctions))
		{
			all++;
			if(page == 1 && clansvisual > 12)
				continue;
			if(!pagereached && all > page * 12)
				continue;
			if(!pagereached && all <= (page - 1) * 12)
				continue;
			
			clansvisual++;

			ItemInstance item = auc.getItem();

			String name = item.getName();
			name = name.replaceAll("Recipe: ", "");
			if(name.length() > 40)
			{
				name = name.substring(0, name.length() - (name.length() - 40));
				name += "...";
			}
			name += item.getEnchantLevel() > 0 ? "&nbsp;<font color=\"LEVEL\">+" + item.getEnchantLevel() + "</font>" : "";
			
			long buyoutprice = auc.getBuyOutPrice();
			
			if (auc.isGolden())
			{
				htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + item.getTemplate().getIcon());
				htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"FF8A21\">" + name + "</font>" + (item.getCount() > 1 ? "&nbsp;&nbsp;<font color=\"5b574c\">x" + item.getCount() + "</font>" : ""));
				
				// fliped with %buyout
				if (buyoutprice > 0)
					htmltosend = htmltosend.replaceAll("%minimalbid" + clansvisual + "%", "<font color=LEVEL>Buyout:</font> <font color=\"E41B17\">" + _nf.format(buyoutprice) + "</font> adena.");
				else
					htmltosend = htmltosend.replaceAll("%minimalbid" + clansvisual + "%", "");
				
				htmltosend = htmltosend.replaceAll("%timeleft" + clansvisual + "%", "- <font color=\"0099FF\">" + TimeUtils.minutesToFullString((int) Math.max(auc.getTimeLeft() / 60000, 0), false, false, true, true) + "</font>");
				htmltosend = htmltosend.replaceAll("%button" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbs_Auction:showAuction " + auc.getAuctionId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
				htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "290");
				htmltosend = htmltosend.replaceAll("%auctionerName" + clansvisual + "%", "<font color=6f76a6>Black Market</font>");
				htmltosend = htmltosend.replaceAll("%itemInfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbs_Auction:itemInfo " + item.getObjectId() + "\" width=16 height=16 back=\"L2UI_CH3.aboutotpicon\" fore=\"L2UI_CH3.aboutotpicon_over\">");
				
				// fliped with %minimalbid
				htmltosend = htmltosend.replaceAll("%buyout" + clansvisual + "%", "<font color=LEVEL>Bid:</font> <font color=\"ad9d46\">" + _nf.format(auc.getMaxBid()) + "</font> adena.");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%icon" + clansvisual + "%", "icon." + item.getTemplate().getIcon());
				htmltosend = htmltosend.replaceAll("%name" + clansvisual + "%", "<font color=\"ad9d46\">" + name + "</font>" + (item.getCount() > 1 ? "&nbsp;&nbsp;<font color=\"5b574c\">x" + item.getCount() + "</font>" : ""));
			
				// fliped with %buyout
				if (buyoutprice > 0)
					htmltosend = htmltosend.replaceAll("%minimalbid" + clansvisual + "%", "Buyout: <font color=\"E41B17\">" + _nf.format(buyoutprice) + "</font> adena.");
				else
					htmltosend = htmltosend.replaceAll("%minimalbid" + clansvisual + "%", "");
				
				htmltosend = htmltosend.replaceAll("%timeleft" + clansvisual + "%", "- <font color=\"0099FF\">" + TimeUtils.minutesToFullString((int) Math.max(auc.getTimeLeft() / 60000, 0), false, false, true, true) + "</font>");
				htmltosend = htmltosend.replaceAll("%button" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbs_Auction:showAuction " + auc.getAuctionId() + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\">");
				htmltosend = htmltosend.replaceAll("%width" + clansvisual + "%", "290");
				htmltosend = htmltosend.replaceAll("%auctionerName" + clansvisual + "%", "<font color=\"FF293D\">[" + CharacterDAO.getInstance().getNameByObjectId(auc.getSellerId()) + "]</font>");
				htmltosend = htmltosend.replaceAll("%itemInfo" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbs_Auction:itemInfo " + item.getObjectId() + "\" width=16 height=16 back=\"L2UI_CH3.aboutotpicon\" fore=\"L2UI_CH3.aboutotpicon_over\">");
				
				// fliped with %minimalbid
				htmltosend = htmltosend.replaceAll("%buyout" + clansvisual + "%", "Bid: <font color=\"ad9d46\">" + _nf.format(auc.getMaxBid()) + "</font> adena.");
				
			}
			
			//ItemInfoCache.getInstance().put(item);
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				htmltosend = htmltosend.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				htmltosend = htmltosend.replaceAll("%name" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%minimalbid" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%buyout" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%timeleft" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%button" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%width" + d + "%", "136");
				htmltosend = htmltosend.replaceAll("%auctionerName" + d + "%", "&nbsp;");
				htmltosend = htmltosend.replaceAll("%itemInfo" + d + "%", "&nbsp;");
			}
		}

			
		htmltosend = htmltosend.replaceAll("%allauctions%", "" + auctions.size());
		ShowBoard.separateAndSend(htmltosend, pl);
	}
	
	private static List<Auction> getFilteredAuctions(String filter)
	{
		if (filter == null)
			filter = "";
		
		List<Auction> filteredList = new FastTable<>();
		
		for (Auction auction : _auctions)
		{
			if (auction.getItem().getName().toLowerCase().contains(filter.toLowerCase()))
				filteredList.add(auction);
		}
		
		return filteredList;
	}

	private static List<Auction> getPlayerListAuctions(Player player)
	{
		List<Auction> list = new FastTable<>();
		
		for (Auction auction : _auctions)
		{
			if (auction.getSellerId() == (player.getObjectId()))
				list.add(auction);
		}
		
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Auction> getSorttedAuctions(SortBy sort, List<Auction> auctions)
	{
		if (sort == null)
			sort = SortBy.LAST_BID;
		
		List<Auction> sorted = new FastTable<>();
		
		// Check for golden auctions
		for (Auction auction : auctions)
		{
			if (auction.isGolden())
			{
				sorted.add(auction); // Golden auctions always appear first on the list.
				auctions.remove(auction);
			}
		}
		
		switch(sort)
		{
			default:
			case LAST_BID:
				List<Auction> notSortedValues = new FastTable<>();
				notSortedValues.addAll(auctions);
				Auction storedid = null;
				int lastpoints = 0;
				
				while(notSortedValues.size() > 0)
				{
					if(sorted.size() == auctions.size())
						break;

					for(Auction cplayer : notSortedValues)
						if(cplayer.getAllBidds() >= lastpoints)
						{
							storedid = cplayer;
							lastpoints = cplayer.getAllBidds();
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
			case BUY_OUT_DSC:
				Map<Auction, Long> tmp7 = new FastMap<>();
				for (Auction auction: auctions)
					tmp7.put(auction, auction.getBuyOutPrice());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp7, true).keySet());
				return sorted;
			case BUY_OUT_ASC:
				Map<Auction, Long> tmp8 = new FastMap<>();
				for (Auction auction: auctions)
					tmp8.put(auction, auction.getBuyOutPrice());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp8, false).keySet());
				return sorted;
			case ITEM_NAME_ASC:
				Map<Auction, String> tmp = new FastMap<>();
				for (Auction auction: auctions)
					tmp.put(auction, auction.getItem().getTemplate().getName());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp, true).keySet());
				return sorted;
			case ITEM_NAME_DSC:
				Map<Auction, String> tmp2 = new FastMap<>();
				for (Auction auction: auctions)
					tmp2.put(auction, auction.getItem().getTemplate().getName());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp2, false).keySet());
				return sorted;
			case TIME_REMANING_ASC:
				Map<Auction, Long> tmp3 = new FastMap<>();
				for (Auction auction: auctions)
					tmp3.put(auction, auction.getTimeLeft());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp3, true).keySet());
				return sorted;
			case TIME_REMANING_DSC:
				Map<Auction, Long> tmp4 = new FastMap<>();
				for (Auction auction: auctions)
					tmp4.put(auction, auction.getTimeLeft());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp4, false).keySet());
				return sorted;
			case BID_PRICE_ASC:
				Map<Auction, Long> tmp5 = new FastMap<>();
				for (Auction auction: auctions)
					tmp5.put(auction, auction.getMaxBid());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp5, true).keySet());
				return sorted;
			case BID_PRICE_DSC:
				Map<Auction, Long> tmp6 = new FastMap<>();
				for (Auction auction: auctions)
					tmp6.put(auction, auction.getMaxBid());
				
				sorted.addAll(ValueSortMap.sortMapByValue(tmp6, false).keySet());
				return sorted;
		}
	}

	public static void getPlayerItemsForAuction(Player pl)
	{
		if (pl == null)
			return;
		
		pl.sendPacket(new PackageSendableList(pl.getObjectId(), pl));
	}

	public static void itemSlectedForAuction(Player pl, int objid, long count)
	{
		if (pl == null)
			return;
		
		String html = HtmCache.getInstance().getNotNull("CustomAuction/setItemBid.htm", pl);

		//GameObject obj = GameObjectsStorage.getAsItem(objid);
		//ItemInfo obj = ItemInfoCache.getInstance().get(objid);
		ItemInstance obj = pl.getInventory().getItemByObjectId(objid);
		if (obj == null || !(obj.isItem()))
		{
			_log.warn("Auction: player " + pl.getName() + " tryed to select Non-item or nulled item to add it at Auction!");
			return;
		}
		
		ItemInstance item = obj;

		String name = item.getName();
		name = name.replaceAll("Recipe: ", "");
		if(name.length() > 25)
		{
			name = name.substring(0, name.length() - (name.length() - 25));
			name += "...";
		}

		html = html.replaceAll("%name%", "<font color=\"af9f47\">" + name + "</font>&nbsp;&nbsp;<font color=\"5b574c\">x" + count + "</font>");
		html = html.replaceAll("%icon%", "icon." + item.getTemplate().getIcon());
		html = html.replaceAll("%itemObjId%", "" + item.getObjectId());
		html = html.replaceAll("%count%", "" + count);

		NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
		npcHtml.setHtml(html);
		pl.sendPacket(npcHtml);
	}

	public static void playerAuctionItems(Player player, int page)
	{
		String html = HtmCache.getInstance().getNotNull("CustomAuction/myauctions.htm", player);
		
		List<Auction> auctions = getPlayerListAuctions(player);
		
		int all = 0;
		int clansvisual = 0;
		boolean pagereached = false;
		int totalpages = (int) (Math.round(auctions.size()) / 12.0 + 1);
		
		if(page == 1)
		{
			if(totalpages == 1)
				html = html.replaceAll("%more%", "&nbsp;");
			else
				html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbs_Auction:myauction " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			html = html.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbs_Auction:myauction " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				html = html.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbs_Auction:myauction " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbs_Auction:myauction " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		for (Auction auc : getPlayerListAuctions(player))
		{
			all++;
			if(page == 1 && clansvisual > 12)
				continue;
			if(!pagereached && all > page * 12)
				continue;
			if(!pagereached && all <= (page - 1) * 12)
				continue;
			
			clansvisual++;
			String name = auc.getItem().getTemplate().getName();
			String icon = auc.getItem().getTemplate().getIcon();
			if (icon == null || icon.equals(StringUtils.EMPTY))
				icon = "icon.etc_question_mark_i00";
			long maxBid = auc.getMaxBid();
			long buyoutprice = auc.getBuyOutPrice();
			
			name = name.replaceAll("Recipe: ", "");
			if(name.length() > 40)
			{
				name = name.substring(0, name.length() - (name.length() - 40));
				name += "...";
			}
			
			name += auc.getItem().getEnchantLevel() > 0 ? "&nbsp;<font color=\"LEVEL\">+" + auc.getItem().getEnchantLevel() + "</font>" : "";
			
			html = html.replaceAll("%icon" + clansvisual + "%", "icon." + icon);
			html = html.replaceAll("%name" + clansvisual + "%", "<font color=\"af9f47\">" + name + "</font>" + (auc.getItem().getCount() > 1 ? "&nbsp;&nbsp;<font color=\"5b574c\">x" + auc.getItem().getCount() + "</font>" : ""));
			html = html.replaceAll("%timeleft" + clansvisual + "%", "Time left: <font color=\"0099FF\">" + TimeUtils.minutesToFullString((int) Math.max(auc.getTimeLeft() / 60000, 0), false, false, true, true) + "</font>");
			html = html.replaceAll("%maxbid" + clansvisual + "%", "Bid: <font color=\"6AA121\">" + Util.convertToLineagePriceFormat(maxBid) + "</font>");
			html = html.replaceAll("%buyout" + clansvisual + "%", "Buyout: <font color=\"3EA99F\">" + Util.convertToLineagePriceFormat(buyoutprice) + "</font>");
			if (auc.getAllBidds() == 0)
			{
				html = html.replaceAll("%width" + clansvisual + "%", "220");
				html = html.replaceAll("%button" + clansvisual + "%", "<button value=\"\" action=\"bypass _bbs_Auction:removeAuction " + auc.getAuctionId() + " " + page + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Over\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\">");
			}
			else
			{
				html = html.replaceAll("%width" + clansvisual + "%", "350");
				html = html.replaceAll("%button" + clansvisual + "%", "&nbsp;");
			}
		}

		if(clansvisual < 12)
		{
			for(int d = clansvisual + 1; d != 13; d++)
			{
				html = html.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				html = html.replaceAll("%name" + d + "%", "&nbsp;");
				html = html.replaceAll("%maxbid" + d + "%", "&nbsp;");
				html = html.replaceAll("%button" + d + "%", "&nbsp;");
				html = html.replaceAll("%timeleft" + d + "%", "&nbsp;");
				html = html.replaceAll("%width" + d + "%", "138");
				html = html.replaceAll("%itemInfo" + d + "%", "&nbsp");
				html = html.replaceAll("%buyout" + d + "%", "&nbsp;");
			}
		}
		
		html = html.replaceAll("%allauctions%", "" + auctions.size());
		
		ShowBoard.separateAndSend(html, player);
	}
	
	private static void showItemAuction(Player player, int auctionid)
	{
		Auction auc = getAuctionById(auctionid);

		if(auc != null)
			player.sendPacket(new ExItemAuctionInfo(auc.getAuctionId(), false));
	}

	public static Auction getAuctionById(int auctionid)
	{
		for(Auction auc : _auctions)
			if(auc.getAuctionId() == auctionid)
				return auc;
		return null;
	}

	public static Auction getAuctionByItemOID(int itemObj)
	{
		for(Auction auc : _auctions)
			if(auc.getItem().getObjectId() == itemObj)
				return auc;
		return null;
	}
	
	public static Auction getAuctionByPlayer(Player player)
	{
		for(Auction auc : _auctions)
			if(auc.getSellerId() == player.getObjectId())
				return auc;
		return null;
	}
	
	public static synchronized void addItemToAuction(Player requester, int objid, long count, long startBid, long buyoutPrice, int hours)
	{
		if (buyoutPrice > 0 && buyoutPrice < startBid)
		{
			requester.sendMessage("Buyout price must be HIGHER than the bid price!");
			return;
		}
		
		if (startBid <= 0 || buyoutPrice < 0)
		{
			_log.warn("Auction: " + requester.getName() + " trying to put negative startingbid or buyoutprice for auction.");
			return;
		}
		
		if (hours < 0 || count < 0)
		{
			_log.warn("Auction: " + requester.getName() + " trying to put negative time or count for auction.");
			return;
		}
		
		ItemInstance item = requester.getInventory().getItemByObjectId(objid);
		if(item == null)
		{
			NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
			npcHtml.setHtml("<html><body><center>An error has occured.</center></body></html>");
			requester.sendPacket(npcHtml);
			
			_log.warn("Auction: " + requester.getName() + " trying to put null item at Auction. Possible cheat!");
			return;
		}
			

		int ownerId = item.getOwnerId();
		if(ownerId == 0 || ownerId != requester.getObjectId())
		{
			_log.warn("Auction: " + requester.getName() + " trying to put item with ownerId == 0. Possible cheat!");
			return;
		}

		/* LETS DISABLE IT...
		double tax = hours / 24 / 100; // Divide by 24 and then divide by 100 to turn it into %.
		tax *= startBid; // Now get those % from the initial bid as tax.
		if (tax < 1)
			tax = 1;
		
		if(requester.getInventory().reduceAdena((long) tax))
		{
			// No Idea if this work....
			Castle nearestCastle = ResidenceHolder.getInstance().getResidenceByCoord(Castle.class, requester.getX(), requester.getY(), requester.getZ(), requester.getReflection());
			if (nearestCastle != null && requester.getCastle() != null && requester.getCastle().getId() > 0)
				nearestCastle.addToTreasuryNoTax((long) tax, false, false);
		}
		else
		{
			requester.sendMessage(new CustomMessage("l2r.gameserver.auction.auctionmanager.tax", requester, tax));
			return;
		}
		
		*/
		
		int auctionerObjId = ServerVariables.getInt("auctionCharacter", 0);
		if(auctionerObjId == 0)
		{
			_log.warn("Auction: There was an error with the auction keeper. His objid is 0");
			return;
		}
		
		if(item.getCount() >= count)
		{
			requester.getInventory().writeLock();
			try
			{
				ItemInstance items = requester.getInventory().removeItemByObjectId(item.getObjectId(), count);
				//items.dropToTheGround(requester, Location.findPointToStay(requester, 100));
				items.setOwnerId(auctionerObjId);
				items.setLocation(ItemLocation.LEASE);
				if(items.getJdbcState().isSavable())
				{
					items.save();
				}
				else
				{
					items.setJdbcState(JdbcEntityState.UPDATED);
					items.update();
				}
				
				requester.sendPacket(new InventoryUpdate().addModifiedItem(items));

				int auctionid = IdFactory.getInstance().getNextId();
				Auction auction = new Auction(items, auctionid, requester.getObjectId(), startBid, buyoutPrice,  requester.getObjectId(), System.currentTimeMillis() + hours * 3600000, requester.isGM()); // requester.isGM() = golden item
				
				_auctions.add(auction);
				updateAuction(auctionid);
				
				//ItemInfoCache.getInstance().put(items);
				
				StringBuilder sb = new StringBuilder();
				sb.append("<html><body>");
				sb.append("<title>Auction item added.</title>");
				sb.append("<center>");
				sb.append("<br>You have added successfully <font color=\"FFFF00\">" + (count > 1 ? count + " " : "") + item.getName() + "</font> to sell by auction.<br><br>");
				sb.append("<br>Bid Price: " + startBid + " <br> Buyout Price: " + buyoutPrice + "");
				sb.append("<br></center>");
				sb.append("</body></html>");
				
				Log.auction("Auction with id: " + auctionid + " itemId: " + item.getItemId() + " itemCount: " + item.getCount() + " Bid Price: " + startBid + " Buyout Price: " + buyoutPrice + " has been ADDED to the auction from " + requester.getName());
				
				
				NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setHtml(sb.toString());
				requester.sendPacket(html);
				
				getAllAuctions(requester, 1);
			}
			finally
			{
				requester.getInventory().writeUnlock();
			}
		}
		else
		{
			requester.sendMessage(new CustomMessage("l2r.gameserver.auction.auctionmanager.cheat2", requester));
			_log.warn("Auction: " + requester.getName() + " trying to put item with less count. Possible Cheat!");
			return;
		}
	}

	public static void restoreAuctions()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM custom_auction");
			rs = statement.executeQuery();
			while(rs.next())
			{
				int itemObj = rs.getInt("item_obj");
				ItemInstance item = ItemsDAO.getInstance().load(itemObj);
					if(item != null)
					{
						_auctions.add(new Auction(item, rs.getInt("auction_id"), rs.getInt("seller_id"), rs.getLong("max_bid"), rs.getLong("buyout_price"), rs.getInt("last_bider"), rs.getLong("auction_end"), rs.getInt("isgolden") == 1));
						//ItemInfoCache.getInstance().put(item);
					}
			}
				
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
		_log.info("Auction: Loaded " + _auctions.size() + " items for auction.");
	}

	public synchronized static void updateAuction(int auctionId)
	{
		Auction auc = getAuctionById(auctionId);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO custom_auction (auction_id,seller_id,item_obj,max_bid,buyout_price,last_bider,auction_end,all_bids,isgolden) values(?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, auc.getAuctionId());
			statement.setInt(2, auc.getSellerId());
			statement.setInt(3, auc.getItem().getObjectId());
			statement.setLong(4, auc.getMaxBid());
			statement.setLong(5, auc.getBuyOutPrice());
			statement.setInt(6, auc.getLastBidder());
			statement.setLong(7, auc.getEndTime());
			statement.setLong(8, auc.getAllBidds());
			statement.setInt(9, auc.isGolden() ? 1 : 0);
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/* no idea why its not working... debug when i have more time...
	private static void sendItemLink(Player player, int itemobjectId)
	{
		player = player.getClient().getConnection().getClient().getActiveChar();
		ItemInfo item = ItemInfoCache.getInstance().get(itemobjectId);
		if (item != null)
			player.getClient().getConnection().getClient().sendPacket(new ExRpItemLink(item));
	}
	*/ 
	
	private static void sendItemInfo(Player player, int itemobjectId)
	{
		String html = HtmCache.getInstance().getNotNull("CustomAuction/itemInfo.htm", player);
		
		Auction auc = getAuctionByItemOID(itemobjectId);
		
		if (auc == null)
			return;
		
		ItemInstance item = auc.getItem();
		
		html = html.replaceAll("%seller%", String.valueOf(CharacterDAO.getInstance().getNameByObjectId(auc.getSellerId())));
		html = html.replaceAll("%icon%", "icon." + item.getTemplate().getIcon());
		html = html.replaceAll("%minimalbid%", _nf.format(auc.getMaxBid()));
		html = html.replaceAll("%buyOutPrice%", auc.getBuyOutPrice() > 0 ? "Buyout: " + _nf.format(auc.getBuyOutPrice()) : "Only Bids");
		html = html.replaceAll("%timeleft%", "" + Math.max(auc.getTimeLeft() / 60000, 0));
		
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
		
		getAllAuctions(player, 1);
	}
	
	public synchronized static void removeAuction(Player player, int aucId, int page, boolean admin)
	{
		Auction auc = getAuctionById(aucId);
		if (auc == null)
			return;
		
		if (!admin && auc.getAllBidds() != 0)
		{
			_log.warn("Player " + player.getName() + " trying to cancel auction whil it have bidders on it!!! Aucid : " + auc.getAuctionId());
			return;
		}
		
		auc.removePlayerAuction(page, admin);
		Log.auction(player == null ? "Admin removed auction with id: " + aucId + "" : "Player " + player.getName() + " remove Auc: " + auc.getAuctionId() + " from the auction.");
		
	}
	
	public synchronized static void deleteAuction(int _auctionId)
	{
		Auction auc = getAuctionById(_auctionId);
		_auctions.remove(auc);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM custom_auction WHERE auction_id=" + _auctionId);
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
