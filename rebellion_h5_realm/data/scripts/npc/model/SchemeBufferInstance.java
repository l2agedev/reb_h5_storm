package npc.model;

import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.SchemeBufferManager;
import l2r.gameserver.instancemanager.SchemeBufferManager.PlayerBuffProfile;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.List;

import javolution.text.TextBuilder;

public class SchemeBufferInstance extends NpcInstance
{
	//private static final Logger _log = LoggerFactory.getLogger(SchemeBufferInstance.class);
	
	public SchemeBufferInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public boolean checkCondition(Player player)
	{
		if (player == null || !player.isConnected() || player.isInOfflineMode())
			return false;
		
		if (player.isCursedWeaponEquipped() || !player.isInPeaceZone()  || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode()  || Olympiad.isRegistered(player) || player.isFlying() || player.isTerritoryFlagEquipped())
			return false;
		
		return true;
	}
	
	@Override
	public void onBypassFeedback(Player activeChar, String bypass)
	{
		if(!canBypassCheck(activeChar, this))
			return;
		
		if (!checkCondition(activeChar))
		{
			activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.cannot_use", activeChar));
			return;
		}
		
		String[] subCommands = bypass.split("_");
		
		if (bypass.startsWith("showMainWindow"))
			showMainWindow(activeChar);
		else if (bypass.startsWith("showCreateProfileWindow"))
		{
			if (activeChar.getProfiles().size() >= Config.MAX_SCHEME_PROFILES)
			{
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.cannot_create_more", activeChar));
				return;
			}
			
			showCreateProfileWindow(activeChar);
		}
		else if (bypass.startsWith("createProfile"))
		{
			if (subCommands.length == 1 || subCommands[0].isEmpty() || subCommands[1].isEmpty())
				return;
			
			if (activeChar.getProfiles().size() >= Config.MAX_SCHEME_PROFILES)
			{
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.cannot_create_more", activeChar));
				return;
			}
			else if (activeChar.getProfile(subCommands[1]) != null) // subCommands[1] = profileName
			{
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.proffilename_already_exist", activeChar));
				return;
			}
			else
			{
				String profileName = subCommands[1];
				while (profileName.startsWith(" "))
					profileName = profileName.replaceFirst(" ", "");
				
				if (!profileName.matches(Config.CNAME_TEMPLATE))
				{
					activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.invalid_symbols", activeChar));
					return;
				}
				// saveProfile(activeChar, subCommands[1]); // subCommands[1] = profileName
				activeChar.addProfile(SchemeBufferManager.getInstance().new PlayerBuffProfile(profileName)); // subCommands[1] = profileName
				showBuffsToAddPage(activeChar, profileName, 1);
			}
		}
		else if (bypass.startsWith("showDeleteProfileWindow"))
			showDeleteProfileWindow(activeChar);
		else if (bypass.startsWith("deleteProfile"))
		{
			
			if (subCommands[1].isEmpty())
				return;
			
			SchemeBufferManager.getInstance().deleteFromDatabase(activeChar, activeChar.getProfile(subCommands[1].trim()));
			showDeleteProfileWindow(activeChar);
		}
		else if (bypass.startsWith("showMyProfilesToEdit"))
			showProfilesToEditWindow(activeChar);
		else if (bypass.startsWith("editProfile"))
		{
			
			if (subCommands[1].isEmpty())
				return;
			
			showProfilePageToEdit(activeChar, subCommands[1], 1);
		}
		else if (bypass.startsWith("deleteBuff"))
		{
			
			if (subCommands[1].isEmpty() || subCommands[2].isEmpty() || subCommands[3].isEmpty())
				return;
			
			activeChar.getProfile(subCommands[1]).removeBuff(subCommands[2]); // subCommands[1] = profileName, subCommands[2] = buffName
			showProfilePageToEdit(activeChar, subCommands[1], Integer.parseInt(subCommands[3]));
		}
		else if (bypass.startsWith("showPageToAddBuffs"))
			showPageToAddBuffs(activeChar);
		else if (bypass.startsWith("showBuffsToAddPage"))
		{
			if (subCommands[1].isEmpty() || subCommands[2].isEmpty())
				return;
			
			showBuffsToAddPage(activeChar, subCommands[1], Integer.valueOf(subCommands[2]));
			// showBuffsToAddRemovePage(activeChar, subCommands[1], Integer.valueOf(subCommands[2]));
		}
		else if (bypass.startsWith("addBuff"))
		{
			
			// Since theres buffs whose name contains " ".To make it cleary: [command + space + subcommand + space]
			
			if (subCommands[1].isEmpty() || subCommands[2].isEmpty() || subCommands[3].isEmpty())
				return;
			
			// Nice hack to support buff chose option
			if ("chosebuffs".equals(subCommands[1]))
			{
				SkillTable.getInstance().getInfo(SchemeBufferManager._buffs.get(subCommands[2])[0], SchemeBufferManager._buffs.get(subCommands[2])[1]).getEffects(activeChar, activeChar, false, false);
				showBuffsToAddPage(activeChar, subCommands[1], Integer.valueOf(subCommands[3]));
				return;
			}
			
			if (activeChar.getProfile(subCommands[1]).buffs.size() >= Config.MAX_BUFFS_PER_PROFILE) // subCommands[1] = profileName
			{
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.cannotaddmorebuffs", activeChar));
				return;
			}
			
			activeChar.getProfile(subCommands[1]).addBuff(subCommands[2]); // subCommands[1] = profileName, subCommands[2] = buffName
			showBuffsToAddPage(activeChar, subCommands[1], Integer.valueOf(subCommands[3]));
		}
		else if (bypass.startsWith("showProfilesToBuff"))
			showProfilesToBuff(activeChar);
		else if (bypass.startsWith("getBuffs"))
		{
			
			if (subCommands[1].isEmpty())
				return;
			
			if (activeChar.getInventory().getItemByItemId(57) == null)
			{
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.notenoughadena", activeChar));
				return;
			}
			
			int priceToPay = Config.PRICE_PER_BUFF * activeChar.getProfile(subCommands[1].trim()).buffs.size(); // subCommands[1] = profileName
			if (activeChar.getInventory().getItemByItemId(57).getCount() < priceToPay)
			{
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.notenoughadena", activeChar));
				return;
			}
			
			activeChar.reduceAdena(priceToPay, true);
			
			for (String st : activeChar.getProfile(subCommands[1]).buffs) // subCommands[1] = profileName
				SkillTable.getInstance().getInfo(SchemeBufferManager._buffs.get(st)[0], SchemeBufferManager._buffs.get(st)[1]).getEffects(activeChar, activeChar, false, false);
			
			showMainWindow(activeChar);
		}
		else if (bypass.startsWith("buffFighter"))
		{
			buffFighter(activeChar);
			return;
		}
		else if (bypass.startsWith("buffMage"))
		{
			buffMage(activeChar);
			return;
		}
		else if (bypass.startsWith("getPetBuffs"))
		{
			
			if (subCommands[1].isEmpty())
				return;
			
			if (activeChar.getPet() == null)
			{
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.summon_not_found", activeChar));
				return;
			}
			if (activeChar.getInventory().getItemByItemId(57) == null)
			{
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.notenoughadena1", activeChar));
				return;
			}
			
			int priceToPay = Config.PRICE_PER_BUFF * activeChar.getProfile(subCommands[1]).buffs.size(); // subCommands[1] = profileName
			
			if (activeChar.getInventory().getItemByItemId(57).getCount() < priceToPay)
			{
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.notenoughadena1", activeChar));
				return;
			}
			
			
			activeChar.reduceAdena(priceToPay, true);
			
			// subCommands[1] = profileName
			for (String st : activeChar.getProfile(subCommands[1]).buffs)		
				SkillTable.getInstance().getInfo(SchemeBufferManager._buffs.get(st)[0], SchemeBufferManager._buffs.get(st)[1]).getEffects(activeChar.getPet(), activeChar.getPet(), false, false);
			
			showMainWindow(activeChar);
		}
		else if (bypass.startsWith("heal"))
		{
			NpcHtmlMessage text = new NpcHtmlMessage(activeChar, this);
			text = text.setFile("scripts/services/SchemeBuffer/main.htm");
			//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/main.htm", activeChar);
			
			activeChar.setCurrentCp(activeChar.getMaxCp());
			activeChar.setCurrentHp(activeChar.getMaxHp(), true);
			activeChar.setCurrentMp(activeChar.getMaxMp());
			if (activeChar.getPet() != null)
			{
				activeChar.getPet().setCurrentCp(activeChar.getPet().getMaxCp());
				activeChar.getPet().setCurrentHp(activeChar.getPet().getMaxHp(), true);
				activeChar.getPet().setCurrentMp(activeChar.getPet().getMaxMp());
			}
			
			activeChar.sendPacket(text);
		}
		else if (bypass.startsWith("cancel"))
		{
			NpcHtmlMessage text = new NpcHtmlMessage(activeChar, this);
			text = text.setFile("scripts/services/SchemeBuffer/main.htm");
			//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/main.htm", activeChar);
			
			if (activeChar.getEffectList().getEffectsBySkillId(Skill.SKILL_RAID_CURSE) == null)
				activeChar.getEffectList().stopAllEffects();
			
			activeChar.sendPacket(text);
		}
		else if (bypass.startsWith("noblesse"))
		{
			NpcHtmlMessage text = new NpcHtmlMessage(activeChar, this);
			text = text.setFile("scripts/services/SchemeBuffer/main.htm");
			//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/main.htm", activeChar);
			SkillTable.getInstance().getInfo(1323, 1).getEffects(activeChar, activeChar, false, false);
			
			activeChar.sendPacket(text);
		}
		else
			super.onBypassFeedback(activeChar, bypass);
		/*
		else if (bypass.startsWith("ultimatepetbuff"))
		{
			if (activeChar.getPet() == null)
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.summon_not_found", activeChar));
			else if (activeChar.getActiveClassId() == 14 || activeChar.getActiveClassId() == 28 || activeChar.getActiveClassId() == 41 || activeChar.getActiveClassId() == 57 || activeChar.getActiveClassId() == 96 || activeChar.getActiveClassId() == 104 || activeChar.getActiveClassId() == 111 || activeChar.getActiveClassId() == 118)
			{
				SkillTable.getInstance().getInfo(5503, 1).getEffects(activeChar.getPet(), activeChar.getPet(), false, false);
			}
			else
				activeChar.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.notenoughadena1", activeChar));
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "Sorry, this buff is only avaiable for summoner classes and maestros.");
		}
		*/	
	}
	
