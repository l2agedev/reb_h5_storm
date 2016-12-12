package l2r.gameserver.model.entity.events.impl;

import l2r.commons.collections.MultiValueSet;
import l2r.gameserver.model.entity.events.GlobalEvent;

public class FantasiIsleParadEvent extends GlobalEvent
{
	public FantasiIsleParadEvent(MultiValueSet<String> set)
	{
		super(set);
	}
	
	@Override
	public void reCalcNextTime(boolean isServerStarted)
	{
		clearActions();
	}
	
	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 30000L;
	}
}
