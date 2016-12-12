package handler.items;

import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.items.ItemHandler;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExAutoSoulShot;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.scripts.ScriptFile;

import org.apache.commons.lang3.ArrayUtils;

public class AutoPotions extends SimpleItemHandler implements ScriptFile
{
	static int SMALL_CP_POTION = 22410;
	static int GREATER_CP_POTION = 22411;
	static int LASER_HEAL_POTION = 22412;
	static int GREATER_HEAL_POTION = 22413;
	static int LASER_MANA_POTION = 22414;
	static int MANA_POTION = 22415;
	
	private static final int[] ITEM_IDS = new int[] { SMALL_CP_POTION, GREATER_CP_POTION, LASER_HEAL_POTION, GREATER_HEAL_POTION, LASER_MANA_POTION, MANA_POTION };

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

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
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		if (!Config.ENABLE_AUTO_POTIONS)
		{
			player.sendMessage("Automatic potions are currently Disabled!");
			return false;
		}
		
		if (item == null)
			return false;
		
		int itemId = item.getItemId();

		if (!ArrayUtils.contains(ITEM_IDS, itemId))
			return false;
		
		if (player.isDead())
		{
			player.sendMessage("Cannot use it when you are dead...");
			return false;
		}
		
		if(player.isInOlympiadMode())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return false;
		}
		
		if (NexusEvents.isRegistered(player) ||  NexusEvents.isInEvent(player))
		{
			player.sendMessage("Cannot be used while in events.");
			return false;
		}

		if (player.getInventory().getItemByItemId(itemId).getCount() < 0)
			return false;
		
		boolean isAutoPotion = player.getAutoPotion().contains(itemId);
		
		String itemName = ItemHolder.getInstance().getTemplateName(itemId);
		int realId = ItemHolder.getInstance().getTemplate(itemId).getdisplayId();
		if (isAutoPotion)
		{
			player.removeAutoPotion(itemId);
			player.sendPacket(new ExAutoSoulShot(realId, false));
			player.stopAutoPotionTask(itemId);
			player.sendMessage("Automatic " + itemName + " been Disabled!");
			return false;
		}
		
		player.setAutoPotion(itemId);
		player.sendPacket(new ExAutoSoulShot(realId, true));
		player.startAutoPotionTask(itemId);
		
		player.sendMessage("Automatic " + itemName + " has been Enabled.");
		return true;
	}
}
