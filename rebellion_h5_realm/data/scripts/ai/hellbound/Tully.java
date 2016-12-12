package ai.hellbound;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.utils.Location;

public class Tully extends Fighter
{
	private static Zone _zone = ZoneHolder.getZone("[tully5_damage]");
	
	// 32371
	private static final Location[] locSD =
	{
		new Location(-10831, 273890, -9040, 81895),
		new Location(-10817, 273986, -9040, -16452),
		new Location(-13773, 275119, -9040, 8428),
		new Location(-11547, 271772, -9040, -19124),
	};
	
	// 22392
	private static final Location[] locFTT =
	{
		new Location(-10832, 273808, -9040, 0),
		new Location(-10816, 274096, -9040, 14964),
		new Location(-13824, 275072, -9040, -24644),
		new Location(-11504, 271952, -9040, 9328),
	};
	
	private boolean s = false;
	private static NpcInstance removable_ghost = null;
	
	public Tully(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		for (final Location aLocSD : locSD)
			try
			{
				NpcInstance npc = SpawnTable.spawnSingle(32371, aLocSD);
				if (!s)
				{
					Functions.npcShout(npc, "Self Destruction mechanism launched: 10 minutes to explosion");
					s = true;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		for (final Location aLocFTT : locFTT)
			try
			{
				SpawnTable.spawnSingle(22392, aLocFTT);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		try
		{
			removable_ghost = SpawnTable.spawnSingle(32370, new Location(-11984, 272928, -9040, 23644));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		ThreadPoolManager.getInstance().schedule(new UnspawnAndExplode(), 600 * 1000L); // 10 mins
		ZoneHolder.getDoor(19260051).openMe();
		ZoneHolder.getDoor(19260052).openMe();
		super.onEvtDead(killer);
	}
	
	private class UnspawnAndExplode extends RunnableImpl
	{
		public UnspawnAndExplode()
		{
		}
		
		@Override
		public void runImpl()
		{
			ThreadPoolManager.getInstance().schedule(new setZoneInActive(), 600 * 1000L); // 10 mins
			
			_zone.setActive(true);
			
			ZoneHolder.getDoor(19260051).closeMe();
			ZoneHolder.getDoor(19260052).closeMe();
			
			for (NpcInstance npc : GameObjectsStorage.getAllByNpcId(32371, true))
				npc.deleteMe();
			
			for (NpcInstance npc : GameObjectsStorage.getAllByNpcId(22392, true))
				npc.deleteMe();
			
			if (removable_ghost != null)
				removable_ghost.deleteMe();
		}
	}
	
	@Override
	protected void onEvtSpawn()
	{
		for (NpcInstance npc : GameObjectsStorage.getAllByNpcId(32370, true))
			npc.deleteMe();
	}
	
	private class setZoneInActive extends RunnableImpl
	{
		public setZoneInActive()
		{
		}
		
		@Override
		public void runImpl()
		{
			_zone.setActive(false);
			SpawnTable.spawnSingle(32370, new Location(-14643, 274588, -9040, 49152));
		}
	}
}