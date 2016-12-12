package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.pledge.SubUnit;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.network.serverpackets.PledgeReceivePowerInfo;

public class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
	private int _pledgeType;
	private String _target;

	@Override
	protected void readImpl()
	{
		_pledgeType = readD();
		_target = readS(Config.CNAME_MAXLEN);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		SubUnit subUnit = activeChar.getClan() == null ? null : activeChar.getClan().getSubUnit(_pledgeType);
		if(subUnit != null)
		{
			UnitMember cm = subUnit.getUnitMember(_target);
			if(cm != null)
				activeChar.sendPacket(new PledgeReceivePowerInfo(cm));
		}
	}
}