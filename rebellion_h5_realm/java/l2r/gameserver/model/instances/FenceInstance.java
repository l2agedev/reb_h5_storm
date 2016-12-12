/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2r.gameserver.model.instances;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExColosseumFenceInfoPacket;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.utils.Location;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KKnD
 */
public final class FenceInstance extends GameObject
{
	private FenceInstance _fence;
	private int _type;
	private int _width;
	private int _length;
	private int _height;
	private int _xLoc, _yLoc, _zLoc, _mapId;
	private Rectangle _shape;
	
	public FenceInstance(int objectId)
	{
		super(objectId);
	}
	
	public FenceInstance(int objectId, int type, int width, int length, int x, int y, int z, int eventId)
	{
		super(objectId);
		_fence = this;
		
		setType(type);
		setWidth(width);
		setlength(length);
		setXloc(x);
		setYloc(y);
		setZloc(z);
		_height = 50;
		_shape = new Rectangle(_xLoc, _yLoc, _width, _length);
		setMapId(eventId);
	}
	
	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer)
	{
		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		list.add(new ExColosseumFenceInfoPacket(this));
		return list;
	}
	
	public void setFence(FenceInstance fence)
	{
		_fence = fence;
	}
	
	public FenceInstance getFence()
	{
		return _fence;
	}
	
	public void setType(int type)
	{
		_type = type;
	}
	
	public void setWidth(int width)
	{
		_width = width;
	}
	
	public void setlength(int length)
	{
		_length = length;
	}
	
	public void setXloc(int xloc)
	{
		_xLoc = xloc;
	}
	
	public void setYloc(int yloc)
	{
		_yLoc = yloc;
	}
	
	public void setZloc(int zloc)
	{
		_zLoc = zloc;
	}
	
	public void setMapId(int mapid)
	{
		_mapId = mapid;
	}
	
	public int getXLoc()
	{
		return _xLoc;
	}
	
	public int getYLoc()
	{
		return _yLoc;
	}
	
	public int getZLoc()
	{
		return _zLoc;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public int getMapId()
	{
		return _mapId;
	}
	
	public int getWidth()
	{
		return _width;
	}
	
	public int getLength()
	{
		return _length;
	}
	
	@Override
	public boolean isFence()
	{
		return true;
	}
	
	public boolean isLocInside(Location loc)
	{
		boolean isInsideZ = loc.getZ() >= getZLoc() && loc.getZ() <= getZLoc() + _height;
		return _shape.contains(loc.getX(), loc.getY()) && isInsideZ;// && (getReflectionId() == loc.getInstanceId());
	}
	
	public boolean isInside(Creature creature)
	{
		boolean isInsideZ = creature.getZ() >= getZLoc() && creature.getZ() <= getZLoc() + _height;
		return _shape.contains(creature.getX(), creature.getY()) && isInsideZ && (getReflectionId() == creature.getReflectionId());
	}
	
	public boolean isLocOutside(Location loc)
	{
		return !isLocInside(loc);
	}
	
	public boolean isOutside(Creature creature)
	{
		return !isInside(creature);
	}
}
