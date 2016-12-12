package ai.hellbound;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.utils.Location;


public class Darion extends Fighter
{
	private static final int[] doors = {
			20250009,
			20250004,
			20250005,
			20250006,
			20250007
	};

	public Darion(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		NpcInstance actor = getActor();
		for(int i = 0; i < 5; i++)
			SpawnTable.spawnSingle(Rnd.get(25614, 25615), Location.findPointToStay(actor, 400, 900));

		//Doors
		for (final int doorId : doors)
		{
			DoorInstance door = ZoneHolder.getDoor(doorId);
			door.closeMe();
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		// Doors
		for (final int doorId : doors)
		{
			DoorInstance door = ZoneHolder.getDoor(doorId);
			door.openMe();
		}

		for(NpcInstance npc : GameObjectsStorage.getAllByNpcId(25614, false))
			npc.deleteMe();

		for(NpcInstance npc : GameObjectsStorage.getAllByNpcId(25615, false))
			npc.deleteMe();

		super.onEvtDead(killer);
	}

}