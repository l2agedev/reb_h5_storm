package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.clientpackets.RequestActionUse.Action;

public interface OnSocialActionListener extends PlayerListener
{
	public void onSocialAction(Player player, GameObject target, Action action);
}
