package l2r.gameserver.nexus_engine.playervalue.criteria;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.SkillData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javolution.util.FastMap;

/**
 * @author hNoke
 *
 */
public class PlayerSkills implements ICriteria
{
	private Map<Integer, Levels> _skills;
	
	private class Levels
	{
		public FastMap<Integer, Integer> levels;
		public Levels(int level, int points)
		{
			levels = new FastMap<Integer, Integer>();
			add(level, points);
		}
		
		public void add(int level, int points)
		{
			levels.put(level, points);
		}
		
		public int get(int level)
		{
			// level doesn't matter in this case, just select the highest # of points available (highest level of skill)
			if(level == -1)
			{
				int top = 0;
				for(int points : levels.values())
				{
					if(points > top)
					{
						top = points;
						continue;
					}
				}
				return top;
			}
			// this level of skill is in the tables
			else if(levels.containsKey(level))
			{
				return levels.get(level);
			}
			// if this level is not in the tables, look for lower levels
			else if(level >= 0)
			{
				level --;
				return get(level);
			}
			else // look if there is -1 value available, otherwise return 0 
			{
				if(levels.containsKey(-1))
					return levels.get(-1);
				else
					return 0;
			}
		}
	}
	
	public PlayerSkills()
	{
		loadData();
	}
	
	private void loadData()
	{
		_skills = new FastMap<Integer, Levels>();
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			statement = con.prepareStatement("SELECT skillId, level, score FROM nexus_playervalue_skills");
			rset = statement.executeQuery();
			
			int skillId, level, score;
			while (rset.next())
			{
				skillId = rset.getInt("skillId");
				level = rset.getInt("level");
				score = rset.getInt("score");
				
				if(_skills.containsKey(skillId))
					_skills.get(skillId).add(level, score);
				else
					_skills.put(skillId, new Levels(level, score));
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			// turn off this system
			EventConfig.getInstance().getGlobalConfig("GearScore", "enableGearScore").setValue("false");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	
	/** level -1 makes to choose automatically the highest level */
	public int getScoreForSkill(int skillId, int level)
	{
		if(_skills.containsKey(skillId))
		{
			return _skills.get(skillId).get(level);
		}
		else
			return 0;
	}
	
	@Override
	public int getPoints(PlayerEventInfo player)
	{
		int points = 0;
		for(SkillData skill : player.getSkills())
		{
			points += getScoreForSkill(skill.getId(), skill.getLevel());
		}
		return points;
	}
	
	public static final PlayerSkills getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PlayerSkills _instance = new PlayerSkills();
	}
}
