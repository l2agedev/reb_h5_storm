package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.stats.Env;
import l2r.gameserver.tables.SkillTable;

public class EffectCallSkills extends Effect
{
	public EffectCallSkills(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		int[] skillIds = getTemplate().getParam().getIntegerArray("skillIds");
		int[] skillLevels = getTemplate().getParam().getIntegerArray("skillLevels");

		for(int i = 0; i < skillIds.length; i++)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillIds[i], skillLevels[i]);
			for(Creature cha : skill.getTargets(getEffector(), getEffected(), false))
				getEffector().broadcastPacket(new MagicSkillUse(getEffector(), cha, skillIds[i], skillLevels[i], 0, 0));
			getEffector().callSkill(skill, skill.getTargets(getEffector(), getEffected(), false), false);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}