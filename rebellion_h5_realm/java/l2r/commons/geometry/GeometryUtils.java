package l2r.commons.geometry;


public class GeometryUtils
{
	private GeometryUtils()
	{
		
	}
	
	public static boolean checkIfLinesIntersects(Point2D a, Point2D b, Point2D c, Point2D d)
	{
		return checkIfLinesIntersects(a, b, c, d, null);
	}

	/**
	 * public domain function by Darel Rex Finley, 2006<br>
	 * <br>
	 * Determines the intersection point of the line defined by points A and B with the<br>
	 * line defined by points C and D.
	 * 
	 * @return true if the intersection point was found<br>
	 *         false if there is no determinable intersection point<br>
	 *         r intersection point
	 */
	public static boolean checkIfLinesIntersects(Point2D a, Point2D b, Point2D c, Point2D d, Point2D r)
	{
		double distAB, theCos, theSin, newX, ABpos;

		//  Fail if either line is undefined.
		if(a.x == b.x && a.y == b.y || c.x == d.x && c.y == d.y)
			return false;

		//  (1) Translate the system so that point A is on the origin.
		double Bx = b.x - a.x;
		double By = b.y - a.y;
		double Cx = c.x - a.x;
		double Cy = c.y - a.y;
		double Dx = d.x - a.x;
		double Dy = d.y - a.y;

		//  Discover the length of segment A-B.
		distAB = Math.sqrt(Bx * Bx + By * By);

		//  (2) Rotate the system so that point B is on the positive X axis.
		theCos = Bx / distAB;
		theSin = By / distAB;
		newX = Cx * theCos + Cy * theSin;
		Cy = (int) (Cy * theCos - Cx * theSin);
		Cx = newX;
		newX = Dx * theCos + Dy * theSin;
		Dy = (int) (Dy * theCos - Dx * theSin);
		Dx = newX;

		//  Fail if the lines are parallel.
		if(Cy == Dy)
			return false;

		//  (3) Discover the position of the intersection point along line A-B.
		ABpos = Dx + (Cx - Dx) * Dy / (Dy - Cy);

		//  (4) Apply the discovered position to line A-B in the original coordinate system.
		if(r != null)
		{
			r.x = (int) (a.x + ABpos * theCos);
			r.y = (int) (a.y + ABpos * theSin);
		}

		//  Success.
		return true;
	}

	public static boolean checkIfLineSegementsIntersects(Point2D a, Point2D b, Point2D c, Point2D d)
	{
		return checkIfLineSegementsIntersects(a, b, c, d, null);
	}

	/**
	 * public domain function by Darel Rex Finley, 2006<br>
	 * <br>
	 * Determines the intersection point of the line segment defined by points A and B<br>
	 * with the line segment defined by points C and D.
	 * 
	 * @return true if the intersection point was found<br>
	 *         false if there is no determinable intersection point<br>
	 *         r intersection point
	 */
	public static boolean checkIfLineSegementsIntersects(Point2D a, Point2D b, Point2D c, Point2D d, Point2D r)
	{
		double distAB, theCos, theSin, newX, ABpos;

		//  Fail if either line is undefined.
		if(a.x == b.x && a.y == b.y || c.x == d.x && c.y == d.y)
			return false;

		//  Fail if the segments share an end-point.
		if(a.x == c.x && a.y == c.y || b.x == c.x && b.y == c.y || a.x == d.x && a.y == d.y || b.x == d.x && b.y == d.y)
			return false;

		//  (1) Translate the system so that point A is on the origin.
		double Bx = b.x - a.x;
		double By = b.y - a.y;
		double Cx = c.x - a.x;
		double Cy = c.y - a.y;
		double Dx = d.x - a.x;
		double Dy = d.y - a.y;

		//  Discover the length of segment A-B.
		distAB = Math.sqrt(Bx * Bx + By * By);

		//  (2) Rotate the system so that point B is on the positive X axis.
		theCos = Bx / distAB;
		theSin = By / distAB;
		newX = Cx * theCos + Cy * theSin;
		Cy = (int) (Cy * theCos - Cx * theSin);
		Cx = newX;
		newX = Dx * theCos + Dy * theSin;
		Dy = (int) (Dy * theCos - Dx * theSin);
		Dx = newX;

		//  Fail if segment C-D doesn't cross line A-B.
		if(Cy < 0. && Dy < 0. || Cy >= 0. && Dy >= 0.)
			return false;

		//  (3) Discover the position of the intersection point along line A-B.
		ABpos = Dx + (Cx - Dx) * Dy / (Dy - Cy);

		//  Fail if segment C-D crosses line A-B outside of segment A-B.
		if(ABpos < 0. || ABpos > distAB)
			return false;

		//  (4) Apply the discovered position to line A-B in the original coordinate system.
		if(r != null)
		{
			r.x = (int) (a.x + ABpos * theCos);
			r.y = (int) (a.y + ABpos * theSin);
		}

		//  Success.
		return true;
	}
	
	/** DRAW A CIRCLE!!!
	 * 			int density = 10;
				int centerX = activeChar.getX();
				int centerY = activeChar.getY();
				int radius = 250;
				Point3D prevPoint = null;
				for (int x = -radius; x <= radius; x += density)
				{
				   int y = (int) Math.sqrt(radius*radius - x*x);
				   Point3D newPoint = new Point3D(centerX+x, centerY+y, activeChar.getZ());
				   if (prevPoint != null)
					   packet.addLine(prevPoint, newPoint);
				   
				   prevPoint = newPoint;
				}
				for (int x = -radius; x <= radius; x += density)
				{
				   int y = (int) Math.sqrt(radius*radius - x*x);
				   Point3D newPoint = new Point3D(centerX-x, centerY-y, activeChar.getZ());
				   if (prevPoint != null)
					   packet.addLine(prevPoint, newPoint);
				   
				   prevPoint = newPoint;
				}
	 */
	
	/** DRAW A RECTANGLE!!!
	 * 			ExServerPrimitive packet = new ExServerPrimitive("", activeChar.getLoc());
				Rectangle rect = new Rectangle(activeChar.getX(), activeChar.getY(), 100, 200);
				PathIterator pi = rect.getPathIterator(null);
				int z = activeChar.getZ() + 5;
				Point3D oldPoint = null;
				Point3D newPoint = null;
				while (!pi.isDone())
				{
					double[] coords = new double[6];
					int type = pi.currentSegment(coords);
					switch (type) 
					{
						case PathIterator.SEG_MOVETO:
							oldPoint = new Point3D((int)coords[0], (int)coords[1], z);
							break;
						case PathIterator.SEG_LINETO:
							newPoint = new Point3D((int)coords[0], (int)coords[1], z);
							packet.addLine(oldPoint, newPoint);
							oldPoint = newPoint;
							break;
						case PathIterator.SEG_QUADTO:
							System.out.println("quadratic to " + coords[0] + ", "
									+ coords[1] + ", " + coords[2] + ", " + coords[3]);
							break;
						case PathIterator.SEG_CUBICTO:
							System.out.println("cubic to " + coords[0] + ", " + coords[1]
									+ ", " + coords[2] + ", " + coords[3] + ", "
									+ coords[4] + ", " + coords[5]);
							break;
						case PathIterator.SEG_CLOSE:
							oldPoint = null;
							newPoint = null;
							break;
						default:
							break;
					}
					pi.next();
				}
				activeChar.sendPacket(packet);
	 */
}
