package l2r.gameserver.model.entity.events;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.lang.reference.HardReferences;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;

import java.util.List;

public abstract class GameEvent
{
	public HardReference<Player> self = HardReferences.emptyRef();
	public HardReference<NpcInstance> npc = HardReferences.emptyRef();
	public static final int STATE_INACTIVE = 0;
	public static final int STATE_ACTIVE = 1;
	public static final int STATE_RUNNING = 2;

	public int getState()
	{
		return 0;
	}

	public abstract String getName();
	public abstract String minLvl();
	public abstract String maxLvl();
	
	public long getNextTime()
	{
		return 0;
	}

	public boolean isRunning()
	{
		return getState() == 2;
	}

	public boolean canRegister(Player player, boolean first)
	{
		return (getState() == 1) && (!isParticipant(player)) && (player._event == null);
	}

	public abstract boolean isParticipant(Player paramPlayer);

	public abstract boolean register(Player paramPlayer);

	public abstract void unreg(Player paramPlayer);

	public abstract void remove(Player paramPlayer);

	public abstract void start();

	public abstract void finish();

	public abstract void abort();

	public boolean canAttack(Creature attacker, Creature target)
	{
		return true;
	}

	public boolean checkPvP(Creature attacker, Creature target)
	{
		return getState() != 2;
	}

	public boolean canUseItem(Player actor, ItemInstance item)
	{
		return true;
	}

	public boolean canUseSkill(Creature caster, Creature target, Skill skill)
	{
		return true;
	}

	public abstract void onLogout(Player paramPlayer);

	public abstract void doDie(Creature paramCreature1, Creature paramCreature2);

	public boolean canTeleportOnDie(Player player)
	{
		return getState() != 2;
	}

	public boolean canLostExpOnDie()
	{
		return getState() != 2;
	}

	public int getCountPlayers()
	{
		return 0;
	}

	public boolean talkWithNpc(Player player, NpcInstance npc)
	{
		return false;
	}

	public static void unRide(Player player)
	{
		Functions.unRide(player);
	}

	public static void unSummonPet(Player player, boolean onlyPets)
	{
		Functions.unSummonPet(player, onlyPets);
	}
	
	public Player getSelf()
	{
		return self.get();
	}

	public NpcInstance getNpc()
	{
		return npc.get();
	}
	
	public static long getItemCount(Playable playable, int itemId)
	{
		return Functions.getItemCount(playable, itemId);
	}

	public static long removeItem(Playable playable, int itemId, long count)
	{
		return Functions.removeItem(playable, itemId, count);
	}

	public static void addItem(Playable playable, int itemId, long count)
	{
		Functions.addItem(playable, itemId, count);
	}

	public static void addItem(Playable playable, int itemId, long count, boolean mess)
	{
		Functions.addItem(playable, itemId, count, mess);
	}

	public static String GetStringCount(long count)
	{
		return Functions.GetStringCount(count);
	}

	public static boolean ride(Player player, int pet)
	{
		return Functions.ride(player, pet);
	}

	public static void show(String text, Player self, NpcInstance npc, Object... arg)
	{
		Functions.show(text, self, npc, arg);
	}

	public static void show(CustomMessage message, Player self)
	{
		Functions.show(message, self);
	}

	public static void sendMessage(String text, Player self)
	{
		Functions.sendMessage(text, self);
	}

	public static void sendMessage(CustomMessage message, Player self)
	{
		Functions.sendMessage(message, self);
	}
	
	public static void npcSayInRange(NpcInstance npc, String text, int range)
	{
		Functions.npcSayInRange(npc, text, range);
	}
	
	public static void npcSayInRange(NpcInstance npc, int range, NpcString fStringId, String... params)
	{
		Functions.npcSayInRange(npc, range, fStringId, params);
	}
	
	public static void npcSay(NpcInstance npc, String text)
	{
		Functions.npcSay(npc, text);
	}
	
	public static void npcSay(NpcInstance npc, NpcString npcString, String... params)
	{
		Functions.npcSay(npc, npcString, params);
	}
	
	public static void npcSayInRangeCustomMessage(NpcInstance npc, int range, String address, Object... replacements)
	{
		Functions.npcSayInRangeCustomMessage(npc, range, address, replacements);
	}
	
	public static void npcSayCustomMessage(NpcInstance npc, String address, Object... replacements)
	{
		Functions.npcSayCustomMessage(npc, address, replacements);
	}
	
	public static void npcSayToPlayer(NpcInstance npc, Player player, String text)
	{
		Functions.npcSayToPlayer(npc, player, text);
	}
	
	public static void npcSayToPlayer(NpcInstance npc, Player player, NpcString npcString, String... params)
	{
		Functions.npcSayToPlayer(npc, player, npcString, params);
	}
	
	public static void npcShout(NpcInstance npc, String text)
	{
		Functions.npcShout(npc, text);
	}
	
	public static void npcShout(NpcInstance npc, NpcString npcString, String... params)
	{
		Functions.npcShout(npc, npcString, params);
	}
	
	public static void npcShoutCustomMessage(NpcInstance npc, String address, Object... replacements)
	{
		Functions.npcShoutCustomMessage(npc, address, replacements);
	}

	public static void npcSay(NpcInstance npc, NpcString address, ChatType type, int range, String... replacements)
	{
		Functions.npcSay(npc, address, type, range, replacements);
	}

	public static void SpawnNPCs(int npcId, int[][] locations, List<SimpleSpawner> list)
	{
		Functions.SpawnNPCs(npcId, locations, list);
	}
	
	public static void SpawnNPCs(int npcId, int[][] locations, List<SimpleSpawner> list, int respawn)
	{
		Functions.SpawnNPCs(npcId, locations, list, respawn);
	}

	public static void deSpawnNPCs(List<SimpleSpawner> list)
	{
		Functions.deSpawnNPCs(list);
	}
}