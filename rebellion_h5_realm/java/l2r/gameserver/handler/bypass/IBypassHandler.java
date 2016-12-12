package l2r.gameserver.handler.bypass;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;

public interface IBypassHandler
{
	String[] getBypasses();

	void onBypassFeedback(NpcInstance npc, Player player, String command);
}
