package l2r.gameserver.data.xml.parser;

import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.OptionDataHolder;
import l2r.gameserver.model.Skill;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.OptionDataTemplate;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

public final class OptionDataParser extends StatParser<OptionDataHolder>
{
	private static final OptionDataParser _instance = new OptionDataParser();

	public static OptionDataParser getInstance()
	{
		return _instance;
	}

	protected OptionDataParser()
	{
		super(OptionDataHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/stats/option_data");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "option_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> itemIterator = rootElement.elementIterator(); itemIterator.hasNext();)
		{
			Element optionDataElement = itemIterator.next();
			OptionDataTemplate template = new OptionDataTemplate(Integer.parseInt(optionDataElement.attributeValue("id")));
			for(Iterator<Element> subIterator = optionDataElement.elementIterator(); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();
				String subName = subElement.getName();
				if(subName.equalsIgnoreCase("for"))
					parseFor(subElement, template);
				else if(subName.equalsIgnoreCase("triggers"))
					parseTriggers(subElement, template);
				else if(subName.equalsIgnoreCase("skills"))
				{
					for(Iterator<Element> nextIterator = subElement.elementIterator(); nextIterator.hasNext();)
					{
						Element nextElement =  nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("id"));
						int level = Integer.parseInt(nextElement.attributeValue("level"));

						Skill skill = SkillTable.getInstance().getInfo(id, level);

						if(skill != null)
							template.addSkill(skill);
						else
							error("Skill not found(" + id + "," + level + ") for option data:" + template.getId() + "; file:" + getCurrentFileName());
					}
				}
				/*else if(subName.equalsIgnoreCase("active_skill") || subName.equalsIgnoreCase("magic_skill") || subName.equalsIgnoreCase("attack_skill") || subName.equalsIgnoreCase("critical_skill")  || subName.equalsIgnoreCase("passive_skill"))
				{
					int id = Integer.parseInt(subElement.attributeValue("id"));
					int level = Integer.parseInt(subElement.attributeValue("level"));

					Skill skill = SkillTable.getInstance().getInfo(id, level);

					if(skill != null)
						template.addSkill(skill);
					else
						info("Skill not found(" + id + "," + level + ") for option data:" + template.getId() + "; file:" + getCurrentFileName());
				}*/
			}
			getHolder().addTemplate(template);
		}
	}

	@Override
	protected Object getTableValue(String name)
	{
		return null;
	}
}
