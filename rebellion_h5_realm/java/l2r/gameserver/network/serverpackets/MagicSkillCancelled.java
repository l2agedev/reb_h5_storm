package l2r.gameserver.network.serverpackets;

public class MagicSkillCancelled extends L2GameServerPacket
{

	private int _objectId;

	public MagicSkillCancelled(int objectId)
	{
		_objectId = objectId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x49);
		writeD(_objectId);
	}
}