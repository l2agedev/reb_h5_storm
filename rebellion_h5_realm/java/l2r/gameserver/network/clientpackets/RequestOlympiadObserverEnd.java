package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.nexus_interface.NexusEvents;

public class RequestOlympiadObserverEnd extends L2GameClientPacket
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
		
		if (activeChar.getObserverMode() == Player.OBSERVER_STARTED)
			if (NexusEvents.isObserving(activeChar))
				NexusEvents.endObserving(activeChar);
			else if (activeChar.getOlympiadObserveGame() != null)
				activeChar.leaveOlympiadObserverMode(true);
	}
}