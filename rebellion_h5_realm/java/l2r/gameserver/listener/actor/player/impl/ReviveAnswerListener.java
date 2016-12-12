package l2r.gameserver.listener.actor.player.impl;

import l2r.commons.lang.reference.HardReference;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.PetInstance;

public class ReviveAnswerListener implements OnAnswerListener
{
	private final HardReference<Player> _playerRef;
	private final double _power;
	private final boolean _forPet;
	private final long _timeStamp;

	public ReviveAnswerListener(Player player, double power, boolean forPet, int expirationTime)
	{
		_playerRef = player.getRef();
		_forPet = forPet;
		_power = power;
		_timeStamp = expirationTime > 0 ? System.currentTimeMillis() + expirationTime : Long.MAX_VALUE;
	}

	@Override
	public void sayYes()
	{
		if (System.currentTimeMillis() > _timeStamp)
			return;
		
		Player player = _playerRef.get();
		if(player == null)
			return;
		
		if(!player.isDead() && !_forPet || _forPet && player.getPet() != null && !player.getPet().isDead())
			return;

		if(!_forPet)
			player.doRevive(_power);
		else if(player.getPet() != null)
			((PetInstance) player.getPet()).doRevive(_power);
	}

	@Override
	public void sayNo()
	{
	}

	public double getPower()
	{
		return _power;
	}

	public boolean isForPet()
	{
		return _forPet;
	}
}
