package l2r.loginserver.gameservercon.lspackets;

import l2r.loginserver.gameservercon.SendablePacket;

public class PingRequest extends SendablePacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xff);
	}
}