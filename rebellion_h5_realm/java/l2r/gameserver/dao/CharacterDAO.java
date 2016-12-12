package l2r.gameserver.dao;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.database.mysql;
import l2r.gameserver.model.Player;
import l2r.gameserver.utils.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterDAO
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterDAO.class);
	
	private static CharacterDAO _instance = new CharacterDAO();
	
	public static CharacterDAO getInstance()
	{
		return _instance;
	}
	
	public void deleteCharByObjId(int objid)
	{
		if (objid < 0)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			// Чистим таблицу character_friends(Друзья персонажа) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу character_hennas(Татту) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу character_macroses(Макросы) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу character_quests(Квесты) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу character_recipebook(Рецепты) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу character_shortcuts(Ярлыки - Панель) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу character_skills(Скиллы) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу character_skills_save(Скиллы) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу character_subclasses(СабКлассы) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу heroes(HERO) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу olympiad_nobles(Олимпиада) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу seven_signs(Семь Печатей) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу pets(Питомцы персонажа) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу item_attributes(Все аттрибуты) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM item_attributes WHERE object_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу items(Все итемы персонажа) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Чистим таблицу characters(Персонаж) - удаляемого чара
			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// achievements
			statement = con.prepareStatement("DELETE FROM character_achievement_levels WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// counters
			statement = con.prepareStatement("DELETE FROM character_counters WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
						
			// buffer
			statement = con.prepareStatement("DELETE FROM scheme_buffer_profiles WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public boolean insert(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `characters` (account_name, obj_Id, char_name, face, hairStyle, hairColor, sex, karma, pvpkills, pkkills, clanid, createtime, deletetime, title, accesslevel, online, leaveclan, deleteclan, nochannel, pledge_type, pledge_rank, lvl_joined_academy, apprentice) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, player.getObjectId());
			statement.setString(3, player.getName());
			statement.setInt(4, player.getFace());
			statement.setInt(5, player.getHairStyle());
			statement.setInt(6, player.getHairColor());
			statement.setInt(7, player.getSex());
			statement.setInt(8, player.getKarma());
			statement.setInt(9, player.getPvpKills());
			statement.setInt(10, player.getPkKills());
			statement.setInt(11, player.getClanId());
			statement.setLong(12, player.getCreateTime() / 1000);
			statement.setInt(13, player.getDeleteTimer());
			statement.setString(14, player.getTitle());
			statement.setInt(15, player.getAccessLevel().getLevel());
			statement.setInt(16, player.isOnline() ? 1 : 0);
			statement.setLong(17, player.getLeaveClanTime() / 1000);
			statement.setLong(18, player.getDeleteClanTime() / 1000);
			statement.setLong(19, player.getNoChannel() > 0 ? player.getNoChannel() / 1000 : player.getNoChannel());
			statement.setInt(20, player.getPledgeType());
			statement.setInt(21, player.getPowerGrade());
			statement.setInt(22, player.getLvlJoinedAcademy());
			statement.setInt(23, player.getApprentice());
			statement.executeUpdate();
			DbUtils.close(statement);
			
			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty, certification) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, player.getTemplate().classId.getId());
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.setDouble(5, player.getTemplate().getBaseHpMax() + player.getTemplate().lvlHpAdd + player.getTemplate().lvlHpMod);
			statement.setDouble(6, player.getTemplate().getBaseMpMax() + player.getTemplate().lvlMpAdd + player.getTemplate().lvlMpMod);
			statement.setDouble(7, player.getTemplate().getBaseCpMax() + player.getTemplate().lvlCpAdd + player.getTemplate().lvlCpMod);
			statement.setDouble(8, player.getTemplate().getBaseHpMax() + player.getTemplate().lvlHpAdd + player.getTemplate().lvlHpMod);
			statement.setDouble(9, player.getTemplate().getBaseMpMax() + player.getTemplate().lvlMpAdd + player.getTemplate().lvlMpMod);
			statement.setDouble(10, player.getTemplate().getBaseCpMax() + player.getTemplate().lvlCpAdd + player.getTemplate().lvlCpMod);
			statement.setInt(11, 1);
			statement.setInt(12, 1);
			statement.setInt(13, 1);
			statement.setInt(14, 0);
			statement.setInt(15, 0);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.error("", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	
	public Location getLocation(String name)
	{
		return getLocation(getObjectIdByName(name));
	}
	
	public Location getLocation(int id)
	{
		if (id == 0)
		{
			return null;
		}
		
		Location result = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT x, y, z FROM characters WHERE obj_Id=?");
			statement.setInt(1, id);
			rset = statement.executeQuery();
			
			if (rset.next())
			{
				result = new Location(rset.getInt(1), rset.getInt(2), rset.getInt(3));
			}
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getLocation(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public void deleteUserVar(String cha, String param)
	{
		deleteUserVar(getObjectIdByName(cha), param);
	}
	
	public void deleteUserVar(int objId, String param)
	{
		if (objId == 0)
		{
			return;
		}
		
		mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", objId, param);
	}
	
	public String getUserVar(String cha, String param)
	{
		return getUserVar(getObjectIdByName(cha), param);
	}
	
	public String getUserVar(int objId, String param)
	{
		if (objId == 0)
			return null;
		
		return (String) mysql.get("SELECT `value` FROM `character_variables` WHERE `obj_id` = " + objId + " AND `type`='user-var' AND `name` = '" + param + "'");
	}
	
	public void setDbLocatio(int objId, int x, int y, int z)
	{
		mysql.set("UPDATE `characters` SET `x`=?, `y`=?, `z`=? WHERE `obj_id`=? LIMIT 1", x, y, z, objId);
	}
	
	public int getObjectIdByName(String name)
	{
		int result = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, name);
			rset = statement.executeQuery();
			if (rset.next())
				result = rset.getInt(1);
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getObjectIdByName(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public String getNameByObjectId(int objectId)
	{
		String result = StringUtils.EMPTY;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?");
			statement.setInt(1, objectId);
			rset = statement.executeQuery();
			if (rset.next())
				result = rset.getString(1);
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getObjectIdByName(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public String getAccountName(String charName)
	{
		String result = StringUtils.EMPTY;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, charName);
			rset = statement.executeQuery();
			if (rset.next())
				result = rset.getString(1);
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getAccountName(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public boolean accountExists(String name)
	{
		String result = StringUtils.EMPTY;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT login FROM " + Config.LOGINSERVER_DB_NAME + ".accounts WHERE login=?");
			statement.setString(1, name);
			rset = statement.executeQuery();
			if (rset.next())
				result = rset.getString(1);
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getAccountName(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result != "";
	}
	
	public int accountCharNumber(String account)
	{
		int number = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			rset = statement.executeQuery();
			if (rset.next())
				number = rset.getInt(1);
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.accountCharNumber(String):", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return number;
	}
	
	public String getAccountPassword(String accountName)
	{
		String result = StringUtils.EMPTY;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT password FROM " + Config.LOGINSERVER_DB_NAME + ".accounts WHERE login=?");
			statement.setString(1, accountName);
			rset = statement.executeQuery();
			if (rset.next())
				result = rset.getString(1);
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getAccountPassword(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public int getLastServerId(String accountName)
	{
		int result = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT last_server FROM " + Config.LOGINSERVER_DB_NAME + ".accounts WHERE login=?");
			statement.setString(1, accountName);
			rset = statement.executeQuery();
			if (rset.next())
				result = rset.getInt(1);
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getLastServerId(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	
	public long getLastAccessTime(String charName)
	{
		long lastAccesss = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT lastAccess FROM characters WHERE char_name=?");
			statement.setString(1, charName);
			rset = statement.executeQuery();
			if (rset.next())
				lastAccesss = rset.getInt(1);
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getLastAccessTime(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return lastAccesss;
	}
	
	public String getOnlineChar(String accountName)
	{
		String result = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
		    statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=? AND online=1");
			statement.setString(1, accountName);
			rset = statement.executeQuery();
			while (rset.next())
			{
				result = rset.getString(1);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return result;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}
	
	public int getClassIdByObjectId(int objectId, boolean baseClass)
	{
		int classId = -1;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id FROM character_subclasses WHERE char_obj_id=? AND " + (baseClass ? "isBase=1" : "active=1") + " LIMIT 1");
			statement.setInt(1, objectId);
			rset = statement.executeQuery();
			if(rset.next())
				classId = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.error("CharacterDAO.getBaseClassIdByObjectId(int):", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return classId;
	}
	
	public long getClassData(int objectId, String sqlColumn)
	{
		long data = -1;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT " + sqlColumn + " FROM character_subclasses WHERE char_obj_id=? AND active=1");
			statement.setInt(1, objectId);
			rset = statement.executeQuery();
			if(rset.next())
				data = rset.getLong(1);
		}
		catch(Exception e)
		{
			_log.error("CharacterDAO.getBaseClassIdByObjectId(int):", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return data;
	}
	
	public List<String> getCharactersByName(String name)
	{
		List<String> players = new ArrayList<String>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name FROM characters WHERE char_name LIKE '%" + name + "%' AND accesslevel >= '0'");
			rset = statement.executeQuery();
			while (rset.next())
				players.add(rset.getString(1));
			
		}
		catch(Exception e)
		{
			_log.error("CharacterDAO.getCharactersByName(String):", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return players;
	}
	
	public String generateCharDataBy(String charName, String sqlColumn)
	{
		String data = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT " + sqlColumn + " FROM characters WHERE char_name=? AND accessLevel >= 0");
			statement.setString(1, charName);
			rset = statement.executeQuery();
			if(rset.next())
				data = rset.getString(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return data;
	}
	
	public boolean haveSecurityPassword(int charObjid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT password FROM character_security WHERE charId = ?");
			statement.setInt(1, charObjid);
			rset = statement.executeQuery();
			if (rset.next())
				return true;
		}
		catch (Exception e)
		{
			_log.warn("Could not restore security password: " + e.getMessage() + " for " + charObjid, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return false;
	}
	
	public void updateSecurityTries(int charObjid, int tries)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("UPDATE `character_security` SET `remainingTries`=? WHERE `charId`=?");
			statement.setInt(1, tries);
			statement.setInt(2, charObjid);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("Could not store security password: " + e.getMessage() + " for " + charObjid, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void setSecurity(int charObjid, String password)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if (haveSecurityPassword(charObjid))
			{
				statement = con.prepareStatement("UPDATE `character_security` SET `password`=?, `changeDate`=?, `changeHWID`=?, `remainingTries`=? WHERE `charId`=?");
				statement.setString(1, password);
				statement.setLong(2, System.currentTimeMillis());
				statement.setString(3, "null");
				statement.setInt(4, 3); // 3 tries left on successful change
				statement.setInt(5, charObjid);
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO character_security (charId, password, activationDate, activationHWID) VALUES (?, ?, ?, ?)");
				statement.setInt(1, charObjid);
				statement.setString(2, password);
				statement.setLong(3, System.currentTimeMillis());
				statement.setString(4, "null");
			}
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn("Could not store security password: " + e.getMessage() + " for " + charObjid, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}