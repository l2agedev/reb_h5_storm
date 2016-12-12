package l2r.gameserver.instancemanager;

import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.entity.Coliseum;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

// TODO: zones for underground coliseum, restart points, spawn points etc.
// TODO: unregister and safe checks on register.
public class UnderGroundColliseumManager
{
	protected static Logger _log = Logger.getLogger(UnderGroundColliseumManager.class.getName());

	private static UnderGroundColliseumManager _instance;

	private HashMap<String, Coliseum> _coliseums;

	public static UnderGroundColliseumManager getInstance()
	{
		if(_instance == null)
			_instance = new UnderGroundColliseumManager();
		return _instance;
	}

	public UnderGroundColliseumManager()
	{
		List<Zone> zones = ZoneHolder.getZonesByType(ZoneType.UnderGroundColiseum);
		if(zones.size() == 0)
			_log.info("Not found zones for UnderGround Colliseum!!!");
		else
			for(Zone zone : zones)
				getColiseums().put(zone.getName(), new Coliseum());
		
		_log.info("Loaded: " + getColiseums().size() + " UnderGround Colliseums.");
	}

	public HashMap<String, Coliseum> getColiseums()
	{
		if(_coliseums == null)
			_coliseums = new HashMap<String, Coliseum>();
		return _coliseums;
	}

	public Coliseum getColiseumByLevelLimit(final int limit)
	{
		for(Coliseum coliseum : _coliseums.values())
			if(coliseum.getMaxLevel() == limit)
				return coliseum;
		return null;
	}
}