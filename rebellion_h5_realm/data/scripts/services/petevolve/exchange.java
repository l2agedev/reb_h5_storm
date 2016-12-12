package services.petevolve;

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.instances.PetInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.PetDataTable;
import l2r.gameserver.tables.PetDataTable.Pet;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.Util;

public class exchange extends Functions
{
	/** Билеты для обмена **/
	private static final int PEticketB = 7583;
	private static final int PEticketC = 7584;
	private static final int PEticketK = 7585;

	/** Дудки для вызова петов **/
	private static final int BbuffaloP = 6648;
	private static final int BcougarC = 6649;
	private static final int BkookaburraO = 6650;

	public void exch_1()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(getItemCount(player, PEticketB) >= 1)
		{
			removeItem(player, PEticketB, 1);
			addItem(player, BbuffaloP, 1);
			return;
		}
		show("scripts/services/petevolve/exchange_no.htm", player);
	}

	public void exch_2()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(getItemCount(player, PEticketC) >= 1)
		{
			removeItem(player, PEticketC, 1);
			addItem(player, BcougarC, 1);
			return;
		}
		show("scripts/services/petevolve/exchange_no.htm", player);
	}

	public void exch_3()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(getItemCount(player, PEticketK) >= 1)
		{
			removeItem(player, PEticketK, 1);
			addItem(player, BkookaburraO, 1);
			return;
		}
		show("scripts/services/petevolve/exchange_no.htm", player);
	}

	public void showBabyPetExchange()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.SERVICES_EXCHANGE_BABY_PET_ITEM);
		String out_ru = "";
		out_ru += "<html><body>Вы можете в любое время обменять вашего Improved Baby пета на другой вид, без потери опыта. Пет при этом должен быть вызван.";
		out_ru += "<br>Стоимость обмена: " + Util.formatAdena(Config.SERVICES_EXCHANGE_BABY_PET_PRICE) + " " + item.getName();
		out_ru += "<br><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToCougar\" value=\"Обменять на Improved Cougar\">";
		out_ru += "<br1><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToBuffalo\" value=\"Обменять на Improved Buffalo\">";
		out_ru += "<br1><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToKookaburra\" value=\"Обменять на Improved Kookaburra\">";
		out_ru += "</body></html>";
		
		String out_en = "";
		out_en += "<html><body>You can at any time exchange your Improved Baby pet to a different type without losing experience. Pet should be spawned.";
		out_en += "<br>Cost: " + Util.formatAdena(Config.SERVICES_EXCHANGE_BABY_PET_PRICE) + " " + item.getName();
		out_en += "<br><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToCougar\" value=\"Exchange for Improved Cougar\">";
		out_en += "<br1><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToBuffalo\" value=\"Exchange for Improved Buffalo\">";
		out_en += "<br1><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToKookaburra\" value=\"Exchange for Improved Kookaburra\">";
		out_en += "</body></html>";
		
		show(player.isLangRus() ? out_ru : out_en, player);
	}

	public void showErasePetName()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_PET_NAME_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_PET_NAME_ITEM);
		String out_ru = "";
		out_ru += "<html><body>Вы можете обнулить имя у пета, для того чтобы назначить новое. Пет при этом должен быть вызван.";
		out_ru += "<br>Стоимость обнуления: " + Util.formatAdena(Config.SERVICES_CHANGE_PET_NAME_PRICE) + " " + item.getName();
		out_ru += "<br><button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:erasePetName\" value=\"Обнулить имя\">";
		out_ru += "</body></html>";
		
		String out_en = "";
		out_en += "<html><body>You can clear the name of a pet, in order to appoint a new one. Pet should be spawned.";
		out_en += "<br>Cost: " + Util.formatAdena(Config.SERVICES_CHANGE_PET_NAME_PRICE) + " " + item.getName();
		out_en += "<br><button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:erasePetName\" value=\"Clear pet name\">";
		out_en += "</body></html>";
		
		show(player.isLangRus() ? out_ru : out_en, player);
	}

	public void erasePetName()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_PET_NAME_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		Summon pl_pet = player.getPet();
		if(pl_pet == null || !pl_pet.isPet())
		{
			show(player.isLangRus() ? "Питомец должен быть вызван." : "The pet should be spawned.", player);
			return;
		}
		if(player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_PET_NAME_ITEM, Config.SERVICES_CHANGE_PET_NAME_PRICE))
		{
			pl_pet.setName(pl_pet.getTemplate().name);
			pl_pet.broadcastCharInfo();

			PetInstance _pet = (PetInstance) pl_pet;
			ItemInstance control = _pet.getControlItem();
			if(control != null)
			{
				control.setCustomType2(1);
				control.setJdbcState(JdbcEntityState.UPDATED);
				control.update();
				player.sendPacket(new InventoryUpdate().addModifiedItem(control));
			}
			show(player.isLangRus() ? "Имя стерто." : "Name erased.", player);
		}
		else if(Config.SERVICES_CHANGE_PET_NAME_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	public void exToCougar()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		Summon pl_pet = player.getPet();
		if(pl_pet == null || pl_pet.isDead() || !(pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID || pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID))
		{
			show(player.isLangRus() ? "Пет должен быть вызван." : "The Pet should be spawned.", player);
			return;
		}
		if(player.getInventory().destroyItemByItemId(Config.SERVICES_EXCHANGE_BABY_PET_ITEM, Config.SERVICES_EXCHANGE_BABY_PET_PRICE))
		{
			ItemInstance control = player.getInventory().getItemByObjectId(player.getPet().getControlItemObjId());
			control.setItemId(Pet.IMPROVED_BABY_COUGAR.getControlItemId());
			control.setJdbcState(JdbcEntityState.UPDATED);
			control.update();
			player.sendPacket(new InventoryUpdate().addModifiedItem(control));
			player.getPet().unSummon();
			show(player.isLangRus() ? "Пет изменен." : "Pet changed.", player);
		}
		else if(Config.SERVICES_EXCHANGE_BABY_PET_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	public void exToBuffalo()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		Summon pl_pet = player.getPet();
		if(pl_pet == null || pl_pet.isDead() || !(pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_COUGAR_ID || pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID))
		{
			show(player.isLangRus() ? "Пет должен быть вызван." : "The pet should be spawned.", player);
			return;
		}
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && player.isMageClass())
		{
			show(player.isLangRus() ? "Этот пет только для воинов." : "This pet is only for fighters.", player);
			return;
		}
		if(player.getInventory().destroyItemByItemId(Config.SERVICES_EXCHANGE_BABY_PET_ITEM, Config.SERVICES_EXCHANGE_BABY_PET_PRICE))
		{
			ItemInstance control = player.getInventory().getItemByObjectId(player.getPet().getControlItemObjId());
			control.setItemId(Pet.IMPROVED_BABY_BUFFALO.getControlItemId());
			control.setJdbcState(JdbcEntityState.UPDATED);
			control.update();
			player.sendPacket(new InventoryUpdate().addModifiedItem(control));
			player.getPet().unSummon();
			show(player.isLangRus() ? "Пет изменен." : "Pet changed.", player);
		}
		else if(Config.SERVICES_EXCHANGE_BABY_PET_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	public void exToKookaburra()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		Summon pl_pet = player.getPet();
		if(pl_pet == null || pl_pet.isDead() || !(pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID || pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_COUGAR_ID))
		{
			show(player.isLangRus() ? "Пет должен быть вызван." : "The pet should be spawned.", player);
			return;
		}
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && !player.isMageClass())
		{
			show(player.isLangRus() ? "Этот пет только для магов." : "This pet is only for mages.", player);
			return;
		}
		if(player.getInventory().destroyItemByItemId(Config.SERVICES_EXCHANGE_BABY_PET_ITEM, Config.SERVICES_EXCHANGE_BABY_PET_PRICE))
		{
			ItemInstance control = player.getInventory().getItemByObjectId(player.getPet().getControlItemObjId());
			control.setItemId(Pet.IMPROVED_BABY_KOOKABURRA.getControlItemId());
			control.setJdbcState(JdbcEntityState.UPDATED);
			control.update();
			player.sendPacket(new InventoryUpdate().addModifiedItem(control));
			player.getPet().unSummon();
			show(player.isLangRus() ? "Пет изменен." : "Pet changed.", player);
		}
		else if(Config.SERVICES_EXCHANGE_BABY_PET_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	public static String DialogAppend_30731(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30827(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30828(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30829(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30830(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30831(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30869(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31067(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31265(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31309(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31954(Integer val)
	{
		return getHtmlAppends(val);
	}

	private static String getHtmlAppends(Integer val)
	{
		String ret = "";
		if(val != 0)
			return ret;
		if(Config.SERVICES_CHANGE_PET_NAME_ENABLED)
			ret = "<br>[scripts_services.petevolve.exchange:showErasePetName|Обнулить имя у пета]";
		if(Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
			ret += "<br>[scripts_services.petevolve.exchange:showBabyPetExchange|Обменять Improved Baby пета]";
		return ret;
	}
}