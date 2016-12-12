package l2r.gameserver.nexus_engine.events.engine.main.globalevent.townrush;

import l2r.commons.util.Rnd;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.GlobalEvent;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.townrush.towns.AdenTown;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.townrush.towns.TownTemplate;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.NpcData;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastTable;

/**
 * @author hNoke
 */
public class TownRushEvent extends GlobalEvent
{
	private final List<TownTemplate> _data = new FastTable<TownTemplate>();

	private ScheduledFuture<?> _nextClock = null;

	public TownRushEvent()
	{
		_data.add(new AdenTown());
	}
	
	private TownTemplate _currentTown;

	@Override
	public String getName()
	{
		return "TownRush";
	}

	@Override
	public boolean canStart(String param)
	{
//		for(Siege siege : SiegeManager.getInstance().getSieges())
//		{
//			if(siege.getIsInProgress())
//				return false;
//		}

		return false;
	}

	private TownTemplate getRandomTown()
	{
		return _data.get(Rnd.get(_data.size())); //TODO first index 0 ?
	}

	@Override
	public void start(String param)
	{
		TownTemplate town = getRandomTown();
		_currentTown = town;

		startClock();

		spawnAll();
		
		//_raidboss = boss.doSpawn();
		//_raidboss.setGlobalEvent(this);
		
		announce("Town rush started in " + town.getName());
	}

	@Override
	public void end()
	{
		stopClock();

		despawnAll();

		/*if(_raidboss == null)
			return;

		if(!_raidboss.isDead())
		{
			_raidboss.deleteMe();
		}
		else if(_raidboss.isDead())
		{
			for(L2PcInstance player : _raidboss.getOwner().getKnownList().getKnownPlayersInRadius(30000))
			{
				_currentBossData.rewardPlayer(player);
			}
		}*/
		
		announce("Global raidboss event ended.");
	}

	private void spawnAll()
	{

	}

	private void despawnAll()
	{

	}

	public void clockTick()
	{
		try
		{

		}
		finally
		{
			startClock();
		}
	}

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

	public void bossDied()
	{
		
	}

	@Override
	public boolean canRegister(PlayerEventInfo player)
	{
		return true;
	}

	@Override
	public void monsterDies(NpcData npc)
	{
	}

	@Override
	public void addPlayer(PlayerEventInfo player)
	{
		if(_currentTown != null)
		{
			if(player.isDead())
			{
				player.doRevive();
			}
		}
	}

	@Override
	public String getStateNameForHtml()
	{
		return "Dangerous";
	}

	@Override
	public void reload() 
	{
		
	}
}
