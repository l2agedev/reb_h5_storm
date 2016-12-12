/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.main.pvpzone;

import l2r.commons.util.Rnd;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.EventManager.DisconnectedPlayerData;
import l2r.gameserver.nexus_engine.events.engine.EventRewardSystem;
import l2r.gameserver.nexus_engine.events.engine.base.ConfigModel;
import l2r.gameserver.nexus_engine.events.engine.base.EventMap;
import l2r.gameserver.nexus_engine.events.engine.base.EventPlayerData;
import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;
import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.base.Loc;
import l2r.gameserver.nexus_engine.events.engine.base.RewardPosition;
import l2r.gameserver.nexus_engine.events.engine.base.SpawnType;
import l2r.gameserver.nexus_engine.events.engine.lang.LanguageEngine;
import l2r.gameserver.nexus_engine.events.engine.team.EventTeam;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.CharacterData;
import l2r.gameserver.nexus_interface.delegate.ItemData;
import l2r.gameserver.nexus_interface.delegate.NpcData;
import l2r.gameserver.nexus_interface.delegate.NpcTemplateData;
import l2r.gameserver.nexus_interface.delegate.PartyData;
import l2r.gameserver.nexus_interface.delegate.SkillData;

import java.util.List;
import java.util.Map;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public class ColiseumZone extends PvpZone
{
	private static EventSpawn team1spawn = new EventSpawn(-1, 1, new Loc(151430, 46716, -3410), 1, "Regular");
	private static EventSpawn team2spawn = new EventSpawn(-1, 2, new Loc(147557, 46723, -3410), 2, "Regular");
	
	private static EventSpawn team1NpcSpawn = new EventSpawn(-1, 2, new Loc(147458, 46532, -3411), 2, "Regular");
	private static EventSpawn team2NpcSpawn = new EventSpawn(-1, 2, new Loc(151543, 46909, -3408), 2, "Regular");
	
	private static final int team1Door = 24190002;
	private static final int team2Door = 24190003;
	private static final int team1StaticDoor = 24190001;
	private static final int team2StaticDoor = 24190004;
	
	private static final int PLAYERS_REQUIRED = 4;
	private static final int ROUND_TIME_LIMIT = 600000;
	private static final int ROUND_PREPARATION_DELAY = 35000;
	private static final int EVENT_TIME_LIMIT = 1200000;
	private static final int MAX_ROUNDS = 10;
	
	private State _state = State.Inactive;
	private long _timeEventStarted = 0;
	private long _timeWhenRoundStartedPreparing = 0;
	private long _timeWhenRoundStartedFighting = 0;
	
	private List<PlayerEventInfo> _playersQueue = null;
	private List<EventTeam> _teams = null;
	
	private List<PlayerEventInfo> _toUnregister = null;
	
	private NpcData _npcTeam1 = null;
	private NpcData _npcTeam2 = null;
	
	private int _round;
	
	enum State
	{
		Inactive, 
		Waiting,
		Preparation,
		Fight
	}
	
	@Override
	public void start()
	{
		try
		{
			if(_state == State.Inactive)
			{
				_timeEventStarted = System.currentTimeMillis();
				
				_state = State.Waiting;
				
				_round = 0;
				
				_playersQueue = new FastTable<PlayerEventInfo>();
				_toUnregister = new FastTable<PlayerEventInfo>();
				
				_teams = new FastTable<EventTeam>();
				_teams.add(new EventTeam(null, 1, "Blue", "Blue Team"));
				_teams.add(new EventTeam(null, 2, "Red", "Red Team"));
				
				final int id = 9995;
				NpcData data;
				final NpcTemplateData template = new NpcTemplateData(id);

				Loc loc = team1NpcSpawn.getLoc();
				data = template.doSpawn(loc.getX(), loc.getY(), loc.getZ(), 1, 0);
				_npcTeam1 = data;
				
				loc = team2NpcSpawn.getLoc();
				data = template.doSpawn(loc.getX(), loc.getY(), loc.getZ(), 1, 0);
				_npcTeam2 = data;
				
				_checker = new Checker(5000);
				_checker.start();
				
				handleGates(false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void scheduledCheck()
	{
		boolean unregister = false;
		if(_checker.tick % 12 == 0)
			unregister = true;
		
		if(_state == State.Waiting)
		{
			if(!canStartNewRound())
			{
				announce(0, "The PvP zone is about to close due to time limit.");
				end();
				return;
			}
			
			// check if enought players are registered
			if(checkIfEnoughtPlayersToStartRound())
			{
				_state = State.Preparation;
				
				for(PlayerEventInfo player : _playersQueue)
				{
					addToTeam(player);
				}
				
				startFirstRound();
			}
		}
		else if(_state == State.Preparation)
		{
			// event doesn't run longer than 20 minutes
			if(!canStartNewRound())
			{
				announce(0, "The PvP zone is about to close due to time limit.");
				end();
			}
			else
			{
				if(_timeWhenRoundStartedPreparing > 0 && _timeWhenRoundStartedPreparing + ROUND_PREPARATION_DELAY <= System.currentTimeMillis())
				{
					startFightPhase();
				}
			}
		}
		else if(_state == State.Fight)
		{
			if(_timeWhenRoundStartedFighting > 0 && _timeWhenRoundStartedFighting + ROUND_TIME_LIMIT <= System.currentTimeMillis())
			{
				announce(0, "The round time limit of " + ROUND_TIME_LIMIT/60000 + " minutes has passed.");
				endRound();
			}
			else if(checkIfPlayersDeadOrMissing())
			{
				endRound();
			}
		}
		
		if(unregister)
		{
			if(_state == State.Inactive)
				return;
			
			try 
			{
				for(PlayerEventInfo player : _toUnregister)
				{
					if(player.isOnline() && player.isRegistered() && player.isRegisteredToPvpZone())
					{
						EventManager.getInstance().getMainEventManager().getPvpZoneManager().unregisterPlayer(player, true);
					}
				}
			} 
			finally
			{
				_toUnregister.clear();
			}
		}
	}
	
	private boolean checkIfEnoughtPlayersToStartRound()
	{
		int currentPlayers = 0;
		
		for(EventTeam t : _teams)
		{
			for(PlayerEventInfo player : t.getPlayers())
			{
				if(player.isOnline())
					currentPlayers ++;
			}
		}
		
		if((_playersQueue.size() + currentPlayers) >= PLAYERS_REQUIRED)
		{
			return true;
		}
		else
			return false;
	}
	
	private boolean checkIfPlayersDeadOrMissing()
	{
		int alive = 0;
		String message = null;
		
		for(EventTeam team : _teams)
		{
			alive = 0;
			for(PlayerEventInfo player : team.getPlayers())
			{
				if(player.isOnline() && !player.isDead())
				{
					alive ++;
				}
			}
			
			if(alive == 0)
			{
				message = "All players from " + team.getFullName() + " have been eliminated.";
				announce(0, message);
				rewardOtherTeam(team);
				break;
			}
		}
		
		if(message != null)
		{
			return true;
		}
		
		return false;
	}
	
	private void rewardOtherTeam(EventTeam losers)
	{
		for(EventTeam t : _teams)
		{
			if(losers.getTeamId() != t.getTeamId())
			{
				announce(0, t.getFullName() + " has won this round.", true);
				
				for(PlayerEventInfo player : t.getPlayers())
				{
					EventRewardSystem.getInstance().rewardPlayer(EventType.PvpZone, 1, player, RewardPosition.Winner, null, 0, 0, 0);
					//player.addItem(14721, 1, true);
				}
			}
		}
	}
	
	private void startFirstRound()
	{
		if(_state == State.Preparation)
		{
			for(EventTeam team : _teams)
			{
				for(PlayerEventInfo player : team.getPlayers())
				{
					teleportToTheRoom(player);
				}
			}
			
			_timeWhenRoundStartedPreparing = System.currentTimeMillis();
		}
	}
	
	private void startFightPhase()
	{
		if(_state == State.Preparation)
		{
			if(!checkIfEnoughtPlayersToStartRound())
			{
				_state = State.Waiting;
				
				announce(0, "There's not enought players to start a new round... Waiting for more.");
				announce(0, "Feel free to leave the zone using the NPC if you get bored.");
				
				return;
			}
			
			
			_state = State.Fight;
			
			_round ++;
			
			announce(0, "Round " + _round + " has started!");
			
			_timeWhenRoundStartedFighting = System.currentTimeMillis();
			
			handleGates(true);
		}
	}
	
	private boolean canStartNewRound()
	{
		return (_round >= MAX_ROUNDS || _timeEventStarted + EVENT_TIME_LIMIT <= System.currentTimeMillis());
	}
	
	private void endRound()
	{
		if(_state == State.Fight)
		{
			_state = State.Preparation;
			
			announce(0, "Round " + _round + " has ended!");
			
			if(canStartNewRound())
				announce(0, "The next round will start soon.");
			
			for(EventTeam team : _teams)
			{
				for(PlayerEventInfo player : team.getPlayers())
				{
					teleportToTheRoom(player);
				}
			}
			
			_timeWhenRoundStartedPreparing = System.currentTimeMillis();
			
			handleGates(false);
		}
	}
	
	// opens or closes gates
	private void handleGates(boolean open)
	{
		DoorInstance team1Door = ZoneHolder.getDoor(ColiseumZone.team1Door);
		DoorInstance team2Door = ZoneHolder.getDoor(ColiseumZone.team2Door);
		DoorInstance team1StaticDoor = ZoneHolder.getDoor(ColiseumZone.team1StaticDoor);
		DoorInstance team2StaticDoor = ZoneHolder.getDoor(ColiseumZone.team2StaticDoor);
		
		try
		{
			team1StaticDoor.closeMe();
			team2StaticDoor.closeMe();
			
			if(open)
			{
				team1Door.openMe();
				team2Door.openMe();
			}
			else
			{
				team1Door.closeMe();
				team2Door.closeMe();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void end()
	{
		_checker.disable();
		
		_state = State.Inactive;
		
		announce(0, "The zone has been closed.");
		
		if(_teams != null)
		{
			for(EventTeam team : _teams)
			{
				for(PlayerEventInfo player : team.getPlayers())
				{
					if(!player.isOnline())
						continue;
					
					player.restoreData();
					
					if (player != null && player.isDead())
					{
						player.doRevive();
						player.setCurrentHp(player.getMaxHp());
						player.setCurrentHp(player.getMaxMp());
						player.setCurrentCp(player.getMaxCp());
					}
					
					player.teleport(player.getOrigLoc(), 0, 0);
					player.sendMessage(LanguageEngine.getMsg("event_teleportBack"));
					
					if (player.getParty() != null)
					{
						PartyData party = player.getParty();
						party.removePartyMember(player);
					}
					
					player.broadcastUserInfo();
					
					team.removePlayer(player);
					
					player.setIsRegisteredToPvpZone(false);
					CallBack.getInstance().getPlayerBase().eventEnd(player);
				}
			}
		}
		
		if(_playersQueue != null)
		{
			for(PlayerEventInfo player : _playersQueue)
			{
				if(!player.isOnline())
					continue;
				
				player.setIsRegisteredToPvpZone(false);
				CallBack.getInstance().getPlayerBase().eventEnd(player);
			}
		}
		
		_toUnregister.clear();
		
		_playersQueue.clear();
		_teams.clear();
		
		if(_npcTeam1 != null)
		{
			_npcTeam1.deleteMe();
			_npcTeam1 = null;
		}
		
		if(_npcTeam2 != null)
		{
			_npcTeam2.deleteMe();
			_npcTeam2 = null;
		}
		
		EventManager.getInstance().getMainEventManager().getPvpZoneManager().stop();
	}
	
	@Override
	public boolean canRegister(PlayerEventInfo player)
	{
		//TODO
		return true;
	}
	
	@Override
	public boolean canUnregister(PlayerEventInfo player)
	{
		if(_state == State.Preparation || _state == State.Waiting)
		{
			return true;
		}
		else
			return false;
	}
	
	@Override
	public void setPlayerToUnregister(PlayerEventInfo player)
	{
		_toUnregister.add(player);
	}
	
	@Override
	public void addPlayer(PlayerEventInfo player)
	{
		if(_state == State.Inactive || !player.isOnline())
			return;
		
		if(_state == State.Waiting)
		{
			_playersQueue.add(player);
			
			player.screenMessage("You will be teleported to the zone soon once enought players register.", "PvP Zone", false);
		}
		
		if(_state == State.Fight)
		{
			_playersQueue.add(player);
			
			player.screenMessage("You will be teleported to the zone after the current round ends.", "PvP Zone", false);
		}
		
		if(_state == State.Preparation)
		{
			addToTeam(player);
			teleportToTheRoom(player);
		}
	}
	
	private void addToTeam(PlayerEventInfo player)
	{
		_playersQueue.remove(player);
		
		int team1Players = _teams.get(0).getPlayers().size();
		int team2Players = _teams.get(1).getPlayers().size();
		
		player.onEventStart(this);
		
		if(team1Players > team2Players)
		{
			_teams.get(1).addPlayer(player, true);
		}
		else if(team1Players < team2Players)
		{
			_teams.get(0).addPlayer(player, true);
		}
		else
		{
			_teams.get(Rnd.get(2)).addPlayer(player, true);
		}
	}
	
	private void teleportToTheRoom(PlayerEventInfo player)
	{
		if(player.isDead())
		{
			player.doRevive();
		}
		
		player.screenMessage("You are being teleported to the waiting room.", "Coliseum", false);
		
		if(player.getTeamId() == 1)
		{
			player.teleport(team1spawn.getLoc(), 0, 0);
		}
		else if(player.getTeamId() == 2)
		{
			player.teleport(team2spawn.getLoc(), 0, 0);
		}
	}
	
	@Override
	public void removePlayer(PlayerEventInfo player)
	{
		_playersQueue.remove(player);
		EventTeam team = player.getEventTeam();
		
		if(team != null)
		{
			team.removePlayer(player);
			
			player.setIsRegisteredToPvpZone(false);
			CallBack.getInstance().getPlayerBase().eventEnd(player);
			
			player.restoreData();
			
			if (player != null && player.isDead())
			{
				player.doRevive();
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentHp(player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
			}
			
			player.teleport(player.getOrigLoc(), 0, 0);
			player.sendMessage(LanguageEngine.getMsg("event_teleportBack"));
			
			if (player.getParty() != null)
			{
				PartyData party = player.getParty();
				party.removePartyMember(player);
			}
			
			player.broadcastUserInfo();
		}
	}

	@Override
	public String getName()
	{
		return "Coliseum";
	}
	
	@Override
	public String getStateNameForHtml()
	{
		return _state.toString();
	}
	
	@Override
	public int getPlayersCountForHtml()
	{
		int num = 0;
		
		num += _playersQueue.size();
		
		if(_teams != null)
		{
			for(EventTeam team : _teams)
				num += team.getPlayers().size();
		}
		
		return num;
	}
	
	public void announce(int teamId, String message)
	{
		announce(teamId, message, false);
	}
	
	public void announce(int teamId, String message, boolean special)
	{
		for(EventTeam team : _teams)
		{
			if(teamId <= 0 || team.getTeamId() == teamId)
			{
				for(PlayerEventInfo player : team.getPlayers())
				{
					player.screenMessage(message, getName(), special);
				}
			}
		}
	}

	@Override
	public boolean canAttack(PlayerEventInfo player, CharacterData target)
	{
		if(target.getEventInfo() == null)
			return true;
		else if(target.getEventInfo().getEvent() != player.getEvent())
			return false;
		else if(target.getEventInfo().getTeamId() != player.getTeamId())
			return true;
		
		return false;
	}

	@Override
	public boolean onAttack(CharacterData cha, CharacterData target)
	{
		return true;
	}

	@Override
	public boolean canSupport(PlayerEventInfo player, CharacterData target)
	{
		if(target.getEventInfo() == null || target.getEventInfo().getEvent() != player.getEvent())
			return false;
		else if(target.getEventInfo().getTeamId() == player.getTeamId())
			return true;
		
		return false;
	}

	@Override
	public void onKill(PlayerEventInfo player, CharacterData target)
	{
		if(target.getEventInfo() == null)
			return;
		
		if(player.getTeamId() != target.getEventInfo().getTeamId())
		{
			EventRewardSystem.getInstance().rewardPlayer(EventType.PvpZone, 1, player, RewardPosition.OnKill, null, 0, 0, 0);
		}
	}

	@Override
	public void onDie(PlayerEventInfo player, CharacterData killer)
	{
	}

	@Override
	public void onDamageGive(CharacterData cha, CharacterData target, int damage, boolean isDOT)
	{
	}
	
	@Override
	public boolean canInviteToParty(PlayerEventInfo player, PlayerEventInfo target)
	{
		if(target.getEvent() != player.getEvent())
			return false;
		else if(!player.canInviteToParty() || !target.canInviteToParty())
			return false;
		else if(target.getTeamId() == player.getTeamId())
			return true;
		
		return false;
	}

	@Override
	public void onDisconnect(PlayerEventInfo player)
	{
		if(player.isOnline()) // still should be online
		{
			EventTeam team = player.getEventTeam();
			player.restoreData(); // restore title, etc. before logging out and saving
			player.setXYZInvisible(player.getOrigLoc().getX(), player.getOrigLoc().getY(), player.getOrigLoc().getZ());
			
			team.removePlayer(player);
			
			CallBack.getInstance().getPlayerBase().playerDisconnected(player);
			
			player.setIsRegisteredToPvpZone(false);
		}
	}
	
	@Override
	public boolean canUseItem(PlayerEventInfo player, ItemData item)
	{
		if (item.isScroll())
			return false;
		
		if(item.isPetCollar())
		{
			player.sendMessage(LanguageEngine.getMsg("event_petsNotAllowed"));
			return false;
		}

		return true;
	}
	
	@Override
	public boolean canUseSkill(PlayerEventInfo player, SkillData skill)
	{
		if (skill.getSkillType().equals("RESURRECT"))
			return false;
		
		if (skill.getSkillType().equals("RECALL"))
			return false;
		
		if (skill.getSkillType().equals("SUMMON_FRIEND"))
			return false;

		if (skill.getSkillType().equals("FAKE_DEATH"))
			return false;

		return true;
	}
	
	@Override
	public EventPlayerData createPlayerData(PlayerEventInfo player)
	{
		return null;
	}

	@Override
	public EventPlayerData getPlayerData(PlayerEventInfo player)
	{
		return null;
	}

	@Override
	public void clearEvent()
	{
	}

	@Override
	public boolean addDisconnectedPlayer(PlayerEventInfo player, DisconnectedPlayerData data)
	{
		return false;
	}

	@Override
	public boolean onSay(PlayerEventInfo player, String text, int channel)
	{
		return true;
	}

	@Override
	public boolean onNpcAction(PlayerEventInfo player, NpcData npc)
	{
		return true;
	}

	@Override
	public void onItemUse(PlayerEventInfo player, ItemData item)
	{
	}

	@Override
	public void onSkillUse(PlayerEventInfo player, SkillData skill)
	{
	}

	@Override
	public boolean canDestroyItem(PlayerEventInfo player, ItemData item)
	{
		return true;
	}

	@Override
	public boolean canTransform(PlayerEventInfo player)
	{
		return false;
	}

	@Override
	public boolean canBeDisarmed(PlayerEventInfo player)
	{
		return true;
	}

	@Override
	public int allowTransformationSkill(PlayerEventInfo playerEventInfo, SkillData skillData)
	{
		return 0;
	}

	@Override
	public boolean canSaveShortcuts(PlayerEventInfo player)
	{
		return true;
	}

	@Override
	public int isSkillOffensive(SkillData skill)
	{
		return -1;
	}

	@Override
	public boolean isSkillNeutral(SkillData skill)
	{
		return false;
	}

	@Override
	public void playerWentAfk(PlayerEventInfo player, boolean warningOnly, int afkTime)
	{
	}

	@Override
	public void playerReturnedFromAfk(PlayerEventInfo player)
	{
		
	}

	@Override
	public double getMaxGearScore()
	{
		return 0;
	}

	@Override
	public void loadConfigs() 
	{
		
	}

	@Override
	public void clearConfigs() 
	{
		
	}

	@Override
	public FastTable<String> getCategories() 
	{
		return null;
	}

	@Override
	public Map<String, ConfigModel> getConfigs() 
	{
		return null;
	}

	@Override
	public Map<String, ConfigModel> getMapConfigs() 
	{
		return null;
	}

	@Override
	public RewardPosition[] getRewardTypes() 
	{
		return new RewardPosition[]{ RewardPosition.OnKill, RewardPosition.Winner, RewardPosition.Looser };
	}

	@Override
	public Map<SpawnType, String> getAvailableSpawnTypes() 
	{
		return null;
	}

	@Override
	public void setConfig(String key, String value, boolean addToValue) 
	{
	}

	@Override
	public String getDescriptionForReward(RewardPosition reward) 
	{
		return "";
	}

	@Override
	public int getTeamsCount() 
	{
		return 2;
	}

	@Override
	public boolean canRun(EventMap map) 
	{
		return false;
	}

	@Override
	public String getMissingSpawns(EventMap map) 
	{
		return null;
	}
}
