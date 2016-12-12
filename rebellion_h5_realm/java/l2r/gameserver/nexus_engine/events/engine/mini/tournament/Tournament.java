package l2r.gameserver.nexus_engine.events.engine.mini.tournament;

import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventGame;
import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventManager;
import l2r.gameserver.nexus_engine.events.engine.mini.RegistrationData;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastTable;
import javolution.util.FastMap;

/**
 * Created by Lukas
 * TODO turn RegistrationData.message into html windows
 * TODO add info about upcoming matches, score board, etc
 * TODO rewrite announcements
 */
public class Tournament
{
	/**
	 * sorts participants by their wonMatches, descending
	 */
	public Comparator<Participant> compareParticipantsByWonMatches = new Comparator<Participant>()
	{
		@Override
		public int compare(Participant t1, Participant t2)
		{
			int wins1 = t1._numberOfWins;
			int wins2 = t2._numberOfWins;

			return wins1 == wins2 ? 0 : wins1 < wins2 ? 1 : -1;
		}
	};

	private MiniEventManager _event = null;
	private int _minCountToRegister = 2;
	private int _maxCountToRegister = 4;
	private int _rewardItemId = 57;
	private String _rewardString = null;

	private boolean _registrationOpen;
	@SuppressWarnings("unused")
	private boolean _matchesInProgress;

	private List<Participant> _originallyRegistered;
	private List<Participant> _registered;
	private int _timeForRegistration;

	private ScheduledFuture<?> _registrationEnd = null;
	private List<MiniEventGame> _activeMatches;

	private ScheduledFuture<?> _matchTimeLimit = null;

	public Tournament(MiniEventManager event)
	{
		_event = event;
		_registrationOpen = false;
		_matchesInProgress = false;
	}

	public void setMinCountToRegister(int countToRegister)
	{
		_minCountToRegister = countToRegister;
	}

	public void setMaxCountToRegister(int countToRegister)
	{
		_maxCountToRegister = countToRegister;
	}

	public void setTimeForRegistration(int timeForRegistration)
	{
		_timeForRegistration = timeForRegistration;
	}

	public void setRewardString(String s)
	{
		_rewardString = s;
	}
	
	public void setRewardItemId(int id)
	{
		_rewardItemId = id;
	}

