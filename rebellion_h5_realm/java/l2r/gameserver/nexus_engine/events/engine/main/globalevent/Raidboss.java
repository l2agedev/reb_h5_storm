/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.main.globalevent;

import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;
import l2r.gameserver.nexus_engine.events.engine.base.Loc;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.NpcData;

import java.util.List;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public class Raidboss extends GlobalEvent
{
	private final List<BossData> _data = new FastTable<BossData>();
	
	public Raidboss()
	{
		BossData data = new BossData(9996, 
				new EventSpawn(1, 1, new Loc(1, 1, 1), 1, "Regular"),
				new EventSpawn(1, 1, new Loc(1, 1, 1), 1, "Regular"));
		
		_data.add(data);
		
		
	}
	
	
	@SuppressWarnings("unused")
	private NpcData _raidboss;
	
	@Override
	public String getName()
	{
		return "Raidboss";
	}

	@Override
	public void start(String param)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void end()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canRegister(PlayerEventInfo player)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPlayer(PlayerEventInfo player)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getStateNameForHtml()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public class BossData
	{
		int bossId;
		EventSpawn bossSpawn;
		EventSpawn playerTeleport;
		
		BossData(int id, EventSpawn spawn, EventSpawn tp)
		{
			this.bossId = id;
			this.bossSpawn = spawn;
			this.playerTeleport = tp;
		}
	}

	@Override
	public boolean canStart(String param)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reload()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void monsterDies(NpcData npc)
	{
		// TODO Auto-generated method stub
		
	}
}
