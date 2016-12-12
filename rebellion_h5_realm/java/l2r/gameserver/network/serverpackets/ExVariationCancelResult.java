package l2r.gameserver.network.serverpackets;

public class ExVariationCancelResult extends L2GameServerPacket
{
	public static final L2GameServerPacket CLOSE = new ExVariationCancelResult(false, false);
	public static final L2GameServerPacket FAIL = new ExVariationCancelResult(true, false);
	public static final L2GameServerPacket SUCCESS = new ExVariationCancelResult(true, true);

	private boolean _openWindow;
	private boolean _result;

	public ExVariationCancelResult(boolean openWindow, boolean result)
	{
		_openWindow = openWindow;
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x58);
		writeD(_result);
		writeD(_openWindow);
	}
}