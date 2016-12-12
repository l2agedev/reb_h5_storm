package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.matching.MatchingRoom;

/**
 * Format: (ch) dd
 */
public class RequestDismissPartyRoom extends L2GameClientPacket
{
	private int _roomId;

	@Override
	protected void readImpl()
	{
		_roomId = readD(); //room id
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		MatchingRoom room = player.getMatchingRoom();
		if(room == null || room.getId() != _roomId || room.getType() != MatchingRoom.PARTY_MATCHING)
			return;

		if(room.getLeader() != player)
			return;

		room.disband();
	}
}