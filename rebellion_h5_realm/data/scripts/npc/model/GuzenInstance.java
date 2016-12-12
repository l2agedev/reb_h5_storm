package npc.model;

import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.model.instances.SpecialMonsterInstance;
import l2r.gameserver.templates.npc.NpcTemplate;

public class GuzenInstance extends SpecialMonsterInstance
{
	private static final int GuzenDoor = 20260004;
	
	public GuzenInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	protected void onSpawn()
	{
		DoorInstance door = ZoneHolder.getDoor(GuzenDoor);
		
		if (door.isOpen())
			door.closeMe();
		super.onSpawn();
	}
	
	protected void onDeath(Creature killer)
	{
		DoorInstance door = ZoneHolder.getDoor(GuzenDoor);
		
		if (!door.isOpen())
			door.openMe();
		super.onDeath(killer);
	}
}