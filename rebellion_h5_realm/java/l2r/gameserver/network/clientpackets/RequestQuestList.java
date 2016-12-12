package l2r.gameserver.network.clientpackets;

import l2r.gameserver.network.serverpackets.QuestList;

public class RequestQuestList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		sendPacket(new QuestList(getClient().getActiveChar()));
	}
}