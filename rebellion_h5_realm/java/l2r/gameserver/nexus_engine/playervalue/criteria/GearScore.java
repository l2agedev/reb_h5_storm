package l2r.gameserver.nexus_engine.playervalue.criteria;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.events.engine.base.GlobalConfigModel;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.ItemData;
import l2r.gameserver.templates.item.ItemTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map.Entry;

import javolution.text.TextBuilder;
import javolution.util.FastTable;
import javolution.util.FastMap;

/**
 * @author hNoke
 *
 */
public class GearScore implements ICriteria
{
	private FastMap<Integer, Integer> _scores;
	private FastTable<Integer> _changed;
	
	public GearScore()
	{
		_changed = new FastTable<Integer>();
		loadData();
	}
	
	private void loadData()
	{
		FastMap<Integer, Integer> _data;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int size = 0;
		
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			statement = con.prepareStatement("SELECT itemId, score FROM nexus_playervalue_items");
			rset = statement.executeQuery();
			
			_data = new FastMap<Integer, Integer>();
			
			int itemId, score;
			while (rset.next())
			{
				itemId = rset.getInt("itemId");
				score = rset.getInt("score");
				_data.put(itemId, score);
			}
			
			rset.close();
			statement.close();
			
			// process data
			
			_scores = new FastMap<Integer, Integer>();
			FastMap<Integer, Integer> missing = new FastMap<Integer, Integer>();
			
			for(ItemTemplate id : CallBack.getInstance().getOut().getAllTemplates())
			{
				if (id != null && id.getType2() < 0)
				{
					if (id.isArmor())
					{
						size ++;
						if(_data.containsKey(id.getItemId()))
						{
							_scores.put(id.getItemId(), _data.get(id.getItemId()));
						}
						else
						{
							int def = getDefaultValue(id.getItemId());
							_scores.put(id.getItemId(), def);
							missing.put(id.getItemId(), def);
						}
					}
					else if (id.isWeapon())
					{
						size ++;
						if(_data.containsKey(id.getItemId()))
						{
							_scores.put(id.getItemId(), _data.get(id.getItemId()));
						}
						else
						{
							int def = getDefaultValue(id.getItemId());
							_scores.put(id.getItemId(), def);
							missing.put(id.getItemId(), def);
						}
					}
				}
			}
			
			// add missing data to db
			if(!missing.isEmpty())
			{
				TextBuilder tb = new TextBuilder();
				for(Entry<Integer, Integer> e : missing.entrySet())
				{
					tb.append("(" + e.getKey() + "," + e.getValue() + "),");
				}
				
				String values = tb.toString();
				
				statement = con.prepareStatement("INSERT INTO nexus_playervalue_items VALUES " + values.substring(0, values.length() - 1) + ";");
				statement.execute();
				missing = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			// turn off this system
			EventConfig.getInstance().getGlobalConfig("GearScore", "enableGearScore").setValue("false");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		_data = null;
		NexusLoader.debug("Nexus Engine: Gear score engine - loaded " + size + " items.");
	}
	
	public void saveAll()
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			
			TextBuilder tb = new TextBuilder();
			for(int i : _changed)
			{
				tb.append("(" + i + "," + getScore(i) + "),");
			}
			
			String values = tb.toString();
			
			statement = con.prepareStatement("REPLACE INTO nexus_playervalue_items VALUES " + values.substring(0, values.length() - 1) + ";");
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public int getScore(int itemId)
	{
		return _scores.get(itemId);
	}
	
	public void setScore(int itemId, int value)
	{
		_scores.put(itemId, value);
		_changed.add(itemId);
	}
	
	public int getDefaultValue(int itemId)
	{
		ItemData item = new ItemData(itemId);
		
		int score = 0;
		
		String configName = "defVal_";
		
		// grade
		if(item.getCrystalType().cry == CallBack.getInstance().getValues().CRYSTAL_NONE())
			configName += "N-Grade_";
		else if(item.getCrystalType().cry == CallBack.getInstance().getValues().CRYSTAL_D())
			configName += "D-Grade_";
		else if(item.getCrystalType().cry == CallBack.getInstance().getValues().CRYSTAL_C())
			configName += "C-Grade_";
		else if(item.getCrystalType().cry == CallBack.getInstance().getValues().CRYSTAL_B())
			configName += "B-Grade_";
		else if(item.getCrystalType().cry == CallBack.getInstance().getValues().CRYSTAL_A())
			configName += "A-Grade_";
		else if(item.getCrystalType().cry == CallBack.getInstance().getValues().CRYSTAL_S())
			configName += "S-Grade_";
		else if(item.getCrystalType().cry == CallBack.getInstance().getValues().CRYSTAL_S80())
			configName += "S80-Grade_";
		else if(item.getCrystalType().cry == CallBack.getInstance().getValues().CRYSTAL_S84())
			configName += "S84-Grade_";
		
		// body part
		if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_UNDERWEAR())
			configName += "Underwear";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_L_EAR() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_R_EAR())
			configName += "Earring";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_NECK())
			configName += "Necklace";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_R_FINGER() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_L_FINGER())
			configName += "Ring";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_HEAD())
			configName += "Helmet";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_R_HAND() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_LR_HAND())
		{
			if(item.isWeapon())
			{
				if(item.getWeaponType() == null)
					return 0;
				
				String first = item.getWeaponType().toString();
				
				if(first.length() > 1)
				{
					first = first.substring(0, 1);
					String name = item.getWeaponType().toString();
					name = name.substring(1, name.length()).toLowerCase();
					
					configName += (first + name);
				}
				else
				{
					configName += item.getWeaponType().toString();
				}
			}
			else 
				return 0;
		}
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_L_HAND())
			configName += "Shield";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_GLOVES())
			configName += "Gloves";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_CHEST())
			configName += "Chest";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_LEGS())
			configName += "Gaiters";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_FEET())
			configName += "Boots";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_BACK())
			configName += "Cloak";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_FULL_ARMOR())
			configName += "FullArmor";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_HAIR() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_HAIR2() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_HAIRALL())
			configName += "Hair";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_R_BRACELET() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_L_BRACELET())
			configName += "Bracelet";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_DECO())
			configName += "Talisman";
		else if(item.getBodyPart() == CallBack.getInstance().getValues().SLOT_BELT())
			configName += "Belt";
		else // formal wear mostly
			return 0;
		
		if(!EventConfig.getInstance().globalConfigExists(configName))
		{
			GlobalConfigModel gc = EventConfig.getInstance().addGlobalConfig("GearScore", configName, "Gear score default value for " + configName + " equippable item type.", "0", 1);
			EventConfig.getInstance().saveGlobalConfig(gc); // this will save it to DB
			
			score = 0;
		}
		else
			score = EventConfig.getInstance().getGlobalConfigInt(configName);
		
		return score;
	}
	
	@Override
	public int getPoints(PlayerEventInfo player)
	{
		int points = 0;
		for(ItemData item : player.getItems())
		{
			//TODO: method param boolean - CHECK ONLY FOR THE BEST ITEMS IN PLAYER's INVENTORY - used when registering to event
			if(!item.isEquipped())
				continue;
			
			if(item.isArmor() || item.isJewellery() || item.isWeapon())
			{
				points += getScore(item.getItemId());
			}
		}
		return points;
	}
	
	public static final GearScore getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final GearScore _instance = new GearScore();
	}
}
