package l2r.gameserver.handler.usercommands.impl;

import l2r.gameserver.handler.usercommands.IUserCommandHandler;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.mapregion.RestartArea;

/**
 * Support for /loc command
 */
public class Loc implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = {0};

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
			return false;

		RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, activeChar);
		int msgId = ra != null ? ra.getRestartPoint().get(activeChar.getRace()).getMsgId() : 0;
		if(msgId > 0)
		{
			if (SystemMsg.contains(msgId))
			{
				SystemMessage sm = new SystemMessage(SystemMsg.valueOf(msgId));
				sm.addNumber(activeChar.getX());
				sm.addNumber(activeChar.getY());
				sm.addNumber(activeChar.getZ());
				activeChar.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(msgId);
				sm.addNumber(activeChar.getX());
				sm.addNumber(activeChar.getY());
				sm.addNumber(activeChar.getZ());
				activeChar.sendPacket(sm);
			}
		}
		else
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.usercommands.impl.loc.message1", activeChar, activeChar.getX(), activeChar.getY(), activeChar.getZ()));

		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}