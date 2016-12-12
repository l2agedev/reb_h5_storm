package l2r.gameserver.listener.actor.npc;

import l2r.gameserver.listener.NpcListener;
import l2r.gameserver.model.instances.NpcInstance;

public interface OnDecayListener extends NpcListener
{
	public void onDecay(NpcInstance actor);
}
