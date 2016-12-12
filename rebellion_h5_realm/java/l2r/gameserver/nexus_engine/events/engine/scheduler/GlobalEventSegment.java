package l2r.gameserver.nexus_engine.events.engine.scheduler;

/**
 * Created by honke_000
 */
public class GlobalEventSegment extends SchedulerSegment
{
	public int _chance;
	public int _breakDuration;
	GlobalEventSegment(int chance, int breakDuration)
	{
		super(SegmentType.GLOBAL);
		_chance = chance;
		_breakDuration = breakDuration;
	}
}
