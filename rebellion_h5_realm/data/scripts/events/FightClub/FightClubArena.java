package events.FightClub;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.listener.actor.player.OnPlayerExitListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.DoorInstance;

import l2r.gameserver.templates.InstantZone;

import l2r.gameserver.utils.ItemFunctions;

import java.util.concurrent.ScheduledFuture;

public class FightClubArena extends FightClubManager implements OnDeathListener, OnPlayerExitListener
{
	protected static final String CLASS_NAME = "events.FightClub.FightClubManager";

	private ScheduledFuture<?> _endTask;
	public static ScheduledFuture<?> _startTask;

	private boolean _isEnded = false;
	private Player _player1;
	private Player _player2;
	private static int _itemId;
	private static int _itemCount;
	private static Reflection _reflection;
	private InstantZone _instantZone = InstantZoneHolder.getInstance().getInstantZone(Rnd.get(609, 612));

	public FightClubArena(Player player1, Player player2, int itemId, int itemCount, Reflection reflection)
	{
		//Подключаем листенеры персонажа
		CharListenerList.addGlobal(this);

		//Инициализируем переменные класса
		_player1 = player1;
		_player2 = player2;
		_itemId = itemId;
		_itemCount = itemCount;
		_reflection = reflection;

		_reflection.init(_instantZone);

		//Инициализируем сражение
		initBattle();
	}

	/**
	 * Вызывается при выходе игрока
	 */
	@Override
	public void onPlayerExit(Player player)
	{
		if((player.getStoredId() == _player1.getStoredId() || player.getStoredId() == _player2.getStoredId()) && !_isEnded)
		{
			stopEndTask();
			setLoose((Player) player);
		}
	}

	/**
	 * Вызывается при смерти игрока
	 */
	@Override
	public void onDeath(Creature actor, Creature killer)
	{
		if((actor.getStoredId() == _player1.getStoredId() || actor.getStoredId() == _player2.getStoredId()) && !_isEnded)
		{
			stopEndTask();
			setLoose((Player) actor);
		}
	}

	private void stopEndTask()
	{
		_endTask.cancel(false);
		_endTask = ThreadPoolManager.getInstance().schedule(new EndTask(), 3000);
	}

	/**
	 * Запускает таймеры боя
	 */
	private void initBattle()
	{
		final Object[] args = { _player1, _player2, _reflection };
		_startTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(new StartTask(_player1, _player2), Config.ARENA_TELEPORT_DELAY * 1000, 1000);
		_endTask = ThreadPoolManager.getInstance().schedule(new EndTask(), ((Config.ARENA_TELEPORT_DELAY + Config.FIGHT_TIME)) * 1000);
		sayToPlayers("scripts.events.fightclub.TeleportThrough", Config.ARENA_TELEPORT_DELAY, false, _player1, _player2);
		executeTask(CLASS_NAME, "resurrectPlayers", args, Config.ARENA_TELEPORT_DELAY * 1000 - 600);
		executeTask(CLASS_NAME, "healPlayers", args, Config.ARENA_TELEPORT_DELAY * 1000 - 500);
		executeTask(CLASS_NAME, "teleportPlayersToColliseum", args, Config.ARENA_TELEPORT_DELAY * 1000);
	}

	/**
	 * Удаляет ауру у игроков
	 */
	private void removeAura()
	{
		_player1.setTeam(TeamType.NONE);
		_player2.setTeam(TeamType.NONE);
	}

	/**
	 * Выдаёт награду
	 */
	private void giveReward(Player player)
	{
		final String name = ItemFunctions.createItem(_itemId).getTemplate().getName();
		sayToPlayer(player, "scripts.events.fightclub.YouWin", false, _itemCount * 2, name);
		addItem(player, _itemId, _itemCount * 2);
	}

	private static String getItemName()
	{
		final String name = ItemFunctions.createItem(_itemId).getTemplate().getName();
		return name;
	}

	private static int getItemCount()
	{
		return _itemCount;
	}
	
	/**
	 * Выводит скорбящее сообщение проигравшему ;)
	 * @param player
	 */
	private void setLoose(Player player)
	{
		if(player.getStoredId() == _player1.getStoredId())
			giveReward(_player2);
		else if(player.getStoredId() == _player2.getStoredId())
			giveReward(_player1);
		_player1.unsetVar("FightClubRate");
		_player2.unsetVar("FightClubRate");
		_isEnded = true;
		sayToPlayer(player, "scripts.events.fightclub.YouLoose", false, new Object[0]);
	}

	/**
	 * Метод, вызываемый при ничьей. Рассчитывает победителя или объявлет ничью.
	 */
	private void draw()
	{
		if(!Config.ALLOW_DRAW && _player1.getCurrentCp() != _player1.getMaxCp() || _player2.getCurrentCp() != _player2.getMaxCp() || _player1.getCurrentHp() != _player1.getMaxHp() || _player2.getCurrentHp() != _player2.getMaxHp())
		{
			if(_player1.getCurrentHp() != _player1.getMaxHp() || _player2.getCurrentHp() != _player2.getMaxHp())
			{
				if(_player1.getMaxHp() / _player1.getCurrentHp() > _player2.getMaxHp() / _player2.getCurrentHp())
				{
					giveReward(_player1);
					setLoose(_player2);
					return;
				}
				else
				{
					giveReward(_player2);
					setLoose(_player1);
					return;
				}
			}
			else
			{
				if(_player1.getMaxCp() / _player1.getCurrentCp() > _player2.getMaxCp() / _player2.getCurrentCp())
				{
					giveReward(_player1);
					setLoose(_player2);
					return;
				}
				else
				{
					giveReward(_player2);
					setLoose(_player1);
					return;
				}
			}

		}
		sayToPlayers("scripts.events.fightclub.Draw", true, _player1, _player2);
		addItem(_player1, _itemId, _itemCount);
		addItem(_player2, _itemId, _itemCount);
	}

