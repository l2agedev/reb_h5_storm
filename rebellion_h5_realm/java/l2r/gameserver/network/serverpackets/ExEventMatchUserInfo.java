package l2r.gameserver.network.serverpackets;

public class ExEventMatchUserInfo extends L2GameServerPacket
{
	protected void writeImpl()
	{
		writeEx(0x02);
	}
}