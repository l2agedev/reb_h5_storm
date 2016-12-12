package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.AcademyList;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2r.gameserver.model.entity.events.impl.SiegeEvent;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class RequestOustPledgeMember extends L2GameClientPacket
{
	private String _target;

	@Override
	protected void readImpl()
	{
		_target = readS(Config.CNAME_MAXLEN);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null || !((activeChar.getClanPrivileges() & Clan.CP_CL_DISMISS) == Clan.CP_CL_DISMISS))
			return;

		Clan clan = activeChar.getClan();
		UnitMember member = clan.getAnyMember(_target);
		if(member == null)
		{
			activeChar.sendPacket(SystemMsg.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
			return;
		}

		Player memberPlayer = member.getPlayer();

		if(member.isOnline() && member.getPlayer().isInCombat())
		{
			activeChar.sendPacket(SystemMsg.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}

		DominionSiegeEvent siegeEvent = memberPlayer == null ? null : memberPlayer.getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null && siegeEvent.isInProgress())
		{
			activeChar.sendPacket(SystemMsg.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}

		if(member.isClanLeader())
		{
			activeChar.sendPacket(SystemMsg.THIS_CLAN_MEMBER_CANNOT_WITHDRAW_OR_BE_EXPELLED_WHILE_PARTICIPATING_IN_A_TERRITORY_WAR);
			return;
		}
		
		// On kick registered academy char, both side lose: inviter his fee, academy does not recive reward.
		// For now only leader can kick such chars.
		if (AcademyList.isAcademyChar(member.getObjectId()) && !activeChar.isClanLeader())
		{
			activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.BATTLEFIELD.ordinal(), "Academy", "Only the clan leader can kick characters recruit by Academy Board Service.");
			return;
		}
		
		AcademyList.removeAcademyFromDB(clan, member.getObjectId(), false, false);
				
		int subUnitType = member.getPledgeType();
		clan.removeClanMember(subUnitType, member.getObjectId());
		clan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.CLAN_MEMBER_S1_HAS_BEEN_EXPELLED).addString(_target), new PledgeShowMemberListDelete(_target));

		if(subUnitType != Clan.SUBUNIT_ACADEMY)
			clan.setExpelledMember();

		if(memberPlayer == null)
			return;

		memberPlayer.removeEvents(SiegeEvent.class);

		if(subUnitType == Clan.SUBUNIT_ACADEMY)
			memberPlayer.setLvlJoinedAcademy(0);
		memberPlayer.setClan(null);

		if(!memberPlayer.isNoble())
			memberPlayer.setTitle("");

		memberPlayer.setLeaveClanCurTime();

		memberPlayer.broadcastCharInfo();
		//memberPlayer.broadcastRelationChanged();
		memberPlayer.store(true);

		memberPlayer.sendPacket(SystemMsg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN, PledgeShowMemberListDeleteAll.STATIC);
	}
}