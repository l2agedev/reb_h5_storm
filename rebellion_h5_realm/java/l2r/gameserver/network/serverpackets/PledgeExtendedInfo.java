package l2r.gameserver.network.serverpackets;

public class PledgeExtendedInfo extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0x8A);
		//TODO SddSddddddddSd
	}
}