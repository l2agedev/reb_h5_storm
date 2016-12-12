package l2r.gameserver.nexus_interface;

import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.DoorHolder;
import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.WorldRegion;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.model.instances.FenceInstance;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.nexus_engine.events.engine.base.Loc;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_engine.l2r.INexusOut;
import l2r.gameserver.nexus_interface.delegate.DoorData;
import l2r.gameserver.nexus_interface.delegate.FenceData;
import l2r.gameserver.nexus_interface.delegate.InstanceData;
import l2r.gameserver.nexus_interface.handlers.AdminCommandHandlerInstance;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.templates.item.ItemTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public class NexusOut implements INexusOut
{
	public void load()
	{
		CallBack.getInstance().setNexusOut(this);
	}
	
	@Override
	public ScheduledFuture<?> scheduleGeneral(Runnable task, long delay)
	{
		return ThreadPoolManager.getInstance().schedule(task, delay);
	}
	
	@Override
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable task, long initial, long delay)
	{
		return ThreadPoolManager.getInstance().scheduleAtFixedRate(task, initial, delay);
	}
	
	@Override
	public void executeTask(Runnable task)
	{
		ThreadPoolManager.getInstance().execute(task);
	}
	

	@Override
	public void purge()
	{
		// TODO: Infern0 check this ThreadPoolManager.getInstance().purge();
	}
	
	@Override
	public int getNextObjectId()
	{
		return IdFactory.getInstance().getNextId();
	}
	
	@Override
	public int random(int min, int max)
	{
		return Rnd.get(min, max);
	}
	
	@Override
	public int random(int max)
	{
		return Rnd.get(0, max);
	}
	
	@Override
	public Connection getConnection()
	{
		try
		{
			return DatabaseFactory.getInstance().getConnection();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public InstanceData createInstance(String name, int duration, int emptyDestroyTime)
	{
		Reflection _ref = new Reflection();
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(614);
		_ref.init(iz);
		_ref.startCollapseTimer(duration);
		
		if(emptyDestroyTime > 0)
			_ref.setCollapseIfEmptyTime(emptyDestroyTime);
		return new InstanceData(_ref);
	}
	
	@Override
	public void addDoorToInstance(int instanceId, int doorId, boolean opened)
	{
		Reflection ref = ReflectionManager.getInstance().get(instanceId);

		ReflectionManager.getInstance().get(instanceId).addDoor(doorId, ref, opened);
	}
	
	@Override
	public DoorData[] getInstanceDoors(int instanceId)
	{
		Reflection ref = ReflectionManager.getInstance().get(instanceId);
		List<DoorData> doors = new FastTable<DoorData>();
		for(DoorInstance d : ref.getDoors())
		{
			doors.add(new DoorData(d));
		}
		return doors.toArray(new DoorData[doors.size()]);
	}
	
	
	@Override
	public void registerAdminCommandHandler(AdminCommandHandlerInstance handler)
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(handler);
	}
	
	// ***
	
	@Override
	public PlayerEventInfo getPlayer(int playerId)
	{
		try
		{
			return World.getPlayer(playerId).getEventInfo();
		}
		catch (Exception e) { return null; }
	}
	
	@Override
	public PlayerEventInfo getPlayer(String name)
	{
		try
		{
			return World.getPlayer(name).getEventInfo();
		}
		catch (Exception e) { return null; }
	}
	
	@Override
	public String getClanName(int clanId)
	{
		try
		{
			return ClanTable.getInstance().getClan(clanId).getName();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	@Override
	public String getAllyName(int clanId)
	{
		try
		{
			return ClanTable.getInstance().getClan(clanId).getAlliance().getAllyName();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	@Override
	public void announceToAllScreenMessage(String message, String announcer)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(player != null)
				player.sendPacket(new CreatureSay(0, ChatType.COMMANDCHANNEL_ALL.ordinal(), "", announcer + ": " + message));
	}
	
	@Override
	public String getHtml(String path)
	{
		return HtmCache.getInstance().getNotNull(path, null);
	}
	
	@Override
	public String getEventHtml(String path)
	{
		
		return HtmCache.getInstance().getNotNull(path, null);
	}
	
	@Override
	public void reloadHtmls()
	{
		HtmCache.getInstance().reload();
	}
	
	@Override
	public String getItemName(int id)
	{
		try
		{
			return ItemHolder.getInstance().getTemplate(id).getName();
		}
		catch (Exception e)
		{
			//TODO: check how we can translate this.
			return "Unknown item";
		}
	}
	
	@Override
	public boolean doorExists(int id)
	{
		return DoorHolder.getInstance().getTemplate(id) != null;
	}
	
	@Override
	public FenceData createFence(int type, int width, int length, int x, int y, int z, int eventId)
	{
		return new FenceData(new FenceInstance(getNextObjectId(), type, width, length, x, y, z, eventId));
	}
	
	@Override
	public void spawnFences(List<FenceData> list, int instance)
	{
		for(FenceData fence : list)
		{
			if(fence.getOwner() != null)
			{
				fence.getOwner().setLoc(fence.getLoc());
				fence.getOwner().setReflection(instance);
				fence.getOwner().spawnMe();
			}
		}
	}
	
	@Override
	public void unspawnFences(List<FenceData> list)
	{
		for(FenceData fence : list)
		{
			if(fence != null)
			{
				WorldRegion region = fence.getOwner().getCurrentRegion();
				fence.getOwner().decayMe();
				
				if (region != null)
					region.removeObject(fence.getOwner());
				
				World.removeObjectFromPlayers(fence.getOwner());
			}
		}
	}
	
	@Override
	public int getGradeFromFirstLetter(String s)
	{
		if(s.equalsIgnoreCase("n") || s.equalsIgnoreCase("ng") || s.equalsIgnoreCase("no"))
		{
			return ItemTemplate.CRYSTAL_NONE;
		}
		else if(s.equalsIgnoreCase("d"))
		{
			return ItemTemplate.CRYSTAL_D;
		}
		else if(s.equalsIgnoreCase("c"))
		{
			return ItemTemplate.CRYSTAL_C;
		}
		else if(s.equalsIgnoreCase("b"))
		{
			return ItemTemplate.CRYSTAL_B;
		}
		else if(s.equalsIgnoreCase("a"))
		{
			return ItemTemplate.CRYSTAL_A;
		}
		else if(s.equalsIgnoreCase("s"))
		{
			return ItemTemplate.CRYSTAL_S;
		}
		else if(s.equalsIgnoreCase("s80"))
		{
			return ItemTemplate.CRYSTAL_S;
		}
		else if(s.equalsIgnoreCase("s84"))
		{
			return ItemTemplate.CRYSTAL_S;
		}
		return 0;
	}
	
	@Override
	public ItemTemplate[] getAllTemplates()
	{
		return ItemHolder.getInstance().getAllTemplates();
	}
	
	@Override
	public Integer[] getAllClassIds()
	{
		List<Integer> idsList = new FastTable<Integer>();
		for(ClassId id : ClassId.values())
			idsList.add(id.getId());
		return idsList.toArray(new Integer[idsList.size()]);
	}
	
	@Override
	public PlayerEventInfo[] getAllPlayers()
	{
		List<PlayerEventInfo> eventInfos = new FastTable<PlayerEventInfo>();
		for(Player player : GameObjectsStorage.getAllPlayers())
		{
			eventInfos.add(player.getEventInfo());
		}
		return eventInfos.toArray(new PlayerEventInfo[eventInfos.size()]);
	}
	
	@Override
	public void loadNextPvpZone()
	{
		//TODO
	}
	
	protected static final NexusOut getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final NexusOut _instance = new NexusOut();
	}

	@Override
	public Loc getPvpZoneMainEventNpcLoc() 
	{
		return null;
	}
}
