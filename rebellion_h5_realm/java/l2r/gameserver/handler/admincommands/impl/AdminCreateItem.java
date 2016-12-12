package l2r.gameserver.handler.admincommands.impl;

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdminCreateItem implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_itemcreate,
		admin_create_item,
		admin_create_item_target,
		admin_create_item_all,
		admin_ci,
		admin_spreaditem,
		admin_create_item_element,
		admin_donate,
		admin_givedonateitem,
		admin_give_item_to_all_except_dualbox,
		admin_give_item_to_all_hwid_unique,
		admin_give_item_to_all_radius_hwid_unique,
		admin_give_item_to_all_radius,
		admin_give_item_to_all_reflection
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_itemcreate:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_ci:
			case admin_create_item:
				try
				{
					if(wordList.length < 2)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message1", activeChar));
						return false;
					}

					int item_id = Integer.parseInt(wordList[1]);
					long item_count = wordList.length < 3 ? 1 : Long.parseLong(wordList[2]);
					createItem(activeChar, item_id, item_count);
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message2", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_create_item_target:
				try
				{
					GameObject target = activeChar.getTarget();
					if(target == null || !(target.isPlayer() || target.isPet()))
					{
						activeChar.sendPacket(SystemMsg.INVALID_TARGET);
						return false;
					}
					if(wordList.length < 2)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message3", activeChar));
						return false;
					}

					int item_id = Integer.parseInt(wordList[1]);
					long item_count = wordList.length < 3 ? 1 : Long.parseLong(wordList[2]);
					createItem((Player)activeChar.getTarget(), item_id, item_count);
					
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message4", activeChar));
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message5", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_create_item_all:
				try
				{
					if(wordList.length < 2)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message6", activeChar));
						return false;
					}
					int item_id = Integer.parseInt(wordList[1]);
					long item_count = wordList.length < 3 ? 1 : Long.parseLong(wordList[2]);
					int count = 0;
					for(Player player : GameObjectsStorage.getAllPlayers())
					{
						if(player != null)
							if(player.isOnline() && !player.isInOfflineMode())
								createItem(player, item_id, item_count);
						count++;
					}
					ItemTemplate tmpl = ItemHolder.getInstance().getTemplate(item_id);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message7", activeChar, tmpl.getName(), item_count, count));
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message8", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_give_item_to_all_except_dualbox:
				try
				{
					if(wordList.length < 2)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message9", activeChar));
						return false;
					}
					int item_id = Integer.parseInt(wordList[1]);
					long item_count = wordList.length < 3 ? 1 : Long.parseLong(wordList[2]);
					int count = 0;
					Collection<Player> pls = getAllExeptBox(activeChar, 0);
					{
						for (Player onlinePlayer : pls)
						{
							if (activeChar != onlinePlayer && onlinePlayer.isOnline() && !onlinePlayer.isInOfflineMode())
							{
								createItem(onlinePlayer, item_id, item_count);
								count++;
							}
						}
					}
					ItemTemplate tmpl = ItemHolder.getInstance().getTemplate(item_id);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message10", activeChar, tmpl.getName(), item_count, count));
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message11", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_give_item_to_all_hwid_unique:
				try
				{
					if(wordList.length < 2)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message12", activeChar));
						return false;
					}
					int item_id = Integer.parseInt(wordList[1]);
					long item_count = wordList.length < 3 ? 1 : Long.parseLong(wordList[2]);
					int count = 0;
					Collection<Player> pls = getAllExeptBox(activeChar, 0);
					{
						for (Player onlinePlayer : pls)
						{
							if (activeChar != onlinePlayer && onlinePlayer.isOnline() && !onlinePlayer.isInOfflineMode())
							{
								createItem(onlinePlayer, item_id, item_count);
								count++;
							}
						}
					}
					ItemTemplate tmpl = ItemHolder.getInstance().getTemplate(item_id);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message13", activeChar, tmpl.getName(), item_count, count));
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message14", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_give_item_to_all_radius_hwid_unique:
				try
				{
					if(wordList.length < 2)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message15", activeChar));
						return false;
					}
					int item_id = Integer.parseInt(wordList[1]);
					long item_count = Long.parseLong(wordList[2]);
					int radius = Integer.parseInt(wordList[3]);
					int count = 0;
					Collection<Player> pls = getAllExeptBox(activeChar, radius);
					{
						for (Player onlinePlayer : pls)
						{
							if (activeChar != onlinePlayer && onlinePlayer.isOnline() && !onlinePlayer.isInOfflineMode())
							{
								createItem(onlinePlayer, item_id, item_count);
								count++;
							}
						}
					}
					ItemTemplate tmpl = ItemHolder.getInstance().getTemplate(item_id);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message16", activeChar, tmpl.getName(), item_count, count, radius));
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message17", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_give_item_to_all_radius:
				try
				{
					if(wordList.length < 2)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message18", activeChar));
						return false;
					}
					int item_id = Integer.parseInt(wordList[1]);
					long item_count = Long.parseLong(wordList[2]);
					int radius = Integer.parseInt(wordList[3]);
					int count = 0;
					Collection<Player> pls = getAllExeptBox(activeChar, radius);
					{
						for (Player onlinePlayer : pls)
						{
							if (activeChar != onlinePlayer && onlinePlayer.isOnline() && !onlinePlayer.isInOfflineMode())
							{
								createItem(onlinePlayer, item_id, item_count);
								count++;
							}
						}
					}
					ItemTemplate tmpl = ItemHolder.getInstance().getTemplate(item_id);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message19", activeChar, tmpl.getName(), item_count, count, radius));
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message20", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_give_item_to_all_reflection:
				try
				{
					if (activeChar.getReflectionId() == ReflectionManager.DEFAULT_ID)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message21", activeChar));
						return false;
					}
					if(wordList.length < 2)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message22", activeChar));
						return false;
					}
					int item_id = Integer.parseInt(wordList[1]);
					long item_count = Long.parseLong(wordList[2]);
					int count = 0;
					Collection<Player> pls = getAllExeptBox(activeChar, 0);
					{
						for (Player onlinePlayer : pls)
						{
							if (activeChar.getReflectionId() == onlinePlayer.getReflectionId() && activeChar != onlinePlayer && onlinePlayer.isOnline() && !onlinePlayer.isInOfflineMode())
							{
								createItem(onlinePlayer, item_id, item_count);
								count++;
							}
						}
					}
					ItemTemplate tmpl = ItemHolder.getInstance().getTemplate(item_id);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message23", activeChar, tmpl.getName(), item_count, count));
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message24", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_spreaditem:
				try
				{
					int id = Integer.parseInt(wordList[1]);
					long count = Long.parseLong(wordList[2]);
					int numItems = Integer.parseInt(wordList[3]);
					int radius = Integer.parseInt(wordList[4]);
					Location loc = activeChar.getLoc();
					for(int i = 0; i < numItems; i++)
					{
						Location rndLoc = loc.coordsRandomize(radius);
						ItemInstance createditem = ItemFunctions.createItem(id);
						createditem.setCount(count);
						createditem.dropMe(activeChar, rndLoc.findPointToStay(100));
					}
					
					ItemTemplate tmpl = ItemHolder.getInstance().getTemplate(id);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message25", activeChar, numItems, tmpl.getName(), count, radius));
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message26", activeChar));
				}
				break;
			case admin_create_item_element:
				try
				{
					if(wordList.length < 4)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message27", activeChar));
						return false;
					}

					int item_id = Integer.parseInt(wordList[1]);
					int elementId = Integer.parseInt(wordList[2]);
					int value = Integer.parseInt(wordList[3]);
					if(elementId > 5 || elementId < 0)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message28", activeChar));
						return false;
					}
					if(value < 1 || value > 300)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message29", activeChar));
						return false;
					}

					ItemInstance item = ItemFunctions.createItem(item_id);
					item.setCount(1);
					Element element = Element.getElementById(elementId);
					item.setAttributeElement(element, item.getAttributeElementValue(element, false) + value);
					item.setJdbcState(JdbcEntityState.UPDATED);
					item.update();
					activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item));
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message30", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			case admin_donate:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/donate.htm"));
				break;
			case admin_givedonateitem:
				try
				{
					String playerName = "";
					Player target = null;
					int targetOID = 0;
					int count = 0;
					
					if (wordList.length < 3)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message31", activeChar));
						return false;
					}
					count = Integer.parseInt(wordList[1]);
					playerName = wordList[2];
					
					target = World.getPlayer(playerName);
					targetOID = CharacterDAO.getInstance().getObjectIdByName(playerName);
					
					if (count <= 0)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message32", activeChar));
						return false;
					}
					else if (targetOID <= 0)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message33", activeChar));
						return false;
					}
					
					Mail letter = new Mail();
					
					// Send a mail to the buyer.
					ItemInstance item = ItemFunctions.createItem(13693); // Gracian Coin
					letter.setSenderId(1);
					letter.setSenderName("Donate");
					letter.setReceiverId(targetOID);
					letter.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(targetOID));
					letter.setTopic("A reward for your donation has arrived!");
					
					if (ServerVariables.getBool("DonationBonusActive", true) && ServerVariables.getLong("DonationBonusTime") >= System.currentTimeMillis())
						count += (count * ServerVariables.getInt("DonationBonusPercent")) / 100; // % extra coins
						
					letter.setBody("Thank you for supporting our server. You are rewarded with " + count + " coins. Enjoy your stay, and your reward :P");
					letter.setType(Mail.SenderType.NONE);
					letter.setUnread(true);
					letter.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					item.setCount(count);
					item.save();
					letter.addAttachment(item);
					letter.save();
					
					if (target != null)
					{
						target.sendPacket(ExNoticePostArrived.STATIC_TRUE);
						target.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
					}
					// TODO: how to add this shit to strings? available string is: l2r.gameserver.handler.admincommands.impl.admincreateitem.message34
					activeChar.sendMessage("A mail with " + count + " donation items has been sent to " + (playerName.length() > 0 ? playerName : target != null ? target.getName() : targetOID != 0 ? "OID: " + targetOID : "[no player found]"));
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message35", activeChar));
				}
				catch (NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincreateitem.message36", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/donate.htm"));
				
				break;
		}

		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private List<Player> getAllExeptBox(Player activeChar, int radius)
	{
		List<Player> allPlayersExeptBox = new ArrayList<Player>();
		
		List<Player> players = (radius <= 0) ? GameObjectsStorage.getAllPlayers() : World.getAroundPlayers(activeChar, radius, 500);
		for (Player player : players)
		{
			if (player == null)
				continue;
			
			boolean box = false;
			
			// ignore offline stores
			if (player.isInOfflineMode())
				continue;
			
			for (Player plr : allPlayersExeptBox)
			{
				if (player.isDualbox(plr))
					box = true;
			}
			if (!box)
				allPlayersExeptBox.add(player);
		}
		return allPlayersExeptBox;
	}
	
	private void createItem(Player activeChar, int itemId, long count)
	{
		ItemTemplate checkItem = ItemHolder.getInstance().getTemplate(itemId);
		if (checkItem == null)
			return;
		
		if (count > 10 && !checkItem.isStackable())
			return;
		
		ItemInstance createditem = ItemFunctions.createItem(itemId);
		createditem.setCount(count);
		
		activeChar.getInventory().addItem(createditem);
		
		if (!createditem.isStackable())
		{
			for (long i = 0; i < count - 1; i++)
			{
				createditem = ItemFunctions.createItem(itemId);
				activeChar.getInventory().addItem(createditem);
				Log.LogItem(activeChar, Log.AdminCreateItem, createditem);
			}
		}
		
		Log.LogItem(activeChar, Log.AdminCreateItem, createditem);
		
		activeChar.sendPacket(SystemMessage2.obtainItems(createditem.getItemId(), createditem.getCount(), 0));
	}
}