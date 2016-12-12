package l2r.gameserver.nexus_engine.events.engine.mini.tournament;

import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventGame;
import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventManager;
import l2r.gameserver.nexus_engine.events.engine.mini.RegistrationData;
import l2r.gameserver.nexus_interface.PlayerEventInfo;


/**
 * @author hNoke
 */
public class TournamentManager
{
	private static Tournament _activeTournament = null;

	// configurations
	public static int _minTeamsCount;
	public static int _maxTeamsCount;
	public static int _timeForRegistration;
	public static int _rewardItemId;
	public static String _rewardsString;
	public static MiniEventManager _selectedEvent;

	// no instances
	public TournamentManager() {}

	public static void setTournamentEvent(PlayerEventInfo gm, MiniEventManager event)
	{
		_selectedEvent = event;
		gm.sendMessage("The event was successfully set to " + event.getEventType().getAltTitle());
	}

	public static void setTournamentTeamsCount(PlayerEventInfo gm, int teamsCount, boolean min)
	{
		gm.sendMessage("Suggested counts of teams are: 4, 8, 16, 32, 64 (the ideal count for a tournament); acceptable also are 6, 10, 12, 20, 24,.. but those matches will end up have one player/team skip his match, because he will have no oppononents to fight with.");

		if(teamsCount >= 4 && teamsCount % 2 == 0)
		{
			if(min)
			{
				_minTeamsCount = teamsCount;
			}
			else
			{
				_maxTeamsCount = teamsCount;
			}

			gm.sendMessage("Successfully set.");
		}
		else
			gm.sendMessage("Could not set, wrong number. It has to be dividable by 2.");
	}

	public static void setTimeForRegistration(PlayerEventInfo gm, int time)
	{
		if(time >= 1)
		{
			_timeForRegistration = time * 60000;
		}
		else
			gm.sendMessage("Time has to be longer than 1 (=1 minute).");
	}

	public static void setRewardString(PlayerEventInfo gm, String rewards)
	{
		_rewardsString = rewards;

		gm.sendMessage("The rewards were set successfully.");
	}
	
	public static void setRewardItemId(PlayerEventInfo gm, int id)
	{
		_rewardItemId = id;
		
		gm.sendMessage("The reward item Id updated.");
	}

	public static void startTournament(PlayerEventInfo gm)
	{
		if(_selectedEvent != null && _selectedEvent.checkCanRun())
		{
			Tournament tournament = new Tournament(_selectedEvent);

			tournament.setMaxCountToRegister(_maxTeamsCount);
			tournament.setMinCountToRegister(_minTeamsCount);
			tournament.setTimeForRegistration(_timeForRegistration);
			tournament.setRewardString(_rewardsString);
			tournament.setRewardItemId(_rewardItemId);

			tournament.openRegistration();

			_selectedEvent.getMode().setAllowed(true);
			_selectedEvent.setTournamentActive(true);
			
			_activeTournament = tournament;
		}
		else
		{
			gm.sendMessage("You must first select an event.");
		}
	}

	public static void onMatchEnd(MiniEventGame game, RegistrationData winner)
	{
		if(_activeTournament != null)
		{
			//System.out.println("ending match");
			_activeTournament.onMatchEnd(game, winner);
		}
	}

	public static void stopTournament(PlayerEventInfo gm)
	{
		if(_activeTournament == null)
		{
			if(gm != null)
				gm.sendMessage("There is no tournament active.");
		}
		else
		{
			if(_activeTournament.stopByGm(gm))
			{
				cleanTournament();

				if(gm != null)
					gm.sendMessage("Tournament successfully stopped.");
			}
		}
	}

	public static void cleanTournament()
	{
		_selectedEvent = null;
		_maxTeamsCount = 0;
		_minTeamsCount = 0;
		_timeForRegistration = 0;

		_activeTournament = null;
	}

	public static void skipRegistration(PlayerEventInfo gm)
	{
		if(_activeTournament != null)
		{
			if(_activeTournament.skipRegistration())
			{
				gm.sendMessage("Registration was skipped.");
			}
		}
	}

	public static void register(PlayerEventInfo player)
	{
		if(_activeTournament != null && _activeTournament.isOpenRegistration())
		{
			_activeTournament.register(player);
		}
	}

	public static MiniEventManager getEvent()
	{
		return _selectedEvent;
	}

	public static Tournament getTournament()
	{
		return _activeTournament;
	}
}
