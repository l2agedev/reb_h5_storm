package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class AdminEnchant implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_seteh, // 6
		admin_setec, // 10
		admin_seteg, // 9
		admin_setel, // 11
		admin_seteb, // 12
		admin_setew, // 7
		admin_setes, // 8
		admin_setle, // 1
		admin_setre, // 2
		admin_setlf, // 4
		admin_setrf, // 5
		admin_seten, // 3
		admin_setun, // 0
		admin_setba,
		admin_setha,
		admin_setdha,
		admin_setlbr,
		admin_setrbr,
		admin_setbelt,
		admin_enchant
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		int armorType = -1;

		switch(command)
		{
			case admin_enchant:
				showMainPage(activeChar);
				return true;
			case admin_seteh:
				armorType = Inventory.PAPERDOLL_HEAD;
				break;
			case admin_setec:
				armorType = Inventory.PAPERDOLL_CHEST;
				break;
			case admin_seteg:
				armorType = Inventory.PAPERDOLL_GLOVES;
				break;
			case admin_seteb:
				armorType = Inventory.PAPERDOLL_FEET;
				break;
			case admin_setel:
				armorType = Inventory.PAPERDOLL_LEGS;
				break;
			case admin_setew:
				armorType = Inventory.PAPERDOLL_RHAND;
				break;
			case admin_setes:
				armorType = Inventory.PAPERDOLL_LHAND;
				break;
			case admin_setle:
				armorType = Inventory.PAPERDOLL_LEAR;
				break;
			case admin_setre:
				armorType = Inventory.PAPERDOLL_REAR;
				break;
			case admin_setlf:
				armorType = Inventory.PAPERDOLL_LFINGER;
				break;
			case admin_setrf:
				armorType = Inventory.PAPERDOLL_RFINGER;
				break;
			case admin_seten:
				armorType = Inventory.PAPERDOLL_NECK;
				break;
			case admin_setun:
				armorType = Inventory.PAPERDOLL_UNDER;
				break;
			case admin_setba:
				armorType = Inventory.PAPERDOLL_BACK;
				break;
			case admin_setha:
				armorType = Inventory.PAPERDOLL_HAIR;
				break;
			case admin_setdha:
				armorType = Inventory.PAPERDOLL_HAIR;
				break;
			case admin_setlbr:
				armorType = Inventory.PAPERDOLL_LBRACELET;
				break;
			case admin_setrbr:
				armorType = Inventory.PAPERDOLL_RBRACELET;
				break;
			case admin_setbelt:
				armorType = Inventory.PAPERDOLL_BELT;
				break;
				
		}

		if(armorType == -1 || wordList.length < 2)
		{
			showMainPage(activeChar);
			return true;
		}

		try
		{
			int ench = Integer.parseInt(wordList[1]);
			if(ench < 0 || ench > 65535)
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminenchant.message1", activeChar));
			else
				setEnchant(activeChar, ench, armorType);
		}
		catch(StringIndexOutOfBoundsException e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminenchant.message2", activeChar));
		}
		catch(NumberFormatException e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminenchant.message3", activeChar));
		}

		// show the enchant menu after an action
		showMainPage(activeChar);
		return true;
	}

	private void setEnchant(Player activeChar, int ench, int armorType)
	{
		// get the target
		GameObject target = activeChar.getTarget();
		if(target == null)
			target = activeChar;
		if(!target.isPlayer())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminenchant.message4", activeChar));
			return;
		}

		Player player = (Player) target;

		// now we need to find the equipped weapon of the targeted character...
		int curEnchant = 0; // display purposes only

		// only attempt to enchant if there is a weapon equipped
		ItemInstance itemInstance = player.getInventory().getPaperdollItem(armorType);

		if(itemInstance != null)
		{
			curEnchant = itemInstance.getEnchantLevel();

			// set enchant value
			player.getInventory().unEquipItem(itemInstance);
			itemInstance.setEnchantLevel(ench);
			player.getInventory().equipItem(itemInstance);

			// send packets
			player.sendPacket(new InventoryUpdate().addModifiedItem(itemInstance));
			player.broadcastCharInfo();

			// informations
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminenchant.message5", activeChar, player.getName(), itemInstance.getName(), curEnchant, ench));
			player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminenchant.message6", player, itemInstance.getName(), curEnchant, ench));
		}
	}

	public void showMainPage(Player activeChar)
	{
		// get the target
		GameObject target = activeChar.getTarget();
		if(target == null)
			target = activeChar;
		Player player = activeChar;
		if(target.isPlayer())
			player = (Player) target;

		String html = HtmCache.getInstance().getNotNull("admin/enchant.htm", player);
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		adminReply.setHtml(html);
		adminReply.replace("%playerName", player.getName());
		activeChar.sendPacket(adminReply);
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}