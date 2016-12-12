package ai.dragonvalley;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;

public class ShadowSummoner extends DragonRaid
{
	private long _lastSpawnTime = 0;

	public ShadowSummoner(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void thinkAttack()
	{
		if(_actor.getCurrentHpPercents() < 50)
		{
			if(_lastSpawnTime + 120 * 1000 < System.currentTimeMillis())
			{
				_lastSpawnTime = System.currentTimeMillis();
				NpcInstance minion = SpawnTable.spawnSingle(25731, _actor.getLoc().findPointToStay(250));
				minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 5000);
			}
		}
		super.thinkAttack();
	}

	@Override
	public int getRateDEBUFF()
	{
		return 15;
	}

	@Override
	public int getRateDAM()
	{
		return 50;
	}

}