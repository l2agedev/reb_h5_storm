package l2r.gameserver.ai;

import l2r.commons.util.Rnd;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.impl.SiegeEvent;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.model.instances.NpcInstance;

public class DoorAI extends CharacterAI
{
	public DoorAI(DoorInstance actor)
	{
		super(actor);
	}

	@Override
	protected void notifyEvent(CtrlEvent evt, Object[] args)
	{
		Creature actor = getActor();
		if(actor == null || !actor.isVisible())
			return;

		super.notifyEvent(evt, args);

		switch(evt)
		{
			case EVT_DBLCLICK:
				onEvtTwiceClick((Player) args[0]);
				break;
			case EVT_OPEN:
				onEvtOpen((Player) args[0]);
				break;
			case EVT_CLOSE:
				onEvtClose((Player) args[0]);
				break;
		}
	}

	protected void onEvtTwiceClick(Player player)
	{
		//
	}

	protected void onEvtOpen(Player player)
	{
		//
	}

	protected void onEvtClose(Player player)
	{
		//
	}

	@Override
	public DoorInstance getActor()
	{
		return (DoorInstance)super.getActor();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		Creature actor;
		if(attacker == null || (actor = getActor()) == null)
			return;

		Player player = attacker.getPlayer();
		if(player == null)
			return;

		SiegeEvent siegeEvent1 = player.getEvent(SiegeEvent.class);
		SiegeEvent siegeEvent2 = actor.getEvent(SiegeEvent.class);

		if(siegeEvent1 == null || siegeEvent1 == siegeEvent2 && siegeEvent1.getSiegeClan(SiegeEvent.ATTACKERS, player.getClan()) != null)
			for(NpcInstance npc : actor.getAroundNpc(900, 200))
			{
				if(!npc.isSiegeGuard())
					continue;

				if(Rnd.chance(20))
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 10000);
				else
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2000);
			}
	}
	
	@Override
	public boolean isDoorAI()
	{
		return true;
	}
}