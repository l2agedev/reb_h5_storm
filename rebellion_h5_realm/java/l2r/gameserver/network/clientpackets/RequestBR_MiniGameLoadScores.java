package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExBR_MiniGameLoadScores;

public class RequestBR_MiniGameLoadScores extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{
		//
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_JAPAN_MINIGAME)
			return;

		player.sendPacket(new ExBR_MiniGameLoadScores(player));
	}
}