	/*
	 * Shows the main window to the player
	 */
	public void showMainWindow(Player player)
	{
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/main.htm", player);
		text = text.setFile("scripts/services/SchemeBuffer/main.htm");
		player.sendPacket(text);
	}
	
	/*
	 * Shows the create profile window
	 */
	private void showCreateProfileWindow(Player player)
	{
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/createscheme.htm", player);
		
		text = text.setFile("scripts/services/SchemeBuffer/createscheme.htm");
		player.sendPacket(text);
	}
	
	/*
	 * Shows a window with all player profiles, to choose one to delete
	 */
	private void showDeleteProfileWindow(Player player)
	{
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/deletescheme.htm", player);
		
		TextBuilder tb = new TextBuilder();
		for (String st : player.getProfiles().keySet())
		{
			tb.append(st);
			tb.append("<button value=\"\" action=\"bypass -h npc_%objectId%_deleteProfile_" + st + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Over\" fore=\"L2UI_ct1.MiniMap_DF_MinusBtn_Red\">");
		}
		
		if (tb.length() == 0) // No schemes
			tb.append("No Scheme to delete");
		
		text = text.setFile("scripts/services/SchemeBuffer/deletescheme.htm");
		text = text.replace("%profilesToDelete%", tb.toString());
		
		player.sendPacket(text);
	}
	
