package bosses;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.instancemanager.NevitHeraldManager;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.model.CommandChannel;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.instances.BossInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.Earthquake;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bosses.EpicBossState.State;

public class AntharasManager extends Functions implements ScriptFile, OnDeathListener
{
	private static final Logger _log = LoggerFactory.getLogger(AntharasManager.class);

	// Constants
	private static final int _teleportCubeId = 31859;
	private static final int ANTHARAS_WEAK = 29066;
	private static final int ANTHARAS_NORMAL = 29067;
	private static final int ANTHARAS_STRONG = 29068;
	private static final int FWA_LIMITOFWEAK = 18;
	private static final int FWA_LIMITOFNORMAL = 27;
	private static final int PORTAL_STONE = 3865;
	private static final Location TELEPORT_POSITION_SINGLE = new Location(179700 + Rnd.get(700), 113800 + Rnd.get(2100), -7709);
	private static final Location TELEPORT_POSITION_CC = new Location(179700 + Rnd.get(200), 113800 + Rnd.get(500), -7709);
	private static final Location _teleportCubeLocation = new Location(177615, 114941, -7709, 0);
	private static final Location _antharasLocation = new Location(181911, 114835, -7678, 32542);

	// Models
	private static BossInstance _antharas;
	private static NpcInstance _teleCube;
	private static List<NpcInstance> _spawnedMinions = new ArrayList<NpcInstance>();

	// tasks.
	private static ScheduledFuture<?> _monsterSpawnTask;
	private static ScheduledFuture<?> _intervalEndTask;
	private static ScheduledFuture<?> _socialTask;
	private static ScheduledFuture<?> _moveAtRandomTask;
	private static ScheduledFuture<?> _sleepCheckTask;
	private static ScheduledFuture<?> _onAnnihilatedTask;

	// Vars
	private static EpicBossState _state;
	private static Zone _zone;
	private static long _lastAttackTime = 0;
	private static final int FWA_LIMITUNTILSLEEP = 15 * 60000;
	private static final int FWA_FIXINTERVALOFANTHARAS = Config.FIXINTERVALOFANTHARAS_HOUR * 60 * 60000;  // respawn time
	private static final int FWA_RANDOMINTERVALOFANTHARAS = Config.RANDOM_TIME_OF_ANTHARAS * 60 * 60000; // random time.
	private static final int FWA_APPTIMEOFANTHARAS = 5 * 60000; // 5 minutes
	private static boolean Dying = false;
	private static boolean _entryLocked = false;
	
	// Custom shits.
	private static ScheduledFuture<?> _antharasStuckThread;
	private static Location _lastAntharasLocation;

	// debug mode for testing
	private static boolean DEBUG = Config.ALT_DEBUG_ENABLED;
	
	private static class AntharasSpawn extends RunnableImpl
	{
		private int _distance = 2550;
		private int _taskId = 0;
		private List<Player> _players = getPlayersInside();
		
		AntharasSpawn(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void runImpl()
		{
			_entryLocked = true;
			switch(_taskId)
			{
				case 0:
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(1), 2000);
					break;
				case 1:
					_antharas = (BossInstance) Functions.spawn(_antharasLocation, 29066 + getTypeAntharas());
					_antharas.setAggroRange(0);
					_state.setRespawnDate(Rnd.get(FWA_FIXINTERVALOFANTHARAS, FWA_FIXINTERVALOFANTHARAS + FWA_RANDOMINTERVALOFANTHARAS));
					_state.setState(EpicBossState.State.ALIVE);
					_state.update();
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(2), 2000);
					AdminTable.broadcastToGMs(new Say2(0, ChatType.HERO_VOICE, "Server", "Antharas has spawned..."));
					break;
				case 2:
					// set camera.
					for(Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, -19, 0, 10000, 0, 0, 0, 0);
						}
						else
							pc.leaveMovieMode();
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(3), 3000);
					break;
				case 3:
					// do social.
					_antharas.broadcastPacket(new SocialAction(_antharas.getObjectId(), 1));

