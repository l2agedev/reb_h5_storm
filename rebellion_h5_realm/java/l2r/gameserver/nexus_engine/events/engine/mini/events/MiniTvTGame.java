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

import l2r.gameserver.Config;
import l2r.gameserver.nexus_engine.events.engine.EventBuffer;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.EventRewardSystem;
import l2r.gameserver.nexus_engine.events.engine.EventWarnings;
import l2r.gameserver.nexus_engine.events.engine.base.EventMap;
import l2r.gameserver.nexus_engine.events.engine.base.EventPlayerData;
import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;
import l2r.gameserver.nexus_engine.events.engine.base.PvPEventPlayerData;
import l2r.gameserver.nexus_engine.events.engine.base.RewardPosition;
import l2r.gameserver.nexus_engine.events.engine.base.SpawnType;
import l2r.gameserver.nexus_engine.events.engine.lang.LanguageEngine;
import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventGame;
import l2r.gameserver.nexus_engine.events.engine.mini.RegistrationData;
import l2r.gameserver.nexus_engine.events.engine.stats.GlobalStats.GlobalStatType;
import l2r.gameserver.nexus_engine.events.engine.stats.GlobalStatsModel;
import l2r.gameserver.nexus_engine.events.engine.team.EventTeam;
import l2r.gameserver.nexus_engine.events.engine.team.FixedPartyTeam;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.callback.CallbackManager;
import l2r.gameserver.nexus_interface.delegate.CharacterData;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import javolution.text.TextBuilder;
import javolution.util.FastTable;
import javolution.util.FastMap;

/**
 * @author hNoke
 *
 */
public class MiniTvTGame extends MiniEventGame implements Runnable
{
	private final int _teamsAmmount;
	private final int _roundsAmmount;
	private final String _root;
	
	private FixedPartyTeam[] _teams;
	
	private ScheduledFuture<?> _eventEnd;
	private ScheduledFuture<?> _roundStart;
	
	private int _round = 0;
	
	private Comparator<RegistrationData> compareByLevels = new Comparator<RegistrationData>()
	{
		@Override
		public int compare(RegistrationData o1, RegistrationData o2)
		{
			int level1 = o1.getKeyPlayer().getLevel();
			int level2 = o2.getKeyPlayer().getLevel();
			
			return level1 == level2 ? 0 : level1 < level2 ? 1 : -1;
		}
	};
	
	public MiniTvTGame(int gameId, EventMap arena, MiniTvTManager event, RegistrationData[] players)
	{
		super(gameId, arena, event, players);
		
		_teamsAmmount = event.getTeamsCount();
		_roundsAmmount = event.getRoundsAmmount();
		_root = getEvent().getMapConfig(_arena, "RootPlayers");
		
		_teams = new FixedPartyTeam[_teamsAmmount];

		for(int i = 0; i < _teamsAmmount; i++)
		{
			_teams[i] = new FixedPartyTeam(players[i], i + 1, event.getPlayersInTeam());
		}
		
		List<RegistrationData> datas = new FastTable<RegistrationData>();
		for(RegistrationData d : players)
			datas.add(d);
		
		Collections.sort(datas, compareByLevels);
		
		int team = 0;
		PlayerEventInfo pi;
		
		for(RegistrationData playerData : datas)
		{
			pi = playerData.getKeyPlayer();
			pi.onEventStart(this);
			
			if(pi.getParty() != null)
				pi.getParty().removePartyMember(pi);
			
			_teams[team].addPlayer(pi, true);
			
			team++;
			
			if(team >= _teamsAmmount)
				team = 0;
		}
		
		boolean createParties = datas.size() > 2;
		if(createParties)
			createParties();
		
		CallbackManager.getInstance().eventStarts(1, getEvent().getEventType(), Arrays.asList(_teams));
	}
	
	private void createParties()
	{
		for(FixedPartyTeam team : _teams)
		{
			team.createParties();
		}
	}
	
