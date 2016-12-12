package ai.hellbound;

import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Location;

public class SteelCitadelKeymaster extends Fighter
{
	private boolean _firstTimeAttacked = true;
	private static final int AMASKARI_ID = 22449;

	public SteelCitadelKeymaster(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;

		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			Functions.npcSay(actor, "You have done well in finding me, but I cannot just hand you the key!");
			for(NpcInstance npc : World.getAroundNpc(actor))
				if(npc.getNpcId() == AMASKARI_ID && npc.getReflectionId() == actor.getReflectionId() && !npc.isDead())
				{
					npc.teleToLocation(Location.findPointToStay(actor, 150, 200));
					break;
				}
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}