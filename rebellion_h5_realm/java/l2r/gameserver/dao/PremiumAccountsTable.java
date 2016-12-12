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

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.utils.Util;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * @author Nik, Infern0
 *
 */
public class PremiumAccountsTable
{
	private static final Logger _log = LoggerFactory.getLogger(PremiumAccountsTable.class);
	
	public static final PremiumAccount DEFAULT_PREMIUM_ACCOUNT = new PremiumAccount(0, 0);
	public static final PremiumTemplate DEFAULT_PREMIUM_TEMPLATE = new PremiumTemplate();
	protected static Map<Integer, PremiumAccount> _premiumAccounts = new FastMap<>(); // accNameHash, PremiumAccount instance
	protected static Map<Integer, PremiumTemplate> _premiumTemplates = new FastMap<>(); // templateId, PremiumTemplate instance
	
	public static final String FILE_DIR = "config/PremiumAccountTemplates.xml";
	
	public static void init()
	{
		loadPremiumTemplates();
		loadPremiumAccounts();
	}
	
	public static void loadPremiumTemplates()
	{
		try
		{
			_premiumTemplates.clear();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = Config.getFile(FILE_DIR);
			if (!file.exists())
			{
				_log.warn("[PremiumAccountTemplate] File is missing " + Config.getFile(FILE_DIR).getAbsolutePath());
				return;
			}
			
			Document doc = factory.newDocumentBuilder().parse(file);
			Node first = doc.getFirstChild();
			if (first != null && "list".equalsIgnoreCase(first.getNodeName()))
			{
				NamedNodeMap attrs = first.getAttributes();
				Node att = attrs.getNamedItem("enabled");
				
				if (att.getNodeValue().equalsIgnoreCase("false"))
					return;
				
				for (Node n = first.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("template".equalsIgnoreCase(n.getNodeName()))
					{
						PremiumTemplate pt = new PremiumTemplate();
						attrs = n.getAttributes();
						
						if ((att = attrs.getNamedItem("id")) == null)
						{
							_log.warn("[PremiumAccountTemplate] Missing template id, skipping");
							continue;
						}
						
						int templateId = Integer.parseInt(att.getNodeValue());
						
						if ((att = attrs.getNamedItem("name")) == null)
						{
							_log.warn("[PremiumAccountTemplate] Missing template name, skipping");
							continue;
						}
						
						pt.name = att.getNodeValue();
						
						for (Node cd = n.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							try
							{
								switch (cd.getNodeName())
								{
									case "setByte":
										pt.getClass().getField(cd.getAttributes().item(0).getNodeName()).setByte(pt, Byte.parseByte(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setBoolean":
										pt.getClass().getField(cd.getAttributes().item(0).getNodeName()).setBoolean(pt, Boolean.parseBoolean(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setDouble":
										pt.getClass().getField(cd.getAttributes().item(0).getNodeName()).setDouble(pt, Double.parseDouble(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setFloat":
										pt.getClass().getField(cd.getAttributes().item(0).getNodeName()).setFloat(pt, Float.parseFloat(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setInt":
										pt.getClass().getField(cd.getAttributes().item(0).getNodeName()).setInt(pt, Integer.parseInt(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setLong":
										pt.getClass().getField(cd.getAttributes().item(0).getNodeName()).setLong(pt, Long.parseLong(cd.getAttributes().item(0).getNodeValue()));
										break;
									case "setString":
										pt.getClass().getField(cd.getAttributes().item(0).getNodeName()).set(pt, cd.getAttributes().item(0).getNodeValue());
										break;
									case "item":
										int itemId = Integer.parseInt(cd.getAttributes().getNamedItem("id").getNodeValue());
										double dropMultiplier = Double.parseDouble(cd.getAttributes().getNamedItem("dropMultiplier").getNodeValue());
										pt.itemDropBonus.put(itemId, dropMultiplier);
										break;
									case "itemConsume":
										int skillId = Integer.parseInt(cd.getAttributes().getNamedItem("skillId").getNodeValue());
										int consumeCount = Integer.parseInt(cd.getAttributes().getNamedItem("count").getNodeValue());
										pt.skillItemConsume.put(skillId, consumeCount);
										break;
									case "cost":
										int cost = Integer.parseInt(cd.getAttributes().getNamedItem("id").getNodeValue());
										pt.cost = cost;
										break;
									case "costWeek":
										int amountweek = Integer.parseInt(cd.getAttributes().getNamedItem("amount").getNodeValue());
										pt.costWeek = amountweek;
										break;
									case "costMonth":
										int amountmonth = Integer.parseInt(cd.getAttributes().getNamedItem("amount").getNodeValue());
										pt.costMonth = amountmonth;
								}
							}
							catch (NoSuchFieldException nsfe)
							{
								_log.warn("[PremiumAccountsTable] The variable [" + att.getNodeName() + "] which is set in the XML config was not found in the java file.");
								nsfe.printStackTrace();
							}
							catch (Exception e)
							{
								_log.warn("[PremiumAccountsTable] A problem occured while setting a value to the variable [" + att.getNodeName() + "]");
								e.printStackTrace();
							}
						}
						
						_premiumTemplates.put(templateId, pt);
					}
				}
			}
			
			_log.info("[PremiumAccountTemplate] Loaded " + _premiumTemplates.size() + " premium templates.");
		}
		catch (Exception e)
		{
			_log.warn("[PremiumAccountsTable] An exception was thrown while parsing XML data file.", e);
		}
	}
	
	public static void loadPremiumAccounts()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		PreparedStatement deleteStatement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM premium_accounts");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				String accountName = rs.getString("accountName");
				int templateId = rs.getInt("templateId");
				long premiumEndTime = rs.getLong("endTime");
				
				// If the premium account is expired, delete it from the table.
				if (premiumEndTime < System.currentTimeMillis())
				{
					deleteStatement = con.prepareStatement("DELETE FROM premium_accounts WHERE accountName=?");
					deleteStatement.setString(1, accountName);
					deleteStatement.execute();
				}
				else
				{
					if (_premiumTemplates.containsKey(templateId))
						_premiumAccounts.put(accountName.hashCode(), new PremiumAccount(templateId, premiumEndTime));
					else
						_log.warn("PremiumAccountsTable: Premium Template not found for ID " + templateId);
				}
			}
		}
		catch (Exception e)
		{
			_log.error("PremiumAccountTable: Failed loading data.", e);
		}
		finally
		{
			DbUtils.closeQuietly(deleteStatement);
			DbUtils.closeQuietly(con, statement, rs);
		}
	}
	
	/**
	 * 
	 * @param accountName
	 * @param templateId
	 * @param endTime
	 * 
	 * Saves the new premium account to the database and sets it active.
	 */
	public static void savePremium(String accountName, int templateId, long endTime)
	{
		if (_premiumTemplates.containsKey(templateId))
			_premiumAccounts.put(accountName.hashCode(), new PremiumAccount(templateId, endTime));
		else
			_log.warn("PremiumAccountsTable: Premium Template not found for ID " + templateId);
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO premium_accounts (accountName, templateId, endTime) VALUES (?, ?, ?)");
			statement.setString(1, accountName);
			statement.setInt(2, templateId);
			statement.setLong(3, endTime);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.error("PremiumAccountsTable: Failed to save data for player " + accountName, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	/**
	 * 
	 * @param accountName
	 * 
	 * Remove premium from account
	 */
	public static void removePremium(String accountName)
	{
		if (_premiumAccounts.containsKey(accountName.hashCode()))
			_premiumAccounts.remove(accountName.hashCode());
		else
			_log.warn("PremiumAccountsTable: Premium remove cannot find such premium account : " + accountName);
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM premium_accounts WHERE accountName=?");
			statement.setString(1, accountName);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.error("PremiumAccountsTable: Failed to remove data for player " + accountName, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static boolean isPremium(Player player)
	{
		if (player == null)
			return false;
		
		return isPremium(player.getAccountName());
	}
	
	public static boolean isPremium(String accountName)
	{
		if (accountName == null)
			return false;
		
		if (!_premiumAccounts.containsKey(accountName.hashCode()))
			return false;
		else if (!_premiumAccounts.get(accountName.hashCode()).isActive())
			return false;
		
		return true;
	}
	
	public static void sendEnterWorldMessage(Player player)
	{
		if (isPremium(player))
		{
			int timeRemaning = (int) (getPremiumAccount(player).getTimeLeftInMilis() / 1000);
			PremiumTemplate template = getPremiumAccount(player).getTemplate();	
			player.sendChatMessage(0, ChatType.TRADE.ordinal(), "Premium", "Your current premium account is " + template.name + ", it will expire after " + Util.formatTime(timeRemaning));
		}
	}
	
	public static double getDropBonus(Player lastAttacker, int itemId)
	{
		double dropChance = 1.0;
		if (lastAttacker == null)
			return dropChance;
		
		// Premium accounts bonus drop
		if (Config.PREMIUM_ACCOUNT_FOR_PARTY && lastAttacker.isInParty())
		{
			// Add the premium bonus of every party member, then divide it to the members count.
			double premiumBonus = 0;
			int size = 0;
			for (Player grMem : lastAttacker.getPlayerGroup())
			{
				premiumBonus += getPremiumAccount(grMem).getTemplate().getItemDropBonus(itemId);
				size++;
			}
			
			premiumBonus /= size;
			dropChance *= premiumBonus;
		}
		else
			dropChance *= getPremiumAccount(lastAttacker).getTemplate().getItemDropBonus(itemId);
		
		return dropChance;
	}
	
	public static double getExpBonus(Player player)
	{
		return getPremiumAccount(player).getTemplate().expBonus;
	}
	
	public static double getSpBonus(Player player)
	{
		return getPremiumAccount(player).getTemplate().spBonus;
	}
	
	public static double getEnchantBonus(Player player)
	{
		return getPremiumAccount(player).getTemplate().enchantBonus;
	}
	
	public static double getAttrEnchBonus(Player player)
	{
		return getPremiumAccount(player).getTemplate().attrEnchBonus;
	}
	
	public static double getFameBonus(Player player)
	{
		return getPremiumAccount(player).getTemplate().fameBonus;
	}
	
	public static double getKarmaDrop(Player player)
	{
		return getPremiumAccount(player).getTemplate().karmaDrop;
	}
	
	public static boolean getWhereisVoiced(Player player)
	{
		return getPremiumAccount(player).getTemplate().whereisVoiced;
	}
	
	public static boolean getPlayerShiftClick(Player player)
	{
		return getPremiumAccount(player).getTemplate().playerShiftClick;
	}
	
	public static boolean getGmShopOutsidePeace(Player player)
	{
		return getPremiumAccount(player).getTemplate().gmShopOutsidePeace;
	}
	
	public static boolean getPlayerInfinityShotsArrows(Player player)
	{
		return getPremiumAccount(player).getTemplate().playerInfinityShotArrows;
	}
	
	public static double getItemDropBonus(Player player, int itemId)
	{
		return getPremiumAccount(player).getTemplate().getItemDropBonus(itemId);
	}
	
	public static int[] getSkillItemConsume(Player player, Skill skill)
	{
		return getPremiumAccount(player).getTemplate().getSkillItemConsume(skill);
	}
	
	public static boolean getBufferOutsidePeace(Player player)
	{
		return getPremiumAccount(player).getTemplate().bufferOutsidePeace;
	}
	
	public static boolean getBufferEnchantedBuffs(Player player)
	{
		return getPremiumAccount(player).getTemplate().bufferEnchantedBuffs;
	}
	
	public static boolean getGlobalChat(Player player)
	{
		return getPremiumAccount(player).getTemplate().globalChat;
	}
	
	public static boolean getWarehouseOutsidePeace(Player player)
	{
		return getPremiumAccount(player).getTemplate().warehouseOutsidePeace;
	}
	
	public static boolean getCareerOutsidePeace(Player player)
	{
		return getPremiumAccount(player).getTemplate().careerOutsidePeace;
	}
	
	public static boolean getGatekeeperOutsidePeace(Player player)
	{
		return getPremiumAccount(player).getTemplate().gatekeeperOutsidePeace;
	}
	
	public static boolean getMailOutsidePeace(Player player)
	{
		return getPremiumAccount(player).getTemplate().mailOutsidePeace;
	}
	
	/**
	 * 
	 * @param player
	 * @return PremiumAccount instance for this player, if there isnt any instance for this player or he isnt premium
	 * it will return the default PremiumAccount instance, but NEVER null.
	 */
	public static PremiumAccount getPremiumAccount(Player player)
	{
		if (player == null || !isPremium(player))
			return DEFAULT_PREMIUM_ACCOUNT;
		
		return getPremiumAccount(player.getAccountName());
	}
	
	/**
	 * 
	 * @param accountName
	 * @return PremiumAccount instance for this account, if there isnt any instance for this account
	 * it will return the default PremiumAccount instance, but NEVER null.
	 */
	public static PremiumAccount getPremiumAccount(String accountName)
	{
		PremiumAccount toReturn = _premiumAccounts.get(accountName.hashCode());
		if (toReturn == null)
			toReturn = DEFAULT_PREMIUM_ACCOUNT;
		
		return toReturn;
	}
	
	/**
	 * 
	 * @param templateId
	 * @return returns the template instance for this ID or the default template instance if the ID is wrong.
	 * it should NEVER return null.
	 */
	public static PremiumTemplate getPremiumTemplate(int templateId)
	{
		if (_premiumTemplates.containsKey(templateId))
			return _premiumTemplates.get(templateId);
		
		return DEFAULT_PREMIUM_TEMPLATE;
	}
	
	public static class PremiumAccount
	{
		private PremiumTemplate template;
		private int templateId;
		private long endTime = 0;
		
		public PremiumAccount(int templateId, long endTime) 
		{
			this.templateId = templateId;
			this.endTime = endTime;
		}
		
		public boolean isActive()
		{
			return System.currentTimeMillis() < endTime;
		}
		
		public long getTimeLeftInMilis()
		{
			if (isActive())
				return endTime - System.currentTimeMillis();
			
			return 0;
		}
		
		public PremiumTemplate getTemplate()
		{
			if (isActive())
			{
				if (template == null)
					template = PremiumAccountsTable.getPremiumTemplate(templateId);
				
				return template;
			}
			
			return DEFAULT_PREMIUM_TEMPLATE;
		}
	}
	
	public static class PremiumTemplate
	{
		public String name = "";
		public int cost = 0;
		public int costWeek = 0;
		public int costMonth = 1;
		public double expBonus = 1.0;
		public double spBonus = 1.0;
		public double dropBonus = 1.0;
		public double enchantBonus = 1.0;
		public double attrEnchBonus = 1.0;
		public double fameBonus = 1.0;
		public double karmaDrop = 1.0; // = 1 is normal; > 1 means higher karma drop rate; < 1 means lower karma drop rate; = 0 means no karma drop at all
		public boolean whereisVoiced = false;
		public boolean playerShiftClick = false;
		public boolean gmShopOutsidePeace = false;
		public boolean playerInfinityShotArrows = false;
		public boolean bufferOutsidePeace = false;
		public boolean bufferEnchantedBuffs = false;
		public boolean globalChat = false;
		public boolean warehouseOutsidePeace = false;
		public boolean careerOutsidePeace = false;
		public boolean gatekeeperOutsidePeace = false;
		public boolean mailOutsidePeace = false;
		
		protected TIntDoubleHashMap itemDropBonus = new TIntDoubleHashMap(); // ItemId, dropBonus
		protected TIntIntHashMap skillItemConsume = new TIntIntHashMap(); // skillId, consumeCount
		
		public PremiumTemplate()
		{
			
		}
		
		/**
		 * 
		 * @param itemId
		 * @return If the itemId is in the itemDropBonus list, it will return its multiplier, else it will return the 'dropBonus' multiplier.
		 */
		public double getItemDropBonus(int itemId)
		{
			if (itemDropBonus.isEmpty() || !itemDropBonus.contains(itemId))
				return dropBonus;
			
			return itemDropBonus.get(itemId);
		}
		
		public int[] getSkillItemConsume(Skill skill)
		{
			if (skill == null)
				return new int[]{0};
			
			if (skillItemConsume.isEmpty() || !skillItemConsume.contains(skill.getId()))
				return skill.getItemConsume();
			
			int itemConsume = skillItemConsume.get(skill.getId());
			int[] ret = skill.getItemConsume();
			for (int i = 0; i < ret.length; i++)
				ret[i] = Math.min(itemConsume, ret[i]);
			
			return ret;
		}
	}
}
