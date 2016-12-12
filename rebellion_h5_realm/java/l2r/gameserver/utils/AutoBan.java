package l2r.gameserver.utils;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Announcements;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.database.mysql;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AutoBan
{
	private static final Logger _log = LoggerFactory.getLogger(AutoBan.class);

	public static String getBanReason(int ObjectId)
	{
		String reason = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT reason FROM bans WHERE obj_Id=?");
			statement.setInt(1, ObjectId);
			rset = statement.executeQuery();

			if(rset.next())
				reason = rset.getString("reason");
		}
		catch(Exception e)
		{
			_log.warn("Could not select ban data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return reason;
	}
	
	public static String getBannedBy(int ObjectId)
	{
		String bannedby = "";
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT GM FROM bans WHERE obj_Id=?");
			statement.setInt(1, ObjectId);
			rset = statement.executeQuery();

			if(rset.next())
				bannedby = rset.getString("GM");
		}
		catch(Exception e)
		{
			_log.warn("Could not restore ban data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return bannedby;
	}
	
	public static long getEndBanDate(int ObjectId)
	{
		long endban = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT endban FROM bans WHERE obj_Id=?");
			statement.setInt(1, ObjectId);
			rset = statement.executeQuery();

			if(rset.next())
				endban = rset.getLong("endban");
		}
		catch(Exception e)
		{
			_log.warn("Could not select ban data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return endban;
	}
	
	public static boolean isBanned(int ObjectId)
	{
		boolean res = false;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT MAX(endban) AS endban FROM bans WHERE obj_Id=? AND endban IS NOT NULL");
			statement.setInt(1, ObjectId);
			rset = statement.executeQuery();

			if(rset.next())
			{
				Long endban = rset.getLong("endban") * 1000L;
				res = endban > System.currentTimeMillis();
			}
		}
		catch(Exception e)
		{
			_log.warn("Could not restore ban data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return res;
	}

	// usable for hours
	public static void Banned(Player actor, int day, int hour, int minute, String msg, String GM)
	{
		int endban = 0;
		if(day == -1 || hour == -1 || minute == -1)
			endban = Integer.MAX_VALUE;
		else if(day > 0 || hour > 0 || minute > 0)
		{
			Calendar end = Calendar.getInstance();
			if (day > 0)
				end.add(Calendar.DAY_OF_MONTH, day);
			if (hour > 0)
				end.add(Calendar.HOUR_OF_DAY, hour);
			if (minute > 0)
				end.add(Calendar.MINUTE, minute);
			
			endban = (int) (end.getTimeInMillis() / 1000);
		}
		else
		{
			_log.warn("Negative ban period: " + day + hour + minute);
			return;
		}

		String date = new SimpleDateFormat("dd.MM.yy H:mm:ss").format(new Date());
		String enddate = new SimpleDateFormat("dd.MM.yy H:mm:ss").format(new Date(endban * 1000L));
		if(endban * 1000L <= Calendar.getInstance().getTimeInMillis())
		{
			_log.warn("Negative ban period | From " + date + " to " + enddate);
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO bans (account_name, obj_id, baned, unban, reason, GM, endban) VALUES(?,?,?,?,?,?,?)");
			statement.setString(1, actor.getAccountName());
			statement.setInt(2, actor.getObjectId());
			statement.setString(3, date);
			statement.setString(4, enddate);
			statement.setString(5, msg);
			statement.setString(6, GM);
			statement.setLong(7, endban);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could not store bans data:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void Banned(Player actor, int period, String msg, String GM)
	{
		int endban = 0;
		if(period == -1)
			endban = Integer.MAX_VALUE;
		else if(period > 0)
		{
			Calendar end = Calendar.getInstance();
			end.add(Calendar.DAY_OF_MONTH, period);
			endban = (int) (end.getTimeInMillis() / 1000);
		}
		else
		{
			_log.warn("Negative ban period: " + period);
			return;
		}

		String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
		String enddate = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date(endban * 1000L));
		if(endban * 1000L <= Calendar.getInstance().getTimeInMillis())
		{
			_log.warn("Negative ban period | From " + date + " to " + enddate);
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO bans (account_name, obj_id, baned, unban, reason, GM, endban) VALUES(?,?,?,?,?,?,?)");
			statement.setString(1, actor.getAccountName());
			statement.setInt(2, actor.getObjectId());
			statement.setString(3, date);
			statement.setString(4, enddate);
			statement.setString(5, msg);
			statement.setString(6, GM);
			statement.setLong(7, endban);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could not store bans data:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	//offline
	public static boolean Banned(String actor, int acc_level, int period, String msg, String GM)
	{
		boolean res;
		int obj_id = CharacterDAO.getInstance().getObjectIdByName(actor);
		res = obj_id > 0;
		if(!res)
			return false;

		Connection con = null;
		PreparedStatement statement = null;
		Connection con2 = null;
		PreparedStatement statement2 = null;
		Connection con3 = null;
		PreparedStatement statement3 = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE obj_Id=?");
			statement.setInt(1, acc_level);
			statement.setInt(2, obj_id);
			statement.executeUpdate();
			DbUtils.closeQuietly(con ,statement);

			if(acc_level < 0)
			{
				int endban = 0;
				if(period == -1)
					endban = Integer.MAX_VALUE;
				else if(period > 0)
				{
					Calendar end = Calendar.getInstance();
					end.add(Calendar.DAY_OF_MONTH, period);
					endban = (int) (end.getTimeInMillis() / 1000);
				}
				else
				{
					_log.warn("Negative ban period: " + period);
					return false;
				}

				String date = new SimpleDateFormat("dd.MM.yy H:mm:ss").format(new Date());
				String enddate = new SimpleDateFormat("dd.MM.yy H:mm:ss").format(new Date(endban * 1000L));
				if(endban * 1000L <= Calendar.getInstance().getTimeInMillis())
				{
					_log.warn("Negative ban period | From " + date + " to " + enddate);
					return false;
				}

				con2 = DatabaseFactory.getInstance().getConnection();
				statement2 = con2.prepareStatement("INSERT INTO bans (obj_id, baned, unban, reason, GM, endban) VALUES(?,?,?,?,?,?)");
				statement2.setInt(1, obj_id);
				statement2.setString(2, date);
				statement2.setString(3, enddate);
				statement2.setString(4, msg);
				statement2.setString(5, GM);
				statement2.setLong(6, endban);
				statement2.execute();
			}
			else
			{
				con3 = DatabaseFactory.getInstance().getConnection();
				statement3 = con3.prepareStatement("DELETE FROM bans WHERE obj_id=?");
				statement3.setInt(1, obj_id);
				statement3.execute();
				res = true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			_log.warn("could not store bans data:" + e);
			res = false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
			DbUtils.closeQuietly(con2, statement2);
			DbUtils.closeQuietly(con3, statement3);
		}

		return res;
	}

	public static void Karma(Player actor, int karma, String msg, String GM)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			String date = new SimpleDateFormat("dd.MM.yy H:mm:ss").format(new Date());
			msg = "Add karma(" + karma + ") " + msg;
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO bans (account_name, obj_id, baned, reason, GM) VALUES(?,?,?,?,?)");
			statement.setString(1, actor.getAccountName());
			statement.setInt(2, actor.getObjectId());
			statement.setString(3, date);
			statement.setString(4, msg);
			statement.setString(5, GM);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could not store bans data:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static void Banned(Player actor, int period, String msg)
	{
		Banned(actor, period, msg, "AutoBan");
	}

	public static boolean ChatBan(String actor, int period, String msg, String GM)
	{
		boolean res = true;
		long NoChannel = period * 60000;
		int obj_id = CharacterDAO.getInstance().getObjectIdByName(actor);
		if(obj_id == 0)
			return false;
		Player plyr = World.getPlayer(actor);

		Connection con = null;
		PreparedStatement statement = null;
		if(plyr != null)
		{

			plyr.sendMessage(new CustomMessage("l2r.Util.AutoBan.ChatBan", plyr).addString(GM).addNumber(period));
			plyr.updateNoChannel(NoChannel);
		}
		else
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET nochannel = ? WHERE obj_Id=?");
				statement.setLong(1, NoChannel > 0 ? NoChannel / 1000 : NoChannel);
				statement.setInt(2, obj_id);
				statement.executeUpdate();
			}
		catch(Exception e)
		{
			res = false;
			_log.warn("Could not activate nochannel:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		return res;
	}

	public static boolean ChatUnBan(String actor, String GM)
	{
		boolean res = true;
		Player plyr = World.getPlayer(actor);
		int obj_id = CharacterDAO.getInstance().getObjectIdByName(actor);
		if(obj_id == 0)
			return false;

		Connection con = null;
		PreparedStatement statement = null;
		if(plyr != null)
		{
			plyr.sendMessage(new CustomMessage("l2r.Util.AutoBan.ChatUnBan", plyr).addString(GM));
			plyr.updateNoChannel(0);
		}
		else
			try
		{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET nochannel = ? WHERE obj_Id=?");
				statement.setLong(1, 0);
				statement.setInt(2, obj_id);
				statement.executeUpdate();
		}
		catch(Exception e)
		{
			res = false;
			_log.warn("Could not activate nochannel:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		return res;
	}
	public static boolean RemoveFromJail(String actor, Player GM)
	{
		Player player = World.getPlayer(actor);
		if(player != null) // чар в мире
		{
			if(player.getVar("jailed") == null)
			{
				GM.sendMessage(GM.isLangRus() ? "Попытка unjail без тюрьме характер." : "Trying to unjail a non-jailed character.");
				return false;
			}

			String[] re = player.getVar("jailedFrom").split(";");
			player.teleToLocation(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
			player.setReflection(re.length > 3 ? Integer.parseInt(re[3]) : 0);
			player.stopUnjailTask();
			player.unsetVar("jailedFrom");
			player.unsetVar("jailed");
			player.unblock();
			player.standUp();

			return true;
		}
		else
		{
			int objId = CharacterDAO.getInstance().getObjectIdByName(actor);
			if (objId == 0)
			{
				GM.sendMessage(GM.isLangRus() ? "Персонаж не найден." : "Char not found.");
				return false;
			}

			String jailed = CharacterDAO.getInstance().getUserVar(objId, "jailed");
			if(jailed == null)
			{
				GM.sendMessage(GM.isLangRus() ? "Попытка unjail без тюрьме персонаж." : "Trying to unjail a non-jailed character.");
				return false;
			}

			CharacterDAO.getInstance().deleteUserVar(objId, "jailed");

			String[] re = CharacterDAO.getInstance().getUserVar(objId, "jailedFrom").split(";");
			CharacterDAO.getInstance().setDbLocatio(objId, Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));

			return true;
		}
	}

	public static boolean Jail(String actor, int period, String msg, Player GM)
	{
		if(period < 1)
		{
			period = -1;
		}

		Player player = World.getPlayer(actor);
		if (player != null) // чар в мире
		{
			
			doJailPlayer(player, period * 60 * 1000L, true);
			
			if (period > 0)
			{
				String per = TimeUtils.minutesToFullString(period);
				Announcements.getInstance().announceToAll("Player " + player.getName() + " jailed for " + per);
				
				// пм от админа
				player.sendPacket(new Say2(0, ChatType.TELL, "Administration", "You go to jail for " + per + " , reason: " + msg));
			}
			else
			{
				Announcements.getInstance().announceToAll("Player " + player.getName() + " has been jailed permanently.");
				
				// пм от админа
				player.sendPacket(new Say2(0, ChatType.TELL, "Administration", "You are put in jail indefinitely, the reason: " + msg));
			}
			
			return true;
		}
		else
		{
			int objId = CharacterDAO.getInstance().getObjectIdByName(actor);
			if (objId == 0)
			{
				GM.sendMessage(GM.isLangRus() ? "Персонаж не найден." : "Char not found.");
				return false;
			}

			Location loc = CharacterDAO.getInstance().getLocation(objId);
			if(loc == null)
			{
				GM.sendMessage(GM.isLangRus() ? "Персонаж местоположение не был загружен." : "Char location was not loaded.");
				return false;
			}

			if(period > 0)
			{
				String per = TimeUtils.minutesToFullString(period);
				Announcements.getInstance().announceToAll("Player " + actor + " jailed for " + per);
			}
			else
			{
				Announcements.getInstance().announceToAll("Player " + actor + " jailed indefinitely");
			}

			mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var',?,?,?)", objId, "jailedFrom", loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";0", -1);
			mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var',?,?,?)", objId, "jailed", "1", System.currentTimeMillis() + (period * 60000));

			return true;
		}
	}

	public static void doJailPlayer(Player player, long period, boolean msg)
	{
		player.setVar("jailedFrom", player.getX() + ";" + player.getY() + ";" + player.getZ() + ";" + player.getReflectionId(), -1);
		player.setVar("jailed", 1, System.currentTimeMillis() + period);
		player.startUnjailTask(player, period, msg);
		player.teleToLocation(AdminFunctions.JAIL_SPAWN.findPointToStay(50, 200), ReflectionManager.JAIL);
		
		if (player.isInStoreMode())
		{
			player.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		}
		
		player.sitDown(null);
		player.block();
	}
}