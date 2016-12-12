package l2r.gameserver.network.clientpackets;

import l2r.gameserver.data.xml.holder.EnchantItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.item.support.EnchantScroll;
import l2r.gameserver.utils.Log;

public class RequestExTryToPutEnchantTargetItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isActionsDisabled() || player.isInStoreMode() || player.isInTrade())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.setEnchantScroll(null);
			return;
		}

		PcInventory inventory = player.getInventory();
		ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		ItemInstance scroll = player.getEnchantScroll();

		if(itemToEnchant == null || scroll == null)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.setEnchantScroll(null);
			return;
		}

		Log.enchant(player.getName() + "|Trying to put enchant|" + itemToEnchant.getItemId() + "|+" + itemToEnchant.getEnchantLevel() + "|" + itemToEnchant.getObjectId());

		int scrollId = scroll.getItemId();
		int itemId = itemToEnchant.getItemId();

		EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scrollId);

		if(!itemToEnchant.canBeEnchanted(enchantScroll == null) || itemToEnchant.isStackable())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			player.setEnchantScroll(null);
			return;
		}

		if(itemToEnchant.getLocation() != ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != ItemInstance.ItemLocation.PAPERDOLL)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			player.setEnchantScroll(null);
			return;
		}

		if(player.isInStoreMode())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.setEnchantScroll(null);
			return;
		}

		if((scroll = inventory.getItemByObjectId(scroll.getObjectId())) == null)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.setEnchantScroll(null);
			return;
		}

		if(enchantScroll == null)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.setEnchantScroll(null);
			return;
		}

		if(enchantScroll.getItems().size() > 0)
		{
			if(!enchantScroll.getItems().contains(itemId))
			{
				player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
				player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
				player.setEnchantScroll(null);
				return;
			}
		}
		else
		{
			if (enchantScroll.getGrade().externalOrdinal != itemToEnchant.getCrystalType().externalOrdinal)
			{
				player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
				player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
				player.setEnchantScroll(null);
				return;
			}
			
			int itemType = itemToEnchant.getTemplate().getType2();
			switch (enchantScroll.getType())
			{
				case ARMOR:
					if (itemType != 0)
						break;
					player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				case WEAPON:
					if ((itemType != 1) && (itemType != 2))
						break;
					player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
			}
		}

		if(enchantScroll.getMaxEnchant() != -1 && itemToEnchant.getEnchantLevel() >= enchantScroll.getMaxEnchant())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			player.setEnchantScroll(null);
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != player.getObjectId())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.setEnchantScroll(null);
			return;
		}

		player.sendPacket(ExPutEnchantTargetItemResult.SUCCESS);
		player.setEnchantCatalyst(0); // When target item is inserted, catalyst slot may be opened. Nullfy, because shitty client caches it from previous enchants.
	}
}
