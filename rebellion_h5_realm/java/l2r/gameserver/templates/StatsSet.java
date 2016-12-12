package l2r.gameserver.templates;

import l2r.commons.collections.MultiValueSet;

public class StatsSet extends MultiValueSet<String>
{
	public static final StatsSet EMPTY = new StatsSet()
	{
		@Override
		public Object put(String a, Object a2)
		{
			throw new UnsupportedOperationException();
		}
	};

	public StatsSet()
	{
		super();
	}

	public StatsSet(StatsSet set)
	{
		super(set);
	}

	@Override
	public StatsSet clone()
	{
		return new StatsSet(this);
	}
}