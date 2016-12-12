package l2r.gameserver.nexus_engine.events.engine.mini.features;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode.FeatureType;
import l2r.gameserver.nexus_engine.events.engine.mini.RegistrationData;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

/**
 * @author hNoke
 *
 */
public class StrenghtChecksFeature extends AbstractFeature
{
	private int maxLevelDiff = 5;
	private int maxGearScoreDiff = 0;
	
	public StrenghtChecksFeature(EventType event, PlayerEventInfo gm, String parametersString)
	{
		super(event);
		
		addConfig("MaxLevelDiff", "The maximal acceptable level difference between players to start the match. For parties/teams, it calculates the average level of all players inside it. Put '0' to disable this config.", 1);
		addConfig("MaxGearScoreDiff", "The maximal acceptable gear score difference between players to start the match. For parties/teams, it calculates the average gearscore  of all players inside it. Put '0' to disable this config.", 1);

		if(parametersString == null)
			parametersString = "5,0";
		
		_params = parametersString;
		initValues();
	}
	
	@Override
	protected void initValues()
	{
		String[] params = splitParams(_params);
		
		try
		{
			maxLevelDiff = Integer.parseInt(params[0]);
			maxGearScoreDiff = Integer.parseInt(params[1]);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			_params = "5,0";
			initValues();
		}
	}
	
	public int getMaxLevelDiff()
	{
		return maxLevelDiff;
	}

	public int getMaxGearScoreDiff()
	{
		return maxGearScoreDiff;
	}

	public boolean canFight(RegistrationData player, RegistrationData opponent)
	{
		if(Math.abs(player.getAverageLevel() - opponent.getAverageLevel()) > maxLevelDiff)
			return false;

		if(maxGearScoreDiff > 0)
		{
			double gScore1 = player.getAverageGearScore();
			double gScore2 = player.getAverageGearScore();

			if(Math.abs(gScore1 - gScore2) > maxGearScoreDiff)
				return false;
		}

		return true;
	}
	
	@Override
	public boolean checkPlayer(PlayerEventInfo player)
	{
		return true;
	}
	
	@Override
	public FeatureType getType()
	{
		return EventMode.FeatureType.StrenghtChecks;
	}
}
