package l2r.loginserver;

import l2r.commons.net.nio.impl.IAcceptFilter;
import l2r.commons.net.nio.impl.IClientFactory;
import l2r.commons.net.nio.impl.IMMOExecutor;
import l2r.commons.net.nio.impl.MMOConnection;
import l2r.loginserver.serverpackets.Init;

import java.nio.channels.SocketChannel;


public class SelectorHelper implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
	@Override
	public void execute(Runnable r)
	{
		ThreadPoolManager.getInstance().execute(r);
	}

	@Override
	public L2LoginClient create(MMOConnection<L2LoginClient> con)
	{
		final L2LoginClient client = new L2LoginClient(con);
		client.sendPacket(new Init(client));
		return client;
	}

	@Override
	public boolean accept(SocketChannel sc)
	{
		return !IpBanManager.getInstance().isIpBanned(sc.socket().getInetAddress().getHostAddress());
	}
}