package l2r.gameserver.network.clientpackets;

public class RequestExBR_EventRankerList extends L2GameClientPacket
{
	int unk, unk2, unk3;

	/**
	 * format: ddd
	 */
	@Override
	protected void readImpl()
	{
		unk = readD();
		unk2 = readD();
		unk3 = readD();
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}

}