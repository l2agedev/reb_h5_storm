package l2r.gameserver.nexus_engine.events.engine.base;

import l2r.gameserver.nexus_engine.events.EventGame;
import l2r.gameserver.nexus_engine.events.engine.stats.GlobalStats.GlobalStatType;
import l2r.gameserver.nexus_engine.events.engine.stats.GlobalStatsModel;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import javolution.util.FastTable;

/**
 * @author hNoke
 * EventPlayerData implementation for PvP/PvE events
 */
public class PvPEventPlayerData extends EventPlayerData
{
	private int _kills = 0;
	private int _deaths = 0;
	private int _spree = 0;

	private FastTable<Integer> _awardedForSpree;
	
	public PvPEventPlayerData(PlayerEventInfo owner, EventGame event, GlobalStatsModel stats)
	{
		super(owner, event, stats);

		_awardedForSpree = new FastTable<>();
	}
	
	// Kills
	public int getKills() 
	{ 
		return _kills; 
	}
	
	public int raiseKills(int i) 
	{ 
		_kills += i;
		_globalStats.raise(GlobalStatType.KILLS, i);
		return _kills;
	}
	
	public void setKills(int i)
	{
		_kills = i;
		_globalStats.set(GlobalStatType.KILLS, i);
	}
	
	// Deaths
	public int getDeaths() 
	{ 
		return _deaths; 
	}
	
	public int raiseDeaths(int i) 
	{ 
		_deaths += i;
		_globalStats.raise(GlobalStatType.DEATHS, i);
		return _deaths;
	}
	
	public void setDeaths(int i)
	{
		_deaths = i;
		_globalStats.set(GlobalStatType.DEATHS, i);
	}
	
	// Sprees
	public int getSpree() 
	{ 
		return _spree; 
	}
	
	public int raiseSpree(int i) 
	{ 
		_spree += i;
		return _spree;
	}
	
	public void setSpree(int i)
	{
		_spree = i;
	}

	public void setWasAwardedForSpree(int kills)
	{
		_awardedForSpree.add(kills);
	}

	public boolean wasAwardedForSpree(int kills)
	{
		return _awardedForSpree.contains(kills);
	}
}