	public void openRegistration()
	{
		_registered = new FastTable<>();
		_registrationOpen = true;

		_event.setTournamentActive(true);

		announce("The registration for the tournament has been opened for " + _timeForRegistration / 60000 + " minutes.");

		_registrationEnd = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				closeRegistration();
			}
		}, _timeForRegistration);
	}

	public boolean skipRegistration()
	{
		boolean success = false;
		if(_registrationOpen && _event != null)
		{
			if(_registrationEnd != null)
			{
				_registrationEnd.cancel(false);
				_registrationEnd = null;
			}

			closeRegistration();
			success = true;
		}
		return success;
	}

	public void closeRegistration()
	{
		_registrationOpen = false;

		//_event.setTournamentActive(false);

		announce("The tournament registration has been closed.");

		CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				startMatches();
			}
		}, 10000);
	}

	public boolean canStartTournament()
	{
		return _registered.size() >= _minCountToRegister;
	}

	public void startMatches()
	{
		if(canStartTournament())
		{
			announce("The tournament has started! The matches will begin soon.");

			// will be used when giving rewards
			_originallyRegistered = new FastTable<>();
			_originallyRegistered.addAll(_registered);

			nextRound(false);
		}
		else
		{
			announce("There is not enought teams in the tournament.");
			announce("The tournament has been aborted.");
			TournamentManager.stopTournament(null);
		}
	}

	private void startTimeLimit()
	{
		_matchTimeLimit = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				abortMatches("Match aborted due to time limit of 15 minutes.");
			}
		}, 900000);
	}

	private void abortTimeLimit()
	{
		if(_matchTimeLimit != null)
		{
			_matchTimeLimit.cancel(false);
			_matchTimeLimit = null;
		}
	}

	private void abortMatches(String reason)
	{
		if(_activeMatches == null)
			return;

		for(MiniEventGame game : _activeMatches)
		{
			if(game != null)
			{
				game.abortDueToError(reason);
			}
		}
	}

	public void nextRound(boolean removeNotWon)
	{
		_activeMatches = new FastTable<MiniEventGame>();

		// first remove invalid participants and those who lost their previous match
		for(Participant participant : _registered)
		{
			if(!participant.canFight())
			{
				participant._data.message("You've been removed from the event because you don't meet the tournament conditions anymore.", true);
				participant._data.register(false, null);

				_registered.remove(participant);
			}

			if(removeNotWon)
			{
				if(!participant._wonMatch)
				{
					participant._data.message("You dropped out from the tournament.", true);
					participant._data.register(false, null);

					_registered.remove(participant);
				}
			}

			participant._data.register(true, _event);

			participant.notifyNewMatch();
		}

		if(_registered.size() < 2)
		{
			finishTournament();
			return;
		}

		List<Matched> matchedList = new FastTable<Matched>();

		List<Participant> paired = new FastTable<>();
		
		List<Participant> toFight = new FastTable<>();
		toFight.addAll(_registered);
		Collections.shuffle(toFight);
		
		Participant opponent = null;
		for(Participant participant : toFight)
		{
			if(participant == null)
				continue;
			
			if(paired.contains(participant))
				continue;
			
			opponent = null;
			
			for(Participant possibleOpponent : toFight)
			{
				if(possibleOpponent == null)
					continue;
				
				if(paired.contains(possibleOpponent))
					continue;
				
				if(possibleOpponent._data.getKeyPlayer().getPlayersId() == participant._data.getKeyPlayer().getPlayersId())
					continue;

				opponent = possibleOpponent;
				break;
			}
			
			if(opponent != null)
			{
				paired.add(participant);
				paired.add(opponent);

				Matched matched = new Matched(participant, opponent);
				matchedList.add(matched);
			}
		}

		// remaining players who didn't receive any opponent
		for(Participant participant : toFight)
		{
			if(participant == null || paired.contains(participant))
				continue;
			
			participant.setWonMatch();
			participant._data.message("There is no opponent for you in this round, you will automatically advance to the next round. Please wait till the current matches end.", false);
		}

		String roundName = TournamentRoundName.getRoundName(_registered.size());
		announce(roundName + " has just started.");
		for(Matched matched : matchedList)
		{
			startMatch(matched);
		}

		startTimeLimit();

		_matchesInProgress = true;
	}

	@SuppressWarnings("unused")
	private Participant findOpponent(Participant challenger, List<Participant> toFight)
	{
		for(Participant opponent : toFight)
		{
			if(opponent._data.getKeyPlayer().getPlayersId() == challenger._data.getKeyPlayer().getPlayersId())
				continue;

			return opponent;
		}

		return null;
	}

	private class Matched
	{
		Participant _p1, _p2;

		private Matched(Participant p1, Participant p2)
		{
			_p1 = p1;
			_p2 = p2;
		}
	}

	private void startMatch(Matched matched)
	{
		List<RegistrationData> players = new FastTable<RegistrationData>();

		players.add(matched._p1._data);
		players.add(matched._p2._data);

		_event.launchGame(players.toArray(new RegistrationData[players.size()]), null);
	}

	public void onMatchEnd(MiniEventGame game, RegistrationData winner)
	{
		if(game.getEvent().getEventType() != _event.getEventType())
			return;
		
		// happens mostly if both teams did nothing during the event
		if(winner == null)
		{
			if(game.getRegisteredTeams() != null)
			{
				for(RegistrationData data : game.getRegisteredTeams())
				{
					if(data != null)
					{
						data.message("The last match didn't have a winner, so you've been removed from the tournament. Try harder next time!", true);

						// remove the team from the tournament
						Participant participant = getParticipantData(data);
						if(participant != null)
						{
							participant._data.message("You have been unregistered from the tournament.", false);
							participant._data.register(false, null);

							_registered.remove(participant);
						}
					}
				}
			}
		}
		// set this player as a winner
		else
		{
			Participant winnerData = getParticipantData(winner);

			winnerData.setWonMatch();
		}
		
		_activeMatches.remove(game);
		
		if(_activeMatches.isEmpty())
		{
			announce("All tournament matches have finished.");
			abortTimeLimit();

			_matchesInProgress = false;

			if(_registered.size() >= 2)
			{
				announce("The next round will begin in 60 seconds.");

				CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						nextRound(true);
					}
				}, 60000);
			}
			else if(_registered.size() == 1)
			{
				finishTournament();
			}
			else
				clean();
		}
	}

	public void finishTournament()
	{
		_matchesInProgress = false;

		rewardParticipants();
	}

	public void rewardParticipants()
	{
		announce("The tournament has ended.");
		announce("The participants have received their rewards.");

		Collections.sort(_originallyRegistered, compareParticipantsByWonMatches);

		Map<Integer, Integer> rewards = new FastMap<Integer, Integer>();

		// format for rewards: POSITION1:COUNT_OF_MEDALS;POSITION2-POSITION3:COUNT_OF_MEDALS
		String[] positions = _rewardString.split(";");

		String positionNumber;
		int rewardCount;

		for(String position : positions)
		{
			if(position.length() == 0)
				continue;

			try
			{
				positionNumber = position.split(":")[0];
				rewardCount = Integer.parseInt(position.split(":")[1]);

				// format #-#
				if(positionNumber.split("-").length == 2)
				{
					int from = Integer.parseInt(positionNumber.split("-")[0]);
					int to = Integer.parseInt(positionNumber.split("-")[1]);

					for(int i = from; from <= to; i++)
					{
						rewards.put(i, rewardCount);
					}
				}
				// format #
				else
				{
					rewards.put(Integer.parseInt(positionNumber), rewardCount);
				}
			}
			catch (Exception e )
			{
				e.printStackTrace();
			}
		}

		int position = 0;
		for(Participant participant : _originallyRegistered)
		{
			position ++;

			if(position == 1 && participant._numberOfWins < 2)
			{
				// the tournament was invalid, all teams most likely disconnected
				return;
			}
			else if(participant._numberOfWins == 0)
				continue;

			//TODO html result
			participant._data.message("You or your team has beaten " + participant._numberOfWins + " opponents.", false);

			if(rewards.containsKey(position))
			{
				rewardCount = rewards.get(position);

				for(PlayerEventInfo player : participant._data.getPlayers())
				{
					if(player != null)
						player.addItem(_rewardItemId, rewardCount, true);
				}
			}
		}
	}

	public boolean stopByGm(PlayerEventInfo gm)
	{
		//if(gm == null || _registrationOpen || _matchesInProgress)
		{
			if(gm != null)
				abortMatches("The tournament aborted by a GM.");
			else
				abortMatches("The tournament aborted.");

			for(Participant participant : _registered)
			{
				participant._data.message("The tournament has been aborted.", true);
				participant._data.register(false, null);

				_registered.remove(participant);
			}

			return true;
		}

		//gm.sendMessage("Can't be aborted now.");
		//return false;
	}

	public void clean()
	{
		_event.setTournamentActive(false);

		TournamentManager.cleanTournament();
	}

	public boolean isOpenRegistration()
	{
		return _registrationOpen;
	}

	public void register(PlayerEventInfo player)
	{
		if(_registrationOpen && _event != null)
		{
			if(_registered.size() >= _maxCountToRegister)
			{
				player.sendMessage("The tournament is already full.");
				return;
			}

			if(!_event.checkCanRegister(player))
			{
				player.sendMessage("You may not register.");
				return;
			}

			RegistrationData regData = _event.createRegistrationData(player);
			regData.register(true, _event);

			Participant participant = new Participant(regData);

			_registered.add(participant);
			regData.message("You've been registered to the tournament.", true);
		}
	}

	public void unregister(PlayerEventInfo player, boolean forced)
	{
		if(!forced)
		{
			if(_registrationOpen && _event != null)
			{
				if(_event.checkCanUnregisterTeam(player))
				{
					for(Participant participant : _registered)
					{
						if(participant._data.getKeyPlayer().getPlayersId() == player.getPlayersId())
						{
							participant._data.message("You've been unregistered from the tournament.", true);
							participant._data.register(false, null);

							_registered.remove(participant);
						}
					}
				}
			}
		}
		else
		{
			if(_event != null)
			{
				for(Participant participant : _registered)
				{
					if(participant._data.getKeyPlayer().getPlayersId() == player.getPlayersId())
					{
						participant._data.message("You've been unregistered from the tournament.", true);
						participant._data.register(false, null);

						_registered.remove(participant);
					}
				}
			}
		}
	}

	public void announce(String text)
	{
		CallBack.getInstance().getOut().announceToAllScreenMessage(text, _event.getMode().getVisibleName());
	}

	public Participant getParticipantData(RegistrationData data)
	{
		for(Participant participant : _registered)
		{
			if(participant._data.getId() == data.getId() || (participant._data.getKeyPlayer() != null && data.getKeyPlayer() != null && participant._data.getKeyPlayer().getPlayersId() == data.getKeyPlayer().getPlayersId()))
			{
				return participant;
			}
		}
		return null;
	}
	
	public int getRegisteredSize()
	{
		return _registered.size();
	}
	
	public List<MiniEventGame> getActiveMatches()
	{
		return _activeMatches;
	}

	public class Participant
	{
		private RegistrationData _data;
		private boolean _wonMatch;
		private int _numberOfWins;

		public Participant(RegistrationData data)
		{
			_data = data;
			_wonMatch = false;
			_numberOfWins = 0;
		}

		// returns false if players disconnected, etc
		public boolean canFight()
		{
			return _event != null && _event.canFight(_data);
		}

		//TODO call this before starting a match
		public void notifyNewMatch()
		{
			_wonMatch = false;
		}

		public void setWonMatch()
		{
			_wonMatch = true;
			_numberOfWins ++;
		}
	}
}
