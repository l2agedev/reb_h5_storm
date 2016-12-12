package events.LindviorInvasion;

import l2r.commons.util.Rnd;
import l2r.gameserver.Announcements;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.tables.SpawnTable;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lindvior extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(Lindvior.class);
	
	private Timer _counter;
	private static boolean _Active = false;
	private final int LINDVIOR = 70013;
	private static String _spawnedTown = "Missing";
	private static int _despawnCounter;
	private static NpcInstance _npc = null;
	
	private final int[][] _towns = 
	{
			 {1, 155647, -115013, -1637},  
             {2, 60747, -33321, 282},     
             {3, 135964, 9172, -3171},
             {4, 105221 , 204746 , -2763},  
             {5, 123625 , -116954 , -2618}, 
	};
	
	private final String[][] _townNames = 
	{
			{"1", "Hot Springs"},
			{"2", "Cursed Village"},
			{"3", "Plains of Glory"},
			{"4", "Alligator Beach"},
			{"5", "Frozen Labyrinth"}
	};

	/**
	 * Читает статус эвента из базы.
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("LindviorEvent");
	}

	/**
	 * Запускает эвент
	 */
	public void startEvent()
	{
		Player player = getSelf();

		if (_npc != null)
		{
			player.sendMessage("Something wen wrong... lindvior boss is already spawned....");
			return;
		}
		
		if(SetActive("LindviorEvent", true))
		{
			System.out.println("Event: 'Lindvior' started.");
			Announcements.getInstance().announceToAll("Warning Lindvior the Dragon of Sky is free!");
			
			int id = Rnd.get(_towns.length);
			
			_spawnedTown = _townNames[id][1];
			
            Announcements.getInstance().announceToAll(_townNames[id][1] + " is under attack by Lindvior no one is safe there for 1 hour!");
            
            _npc = SpawnTable.spawnSingle(LINDVIOR, _towns[id][1], _towns[id][2], _towns[id][3], 3600000); // despawn it after 1 hour
            
            schedule(3600); // 1 hour
		}
		else
			player.sendMessage("Lindvior event already running...");
		
		_Active = true;
		show("admin/events/events.htm", player);
	}

	/**
	 * Останавливает эвент
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(SetActive("LindviorEvent", false))
		{
			System.out.println("Event: 'Lindvior' stopped.");
			Announcements.getInstance().announceToAll("Lindvior has left and the area is safe!");
		}
		else
			player.sendMessage(new CustomMessage("scripts.events.aprilfoolsday.stopevent", player));
		
		if (_npc != null)
		{
			_npc.deleteMe();
			_npc = null;
		}
		
		if(_counter != null)
		{
			_counter.cancel();
			_counter = null;
		}
		
		_Active = false;
		show("admin/events/events.htm", player);
	}

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_Active = true;
			_log.info("Loaded Event: Lindvior [state: activated]");
		}
		else
			_log.info("Loaded Event: Lindvior [state: deactivated]");
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	@Override
	public void onPlayerEnter(Player player)
	{
		if (_Active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.LindviorEvent.AnnounceEventStarted", new String[] { _spawnedTown });
	}

	/**
	 * Обработчик смерти мобов, управляющий эвентовым дропом
	 */
	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if (cha == null || killer == null)
			return;
		
		NpcInstance npc = getNpc();
		if (npc != null && npc.getNpcId() == LINDVIOR)
		{
			Announcements.getInstance().announceToAll("The Sky Dragon Lindvior has been killed by " + killer.getName() + " and now the area is safe!");
			
			if(_counter != null)
				_counter.cancel();
			
			_npc = null;
			
			if(SetActive("LindviorEvent", false))
				_log.info("Lindvior Event: Lindvior was killed by " + killer.getName());
			
			_Active = false;
		}
	}
	
	/**
	 * @param sec time in seconds.
	 */
	private void schedule(int sec)
	{
		if(sec < 0)
			return;

		if(_counter != null)
			_counter.cancel();
		
		_counter = new Timer("ShutdownCounter", true);
		_despawnCounter = sec;
		_counter.scheduleAtFixedRate(new DespawnCounter(), 0, 1000L);
	}
	
	private class DespawnCounter extends TimerTask
	{
		@Override
		public void run()
		{
			switch(_despawnCounter)
			{
				case 3600:
					Announcements.getInstance().announceToAll("1 hour until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 2400:
					Announcements.getInstance().announceToAll("40 minutes until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 1200:
					Announcements.getInstance().announceToAll("20 minutes until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 600:
					Announcements.getInstance().announceToAll("10 minutes until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 300:
					Announcements.getInstance().announceToAll("5 minutes until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 60:
					Announcements.getInstance().announceToAll("1 minute until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 10:
					Announcements.getInstance().announceToAll("10 seconds until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 5:
					Announcements.getInstance().announceToAll("5 seconds until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 4:
					Announcements.getInstance().announceToAll("4 seconds until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 3:
					Announcements.getInstance().announceToAll("3 seconds until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 2:
					Announcements.getInstance().announceToAll("2 seconds until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 1:
					Announcements.getInstance().announceToAll("1 second until Lindvior leaves " + _spawnedTown + ".");
					break;
				case 0:
					cancel();
					return;
			}

			_despawnCounter--;
		}
	}
}