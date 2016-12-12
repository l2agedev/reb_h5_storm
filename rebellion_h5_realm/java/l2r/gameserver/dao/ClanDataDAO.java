package l2r.gameserver.dao;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.entity.residence.Residence;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.tables.ClanTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClanDataDAO
{
	private static final Logger _log = LoggerFactory.getLogger(ClanDataDAO.class);
	private static final ClanDataDAO _instance = new ClanDataDAO();

	public static final String SELECT_CASTLE_OWNER = "SELECT clan_id FROM clan_data WHERE hasCastle = ? LIMIT 1";
	public static final String SELECT_FORTRESS_OWNER = "SELECT clan_id FROM clan_data WHERE hasFortress = ? LIMIT 1";
	public static final String SELECT_CLANHALL_OWNER = "SELECT clan_id FROM clan_data WHERE hasHideout = ? LIMIT 1";

	public static ClanDataDAO getInstance()
	{
		return _instance;
	}

	public Clan getOwner(Castle c)
	{
		return getOwner(c, SELECT_CASTLE_OWNER);
	}

	public Clan getOwner(Fortress f)
	{
		return getOwner(f, SELECT_FORTRESS_OWNER);
	}

	public Clan getOwner(ClanHall c)
	{
		return getOwner(c, SELECT_CLANHALL_OWNER);
	}

	private Clan getOwner(Residence residence, String sql)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(sql);
			statement.setInt(1, residence.getId());
			rset = statement.executeQuery();
			if(rset.next())
				return ClanTable.getInstance().getClan(rset.getInt("clan_id"));
		}
		catch(Exception e)
		{
			_log.error("ClanDataDAO.getOwner(Residence, String)", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return null;
	}
	
	public void updateDescription(int id, String description)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_description SET description=? WHERE clan_id=?");
			statement.setString(1, description);
			statement.setInt(2, id);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("ClanDataDAO.updateDescription(int, String)", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void insertDescription(int id, String description)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO clan_description (clan_id, description) VALUES (?, ?)");
			statement.setInt(1, id);
			statement.setString(2, description);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("ClanDataDAO.updateDescription(int, String)", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
