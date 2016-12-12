package l2r.gameserver.network.serverpackets;

public class ExEventMatchCreate extends L2GameServerPacket
{
	final int _raceId;
	
	public ExEventMatchCreate(int raceId)
	{
		_raceId = raceId;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x1D);
		writeD(_raceId);
	}
}