	/*
	 * Show a window with all player profiles. They can be edited from here
	 */
	private void showProfilesToEditWindow(Player player)
	{
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		
		//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/profiletoedit.htm", player);
		
		TextBuilder tb = new TextBuilder();
		boolean hasSchemes = false;
		for (String st : player.getProfiles().keySet())
		{
			tb.append("<button value=\"" + st + "\" action=\"bypass -h npc_%objectId%_editProfile_" + st + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
			hasSchemes = true;
		}
		if (!hasSchemes)
			tb.append("<button value=\"New Scheme\" action=\"bypass -h npc_%objectId%_showCreateProfileWindow\" width=140 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		
		text = text.setFile("scripts/services/SchemeBuffer/profiletoedit.htm");
		text = text.replace("%profilesToEdit%", tb.toString());
		
		player.sendPacket(text);
	}
	
	/*
	 * Shows a window with all buffs contains into a single profile
	 */
	private void showProfilePageToEdit(Player player, String profileName, int page)
	{
		//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/buffsedit.htm", player);
		
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		
		TextBuilder tb = new TextBuilder();
		
		text = text.replace("%profileName%", profileName);
		PlayerBuffProfile profile = player.getProfile(profileName);
		if (profile == null)
		{
			player.sendMessage(new CustomMessage("scripts.npc.model.schemebufferinstance.nosuchproffile", player));
			return;
		}
		
		List<String> tempBuff = profile.buffs;
		int toIndex = page * 16;
		if (toIndex > tempBuff.size())
			toIndex = tempBuff.size();
		
		for (String name : tempBuff.size() <= 16 ? tempBuff : tempBuff.subList(Math.max(toIndex - 16, 0), toIndex))
		{
			int buffId = SchemeBufferManager._buffs.get(name)[0];
			
				tb.append("<tr>");
			
			if (buffId < 1000)
				tb.append("<td><img src=\"icon.skill0" + buffId + "\" width=32 height=32></td>");
			else
			{
				if (buffId == 4699 || buffId == 4700)
					tb.append("<td><img src=\"icon.skill1331\" width=32 height=32></td>");
				else if (buffId == 4702 || buffId == 4703)
					tb.append("<td><img src=\"icon.skill1332\" width=32 height=32></td>");
				else
					tb.append("<td><img src=\"icon.skill" + buffId + "\" width=32 height=32></td>");
			}
				
			tb.append("<td> " + name + "</td>");
			tb.append("<td><button value=\"\" action=\"bypass -h npc_%objectId%_deleteBuff_" + profileName + "_" + name + "_" + page + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>");

			
			tb.append("<td width=70></td>");
			
			tb.append("</tr>");
		}
		
		text = text.replace("%buffs%", tb.toString());
		tb.clear();
		
		// +2, +1 to complete the number (X.4 would be 3), and +1 to reach the max
		for (int i = 1; i < Math.round(tempBuff.size() / 8) + 2; i++)
		{
			if (i == page) // Current page, no bypass for it.
				tb.append("<td><button value=\"" + i + "\" width=20 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df_calculator\"></td>");
			else
				tb.append("<td><button value=\"" + i + "\" action=\"bypass -h npc_%objectId%_deleteBuff_" + profileName + "_null_" + i + "\" width=20 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		text = text.setFile("scripts/services/SchemeBuffer/buffsedit.htm");
		text = text.replace("%pages%", tb.toString());
		
		player.sendPacket(text);
	}
	
	/*
	 * Shows a window with the current profiles to add buffs
	 */
	private void showPageToAddBuffs(Player player)
	{
		//String text = "scripts/services/SchemeBuffer/profilebuff.htm";
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		
		TextBuilder tb = new TextBuilder();
		
		for (String st : player.getProfiles().keySet())
		{
			tb.append("<button value=\"" + st + "\" action=\"bypass -h npc_%objectId%_showBuffsToAddPage_" + st + "_1\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		}
		
		if (tb.length() == 0) // No schemes
			tb.append("<button value=\"New Scheme\" action=\"bypass -h npc_%objectId%_showCreateProfileWindow\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		
		text = text.setFile("scripts/services/SchemeBuffer/profilebuff.htm");
		text = text.replace("%profilebuff%", tb.toString());
		
		player.sendPacket(text);
	}
	
	/*
	 * Show all avaliable buffs to add to a profile
	 */
	private void showBuffsToAddPage(Player player, String profileName, int page)
	{
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		
		//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/showbufftoadd.htm", player);
		
		List<String> tempBuff = new ArrayList<>();
		tempBuff.addAll(SchemeBufferManager._buffs.keySet());
		TextBuilder tb = new TextBuilder();
		
		int toIndex = page * 16;
		if (toIndex > tempBuff.size())
			toIndex = tempBuff.size();
		
		boolean isPageEmpty = true;
		for (String st : tempBuff.subList((page * 16) - 16, toIndex))
		{
			int buffId = SchemeBufferManager._buffs.get(st)[0];
			
			// The player already has this buff, so dont show it.
			if (player.getProfile(profileName) != null && player.getProfile(profileName).buffs.contains(st))
				continue;
			
			isPageEmpty = false;
			
			tb.append("<tr>");
			
			if (buffId < 1000)
				tb.append("<td><img src=\"icon.skill0" + buffId + "\" width=32 height=32></td>");
			else
			{
				if (buffId == 4699 || buffId == 4700)
					tb.append("<td><img src=\"icon.skill1331\" width=32 height=32></td>");
				else if (buffId == 4702 || buffId == 4703)
					tb.append("<td><img src=\"icon.skill1332\" width=32 height=32></td>");
				else
					tb.append("<td><img src=\"icon.skill" + buffId + "\" width=32 height=32></td>");
			}
				
			
			tb.append("<td> " + st + "</td>");
			tb.append("<td><button value=\"\" action=\"bypass -h npc_%objectId%_addBuff_" + profileName + "_" + st + "_" + page + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>");
			
			
			tb.append("</tr>");
			
		}
		
		if (isPageEmpty)
			tb.append("<tr><td>No more buffs at this page.</td></tr>");
		
		text = text.replace("%buffs%", tb.toString());
		tb.clear();
		
		// +2, +1 to complete the number (X.4 would be 3), and +1 to reach the max
		for (int i = 1; i < SchemeBufferManager._pages + 2; i++)
		{
			if (i == page) // Current page, no bypass for it.
				tb.append("<td><button value=\"" + i + "\" width=20 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df_calculator\"></td>");
			else
				tb.append("<td><button value=\"" + i + "\" action=\"bypass -h npc_%objectId%_showBuffsToAddPage_" + profileName + "_" + i + "\" width=20 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		text = text.setFile("scripts/services/SchemeBuffer/showbufftoadd.htm");
		text = text.replace("%pages%", tb.toString());
		text = text.replace("%profileName%", profileName);
		
		player.sendPacket(text);
	}
	
	/*
	 * Show all profiles to buff the player
	 */
	private void showProfilesToBuff(Player player)
	{
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/profiles.htm", player);
		
		TextBuilder tb = new TextBuilder();
		
		boolean hasSchemes = false;
		for (String st : player.getProfiles().keySet())
		{
			tb.append("<tr>");
			tb.append("<td><button value=\"" + st + "\" action=\"bypass -h npc_%objectId%_getBuffs_" + st + "\" width=120 height=24 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			tb.append("<td><button value=\"" + st + "\" action=\"bypass -h npc_%objectId%_getPetBuffs_" + st + "\" width=120 height=24 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			tb.append("</tr>");
			hasSchemes = true;
		}
		if (!hasSchemes)
			tb.append("<tr><td width=140><button value=\"Create New\" action=\"bypass -h npc_%objectId%_showCreateProfileWindow\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td><td width=140><button value=\"Create New\" action=\"bypass -h npc_%objectId%_showCreateProfileWindow\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		
		text = text.setFile("scripts/services/SchemeBuffer/profiles.htm");
		text = text.replace("%profiles%", tb.toString());
		
		player.sendPacket(text);
	}
	
	@SuppressWarnings("unused")
	private void showBuffsToAddRemovePage(Player player, String profileName, int page)
	{
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		//String text = HtmCache.getInstance().getNotNull("scripts/services/SchemeBuffer/addbuffstoscheme.htm", player);
		
		List<String> tempBuff = new ArrayList<>();
		tempBuff.addAll(SchemeBufferManager._buffs.keySet());
		TextBuilder tb = new TextBuilder();
		
		int toIndex = page * 16;
		if (toIndex > tempBuff.size())
			toIndex = tempBuff.size();
		
		for (String st : tempBuff.subList((page * 16) - 16, toIndex))
		{
			int buffId = SchemeBufferManager._buffs.get(st)[0];
			
			tb.append("<tr>");
			
			if (buffId < 1000)
				tb.append("<td align=center><img src=\"icon.skill0" + buffId + "\" width=32 height=32></td>");
			else
			{
				if (buffId == 4699 || buffId == 4700)
					tb.append("<td><img src=\"icon.skill1331\" width=32 height=32></td>");
				else if (buffId == 4702 || buffId == 4703)
					tb.append("<td><img src=\"icon.skill1332\" width=32 height=32></td>");
				else
					tb.append("<td><img src=\"icon.skill" + buffId + "\" width=32 height=32></td>");
			}
			
			// The player already has this buff, set the bypass to remove
			if (player.getProfile(profileName) != null && player.getProfile(profileName).buffs.contains(st))
			{
				tb.append("<td>" + st + "</td>");
				tb.append("<td><button value=\"\" action=\"bypass -h npc_%objectId%_addBuff_" + profileName + "_" + st + "_" + page + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>");
			}
			else
			{
				tb.append("<td>" + st + "</td>");
				tb.append("<td><button value=\"\" action=\"bypass -h npc_%objectId%_addBuff_" + profileName + "_" + st + "_" + page + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>");
				tb.append("<td align=center><button value=\"\" action=\"bypass -h npc_%objectId%_addBuff_" + profileName + "_" + st + "_" + page + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Blue\"></td>");
			}
			
			tb.append("<td width=70></td>");
			
			tb.append("</tr>");
		}
		
		text = text.setFile("scripts/services/SchemeBuffer/addbuffstoscheme.htm");
		text = text.replace("%profileName%", profileName);
		text = text.replace("%buffs%", tb.toString());
		tb.clear();
		
		// +2, +1 to complete the number (X.4 would be 3), and +1 to reach the max
		for (int i = 1; i < SchemeBufferManager._pages + 2; i++)
		{
			if (i == page) // Current page, no bypass for it.
				tb.append("<td><button value=\"" + i + "\" width=20 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df_calculator\"></td>");
			else
				tb.append("<td><button value=\"" + i + "\" action=\"bypass -h npc_%objectId%_showBuffsToAddPage_" + profileName + "_" + i + "\" width=20 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		text = text.replace("%pages%", tb.toString());
		
		player.sendPacket(text);
	}
	
	private void buffMage(Player player)
	{
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		text = text.setFile("scripts/services/SchemeBuffer/main.htm");
		int[][] buffs = null;
		buffs = new int[][]{ {1204,2 },  {1040,3 }, {1035, 4}, {1045, 6 }, {1048, 6 },  {1259,4 },  {1078,6 }, {1085,3 },  {1059,3 },  {1389,3 },  {1303,2 },  {1087,3 },  {1352,2 }, {1250,3 },  {4703,12 },  {1036,2 },  {1393,3 },  {1392,3 },  {1353,2 },  {1304,3 }, {1397,3 },  {830,1 }, {1461,1 },  {1363,1 }, {264,1 },  {265,1 },  {267,1 },  {268,1 },  {349,1 },  {270,1 },  {304,1 },  {363,1 },  {306,1 },  {529,1 },  {308,1 },  {273,1 },  {276,1 },  {265,1 },  {307,2 },   {309,2 },   {311,2 },  {530,2 },   {266,2 },  {364,2 } };
		
		try
		{
			for(int skills[] : buffs)
				SkillTable.getInstance().getInfo(skills[0], skills[1]).getEffects(player, player, false, false);
			
		}
		catch (Exception e)
		{
			
		}
		
		player.sendPacket(text);
	}
	
	private void buffFighter(Player player)
	{
		NpcHtmlMessage text = new NpcHtmlMessage(player, this);
		text = text.setFile("scripts/services/SchemeBuffer/main.htm");
		
		int[][] buffs = null;
		buffs = new int[][]{ {1204, 2 }, {1040, 3 }, {1068, 3 }, {1035, 4}, {1045, 6 }, {1048, 6 }, {1036, 2 }, {1259,4 }, {1086,2 }, {1388,3 }, {1397,3 }, {1087,3 }, {1352,2 }, {1250,3 }, {4699,13 }, {1357,1 }, {1353,2 }, {1304,3 }, { 1392,3 }, {1393,3 }, {1077,3 },  {1242,3 }, {825,1 }, {828,1 }, {1461,1 }, {1310,4 }, {264,1 }, {265,1 }, {267,1 }, {268,1 }, {269,1 }, {349,1 },  {270,1 }, {304,1 }, {306,1 }, {529,1 }, {308,1 },  {271,1 },  {272,1 },  {310,1 }, {275,1 },  {274,1 },  {307,2 },  {309,2 },  {311,2 },  {530,2 },  {266,2 },  {364,2 } };
		
		try
		{
			for(int skills[] : buffs)
				SkillTable.getInstance().getInfo(skills[0], skills[1]).getEffects(player, player, false, false);
			
		}
		catch (Exception e)
		{
			
		}
		
		player.sendPacket(text);
	}
	
	public int getBuffLevel(int skillId)
	{
		return SchemeBufferManager._skillLevels.get(skillId);
	}
	
}
