package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.actor.instances.player.Friend;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

import java.util.Map;

public class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if (activeChar.getEventInfo().hasAntifeedProtection())
		{
			activeChar.sendMessage("You may not open your friend list while you're on an event.");
			return;
		}
		
		activeChar.sendPacket(SystemMsg.FRIENDS_LIST);
		Map<Integer, Friend> _list = activeChar.getFriendList().getList();
		for(Map.Entry<Integer, Friend> entry : _list.entrySet())
		{
			Player friend = World.getPlayer(entry.getKey());
			if(friend != null)
				activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CURRENTLY_ONLINE).addName(friend));
			else
				activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CURRENTLY_OFFLINE).addString(entry.getValue().getName()));
		}
		activeChar.sendPacket(SystemMsg.__EQUALS__);
	}
}