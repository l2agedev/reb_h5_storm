package l2r.gameserver.network.serverpackets;

/**
 * @author VISTALL
 * type:
 * 0 - player name
 * 1 - clan name
 * reason
 * 0 - during the server merge, your character name, S1, conflicted with another. Your name may still be available. Please enter your desired name
 * 1 - name is incorrect
 */
public class ExNeedToChangeName extends L2GameServerPacket
{
	public static final int TYPE_PLAYER_NAME = 0;
	public static final int TYPE_CLAN_NAME = 1;

	public static final int REASON_EXISTS = 0;
	public static final int REASON_INVALID = 1;

	private int _type, _reason;
	private String _origName;

	public ExNeedToChangeName(int type, int reason, String origName)
	{
		_type = type;
		_reason = reason;
		_origName = origName;
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x69);
		writeD(_type);
		writeD(_reason);
		writeS(_origName);
	}
}