package l2r.gameserver.network.serverpackets;

public class ExShowPetitionHtml extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0xB1);
		// TODO dx[dcS]
	}
}