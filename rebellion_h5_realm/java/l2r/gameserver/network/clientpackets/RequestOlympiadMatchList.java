package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.ExReceiveOlympiad;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class RequestOlympiadMatchList extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{
		// trigger
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(!Olympiad.inCompPeriod() || Olympiad.isOlympiadEnd())
		{
			player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return;
		}

		player.sendPacket(new ExReceiveOlympiad.MatchList());
	}
}
