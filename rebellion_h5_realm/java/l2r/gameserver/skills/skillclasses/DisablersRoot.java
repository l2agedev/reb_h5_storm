package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Skill;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class DisablersRoot extends Skill
{
	public DisablersRoot(StatsSet set) 
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		Effect[] effects = activeChar.getPlayer().getEffectList().getAllFirstEffects();
		for(Effect effect : effects)
			if(effect != null && effect.getEffectType() == EffectType.Root)
			{
				effect.exit();
			}
	}
}