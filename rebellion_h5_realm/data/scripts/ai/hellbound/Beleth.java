package ai.hellbound;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Mystic;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.utils.Location;

import bosses.BelethManager;

public class Beleth extends Mystic
{
	private long _lastFactionNotifyTime = 0;
	private static final int CLONE = 29119;
	private static final int BELETH = 29118;
	
	public Beleth(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if (Config.ENABLE_PLAYER_COUNTERS && getActor().getNpcId() == BELETH)
		{
			if (killer != null && killer.getPlayer() != null)
			{
				for (Player member : killer.getPlayer().getPlayerGroup())
				{
					if (member != null && Location.checkIfInRange(3000, member, killer, false))
						member.getCounters().addPoint("_Beleth_Killed");
				}
			}
		}
		
		BelethManager.setBelethDead();
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();

		if(System.currentTimeMillis() - _lastFactionNotifyTime > _minFactionNotifyInterval)
		{
			_lastFactionNotifyTime = System.currentTimeMillis();
			
			for(NpcInstance npc : World.getAroundNpc(actor))
				if(npc.getNpcId() == CLONE)
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
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

	@Override
	public void addTaskAttack(Creature target)
	{
		return;
	}

}