package l2r.gameserver.network.serverpackets;

import l2r.commons.geometry.Point3D;


public class ShowRadar extends L2GameServerPacket
{
	private boolean _showRadar;
	private boolean _showRedFlag;
	private int _x, _y, _z;
	
	public ShowRadar(boolean showRadar, boolean showRedFlag, int x, int y, int z)
	{
		_showRadar = showRadar;
		_showRedFlag = showRedFlag;
		_x = x;
		_y = y;
		_z = z;
	}
	
	public ShowRadar(boolean showRadar, boolean showRedFlag, Point3D point)
	{
		_showRadar = showRadar;
		_showRedFlag = showRedFlag;
		_x = point.x;
		_y = point.y;
		_z = point.z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xAA);
		writeD(_showRadar ? 0 : 1);
		writeD(_showRedFlag ? 1 : 0); // 0 = Nothing, 1 = Redflag.
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}