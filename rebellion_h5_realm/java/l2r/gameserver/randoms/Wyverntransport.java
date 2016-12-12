package l2r.gameserver.randoms;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExRotation;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.FuncSet;
import l2r.gameserver.tables.PetDataTable;
import l2r.gameserver.utils.Location;

/**
 * Travel with Wyvern
 * @author Infern0
 */

public class Wyverntransport
{
	private static Wyverntransport _instance;
	
	public static Wyverntransport getInstance()
	{
		if (_instance == null)
			_instance = new Wyverntransport();
		return _instance;
	}
	
	public void wyvernTeleport(final Player player, final Location loc)
	{
		if (player == null)
			return;
		
		player.broadcastPacket(new ExRotation(player.getObjectId(), 180));
		
		player.block();
		player.setWyvernTeleport(true);
		
		player.addStatFunc(new FuncSet(Stats.RUN_SPEED, 0x90, player, 180));
		
		player.setMount(PetDataTable.WYVERN_ID, 0, 0);
		//player.sendPacket(new SpecialCamera(player.getObjectId(), 900, 150, 140, 0, 9000, 1, 1, 1, 0));
		player.setFlying(true);
		player.setLoc(player.getLoc().changeZ(2000));
		player.validateLocation(1);
		player.moveToLocation(new Location(loc.getX(), loc.getY(), loc.getZ()), 0, false);
		ThreadPoolManager.getInstance().schedule(new Teleport(player, loc), 10000);
	}
	
	public static class Teleport implements Runnable
	{
		private final long playerStoreId;
		private Location _loc;
		
		public Teleport(Player player, Location loc)
		{
			playerStoreId = player.getStoredId();
			_loc = loc;
		}
		
		@Override
		public void run()
		{
			final Player pl = GameObjectsStorage.getAsPlayer(playerStoreId);
			if (pl != null)
			{
				pl.unblock();
				pl.setWyvernTeleport(false);
				pl.removeStatsOwner(pl);
				pl.setFlying(false);
				pl.setRiding(false);
				pl.setLastServerPosition(null);
				pl.setLastClientPosition(null);
				pl.stopMove();
				pl.dismount();
				pl.teleToLocation(_loc.coordsRandomize(10, 100));
			}
		}
	}
}
