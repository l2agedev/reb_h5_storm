/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.nexus_engine.events.engine.mini.events;

import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.events.engine.EventMapSystem;
import l2r.gameserver.nexus_engine.events.engine.base.ConfigModel;
import l2r.gameserver.nexus_engine.events.engine.base.EventMap;
import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.base.SpawnType;
import l2r.gameserver.nexus_engine.events.engine.base.description.EventDescription;
import l2r.gameserver.nexus_engine.events.engine.base.description.EventDescriptionSystem;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode.FeatureType;
import l2r.gameserver.nexus_engine.events.engine.mini.RegistrationData;
import l2r.gameserver.nexus_engine.events.engine.mini.features.AbstractFeature;
import l2r.gameserver.nexus_engine.events.engine.mini.features.DelaysFeature;
import l2r.gameserver.nexus_engine.events.engine.mini.features.RoundsFeature;
import l2r.gameserver.nexus_engine.events.engine.mini.features.TeamSizeFeature;
import l2r.gameserver.nexus_engine.events.engine.mini.features.TeamsAmmountFeature;
import l2r.gameserver.nexus_engine.events.engine.mini.features.TimeLimitFeature;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.Collections;
import java.util.List;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public class MiniTvTManager extends OnevsOneManager
{
	private final int MAX_GAMES_COUNT = 3;
	
	public MiniTvTManager(EventType type)
	{
		super(type);
	}
	
	@Override
	public void loadConfigs()
	{
		super.loadConfigs();
		
		removeConfig("TeamsAmmount");
		addConfig(new ConfigModel("TeamSize", "10", "The count of players in one team."));
	}
	
	@Override
	public void createGame()
	{
		if(_locked) 
			return;

		if(isTournamentActive())
		{
			check();
			return;
		}
		
		removeInactiveTeams();
		
		final int teamsSize = getPlayersInTeam() * 2;
		
		if(_parties.size() < teamsSize || _games.size() >= getMaxGamesCount())
		{
			check();
			return;
		}
		
		EventMap chosenMap = null;
		if(!NexusLoader.allowInstances())
		{
			chosenMap = getFreeMap();
			
			// all maps are busy
			if(chosenMap == null)
			{
				check();
				return;
			}
		}
		
		List<RegistrationData> players = new FastTable<RegistrationData>();
		setIsTemporaryLocked(true);

		Collections.sort(_parties, _compareByGearScore);
		
		try
		{
			for(RegistrationData team : _parties)
			{
				if(team.isChosen())
					continue;
				
				if(players.size() < teamsSize)
				{
					team.setIsChosen(true);
					players.add(team);
					
					if(players.size() >= teamsSize)
					{
						launchGame(players.toArray(new RegistrationData[players.size()]), chosenMap);

						setIsTemporaryLocked(false);
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		for(RegistrationData p : _parties)
		{
			if(p.isChosen())
				_parties.remove(p);
		}
		
		setIsTemporaryLocked(false);
		
		check();
	}
	
	@Override
	public boolean launchGame(RegistrationData[] players, EventMap map)
	{
		if(map == null)
		{
			if(NexusLoader.allowInstances())
				map = EventMapSystem.getInstance().getNextMap(this, _lastMapIndex, getMode());
			else
				map = getFreeMap();
		}
		
		if(map == null)
		{
			cleanMe(true);
			_mode.setAllowed(false);
			
			_log.warn("No map available for event " + getEventType().getAltTitle() + " !!! Mode has been disabled.");
			return false;
		}
		
		_lastMapIndex = EventMapSystem.getInstance().getMapIndex(getEventType(), map);
		getNextGameId();
		
		MiniTvTGame game = new MiniTvTGame(_lastGameId, map, this, players);
		new Thread(game, getEventName() + " ID" + _lastGameId).start();
		
		_games.add(game);
		return true;
	}
	
	@Override
	public boolean checkCanFight(PlayerEventInfo gm, RegistrationData[] team)
	{
		if(team.length != 2)
		{
			gm.sendMessage("2 teams are required.");
			return false;
		}
		
		//TODO: check players for manually started matches
		return true;
	}
	
	public int getPlayersInTeam()
	{
		for(AbstractFeature feature : getMode().getFeatures())
		{
			if(feature.getType() == FeatureType.TeamSize)
			{
				if(((TeamSizeFeature) feature).getTeamSize() > 0)
					return ((TeamSizeFeature) feature).getTeamSize();
			}
		}
		
		return getInt("TeamSize");
	}
	
	@Override
	public int getTeamsCount()
	{
		return 2;
	}

	@Override
	protected int getStartGameInterval()
	{
		return 10000;
	}
	
	@Override
	public int getDefaultPartySizeToJoin()
	{
		return 1;
	}
	
	@Override
	public boolean requireParty()
	{
		return false;
	}
	
	@Override
	public int getMaxGamesCount()
	{
		return MAX_GAMES_COUNT;
	}
	
	@Override
	public String getHtmlDescription()
	{
		if(_htmlDescription == null)
		{
			int roundsCount = getInt("RoundsAmmount");
			int teamsCount = getInt("TeamsAmmount");
			int teamSize = getInt("TeamSize");
			int rejoinDelay = getInt("DelayToWaitSinceLastMatchMs");
			int timeLimit = getInt("TimeLimitMs");
			
			for(AbstractFeature feature : getMode().getFeatures())
			{
				if(feature instanceof RoundsFeature)
					roundsCount = ((RoundsFeature)feature).getRoundsAmmount();
				else if(feature instanceof TeamsAmmountFeature)
					teamsCount = ((TeamsAmmountFeature)feature).getTeamsAmmount();
				else if(feature instanceof DelaysFeature)
					rejoinDelay = ((DelaysFeature)feature).getRejoinDealy();
				else if(feature instanceof TimeLimitFeature)
					timeLimit = ((TimeLimitFeature)feature).getTimeLimit();
				else if(feature instanceof TeamSizeFeature)
					teamSize = ((TeamSizeFeature)feature).getTeamSize();
			}
			
			EventDescription desc = EventDescriptionSystem.getInstance().getDescription(getEventType());
			if(desc != null)
			{
				_htmlDescription = desc.getDescription(getConfigs(), roundsCount, teamsCount, teamSize, rejoinDelay, timeLimit);
			}
			else
			{
				_htmlDescription = "This is a team-based mini event. This event is similar to Party fights, but you don't need any party here - ";
				_htmlDescription += " the event will automatically put you to one of " + teamsCount + " teams, which will fight against each other. Each team has " + teamSize + " players.<br1> ";
				
				if(roundsCount > 1)
				{
					if(teamsCount == 2)
					{
						_htmlDescription += "The match has " + roundsCount + " rounds. Round ends when all players from one team are dead (they will be resurrected in start of the next round). ";
						_htmlDescription += "The winner of the match is in the end of all rounds the team, which won the most of rounds. ";
					}
					else
					{
						_htmlDescription += "The match has " + roundsCount + " rounds. Round ends when one team kills all it's opponents (dead players will be resurrected in start of the next round). ";
						_htmlDescription += "The winner of the match is in the end of all rounds the team, which won the most of rounds. ";
					}
				}
				else
				{
					if(teamsCount == 2)
					{
						_htmlDescription += "This event has only one round. If you die, the event ends for you. ";
						_htmlDescription += "The match ends when all players of one team are dead. ";
					}
					else
					{
						_htmlDescription += "This event has only one round. If you die, the event ends for you. ";
						_htmlDescription += "The winner of the match is the team, who kills all it's opponents. ";
					}
				}
				
				_htmlDescription += "Your opponents will be selected automatically and don't worry, there's a protection, which will ensure that you will always fight only players whose level is similar to yours. ";
				_htmlDescription += "If the match doesn't end within " + timeLimit/60000 + " minutes, it will be aborted automatically. ";
				_htmlDescription += "Also, after you visit this event, you will have to wait at least " + rejoinDelay/60000 + " minutes to join this event again. ";
			}
		}
		return _htmlDescription;
	}
	
	@Override
	protected String addMissingSpawn(SpawnType type, int team, int count)
	{
		return "<font color=bfbfbf>" + getMode().getModeName() + " </font><font color=696969>mode</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>" + type.toString().toUpperCase() + "</font> <font color=9f9f9f>spawn for team " + team + " count " + count + " (or more)</font><br1>";
	}
}
