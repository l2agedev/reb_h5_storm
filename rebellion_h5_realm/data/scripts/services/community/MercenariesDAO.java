package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Kolobrodik
 * @date: 21:29/05.05.2012
 * @description: Класс для работы с БД по сервису "Заказ на убийство"
 */
public class MercenariesDAO
{
	private static final Logger _log = LoggerFactory.getLogger(MercenariesDAO.class);
	
	protected static void restoreOrders()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `mercenaries_orders`");
			rset = statement.executeQuery();
			
			int target;
			String targetName;
			int client;
			String clientName;
			int countToKill;
			
			MercenariesOrder order;
			
			while (rset.next())
			{
				target = rset.getInt("target");
				targetName = rset.getString("targetName");
				client = rset.getInt("client");
				clientName = rset.getString("clientName");
				countToKill = rset.getInt("countToKill");
				
				order = new MercenariesOrder(client, clientName, target, targetName, countToKill);
				restoreRewards(order);
				restoreKills(order);
				MercenariesManager._orders.put(target, order);
			}
			_log.info("Restore Mercenaries completed.");
		}
		catch (final Exception e)
		{
			_log.warn("Could not restore orders!");
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	protected static void restoreRewards(MercenariesOrder order)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id,count FROM `mercenaries_rewards` WHERE `target`=?");
			statement.setInt(1, order._target);
			rset = statement.executeQuery();
			
			int id;
			long count;
			
			while (rset.next())
			{
				id = rset.getInt("id");
				count = rset.getLong("count");
				order.addReward(id, count);
			}
		}
		catch (final Exception e)
		{
			_log.warn("Could not restore orders!");
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	protected static void restoreKills(MercenariesOrder order)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT killer,count FROM `mercenaries_kills` WHERE `target`=?");
			statement.setInt(1, order._target);
			rset = statement.executeQuery();
			
			int killer, count;
			
			while (rset.next())
			{
				killer = rset.getInt("killer");
				count = rset.getInt("count");
				order._kills.put(killer, count);
			}
		}
		catch (final Exception e)
		{
			_log.warn("Could not restore orders!");
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	protected static void storeOrders()
	{
		Connection con = null;
		PreparedStatement saveOrder = null;
		PreparedStatement saveOrderRewards = null;
		PreparedStatement saveOrderKills = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			saveOrder = con.prepareStatement("REPLACE INTO `mercenaries_orders` (target,targetName,client,clientName,countToKill) values(?,?,?,?,?)");
			saveOrderRewards = con.prepareStatement("REPLACE INTO `mercenaries_rewards` (target,id,count) values(?,?,?)");
			saveOrderKills = con.prepareStatement("REPLACE INTO `mercenaries_kills` (target,killer,count) values(?,?,?)");
			
			for (MercenariesOrder order : MercenariesManager._orders.values())
			{
				saveOrder.setInt(1, order._target);
				saveOrder.setString(2, order._targetName);
				saveOrder.setInt(3, order._client);
				saveOrder.setString(4, order._clientName);
				saveOrder.setInt(5, order._countToKill);
				saveOrder.execute();
				
				for (Map.Entry<Integer, Long> item : order._reward.entrySet())
				{
					saveOrderRewards.setInt(1, order._target);
					saveOrderRewards.setInt(2, item.getKey());
					saveOrderRewards.setLong(3, item.getValue());
					saveOrderRewards.execute();
				}
				
				for (Map.Entry<Integer, Integer> killer : order._kills.entrySet())
				{
					saveOrderKills.setInt(1, order._target);
					saveOrderKills.setInt(2, killer.getKey());
					saveOrderKills.setInt(3, killer.getValue());
					saveOrderKills.execute();
				}
			}
			
			_log.info("Store Mercenaries completed.");
		}
		catch (final Exception e)
		{
			_log.warn("Could not restore orders!");
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
			DbUtils.closeQuietly(saveOrder);
			DbUtils.closeQuietly(saveOrderKills);
			DbUtils.closeQuietly(saveOrderRewards);
		}
	}
}