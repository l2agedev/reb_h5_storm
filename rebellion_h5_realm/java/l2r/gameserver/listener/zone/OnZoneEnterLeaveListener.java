package l2r.gameserver.listener.zone;

import l2r.commons.listener.Listener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Zone;

public interface OnZoneEnterLeaveListener extends Listener<Zone>
{
	public void onZoneEnter(Zone zone, Creature actor);

	public void onZoneLeave(Zone zone, Creature actor);
}
