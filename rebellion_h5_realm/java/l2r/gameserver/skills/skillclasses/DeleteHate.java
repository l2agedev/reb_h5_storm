package l2r.gameserver.skills.skillclasses;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class DeleteHate extends Skill
{
	public DeleteHate(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{

				if(target.isRaid())
					continue;

				if(getActivateRate() > 0)
				{
					if(Config.SKILLS_CHANCE_SHOW && activeChar.isPlayer() && ((Player)activeChar).getVarB("SkillsHideChance")  || ((Player) activeChar).isGM())
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.skills.Formulas.Chance", (Player)activeChar).addString(getName()).addNumber(getActivateRate()));

					if(!Rnd.chance(getActivateRate()))
						return;
				}

				if(target.isNpc())
				{
					NpcInstance npc = (NpcInstance) target;
					npc.getAggroList().clear(false);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				}

				getEffects(activeChar, target, false, false);
			}
	}
}
