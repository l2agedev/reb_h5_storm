package l2r.gameserver.network.clientpackets;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.model.Player;
import l2r.gameserver.utils.Location;

public class CannotMoveAnymore extends L2GameClientPacket
{
	private Location _loc = new Location();

	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if (!activeChar.isOutOfControl())
			activeChar.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, _loc, null);
	}
}