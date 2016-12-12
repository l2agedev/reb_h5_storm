package services;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.randoms.WebForum;
import l2r.gameserver.scripts.Functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebLeech extends Functions
{
	public static final Logger _log = LoggerFactory.getLogger(WebLeech.class);
	
	public void change(String[] param)
	{
		String actualCommand = param[0]; // Get actual command
		
		Player activeChar = getSelf();
		if (activeChar == null)
			return;
		
		if (!Config.ALLOW_BOARD_NEWS_LEECH)
			return;
		
		if (actualCommand.equalsIgnoreCase("viewforumtopic"))
		{
			try
			{
				String topic = param[1];
				
				String html = WebForum.getTopicContent(Integer.parseInt(topic));
				//Util.sendCBHtml(activeChar, html);
				ShowBoard.separateAndSend(html, activeChar);
			}
			catch (Exception e) 
			{
				_log.warn("WebLecch: Error while sending forum topic to community board.", e);
			}
		}
		else if (actualCommand.equalsIgnoreCase("bbschangelog"))
		{
			//Util.sendCBHtml(activeChar, WebForum.CHANGELOG_HTML);
			ShowBoard.separateAndSend(WebForum.CHANGELOG_HTML, activeChar);
		}
		else if (actualCommand.equalsIgnoreCase("bbsannouncements"))
		{
			//Util.sendCBHtml(activeChar, WebForum.ANNOUNCEMENTS_HTML);
			ShowBoard.separateAndSend(WebForum.ANNOUNCEMENTS_HTML, activeChar);
		}
		else if (actualCommand.equalsIgnoreCase("test"))
		{
			WebForum.updateAnnouncementsData();
			activeChar.sendMessage(new CustomMessage("scripts.services.webleech.done", activeChar));
		}
	}
}