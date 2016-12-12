package services;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

public class ExpandInventory extends Functions
{
	public void get()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}

		if(player.getInventoryLimit() >= Config.SERVICES_EXPAND_INVENTORY_MAX)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You have maximum slots thats allowed!");
			return;
		}

		if(getItemCount(player, Config.SERVICES_EXPAND_INVENTORY_ITEM) < Config.SERVICES_EXPAND_INVENTORY_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}
		
		player.setExpandInventory(player.getExpandInventory() + 1);
		player.setVar("ExpandInventory", String.valueOf(player.getExpandInventory()), -1);
		player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Inventory capacity is now " + player.getInventoryLimit());
		removeItem(player, Config.SERVICES_EXPAND_INVENTORY_ITEM, Config.SERVICES_EXPAND_INVENTORY_PRICE);
		Log.addDonation("Character " + player + " expand inventory slot by service.", "expandinventory");

		show();
	}

	public void show()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		String expandinventory = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/expandinventory.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_EXPAND_INVENTORY_ITEM).getName();
		String cost = Util.formatAdena(Config.SERVICES_EXPAND_INVENTORY_PRICE);
		
		expandinventory = expandinventory.replace("%item%", itemName);
		expandinventory = expandinventory.replace("%slotprice%", cost);
		expandinventory = expandinventory.replace("%playerName%", player.getName());
		expandinventory = expandinventory.replace("%currentsize%", "" + player.getInventoryLimit());
		expandinventory = expandinventory.replace("%maxsize%", "" + Config.SERVICES_EXPAND_INVENTORY_MAX);
		
		show(expandinventory, player);
	}
}