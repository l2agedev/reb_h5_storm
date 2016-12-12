package l2r.gameserver.listener.actor.ai;

import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.listener.AiListener;
import l2r.gameserver.model.Creature;

public interface OnAiIntentionListener extends AiListener
{
	public void onAiIntention(Creature actor, CtrlIntention intention, Object arg0, Object arg1);
}
