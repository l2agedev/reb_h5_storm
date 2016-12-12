package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.auction.Auction;
import l2r.gameserver.auction.AuctionManager;
import l2r.gameserver.instancemanager.itemauction.ItemAuction;
import l2r.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import l2r.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;

/**
 * @author n0nam3
 */
public final class RequestBidItemAuction extends L2GameClientPacket
{
	private int _instanceId;
	private long _bid;

	@Override
	protected final void readImpl()
	{
		_instanceId = readD();
		_bid = readQ();
	}

	@Override
	protected final void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		ItemInstance adena = activeChar.getInventory().getItemByItemId(57);
		if(_bid < 0 || _bid > (adena != null ? adena.getCount() : 0))
			return;

		if (Config.ENABLE_CUSTOM_AUCTION)
		{
			boolean buyout = false;
			Auction auc = AuctionManager.getAuctionById(_instanceId);
			if(auc != null)
			{
				long buyoutprice = auc.getBuyOutPrice();
				if (buyoutprice > 0)
				{
					if (_bid >= buyoutprice)
						buyout = true;
					
				}
				auc.registerBid(activeChar, _bid, buyout);
			}
		}
		
		final ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
		NpcInstance broker = activeChar.getLastNpc();
		if(broker == null || broker.getNpcId() != _instanceId || activeChar.getDistance(broker.getX(), broker.getY()) > Creature.INTERACTION_DISTANCE)
			return;
		if(instance != null)
		{
			final ItemAuction auction = instance.getCurrentAuction();
			if(auction != null)
				auction.registerBid(activeChar, _bid);
		}
	}
}