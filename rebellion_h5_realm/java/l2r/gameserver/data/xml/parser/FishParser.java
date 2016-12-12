package l2r.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import l2r.commons.collections.MultiValueSet;
import l2r.commons.data.xml.AbstractFileParser;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.FishDataHolder;
import l2r.gameserver.templates.item.support.FishGroup;
import l2r.gameserver.templates.item.support.FishTemplate;
import l2r.gameserver.templates.item.support.LureTemplate;
import l2r.gameserver.templates.item.support.LureType;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class FishParser extends AbstractFileParser<FishDataHolder>
{
	private static final FishParser _instance = new FishParser();

	public static FishParser getInstance()
	{
		return _instance;
	}

	private FishParser()
	{
		super(FishDataHolder.getInstance());
	}

	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/fish.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "fish.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element e = iterator.next();
			if("fish".equals(e.getName()))
			{
				MultiValueSet<String> fish = new MultiValueSet<String>();
				List<MultiValueSet<String>> rewards = new ArrayList<MultiValueSet<String>>();

				for(Iterator<Attribute> attributeIterator = e.attributeIterator(); attributeIterator.hasNext();)
				{
					Attribute attribute = attributeIterator.next();
					fish.put(attribute.getName(), attribute.getValue());
				}

				for(Iterator<Element> iteratorFish = e.elementIterator(); iteratorFish.hasNext();)
				{
					Element elFish = iteratorFish.next();

					if ("set".equals(elFish.getName()))
						fish.put(elFish.attributeValue("name"), elFish.attributeValue("value"));
					else if ("reward".equals(elFish.getName()))
					{
						MultiValueSet<String> reward = new MultiValueSet<String>();
						for(Iterator<Attribute> attributeIterator = elFish.attributeIterator(); attributeIterator.hasNext();)
						{
							Attribute attribute = attributeIterator.next();
							reward.put(attribute.getName(), attribute.getValue());
						}
						rewards.add(reward);
					}
				}

				getHolder().addFish(new FishTemplate(fish, rewards));
			}
			else if("lure".equals(e.getName()))
			{
				MultiValueSet<String> map = new MultiValueSet<String>();
				for(Iterator<Attribute> attributeIterator = e.attributeIterator(); attributeIterator.hasNext();)
				{
					Attribute attribute = attributeIterator.next();
					map.put(attribute.getName(), attribute.getValue());
				}

				Map<FishGroup, Integer> chances = new HashMap<FishGroup, Integer>();
				for(Iterator<Element> elementIterator = e.elementIterator(); elementIterator.hasNext();)
				{
					Element chanceElement = elementIterator.next();
					chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.parseInt(chanceElement.attributeValue("value")));
				}
				map.put("chances", chances);
				getHolder().addLure(new LureTemplate(map));
			}
			else if("distribution".equals(e.getName()))
			{
				int id = Integer.parseInt(e.attributeValue("id"));

				for(Iterator<Element> forLureIterator = e.elementIterator(); forLureIterator.hasNext();)
				{
					Element forLureElement = forLureIterator.next();

					LureType lureType = LureType.valueOf(forLureElement.attributeValue("type"));
					Map<FishGroup, Integer> chances = new HashMap<FishGroup, Integer>();

					for(Iterator<Element> chanceIterator = forLureElement.elementIterator(); chanceIterator.hasNext();)
					{
						Element chanceElement = chanceIterator.next();
						chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.parseInt(chanceElement.attributeValue("value")));
					}
					getHolder().addDistribution(id, lureType, chances);
				}
			}
		}
	}
}
