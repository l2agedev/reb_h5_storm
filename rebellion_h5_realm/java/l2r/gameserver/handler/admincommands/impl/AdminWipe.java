package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.tables.PetDataTable;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.Util;

public class AdminWipe implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_wipe,
		admin_wipewh,
		admin_clearinventory
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_wipe:
			{
				if (wordList.length <= 1 || wordList.length >= 5)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminwipe.message1", activeChar));
					return false;
				}
				
				
				try
				{
					int itemId = -1;
					long itemCount = -1;
					Player target = null;
					
					try
					{
						if (wordList.length == 2)
						{
							target = activeChar.getTarget().getPlayer();
							itemId = Integer.valueOf(wordList[1]);
						}
						else if (wordList.length == 3)
						{
							if (Util.isDigit(wordList[1]))
							{
								target = activeChar.getTarget().getPlayer();
								itemId = Integer.valueOf(wordList[1]);
								itemCount = Integer.valueOf(wordList[2]);
							}
							else
							{
								target = World.getPlayer(wordList[1]);
								itemId = Integer.valueOf(wordList[2]);
							}
						}
						else if (wordList.length == 4)
						{
							target = World.getPlayer(wordList[1]);
							itemId = Integer.valueOf(wordList[2]);
							itemCount = Integer.valueOf(wordList[3]);
						}
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Usage: //wipe itemId; //wipe itemId count; //wipe playerName itemId; //wipe playerName itemId count. If player's name is only numbers, use the last example, because command treats name as ItemId. Error: " + e.getMessage()); 
						return false;
					}
					
					if (target == null)
					{
						activeChar.sendMessage("Target not found.");
						return false;
					}
					
					target.getInventory().destroyItemByItemId(itemId, (itemCount < 0 ? target.getInventory().getCountOf(itemId) : itemCount));
					ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
					if (item != null)
					{
						target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminwipe.message3", target, activeChar.getName(), itemCount, item.getName()));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminwipe.message4", activeChar, itemCount, item.getName(), target.getName()));
					}
					
					return true;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_wipewh:
			{
				if (wordList.length <= 1 || wordList.length >= 5)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminwipe.message1", activeChar));
					return false;
				}
				
				
				try
				{
					int itemId = -1;
					long itemCount = -1;
					Player target = null;
					
					try
					{
						if (wordList.length == 2)
						{
							target = activeChar.getTarget().getPlayer();
							itemId = Integer.valueOf(wordList[1]);
						}
						else if (wordList.length == 3)
						{
							if (Util.isDigit(wordList[1]))
							{
								target = activeChar.getTarget().getPlayer();
								itemId = Integer.valueOf(wordList[1]);
								itemCount = Integer.valueOf(wordList[2]);
							}
							else
							{
								target = World.getPlayer(wordList[1]);
								itemId = Integer.valueOf(wordList[2]);
							}
						}
						else if (wordList.length == 4)
						{
							target = World.getPlayer(wordList[1]);
							itemId = Integer.valueOf(wordList[2]);
							itemCount = Integer.valueOf(wordList[3]);
						}
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Usage: //wipewh itemId; //wipe itemId count; //wipewh playerName itemId; //wipewh playerName itemId count. If player's name is only numbers, use the last example, because command treats name as ItemId. Error: " + e.getMessage()); 
						return false;
					}
					
					if (target == null)
					{
						activeChar.sendMessage("Target not found.");
						return false;
					}
					
					if (itemCount < 0)
						itemCount = target.getInventory().getCountOf(itemId);
					target.getWarehouse().destroyItemByItemId(itemId, itemCount);
					ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
					if (item != null)
					{
						target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminwipe.message3", target, activeChar.getName(), itemCount, item.getName()));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminwipe.message4", activeChar, itemCount, item.getName(), target.getName()));
					}
					
					return true;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_clearinventory:
			{
				//int[] itemObjIds = activeChar.getAllShortCuts().stream().filter(sc -> sc.getType() == ShortCut.TYPE_ITEM).mapToInt(sc -> sc.getId()).toArray();
				for (ItemInstance item : activeChar.getInventory().getItems())
				{
					if (item.isEquipped() || item.getItemId() == 57 || item.getTemplate().isQuest() || PetDataTable.isPetControlItem(item) /*|| Util.contains(itemObjIds, item.getObjectId())*/)
						continue;
					
					activeChar.getInventory().destroyItem(item);
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(item);
					activeChar.sendPacket(iu);
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminwipe.message6", activeChar));
				break;
			}
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
