package l2r.gameserver.instancemanager.games;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityReward implements OnDeathListener
{
	private static final Logger _log = LoggerFactory.getLogger(ActivityReward.class);
	private static ActivityReward _instance = new ActivityReward();
	
	private List<Integer[]> _rewards;
	private Map<Integer, Long> _activePlayers;

	public static ActivityReward getInstance()
	{
		return _instance;
	}

	private ActivityReward()
	{
		if(Config.ACTIVITY_REWARD_ENABLED)
		{
			_rewards = new ArrayList<Integer[]>();
			_activePlayers = new HashMap<Integer, Long>();
			start();
		}
	}

	private void start()
	{
		for (String str : Config.ACTIVITY_REWARD_ITEMS)
		{
			try
			{
				Integer[] item = new Integer[4];
				String[] s = str.split(",");
				for (int i = 0; i < item.length; i++)
					item[i] = Integer.parseInt(s[i]);
				
				_rewards.add(item);
			}
			catch (NumberFormatException nfe)
			{
				_log.warn("NumberFormatException at Config - ActivityRewardItems: " + nfe.getMessage());
				_rewards.add(new Integer[]{57,1,2,100});
			}
		}
		
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			public void run()
			{
				Collections.shuffle(_rewards);
				List<String> rewardedHWIDs = new ArrayList<String>();
				for (Entry<Integer, Long> n3 : _activePlayers.entrySet())
				{
					if (n3.getValue() + 1800000 < System.currentTimeMillis()) // 1800000 = 30mins
						continue;
					
					Player player = World.getPlayer(n3.getKey());
					if (player.hasHWID() && rewardedHWIDs.contains(player.getHWID()))
						continue;
					
					for (Integer[] reward : _rewards)
					{
						if (!Rnd.chance(reward[3])) // 3 = chance
							continue;
						
						ItemFunctions.addItem(player, reward[0], Rnd.get(reward[1],reward[2]), true); // 0 = ItemId, 1 = minCount, 2 = maxCount
						player.sendChatMessage(0, ChatType.TELL.ordinal(), "Event", "You have been rewarded for being active on the server.");
						
						if (player.hasHWID())
							rewardedHWIDs.add(player.getHWID());
					}
				}
				_activePlayers.clear();
			}
		}, Config.ACTIVITY_REWARD_TIME * 60000, Config.ACTIVITY_REWARD_TIME * 60000);
		
		CharListenerList.addGlobal(this);
	}

	@Override
	public void onDeath(Creature actor, Creature killer)
	{
		if (Config.ACTIVITY_REWARD_ENABLED)
		{
			if (killer != null)
				_activePlayers.put(killer.getObjectId(), System.currentTimeMillis());
		}
	}
}
