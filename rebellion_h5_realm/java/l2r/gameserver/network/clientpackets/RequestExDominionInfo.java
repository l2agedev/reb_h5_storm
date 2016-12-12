package l2r.gameserver.network.clientpackets;

import l2r.gameserver.data.xml.holder.EventHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.EventType;
import l2r.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2r.gameserver.network.serverpackets.ExReplyDominionInfo;
import l2r.gameserver.network.serverpackets.ExShowOwnthingPos;

public class RequestExDominionInfo extends L2GameClientPacket
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

		activeChar.sendPacket(new ExReplyDominionInfo());

		DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
		if(runnerEvent.isInProgress())
			activeChar.sendPacket(new ExShowOwnthingPos());
	}
}