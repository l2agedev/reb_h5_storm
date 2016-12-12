package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.entity.boat.ClanAirShip;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class Refill extends Skill
{
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || !target.isPlayer() || !target.isInBoat() || !target.getPlayer().getBoat().isClanAirShip())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(_id, _level));
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	public Refill(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null || target.isDead() || !target.isPlayer() || !target.isInBoat() || !target.getPlayer().getBoat().isClanAirShip())
				continue;

			ClanAirShip airship = (ClanAirShip) target.getPlayer().getBoat();
			airship.setCurrentFuel(airship.getCurrentFuel() + (int) _power);
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}