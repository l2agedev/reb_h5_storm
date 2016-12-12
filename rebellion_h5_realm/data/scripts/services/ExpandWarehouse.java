package services;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

public class ExpandWarehouse extends Functions
{
	public void get()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_EXPAND_WAREHOUSE_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		if(player.getWarehouseLimit() >= Config.SERVICES_EXPAND_WAREHOUSE_MAX)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You have maximum slots thats allowed!");
			return;
		}

		if(getItemCount(player, Config.SERVICES_EXPAND_WAREHOUSE_ITEM) < Config.SERVICES_EXPAND_WAREHOUSE_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}
		
		player.setExpandInventory(player.getExpandWarehouse() + 1);
		player.setVar("ExpandWarehouse", String.valueOf(player.getExpandWarehouse()), -1);
		player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Warehouse capacity is now " + player.getWarehouseLimit());
		removeItem(player, Config.SERVICES_EXPAND_WAREHOUSE_ITEM, Config.SERVICES_EXPAND_WAREHOUSE_PRICE);
		Log.addDonation("Character " + player + " expand warehouse slot by service.", "expandwarehouse");
		
		show();
	}

	public void show()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_EXPAND_WAREHOUSE_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}

		String expandwarehouse = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/expandwarehouse.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_EXPAND_WAREHOUSE_ITEM).getName();
		String cost = Util.formatAdena(Config.SERVICES_EXPAND_WAREHOUSE_PRICE);
		
		expandwarehouse = expandwarehouse.replace("%item%", itemName);
		expandwarehouse = expandwarehouse.replace("%slotprice%", cost);
		expandwarehouse = expandwarehouse.replace("%playerName%", player.getName());
		expandwarehouse = expandwarehouse.replace("%currentsize%", "" + player.getWarehouseLimit());
		expandwarehouse = expandwarehouse.replace("%maxsize%", "" + Config.SERVICES_EXPAND_WAREHOUSE_MAX);

		show(expandwarehouse, player);
	}
}