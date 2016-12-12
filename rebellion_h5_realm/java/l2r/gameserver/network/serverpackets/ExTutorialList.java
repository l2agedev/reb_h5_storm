package l2r.gameserver.network.serverpackets;

public class ExTutorialList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x6B);
		// todo writeB(new byte[128]);
	}
}