package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Shutdown;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.SessionKey;
import l2r.gameserver.network.loginservercon.gspackets.PlayerAuthRequest;
import l2r.gameserver.network.serverpackets.LoginFail;
import l2r.gameserver.network.serverpackets.ServerClose;
import l2r.gameserver.utils.Language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * cSddddd
 * cSdddddQ
 * loginName + keys must match what the loginserver used.
 */
public class AuthLogin extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(AuthLogin.class);
	
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private Language _language;

	@Override
	protected void readImpl()
	{
		_loginName = readS(32).toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
		_language = Language.getLanguage(readD());
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		
		if (_language == null)
		{
			_log.error("Trying to auth with unknown lang. " + client.toString());
			client.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
			return;
		}
		client.setLanguage(_language);
		  
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		client.setSessionId(key);
		client.setLoginName(_loginName);
		client.updateAccountData();
		
		if(Shutdown.getInstance().getMode() != Shutdown.NONE && Shutdown.getInstance().getSeconds() <= 15)
			client.closeNow(false);
		else
		{			
			if(AuthServerCommunication.getInstance().isShutdown())
			{
				client.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
				return;
			}
			
			GameClient oldClient = AuthServerCommunication.getInstance().addWaitingClient(client);
			if(oldClient != null)
				oldClient.close(ServerClose.STATIC);
			
			AuthServerCommunication.getInstance().sendPacket(new PlayerAuthRequest(client));
		}
	}
}