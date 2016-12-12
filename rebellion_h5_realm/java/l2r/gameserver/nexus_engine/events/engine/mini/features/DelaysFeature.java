package l2r.gameserver.nexus_engine.events.engine.mini.features;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode.FeatureType;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

/**
 * @author hNoke
 *
 */
public class DelaysFeature extends AbstractFeature
{
	private int rejoinDelay = 600000;
	
	public DelaysFeature(EventType event, PlayerEventInfo gm, String parametersString)
	{
		super(event);
		
		addConfig("RejoinDelay", "The delay player has to wait to rejoin this mode again (in ms). This delay is divided by 2 if the player has lost his last match.", 1);
		
		if(parametersString == null)
			parametersString = "600000";
		
		_params = parametersString;
		initValues();
	}
	
	@Override
	protected void initValues()
	{
		String[] params = splitParams(_params);
		
		try
		{
			rejoinDelay = Integer.parseInt(params[0]);
		} 
		catch (NumberFormatException e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	public int getRejoinDealy()
	{
		return rejoinDelay;
	}
	
	@Override
	public boolean checkPlayer(PlayerEventInfo player)
	{
		// is checked in the event manager
		return true;
	}
	
	@Override
	public FeatureType getType()
	{
		return EventMode.FeatureType.Delays;
	}
}
