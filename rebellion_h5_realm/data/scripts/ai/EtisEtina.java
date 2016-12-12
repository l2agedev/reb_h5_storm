package ai;

import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.utils.Location;
import l2r.gameserver.tables.SpawnTable;

public class EtisEtina extends Fighter
{
	private boolean summonsReleased = false;
	private NpcInstance summon1;
	private NpcInstance summon2;

	public EtisEtina(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(actor.getCurrentHpPercents() < 70 && !summonsReleased)
		{
			summonsReleased = true;
			summon1 = SpawnTable.spawnSingle(18950, Location.findAroundPosition(actor, 150), actor.getReflection());
			summon2 = SpawnTable.spawnSingle(18951, Location.findAroundPosition(actor, 150), actor.getReflection());
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(summon1 != null && !summon1.isDead())
			summon1.decayMe();
		if(summon2 != null && !summon2.isDead())
			summon2.decayMe();
		super.onEvtDead(killer);
	}
}