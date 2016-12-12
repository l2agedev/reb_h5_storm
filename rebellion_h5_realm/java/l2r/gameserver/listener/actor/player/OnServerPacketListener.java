package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.IStaticPacket;

public interface OnServerPacketListener extends PlayerListener
{
	/**
	 * 
	 * @param player
	 * @param packet
	 * @return true to block the packet from being sent to the client.
	 */
	public boolean onPacketReceived(Player player, IStaticPacket packet);
}
