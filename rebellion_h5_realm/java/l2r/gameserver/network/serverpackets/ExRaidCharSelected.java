package l2r.gameserver.network.serverpackets;

public class ExRaidCharSelected extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0xB5);
		// just a trigger
	}
}