package handler.items;

import java.util.Collection;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.FishDataHolder;
import l2r.gameserver.handler.items.ItemHandler;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.reward.RewardItem;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Util;

public class FishItem extends ScriptItemHandler implements ScriptFile
{
	@Override
	public boolean pickupItem(Playable playable, ItemInstance item)
	{
		return true;
	}

	@Override
	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		if(player.getWeightPenalty() >= 3 || player.getUsedInventoryPercents() >= 90)
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return false;
		}
		
		if(!player.getInventory().destroyItem(item, 1L))
		{
			player.sendActionFailed();
			return false;
		}

		int count = 0;
		Collection<RewardItem> rewards = FishDataHolder.getInstance().getFishReward(item.getItemId());
		for(RewardItem d : rewards)
		{
			long roll = Util.rollDrop(d.getMinDrop(), d.getMaxDrop(), d.getChance() * Config.RATE_FISH_DROP_COUNT * Config.RATE_DROP_ITEMS * player.getRateItems() * 10000L, false);
			if(roll > 0)
			{
				ItemFunctions.addItem(player, d.getItemId(), Rnd.get(d.getMinDrop(), d.getMaxDrop()), true);
				count++;
			}
		}
		if(count == 0)
			player.sendPacket(SystemMsg.THERE_WAS_NOTHING_FOUND_INSIDE);
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return FishDataHolder.getInstance().getFishIds();
	}
}