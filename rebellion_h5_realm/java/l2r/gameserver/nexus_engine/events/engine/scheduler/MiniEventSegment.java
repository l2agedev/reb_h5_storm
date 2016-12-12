package l2r.gameserver.nexus_engine.events.engine.scheduler;

import javolution.util.FastTable;

import java.util.List;


/**
 * Created by honke_000
 */
public class MiniEventSegment extends SchedulerSegment
{
	public int _regDuration;

	public List<Mode> _modes = new FastTable<>();

	public class Mode
	{
		public String _name;
		public int _id;
		public int _chance;
		public int _eventClass;

		Mode(String name, int id, int chance, int eventClass)
		{
			_name = name;
			_id = id;
			_chance = chance;
			_eventClass = eventClass;
		}
	}

	MiniEventSegment(int duration)
	{
		super(SegmentType.MINI);
		_regDuration = duration;
	}

	public void addMode(String eventName, int modeId, int chance, int eventClass)
	{
		Mode mode = new Mode(eventName, modeId, chance, eventClass);
		_modes.add(mode);
	}
}
