package l2r.gameserver.nexus_engine.events.engine.scheduler;

/**
 * Created by honke_000
 */
public class BreakSegment extends SchedulerSegment
{
	public int _duration;

	BreakSegment(int duration)
	{
		super(SegmentType.BREAK);
		_duration = duration;
	}
}
