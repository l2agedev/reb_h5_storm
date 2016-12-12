package l2r.gameserver.network.clientpackets;

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExVariationCancelResult;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.ShortCutRegister;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.item.ItemTemplate;

public final class RequestRefineCancel extends L2GameClientPacket
{
	private int _targetItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (Config.SECURITY_ENABLED && Config.SECURITY_ITEM_REMOVE_AUGUMENT_ENABLED && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(ExVariationCancelResult.CLOSE);
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(ExVariationCancelResult.CLOSE);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(ExVariationCancelResult.CLOSE);
			return;
		}

		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);

		// cannot remove augmentation from a not augmented item
		if(targetItem == null || !targetItem.isAugmented())
		{
			activeChar.sendPacket(ExVariationCancelResult.FAIL, SystemMsg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			return;
		}

		// get the price
		int price = getRemovalPrice(targetItem.getTemplate());

		if(price < 0)
			activeChar.sendPacket(ExVariationCancelResult.FAIL);

		// try to reduce the players adena
		if(!activeChar.reduceAdena(price, true))
		{
			activeChar.sendPacket(ExVariationCancelResult.FAIL, SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		boolean equipped = false;
		if(equipped = targetItem.isEquipped())
			activeChar.getInventory().unEquipItem(targetItem);

		// remove the augmentation
		targetItem.setAugmentationId(0);
		targetItem.setJdbcState(JdbcEntityState.UPDATED);
		targetItem.update();

		if(equipped)
			activeChar.getInventory().equipItem(targetItem);

		// send inventory update
		InventoryUpdate iu = new InventoryUpdate().addModifiedItem(targetItem);

		// send system message
		SystemMessage2 sm = new SystemMessage2(SystemMsg.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
		sm.addItemName(targetItem.getItemId());
		activeChar.sendPacket(ExVariationCancelResult.SUCCESS, iu, sm);

		for(ShortCut sc : activeChar.getAllShortCuts())
			if(sc.getId() == targetItem.getObjectId() && sc.getType() == ShortCut.TYPE_ITEM)
				activeChar.sendPacket(new ShortCutRegister(activeChar, sc));
		activeChar.sendChanges();
	}

	public static int getRemovalPrice(ItemTemplate item)
	{
		switch(item.getItemGrade().cry)
		{
			case ItemTemplate.CRYSTAL_C:
				if(item.getCrystalCount() < 1720)
					return 95000;
				else if(item.getCrystalCount() < 2452)
					return 150000;
				else
					return 210000;
			case ItemTemplate.CRYSTAL_B:
				if(item.getCrystalCount() < 1746)
					return 240000;
				else
					return 270000;
			case ItemTemplate.CRYSTAL_A:
				if(item.getCrystalCount() < 2160)
					return 330000;
				else if(item.getCrystalCount() < 2824)
					return 390000;
				else
					return 420000;
			case ItemTemplate.CRYSTAL_S:
				if(item.getCrystalCount() == 10394) // Icarus
					return 920000;
				else if(item.getCrystalCount() == 7050) // Dynasty
					return 720000;
				else if(item.getName().contains("Vesper")) // Vesper
					return 920000;
				else
					return 480000;
				// any other item type is not augmentable
			default:
				return -1;
		}
	}
}