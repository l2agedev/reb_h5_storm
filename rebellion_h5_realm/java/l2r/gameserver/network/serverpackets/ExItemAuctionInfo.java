package l2r.gameserver.network.serverpackets;

import l2r.gameserver.auction.Auction;
import l2r.gameserver.auction.AuctionManager;
import l2r.gameserver.instancemanager.itemauction.ItemAuction;
import l2r.gameserver.instancemanager.itemauction.ItemAuctionBid;
import l2r.gameserver.instancemanager.itemauction.ItemAuctionState;
import l2r.gameserver.model.items.ItemInfo;
import l2r.gameserver.model.items.ItemInstance;

/**
 * @author n0nam3
 */
public class ExItemAuctionInfo extends L2GameServerPacket
{
	private boolean _refresh;
	private int _timeRemaining;
	private ItemAuction _currentAuction;
	private ItemAuction _nextAuction;

	// Custom
	private boolean _isCustomAuction = false;
	private int _auctionId = 0;
	private long _currectPrice = 0;
	private ItemInstance _item = null;
		
	public ExItemAuctionInfo(boolean refresh, ItemAuction currentAuction, ItemAuction nextAuction)
	{
		if(currentAuction == null)
			throw new NullPointerException();

		if(currentAuction.getAuctionState() != ItemAuctionState.STARTED)
			_timeRemaining = 0;
		else
			_timeRemaining = (int) (currentAuction.getFinishingTimeRemaining() / 1000); // in seconds
		_refresh = refresh;
		_currentAuction = currentAuction;
		_nextAuction = nextAuction;
	}

	public ExItemAuctionInfo(int auctionId, boolean refresh)
	{
		_nextAuction = null;
		_currentAuction = null;
		_isCustomAuction = true;
		Auction a = AuctionManager.getAuctionById(auctionId);
		_auctionId = a.getAuctionId();
		_refresh = refresh;
		_timeRemaining = (int) a.getTimeLeft() / 1000;
		_currectPrice = a.getCurrPrice();
		_item = a.getItem();
	}
	
	@Override
	protected void writeImpl()
	{
		if (_isCustomAuction) 
		{
			writeEx(0x68);
			writeC(_refresh ? 0x00 : 0x01);
			writeD(_auctionId);// auction id
			writeQ(_currectPrice);
			writeD(_timeRemaining);
			writeItemInfo(new ItemInfo(_item));
		} 
		else 
		{
			writeEx(0x68);
			writeC(_refresh ? 0x00 : 0x01);
			writeD(_currentAuction.getInstanceId());

			ItemAuctionBid highestBid = _currentAuction.getHighestBid();
			writeQ(highestBid != null ? highestBid.getLastBid() : _currentAuction.getAuctionInitBid());

			writeD(_timeRemaining);
			writeItemInfo(_currentAuction.getAuctionItem());

			if (_nextAuction != null) 
			{
				writeQ(_nextAuction.getAuctionInitBid());
				writeD((int) (_nextAuction.getStartingTime() / 1000L)); // unix
																		// time
																		// in
																		// seconds
				writeItemInfo(_nextAuction.getAuctionItem());
			}
		}
	}
}