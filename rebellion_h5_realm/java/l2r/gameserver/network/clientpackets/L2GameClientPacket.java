package l2r.gameserver.network.clientpackets;

import l2r.commons.net.nio.impl.ReceivablePacket;
import l2r.gameserver.GameServer;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;

import java.nio.BufferUnderflowException;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Packets received by the game server from clients
 */
public abstract class L2GameClientPacket extends ReceivablePacket<GameClient>
{
	private static final Logger _log = LoggerFactory.getLogger(L2GameClientPacket.class);
	
	@Override
	public final boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch(BufferUnderflowException e)
		{
			_client.onPacketReadFail();
			_log.error("Client: " + _client + " - Failed reading: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
		}
		catch(Exception e)
		{
			_log.error("Client: " + _client + " - Failed reading: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
		}

		return false;
	}

	protected abstract void readImpl() throws Exception;

	@Override
	public final void run()
	{
		GameClient client = getClient();
		try
		{
			runImpl();
			
			Player player = client.getActiveChar();
			
			if (player != null && player.getEventInfo() != null && player.getEventInfo().isInEvent())
			{
				boolean hasMoved = System.currentTimeMillis() - player.getLastMovePacket() > 60000;
				
				if (triggersOnActionRequest() && !hasMoved)
					player.getEventInfo().onAction();
			}
		}
		catch(Exception e)
		{
			_log.error("Client: " + client + " - Failed running: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
		}
	}

	protected abstract void runImpl() throws Exception;

	protected String readS(int len)
	{
		String ret = readS();
		return ret.length() > len ? ret.substring(0, len) : ret;
	}

	protected void sendPacket(L2GameServerPacket packet)
	{
		getClient().sendPacket(packet);
	}

	protected void sendPacket(L2GameServerPacket... packets)
	{
		getClient().sendPacket(packets);
	}

	protected void sendPackets(List<L2GameServerPacket> packets)
	{
		getClient().sendPackets(packets);
	}

	public String getType()
	{
		return "[C] " + getClass().getSimpleName();
	}
	
	protected boolean triggersOnActionRequest()
	{
		return true;
	}
	
}