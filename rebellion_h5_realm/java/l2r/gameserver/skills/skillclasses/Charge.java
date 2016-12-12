package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.stats.Formulas.AttackInfo;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class Charge extends Skill
{
	public static final int MAX_CHARGE = 8;

	private int _charges;
	private boolean _fullCharge;

	public Charge(StatsSet set)
	{
		super(set);
		_charges = set.getInteger("charges", MAX_CHARGE);
		_fullCharge = set.getBool("fullCharge", false);
	}

	@Override
	public boolean checkCondition(final Creature activeChar, final Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;

		Player player = (Player) activeChar;

		//Камушки можно юзать даже если заряд > 7, остальное только если заряд < уровень скила
		if(getPower() <= 0 && getId() != 2165 && player.getIncreasedForce() >= _charges)
		{
			activeChar.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY);
			return false;
		}
		else if(getId() == 2165)
			player.sendPacket(new MagicSkillUse(player, player, 2165, 1, 0, 0));

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;

		boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		if(ss && getTargetType() != SkillTargetType.TARGET_SELF)
			activeChar.unChargeShots(false);

		Creature realTarget;
		boolean reflected;

		for(Creature target : targets)
		{
			if(target.isDead() || target == activeChar)
				continue;

			reflected = target.checkReflectSkill(activeChar, this);
			realTarget = reflected ? activeChar : target;

			if(getPower() > 0) // Если == 0 значит скилл "отключен"
			{
				AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, ss, false); // Skill is null because it does too much damage

				if (info.lethal_dmg > 0)
					realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);

				realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, false, true, false, false, true);
				if(!reflected)
					realTarget.doCounterAttack(this, activeChar, false);
			}

			getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
		}

		chargePlayer((Player) activeChar, getId());
	}

	public void chargePlayer(Player player, Integer skillId)
	{
		if(player.getIncreasedForce() >= _charges)
		{
			player.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
			return;
		}
		if(_fullCharge)
			player.setIncreasedForce(_charges);
		else
			player.setIncreasedForce(player.getIncreasedForce() + 1);
		
		player.stopChargeForceCountDown();
		player.startChargeForceCountDown();
	}
}