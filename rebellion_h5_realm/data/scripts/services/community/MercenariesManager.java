package services.community;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.HtmlUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Kolobrodik
 * @date: 21:29/05.05.2012
 * @description: CommunityManager для сервиса "Заказ на убийство"
 */
public class MercenariesManager extends Functions implements ScriptFile, ICommunityBoardHandler, OnDeathListener
{
	private static final Logger _log = LoggerFactory.getLogger(MercenariesManager.class);
	
	// Список "Заказов", все сделано для максимально быстрого доступа и с наименьшим кол-вом запросов,
	// после ряда тестов было решено хранить всю информацию исключительно в самом ордере.
	public static HashMap<Integer, MercenariesOrder> _orders = new HashMap<Integer, MercenariesOrder>();
	public static ArrayList<Integer> _participants = new ArrayList<Integer>();
	public HashMap<Integer, Session> _sessions = new HashMap<Integer, Session>();
	
	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if (killer != null)
		{
			if (MercenariesManager._participants.contains(killer.getObjectId()))
			{
				MercenariesOrder order = MercenariesManager._orders.get(self.getObjectId());
				if (order != null)
					order.onKill((Player) killer);
			}
		}
	}
	
	@Override
	public void onLoad()
	{
		if (Config.COMMUNITYBOARD_ENABLED && Config.SERVICES_BBSMERCENARIES)
		{
			MercenariesDAO.restoreOrders();
			CharListenerList.addGlobal(this);
			CommunityBoardManager.getInstance().registerHandler(this);
			_log.info("CommunityBoard: Mercenaries Community service loaded.");
		}
	}
	
	@Override
	public void onReload()
	{
		if (Config.COMMUNITYBOARD_ENABLED && Config.SERVICES_BBSMERCENARIES)
		{
			MercenariesDAO.storeOrders();
			CharListenerList.removeGlobal(this);
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}
	
	@Override
	public void onShutdown()
	{
		if (Config.COMMUNITYBOARD_ENABLED)
			MercenariesDAO.storeOrders();
	}
	
	/**
	 * Команды: _bbsMercenaries; - открыть страницу с заказами. _bbsMercenaries;showorder;(ID цели) - посмотреть заказ. _bbsMercenaries;addorder - меню создания заказа. _bbsMercenaries;addreward;(ID-предмета Кол-во-предмета) - добавить награды _bbsMercenaries;createorder; (Ник_перса Хайд/Не хайд)
	 * ;(ID-предмета Кол-во-предмета) - создать ордер _bbsMercenaries;registration - регистрация _bbsMercenaries;updatereward;(ID цели) - обновить награду _bbsMercenaries;updateorder;(ID цели) - обновить заказ _bbsMercenaries;addupdateorder;
	 */
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbsMercenaries"
		};
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		if (bypass.equals("_bbsMercenaries"))
			showHeadPage(player, 0);
		else if (bypass.startsWith("_bbsMercenaries;page;"))
		{
			int page;
			try
			{
				page = Integer.parseInt(bypass.split(";")[2]);
			}
			catch (Exception e)
			{
				page = 0;
			}
			
			showHeadPage(player, page);
		}
		else if (bypass.startsWith("_bbsMercenaries;addorder"))
		{
			_sessions.remove(player.getObjectId());
			showAddOrderPage(player);
		}
		else if (bypass.startsWith("_bbsMercenaries;showorder;"))
		{
			if (bypass.length() > 26)
				bypass = bypass.substring(26, bypass.length());
			else
			{
				player.sendMessage("Цель не выбрана.");
				return;
			}
			ShowBoard.separateAndSend(getOrderInformation(player, Integer.parseInt(bypass)), player);
		}
		else if (bypass.startsWith("_bbsMercenaries;addreward;"))
		{
			Session sessiong = _sessions.get(player.getObjectId());
			
			if (sessiong == null)
				_sessions.put(player.getObjectId(), sessiong = new Session());
			
			if (bypass.length() > 26)
			{
				String[] temp = bypass.substring(26, bypass.length()).split(" ");
				if (temp.length >= 2)
				{
					long count;
					try
					{
						count = Long.parseLong(temp[1]);
					}
					catch (Exception e)
					{
						player.sendMessage("Недопустимое кол-во предметов.");
						return;
					}
					int id = Integer.parseInt(temp[0]);
					ItemInstance item = player.getInventory().getItemByItemId(id);
					if (item != null && item.getCount() >= count)
					{
						sessiong._items.put(item.getItemId(), count);
						if (temp.length == 3)
						{
							Player target = GameObjectsStorage.getPlayer(Integer.parseInt(temp[2]));
							if (target != null)
							{
								showUpdateRewardPage(player, target);
								return;
							}
						}
						showAddOrderPage(player);
					}
					else
					{
						player.sendMessage("Недопустимое кол-во предметов.");
					}
				}
				else
				{
					player.sendMessage("Введите кол-во предметов отличное от нуля.");
				}
			}
		}
		else if (bypass.startsWith("_bbsMercenaries;updatereward;"))
		{
			if (bypass.length() > 29)
				bypass = bypass.substring(29, bypass.length());
			else
				return;
			
			Player target;
			try
			{
				target = GameObjectsStorage.getPlayer(Integer.parseInt(bypass));
			}
			catch (Exception ingored)
			{
				player.sendMessage("Цели не существует.");
				return;
			}
			
			MercenariesOrder order = _orders.get(target.getObjectId());
			
			if (order == null)
			{
				player.sendMessage("Обновляемого ордера не существует.");
				return;
			}
			
			if (_sessions.get(player.getObjectId()) != null && _sessions.get(player.getObjectId())._items.size() > 0)
			{
				ItemInstance item;
				HashMap<Integer, Long> tempItems = _sessions.get(player.getObjectId())._items;
				for (Map.Entry<Integer, Long> ent : tempItems.entrySet())
				{
					if ((item = player.getInventory().getItemByItemId(ent.getKey())) != null && item.getCount() >= ent.getValue())
					{
						removeItem(player, ent.getKey(), ent.getValue());
						if (!order._reward.containsKey(ent.getKey()))
							order.addReward(ent.getKey(), ent.getValue());
						else
							order.addReward(ent.getKey(), order._reward.get(ent.getKey()) + ent.getValue());
					}
				}
			}
			else
			{
				player.sendMessage("Вы не добавили награду.");
				return;
			}
			_orders.put(target.getObjectId(), order);
			
			long price = (order._clientName.startsWith("Скрыт") ? Config.MERCENARIES_ITEM_HIDE_COUNT : Config.MERCENARIES_ITEM_COUNT);
			
			if (player.getAdena() < price + (_sessions.get(player.getObjectId())._items.containsKey(57) ? _sessions.get(player.getObjectId())._items.get(57) : 0))
			{
				player.sendMessage("У вас недостаточно средств для создания ордера.");
				return;
			}
			removeItem(player, Config.MERCENARIES_ITEM, price);
			
			if (Config.MERCENARIES_ANNOUNCE)
				Announcements.getInstance().announceToAll("Заказ на убийство " + target.getName() + " был обновлен.");
			else
				player.sendMessage("Заказ на убийство " + target.getName() + " был обновлен.");
			showHeadPage(player, 0);
		}
		else if (bypass.startsWith("_bbsMercenaries;updateorder;"))
		{
			if (bypass.length() > 28)
				bypass = bypass.substring(28, bypass.length());
			else
				return;
			
			Player target;
			try
			{
				target = GameObjectsStorage.getPlayer(Integer.parseInt(bypass));
			}
			catch (Exception ingored)
			{
				player.sendMessage("Цели не существует.");
				return;
			}
			_sessions.remove(player.getObjectId());
			showUpdateRewardPage(player, target);
		}
		else if (bypass.startsWith("_bbsMercenaries;createorder"))
		{
			String[] temp = bypass.substring(29, bypass.length()).split(" ");
			
			if (temp.length < 3)
			{
				player.sendMessage("Вы не ввели: кол-во убийств или ник персонажа.");
				return;
			}
			
			Player target = GameObjectsStorage.getPlayer(temp[0]);
			if (target == null)
			{
				player.sendMessage("Вы не ввели ник цели или цель не существует.");
				return;
			}
			
			if (target == player)
			{
				player.sendMessage("Вы не можете заказать сами себя.");
				return;
			}
			
			MercenariesOrder order = _orders.get(target.getObjectId());
			if (order != null)
			{
				player.sendMessage("Заказ на этого игрока уже существует.");
				return;
			}
			
			int needToKill = 0;
			try
			{
				needToKill = Integer.parseInt(temp[2]);
			}
			catch (Exception ignored)
			{
			}
			
			if (needToKill <= 0)
			{
				player.sendMessage("Необходимо указать кол-во убийств больше нуля.");
				return;
			}
			
			if (needToKill > 10)
			{
				player.sendMessage("Количество убийств не должно привышать 10.");
				return;
			}
			
			if (_sessions.get(player.getObjectId()) == null || _sessions.get(player.getObjectId())._items.size() < 1)
			{
				player.sendMessage("Вы должны назначить награду.");
				return;
			}
			
			long price = (temp[2].startsWith("Скрыт") ? Config.MERCENARIES_ITEM_HIDE_COUNT : Config.MERCENARIES_ITEM_COUNT);
			if (player.getAdena() < price + (_sessions.get(player.getObjectId())._items.containsKey(57) ? _sessions.get(player.getObjectId())._items.get(57) : 0))
			{
				player.sendMessage("У вас недостаточно средств для создания ордера.");
				return;
			}
			
			removeItem(player, Config.MERCENARIES_ITEM, price);
			
			order = new MercenariesOrder(player.getObjectId(), temp[1].equals("Hide") ? "Скрыто" : player.getName(), target.getObjectId(), target.getName(), needToKill);
			
			ItemInstance item;
			HashMap<Integer, Long> tempItems = _sessions.get(player.getObjectId())._items;
			for (Map.Entry<Integer, Long> ent : tempItems.entrySet())
			{
				if ((item = player.getInventory().getItemByItemId(ent.getKey())) != null && item.getCount() >= ent.getValue())
				{
					removeItem(player, ent.getKey(), ent.getValue());
					if (!order._reward.containsKey(ent.getKey()))
						order.addReward(ent.getKey(), ent.getValue());
					else
						order.addReward(ent.getKey(), order._reward.get(ent.getKey()) + ent.getValue());
				}
			}
			
			_orders.put(target.getObjectId(), order);
			if (Config.MERCENARIES_ANNOUNCE)
				Announcements.getInstance().announceToAll("Был размещен заказ на убийство " + target.getName() + ".");
			else
				player.sendMessage("Был размещен заказ на убийство " + target.getName() + ".");
			showHeadPage(player, 0);
		}
		else if (bypass.startsWith("_bbsMercenaries;registration"))
		{
			if (_participants.contains(player.getObjectId()))
				_participants.remove((Object) player.getObjectId());
			else
				_participants.add(player.getObjectId());
			showHeadPage(player, 0);
		}
	}
	
	public void showHeadPage(Player player, int page)
	{
		String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/mercenaries/Mercenaries.htm", player);
		content = replace("%button_reg%", getButton(player), content);
		content = replace("%content%", getOrders(player, page), content);
		content = replace("%pages%", getPages(), content);
		ShowBoard.separateAndSend(content, player);
	}
	
	public void showAddOrderPage(Player player)
	{
		String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/mercenaries/MercenariesOrder.htm", player);
		Session sessiong = _sessions.get(player.getObjectId());
		String items = "<table width=600>";
		String items_added = StringUtils.EMPTY;
		ItemInstance[] itemsTemp = player.getInventory().getItems();
		String button;
		int count = 0;
		for (ItemInstance item : itemsTemp)
		{
			if (count == 0)
				items += "<tr>";
			if (ItemHolder.getInstance().getTemplate(item.getItemId()) == null || (sessiong != null && sessiong._items.containsKey(item.getItemId()) && sessiong._items.get(item.getItemId()) >= item.getCount()) || !item.canBeTraded(player))
				continue;
			button = HtmlUtils.htmlButton("+", "bypass _bbsMercenaries;addreward;" + item.getItemId() + " $count", 27, 23);
			items += "<td align=center>" + ItemHolder.getInstance().getTemplate(item.getItemId()).getIcon32() + "<br1>" + button + "</td>";
			if (count < 17)
				count++;
			else
			{
				count = 0;
				items += "</tr><tr><td></td></tr>";
			}
		}
		if (count != 0)
			items += "</tr>";
		items += "</table>";
		
		for (ItemInstance item : itemsTemp)
		{
			if (sessiong != null && sessiong._items.containsKey(item.getItemId()))
				items_added += "<tr><td>" + (player.isLangRus() ? item.getName() : item.getName()) + " " + sessiong._items.get(item.getItemId()) + " шт.</td></tr>";
		}
		
		button = HtmlUtils.htmlButton("Создать ордер", "bypass _bbsMercenaries;createorder; $name $hide $countToKill", 150, 25);
		content = replace("%items%", items, content);
		content = replace("%items_added%", items_added, content);
		content = replace("%button_addOrder%", button, content);
		content = replace("%items_added%", StringUtils.EMPTY, content);
		ShowBoard.separateAndSend(content, player);
	}
	
	public void showUpdateRewardPage(Player player, Player target)
	{
		if (target == null)
		{
			player.sendMessage("Цели не существует.");
			return;
		}
		String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/mercenaries/MercenariesUpdateOrder.htm", player);
		Session sessiong = _sessions.get(player.getObjectId());
		String items = "<table width=600>";
		String items_added = StringUtils.EMPTY;
		ItemInstance[] itemsTemp = player.getInventory().getItems();
		String button;
		int count = 0;
		for (ItemInstance item : itemsTemp)
		{
			if (count == 0)
				items += "<tr>";
			if (ItemHolder.getInstance().getTemplate(item.getItemId()) == null || (sessiong != null && sessiong._items.containsKey(item.getItemId()) && sessiong._items.get(item.getItemId()) >= item.getCount()) || !item.canBeTraded(player))
				continue;
			button = HtmlUtils.htmlButton("+", "bypass _bbsMercenaries;addreward;" + item.getItemId() + " $count " + target.getObjectId(), 27, 23);
			items += "<td align=center>" + ItemHolder.getInstance().getTemplate(item.getItemId()).getIcon32() + "<br1>" + button + "</td>";
			if (count < 17)
				count++;
			else
			{
				count = 0;
				items += "</tr><tr><td></td></tr>";
			}
		}
		
		if (count != 0)
			items += "</tr>";
		items += "</table>";
		
		for (ItemInstance item : itemsTemp)
		{
			if (sessiong != null && sessiong._items.containsKey(item.getItemId()))
				items_added += "<tr><td>" + (player.isLangRus() ? item.getName() : item.getName()) + " " + sessiong._items.get(item.getItemId()) + " шт.</td></tr>";
		}
		
		button = HtmlUtils.htmlButton("Создать ордер", "bypass _bbsMercenaries;updatereward;" + target.getObjectId(), 150, 25);
		content = replace("%items%", items, content);
		content = replace("%items_added%", items_added, content);
		content = replace("%button_addOrder%", button, content);
		content = replace("%target_name%", target.getName(), content);
		ShowBoard.separateAndSend(content, player);
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
	
	public String replace(String replaced, String replacing, String content)
	{
		StringBuilder content2 = new StringBuilder(content);
		int startIndex = content.indexOf(replaced);
		if (startIndex < 0)
			return content;
		content = content2.replace(startIndex, startIndex + replaced.length(), replacing).toString();
		return content;
	}
	
	public String getOrders(Player player, int page)
	{
		if (_orders.size() == 0)
			return "Заказы отсутствуют.";
		
		String content = StringUtils.EMPTY;
		boolean color = true;
		ArrayList<MercenariesOrder> temp = new ArrayList<MercenariesOrder>(_orders.size());
		temp.addAll(_orders.values());
		if (page < 1)
			page = 1;
		MercenariesOrder order;
		for (int i = page * 10 - 10; i <= page * 10 - 1; i++)
		{
			if (i < temp.size())
			{
				order = temp.get(i);
				content += "<table width=600 bgcolor=" + (color ? "c4c4c4" : "797a75") + ">" + "<tr>" + "<td width=80 align=center><font color=LEVEL>" + order._clientName + "</font></td>"
					+ "<td width=80 align=center><font color=LEVEL>" + order._targetName + "</font></td>" + "<td width=40 align=center><font color=LEVEL>" + order._countToKill + " раз</font></td>"
					+ "<td width=270 align=center><font color=b7dc2e>" + getReward(player, order) + "</font></td>" + "<td width=100 align=center><button value=\"Подробнее\" action=\"bypass _bbsMercenaries;showorder;"
					+ order._target + "\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>" + "</tr>" + "</table>";
				color = !color;
			}
			else
				break;
		}
		
		return content;
	}
	
	public String getPages()
	{
		String content;
		int pages = (int) Math.ceil((double) _orders.size() / 10);
		pages = pages > 0 ? pages : 1;
		
		content = "<table width=600>" + "<tr>" + "<td align=center>" + "<table>" + "<tr>";
		for (int i = 1; i <= pages; i++)
			content += "<td width=26 height=25 align=center>" + HtmlUtils.htmlButton(Integer.toString(i), "bypass _bbsMercenaries;page;" + i, 25, 25) + "</td>";
		
		content += "</tr>" + "</table>" + "</td>" + "</tr>" + "</table>";
		
		return content;
	}
	
	public String getOrderInformation(Player player, int objId)
	{
		MercenariesOrder order = _orders.get(objId);
		if (order == null)
			return "Такого ордера не существует.";
		
		String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/mercenaries/MercenariesOrderInformation.htm", player);
		content = replace("%Killers%", getOrderInformationKillers(order), content);
		content = replace("%Rewards%", getOrderInformationReward(player, order), content);
		content = replace("%Owners%", getOrderInformationOwner(order), content);
		content = replace("%target_obj%", Integer.toString(objId), content);
		
		return content;
	}
	
	public String getOrderInformationKillers(MercenariesOrder order)
	{
		String content = StringUtils.EMPTY;
		boolean color = true;
		Player player;
		for (Map.Entry<Integer, Integer> kill : order._kills.entrySet())
		{
			player = GameObjectsStorage.getPlayer(kill.getKey());
			if (player == null)
				continue;
			content += "<tr>" + "<td align=center>" + "<table bgcolor=" + (color ? "a69a69" : "5c7024") + ">" + "<tr><td fixwidth=150 align=center>" + player.getName() + "</td><td fixwidth=50 align=center>" + kill.getValue() + " раз" + "</td></tr></table></td></tr><tr><td></td></tr>";
			color = !color;
		}
		if (content.isEmpty())
			content = "<tr><td>Пока еще никто не убивал цель.</td></tr>";
		return content;
	}
	
	public String getOrderInformationOwner(MercenariesOrder order)
	{
		return "<tr>" + "<td align=center>" + "<table bgcolor=232f70>" + "<tr><td fixwidth=150 align=center>" + "Заказчик: <font color=LEVEL>" + order._clientName + "</font>" + "</td></tr></table></td></tr><tr><td></td></tr><tr><td align=center><table bgcolor=612370><tr><td fixwidth=150 align=center>" + "Цель: <font color=LEVEL>" + order._targetName + "</font>" + "</td></tr></table></td></tr><tr><td></td></tr><tr><td align=center><table bgcolor=232f70><tr><td fixwidth=150 align=center>" + "Необходимо убить <font color=LEVEL>" + order._countToKill + " раз</font>." + "</td></tr></table></td></tr>";
	}
	
	public String getOrderInformationReward(Player player, MercenariesOrder order)
	{
		String content = StringUtils.EMPTY;
		boolean color = true;
		ItemTemplate item;
		for (Map.Entry<Integer, Long> it : order._reward.entrySet())
		{
			item = ItemHolder.getInstance().getTemplate(it.getKey());
			if (item == null)
				continue;
			content += "<tr>" + "<td align=center>" + "<table bgcolor=" + (color ? "a69a69" : "5c7024") + ">" + "<tr><td fixwidth=150 align=center>" + it.getValue() + " " + (player.isLangRus() ? item.getName() : item.getName()) + "</td></tr></table></td></tr><tr><td></td></tr>";
			color = !color;
		}
		if (content.isEmpty())
			content = "<tr><td>Награда отсутствует.</td></tr>";
		return content;
	}
	
	public String getButton(Player player)
	{
		return HtmlUtils.htmlButton((_participants.contains(player.getObjectId()) ? "Отменить регистрацию" : "Зарегистрироваться"), "bypass _bbsMercenaries;registration", 130, 25);
	}
	
	public String getReward(Player player, MercenariesOrder order)
	{
		String content = StringUtils.EMPTY;
		
		if (order._reward.size() > 0)
			for (Map.Entry<Integer, Long> ent : order._reward.entrySet())
			{
				if (ItemHolder.getInstance().getTemplate(ent.getKey()) != null)
				{
					content += ent.getValue() + " ";
					content += player.isLangRus() ? ItemHolder.getInstance().getTemplate(ent.getKey()).getName() : ItemHolder.getInstance().getTemplate(ent.getKey()).getName();
					content += ", ";
					if (content.length() >= 40)
						break;
				}
			}
		else
			content = "Награды нет.";
		
		return content.substring(0, content.length() - 2);
	}
}

class Session
{
	HashMap<Integer, Long> _items = new HashMap<Integer, Long>();
}