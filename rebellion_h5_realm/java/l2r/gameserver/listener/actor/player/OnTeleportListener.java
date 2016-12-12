package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Reflection;

public interface OnTeleportListener extends PlayerListener
{
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection);
}
