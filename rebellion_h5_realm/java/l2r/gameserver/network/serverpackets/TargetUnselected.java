package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.GameObject;
import l2r.gameserver.utils.Location;

/**
 * format  ddddd
 */
public class TargetUnselected extends L2GameServerPacket
{
	private int _targetId;
	private Location _loc;

	public TargetUnselected(GameObject obj)
	{
		_targetId = obj.getObjectId();
		_loc = obj.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x24);
		writeD(_targetId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(0x00); // иногда бывает 1
	}
}