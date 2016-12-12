package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

/**
 * Format SSSSSS
 */
public class RequestBBSwrite extends L2GameClientPacket
{
	private String _url;
	private String _arg1;
	private String _arg2;
	private String _arg3;
	private String _arg4;
	private String _arg5;

	@Override
	public void readImpl()
	{
		_url = readS();
		_arg1 = readS();
		_arg2 = readS();
		_arg3 = readS();
		_arg4 = readS();
		_arg5 = readS();
	}

	@Override
	public void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(_url);
		if(handler != null)
		{
			if(!Config.COMMUNITYBOARD_ENABLED)
				activeChar.sendPacket(new SystemMessage2(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE));
			else
				handler.onWriteCommand(activeChar, _url, _arg1, _arg2, _arg3, _arg4, _arg5);
		}
	}
}