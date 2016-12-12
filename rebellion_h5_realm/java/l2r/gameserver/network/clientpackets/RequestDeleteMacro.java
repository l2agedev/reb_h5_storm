package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;

public class RequestDeleteMacro extends L2GameClientPacket
{
	private int _id;

	/**
	 * format:		cd
	 */
	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if (Config.SECURITY_ENABLED && Config.SECURITY_DELETE_MACRO && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		activeChar.deleteMacro(_id);
	}
}