package ai.freya;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SkillTable;

public class Glacier extends Fighter
{
	private long _buffTimer = 0;

	public Glacier(NpcInstance actor)
	{
		super(actor);
		actor.block();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		getActor().setNpcState(1);
		ThreadPoolManager.getInstance().schedule(new Freeze(), 800);
		ThreadPoolManager.getInstance().schedule(new Despawn(), 120000L);
		setIntention(CtrlIntention.AI_INTENTION_ATTACK);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		for(Creature cha : getActor().getAroundCharacters(350, 100))
			if(cha.isPlayer())
				cha.altOnMagicUseTimer(cha, SkillTable.getInstance().getInfo(6301, 1));

		super.onEvtDead(killer);
	}

	@Override
	protected void thinkAttack()
	{
		// Cold Air buff
		if(_buffTimer + 5000L < System.currentTimeMillis())
		{
			_buffTimer = System.currentTimeMillis();
			for(Creature cha : _actor.getAroundCharacters(200, 100))
				if(cha.isPlayer() && Rnd.chance(30))
					cha.altOnMagicUseTimer(cha, SkillTable.getInstance().getInfo(6302, 1));
		}
		super.thinkAttack();
	}

	private class Freeze extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			getActor().setNpcState(2);
		}
	}

	private class Despawn extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			getActor().deleteMe();
		}
	}
}