package l2r.gameserver.network.loginservercon.lspackets;

import l2r.gameserver.dao.AccountsDAO;
import l2r.gameserver.network.AccountData;
import l2r.gameserver.network.loginservercon.ReceivablePacket;


public class AccountDataReceived extends ReceivablePacket
{
	private AccountData _data;

	@Override
	public void readImpl()
	{
		_data = new AccountData();
		_data.account = readS();
		_data.accessLevel = readD();
		_data.banExpire = readD();
		_data.allowedIps = readS();
		_data.allowedHwids = readS();
		_data.bonus = readF();
		_data.bonusExpire = readD();
		_data.lastServer = readD();
		_data.lastIp = readS();
		_data.lastAccess = readD();
		_data.botReportPoints = readD();
		_data.points = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!AccountData.DUMMY.lastIp.equalsIgnoreCase(_data.lastIp)) // This is identifier that such account doesnt exist.
			AccountsDAO.setAccountData(_data.account, _data);
	}
}