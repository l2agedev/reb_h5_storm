package l2r.gameserver.nexus_engine.events.engine.main.base;

import l2r.gameserver.nexus_engine.events.engine.main.events.AbstractMainEvent.Clock;
import l2r.gameserver.nexus_interface.delegate.InstanceData;

import java.util.concurrent.ScheduledFuture;

/**
 * @author hNoke
 *
 */
public interface IEventInstance
{
	public InstanceData getInstance();
	public ScheduledFuture<?> scheduleNextTask(int time);
	public Clock getClock();
	public boolean isActive();
}
