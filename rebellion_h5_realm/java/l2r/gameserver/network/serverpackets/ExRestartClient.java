package l2r.gameserver.network.serverpackets;

public class ExRestartClient extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeEx(0x48);
	}
}
