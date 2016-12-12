package l2r.gameserver.network.clientpackets;

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemAttributes;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.ExBaseAttributeCancelResult;
import l2r.gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.Log;

/**
 * @author SYS
 */
public class RequestExRemoveItemAttribute extends L2GameClientPacket
{
	// Format: chd
	private int _objectId;
	private int _attributeId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_attributeId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled() || activeChar.isInStoreMode() || activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		if (Config.SECURITY_ENABLED && Config.SECURITY_ITEM_ATTRIBUTE_REMOVE_ENABLED && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		PcInventory inventory = activeChar.getInventory();
		ItemInstance itemToUnnchant = inventory.getItemByObjectId(_objectId);

		if(itemToUnnchant == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemAttributes set = itemToUnnchant.getAttributes();
		Element element = Element.getElementById(_attributeId);

		if(element == Element.NONE || set.getValue(element) <= 0)
		{
			activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, element), ActionFail.STATIC);
			return;
		}

		// проверка делается клиентом, если зашло в эту проверку знач чит
		if(!activeChar.reduceAdena(ExShowBaseAttributeCancelWindow.getAttributeRemovePrice(itemToUnnchant), true))
		{
			activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, element), SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, ActionFail.STATIC);
			return;
		}

		boolean equipped = false;
		if(equipped = itemToUnnchant.isEquipped())
			activeChar.getInventory().unEquipItem(itemToUnnchant);

		itemToUnnchant.setAttributeElement(element, 0);
		itemToUnnchant.setJdbcState(JdbcEntityState.UPDATED);
		itemToUnnchant.update();

		if(equipped)
			activeChar.getInventory().equipItem(itemToUnnchant);

		activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToUnnchant));
		activeChar.sendPacket(new ExBaseAttributeCancelResult(true, itemToUnnchant, element));

		activeChar.updateStats();
		
		Log.enchant(activeChar.getName() + "|Successfully unenchanted attribute|" + itemToUnnchant.getItemId());
		Log.LogItem(activeChar, Log.AttributeRemove, itemToUnnchant);
	}
}