package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.templates.item.ItemTemplate;

public class RequestUnEquipItem extends L2GameClientPacket
{
	private int _slot;

	/**
	 * packet type id 0x16
	 * format:		cd
	 */
	@Override
	protected void readImpl()
	{
		_slot = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if (!activeChar.canOverrideCond(PcCondOverride.ITEM_USE_CONDITIONS))
		{
			if(activeChar.isActionsDisabled())
			{
				activeChar.sendActionFailed();
				return;
			}

			if (Config.SECURITY_ENABLED && Config.SECURITY_ITEM_UNEQUIP && activeChar.getSecurity())
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
			}
			
			// You cannot do anything else while fishing
			if(activeChar.isFishing())
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
				return;
			}

			// Нельзя снимать проклятое оружие и флаги
			if((_slot == ItemTemplate.SLOT_R_HAND || _slot == ItemTemplate.SLOT_L_HAND || _slot == ItemTemplate.SLOT_LR_HAND) && (activeChar.isCursedWeaponEquipped() || activeChar.getActiveWeaponFlagAttachment() != null))
				return;
			
			if(_slot == ItemTemplate.SLOT_R_HAND)
			{
				ItemInstance weapon = activeChar.getActiveWeaponInstance();
				if(weapon == null)
					return;
				
				if (NexusEvents.isInEvent(activeChar))
				{
					if (!NexusEvents.canUseItem(activeChar, weapon))
					{
						activeChar.sendMessage("Cannot use this item.");
						return;
					}
				}
				
				activeChar.abortAttack(true, true);
				activeChar.abortCast(true, true);
				activeChar.sendDisarmMessage(weapon);
			}
		}

		activeChar.getInventory().unEquipItemInBodySlot(_slot);
	}
}