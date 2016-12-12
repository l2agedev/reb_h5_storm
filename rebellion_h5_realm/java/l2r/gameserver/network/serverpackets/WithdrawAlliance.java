package l2r.gameserver.network.serverpackets;

public class WithdrawAlliance extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0xAB);
		//TODO d
	}
}