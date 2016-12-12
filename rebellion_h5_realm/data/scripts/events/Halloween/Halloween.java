package events.Halloween;

import l2r.gameserver.Announcements;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Halloween extends Functions implements ScriptFile
{
	private static final String EVENT_NAME = "Halloween";
	private static final int EVENT_MANAGER_ID = 10004; // npc id
	
	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();
	private static final Logger _log = LoggerFactory.getLogger(Halloween.class);

	/**
	 * Спавнит эвент менеджеров
	 */
	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS[][] = { { -119494, 44882, 360, 24576 }, //Kamael Village
			{ -117239, 46842, 360, 49151 },
			{ -84023, 243051, -3728, 4096 }, //Talking Island Village
			{ -84411, 244813, -3728, 57343 },
			{ 45538, 48357, -3056, 18000 }, //Elven Village
			{ 46908, 50856, -2992, 8192 },
			{ 9929, 16324, -4568, 62999 }, //Dark Elven Village
			{ 11546, 17599, -4584, 46900 },
			{ 115096, -178370, -880, 0 }, //Dwarven Village
			{ 116199, -182694, -1488, 0 },
			{ -45372, -114104, -240, 16384 }, //Orc Village
			{ -45278, -112766, -240, 0 },
			{ -83156, 150994, -3120, 0 }, //Gludin
			{ -81031, 150038, -3040, 0 },
			{ -13727, 122117, -2984, 16384 }, //Gludio
			{ -14129, 123869, -3112, 40959 },
			{ 16111, 142850, -2696, 16000 }, //Dion
			{ 17275, 145000, -3032, 25000 },
			{ 111004, 218928, -3536, 16384 }, //Heine
			{ 108426, 221876, -3592, 49151 },
			{ 81755, 146487, -3528, 32768 }, //Giran
			{ 82145, 148609, -3464, 0 },
			{ 83037, 149324, -3464, 44000 },
			{ 81083, 56118, -1552, 32768 }, //Oren
			{ 81987, 53723, -1488, 0 },
			{ 117356, 76708, -2688, 49151 }, //Hunters Village
			{ 115887, 76382, -2712, 0 },
			{ 147200, 25614, -2008, 16384 }, //Aden
			{ 148557, 26806, -2200, 32768 },
			{ 43966, -47709, -792, 49999 }, //Rune
			{ 43165, -48461, -792, 17000 },
			{ 147421, -55435, -2728, 49151 }, //Goddart
			{ 148206, -55786, -2776, 904 },
			{ 85584, -142490, -1336, 0 }, //Schutgard
			{ 86865, -142915, -1336, 26000 } };

		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
	}

	public enum HatType
	{
		HALLOWEENHAT,
		GOLDENJACK,
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
			System.out.println("Event: 'Halloween' started.");
			Announcements.getInstance().announceToAll("Halloween Event has been started.");
		}
		else
			player.sendMessage(new CustomMessage("scripts.events.halloween.eventstart", player));

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
			System.out.println(player.isLangRus() ? "Событие 'Halloween' остановившийся." : "Event: 'Halloween' stopped.");
			Announcements.getInstance().announceToAll("Halloween Event has been stopped.");
		}
		else
			player.sendMessage(new CustomMessage("scripts.events.halloween.eventstop", player));

		show("admin/events/events.htm", player);
	}

	@Override
	public void onLoad()
	{
		if(isActive())
		{
			spawnEventManagers();
			_log.info("Loaded Event: Halloween [state: activated]");
		}
		else
			_log.info("Loaded Event: Halloween [state: deactivated]");
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

	private void showHtml()
	{
		if(getSelf() == null || getNpc() == null || getSelf().getPlayer() == null)
			return;

		if (!isActive())
		{
			show("scripts/events/Halloween/halloween-notactive.htm", getSelf().getPlayer());
			return;
		}
		
		String htmltext = null;
		Player player = getSelf().getPlayer();
		String var = player.getVar("halloweenEvent");

		if(var != null)
			htmltext = "halloween-rewarded.htm";
		else
			htmltext = "halloween-reward.htm";
		
		show("scripts/events/Halloween/" + htmltext, getSelf().getPlayer());
	}

	private void giveHat(HatType type)
	{
		if(getSelf() == null || getNpc() == null || getSelf().getPlayer() == null)
			return;

		if (!isActive())
		{
			show("scripts/events/Halloween/halloween-notactive.htm", getSelf().getPlayer());
			return;
		}
		
		String htmltext = null;
		Player player = getSelf().getPlayer();
		String var = player.getVar("halloweenEvent");

		switch(type)
		{
			case GOLDENJACK:
				if(var == null)
				{
					ItemFunctions.addItem(player, 20723, 1, true);
					player.setVar("halloweenEvent", 1, -1);
					htmltext = "halloween-reward.htm";
				}
				else
					htmltext = "halloween-rewarded.htm";
				break;
			case HALLOWEENHAT:
				if(var == null)
				{
					ItemFunctions.addItem(player, 13489, 1, true);
					player.setVar("halloweenEvent", 1, -1);
					htmltext = "halloween-reward.htm";
				}
				else
					htmltext = "halloween-rewarded.htm";
				break;
		}
		
		show("scripts/events/Halloween/" + htmltext, getSelf().getPlayer());
	}
	
	public void showNpcHtml()
	{
		showHtml();
	}

	public void giveHat1()
	{
		giveHat(HatType.GOLDENJACK);
	}

	public void giveHat2()
	{
		giveHat(HatType.HALLOWEENHAT);
	}
}