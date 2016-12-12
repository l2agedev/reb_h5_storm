package l2r.gameserver.data.xml.holder;

import l2r.commons.data.xml.AbstractHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.templates.ZoneTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author G1ta0
 */
public class ZoneHolder extends AbstractHolder
{
	private static final ZoneHolder _instance = new ZoneHolder();

	private final Map<String, ZoneTemplate> _zones = new HashMap<String, ZoneTemplate>();

	public static ZoneHolder getInstance()
	{
		return _instance;
	}

	public void addTemplate(ZoneTemplate zone)
	{
		_zones.put(zone.getName(), zone);
	}

	public ZoneTemplate getTemplate(String name)
	{
		return _zones.get(name);
	}

	public Map<String, ZoneTemplate> getZones()
	{
		return _zones;
	}
	
	/**
	 * Использовать акуратно возращает дверь нулевого рефлекта
	 * @param id
	 * @return
	 */
	public static DoorInstance getDoor(int id)
	{
		return ReflectionManager.DEFAULT.getDoor(id);
	}

	/**
	 * Использовать акуратно возращает зону нулевого рефлекта
	 * @param name
	 * @return
	 */
	public static Zone getZone(String name)
	{
		return ReflectionManager.DEFAULT.getZone(name);
	}
	
	public static List<Zone> getZonesByType(ZoneType zoneType)
	{
		Collection<Zone> zones = ReflectionManager.DEFAULT.getZones();
		if(zones.isEmpty())
			return Collections.emptyList();

		List<Zone> zones2 = new ArrayList<Zone>(5);
		for(Zone z : zones)
			if(z.getType() == zoneType)
				zones2.add(z);

		return zones2;
	}

	@Override
	public int size()
	{
		return _zones.size();
	}

	@Override
	public void clear()
	{
		_zones.clear();
	}
}
