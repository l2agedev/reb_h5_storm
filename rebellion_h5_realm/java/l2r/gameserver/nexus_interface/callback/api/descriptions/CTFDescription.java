/**
 * 
 */
package l2r.gameserver.nexus_interface.callback.api.descriptions;

import l2r.gameserver.nexus_engine.events.engine.base.ConfigModel;
import l2r.gameserver.nexus_engine.events.engine.base.description.EventDescription;

import java.util.Map;

/**
 * @author hNoke
 *
 */
public class CTFDescription extends EventDescription
{
	@Override
	public String getDescription(Map<String, ConfigModel> configs)
	{
		//TODO: translate this shits.
		String text;
		text = "There are " + getInt(configs, "teamsCount") + " teams; in order to score you need to steal enemy team's flag and bring it back your team's base (to the flag holder). ";
		
		if(getInt(configs, "flagReturnTime") > -1)
			text += "If you hold the flag and don't manage to score within " + getInt(configs, "flagReturnTime")/1000 + " seconds, the flag will be returned back to enemy's flag holder. ";
				
		if(getBoolean(configs, "waweRespawn"))
			text += "Dead players are resurrected by an advanced wawe-spawn engine each " + getInt(configs, "resDelay") + " seconds.";
		else
			text += "If you die, you will be resurrected in " + getInt(configs, "resDelay") + " seconds. ";
		
		if(getBoolean(configs, "createParties"))
			text += "The event automatically creates parties on start.";
		
		return text;
	}
}
