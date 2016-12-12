package l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.bosses;

import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.RaidbossEvent;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.NpcData;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public abstract class BossTemplate
{
	public BossTemplate()
	{
	}

	List<BossRewardItem> _rewards = new FastTable<>();
	
	ScheduledFuture<?> _nextClock = null;

	public void startClock()
	{
		_nextClock = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				clockTick();
			}
		}, 5000);
	}
	
	public void stopClock()
	{
		if(_nextClock != null)
		{
			_nextClock.cancel(false);
			_nextClock = null;
		}
	}
	
	public void monsterDied(RaidbossEvent event, NpcData npc)
	{
		if(npc != null && npc.getNpcId() == getBossId())
			stopClock();
	}

	public void clockTick()
	{
		startClock();
	}
	
	public abstract int getBossId();

	public abstract int getChance();

	public abstract String getName();
	
	public abstract EventSpawn getBossSpawn();
	public abstract EventSpawn getPlayersSpawn();
	
	public abstract void rewardPlayer(PlayerEventInfo player);
	
	public abstract NpcData doSpawn();
}
