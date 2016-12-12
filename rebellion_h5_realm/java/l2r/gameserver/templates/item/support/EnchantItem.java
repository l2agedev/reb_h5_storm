package l2r.gameserver.templates.item.support;

import l2r.gameserver.templates.item.ItemTemplate;

import gnu.trove.map.hash.TIntIntHashMap;

import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

public class EnchantItem
{
	private final int _itemId;
	private final int _chance;
	private final int _magicChance;
	private final int _maxEnchant;
	private final EnchantType _type;
	private final ItemTemplate.Grade _grade;
	private final int _safeLevel;
	private final int _safeLevelFullArmor;
	private IntSet _items = Containers.EMPTY_INT_SET;
	private TIntIntHashMap _enchantChancePerLevel;;
	
	public EnchantItem(int itemId, int chance, int magicChance, int maxEnchant, EnchantType type, ItemTemplate.Grade grade, int safeLevel, int safeLevelFull)
	{
		_itemId = itemId;
		_chance = chance;
		_magicChance = magicChance;
		_maxEnchant = maxEnchant;
		_type = type;
		_grade = grade;
		_safeLevel = safeLevel;
		_safeLevelFullArmor = safeLevelFull;
	}
	
	public void addItemId(int id)
	{
		if (_items.isEmpty())
			_items = new HashIntSet();
		
		_items.add(id);
	}
	
	public void addEnchantChance(int level, int chance)
	{
		if (_enchantChancePerLevel == null)
			_enchantChancePerLevel = new TIntIntHashMap();
		
		_enchantChancePerLevel.put(level, chance);
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getChance(boolean isMagic)
	{
		return getChance(-1, isMagic);
	}
	
	public int getChance(int level, boolean isMagic)
	{
		if (level > 0 && _enchantChancePerLevel != null && _enchantChancePerLevel.contains(level))
			return _enchantChancePerLevel.get(level);
		
		return isMagic ? _magicChance : _chance;
	}
	
	public int getMaxEnchant()
	{
		return _maxEnchant;
	}
	
	public ItemTemplate.Grade getGrade()
	{
		return _grade;
	}
	
	public IntSet getItems()
	{
		return _items;
	}
	
	public EnchantType getType()
	{
		return _type;
	}
	
	public int getSafeLevel()
	{
		return _safeLevel;
	}
	
	public int getSafeLevelFullArmor()
	{
		return _safeLevelFullArmor;
	}
}
