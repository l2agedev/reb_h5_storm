package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.TrapInstance;
import l2r.gameserver.network.serverpackets.NpcInfo;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class DetectTrap extends Skill
{
	public DetectTrap(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null && target.isTrap())
			{
				TrapInstance trap = (TrapInstance) target;
				if(trap.getLevel() <= getPower())
				{
					trap.setDetected(true);
					for(Player player : World.getAroundPlayers(trap))
						player.sendPacket(new NpcInfo(trap, player));
				}
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}