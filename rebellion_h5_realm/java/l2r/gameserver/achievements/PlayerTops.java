package l2r.gameserver.achievements;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.utils.TimeUtils;
import l2r.gameserver.utils.Util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javolution.util.FastTable;


public class PlayerTops
{
	public HashMap<String, List<TopScore>> _playersintop = new HashMap<String, List<TopScore>>();
	public HashMap<String, List<SeasonData>> _seasonstop = new HashMap<String, List<SeasonData>>();
	private static PlayerTops _instance;
	static NumberFormat _nf = NumberFormat.getInstance();

	public PlayerTops()
	{
		if (Config.ENABLE_PLAYER_COUNTERS)
		{
			_updateTimer.scheduleAtFixedRate(_updateTask, 1000, Config.PLAYER_COUNTERS_REFRESH * 60 * 1000);
			
			if (Config.PLAYER_TOP_MONTHLY_RANKING)
				generateSeasonData();
		}
	}

	private TimerTask _updateTask = new TimerTask()
	{
		@Override
		public void run()
		{
			update();
		}
	};
	
	private Timer _updateTimer = new Timer();

	public void usebypass(Player player, String bypass)
	{
		if(player == null)
			return;

		String[] cm = bypass.split(" ");
		
		if (bypass.equalsIgnoreCase("_bbsmemo"))
			showTop(player, 1);
		else if (bypass.startsWith("_bbsmemo;page"))
			showTop(player, Integer.parseInt(cm[1]));
		else if (bypass.startsWith("_bbsmemo;lifetimetops"))
			lifeTimeTop(player, cm[1]);
		else if (bypass.startsWith("_bbsmemo;halloffame") && Config.PLAYER_TOP_MONTHLY_RANKING)
			showSeasonTop(player, Integer.parseInt(cm[1]), Integer.parseInt(cm[2]));
		else
			showTop(player, 1);
	}

