package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.Player;

public interface OnLevelChangeListener extends PlayerListener
{
	public void onLevelChange(Player player, int oldLvl, int newLvl);
}
