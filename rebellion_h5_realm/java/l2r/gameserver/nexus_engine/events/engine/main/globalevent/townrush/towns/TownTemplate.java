package l2r.gameserver.nexus_engine.events.engine.main.globalevent.townrush.towns;

import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;


/**
 * Created by honke
 */
public abstract class TownTemplate
{
	public abstract EventSpawn getPlayersSpawn();


	public abstract String getName();
}
