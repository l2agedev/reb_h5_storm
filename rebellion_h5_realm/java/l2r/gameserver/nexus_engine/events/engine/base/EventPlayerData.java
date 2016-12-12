package l2r.gameserver.nexus_engine.events.engine.base;

import l2r.gameserver.nexus_engine.events.EventGame;
import l2r.gameserver.nexus_engine.events.engine.stats.GlobalStats.GlobalStatType;
import l2r.gameserver.nexus_engine.events.engine.stats.GlobalStatsModel;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

/**
 * @author hNoke
 * stores data related to one player and event he's in
 */
public class EventPlayerData
{
	private PlayerEventInfo _owner;

	protected GlobalStatsModel _globalStats;
	
	private int _score;
	
	public EventPlayerData(PlayerEventInfo owner, EventGame event, GlobalStatsModel stats)
	{
		_owner = owner;
		_globalStats = stats;
	}
	
	public PlayerEventInfo getOwner()
	{
		return _owner;
	}
	
	// Score
	public int getScore() 
	{ 
		return _score; 
	}
	
	public int raiseScore(int i) 
	{ 
		_score += i;
		_globalStats.raise(GlobalStatType.SCORE, i);
		return _score;
	}
	
	public void setScore(int i)
	{
		_score = i;
		_globalStats.set(GlobalStatType.SCORE, i);
	}
	
	public GlobalStatsModel getGlobalStats()
	{
		return _globalStats;
	}
}
