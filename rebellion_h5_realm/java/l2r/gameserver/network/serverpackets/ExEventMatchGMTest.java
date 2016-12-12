package l2r.gameserver.network.serverpackets;

public class ExEventMatchGMTest extends L2GameServerPacket
{
	public ExEventMatchGMTest()
	{
		
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x07);
		// just trigger
	}
}