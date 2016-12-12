package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2r.gameserver.model.instances.ArtefactInstance;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.utils.Log;

import java.util.List;

public class TakeCastle extends Skill
{
	public TakeCastle(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Zone siegeZone = target.getZone(ZoneType.SIEGE);

		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;

		if(activeChar == null || !activeChar.isPlayer())
			return false;

		Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}

		CastleSiegeEvent siegeEvent = player.getEvent(CastleSiegeEvent.class);
		if(siegeEvent == null)
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}

		if(siegeEvent.getSiegeClan(CastleSiegeEvent.ATTACKERS, player.getClan()) == null || siegeEvent.getResidence().getId() != siegeZone.getParams().getInteger("residence", 0))
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		if(!siegeEvent.isActiveArtefact(activeChar, (ArtefactInstance)target))
		{
			player.sendMessage("You have left a stamp on this altar. Place the seal on the second.");
			return false;
		}
		if(player.isMounted())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}

		if(!player.isInRangeZ(target, 185) || player.getZDeltaSq(target.getZ()) > 2500)
		{
			player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(first)
		{
			siegeEvent.broadcastTo(SystemMsg.THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_HOLY_ARTIFACT, CastleSiegeEvent.DEFENDERS);
			String debug = "TakeCastle: caster: " + activeChar.getName() + ", loc:" + activeChar.getLoc() + ", castle: " + siegeEvent.getName() + ", target: " + target;

			Log.debug(debug);
			AdminTable.broadcastMessageToGMs(debug);
		}

		return true;
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(!target.isArtefact())
					continue;
				Player player = (Player) activeChar;

				CastleSiegeEvent siegeEvent = player.getEvent(CastleSiegeEvent.class);
				if(siegeEvent != null)
				{
					siegeEvent.engrave(activeChar, (ArtefactInstance)target);
				}
			}
	}
}