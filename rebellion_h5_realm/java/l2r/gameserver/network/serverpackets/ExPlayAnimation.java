package l2r.gameserver.network.serverpackets;

public class ExPlayAnimation extends L2GameServerPacket
{
	int _objectId;
	int _actionId;
	public ExPlayAnimation(int objectId, int actionId)
	{
		_objectId = objectId;
		_actionId = actionId;
	}
	@Override
	protected void writeImpl()
	{
		writeEx(0x5A);
		writeD(_objectId); // ObjectId
		writeC(0); // Does social action if set to 0. 1 does ???
		writeD(_actionId); // Social Action Id, Starts from 2 on players. 0 and 1 work on NPCs
		writeS(""); // TODO ???
	}
}