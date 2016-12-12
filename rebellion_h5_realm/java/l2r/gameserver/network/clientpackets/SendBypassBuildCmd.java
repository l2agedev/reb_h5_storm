package l2r.gameserver.network.clientpackets;

import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.tables.AdminTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendBypassBuildCmd extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(SendBypassBuildCmd.class);
	private String _command;

	@Override
	protected void readImpl()
	{
		_command = readS();

		if(_command != null)
			_command = _command.trim();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		String cmd = _command;

		if(!cmd.contains("admin_"))
			cmd = "admin_" + cmd;

		if (!AdminTable.getInstance().hasAccess(cmd , activeChar.getAccessLevel()))
		{
			//activeChar.sendMessage("You don't have the access right to use this command!");
			_log.warn("Character " + activeChar.getName() + " tryed to use admin command " + cmd + ", but have no access to it!");
			return;
		}
		
		AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, cmd);
	}
}