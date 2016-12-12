package l2r.gameserver.auction;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Auction
{
	private static final Logger _log = LoggerFactory.getLogger(Auction.class);
	
	ItemInstance _item = null;
	int _sellerId;
	int _auctionId;
	long _endTime;
	long _highestBid;
	int _lastBidder;
	int _allBidds;
	long _buyOutPrice;
	boolean _running;
	boolean _golden = false;
	private ScheduledFuture<?> _endTask;

	public Auction(ItemInstance item, int id, int ownerId, long startBid, long buyOutPrice, int lastbider, long end, boolean isGolden)
	{
		_item = item;
		_auctionId = id;
		_sellerId = ownerId;
		_highestBid = startBid;
		_buyOutPrice = buyOutPrice;
		_endTime = end;
		_lastBidder = lastbider;
		_running = true;
		_endTask = ThreadPoolManager.getInstance().schedule(new EndAuction(false, false, 0, false), getTimeLeft());
		_golden = isGolden;
	}

	public void cancelReturnTask()
	{
		if(_endTask != null)
			_endTask.cancel(true);
		_endTask = null;
	}

	public void removePlayerAuction(int page, boolean admin)
	{
		if (admin)
			ThreadPoolManager.getInstance().schedule(new EndAuction(false, false, 0, admin), 1000);
		else
			ThreadPoolManager.getInstance().schedule(new EndAuction(false, true, page, false), 500);
	}
	
	public class EndAuction implements Runnable
	{
		private final boolean _buyout;
		private final boolean _cancel;
		private int _page = 0;
		private final boolean _admin;
		
		public EndAuction(boolean buyout, boolean cancel, int page, boolean admin)
		{
			_buyout = buyout;
			_cancel = cancel;
			_page = page;
			_admin = admin;
		}
		
		@Override
		public void run()
		{
			if(!_running && !_admin)
			{
				_log.warn("Auction: Tryed to return auction with Id: " + _auctionId + " after it already ENDED!");
				return;
			}

			if (_cancel && _sellerId != _lastBidder)
			{
				_log.warn("Auction: Trying to cancel auction while it have bidders...!");
				return;
			}
			
			_running = false;
			
			Mail letter1 = new Mail();
			Mail letter2 = new Mail();
			Mail letter3 = new Mail();
			Mail letter4 = new Mail();
			Mail letter5 = new Mail();
			Mail letter6 = new Mail();
			Mail letter7 = new Mail();
			
			Player player;

			// cancel auction
			if (_cancel)
			{
				// Send a mail to the buyer.
				letter1.setSenderId(1);
				letter1.setSenderName("Auction");
				letter1.setReceiverId(_sellerId);
				letter1.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_sellerId));
				letter1.setTopic("Cancel on auction.");
				letter1.setBody("You have chosen to cancel auction for item " + _item.getName());
				letter1.addAttachment(_item);
				letter1.setType(Mail.SenderType.NEWS_INFORMER);
				letter1.setUnread(true);
				letter1.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
				letter1.save();
				
				player = World.getPlayer(_sellerId);
				if(player != null)
				{
					player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
					player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
					
					if (_page > 0)
						AuctionManager.playerAuctionItems(player, _page);
				}
				
				Log.auction("Auction with id: " + _auctionId + " itemId: " + _item.getItemId() + " itemCount: " + _item.getCount() + " was CANCALED by his owner!");
			}
			else if (_buyout && !_cancel)
			{
				if(_sellerId != _lastBidder)
				{
					// Send a mail to the buyer.
					letter2.setSenderId(1);
					letter2.setSenderName("Auction");
					letter2.setReceiverId(_lastBidder);
					letter2.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_lastBidder));
					letter2.setTopic("You buyout an item from auction!");
					letter2.setBody("You buyout " + (_item.getCount() > 1 ? _item.getCount() + " " : "") + _item.getName() + " from " + CharacterDAO.getInstance().getNameByObjectId(getSellerId()) + "'s auction for " + _highestBid + " adena.");
					letter2.addAttachment(_item);
					letter2.setType(Mail.SenderType.NEWS_INFORMER);
					letter2.setUnread(true);
					letter2.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					letter2.save();
					
					player = World.getPlayer(_lastBidder);
					if (player != null)
					{
						player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
						player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
						
						if (_page > 0)
							AuctionManager.playerAuctionItems(player, _page);
					}
					
					Log.auction("Auction with id: " + _auctionId + " itemId: " + _item.getItemId() + " itemCount: " + _item.getCount() + " was BUYOUT by " + (player == null ? _lastBidder : player.getName()));
				}
				
				// Send a mail to the seller.
				ItemInstance sellerItem = ItemFunctions.createItem(57);
				letter3.setSenderId(1);
				letter3.setSenderName("Auction");
				letter3.setReceiverId(_sellerId);
				letter3.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_sellerId));
				letter3.setTopic("You sold an item in auction!");
				letter3.setBody("You sold " + (_item.getCount() > 1 ? _item.getCount() + " " : "") + _item.getName() + " to " + CharacterDAO.getInstance().getNameByObjectId(getLastBidder()) + " through auction for " + _buyOutPrice + " adena.");
				letter3.setType(Mail.SenderType.NEWS_INFORMER);
				letter3.setUnread(true);
				letter3.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
				
				sellerItem.setLocation(ItemInstance.ItemLocation.MAIL);
				sellerItem.setCount(_buyOutPrice);
				sellerItem.save();
				letter3.addAttachment(sellerItem);
				letter3.save();
				
				player = World.getPlayer(_sellerId);
				if (player != null)
				{
					player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
					player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
				}
				
				Log.auction("Auction with id: " + _auctionId + " itemId: " + _item.getItemId() + " itemCount: " + _item.getCount() + " was BUYOUT - " + sellerItem.getCount() + "  ADENA WAS SEND TO " + (player == null ? _sellerId : player.getName()));
			}
			else if(_sellerId != _lastBidder && !_cancel && !_buyout && !_admin)
			{
				// Send a mail to the buyer.
				letter4.setSenderId(1);
				letter4.setSenderName("Auction");
				letter4.setReceiverId(_lastBidder);
				letter4.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_lastBidder));
				letter4.setTopic("You bought an item from auction!");
				letter4.setBody("You bought "+(_item.getCount() > 1 ? _item.getCount() + " " : "") + _item.getName() + " from " + CharacterDAO.getInstance().getNameByObjectId(getSellerId()) + "'s auction for "+_highestBid+" adena.");
				letter4.addAttachment(_item);
				letter4.setType(Mail.SenderType.NEWS_INFORMER);
				letter4.setUnread(true);
				letter4.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
				letter4.save();
				
				player = World.getPlayer(_lastBidder);
				if(player != null)
				{
					player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
					player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
				}
				
				Log.auction("Auction with id: " + _auctionId + " itemId: " + _item.getItemId() + " itemCount: " + _item.getCount() + " was SOLD BY BID to " + (player == null ? _lastBidder: player.getName()));
				
				// Send a mail to the seller.
				ItemInstance sellerItem = ItemFunctions.createItem(57);
				letter5.setSenderId(1);
				letter5.setSenderName("Auction");
				letter5.setReceiverId(_sellerId);
				letter5.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_sellerId));
				letter5.setTopic("You sold an item in auction!");
				letter5.setBody("You sold "+(_item.getCount() > 1 ? _item.getCount() + " " : "") + _item.getName() + " to " + CharacterDAO.getInstance().getNameByObjectId(getLastBidder()) + " through auction for "+_highestBid+" adena.");
				letter5.setType(Mail.SenderType.NEWS_INFORMER);
				letter5.setUnread(true);
				letter5.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
				
				sellerItem.setLocation(ItemInstance.ItemLocation.MAIL);
				sellerItem.setCount(_highestBid);
				sellerItem.save();
				letter5.addAttachment(sellerItem);
				letter5.save();
				
				player = World.getPlayer(_sellerId);
				if(player != null)
				{
					player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
					player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
				}
				
				Log.auction("Auction with id: " + _auctionId + " itemId: " + _item.getItemId() + " itemCount: " + _item.getCount() + " was SOLD BY BID - " + sellerItem.getCount() + "  ADENA WAS SEND TO " + (player == null ? _sellerId: player.getName()));
			}
			else
			{
				if (_admin && getAllBidds() != 0 && _lastBidder != _sellerId)
				{
					// Message if the player is overbided.
					ItemInstance overBidItem = ItemFunctions.createItem(57);
					letter6.setSenderId(1);
					letter6.setSenderName("Auction");
					letter6.setReceiverId(_lastBidder);
					letter6.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_lastBidder));
					letter6.setTopic("Return of bid!");
					letter6.setBody("Auction was canceled by Admin. Your adena is returned.");
					letter6.setType(Mail.SenderType.NEWS_INFORMER);
					letter6.setUnread(true);
					letter6.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					
					overBidItem.setLocation(ItemInstance.ItemLocation.MAIL);
					overBidItem.setCount(_highestBid);
					overBidItem.save();
					
					letter6.addAttachment(overBidItem);
					letter6.save();
					
					player = World.getPlayer(_lastBidder);
					if (player != null)
					{
						player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
						player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
					}
				}
				
				letter7.setSenderId(1);
				letter7.setSenderName("Auction");
				letter7.setReceiverId(_sellerId);
				letter7.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_sellerId));
				letter7.setTopic("Auction finished. Your item was not sold.");
				letter7.setBody("The auction for the "+(_item.getCount() > 1 ? _item.getCount() + " " : "") + _item.getName() + " has finished. No one was interested from your item, therefore it was not sold.");
				letter7.addAttachment(_item);
				letter7.setType(Mail.SenderType.NEWS_INFORMER);
				letter7.setUnread(true);
				letter7.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
				letter7.save();
				
				player = World.getPlayer(_sellerId);
				if(player != null)
				{
					player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
					player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
				}
				
				Log.auction("Auction with id: " + _auctionId + " itemId: " + _item.getItemId() + " itemCount: " + _item.getCount() + " was NOT SOLD and returned to his owner " + (player == null ? _sellerId: player.getName()));
			}
			
			if (_admin)
				AdminTable.broadcastMessageToGMs("Auction with ID " + _auctionId + " was removed from the auction!");
			
			AuctionManager.deleteAuction(_auctionId);
			Log.auction("Auction with id: " + _auctionId + " itemId: " + _item.getItemId() + " itemCount: " + _item.getCount() + " was deleted from the auction.");
			cancelReturnTask();
			
			if (_page > 0 && player != null)
				AuctionManager.playerAuctionItems(player, _page);
		}
	}

	public synchronized void registerBid(Player player, long newBid, boolean buyout)
	{
		if(player == null || !_running)
			return;

		final int playerObjId = player.getObjectId();

		if(playerObjId == _sellerId)
		{
			player.sendMessage(new CustomMessage("l2r.gameserver.auction.auction.cannot_bid_own", player));
			return;
		}

		if(playerObjId == _lastBidder && !buyout)
		{
			player.sendMessage("You cannot overbid your previous bid!");
			return;
		}
		
		if(newBid <= _highestBid)
		{
			player.sendMessage(new CustomMessage("l2r.gameserver.auction.auction.min_bid", player).addNumber(_highestBid + 1));
			return;
		}

		if(player.getAdena() < newBid)
		{
			player.sendMessage(new CustomMessage("l2r.gameserver.auction.auction.not_enough_adena", player));
			return;
		}
		
		if(buyout && _buyOutPrice > 0 && player.getAdena() < _buyOutPrice)
		{
			player.sendMessage(new CustomMessage("l2r.gameserver.auction.auction.not_enough_adena", player));
			return;
		}
		
		if (buyout ? player.reduceAdena(_buyOutPrice, true) : player.reduceAdena(newBid, true))
		{
			if (buyout && playerObjId != _sellerId)
			{
				if (getAllBidds() != 0)
				{
					Mail letter = new Mail();
					ItemInstance overBidItem = ItemFunctions.createItem(57);
					if (playerObjId == _lastBidder)
					{
						letter.setSenderId(1);
						letter.setSenderName("Auction");
						letter.setReceiverId(_lastBidder);
						letter.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_lastBidder));
						letter.setTopic("Return of auction bid!");
						letter.setBody("Here is your previous bid.");
						letter.setType(Mail.SenderType.NEWS_INFORMER);
						letter.setUnread(true);
						letter.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					}
					else
					{
						letter.setSenderId(1);
						letter.setSenderName("Auction");
						letter.setReceiverId(_lastBidder);
						letter.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_lastBidder));
						letter.setTopic("You've lost an auction!");
						letter.setBody(player.getName() + " purchased  " + (_item.getCount() > 1 ? _item.getCount() + " " : "") + _item.getName() + " . Your adena is returned.");
						letter.setType(Mail.SenderType.NEWS_INFORMER);
						letter.setUnread(true);
						letter.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					}
					overBidItem.setLocation(ItemInstance.ItemLocation.MAIL);
					overBidItem.setCount(_highestBid);
					overBidItem.save();
					
					letter.addAttachment(overBidItem);
					letter.save();
					
					player = World.getPlayer(_lastBidder);
					if (player != null)
					{
						player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
						player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
					}
				}
				
				_lastBidder = playerObjId;
				_highestBid = _buyOutPrice;
				// end the auction
				ThreadPoolManager.getInstance().schedule(new EndAuction(true, false, 1, false), 1000);
			}
			else if (getAllBidds() != 0 && playerObjId != _sellerId)
			{
				// Message if the player is overbided.
				Mail letter = new Mail();
				ItemInstance overBidItem = ItemFunctions.createItem(57);
				letter.setSenderId(1);
				letter.setSenderName("Auction");
				letter.setReceiverId(_lastBidder);
				letter.setReceiverName(CharacterDAO.getInstance().getNameByObjectId(_lastBidder));
				letter.setTopic("Someone overbid you at auction!");
				letter.setBody(player.getName() + " overbid you with " + newBid + " adena at the auction for the " + (_item.getCount() > 1 ? _item.getCount() + " " : "") + _item.getName() + ". Your adena is returned.");
				letter.setType(Mail.SenderType.NEWS_INFORMER);
				letter.setUnread(true);
				letter.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
				
				overBidItem.setLocation(ItemInstance.ItemLocation.MAIL);
				overBidItem.setCount(_highestBid);
				overBidItem.save();
				
				letter.addAttachment(overBidItem);
				letter.save();

				player = World.getPlayer(_lastBidder);
				if(player != null)
				{
					player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
					player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
				}
			}

			_lastBidder = playerObjId;
			_highestBid = newBid;
			_allBidds++;
			
			AuctionManager.updateAuction(_auctionId);
		}
		else
		{
			player.sendMessage(new CustomMessage("l2r.gameserver.auction.auction.problem", player));
			return;
		}
	}

	public ItemInstance getItem()
	{
		return _item;
	}

	public int getAuctionId()
	{
		return _auctionId;
	}

	public long getCurrPrice()
	{
		return _highestBid;
	}

	public long getTimeLeft()
	{
		return Math.max(_endTime - System.currentTimeMillis(), 1);
	}

	public int getSellerId()
	{
		return _sellerId;
	}

	public long getEndTime()
	{
		return _endTime;
	}

	public long getMaxBid()
	{
		return _highestBid;
	}

	public int getLastBidder()
	{
		return _lastBidder;
	}

	public int getAllBidds()
	{
		return _allBidds;
	}
	
	public long getBuyOutPrice()
	{
		return _buyOutPrice;
	}
	
	/**
	 * Golden auctions always appear first in the list.
	 * @return
	 */
	public boolean isGolden()
	{
		return _golden;
	}
}
