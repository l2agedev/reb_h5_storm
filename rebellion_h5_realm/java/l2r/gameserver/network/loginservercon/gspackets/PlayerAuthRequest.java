package l2r.gameserver.network.loginservercon.gspackets;

import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.loginservercon.SendablePacket;
import l2r.gameserver.network.serverpackets.LoginFail;
import l2r.gameserver.utils.HwidBansChecker;

import ru.akumu.smartguard.GuardConfig;
import ru.akumu.smartguard.manager.session.ClientSessionManager;
import ru.akumu.smartguard.manager.session.model.ClientSession;

public class PlayerAuthRequest extends SendablePacket
{
	private String account;
	private int playOkID1, playOkID2, loginOkID1, loginOkID2;
	
	public PlayerAuthRequest(GameClient client)
	{
		account = client.getLogin();
		playOkID1 = client.getSessionKey().playOkID1;
		playOkID2 = client.getSessionKey().playOkID2;
		loginOkID1 = client.getSessionKey().loginOkID1;
		loginOkID2 = client.getSessionKey().loginOkID2;
		
		// lets set hwid here for smartguard
		if (GuardConfig.ProtectionEnabled)
		{
			ClientSession cd = ClientSessionManager.getSession(client);
			if (cd != null)
			{
				client.setHWID(cd.hwid());
			}
		}
		
		if (client != null)
		{
			if (HwidBansChecker.getInstance().isClientBanned(client))
			{
				client.close(new LoginFail(LoginFail.INCORRECT_ACCOUNT_INFO_CONTACT_CUSTOMER_SUPPORT));
				return;
			}
		}
	}
	
	protected void writeImpl()
	{
		writeC(0x02);
		writeS(account);
		writeD(playOkID1);
		writeD(playOkID2);
		writeD(loginOkID1);
		writeD(loginOkID2);
	}
}