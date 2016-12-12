package l2r.gameserver.scripts;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.lang.reference.HardReferences;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Party;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.ExShowTrace;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Strings;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class Functions
{
	public HardReference<Player> self = HardReferences.emptyRef();
	public HardReference<NpcInstance> npc = HardReferences.emptyRef();

	/**
	 * Вызывает метод с задержкой
	 *
	 * @param object	 - от чьего имени вызывать
	 * @param sClass<?>  - вызываемый класс
	 * @param methodName - вызываемый метод
	 * @param args	   - массив аргуметов
	 * @param variables  - список выставляемых переменных
	 * @param delay	  - задержка в миллисекундах
	 */
	public static ScheduledFuture<?> executeTask(final Player caller, final String className, final String methodName, final Object[] args, final Map<String, Object> variables, long delay)
	{
		return ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
			@Override
			public void runImpl() throws Exception
			{
				callScripts(caller, className, methodName, args, variables);
			}
		}, delay);
	}

	public static ScheduledFuture<?> executeTask(String className, String methodName, Object[] args, Map<String, Object> variables, long delay)
	{
		return executeTask(null, className, methodName, args, variables, delay);
	}

	public static ScheduledFuture<?> executeTask(Player player, String className, String methodName, Object[] args, long delay)
	{
		return executeTask(player, className, methodName, args, null, delay);
	}

	public static ScheduledFuture<?> executeTask(String className, String methodName, Object[] args, long delay)
	{
		return executeTask(className, methodName, args, null, delay);
	}

	public static Object callScripts(String className, String methodName, Object ... args)
	{
		return callScripts(className, methodName, args, null);
	}

	public static Object callScripts(String className, String methodName)
	{
		return callScripts(className, methodName, null, null);
	}
	
	public static Object callScripts(String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		return callScripts(null, className, methodName, args, variables);
	}

	public static Object callScripts(Player player, String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		return Scripts.getInstance().callScripts(player, className, methodName, args, variables);
	}

	/**
	 * Вызывать только из скриптов
	 */
	public void show(String text, Player self)
	{
		show(text, self, getNpc());
	}

	/**
	 * Статический метод, для вызова из любых мест
	 */
	public static void show(String text, Player self, NpcInstance npc, Object... arg)
	{
		if(text == null || self == null)
			return;

		NpcHtmlMessage msg = new NpcHtmlMessage(self, npc);

		// приводим нашу html-ку в нужный вид
		if(text.endsWith(".html") || text.endsWith(".htm"))
			msg.setFile(text);
		else
			msg.setHtml(Strings.bbParse(text));

		if(arg != null && arg.length % 2 == 0)
		{
			for(int i = 0; i < arg.length; i = +2)
			{
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
			}
		}

		self.sendPacket(msg);
	}

	public static void show(CustomMessage message, Player self)
	{
		show(message.toString(), self, null);
	}

	public static void sendMessage(String text, Player self)
	{
		self.sendMessage(text);
	}

	public static void sendMessage(CustomMessage message, Player self)
	{
		self.sendMessage(message);
	}

	// Белый чат
	public static void npcSayInRange(NpcInstance npc, String text, int range)
	{
		npcSayInRange(npc, range, NpcString.NONE, text);
	}

	// Белый чат
	public static void npcSayInRange(NpcInstance npc, int range, NpcString fStringId, String... params)
	{
		if(npc == null)
			return;
		NpcSay cs = new NpcSay(npc, ChatType.ALL, fStringId, params);
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
			if(npc.getReflection() == player.getReflection())
				player.sendPacket(cs);
	}

	// Белый чат
	public static void npcSay(NpcInstance npc, String text)
	{
		npcSayInRange(npc, text, 1500);
	}

	// Белый чат
	public static void npcSay(NpcInstance npc, NpcString npcString, String... params)
	{
		npcSayInRange(npc, 1500, npcString, params);
	}

	// Белый чат
	public static void npcSayInRangeCustomMessage(NpcInstance npc, int range, String address, Object... replacements)
	{
		if(npc == null)
			return;
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
			if(npc.getReflection() == player.getReflection())
				player.sendPacket(new NpcSay(npc, ChatType.ALL, new CustomMessage(address, player, replacements).toString()));
	}

	// Белый чат
	public static void npcSayCustomMessage(NpcInstance npc, String address, Object... replacements)
	{
		npcSayInRangeCustomMessage(npc, 1500, address, replacements);
	}

	// private message
	public static void npcSayToPlayer(NpcInstance npc, Player player, String text)
	{
		npcSayToPlayer(npc, player, NpcString.NONE, text);
	}

	// private message
	public static void npcSayToPlayer(NpcInstance npc, Player player, NpcString npcString, String... params)
	{
		if(npc == null)
			return;
		player.sendPacket(new NpcSay(npc, ChatType.TELL, npcString, params));
	}

	// Shout (желтый) чат
	public static void npcShout(NpcInstance npc, String text)
	{
		npcShout(npc, NpcString.NONE, text);
	}

	// Shout (желтый) чат
	public static void npcShout(NpcInstance npc, NpcString npcString, String... params)
	{
		if(npc == null)
			return;
		NpcSay cs = new NpcSay(npc, ChatType.SHOUT, npcString, params);

		int rx = World.regionX(npc);
		int ry = World.regionY(npc);
		int offset = Config.SHOUT_OFFSET;

		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player.getReflection() != npc.getReflection())
				continue;

			int tx = World.regionX(player);
			int ty = World.regionY(player);

			if(tx >= rx - offset && tx <= rx + offset && ty >= ry - offset && ty <= ry + offset)
				player.sendPacket(cs);
		}
	}

	// Shout (желтый) чат
	public static void npcShoutCustomMessage(NpcInstance npc, String address, Object... replacements)
	{
		if(npc == null)
			return;

		int rx = World.regionX(npc);
		int ry = World.regionY(npc);
		int offset = Config.SHOUT_OFFSET;

		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player.getReflection() != npc.getReflection())
				continue;

			int tx = World.regionX(player);
			int ty = World.regionY(player);

			if(tx >= rx - offset && tx <= rx + offset && ty >= ry - offset && ty <= ry + offset || npc.isInRange(player, Config.CHAT_RANGE))
				player.sendPacket(new NpcSay(npc, ChatType.SHOUT, new CustomMessage(address, player, replacements).toString()));
		}
	}

	public static void npcSay(NpcInstance npc, NpcString address, ChatType type, int range, String... replacements)
	{
		if(npc == null)
			return;
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
		{
			if(player.getReflection() == npc.getReflection())
				player.sendPacket(new NpcSay(npc, type, address, replacements));
		}
	}

	public static void addItemToParty(Player player, int item_id, long count)
	{
		if(player == null || count < 1)
			return;

		if(player.getParty() != null)
			if(player.getParty().getLootDistribution() == Party.ITEM_LOOTER)
				addItem(player, item_id, count);
			else if(player.getParty().getLootDistribution() == Party.ITEM_ORDER || player.getParty().getLootDistribution() == Party.ITEM_RANDOM_SPOIL)
			{
				ItemInstance item = ItemFunctions.createItem(item_id);
				item.setCount(count);
				player.getParty().distributeItem(player, item, null);
			}
			else
			{
				ItemInstance item = ItemFunctions.createItem(item_id);
				item.setCount(count);
				player.getParty().distributeItem(player, item, null);
			}
	}
	
	/**
	 * @see ItemFunctions
	 */
	public static void addItem(Playable playable, int itemId, long count)
	{
		ItemFunctions.addItem(playable, itemId, count, true);
	}

	/**
	 * @see ItemFunctions
	 */
	public static void addItem(Playable playable, int itemId, long count, boolean mess)
	{
		ItemFunctions.addItem(playable, itemId, count, mess);
	}

	/**
	 * @see ItemFunctions
	 */
	public static long getItemCount(Playable playable, int itemId)
	{
		return ItemFunctions.getItemCount(playable, itemId);
	}

	/**
	 * @see ItemFunctions
	 */
	public static long removeItem(Playable playable, int itemId, long count)
	{
		return ItemFunctions.removeItem(playable, itemId, count, true);
	}

	public static boolean ride(Player player, int pet)
	{
		if(player.isMounted())
			player.dismount();

		if(player.getPet() != null)
		{
			player.sendPacket(SystemMsg.YOU_ALREADY_HAVE_A_PET);
			return false;
		}

		player.unEquipWeapon();
		player.setMount(pet, 0, 0);
		return true;
	}

	public static void unRide(Player player)
	{
		if(player.isMounted())
			player.dismount();
	}

	public static void unSummonPet(Player player, boolean onlyPets)
	{
		Summon pet = player.getPet();
		if(pet == null)
			return;
		if(pet.isPet() || !onlyPets)
			pet.unSummon();
	}

	public static NpcInstance spawn(Location loc, int npcId)
	{
		return spawn(loc, npcId, ReflectionManager.DEFAULT);
	}

	public static NpcInstance spawn(Location loc, int npcId, Reflection reflection)
	{
		return SpawnTable.spawnSingle(npcId, loc, reflection, 0);
	}

	public static NpcInstance spawn(Location loc, int npcId, Reflection reflection, long despawnTime)
	{
		return SpawnTable.spawnSingle(npcId, loc, reflection, despawnTime);
	}
	
	public Player getSelf()
	{
		return self.get();
	}

	public NpcInstance getNpc()
	{
		return npc.get();
	}

	public static void SpawnNPCs(int npcId, int[][] locations, List<SimpleSpawner> list)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! Functions.SpawnNPCs template is null for npc: " + npcId);
			Thread.dumpStack();
			return;
		}
		for(int[] location : locations)
		{
			SimpleSpawner sp = new SimpleSpawner(template);
			sp.setLoc(new Location(location[0], location[1], location[2]));
			sp.setAmount(1);
			sp.setRespawnDelay(0);
			sp.init();
			if(list != null)
				list.add(sp);
		}
	}
	
	public static void SpawnNPCs(int npcId, int[][] locations, List<SimpleSpawner> list, int respawn)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! Functions.SpawnNPCs template is null for npc: " + npcId);
			Thread.dumpStack();
			return;
		}
		for(int[] location : locations)
		{
			SimpleSpawner sp = new SimpleSpawner(template);
			sp.setLoc(new Location(location[0], location[1], location[2]));
			sp.setAmount(1);
			sp.setRespawnDelay(respawn);
			sp.setRespawnDelay(0);
			sp.init();
			if(list != null)
				list.add(sp);
		}
	}

	public static void deSpawnNPCs(List<SimpleSpawner> list)
	{
		for(SimpleSpawner sp : list)
			sp.deleteAll();

		list.clear();
	}

	public static boolean IsActive(String name)
	{
		return ServerVariables.getString(name, "off").equalsIgnoreCase("on");
	}

	public static boolean SetActive(String name, boolean active)
	{
		if(active == IsActive(name))
			return false;
		if(active)
			ServerVariables.set(name, "on");
		else
			ServerVariables.unset(name);
		return true;
	}

	public static boolean SimpleCheckDrop(Creature mob, Creature killer)
	{
		return mob != null && mob.isMonster() && !mob.isRaid() && killer != null && killer.getPlayer() != null && killer.getLevel() - mob.getLevel() < 9;
	}

	public static boolean isPvPEventStarted()
	{
		if((Boolean) callScripts("events.TvT_New.TvT_New", "isRunned", new Object[] {}))
			return true;
		if((Boolean) callScripts("events.lastHero.LastHero", "isRunned", new Object[] {}))
			return true;
		if((Boolean) callScripts("events.CtF_New.CtF_New", "isRunned", new Object[] {}))
			return true;
		return false;
	}

	public static boolean isEventStarted(String event)
	{
		if((Boolean) callScripts(event, "isRunned", new Object[] {}))
			return true;
		return false;
	}

	public static void sendDebugMessage(GameObject player, String message)
	{
		if(player == null || !player.isPlayer() || !player.getPlayer().canOverrideCond(PcCondOverride.DEBUG_CONDITIONS))
			return;
		player.getPlayer().sendMessage(message);
	}

	public static void sendSystemMail(Player receiver, String title, String body, Map<Integer, Long> items)
	{
		if(receiver == null || !receiver.isOnline())
			return;
		if(title == null)
			return;
		if(items.keySet().size() > 8)
			return;

		Mail mail = new Mail();
		mail.setSenderId(1);
		mail.setSenderName("Admin");
		mail.setReceiverId(receiver.getObjectId());
		mail.setReceiverName(receiver.getName());
		mail.setTopic(title);
		mail.setBody(body);
		
		if (items.size() != 0)
		{
			for(Map.Entry<Integer, Long> itm : items.entrySet())
			{
				ItemInstance item = ItemFunctions.createItem(itm.getKey());
				item.setLocation(ItemInstance.ItemLocation.MAIL);
				item.setCount(itm.getValue());
				item.save();
				mail.addAttachment(item);
			}
		}
		
		mail.setType(Mail.SenderType.NEWS_INFORMER);
		mail.setUnread(true);
		mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
		mail.save();

		receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
		receiver.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
	}

	public static void sendAuctionMail(Player receiver, String title, String body, Map<Integer, Long> items)
	{
		if(receiver == null || !receiver.isOnline())
			return;
		if(title == null)
			return;
		if(items.keySet().size() > 8)
			return;

		Mail mail = new Mail();
		mail.setSenderId(1);
		mail.setSenderName("Admin");
		mail.setReceiverId(receiver.getObjectId());
		mail.setReceiverName(receiver.getName());
		mail.setTopic(title);
		mail.setBody(body);
		for(Map.Entry<Integer, Long> itm : items.entrySet())
		{
			ItemInstance item = ItemFunctions.createItem(itm.getKey());
			item.setLocation(ItemInstance.ItemLocation.MAIL);
			item.setCount(itm.getValue());
			item.save();
			mail.addAttachment(item);
		}
		mail.setType(Mail.SenderType.NONE);
		mail.setUnread(true);
		mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
		mail.save();

		receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
		receiver.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
	}
	
	public static String GetStringCount(long count)
	{
		String scount = Long.toString(count);
		if (count < 1000)
			return scount;
		if ((count > 999) && (count < 1000000))
			return scount.substring(0, scount.length() - 3) + "k";
		if ((count > 999999) && (count < 1000000000))
			return scount.substring(0, scount.length() - 6) + "kk";
		if (count > 999999999)
			return scount.substring(0, scount.length() - 9) + "kkk";
		if (count == 0)
			return "00.00";
		return "ERROR";
	}
	
	public static ExShowTrace Points2Trace(List<int[]> points, int step, boolean auto_compleate, boolean maxz)
	{
		ExShowTrace result = new ExShowTrace();

		int[] prev = null;
		int[] first = null;
		for(int[] p : points)
		{
			if(first == null)
				first = p;

			if(prev != null)
				result.addLine(prev[0], prev[1], maxz ? prev[3] : prev[2], p[0], p[1], maxz ? p[3] : p[2], step, 60000);

			prev = p;
		}

		if(prev == null || first == null)
			return result;

		if(auto_compleate)
			result.addLine(prev[0], prev[1], maxz ? prev[3] : prev[2], first[0], first[1], maxz ? first[3] : first[2], step, 60000);

		return result;
	}
}
