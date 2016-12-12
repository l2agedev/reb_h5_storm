package ai.hellbound;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;


public class Leodas extends Fighter
{
	public Leodas(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		ZoneHolder.getDoor(19250003).openMe();
		ZoneHolder.getDoor(19250004).openMe();
		ThreadPoolManager.getInstance().schedule(new CloseDoor(), 60 * 1000L);
		super.onEvtDead(killer);
	}

	private class CloseDoor extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			ZoneHolder.getDoor(19250003).closeMe();
			ZoneHolder.getDoor(19250004).closeMe();
		}
	}
}