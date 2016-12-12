package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.InvisibleType;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.stats.Env;

public final class EffectInvisible extends Effect
{
	private InvisibleType _invisibleType = InvisibleType.NONE;

	public EffectInvisible(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(!_effected.isPlayer())
			return false;
		Player player = (Player) _effected;
		if(player.isInvisible())
			return false;
		if(player.getActiveWeaponFlagAttachment() != null)
			return false;
		if (NexusEvents.isInEvent(player) || player.isCombatFlagEquipped() || player.isTerritoryFlagEquipped() || player.isFlagEquipped())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = (Player) _effected;

		_invisibleType = player.getInvisibleType();

		player.setInvisibleType(InvisibleType.EFFECT);

		player.sendUserInfo(true);
		
		World.removeObjectFromPlayers(player);

		for(Creature cr : World.getAroundCharacters(player))
		{
			if (cr.getCastingTarget() != null && cr.getCastingTarget().equals(player))
			{
				cr.abortCast(true, true);
				cr.setTarget(null);
			}
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		Player player = (Player) _effected;
		if(!player.isInvisible())
			return;

		player.setInvisibleType(_invisibleType);

		player.broadcastUserInfo(true);
		if(player.getPet() != null)
			player.getPet().broadcastCharInfo();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}