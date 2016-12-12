package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class TeleportNpc extends Skill
{
	public TeleportNpc(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null && !target.isDead())
			{
				getEffects(activeChar, target, getActivateRate() > 0, false);
				target.abortAttack(true, true);
				target.abortCast(true, true);
				target.stopMove();
				int x = activeChar.getX();
				int y = activeChar.getY();
				int z = activeChar.getZ();
				int h = activeChar.getHeading();
				int range = (int) (activeChar.getColRadius() + target.getColRadius());
				int hyp = (int) Math.sqrt(range * range / 2);
				if(h < 16384)
				{
					x += hyp;
					y += hyp;
				}
				else if(h > 16384 && h <= 32768)
				{
					x -= hyp;
					y += hyp;
				}
				else if(h < 32768 && h <= 49152)
				{
					x -= hyp;
					y -= hyp;
				}
				else if(h > 49152)
				{
					x += hyp;
					y -= hyp;
				}
				target.setXYZ(x, y, z);
				target.validateLocation(1);
			}
	}
}