package l2r.gameserver.model.reward;

import l2r.commons.math.SafeMath;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RewardGroup implements Cloneable
{
	private double _chance;
	private boolean _isAdena = false; // Шанс фиксирован, растет только количество
	private boolean _isAncientAdena = false; // Шанс фиксирован, растет только количество
	private boolean _notRate = false; // Рейты вообще не применяются
	private List<RewardItem> _items = new ArrayList<RewardItem>();
	private double _chanceSum;

	public RewardGroup(double chance)
	{
		if(chance > RewardList.MAX_CHANCE)
			chance = RewardList.MAX_CHANCE;
		setChance(chance);
	}

	public boolean notRate()
	{
		return _notRate;
	}

	public void setNotRate(boolean notRate)
	{
		_notRate = notRate;
	}
	
	public double getChance()
	{
		return _chance;
	}

	public void setChance(double chance)
	{
		_chance = chance;
	}

	public boolean isAdena()
	{
		return _isAdena;
	}
	
	public void setIsAdena(boolean isAdena)
	{
		_isAdena = isAdena;
	}
	
	public boolean isAncientAdena()
	{
		return _isAncientAdena;
	}
	
	public void setIsAncientAdena(boolean isAdena)
	{
		_isAncientAdena = isAdena;
	}
	
	public void addData(RewardItem item)
	{
		if(item.getItem().isAdena())
			setIsAdena(true);
		else if (item.getItem().isAncientAdena())
			setIsAncientAdena(true);
		
		_chanceSum += item.getChance();
		item.setChanceInGroup(_chanceSum);
		_items.add(item);
	}

	/**
	 * Возвращает список вещей
	 */
	public List<RewardItem> getItems()
	{
		return _items;
	}

	/**
	 * Возвращает полностью независимую копию группы
	 */
	@Override
	public RewardGroup clone()
	{
		RewardGroup ret = new RewardGroup(_chance);
		for(RewardItem i : _items)
			ret.addData(i.clone());
		return ret;
	}

	/**
	 * Функция используется в основном механизме расчета дропа, выбирает одну/несколько вещей из группы, в зависимости от рейтов
	 * 
	 */
	public List<RewardItemResult> roll(RewardType type, Player player, double mod, boolean isRaid, boolean isBoss, boolean isSiegeGuard)
	{
		double adenarate = 0;
		switch(type)
		{
			case NOT_RATED_GROUPED:
			case NOT_RATED_NOT_GROUPED:
				return rollItems(mod, 1.0, 1.0);
			case SWEEP:
				return rollItems(mod, Config.RATE_DROP_SPOIL, player.getRateSpoil());
			case RATED_GROUPED:
				if(_isAdena)
					if (!Config.ADENA_DROP_RATE_BY_LEVEL.isEmpty())
					{
						int level = 0;
						
						// prevent abuse from party by getting the biggest lvl player in the pt
						if (player.getParty() != null)
						{
							for (Player partyMember : player.getParty())
							{
								if (partyMember.getLevel() > level)
									level = partyMember.getLevel();
							}
						}
						else
							level = player.getLevel();
						
						// if the level is not in the config, then take the adena rate
						if (Config.ADENA_DROP_RATE_BY_LEVEL.get(level) > 0)
							adenarate = Config.ADENA_DROP_RATE_BY_LEVEL.get(level);
						else
							adenarate = Config.RATE_DROP_ADENA;
						
						return rollAdena(mod, adenarate, player.getRateAdena());
					}
					else
						return rollAdena(mod, Config.RATE_DROP_ADENA, player.getRateAdena());

				if (_isAncientAdena)
					return rollAdena(mod, Config.RATE_DROP_AA_ADENA, player.getRateAdena());
				
				if(isRaid && !isBoss)
					return rollItems(mod, Config.RATE_DROP_RAIDBOSS, 1.0);
				
				if(isRaid && isBoss)
					return rollItems(mod, 1.0, 1.0);
				
				if(isSiegeGuard)
					return rollItems(mod, Config.RATE_DROP_SIEGE_GUARD, 1.0);
				
				return rollItems(mod, Config.RATE_DROP_ITEMS, player.getRateItems());
			default:
				return Collections.emptyList();
		}
	}

	public List<RewardItemResult> rollItems(double mod, double baseRate, double playerRate)
	{
		if(mod <= 0)
			return Collections.emptyList();

		double rate;
		if(_notRate)
			rate = Math.min(mod, 1.0);
		else
			rate = baseRate * playerRate * mod;

		double mult = Math.ceil(rate);

		List<RewardItemResult> ret = new ArrayList<RewardItemResult>((int) (mult * _items.size()));
		for(long n = 0; n < mult; n++)
			if(Rnd.get(1, RewardList.MAX_CHANCE) <= _chance * Math.min(rate - n, 1.0))
				rollFinal(_items, ret, 1., Math.max(_chanceSum, RewardList.MAX_CHANCE));
		return ret;
	}

	private List<RewardItemResult> rollAdena(double mod, double baseRate, double playerRate)
	{
		double chance = _chance;
		if(mod > 10)
		{
			mod *= _chance / RewardList.MAX_CHANCE;
			chance = RewardList.MAX_CHANCE;
		}

		if(mod <= 0)
			return Collections.emptyList();

		if(Rnd.get(1, RewardList.MAX_CHANCE) > chance)
			return Collections.emptyList();

		double rate = baseRate * playerRate * mod;

		List<RewardItemResult> ret = new ArrayList<RewardItemResult>(_items.size());
		rollFinal(_items, ret, rate, Math.max(_chanceSum, RewardList.MAX_CHANCE));
		for(RewardItemResult i : ret)
			i.isAdena = true;

		return ret;
	}

	private void rollFinal(List<RewardItem> items, List<RewardItemResult> ret, double mult, double chanceSum)
	{
		// перебираем все вещи в группе и проверяем шанс
		int chance = Rnd.get(0, (int) chanceSum);
		long count;

		for(RewardItem i : items)
		{
			if(chance < i.getChanceInGroup() && chance > i.getChanceInGroup() - i.getChance())
			{
				double imult = i.notRate() ? 1.0 : mult;
				
				if(i.getMinDrop() >= i.getMaxDrop())
					count = Math.round(i.getMinDrop() * imult);
				else
					count = Rnd.get(Math.round(i.getMinDrop() * imult), Math.round(i.getMaxDrop() * imult));

				RewardItemResult t = null;

				for(RewardItemResult r : ret)
					if(i.getItemId() == r.itemId)
					{
						t = r;
						break;
					}

				if(t == null)
				{
					ret.add(t = new RewardItemResult(i.getItemId()));
					t.count = count;
				}
				else if(!i.notRate())
				{
					t.count = SafeMath.addAndLimit(t.count, count);
				}

				break;
			}
		}
	}
}