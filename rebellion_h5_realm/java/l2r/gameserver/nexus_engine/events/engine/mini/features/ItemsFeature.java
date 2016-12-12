package l2r.gameserver.nexus_engine.events.engine.mini.features;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode.FeatureType;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.ItemData;

import java.util.Arrays;

/**
 * @author hNoke
 *
 */
public class ItemsFeature extends AbstractFeature
{
	private boolean allowPotions = true;
	private boolean allowScrolls = true;
	
	private int[] disabledItems = null;
	private String[] enabledTiers = null;
	private double maxGearScore = -1;
	
	public ItemsFeature(EventType event, PlayerEventInfo gm, String parametersString)
	{
		super(event);
		
		addConfig("AllowPotions", "Will the potions be enabled for this mode?", 1);
		addConfig("AllowScrolls", "Will the scrolls be enabled for this mode?", 1);
		
		addConfig("DisabledItems", "Specify here which items will be disabled (not usable/equipable) for this mode. Write their IDs and separate by SPACE. Eg. <font color=LEVEL>111 222 525</font>. Put <font color=LEVEL>0</font> to disable this config.", 2);
		
		addConfig("EnabledTiers", "This config is not fully implemented. Requires gameserver support.", 2);
		
		addConfig("MaxGearScore", "Max Gear score for this event.", 2);
		
		if(parametersString == null || parametersString.split(",").length != 3)
			parametersString = "true,false,0,Allitems,-1";
		
		_params = parametersString;
		initValues();
	}
	
	@Override
	protected void initValues()
	{
		String[] params = splitParams(_params);
		
		try
		{
			allowPotions = Boolean.parseBoolean(params[0]);
			allowScrolls = Boolean.parseBoolean(params[1]);
			
			String splitted[] = params[2].split(" ");
			disabledItems = new int[splitted.length];
			
			for(int i = 0; i < splitted.length; i++)
			{
				disabledItems[i] = Integer.parseInt(splitted[i]);
			}
			
			Arrays.sort(disabledItems);
			
			String splitted2[] = params[3].split(" ");
			enabledTiers = new String[splitted2.length];
			for (int i = 0; i < splitted2.length; i++)
			{
				if (splitted2[i].length() > 0)
				{
					enabledTiers[i] = splitted2[i];
				}
			}
			
			Arrays.sort(enabledTiers);
			
			maxGearScore = Double.parseDouble(params[4]);
		} 
		catch (Exception e)
		{
			_params = "true,false,0,Allitems,-1";
			initValues();
			e.printStackTrace();
		}
	}
	
	public boolean checkItem(PlayerEventInfo player, ItemData item)
	{
		if (!allowPotions && item.isPotion())
			return false;
		
		if (!allowScrolls && item.isScroll())
			return false;
		
		if (Arrays.binarySearch(disabledItems, item.getItemId()) >= 0)
			return false;
		
		if (!checkIfAllowed(item))
			return false;
		      
		return true;
	}
	
	private boolean checkIfAllowed(ItemData item)
	{
		return true;
	}
	
	public double getMaxGearScore()
	{
		return maxGearScore;
	}
	
	@Override
	public boolean checkPlayer(PlayerEventInfo player)
	{
		boolean canJoin = true;
		
		if(maxGearScore <= 0)
			return true;
		
		double gearScore = player.getGearScore();
		
		if(gearScore >= maxGearScore)
		{
			player.screenMessage("Your Player rating is too high.", "Event", false);
			player.sendMessage("The Player rating index for this event is " + maxGearScore + ", yours is " + gearScore + ". Check out Wondrous Cubic -> Stats (click on Player rating index) for more info.");
			canJoin = false;
		}
		
		return canJoin;
	}
	
	@Override
	public FeatureType getType()
	{
		return EventMode.FeatureType.Items;
	}
}
