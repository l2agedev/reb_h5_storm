package l2r.gameserver.data.xml.parser;

import l2r.commons.data.xml.AbstractFileParser;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.EnchantItemHolder;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.item.support.EnchantScroll;
import l2r.gameserver.templates.item.support.EnchantType;
import l2r.gameserver.templates.item.support.FailResultType;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

/**
 * @author VISTALL
 * @date 3:10/18.06.2011
 */
public class EnchantItemParser extends AbstractFileParser<EnchantItemHolder>
{
	private static EnchantItemParser _instance = new EnchantItemParser();

	public static EnchantItemParser getInstance()
	{
		return _instance;
	}

	private EnchantItemParser()
	{
		super(EnchantItemHolder.getInstance());
	}

	@Override
	public File getXMLFile()
	{
		return Config.getFile("config/enchant_items.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "enchant_items.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		int defaultMaxEnchant = 0;
		int defaultChance = 0;
		int defaultMagicChance = 0;
		int defaultMinEcnhEff = 0;
	    int defaultMaxEcnhEff = 0;
		int defaultSafeLevel = 3;
		int defaultSafeLevelFullArmor = 4;
		boolean defaultVisualEffect = false;

		Element defaultElement = rootElement.element("default");
		if(defaultElement != null)
		{
			defaultMaxEnchant = Integer.parseInt(defaultElement.attributeValue("max_enchant"));
			defaultChance = Integer.parseInt(defaultElement.attributeValue("chance"));
			defaultMagicChance = Integer.parseInt(defaultElement.attributeValue("magic_chance"));
			defaultMinEcnhEff = Integer.parseInt(defaultElement.attributeValue("succ_eff_ench_min"));
		    defaultMaxEcnhEff = Integer.parseInt(defaultElement.attributeValue("succ_eff_ench_max"));
			defaultVisualEffect = Boolean.parseBoolean(defaultElement.attributeValue("visual_effect"));
			defaultSafeLevel = Integer.parseInt(defaultElement.attributeValue("safe_level"));
			defaultSafeLevelFullArmor = Integer.parseInt(defaultElement.attributeValue("safe_level_full_armor"));
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("enchant_scroll"); iterator.hasNext();)
		{
			Element enchantItemElement = iterator.next();
			int itemId = Integer.parseInt(enchantItemElement.attributeValue("id"));
			int chance = enchantItemElement.attributeValue("chance") == null ? defaultChance : Integer.parseInt(enchantItemElement.attributeValue("chance"));

			int magicChance = enchantItemElement.attributeValue("magic_chance") == null ? defaultMagicChance : Integer.parseInt(enchantItemElement.attributeValue("magic_chance"));
			int maxEnchant = enchantItemElement.attributeValue("max_enchant") == null ? defaultMaxEnchant : Integer.parseInt(enchantItemElement.attributeValue("max_enchant"));
			FailResultType resultType = FailResultType.valueOf(enchantItemElement.attributeValue("on_fail"));
			EnchantType enchantType = enchantItemElement.attributeValue("type") == null ? EnchantType.ALL : EnchantType.valueOf(enchantItemElement.attributeValue("type"));
			ItemTemplate.Grade grade = enchantItemElement.attributeValue("grade") == null ? ItemTemplate.Grade.NONE : ItemTemplate.Grade.valueOf(enchantItemElement.attributeValue("grade"));
			int minEcnhEff = enchantItemElement.attributeValue("succ_eff_ench_min") == null ? defaultMinEcnhEff : Integer.parseInt(enchantItemElement.attributeValue("succ_eff_ench_min"));
		    int maxEcnhEff = enchantItemElement.attributeValue("succ_eff_ench_max") == null ? defaultMaxEcnhEff : Integer.parseInt(enchantItemElement.attributeValue("succ_eff_ench_max"));
			boolean visualEffect = enchantItemElement.attributeValue("visual_effect") == null ? defaultVisualEffect : Boolean.parseBoolean(enchantItemElement.attributeValue("visual_effect"));
			int safe_level = enchantItemElement.attributeValue("safe_level") == null ? defaultSafeLevel : Integer.parseInt(enchantItemElement.attributeValue("safe_level"));
			int safe_level_full_armor = enchantItemElement.attributeValue("safe_level_full_armor") == null ? defaultSafeLevelFullArmor : Integer.parseInt(enchantItemElement.attributeValue("safe_level_full_armor"));
			
			EnchantScroll item = new EnchantScroll(itemId, chance, magicChance, maxEnchant, enchantType, grade, resultType, minEcnhEff, maxEcnhEff, visualEffect, safe_level, safe_level_full_armor);
			
			getHolder().addEnchantScroll(item);

			for(Iterator<Element> iterator2 = enchantItemElement.elementIterator(); iterator2.hasNext();)
			{
				Element element2 = iterator2.next();
				if(element2.getName().equals("item_list"))
				{
					for(Element e : element2.elements())
						item.addItemId(Integer.parseInt(e.attributeValue("id")));
				}
				else if(element2.getName().equals("chance_per_level"))
				{
					for(Element e : element2.elements())
					{
						int level = Integer.parseInt(e.attributeValue("level"));
						int chancePerLvl = Integer.parseInt(e.attributeValue("chance"));
						item.addEnchantChance(level, chancePerLvl);
					}
				}
				else
				{
					info("Not supported for now.2");
				}
			}
		}
	}
}
