package l2r.gameserver.geodata;

import l2r.commons.geometry.Shape;

public interface GeoCollision
{
	public Shape getShape();

	public byte[][] getGeoAround();

	public void setGeoAround(byte[][] geo);

	public boolean isConcrete();
}
