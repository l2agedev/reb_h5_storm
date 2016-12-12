package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class CombatPointHeal extends Skill
{
	private final boolean _ignoreCpEff;

	public CombatPointHeal(StatsSet set)
	{
		super(set);
		_ignoreCpEff = set.getBool("ignoreCpEff", false);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead() || target.isHealBlocked())
					continue;
				double maxNewCp = _power * (!_ignoreCpEff ? target.calcStat(Stats.CPHEAL_EFFECTIVNESS, 100., activeChar, this) : 100.) / 100.;
				double addToCp = Math.max(0, Math.min(maxNewCp, target.calcStat(Stats.CP_LIMIT, null, null) * target.getMaxCp() / 100. - target.getCurrentCp()));
				if(addToCp > 0)
					target.setCurrentCp(addToCp + target.getCurrentCp());
				target.sendPacket(new SystemMessage2(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addInteger((long) addToCp));
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
