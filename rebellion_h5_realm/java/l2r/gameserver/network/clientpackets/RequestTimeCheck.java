package l2r.gameserver.network.clientpackets;

public class RequestTimeCheck extends L2GameClientPacket
{
	int unk, unk2;

	/**
	 * format: dd
	 */
	@Override
	protected void readImpl()
	{
		unk = readD();
		unk2 = readD();
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}