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

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ArmorSetsHolder;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2r.gameserver.model.ArmorSet;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.clientpackets.EnterWorld;
import l2r.gameserver.network.serverpackets.HennaEquipList;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.TutorialCloseHtml;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Infern0
 *
 */
public class PvPCharacterIntro implements OnPlayerEnterListener
{
	private final Logger _log = LoggerFactory.getLogger(PvPCharacterIntro.class);
	private static PvPCharacterIntro _instance;
	
	public PvPCharacterIntro()
	{
		CharListenerList.addGlobal(this);
		_log.info("CharacterIntro System has been laoded!");
	}
	
	public static PvPCharacterIntro getInstance()
	{
		if(_instance == null)
			_instance = new PvPCharacterIntro();
		return _instance;
	}
	
	
	private static Map<String, Location> TOWN_LOCATIONS = new HashMap<String, Location>();
	static
	{
		TOWN_LOCATIONS.put("Giran", new Location(83375, 147953, -3401));
		TOWN_LOCATIONS.put("Gludin", new Location(-80789, 149817, -3040));
		TOWN_LOCATIONS.put("Gludio", new Location(-12717, 122775, -3113));
		TOWN_LOCATIONS.put("Dion", new Location(15659, 142927, -2702));
		TOWN_LOCATIONS.put("Heine", new Location(111394, 219350, -3542));
		TOWN_LOCATIONS.put("Hunter", new Location(117073, 76895, -2697));
		TOWN_LOCATIONS.put("Oren", new Location(82901, 53208, -14927));
		TOWN_LOCATIONS.put("Aden", new Location(146814, 25790, -2009));
		TOWN_LOCATIONS.put("Goddard", new Location(147941, -55278, -2729));
		TOWN_LOCATIONS.put("Rune", new Location(43782, -47707, -793));
		TOWN_LOCATIONS.put("Schuttgart", new Location(87092, -143381, -1289));
		TOWN_LOCATIONS.put("FarmLocation", new Location(-71512, 258456, -3133)); // dummy farm location, set your farm location here.
	}
	
	public void showTutorialHTML(String toShow, Player player)
	{
		String html = HtmCache.getInstance().getNotNull("mods/pvpcharacterintro/" + toShow + ".htm", player);
		html = html.replaceAll("%playername%", "" + player.getName());
		html = html.replaceAll("%palyerOID%", "" + player.getObjectId());
		html = html.replaceAll("%mainclass%", player.getClassId().name());
		
		player.sendPacket(new TutorialShowHtml(html));
	}

	public void sendIntro(Player activeChar)
	{
		if (getCharactertStep(activeChar).equalsIgnoreCase("finished"))
			return;
		
		switch (getCharactertStep(activeChar))
		{
			case "":
			case "intro":
				showClassMenu(activeChar);
				break;
			case "armor_select":
				showArmorMenu(activeChar);
				break;
			case "weapon_select":
				showWeaponMenu(activeChar);
				break;
			case "attribute":
				showAttributeMenu(activeChar);
				break;
			case "dyes":
				showDyesMenu(activeChar);
				break;
			case "teleport":
				showTutorialHTML("teleport", activeChar);
				break;
		}
	}

	public void bypassIntro(Player activeChar, String bypass)
	{
		if (bypass.startsWith("_pvpcharintro:chooseClass"))
		{
			String[] cm = bypass.split(" ");
			setClass(activeChar, Integer.parseInt(cm[1]));
		}
		else if (bypass.startsWith("_pvpcharintro:chooseArmor"))
		{
			String[] cm = bypass.split(" ");
			setArmor(activeChar, Integer.parseInt(cm[1]));
		}
		else if (bypass.startsWith("_pvpcharintro:chooseWeapon"))
		{
			String[] cm = bypass.split(" ");
			setWeapon(activeChar, Integer.parseInt(cm[1]));
		}
		else if (bypass.startsWith("_pvpcharintro:attributeItem"))
		{
			String[] cm = bypass.split(" ");
			setAttribute(activeChar, cm);
		}
		else if (bypass.startsWith("_pvpcharintro:showAttributeMenu"))
		{
			showAttributeMenu(activeChar);
		}
		else if (bypass.startsWith("_pvpcharintro:jewery"))
		{
			equipJewels(activeChar);
		}
		else if (bypass.startsWith("_pvpcharintro:dyeMenu"))
		{
			showDyesMenu(activeChar);
		}
		else if (bypass.startsWith("_pvpcharintro:dyeWindow"))
		{
			showDyeWindow(activeChar);
		}
		else if (bypass.startsWith("_pvpcharintro:finish"))
		{
			String[] cm = bypass.split(" ");
			finish(activeChar, cm[1]);
		}
	}
	
