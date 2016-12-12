package services.community;

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2r.gameserver.templates.item.EtcItemTemplate;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.Log;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantCommunity extends Functions implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(EnchantCommunity.class);
	
	@Override
	public void onLoad()
	{
		if (Config.COMMUNITYBOARD_ENCHANT_ENABLED)
		{
			_log.info("CommunityBoard: Enchant Community service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if (Config.COMMUNITYBOARD_ENCHANT_ENABLED)
		{
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbsechant",
			"_bbsechantlist",
			"_bbsechantChus",
			"_bbsechantAtr",
			"_bbsechantgo",
			"_bbsechantuseAtr",
			"_bbsRemoveAttribute"
		};
	}
	
	@Override
	public void onBypassCommand(Player activeChar, String bypass)
	{
		if (!CheckCondition(activeChar))
			return;
		
		if(bypass.equalsIgnoreCase("_bbsRemoveAttribute"))
		{
			activeChar.sendPacket(new ExShowBaseAttributeCancelWindow(activeChar));
		}
		if (bypass.startsWith("_bbsechant"))
		{
			String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/enchant.htm", activeChar);
			StringBuilder sb = new StringBuilder("");
			sb.append("<table width=400>");
			ItemInstance[] arr = activeChar.getInventory().getItems();
			int len = arr.length;
			for (int i = 0; i < len; i++)
			{
				ItemInstance _item = arr[i];
				// Here we will split by conifg coz on elemental i dont wanna see jewels.
				if (Config.ALLOW_BBS_ENCHANT_ATT)
					if (_item == null || _item.getTemplate().getBodyPart() == (ItemTemplate.SLOT_R_EAR | ItemTemplate.SLOT_L_EAR) ||  _item.getTemplate().getBodyPart() == (ItemTemplate.SLOT_L_EAR | ItemTemplate.SLOT_R_EAR) || _item.getTemplate().getBodyPart() == (ItemTemplate.SLOT_R_FINGER | ItemTemplate.SLOT_L_FINGER) || _item.getTemplate().getBodyPart() == (ItemTemplate.SLOT_L_FINGER | ItemTemplate.SLOT_R_FINGER) || _item.getTemplate().getBodyPart() == (ItemTemplate.SLOT_R_FINGER | ItemTemplate.SLOT_L_FINGER) || _item.getTemplate().getBodyPart() == ItemTemplate.SLOT_NECK || _item.getTemplate() instanceof EtcItemTemplate || _item.getTemplate().isBelt() || _item.isCursed() || _item.isArrow() || _item.getTemplate().isBracelet() || _item.getTemplate().isCloak() || _item.isNoEnchant() || !_item.isEquipped() || _item.isShieldNoEnchant() || _item.getItemType() == ArmorType.SIGIL || _item.isHeroWeapon() || _item.getItemId() >= 7816 && _item.getItemId() <= 7831 || _item.isShadowItem() || _item.isCommonItem() || _item.getEnchantLevel() >= (Config.COMMUNITYBOARD_MAX_ENCHANT + 1) || !_item.canBeEnchanted(true) || _item.getBodyPart() == ItemTemplate.SLOT_HAIR || _item.getBodyPart() == ItemTemplate.SLOT_DHAIR)
						continue;
				
				if (Config.ALLOW_BBS_ENCHANT_ELEMENTAR && Config.ALLOW_BBS_ENCHANT_ATT)
					if (_item == null || _item.getTemplate() instanceof EtcItemTemplate || _item.getTemplate().isBelt() || _item.isCursed() || _item.isArrow() || _item.getTemplate().isBracelet() || _item.getTemplate().isCloak() || _item.isNoEnchant() || !_item.isEquipped() || _item.isShieldNoEnchant() || _item.getItemType() == ArmorType.SIGIL || _item.isHeroWeapon() || _item.getItemId() >= 7816 && _item.getItemId() <= 7831 || _item.isShadowItem() || _item.isCommonItem() || _item.getEnchantLevel() >= (Config.COMMUNITYBOARD_MAX_ENCHANT + 1) || !_item.canBeEnchanted(true) || _item.getBodyPart() == ItemTemplate.SLOT_HAIR || _item.getBodyPart() == ItemTemplate.SLOT_DHAIR)
						continue;
				
				sb.append(new StringBuilder("<tr><td><img src=icon." + _item.getTemplate().getIcon() + " width=32 height=32></td><td>"));
				sb.append(new StringBuilder("<font color=\"LEVEL\">" + _item.getTemplate().getName() + " " + (_item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font><font color=3293F3>(+" + _item.getEnchantLevel())) + ")</font><br1>"));
				//sb.append(new StringBuilder("" + (_item.getAttributeElementValue() == 0 ? "" : "" + _item.getAttributeElement().name() + "<font color=3293F3>(" + _item.getAttributeElementValue())) + ")</font><br1>");
				//sb.append(new StringBuilder("Enchant for: <font color=\"LEVEL\">" + name + "</font>"));
				sb.append("<img src=\"l2ui.squaregray\" width=\"170\" height=\"1\">");
				sb.append("</td><td>");
				if (Config.ALLOW_BBS_ENCHANT_ELEMENTAR)
					sb.append(new StringBuilder("<button value=\"Enchant\" action=\"bypass _bbsechantlist:" + _item.getObjectId() + ";\" width=75 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"));
				sb.append("</td><td>");
				if (Config.ALLOW_BBS_ENCHANT_ATT)
					sb.append(new StringBuilder("<button value=\"Attribute\" action=\"bypass _bbsechantChus:" + _item.getObjectId() + ";\" width=75 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"));
				sb.append("</td></tr>");
			}
			
			sb.append("</table>");
			
			sb.append("<br>");
			sb.append("<br>");
			sb.append(new StringBuilder("<table width=400><tr><td><center><button value=\"Back\" action=\"bypass _bbspage:donate/Services\" width=75 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td></tr></table>"));
			sb.append("<br>");
			
			html = html.replace("%enchanter%", sb.toString());
			
			ShowBoard.separateAndSend(html, activeChar);
		}
		if (bypass.startsWith("_bbsechantlist"))
		{
			if (!Config.ALLOW_BBS_ENCHANT_ELEMENTAR)
				return;
			
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			int ItemForEchantObjID = Integer.parseInt(mBypass[1]);
			String name = "None Name";
			String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/enchant.htm", activeChar);
			name = ItemHolder.getInstance().getTemplate(Config.COMMUNITYBOARD_ENCHANT_ITEM).getName();
			ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			
			StringBuilder sb = new StringBuilder("");
			sb.append("Select item: <br1><table width=300>");
			sb.append(new StringBuilder("<tr><td width=32><img src=icon." + EhchantItem.getTemplate().getIcon() + " width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>"));
			sb.append(new StringBuilder("<font color=\"LEVEL\">" + EhchantItem.getTemplate().getName() + " " + (EhchantItem.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font><font color=3293F3>(+" + EhchantItem.getEnchantLevel())) + ")</font><br1>"));
			
			//sb.append(new StringBuilder("Enchant for: <font color=\"LEVEL\">" + name + "</font>"));
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append(new StringBuilder("<td width=32><img src=icon." + EhchantItem.getTemplate().getIcon() + " width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>"));
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br>");
			sb.append("<br>");
			sb.append("<table border=0 width=400><tr><td width=200>");
			for (int i = 0; i < Config.COMMUNITYBOARD_ENCHANT_LVL.length; i++)
			{
				sb.append(new StringBuilder("<button value=\"Add +" + Config.COMMUNITYBOARD_ENCHANT_LVL[i] + " (Price:" + (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_PRICE_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_PRICE_ARMOR[i]) + " " + name + ")\" action=\"bypass _bbsechantgo:" + Config.COMMUNITYBOARD_ENCHANT_LVL[i] + ":" + (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_PRICE_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_PRICE_ARMOR[i]) + ":" + ItemForEchantObjID + ";\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
				// sb.append("<br1>");
			}
			sb.append("<br>");
			sb.append("</td></tr></table><br1><button value=\"Back\" action=\"bypass _bbsechant;\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			html = html.replace("%enchanter%", sb.toString());
			
			ShowBoard.separateAndSend(html, activeChar);
		}
		if (bypass.startsWith("_bbsechantChus"))
		{
			if (!Config.ALLOW_BBS_ENCHANT_ATT)
				return;
			
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			int ItemForEchantObjID = Integer.parseInt(mBypass[1]);
			String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/enchant.htm", activeChar);
			ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			
			StringBuilder sb = new StringBuilder("");
			sb.append("<center><table width=300>");
			sb.append(new StringBuilder("<tr><td width=32><img src=icon." + EhchantItem.getTemplate().getIcon() + " width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>"));
			sb.append(new StringBuilder("<font color=\"LEVEL\">" + EhchantItem.getTemplate().getName() + " " + (EhchantItem.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font><font color=3293F3>(+" + EhchantItem.getEnchantLevel())) + ")</font><br1>"));
			
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append(new StringBuilder("<td width=32><img src=icon." + EhchantItem.getTemplate().getIcon() + " width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>"));
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br>");
			sb.append("<br>");
			sb.append("<table border=0 width=400><tr><td width=200>");
			sb.append("<center><img src=icon.etc_wind_stone_i00 width=32 height=32></center><br>");
			sb.append(new StringBuilder("<button value=\"Wind \" action=\"bypass _bbsechantAtr:2:" + ItemForEchantObjID + ";\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
			sb.append("<br><center><img src=icon.etc_earth_stone_i00 width=32 height=32></center><br>");
			sb.append(new StringBuilder("<button value=\"Earth \" action=\"bypass _bbsechantAtr:3:" + ItemForEchantObjID + ";\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
			sb.append("<br><center><img src=icon.etc_fire_stone_i00 width=32 height=32></center><br>");
			sb.append(new StringBuilder("<button value=\"Fire \" action=\"bypass _bbsechantAtr:0:" + ItemForEchantObjID + ";\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
			sb.append("</td><td width=200>");
			sb.append("<center><img src=icon.etc_water_stone_i00 width=32 height=32></center><br>");
			sb.append(new StringBuilder("<button value=\"Water \" action=\"bypass _bbsechantAtr:1:" + ItemForEchantObjID + ";\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
			sb.append("<br><center><img src=icon.etc_holy_stone_i00 width=32 height=32></center><br>");
			sb.append(new StringBuilder("<button value=\"Divine \" action=\"bypass _bbsechantAtr:4:" + ItemForEchantObjID + ";\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
			sb.append("<br><center><img src=icon.etc_unholy_stone_i00 width=32 height=32></center><br>");
			sb.append(new StringBuilder("<button value=\"Dark \" action=\"bypass _bbsechantAtr:5:" + ItemForEchantObjID + ";\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
			sb.append("</td></tr></table><br><button value=\"Back\" action=\"bypass _bbsechant;\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
			html = html.replace("%enchanter%", sb.toString());
			
			ShowBoard.separateAndSend(html, activeChar);
		}
		if (bypass.startsWith("_bbsechantAtr"))
		{
			if (!Config.ALLOW_BBS_ENCHANT_ATT)
				return;
			
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			int attrType = Integer.parseInt(mBypass[1]);
			int ItemForEchantObjID = Integer.parseInt(mBypass[2]);
			String elementName = "";
				 if (attrType == 0) elementName = "<font color=\"F62817\">Fire</font>";
			else if (attrType == 1) elementName = "<font color=\"306EFF\">Water</font>";
			else if (attrType == 2) elementName = "<font color=\"4EE2EC\">Wind</font>";
			else if (attrType == 3) elementName = "<font color=\"7F462C\">Earth</font>";
			else if (attrType == 4) elementName = "<font color=\"E3E4FA\">Divine</font>";
			else if (attrType == 5) elementName = "<font color=\"6D6968\">Dark</font>";
			String name = "None Name";
			String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/enchant.htm", activeChar);
			name = ItemHolder.getInstance().getTemplate(Config.COMMUNITYBOARD_ENCHANT_ITEM).getName();
			ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			
			StringBuilder sb = new StringBuilder("");
			sb.append("<centeR><table width=300>");
			sb.append(new StringBuilder("<tr><td width=32><img src=icon." + EhchantItem.getTemplate().getIcon() + " width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>"));
			sb.append(new StringBuilder("<font color=\"LEVEL\">" + EhchantItem.getTemplate().getName() + " " + (EhchantItem.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font><font color=3293F3>(+" + EhchantItem.getEnchantLevel())) + ")</font><br1>"));
			
			sb.append(new StringBuilder("Elemental: " + elementName));
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append(new StringBuilder("<td width=32><img src=icon." + EhchantItem.getTemplate().getIcon() + " width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>"));
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br>");
			sb.append("<br>");
			
			if (EhchantItem.getTemplate().getName().contains("PvP") && Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PVP || (EhchantItem.getTemplate().getCrystalType() == ItemTemplate.Grade.S || EhchantItem.getTemplate().getCrystalType() == ItemTemplate.Grade.S80 || EhchantItem.getTemplate().getCrystalType() == ItemTemplate.Grade.S84))
			{
				sb.append("<table border=0 width=400><tr><td width=200>");
				for (int i = 0; i < (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON.length : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR.length); i++)
				{
					sb.append("<center><button value=\"Add +");
					sb.append(new StringBuilder((EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR[i]) + " (Cost: " + (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_ARMOR[i]) + " " + name + ")\" action=\"bypass _bbsechantuseAtr:" + (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR[i]) + ":" + attrType + ":" + (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_ARMOR[i]) + ":" + ItemForEchantObjID + ";\" width=290 height=30 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
					sb.append("<br1>");
				}
				sb.append("</td></tr></table><br1>");
			}
			else if (EhchantItem.getTemplate().getName().contains("PvP") && Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PVP || (EhchantItem.getTemplate().getCrystalType() == ItemTemplate.Grade.S || EhchantItem.getTemplate().getCrystalType() == ItemTemplate.Grade.S80 || EhchantItem.getTemplate().getCrystalType() == ItemTemplate.Grade.S84))
			{
				sb.append("<table border=0 width=400><tr><td width=200>");
				for (int i = 0; i < (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON.length : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR.length); i++)
				{
					sb.append(new StringBuilder("<center><button value=\"Add +" + (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR[i]) + " (Price:" + (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_ARMOR[i]) + " " + name + ")\" action=\"bypass _bbsechantuseAtr:" + (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR[i]) + ":" + attrType + ":" + (EhchantItem.getTemplate().isWeapon() != false ? Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_WEAPON[i] : Config.COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_ARMOR[i]) + ":" + ItemForEchantObjID + ";\" width=290 height=30 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
					sb.append("<br1>");
				}
				sb.append("</td></tr></table><br1>");
				sb.append("</table>");
			}
			else
			{
				sb.append("<table border=0 width=400><tr><td width=200>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<center><font color=\"LEVEL\">Enchant is not possible!</font></center>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("</td></tr></table><br1>");
			}
			sb.append("<br>");
			sb.append("<button value=\"Back\" action=\"bypass _bbsechant;\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
			html = html.replace("%enchanter%", sb.toString());
			
			ShowBoard.separateAndSend(html, activeChar);
		}
		if (bypass.startsWith("_bbsechantgo"))
		{
			if (!Config.ALLOW_BBS_ENCHANT_ELEMENTAR)
				return;
			
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			int enchVal = Integer.parseInt(mBypass[1]);
			int enchPrice = Integer.parseInt(mBypass[2]);
			int enchObjId = Integer.parseInt(mBypass[3]);
			
			ItemInstance itemToEnch = activeChar.getInventory().getItemByObjectId(enchObjId);
			if (activeChar.getInventory().destroyItemByItemId(Config.COMMUNITYBOARD_ENCHANT_ITEM, enchPrice))
			{
				itemToEnch.setEnchantLevel(enchVal);
				itemToEnch.setJdbcState(JdbcEntityState.UPDATED);
				itemToEnch.update();
				
				activeChar.getInventory().equipItem(itemToEnch);
				activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnch));
				activeChar.broadcastUserInfo(true);
				activeChar.broadcastCharInfo();
				
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "" + itemToEnch.getName() + " was enchanted to +" + enchVal + ".");
				Log.addDonation(new StringBuilder(activeChar.getName() + " enchant item: " + itemToEnch.getName() + " val: " + enchVal + "").toString(), "Enchanter");
				onBypassCommand(activeChar, "_bbsechant");
			}
			else
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "You need " + enchPrice + " " + ItemHolder.getInstance().getTemplateName(Config.COMMUNITYBOARD_ENCHANT_ITEM) + " to use this option.");
		}
		if (bypass.startsWith("_bbsechantuseAtr"))
		{
			if (!Config.ALLOW_BBS_ENCHANT_ATT)
				return;
			
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			int attrVal = Integer.parseInt(mBypass[1]);
			int attrType = Integer.parseInt(mBypass[2]);
			int attrPrice = Integer.parseInt(mBypass[3]);
			int attrObjId = Integer.parseInt(mBypass[4]);
			
			ItemInstance itemToAttr = activeChar.getInventory().getItemByObjectId(attrObjId);
			
			Element el = Element.getElementById(attrType);
			
			if (itemToAttr.getAttributeElementValue(el, false) >= attrVal)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "You cannot set same or lower value to your item.");
				return;
			}
			
			if (itemToAttr.isWeapon())
			{
				if (itemToAttr.getAttackElementValue() != 0)
				{
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "Please remove the current attribute from the weapon and try again.");
					onBypassCommand(activeChar, "_bbsechant");
					return;
				}
				if (activeChar.getInventory().destroyItemByItemId(Config.COMMUNITYBOARD_ENCHANT_ITEM, attrPrice))
				{
					activeChar.getInventory().unEquipItem(itemToAttr);
					itemToAttr.setAttributeElement(Element.getElementById(attrType), attrVal);
					itemToAttr.setJdbcState(JdbcEntityState.UPDATED);
					itemToAttr.update();
					
					activeChar.getInventory().equipItem(itemToAttr);
					activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToAttr));
					activeChar.broadcastUserInfo(true);
					
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "Attribute value on " + itemToAttr.getName() + " has been changed to " + attrVal + ".");
					Log.addDonation(new StringBuilder(activeChar.getName() + " enchant item:" + itemToAttr.getName() + " val: " + attrVal + " AtributType:" + attrType).toString(), "Enchanter");
					onBypassCommand(activeChar, "_bbsechant");
				}
				else
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "You need " + attrPrice + " " + ItemHolder.getInstance().getTemplateName(Config.COMMUNITYBOARD_ENCHANT_ITEM) + " to use this option.");
			}
			else if (itemToAttr.isArmor() && !itemToAttr.isUnderwear() && !itemToAttr.getTemplate().isBelt())
			{
				if (itemToAttr.getAttributeElementValue(Element.getReverseElement(Element.getElementById(attrType)), false) != 0)
				{
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "Cannot add this attribute to the item, opposite attribute is already present.");
					onBypassCommand(activeChar, "_bbsechant");
					return;
				}
				if (activeChar.getInventory().destroyItemByItemId(Config.COMMUNITYBOARD_ENCHANT_ITEM, attrPrice))
				{
					activeChar.getInventory().unEquipItem(itemToAttr);
					itemToAttr.setAttributeElement(Element.getElementById(attrType), attrVal);
					activeChar.getInventory().equipItem(itemToAttr);
					activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToAttr));
					activeChar.broadcastUserInfo(true);
					
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "Attribute value on " + itemToAttr.getName() + " has been changed to " + attrVal + ".");
					Log.addDonation(new StringBuilder(activeChar.getName() + " enchant item:" + itemToAttr.getName() + " val: " + attrVal + " AtributType:" + attrType).toString(), "Enchanter");
					onBypassCommand(activeChar, "_bbsechant");
				}
				else
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "You need " + attrPrice + " " + ItemHolder.getInstance().getTemplateName(Config.COMMUNITYBOARD_ENCHANT_ITEM) + " to use this option.");
			}
			else
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "Cannot attribute this item, conditions are not meet!");
		}
	}
	
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		// To change body of implemented methods use File | Settings | File Templates.
	}
	
	private static boolean CheckCondition(Player player)
	{
		if (!Config.COMMUNITYBOARD_ENCHANT_ENABLED || player == null)
			return false;
		
		if (player.isCursedWeaponEquipped() || player.isInJail() || NexusEvents.isInEvent(player) || player.isInDuel() || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped())
		{
			player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "You can not use the enchanter at this moment!");
			return false;
		}
		
		if (player.getReflectionId() != 0 || player.isInZone(ZoneType.epic))
		{
			player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "Can not be used in these areas!");
			return false;
		}
		
		// Custom
		if (!player.isInZone(ZoneType.peace_zone) && !player.isInZone(ZoneType.RESIDENCE))
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Enchanter", "You must be inside peace zone to use this function.");
			return false;
		}
				
		if (player.isInZone(ZoneType.SIEGE))
		{
			player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Enchanter", "Can not be used during the siege!");
			return false;
		}
		
		return true;
	}
	
}