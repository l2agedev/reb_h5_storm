package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.events.impl.UndergroundColiseumBattleEvent;
import l2r.gameserver.network.serverpackets.ExPVPMatchRecord;

public class RequestPVPMatchRecord extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		UndergroundColiseumBattleEvent battleEvent = player.getEvent(UndergroundColiseumBattleEvent.class);
		if(battleEvent == null)
			return;

		player.sendPacket(new ExPVPMatchRecord(ExPVPMatchRecord.UPDATE, TeamType.NONE, battleEvent));
	}
}