package l2r.gameserver.nexus_engine.events.engine.scheduler;

/**
 * Created by honke_000
 */
public enum SegmentType
{
	BREAK("Waiting"),
	MAIN("Main event"),
	MINI("Mini events"),
	GLOBAL("Global event");
	
	public final String _name;
	SegmentType(String name)
	{
		_name = name;
	}
}
