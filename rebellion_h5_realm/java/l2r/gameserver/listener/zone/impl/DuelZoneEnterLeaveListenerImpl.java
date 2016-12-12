package l2r.gameserver.listener.zone.impl;

import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.entity.events.impl.DuelEvent;

/**
 * @author VISTALL
 * @date 15:07/28.08.2011
 */
public class DuelZoneEnterLeaveListenerImpl implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new DuelZoneEnterLeaveListenerImpl();

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		if(!actor.isPlayable())
			return;

		Player player = actor.getPlayer();

		DuelEvent duelEvent = player.getEvent(DuelEvent.class);
		if(duelEvent != null)
			duelEvent.playerExit(player);
	}

	@Override
	public void onZoneLeave(Zone zone, Creature actor)
	{

	}
}
