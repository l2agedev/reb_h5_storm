package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Env;

public class EffectRelax extends Effect
{
	private boolean _isWereSitting;

	public EffectRelax(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		Player player = _effected.getPlayer();
		if(player == null)
			return false;
		if(player.isMounted())
		{
			player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(_skill.getId(), _skill.getLevel()));
			return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = _effected.getPlayer();
		if(player.isMoving())
			player.stopMove();
		_isWereSitting = player.isSitting();
		player.setRelax(false);
		player.sitDown(null);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		Player player = _effected.getPlayer();
		
		if (!_isWereSitting)
			player.standUp();
		
		player.setRelax(false);
	}

	@Override
	public boolean onActionTime()
	{
		Player player = _effected.getPlayer();
		if(player == null)
			return false;

		if(player.isAlikeDead() || !player.isSitting())
		{
			player.setRelax(false);
			return false;
		}

		if(player.isCurrentHpFull() && getSkill().isToggle())
		{
			getEffected().sendPacket(SystemMsg.THAT_SKILL_HAS_BEEN_DEACTIVATED_AS_HP_WAS_FULLY_RECOVERED);
			player.setRelax(false);
			return false;
		}

		double manaDam = calc();
		if(manaDam > _effected.getCurrentMp())
			if(getSkill().isToggle())
			{
				player.sendPacket(SystemMsg.NOT_ENOUGH_MP, new SystemMessage2(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
				player.setRelax(false);
				return false;
			}

		_effected.reduceCurrentMp(manaDam, null);

		return true;
	}
}