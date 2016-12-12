package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.model.CursedWeapon;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.utils.ItemFunctions;

public class AdminCursedWeapons implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_cw_info,
		admin_cw_remove,
		admin_cw_goto,
		admin_cw_reload,
		admin_cw_add,
		admin_cw_drop,
		admin_cw_increase
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();

		CursedWeapon cw = null;
		switch(command)
		{
			case admin_cw_remove:
			case admin_cw_goto:
			case admin_cw_add:
			case admin_cw_drop:
			case admin_cw_increase:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message1", activeChar));
					return false;
				}
				for(CursedWeapon cwp : CursedWeaponsManager.getInstance().getCursedWeapons())
					if(cwp.getName().toLowerCase().contains(wordList[1].toLowerCase()))
						cw = cwp;
				if(cw == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message2", activeChar));
					return false;
				}
				break;
		}

		switch(command)
		{
			case admin_cw_info:
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message3", activeChar));
				for(CursedWeapon c : cwm.getCursedWeapons())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message4", activeChar, c.getName(), c.getItemId()));
					if(c.isActivated())
					{
						Player pl = c.getPlayer();
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message5", activeChar, pl.getName()));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message6", activeChar, c.getPlayerKarma()));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message7", activeChar, c.getTimeLeft() / 60000));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message8", activeChar, c.getNbKills()));
					}
					else if(c.isDropped())
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message9", activeChar));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message10", activeChar, c.getTimeLeft() / 60000));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message11", activeChar, c.getNbKills()));
					}
					else
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message12", activeChar));
				}
				break;
			case admin_cw_reload:
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message13", activeChar));
				break;
			case admin_cw_remove:
				if(cw == null)
					return false;
				CursedWeaponsManager.getInstance().endOfLife(cw);
				break;
			case admin_cw_goto:
				if(cw == null)
					return false;
				activeChar.teleToLocation(cw.getLoc());
				break;
			case admin_cw_add:
				if(cw == null)
					return false;
				if(cw.isActive())
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message14", activeChar));
				else
				{
					GameObject target = activeChar.getTarget();
					if(target != null && target.isPlayer() && !((Player) target).isInOlympiadMode())
					{
						Player player = (Player) target;
						ItemInstance item = ItemFunctions.createItem(cw.getItemId());
						
						// Изменил команду для выдачи проклятого оружия, теперь работает хорошо.
						cw.setLoc(player.getLoc());
						cw.setEndTime(System.currentTimeMillis() + cw.getRndDuration() * 60000);
						
						player.getInventory().addItem(item);
					}
				}
				break;
			case admin_cw_increase:
				// Увеличивает кол-во убийств у цели-владельца проклятого оружия.
				if(cw == null)
					return false;
				
				if(cw.isActive())
				{
					cw.increaseKills();
					
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message15", activeChar));
				}
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message16", activeChar));
				
				break;
			case admin_cw_drop:
				if(cw == null)
					return false;
				if(cw.isActive())
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincursedweapons.message17", activeChar));
				else
				{
					GameObject target = activeChar.getTarget();
					if(target != null && target.isPlayer() && !((Player) target).isInOlympiadMode())
					{
						Player player = (Player) target;
						cw.create(null, player);
					}
				}
				break;
		}
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}