package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.DimensionalRift;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class RequestWithDrawalParty extends L2GameClientPacket
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

		Party party = activeChar.getParty();
		if(party == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете сейчас выйти из группы." : "Now you can not leave the group."); //TODO [G1ta0] custom message
			return;
		}

		Reflection r = activeChar.getParty().getReflection();
		if(r != null && r instanceof DimensionalRift && activeChar.getReflection().equals(r))
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.clientpackets.RequestWithDrawalParty.Rift", activeChar));
		else if(r != null && activeChar.isInCombat())
			activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете сейчас выйти из группы." : "Now you can not leave the group.");
		else
			party.removePartyMember(activeChar, false, true);
	}
}