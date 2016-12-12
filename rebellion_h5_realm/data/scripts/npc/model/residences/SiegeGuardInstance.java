package npc.model.residences;

import java.util.List;
import java.util.Map;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.entity.events.impl.SiegeEvent;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.reward.RewardItemResult;
import l2r.gameserver.model.reward.RewardList;
import l2r.gameserver.model.reward.RewardType;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.npc.NpcTemplate;

public class SiegeGuardInstance extends NpcInstance
{
	public SiegeGuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	@Override
	public boolean isSiegeGuard()
	{
		return true;
	}

	@Override
	public int getAggroRange()
	{
		return 1200;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null)
			return false;
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		SiegeEvent siegeEvent2 = attacker.getEvent(SiegeEvent.class);
		Clan clan = player.getClan();
		if(siegeEvent == null)
			return false;
		if(clan != null && siegeEvent == siegeEvent2 && siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, clan) != null)
			return false;
		return true;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		if(killer != null)
		{
			Player player = killer.getPlayer();
			if(siegeEvent != null && player != null)
			{
				Clan clan = player.getClan();
				SiegeEvent siegeEvent2 = killer.getEvent(SiegeEvent.class);
				if(clan != null && siegeEvent == siegeEvent2 && siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, clan) == null)
				{
					Creature topdam = getAggroList().getTopDamager();
					if(topdam == null)
						topdam = killer;

					for(Map.Entry<RewardType, RewardList> entry : getTemplate().getRewards().entrySet())
						rollRewards(entry, killer, topdam);
				}
			}
		}
		super.onDeath(killer);
	}

	public void rollRewards(Map.Entry<RewardType, RewardList> entry, final Creature lastAttacker, Creature topDamager)
	{
		RewardList list = entry.getValue();

		final Player activePlayer = topDamager.getPlayer();

		if(activePlayer == null)
			return;

		final int diff = calculateLevelDiffForDrop(topDamager.getLevel());
		double mod = calcStat(Stats.REWARD_MULTIPLIER, 1., topDamager, null);
		mod *= Experience.penaltyModifier(diff, 9);
		
		List<RewardItemResult> rewardItems = list.roll(activePlayer, mod, false, false, true);

		for(RewardItemResult drop : rewardItems)
			dropItem(activePlayer, drop.itemId, drop.count);
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}