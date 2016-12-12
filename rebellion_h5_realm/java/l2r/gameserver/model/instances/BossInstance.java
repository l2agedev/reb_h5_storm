package l2r.gameserver.model.instances;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.HeroDiary;
import l2r.gameserver.templates.npc.NpcTemplate;

import org.apache.commons.lang3.ArrayUtils;

public class BossInstance extends RaidBossInstance
{
	private boolean _teleportedToNest;

	public BossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isBoss()
	{
		return true;
	}

	@Override
	public final boolean isMovementDisabled()
	{
		// Core should stay anyway
		return getNpcId() == 29006 || super.isMovementDisabled();
	}

	@Override
	protected void onDeath(Creature killer)
	{
		boolean skipRecord = ArrayUtils.contains(Config.HERO_DIARY_EXCLUDED_BOSSES, getNpcId());
		if(killer.isPlayable() && !skipRecord)
		{
			Player player = killer.getPlayer();
			if(player.isInParty())
			{
				for(Player member : player.getParty())
					if(member.isNoble())
						Hero.getInstance().addHeroDiary(member.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
			}
			else if(player.isNoble())
				Hero.getInstance().addHeroDiary(player.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
		}
		super.onDeath(killer);
	}

	/**
	 * Used by Orfen to set 'teleported' flag, when hp goes to <50%
	 * @param flag
	 */
	public void setTeleported(boolean flag)
	{
		_teleportedToNest = flag;
	}

	public boolean isTeleported()
	{
		return _teleportedToNest;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}
}
