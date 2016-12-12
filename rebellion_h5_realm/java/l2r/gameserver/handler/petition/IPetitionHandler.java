package l2r.gameserver.handler.petition;

import l2r.gameserver.model.Player;

public interface IPetitionHandler
{
	void handle(Player player, int id, String txt);
}
