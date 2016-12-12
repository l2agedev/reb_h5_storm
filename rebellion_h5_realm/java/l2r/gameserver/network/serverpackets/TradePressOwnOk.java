package l2r.gameserver.network.serverpackets;

//@Deprecated
public class TradePressOwnOk extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0x53);
	}
}