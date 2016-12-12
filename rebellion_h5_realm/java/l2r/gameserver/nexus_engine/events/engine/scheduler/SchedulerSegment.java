package l2r.gameserver.nexus_engine.events.engine.scheduler;

/**
 * Created by honke_000
 */
public abstract class SchedulerSegment
{
	public SegmentType _type;

	SchedulerSegment(SegmentType type)
	{
		_type = type;
	}
}
