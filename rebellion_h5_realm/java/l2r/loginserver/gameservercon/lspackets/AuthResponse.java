package l2r.loginserver.gameservercon.lspackets;

import l2r.loginserver.gameservercon.GameServer;
import l2r.loginserver.gameservercon.SendablePacket;

public class AuthResponse extends SendablePacket
{
	private int serverId;
	private String name;

	public AuthResponse(GameServer gs)
	{
		serverId = gs.getIds()[0];
		name = gs.getName();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x00);
		writeC(serverId);
		writeS(name);
	}
}