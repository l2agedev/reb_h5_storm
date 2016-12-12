package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.tables.PetDataTable;

public class AdminRide implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_ride,
		admin_ride_wyvern,
		admin_ride_strider,
		admin_set_ride,
		admin_unride,
		admin_wr,
		admin_sr,
		admin_ur
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_ride:
				if (activeChar.isMounted())
					activeChar.dismount();
				if(wordList.length != 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminride.message1", activeChar));
					return false;
				}
				activeChar.setMount(Integer.parseInt(wordList[1]), 0, 85);
				break;
			case admin_set_ride:
				if (activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminride.message2", activeChar));
					return false;
				}
				
				if (activeChar.getTarget().getPlayer().isMounted())
					activeChar.getTarget().getPlayer().dismount();
				if(wordList.length != 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminride.message3", activeChar));
					return false;
				}
				activeChar.getTarget().getPlayer().setMount(Integer.parseInt(wordList[1]), 0, 85);
				break;
			case admin_ride_wyvern:
			case admin_wr:
				if(activeChar.isMounted() || activeChar.getPet() != null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminride.message4", activeChar));
					return false;
				}
				activeChar.setMount(PetDataTable.WYVERN_ID, 0, 85);
				break;
			case admin_ride_strider:
			case admin_sr:
				if(activeChar.isMounted() || activeChar.getPet() != null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminride.message5", activeChar));
					return false;
				}
				activeChar.setMount(PetDataTable.STRIDER_WIND_ID, 0, 85);
				break;
			case admin_unride:
			case admin_ur:
				activeChar.dismount();
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