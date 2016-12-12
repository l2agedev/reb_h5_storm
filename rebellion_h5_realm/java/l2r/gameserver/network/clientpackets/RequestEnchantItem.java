package l2r.gameserver.network.clientpackets;


import l2r.commons.dao.JdbcEntityState;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.xml.holder.EnchantItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.network.serverpackets.EnchantResult;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.item.support.EnchantScroll;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestEnchantItem extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestEnchantItem.class);
	private int _objectId, _catalystObjId;

	private static final int SUCCESS_VISUAL_EFF_ID = 5965;
	private static final int FAIL_VISUAL_EFF_ID = 5949;
	  
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_catalystObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isActionsDisabled())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if (Config.SECURITY_ENABLED && Config.SECURITY_ENCHANT_ITEM_ENABLED && player.getSecurity())
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (player.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			player.setEnchantScroll(null);
			player.sendPacket(EnchantResult.CANCEL);
			player.sendActionFailed();
			return;
		}
		
		if(player.isInTrade())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if(player.isInStoreMode())
		{
			player.setEnchantScroll(null);
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.sendActionFailed();
			return;
		}

		PcInventory inventory = player.getInventory();
		inventory.writeLock();
		try
		{
			ItemInstance item = inventory.getItemByObjectId(_objectId);
			ItemInstance catalyst = _catalystObjId > 0 ? inventory.getItemByObjectId(_catalystObjId) : null;
			ItemInstance scroll = player.getEnchantScroll();

			if(item == null || scroll == null)
			{
				player.sendActionFailed();
				return;
			}

			// Затычка, ибо клиент криво обрабатывает RequestExTryToPutEnchantSupportItem
			if(!ItemFunctions.checkCatalyst(item, catalyst))
				catalyst = null;
			else if (catalyst != null && player.getEnchantCatalyst() != catalyst.getItemId())
				catalyst = null;
			
			EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
			if(enchantScroll == null)
			{
				_log.warn("Missing enchant scroll for item: " + scroll.getName() + "(" + scroll.getItemId() + ")");
				return;
			}

			Log.enchant(player.getName() + "|Trying to enchant|" + item.getItemId() + "|+" + item.getEnchantLevel() + "|" + item.getObjectId());
			
			if(enchantScroll.getMaxEnchant() != -1 && item.getEnchantLevel() >= enchantScroll.getMaxEnchant())
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}

			if(enchantScroll.getItems().size() > 0)
			{
				if(!enchantScroll.getItems().contains(item.getItemId()))
				{
					player.sendPacket(EnchantResult.CANCEL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				}
			}
			else
			{
				if (enchantScroll.getGrade().externalOrdinal != item.getCrystalType().externalOrdinal)
				{
					player.sendPacket(EnchantResult.CANCEL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				}
				int itemType = item.getTemplate().getType2();
				switch (enchantScroll.getType())
				{
					case ARMOR:
						if (itemType != 0)
							break;
						player.sendPacket(EnchantResult.CANCEL);
						player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
						player.sendActionFailed();
						return;
					case WEAPON:
						if ((itemType != 1) && (itemType != 2))
							break;
						player.sendPacket(EnchantResult.CANCEL);
						player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
						player.sendActionFailed();
						return;
				}
			}

			if(!item.canBeEnchanted(false))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}

			if(!inventory.destroyItem(scroll, 1L) || catalyst != null && !inventory.destroyItem(catalyst, 1L))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendActionFailed();
				return;
			}

			boolean equipped = false;
			if(equipped = item.isEquipped())
			{
				inventory.setIsRefreshed(true);
				inventory.unEquipItem(item);
			}

			int safeEnchantLevel = item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? enchantScroll.getSafeLevelFullArmor() : enchantScroll.getSafeLevel();

			int chance = enchantScroll.getChance(item.getTemplate().isMagicWeapon());
			if (catalyst != null)
		        chance += ItemFunctions.getCatalystPower(catalyst.getItemId());
			
			// Premium System
			chance *= PremiumAccountsTable.getEnchantBonus(player);
						
			if(item.getEnchantLevel() < safeEnchantLevel)
				chance = 100;
			
			Functions.sendDebugMessage(player, "Enchant chance is " + chance);
			
			if(Rnd.chance(chance))
			{
				if (Config.ENABLE_PLAYER_COUNTERS)
				{
					boolean isBlessedScroll = ItemFunctions.isBlessedEnchantScroll(enchantScroll.getItemId());
					boolean isCrystalScroll = ItemFunctions.isCrystallEnchantScroll(enchantScroll.getItemId());
					
					if (Config.ENABLE_ACHIEVEMENTS)
					{
						if (item.getEnchantLevel() == 2 && player.getCounters().getPoints("_Enchant_Item") < 3)
							player.getCounters().setPoints("_Enchant_Item", 3);
						else if (item.getEnchantLevel() == 7 && player.getCounters().getPoints("_Enchant_Item") < 8)
							player.getCounters().setPoints("_Enchant_Item", 8);
						else if (item.getEnchantLevel() == 11 && player.getCounters().getPoints("_Enchant_Item") < 12)
							player.getCounters().setPoints("_Enchant_Item", 12);
						else if (item.getEnchantLevel() == 15 && player.getCounters().getPoints("_Enchant_Item") < 15)
							player.getCounters().setPoints("_Enchant_Item", 16);
					}
					
					// success
					if (isBlessedScroll)
						player.getCounters().addPoint("_Enchanted_Blessed_Scroll");
					else if (!isBlessedScroll && !isCrystalScroll)
						player.getCounters().addPoint("_Enchanted_Normal_Scroll");
				}
				
				item.setEnchantLevel(item.getEnchantLevel() + 1);
				item.setJdbcState(JdbcEntityState.UPDATED);
				item.update();
				showEnchantAnimation(player, item.getEnchantLevel());

				if (equipped)
				{
					inventory.equipItem(item);
					inventory.setIsRefreshed(false);
				}

				player.sendPacket(new InventoryUpdate().addModifiedItem(item));

				player.sendPacket(EnchantResult.SUCESS);

				Log.enchant(player.getName() + "|Succesfully Enchanted|" + item.getItemId() + "|+" + item.getEnchantLevel() + "|" + item.getObjectId() + "|" + enchantScroll.getItemId() + "|" + chance);
				Log.LogItem(player, Log.EnchantSuccess, item);
				
				if(enchantScroll.isHasVisualEffect() && item.getEnchantLevel() > 3)
					player.broadcastPacket(new MagicSkillUse(player, player, SUCCESS_VISUAL_EFF_ID, 1, 500, 1500));
			}
			else
			{
				Log.enchant(player.getName() + "|Failed to enchant|" + item.getItemId() + "|+" + item.getEnchantLevel() + "|" + chance);
				
				switch(enchantScroll.getResultType())
				{
					case CRYSTALS:
						if(item.isEquipped())
						player.sendDisarmMessage(item);

						Log.LogItem(player, Log.EnchantFail, item);

						if(!inventory.destroyItem(item, 1L))
						{
							player.sendActionFailed();
							return;
						}

						int crystalId = item.getCrystalType().cry;
						if(crystalId > 0 && item.getTemplate().getCrystalCount() > 0 && item.getCustomFlags() <= 0)
						{
							int crystalAmount = (int) (item.getTemplate().getCrystalCount() * 0.87);
							if(item.getEnchantLevel() > 3)
								crystalAmount += item.getTemplate().getCrystalCount() * 0.25 * (item.getEnchantLevel() - 3);
							if(crystalAmount < 1)
								crystalAmount = 1;

							player.sendPacket(new EnchantResult(1, crystalId, crystalAmount));
							ItemFunctions.addItem(player, crystalId, crystalAmount, true);
						}
						else
							player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);
						
						if(enchantScroll.isHasVisualEffect())
							player.broadcastPacket(new MagicSkillUse(player, player, FAIL_VISUAL_EFF_ID, 1, 500, 1500));
						break;
					case DROP_ENCHANT:
						item.setEnchantLevel(0);
						item.setJdbcState(JdbcEntityState.UPDATED);
						item.update();

						if (equipped)
						{
							inventory.equipItem(item);
							inventory.setIsRefreshed(false);
						}
						player.sendPacket(new InventoryUpdate().addModifiedItem(item));
						player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
						player.sendPacket(EnchantResult.BLESSED_FAILED);
						break;
					case NOTHING:
						player.sendPacket(EnchantResult.ANCIENT_FAILED);
						break;
				}
			}
		}
		finally
		{
			inventory.writeUnlock();

			player.setEnchantScroll(null);
			player.setEnchantCatalyst(0);
			player.updateStats();
		}
	}
	
	/**
	 * 
	 * @param player
	 * @param enchantLevel : 0 = fail
	 */
	private static void showEnchantAnimation(Player player, int enchantLevel)
	{
		if (player.getVarB("EnchantAnimationDisable"))
			return;
		
		enchantLevel = Math.min(enchantLevel, 20);
		int skillId = 23096 + enchantLevel;
		player.broadcastPacketToOthers(new MagicSkillUse(player, player, skillId, 1, 0, 500));
	}
}