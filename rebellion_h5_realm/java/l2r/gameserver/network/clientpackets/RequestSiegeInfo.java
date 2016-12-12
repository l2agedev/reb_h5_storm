package l2r.gameserver.network.clientpackets;

public class RequestSiegeInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		System.out.println(getType());
	}
}