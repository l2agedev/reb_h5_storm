package l2r.gameserver.nexus_engine.events.engine.configtemplate;

import l2r.gameserver.nexus_engine.events.Configurable;
import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

/**
 * @author hNoke
 * - work in progress -
 */
public abstract class ConfigTemplate 
{
	public abstract String getName();
	public abstract EventType getEventType();
	public abstract String getDescription();
	public abstract SetConfig[] getConfigs();
	
	public class SetConfig
	{
		String key;
		String value;
		
		public SetConfig(String key, String value)
		{
			this.key = key;
			this.value = value;
		}
	}
	
	//TODO implement this in admin panel
	public void applyTemplate(PlayerEventInfo gm, EventType type, Configurable event)
	{
		int changed = 0;
		for(SetConfig sc : getConfigs())
		{
			if(event.getConfigs().containsKey(sc.key))
			{
				if(event.getConfigs().get(sc.key).getValue().equals(sc.value))
					continue;
				
				event.getConfigs().get(sc.key).setValue(sc.value);
				changed ++;
			}
		}
		
		int total = event.getConfigs().size();
		
		EventConfig.getInstance().updateInDb(type);
		
		gm.sendMessage("Applied template " + getName() + " to event " + type.getAltTitle() + ". " + changed + "/" + total + " configs have been changed.");
	}
	
	//TODO check for missing configs somehow
	//add an array with those configs as template
}
