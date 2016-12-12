package l2r.gameserver.network.serverpackets;

public class ExEventMatchTeamUnlocked extends L2GameServerPacket
{
	public ExEventMatchTeamUnlocked()
	{
		
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x06);
		writeD(1); // Unknown
		writeC(1); // Unknown
	}
}