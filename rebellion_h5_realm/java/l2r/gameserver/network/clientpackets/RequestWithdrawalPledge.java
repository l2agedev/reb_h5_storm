package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.AcademyList;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2r.gameserver.model.entity.events.impl.SiegeEvent;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

import org.apache.commons.lang3.StringUtils;

public class RequestWithdrawalPledge extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		//is the guy in a clan  ?
		if(activeChar.getClanId() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_LEAVE_A_CLAN_WHILE_ENGAGED_IN_COMBAT);
			return;
		}

		Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		UnitMember member = clan.getAnyMember(activeChar.getObjectId());
		if(member == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(member.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.A_CLAN_LEADER_CANNOT_WITHDRAW_FROM_THEIR_OWN_CLAN));
			return;
		}

		DominionSiegeEvent siegeEvent = activeChar.getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null && siegeEvent.isInProgress())
		{
			activeChar.sendPacket(SystemMsg.THIS_CLAN_MEMBER_CANNOT_WITHDRAW_OR_BE_EXPELLED_WHILE_PARTICIPATING_IN_A_TERRITORY_WAR);
			return;
		}

		activeChar.removeEvents(SiegeEvent.class);

		int subUnitType = activeChar.getPledgeType();

		clan.removeClanMember(subUnitType, activeChar.getObjectId());

		clan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addString(activeChar.getName()), new PledgeShowMemberListDelete(activeChar.getName()));

		if(subUnitType == Clan.SUBUNIT_ACADEMY)
			activeChar.setLvlJoinedAcademy(0);
		
		activeChar.setClan(null);
		if(!activeChar.isNoble())
			activeChar.setTitle(StringUtils.EMPTY);

		activeChar.setLeaveClanCurTime();
		activeChar.broadcastCharInfo();

		activeChar.sendPacket(SystemMsg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN, PledgeShowMemberListDeleteAll.STATIC);
		
		// Delete from sql if he leave the academy.
		AcademyList.removeAcademyFromDB(clan, activeChar.getObjectId(), false, true);
	}
}