package l2r.gameserver.listener.actor;

import l2r.gameserver.listener.CharListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;

public interface OnMagicUseListener extends CharListener
{
	public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt);
}
