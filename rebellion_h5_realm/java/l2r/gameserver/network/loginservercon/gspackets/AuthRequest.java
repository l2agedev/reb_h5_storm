package l2r.gameserver.network.loginservercon.gspackets;

import l2r.commons.net.AdvIP;
import l2r.gameserver.Config;
import l2r.gameserver.GameServer;
import l2r.gameserver.network.loginservercon.SendablePacket;

public class AuthRequest extends SendablePacket
{
	protected void writeImpl()
	{
		writeC(0x00);
		writeD(GameServer.AUTH_SERVER_PROTOCOL);
		writeC(Config.REQUEST_IDS.length);
		for (int id : Config.REQUEST_IDS)
			writeC(id);
		writeC(Config.ACCEPT_ALTERNATE_ID ? 0x01 : 0x00);
		writeD(Config.AUTH_SERVER_SERVER_TYPE);
		writeD(Config.AUTH_SERVER_AGE_LIMIT);
		writeC(Config.AUTH_SERVER_GM_ONLY ? 0x01 : 0x00);
		writeC(Config.AUTH_SERVER_BRACKETS ? 0x01 : 0x00);
		writeC(Config.AUTH_SERVER_IS_PVP ? 0x01 : 0x00);
		writeC(Config.EXTERNAL_LOGIN_HOSTNAME.length);
		for (String host : Config.EXTERNAL_LOGIN_HOSTNAME)
			writeS(host);
		writeS(Config.INTERNAL_HOSTNAME);
		writeH(Config.PORTS_GAME.length);
		for(int PORT_GAME : Config.PORTS_GAME)
			writeH(PORT_GAME);
		writeD(Config.MAXIMUM_ONLINE_USERS);
		writeD(Config.GAMEIPS.size());
		for(AdvIP ip : Config.GAMEIPS)
		{
			writeS(ip.ipadress);
			writeS(ip.ipmask);
			writeS(ip.bitmask);
		}
	}
}
