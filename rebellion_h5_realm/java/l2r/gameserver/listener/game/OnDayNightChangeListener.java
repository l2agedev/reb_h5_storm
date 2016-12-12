package l2r.gameserver.listener.game;

import l2r.gameserver.listener.GameListener;

public interface OnDayNightChangeListener extends GameListener
{
	public void onDay();

	public void onNight();
}
