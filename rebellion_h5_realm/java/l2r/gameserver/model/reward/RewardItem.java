package l2r.gameserver.model.reward;

import l2r.commons.math.SafeMath;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class RewardItem implements Cloneable
{
	private ItemTemplate _item;
	private boolean _notRate = false; // Рейты к вещи не применяются
	
	private long _mindrop;
	private long _maxdrop;
	private double _chance;
	private double _chanceInGroup;
	
	public RewardItem(int itemId)
	{
		_item = ItemHolder.getInstance().getTemplate(itemId);
		if(_item.isArrow() // стрелы не рейтуются
				|| (Config.NO_RATE_EQUIPMENT && _item.isEquipment()) // отключаемая рейтовка эквипа
				|| (Config.NO_RATE_KEY_MATERIAL && _item.isKeyMatherial()) // отключаемая рейтовка ключевых материалов
				|| (Config.NO_RATE_RECIPES && _item.isRecipe()) // отключаемая рейтовка рецептов
				|| ArrayUtils.contains(Config.NO_RATE_ITEMS, itemId) // индивидаульная отключаемая рейтовка для списка предметов
				|| (Config.NO_RATE_HERBS && _item.isHerb()) // отключаемая рейтовка хербов
				|| (Config.NO_RATE_ENCHANT_SCROLL && _item.isEnchantScroll())
				|| (Config.NO_RATE_ATT && _item.isAttributeCrystal())
				|| (Config.NO_RATE_ATT && _item.isAttributeJewel())
				|| (Config.NO_RATE_ATT && _item.isAttributeStone())
				|| (Config.NO_RATE_ATT && _item.isAttributeEnergy())
				|| (Config.NO_RATE_LIFE_STONE && _item.isLifeStone())
				|| (Config.NO_RATE_CODEX_BOOK && _item.isCodexBook())
				|| (Config.NO_RATE_FORGOTTEN_SCROLL && _item.isForgottenScroll()))
			_notRate = true;
	}

	public RewardItem(int itemId, long min, long max, double chance)
	{
		this(itemId);
		_mindrop = min;
		_maxdrop = max;
		_chance = chance;
	}

	public boolean notRate()
	{
		return _notRate;
	}
	
	public void setNotRate(boolean notRate)
	{
		_notRate = notRate;
	}

	public int getItemId()
	{
		return _item.getItemId();
	}

	public ItemTemplate getItem()
	{
		return _item;
	}

	public long getMinDrop()
	{
		return _mindrop;
	}

	public long getMaxDrop()
	{
		return _maxdrop;
	}

	public double getChance()
	{
		return _chance;
	}

	public void setMinDrop(long mindrop)
	{
		_mindrop = mindrop;
	}

	public void setMaxDrop(long maxdrop)
	{
		_maxdrop = maxdrop;
	}

	public void setChance(double chance)
	{
		_chance = chance;
	}

	public void setChanceInGroup(double chance)
	{
		_chanceInGroup = chance;
	}

	public double getChanceInGroup()
	{
		return _chanceInGroup;
	}

	@Override
	public String toString()
	{
		return "ItemID: " + getItem() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0 + "%";
	}

	@Override
	public RewardItem clone()
	{
		return new RewardItem(getItemId(), getMinDrop(), getMaxDrop(), getChance());
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof RewardItem)
		{
			RewardItem drop = (RewardItem) o;
			return drop.getItemId() == getItemId();
		}
		return false;
	}
	
	/**
	 * 
	 * @param player used to get the bonus drop rate or <b>null</b> to avoid it.
	 * @return Config-specific rate of this item.
	 */
	public double getRate(Player player)
	{
		double rate = 1.0;
		double adenaRate = player != null ? player.getRateAdena() : 1.0;
		double itemRate = player != null ? player.getRateItems() : 1.0;
		if(_item.isAdena())
			rate = Config.RATE_DROP_ADENA * adenaRate;
		else if (_item.isAncientAdena())
			rate = Config.RATE_DROP_AA_ADENA * adenaRate;
		else
			rate = Config.RATE_DROP_ITEMS * itemRate;
		
		rate *= PremiumAccountsTable.getDropBonus(player, _item.getItemId());
		
		return rate;
	}

	/**
	 * Rolls the chance of this specific item.
	 * @param player used to get bonus drop for this specific player. Use <b>null</b> to avoid player bonus.
	 * @param rateMultiplier multiplies the default (config + player bonus) rate.
	 * @return List containing 1 item, or empty if roll failed.
	 */
	public List<RewardItemResult> roll(Player player, double rateMultiplier)
	{
		double rate = getRate(player);
		return roll(rate * rateMultiplier);
	}
	
	/**
	 * Rolls the chance of this specific item.
	 * @param rate the amount of times it will roll the chance. If the rate is 2.5, it will roll 2 times and once with 50% less chance.
	 * <br> For every successful roll, count will be increased by minDrop-maxDrop.
	 * @return List containing 1 item, or empty if roll failed.
	 */
	public List<RewardItemResult> roll(double rate)
	{
		double mult = Math.ceil(rate);

		List<RewardItemResult> ret = new ArrayList<RewardItemResult>(1);
		RewardItemResult t = null;
		long count;
		for(int n = 0; n < mult; n++)
		{
			if(Rnd.get(RewardList.MAX_CHANCE) <= _chance * Math.min(rate - n, 1.0))
			{
				if(getMinDrop() >= getMaxDrop())
					count = getMinDrop();
				else
					count = Rnd.get(getMinDrop(), getMaxDrop());

				if(t == null)
				{
					ret.add(t = new RewardItemResult(_item.getItemId()));
					t.count = count;
				}
				else
					t.count = SafeMath.addAndLimit(t.count, count);
			}
		}

		return ret;
	}
}