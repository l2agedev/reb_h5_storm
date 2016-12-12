package l2r.gameserver.network.serverpackets;

public class JoinParty extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL = new JoinParty(0, false);

	private int _response;
	private boolean _hasSummon;

	public JoinParty(int response, boolean hasSummon)
	{
		_response = response;
		_hasSummon = hasSummon;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x3A);
		writeD(_response);
		writeD(_hasSummon);
	}
}