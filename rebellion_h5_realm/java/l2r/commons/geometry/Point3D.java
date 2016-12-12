package l2r.commons.geometry;

public class Point3D extends Point2D
{
	public static final Point3D[] EMPTY_ARRAY = new Point3D[0];
	public int z;

	public Point3D()
	{}

	public Point3D(int x, int y, int z)
	{
		super(x, y);
		this.z = z;
	}

	public int getZ()
	{
		return z;
	}
	
	public Point3D setX(int x)
	{
		this.x = x;
		return this;
	}
	
	public Point3D setY(int y)
	{
		this.y = y;
		return this;
	}
	
	public Point3D setZ(int z)
	{
		this.z = z;
		return this;
	}
	
	public Point3D set(int x, int y)
	{
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Point3D set(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Point3D changeX(int xDiff)
	{
		x += xDiff;
		return this;
	}
	
	public Point3D changeY(int yDiff)
	{
		y += yDiff;
		return this;
	}

	public Point3D changeZ(int zDiff)
	{
		z += zDiff;
		return this;
	}
	
	public double distance3D(Point3D target)
	{
		return distance3D(target.x, target.y, target.z);
	}
	
	public double distance3D(int tx, int ty, int tz)
	{
		long dx = x - tx;
		long dy = y - ty;
		long dz = z - tz;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public static Point3D coordsRandomize(int x, int y, int z, int radiusmin, int radiusmax)
	{
		return new Point3D(x, y, z).coordsRandomize(radiusmin, radiusmax);
	}
	
	public static Point3D coordsRandomize(int x, int y, int z, int radius)
	{
		return coordsRandomize(x, y, z, 0, radius);
	}

	@Override
	public Point3D clone()
	{
		return new Point3D(this.x, this.y, this.z);
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == this)
			return true;
		if(o == null)
			return false;
		if(o.getClass() != getClass())
			return false;
		return equals((Point3D) o);
	}

	public boolean equals(Point3D p)
	{
		return equals(p.x, p.y, p.z);
	}

	public boolean equals(int x, int y, int z)
	{
		return (this.x == x) && (this.y == y) && (this.z == z);
	}

	@Override
	public String toString()
	{
		return "[x: " + this.x + " y: " + this.y + " z: " + this.z + "]";
	}
}