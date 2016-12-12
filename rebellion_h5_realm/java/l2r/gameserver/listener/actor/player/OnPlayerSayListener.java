package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;

/**
 * @author VISTALL
 * @date 20:45/15.09.2011
 */
public interface OnPlayerSayListener extends PlayerListener
{
	void onSay(Player activeChar, ChatType type, String target, String text);
}
