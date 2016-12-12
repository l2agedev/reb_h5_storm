package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Creature;
import l2r.gameserver.network.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Creature activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExCursedWeaponList());
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}