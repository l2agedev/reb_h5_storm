/**
 * 
 */
package l2r.gameserver.nexus_interface.callback.api;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.base.description.EventDescriptionSystem;
import l2r.gameserver.nexus_interface.callback.api.descriptions.CTFDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.ChestsDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.DMDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.DominationDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.KoreanDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.LMSDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.MassDominationDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.MiniTvTDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.PartyFightsDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.SinglePlayersFightsDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.TvTAdvancedDescription;
import l2r.gameserver.nexus_interface.callback.api.descriptions.TvTDescription;

/**
 * @author hNoke
 *
 */
public class DescriptionLoader
{
	public static void load()
	{
		EventDescriptionSystem.getInstance().addDescription(EventType.TvT, new TvTDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.TvTAdv, new TvTAdvancedDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.CTF, new CTFDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.Domination, new DominationDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.MassDomination, new MassDominationDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.DM, new DMDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.LastMan, new LMSDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.LuckyChests, new ChestsDescription());
		
		EventDescriptionSystem.getInstance().addDescription(EventType.Classic_1v1, new SinglePlayersFightsDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.PartyvsParty, new PartyFightsDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.Korean, new KoreanDescription());
		EventDescriptionSystem.getInstance().addDescription(EventType.MiniTvT, new MiniTvTDescription());
	}
}
