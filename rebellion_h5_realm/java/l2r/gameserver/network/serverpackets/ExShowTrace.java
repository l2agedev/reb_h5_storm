package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.GameObject;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;


public class ExShowTrace extends L2GameServerPacket
{
	private final List<Trace> _traces = new ArrayList<Trace>();

	static final class Trace
	{
		public final int _x;
		public final int _y;
		public final int _z;
		public final int _time;

		public Trace(int x, int y, int z, int time)
		{
			_x = x;
			_y = y;
			_z = z;
			_time = time;
		}
	}
	
	public void addTrace(int x, int y, int z, int time)
	{
		_traces.add(new Trace(x, y, z, time));
	}
	
	public void addTrace(Location loc, int time)
	{
		_traces.add(new Trace(loc.getX(), loc.getY(), loc.getZ(), time));
	}
	
	public void addLine(Location from, Location to, int step)
	{
		addLine(from.x, from.y, from.z, to.x, to.y, to.z, step, 0);
	}
	
	public void addTrace(GameObject obj, int time)
	{
		addTrace(obj.getX(), obj.getY(), obj.getZ(), time);
	}

	public void addLine(Location from, Location to, int step, int time)
	{
		addLine(from.x, from.y, from.z, to.x, to.y, to.z, step, time);
	}

	public void addLine(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, int step, int time)
	{
		int Xdiff = toX - fromX;
		int Ydiff = toY - fromY;
		int Zdiff = toZ - fromZ;
		double XYdist = Math.sqrt(Xdiff * Xdiff + Ydiff * Ydiff);
		double full_dist = Math.sqrt(XYdist * XYdist + Zdiff * Zdiff);
		int steps = (int) (full_dist / step);

		addTrace(fromX, fromY, fromZ, time);
		if(steps > 1)
		{
			int stepX = Xdiff / steps;
			int stepY = Ydiff / steps;
			int stepZ = Zdiff / steps;

			for(int i = 1; i < steps; i++)
				addTrace(fromX + stepX * i, fromY + stepY * i, fromZ + stepZ * i, time);
		}
		addTrace(toX, toY, toZ, time);
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x67);
		
		writeH(0); // type broken in H5
		writeD(0); // time broken in H5
		writeH(_traces.size());
		for(Trace t : _traces)
		{
			writeD(t._x);
			writeD(t._y);
			writeD(t._z);
			writeH(t._time);
		}
	}
}