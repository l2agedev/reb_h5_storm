package l2r.gameserver.randoms;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.RadarControl;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public final class Radar
{
	private Player player;
	private List<RadarMarker> markers;

	public Radar(Player player_)
	{
		player = player_;
		markers = new ArrayList<RadarMarker>();
	}

	// Add a marker to player's radar
	public void addMarker(int x, int y, int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);
		markers.add(newMarker);
		player.sendPacket(new RadarControl(2, 2, newMarker));
		player.sendPacket(new RadarControl(0, 1, newMarker));
	}

	public void addMarker(Location loc)
	{
		addMarker(loc.x, loc.y, loc.z);
	}

	// Remove a marker from player's radar
	public void removeMarker(int x, int y, int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);
		markers.remove(newMarker);
		player.sendPacket(new RadarControl(1, 1, newMarker));
	}

	public void removeAllMarkers()
	{
		for(RadarMarker tempMarker : markers)
			player.sendPacket(new RadarControl(2, 2, tempMarker));
		markers.clear();
	}

	public void loadMarkers()
	{
		player.sendPacket(new RadarControl(2, 2, player.getX(), player.getY(), player.getZ()));
		for(RadarMarker tempMarker : markers)
			player.sendPacket(new RadarControl(0, 1, tempMarker));
	}

	public class RadarMarker extends Location
	{
		// Simple class to model radar points.
		public int type;

		public RadarMarker(int type_, int x_, int y_, int z_)
		{
			super(x_, y_, z_);
			type = type_;
		}

		public RadarMarker(int x_, int y_, int z_)
		{
			super(x_, y_, z_);
			type = 1;
		}

		@Override
		public boolean equals(Object obj)
		{
			try
			{
				RadarMarker temp = (RadarMarker) obj;
				if(temp.x == x && temp.y == y && temp.z == z && temp.type == type)
					return true;
				return false;
			}
			catch(Exception e)
			{
				return false;
			}
		}
	}
}