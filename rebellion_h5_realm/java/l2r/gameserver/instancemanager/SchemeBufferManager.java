package l2r.gameserver.instancemanager;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;

import gnu.trove.map.hash.TIntIntHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastTable;
import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemeBufferManager
{
	private static final Logger _log = LoggerFactory.getLogger(SchemeBufferManager.class);
	/* FastMap to hold the avaliable buffs, FastList to properly iterate showing the buffs */
	public static FastMap<String, Integer[]> _buffs = new FastMap<>();
	public static TIntIntHashMap _skillLevels = new TIntIntHashMap();
	
	/* Pages to show the buffs. Each page should have 15 buffs */
	public static int _pages;
	
	public SchemeBufferManager()
	{
		loadData();
	}
	
	/*
	 * Load all buffs into a FastMap at server start up
	 */
	private void loadData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM scheme_buffs");
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				Integer[] buffs = new Integer[2];
				String name = rset.getString("buff_name");
				buffs[0] = rset.getInt("buff_id");
				buffs[1] = rset.getInt("buff_lvl");
				_buffs.put(name, buffs);
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
		
		_pages = Math.round(_buffs.size() / 22);
		
		for(Integer[] i : _buffs.values())
			_skillLevels.put(i[0], i[1]);
		
		//All was correct, print out a message
		_log.info("Loaded " + _buffs.size() + " buffs for the Scheme Buffer. "+_pages+" pages available.");
	}
	
	/*
	 * Load all player profiles into a FastList
	 */
	public static FastTable<PlayerBuffProfile> loadPlayerProfiles(Player player)
	{
		
		FastTable<PlayerBuffProfile> profiles = new FastTable<>();
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT profile, buffs FROM scheme_buffer_profiles WHERE charId = ?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			
			if (rset == null)
				return null;
			
			while (rset.next())
			{
				PlayerBuffProfile profile = SchemeBufferManager.getInstance().new PlayerBuffProfile(rset.getString("profile"));
				String[] buffs = rset.getString("buffs").split(";");
				for (String buff : buffs)
					profile.buffs.add(buff);
				
				profiles.add(profile);
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
		
		return profiles;
	}
	
	public void deleteFromDatabase(Player owner, PlayerBuffProfile profile)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM scheme_buffer_profiles WHERE charId = ? AND profile = ?");
			statement.setInt(1, owner.getObjectId());
			statement.setString(2, profile.profileName);
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			owner.sendChatMessage(0, ChatType.TELL.ordinal(), "Buffer", "Successfuly deleted profile: " + profile.profileName);
			owner.removeProfile(profile);
			
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public class PlayerBuffProfile
	{
		public final String profileName;
		public final FastTable<String> buffs;
		private boolean isModified = false;
		
		public PlayerBuffProfile(String $profileName)
		{
			profileName = $profileName;
			buffs = new FastTable<>();
		}
		
		public void addBuff(String buffName)
		{
			isModified = true;
			buffs.add(buffName);
		}
		
		public void removeBuff(String buffName)
		{
			isModified = true;
			buffs.remove(buffName);
		}
		
		public void save(Player owner)
		{
			if (isModified)
				saveToDatabase(owner);
		}
		
		private void saveToDatabase(Player owner)
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("REPLACE INTO scheme_buffer_profiles VALUES ( ? , ? , ? )");
				statement.setInt(1, owner.getObjectId());
				statement.setString(2, profileName);
				statement.setString(3, getBuffsAsString());
				statement.execute();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				isModified = false;
				DbUtils.closeQuietly(con, statement);
			}
		}
		
		private String getBuffsAsString()
		{
			StringBuilder sb = new StringBuilder();
			
			for (String buff : buffs)
			{
				sb.append(buff);
				sb.append(";");
			}
			
			return sb.toString();
		}
	}
	
	public static final SchemeBufferManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SchemeBufferManager _instance = new SchemeBufferManager();
	}
}