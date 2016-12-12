package l2r.gameserver.network.serverpackets;

public class ExPeriodicItemList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x87);
		writeD(0); // count of dd
	}
}