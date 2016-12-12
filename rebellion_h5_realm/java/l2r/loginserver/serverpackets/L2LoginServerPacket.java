package l2r.loginserver.serverpackets;

import l2r.commons.net.nio.impl.SendablePacket;
import l2r.loginserver.L2LoginClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class L2LoginServerPacket extends SendablePacket<L2LoginClient>
{
	private static final Logger _log = LoggerFactory.getLogger(L2LoginServerPacket.class);

	@Override
	public final boolean write()
	{
		try
		{
			writeImpl();
			return true;
		}
		catch(Exception e)
		{
			_log.error("Client: " + getClient() + " - Failed writing: " + getClass().getSimpleName() + "!", e);
		}
		return false;
	}

	protected abstract void writeImpl();
}
