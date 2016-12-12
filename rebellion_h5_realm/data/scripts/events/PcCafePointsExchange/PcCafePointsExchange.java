package events.PcCafePointsExchange;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcCafePointsExchange extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(PcCafePointsExchange.class);
	private static final String EVENT_NAME = "PcCafePointsExchange";
	private static final int EVENT_MANAGER_ID = 32130; // npc id
	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();

	/**
	 * Спавнит эвент менеджеров
	 */
	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS[][] = { { 15880, 143704, -2888, 0 }, //Dion
				{ 83656, 148440, -3430, 32768 }, //Giran
				{ 147272, 27416, -2228, 16384 }, //Aden
				{ 42808, -47896, -822, 49152 }, //Rune
		};

		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
	}

	/**
	 * Удаляет спавн эвент менеджеров
	 */
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	/**
	 * Читает статус эвента из базы.
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive(EVENT_NAME);
	}

	/**
	 * Запускает эвент
	 */
	public void startEvent()
	{
		Player player = getSelf();

		if(SetActive(EVENT_NAME, true))
		{
			spawnEventManagers();
			System.out.println("Event: 'PcCafePointsExchange' started.");
		}
		else
			player.sendMessage(new CustomMessage("scripts.events.pccafepointsexchange.eventstart", player));

		show("admin/events/events.htm", player);
	}

	/**
	 * Останавливает эвент
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(SetActive(EVENT_NAME, false))
		{
			unSpawnEventManagers();
			System.out.println("Event: 'PcCafePointsExchange' stopped.");
		}
		else
			player.sendMessage(new CustomMessage("scripts.events.pccafepointsexchange.eventstop", player));
		
		show("admin/events/events.htm", player);
	}

	@Override
	public void onLoad()
	{
		if(isActive())
		{
			spawnEventManagers();
			_log.info("Loaded Event: PcCafePointsExchange [state: activated]");
		}
		else
			_log.info("Loaded Event: PcCafePointsExchange [state: deactivated]");
	}

	@Override
	public void onReload()
	{
		unSpawnEventManagers();
	}

	@Override
	public void onShutdown()
	{
		unSpawnEventManagers();
	}
}