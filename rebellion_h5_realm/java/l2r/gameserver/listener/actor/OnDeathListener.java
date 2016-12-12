package l2r.gameserver.listener.actor;

import l2r.gameserver.listener.CharListener;
import l2r.gameserver.model.Creature;

public interface OnDeathListener extends CharListener
{
	public void onDeath(Creature actor, Creature killer);
}
