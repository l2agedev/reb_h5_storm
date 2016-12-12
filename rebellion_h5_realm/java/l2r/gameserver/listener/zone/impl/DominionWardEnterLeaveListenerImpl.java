package l2r.gameserver.listener.zone.impl;

import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2r.gameserver.model.items.attachment.FlagItemAttachment;

public class DominionWardEnterLeaveListenerImpl implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new DominionWardEnterLeaveListenerImpl();

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		if(!actor.isPlayer())
			return;

		Player player = actor.getPlayer();
		FlagItemAttachment flag = player.getActiveWeaponFlagAttachment();
		if(flag instanceof TerritoryWardObject)
		{
			flag.onLogout(player);

			player.sendDisarmMessage(((TerritoryWardObject) flag).getWardItemInstance());
		}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature actor)
	{
		//
	}
}
