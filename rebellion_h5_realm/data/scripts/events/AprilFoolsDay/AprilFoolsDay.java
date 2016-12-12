package events.AprilFoolsDay;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.network.serverpackets.ExBR_BroadcastEventState;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class AprilFoolsDay extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(AprilFoolsDay.class);
	private static final int APRIL_FOOL_CHEST = 17161;
	private static boolean _active = false;

	/**
	 * Читает статус эвента из базы.
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("AprilFoolsDay");
	}

	/**
	 * Запускает эвент
	 */
	public void startEvent()
	{
		Player player = getSelf();

		if(SetActive("AprilFoolsDay", true))
		{
			System.out.println("Event: 'April Fools Day' started.");
			ExBR_BroadcastEventState es = new ExBR_BroadcastEventState(ExBR_BroadcastEventState.APRIL_FOOLS, 1);
			for(Player p : GameObjectsStorage.getAllPlayersForIterate())
				p.sendPacket(es);
		}
		else
			player.sendMessage(new CustomMessage("scripts.events.aprilfoolsday.eventstarted", player));
		
		_active = true;
		show("admin/events/events.htm", player);
	}

	/**
	 * Останавливает эвент
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(SetActive("AprilFoolsDay", false))
			System.out.println("Event: 'April Fools Day' stopped.");
		else
			player.sendMessage(new CustomMessage("scripts.events.aprilfoolsday.stopevent", player));
		
		_active = false;
		show("admin/events/events.htm", player);
	}

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			_log.info("Loaded Event: Apil Fool's Day [state: activated]");
		}
		else
			_log.info("Loaded Event: Apil Fool's Day [state: deactivated]");
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
		if(_active)
			player.sendPacket(new ExBR_BroadcastEventState(ExBR_BroadcastEventState.APRIL_FOOLS_10, 1));
	}

	/**
	 * Обработчик смерти мобов, управляющий эвентовым дропом
	 */
	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if (cha == null || killer == null)
			return;
		
		// No rewards in instances.
		if (killer.getReflectionId() != ReflectionManager.DEFAULT_ID || cha.getReflectionId() != ReflectionManager.DEFAULT_ID)
			return;
				
		if(_active && SimpleCheckDrop(cha, killer) && checkValidate(killer, cha) && Rnd.chance(Config.EVENT_APIL_FOOLS_DROP_CHANCE / 10.0D))
			((MonsterInstance) cha).dropItem(killer.getPlayer(), APRIL_FOOL_CHEST, 1);
	}
	
	private boolean checkValidate(Creature killer, Creature mob)
	{
		if(mob == null || killer == null)
			return false;

		if(killer.getLevel() >= 80 && killer.getLevel() <= 86)
			return true;

		if(mob.getLevel() >= 80 && mob.getLevel() <= 100)
			return true;

		return false;
	}
}