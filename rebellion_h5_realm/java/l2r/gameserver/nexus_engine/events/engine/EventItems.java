package l2r.gameserver.nexus_engine.events.engine;

import l2r.gameserver.Config;
import l2r.gameserver.nexus_engine.events.NexusLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastTable;
import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class EventItems
{
	public Map<Integer, List<Item>> _items;
	
	public EventItems()
	{
		//load();
	}
	
	private void load()
	{
		_items = new FastMap<Integer, List<Item>>();
		loadXml();
	}
	
	public void reload()
	{
		_items.clear();
		
		NexusLoader.debug("reloading nexus items");
		
		load();
		
		NexusLoader.debug("reloading nexus items done");
	}
	
	public void loadXml()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT + "/data/NexusItems.xml");
			if (!file.exists())
				throw new IOException();
			
			Document doc = factory.newDocumentBuilder().parse(file);
			NamedNodeMap attrs;
			
			int setId;
			String setName;
			
			int itemId;
			String itemName;
			//TODO add more
			
			Item itemData;
			FastTable<Item> items;
			
			for (Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling())
			{
				if ("list".equalsIgnoreCase(rift.getNodeName()))
				{
					for (Node set = rift.getFirstChild(); set != null; set = set.getNextSibling())
					{
						if ("set".equalsIgnoreCase(set.getNodeName()))
						{
							attrs = set.getAttributes();
							setId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							setName = attrs.getNamedItem("name").getNodeValue();
							
							items = new FastTable<Item>();
							
							for (Node item = set.getFirstChild(); item != null; item = item.getNextSibling())
							{
								if ("item".equalsIgnoreCase(item.getNodeName()))
								{
									attrs = item.getAttributes();
									itemId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
									itemName = attrs.getNamedItem("name").getNodeValue();
									//TODO add more params
									
									itemData = new Item(itemId, itemName, setName);
									items.add(itemData);
								}
							}
							
							addItemSet(setId, setName, items);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			NexusLoader.debug("error while loading nexus items xml");
			e.printStackTrace();
		}
	}
	
	public void addItemSet(int id, String name, FastTable<Item> items)
	{
		_items.put(id, items);
		
		NexusLoader.debug("added item set of id " + id + ", name " + name);
	}
	
	public List<Item> getItemSet(int id)
	{
		return _items.get(id);
	}
	
	public static final EventMapSystem getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventMapSystem _instance = new EventMapSystem();
	}
	
	public class Item
	{
		int _id;
		String _itemName;
		String _setName;
		
		Item(int id, String itemName, String setName)
		{
			_id = id;
			
			if (itemName != null)
				_itemName = itemName;
			
			_setName = setName;
		}
	}
}