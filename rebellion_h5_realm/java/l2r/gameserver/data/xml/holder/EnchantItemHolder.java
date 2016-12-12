package l2r.gameserver.data.xml.holder;

import l2r.commons.data.xml.AbstractHolder;
import l2r.gameserver.templates.item.support.EnchantScroll;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

/**
 * @author VISTALL
 * @date 3:10/18.06.2011
 */
public class EnchantItemHolder extends AbstractHolder
{
	private static EnchantItemHolder _instance = new EnchantItemHolder();

	private IntObjectMap<EnchantScroll> _enchantScrolls = new HashIntObjectMap<EnchantScroll>();

	public static EnchantItemHolder getInstance()
	{
		return _instance;
	}

	private EnchantItemHolder()
	{
	}

	public void addEnchantScroll(EnchantScroll enchantScroll)
	{
		_enchantScrolls.put(enchantScroll.getItemId(), enchantScroll);
	}

	public EnchantScroll getEnchantScroll(int id)
	{
		return _enchantScrolls.get(id);
	}

	public int[] getEnchantScrolls()
	{
		return _enchantScrolls.keySet().toArray();
	}

	@Override
	public void log()
	{
		info("load " + _enchantScrolls.size() + " enchant scroll(s).");
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{

	}
}
