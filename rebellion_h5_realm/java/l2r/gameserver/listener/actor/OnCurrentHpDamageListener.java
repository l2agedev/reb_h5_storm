package l2r.gameserver.listener.actor;

import l2r.gameserver.listener.CharListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;

public interface OnCurrentHpDamageListener extends CharListener
{
	public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill);
}
