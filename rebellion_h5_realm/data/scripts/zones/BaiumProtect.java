package zones;

import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.scripts.ScriptFile;

public class BaiumProtect implements ScriptFile
{
	private static ZoneListener _zoneListener;
	
	@Override
	public void onLoad()
	{
		_zoneListener = new ZoneListener();
		Zone zone = ZoneHolder.getZone("[Baium_Protect]");
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
			Player player = cha.getPlayer();
			
			if (player == null)
				return;
			
			if (player.isGM() || NexusEvents.isInEvent(player))
				return;
			
			if (player.getVar("EnterBaium") != null)
			{
				if (player.getVarInt("EnterBaium") != 0)
					return;
				
				player.sendMessage(new CustomMessage("scripts.zones.baiumprotect.cannotbehere", player));
				player.teleToLocation(147450, 27120, -2208);
			}
			else
			{
				player.sendMessage(new CustomMessage("scripts.zones.baiumprotect.cannotbehere", player));
				player.teleToLocation(147450, 27120, -2208);
			}
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
		}
	}
}