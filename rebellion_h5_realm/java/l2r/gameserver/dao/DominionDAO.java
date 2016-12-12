package l2r.gameserver.dao;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.entity.residence.Dominion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 18:10/15.04.2011
 */
public class DominionDAO
{
	private static final Logger _log = LoggerFactory.getLogger(DominionDAO.class);
	private static final DominionDAO _instance = new DominionDAO();

	public static final String SELECT_SQL_QUERY = "SELECT lord_object_id, wards, siege_date FROM dominion WHERE id=?";
	public static final String UPDATE_SQL_QUERY = "UPDATE dominion SET lord_object_id=?, wards=?, siege_date=? WHERE id=?";

	public static DominionDAO getInstance()
	{
		return _instance;
	}

	public void select(Dominion dominion)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setInt(1, dominion.getId());
			rset = statement.executeQuery();
			if(rset.next())
			{
				dominion.setLordObjectId(rset.getInt("lord_object_id"));

				String flags = rset.getString("wards");
				if(!flags.isEmpty())
				{
					String[] values = flags.split(";");
					for(int i = 0; i < values.length; i++)
						dominion.addFlag(Integer.parseInt(values[i]));
				}
				
				dominion.getSiegeDate().setTimeInMillis(rset.getLong("siege_date"));
			}
		}
		catch(Exception e)
		{
			_log.error("Dominion.loadData(): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void update(Dominion dominion)
	{
		if(!dominion.getJdbcState().isUpdatable())
			return;

		dominion.setJdbcState(JdbcEntityState.STORED);
		
		String wardsString = "";
		int[] flags = dominion.getFlags();
		if(flags.length > 0)
			for(int flag : flags)
				wardsString += flag + ";";

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_SQL_QUERY);
			statement.setInt(1, dominion.getLordObjectId());
			statement.setString(2, wardsString);
			statement.setLong(3, dominion.getSiegeDate().getTimeInMillis());
			statement.setInt(4, dominion.getId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("DominionDAO#update0(Dominion): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
