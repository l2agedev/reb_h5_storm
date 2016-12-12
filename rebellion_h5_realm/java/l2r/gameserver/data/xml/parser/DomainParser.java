package l2r.gameserver.data.xml.parser;

import l2r.commons.data.xml.AbstractFileParser;
import l2r.commons.geometry.Polygon;
import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.Territory;
import l2r.gameserver.templates.mapregion.DomainArea;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

public class DomainParser extends AbstractFileParser<MapRegionManager>
{
	private static final DomainParser _instance = new DomainParser();

	public static DomainParser getInstance()
	{
		return _instance;
	}

	protected DomainParser()
	{
		super(MapRegionManager.getInstance());
	}

	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/mapregion/domains.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "domains.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element listElement = iterator.next();

			if("domain".equals(listElement.getName()))
			{
				int id = Integer.parseInt(listElement.attributeValue("id"));
				String name = listElement.attributeValue("name");
				Territory territory = null;

				for(Iterator<Element> i = listElement.elementIterator(); i.hasNext();)
				{
					Element n = i.next();

					if("polygon".equalsIgnoreCase(n.getName()))
					{
						Polygon shape = ZoneParser.parsePolygon(n);

						if(!shape.validate())
							error("DomainParser: invalid territory data : " + shape + "!");

						if(territory == null)
							territory = new Territory();

						territory.add(shape);
					}
				}

				if(territory == null)
					throw new RuntimeException("DomainParser: empty territory!");

				getHolder().addRegionData(new DomainArea(id, territory, name));
			}
		}
	}
}
