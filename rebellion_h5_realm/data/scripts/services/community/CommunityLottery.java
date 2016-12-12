package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunityLottery implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityLottery.class);
	private static int total_games;
	private static int day_games;
	private static long jackpot;
	private static Winner winner = new Winner();

	@Override
	public void onLoad()
	{
		if(Config.BBS_GAME_LOTTERY_ALLOW)
		{
			_log.info("CommunityBoard: Lottery games loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
			restoreLotteryData();
			_log.info("CommunityBoard: Lottery games played " + Util.formatAdena(getTotalGames()) + ".");
			restoreJackpot();
			_log.info("CommunityBoard: Lottery jackpot is " + Util.formatAdena(jackpot) + " " + Util.getItemName(Config.BBS_GAME_LOTTERY_ITEM) + ".");
			restoreWinnerData();
			
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new storeLotteryData(), 60000, Config.BBS_GAME_LOTTERY_STORE_DATA);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.BBS_GAME_LOTTERY_ALLOW)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{
		storeLotteryData();
		storeJackpot();
		restoreWinnerData();
	}

	private class storeLotteryData implements Runnable
	{
		@Override
		public void run()
		{
			storeLotteryData();
			storeJackpot();
		}
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbslottery" };
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/lottery.htm", player);
		
		if(bypass.startsWith("_bbslottery"))
		{
			boolean win = false;
			boolean index = false;
			boolean win_jackpot = false;
			String[] cmd = bypass.split(":");
			
			if(cmd[1].equals("play"))
			{
				int price = Config.BBS_GAME_LOTTERY_BET[Integer.parseInt(cmd[2])];
				if(ItemFunctions.removeItem(player, Config.BBS_GAME_LOTTERY_ITEM, price, true) > 0)
				{
					increaseDayGames();
					if(Rnd.chance(Config.BBS_GAME_LOTTERY_WIN_CHANCE))
					{
						win = true;
						Functions.addItem(player, Config.BBS_GAME_LOTTERY_ITEM, price * Config.BBS_GAME_LOTTERY_REWARD_MULTIPLER);
						if(Rnd.chance((Integer.parseInt(cmd[2]) + 1) * Config.BBS_GAME_LOTTERY_JACKPOT_CHANCE))
						{
							win_jackpot = true;
							Functions.addItem(player, Config.BBS_GAME_LOTTERY_ITEM, jackpot);
							updateWinner(jackpot, player.getName());
							player.broadcastPacket(new MagicSkillUse(player, player, 6234, 1, 1000, 0));

							String[] params =
							{
								player.getName(),
								Util.formatAdena(jackpot),
								Util.getItemName(Config.BBS_GAME_LOTTERY_ITEM)
							};
							
							Util.sayToAll("scripts.services.communityboard.games.lottery.jackpot.announce", params);
							Log.addGame(player.getName() + " has won the ackpot of " + jackpot + " " + Util.getItemName(Config.BBS_GAME_LOTTERY_ITEM) + "", "CommunityBoardLottery");
							resetJackpot();
						}
					}
					else
						setJackpot(price * Config.BBS_GAME_LOTTERY_AMOUNT_PERCENT_TO_JACKPOT / 100);
				}
				else
					player.sendMessage("Not enough items.");
			}
			else if(cmd[1].equals("index"))
				index = true;
			else if(cmd[1].equals("winner"))
			{
				index = true;
				Functions.show(showWinnerPage(player), player, null);
			}

			html = html.replace("<?lottery_result?>", index ? new CustomMessage("scripts.services.communityboard.games.lottery.bet.set", player).toString() : win ? new CustomMessage("scripts.services.communityboard.games.lottery.win", player).toString() : new CustomMessage("scripts.services.communityboard.games.lottery.loose", player).toString());
			html = html.replace("<?lottery_button?>", button(player));
			html = html.replace("<?lottery_jackpot?>", win_jackpot ? new CustomMessage("scripts.services.communityboard.games.lottery.jackpot.win", player).toString() : jackpot >= Integer.MAX_VALUE ? Util.formatAdena(jackpot) + " MAX" : Util.formatAdena(jackpot));
			html = html.replace("<?lottery_game_all?>", Util.formatAdena(getTotalGames()));
			html = html.replace("<?lottery_game_day?>", Util.formatAdena(getDayGames()));

		}
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>" + new CustomMessage("scripts.services.communityboard.notdone", player).addString(bypass) + "</center><br><br></body></html>", player);

		ShowBoard.separateAndSend(html, player);
	}

	public static String button(Player player)
	{
		StringBuilder html = new StringBuilder();
		for(int i = 1; i <= Config.BBS_GAME_LOTTERY_BET.length; i++)
		{
			html.append("<td>");
			html.append("<button action=\"bypass _bbslottery:play:" + (i - 1) + "\" value=\"" + new CustomMessage("scripts.services.communityboard.games.lottery.bet", player).addString(Util.formatAdena(Config.BBS_GAME_LOTTERY_BET[i - 1])).toString() + "\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\">");
			html.append("</td>");
			html.append(i % 3 == 0 ? "</tr><tr>" : "");
		}

		return html.toString();
	}

	private static class Winner
	{
		public int[] count = new int[10];
		public String[] name = new String[10];
	}

	private String showWinnerPage(Player player)
	{
		StringBuilder html = new StringBuilder();

		html.append("<html noscrollbar>");
		html.append("<title>" + new CustomMessage("scripts.services.communityboard.games.lottery.top.win.title", player).toString() + "</title>");
		html.append("<body>");
		html.append("<table border=0 cellpadding=0 cellspacing=0 width=292 height=358 background=\"l2ui_ct1.Windows_DF_TooltipBG\">");
		html.append("<tr>");
		html.append("<td valign=top>");
		html.append("<table width=280 align=center height=25>");
		html.append("<tr>");
		html.append("<td valign=top width=10></td>");
		html.append("<td valign=top width=120><br>");
		html.append("<table height=20 bgcolor=808080>");
		html.append("<tr>");
		html.append("<td width=100 align=center>");
		html.append(new CustomMessage("common.name", player).toString());
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td>");
		html.append("<td valign=top width=174><br>");
		html.append("<table height=20 bgcolor=808080>");
		html.append("<tr>");
		html.append("<td width=160 align=center>");
		html.append(Util.getItemName(Config.BBS_GAME_LOTTERY_ITEM));
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");

		int colorN = 0;
		String[] color = new String[] { "333333", "666666" };

		for(int i = 0; i < 10; i++)
		{
			if(winner.name[i] != null)
			{
				if(colorN > 1)
					colorN = 0;

				html.append("<table width=280 align=center height=20>");
				html.append("<tr>");
				html.append("<td valign=top width=10></td>");
				html.append("<td valign=top width=120><br>");
				html.append("<table height=20 bgcolor=" + color[colorN] + ">");
				html.append("<tr>");
				html.append("<td width=10 align=left>");
				html.append("<font color=610B21>" + (i + 1) + "</font>");
				html.append("</td>");
				html.append("<td width=100 align=center>");
				html.append("<font color=B59A75>" + winner.name[i] + "</font>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("<td valign=top width=174><br>");
				html.append("<table height=20 bgcolor=" + color[colorN] + ">");
				html.append("<tr>");
				html.append("<td width=160 align=center>");
				html.append("<font color=LEVEL>" + Util.formatAdena(winner.count[i]) + "</font>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				colorN++;
			}
			else
			{
				if(colorN > 1)
					colorN = 0;

				html.append("<table width=280 align=center height=20>");
				html.append("<tr>");
				html.append("<td valign=top width=10></td>");
				html.append("<td valign=top width=120><br>");
				html.append("<table height=20 bgcolor=" + color[colorN] + ">");
				html.append("<tr>");
				html.append("<td width=10 align=left>");
				html.append("&nbsp;");
				html.append("</td>");
				html.append("<td width=100 align=center>");
				html.append("&nbsp;");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("<td valign=top width=174><br>");
				html.append("<table height=20 bgcolor=" + color[colorN] + ">");
				html.append("<tr>");
				html.append("<td width=160 align=center>");
				html.append("&nbsp;");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				colorN++;
			}
		}

		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</body>");
		html.append("</html>");

		return html.toString();
	}

	private void restoreWinnerData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int counter = 0;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `bbs_lottery` WHERE `type`='winner' ORDER BY `count` DESC LIMIT 0,10");
			rset = statement.executeQuery();

			while(rset.next())
			{
				winner.count[counter] = rset.getInt("count");
				winner.name[counter] = rset.getString("name");
				counter++;
			}
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not restore lottery winner: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private boolean restoreJackpot()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `count` FROM `bbs_lottery` WHERE `type`='jackpot'");
			rset = statement.executeQuery();

			if(rset.next())
				jackpot = rset.getInt("count");
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not restore lottery jackpot: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return true;
	}

	private boolean restoreLotteryData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `count` FROM `bbs_lottery` WHERE `type`='total_games'");
			rset = statement.executeQuery();

			if(rset.next())
				total_games = rset.getInt("count");
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not restore lottery games: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return true;
	}

	private void updateWinner(long count, String name)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `bbs_lottery` (`count`, `type`, `name`) VALUES (" + count + ", 'winner', '" + name + "');");
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not increase current lottery winner: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void storeJackpot()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE `bbs_lottery` SET `count`=" + jackpot + " WHERE `type`='jackpot'");
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not increase current lottery jackpot: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void storeLotteryData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE `bbs_lottery` SET `count`=" + getTotalGames() + " WHERE `type`='total_games'");
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not increase current lottery games: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private int getTotalGames()
	{
		return total_games + day_games;
	}

	private int getDayGames()
	{
		return day_games;
	}

	private void increaseDayGames()
	{
		day_games++;
	}

	private void setJackpot(int count)
	{
		if(jackpot + count >= Long.MAX_VALUE)
			jackpot = Long.MAX_VALUE;
		else
			jackpot = jackpot + count;
	}

	private void resetJackpot()
	{
		jackpot = Config.BBS_GAME_LOTTERY_INITIAL_JACKPOT;
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}
}