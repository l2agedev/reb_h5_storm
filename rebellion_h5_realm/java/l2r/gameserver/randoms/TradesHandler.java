package l2r.gameserver.randoms;

import l2r.commons.lang.reference.HardReference;
import l2r.gameserver.cache.ItemInfoCache;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.items.ItemInfo;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.inc.incolution.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradesHandler
{
	private static final Logger _log = LoggerFactory.getLogger(TradesHandler.class);
	
	private static Map<Integer, Trade> _trades = new HashMap<Integer, Trade>();
	
	public static void tryPublishTrade(Player player, String fullChatText)
	{	
		// Do not allow trade publishing spam... one trade every minute.
		if (!player.canOverrideCond(PcCondOverride.ITEM_TRADE_CONDITIONS) && _trades.containsKey(player.getObjectId()))
		{
			Trade trade = _trades.get(player.getObjectId());
			if (System.currentTimeMillis() - (trade.timeAddedInSecs*1000) < 60000)
				return;
		}
		
		int pos1 = fullChatText.indexOf(8);
		if (pos1 > -1)
		{
			int pos = fullChatText.indexOf("ID=", pos1);
			if(pos == -1)
				return;
			
			pos += 3; // Change position to ID
			StringBuilder result = new StringBuilder(9);
			while(Character.isDigit(fullChatText.charAt(pos)))
				result.append(fullChatText.charAt(pos++));
			
			int id = Integer.parseInt(result.toString());

			// Get the cached item or cache it if not present.
			ItemInfo item = ItemInfoCache.getInstance().get(id);
			if (item == null)
			{
				ItemInstance itemInstance = player.getInventory().getItemByObjectId(id);
				if (itemInstance != null)
				{
					ItemInfoCache.getInstance().put(itemInstance);
					item = ItemInfoCache.getInstance().get(id);
				}
			}
			
			if(item != null)
			{
				// Ask the player if he wants to publish the item to .tradelist
				player.ask(new l2r.gameserver.network.serverpackets.ConfirmDlg(SystemMsg.S1, 10000).addString("Would you like to add this trade message to .tradelist so others can see your trade request?"), new TradeItemListener(player, item, fullChatText));
			}
			else
			{
				_log.info(player + " trying publish object which is not his item! Object:" + item);
				return;
			}
		}
	}
	
	public static void addTrade(ItemInfo itemInfo, String message)
	{
		Trade trade = new Trade();
		trade.itemName = itemInfo.getItem().getName();
		trade.itemInfo = itemInfo;
		trade.message = message;
		trade.ownerObjId = itemInfo.getOwnerId();
		trade.ownerName = CharacterDAO.getInstance().getNameByObjectId(itemInfo.getOwnerId());
		
		// Only 1 trade per owner.
		_trades.put(itemInfo.getOwnerId() + Rnd.get(1000000), trade);
	}
	
	private static class Trade
	{
		String itemName;
		ItemInfo itemInfo;
		String message;
		int ownerObjId;
		String ownerName;
		final int timeAddedInSecs = (int) (System.currentTimeMillis() / 1000);
		
		@Override
		public String toString()
		{
			return itemName + "(" + itemInfo.getObjectId() + "): ListedBy " + ownerName + "(" + ownerObjId + ") OwnedBy: " + itemInfo.getOwnerId();
		}
	}
	
	/**
	 * @return Sorted list of all trades by time added.
	 */
	private static List<Trade> getList()
	{
		return new ArrayList<Trade>(_trades.values());
	}
	
	private static List<Trade> getList(String search)
	{
		if (search == null || search.isEmpty())
			return getList();
		
		search = search.toLowerCase();
		String[] searchStrings = search.split(" ");
		Map<Integer, Trade> result = new TreeMap<Integer, Trade>(); // OrderPoints, Trade
		
		for (Trade trade : getList())
		{
			if (trade.itemInfo.getOwnerId() != trade.ownerObjId)
			{
				_trades.remove(trade.ownerObjId);
				_log.warn("TradesHandler: Listed item in incorrect owner found and removed." + trade);
			}
			if (trade.itemName.equalsIgnoreCase(search) || trade.message.equalsIgnoreCase(search))
				result.put(100, trade);
			else if (trade.itemName.toLowerCase().startsWith(search))
				result.put(80, trade);
			else
			{
				int orderPts = 0;
				//String[] messageStrings = trade.message.toLowerCase().split(" ");
				for (String srch : searchStrings)
				{
					if (trade.itemName.toLowerCase().contains(srch))
						orderPts += 10;
				}
				
				for (String srch : searchStrings)
				{
					if (trade.message.toLowerCase().contains(srch))
						orderPts += 4;
				}
				
				orderPts = orderPts / searchStrings.length; // I dunno what im doing... atm im not even thinking, just writing :D
				// It should be like... the more occurances there are in the search strings, the more order points it gets
				
				if (orderPts > 0)
					result.put(orderPts, trade);
			}
		}
		
		return new ArrayList<Trade>(result.values());
	}
	
	public static void display(Player player, String search)
	{
		if (player == null)
			return;
		
		List<Trade> trades = getList(search);
		int tradesLeftToDisplay = 50;
		StringBuilder msg = new StringBuilder(100 + trades.size() * 60);
		msg.append("=============[ TRADE LIST ]============\n");
		msg.append("[>] Displaying: " + (search == null || search.isEmpty() ? "Most Recent" : "\""+search+"\"") + " [>] Results: " + (trades.size() > 50 ? ("50/" + trades.size()) : trades.size()) + "\n");
		for (Trade trade : trades)
		{
			if (tradesLeftToDisplay-- <= 0)
				break;
			
			//player.sendPacket(new ExRpItemLink(trade.itemInfo));
			trade.message = trade.message.replaceAll("Color=0", "Color=255");
			msg.append('\n').append(trade.ownerName + ": " + trade.message);
		}
		msg.append("\n===================================");
		player.sendMessage(msg.toString());
	}
	
	protected static class TradeItemListener implements OnAnswerListener
	{
		private HardReference<Player> _playerRef;
		private ItemInfo _itemInfo;
		private String _message;

		protected TradeItemListener(Player player, ItemInfo itemInfo, String message)
		{
			_playerRef = player.getRef();
			_itemInfo = itemInfo;
			_message = message;
		}

		@Override
		public void sayYes()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			
			if(_itemInfo == null)
				return;
			
			if (_itemInfo.getOwnerId() != player.getObjectId())
				return;
			
			addTrade(_itemInfo, _message);
		}

		@Override
		public void sayNo()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			
			if (!player.getVarB("DisableTradelistAutoask", false))
				player.sendMessage("You can disable this message in .cfg -> DisableTradelistAutoask");
		}
	}
}