	public void showTop(Player player, int page)
	{
		if(player == null)
			return;

		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/playerTops.htm", player);
		// Generate used fields list.
		List<String> fieldNames = new ArrayList<String>();
		for (Field field : PlayerCounters.class.getFields())
		{
			switch (field.getName())
			// Fields that we dont use here.
			{
				case "_activeChar":
				case "_playerObjId":
				case "DUMMY_COUNTER":
				case "_Get_Married":
				case "_Longest_Kill_Spree":
				case "_Kill_Sprees_Ended":
				case "_Enchant_Item":
					continue;
				default:
					fieldNames.add(field.getName());
			}
		}
		
		int all = 0;
		boolean pagereached = false;
		int totalpages = (int) Math.round(fieldNames.size() / 3.0);
		
		if (page == 1)
		{
			if (totalpages == 1)
				html = html.replaceAll("%more%", "&nbsp;");
			else
				html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsmemo;page " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			html = html.replaceAll("%back%", "&nbsp;");
		}
		else if (page > 1)
		{
			if (totalpages <= page)
			{
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsmemo;page " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				html = html.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsmemo;page " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsmemo;page " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		if (Config.PLAYER_TOP_MONTHLY_RANKING)
			html = html.replaceAll("%nextseasondate%", "<td width=32 valign=top><font name=hs9>Next Season Starts after <font name=hs9 color=7B6E6E>" + TimeUtils.getConvertedTime((getEndSeasonTime().getTimeInMillis() - System.currentTimeMillis()) / 1000) + "</font></td>");
		else
			html = html.replaceAll("%nextseasondate%", "&nbsp;");
		
		html = html.replaceAll("%refresh%", "" + Config.PLAYER_COUNTERS_REFRESH);
		
		int ccCount = 0;
		int fieldCount = fieldNames.size();
		
		if (fieldCount % 2 == 1)
			fieldNames.add("");
		
		for (String type : fieldNames)
		{
			all++;
			if (page == 1 && ccCount > 3)
				continue;
			if (!pagereached && all > page * 3)
				continue;
			if (!pagereached && all <= (page - 1) * 3)
				continue;
			
			ccCount++;
			String[] color = getColor(ccCount);
			int count = 0;
			
			String name = type.replace("_", " ");
			
			html = html.replaceAll("%title" + ccCount + "%", name.trim());
			
			if (_playersintop.get(type) != null)
			{
				TopScore thispl = getPlayerInTop(player.getName(), type);
				for (TopScore pl : _playersintop.get(type))
				{
					count++;
					if (pl.getPlace() == 11 + 1)
						break;
					
					//Player plInstance = World.getPlayer(CharacterDAO.getInstance().getObjectIdByName(pl.getName()));
					
					if (thispl == null && pl.getPlace() == 11)
					{
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", count % 2 == 0 ? color[0] : color[1]);
						html = html.replaceAll("%" + ccCount + "InTop" + count + "%", "" + getImage(type, pl, player));
						html = html.replaceAll("%" + ccCount + "Name" + count + "%", getNameColor(type, pl, player));
						html = html.replaceAll("%" + ccCount + "Count" + count + "%", "" + (Util.isDigit(pl.getTop()) ? _nf.format(Integer.valueOf(pl.getTop())) : pl.getTop()));
						//html = html.replaceAll("%" + ccCount + "Online" + count + "%", (plInstance == null || !plInstance.isOnline()) ? "<button fore=\"L2UI_CH3.radar_target\" back=\"L2UI_CH3.radar_target\" width=\"8\" height=\"8\">" : "<button fore=\"L2UI_CH3.radar_party\" back=\"L2UI_CH3.radar_party\" width=\"8\" height=\"8\">");
						break;
					}
					if (thispl != null && pl.getPlace() == 11 && thispl.getPlace() > 11 - 1)
					{
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", color[2]);
						html = html.replaceAll("%" + ccCount + "InTop" + count + "%", "" + getImage(type, pl, player));
						html = html.replaceAll("%" + ccCount + "Name" + count + "%", getNameColor(type, pl, player));
						html = html.replaceAll("%" + ccCount + "Count" + count + "%", "" + (Util.isDigit(thispl.getTop()) ? _nf.format(Integer.valueOf(thispl.getTop())) : thispl.getTop()));
						//html = html.replaceAll("%" + ccCount + "Online" + count + "%", (plInstance == null || !plInstance.isOnline()) ? "<button fore=\"L2UI_CH3.radar_target\" back=\"L2UI_CH3.radar_target\" width=\"8\" height=\"8\">" : "<button fore=\"L2UI_CH3.radar_party\" back=\"L2UI_CH3.radar_party\" width=\"8\" height=\"8\">");
						break;
					}
					else if (thispl != null && pl.getName().equals(player.getName()))
					{
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", color[2]);
						html = html.replaceAll("%" + ccCount + "InTop" + count + "%", "" + getImage(type, pl, player));
						html = html.replaceAll("%" + ccCount + "Name" + count + "%", getNameColor(type, pl, player));
						html = html.replaceAll("%" + ccCount + "Count" + count + "%", "" + (Util.isDigit(thispl.getTop()) ? _nf.format(Integer.valueOf(thispl.getTop())) : thispl.getTop()));
						//html = html.replaceAll("%" + ccCount + "Online" + count + "%", (plInstance == null || !plInstance.isOnline()) ? "<button fore=\"L2UI_CH3.radar_target\" back=\"L2UI_CH3.radar_target\" width=\"8\" height=\"8\">" : "<button fore=\"L2UI_CH3.radar_party\" back=\"L2UI_CH3.radar_party\" width=\"8\" height=\"8\">");
					}
					else
					{
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", count % 2 == 0 ? color[0] : color[1]);
						html = html.replaceAll("%" + ccCount + "InTop" + count + "%", "" + getImage(type, pl, player));
						html = html.replaceAll("%" + ccCount + "Name" + count + "%", getNameColor(type, pl, player));
						html = html.replaceAll("%" + ccCount + "Count" + count + "%", "" + (Util.isDigit(pl.getTop()) ? _nf.format(Integer.valueOf(pl.getTop())) : pl.getTop()));
						//html = html.replaceAll("%" + ccCount + "Online" + count + "%", (plInstance == null || !plInstance.isOnline()) ? "<button fore=\"L2UI_CH3.radar_target\" back=\"L2UI_CH3.radar_target\" width=\"8\" height=\"8\">" : "<button fore=\"L2UI_CH3.radar_party\" back=\"L2UI_CH3.radar_party\" width=\"8\" height=\"8\">");
					}
				}
			}
			
			if (count < 12)
				for (int numeris = count + 1; numeris <= 11; numeris++)
				{
					html = html.replaceAll("%" + ccCount + "BG" + numeris + "%", numeris % 2 == 0 ? color[0] : color[1]);
					html = html.replaceAll("%" + ccCount + "InTop" + numeris + "%", "");
					html = html.replaceAll("%" + ccCount + "Name" + numeris + "%", "");
					html = html.replaceAll("%" + ccCount + "Count" + numeris + "%", "");
					html = html.replaceAll("%" + ccCount + "Online" + numeris + "%", "");
				}
		}
		ShowBoard.separateAndSend(html, player);
	}
	
	private String getImage(String type, TopScore pl, Player player)
	{
		TopScore finalPlayer = null;
		TopScore thispl = getPlayerInTop(player.getName(), type);
		
		String img = "";
		
		if (thispl == null && pl.getPlace() == 11)
			finalPlayer = pl;
		if (thispl != null && pl.getPlace() == 11 && thispl.getPlace() > 11 - 1)
			finalPlayer = thispl;
		else if (thispl != null && pl.getName().equals(player.getName()))
			finalPlayer = thispl;
		else
			finalPlayer = pl;
		
		if (finalPlayer.getPlace() < 4)
			img = "<img src=L2UI_CT1.MiniGame_DF_Text_Level_" + finalPlayer.getPlace() + " width=18 height=33>";
		else if (finalPlayer.getPlace() < 10)
			img = "<img src=L2UI_CT1.ShortcutWnd_DF_Number_f0" + finalPlayer.getPlace() + " width=12 height=12>";
		else if (finalPlayer.getPlace() > 9 && finalPlayer.getPlace() < 12)
			img = "<img src=L2UI_CT1.ShortcutWnd_DF_Number_f" + finalPlayer.getPlace() + " width=14 height=12>";
		else
			img = String.valueOf(finalPlayer.getPlace());
			
		return img;
	}
	
	private String getNameColor(String type, TopScore pl, Player player)
	{
		TopScore finalPlayer = null;
		TopScore thispl = getPlayerInTop(player.getName(), type);
		
		String name = "";
		
		if (thispl == null && pl.getPlace() == 11)
			finalPlayer = pl;
		if (thispl != null && pl.getPlace() == 11 && thispl.getPlace() > 11 - 1)
			finalPlayer = thispl;
		else if (thispl != null && pl.getName().equals(player.getName()))
			finalPlayer = thispl;
		else
			finalPlayer = pl;
		
		if (finalPlayer.getPlace() < 4)
			name = "<font name=hs12 color=D7DF01>" + finalPlayer.getName() + "</font>";
		else
			name = "<font color=bd4539>" + finalPlayer.getName() + "</font>";
			
		return name;
	}
	
	public void showSeasonTop(Player player, int season, int page)
	{
		if(player == null)
			return;

		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/playerSeasonTop.htm", player);
		
		// Generate used fields list.
		List<String> fieldNames = new ArrayList<String>();
		for (Field field : PlayerCounters.class.getFields())
		{
			switch (field.getName())
			// Fields that we dont use here.
			{
				case "_activeChar":
				case "_playerObjId":
				case "DUMMY_COUNTER":
				case "_Get_Married":
				case "_Longest_Kill_Spree":
				case "_Kill_Sprees_Ended":
				case "_Enchant_Item":
					continue;
				default:
					fieldNames.add(field.getName());
			}
		}
		StringBuilder sb = new StringBuilder();
		
		int totalSeason = ServerVariables.getInt("SeasonRanking", 0);
		
		sb.append("<td height=20 valign=center><font name=hs15 color=3E3E3F>Season </font></td>");
		
		for (int i=1; i <= totalSeason; i++)
		{
			if (season == i)
				sb.append("<td width=32 valign=top><button value=\"" + i + "\" action=\"bypass _bbsmemo;halloffame " + i + " 1\" width=27 height=32 back=\"L2UI_ct1.BuffFrame_24_3\" fore=\"L2UI_CT1.BuffFrame_24_3\"></td>");
			else
				sb.append("<td width=32 valign=top><button value=\"" + i + "\" action=\"bypass _bbsmemo;halloffame " + i + " 1\" width=27 height=32 back=\"L2UI_ct1.BuffFrame_24_2\" fore=\"L2UI_CT1.BuffFrame_24_2\"></td>");
		}
		
		if (totalSeason > 0)
			html = html.replaceAll("%seasonbutton%", sb.toString());
		else
			html = html.replaceAll("%seasonbutton%", "<td width=32 valign=top><font name=hs9>First Season will start after <font name=hs9 color=088A08>" + TimeUtils.getConvertedTime((getEndSeasonTime().getTimeInMillis() - System.currentTimeMillis()) / 1000) + "</font></td>");
		
		int all = 0;
		boolean pagereached = false;
		int totalpages = (int) Math.round(fieldNames.size() / 3.0);
		
		if (page == 1)
		{
			if (totalpages == 1)
				html = html.replaceAll("%more%", "&nbsp;");
			else
				html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsmemo;halloffame " + season + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			html = html.replaceAll("%back%", "&nbsp;");
		}
		else if (page > 1)
		{
			if (totalpages <= page)
			{
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsmemo;halloffame " + season + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				html = html.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbsmemo;halloffame " + season + " " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbsmemo;halloffame " + season + " " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		int ccCount = 0;
		int fieldCount = fieldNames.size();
		
		if (fieldCount % 2 == 1)
			fieldNames.add("");
		
		for (String type : fieldNames)
		{
			all++;
			if (page == 1 && ccCount > 3)
				continue;
			if (!pagereached && all > page * 3)
				continue;
			if (!pagereached && all <= (page - 1) * 3)
				continue;
			
			ccCount++;
			String[] color = getSeasonColor(ccCount);
			int count = 0;
			
			String name = type.replace("_", " ");
			
			html = html.replaceAll("%title" + ccCount + "%", name.trim());
			
			if (_seasonstop.get(type) != null)
			{
				for (SeasonData pl : _seasonstop.get(type))
				{
					if (pl.getSeason() != season)
						continue;
					
					count++;
					if (pl.getPlace() == 11)
						break;
					String plrname = CharacterDAO.getInstance().getNameByObjectId(pl.getObjectId());
					
					if (plrname != null && plrname.equals(player.getName()))
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", color[2]);
					else
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", count % 2 == 0 ? color[0] : color[1]);
					
					String img = "";
					if (pl.getPlace() < 4)
					{
						img = "<img src=L2UI_CT1.MiniGame_DF_Text_Level_" + pl.getPlace() + " width=18 height=32>";
						plrname = "<font name=hs12 color=31B404>" + plrname + "</font>";
					}
					else if (pl.getPlace() < 10)
						img = "<img src=L2UI_CT1.ShortcutWnd_DF_Number_f0" + pl.getPlace() + " width=12 height=12>";
					else
						img = "<img src=L2UI_CT1.ShortcutWnd_DF_Number_f" + pl.getPlace() + " width=14 height=12>";
					
					html = html.replaceAll("%" + ccCount + "InTop" + count + "%", img);
					html = html.replaceAll("%" + ccCount + "Name" + count + "%", plrname);
					html = html.replaceAll("%" + ccCount + "Count" + count + "%", "" + _nf.format(pl.getTop()));
				}
			}
			
			if (count < 12)
				for (int numeris = count + 1; numeris <= 11; numeris++)
				{
					html = html.replaceAll("%" + ccCount + "BG" + numeris + "%", numeris % 2 == 0 ? color[0] : color[1]);
					html = html.replaceAll("%" + ccCount + "InTop" + numeris + "%", "");
					html = html.replaceAll("%" + ccCount + "Name" + numeris + "%", "");
					html = html.replaceAll("%" + ccCount + "Count" + numeris + "%", "");
				}
		}
		ShowBoard.separateAndSend(html, player);
	}
	
	private Calendar getEndSeasonTime()
	{
		Date today = new Date();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DATE, 0);
		
		return calendar;
	}
	
	public TopScore getPlayerInTop(String name, String type)
	{
		if (name.isEmpty() || type.isEmpty())
			return null;
		
		for(TopScore pl : _playersintop.get(type))
			if(pl.getName() != null && pl.getName().equalsIgnoreCase(name))
				return pl;
		
		return null;
	}

	public void update()
	{
		_playersintop = new HashMap<>();
		
		// Generate lifetime top
		generateDataBy("pvpkills", 50);
		generateDataBy("pkkills", 50);
		generateDataBy("onlinetime", 50);
		
		// Generate used fields list.
		for (Field field : PlayerCounters.class.getFields())
		{
			switch (field.getName())
			{
				case "_activeChar":
				case "_playerObjId":
				case "DUMMY_COUNTER":
					continue;
				default:
					generateDataFromAcsBy(field.getName());
			}
		}
	}

	public List<String> getFieldList()
	{
		List<String> fieldNames = new ArrayList<String>();
		for (Field field : PlayerCounters.class.getFields())
		{
			switch (field.getName())
			{
				// Fields that we dont use here.
				case "_activeChar":
				case "_playerObjId":
				case "DUMMY_COUNTER":
				case "_Get_Married":
				case "_Longest_Kill_Spree":
				case "_Kill_Sprees_Ended":
				case "_Enchant_Item":
					continue;
				default:
					fieldNames.add(field.getName());
			}
		}
		
		return fieldNames;
	}

	public void generateDataBy(String type, int limit)
	{
		List<TopScore> _temp = new ArrayList<>();
		
		int i = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name," + type + " FROM characters WHERE " + type + " > 0 AND accessLevel >= 0 ORDER BY " + type + " DESC LIMIT " + limit + "");
			rset = statement.executeQuery();
			while(rset.next())
			{
				i++;
				String character_name = rset.getString("char_name");
				
				long intType = rset.getLong(type);
				String _type = "";
				if (type.equalsIgnoreCase("onlinetime"))
					_type = Util.formatTime((int) intType, 1);
				else
					_type = String.valueOf(intType);
				
				_temp.add(new TopScore(character_name, _type, i));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_playersintop.put(type, _temp);
	}
	
	public void lifeTimeTop(Player player, String type)
	{
		if (player == null)
			return;
		
		String result = "";
		String fullhtm = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/LifeTimeTop.htm", player);
		String one = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/LifeTimeTopO.htm", player);
		
		for (TopScore pl : _playersintop.get(type))
		{
			String bgcolor = pl.getPlace() % 2 == 0 ? "121618" : "070e13";
			String namecolor = "656FFD";
			if (pl != null && pl.getName().equals(player.getName()))
			{
				bgcolor = "676767";
				namecolor = "6BC00B";
			}
			
			result += one.replaceAll("%place%", "" + pl.getPlace()).replaceAll("%name%", pl.getName()).replaceAll("%count%", "" + pl.getTop()).replaceAll("%bg%", bgcolor).replaceAll("%namecolor%", namecolor);
		}
		
		fullhtm = fullhtm.replace("%tops%", result);
		
		if (getPlayerInTop(player.getName(), type) != null)
			fullhtm = fullhtm.replace("%yourPlace%", "You are on " + getPlayerInTop(player.getName(), type).getPlace() + " position!");
		else
			fullhtm = fullhtm.replace("%yourPlace%", "You are not in the top!");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(fullhtm);
		player.sendPacket(msg);
	}
	
	public void generateClanTop()
	{
		List<TopScore> _temp = new FastTable<>();
		
		int i = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id,reputation_score FROM clan_data where reputation_score > 0 ORDER BY reputation_score DESC LIMIT 50");
			rset = statement.executeQuery();
			while(rset.next())
			{
				i++;
				Clan clanId = ClanTable.getInstance().getClan(rset.getInt("clan_id"));
				String clan_name = clanId.getName();
				String _type = String.valueOf(rset.getInt("reputation_score"));
				_temp.add(new TopScore(clan_name, _type, i));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_playersintop.put("clan_top", _temp);
	}

	public void generateDataFromAcsBy(String type)
	{
		List<TopScore> _temp = new ArrayList<>();
		int i = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT " + type + ",char_name FROM character_counters LEFT JOIN characters ON ( character_counters.char_id = characters.obj_Id ) WHERE " + type + " > 0 AND accessLevel >= 0 ORDER BY " + type + " DESC");
			rset = statement.executeQuery();
			while(rset.next())
			{
				String character_name = rset.getString("char_name");
				String _type = String.valueOf(rset.getInt(type));
				
				if(character_name != null && !character_name.equals("") && !character_name.contains(" "))
				{
					i++;
					_temp.add(new TopScore(character_name, _type, i));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_playersintop.put(type, _temp);
	}

	public void generateSeasonData()
	{
		_seasonstop = new HashMap<>();
		for (String field : getFieldList())
			generateSeasonData(field);
	}
	
	private void generateSeasonData(String type)
	{
		List<SeasonData> _temp = new ArrayList<SeasonData>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM character_counters_monthly WHERE type=?");
			statement.setString(1, type);
			rset = statement.executeQuery();
			while(rset.next())
			{
				int season = rset.getInt("season");
				String wholedata = rset.getString("data");
				
				// If there is empty data for type skip it.
				if (wholedata.isEmpty())
					continue;
				
				String[] dataperplayer = wholedata.split(";");
				
				for (String data : dataperplayer)
				{
					String[] realdata = data.split(",");
					
					int objId = Integer.parseInt(realdata[0]);
					int place = Integer.parseInt(realdata[1]);
					long value = Long.valueOf(realdata[2]);
					
					_temp.add(new SeasonData(objId, season, place, value));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_seasonstop.put(type, _temp);
	}
	
	private String[] getColor(int cc)
	{

		switch(cc)
		{
			case 1:
				return new String[] { "0f100f", "090908", "2E2E2E" };
			case 2:
				return new String[] { "140504", "300a06", "2E2E2E" };
			case 3:
				return new String[] { "0f100f", "090908", "2E2E2E" };
		}
		return new String[] { "FFFFFF", "FFFFFF", "FFFFFF" };
	}
	
	private String[] getSeasonColor(int cc)
	{

		switch(cc)
		{
			case 1:
				return new String[] { "3B0B17", "071418", "A20505" };
			case 2:
				return new String[] { "300610", "030C0F", "A20505" };
			case 3:
				return new String[] { "3B0B17", "071418", "A20505" };
		}
		return new String[] { "FFFFFF", "FFFFFF", "FFFFFF" };
	}

	public static PlayerTops getInstance()
	{
		if(_instance == null)
			_instance = new PlayerTops();
		return _instance;
	}
	
	public class SeasonData
	{
		int _objId = 0;
		int _season = 0;
		long _topValue = 0;
		int _place = 0;
		
		public SeasonData(int objId, int season, int place, long value)
		{
			_objId = objId;
			_season = season;
			_place = place;
			_topValue = value;
		}
		
		public int getObjectId()
		{
			return _objId;
		}
		
		public long getSeason()
		{
			return _season;
		}
		
		public long getTop()
		{
			return _topValue;
		}
		
		public int getPlace()
		{
			return _place;
		}
	}
	
	public class TopScore
	{
		String _name = "NULL";
		String _topValue = "NULL";
		int _place = 0;
		
		public TopScore(String name, String topValue, int place)
		{
			_name = name;
			_place = place;
			_topValue = topValue;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public String getTop()
		{
			return _topValue;
		}
		
		public int getPlace()
		{
			return _place;
		}
	}
}