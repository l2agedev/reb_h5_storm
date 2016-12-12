/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.dao;

import l2r.gameserver.Config;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * 
 * @author Nik
 *
 */
public class ChampionTemplateTable
{
	private static final Logger _log = LoggerFactory.getLogger(ChampionTemplateTable.class);
	public static final String FILE_DIR = "config/ExtChampionMode.xml";
	
	public static boolean ENABLE_EXT_CHAMPION_MODE = false;
	public static int EXT_CHAMPION_MODE_MAX_ROLL_VALUE = 0;
	private static List<ChampionTemplate> _championTemplates = new FastTable<>();
	
	public static List<ChampionTemplate> getChampionTemplates()
	{
		return _championTemplates;
	}
	
	public ChampionTemplateTable()
	{
		loadConfig();
	}
	
	public void loadConfig()
	{
		try
		{
			_championTemplates.clear();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = Config.getFile(FILE_DIR);
			if (!file.exists())
			{
				_log.warn("[ChampionTemplateTable] File is missing " + Config.getFile(FILE_DIR).getAbsolutePath());
				return;
			}
			
			Document doc = factory.newDocumentBuilder().parse(file);
			Node first = doc.getFirstChild();
			if (first != null && "list".equalsIgnoreCase(first.getNodeName()))
			{
				NamedNodeMap attrs = first.getAttributes();
				Node att = attrs.getNamedItem("enabled");
				try
				{
					ENABLE_EXT_CHAMPION_MODE = Boolean.parseBoolean(att.getNodeValue());
					att = attrs.getNamedItem("maxRollValue");
					EXT_CHAMPION_MODE_MAX_ROLL_VALUE = Integer.parseInt(att.getNodeValue());
				}
				catch (Exception e)
				{
					_log.warn("[ChampionTemplateTable] Failed to load initial list params, mode skipped.");
					e.printStackTrace();
				}
				
				if (!ENABLE_EXT_CHAMPION_MODE)
					return;
				
				ChampionTemplate ct;
				
				for (Node n = first.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("champion".equalsIgnoreCase(n.getNodeName()))
					{
						ct = new ChampionTemplate();
						attrs = n.getAttributes();
						
						if ((att = attrs.getNamedItem("minChance")) == null)
						{
							_log.warn("[ChampionTemplateTable] Missing minChance, skipping");
							continue;
						}
						ct.minChance = Integer.parseInt(att.getNodeValue());
						
						if ((att = attrs.getNamedItem("maxChance")) == null)
						{
							_log.warn("[ChampionTemplateTable] Missing maxChance, skipping");
							continue;
						}
						ct.maxChance = Integer.parseInt(att.getNodeValue());
						
						if ((att = attrs.getNamedItem("minLevel")) == null)
						{
							_log.warn("[ChampionTemplateTable] Missing minLevel, skipping");
							continue;
						}
						ct.minLevel = Integer.parseInt(att.getNodeValue());
						
						if ((att = attrs.getNamedItem("maxLevel")) == null)
						{
							_log.warn("[ChampionTemplateTable] Missing maxLevel, skipping");
							continue;
						}
						ct.maxLevel = Integer.parseInt(att.getNodeValue());
						
						for (Node cd = n.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							try
							{
								switch (cd.getNodeName())
								{
									case "setByte":
										ct.getClass().getField(cd.getAttributes().item(0).getNodeName()).setByte(ct, Byte.parseByte(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setBoolean":
										ct.getClass().getField(cd.getAttributes().item(0).getNodeName()).setBoolean(ct, Boolean.parseBoolean(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setDouble":
										ct.getClass().getField(cd.getAttributes().item(0).getNodeName()).setDouble(ct, Double.parseDouble(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setFloat":
										ct.getClass().getField(cd.getAttributes().item(0).getNodeName()).setFloat(ct, Float.parseFloat(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setInt":
										ct.getClass().getField(cd.getAttributes().item(0).getNodeName()).setInt(ct, Integer.parseInt(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setLong":
										ct.getClass().getField(cd.getAttributes().item(0).getNodeName()).setLong(ct, Long.parseLong(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setString":
										ct.getClass().getField(cd.getAttributes().item(0).getNodeName()).set(ct, cd.getAttributes().item(0).getNodeValue());
										break;
									case "item":
										int itemId = Integer.parseInt(cd.getAttributes().getNamedItem("itemId").getNodeValue());
										int minCount = Integer.parseInt(cd.getAttributes().getNamedItem("minCount").getNodeValue());
										int maxCount = Integer.parseInt(cd.getAttributes().getNamedItem("maxCount").getNodeValue());
										int dropChance = Integer.parseInt(cd.getAttributes().getNamedItem("dropChance").getNodeValue());
										ct.rewards.add(new RewardItem(itemId, minCount, maxCount, dropChance));
								}
							}
							catch (NoSuchFieldException nsfe)
							{
								_log.warn("[ChampionTemplateTable] The variable [" + att.getNodeName() + "] which is set in the XML config was not found in the java file.");
								nsfe.printStackTrace();
							}
							catch (Exception e)
							{
								_log.warn("[ChampionTemplateTable] A problem occured while setting a value to the variable [" + att.getNodeName() + "]");
								e.printStackTrace();
							}
						}
						
						_championTemplates.add(ct);
					}
				}
			}
		}
		catch (Exception e)
		{
			ENABLE_EXT_CHAMPION_MODE = false;
			_log.warn("[ChampionTemplateTable] Failed to parse xml: " + e.getMessage(), e);
		}
		finally
		{
			// If this champion mode is enabled, disable the other champion mode.
			//if (ENABLE_EXT_CHAMPION_MODE)
			//	Config.L2JMOD_CHAMPION_ENABLE = false;
		}
		
		_log.info("ChampionTemplateTable: Loaded " + _championTemplates.size() + " champion templates.");
	}
	
	public class ChampionTemplate
	{
		public int minChance = -1;
		public int maxChance = -1;
		public int minLevel = -1;
		public int maxLevel = -1;
		public boolean isPassive = false;
		public boolean useVitalityRate = false;
		public boolean spawnsInInstances = false;
		public String title = null;
		public float patkMultiplier = 1;
		public float matkMultiplier = 1;
		public float pdefMultiplier = 1;
		public float mdefMultiplier = 1;
		public float atkSpdMultiplier = 1;
		public float matkSpdMultiplier = 1;
		public double hpMultiplier = 1;
		public double hpRegenMultiplier = 1;
		public double expMultiplier = 1;
		public double spMultiplier = 1;
		public double itemDropMultiplier = 1;
		public double spoilDropMultiplier = 1;
		public double adenaMultiplier = 1;
		public int weaponEnchant = 0;
		public boolean redCircle = false;
		public boolean blueCircle = false;
		public List<RewardItem> rewards = new FastTable<>();
	}
	
	public class RewardItem
	{
		private int _itemId;
		private int _minCount;
		private int _maxCount;
		private int _dropChance;
		
		public RewardItem(int itemId, int minCount, int maxCount, int dropChance)
		{
			_itemId = itemId;
			_minCount = minCount;
			_maxCount = maxCount;
			_dropChance = dropChance;
		}

		public int getItemId() {return _itemId;}
		public int getMinCount() {return _minCount;}
		public int getMaxCount() {return _maxCount;}
		public int getDropChance() {return _dropChance;}
	}
	
	public static final ChampionTemplateTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ChampionTemplateTable _instance = new ChampionTemplateTable();
	}
}
