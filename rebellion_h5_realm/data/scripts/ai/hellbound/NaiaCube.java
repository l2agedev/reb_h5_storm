package ai.hellbound;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.instancemanager.naia.NaiaCoreManager;
import l2r.gameserver.model.instances.NpcInstance;


public class NaiaCube extends DefaultAI
{

	public NaiaCube(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		ThreadPoolManager.getInstance().schedule(new Despawn(getActor()), 120 * 1000L);
	}

	private class Despawn extends RunnableImpl
	{
		NpcInstance _npc;

		private Despawn(NpcInstance npc)
		{
			_npc = npc;
		}

		@Override
		public void runImpl()
		{
			_npc.deleteMe();
			NaiaCoreManager.setZoneActive(false);
			ZoneHolder.getDoor(20240001).openMe(); // Beleth Door
			ZoneHolder.getDoor(18250025).openMe(); // Epidos Door
		}
	}
}