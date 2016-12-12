package l2r.gameserver.network.clientpackets;

import l2r.gameserver.network.serverpackets.ExShowCastleInfo;

public class RequestAllCastleInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		getClient().getActiveChar().sendPacket(new ExShowCastleInfo());
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}