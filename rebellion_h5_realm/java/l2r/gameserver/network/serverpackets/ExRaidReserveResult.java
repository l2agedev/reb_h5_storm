package l2r.gameserver.network.serverpackets;

public class ExRaidReserveResult extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0xB6);
		// TODO dx[dddd]
	}
}