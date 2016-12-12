package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;

/**
 * @author VISTALL
 * @date 23:33/23.03.2011
 */
public class RequestGoodsInventoryInfo  extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{

	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		//player.sendPacket(new ExGoodsInventoryInfo(player));
	}
}
