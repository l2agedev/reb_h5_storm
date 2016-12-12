/*
package l2r.gameserver.instancemanager;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;


public class VoteRewardManager
{
	private static boolean _hasVotedHop;
	private static boolean _hasVotedTop;
	
	public static void load()
	{
		TriesResetTask.getInstance();
		MonthlyResetTask.getInstance();
	}
	
	protected static int getHopZoneVotes()
	{
		int votes = -1;
		
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		
		try
		{
			url = new URL(Config.VOTE_LINK_HOPZONE);
			con = url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);
			in = new BufferedReader(isr);
			String inputLine;
			
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("no steal make love") || inputLine.contains("no votes here") || inputLine.contains("bang, you don't have votes") || inputLine.contains("la vita e bella"))
					votes = Integer.valueOf(inputLine.split(">")[2].replace("</span", "")).intValue();
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			is.close();
			isr.close();
			in.close();
		}
		
		return votes;
	}
	
	private static int getTopZoneVotes()
	{
		int votes = -1;
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(Config.VOTE_LINK_TOPZONE);
			con = url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);
			in = new BufferedReader(isr);
			
			String inputLine;
			
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("Votes"))
				{
					String votesLine = in.readLine();
					
					votes = Integer.valueOf(votesLine.split(">")[5].replace("</font", "")).intValue();
				}
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return votes;
	}
	
	public static String hopCd(Player player)
	{
		long hopCdMs = 0L;
		long voteDelay = 43200000L;
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT lastVoteHopzone FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			
			while (rset.next())
				hopCdMs = rset.getLong("lastVoteHopzone");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
		
		Date resultdate = new Date(hopCdMs + voteDelay);
		return sdf.format(resultdate);
	}
	
	public static String topCd(Player player)
	{
		long topCdMs = 0L;
		long voteDelay = 43200000L;
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT lastVoteTopzone FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			
			while (rset.next())
				topCdMs = rset.getLong("lastVoteTopzone");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
		
		Date resultdate = new Date(topCdMs + voteDelay);
		return sdf.format(resultdate);
	}
	
	public static String whosVoting()
	{
		for (Player voter : GameObjectsStorage.getAllPlayersForIterate())
			if (voter != null && voter.isVoting())
				return voter.getName();
		
		return "None";
	}
	
	public static void setVotedHopzone(final Player player)
	{
		long lastVoteHopzone = 0L;
		long voteDelay = 43200000L;
		
		final int firstvoteshop = getHopZoneVotes();
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT lastVoteHopzone FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			
			while (rset.next())
				lastVoteHopzone = rset.getLong("lastVoteHopzone");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		if (getTries(player) <= 0)
		{
			player.sendMessage("Due to your multiple failures in voting you lost your chance to vote today");
		}
		else if (lastVoteHopzone + voteDelay < System.currentTimeMillis() && getTries(player) > 0)
		{
			for (Player plr : GameObjectsStorage.getAllPlayersForIterate())
			{
				if (plr.isVoting())
				{
					player.sendMessage("Someone is already voting.Wait for your turn please!");
					return;
				}
			}
			
			player.setIsVoting(true);
			player.sendMessage("Go fast on the site and vote on the hopzone banner!");
			player.sendMessage("You have " + Config.SECS_TO_VOTE + " seconds.Hurry!");
			
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run()
				{
					if (firstvoteshop < getHopZoneVotes())
					{
						player.setIsVoting(false);
						setHasVotedHop(player);
						player.sendMessage("Thank you for voting for us!");
						updateLastVoteHopzone(player);
						updateVotes(player);
					}
					else
					{
						player.setIsVoting(false);
						player.sendMessage("You did not vote.Please try again.");
						setTries(player, getTries(player) - 1);
					}
				}
			}, Config.SECS_TO_VOTE * 1000);
		}
		else if (getTries(player) <= 0 && lastVoteHopzone + voteDelay < System.currentTimeMillis())
		{
			for (Player plr : GameObjectsStorage.getAllPlayersForIterate())
			{
				if (plr.isVoting())
				{
					player.sendMessage("Someone is already voting.Wait for your turn please!");
					return;
				}
			}
			
			player.setIsVoting(true);
			player.sendMessage("Go fast on the site and vote on the hopzone banner!");
			player.sendMessage("You have " + Config.SECS_TO_VOTE + " seconds.Hurry!");
			
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run()
				{
					if (firstvoteshop < getHopZoneVotes())
					{
						player.setIsVoting(false);
						setHasVotedHop(player);
						player.sendMessage("Thank you for voting for us!");
						updateLastVoteHopzone(player);
						updateVotes(player);
					}
					else
					{
						player.setIsVoting(false);
						player.sendMessage("You did not vote.Please try again.");
						setTries(player, getTries(player) - 1);
					}
				}
			}, Config.SECS_TO_VOTE * 1000);
		}
		else
		{
			player.sendMessage("12 hours have to pass till you are able to vote again.");
		}
	}
	
	public static void setVotedTopzone(final Player player)
	{
		long lastVoteTopzone = 0L;
		long voteDelay = 43200000L;
		
		final int firstvotestop = getTopZoneVotes();
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT lastVoteTopzone FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			
			while (rset.next())
				lastVoteTopzone = rset.getLong("lastVoteTopzone");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		if (getTries(player) <= 0)
		{
			player.sendMessage("Due to your multiple failures in voting you lost your chance to vote today");
		}
		else if (getTries(player) <= 0 && lastVoteTopzone + voteDelay < System.currentTimeMillis())
		{
			for (Player plr : GameObjectsStorage.getAllPlayersForIterate())
			{
				if (plr.isVoting())
				{
					player.sendMessage("Someone is already voting.Wait for your turn please!");
					return;
				}
			}
			
			player.setIsVoting(true);
			player.sendMessage("Go fast on the site and vote on the topzone banner!");
			player.sendMessage("You have " + Config.SECS_TO_VOTE + " seconds.Hurry!");
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				public void run()
				{
					if (firstvotestop < getTopZoneVotes())
					{
						player.setIsVoting(false);
						setHasVotedTop(player);
						player.sendMessage("Thank you for voting for us!");
						updateLastVoteTopzone(player);
						updateVotes(player);
					}
					else
					{
						player.setIsVoting(false);
						player.sendMessage("You did not vote.Please try again.");
						setTries(player, getTries(player) - 1);
					}
				}
			}, Config.SECS_TO_VOTE * 1000);
		}
		else if (lastVoteTopzone + voteDelay < System.currentTimeMillis() && getTries(player) > 0)
		{
			for (Player plr : GameObjectsStorage.getAllPlayersForIterate())
			{
				if (plr.isVoting())
				{
					player.sendMessage("Someone is already voting.Wait for your turn please!");
					return;
				}
			}
			player.setIsVoting(true);
			player.sendMessage("Go fast on the site and vote on the topzone banner!");
			player.sendMessage("You have " + Config.SECS_TO_VOTE + " seconds.Hurry!");
			
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run()
				{
					if (firstvotestop < getTopZoneVotes())
					{
						player.setIsVoting(false);
						setHasVotedTop(player);
						player.sendMessage("Thank you for voting for us!");
						updateLastVoteTopzone(player);
						updateVotes(player);
					}
					else
					{
						player.setIsVoting(false);
						player.sendMessage("You did not vote.Please try again.");
						setTries(player, getTries(player) - 1);
					}
				}
			}, Config.SECS_TO_VOTE * 1000);
		}
		else
		{
			player.sendMessage("12 hours have to pass till you are able to vote again.");
		}
	}
	
	public static boolean hasVotedHop(Player player)
	{
		int hasVotedHop = -1;
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT hasVotedHop FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			
			while (rset.next())
				hasVotedHop = rset.getInt("hasVotedHop");
			
			if (hasVotedHop == 1)
			{
				setHasVotedHop(true);
				return true;
			}
			
			if (hasVotedHop == 0)
			{
				setHasVotedHop(false);
				return false;
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return false;
	}
	
	public static boolean hasVotedTop(Player player)
	{
		int hasVotedTop = -1;
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT hasVotedTop FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			
			while (rset.next())
				hasVotedTop = rset.getInt("hasVotedTop");
			
			if (hasVotedTop == 1)
			{
				setHasVotedTop(true);
				return true;
			}
			
			if (hasVotedTop == 0)
			{
				setHasVotedTop(false);
				return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return false;
	}
	
	public static void updateVotes(Player activeChar)
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET monthVotes=?, totalVotes=? WHERE obj_Id=?");
			statement.setInt(1, getMonthVotes(activeChar) + 1);
			statement.setInt(2, getTotalVotes(activeChar) + 1);
			statement.setInt(3, activeChar.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void setHasVotedHop(Player activeChar)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET hasVotedHop=1 WHERE obj_Id=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement,);
		}
	}
	
	public static void setHasVotedTop(Player activeChar)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET hasVotedTop=1 WHERE obj_Id=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void setHasNotVotedHop(Player activeChar)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET hasVotedHop=0 WHERE obj_Id=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void setHasNotVotedTop(Player activeChar)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET hasVotedTop=0 WHERE obj_Id=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static int getTries(Player player)
	{
		int tries = -1;
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT tries FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			
			while (rset.next())
				tries = rset.getInt("tries");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return tries;
	}
	
	public static void setTries(Player player, int tries)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET tries=? WHERE obj_Id=?");
			statement.setInt(1, tries);
			statement.setInt(2, player.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static int getMonthVotes(Player player)
	{
		int monthVotes = -1;
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT monthVotes FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while (rset.next())
				monthVotes = rset.getInt("monthVotes");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return monthVotes;
	}
	
	public static int getTotalVotes(Player player)
	{
		int totalVotes = -1;
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT totalVotes FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			
			while (rset.next())
				totalVotes = rset.getInt("totalVotes");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return totalVotes;
	}
	
	public static int getBigTotalVotes()
	{
		int bigTotalVotes = -1;
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT SUM(totalVotes) FROM characters");
			rset = statement.executeQuery();
			
			while (rset.next())
				bigTotalVotes = rset.getInt("SUM(totalVotes)"); // getInt(1) ??
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return bigTotalVotes;
	}
	
	public static int getBigMonthVotes()
	{
		int bigMonthVotes = -1;
		
		Connection con = null;
		ResultSet rset = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT SUM(monthVotes) FROM characters");
			rset = statement.executeQuery();
			
			while (rset.next())
				bigMonthVotes = rset.getInt("SUM(monthVotes)"); // getInt(1) ??
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return bigMonthVotes;
	}
	
	public static void updateLastVoteHopzone(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET lastVoteHopzone=? WHERE obj_Id=?");
			statement.setLong(1, System.currentTimeMillis());
			statement.setInt(2, player.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void updateLastVoteTopzone(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET lastVoteTopzone=? WHERE obj_Id=?");
			statement.setLong(1, System.currentTimeMillis());
			statement.setInt(2, player.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static boolean hasVotedHop()
	{
		return _hasVotedHop;
	}
	
	public static void setHasVotedHop(boolean hasVotedHop)
	{
		_hasVotedHop = hasVotedHop;
	}
	
	public static boolean hasVotedTop()
	{
		return _hasVotedTop;
	}
	
	public static void setHasVotedTop(boolean hasVotedTop)
	{
		_hasVotedTop = hasVotedTop;
	}
}
*/