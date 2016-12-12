package services.community;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: Kolobrodik
 * @date: 21:29/05.05.2012
 * @description: Контейнер с информацией по заказу для эвента "Заказ на убийство"
 */
public class MercenariesOrder
{
	// Клиент
	public int _client;
	public String _clientName;
	// Цель
	public int _target;
	public String _targetName;
	// Сколько раз необходимо убить?
	public int _countToKill;
	// Список награды, формата ID : Кол-во
	public HashMap<Integer, Long> _reward = new HashMap<Integer, Long>();
	// Список тех, кто убивал и сколько (соответственно).
	public HashMap<Integer, Integer> _kills = new HashMap<Integer, Integer>();
	
	String _hwid = StringUtils.EMPTY;
	
	protected MercenariesOrder(int client, String clientName, int target, String targetName, int countToKill)
	{
		_client = client;
		_clientName = clientName;
		_target = target;
		_targetName = targetName;
		_countToKill = countToKill;
	}
	
	protected void addReward(int id, long count)
	{
		_reward.put(id, count);
	}
	
	protected void closeOrder(Player player)
	{
		for (Map.Entry<Integer, Long> rew : _reward.entrySet())
			Functions.addItem(player, rew.getKey(), rew.getValue());
		
		MercenariesManager._orders.remove(_target);
		_reward.clear();
		if (Config.MERCENARIES_ANNOUNCE)
			Announcements.getInstance().announceToAll(player.getName() + " выполнил заказ на убийство " + _targetName + ".");
		else
			player.sendMessage("Вы выполнили заказ на убийство " + _targetName + ".");
		deleteOrder();
	}
	
	protected void onKill(Player player) // player - убийца.
	{
		int countKills;
		if (_kills.containsKey(player.getObjectId()))
			countKills = _kills.get(player.getObjectId()) + 1;
		else
			countKills = 1;
		
		if (countKills >= _countToKill)
			closeOrder(player);
		
		Player targ = GameObjectsStorage.getPlayer(_target);
		if (targ != null)
			if (targ.getClan() == null || (targ.getClanId() != player.getClanId() && (targ.getAlliance() == null || targ.getAllyId() != player.getAllyId())))
				_kills.put(player.getObjectId(), countKills);
	}
	
	protected void deleteOrder()
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			con.prepareStatement("DELETE FROM `mercenaries_orders` where `target`=" + _target).execute();
			con.prepareStatement("DELETE FROM `mercenaries_rewards` where `target`=" + _target).execute();
			con.prepareStatement("DELETE FROM `mercenaries_kills` where `target`=" + _target).execute();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}
}