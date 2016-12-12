package l2r.gameserver.randoms;

import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.RaidBossInstance;
import l2r.gameserver.model.reward.RewardGroup;
import l2r.gameserver.model.reward.RewardItem;
import l2r.gameserver.model.reward.RewardList;
import l2r.gameserver.model.reward.RewardType;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.DocumentParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * @author Infern0
 *
 */
public class CustomDroplist extends DocumentParser
{
	private final Map<Integer[], Map<RewardType, RewardList>> _templates = new HashMap<>();
	
	
	public CustomDroplist()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_templates.clear();
		parseFile(Config.getFile("config/CustomDroplist.xml"));
		insertData();
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d1 = n.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
				{
					if(d1.getNodeName().equalsIgnoreCase("npc"))
					{
						NamedNodeMap attrs = d1.getAttributes();
						
						int minRange = parseInteger(attrs, "minLevel");
						int maxRange = parseInteger(attrs, "maxLevel");
						Integer[] range = new Integer[]
						{
							minRange,
							maxRange
						};
						
						Map<RewardType, RewardList> map = new HashMap<>();
						
						for (Node d = d1.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if(d.getNodeName().equalsIgnoreCase("rewardlist"))
							{
								attrs = d.getAttributes();
								
								RewardType type = RewardType.valueOf(parseString(attrs, "type"));
								RewardList list = new RewardList(type, false);

								for (Node group1 = d.getFirstChild(); group1 != null; group1 = group1.getNextSibling())
								{
									if(group1.getNodeName().equalsIgnoreCase("group"))
									{
										attrs = group1.getAttributes();
										double enterChance = parseDouble(attrs, "chance") == null ? RewardList.MAX_CHANCE : parseDouble(attrs, "chance") * 10000;

										RewardGroup group = (type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED) ? null : new RewardGroup(enterChance);
										
										for (Node rwrds = group1.getFirstChild(); rwrds != null; rwrds = rwrds.getNextSibling())
										{
											if(rwrds.getNodeName().equalsIgnoreCase("reward"))
											{
												attrs = rwrds.getAttributes();
												RewardItem data = parseReward(attrs);
												group.addData(data);
											}
										}

										if(group != null)
											list.add(group);
									}
									else if(group1.getNodeName().equalsIgnoreCase("reward"))
									{
										attrs = group1.getAttributes();
										
										if(type != RewardType.SWEEP && type != RewardType.NOT_RATED_NOT_GROUPED)
											continue;
										
										RewardItem data;
										data = parseReward(attrs);
										RewardGroup g = new RewardGroup(RewardList.MAX_CHANCE);
										g.addData(data);
										list.add(g);
									}
								}
								
								map.put(type, list);
								_templates.put(range, map);
							}
						}
					}
				}
			}
		}
	}
	
	public void insertData()
	{
		for (NpcTemplate npc : NpcHolder.getInstance().getAll())
		{
			if (npc == null)
				continue;
			
			// only raid and monsters can recieve drop
			if(npc.isInstanceOf(RaidBossInstance.class) || npc.isInstanceOf(MonsterInstance.class))
			{
				for (Entry<Integer[], Map<RewardType, RewardList>> data : _templates.entrySet())
				{
					Integer[] level = data.getKey();
					Map<RewardType, RewardList> droplist = data.getValue();
					int minLevel = level[0];
					int maxLevel = level[1];
					
					// check for levels
					if (npc.getLevel() >= minLevel  && npc.getLevel() <= maxLevel)
					{
						for (Entry<RewardType, RewardList> dropdata : droplist.entrySet())
						{
							npc.putRewardList(dropdata.getKey(), dropdata.getValue());
						}
					}
				}
			}
		}
	}

	private RewardItem parseReward(NamedNodeMap map)
	{
		int itemId =  parseInteger(map, "item_id");
		int min = parseInteger(map, "min");
		int max = parseInteger(map, "max");
		double chance = parseDouble(map, "chance") * 10000;
		
		if(chance > 1000000)
			chance = 1000000;
		
		RewardItem data = new RewardItem(itemId);
		data.setChance(chance);
		data.setMinDrop(min);
		data.setMaxDrop(max);

		return data;
	}
	
	public static CustomDroplist getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CustomDroplist _instance = new CustomDroplist();
	}
}