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
package l2r.gameserver.model.entity;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.utils.PlayerEventStatus;
import l2r.gameserver.utils.ValueSortMap;

import gnu.trove.list.array.TIntArrayList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastTable;
import javolution.util.FastMap;

/**
 * @since $Revision: 1.3.4.1 $ $Date: 2005/03/27 15:29:32 $ This ancient thingie got reworked by Nik at $Date: 2011/05/17 21:51:39 $ Yeah, for 6 years no one bothered reworking this buggy event engine.
 */
public class L2Event
{
	protected static final Logger _log = Logger.getLogger(L2Event.class.getName());
	public static EventState eventState = EventState.OFF;
	public static String _eventName = "";
	public static String _eventCreator = "";
	public static String _eventInfo = "";
	public static int _teamsNumber = 0;
	public static final Map<Integer, String> _teamNames = new FastMap<>();
	public static final List<Player> _registeredPlayers = new FastTable<>();
	public static final Map<Integer, FastTable<Player>> _teams = new FastMap<>();
	public static int _npcId = 0;
	// public static final List<L2Npc> _npcs = new FastTable<L2Npc>();
	private static final Map<Player, PlayerEventStatus> _connectionLossData = new FastMap<>();
	private static final List<String> _registeredHWIDs = new FastTable<>();
	
	public enum EventState
	{
		OFF, // Not running
		STANDBY, // Waiting for participants to register
		ON // Registration is over and the event has started.
	}
	
	/**
	 * @param player
	 * @return The team ID where the player is in, or -1 if player is null or team not found.
	 */
	public static int getPlayerTeamId(Player player)
	{
		if (player == null)
		{
			return -1;
		}
		
		for (Entry<Integer, FastTable<Player>> team : _teams.entrySet())
		{
			if (team.getValue().contains(player))
			{
				return team.getKey();
			}
		}
		
		return -1;
	}
	
	public static List<Player> getTopNKillers(int n)
	{
		Map<Player, Integer> tmp = new FastMap<>();
		
		for (FastTable<Player> teamList : _teams.values())
		{
			for (Player player : teamList)
			{
				if (player.getEventStatus() == null)
				{
					continue;
				}
				
				tmp.put(player, player.getEventStatus().kills.size());
			}
		}
		
		ValueSortMap.sortMapByValue(tmp, false);
		
		// If the map size is less than "n", n will be as much as the map size
		if (tmp.size() <= n)
		{
			List<Player> toReturn = new FastTable<>();
			toReturn.addAll(tmp.keySet());
			return toReturn;
		}
		
		List<Player> toReturn = new FastTable<>();
		toReturn.addAll(tmp.keySet());
		return toReturn.subList(1, n);
	}
	
