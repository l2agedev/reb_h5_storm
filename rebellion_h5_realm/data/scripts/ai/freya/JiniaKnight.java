package ai.freya;

import java.util.List;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;

public class JiniaKnight extends Fighter
{
	public JiniaKnight(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return false;

		List<NpcInstance> around = actor.getAroundNpc(4000, 300);
		if(around != null && !around.isEmpty())
			for(NpcInstance npc : around)
				if(npc.getNpcId() == 22767)
					actor.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, 300);
		return true;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(attacker == null || attacker.isPlayable())
			return;

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	public boolean checkAggression(Creature target)
	{
		if(target.isPlayable())
			return false;

		return super.checkAggression(target);
	}
}