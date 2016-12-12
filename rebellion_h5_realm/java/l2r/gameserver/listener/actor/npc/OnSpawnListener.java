package l2r.gameserver.listener.actor.npc;

import l2r.gameserver.listener.NpcListener;
import l2r.gameserver.model.instances.NpcInstance;

public interface OnSpawnListener extends NpcListener
{
	public void onSpawn(NpcInstance actor);
}
