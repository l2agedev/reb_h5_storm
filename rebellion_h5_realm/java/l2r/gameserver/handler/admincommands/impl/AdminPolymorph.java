package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class AdminPolymorph implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_polyself,
		admin_polymorph,
		admin_poly,
		admin_unpolyself,
		admin_unpolymorph,
		admin_unpoly
	}

	@Override
	@SuppressWarnings("fallthrough")
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		GameObject target = activeChar.getTarget();

		switch(command)
		{
			case admin_polyself:
				target = activeChar;
			case admin_polymorph:
			case admin_poly:
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				try
				{
					int id = Integer.parseInt(wordList[1]);
					if(NpcHolder.getInstance().getTemplate(id) != null)
					{
						((Player) target).setPolyId(id);
						((Player) target).broadcastCharInfo();
					}
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpolymorph.message1", activeChar));
					return false;
				}
				break;
			case admin_unpolyself:
				target = activeChar;
			case admin_unpolymorph:
			case admin_unpoly:
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				((Player) target).setPolyId(0);
				((Player) target).broadcastCharInfo();
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