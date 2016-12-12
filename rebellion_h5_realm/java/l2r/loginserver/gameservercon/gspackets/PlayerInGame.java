package l2r.loginserver.gameservercon.gspackets;

import l2r.loginserver.gameservercon.GameServer;
import l2r.loginserver.gameservercon.ReceivablePacket;

public class PlayerInGame extends ReceivablePacket
{
	private String account;

	@Override
	protected void readImpl()
	{
		account = readS();
	}

	@Override
	protected void runImpl()
	{
		GameServer gs = getGameServer();
		if(gs.isAuthed())
			gs.addAccount(account);
	}
}
