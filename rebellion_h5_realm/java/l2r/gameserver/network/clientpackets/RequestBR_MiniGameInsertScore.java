package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.games.MiniGameScoreManager;
import l2r.gameserver.model.Player;

public class RequestBR_MiniGameInsertScore extends L2GameClientPacket
{
	private int _score;

	@Override
	protected void readImpl() throws Exception
	{
		_score = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_JAPAN_MINIGAME)
			return;

		MiniGameScoreManager.getInstance().insertScore(player, _score);
	}
}