package l2r.gameserver.nexus_engine.events.engine.mini.features;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode.FeatureType;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

/**
 * @author hNoke
 *
 */
public class TimeLimitFeature extends AbstractFeature
{
	private int timeLimit = 600000;
	
	public TimeLimitFeature(EventType event, PlayerEventInfo gm, String parametersString)
	{
		super(event);
		
		addConfig("TimeLimit", "Event's time limit, after which the event will be automatically ended (in ms).", 1);
		
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
			timeLimit = Integer.parseInt(params[0]);
		} 
		catch (NumberFormatException e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	public int getTimeLimit()
	{
		return timeLimit;
	}
	
	@Override
	public boolean checkPlayer(PlayerEventInfo player)
	{
		// is checked in the event
		return true;
	}
	
	@Override
	public FeatureType getType()
	{
		return EventMode.FeatureType.TimeLimit;
	}
}
