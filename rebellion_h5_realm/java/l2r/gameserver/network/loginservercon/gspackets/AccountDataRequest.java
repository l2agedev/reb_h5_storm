package l2r.gameserver.network.loginservercon.gspackets;

import l2r.gameserver.network.loginservercon.SendablePacket;

public class AccountDataRequest extends SendablePacket
{
	private String _account;
	
	public AccountDataRequest(String account)
	{
		_account = account;
	}
	
	protected void writeImpl()
	{
		writeC(0x12);
		writeS(_account);
	}
}