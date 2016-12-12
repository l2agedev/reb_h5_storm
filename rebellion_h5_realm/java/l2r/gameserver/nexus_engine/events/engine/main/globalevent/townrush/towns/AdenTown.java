package l2r.gameserver.nexus_engine.events.engine.main.globalevent.townrush.towns;

import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;
import l2r.gameserver.nexus_engine.events.engine.base.Loc;


/**
 * Created by honke_000
 */
public class AdenTown extends TownTemplate
{



	@Override
	public EventSpawn getPlayersSpawn()
	{
		return new EventSpawn(1, 1, new Loc(147463, 10366, -667), 1, "Regular");
	}

	@Override
	public String getName()
	{
		return "Aden";
	}
}
