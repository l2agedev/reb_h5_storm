package l2r.gameserver.randoms;

import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.VisualInstance;
import l2r.gameserver.network.serverpackets.NpcInfo;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.skills.AbnormalEffect;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * 
 * @author Infern0
 * @Idea Midnex
 *
 */

public class Visuals
{
	private static ScheduledFuture<?> _deleteVisualNpcTask;
	
	private static final Logger _log = LoggerFactory.getLogger(Visuals.class);
	private Map<Integer, VisualData> _availableItems = new HashMap<Integer, VisualData>();
	
	private Map<Integer, String> _npcs = new HashMap<Integer, String>();
	
	public Map<Integer, VisualData> getavailableItems()
	{
		return _availableItems;
	}
	
	public int getCountBySlot(int slot)
	{
		int count = 0;
		for (VisualData data : _availableItems.values())
		{
			if (data.getSlot() == slot)
				count++;
		}
		return count;
	}

	public List<Integer> getVisual(int slotId, int frompage, int topage)
	{
		List<Integer> _temp = new ArrayList<Integer>();
		int i = -1;
		SortedMap<Integer, VisualData> sortedMap = new TreeMap<>(_availableItems);
		for(Entry<Integer, VisualData> items : sortedMap.entrySet())
		{
			if (items.getValue().getSlot() != slotId)
				continue;
			
			i++;
			if(frompage > i)
				continue;
			if(topage == i)
				break;
			_temp.add(items.getKey());
		}
		
		Collections.sort(_temp);
		
		return _temp;
	}

	public int getPageIn(int slot, int itemId)
	{
		int page = 1;
		int c = 0;
		SortedMap<Integer, VisualData> sortedMap = new TreeMap<>(_availableItems);
		for(Entry<Integer, VisualData> data : sortedMap.entrySet())
		{
			if (data.getValue().getSlot() != slot)
				continue;
			
			c++;
			if(c > 4)
			{
				c = 1;
				page++;
			}
			if(data.getKey() == itemId)
				break;
		}
		return page;
	}

	public boolean isAvailable(int slot, int itemId)
	{
		SortedMap<Integer, VisualData> sortedMap = new TreeMap<>(_availableItems);
		for(Entry<Integer, VisualData> data : sortedMap.entrySet())
		{
			if (data.getKey() == itemId && data.getValue().getSlot() == slot)
				return true;
		}
		return false;
	}

	public void load()
	{
		_availableItems.clear();

		try
		{
			File file = new File(Config.DATAPACK_ROOT + "/data/visualItems.xml");
			DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			Document doc1 = factory1.newDocumentBuilder().parse(file);
			for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
				if("list".equalsIgnoreCase(n1.getNodeName()))
					for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
						if("set".equalsIgnoreCase(d1.getNodeName()))
						{
							int slot = Integer.parseInt(d1.getAttributes().getNamedItem("slot").getNodeValue());
							int itemid = Integer.parseInt(d1.getAttributes().getNamedItem("itemid").getNodeValue());
							int costid = Integer.parseInt(d1.getAttributes().getNamedItem("costid").getNodeValue());
							long price = Long.parseLong(d1.getAttributes().getNamedItem("price").getNodeValue());

							VisualData data = new VisualData();
							data.setSlot(slot);
							data.setCostId(costid);
							data.setCostAmount(price);
							
							_availableItems.put(itemid, data);
						}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		_log.info("Visual System Loaded: Total Items " + _availableItems.size());
	}
	
	public void spawnVisual(Player player)
	{
		if (!Config.ENABLE_VISUAL_SYSTEM)
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Visual transformation is disabled!");
			return;
		}

		if (player == null)
			return;
		
		// Conditions
		if (!player.isGM())
		{
			
			if (player.isInJail() || player.isCursedWeaponEquipped() || NexusEvents.isInEvent(player) || player.getReflectionId() != ReflectionManager.DEFAULT_ID/* || player.getPvpFlag() != 0*/ || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped() || player.isInZone(ZoneType.no_escape) || player.isInZone(ZoneType.SIEGE) || player.isInZone(ZoneType.epic))
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Cannot be spawned now due conditions");
				return;
			}
			
			if (!player.isInZone(ZoneType.peace_zone))
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You must be inside peace zone to spawn Visual NPC!");
				return;
			}
			
