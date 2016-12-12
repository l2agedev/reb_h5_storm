package l2r.loginserver.gameservercon.gspackets;

import l2r.commons.net.AdvIP;
import l2r.loginserver.GameServerManager;
import l2r.loginserver.gameservercon.GameServer;
import l2r.loginserver.gameservercon.ReceivablePacket;
import l2r.loginserver.gameservercon.lspackets.AuthResponse;
import l2r.loginserver.gameservercon.lspackets.LoginServerFail;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthRequest extends ReceivablePacket
{
	private final static Logger _log = LoggerFactory.getLogger(AuthRequest.class);

	private int _protocolVersion;
	private int[] requestIds;
	private boolean acceptAlternateID;
	private String[] externalIps;
	private String internalIp;
	private int maxOnline;
	private int _serverType;
	private int _ageLimit;
	private boolean _gmOnly;
	private boolean _brackets;
	private boolean _pvp;
	private int[] ports;
	private ArrayList<AdvIP> advIpList;


	@Override
	protected void readImpl()
	{
		_protocolVersion = readD();
		requestIds = new int[readC()];
		for (int i = 0; i < requestIds.length; i++)
			requestIds[i] = readC();
		acceptAlternateID = readC() == 1;
		_serverType = readD();
		_ageLimit = readD();
		_gmOnly = readC() == 1;
		_brackets = readC() == 1;
		_pvp = readC() == 1;
		externalIps = new String[readC()];
		for (int i = 0; i < externalIps.length; i++)
			externalIps[i] = readS();
		internalIp = readS();
		ports = new int[readH()];
		for(int i = 0; i < ports.length; i++)
			ports[i] = readH();
		maxOnline = readD();
		int advIpsSize = readD();
		advIpList = new ArrayList<AdvIP>();
		for(int i = 0; i < advIpsSize; i++)
		{
			AdvIP ip = new AdvIP();
			ip.ipadress = readS();
			ip.ipmask = readS();
			ip.bitmask = readS();
			advIpList.add(ip);
		}
	}

	@Override
	protected void runImpl()
	{
		_log.info("Registering gameserver: " + requestIds[0] + "(" + requestIds.length + ") [" + getGameServer().getConnection().getIpAddress() + "]");
		for (int i = 0; i < requestIds.length; i++)
			_log.info(" -> ServerId " + requestIds[i] + ": IP " + externalIps[i] + " Port " + ports[i]);

		int failReason = 0;

		GameServer gs = getGameServer();
		if(GameServerManager.getInstance().registerGameServer(requestIds, gs))
		{
			gs.setPorts(ports);
			gs.setExternalHosts(externalIps);
			gs.setInternalHost(internalIp);
			gs.setMaxPlayers(maxOnline);
			gs.setPvp(_pvp);
			gs.setServerType(_serverType);
			gs.setShowingBrackets(_brackets);
			gs.setGmOnly(_gmOnly);
			gs.setAgeLimit(_ageLimit);
			gs.setProtocol(_protocolVersion);
			gs.setAuthed(true);
			gs.getConnection().startPingTask();
			gs.setAdvIP(advIpList);
		}
		else if(acceptAlternateID)
		{
			if(GameServerManager.getInstance().registerGameServer(gs = getGameServer()))
			{
				gs.setPorts(ports);
				gs.setExternalHosts(externalIps);
				gs.setInternalHost(internalIp);
				gs.setMaxPlayers(maxOnline);
				gs.setPvp(_pvp);
				gs.setServerType(_serverType);
				gs.setShowingBrackets(_brackets);
				gs.setGmOnly(_gmOnly);
				gs.setAgeLimit(_ageLimit);
				gs.setProtocol(_protocolVersion);
				gs.setAuthed(true);
				gs.getConnection().startPingTask();
				gs.setAdvIP(advIpList);
			}
			else
				failReason = LoginServerFail.REASON_NO_FREE_ID;
		}
		else
			failReason = LoginServerFail.REASON_ID_RESERVED;

		if(failReason != 0)
		{
			_log.info("Gameserver registration failed.");
			sendPacket(new LoginServerFail(failReason));
			return;
		}

		_log.info("Gameserver registration successful.");
		sendPacket(new AuthResponse(gs));
	}
}
