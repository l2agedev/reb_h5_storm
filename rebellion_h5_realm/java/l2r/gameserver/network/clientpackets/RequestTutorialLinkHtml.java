package l2r.gameserver.network.clientpackets;

import l2r.gameserver.achievements.Achievements;
import l2r.gameserver.auction.AuctionManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.quest.Quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	// format: cS
	private static final Logger _log = LoggerFactory.getLogger(RequestTutorialLinkHtml.class);
	String _bypass;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest q = QuestManager.getQuest(255);
		if(q != null)
			player.processQuestEvent(q.getName(), _bypass, null);
		
		if(_bypass.startsWith("_bbs_achievements")) 
		{ 
			_bypass = _bypass.replaceAll("%", " "); 
			
			if(_bypass.length() < 5) 
			{ 
				_log.warn("Bad Script bypass!"); 
				return; 
			} 
			Achievements.getInstance().usebypass(player, _bypass, null); 
		}
		else if(_bypass.startsWith("_bbs_Auction"))
		{
			_bypass = _bypass.replaceAll("%", " ");
			AuctionManager.usebypass(player, _bypass);
		}
	}
}