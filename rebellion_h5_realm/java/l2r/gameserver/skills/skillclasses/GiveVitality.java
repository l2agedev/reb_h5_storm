package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class GiveVitality extends Skill
{
	public GiveVitality(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int vitalityToGive = (int) _power;
		int currentVitality = 0;
		for(Creature target : targets)
		{
			if(target.isPlayer())
			{
				Player player = target.getPlayer();
				currentVitality = (int) player.getVitality();
				player.setVitality(currentVitality + vitalityToGive);
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}