package l2r.gameserver.nexus_engine.events.engine.scheduler;

/**
 * Created by honke_000
 */
public class MainEventSegment extends SchedulerSegment
{
	public int _regDuration;

	MainEventSegment(int duration)
	{
		super(SegmentType.MAIN);
		_regDuration = duration;
	}
}
