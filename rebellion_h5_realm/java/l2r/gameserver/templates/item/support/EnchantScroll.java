package l2r.gameserver.templates.item.support;

import l2r.gameserver.templates.item.ItemTemplate;

public class EnchantScroll extends EnchantItem
{
	private final FailResultType _resultType;
	private final int _minEncVisEff;
	private final int _maxEncVisEff;
	private final boolean _visualEffect;
	
	public EnchantScroll(int itemId, int chance, int magicChance, int maxEnchant, EnchantType type, ItemTemplate.Grade grade, FailResultType resultType, int minEncVisEff, int maxEncVisEff, boolean visualEffect, int safeLevel, int safeLevelFull)
	{
		super(itemId, chance, magicChance, maxEnchant, type, grade, safeLevel, safeLevelFull);
		_resultType = resultType;
		_minEncVisEff = minEncVisEff;
		_maxEncVisEff = maxEncVisEff;
		_visualEffect = visualEffect;
	}
	
	public FailResultType getResultType()
	{
		return _resultType;
	}
	
	public boolean showSuccessEffect(int enchant)
	{
		return (enchant >= this._minEncVisEff) && (enchant <= this._maxEncVisEff);
	}
	
	public boolean isHasVisualEffect()
	{
		return _visualEffect;
	}
}
