package l2r.gameserver.network.serverpackets;

public class OustAllianceMemberPledge extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xAC);
		//TODO d
	}
}