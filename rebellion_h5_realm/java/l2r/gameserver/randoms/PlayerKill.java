/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.randoms;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.listener.actor.OnKillListener;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.World;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author horato, Nik
 * 
 * Deadman's Chest
 * When a player dies, there is a chance that a chest spawns on his corpse
 * (the bigger killstreak he has, the bigger the chance is)
 * when someone approaches the chest and talks with it
 * a random thing will happen:
 * 1. The dead player (if hasnt went to village) will be ressurected and buffed with salvation
 * 2. The chest explodes and does a random damage to nearby players
 * 3. The chest drops a random item(s)
 * 4. The person that talked with the chest have a chance to steal one of the dead player's (if hasnt went to village) items that is not equipped
 * 5. The chest gives a cool buff to a random player
 * 6. The chest spawns a terrifying RB
 * etc. 
 * 
 * Item Copier
 * When you kill a player, there is a small chance (the bigger the enemy's killstreak is, the bigger the chance is)
 * that you can copy his equipment for 1 hour. You are free to chose if you want to use it or not.
 * 
 * Fame
 * Except sieges, fame can be gathered through PvP. On your fifth killstreak you start to aquire fame for each kill you do without dieing.
 * 
 * Enchants/Life Stones
 * When you kill a player, there is a chance that you can obtain EWS, EAS, High-Grade Life Stone, Top-Grade Life Stone
 * 
 * Dynamic Weapon Elementals
 * When you kill someone holding a weapon with 300 element or more, it loses some of its elemental power.
 * If your weapon has 300 elemental attribute or more and the killed one's weapon elemental attribute surpasses yours, you will gain the amount he lost.
 * If your weapon's elemental attribute surpasses the killed one's elemental attribute by 25 or less, you gain 5 elemental attribute.
 * Summary, dynamic weapon elements only change from 300 to 450. They cant go higher or lower.
 * 
 * Item Distribution
 * When a player is in party, the items he obtains through pvp kill are distributed depending on the party's loot distribution:
 *  - Finders Keepers: The actor takes everything he got as a drop from the killed player.
 *  - Random: The drop is given to a random player from the party. Adena is splitted and given to every party member.
 *  - Random including spoil: The drop is given randomly to the looter or the healers in the party. Adena is splitted and given to every healer in the party including the actor.
 * 
 * Support classes boost:
 * Some support classes (more incoming) will have the ability to get some skills from other classes. Blade Dancers and Sword Singers can chose up to 5 skills, while Shillien Elders, Elven Elders and Bishops can select up to 4.
 * Here is a list of the skills avaiable for those classes (skills will be added/removed in order to create a balance):
 *  - Blade Dancers:
 *      Heavy Armor Mastery
 *      Pain of Shilen
 *      Spirit of Shilen
 *      Touch of Death
 *      Combat Aura
 *      Judgment
 *      Lightning Strike
 *      Ultimate Defense
 *      Sonic Focus 
 *      Sonic Mastery  
 *      Triple Slash  
 *      Sonic Blaster 
 *      Sonic Buster  
 *      War Cry  
 *      Double Sonic Slash
 *      Sonic Storm  
 *      Triple Sonic Slash
 *      Lionheart  
 *      Duelist Spirit  
 *      Sonic Rage
 *      Weapon Blockade
 *      Final Secret
 *      Maximum Focus Sonic
 *      Bow Mastery
 *      Double Shot 
 *      Light Armor Mastery  
 *      Long Shot
 *      Stun Shot
 *      Quick Step
 *      Rapid Shot 
 *      Dead Eye
 *      Fatal Counter
 *      Lethal Shot  
 *      Hamstring Shot
 *      Evade Shot 
 *      Seven Arrow
 *      Multiple Shot
 *      Death Shot  
 *      Ghost Piercing
 *  - Swordsingers:
 *      Heavy Armor Mastery
 *      Shield Mastery  
 *      Shield Strike 
 *      Guard Stance 
 *      Combat Aura
 *      Tribunal
 *      Aegis
 *      Shield Deflect Magic  
 *      Shield Fortress
 *      Shield Bash  
 *      Ultimate Defense
 *      Vengeance  
 *      Magical Mirror  
 *      Touch of Life
 *      Iron Shield  
 *      Challenge for Fate
 *      Bow Mastery
 *      Double Shot 
 *      Light Armor Mastery
 *      Long Shot
 *      Quick Step 
 *      Stun Shot 
 *      Burst Shot  
 *      Rapid Shot
 *      Blessing of Sagittarius
 *      Rapid Fire  
 *      Lethal Shot 
 *      Hamstring Shot
 *      Evade Shot  
 *      Seven Arrow
 *      Multiple Shot
 *      Arrow Rain
 *      Death Shot
 *  - Shillien Elders:
 *      Curse Chaos  
 *      Curse Fear
 *      Hurricane
 *      Shadow Flare
 *      Slow
 *      Surrender To Wind
 *      Vampiric Claw
 *      Death Spike
 *      Tempest
 *      Aura Flash
 *      Aura Symphony
 *      Demon Wind
 *      Elemental Storm
 *      Dark Vortex
 *  - Elven Elders:
 *      Aura Bolt
 *      Aura Flare 
 *      Curse Fear 
 *      Curse Weakness
 *      Frost Bolt
 *      Solar Flare 
 *      Hydro Blast
 *      Surrender To Water
 *      Aqua Splash 
 *      Ice Dagger
 *      Aura Flash 
 *      Frost Wall
 *      Aura Symphony
 *      Blizzard
 *      Elemental Symphony
 *  
 * Healers Penalty
 * When a Shillien Elder or Elven Elder casts a damage dealing skill obtained through skill transfer (such as Hurricane, Hydro Blast, etc.), the following skills will be disabled for 2 seconds:
 * - Greater Battle Heal
 * - Greater Group Heal
 * - Greater Heal
 * - Vitalize
 * - Mass Vitalize
 * - Major Heal
 * - Major Group Heal
 * - Chain Heal
 * - Blessed Blood
 * - Blessing of Eva
 * - Body of Avatar
 */

public class PlayerKill implements OnKillListener
{
	private static final Logger _log = LoggerFactory.getLogger(PlayerKill.class);
	
	private static PlayerKill _instance;
	private static final int NPC_ID = 660;
	private static Map<NpcInstance, Player> chests = new FastMap<NpcInstance, Player>();
	
	// ZONES
	private static ZoneListener _zoneListener;
	private static final String[] ZONES = {
			"[gludin_town_peace1]",
			"[gludin_town_peace2]",
			"[gludin_town_peace3]",
			"[gludin_town_peace4]"
	};
	
	private static Integer[][] ITEMS = 
	{
			// ItemId, Count, chance(if fails to obtain this item, it will try an item again.)
			{ 17051, 1, 1 }, // Talisman - STR
			{ 17052, 1, 1 }, // Talisman - DEX
			{ 17053, 1, 1 }, // Talisman - CON
			{ 17054, 1, 1 }, // Talisman - WIT
			{ 17055, 1, 1 }, // Talisman - INT
			{ 17056, 1, 1 }, // Talisman - MEN
			{ 17057, 1, 2 }, // Talisman - Resistance to Stun
			{ 17058, 1, 2 }, // Talisman - Resistance to Sleep
			{ 17059, 1, 2 }, // Talisman - Resistance to Hold
			{ 17060, 1, 2 }, // Talisman - Paralyze Resistance
			{ 17061, 1, 1 }, // Talisman - ALL STAT
			{ 57, 2000000, 10 } // Adena
	};
	
	private static final int[] MANTRAS = { 5570, 5572, 5574 };
	
	public static PlayerKill getInstance()
	{
		if (_instance == null)
			_instance = new PlayerKill();
		return _instance;
	}
	
	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(player == null)
				return;
			
			if (player.isPlayer() && zone.getName().startsWith("[gludin_town_peace"))
			{
				switch (player.getPlayer().getClassId())
				{
					case bishop:
					case cardinal:
					case elder:
					case evaSaint:
					case shillienOracle:
					case shillienSaint:
					case dominator:
					case doomcryer:
						player.getPlayer().leaveParty();
						player.getPlayer().sendMessage("Your class is not allowed to be in a party in this zone.");
				}
			}
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
		}
	}
	
	/**
	 * @param rewardCount - a value made from CustomConfig.MASSACRE_EVENT_REWARD_COUNT && CustomConfig.MASSACRE_EVENT_ADDITIONAL_COUNT
	 */
	@SuppressWarnings("unused")
	private void onMassacreEventKill(Player actorPlayer, Player killed, int rewardCount)
	{
		if (actorPlayer.getReflectionId() == 0 && actorPlayer.isInZone(ZoneType.battle_zone))
		{
			actorPlayer.sendChatMessage(actorPlayer.getObjectId(), ChatType.TELL.ordinal(), "Massacre Event", "No adena rewards are given inside pvp zones.");
			return;
		}
		
		if (actorPlayer.getDistance(-83002, 150862) < 15000)
		{
			actorPlayer.sendChatMessage(actorPlayer.getObjectId(), ChatType.TELL.ordinal(), "Massacre Event", "No adena rewards are given for pvp near Gludin.");
			return;
		}
			
		// actor's GPTS divided by the killed's GPTS
		double killedGP = Util.getGearPoints(killed);
		double actorPlayerGP = Util.getGearPoints(actorPlayer);			
		// Minimum gear points - 1800
		killedGP = Math.max(killedGP, 1800);
		actorPlayerGP = Math.max(actorPlayerGP, 1800);
		
		double diff = killedGP / actorPlayerGP;
		
		// Avoid huge rewards
		diff = Math.min(diff, 4);
		
		rewardCount *= diff;
			
		// Avoid zero rewards
		rewardCount = Math.max(rewardCount, 1);
		
		// TODO: rewardCount += actorPlayer.getKillsInRow() * Config.MASSACRE_EVENT_ADDITIONAL_COUNT * 3;
		rewardCount += actorPlayer.getKillsInRow() * 3;
		
		// Add to hb zone stash TODO: ...
		if (actorPlayer.getClan() != null)
			actorPlayer.processQuestEvent("HellboundSystem", "stash "+rewardCount, null);
		
		actorPlayer.sendChatMessage(actorPlayer.getObjectId(), ChatType.TELL.ordinal(), "Massacre Event", "You will recieve a reward for killing this player.");
		// TODO: distributeItem(actorPlayer, Config.MASSACRE_EVENT_REWARD_ID, rewardCount);
		distributeItem(actorPlayer, 57, rewardCount);
	}
	
	public void deadmanChestFuncs(NpcInstance npc, Player opener)
	{
		Player deadmen = chests.get(npc);
		boolean tryAgain = false;
		
		switch (Rnd.get(5))
		{
			case 0:
				if (deadmen.isDead())
				{
					deadmen.doRevive();
					SkillTable.getInstance().getInfo(1410, 1).getEffects(npc, deadmen, false, false, false); // Salvation
					Functions.npcSay(npc, "Huhu, " + deadmen.getName() + " is a brave one! Lets see if he is strong enough to stay alive.");
				}
				else
				{
					Functions.npcSay(npc, "Soo... " + deadmen.getName() + " doesn\'t want to be revived huh? How pitiful. Ok then, " + opener.getName() + " will get the salvation.");
					SkillTable.getInstance().getInfo(1410, 1).getEffects(npc, opener, false, false); // Salvation
				}
				break;
			case 1: // Do something bad :)
				switch (Rnd.get(2))
				{
					case 0: // Explosion
						npc.broadcastPacketToOthers(new MagicSkillUse(npc, opener, 1171, 1, 500, 4000));
						giveDamage(opener, npc, 200);
						Functions.npcSay(npc, "Hahahahahaha! Die you bastards!");
						break;
					case 1: // Petrification
						npc.broadcastPacketToOthers(new MagicSkillUse(npc, npc, 367, 1, 500, 4000));
						givePetrify(opener, npc, 200);
						Functions.npcSay(npc, "Freeze! No one move a muscle!");
						break;
						
				}
				break;
			case 2:
				List<Integer[]> tmpRewards = new ArrayList<Integer[]>(ITEMS.length);
				for (Integer[] i : ITEMS)
					tmpRewards.add(i);
				
				Collections.shuffle(tmpRewards);

				for (Integer[] itemIdCountChance : tmpRewards)
				{
					if (Rnd.get(100) < itemIdCountChance[2]) // if chance succeeds
					{
						ItemFunctions.addItem(opener, itemIdCountChance[0], itemIdCountChance[1], true);
						break;
					}
				}		
				
				Functions.npcSay(npc, opener.getName() + ", do you feel well rewarded?");
				break;
			case 3: // Item steal
				/*if (Rnd.get(100) <= 10 && opener.isDead())
				{
					ItemInstance item1 = getRandomItem(opener.getInventory().getItems());
					item1.setOwnerId(opener.getObjectId());
					item1.update();
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(item1);
					opener.sendPacket(iu);
					deadmen.sendPacket(iu);
					
					Functions.npcSay(npc, "Aww, sweet! " + opener.getName() + " has stolen " + deadmen.getName() + "\'s " + item1.getName());
				}
				else
				{
					tryAgain = true;
				}
				break;*/
			case 4:
				if (Rnd.get(100) <= 20)
				{
					if (Rnd.get(2) == 0)
					{
						SkillTable.getInstance().getInfo(7029, 4).getEffects(npc, opener, false, false); // Super Haste lvl 4 ;D
						Functions.npcSay(npc, "Oh noes, Super Haste user alert! Hurry up and kill " + opener.getName() + " before he kills you.");
					}
					else
					{
						Player rndPlr = Rnd.get(World.getAroundPlayers(npc, 10000, 200)); // 10000 range is enough ?
						SkillTable.getInstance().getInfo(7029, 1).getEffects(npc, rndPlr, false, false, 3* 60000, false); // Super Haste lvl 4 ;D
						Functions.npcSay(npc, "Super Haste released! Hehehe, lets find out who got it.");
					}
				}
				else if (Rnd.get(2) == 0)
					tryAgain = true;
				else
				{
					opener.sendPacket(new SystemMessage2(SystemMsg.NOTHING_HAPPENED));
					Functions.npcSay(npc, "Im not on a mood to do something, cya later!");
				}
				break;
			case 5:
				//TODO: spawn some rb. I dont want fuck with this boring shit :D just fuck it
				opener.sendMessage("The chest have raidboss summoning item inside. Unfortunately its locked");
				break;
		}
		
		if (tryAgain)
			deadmanChestFuncs(npc, opener);
		else
			npc.deleteMe();
	}
	
	private void giveDamage(Player opener, NpcInstance npc, int radius)
	{
		for (Player player : World.getAroundPlayers(npc, radius, 200))
		{
			if (player == opener)
				continue;
			if (player.getPvpFlag() > 0 || player.getKarma() > 0 || player.getReflectionId() > 0)
				player.reduceCurrentHp(Rnd.get(1, 15000), opener, null, true, true, false, false, false, false, false);
			else
				player.reduceCurrentHp(Rnd.get(1, 15000), npc, null, true, true, false, false, false, false, false);
		}
	}
	
	private void givePetrify(Player opener, NpcInstance npc, int radius)
	{
		Skill petrify = SkillTable.getInstance().getInfo(367, 205);
		for (Player player : World.getAroundPlayers(npc, radius, 200))
		{
			if (player == opener)
				continue;
			petrify.getEffects(npc, player, false, false);
		}
	}
	
	private void distributeItem(Player player, int itemId, int count)
	{
		if (player.getParty() == null)
			ItemFunctions.addItem(player, itemId, count, true);
		else
			Functions.addItemToParty(player, itemId, count);
	}
	
	private static NpcInstance spawnChest(int npcId, Player player)
	{
		// TODO: chest.. more options animation and etc..
		try
		{
			NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
			if (template != null)
			{
				Location loc = new Location(player.getX(), player.getY(), player.getZ() + 20);
				NpcInstance npc = template.getNewInstance();
				npc.setSpawnedLoc(loc);
				npc.setLoc(loc);
				npc.setHeading(player.getHeading());
				npc.spawnMe();
				
				return npc;
			}
		}
		catch (Exception e1)
		{
			_log.warn("DeadManChest: Could not spawn Npc " + npcId);
		}
		
		return null;
	}
	
	private String getKillstreakName(int n)
	{
		switch (n)
		{
			case 0:
			case 1:
			case 2:
			case 3:
				return "killing spree";
			case 4:
			case 5:
				return "dominating";
			case 6:
			case 7:		
			case 8:
				return "mega kill";
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:	
			case 15:
				return "unstoppable";
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
				return "wicked sick";
			case 21:
			case 22:
			case 23:
			case 24:
			case 25:
				return "monster kill";
			case 26:
			case 27:
			case 28:
			case 29:
			case 30:
				return "GODLIKE";
			default:
				return "Beyond GODLIKE";
		}
	}
	
	private static void getZoneForCharacter(Creature character)
	{
		for(String s : ZONES)
		{
			Zone zone = ZoneHolder.getZone(s);
			if (zone != null && zone.getInsidePlayers().contains(character))
				SkillTable.getInstance().getInfo(1323, 1).getEffects(character, character, false, false);
		}
	}

	public void init()
	{
		_zoneListener = new ZoneListener();

		if (Config.PLAYER_KILL_ALLOW_CUSTOM_PVP_ZONES)
		{
			for(String s : ZONES)
			{
				Zone zone = ZoneHolder.getZone(s);
				zone.addListener(_zoneListener);
				zone.setType(ZoneType.no_summon);
				zone.setType(ZoneType.special_pvp);
				zone.setType(ZoneType.SIEGE);
				zone.setActive(true);
			}
		}
		
		if (Config.ENABLE_PLAYER_KILL_SYSTEM)
			CharListenerList.addGlobal(this);

		_log.info("PlayerKill: System has been loaded.");
	}
	
	private void announceToAll(String name, String text, ChatType type)
	{
		Say2 cs = new Say2(0, type, name, text);
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			//if (!NexusEvents.isInEvent(player))
			//	continue;
			
			if (!player.getVarB("PlayerKillSystem"))
				continue;
			
			player.sendPacket(cs);
		}
	}

	@Override
	public void onKill(Creature actor, Creature victim)
	{
		if (Config.ENABLE_PLAYER_KILL_SYSTEM)
			return;
		
		// If not player or summon
		if (actor.getPlayer() == null)
			return;
		
		Player killedPlayer = victim.getPlayer();
		if (killedPlayer == null || actor == null)
			return;
		
		if (!killedPlayer.isPlayer())
			return;
		
		if (NexusEvents.isInEvent(actor) || NexusEvents.isInEvent(killedPlayer))
			return;
		
		if (killedPlayer.getKillsInRow() >= 3)
		{
			if (Config.ENABLE_PLAYER_COUNTERS && actor != null && actor.getPlayer() != null)
				actor.getPlayer().getCounters().addPoint("_Kill_Sprees_Ended");
			
			announceToAll("[KillstreakSystem]", actor.getName() + " has just ended " + killedPlayer.getName() + "'s " + getKillstreakName(killedPlayer.getKillsInRow()) + " streak!", ChatType.BATTLEFIELD);
		}
		
		//if(!killedPlayer.isNoblesseBlessed() && killedPlayer.isInsideZone(Creature.ZONE_TOWN))
			//SkillTable.getInstance().getInfo(1323, 1).getEffects(killedPlayer, killedPlayer);
		
		getZoneForCharacter(killedPlayer);
		
		killedPlayer.setKillsInRow(0);
		
		if(actor.isPlayer())
		{
			//if (actor.getPlayer().checkIfKillIsFeed(killedPlayer))
			//	return;
			
			if (Config.PLAYER_KILL_SPAWN_UNIQUE_CHEST)
				if (Rnd.get(100) < (7 + (killedPlayer.getKillsInRow() * 3))) // 7 + kills in row * 3 = chance..
					chests.put(spawnChest(NPC_ID, killedPlayer), killedPlayer);
			
			// Elementals
			if (Config.PLAYER_KILL_INCREASE_ATTRIBUTE)
			{
				ItemInstance actorWpn = actor.getPlayer().getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				ItemInstance killedPlayerWpn = killedPlayer.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				
				if (actorWpn == null || killedPlayerWpn == null)
					return;
				
				int elementalDiff = actorWpn.getAttackElementValue() - killedPlayerWpn.getAttackElementValue();
				
				if (actorWpn.getAttackElementValue() >= 300 && actorWpn.getAttackElementValue() < 450) // If actor wpn has 300-445 element
				{
					if (elementalDiff <= 25 && elementalDiff > 0) // If actor's element surpasses killed's element by 25 or less.
					{
						actorWpn.setAttributeElement(actorWpn.getAttackElement(), Math.min(450, actorWpn.getAttackElementValue() + 5));
						actor.sendMessage("Your weapon's element has been increased by 5.");
					}
					else if (elementalDiff < 0) // If actor's element is less than the killed one
					{
						elementalDiff = Math.abs(elementalDiff); // First set this negative value to positive :)
						
						// Example               ((80 / 15 = 5.33) = 5) * 5 = 25 element stolen
						int eleStolen = (elementalDiff / 15) * 5;
						eleStolen = Math.max(5, eleStolen); // Prevent 0
						
						// Increase actor's wpn element and decrease killed player's wpn element.
						actorWpn.setAttributeElement(actorWpn.getAttackElement(), Math.min(450, actorWpn.getAttackElementValue() + eleStolen));
						killedPlayerWpn.setAttributeElement(killedPlayerWpn.getAttackElement(), Math.max(300, killedPlayerWpn.getAttackElementValue() - eleStolen));
						actor.sendMessage("Your weapon's element has been increased by "+eleStolen+".");
						killedPlayer.sendMessage("Your weapon's element has been decreased by "+eleStolen+".");
					}
				}
				else if (actorWpn.getAttackElementValue() < 300 && killedPlayerWpn.getAttackElementValue() > 300) // If actor's element is lesser than 300 but the killed's element is more
				{
					elementalDiff = Math.abs(elementalDiff); // First set this negative value to positive :)
					
					int eleStolen = (elementalDiff / 15) * 5;
					eleStolen = Math.max(5, eleStolen); // Prevent 0
					eleStolen = Math.min(100, eleStolen); // Do not allow more than 100 element to be stolen
					
					killedPlayerWpn.setAttributeElement(killedPlayerWpn.getAttackElement(), Math.max(300, killedPlayerWpn.getAttackElementValue() - eleStolen));
					killedPlayer.sendMessage("Your weapon's element has been decreased by "+eleStolen+".");
				}
			}
			
			// Enchants
			if(Config.PLAYER_KILL_GIVE_ENCHANTS && Rnd.get(100) < 9 && !actor.isInZone(ZoneType.special_pvp))
			{
				if(Rnd.get(100) < 30)
					distributeItem(actor.getPlayer(), 960, 1); // EAS
				else
					distributeItem(actor.getPlayer(), 959, 1);// EWS
			}
			
			// Life Stones
			if(Config.PLAYER_KILL_GIVE_LIFE_STONE && Rnd.get(100) < 5 && !actor.isInZone(ZoneType.special_pvp))
			{
				if(Rnd.get(100) < 40)
					distributeItem(actor.getPlayer(), 14168, 1); // Hi-grade 84 life stone
				else
					distributeItem(actor.getPlayer(), 14169, 1); // TOP 84 life stone
			}
			
			// Special PvP Zone
			if(Config.PLAYER_KILL_GIVE_MANTRAS && Rnd.get(100) < 20 && actor.isInZone(ZoneType.special_pvp))
			{
				if(Rnd.get(100) < 50)
					distributeItem(actor.getPlayer(), MANTRAS[Rnd.get(MANTRAS.length)], 1); // MANTRA	
			    else if(Rnd.get(100) < 30)
					distributeItem(actor.getPlayer(), 6393, 1); // Event Gliter Medal
				else if(Rnd.get(100) < 10)
					distributeItem(actor.getPlayer(), 14721, 1); // Event Apiga
				else if (Rnd.get(100) < 2)
					distributeItem(actor.getPlayer(), 6578, 1); // CRYSTAL ENCHANT ARMOR
				else if (Rnd.get(100) < 1)
					distributeItem(actor.getPlayer(), 6577, 1); // CRYSTAL ENCHANT weapon
				else
					distributeItem(actor.getPlayer(), 57, 2000000); // Adena
			}
			
			// Fame
			if(Config.PLAYER_KILL_AQUIRE_FAME && actor.getPlayer().getKillsInRow() >= 5)
			{
				int aquiredFame = 0;
				if (actor.getPlayer().isInZone(ZoneType.special_pvp))
					aquiredFame = 200 + (25 * killedPlayer.getKillsInRow());
				else					
					aquiredFame = 125 + (25 * killedPlayer.getKillsInRow());// 125 + 25 * kills in row = total fame that earn..

				actor.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_REPUTATION_SCORE).addNumber(actor.getPlayer().getFame() + aquiredFame));
				actor.getPlayer().sendPacket(new UserInfo(actor.getPlayer()));
			}
			
			// Item Copier
			/*if (Rnd.get(100) <= killedPlayer.getKillsInRow())
			{
				List<ItemInstance> equipedItems = getEquipedItems(killedPlayer);
				
				for (ItemInstance equipedItem : equipedItems)
				{
					if (equipedItem == null)
						continue;
					
					ItemInstance item = ItemTable.getInstance().createItem("ItemCopier", equipedItem.getItemId(), 1, actor.getActingPlayer(), killedPlayer);
					item.setAugmentation(equipedItem.getAugmentation());
					if (equipedItem.getElementals() != null)
						for (Elementals element : equipedItem.getElementals())
							item.setElementAttr(element.getElement(), element.getValue());
					item.setEnchantLevel(equipedItem.getEnchantLevel());
					item._time = System.currentTimeMillis() + 60000;
					item.scheduleLifeTimeTask();
					actor.getActingPlayer().addItem("ItemCopier", item, killedPlayer, true);
				}
			}*/
			
			// Killstreak System
			actor.getPlayer().setKillsInRow(actor.getPlayer().getKillsInRow() + 1);
			
			String msg = null;
			
			switch (actor.getPlayer().getKillsInRow())
			{
				/*
				case 1:
					break;
				
				case 2:
					actor.getActingPlayer().sendPacket(new ExShowScreenMessage("Double Kill!", 10000));
					actor.getActingPlayer().sendPacket(new PlaySound(1, "Double_Kill", 0, 0, 0, 0, 0));
					break;
				*/
				case 3:
					actor.getPlayer().sendPacket(new ExShowScreenMessage("Killing Spree!", 1000));
					//actor.getPlayer().sendPacket(new PlaySound(1, "killing_spree", 0, 0, 0, 0, 0));
					msg = actor.getName() + " is on a killing spree!";
					break;
				
				case 5:
					actor.getPlayer().sendPacket(new ExShowScreenMessage("Dominating!", 1000));
					//actor.getPlayer().sendPacket(new PlaySound(1, "Dominating", 0, 0, 0, 0, 0));
					msg = actor.getName() + " is dominating!";
					break;
				
				case 8:
					actor.getPlayer().sendPacket(new ExShowScreenMessage("Mega Kill!", 1000));
					//actor.getPlayer().sendPacket(new PlaySound(1, "MegaKill", 0, 0, 0, 0, 0));
					msg = actor.getName() + " has a mega kill!";
					break;
				
				case 15:
					actor.getPlayer().sendPacket(new ExShowScreenMessage("Unstoppable!!", 1000));
					//actor.getPlayer().sendPacket(new PlaySound(1, "Unstoppable", 0, 0, 0, 0, 0));
					msg = actor.getName() + " is unstoppable!!";
					break;
				
				case 20:
					actor.getPlayer().sendPacket(new ExShowScreenMessage("Wicked Sick!!", 1000));
					//actor.getPlayer().sendPacket(new PlaySound(1, "WickedSick", 0, 0, 0, 0, 0));
					msg = actor.getName() + " is wicked sick!!";
					break;
				
				case 25:
					actor.getPlayer().sendPacket(new ExShowScreenMessage("Monster Kill!!", 1000));
					//actor.getPlayer().sendPacket(new PlaySound(1, "monster_kill", 0, 0, 0, 0, 0));
					msg = actor.getName() + " has a monster kill!!";
					break;
				
				case 30:
					actor.getPlayer().sendPacket(new ExShowScreenMessage("GODLIKE!!!", 1000));
					//actor.getPlayer().sendPacket(new PlaySound(1, "GodLike", 0, 0, 0, 0, 0));
					msg = actor.getName() + " is GODLIKE!!!";
					break;
				
				case 40:
					actor.getPlayer().sendPacket(new ExShowScreenMessage("Beyond GODLIKE!!!", 1000));
					//actor.getPlayer().sendPacket(new PlaySound(1, "GodLike", 0, 0, 0, 0, 0));
					msg = actor.getName() + " is beyond GODLIKE. Someone KILL HIM!!!";
					break;
				
				default:
					if (actor.getPlayer().getKillsInRow() > 40)
					{
						actor.getPlayer().sendPacket(new ExShowScreenMessage("Beyond GODLIKE!!!", 1000));
						//actor.getPlayer().sendPacket(new PlaySound(1, "GodLike", 0, 0, 0, 0, 0));
						msg = actor.getName() + " is beyond GODLIKE. Someone KILL HIM!!!";
					}
					if(actor.getPlayer().getKillsInRow() >= 50)
					{
						_log.warn(actor.getPlayer().getName()+" have "+actor.getPlayer().getKillsInRow()+" kills in row. Check him!");
						AdminTable.broadcastMessageToGMs(actor.getPlayer().getName()+" have "+actor.getPlayer().getKillsInRow()+" kills in row. Check him!");
						//((Player) actor).logFeed(killedPlayer, "KILLSTREAK - "+actor.getPlayer().getKillsInRow());
						//Announcements.getInstance().announceToAll(actor.getActingPlayer().getName()+" is probably feeding. Screen this and report him!");
					}
					break;
			}
			
			if (msg != null)
				announceToAll("[KillstreakSystem]", msg, ChatType.BATTLEFIELD);
		}
	}

	@Override
	public boolean ignorePetOrSummon()
	{
		return true;
	}
}