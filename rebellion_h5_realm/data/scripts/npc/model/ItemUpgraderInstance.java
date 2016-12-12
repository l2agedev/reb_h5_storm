package npc.model;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.npc.NpcTemplate;

/**
 * @author Nik
 */

public final class ItemUpgraderInstance extends NpcInstance
{
	public ItemUpgraderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (!canBypassCheck(player, this))
			return;
		
		if (command.startsWith("check_item_upgrade"))
		{
			ItemInstance item = getPlayer().getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (item != null)
			{
				showChatWindow(player, "custom/ItemUpgrader.htm", "%itemLevel%", item.getItemLevel());
			}
			else
			{
				player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_weapon", player));
				return;
			}
		}
		else if (command.startsWith("item_level_upgrade"))
		{
			ItemInstance item = getPlayer().getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (item == null)
			{
				player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_weapon", player));
				return;
			}
			else
			{
				
				if (item.getItemLevel() >= 10)
				{
					player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.cannot_upgrade", player));
					return;
				}
				else
				{
					switch (item.getItemLevel())
					{
						case 0:
							if (player.getSp() < 100000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp1", player));
								return;
							}
						case 1:
							if (player.getSp() < 200000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp2", player));
								return;
							}
						case 2:
							if (player.getSp() < 300000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp3", player));
								return;
							}
						case 3:
							if (player.getSp() < 400000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp4", player));
								return;
							}
						case 4:
							if (player.getSp() < 500000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp5", player));
								return;
							}
						case 5:
							if (player.getSp() < 600000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp6", player));
								return;
							}
						case 6:
							if (player.getSp() < 700000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp7", player));
								return;
							}
						case 7:
							if (player.getSp() < 800000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp8", player));
								return;
							}
						case 8:
							if (player.getSp() < 900000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp9", player));
								return;
							}
						case 9:
							if (player.getSp() < 1000000000)
							{
								player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.need_sp_last", player));
								return;
							}
					}
					
					item.setItemLevel(item.getItemLevel() + 1);
					player.sendMessage(new CustomMessage("scripts.npc.model.itemupgraderinstance.success", player));
				}
			}
		}
		
		else
			super.onBypassFeedback(player, command);
	}
}