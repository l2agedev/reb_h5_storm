package l2r.gameserver.model.reward;

import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.utils.ItemFunctions;

public class RewardItemResult
{
	public final int itemId;
	public long count;
	public boolean isAdena;

	public RewardItemResult(int itemId)
	{
		this.itemId = itemId;
		count = 1;
	}
	
	public ItemInstance createItem()
	{
		ItemInstance item = ItemFunctions.createItem(itemId);
		if (item != null)
		{
			item.setCount(count);
			return item;
		}
		
		return null;
	}
}
