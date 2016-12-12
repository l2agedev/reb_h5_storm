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
public class BattlefieldDescription extends EventDescription
{
	@Override
	public String getDescription(Map<String, ConfigModel> configs)
	{
		String text;
		text = "This is a team-based event. The goal of this event is to capture bases. The count of bases is configurable, so is the method of capturing. ";
		text += "Usually, in order to capture a base, a majority of players from one team needs to stay close to the base (which is represented by an NPC) for ";
		text += "some time and don't let the other team come close. But this thing is very customizable, you will see yourself. ";
		text += "When a base is captured, it will stay captured untill another team captures it for itself. The team, which ownes most bases in the event gets most score. ";
		text += "This is a team-based event. The goal of this event is to capture bases. The count of bases is configurable, so is the method of capturing. ";

		if(getBoolean(configs, "waweRespawn"))
			text += "Dead players are resurrected by an advanced wawe-spawn engine each " + getInt(configs, "resDelay") + " seconds.";
		else
			text += "If you die, you will be resurrected in " + getInt(configs, "resDelay") + " seconds. ";
		
		if(getBoolean(configs, "createParties"))
			text += "The event automatically creates parties on start.";
		
		return text;
	}
}
