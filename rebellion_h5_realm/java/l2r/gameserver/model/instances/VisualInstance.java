package l2r.gameserver.model.instances;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.network.serverpackets.ModelCharInfo;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.randoms.Visuals;
import l2r.gameserver.randoms.Visuals.VisualData;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.npc.NpcTemplate;

/**
 * 
 * @author Infern0
 * @Idea Midnex
 *
 */
public class VisualInstance extends NpcInstance
{
	public int _tcolor = Integer.decode(Config.VISUALS_TITLE_COLOR);
	public int _ncolor = Integer.decode(Config.VISUALS_NAME_COLOR);
	
	public int _face = -1;
	public int _hairStyle = -1;
	public int _hairColor = -1;

	public int _chest = 0;
	public int _pageup = 1;
	public int _totalpageup = Visuals.getInstance().getCountBySlot(0) / 4 + 1;

	public int _legs = 0;
	public int _pagelower = 1;
	public int _totalpagelower = Visuals.getInstance().getCountBySlot(1) / 4 + 1;

	public int _gloves = 0;
	public int _pagegloves = 1;
	public int _totalpagegloves = Visuals.getInstance().getCountBySlot(2) / 4 + 1;

	public int _boots = 0;
	public int _pageboots = 1;
	public int _totalpageboots = Visuals.getInstance().getCountBySlot(3) / 4 + 1;
	
	public int _accessory = 0;
	public int _pageaccessory = 1;
	public int _totalpageaccessory = Visuals.getInstance().getCountBySlot(4) / 4 + 1;
	
	public int _weapons = 0;
	public int _pageweapons = 1;
	public int _totalpageweapons = Visuals.getInstance().getCountBySlot(5) / 4 + 1;
	
	public int _shields = 0;
	public int _pageshields = 1;
	public int _totalpageshields = Visuals.getInstance().getCountBySlot(6) / 4 + 1;
	
	public int _cloaks = 0;
	public int _pagecloaks = 1;
	public int _totalpagecloaks = Visuals.getInstance().getCountBySlot(7) / 4 + 1;

	public VisualInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(player.getVar("jailed") != null)
		{
			player.sendMessageS("You cannot use this while in jail", 3);
			player.sendActionFailed();
			return;
		}
		
