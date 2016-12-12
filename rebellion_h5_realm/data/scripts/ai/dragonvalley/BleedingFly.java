package ai.dragonvalley;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.utils.Location;

public class BleedingFly extends DragonRaid
{
	private int _spawnCount = 0;

	public BleedingFly(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void thinkAttack()
	{
		if(_spawnCount == 0 && _actor.getCurrentHpPercents() < 50)
		{
			_spawnCount++;
			spawnMinions();
		}
		else if(_spawnCount == 1 && _actor.getCurrentHpPercents() < 25)
		{
			_spawnCount++;
			spawnMinions();
			_actor.doCast(SkillTable.getInstance().getInfo(6915, 3), _actor, false);
		}
		super.thinkAttack();
	}

	private void spawnMinions()
	{
		int count = 1 + Rnd.get(1, 3);
		_actor.doCast(SkillTable.getInstance().getInfo(6832, 1), _actor, false);
		for(int i = 0; i < count; i++)
		{
			NpcInstance minion = SpawnTable.spawnSingle(25734, Location.findPointToStay(_actor, 250));
			minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 5000);
		}
	}

	@Override
	public int getRateDEBUFF()
	{
		return 5;
	}

	@Override
	public int getRateDAM()
	{
		return 80;
	}

}