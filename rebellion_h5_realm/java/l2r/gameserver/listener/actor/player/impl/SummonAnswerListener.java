package l2r.gameserver.listener.actor.player.impl;

import l2r.commons.lang.reference.HardReference;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.skills.skillclasses.Call;
import l2r.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 11:28/15.04.2011
 */
public class SummonAnswerListener implements OnAnswerListener
{
	private final HardReference<Player> _playerRef;
	private final HardReference<Player> _summonerRef;
	private final Location _location;
	private final long _count;
	private final long _timeStamp;

	public SummonAnswerListener(Player summoner, Player player, Location loc, long count, int expirationTime)
	{
		_summonerRef = summoner.getRef();
		_playerRef = player.getRef();
		_location = loc;
		_count = count;
		_timeStamp = expirationTime > 0 ? System.currentTimeMillis() + expirationTime : Long.MAX_VALUE;
	}

	@Override
	public void sayYes()
	{
		if (System.currentTimeMillis() > _timeStamp)
			return;
		
		Player summoner = _summonerRef.get();
		Player player = _playerRef.get();
		if(player == null || summoner == null)
			return;

		if (Call.canSummonHere(summoner) != null)
			return;

		if (Call.canBeSummoned(player) != null)
			return;
		
		player.abortAttack(true, true);
		player.abortCast(true, true);
		player.stopMove();
		if(_count > 0)
		{
			if(player.getInventory().destroyItemByItemId(8615, _count))
			{
				player.sendPacket(SystemMessage2.removeItems(8615, _count));
				player.teleToLocation(_location);
			}
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		}
		else
			player.teleToLocation(_location);
	}

	@Override
	public void sayNo()
	{
		//
	}
}
