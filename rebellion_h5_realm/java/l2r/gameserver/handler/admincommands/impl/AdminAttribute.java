package l2r.gameserver.handler.admincommands.impl;

//import l2r.extensions.scripts.ScriptFile;
//import l2r.gameserver.handler.AdminCommandHandler;
import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class AdminAttribute implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_setatreh, // 6
		admin_setatrec, // 10
		admin_setatreg, // 9
		admin_setatrel, // 11
		admin_setatreb, // 12
		admin_setatrew, // 7
		admin_setatres, // 8
		admin_setatrle, // 1
		admin_setatrre, // 2
		admin_setatrlf, // 4
		admin_setatrrf, // 5
		admin_setatren, // 3
		admin_setatrun, // 0
		admin_setatrbl, // 24
		admin_attribute
	}
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		int armorType = -1;
		
		switch (command)
		{
			case admin_attribute:
				showMainPage(activeChar);
				return true;
			case admin_setatreh:
				armorType = Inventory.PAPERDOLL_HEAD;
				break;
			case admin_setatrec:
				armorType = Inventory.PAPERDOLL_CHEST;
				break;
			case admin_setatreg:
				armorType = Inventory.PAPERDOLL_GLOVES;
				break;
			case admin_setatreb:
				armorType = Inventory.PAPERDOLL_FEET;
				break;
			case admin_setatrel:
				armorType = Inventory.PAPERDOLL_LEGS;
				break;
			case admin_setatrew:
				armorType = Inventory.PAPERDOLL_RHAND;
				break;
			case admin_setatres:
				armorType = Inventory.PAPERDOLL_LHAND;
				break;
			case admin_setatrle:
				armorType = Inventory.PAPERDOLL_LEAR;
				break;
			case admin_setatrre:
				armorType = Inventory.PAPERDOLL_REAR;
				break;
			case admin_setatrlf:
				armorType = Inventory.PAPERDOLL_LFINGER;
				break;
			case admin_setatrrf:
				armorType = Inventory.PAPERDOLL_RFINGER;
				break;
			case admin_setatren:
				armorType = Inventory.PAPERDOLL_NECK;
				break;
			case admin_setatrun:
				armorType = Inventory.PAPERDOLL_UNDER;
				break;
			case admin_setatrbl:
				armorType = Inventory.PAPERDOLL_BELT;
				break;
		}
		
		GameObject target = activeChar.getTarget();
		if (target == null)
			target = activeChar;
		
		if (!target.isPlayer())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.message4", activeChar));
			return false;
		}
		
		if (armorType == -1 || wordList.length < 2)
		{
			showMainPage(activeChar);
			return true;
		}
		
		try
		{
			
			int ench = Integer.parseInt(wordList[1]);
			byte element = -2;
			
			if (wordList[2].equals("Fire"))
				element = 0;
			if (wordList[2].equals("Water"))
				element = 1;
			if (wordList[2].equals("Wind"))
				element = 2;
			if (wordList[2].equals("Earth"))
				element = 3;
			if (wordList[2].equals("Holy"))
				element = 4;
			if (wordList[2].equals("Dark"))
				element = 5;
			
			if (ench < 0 || ench > 600)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.message1", activeChar));
			}
			else
				setEnchant((Player) target, ench, element, armorType);
		}
		catch (StringIndexOutOfBoundsException e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.message2", activeChar));
		}
		catch (NumberFormatException e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.message3", activeChar));
		}
		
		// show the enchant menu after an action
		showMainPage(activeChar);
		return true;
	}
	
	private void setEnchant(Player target, int value, byte element, int armorType)
	{
		if (target == null)
			return;
		
		Element El = Element.NONE;
		switch (element)
		{
			case 0:
				El = Element.FIRE;
				break;
			case 1:
				El = Element.WATER;
				break;
			case 2:
				El = Element.WIND;
				break;
			case 3:
				El = Element.EARTH;
				break;
			case 4:
				El = Element.HOLY;
				break;
			case 5:
				El = Element.UNHOLY;
				break;
		}
		
		Player player = (Player) target;
		
		int curEnchant = 0;
		
		ItemInstance item = player.getInventory().getPaperdollItem(armorType);
		if (item != null)
		{
			curEnchant = item.getEnchantLevel();
			if (item.isWeapon())
			{
				target.getInventory().unEquipItem(item);
				item.setAttributeElement(El, value);
				item.setJdbcState(JdbcEntityState.UPDATED);
				item.update();
				
				target.getInventory().equipItem(item);
				target.sendPacket(new InventoryUpdate().addModifiedItem(item));
				target.broadcastUserInfo(true);
			}
			
			if (item.isArmor())
			{
				if (item.getAttributeElementValue(Element.getReverseElement(El), false) != 0)
				{
					target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.message5", target));
					return;
				}
				
				target.getInventory().unEquipItem(item);
				item.setAttributeElement(El, value);
				target.getInventory().equipItem(item);
				target.sendPacket(new InventoryUpdate().addModifiedItem(item));
				target.broadcastUserInfo(true);
			}
			
			String ElementName = "";
			switch (element)
			{
				case 0:
					ElementName = "" + new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.fire", player);
					break;
				case 1:
					ElementName = "" + new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.water", player);
					break;
				case 2:
					ElementName = "" + new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.wind", player);
					break;
				case 3:
					ElementName = "" + new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.earth", player);
					break;
				case 4:
					ElementName = "" + new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.holy", player);
					break;
				case 5:
					ElementName = "" + new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.dark", player);
					break;
			}
			target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.message6", target, ElementName, value, item.getName(), curEnchant));
			player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminattribute.message7", player, ElementName, value, item.getName(), curEnchant));
		}
	}
	
	private void showMainPage(Player activeChar)
	{
		// get the target
		GameObject target = activeChar.getTarget();
		if (target == null)
			target = activeChar;
		Player player = activeChar;
		if (target.isPlayer())
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