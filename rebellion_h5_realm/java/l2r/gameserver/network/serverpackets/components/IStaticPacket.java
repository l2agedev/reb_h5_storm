package l2r.gameserver.network.serverpackets.components;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;

public interface IStaticPacket
{
	L2GameServerPacket packet(Player player);
}
