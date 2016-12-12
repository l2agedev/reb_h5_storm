/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.stats;

import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.ShowBoardData;


/**
 * @author hNoke
 *
 */
public abstract class EventStats
{
	public EventStats()
	{
		
	}
	
	public void showHtmlText(PlayerEventInfo player, String text)
	{
		if (text.length() < 8180)
		{
			ShowBoardData sb = new ShowBoardData(text, "101", player.getOwner());
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(null, "102", player.getOwner());
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(null, "103", player.getOwner());
			sb.sendToPlayer(player);
		}
		else if (text.length() < 8180 * 2)
		{
			ShowBoardData sb = new ShowBoardData(text.substring(0, 8180), "101", player.getOwner());
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(text.substring(8180, text.length()), "102", player.getOwner());
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(null, "103", player.getOwner());
			sb.sendToPlayer(player);
		}
		else if (text.length() < 8180 * 3)
		{
			ShowBoardData sb = new ShowBoardData(text.substring(0, 8180), "101", player.getOwner());
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(text.substring(8180, 8180 * 2), "102", player.getOwner());
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(text.substring(8180 * 2, text.length()), "103", player.getOwner());
			sb.sendToPlayer(player);
		}
	}
	
	public abstract void load();
	public abstract void onLogin(PlayerEventInfo player);
	public abstract void onDisconnect(PlayerEventInfo player);
	public abstract void onCommand(PlayerEventInfo player, String command);
	public abstract void statsChanged(PlayerEventInfo player);
}