					// set camera.
					for(Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, 0, 6000, 10000, 0, 0, 0, 0);
						}
						else
							pc.leaveMovieMode();
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(4), 10000);
					break;
				case 4:
					// set camera.
					for(Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 3800, 0, -3, 0, 10000, 0, 0, 0, 0);
						}
						else
							pc.leaveMovieMode();
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(5), 200);
					break;
				case 5:
					// do social.
					_antharas.broadcastPacket(new SocialAction(_antharas.getObjectId(), 2));
					// set camera.
					for(Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1100, 0, -3, 22000, 11000, 0, 0, 0, 0);
						}
						else
							pc.leaveMovieMode();
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(6), 10800);
					break;
				case 6:
					// set camera.
					for(Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1200, 0, -3, 300, 2000, 0, 0, 0, 0);
						}
						else
							pc.leaveMovieMode();
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(7), 1900);
					break;
				case 7:
					// reset camera.
					for(Player pc : _players)
						pc.leaveMovieMode();

					broadcastScreenMessage(NpcString.ANTHARAS_YOU_CANNOT_HOPE_TO_DEFEAT_ME);
					_antharas.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS02_A", 1, _antharas.getObjectId(), _antharas.getLoc()));
					_antharas.setAggroRange(_antharas.getTemplate().aggroRange);
					_antharas.setRunning();
					_antharas.moveToLocation(new Location(179011, 114871, -7704), 0, false);
					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000);
					_antharasStuckThread = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
					{
						public void run()
						{
							if (_lastAntharasLocation != null && _lastAntharasLocation.equals(_antharas.getLoc()))
							{
								sleep();
							}
							else
								_lastAntharasLocation = _antharas.getLoc();
						}
					}, FWA_LIMITUNTILSLEEP, FWA_LIMITUNTILSLEEP);
					break;
				case 8:
					for(Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1200, 20, -10, 0, 13000, 0, 0, 0, 0);
						}
						else
							pc.leaveMovieMode();
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(9), 13000);
					break;
				case 9:
					for (Player pc : _players)
						pc.leaveMovieMode();
					onAntharasDie();
					break;
			}
		}
	}

	private static class CheckLastAttack extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(_state.getState() == EpicBossState.State.ALIVE)
				if(_lastAttackTime + FWA_LIMITUNTILSLEEP < System.currentTimeMillis())
					sleep();
				else
					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 60000);
		}
	}

	// at end of interval.
	private static class IntervalEnd extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			// Earthquake
			for (Player p : GameObjectsStorage.getAllPlayersForIterate())
			{
				p.broadcastPacket(new Earthquake(new Location(185708, 114298, -8221), 20, 10));
			}
		}
	}

	private static class onAnnihilated extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			sleep();
		}
	}

	private static void banishForeigners()
	{
		for(Player player : getPlayersInside())
			player.teleToClosestTown();
	}

	private synchronized static void checkAnnihilated()
	{
		if(_onAnnihilatedTask == null && isPlayersAnnihilated())
			_onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), 5000);
	}

	private static List<Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}

	private static int getRespawnInterval()
	{
		return (int) (Config.RAID_RESPAWN_MULTIPLIER * (FWA_FIXINTERVALOFANTHARAS + Rnd.get(0, FWA_RANDOMINTERVALOFANTHARAS)));
	}

	public static Zone getZone()
	{
		return _zone;
	}

	private static boolean isPlayersAnnihilated()
	{
		for(Player pc : getPlayersInside())
			if(!pc.isDead())
				return false;
		return true;
	}

	private static void onAntharasDie()
	{
		if(Dying)
			return;

		Dying = true;
		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();

		_entryLocked = false;
		_teleCube = Functions.spawn(_teleportCubeLocation, _teleportCubeId);
		Log.addGame("Antharas died", "bosses");
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY()))
			checkAnnihilated();
		else if (self.isNpc() && (self.getNpcId() == ANTHARAS_WEAK || self.getNpcId() == ANTHARAS_NORMAL || self.getNpcId() == ANTHARAS_STRONG))
		{
			if (self.getNpcId() == ANTHARAS_STRONG)
				NevitHeraldManager.getInstance().doSpawn(ANTHARAS_STRONG);
			
			ThreadPoolManager.getInstance().schedule(new AntharasSpawn(8), 10);
			
			AdminTable.broadcastToGMs(new Say2(0, ChatType.HERO_VOICE, "Server", "Antharas (" + self.getNpcId() + ") has died..."));
			ServerVariables.set("AntharasDeath", System.currentTimeMillis());
			
			_log.info("Antharas(" + self.getNpcId() + "): Was killed by : " + killer.getName() + " ");
			
			// achievement
			if (Config.ENABLE_PLAYER_COUNTERS && self.getNpcId() == ANTHARAS_STRONG)
			{
				if (killer != null && killer.getPlayer() != null)
				{
					for (Player member : killer.getPlayer().getPlayerGroup())
					{
						if (member != null && Location.checkIfInRange(3000, member, killer, false))
							member.getCounters().addPoint("_Antharas_Killed");
					}
				}
			}
		}
	}

	private static void setIntervalEndTask()
	{
		setUnspawn();

		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			return;
		}

		if(!_state.getState().equals(EpicBossState.State.INTERVAL))
		{
			_state.setRespawnDate(getRespawnInterval());
			_state.setState(EpicBossState.State.INTERVAL);
			_state.update();
		}

		_intervalEndTask = ThreadPoolManager.getInstance().schedule(new IntervalEnd(), _state.getInterval());
	}

	// clean Antharas's lair.
	private static void setUnspawn()
	{
		// eliminate players.
		banishForeigners();

		if(_antharas != null)
			_antharas.deleteMe();
		for(NpcInstance npc : _spawnedMinions)
			npc.deleteMe();
		if(_teleCube != null)
			_teleCube.deleteMe();

		_entryLocked = false;

		// not executed tasks is cancelled.
		if(_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(false);
			_monsterSpawnTask = null;
		}
		if(_intervalEndTask != null)
		{
			_intervalEndTask.cancel(false);
			_intervalEndTask = null;
		}
		if(_socialTask != null)
		{
			_socialTask.cancel(false);
			_socialTask = null;
		}
		if(_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(false);
			_moveAtRandomTask = null;
		}
		if(_sleepCheckTask != null)
		{
			_sleepCheckTask.cancel(false);
			_sleepCheckTask = null;
		}
		if(_onAnnihilatedTask != null)
		{
			_onAnnihilatedTask.cancel(false);
			_onAnnihilatedTask = null;
		}
		if(_antharasStuckThread != null)
		{
			_antharasStuckThread.cancel(true);
			_antharasStuckThread = null;
		}
	}

	private void init()
	{
		_state = new EpicBossState(ANTHARAS_STRONG);
		_zone = ZoneHolder.getZone("[antharas_epic]");

		CharListenerList.addGlobal(this);
		_log.info("AntharasManager: State of Antharas is " + _state.getState() + ".");
		if(!_state.getState().equals(EpicBossState.State.NOTSPAWN))
			setIntervalEndTask();

		_log.info("AntharasManager: Next spawn date of Antharas is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
	}

	private static void sleep()
	{
		String playersName = "";
		for (Player plr : getPlayersInside())
		{
			playersName += plr.getName() + ", ";
		}
		
		_log.info("Antharas has went to sleep while his HP % is " + _antharas.getCurrentHpPercents() + " and it had " + getPlayersInside().size() + " players inside.");
		if (playersName.length() <= 2)
			_log.info("Antharas has went to sleep players inside: " + playersName.substring(0, playersName.length()) + " ");
		else
			_log.info("Antharas has went to sleep players inside: " + playersName.substring(0, playersName.length() - 2) + " ");
		
		setUnspawn();
		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}

	public static void setLastAttackTime()
	{
		_lastAttackTime = System.currentTimeMillis();
	}

	// setting Antharas spawn task.
	public synchronized static void setAntharasSpawnTask()
	{
		if(_monsterSpawnTask == null)
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(1), FWA_APPTIMEOFANTHARAS);
		//_entryLocked = true;
	}

	public static void broadcastScreenMessage(NpcString npcs)
	{
		for(Player p : getPlayersInside())
			p.sendPacket(new ExShowScreenMessage(npcs, 8000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
	}

	public static void addSpawnedMinion(NpcInstance npc)
	{
		_spawnedMinions.add(npc);
	}

	public static void enterTheLair(Player player)
	{
		if(player == null)
			return;
		
		// debug mode for gm
		if (DEBUG && player.isGM())
		{
			player.teleToLocation(TELEPORT_POSITION_SINGLE);
			setAntharasSpawnTask();
		}
		
		if (Config.ANTHARAS_DIABLE_CC_ENTER)
		{
			if(getPlayersInside().size() > 200)
			{
				player.sendMessage(new CustomMessage("scripts.bosses.AntharasManager.max_players_inside", player));
				return;
			}
			if(_state.getState() != EpicBossState.State.NOTSPAWN)
			{
				player.sendMessage(new CustomMessage("scripts.bosses.AntharasManager.notspawn", player));
				return;
			}
			if(_entryLocked || _state.getState() == EpicBossState.State.ALIVE)
			{
				player.sendMessage(new CustomMessage("scripts.bosses.AntharasManager.alive", player));
				return;
			}
			
			if (player.isDead() || player.isFlying() || player.isCursedWeaponEquipped() || player.isTerritoryFlagEquipped() ||  player.getInventory().getCountOf(PORTAL_STONE) < 1)
			{
				player.sendMessage(new CustomMessage("scripts.bosses.AntharasManager.requirements", player, player.getName()));
				return;
			}
			
			player.setVar("EnterAntharas", 1, -1);
			player.teleToLocation(TELEPORT_POSITION_SINGLE);
			
			Log.addGame("antharas: Player " + player.getName() + " has entered into antharas lair. Total players inside: " + getPlayersInside().size(), "antharas");
		}
		else
		{
			// Телепортироваться могут только ЦЦ, не знаю нужно ли но оставлю.
			if(player.getParty() == null || !player.getParty().isInCommandChannel())
			{
				player.sendPacket(Msg.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_IN_A_CURRENT_COMMAND_CHANNEL);
				return;
			}
			CommandChannel cc = player.getParty().getCommandChannel();
			if(cc.isLeader(player))
			{
				player.sendPacket(Msg.ONLY_THE_ALLIANCE_CHANNEL_LEADER_CAN_ATTEMPT_ENTRY);
				return;
			}
			if(cc.size() > 200)
			{
				player.sendMessage(new CustomMessage("scripts.bosses.AntharasManager.max_players_inside", player));
				return;
			}
			if(getPlayersInside().size() > 200)
			{
				player.sendMessage(new CustomMessage("scripts.bosses.AntharasManager.max_players_inside", player));
				return;
			}
			if(_state.getState() != EpicBossState.State.NOTSPAWN)
			{
				player.sendMessage(new CustomMessage("scripts.bosses.AntharasManager.notspawn", player));
				return;
			}
			if(_entryLocked || _state.getState() == EpicBossState.State.ALIVE)
			{
				player.sendMessage(new CustomMessage("scripts.bosses.AntharasManager.alive", player));
				return;
			}
			// checking every member of CC for the proper conditions

			for(Player p : cc)
				if(p.isDead() || p.isFlying() || p.isCursedWeaponEquipped() || player.isTerritoryFlagEquipped() || p.getInventory().getCountOf(PORTAL_STONE) < 1 || !p.isInRange(player, 1000))
				{
					player.sendMessage(new CustomMessage("scripts.bosses.AntharasManager.requirements", player, player.getName()));
					return;
				}

			for(Player p : cc)
			{
				p.setVar("EnterAntharas", 1, -1);
				p.teleToLocation(TELEPORT_POSITION_CC);
			}
			
			_log.info("Antharas: CC leader : " + cc.getLeader().getName() + " has entered into antharas lair. Total players in CC: " + cc.size());
		}
		
		setAntharasSpawnTask();
	}
	
	public static int getTypeAntharas()
	{
		if (Config.SPAWN_CUSTOM_ANTHARAS > 0)
			return Config.SPAWN_CUSTOM_ANTHARAS - 1;
			
		int antharasType = 1;
		if (getPlayersInside().size() <= FWA_LIMITOFWEAK)
			antharasType = 0;
		else if (getPlayersInside().size() >= FWA_LIMITOFNORMAL)
			antharasType = 2;
		
		return antharasType;
	}
	
	@Override
	public void onLoad()
	{
		init();
	}

	@Override
	public void onReload()
	{
		sleep();
	}

	@Override
	public void onShutdown()
	{
	}
}