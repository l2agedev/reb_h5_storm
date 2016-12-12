package ai;

import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;

public class PrizeLuckyPig extends DefaultAI
{
	
	public PrizeLuckyPig(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		getActor().doDie(attacker);
	}
}
