package l2r.gameserver.model.entity.events.impl;

import l2r.commons.collections.MultiValueSet;
import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.Config;
import l2r.gameserver.dao.SiegeClanDAO;
import l2r.gameserver.instancemanager.PlayerMessageStack;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.actions.StartStopAction;
import l2r.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import l2r.gameserver.model.entity.events.objects.SiegeClanObject;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.templates.item.ItemTemplate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ClanHallAuctionEvent extends SiegeEvent<ClanHall, AuctionSiegeClanObject>
{
	private Calendar _endSiegeDate = Calendar.getInstance();

	public ClanHallAuctionEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void reCalcNextTime(boolean onStart)
	{
		clearActions();
		_onTimeActions.clear();

		Clan owner = getResidence().getOwner();

		_endSiegeDate.setTimeInMillis(0);
		// первый старт
		if (getResidence().getAuctionLength() == 0 && owner == null)
		{
			setInProgress(true);
			
			Calendar siegeDate = getResidence().getSiegeDate();
			siegeDate.setTimeInMillis(System.currentTimeMillis());
			siegeDate.set(Calendar.HOUR_OF_DAY, 15);
			siegeDate.set(Calendar.MINUTE, 0);
			siegeDate.set(Calendar.SECOND, 0);
			siegeDate.set(Calendar.MILLISECOND, 0);
			siegeDate.setTimeInMillis(siegeDate.getTimeInMillis() + (Config.CLAN_HALL_AUCTION_LENGTH * 86400000L));
			
			getResidence().setAuctionLength(Config.CLAN_HALL_AUCTION_LENGTH);
			getResidence().setAuctionMinBid(getResidence().getBaseMinBid());
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().update();

			_onTimeActions.clear();
			addOnTimeAction(0, new StartStopAction(EVENT, true));
			addOnTimeAction(getResidence().getAuctionLength() * 86400, new StartStopAction(EVENT, false));

			_endSiegeDate.setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis() + getResidence().getAuctionLength() * 86400000L);

			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().update();
			
			registerActions();
		}
		else if (getResidence().getAuctionLength() != 0 || owner == null)
		{
			setInProgress(true);
			long endDate = getResidence().getSiegeDate().getTimeInMillis() + getResidence().getAuctionLength() * 86400000L;
			// дата окончания далека от текущей деты
			if(endDate <= System.currentTimeMillis())
				getResidence().getSiegeDate().setTimeInMillis(System.currentTimeMillis());
			
			_onTimeActions.clear();
    		addOnTimeAction(0, new StartStopAction(EVENT, true));
			addOnTimeAction(getResidence().getAuctionLength() * 86400, new StartStopAction(EVENT, false));
			
			_endSiegeDate.setTimeInMillis(endDate);
			
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().update();
			
			registerActions();
		}
	}

	@Override
	public void stopEvent(boolean step)
	{
		if (!_isInProgress.compareAndSet(true, false))
		      return;
		
		List<AuctionSiegeClanObject> siegeClanObjects = removeObjects(ATTACKERS);
		// сортуруем с Макс к мин
		AuctionSiegeClanObject[] clans = siegeClanObjects.toArray(new AuctionSiegeClanObject[siegeClanObjects.size()]);
		Arrays.sort(clans, SiegeClanObject.SiegeClanComparatorImpl.getInstance());

		Clan oldOwner = getResidence().getOwner();
		AuctionSiegeClanObject winnerSiegeClan = clans.length > 0 ? clans[0] : null;

		// если есть победитель(тоисть больше 1 клана)
		if(winnerSiegeClan != null)
		{
			// розсылаем мессагу, возращаем всем деньги
			SystemMessage2 msg = new SystemMessage2(SystemMsg.THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN).addString(winnerSiegeClan.getClan().getName());
			for(AuctionSiegeClanObject siegeClan : siegeClanObjects)
			{
				Player player = siegeClan.getClan().getLeader().getPlayer();
				if(player != null)
					player.sendPacket(msg);
				else
					PlayerMessageStack.getInstance().mailto(siegeClan.getClan().getLeaderId(), msg);

				if(siegeClan != winnerSiegeClan)
				{
					long returnBid = siegeClan.getParam() - (long)(siegeClan.getParam() * 0.1);

					siegeClan.getClan().getWarehouse().addItem(ItemTemplate.ITEM_ID_ADENA, returnBid);
				}
			}

			// если был овнер, возращаем депозит
			if (oldOwner != null)
				oldOwner.getWarehouse().addItem(ItemTemplate.ITEM_ID_ADENA, winnerSiegeClan.getParam());
						
			SiegeClanDAO.getInstance().delete(getResidence());
			
			getResidence().setAuctionLength(0);
			getResidence().setAuctionMinBid(0);
			getResidence().setAuctionDescription(StringUtils.EMPTY);
			getResidence().getSiegeDate().setTimeInMillis(0);
			getResidence().getLastSiegeDate().setTimeInMillis(0);
			getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
			getResidence().setJdbcState(JdbcEntityState.UPDATED);

			getResidence().changeOwner(winnerSiegeClan.getClan());
			getResidence().startCycleTask();
		}
		else
		{
			if(oldOwner != null)
			{
				Player player = oldOwner.getLeader().getPlayer();
				if(player != null)
					player.sendPacket(SystemMsg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED);
				else
					PlayerMessageStack.getInstance().mailto(oldOwner.getLeaderId(), SystemMsg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED.packet(null));
			}
			generateSiegeDate();
		}

		super.stopEvent(step);
	}

	@Override
	public boolean isParticle(Player player)
	{
		return false;
	}

	@Override
	public AuctionSiegeClanObject newSiegeClan(String type, int clanId, long param, long date)
	{
		Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : new AuctionSiegeClanObject(type, clan, param, date);
	}

	public Calendar getEndSiegeDate()
	{
		return _endSiegeDate;
	}

	public void setEndSiegeDate()
	{
		getResidence().update();
	}
	
	private void generateSiegeDate()
	{
		Calendar siegeDate = getResidence().getSiegeDate();
		int hourOfDay = siegeDate.get(Calendar.HOUR_OF_DAY);
		siegeDate.setTimeInMillis(System.currentTimeMillis() + (Config.CLAN_HALL_AUCTION_LENGTH * 86400000L));
		getResidence().getSiegeDate().set(Calendar.HOUR_OF_DAY, hourOfDay);
		getResidence().getSiegeDate().set(Calendar.MINUTE, 0);
		getResidence().getSiegeDate().set(Calendar.SECOND, 0);
		getResidence().getSiegeDate().set(Calendar.MILLISECOND, 0);
		
		getResidence().setJdbcState(JdbcEntityState.UPDATED);
		getResidence().update();
	}
}
