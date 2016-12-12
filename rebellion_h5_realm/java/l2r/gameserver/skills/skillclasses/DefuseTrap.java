package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.TrapInstance;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class DefuseTrap extends Skill
{
	public DefuseTrap(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || !target.isTrap())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null && target.isTrap())
			{

				TrapInstance trap = (TrapInstance) target;
				if(trap.getLevel() <= getPower())
					trap.deleteMe();
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}