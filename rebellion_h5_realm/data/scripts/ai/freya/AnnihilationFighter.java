package ai.freya;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.utils.Location;
import l2r.gameserver.tables.SpawnTable;

public class AnnihilationFighter extends Fighter
{
	public AnnihilationFighter(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(Rnd.chance(5))
			SpawnTable.spawnSingle(18839, Location.findPointToStay(getActor(), 40, 120), getActor().getReflection()); // Maguen

		super.onEvtDead(killer);
	}

	@Override
	public boolean canSeeInSilentMove(Playable target)
	{
		return true;
	}

	@Override
	public boolean canSeeInHide(Playable target)
	{
		return true;
	}
}