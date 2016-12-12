package handler.items;

import l2r.gameserver.handler.items.ItemHandler;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.ScriptFile;

public class TeleportBookmark extends SimpleItemHandler implements ScriptFile
{
	private static final int[] ITEM_IDS = new int[] { 13015, 13301 };

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
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		if(player == null || item == null || !player.isPlayer())
			return false;

		if(player.getTeleportBookmarks().getCapacity() >= 30)
		{
			player.sendPacket(SystemMsg.YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT);
			return false;
		}
		else
		{
			player.getInventory().destroyItem(item, 1);
			player.sendPacket(new SystemMessage2(SystemMsg.S1_HAS_DISAPPEARED).addItemName(item.getItemId()));
			player.getTeleportBookmarks().setCapacity(player.getTeleportBookmarks().getCapacity() + 3);
			player.sendPacket(SystemMsg.THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED);
			return true;
		}
	}
}
