package l2r.gameserver.listener.actor;

import l2r.gameserver.listener.CharListener;
import l2r.gameserver.model.Creature;

/**
 * @author VISTALL
 */
public interface OnReviveListener extends CharListener
{
	public void onRevive(Creature actor);
}
