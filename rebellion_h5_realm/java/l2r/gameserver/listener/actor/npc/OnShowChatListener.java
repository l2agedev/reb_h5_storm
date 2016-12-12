package l2r.gameserver.listener.actor.npc;

import l2r.gameserver.listener.NpcListener;
import l2r.gameserver.model.instances.NpcInstance;

public abstract interface OnShowChatListener extends NpcListener
{
	public abstract void onShowChat(NpcInstance paramNpcInstance);
}