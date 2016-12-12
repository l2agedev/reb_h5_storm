package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExGetBookMarkInfo;

public class RequestBookMarkSlotInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//just trigger
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
	}
}