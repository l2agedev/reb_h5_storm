package l2r.gameserver.network.clientpackets;

public class RequestSEKCustom extends L2GameClientPacket
{
	int SlotNum, Direction;

	/**
	 * format: dd
	 */
	@Override
	protected void readImpl()
	{
		SlotNum = readD();
		Direction = readD();
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}