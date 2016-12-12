package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExGetBookMarkInfo;
import l2r.gameserver.network.serverpackets.components.ChatType;

public class RequestDeleteBookMarkSlot extends L2GameClientPacket
{
	private int slot;

	@Override
	protected void readImpl()
	{
		slot = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			if (Config.SECURITY_ENABLED && Config.SECURITY_DELETE_BOOKMARK_SLOT && activeChar.getSecurity())
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
			}
			
			//TODO Msg.THE_SAVED_TELEPORT_LOCATION_WILL_BE_DELETED_DO_YOU_WISH_TO_CONTINUE
			activeChar.getTeleportBookmarks().remove(slot);
			activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
		}
	}
}