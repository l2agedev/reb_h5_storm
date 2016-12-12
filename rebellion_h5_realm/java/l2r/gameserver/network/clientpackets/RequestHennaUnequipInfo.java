package l2r.gameserver.network.clientpackets;

import l2r.gameserver.data.xml.holder.HennaHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.HennaUnequipInfo;
import l2r.gameserver.templates.Henna;

public class RequestHennaUnequipInfo extends L2GameClientPacket
{
	private int _symbolId;

	/**
	 * format: d
	 */
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Henna henna = HennaHolder.getInstance().getHenna(_symbolId);
		if(henna != null)
			player.sendPacket(new HennaUnequipInfo(henna, player));
	}
}