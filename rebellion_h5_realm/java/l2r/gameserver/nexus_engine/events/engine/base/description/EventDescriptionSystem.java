/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.base.description;

import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.events.engine.base.EventType;

import java.util.Map;
import java.util.logging.Level;

import javolution.util.FastMap;

/**
 * @author hNoke
 *
 */
public class EventDescriptionSystem
{
	private Map<EventType, EventDescription> _descriptions;
	
	public EventDescriptionSystem()
	{
		_descriptions = new FastMap<EventType, EventDescription>();
		NexusLoader.debug("Loaded editable Event Description system.", Level.INFO);
	}
	
	public void addDescription(EventType type, EventDescription description)
	{
		_descriptions.put(type, description);
	}
	
	public EventDescription getDescription(EventType type)
	{
		if(_descriptions.containsKey(type))
			return _descriptions.get(type);
		else return null;
	}
	
	public static final EventDescriptionSystem getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final EventDescriptionSystem _instance = new EventDescriptionSystem();
	}
}
