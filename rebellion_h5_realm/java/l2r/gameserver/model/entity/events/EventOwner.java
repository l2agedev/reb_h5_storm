package l2r.gameserver.model.entity.events;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class EventOwner implements Serializable
{
	private Set<GlobalEvent> _events = new HashSet<GlobalEvent>(2);

	@SuppressWarnings("unchecked")
	public <E extends GlobalEvent> E getEvent(Class<E> eventClass)
	{
		for(GlobalEvent e : _events)
		{
			if(e.getClass() == eventClass)    // fast hack
				return (E)e;
			if(eventClass.isAssignableFrom(e.getClass()))    //FIXME [VISTALL]    какойто другой способ определить
				return (E)e;
		}

		return null;
	}

	public void addEvent(GlobalEvent event)
	{
		_events.add(event);
	}
	
	public void removeEvent(GlobalEvent event)
	{
		_events.remove(event);
	}
	
	public Set<GlobalEvent> getEvents()
	{
		return _events;
	}
	
	public void removeEvents(Class<? extends GlobalEvent> eventClass)
	{
		for (GlobalEvent e : _events)
		{
			if (e.getClass() == eventClass)
				_events.remove(e);
			else if (eventClass.isAssignableFrom(e.getClass()))
				_events.remove(e);
		}
	}
}
