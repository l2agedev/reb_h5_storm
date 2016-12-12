package custom;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.Location;

/**
 * endX = centerX + radius * cos( angle ) endY = centerY + radius * sin( angle )
 * @author Nik
 */
public class GiranHarborBazar implements ScriptFile
{
	private static boolean ENABLED = false;
	private static final Logger _log = LoggerFactory.getLogger(GiranHarborBazar.class);
	private static List<Location> LOCATIONS = new LinkedList<>();
	private static List<Location> _takenLocs = new LinkedList<>();
	
	private ScheduledFuture<?> _thread = null;
	
	private static class CheckShops implements Runnable
	{
		
		@Override
		public void run()
		{
			// Generate Taken locs first.
			_takenLocs = GameObjectsStorage.getAllPlayersStream().filter(p -> p.getPrivateStoreType() != Player.STORE_PRIVATE_NONE).map(p -> p.getLoc()).collect(Collectors.toList());
			
			GameObjectsStorage.getAllPlayersStream()
			.filter(p -> p.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
			.filter(p -> !p.isInZone(ZoneType.RESIDENCE))
			.filter(p -> (p.getReflection() == ReflectionManager.DEFAULT) && isInTown(p))
			.filter(p -> p.isInOfflineMode() || ((System.currentTimeMillis() - p.getLastMovePacket()) > 120000)) // Offline
			.forEach(p -> teleportPlayer(p));
			
			/*
			 * int i=0; for (Location loc : LOCATIONS) { ExServerPrimitive packet = new ExServerPrimitive("", loc); packet.addPoint(i+++"", loc); L2ObjectsStorage.getAllPlayersStream().forEach(p -> p.sendPacket(packet)); } L2ObjectsStorage.getAllPlayersStream().forEach(p -> p.sendMessage("LOL"));
			 */
		}
		
	}
	
	private static void teleportPlayer(Player player)
	{
		// Check if the player is already at a bazar point.
		for (Location loc : LOCATIONS)
		{
			if (player.isInRange(loc, 30))
			{
				return;
			}
		}
		
		// Not in a bazar point... tele.
		L1: for (Location loc : LOCATIONS)
		{
			for (Location taken : _takenLocs)
			{
				if (Location.calculateDistance(loc, taken, false) < 30)
					continue L1;
			}
			
			player.teleToLocation(loc);
			_takenLocs.add(loc);
			player.sendMessage("You have been automatically teleported to Giran Harbor Bazar.");
			break;
		}
	}
	
	private static boolean isInTown(Player player)
	{
		if (!player.isInPeaceZone())
			return false;
		
		// Shops in clanhalls allowed.
		if (player.isInZone(ZoneType.RESIDENCE))
			return false;
		
		for (Zone zone : player.getZones())
			if (zone.getName().contains("talking_island_town_peace_zone") || zone.getName().contains("darkelf_town_peace_zone") || zone.getName().contains("elf_town_peace") || zone.getName().contains("guldiocastle_town_peace") || zone.getName().contains("gludin_town_peace") || zone.getName().contains("dion_town_peace") || zone.getName().contains("floran_town_peace") || zone.getName().contains("giran_town_peace") || zone.getName().contains("orc_town_peace") || zone.getName().contains("dwarf_town_peace") || zone.getName().contains("oren_town_peace") || zone.getName().contains("hunter_town_peace") || zone.getName().contains("aden_town_peace") || zone.getName().contains("speaking_port_peace") || zone.getName().contains("gludin_port") || zone.getName().contains("giran_port") || zone.getName().contains("heiness_peace") || zone.getName().contains("godad_peace") || zone.getName().contains("rune_peace") || zone.getName().contains("gludio_airship_peace") || zone.getName().contains("schuttgart_town_peace") || zone.getName().contains("kamael_village_town_peace") || zone.getName().contains("keucereus_alliance_base_town_peace") || zone.getName().contains("giran_harbor_peace_alt") || zone.getName().contains("parnassus_peace"))
				return true;
		
		return false;
	}
	
	@Override
	public void onLoad()
	{
		if (!ENABLED)
			return;
		
		LOCATIONS.clear();
		
		int xLoc = 47832;
		int yLoc = 185400;
		final int z = -3511 + 50;
		
		final double angle = 312; // the angle of rotation in degrees
		final double angle2 = angle - 270;
		final double length = 2250;
		final double length2 = 1250;
		final double step = 50;
		
		for (int j = 0; j < length2; j += step * 2) // Step * 2 cause I wanna make some pathways to go through.
		{
			int xLoc2 = (int) (xLoc + (j * Math.cos(Math.toRadians(angle2))));
			int yLoc2 = (int) (yLoc + (j * Math.sin(Math.toRadians(angle2))));
			
			for (int i = 0; i < length; i += step)
			{
				if ((i % 400) == 0)
					continue;
				
				int x = (int) (xLoc2 + (i * Math.cos(Math.toRadians(angle))));
				int y = (int) (yLoc2 + (i * Math.sin(Math.toRadians(angle))));
				LOCATIONS.add(new Location(x, y, z));
			}
		}
		
		_thread = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckShops(), 60000, 60000);
		_log.info("GiranHarborBazar: Thread has been started.");
	}
	
	@Override
	public void onReload()
	{
		
	}
	
	@Override
	public void onShutdown()
	{
		if (_thread != null)
		{
			_thread.cancel(true);
			_thread = null;
		}
		_log.info("GiranHarborBazar: Thread has been stopped.");
	}
}