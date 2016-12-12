package l2r.gameserver.listener.actor;

import l2r.gameserver.listener.CharListener;
import l2r.gameserver.model.Creature;

public interface OnAttackListener extends CharListener
{
	public void onAttack(Creature actor, Creature target);
}
