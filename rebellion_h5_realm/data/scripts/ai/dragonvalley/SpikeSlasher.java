package ai.dragonvalley;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SpawnTable;

/**
 * @author pchayka
 */
public class SpikeSlasher extends DragonRaid
{
	private int _spawnCount = 0;

	public SpikeSlasher(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void thinkAttack()
	{
		if(_spawnCount == 0 && _actor.getCurrentHpPercents() < 60)
		{
			_spawnCount++;
			spawnMinions();
		}
		else if(_spawnCount == 1 && _actor.getCurrentHpPercents() < 20)
		{
			_spawnCount++;
			spawnMinions();
		}
		super.thinkAttack();
	}

	private void spawnMinions()
	{
		int count = 1 + Rnd.get(1, 3);
		_actor.doCast(SkillTable.getInstance().getInfo(6841, 1), _actor, false);
		for(int i = 0; i < count; i++)
		{
			NpcInstance minion = SpawnTable.spawnSingle(25733, _actor.getLoc().findPointToStay(250));
			minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 5000);
		}
	}

}