		super.onAction(player, shift);
	}

	public void setItems(int chest, int legs, int gloves, int boots, int accessorys, int weapons, int shields, int cloaks, int face, int haircolor, int hairstyle)
	{
		_chest = chest;
		_legs = legs;
		_gloves = gloves;
		_boots = boots;
		_accessory = accessorys;
		_weapons = weapons;
		_shields = shields;
		_cloaks = cloaks;
		_face = face;
		_hairColor = haircolor;
		_hairStyle = hairstyle;
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		String visualOwner = Visuals.getInstance().getAllSpawnedNPCs().get(getObjectId());
		if (visualOwner == null || visualOwner.isEmpty() || !player.getName().equalsIgnoreCase(visualOwner))
			return;
		
		if (!Config.ENABLE_VISUAL_SYSTEM)
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Visual transformation is disabled!");
			return;
		}
		
		showHtml(player, true);
	}
	
	private void showHtml(Player player, boolean first)
	{
		if (!Config.ENABLE_VISUAL_SYSTEM)
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Visual transformation is disabled!");
			return;
		}
		
		if (player == null)
			return;
		
		String htmlToSend = HtmCache.getInstance().getNotNull("mods/Visuals/index.htm", player);
		
		int page = 1;

		StringBuilder sb;
		
		String[][] boxes = { {"None", "Face A", "Face B", "Face C"}, { "None", "Hair A", "Hair B", "Hair C", "Hair D", "Hair F", "Hair G" }, { "None", "Color A", "Color B", "Color C", "Color D" } };
		
		for (int i = 0; i < 3; i++)
		{
			sb = new StringBuilder();
			String type = "";
			
			if (i == 0)
			{
				switch (_face)
				{
					case 0: type = "Face A"; break;
					case 1: type = "Face B"; break;
					case 2: type = "Face C"; break;
				}
			}
			else if (i == 1)
			{
				switch (_hairStyle)
				{
					case 0: type = "Hair A"; break;
					case 1: type = "Hair B"; break;
					case 2: type = "Hair C"; break;
					case 3: type = "Hair D"; break;
					case 4: type = "Hair F"; break;
					case 5: type = "Hair G"; break;
					case 6: type = "Hair H"; break;
				}
			}
			else if (i == 2)
			{
				switch (_hairColor)
				{
					case 0: type = "Color A"; break;
					case 1: type = "Color B"; break;
					case 2: type = "Color C"; break;
					case 3: type = "Color D"; break;
				}
			}
			
			String name = type.toString() + ";";
			sb.append(name);
			
			for (String s : boxes[i])
			{
				String str = s + ";";
				if (!str.toString().equalsIgnoreCase(name))
					sb.append(str);
			}
			
			htmlToSend = htmlToSend.replaceAll("%facebox" + i + "%", sb.toString());
		}
		
		// chest
		page = first && _chest != 0 ? Visuals.getInstance().getPageIn(0, _chest) : _pageup;
		htmlToSend = htmlToSend.replaceAll("%itemsUP%", getHtml(0, page, _chest));
		htmlToSend = htmlToSend.replaceAll("%costIdUP%", getCost(_chest) == null ? "Select Item" : ItemHolder.getInstance().getTemplate(getCost(_chest).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costAmountUP%", getCost(_chest) == null ? "&nbsp;" : "Cost: " + getCost(_chest).getCostAmount());

		// legs
		page = first && _legs != 0 ? Visuals.getInstance().getPageIn(1, _legs) : _pagelower;
		htmlToSend = htmlToSend.replaceAll("%itemsLOWER%", getHtml(1, page, _legs));
		htmlToSend = htmlToSend.replaceAll("%costIdLOWER%", getCost(_legs) == null ? "Select Item" : ItemHolder.getInstance().getTemplate(getCost(_legs).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costAmountLOWER%", getCost(_legs) == null ? "&nbsp;" : "Cost: " + getCost(_legs).getCostAmount());

		// gloves
		page = first && _gloves != 0 ? Visuals.getInstance().getPageIn(2, _gloves) : _pagegloves;
		htmlToSend = htmlToSend.replaceAll("%itemsGLOVES%", getHtml(2, page, _gloves));
		htmlToSend = htmlToSend.replaceAll("%costIdGLOVES%", getCost(_gloves) == null ? "Select Item" : ItemHolder.getInstance().getTemplate(getCost(_gloves).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costAmountGLOVES%", getCost(_gloves) == null ? "&nbsp;" : "Cost: " + getCost(_gloves).getCostAmount());

		// boots
		page = first && _boots != 0 ? Visuals.getInstance().getPageIn(3, _boots) : _pageboots;
		htmlToSend = htmlToSend.replaceAll("%itemsBOOTS%", getHtml(3, page, _boots));
		htmlToSend = htmlToSend.replaceAll("%costIdBOOTS%", getCost(_boots) == null ? "Select Item" : ItemHolder.getInstance().getTemplate(getCost(_boots).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costAmountBOOTS%", getCost(_boots) == null ? "&nbsp;" : "Cost: " + getCost(_boots).getCostAmount());
		
		// accessory
		page = first && _accessory != 0 ? Visuals.getInstance().getPageIn(4, _accessory) : _pageaccessory;
		htmlToSend = htmlToSend.replaceAll("%itemsACCESSORY%", getHtml(4, page, _accessory));
		htmlToSend = htmlToSend.replaceAll("%costIdACCESSORY%", getCost(_accessory) == null ? "Select Item" : ItemHolder.getInstance().getTemplate(getCost(_accessory).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costAmountACCESSORY%", getCost(_accessory) == null ? "&nbsp;" : "Cost: " + getCost(_accessory).getCostAmount());

		// weapons
		page = first && _weapons != 0 ? Visuals.getInstance().getPageIn(5, _weapons) : _pageweapons;
		htmlToSend = htmlToSend.replaceAll("%itemsWEAPONS", getHtml(5, page, _weapons));
		htmlToSend = htmlToSend.replaceAll("%costIdWEAPONS%", getCost(_weapons) == null ? "Select Item" : ItemHolder.getInstance().getTemplate(getCost(_weapons).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costAmountWEAPONS%", getCost(_weapons) == null ? "&nbsp;" : "Cost: " + getCost(_weapons).getCostAmount());
		
		// shields
		page = first && _shields != 0 ? Visuals.getInstance().getPageIn(6, _shields) : _pageshields;
		htmlToSend = htmlToSend.replaceAll("%itemsSHIELDS%", getHtml(6, page, _shields));
		htmlToSend = htmlToSend.replaceAll("%costIdSHIELDS%", getCost(_shields) == null ? "Select Item" : ItemHolder.getInstance().getTemplate(getCost(_shields).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costAmountSHIELDS%", getCost(_shields) == null ? "&nbsp;" : "Cost: " + getCost(_shields).getCostAmount());
		
		// cloaks
		page = first && _cloaks != 0 ? Visuals.getInstance().getPageIn(7, _cloaks) : _pagecloaks;
		htmlToSend = htmlToSend.replaceAll("%itemsCLOAKS%", getHtml(7, page, _cloaks));
		htmlToSend = htmlToSend.replaceAll("%costIdCLOAKS%", getCost(_cloaks) == null ? "Select Item" : ItemHolder.getInstance().getTemplate(getCost(_cloaks).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costAmountCLOAKS%", getCost(_cloaks) == null ? "&nbsp;" : "Cost: " + getCost(_cloaks).getCostAmount());
		
		// face, hair, color
		htmlToSend = htmlToSend.replaceAll("%costFace%", getCost(_face >= 0 ? 80001 : -1) == null ? "&nbsp;" : "Cost: " + getCost(80001).getCostAmount() + " " + ItemHolder.getInstance().getTemplate(getCost(80001).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costHair%", getCost(_hairStyle >= 0 ? 80002 : -1) == null ? "&nbsp;" : "Cost: " + getCost(80002).getCostAmount() + " " + ItemHolder.getInstance().getTemplate(getCost(80002).getCostId()).getName());
		htmlToSend = htmlToSend.replaceAll("%costColor%", getCost(_hairColor >= 0 ? 80003 : -1) == null ? "&nbsp;" : "Cost: " + getCost(80003).getCostAmount() + " " + ItemHolder.getInstance().getTemplate(getCost(80003).getCostId()).getName());
				
		if (player.getVar("visualItems") != null)
			htmlToSend = htmlToSend.replaceAll("%button%", "<td width=\"32\" valign=\"top\"><button value=\"\" action=\"bypass -h npc_%objectId%_removeVisual\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\"></td>");
		else
			htmlToSend = htmlToSend.replaceAll("%button%", "<td width=\"32\" valign=\"top\"><button value=\"\" action=\"bypass -h npc_%objectId%_putVisual\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\"></td>");
		
		if (Visuals.getInstance().getAllSpawnedNPCs().containsValue(player.getName()))
			htmlToSend = htmlToSend.replaceAll("%buttonVisual%", "<td width=\"32\" valign=\"top\"><button value=\"\" action=\"bypass _bbsVisual\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Blue\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Blue\"></td>");
		else
			htmlToSend = htmlToSend.replaceAll("%buttonVisual%", "<td width=\"32\" valign=\"top\"><button value=\"\" action=\"bypass _bbsVisual\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\"></td>");
		
		htmlToSend = htmlToSend.replaceAll("%objectId%", "" + getObjectId());
		
		ShowBoard.separateAndSend(htmlToSend, player);
	}

	private VisualData getCost(int itemId)
	{
		return Visuals.getInstance().getavailableItems().get(itemId);
	}
	
	private String getHtml(int slotId, int page, int itemId)
	{
		String itemToReplace = "";

		int i = 0;

		for(Integer item : Visuals.getInstance().getVisual(slotId, page * 4 - 4, page * 4))
		{
			i++;
			String itemOne = HtmCache.getInstance().getNotNull("mods/Visuals/template.htm", null);
			String temp = "";
			temp = itemOne;

			String icon = ItemHolder.getInstance().getTemplate(item).getIcon();
			
			if(item == itemId)
			{
				temp = temp.replaceAll("%selb%", "L2_SkillTime.ToggleEffect001");
				temp = temp.replaceAll("%sel%", "L2_SkillTime.ToggleEffect001");
				
			}
			else
			{
				temp = temp.replaceAll("%selb%", "L2_SkillTime.menu_outline_Down");
				temp = temp.replaceAll("%sel%", "L2UI_CH3.menu_outline");
			}
			
			temp = temp.replaceAll("%type%", "" + slotId);
			temp = temp.replaceAll("%id%", "" + item);
			temp = temp.replaceAll("%icon%", icon);
			temp = temp.replaceAll("%objectId%", "" + getObjectId());
			itemToReplace += temp;
		}

		if(i < 4)
			for(int a = 4 - i; a != 0; a--)
			{
				String itemOne = HtmCache.getInstance().getNotNull("mods/Visuals/template.htm", null);
				String temp = "";
				temp = itemOne;
				temp = temp.replaceAll("%sel%", "");
				temp = temp.replaceAll("%type%", "none");
				temp = temp.replaceAll("%id%", "0");
				temp = temp.replaceAll("%icon%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				temp = temp.replaceAll("%objectId%", "" + getObjectId());
				itemToReplace += temp;
			}
		return itemToReplace;
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;
		
		if (!Config.ENABLE_VISUAL_SYSTEM)
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Visual transformation is disabled!");
			return;
		}
		
		if(command.equals("forw0"))
		{
			if(_totalpageup == _pageup)
				return;

			_pageup++;
			showHtml(player, false);
		}
		else if(command.equals("back0"))
		{
			if(_pageup == 1)
				return;

			_pageup--;
			showHtml(player, false);
		}
		else if(command.equals("forw1"))
		{
			if(_totalpagelower == _pagelower)
				return;

			_pagelower++;
			showHtml(player, false);
		}
		else if(command.equals("back1"))
		{
			if(_pagelower == 1)
				return;

			_pagelower--;
			showHtml(player, false);
		}

		else if(command.equals("forw2"))
		{
			if(_totalpagegloves == _pagegloves)
				return;

			_pagegloves++;
			showHtml(player, false);
		}
		else if(command.equals("back2"))
		{
			if(_pagegloves == 1)
				return;

			_pagegloves--;
			showHtml(player, false);
		}
		else if(command.equals("forw3"))
		{
			if(_totalpageboots == _pageboots)
				return;

			_pageboots++;
			showHtml(player, false);
		}
		else if(command.equals("back3"))
		{
			if(_pageboots == 1)
				return;

			_pageboots--;
			showHtml(player, false);
		}
		else if(command.equals("forw4"))
		{
			if(_totalpageaccessory == _pageaccessory)
				return;

			_pageaccessory++;
			showHtml(player, false);
		}
		else if(command.equals("back4"))
		{
			if(_pageaccessory == 1)
				return;

			_pageaccessory--;
			showHtml(player, false);
		}
		else if(command.equals("forw5"))
		{
			if(_totalpageweapons == _pageweapons)
				return;

			_pageweapons++;
			showHtml(player, false);
		}
		else if(command.equals("back5"))
		{
			if(_pageweapons == 1)
				return;

			_pageweapons--;
			showHtml(player, false);
		}
		else if(command.equals("forw6"))
		{
			if(_totalpageshields == _pageshields)
				return;

			_pageshields++;
			showHtml(player, false);
		}
		else if(command.equals("back6"))
		{
			if(_pageshields == 1)
				return;

			_pageshields--;
			showHtml(player, false);
		}
		else if(command.equals("forw7"))
		{
			if(_totalpagecloaks == _pagecloaks)
				return;

			_pagecloaks++;
			showHtml(player, false);
		}
		else if(command.equals("back7"))
		{
			if(_pagecloaks == 1)
				return;

			_pagecloaks--;
			showHtml(player, false);
		}

		else if(command.startsWith("0"))
		{
			String[] id = command.split(" ");

			int tempitem = Integer.parseInt(id[1]);
			if(Visuals.getInstance().isAvailable(0, tempitem))
				if(_chest == tempitem)
					_chest = 0;
				else
					_chest = tempitem;

			player.sendPacket(new ModelCharInfo(this, player));

			showHtml(player, false);
		}

		else if(command.startsWith("1"))
		{
			String[] id = command.split(" ");

			int tempitem = Integer.parseInt(id[1]);
			if(Visuals.getInstance().isAvailable(1, tempitem))
				if(_legs == tempitem)
					_legs = 0;
				else
					_legs = tempitem;

			player.sendPacket(new ModelCharInfo(this, player));

			showHtml(player, false);
		}
		else if(command.startsWith("2"))
		{
			String[] id = command.split(" ");

			int tempitem = Integer.parseInt(id[1]);
			if(Visuals.getInstance().isAvailable(2, tempitem))
				if(_gloves == tempitem)
					_gloves = 0;
				else
					_gloves = tempitem;

			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if(command.startsWith("3"))
		{
			String[] id = command.split(" ");

			int tempitem = Integer.parseInt(id[1]);
			if(Visuals.getInstance().isAvailable(3, tempitem))
				if(_boots == tempitem)
					_boots = 0;
				else
					_boots = tempitem;

			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if(command.startsWith("4"))
		{
			String[] id = command.split(" ");

			int tempitem = Integer.parseInt(id[1]);
			if(Visuals.getInstance().isAvailable(4, tempitem))
				if(_accessory == tempitem)
					_accessory = 0;
				else
					_accessory = tempitem;

			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if(command.startsWith("5"))
		{
			String[] id = command.split(" ");

			int tempitem = Integer.parseInt(id[1]);
			
			ItemTemplate itm = ItemHolder.getInstance().getTemplate(tempitem);
			if (itm != null)
			{
				if (itm.getBodyPart() == ItemTemplate.SLOT_LR_HAND && _shields != 0)
				{
					player.sendMessageS("You cannot select two-hand weapon while having shield.", 2);
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You cannot select two-hand weapon while having shield.");
					return;
				}
			}
			
			if(Visuals.getInstance().isAvailable(5, tempitem))
				if(_weapons == tempitem)
					_weapons = 0;
				else
					_weapons = tempitem;

			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if(command.startsWith("6"))
		{
			String[] id = command.split(" ");

			int tempitem = Integer.parseInt(id[1]);
			
			if (_weapons > 0)
			{
				ItemTemplate itm = ItemHolder.getInstance().getTemplate(_weapons);
				if (itm != null)
				{
					if (itm.getBodyPart() == ItemTemplate.SLOT_LR_HAND)
					{
						player.sendMessageS("You cannot select shield while having two-hand weapon.", 2);
						player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You cannot select shield while having two-hand weapon.");
						return;
					}
				}
			}
			
			if(Visuals.getInstance().isAvailable(6, tempitem))
				if(_shields == tempitem)
					_shields = 0;
				else
					_shields = tempitem;

			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if(command.startsWith("7"))
		{
			String[] id = command.split(" ");

			int tempitem = Integer.parseInt(id[1]);
			if(Visuals.getInstance().isAvailable(7, tempitem))
				if(_cloaks == tempitem)
					_cloaks = 0;
				else
					_cloaks = tempitem;

			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		
		else if(command.startsWith("face"))
		{
			String[] type = command.split(" ");

			if (type[1].equals("None"))
			{
				_face = -1;
				player.sendPacket(new ModelCharInfo(this, player));
				showHtml(player, false);
				return;
			}
			
			switch (type[2])
			{
				case "face":
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Please select Face Type.");
					player.sendMessageS("Please select Face type.", 3);
					return;
				}
				case "A":
					_face = 0;
					break;
				case "B":
					_face = 1;
					break;
				case "C":
					_face = 2;
					break;
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if(command.startsWith("hair"))
		{
			String[] type = command.split(" ");

			if (type[1].equals("None"))
			{
				_hairStyle = -1;
				player.sendPacket(new ModelCharInfo(this, player));
				showHtml(player, false);
				return;
			}
			
			switch (type[2])
			{
				case "hair":
				{
					player.sendMessageS("Please select Hair style.", 3);
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Please select Hair Style.");
					return;
				}
				case "A":
					_hairStyle = 0;
					break;
				case "B":
					_hairStyle = 1;
					break;
				case "C":
					_hairStyle = 2;
					break;
				case "D":
					_hairStyle = 3;
					break;
				case "F":
					_hairStyle = 4;
					break;
				case "G":
					_hairStyle = 5;
					break;
				case "H":
					_hairStyle = 6;
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if(command.startsWith("color"))
		{
			String[] type = command.split(" ");

			if (type[1].equals("None"))
			{
				_hairColor = -1;
				player.sendPacket(new ModelCharInfo(this, player));
				showHtml(player, false);
				return;
			}
			
			switch (type[2])
			{
				case "color":
				{
					player.sendMessageS("Please select Hair Color.", 3);
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Please select Hair Color.");
					return;
				}
				case "A":
					_hairColor = 0;
					break;
				case "B":
					_hairColor = 1;
					break;
				case "C":
					_hairColor = 2;
					break;
				case "D":
					_hairColor = 3;
					break;
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		
		else if(command.equals("putVisual"))
		{
			// Conditions
			if (!player.isGM())
			{
				// is transformed etc..
				if (player.isInJail() || player.isCursedWeaponEquipped() || NexusEvents.isInEvent(player) || player.getReflectionId() != ReflectionManager.DEFAULT_ID/* || player.getPvpFlag() != 0*/ || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped() || player.isInZone(ZoneType.no_escape) || player.isInZone(ZoneType.SIEGE) || player.isInZone(ZoneType.epic))
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Cannot polymorph due conditions!");
					return;
				}
				
				if (!player.isInZone(ZoneType.peace_zone) && !player.isInZone(ZoneType.RESIDENCE))
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You must be inside peace zone to polymorph!");
					return;
				}
				
				if(player.isTerritoryFlagEquipped() || player.isCombatFlagEquipped())
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You cannot polymorph while holding territory flag!");
					return;
				}
			}
			
			int[] items = { _chest, _legs, _gloves, _boots, _accessory, _weapons, _shields, _cloaks, _face >= 0 ? 80001 : -1, _hairStyle >= 0 ? 80002 : -1, _hairColor >= 0 ? 80003 : -1 };
			
			boolean canDestroy = false;
			for (int itemId : items)
			{
				if (getCost(itemId) == null)
					continue;
				
				int item = getCost(itemId).getCostId();
				long count = getCost(itemId).getCostAmount();
				
				if (player.getInventory().getCountOf(item) > count)
					canDestroy = true;
				
				if (!canDestroy)
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You need " + count + " " + ItemHolder.getInstance().getTemplateName(item) + " to finish the visual change!");
					break;
				}
			}
			
			if (!canDestroy)
				return;
			
			for (int itemId : items)
			{
				if (getCost(itemId) == null)
					continue;
				
				int item = getCost(itemId).getCostId();
				long count = getCost(itemId).getCostAmount();
				
				Functions.removeItem(player, item, count);
			}
			
			int[] visualItems = { _chest, _legs, _gloves, _boots, _accessory, _weapons, _shields, _cloaks, _face, _hairStyle, _hairColor };
			player.setVisualItems(visualItems);
			player.setVar("visualItems", _chest + ";" + _legs + ";" + _gloves + ";" + _boots + ";" + _accessory + ";" + _weapons + ";" + _shields + ";" + _cloaks + ";" + _face + ";" + _hairColor + ";" + _hairStyle);
			player.sendMessageS("You have been polymorphed!", 4);
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "You have been polymorphed!");
			player.broadcastCharInfo();
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new UserInfo(player));

			_tcolor = Integer.decode("0xD4F212");
			_ncolor = Integer.decode("0xD4F212");
			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if(command.equals("removeVisual"))
		{
			player.setVisualItems(null);
			player.sendMessageS("Your visual template has been reseted!", 4);
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Visual Template has been reseted!");
			player.broadcastCharInfo();
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new UserInfo(player));
			player.unsetVar("visualItems");
			
			_tcolor = Integer.decode("0xa4e598");
			_ncolor = Integer.decode("0xFFFFFF");
			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else
			super.onBypassFeedback(player, command);
	}

	public String getSaveString()
	{
		return _chest + "," + _legs + "," + _gloves + "," + _boots + "," + _accessory + "," + _weapons + "," + _shields + ";" + _cloaks + ";";
	}
}
