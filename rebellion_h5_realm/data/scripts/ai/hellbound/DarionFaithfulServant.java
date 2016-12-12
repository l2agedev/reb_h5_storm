package ai.hellbound;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.utils.Location;

public class DarionFaithfulServant extends Fighter
{
	private static final int MysteriousAgent = 32372;

	public DarionFaithfulServant(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(Rnd.chance(15))
			try
			{
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(MysteriousAgent));
				sp.setLoc(new Location(-11984, 278880, -13599, -4472));
				sp.doSpawn(true);
				sp.stopRespawn();
				ThreadPoolManager.getInstance().schedule(new Unspawn(), 600 * 1000L); // 10 mins
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		super.onEvtDead(killer);
	}

	private class Unspawn extends RunnableImpl
	{
		public Unspawn()
		{}

		@Override
		public void runImpl()
		{
			for(NpcInstance npc : GameObjectsStorage.getAllByNpcId(MysteriousAgent, true))
				npc.deleteMe();
		}
	}

}