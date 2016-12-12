package l2r.gameserver.listener.actor.ai;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.listener.AiListener;
import l2r.gameserver.model.Creature;

public interface OnAiEventListener extends AiListener
{
	public void onAiEvent(Creature actor, CtrlEvent evt, Object[] args);
}
