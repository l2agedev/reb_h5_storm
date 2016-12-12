package l2r.gameserver.nexus_engine.events;

import l2r.gameserver.nexus_engine.events.engine.base.ConfigModel;
import l2r.gameserver.nexus_engine.events.engine.base.EventMap;
import l2r.gameserver.nexus_engine.events.engine.base.RewardPosition;
import l2r.gameserver.nexus_engine.events.engine.base.SpawnType;

import java.util.Map;

import javolution.util.FastTable;

/**
 * @author hNoke
 * configurable part of an event (MiniEventManager, AbstractMainEvent)
 */
public interface Configurable
{
	public void loadConfigs();
	public void clearConfigs();
	public FastTable<String> getCategories();
	
	public Map<String, ConfigModel> getConfigs();
	public Map<String, ConfigModel> getMapConfigs();
	public RewardPosition[] getRewardTypes();
	public Map<SpawnType, String> getAvailableSpawnTypes();
	
	public void setConfig(String key, String value, boolean addToValue);
	public String getDescriptionForReward(RewardPosition reward);
	
	public int getTeamsCount();
	public boolean canRun(EventMap map);
	public String getMissingSpawns(EventMap map);
}