			if(player.isTerritoryFlagEquipped() || player.isCombatFlagEquipped())
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You cannot use this NPC while holding Territory Flag.");
				return;
			}
		}
		
		if (_npcs.containsValue(player.getName()))
		{
			player.sendMessageS("You already have spawned visual NPC.", 3);
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You already have spawned Visual NPC.");
			return;
		}
		
		String items;
		if (player.getVar("visualItems") != null)
		{
			String[] var = player.getVar("visualItems").split(";");
			items = var[0] + "," + var[1] + "," + var[2] + "," + var[3] + "," + var[4] + "," + var[5] + "," + var[6] + "," + var[7] + "," + var[8] + "," + var[9] + "," + var[10];
		}
		else
			items = "0,0,0,0,0,0,0,0,-1,-1,-1";
		
		NpcInstance npc = spawnVisualNpc(player, 603, "Visual", player.getName() + " Model", items);
		
		if (npc == null)
		{
			_log.error("Visualizer: Cannot create visual NPC....");
			return;
		}
		
		// Spawn the default model and add it to list.
		_npcs.put(npc.getObjectId(), player.getName());
		
		// Small cheat to call visualizerinstnace...
		npc.showChatWindow(player, 0, new Object[] {});
		
		// Schedule delete of the npc.
		startVisualNpcDelete(npc.getObjectId());
		
		player.sendMessageS("You have spawned Visual NPC. It will be deleted in " + Config.VISUAL_NPC_DELETE_TIME + " minutes.", 3);
		player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You have spawned Visual NPC. It will be deleted in " + Config.VISUAL_NPC_DELETE_TIME + " minutes.");
		
		player.sendPacket(new NpcInfo(npc, player));
	}

	private NpcInstance spawnVisualNpc(Player player, int npcId, String name, String title, String items)
	{
		try
		{
			NpcInstance npc = NpcHolder.getInstance().getTemplate(npcId).getNewInstance();
			String[] item = items.split(",");
			((VisualInstance) npc).setItems(Integer.parseInt(item[0]), Integer.parseInt(item[1]), Integer.parseInt(item[2]), Integer.parseInt(item[3]), Integer.parseInt(item[4]), Integer.parseInt(item[5]), Integer.parseInt(item[6]), Integer.parseInt(item[7]), Integer.parseInt(item[8]), Integer.parseInt(item[9]), Integer.parseInt(item[10]));
			npc.setName(name);
			npc.setTitle(title);
			npc.startAbnormalEffect(AbnormalEffect.FLAME);
			npc.setTeam(TeamType.BLUE);
			npc.setReflection(player.getReflectionId());
			npc.setSpawnedLoc(player.getLoc().findPointToStay(20, 100).correctGeoZ());
			npc.spawnMe(npc.getSpawnedLoc());
			return npc;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}

	private class deleteNpc implements Runnable
	{
		private final int _npcID;

		private deleteNpc(int id)
		{
			_npcID = id;
		}

		@Override
		public void run()
		{
			NpcInstance npc = GameObjectsStorage.getNpc(_npcID);
			
			if (npc != null)
				npc.deleteMe();
			
			if (_npcs.containsKey(_npcID))
				_npcs.remove(_npcID);
		}
	}
	
	public void startVisualNpcDelete(int npcObject)
	{
		_deleteVisualNpcTask = ThreadPoolManager.getInstance().schedule(new deleteNpc(npcObject), Config.VISUAL_NPC_DELETE_TIME * 60000);
	}
	
	public void endVisualNpcDelete()
	{
		if(_deleteVisualNpcTask != null)
		{
			_deleteVisualNpcTask.cancel(true);
			_deleteVisualNpcTask = null;
		}
	}
	
	public void destroyVisual(String playerName)
	{
		if (playerName == null)
			return;
		
		endVisualNpcDelete();
		
		if (_npcs == null)
			return;
		
		for(Entry<Integer, String> entry : _npcs.entrySet())
		{
			int npcObject = entry.getKey();
			String plrName = entry.getValue();
			
			if (plrName == null || plrName.isEmpty())
				continue;
			
			if (plrName.equalsIgnoreCase(playerName))
			{
				NpcInstance npc = GameObjectsStorage.getNpc(npcObject);
				
				if (npc != null)
					npc.deleteMe();
				
				if (_npcs.containsKey(npcObject))
					_npcs.remove(npcObject);
			}
		}
	}
	
	public Map<Integer, String> getAllSpawnedNPCs()
	{
		return _npcs;
	}
	
	public class VisualData
	{
		private int _slot = -1;
		private int _costId = 0;
		private long _costAmount = 0;
		
		public int getSlot()
		{
			return _slot;
		}
		
		public void setSlot(int id)
		{
			_slot = id;
		}
		
		public int getCostId()
		{
			return _costId;
		}
		
		public void setCostId(int id)
		{
			_costId = id;
		}
		
		public long getCostAmount()
		{
			return _costAmount;
		}
		
		public void setCostAmount(long amount)
		{
			_costAmount = amount;
		}
	}
	
	public static final Visuals getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final Visuals _instance = new Visuals();
	}
}
