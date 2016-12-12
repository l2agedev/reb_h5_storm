package l2r.gameserver.nexus_engine.events.engine.html;

import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.NpcData;

/**
 * @author hNoke
 * - this interface will be used in a future api
 */
public interface IHtmlManager
{
	public boolean showNpcHtml(PlayerEventInfo player, NpcData npc);
	public boolean onBypass(PlayerEventInfo player, String bypass);
	boolean onCbBypass(PlayerEventInfo player, String bypass);
}