	private void showDyeWindow(Player player)
	{
		player.sendPacket(new HennaEquipList(player, false));
	}
	
	private void showDyesMenu(Player player)
	{
		showTutorialHTML("dyes", player);
		showDyeWindow(player);
	}
	
	private void showArmorMenu(Player player)
	{
		if(player.getRace() == Race.kamael)
		{
			showTutorialHTML("KamaelArmors", player);
			return;
		}
		
		if(player.getClassId().isMage())
			showTutorialHTML("MageArmors", player);
		else
			showTutorialHTML("FighterArmors", player);
	}
	
	private void showWeaponMenu(Player player)
	{
		if(player.getRace() == Race.kamael)
		{
			showTutorialHTML("KamaelWeapons", player);
			return;
		}
		
		if(player.getClassId().isMage())
			showTutorialHTML("MageWeapons", player);
		else
			showTutorialHTML("FighterWeapons", player);
	}
	
	public void setClass(Player player, int classId)
	{
		if(player == null)
			return;
		
		if(player.getClassId().getLevel() == 3)
			player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_YOUR_THIRDCLASS_TRANSFER_QUEST); // для 3 профы
		else
			player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER); // для 1 и 2 профы

		player.setClassId(classId, false, false);
		player.broadcastUserInfo(true);
		
		if(player.getClassId().getLevel() == 4)
		{
			setCharacterStep(player, "armor_select");
			showArmorMenu(player);
			return;
		}
			
		showClassMenu(player);
	}

	public void showClassMenu(Player player)
	{
		if(player == null)
			return;
		
		String text = HtmCache.getInstance().getNotNull("mods/pvpcharacterintro/classChange.htm", player);
		
		ClassId classId = player.getClassId();
		int jobLevel = classId.getLevel();
		int level = player.getLevel();
		
		if (jobLevel == 4)
		{
			showArmorMenu(player);
			return;
		}
		
		StringBuilder html = new StringBuilder();
		
		if((level >= 20 && jobLevel == 1 || level >= 40 && jobLevel == 2 || level >= 76 && jobLevel == 3))
		{
			html.append("<center><table width=\"269\" bgcolor=\"0f100f\" cellpadding=\"0\" cellspacing=\"7\" valign=\"center\"><tr>");
			for(ClassId cid : ClassId.values())
			{
				if(cid == ClassId.inspector)
					continue;
				
				if(cid.childOf(classId) && cid.level() == classId.level() + 1)
					html.append("<td><center><font name=hs10><a action=\"bypass _pvpcharintro:chooseClass ").append(cid.getId()).append("\">" + cid.name() + "</a></font></center></td>");
			}
			html.append("<br><br>");
			html.append("</center>");
			html.append("</tr></table></center>");
		}
		
		text = text.replaceAll("%availableClasses%", html.toString());
		text = text.replaceAll("%playername%", "" + player.getName());
		
		player.sendPacket(new TutorialShowHtml(text));
	}
	
	public void setWeapon(Player player, int weaponId)
	{
		if (player == null)
			return;

		ItemTemplate item = ItemHolder.getInstance().getTemplate(weaponId);
		if (!item.isWeapon())
			return;
		
		player.getInventory().addItem(ItemFunctions.createItem(weaponId));
		player.getInventory().equipItem(player.getInventory().getItemByItemId(weaponId));
		player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1).addItemName(weaponId));

		activateShots(player);
		
		showAttributeMenu(player);
		setCharacterStep(player, "attribute");
	}
	
	public void setArmor(Player player, int setId)
	{
		if (player == null)
			return;

		ArmorSet armorSet = ArmorSetsHolder.getInstance().getSet(setId);
		
		if(armorSet == null)
			return;
		
		for (int items : armorSet.getPrimarySetItems())
		{
			if (items != 0)
			{
				player.getInventory().addItem(ItemFunctions.createItem(items));
				player.getInventory().equipItem(player.getInventory().getItemByItemId(items));
				player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1).addItemName(items));
			}
		}
		
		showWeaponMenu(player);
		setCharacterStep(player, "weapon_select");
	}
	
	public void equipJewels(Player player)
	{
		equipJewels(player, Math.max(0, player.getActiveWeaponInstance().getCrystalType().ordinal())); // equip jewels the same gear grade as weapon.
		
		showDyesMenu(player);
		setCharacterStep(player, "dyes");
	}
	
	public void showAttributeMenu(Player player)
	{
		if (player == null)
			return;
		
		String text = HtmCache.getInstance().getNotNull("mods/pvpcharacterintro/armorAttribute.htm", player);
		
		int[] slotstocheck =
		{
			Inventory.PAPERDOLL_LRHAND,
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_HEAD,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET
		};
		
		int clansvisual = 0;
		//int attributed = 0;
		// lets check for armor attribute.
		for (Integer slot : slotstocheck)
		{
			ItemInstance item = player.getInventory().getPaperdollItem(slot);
			if (item == null || item.getTemplate().isShield())
				continue;
			
			clansvisual++;
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			
			// armors & weapons
			if (item.isArmor() || item.isWeapon())
			{
				text = text.replaceAll("%icon" + clansvisual + "%", "icon." + item.getTemplate().getIcon());
				text = text.replaceAll("%name" + clansvisual + "%", "" + item.getName());
				
				for (Element el : Element.values())
				{
					if (el.name().equals("NONE"))
						continue;
					
					if (item.getAttributeElementValue(Element.getReverseElement(el), false) == 0 && item.getAttributeElementValue(el, false) == 0)
						sb.append("<a action=\"bypass _pvpcharintro:attributeItem " + item.getObjectId() + " " + el.getId() + "\">" + el.name() + " </a> &nbsp;");
					
					if (item.getAttributeElementValue(el, false) > 0)
					{
						sb2.append(el.name() + " &nbsp;");
						//attributed++;
					}
				}
				
				if (item.isWeapon() && item.getAttributeElement() != Element.NONE)
					text = text.replaceAll("%addelement" + clansvisual + "%", "&nbsp;");
				else
					text = text.replaceAll("%addelement" + clansvisual + "%", sb.toString());
				
				text = text.replaceAll("%attribute" + clansvisual + "%", sb2.toString());
			}
		}
		
		if(clansvisual < 6)
		{
			for(int d = clansvisual + 1; d != 7; d++)
			{
				text = text.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				text = text.replaceAll("%name" + d + "%", "&nbsp;");
				text = text.replaceAll("%attribute" + d + "%", "&nbsp;");
				text = text.replaceAll("%addelement" + d + "%", "&nbsp;");
			}
		}
		
		text = text.replaceAll("%playername%", "" + player.getName());
		
		player.sendPacket(new TutorialShowHtml(text));
	}

	public void setAttribute(Player player, String[] cm)
	{
		if (player == null)
			return;
		
		if (cm.length != 3)
			return;
		
		int itemObjectid = Integer.parseInt(cm[1]);
		int attributeType = Integer.parseInt(cm[2]);
		int value = 120;
		
		ItemInstance item = player.getInventory().getItemByObjectId(itemObjectid);
		if (item == null)
			return;

		if (item.isWeapon())
			value = 300;
		
		player.getInventory().unEquipItem(item);
		item.setAttributeElement(Element.getElementById(attributeType), value);
		item.setJdbcState(JdbcEntityState.UPDATED);
		item.update();
		
		player.getInventory().equipItem(item);
		player.sendPacket(new InventoryUpdate().addModifiedItem(item));
		player.broadcastUserInfo(true);
		
		showAttributeMenu(player);
	}

	public void finish(Player player, String location)
	{
		if(player == null)
			return;
		
		player.unblock();
		
		player.fullHeal();
		
		Location loc = TOWN_LOCATIONS.get(location);
		if (loc != null)
			player.teleToLocation(loc);
		else
			player.teleToLocation(TOWN_LOCATIONS.get("Giran"));// default to giran
		
		setCharacterStep(player, "finished");
		player.sendPacket(new PlaySound(Quest.SOUND_FINISH));
		player.sendPacket(TutorialCloseHtml.STATIC);
	}
	
	@Override
	public void onPlayerEnter(Player player)
	{
		if (player == null)
			return;

		if (getCharactertStep(player).equalsIgnoreCase("finished"))
			return;
		
		long exp_add = Experience.LEVEL[Experience.getMaxLevel()] - player.getExp();
		player.addExpAndSp(exp_add, 0);
		
		if (!player.isBlocked())
			player.block();
		
		player.sendChatMessage(0, ChatType.BATTLEFIELD.ordinal(), "CharacterIntro", "Hello, " + player.getName() + " you character is blocked until you complete our character intro!");
		player.sendChatMessage(0, ChatType.BATTLEFIELD.ordinal(), "CharacterIntro", "For problems or questions, please contact server staff!");
		
		sendIntro(player);
	}
	
	private void activateShots(Player actor)
	{
		if (actor == null)
			return;
		
		// Give shots and make them auto.
		int soulId = -1;
		int bspiritId = -1;
		
		if (actor.getActiveWeaponItem() != null)
		{
			switch (actor.getActiveWeaponItem().getCrystalType())
			{
				case NONE:
					soulId = 1835;
					bspiritId = 3947;
					break;
				case D:
					soulId = 1463;
					bspiritId = 3948;
					break;
				case C:
					soulId = 1464;
					bspiritId = 3949;
					break;
				case B:
					soulId = 1465;
					bspiritId = 3950;
					break;
				case A:
					soulId = 1466;
					bspiritId = 3951;
					break;
				case S:
				case S80:
				case S84:
					soulId = 1467;
					bspiritId = 3952;
					break;
			}
			
			// Soulshots
			if (soulId > -1)
			{
				long shotsCount = actor.getInventory().getCountOf(soulId);
				if (shotsCount < 10000)
					actor.getInventory().addItem(soulId, 1000 - shotsCount);
			}
			
			// Blessed Spirishots
			if (bspiritId > -1)
			{
				long shotsCount = actor.getInventory().getCountOf(bspiritId);
				if (shotsCount < 10000)
					actor.getInventory().addItem(bspiritId, 10000 - shotsCount);
			}
			
			EnterWorld.verifyAndLoadShots(actor);
		}
	}
	
	
	public void setCharacterStep(Player activeChar, String value)
	{
		if (activeChar == null)
			return;
		
		activeChar.setVar("intro_step", value);
	}
	
	public String getCharactertStep(Player activeChar)
	{
		if (activeChar == null)
			return "";
		
		try
		{
			return activeChar.getVar("intro_step", "");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
	public void equipJewels(Player player, int grade)
	{
		int[] jewels = null;
		
		switch (grade)
		{
			case 0:
				jewels = new int[]
				{
					882,
					882,
					851,
					851,
					914
				}; // D-grade shareniq TOP
				break;
			case 1:
				jewels = new int[]
				{
					888,
					888,
					857,
					857,
					919
				}; // C-grade Blessed set TOP
				break;
			case 2:
				jewels = new int[]
				{
					895,
					895,
					864,
					864,
					926
				}; // B-grade Black ore set TOP
				break;
			case 3:
				jewels = new int[]
				{
					893,
					893,
					862,
					862,
					924
				}; // A-grade Majestic set TOP
				break;
			case 4:
				jewels = new int[]
				{
					889,
					889,
					858,
					858,
					920
				}; // S-grade Tateossian set TOP
				break;
			case 5:
				jewels = new int[]
				{
					9457,
					9457,
					9455,
					9455,
					9456
				}; // Dynasty set TOP
				break;
			case 6:
				jewels = new int[]
				{
					15723,
					15723,
					15724,
					15724,
					15725
				}; // S82 Moirai set TOP
				break;
			case 7:
			case 8:
				jewels = new int[]
				{
					14165,
					14165,
					14163,
					14163,
					14164
				}; // S84 Vesper set TOP
		}
		
		if (jewels == null)
			return;
		
		for (int jewel : jewels)
		{
			player.getInventory().addItem(ItemFunctions.createItem(jewel));
			player.getInventory().equipItem(player.getInventory().getItemByItemId(jewel));
			
			player.sendChanges();
		}
	}
}
