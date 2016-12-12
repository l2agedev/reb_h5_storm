package l2r.gameserver.instancemanager;

import l2r.gameserver.data.xml.holder.DoorHolder;
import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.CommandChannel;
import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.PlayerGroup;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReflectionManager
{
	//public static final int DYNAMIC_REFLECTION_START_ID = 100;
	public static final int DEFAULT_ID = 0;
	public static final Reflection DEFAULT = Reflection.createReflection(DEFAULT_ID);
	public static final Reflection PARNASSUS = Reflection.createReflection(-1);
	public static final Reflection GIRAN_HARBOR = Reflection.createReflection(-2);
	public static final Reflection JAIL = Reflection.createReflection(-3);
	public static final Reflection CTF_EVENT = Reflection.createReflection(-4);
	public static final Reflection TVT_EVENT = Reflection.createReflection(-5);

	private static final ReflectionManager _instance = new ReflectionManager();

	public static ReflectionManager getInstance()
	{
		return _instance;
	}

	private final TIntObjectHashMap<Reflection> _reflections = new TIntObjectHashMap<Reflection>();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	private ReflectionManager()
	{
		add(DEFAULT);
		add(PARNASSUS);
		add(GIRAN_HARBOR);
		add(JAIL);
		add(CTF_EVENT);
		add(TVT_EVENT);

		// создаем в рефлекте все зоны, и все двери
		DEFAULT.init(DoorHolder.getInstance().getDoors(), ZoneHolder.getInstance().getZones());

		JAIL.setCoreLoc(new Location(-114648, -249384, -2984));
	}

	public Reflection get(int id)
	{
		readLock.lock();
		try
		{
			return _reflections.get(id);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public Reflection add(Reflection ref)
	{
		writeLock.lock();
		try
		{
			return _reflections.put(ref.getId(), ref);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public Reflection remove(Reflection ref)
	{
		writeLock.lock();
		try
		{
			return _reflections.remove(ref.getId());
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public Reflection[] getAll()
	{
		readLock.lock();
		try
		{
			return _reflections.values(new Reflection[_reflections.size()]);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public int getCountByIzId(int izId)
	{
		readLock.lock();
		try
		{
			int i = 0;
			for(Reflection r : getAll())
				if(r.getInstancedZoneId() == izId)
					i++;
			return i;
		}
		finally
		{
			readLock.unlock();
		}
	}

	public int size()
	{
		return _reflections.size();
	}
	
	public static Reflection enterReflection(Player invoker, int instancedZoneId)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		return enterReflection(invoker, new Reflection(), iz);
	}

	public static Reflection enterReflection(Player invoker, Reflection r, int instancedZoneId)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		return enterReflection(invoker, r, iz);
	}

	public static Reflection enterReflection(Player invoker, Reflection r, InstantZone iz)
	{
		r.init(iz);

		if(r.getReturnLoc() == null)
			r.setReturnLoc(invoker.getLoc());

		final Party party = invoker.getParty();
		if (party != null)
		{
			final CommandChannel cc = party.getCommandChannel();
			if (cc != null)
			{
				cc.setReflection(r);
				r.setCommandChannel(cc);
			}
			else
			{
				party.setReflection(r);
				r.setParty(party);
			}
		}
		
		final PlayerGroup pg = invoker.getPlayerGroup();
		for(Player member : pg)
		{
			if (!member.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
			{
				if(iz.getRemovedItemId() > 0)
					ItemFunctions.removeItem(member, iz.getRemovedItemId(), iz.getRemovedItemCount(), true);
				if(iz.getGiveItemId() > 0)
					ItemFunctions.addItem(member, iz.getGiveItemId(), iz.getGiveItemCount(), true);
				if(iz.isDispelBuffs())
					member.dispelBuffs();
				if(iz.getSetReuseUponEntry())
					member.setInstanceReuse(iz.getId(), System.currentTimeMillis());
			}
			
			member.setVar("backCoords", invoker.getLoc().toXYZString(), -1);
			member.teleToLocation(iz.getTeleportCoord(), r);
		}

		return r;
	}
}