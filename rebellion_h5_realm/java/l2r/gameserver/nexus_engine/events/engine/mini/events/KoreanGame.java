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
import l2r.gameserver.nexus_engine.events.engine.team.KoreanTeam;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.callback.CallbackManager;
import l2r.gameserver.nexus_interface.delegate.CharacterData;
import l2r.gameserver.nexus_interface.delegate.SkillData;
import l2r.gameserver.skills.AbnormalEffect;

import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;


/**
 * @author hNoke
 *
 */
public class KoreanGame extends MiniEventGame implements Runnable
{
	private KoreanTeam[] _teams;
	private boolean _initState;
	
	private ScheduledFuture<?> _eventEnd;
	private ScheduledFuture<?> _roundStart;
	
	public KoreanGame(int gameId, EventMap arena, KoreanManager event, RegistrationData[] teams)
	{
		super(gameId, arena, event, teams);
		
		_initState = true;
		final int teamsAmmount = 2;
		
		_teams = new KoreanTeam[teamsAmmount];
		
		for(int i = 0; i < teamsAmmount; i++)
		{
			_teams[i] = new KoreanTeam(teams[i], i + 1, teams[i].getKeyPlayer().getPlayersName() + "'s party");
			
			for(PlayerEventInfo pi : teams[i].getPlayers())
			{
				pi.onEventStart(this);
				_teams[i].addPlayer(pi, true);
			}
		}
		
		CallbackManager.getInstance().eventStarts(1, getEvent().getEventType(), Arrays.asList(_teams));
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
		try
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

			EventSpawn spawn;
			
			for(KoreanTeam team : _teams)
			{
				for(PlayerEventInfo pi : team.getPlayers())
				{
					spawn = _arena.getNextSpawn(team.getTeamId(), SpawnType.Safe);

					pi.setLastTeleportedSpawn(spawn);
					pi.teleport(spawn.getLoc(), 0, _instanceId);
					
					pi.disableAfkCheck(true);
					
					if(getEvent().getBoolean("removeCubics"))
						pi.removeCubics();
					
					if(_allowSchemeBuffer)
						EventBuffer.getInstance().buffPlayer(pi, true);

					checkItems(pi);
					
					pi.enableAllSkills();
					
					// Achievements
					if (Config.ENABLE_PLAYER_COUNTERS)
						pi.getOwner().getCounters().addPoint("_Mini_Events");
				}
			}

			scheduleMessage(LanguageEngine.getMsg("game_teleportDone"), 1500, true);
			
			handleDoorsAndItems(1);
			
			int startTime = _event.getMapConfigInt(_arena, "WaitTime");

			_roundStart = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					finishRoundStart();
				}
			}, startTime);
			
			scheduleMessage(LanguageEngine.getMsg("game_matchStartsIn", (startTime / 1000)), 5000, true);
		}
		catch (Exception e)
		{
			abortDueToError("Map wasn't set up correctly.");
			e.printStackTrace();
		}
	}
	
	private void finishRoundStart()
	{
		if(_aborted)
			return;

		if(getEvent().getBoolean("TeleportChecker"))
		{
			for(EventTeam team : getTeams())
			{
				for(PlayerEventInfo pi : team.getPlayers())
				{
					if(!pi.checkIfNearbyLastTeleport())
					{
						try
						{
							clearEvent();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						if(_aborted)
							return;
					}
				}
			}
		}

		broadcastMessage(LanguageEngine.getMsg("game_korean_teleportingToArena"), true);
		
		unspawnBuffers();
		
		handleDoorsAndItems(2);
		
		teleportToEventLocation();
		
		_initState = false;
		
		final PlayerEventInfo player1 = getNextPlayer(1);
		final PlayerEventInfo player2 = getNextPlayer(2);

		scheduleMessage(LanguageEngine.getMsg("game_korean_nextFight", player1.getPlayersName(), player2.getPlayersName(), 8), 3000, true);
		
		CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				startFight(player1, player2);
			}
		}, 11000);

		startAnnouncing();
	}
	
	private void startFight(PlayerEventInfo player1, PlayerEventInfo player2)
	{
		if(_aborted)
			return;
		
		SkillData skill = new SkillData(5965, 1);
		
		player1.disableAfkCheck(false);
		player1.unblock();
		player1.setIsParalyzed(false);
		player1.setIsInvul(false);
		player1.stopAbnormalEffect(AbnormalEffect.STEALTH);
		player1.broadcastSkillUse(null, null, skill.getId(), skill.getLevel());
		player1.startAbnormalEffect(AbnormalEffect.REAL_TARGET);
		
		player2.disableAfkCheck(false);
		player2.unblock();
		player2.setIsParalyzed(false);
		player2.setIsInvul(false);
		player2.stopAbnormalEffect(AbnormalEffect.STEALTH);
		player2.broadcastSkillUse(null, null, skill.getId(), skill.getLevel());
		player2.startAbnormalEffect(AbnormalEffect.REAL_TARGET);
		
		broadcastMessage(LanguageEngine.getMsg("game_korean_fightStarted"), true);
	}
	
	private void teleportToEventLocation()
	{
		try
		{
			EventSpawn spawn;
			for(KoreanTeam team : _teams)
			{
				for(PlayerEventInfo member : team.getPlayers())
				{
					spawn = _arena.getNextSpawn(team.getTeamId(), SpawnType.Regular);

					member.setLastTeleportedSpawn(spawn);
					member.teleport(spawn.getLoc(), 0, -1);
					
					member.setIsInvul(true);
					member.block();
					member.setIsParalyzed(true);
					member.startAbnormalEffect(AbnormalEffect.STEALTH);
				}
			}
		} 
		catch (Exception e)
		{
			abortDueToError("Map wasn't propably set up correctly.");
			e.printStackTrace();
		}
		
	}
	
	private PlayerEventInfo getNextPlayer(int teamId)
	{
		return _teams[teamId - 1].getNextPlayer();
	}
	
	@Override
	public void onDie(final PlayerEventInfo player, CharacterData killer)
	{
		if(_aborted)
			return;
		
		updateScore(player, killer);
		
		player.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
		
		if(player.getEventTeam().getDeaths() >= player.getEventTeam().getPlayers().size())
		{
			CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					endByDie(oppositeTeam(player.getEventTeam()));
				}
			}, 3000);
		}
		else // unroots another player
		{
			final PlayerEventInfo nextPlayer = ((KoreanTeam) player.getEventTeam()).getNextPlayer();
			
			CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					announceNextPlayer(nextPlayer);
				}
			}, 3000);
		}
	}
	
	private void announceNextPlayer(PlayerEventInfo nextPlayer)
	{
		SkillData skill = new SkillData(5965, 1);
		
		nextPlayer.unblock();
		nextPlayer.setIsParalyzed(false);
		nextPlayer.setIsInvul(false);
		nextPlayer.stopAbnormalEffect(AbnormalEffect.STEALTH);
		nextPlayer.broadcastSkillUse(null, null, skill.getId(), skill.getLevel());
		nextPlayer.startAbnormalEffect(AbnormalEffect.REAL_TARGET);
		
		broadcastMessage(LanguageEngine.getMsg("game_korean_nextPlayer", nextPlayer.getPlayersName()), false);
	}

	private void endByTime()
	{
		if(_aborted)
			return;
		
		cancelSchedulers();
		
		broadcastMessage(LanguageEngine.getMsg("game_matchEnd_timeLimit", getGameTime() / 60000), false);
		scheduleMessage(LanguageEngine.getMsg("game_matchEnd_tie"), 3000, false);

		int topScore = 0;
		KoreanTeam top = null;
		for(KoreanTeam team : _teams)
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
		
		CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				clearEvent();
			}
		}, 8000);
		
		return;
	}

	private void endByDie(EventTeam winner)
	{
		cancelSchedulers();
		
		broadcastMessage(LanguageEngine.getMsg("game_korean_winner", winner.getTeamName()), false);

		setWinner(winner);

		for(PlayerEventInfo pi : winner.getPlayers())
		{
			if(pi.isOnline())
			{
				EventRewardSystem.getInstance().rewardPlayer(getEvent().getEventType(), getEvent().getMode().getModeId(), pi, RewardPosition.Winner, null, pi.getTotalTimeAfk(), 0, 0);
				setEndStatus(pi, 1);
				//showScore(pi, 1);
			}

			getPlayerData(pi).getGlobalStats().raise(GlobalStatType.WINS, 1);
			_event.logPlayer(pi, 1);
		}
		
		for(PlayerEventInfo pi : oppositeTeam(winner).getPlayers())
		{
			if(pi.isOnline())
			{
				EventRewardSystem.getInstance().rewardPlayer(getEvent().getEventType(), getEvent().getMode().getModeId(), pi, RewardPosition.Looser, null, pi.getTotalTimeAfk(), 0, 0);
				setEndStatus(pi, 0);
				//showScore(pi, 2);
			}
			
			getPlayerData(pi).getGlobalStats().raise(GlobalStatType.LOSES, 1);
			_event.logPlayer(pi, 2);
		}
		
		saveGlobalStats();

		CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				clearEvent();
			}
		}, 5000);
	}

	@Override
	public void clearEvent()
	{
		cancelSchedulers();
		
		cleanSpectators();
		
		applyStatsChanges();
		
		for(KoreanTeam team : _teams)
		{
			for(PlayerEventInfo pi : team.getPlayers())
			{
				if(pi.isOnline())
				{
					if(pi.isBlocked())
						pi.unblock();
					
					if(pi.isParalyzed())
						pi.setIsParalyzed(false);
					
					pi.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
					pi.stopAbnormalEffect(AbnormalEffect.STEALTH);
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
	public void onDisconnect(final PlayerEventInfo player)
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
			
			final EventTeam playerTeam = player.getEventTeam();
			playerTeam.removePlayer(player);

			try
			{
				if(player.isParalyzed())
					player.setIsParalyzed(false);
				
				if(player.isBlocked())
					player.unblock();
				
				if(!player.isGM())
					player.setIsInvul(false);
				
				player.restoreData();
				player.teleport(player.getOrigLoc(), 0, 0);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			//player.restoreData();
			player.setXYZInvisible(player.getOrigLoc().getX(), player.getOrigLoc().getY(), player.getOrigLoc().getZ());
			
			if(!_aborted)
			{
				if(playerTeam.getPlayers().isEmpty() || !checkTeamStatus(playerTeam.getTeamId()))
				{
					cancelSchedulers();
					
					CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							broadcastMessage(LanguageEngine.getMsg("event_disconnect_all"), false);
							if(_initState)
								clearEvent();
							else 
								endByDie(oppositeTeam(playerTeam));
						}
					}, 3000);
				}
				else if(!_initState)
				{
					if(((KoreanTeam) playerTeam).isFighting(player))
					{
						final PlayerEventInfo nextPlayer = ((KoreanTeam) playerTeam).getNextPlayer();
						
						if(nextPlayer == null)
						{
							cancelSchedulers();
							
							CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
							{
								@Override
								public void run()
								{
									broadcastMessage(LanguageEngine.getMsg("event_disconnect_all"), false);
									endByDie(oppositeTeam(playerTeam));
								}
							}, 5000);
						}
						else
						{
							CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
							{
								@Override
								public void run()
								{
									announceNextPlayer(nextPlayer);
								}
							}, 5000);
						}
					}
				}
			}
		}
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
	
	private KoreanTeam oppositeTeam(EventTeam team)
	{
		if(team.getTeamId() == 1)
			return _teams[1]; // returns second team
		else if(team.getTeamId() == 2)
			return _teams[0]; // returns first team
		return null;
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