	@Override
	public void run() 
	{
		initEvent();
	}

	@Override
	protected void initEvent()
	{
		super.initEvent();
		
		loadBuffers();
		startEvent();
	}

	@Override
	protected void startEvent()
	{
		broadcastMessage(LanguageEngine.getMsg("game_teleporting"), true);

		_eventEnd = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				endByTime();
			}
		}, getGameTime());

		nextRound(false);
	}
	
	private void nextRound(boolean forceEnd)
	{
		if(_aborted)
			return;
		
		if(_round == _roundsAmmount || forceEnd)
		{
			endByDie();
			return;
		}
		else
		{
			_round++;
			
			boolean removeBuffs = getEvent().getBoolean("removeBuffsOnRespawn");
			if(_round == 1) // first round
				removeBuffs = getEvent().getBoolean("removeBuffsOnStart");
			
			EventSpawn spawn;
			for(FixedPartyTeam team : _teams)
			{
				spawn = _arena.getNextSpawn(team.getTeamId(), SpawnType.Regular);
				
				if(spawn == null)
				{
					abortDueToError("No regular spawn found for team " + team.getTeamId() + ". Match aborted.");
					clearEvent();
					return;
				}
				
				for(PlayerEventInfo pi : team.getPlayers())
				{
					if(!pi.isOnline()) 
						continue;
					
					pi.setLastTeleportedSpawn(spawn);
					pi.teleport(spawn.getLoc(), 0, _instanceId);
					
					if(removeBuffs)
						pi.removeBuffs();
					
					if(_root.equalsIgnoreCase("true"))
						pi.block();
					
					pi.setIsInvul(true);
					
					if (_round == 1 && getEvent().getBoolean("removeCubics"))
						pi.removeCubics();
				            
					pi.disableAfkCheck(true);

					if(_round == 1)
						checkItems(pi);

					if(_allowSchemeBuffer)
						EventBuffer.getInstance().buffPlayer(pi, true);
					
					pi.enableAllSkills();
					
					// Achievements
					if (Config.ENABLE_PLAYER_COUNTERS && _round == 1)
						pi.getOwner().getCounters().addPoint("_Mini_Events");
				}
			}

			handleDoorsAndItems(1);
			
			final int startTime;
			if(_round == 1)
				startTime = getEvent().getMapConfigInt(_arena, "FirstRoundWaitDelay");
			else
				startTime = getEvent().getMapConfigInt(_arena, "RoundWaitDelay");
			
			scheduleMessage(LanguageEngine.getMsg("game_roundStartIn", getRoundName(_round, _roundsAmmount), startTime / 1000), 5000, true);
			
			_roundStart = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					finishRoundStart();
				}
			}, startTime);
		}
	}
	
	private void finishRoundStart()
	{
		if(_aborted)
			return;
		
		unspawnBuffers();
		handleDoorsAndItems(2);
		
		for(FixedPartyTeam team : _teams)
		{
			for(PlayerEventInfo pi : team.getPlayers())
			{
				if(!pi.isOnline()) 
					continue;
					
				pi.disableAfkCheck(false);
				
				if(_root.equalsIgnoreCase("true"))
					pi.unblock();
				
				pi.setIsInvul(false);
			}
		}
		
		if(_round == 1)
			startAnnouncing();
		
		broadcastMessage(LanguageEngine.getMsg("game_roundStarted", getRoundName(_round, _roundsAmmount)), true);
	}
	
	@Override
	public synchronized void onDie(PlayerEventInfo player, CharacterData killer)
	{
		if(_aborted)
			return;
		
		updateScore(player, killer);
		
		final FixedPartyTeam team = checkLastAliveTeam();
		
		if(team != null)
		{
			team.raiseScore(1);
			onScore(team.getPlayers(), 1);
			
			final boolean forceEnd = !checkIfTheMatchCanContinue();
			
			if(_round == _roundsAmmount || forceEnd)
				scheduleMessage(LanguageEngine.getMsg("game_matchEnd"), 3000, true);
			else 
				scheduleMessage(LanguageEngine.getMsg("game_roundWonBy", getRoundName(_round, _roundsAmmount), team.getTeamName()), 3000, true);
			
			CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					nextRound(forceEnd);
				}
			}, 4000);
		}
	}
	
	private boolean checkIfTheMatchCanContinue()
	{
		int remainingRounds = _roundsAmmount - _round;
		int bestScore = 0;
		int secondScore = 0;
		
		for(FixedPartyTeam team : _teams)
		{
			if(team.getScore() > bestScore)
			{
				secondScore = bestScore;
				bestScore = team.getScore();
			}
			else if(team.getScore() > secondScore && secondScore != bestScore)
				secondScore = team.getScore();
		}
		
		// second team has no chance to win the match anymore
		if(bestScore - secondScore > remainingRounds)
			return false;
		else // there are still enought rounds so the second team can still win the match
			return true;
	}
	
	private FixedPartyTeam checkLastAliveTeam()
	{
		int aliveTeams = 0;
		FixedPartyTeam tempTeam = null;
		
		for(FixedPartyTeam team : _teams)
		{
			for(PlayerEventInfo pi : team.getPlayers())
			{
				if(pi.isOnline())
				{
					if(!pi.isDead())
					{
						aliveTeams ++;
						tempTeam = team;
						break;
					}
				}
			}
		}
		
		if(aliveTeams == 1)
			return tempTeam;
		else
			return null;
	}
	
	private void endByTime()
	{
		if(_aborted)
			return;
		
		cancelSchedulers();
		
		broadcastMessage(LanguageEngine.getMsg("game_matchEnd_timeLimit", getGameTime() / 60000), false);
		scheduleMessage(LanguageEngine.getMsg("game_matchEnd_tie"), 3000, false);

		int topScore = 0;
		FixedPartyTeam top = null;

		for(FixedPartyTeam team : _teams)
		{
			if(team.getScore() > topScore)
			{
				topScore = team.getScore();
				top = team;
			}

			for(PlayerEventInfo pi : team.getPlayers())
			{
				EventRewardSystem.getInstance().rewardPlayer(getEvent().getEventType(), getEvent().getMode().getModeId(), pi, RewardPosition.Tie_TimeLimit, null, pi.getTotalTimeAfk(), 0, 0);
				
				getPlayerData(pi).getGlobalStats().raise(GlobalStatType.LOSES, 1);
				_event.logPlayer(pi, 2);
			}
		}

		if(top != null)
			setWinner(top);
		
		saveGlobalStats();
		
		scheduleClearEvent(8000);
	}

	private void endByDie()
	{
		cancelSchedulers();
		
		List<FixedPartyTeam> sortedTeams = new FastTable<FixedPartyTeam>();
		for(FixedPartyTeam team : _teams)
		{
			sortedTeams.add(team);
		}
		
		Collections.sort(sortedTeams, EventManager.getInstance().compareTeamScore);
		
		// score, teams
		Map<Integer, FastTable<FixedPartyTeam>> scores = new FastMap<Integer, FastTable<FixedPartyTeam>>();
		
		for(FixedPartyTeam team : sortedTeams)
		{
			if(!scores.containsKey(team.getScore()))
				scores.put(team.getScore(), new FastTable<FixedPartyTeam>());
			
			scores.get(team.getScore()).add(team);
		}

		int place = 1;
		for(FixedPartyTeam team : sortedTeams)
		{
			broadcastMessage(LanguageEngine.getMsg("event_announceScore_includeKills", place, team.getTeamName(), team.getScore(), team.getKills()), false);
			place++;
		}
		
		place = 1;
		for(Entry<Integer, FastTable<FixedPartyTeam>> i : scores.entrySet())
		{
			// winners
			if(place == 1)
			{
				if(i.getValue().size() > 1)// at least two teams are winners (have the same score) and the match has more than 2 teams
				{
					if(_teamsAmmount > i.getValue().size())
					{
						TextBuilder tb = new TextBuilder();
						
						for(FixedPartyTeam team : i.getValue())
						{
							tb.append(LanguageEngine.getMsg("event_ffa_announceWinner2_part1", team.getTeamName()));
						}
						
						String s = tb.toString();
						tb = new TextBuilder(s.substring(0, s.length() - 4));
						tb.append(LanguageEngine.getMsg("event_ffa_announceWinner2_part2"));
						
						broadcastMessage(tb.toString(), false);
						
						for(FixedPartyTeam team : i.getValue())
						{
							setWinner(team);

							for(PlayerEventInfo pi : team.getPlayers())
							{
								if(pi.isOnline())
								{
									EventRewardSystem.getInstance().rewardPlayer(getEvent().getEventType(), getEvent().getMode().getModeId(), pi, RewardPosition.Winner, null, pi.getTotalTimeAfk(), 0, 0);
									setEndStatus(pi, 1);
								}
								
								getPlayerData(pi).getGlobalStats().raise(GlobalStatType.WINS, 1);
								_event.logPlayer(pi, 1);
							}
						}
					}
					else // all teams are 'winners' - have the same score (but > 0)
					{
						broadcastMessage(LanguageEngine.getMsg("event_team_announceWinner3"), false);
						
						for(FixedPartyTeam team : i.getValue())
						{
							setWinner(team);

							for(PlayerEventInfo pi : team.getPlayers())
							{
								if(pi.isOnline())
								{
									EventRewardSystem.getInstance().rewardPlayer(getEvent().getEventType(), getEvent().getMode().getModeId(), pi, RewardPosition.Tie, null, pi.getTotalTimeAfk(), 0, 0);
									//setEndStatus(pi, 1);
								}
								
								getPlayerData(pi).getGlobalStats().raise(GlobalStatType.WINS, 1);
								_event.logPlayer(pi, 2);
							}
						}
					}
				}
				else // single team is winner
				{
					FixedPartyTeam winnerTeam = i.getValue().getFirst();

					setWinner(winnerTeam);
					
					broadcastMessage(LanguageEngine.getMsg("event_team_announceWinner1", i.getValue().getFirst().getTeamName()), false);
					
					for(PlayerEventInfo pi : winnerTeam.getPlayers())
					{
						if(pi.isOnline())
						{
							EventRewardSystem.getInstance().rewardPlayer(getEvent().getEventType(), getEvent().getMode().getModeId(), pi, RewardPosition.Winner, null, pi.getTotalTimeAfk(), 0, 0);
							setEndStatus(pi, 1);
						}
						
						getPlayerData(pi).getGlobalStats().raise(GlobalStatType.WINS, 1);
						_event.logPlayer(pi, 1);
					}
				}
			}
			else // loosers
			{
				for(FixedPartyTeam team : i.getValue())
				{
					for(PlayerEventInfo pi : team.getPlayers())
					{
						if(pi.isOnline())
						{
							EventRewardSystem.getInstance().rewardPlayer(getEvent().getEventType(), getEvent().getMode().getModeId(), pi, RewardPosition.Looser, null, pi.getTotalTimeAfk(), 0, 0);
							setEndStatus(pi, 0);
						}
						
						getPlayerData(pi).getGlobalStats().raise(GlobalStatType.LOSES, 1);
						_event.logPlayer(pi, 2);
					}
				}
			}
			
			place++;
		}
		
		saveGlobalStats();

		scheduleClearEvent(8000);
	}

	@Override
	public void clearEvent()
	{
		cancelSchedulers();
		
		cleanSpectators();
		
		applyStatsChanges();
		
		for(FixedPartyTeam team : _teams)
		{
			for(PlayerEventInfo pi : team.getPlayers())
			{
				if(pi.isOnline())
				{
					if(_root.equalsIgnoreCase("true"))
						pi.unblock();
					
					if (!pi.isGM())
						pi.setIsInvul(false);
				       
					if (pi != null && pi.isDead())
					{
						pi.doRevive();
						pi.setCurrentHp(pi.getMaxHp());
						pi.setCurrentHp(pi.getMaxMp());
						pi.setCurrentCp(pi.getMaxCp());
					}
					
					pi.restoreData();
					pi.teleport(pi.getOrigLoc(), 0, 0);
					pi.sendMessage(LanguageEngine.getMsg("event_teleportBack"));
					
					CallBack.getInstance().getPlayerBase().eventEnd(pi);
				}
			}
		}

		if(_fences != null)
			CallBack.getInstance().getOut().unspawnFences(_fences);
		
		unspawnMapGuards();
		unspawnNpcs();
		
		_event.notifyGameEnd(this, getWinner(), _arena);
	}

	@Override
	public void onDisconnect(PlayerEventInfo player) 
	{
		if(player != null && player.isOnline())
		{
			if(player.isSpectator())
			{
				removeSpectator(player, true);
				return;
			}
			
			EventWarnings.getInstance().addPoints(player.getPlayersId(), 1);
			
			broadcastMessage(LanguageEngine.getMsg("game_playerDisconnected", player.getPlayersName()), true);
			
			FixedPartyTeam team = (FixedPartyTeam) player.getEventTeam();
			
			player.restoreData();
			player.setXYZInvisible(player.getOrigLoc().getX(), player.getOrigLoc().getY(), player.getOrigLoc().getZ());

			if(!_aborted)
			{
				team.removePlayer(player);
				
				if(checkIfAllTeamsDisconnected())
				{
					broadcastMessage(LanguageEngine.getMsg("event_disconnect_all"), true);
					clearEvent();
					return;
				}
				
				final FixedPartyTeam lastTeam = checkLastAliveTeam();
				if(lastTeam != null)
				{
					CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							nextRound(false);
						}
					}, 3000);
				}
			}
		}
	}
	
	private boolean checkIfAllTeamsDisconnected()
	{
		int teamsOn = 0;
		
		for(FixedPartyTeam team : _teams)
		{
			for(PlayerEventInfo pi : team.getPlayers())
			{
				if(pi != null && pi.isOnline())
				{
					teamsOn ++;
					break;
				}
			}
		}
		
		return teamsOn == 0 || teamsOn == 1;
	}
	
	@Override
	protected void checkPlayersLoc()
	{
		//TODO check if player is not in another location
	}
	
	@Override
	protected void checkIfPlayersTeleported()
	{
		
	}
	
	private void cancelSchedulers()
	{
		// already cancelled
		if(_aborted)
			return;
		
		_aborted = true;
		
		CallbackManager.getInstance().eventEnded(1, getEvent().getEventType(), Arrays.asList(_teams));
		
		if(_announcer != null)
		{
			_announcer.cancel();
			_announcer = null;
		}
		
		if(_locChecker != null)
		{
			_locChecker.cancel(false);
			_locChecker = null;
		}
		
		if(_eventEnd != null)
		{
			_eventEnd.cancel(false);
			_eventEnd = null;
		}
		
		if(_roundStart != null)
		{
			_roundStart.cancel(false);
			_roundStart = null;
		}
	}
	
	@Override
	public int getReflectionId()
	{
		return _instanceId;
	}

	@Override
	public EventTeam[] getTeams()
	{
		return _teams;
	}

	@Override
	public EventPlayerData createPlayerData(PlayerEventInfo player)
	{
		return new PvPEventPlayerData(player, this, new GlobalStatsModel(_event.getEventType()));
	}

	@Override
	public PvPEventPlayerData getPlayerData(PlayerEventInfo player)
	{
		return (PvPEventPlayerData) player.getEventData();
	}
}

