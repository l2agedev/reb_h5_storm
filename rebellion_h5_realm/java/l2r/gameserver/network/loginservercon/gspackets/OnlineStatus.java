package l2r.gameserver.network.loginservercon.gspackets;

import l2r.gameserver.network.loginservercon.SendablePacket;

public class OnlineStatus extends SendablePacket
{
	private boolean _online;

	public OnlineStatus(boolean online)
	{
		_online = online;
	}

	protected void writeImpl()
	{
		writeC(0x01);
		writeC(_online ? 1 : 0);
	}
}
