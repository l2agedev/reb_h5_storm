package events.Cataclizm;

import l2r.gameserver.Announcements;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.listener.actor.npc.OnDecayListener;
import l2r.gameserver.listener.actor.npc.OnShowChatListener;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Territory;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.npc.NpcTemplate;

/**
 * @author PaInKiLlEr - ВНИМАНИЕ: изменить айди херо статуи TODO заспавнить херо статуи, ибо на руофе осады идут, координаты хз
 */
public abstract class Cataclizm extends Functions implements ScriptFile, OnShowChatListener, OnDecayListener, OnDeathListener
{
	// Куда кричать анонс? во весь мир или в шаут (по умолчанию во весь мир)
	private static boolean shout = false;
	public static String[] rewards1 = new String[]
	{
		"57,1;58,1"
	}; // Призы за убийство моба из 1 круга
	public static String[] rewards2 = new String[]
	{
		"57,1;58,1"
	}; // Призы за убийство моба из 2 круга
	public static String[] rewards3 = new String[]
	{
		"57,1;58,1"
	}; // Призы за убийство моба из 3 круга
	public static String[] rewards4 = new String[]
	{
		"57,1;58,1"
	}; // Призы за убийство статуи
	
	// Ид статуи у котой можно будет смотреть таблицу кто убил обычные статуи
	public static int heroStatuya = 36800;
	
	@Override
	public void onLoad()
	{
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private static int allTown;
	
	public static void addTown()
	{
		allTown++;
		if (allTown >= 9)
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmFinishAll", null);
	}
	
	public static void deleteTown()
	{
		allTown--;
	}
	
	public static void sayToAll(String address, String[] all)
	{
		if (shout)
		{
			for (Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				CustomMessage cm = new CustomMessage(address, player);
				Announcements.shout(player, cm.toString(), ChatType.CRITICAL_ANNOUNCE);
			}
		}
		else
			Announcements.getInstance().announceByCustomMessage(address, null, ChatType.CRITICAL_ANNOUNCE);
	}
	
	public void spawning(String[] name, Territory territory)
	{
		if (name != null)
		{
			for (String str : name)
			{
				String[] str2 = str.split(";");
				for (String str3 : str2)
				{
					String[] str4 = str3.split(",");
					int id = Integer.parseInt(str4[0]);
					int count = Integer.parseInt(str4[1]);
					for (int i = 0; i < count; i++)
					{
						NpcTemplate template = NpcHolder.getInstance().getTemplate(id);
						MonsterInstance monster = new MonsterInstance(IdFactory.getInstance().getNextId(), template);
						monster.setCurrentHpMp(monster.getMaxHp(), monster.getMaxMp(), true);
						monster.setLoc(Territory.getRandomLoc(territory), false);
						monster.spawnMe();
					}
				}
			}
		}
	}
	
	public void despawning(String[] name)
	{
		if (name != null)
		{
			for (String str : name)
			{
				String[] str2 = str.split(";");
				for (String str3 : str2)
				{
					String[] str4 = str3.split(",");
					int id = Integer.parseInt(str4[0]);
					for (NpcInstance n : GameObjectsStorage.getAllNpcs())
					{
						if (n != null && n.getNpcId() == id)
							n.deleteMe();
					}
				}
			}
		}
	}
	
	public int rewards(String[] name, NpcInstance actor)
	{
		if (name != null)
		{
			for (String str : name)
			{
				String[] str2 = str.split(";");
				for (String str3 : str2)
				{
					String[] str4 = str3.split(",");
					int id = Integer.parseInt(str4[0]);
					if (actor.getNpcId() == id)
						return id;
				}
			}
		}
		
		return 0;
	}
}