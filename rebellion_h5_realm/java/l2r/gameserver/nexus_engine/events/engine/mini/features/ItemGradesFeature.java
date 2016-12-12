package l2r.gameserver.nexus_engine.events.engine.mini.features;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode.FeatureType;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.ItemData;

/**
 * @author hNoke
 *
 */
public class ItemGradesFeature extends AbstractFeature
{
	private int[] allowedGrades;
	
	public ItemGradesFeature(EventType event, PlayerEventInfo gm, String parametersString)
	{
		super(event);
		
		addConfig("GradesAvailable", "Write the letters of all allowed item grades here. Separate by SPACE. Eg. <font color=LEVEL>a s s80</font>.", 1);
		
		if(parametersString == null)
			parametersString = "no d c b a s s80 s84";
		
		_params = parametersString;
		initValues();
	}
	
	@Override
	protected void initValues()
	{
		String[] params = splitParams(_params);
		
		try
		{
			String splitted[] = params[0].split(" ");
			allowedGrades = new int[splitted.length];
			
			for(int i = 0; i < splitted.length; i++)
			{
				allowedGrades[i] = CallBack.getInstance().getOut().getGradeFromFirstLetter(splitted[i]);
			}
		} 
		catch (NumberFormatException e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	
	
	public int[] getGrades()
	{
		return allowedGrades;
	}
	
	public boolean checkItem(PlayerEventInfo player, ItemData item)
	{
		int type = item.getCrystalType().cry;
		boolean allowed = false;
		
		if(item.isArmor() || item.isWeapon())
		{
			for(int grade : allowedGrades)
			{
				if(type == grade)
				{
					allowed = true;
				}
			}
		}
		else
			allowed = true;
		
		return allowed;
	}
	
	@Override
	public boolean checkPlayer(PlayerEventInfo player)
	{
		boolean canJoin = true;
		for(ItemData item : player.getItems())
		{
			if(!checkItem(player, item))
			{
				canJoin = false;
				player.sendMessage("(G)Please put item " + item.getItemName() + " to your warehouse before participating. It is not allowed for this event.");
			}
		}
		
		return canJoin;
	}
	
	@Override
	public FeatureType getType()
	{
		return EventMode.FeatureType.ItemGrades;
	}
}
