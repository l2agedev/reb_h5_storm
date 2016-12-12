package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class VitalityHeal extends Skill
{
	public VitalityHeal(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int fullPoints = Config.VITALITY_LEVELS[4];
		double percent = _power;

		for(Creature target : targets)
		{
			if(target.isPlayer())
			{
				Player player = target.getPlayer();
				double points = fullPoints / 100 * percent;
				player.addVitality(points);
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}