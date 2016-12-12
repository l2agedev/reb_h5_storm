package l2r.gameserver.model.reward;

import l2r.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @reworked VISTALL
 */
@SuppressWarnings("serial")
public class RewardList extends ArrayList<RewardGroup>
{
	public static final int MAX_CHANCE = 1000000;
	private final RewardType _type;
	private final boolean _autoLoot;

	public RewardList(RewardType rewardType, boolean a)
	{
		super(5);
		_type = rewardType;
		_autoLoot = a;
	}

	public List<RewardItemResult> roll(Player player)
	{
		return roll(player, 1.0, false, false, false);
	}
	
	public List<RewardItemResult> roll(Player player, double mod)
	{
		return roll(player, mod, false, false, false);
	}
	
	public List<RewardItemResult> roll(Player player, double mod, boolean isRaid, boolean isBoss)
	{
		return roll(player, mod, isRaid, isBoss, false);
	}
	
	public List<RewardItemResult> roll(Player player, double mod, boolean isRaid, boolean isBoss, boolean isSiegeGuard)
	{
		List<RewardItemResult> temp = new ArrayList<RewardItemResult>(size());
		for(RewardGroup g : this)
		{
			List<RewardItemResult> tdl = g.roll(_type, player, mod, isRaid, isBoss, isSiegeGuard);
			if(!tdl.isEmpty())
				for(RewardItemResult itd : tdl)
					temp.add(itd);
		}
		return temp;
	}

	public boolean validate()
	{
		for(RewardGroup g : this)
		{
			int chanceSum = 0; // сумма шансов группы
			for(RewardItem d : g.getItems())
				chanceSum += d.getChance();
			if(chanceSum <= MAX_CHANCE) // всё в порядке?
				return true;
			double mod = MAX_CHANCE / chanceSum;
			for(RewardItem d : g.getItems())
			{
				double chance = d.getChance() * mod; // коррекция шанса группы
				if (chance == 0)
					chance = MAX_CHANCE; // In case if mod is 0
				d.setChance(chance);
				g.setChance(MAX_CHANCE);
			}
		}
		return false;
	}

	public boolean isAutoLoot()
	{
		return _autoLoot;
	}

	public RewardType getType()
	{
		return _type;
	}
}