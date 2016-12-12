package l2r.gameserver.model.entity.events.objects;

import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.utils.Location;
import l2r.gameserver.tables.SpawnTable;

public class SpawnSimpleObject implements SpawnableObject
{
	private int _npcId;
	private Location _loc;

	private NpcInstance _npc;

	public SpawnSimpleObject(int npcId, Location loc)
	{
		_npcId = npcId;
		_loc = loc;
	}

	@Override
	public void spawnObject(GlobalEvent event)
	{
		_npc = SpawnTable.spawnSingle(_npcId, _loc, event.getReflection());
		if (_npc != null)
			_npc.addEvent(event);
	}

	@Override
	public void despawnObject(GlobalEvent event)
	{
		if (_npc != null)
		{
			_npc.removeEvent(event);
			_npc.deleteMe();
			_npc = null;
		}
	}

	@Override
	public void refreshObject(GlobalEvent event)
	{

	}
}
