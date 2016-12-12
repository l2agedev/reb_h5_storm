package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExShowFortressInfo;

public class RequestAllFortressInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new ExShowFortressInfo());
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}