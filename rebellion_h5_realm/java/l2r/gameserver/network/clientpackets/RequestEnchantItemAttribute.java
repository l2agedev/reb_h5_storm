package l2r.gameserver.network.clientpackets;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.ExAttributeEnchantResult;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;

/**
 * @author SYS
 * Format: d
 */
public class RequestEnchantItemAttribute extends L2GameClientPacket
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
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_objectId == -1)
		{
			activeChar.setEnchantScroll(null);
			activeChar.sendPacket(Msg.ELEMENTAL_POWER_ENCHANCER_USAGE_HAS_BEEN_CANCELLED);
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP, ActionFail.STATIC);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		ItemInstance stone = activeChar.getEnchantScroll();
		int attributeTries = Math.max(1, activeChar.getEnchantScrollValue());
		activeChar.setEnchantScroll(null);

		if(itemToEnchant == null || stone == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemTemplate item = itemToEnchant.getTemplate();

		if(!itemToEnchant.canBeEnchanted(true) || item.getCrystalType().cry < ItemTemplate.CRYSTAL_S)
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		if(itemToEnchant.getLocation() != ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != ItemInstance.ItemLocation.PAPERDOLL)
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		if(itemToEnchant.isStackable() || (stone = inventory.getItemByObjectId(stone.getObjectId())) == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		Element element = ItemFunctions.getEnchantAttributeStoneElement(stone.getItemId(), itemToEnchant.isArmor());

		if(itemToEnchant.isArmor())
		{
			if(itemToEnchant.getAttributeElementValue(Element.getReverseElement(element), false) != 0)
			{
				activeChar.sendPacket(Msg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFail.STATIC);
				return;
			}
		}
		else if(itemToEnchant.isWeapon())
		{
			if(itemToEnchant.getAttributeElement() != Element.NONE && itemToEnchant.getAttributeElement() != element)
			{
				activeChar.sendPacket(Msg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFail.STATIC);
				return;
			}
		}
		else
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		if(item.isUnderwear() || item.isCloak() || item.isBracelet() || item.isBelt() || !item.isAttributable())
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		int maxValue = itemToEnchant.isWeapon() ? Config.MAX_ATTRIBUTE_WEAPON : Config.MAX_ATTRIBUTE_ARMOR;

		if(stone.getTemplate().isAttributeCrystal())
			maxValue += itemToEnchant.isWeapon() ? Config.MAX_ATTRIBUTE_WEAPON : Config.MAX_ATTRIBUTE_ARMOR;

		if(itemToEnchant.getAttributeElementValue(element, false) >= maxValue)
		{
			activeChar.sendPacket(Msg.ELEMENTAL_POWER_ENCHANCER_USAGE_HAS_BEEN_CANCELLED, ActionFail.STATIC);
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
			return;
		}

		for (int i = 0; i < attributeTries; i++)
		{
			// Batch attribute support.
			if (attributeTries > 1 && itemToEnchant.getAttributeElementValue(element, false) >= maxValue)
				return;
			
			if (!inventory.destroyItem(stone, 1L))
			{
				activeChar.sendActionFailed();
				return;
			}
			
			Log.enchant(activeChar.getName() + "|Trying to attribute enchant|" + itemToEnchant.getItemId() + "|attribute:" + element + "|" + itemToEnchant.getObjectId());
			
			double premiumBonus = PremiumAccountsTable.getAttrEnchBonus(activeChar);
			double chance = (stone.getTemplate().isAttributeCrystal() ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE : Config.ENCHANT_ATTRIBUTE_STONE_CHANCE) * premiumBonus;
			
			Functions.sendDebugMessage(activeChar, "Enchant chance is (" + chance + ") Premium Bonus is: " + premiumBonus);
			
			// Premium system added
			if (Rnd.chance(chance))
			{
				if (itemToEnchant.getEnchantLevel() == 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessage.S2_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1);
					sm.addItemName(itemToEnchant.getItemId());
					sm.addItemName(stone.getItemId());
					activeChar.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessage.S3_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO__S1S2);
					sm.addNumber(itemToEnchant.getEnchantLevel());
					sm.addItemName(itemToEnchant.getItemId());
					sm.addItemName(stone.getItemId());
					activeChar.sendPacket(sm);
				}
				
				Log.enchant(activeChar.getName() + "|Successful to attribute enchant|" + itemToEnchant.getItemId() + "|attribute:" + element + "|" + itemToEnchant.getObjectId());
				
				int value = itemToEnchant.isWeapon() ? Config.ATTRIBUTE_WEAPON : Config.ATTRIBUTE_ARMOR;
				
				// Для оружия 1й камень дает +20 атрибута
				if (itemToEnchant.getAttributeElementValue(element, false) == 0 && itemToEnchant.isWeapon())
					value = Config.ATTRIBUTE_FIRST_WEAPON;
				
				boolean equipped = false;
				if (equipped = itemToEnchant.isEquipped())
				{
					activeChar.getInventory().setIsRefreshed(true);
					activeChar.getInventory().unEquipItem(itemToEnchant);
				}
				
				itemToEnchant.setAttributeElement(element, itemToEnchant.getAttributeElementValue(element, false) + value);
				itemToEnchant.setJdbcState(JdbcEntityState.UPDATED);
				itemToEnchant.update();
				
				if (equipped)
				{
					activeChar.getInventory().equipItem(itemToEnchant);
					activeChar.getInventory().setIsRefreshed(false);
				}
				
				activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnchant));
				activeChar.sendPacket(new ExAttributeEnchantResult(value));
			}
			else
			{
				Log.enchant(activeChar.getName() + "|Failed to attribute enchant|" + itemToEnchant.getItemId() + "|attribute:" + element + "|" + itemToEnchant.getObjectId());
				activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_ADD_ELEMENTAL_POWER);
			}
		}

		activeChar.setEnchantScroll(null);
		activeChar.updateStats();
	}
}