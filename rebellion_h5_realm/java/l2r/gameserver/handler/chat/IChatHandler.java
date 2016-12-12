package l2r.gameserver.handler.chat;

import l2r.gameserver.network.serverpackets.components.ChatType;

public interface IChatHandler
{
	void say();

	ChatType getType();
}
