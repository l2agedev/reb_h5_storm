package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.Player;

public interface OnPlayerPartyInviteListener extends PlayerListener
{
	public void onPartyInvite(Player player);
}
