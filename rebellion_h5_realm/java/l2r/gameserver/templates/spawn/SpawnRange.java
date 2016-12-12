package l2r.gameserver.templates.spawn;

import l2r.gameserver.utils.Location;

public interface SpawnRange
{
	Location getRandomLoc(int geoIndex);
}
