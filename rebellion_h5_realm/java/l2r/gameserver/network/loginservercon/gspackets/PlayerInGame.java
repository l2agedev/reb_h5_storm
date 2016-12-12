package l2r.gameserver.network.loginservercon.gspackets;

import l2r.gameserver.network.loginservercon.SendablePacket;

public class PlayerInGame extends SendablePacket
{
	private String account;
	
	public PlayerInGame(String account)
	{
		this.account = account;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x03);
		writeS(account);
	}
}
