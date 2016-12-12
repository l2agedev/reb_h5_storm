package services;

import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.Log;

public class ExpandCWH extends Functions
{
	public void get()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_EXPAND_CWH_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}

		if(player.getClan() == null)
		{
			player.sendMessage(new CustomMessage("scripts.services.expandcwh.needclan", player));
			return;
		}

		if(player.getInventory().destroyItemByItemId(Config.SERVICES_EXPAND_CWH_ITEM, Config.SERVICES_EXPAND_CWH_PRICE))
		{
			player.getClan().setWhBonus(player.getClan().getWhBonus() + 1);
			player.sendMessage(new CustomMessage("scripts.services.expandcwh.extended_capacity", player, (Config.WAREHOUSE_SLOTS_CLAN + player.getClan().getWhBonus())));
			Log.addDonation("Character " + player + " expand clan warehouse slot by service.", "expandcwarehouse");
		}
		else if(Config.SERVICES_EXPAND_CWH_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);

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

		if(player.getClan() == null)
		{
			player.sendMessage(new CustomMessage("scripts.services.expandcwh.needclan", player));
			return;
		}

		ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.SERVICES_EXPAND_CWH_ITEM);

		String out_ru = "";
		out_ru += "<html><body>Расширение кланового склада";
		out_ru += "<br><br><table>";
		out_ru += "<tr><td>Текущий размер:</td><td>" + (Config.WAREHOUSE_SLOTS_CLAN + player.getClan().getWhBonus()) + "</td></tr>";
		out_ru += "<tr><td>Стоимость слота:</td><td>" + Config.SERVICES_EXPAND_CWH_PRICE + " " + item.getName() + "</td></tr>";
		out_ru += "</table><br><br>";
		out_ru += "<button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ExpandCWH:get\" value=\"Расширить\">";
		out_ru += "</body></html>";
		
		String out_en = "";
		out_en += "<html><body>Clan Warehouse extension";
		out_en += "<br><br><table>";
		out_en += "<tr><td>Current Size:</td><td>" + (Config.WAREHOUSE_SLOTS_CLAN + player.getClan().getWhBonus()) + "</td></tr>";
		out_en += "<tr><td>Slot price:</td><td>" + Config.SERVICES_EXPAND_CWH_PRICE + " " + item.getName() + "</td></tr>";
		out_en += "</table><br><br>";
		out_en += "<button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ExpandCWH:get\" value=\"Expand\">";
		out_en += "</body></html>";

		show(player.isLangRus() ? out_ru : out_en, player);
	}
}