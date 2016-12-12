package l2r.gameserver.nexus_engine.playervalue;

import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.playervalue.criteria.GearScore;
import l2r.gameserver.nexus_engine.playervalue.criteria.ICriteria;
import l2r.gameserver.nexus_engine.playervalue.criteria.PlayerClass;
import l2r.gameserver.nexus_engine.playervalue.criteria.PlayerLevel;
import l2r.gameserver.nexus_engine.playervalue.criteria.PlayerSkills;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.List;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public class PlayerValueEngine
{
	private List<ICriteria> criterias = new FastTable<ICriteria>();
	
	public PlayerValueEngine()
	{
		load();
		NexusLoader.debug("Nexus Engine: Loaded PlayerValue engine.");
	}
	
	private void load()
	{
		criterias.add(GearScore.getInstance());
		criterias.add(PlayerClass.getInstance());
		criterias.add(PlayerLevel.getInstance());
		criterias.add(PlayerSkills.getInstance());
	}
	
	public void addCriteria(ICriteria c)
	{
		criterias.add(c);
	}
	
	public int getPlayerValue(PlayerEventInfo player)
	{
		if(!EventConfig.getInstance().getGlobalConfigBoolean("GearScore", "enableGearScore"))
			return 0;
		
		int value = 0;
		for(ICriteria i : criterias)
		{
			value += i.getPoints(player);
		}
		return value;
	}
	
	public static final PlayerValueEngine getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PlayerValueEngine _instance = new PlayerValueEngine();
	}
}
