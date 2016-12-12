package l2r.gameserver.network.serverpackets;

public class ExEventMatchFirecracker extends L2GameServerPacket
{
	private int _playerObjectId;
	public ExEventMatchFirecracker(int playerObjectId)
	{
		_playerObjectId = playerObjectId;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x05);
		writeD(_playerObjectId);
	}
}