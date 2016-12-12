package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.PlayerListener;

public interface OnAnswerListener extends PlayerListener
{
	void sayYes();

	void sayNo();
}
