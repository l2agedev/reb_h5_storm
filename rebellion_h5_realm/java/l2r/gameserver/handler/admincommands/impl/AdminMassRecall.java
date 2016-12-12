package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.Location;

public class AdminMassRecall implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_mass_recall,
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, final Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_mass_recall:
			{
				final Location loc = activeChar.getLoc();
				final int reflectionId = activeChar.getReflectionId();
				for(final Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					if(player != null && !player.isInOfflineMode() && !player.isInJail() && !player.isInObserverMode() && player.getKarma() == 0 && !player.isInOlympiadMode() && !player.isDead() && player.getReflectionId() == 0)
					{
						player.ask(new ConfirmDlg(SystemMsg.S1, 30000).addString("Do you want to join the event managed by the Game Master?"), new OnAnswerListener()
						{
							@Override
							public void sayYes()
							{
								player.teleToLocation(loc, reflectionId);
							}

							@Override
							public void sayNo()
							{
								//
							}
						});
					}
				}
				
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