	public static void showEventHtml(Player player, String objectid)
	{// TODO: work on this
	
		if (eventState == EventState.STANDBY)
		{
			try
			{
				final String htmContent;
				NpcHtmlMessage html = new NpcHtmlMessage(5);
				
				if (_registeredPlayers.contains(player))
				{
					htmContent = HtmCache.getInstance().getNotNull("EventEngine/Participating.htm", player);
				}
				else
				{
					htmContent = HtmCache.getInstance().getNotNull("EventEngine/Participation.htm", player);
				}
				
				if (htmContent != null)
				{
					html.setHtml(htmContent);
				}
				
				html.replace("%objectId%", objectid); // Yeah, we need this.
				html.replace("%eventName%", _eventName);
				html.replace("%eventCreator%", _eventCreator);
				html.replace("%eventInfo%", _eventInfo);
				player.sendPacket(html);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception on showEventHtml(): " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Spawns an event participation NPC near the player. The npc id used to spawning is L2Event._npcId
	 * @param target
	 */
	public static void spawnEventNpc(Player target)
	{
		try
		{
			NpcInstance npc = NpcHolder.getInstance().getTemplate(_npcId).getNewInstance();
			npc.setReflection(target.getReflectionId());
			npc.setSpawnedLoc(target.getLoc().findPointToStay(50));
			npc.spawnMe(npc.getSpawnedLoc());
			npc.isEventMob = true;
			npc.broadcastPacket(new MagicSkillUse(npc, npc, 1034, 1, 1, 1));
			
			// _npcs.add(spawn.getLastSpawn());
			
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on spawn(): " + e.getMessage(), e);
		}
		
	}
	
	public static void unspawnEventNpcs()
	{
		// Its a little rough, but for sure it will remove every damn event NPC.
		for (NpcInstance npc : GameObjectsStorage.getAllByNpcId(_npcId, false))
		{
			if (npc != null && npc.isEventMob)
				npc.deleteMe();
		}
	}
	
	/**
	 * @param player
	 * @return False: If player is null, his event status is null or the event state is off. True: if the player is inside the _registeredPlayers list while the event state is STANDBY. If the event state is ON, it will check if the player is inside in one of the teams.
	 */
	public static boolean isParticipant(Player player)
	{
		if ((player == null) || (player.getEventStatus() == null))
		{
			return false;
		}
		
		switch (eventState)
		{
			case OFF:
				return false;
			case STANDBY:
				return _registeredPlayers.contains(player);
			case ON:
				for (FastTable<Player> teamList : _teams.values())
				{
					if (teamList.contains(player))
					{
						return true;
					}
				}
		}
		return false;
		
	}
	
	/**
	 * Adds the player to the list of participants. If the event state is NOT STANDBY, the player wont be registered.
	 * @param player
	 */
	public static void registerPlayer(Player player)
	{
		if (eventState != EventState.STANDBY)
		{
			player.sendMessage(new CustomMessage("l2r.gameserver.model.entity.l2event.message1", player));
			return;
		}
		
		if (player.hasHWID() && _registeredHWIDs.contains(player.getHWID()))
		{
			player.sendMessage(new CustomMessage("l2r.gameserver.model.entity.l2event.message3", player));
			return;
		}
		
		_registeredPlayers.add(player);
		if (player.hasHWID())
			_registeredHWIDs.add(player.getHWID());
		player.sendMessage(new CustomMessage("l2r.gameserver.model.entity.l2event.message2", player));
		
	}
	
	/**
	 * Removes the player from the participating players and the teams and restores his init stats before he registered at the event (loc, pvp, pk, title etc)
	 * @param player
	 */
	public static void removeAndResetPlayer(Player player)
	{
		
		try
		{
			if (isParticipant(player))
			{
				if (player.isDead())
				{
					player.restoreExp(100.0);
					player.doRevive();
					player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
					player.setCurrentCp(player.getMaxCp());
				}
				
				player.setTransformation(0);
			}
			
			if (player.getEventStatus() != null)
			{
				player.getEventStatus().restoreInits();
			}
			
			player.setEventStatus(null);
			
			_registeredPlayers.remove(player);
			_registeredHWIDs.remove(player.getHWID());
			int teamId = getPlayerTeamId(player);
			if (_teams.containsKey(teamId))
			{
				_teams.get(teamId).remove(player);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error at unregisterAndResetPlayer in the event:" + e.getMessage(), e);
		}
	}
	
	/**
	 * The player's event status will be saved at _connectionLossData
	 * @param player
	 */
	public static void savePlayerEventStatus(Player player)
	{
		_connectionLossData.put(player, player.getEventStatus());
	}
	
	/**
	 * If _connectionLossData contains the player, it will restore the player's event status. Also it will remove the player from the _connectionLossData.
	 * @param player
	 */
	public static void restorePlayerEventStatus(Player player)
	{
		if (_connectionLossData.containsKey(player))
		{
			player.setEventStatus(_connectionLossData.get(player));
			_connectionLossData.remove(player);
		}
	}
	
	/**
	 * If the event is ON or STANDBY, it will not start.
	 * Sets the event state to STANDBY and spawns registration NPCs
	 * @return a string with information if the event participation has been successfully started or not.
	 */
	public static String startEventParticipation()
	{
		try
		{
			switch (eventState)
			{
				case ON:
					return "Cannot start event, it is already on.";
				case STANDBY:
					return "Cannot start event, it is on standby mode.";
				case OFF: // Event is off, so no problem turning it on.
					eventState = EventState.STANDBY;
					break;
			}
			
			// Just in case
			unspawnEventNpcs();
			_registeredPlayers.clear();
			_registeredHWIDs.clear();
			// _npcs.clear();
			
			if (NpcHolder.getInstance().getTemplate(_npcId) == null)
			{
				return "Cannot start event, invalid npc id.";
			}
			
			try (FileReader fr = new FileReader(Config.DATAPACK_ROOT + "/data/gmevents/" + _eventName);
				BufferedReader br = new BufferedReader(fr))
			{
				_eventCreator = br.readLine();
				_eventInfo = br.readLine();
			}
			
			TIntArrayList temp = new TIntArrayList();
			for (Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				if (!player.isOnline() || player.isInOfflineMode()) // Offline shops? 
				{
					continue;
				}
				
				if (!temp.contains(player.getObjectId())) // The player is able to get NPC spawned near him.
				{
					spawnEventNpc(player);
					temp.add(player.getObjectId());
					
					for (Player playertemp : World.getAroundPlayers(player)) // If there are nearby players, do not spawn additional NPCs, instead spawn only 1 around them.
					{
						// LOS check (to prevent NPC not spawning because of a player behind some wall or smth) 
						if ((Math.abs(playertemp.getX() - player.getX()) < 500) && (Math.abs(playertemp.getY() - player.getY()) < 500))
							temp.add(playertemp.getObjectId());
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("L2Event: " + e.getMessage());
			return "Cannot start event participation, an error has occured.";
		}
		
		return "The event participation has been successfully started.";
	}
	
	/**
	 * If the event is ON or OFF, it will not start. Sets the event state to ON, creates the teams, adds the registered players ordered by level at the teams and adds a new event status to the players.
	 * @return a string with information if the event has been successfully started or not.
	 */
	public static String startEvent()
	{
		try
		{
			switch (eventState)
			{
				case ON:
					return "Cannot start event, it is already on.";
				case STANDBY:
					eventState = EventState.ON;
					break;
				case OFF: // Event is off, so no problem turning it on.
					return "Cannot start event, it is off. Participation start is required.";
			}
			
			// Clean the things we will use, just in case.
			unspawnEventNpcs();
			_teams.clear();
			_connectionLossData.clear();
			
			// Insert empty lists at _teams.
			for (int i = 0; i < _teamsNumber; i++)
			{
				_teams.put(i + 1, new FastTable<Player>());
			}
			
			int i = 0;
			while (!_registeredPlayers.isEmpty())
			{
				// Get the player with the biggest level
				int max = 0;
				Player biggestLvlPlayer = null;
				for (Player player : _registeredPlayers)
				{
					if (player == null)
					{
						continue;
					}
					
					if (max < player.getLevel())
					{
						max = player.getLevel();
						biggestLvlPlayer = player;
					}
				}
				
				if (biggestLvlPlayer == null)
				{
					continue;
				}
				
				_registeredPlayers.remove(biggestLvlPlayer);
				_teams.get(i + 1).add(biggestLvlPlayer);
				biggestLvlPlayer.setEventStatus();
				i = (i + 1) % _teamsNumber;
			}
			
		}
		catch (Exception e)
		{
			_log.warning("L2Event: " + e.getMessage());
			return "Cannot start event, an error has occured.";
		}
		
		return "The event has been successfully started.";
	}
	
	/**
	 * If the event state is OFF, it will not finish. Sets the event state to OFF, unregisters and resets the players, unspawns and clers the event NPCs, clears the teams, registered players, connection loss data, sets the teams number to 0, sets the event name to empty.
	 * @return a string with information if the event has been successfully stopped or not.
	 */
	public static String finishEvent()
	{
		switch (eventState)
		{
			case OFF:
				return "Cannot finish event, it is already off.";
			case STANDBY:
				for (Player player : _registeredPlayers)
				{
					removeAndResetPlayer(player);
				}
				
				unspawnEventNpcs();
				// _npcs.clear();
				_registeredPlayers.clear();
				_registeredHWIDs.clear();
				_teams.clear();
				_connectionLossData.clear();
				_teamsNumber = 0;
				_eventName = "";
				eventState = EventState.OFF;
				return "The event has been stopped at STANDBY mode, all players unregistered and all event npcs unspawned.";
			case ON:
				for (FastTable<Player> teamList : _teams.values())
				{
					for (Player player : teamList)
					{
						removeAndResetPlayer(player);
					}
				}
				
				eventState = EventState.OFF;
				unspawnEventNpcs(); // Just in case
				// _npcs.clear();
				_registeredPlayers.clear();
				_registeredHWIDs.clear();
				_teams.clear();
				_connectionLossData.clear();
				_teamsNumber = 0;
				_eventName = "";
				_npcId = 0;
				_eventCreator = "";
				_eventInfo = "";
				return "The event has been stopped, all players unregistered and all event npcs unspawned.";
		}
		
		return "The event has been successfully finished.";
	}
}
