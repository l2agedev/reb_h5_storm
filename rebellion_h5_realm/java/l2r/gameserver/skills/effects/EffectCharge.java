package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Env;

public final class EffectCharge extends Effect
{
	// Максимальное количество зарядов находится в поле val="xx"

	public EffectCharge(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (getEffected().isPlayer())
		{
			final Player player = (Player)getEffected();

			if (player.getIncreasedForce() >= calc())
				player.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
			else
				player.setIncreasedForce(player.getIncreasedForce() + 1);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
