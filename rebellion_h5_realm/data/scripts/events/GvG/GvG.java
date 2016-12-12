package events.GvG;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.lang.reference.HardReference;
import l2r.commons.lang.reference.HardReferences;
import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.utils.Location;

import instances.GvGInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GvG extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(GvG.class);

	public static final Location TEAM1_LOC = new Location(139736, 145832, -15264); // Team location after teleportation
	public static final Location TEAM2_LOC = new Location(139736, 139832, -15264);
	public static final Location RETURN_LOC = new Location(43816, -48232, -822);
	public static final int[] everydayStartTime =
	{
		Config.GvG_HOUR_START,
		Config.GvG_MINUTE_START,
		00
	}; // hh mm ss
	
	private static boolean _active = false;
	private static boolean _isRegistrationActive = false;

	private static int _minLevel = Config.GVG_MIN_LEVEL;
	private static int _maxLevel = Config.GVG_MAX_LEVEL;
	private static int _groupsLimit = Config.GVG_MAX_GROUPS; // Limit of groups can register
	private static int _minPartyMembers = Config.GVG_MIN_PARTY_MEMBERS; // self-explanatory
	private static long regActiveTime = Config.GVG_TIME_TO_REGISTER * 60 * 1000L; // Timelimit for registration

	private static ScheduledFuture<?> _globalTask;
	private static ScheduledFuture<?> _regTask;
	private static ScheduledFuture<?> _countdownTask1;
	private static ScheduledFuture<?> _countdownTask2;
	private static ScheduledFuture<?> _countdownTask3;
	
	private static List<HardReference<Player>> leaderList = new CopyOnWriteArrayList<HardReference<Player>>();

	public static class RegTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			prepare();
		}
	}

	public static class Countdown extends RunnableImpl
	{
		int _timer;

		public Countdown(int timer)
		{
			_timer = timer;
		}

		@Override
		public void runImpl() throws Exception
		{
			Announcements.getInstance().announceToAll("GvG: Until the end of the applications for the tournament remains " + Integer.toString(_timer) + " min(s).");
		}
	}

	@Override
	public void onLoad()
	{
		if (!Config.ENABLE_GVG_EVENT)
			return;
		
		_log.info("Loaded Event: GvG");
		initTimer();
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	private static void initTimer()
	{
		long day = 24 * 60 * 60000L;
		Calendar ci = Calendar.getInstance();
		ci.set(Calendar.HOUR_OF_DAY, everydayStartTime[0]);
		ci.set(Calendar.MINUTE, everydayStartTime[1]);
		ci.set(Calendar.SECOND, everydayStartTime[2]);

		long delay = ci.getTimeInMillis() - System.currentTimeMillis();
		if(delay < 0)
			delay = delay + day;

		if(_globalTask != null)
			_globalTask.cancel(true);
		_globalTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Launch(), delay, day);
	}
	
	public static class Launch extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			activateEvent();
		}
	}
	
	private static boolean canBeStarted()
	{
		for(Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress())
				return false;
		return true;
	}

	private static boolean isActive()
	{
		return _active;
	}

	public void activateEventGMPanel()
	{
		Player player = getSelf();
		if (!player.isGM())
			return;
		
		if (!isActive())
		{
			initTimer();
			ServerVariables.set("GvG", "on");
			_log.info("Event 'GvG' activated.");
		}
		else
			player.sendMessage("Event 'GvG' already active.");
		
		show("admin/events/events.htm", player);
	}
	
	public static void activateEvent()
	{
		if(!isActive() && canBeStarted())
		{
			_regTask = ThreadPoolManager.getInstance().schedule(new RegTask(), regActiveTime);
			if(regActiveTime > 2 * 60000L) //display countdown announcements only when timelimit for registration is more than 3 mins
			{
				if(regActiveTime > 5 * 60000L)
					_countdownTask3 = ThreadPoolManager.getInstance().schedule(new Countdown(5), regActiveTime - 300 * 1000);

				_countdownTask1 = ThreadPoolManager.getInstance().schedule(new Countdown(2), regActiveTime - 120 * 1000);
				_countdownTask2 = ThreadPoolManager.getInstance().schedule(new Countdown(1), regActiveTime - 60 * 1000);
			}
			//ServerVariables.set("GvG", "on");
			_log.info("Event 'GvG' activated.");
			Announcements.getInstance().announceToAll("Registration for GvG Tournament has begun!");
			Announcements.getInstance().announceToAll("Applications will be accepted next " + regActiveTime / 60000 + " min(s).");
			_active = true;
			_isRegistrationActive = true;
		}
	}

	/**
	 * Cancels the event during registration time
	 */
	public static void deactivateEvent()
	{
		if(isActive())
		{
			stopTimers();
			ServerVariables.unset("GvG");
			_log.info("Event 'GvG' cancelled.");
			Announcements.getInstance().announceToAll("GvG: Tournament cancelled.");
			_active = false;
			_isRegistrationActive = false;
			leaderList.clear();
		}
	}

	/**
	 * Shows groups and their leaders who's currently in registration list
	 */
	public void showStats()
	{
		Player player = getSelf();

		if (!player.isGM())
			return;
		
		if(!isActive())
		{
			player.sendMessage(new CustomMessage("scripts.events.gvg.notstarted", player));
			return;
		}

		StringBuilder string = new StringBuilder();
		String refresh = "<button value=\"Refresh\" action=\"bypass -h scripts_events.GvG.GvG:showStats\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		String start = "<button value=\"Start Now\" action=\"bypass -h scripts_events.GvG.GvG:startNow\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		int i = 0;

		if(!leaderList.isEmpty())
		{
			for(Player leader : HardReferences.unwrap(leaderList))
			{
				if(!leader.isInParty())
					continue;
				string.append("*").append(leader.getName()).append("*").append(" | group members: ").append(leader.getParty().size()).append("\n\n");
				i++;
			}
			show("There are " + i + " group leaders who registered for the event:\n\n" + string + "\n\n" + refresh + "\n\n" + start, player, null);
		}
		else
			show("There are no participants at the time\n\n" + refresh, player, null);
	}

	public void startNow()
	{
		Player player = getSelf();
		
		if (!player.isGM())
			return;
		
		if(!isActive() || !canBeStarted())
		{
			player.sendMessage(new CustomMessage("scripts.events.gvg.notstarted", player));
			return;
		}

		prepare();
	}

	/**
	 * Handles the group applications and apply restrictions
	 */
	public void addGroup()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!_isRegistrationActive)
		{
			player.sendMessage(new CustomMessage("scripts.events.gvg.not_active", player));
			return;
		}

		if (!player.isGM())
		{
			if(leaderList.contains(player.getRef()))
			{
				player.sendMessage(new CustomMessage("scripts.events.gvg.already_registered", player));
				return;
			}

			if(!player.isInParty())
			{
				player.sendMessage(new CustomMessage("scripts.events.gvg.requirements.party_needed", player));
				return;
			}

			if(!player.getParty().isLeader(player))
			{
				player.sendMessage(new CustomMessage("scripts.events.gvg.requirements.partyleader", player));
				return;
			}
			if(player.getParty().isInCommandChannel())
			{
				player.sendMessage(new CustomMessage("scripts.events.gvg.requirements.leavecc", player));
				return;
			}

			if(leaderList.size() >= _groupsLimit)
			{
				player.sendMessage(new CustomMessage("scripts.events.gvg.requirements.limit_reached", player));
				return;
			}
			
			String[] abuseReason = {
				"is not online.",
				"is not in the party.",
				"not enough players in the party. Minimum players - 6.",
				"is not party leader to register.",
				"doesn't meet the minimum/maximum level requirements.",
				"is mounted on a pet.",
				"is playing in a duel.",
				"is a participant of some other type of an event.",
				"is participating in olympiad games.",
				"is currently teleporting, please try again after he teleports.",
				"is participating in the Dimensional Rift.",
				"is equipped with a cursed weapon.",
				"is not in peace zone.",
				"is observing.",
				"is in an instance.",
				"is in Nexus."};

			for(Player eachmember : player.getParty())
			{
				int abuseId = checkPlayer(eachmember, false);
				if(abuseId != 0)
				{
					player.sendMessage(new CustomMessage("scripts.events.gvg.abuse", player, eachmember.getName(), abuseReason[abuseId - 1]));
					return;
				}
			}
		}

		leaderList.add(player.getRef());
		player.getPlayerGroup().sendMessage("Your group was added to the tournament!");
	}

	public void removeGroup(String[] param)
	{
		if (param == null)
			return;
		
		String playerName = param[0];
		
		if (playerName == null || playerName.isEmpty())
			return;
		
		Player plr = GameObjectsStorage.getPlayer(playerName);
		if (plr != null)
		{
			if (leaderList.contains(plr.getRef()))
			{
				leaderList.remove(plr.getRef());
				plr.getPlayerGroup().sendMessage("Unregistered from GvG event!");
			}
		}
		
	}
	
	private static void stopTimers()
	{
		if(_regTask != null)
		{
			_regTask.cancel(false);
			_regTask = null;
		}
		if(_countdownTask1 != null)
		{
			_countdownTask1.cancel(false);
			_countdownTask1 = null;
		}
		if(_countdownTask2 != null)
		{
			_countdownTask2.cancel(false);
			_countdownTask2 = null;
		}
		if(_countdownTask3 != null)
		{
			_countdownTask3.cancel(false);
			_countdownTask3 = null;
		}
	}

	private static void prepare()
	{
		checkPlayers();
		shuffleGroups();

		if(isActive())
		{
			stopTimers();
			//ServerVariables.unset("GvG");
			_active = false;
			_isRegistrationActive = false;
		}

		if(leaderList.size() < 2)
		{
			leaderList.clear();
			Announcements.getInstance().announceToAll("GvG: Tournament is cancelled due to lack of participants.");
			return;
		}

		Announcements.getInstance().announceToAll("GvG: Receipt of applications is completed. Starting the tournament.");
		start();
	}

	/**
	 * @param player
	 * @param doCheckLeadership
	 * @return
	 * Handles all limits for every group member. Called 2 times: when registering group and before sending it to the instance
	 */
	private static int checkPlayer(Player player, boolean doCheckLeadership)
	{
		if(!player.isOnline())
			return 1;

		if(!player.isInParty())
			return 2;

		if(doCheckLeadership && (player.getParty() == null || !player.getParty().isLeader(player)))
			return 4;

		if(player.getParty() == null || player.getParty().size() < _minPartyMembers)
			return 3;

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
			return 5;

		if(player.isMounted())
			return 6;

		if(player.isInDuel())
			return 7;

		if(player.getTeam() != TeamType.NONE)
			return 8;

		if(player.getOlympiadGame() != null || Olympiad.isRegistered(player))
			return 9;

		if(player.isTeleporting())
			return 10;

		if(player.getParty().isInDimensionalRift())
			return 11;

		if(player.isCursedWeaponEquipped())
			return 12;

		if(!player.isInPeaceZone())
			return 13;

		if(player.isInObserverMode())
			return 14;
		
		if (NexusEvents.isRegistered(player) || NexusEvents.isInEvent(player))
			return 15;

		return 0;
	}

	/**
	 * @return
	 * Shuffles groups to separate them in two lists of equals size
	 */
	private static void shuffleGroups()
	{
		if(leaderList.size() % 2 != 0) // If there are odd quantity of groups in the list we should remove one of them to make it even
		{
			int rndindex = Rnd.get(leaderList.size());
			Player expelled = leaderList.remove(rndindex).get();
			if(expelled != null)
				expelled.sendMessage(new CustomMessage("scripts.events.gvg.requirements.expelled", expelled));
		}

		//Перемешиваем список
		for(int i = 0; i < leaderList.size(); i++)
		{
			int rndindex = Rnd.get(leaderList.size());
			leaderList.set(i, leaderList.set(rndindex, leaderList.get(i)));
		}
	}

	private static void checkPlayers()
	{
		for(Player player : HardReferences.unwrap(leaderList))
		{
			if(checkPlayer(player, true) != 0)
			{
				leaderList.remove(player.getRef());
				continue;
			}

			for(Player partymember : player.getParty())
			{
				if(checkPlayer(partymember, false) != 0)
				{
					player.sendMessage(new CustomMessage("scripts.events.gvg.requirements.disqualified", player));
					leaderList.remove(player.getRef());
					break;
				}
			}
		}
	}

	public static void updateWinner(Player winner)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO event_data(charId, score) VALUES (?,1) ON DUPLICATE KEY UPDATE score=score+1");
			statement.setInt(1, winner.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private static void start()
	{
		int instancedZoneId = 504;
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if(iz == null)
		{
			_log.warn("GvG: InstanceZone : " + instancedZoneId + " not found!");
			return;
		}
		
		for(int i = 0; i < leaderList.size(); i += 2)
		{
			Player team1Leader = leaderList.get(i).get();
			Player team2Leader = leaderList.get(i + 1).get();
			
			GvGInstance r = new GvGInstance();
			r.setTeam1(team1Leader.getParty());
			r.setTeam2(team2Leader.getParty());
			r.init(iz);
			r.setReturnLoc(GvG.RETURN_LOC);
			
			for(Player member : team1Leader.getParty())
			{
				if(Config.EVENT_GvGDisableEffect)
					member.getEffectList().stopAllEffects();
				Functions.unRide(member);
				Functions.unSummonPet(member, true);
				member.setTransformation(0);
				member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
				member.dispelBuffs();
				
				member.teleToLocation(GvG.TEAM1_LOC.setR(r).findPointToStay(150), r);
			}

			for(Player member : team2Leader.getParty())
			{
				if(Config.EVENT_GvGDisableEffect)
					member.getEffectList().stopAllEffects();
				Functions.unRide(member);
				Functions.unSummonPet(member, true);
				member.setTransformation(0);
				member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
				member.dispelBuffs();
				
				member.teleToLocation(GvG.TEAM2_LOC.setR(r).findPointToStay(150), r);
			}
			
			r.start();
		}
		
		leaderList.clear();
		_log.info("GvG: Event started successfully.");
	}
}