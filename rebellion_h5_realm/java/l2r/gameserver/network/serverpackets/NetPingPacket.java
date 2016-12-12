package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Player;

public class NetPingPacket extends L2GameServerPacket
{
	private int _clientId;
	
	public NetPingPacket(Player cha)
	{
		_clientId = cha.getObjectId();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xD9);
		writeD(_clientId);
	}
}
