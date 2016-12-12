/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.serverpackets;

import l2r.commons.geometry.Point3D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nos
 */
public class ExServerPrimitive extends L2GameServerPacket
{
	private final String _name;
	private final Point3D _coord;
	private final List<Point> _points = new ArrayList<>();
	private final List<Line> _lines = new ArrayList<>();
	
	public ExServerPrimitive(String name, int x, int y, int z)
	{
		_name = name;
		_coord = new Point3D(x,y,z);
	}
	
	public ExServerPrimitive(String name, Point3D coord)
	{
		_name = name;
		_coord = coord;
	}
	
	public void addPoint(String name, int color, boolean isNameColored, Point3D coord)
	{
		_points.add(new Point(name, color, isNameColored, coord));
	}
	
	public void addPoint(String name, Color color, boolean isNameColored, Point3D coord)
	{
		addPoint(name, color.getRGB(), isNameColored, coord);
	}
	
	public void addPoint(String name, int color, Point3D coord)
	{
		addPoint(name, color, true, coord);
	}
	
	public void addPoint(String name, Color color, Point3D coord)
	{
		addPoint(name, color.getRGB(), true, coord);
	}
	
	public void addPoint(String name, Point3D coord)
	{
		addPoint(name, Color.GREEN, true, coord);
	}
	
	public void addPoint(int color, Point3D coord)
	{
		addPoint("", color, false, coord);
	}
	
	public void addPoint(Color color, Point3D coord)
	{
		addPoint("", color.getRGB(), false, coord);
	}
	
	public void addPoint(Point3D coord)
	{
		addPoint("", Color.GREEN, false, coord);
	}
	
	public void addLine(String name, int color, boolean isNameColored, Point3D coord1, Point3D coord2)
	{
		_lines.add(new Line(name, color, isNameColored, coord1, coord2));
	}
	
	public void addLine(String name, Color color, boolean isNameColored, Point3D coord1, Point3D coord2)
	{
		addLine(name, color.getRGB(), isNameColored, coord1, coord2);
	}
	
	public void addLine(String name, int color, Point3D coord1, Point3D coord2)
	{
		addLine(name, color, true, coord1, coord2);
	}
	
	public void addLine(String name, Color color, Point3D coord1, Point3D coord2)
	{
		addLine(name, color.getRGB(), true, coord1, coord2);
	}
	
	public void addLine(String name, Point3D coord1, Point3D coord2)
	{
		addLine(name, Color.GREEN, true, coord1, coord2);
	}
	
	public void addLine(int color, Point3D coord1, Point3D coord2)
	{
		addLine("", color, false, coord1, coord2);
	}
	
	public void addLine(Color color, Point3D coord1, Point3D coord2)
	{
		addLine("", color.getRGB(), false, coord1, coord2);
	}
	
	public void addLine(Point3D coord1, Point3D coord2)
	{
		addLine("", Color.GREEN, false, coord1, coord2);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x11);
		writeS(_name);
		writeD(_coord.x);
		writeD(_coord.y);
		writeD(_coord.z);
		writeD((int) (Math.sqrt(2) * 1024));
		writeD(32000);
		
		writeD(_points.size() + _lines.size());
		
		for (Point point : _points)
		{
			writeC(1); // Its the type in this case Point
			writeS(point.getName());
			int color = point.getColor();
			writeD((color >> 16) & 0xFF); // R
			writeD((color >> 8) & 0xFF); // G
			writeD(color & 0xFF); // B
			writeD(point.isNameColored() ? 1 : 0);
			writeD(point.getX());
			writeD(point.getY());
			writeD(point.getZ());
		}
		
		for (Line line : _lines)
		{
			writeC(2); // Its the type in this case Line
			writeS(line.getName());
			int color = line.getColor();
			writeD((color >> 16) & 0xFF); // R
			writeD((color >> 8) & 0xFF); // G
			writeD(color & 0xFF); // B
			writeD(line.isNameColored() ? 1 : 0);
			writeD(line.getX());
			writeD(line.getY());
			writeD(line.getZ());
			writeD(line.getX2());
			writeD(line.getY2());
			writeD(line.getZ2());
		}
	}
	
	private static class Point
	{
		private final String _name;
		private final int _color;
		private final boolean _isNameColored;
		private final Point3D _coord;
		
		public Point(String name, int color, boolean isNameColored, Point3D coord)
		{
			_name = name;
			_color = color;
			_isNameColored = isNameColored;
			_coord = coord;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public int getColor()
		{
			return _color;
		}
		
		public boolean isNameColored()
		{
			return _isNameColored;
		}
		
		public int getX()
		{
			return _coord.x;
		}
		
		public int getY()
		{
			return _coord.y;
		}
		
		public int getZ()
		{
			return _coord.z;
		}
	}
	
	private static class Line extends Point
	{
		private final Point3D _coord2;
		
		public Line(String name, int color, boolean isNameColored, Point3D coord1, Point3D coord2)
		{
			super(name, color, isNameColored, coord1);
			_coord2 = coord2;
		}
		
		public int getX2()
		{
			return _coord2.x;
		}
		
		public int getY2()
		{
			return _coord2.y;
		}
		
		public int getZ2()
		{
			return _coord2.z;
		}
		
	}
}