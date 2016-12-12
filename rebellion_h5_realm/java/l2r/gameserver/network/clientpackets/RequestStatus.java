package l2r.gameserver.network.clientpackets;

import l2r.gameserver.network.serverpackets.SendStatus;

public final class RequestStatus extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		getClient().close(new SendStatus());
	}
}