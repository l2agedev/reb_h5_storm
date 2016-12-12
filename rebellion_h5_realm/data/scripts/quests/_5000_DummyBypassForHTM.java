/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests;

import l2r.gameserver.Config;
import l2r.gameserver.auction.AuctionManager;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;

import services.community.CareerManager;
import services.community.CommunityRaidBoss;

public class _5000_DummyBypassForHTM extends Quest implements ScriptFile
{
	private static final int GATEKEEPER_ID = Config.CB_NPC_GATEKEEPER_ID;
	private static final int GMSHOP_ID = Config.CB_NPC_GMSHOP_ID;
	private static final int BUFFER_ID = Config.CB_NPC_BUFFER_ID;
	private static final int AUCTION_ID = Config.CB_NPC_AUCTION_ID;
	private static final int RBSTATUS_ID = Config.CB_NPC_RBSTATUS_ID;
	// Cat
	private static final int CLASS_CAT_ID = Config.CB_NPC_CLASS_MASTER_ID;
	
	public _5000_DummyBypassForHTM()
	{
		super(PARTY_NONE);
		
		loadNpc();
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		talk(npc, st.getPlayer());
		return null;
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		talk(npc, player);
		return null;
	}
	
	private void talk(NpcInstance npc, Player player)
	{
		String html = null;
		
		Object[] arg2 = { player };
		
		if (npc.getNpcId() == CLASS_CAT_ID)
		{
			if (!Config.CLASS_MASTER_NPC)
				CareerManager.getInstance().onBypassCommand(player, "_bbscareer;");
			return;
		}
		
		if (npc.getNpcId() == AUCTION_ID)
		{
			AuctionManager.getAllAuctions(player, 1);
			return;
		}
		
		if (npc.getNpcId() == BUFFER_ID)
		{
			Functions.callScripts("services.community.BuffManager", "showMainWindow", arg2);
			return;
		}
		
		if (npc.getNpcId() == RBSTATUS_ID)
		{
			CommunityRaidBoss.getInstance().onBypassCommand(player, "_bbsraidboss");
			return;
		}
		
		if (npc.getNpcId() == GATEKEEPER_ID)
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/teleport/teleport.htm", player);
		
		if (npc.getNpcId() == GMSHOP_ID)
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/gmshop.htm", player);
		
		if (html != null)
			ShowBoard.separateAndSend(html, player);
	}

	private void loadNpc()
	{
		addFirstTalkId(GATEKEEPER_ID);
		addTalkId(GATEKEEPER_ID);
		addStartNpc(GATEKEEPER_ID);
		addFirstTalkId(GMSHOP_ID);
		addTalkId(GMSHOP_ID);
		addStartNpc(GMSHOP_ID);
		addFirstTalkId(BUFFER_ID);
		addTalkId(BUFFER_ID);
		addStartNpc(BUFFER_ID);
		addFirstTalkId(AUCTION_ID);
		addTalkId(AUCTION_ID);
		addStartNpc(AUCTION_ID);
		addFirstTalkId(CLASS_CAT_ID);
		addTalkId(CLASS_CAT_ID);
		addStartNpc(CLASS_CAT_ID);
		addFirstTalkId(RBSTATUS_ID);
		addTalkId(RBSTATUS_ID);
		addStartNpc(RBSTATUS_ID);
	}
	
	@Override
	public void onLoad()
	{
	}

	@Override
	public void onReload()
	{
	}

	@Override
	public void onShutdown()
	{
	}
}
