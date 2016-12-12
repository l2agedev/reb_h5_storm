package l2r.gameserver.network.serverpackets;

public class DismissAlliance extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xAD);
		//TODO d
	}
}