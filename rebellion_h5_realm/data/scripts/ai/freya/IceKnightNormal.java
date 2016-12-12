package ai.freya;

import java.util.concurrent.ScheduledFuture;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.NpcInstance;

public class IceKnightNormal extends Fighter
{
	private boolean iced;
	private ScheduledFuture<?> task;

	public IceKnightNormal(NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = 6000;
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		NpcInstance actor = getActor();
		iced = true;
		actor.setNpcState(1);
		actor.block();
		Reflection r = actor.getReflection();
		if(r != null && r.getPlayers() != null)
			for(Player p : r.getPlayers())
				this.notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 300);

		task = ThreadPoolManager.getInstance().schedule(new ReleaseFromIce(), 6000L);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();

		if(iced)
		{
			iced = false;
			if(task != null)
				task.cancel(false);
			actor.unblock();
			actor.setNpcState(2);
		}
		super.onEvtAttacked(attacker, damage);
	}

	private class ReleaseFromIce extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(iced)
			{
				iced = false;
				getActor().setNpcState(2);
				getActor().unblock();
			}
		}
	}

	@Override
	protected void teleportHome()
	{
	}
}