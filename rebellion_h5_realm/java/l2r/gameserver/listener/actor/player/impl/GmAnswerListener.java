package l2r.gameserver.listener.actor.player.impl;

import l2r.commons.lang.reference.HardReference;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.tables.AdminTable;

public class GmAnswerListener implements OnAnswerListener
{
	private HardReference<Player> _playerRef;
	private String _command;

	public GmAnswerListener(Player player, String commandBypass)
	{
		_command = commandBypass;
		_playerRef = player.getRef();
	}

	@Override
	public void sayYes()
	{
		Player player = _playerRef.get();
		if(player == null)
			return;

		if (AdminTable.getInstance().hasAccess(_command, player.getAccessLevel()))
			AdminCommandHandler.getInstance().useAdminCommandHandler(player, _command);
	}

	@Override
	public void sayNo()
	{
		//
	}
}
