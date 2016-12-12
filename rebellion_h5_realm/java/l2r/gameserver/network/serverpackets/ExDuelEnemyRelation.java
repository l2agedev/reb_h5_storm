package l2r.gameserver.network.serverpackets;

public class ExDuelEnemyRelation extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x59);
		// just trigger
	}
}