	/**
	 * Возващает ссылку на первого игрока
	 * @return - ссылка на игрока
	 */
	protected Player getPlayer1()
	{
		return _player1;
	}

	/**
	 * Возващает ссылку на второго игрока
	 * @return - ссылка на игрока
	 */
	protected Player getPlayer2()
	{
		return _player2;
	}

	/**
	 * Возвращает отражение
	 * @return - reflection
	 */
	protected Reflection getReflection()
	{
		return _reflection;
	}

	/**
	 * Вызывает метод суперкласса, удаляющий рефлекшен
	 * @param arena - ссылка на арену
	 */
	private void delete(long delay)
	{
		final FightClubArena[] arg = { this };
		executeTask(CLASS_NAME, "deleteArena", arg, delay);
	}

	protected static class StartTask extends RunnableImpl
	{

		private Player _player1;
		private Player _player2;
		private int _second;

		public StartTask(Player player1, Player player2)
		{
			_player1 = player1;
			_player2 = player2;
			_second = Config.TIME_TO_PREPARATION;
		}

		@Override
		public void runImpl() throws Exception
		{
			addBuffers();
			switch(_second)
			{
				case 60:
					sayToPlayers("scripts.events.fightclub.TimeToStart", _second, false, _player1, _player2);
					break;
				case 30:
					sayToPlayers("scripts.events.fightclub.TimeToStart", _second, false, _player1, _player2);
					break;
				case 20:
					sayToPlayers("scripts.events.fightclub.TimeToStart", _second, false, _player1, _player2);
					break;
				case 10:
					sayToPlayers("scripts.events.fightclub.TimeToStart", _second, false, _player1, _player2);
					break;
				case 5:
					sayToPlayers("scripts.events.fightclub.TimeToStart", _second, false, _player1, _player2);
					break;
				case 3:
					sayToPlayers("scripts.events.fightclub.TimeToStart", _second, false, _player1, _player2);
					break;
				case 2:
					sayToPlayers("scripts.events.fightclub.TimeToStart", _second, false, _player1, _player2);
					break;
				case 1:
					sayToPlayers("scripts.events.fightclub.TimeToStart", _second, false, _player1, _player2);
					break;
				case 0:
					openDoors();
					deleteBuffers();
					startBattle(_player1, _player2);
					_startTask.cancel(true);
					_startTask = null;
					if(Config.FIGHT_CLUB_ANNOUNCE_START_TO_SCREEN)
						sayStartToAllPlayers("scripts.events.fightclub.AnnounceStartBatle", _player1, _player2, getItemName(), getItemCount(), false);
			}
			_second -= 1;
		}
	}

	private static String getBufferSpawnGroup(int instancedZoneId)
	{
		String bufferGroup = null;
		switch(instancedZoneId)
		{
			case 147:
				bufferGroup = "olympiad_147_buffers";
				break;
			case 148:
				bufferGroup = "olympiad_148_buffers";
				break;
			case 149:
				bufferGroup = "olympiad_149_buffers";
				break;
			case 150:
				bufferGroup = "olympiad_150_buffers";
				break;
		}
		return bufferGroup;
	}

	private static void addBuffers()
	{
		if(getBufferSpawnGroup(_reflection.getInstancedZoneId()) != null)
			_reflection.spawnByGroup(getBufferSpawnGroup(_reflection.getInstancedZoneId()));
	}

	private static void deleteBuffers()
	{
		_reflection.despawnByGroup(getBufferSpawnGroup(_reflection.getInstancedZoneId()));
	}
	
	private class EndTask extends RunnableImpl
	{
		private final Object[] args = { _player1, _player2, new Object[0] };

		@Override
		public void runImpl() throws Exception
		{
			removeAura();
			if(!_isEnded)
			{
				draw();
				_isEnded = true;
				stopEndTask();
			}
			sayToPlayers("scripts.events.fightclub.TeleportBack", Config.TIME_TELEPORT_BACK, false, _player1, _player2);
			executeTask(CLASS_NAME, "resurrectPlayers", args, Config.TIME_TELEPORT_BACK * 1000 - 300);
			executeTask(CLASS_NAME, "healPlayers", args, Config.TIME_TELEPORT_BACK * 1000 - 200);
			executeTask(CLASS_NAME, "teleportPlayersBack", args, Config.TIME_TELEPORT_BACK * 1000);
			delete((Config.TIME_TELEPORT_BACK + 10) * 1000);
		}

	}

	public static void openDoors()
	{
		for(DoorInstance door : _reflection.getDoors())
			door.openMe();
	}

	public FightClubArena()
	{}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}