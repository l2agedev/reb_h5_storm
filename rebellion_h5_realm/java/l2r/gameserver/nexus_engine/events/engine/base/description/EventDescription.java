/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.base.description;

import l2r.gameserver.nexus_engine.events.engine.base.ConfigModel;

import java.util.Map;

/**
 * @author hNoke
 *
 */
public abstract class EventDescription
{
	public String getDescription(Map<String, ConfigModel> configs) {return ""; }
	public String getDescription(Map<String, ConfigModel> configs, int roundsCount, int teamsCount, int teamSize, int rejoinDelay, int timeLimit) {return "";}

	public final String getString(Map<String, ConfigModel> configs, String propName)
	{
		if(configs.containsKey(propName))
		{
			return configs.get(propName).getValue();
		}
		
		return "";
	}
	
	public final int getInt(Map<String, ConfigModel> configs, String propName)
	{
		if(configs.containsKey(propName))
		{
			return configs.get(propName).getValueInt();
		}
		
		return 0;
	}
	
	public final boolean getBoolean(Map<String, ConfigModel> configs, String propName)
	{
		if(configs.containsKey(propName))
			return configs.get(propName).getValueBoolean();
		
		return false;
	}
}
