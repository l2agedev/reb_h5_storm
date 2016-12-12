package l2r.gameserver.nexus_engine.l2r;

import l2r.gameserver.nexus_engine.events.engine.base.Loc;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.DoorData;
import l2r.gameserver.nexus_interface.delegate.FenceData;
import l2r.gameserver.nexus_interface.delegate.InstanceData;
import l2r.gameserver.nexus_interface.handlers.AdminCommandHandlerInstance;
import l2r.gameserver.templates.item.ItemTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author hNoke
 */
public interface INexusOut
{
	public ScheduledFuture<?> scheduleGeneral(Runnable task, long delay);
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable task, long initial, long delay);
	public void executeTask(Runnable task);
	public void purge();
	
	public int getNextObjectId();
	
	public int random(int min, int max);
	public int random(int max);
	
	public Connection getConnection();
	
	public InstanceData createInstance(String name, int duration, int emptyDestroyTime);
	
	public void registerAdminCommandHandler(AdminCommandHandlerInstance handler);
	
	public String getClanName(int clanId);
	public String getAllyName(int allyId);
	public PlayerEventInfo getPlayer(int playerId);
	public PlayerEventInfo getPlayer(String name);
	public Integer[] getAllClassIds();
	public PlayerEventInfo[] getAllPlayers();
	
	public void announceToAllScreenMessage(String message, String announcer);
	
	public String getHtml(String path);
	public String getEventHtml(String path);
	public void reloadHtmls();
	
	public boolean doorExists(int id);
	public FenceData createFence(int type, int width, int length, int x, int y, int z, int eventId);
	public void spawnFences(List<FenceData> list, int instance);
	public void unspawnFences(List<FenceData> list);
	
	public int getGradeFromFirstLetter(String s);
	public String getItemName(int id);
	public ItemTemplate[] getAllTemplates();
	public void addDoorToInstance(int instanceId, int doorId, boolean opened);
	public DoorData[] getInstanceDoors(int instanceId);
	public void loadNextPvpZone();
	public Loc getPvpZoneMainEventNpcLoc();
}
