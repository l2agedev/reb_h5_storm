package l2r.gameserver.nexus_engine.events.engine.base;

import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.l2r.CallBack;

/**
 * @author hNoke
 * a mother class of all Nexus events
 */
public abstract class Event
{
	protected EventType _type;
	
	public Event(EventType type)
	{
		_type = type;
	}
	
	public final EventType getEventType()
	{
		return _type;
	}
	
	public String getEventName()
	{
		return _type.getAltTitle();
	}
	
	public void announce(String text)
	{
		CallBack.getInstance().getOut().announceToAllScreenMessage(text, getEventType().getAltTitle());
	}
	
	// utilities
	protected void debug(String text)
	{
		NexusLoader.debug(text);
	}
	
	/** detailed debug */
	protected void print(String msg)
	{
		NexusLoader.detailedDebug(msg);
	}
}
