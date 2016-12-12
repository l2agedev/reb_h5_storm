package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.InvisibleType;
import l2r.gameserver.network.serverpackets.CameraMode;
import l2r.gameserver.network.serverpackets.SpecialCamera;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class AdminCamera implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_freelook,
		admin_cinematic,
		admin_scene
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_freelook:
			{
				if(fullString.length() > 15)
					fullString = fullString.substring(15);
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincamera.usage", activeChar));
					return false;
				}

				int mode = Integer.parseInt(fullString);
				if(mode == 1)
				{
					activeChar.setInvisibleType(InvisibleType.NORMAL);
					activeChar.setIsInvul(true);
					activeChar.setNoChannel(-1);
					activeChar.setFlying(true);
				}
				else
				{
					activeChar.setInvisibleType(InvisibleType.NONE);
					activeChar.setIsInvul(false);
					activeChar.setNoChannel(0);
					activeChar.setFlying(false);
				}
				activeChar.sendPacket(new CameraMode(mode));

				break;
			}
			case admin_cinematic:
			{
				int id = Integer.parseInt(wordList[1]);
				int dist = Integer.parseInt(wordList[2]);
				int yaw = Integer.parseInt(wordList[3]);
				int pitch = Integer.parseInt(wordList[4]);
				int time = Integer.parseInt(wordList[5]);
				int duration = Integer.parseInt(wordList[6]);
				activeChar.sendPacket(new SpecialCamera(id, dist, yaw, pitch, time, duration));
				break;
			}
			case admin_scene:
			{
				int id = Integer.parseInt(wordList[1]);
				activeChar.showQuestMovie(id);
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