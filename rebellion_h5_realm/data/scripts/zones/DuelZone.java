package zones;

import java.util.List;

import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.entity.events.impl.DuelEvent;
import l2r.gameserver.model.entity.events.impl.PlayerVsPlayerDuelEvent;
import l2r.gameserver.scripts.ScriptFile;

/**
 * @author PaInKiLlEr - При входе в мирную зону, дуэль заканчивается
 */
public class DuelZone implements ScriptFile
{
	private static ZoneListener _zoneListener;
	
	@Override
	public void onLoad()
	{
		_zoneListener = new ZoneListener();
		List<Zone> zones = ZoneHolder.getZonesByType(ZoneType.peace_zone);
		for (Zone zone : zones)
			zone.addListener(_zoneListener);
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if (!cha.isPlayer())
				return;
			
			Player player = (Player) cha;
			if (!player.isInDuel())
				return;
			
			DuelEvent duelEvent = player.getEvent(DuelEvent.class);
			if (duelEvent != null && duelEvent instanceof PlayerVsPlayerDuelEvent)
				duelEvent.stopEvent();
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
		}
	}
}
