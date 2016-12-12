package ai.custom;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;

public class MutantChest extends Fighter
{
	public MutantChest(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(Rnd.chance(30))
			Functions.npcSay(actor, "Enemies! Enemies everywhere! Everyone come, the enemies are here!");

		actor.deleteMe();
	}
}