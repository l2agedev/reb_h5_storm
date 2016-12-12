package handler.items;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.handler.items.ItemHandler;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.NpcInfo;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.npc.NpcTemplate;

import events.Christmas.ctreeAI;

public class XmassTree extends SimpleItemHandler implements ScriptFile
{
	@Override
	public boolean pickupItem(Playable playable, ItemInstance item)
	{
		return true;
	}

	@Override
	public void onLoad()
	{
		if (ServerVariables.getString("Christmas", "off").equalsIgnoreCase("on"))
			ItemHandler.getInstance().registerItemHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}
	
	public class DeSpawnScheduleTimerTask extends RunnableImpl
	{
		SimpleSpawner spawnedTree = null;
		
		public DeSpawnScheduleTimerTask(SimpleSpawner spawn)
		{
			spawnedTree = spawn;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			spawnedTree.deleteAll();
		}
	}
	
	private static int[] _itemIds =
	{
		5560, // Christmas Tree
		5561 // Special Christmas Tree
	};
	
	private static int[] _npcIds =
	{
		13006, // Christmas Tree
		13007 // Special Christmas Tree
	};
	
	private static final int DESPAWN_TIME = 600000; // 10 min
	
	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		NpcTemplate template = null;
		
		int itemId = item.getItemId();
		for (int i = 0; i < _itemIds.length; i++)
			if (_itemIds[i] == itemId)
			{
				template = NpcHolder.getInstance().getTemplate(_npcIds[i]);
				break;
			}
		
		for (NpcInstance npc : World.getAroundNpc(player, 300, 200))
			if (npc.getNpcId() == _npcIds[0] || npc.getNpcId() == _npcIds[1])
			{
				player.sendPacket(new SystemMessage2(SystemMsg.SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN).addName(npc));
				return false;
			}
		
		// Запрет на саммон елок слищком близко к другим НПЦ
		if (World.getAroundNpc(player, 100, 200).size() > 0)
		{
			player.sendPacket(Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			return false;
		}
		
		if (template == null)
			return false;
		
		if (!player.getInventory().destroyItem(item, 1L))
			return false;
		
		SimpleSpawner spawn = new SimpleSpawner(template);
		spawn.setLoc(player.getLoc());
		NpcInstance npc = spawn.doSpawn(true);
		npc.setTitle(player.getName());
		
		for(Player plr : World.getAroundPlayers(npc))
			player.sendPacket(new NpcInfo(npc, plr));
			
		// АИ вещающее бафф регена устанавливается только для большой елки
		if (itemId == 5561)
			npc.setAI(new ctreeAI(npc));
		
		ThreadPoolManager.getInstance().schedule(new DeSpawnScheduleTimerTask(spawn), (player.isInPeaceZone() ? DESPAWN_TIME / 3 : DESPAWN_TIME));
		return true;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}