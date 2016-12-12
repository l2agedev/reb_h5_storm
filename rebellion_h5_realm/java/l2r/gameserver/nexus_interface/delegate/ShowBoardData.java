package l2r.gameserver.nexus_interface.delegate;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.nexus_engine.l2r.delegate.IShowBoardData;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

/**
 * @author hNoke
 *
 */
public class ShowBoardData implements IShowBoardData
{
	private ShowBoard _board;
	
	public ShowBoardData(ShowBoard sb)
	{
		_board = sb;
	}
	
	public ShowBoardData(String text, String id, Player player)
	{
		_board = new ShowBoard(text, id, player);
	}
	
	@Override
	public void sendToPlayer(PlayerEventInfo player)
	{
		player.getOwner().sendPacket(_board);
	}
}
