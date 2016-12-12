package l2r.gameserver.network.serverpackets;


public class ExVariationResult extends L2GameServerPacket
{
	public static final ExVariationResult CLOSE = new ExVariationResult();
	public static final ExVariationResult FAIL = new ExVariationResult(new int[2]);
	private int[] _augmentations;
	private boolean _openWindow;

	public ExVariationResult(int[] augmentations)
	{
		_augmentations = augmentations;
		_openWindow = true;
	}

	public ExVariationResult()
	{
		_augmentations = new int[2];
		_openWindow = false;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x56);
		writeD(_augmentations[0]);
		writeD(_augmentations[1]);
		writeD(_openWindow);
	}
}