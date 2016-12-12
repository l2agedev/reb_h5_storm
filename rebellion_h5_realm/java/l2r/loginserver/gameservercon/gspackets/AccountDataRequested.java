package l2r.loginserver.gameservercon.gspackets;


import l2r.loginserver.gameservercon.ReceivablePacket;
import l2r.loginserver.gameservercon.lspackets.AccountDataResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountDataRequested extends ReceivablePacket
{
	public static final Logger _log = LoggerFactory.getLogger(AccountDataRequested.class);

	private String _account;

	@Override
	protected void readImpl()
	{
		_account = readS();
	}

	@Override
	protected void runImpl()
	{
		sendPacket(new AccountDataResponse(_account));
	}
}