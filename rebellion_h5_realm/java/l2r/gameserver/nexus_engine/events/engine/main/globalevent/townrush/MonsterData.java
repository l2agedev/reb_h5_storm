package l2r.gameserver.nexus_engine.events.engine.main.globalevent.townrush;

import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;

/**
 * Created by honke_000
 */
public class MonsterData
{
	private EventSpawn _spawn;
	private int _npcId;

	public MonsterData(EventSpawn spawn, int npcId)
	{
		_spawn = spawn;
		_npcId = npcId;
	}

	public EventSpawn getSpawn()
	{
		return _spawn;
	}

	public void set_spawn(EventSpawn spawn)
	{
		this._spawn = spawn;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public void setNpcId(int npcId)
	{
		_npcId = npcId;
	}
}
