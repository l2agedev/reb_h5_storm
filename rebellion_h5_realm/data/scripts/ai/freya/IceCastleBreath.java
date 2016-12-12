package ai.freya;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.NpcInstance;

public class IceCastleBreath extends Fighter
{
	public IceCastleBreath(NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = 6000;
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		Reflection r = getActor().getReflection();
		if(r != null && r.getPlayers() != null)
			for(Player p : r.getPlayers())
				this.notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 5);
	}

	@Override
	protected void teleportHome()
	{
	}
}