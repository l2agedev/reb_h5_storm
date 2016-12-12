package l2r.gameserver.skills.skillclasses;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.FlyToLocation;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.ValidateLocation;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.utils.Location;

import java.util.List;

/**
 * @author Ro0TT
 * @date 4.2.2012
 **/

public class InstantJump extends Skill
{
	public InstantJump(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if (targets.isEmpty())
			return;
		
		/*if(activeChar != null && activeChar.isImmobilized())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(getId(), getLevel()));
			return;
		}*/

		Creature target = targets.get(0);
		if(Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0, activeChar, this)))
		{
			if(activeChar.isPlayer())
				activeChar.sendPacket(new SystemMessage(SystemMessage.C1_DODGES_THE_ATTACK).addName(target));
			if(target.isPlayer())
				target.sendPacket(new SystemMessage(SystemMessage.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(activeChar));
			return;
		}
		int x, y, z;

		int px = target.getX();
		int py = target.getY();
		double ph = Location.convertHeadingToDegree(target.getHeading());

		ph += 180;

		if (ph > 360)
			ph -= 360;

		ph = (Math.PI * ph) / 180;

		x = (int) (px + (25 * Math.cos(ph)));
		y = (int) (py + (25 * Math.sin(ph)));
		z = target.getZ();

		Location loc = new Location(x, y, z);

		if (Config.ALLOW_GEODATA)
			loc = GeoEngine.moveCheck(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, activeChar.getReflection().getGeoIndex());


		if(target.isNpc())
		{
			NpcInstance npc = (NpcInstance) target;
			npc.abortAttack(true, true);
			npc.abortCast(true, true);
			npc.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		else target.setTarget(null);

		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.broadcastPacket(new FlyToLocation(activeChar, loc, FlyToLocation.FlyType.DUMMY));
		activeChar.abortAttack(true, true);
		//activeChar.abortCast(true, false); Creates a splitsecond cast block which really fucks daggers...
		activeChar.setXYZ(loc.x, loc.y, loc.z);
		activeChar.setHeading(target.getHeading());
		activeChar.broadcastPacket(new ValidateLocation(activeChar));
		
		getEffects(activeChar, target, getActivateRate() > 0, false, false);
	}
}
