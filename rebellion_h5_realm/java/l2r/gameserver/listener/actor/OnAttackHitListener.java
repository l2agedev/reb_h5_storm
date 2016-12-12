package l2r.gameserver.listener.actor;

import l2r.gameserver.listener.CharListener;
import l2r.gameserver.model.Creature;

public interface OnAttackHitListener extends CharListener
{
	public void onAttackHit(Creature actor, Creature attacker);
}
