package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.HennaUnequipList;

public class RequestHennaUnequipList extends L2GameClientPacket
{
	int _symbolId;

	/**
	 * format: d
	 */
	@Override
	protected void readImpl()
	{
		_symbolId = readD(); //?
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		HennaUnequipList he = new HennaUnequipList(activeChar);
		activeChar.sendPacket(he);
	}
}