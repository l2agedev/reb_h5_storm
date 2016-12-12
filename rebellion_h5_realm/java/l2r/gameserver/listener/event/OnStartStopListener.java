package l2r.gameserver.listener.event;

import l2r.gameserver.listener.EventListener;
import l2r.gameserver.model.entity.events.GlobalEvent;

public interface OnStartStopListener extends EventListener
{
	void onStart(GlobalEvent event);

	void onStop(GlobalEvent event);
}
