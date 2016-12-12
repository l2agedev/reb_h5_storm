package services.community;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.instancemanager.SchemeBufferManager;
import l2r.gameserver.instancemanager.SchemeBufferManager.PlayerBuffProfile;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;

public class BuffManager implements ScriptFile, ICommunityBoardHandler
{
	
	private static final Logger _log = LoggerFactory.getLogger(BuffManager.class);
	
	@Override
	public void onLoad()
	{
		if (Config.COMMUNITYBOARD_ENABLED && Config.ENABLE_SCHEME_BUFFER)
		{
			_log.info("CommunityBoard: Manage Buffer service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if (Config.COMMUNITYBOARD_ENABLED && Config.ENABLE_SCHEME_BUFFER)
			CommunityBoardManager.getInstance().removeHandler(this);
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
			"_bbsbuff;",
			"_bbsbuff;showMainWindow",
			"_bbsbuff;showCreateProfileWindow",
			"_bbsbuff;createProfile;",
			"_bbsbuff;showDeleteProfileWindow;",
			"_bbsbuff;deleteProfile;",
			"_bbsbuff;showMyProfilesToEdit; ",
			"_bbsbuff;editProfile;",
			"_bbsbuff;deleteBuff;",
			"_bbsbuff;showPageToAddBuffs",
			"_bbsbuff;showBuffsToAddPage;",
			"_bbsbuff;addBuff;",
			"_bbsbuff;showProfilesToBuff",
			"_bbsbuff;getBuffs;",
			"_bbsbuff;buffmyclass",
			"_bbsbuff;heal",
			"_bbsbuff;cancel",
			"_bbsbuff;noblesse",
			"_bbsbuff;ultimatepetbuff"
		};
	}
	
	@Override
	public void onBypassCommand(Player activeChar, String bypass)
	{
		if (!Config.ENABLE_SCHEME_BUFFER || activeChar == null)
			return;
		
		if (activeChar == null || (!activeChar.isConnected() || activeChar.isInOfflineMode()) && !activeChar.isPhantom())
			return;
		
		if (!activeChar.isGM() && !activeChar.isPhantom())
		{
			if (activeChar.isCursedWeaponEquipped() || NexusEvents.isInEvent(activeChar) || activeChar.isInJail() || activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isFlying() || activeChar.isTerritoryFlagEquipped())
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", activeChar.isLangRus() ? "Невозможно использовать в данный момент!" : "You can not use the buffer at this moment!");
				return;
			}

			if (activeChar.getReflectionId() != 0 || activeChar.isInZone(ZoneType.epic))
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", activeChar.isLangRus() ? "Невозможно использовать в данных зонах!" : "Can not be used in these areas!");
				return;
			}
			
			
			if (activeChar.isInZone(ZoneType.SIEGE))
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", activeChar.isLangRus() ? "Невозможно использовать во время осад!" : "Can not be used during the siege!");
				return;
			}
			
			if (activeChar.getLevel() > Config.BUFFER_MAX_LEVEL || activeChar.getLevel() < Config.BUFFER_MIN_LEVEL)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", activeChar.isLangRus() ? "Ваш уровень не отвечает требованиям!" : "Your level does not meet the requirements!");
				return;
			}
			
			if (!activeChar.isInZone(ZoneType.peace_zone) && !activeChar.isInZone(ZoneType.RESIDENCE))
			{
				if (activeChar.getPvpFlag() > 0 || activeChar.isInCombat())
				{
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "You cannot buff while in combat or pvp flagged.");
					return;
				}
				
				/*
				if (PremiumAccountsTable.getBufferOutsidePeace(activeChar))
				{
					if (activeChar.getPvpFlag() > 0 || activeChar.isInCombat())
					{
						activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "You cannot buff while in combat or pvp flagged.");
						return;
					}
					
					int buffTimeLeft = 0;
					int buffsCount = 0;
					for (Effect e : activeChar.getEffectList().getAllEffects())
					{
						if (e != null && e.getSkill().getSkillType() == SkillType.BUFF && e.getSkill().getTargetType() != SkillTargetType.TARGET_SELF)
						{
							buffTimeLeft += e.getTimeLeft();
							buffsCount++;
						}
					}
					
					if (buffsCount > 0 && buffTimeLeft > 0 && (buffTimeLeft / buffsCount) > 120) // bufftime in sec / buff count = timeleft... and if time is above 120 sec .. that mean 2 minutes... return him..
					{
						activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "Your buffs timeleft must be below 2 minutes to use the buffer.");
						return;
					}
				}
				else
				{	
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "You must be inside Town or ClanHall to use the buffer.");
					return;
				}
				*/
			}
		}	
			
		String[] subCommands = bypass.split(";");
		
		if (bypass.startsWith("_bbsbuff;showMainWindow"))
			showMainWindow(activeChar);
		else if (bypass.startsWith("_bbsbuff;showCreateProfileWindow"))
		{
			if (activeChar.getProfiles().size() >= Config.MAX_SCHEME_PROFILES)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "You are allowed to create only up to " + Config.MAX_SCHEME_PROFILES + " schemes.");
				return;
			}
			
			showCreateProfileWindow(activeChar);
		}
		else if (bypass.startsWith("_bbsbuff;createProfile;"))
		{
			if (subCommands.length == 2 || subCommands[0].isEmpty() || subCommands[1].isEmpty() || subCommands[2].isEmpty())
				return;
			
			if (activeChar.getProfiles().size() >= Config.MAX_SCHEME_PROFILES)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "You are allowed to create only up to " + Config.MAX_SCHEME_PROFILES + " schemes.");
				return;
			}
			else if (activeChar.getProfile(subCommands[2]) != null) // subCommands[1] = profileName
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "Duplicate profile name! Choose another.");
				return;
			}
			else
			{
				String profileName = subCommands[2];
				while (profileName.startsWith(" "))
					profileName = profileName.replaceFirst(" ", "");
				
				if (!profileName.matches(Config.CNAME_TEMPLATE))
				{
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "You cannot create template with such symbols. Please use only (A-Za-z0-9)");
					return;
				}
				// saveProfile(activeChar, subCommands[1]); // subCommands[1] = profileName
				activeChar.addProfile(SchemeBufferManager.getInstance().new PlayerBuffProfile(profileName)); // subCommands[1] = profileName
				showBuffsToAddPage(activeChar, profileName, 1);
			}
		}
		else if (bypass.startsWith("_bbsbuff;showDeleteProfileWindow"))
			showDeleteProfileWindow(activeChar);
		else if (bypass.startsWith("_bbsbuff;deleteProfile;"))
		{
			if (subCommands[2].isEmpty())
				return;
			
			SchemeBufferManager.getInstance().deleteFromDatabase(activeChar, activeChar.getProfile(subCommands[2].trim()));
			Util.communityNextPage(activeChar, "_bbsbuff;showMainWindow");
		}
		else if (bypass.startsWith("_bbsbuff;showMyProfilesToEdit"))
			showProfilesToEditWindow(activeChar);
		else if (bypass.startsWith("_bbsbuff;editProfile;"))
		{
			
			if (subCommands[2].isEmpty())
				return;
			
			showProfilePageToEdit(activeChar, subCommands[2], 1);
		}
		else if (bypass.startsWith("_bbsbuff;deleteBuff;"))
		{
			
			if (subCommands[1].isEmpty() || subCommands[2].isEmpty() || subCommands[3].isEmpty() || subCommands[4].isEmpty())
				return;
			
			activeChar.getProfile(subCommands[2]).removeBuff(subCommands[3]); // subCommands[1] = profileName, subCommands[2] = buffName
			showProfilePageToEdit(activeChar, subCommands[2], Integer.parseInt(subCommands[4]));
		}
		else if (bypass.startsWith("_bbsbuff;showPageToAddBuffs"))
			showPageToAddBuffs(activeChar);
		else if (bypass.startsWith("_bbsbuff;showBuffsToAddPage;"))
		{
			if (subCommands[1].isEmpty() || subCommands[2].isEmpty() || subCommands[3].isEmpty())
				return;
			
			showBuffsToAddPage(activeChar, subCommands[2], Integer.valueOf(subCommands[3]));
			// showBuffsToAddRemovePage(activeChar, subCommands[1], Integer.valueOf(subCommands[2]));
		}
		else if (bypass.startsWith("_bbsbuff;addBuff;"))
		{
			
			// Since theres buffs whose name contains " ".To make it cleary: [command + space + subcommand + space]
			
			if (subCommands[1].isEmpty() || subCommands[2].isEmpty() || subCommands[3].isEmpty() || subCommands[4].isEmpty() || !Util.isDigit(subCommands[4]))
				return;
			
			// Nice hack to support buff chose option
			if ("chosebuffs".equals(subCommands[2]))
			{
				int buffid = SchemeBufferManager._buffs.get(subCommands[3])[0];
				int skillLevel = SchemeBufferManager._buffs.get(subCommands[3])[1];
				
				if (skillLevel == 0)
					return;
				
				SkillTable.getInstance().getInfo(buffid, skillLevel).getEffects(activeChar, activeChar, false, false, Config.BUFFS_TIME * 60000, false);
				showBuffsToAddPage(activeChar, subCommands[2], Integer.valueOf(subCommands[4]));
				return;
			}
			
			if (activeChar.getProfile(subCommands[2]).buffs.size() >= Config.MAX_BUFFS_PER_PROFILE) // subCommands[1] = profileName
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "You are allowed to have up to " + Config.MAX_BUFFS_PER_PROFILE + " buffs in a scheme. You've reached the maximum!");
				showBuffsToAddPage(activeChar, subCommands[2], Integer.valueOf(subCommands[4]));
				return;
			}
			
			activeChar.getProfile(subCommands[2]).addBuff(subCommands[3]); // subCommands[1] = profileName, subCommands[2] = buffName
			showBuffsToAddPage(activeChar, subCommands[2], Integer.valueOf(subCommands[4]));
		}
		else if (bypass.startsWith("_bbsbuff;showProfilesToBuff"))
			showProfilesToBuff(activeChar);
		else if (bypass.startsWith("_bbsbuff;getBuffs;"))
		{
			if (subCommands[1].isEmpty() || subCommands[2].isEmpty() || subCommands[3].isEmpty())
				return;
			
			Creature target = null;
			
			if (subCommands[3].trim().equals("Player")) // type
			{
				target = activeChar;
			}
			else
			{
				if (activeChar.getPet() == null)
				{
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "You dont have any pet summoned!");
					showProfilesToBuff(activeChar);
					return;
				}
				
				target = activeChar.getPet();
			}
			
			if (target == null)
				return;
			
			if (activeChar.getProfile(subCommands[2]).buffs.isEmpty())
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "There is no buffs in your scheme....");
				showProfilesToBuff(activeChar);
				return;
			}
			
			if (activeChar.getInventory().getItemByItemId(57) == null)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "You dont have any money!");
				showProfilesToBuff(activeChar);
				return;
			}
			
			int priceToPay = Config.PRICE_PER_BUFF * activeChar.getProfile(subCommands[2].trim()).buffs.size(); // subCommands[1] = profileName
			if (activeChar.getInventory().getItemByItemId(57).getCount() < priceToPay)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "Not enough adena to get those buffs!");
				showProfilesToBuff(activeChar);
				return;
			}
			
			activeChar.reduceAdena(priceToPay, true);
			
			for (String st : activeChar.getProfile(subCommands[2]).buffs) // subCommands[1] = profileName
			{
				if (SchemeBufferManager._buffs.get(st) == null)
					continue;
				
				int buffid = SchemeBufferManager._buffs.get(st)[0];
				int skillLevel = SchemeBufferManager._buffs.get(st)[1];
				
				Skill skill = SkillTable.getInstance().getInfo(buffid, skillLevel);
				if (skill != null)
					skill.getEffects(target, target, false, false, Config.BUFFS_TIME * 60000, false);
				try
				{
					Thread.sleep(10L);
				}
				catch(Exception e) {}
			}
			
			showMainWindow(activeChar);
		}
		else if (bypass.startsWith("_bbsbuff;buffmyclass"))
		{
			if (!Config.ALLOW_BUFF_FOR_MY_CLASS)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "This functions is not enabled!");
				return;
			}
			
			buffMyClass(activeChar);
			showMainWindow(activeChar);
		}
		else if (bypass.startsWith("_bbsbuff;heal"))
		{
			if (!Config.ALLOW_BUFFER_HEAL)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "This functions is not enabled!");
				return;
			}
			
			activeChar.setCurrentCp(activeChar.getMaxCp());
			activeChar.setCurrentHp(activeChar.getMaxHp(), true);
			activeChar.setCurrentMp(activeChar.getMaxMp());
			
			if (activeChar.getPet() != null)
			{
				activeChar.getPet().setCurrentCp(activeChar.getPet().getMaxCp());
				activeChar.getPet().setCurrentHp(activeChar.getPet().getMaxHp(), true);
				activeChar.getPet().setCurrentMp(activeChar.getPet().getMaxMp());
			}
			
			showMainWindow(activeChar);
		}
		else if (bypass.startsWith("_bbsbuff;cancel"))
		{
			if (!Config.ALLOW_CANCEL_BUFFS)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "This functions is not enabled!");
				return;
			}
			
			if (activeChar.getEffectList().getEffectsBySkillId(Skill.SKILL_RAID_CURSE) == null)
				activeChar.getEffectList().stopAllEffects();

			showMainWindow(activeChar);
		}
		else if (bypass.startsWith("_bbsbuff;noblesse"))
		{
			if (!Config.ALLOW_BUFFER_NOBLE)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "This functions is not enabled!");
				return;
			}
			
			SkillTable.getInstance().getInfo(1323, 1).getEffects(activeChar, activeChar, false, false, Config.BUFFS_TIME * 60000, false);

			showMainWindow(activeChar);
		}
		else if (bypass.startsWith("_bbsbuff;ultimatepetbuff"))
		{
			if (Config.ALLOW_BUFFER_UP_BUFF)
			{
				if (activeChar.getPet() == null)
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "No summoned pets/servitors found.");
				else if (activeChar.getActiveClassId() == 14 || activeChar.getActiveClassId() == 28 || activeChar.getActiveClassId() == 41 || activeChar.getActiveClassId() == 57 || activeChar.getActiveClassId() == 96 || activeChar.getActiveClassId() == 104 || activeChar.getActiveClassId() == 111 || activeChar.getActiveClassId() == 118)
				{
					SkillTable.getInstance().getInfo(5503, 1).getEffects(activeChar.getPet(), activeChar.getPet(), false, false, Config.BUFFS_TIME * 60000, false);
				}
				else
					activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "Sorry, this buff is only avaiable for summoner classes and maestros.");
			}
			else
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Buffer", "This functions is not enabled!");
			
			showMainWindow(activeChar);
		}
	}
	
	/*
	 * Shows the main window to the player
	 */
	public void showMainWindow(Player player)
	{
		String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/communitybuffer/main.htm", player);
		
		ShowBoard.separateAndSend(text, player);
	}
	
	/*
	 * Shows the create profile window
	 */
	private void showCreateProfileWindow(Player player)
	{
		String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/communitybuffer/createscheme.htm", player);
		
		ShowBoard.separateAndSend(text, player);
	}
	
	/*
	 * Shows a window with all player profiles, to choose one to delete
	 */
	private void showDeleteProfileWindow(Player player)
	{
		String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/communitybuffer/deletescheme.htm", player);
		
		TextBuilder tb = new TextBuilder();
		for (String st : player.getProfiles().keySet())
			tb.append("<button value=\"" + st + "\" action=\"bypass _bbsbuff;deleteProfile;" + st + "\" width=120 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		
		text = text.replace("%profilesToDelete%", tb.toString());
		
		ShowBoard.separateAndSend(text, player);
	}
	
	/*
	 * Show a window with all player profiles. They can be edited from here
	 */
	private void showProfilesToEditWindow(Player player)
	{
		String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/communitybuffer/profiletoedit.htm", player);
		
		TextBuilder tb = new TextBuilder();
		boolean hasSchemes = false;
		
		for (String st : player.getProfiles().keySet())
		{
			tb.append("<tr>");
			tb.append("<td><button value=\"" + st + "\" action=\"bypass _bbsbuff;editProfile;" + st + "\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			tb.append("<td><br><button value=\"\" action=\"bypass _bbsbuff;showBuffsToAddPage;" + st + ";1\" width=15 height=15 back=\"L2UI_CH3.QuestWndPlusBtn\" fore=\"L2UI_CH3.QuestWndPlusBtn\"></td>");
			tb.append("<td><br><button value=\"\" action=\"bypass _bbsbuff;deleteProfile;" + st + "\" width=15 height=15 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_ct1.Button_DF_Delete\"></td>");
			tb.append("</tr>");
			hasSchemes = true;
		}
		
		if (!hasSchemes) // No schemes
		{
			tb.append("<tr><td width=150 align=center valign=center><font color=EB6D6D name=hs9>You dont have any scheme profile!</font><br></td></tr>");
			tb.append("<tr><td width=150 align=center valign=center>Type the new scheme name and click the<br1> 'Create Scheme' button.<br></td></tr>");
			tb.append("<tr><td width=150 align=center valign=center><edit var=\"profilename\" width=140 length=16></td></tr>");
			tb.append("<tr><td width=150 align=center valign=center><button value=\"Create Scheme\" action=\"bypass _bbsbuff;createProfile; $profilename\" width=140 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		}
		
		text = text.replace("%profilesToEdit%", tb.toString());
		
		ShowBoard.separateAndSend(text, player);
	}
	
	/*
	 * Shows a window with all buffs contains into a single profile
	 */
	private void showProfilePageToEdit(Player player, String profileName, int page)
	{
		String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/communitybuffer/buffsedit.htm", player);
		
		TextBuilder tb = new TextBuilder();
		
		text = text.replace("%profileName%", profileName);
		PlayerBuffProfile profile = player.getProfile(profileName);
		if (profile == null)
		{
			player.sendMessage(new CustomMessage("scripts.services.community.buffmanager.proffile_not_found", player));
			return;
		}
		
		List<String> tempBuff = profile.buffs;
		int toIndex = page * 22;
		if (toIndex > tempBuff.size())
			toIndex = tempBuff.size();
		
		boolean side = true;
		boolean isPageEmpty = true;
		for (String name : tempBuff.size() <= 22 ? tempBuff : tempBuff.subList((page * 22) - 22, toIndex))
		{
			if (!SchemeBufferManager._buffs.containsKey(name))
				continue;
			
			int buffId = SchemeBufferManager._buffs.get(name)[0];
			
			isPageEmpty = false;
			
			if (side)
				tb.append("<tr>");
			
			// if summoners skills (exception)
			if (buffId == 4699 || buffId == 4700)
				buffId = 1331;
			
			if (buffId == 4702 || buffId == 4703)
				buffId = 1332;
						
			if (buffId < 1000)
				tb.append("<td><img src=\"icon.skill0" + buffId + "\" width=32 height=32></td>");
			else
				tb.append("<td><img src=\"icon.skill" + buffId + "\" width=32 height=32></td>");
			
			tb.append("<td><button value=\"" + name + "\" action=\"bypass _bbsbuff;deleteBuff;" + profileName + ";" + name + ";" + page + "\" width=180 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			
			if (buffId < 1000)
				tb.append("<td><img src=\"icon.skill0" + buffId + "\" width=32 height=32></td>");
			else
				tb.append("<td><img src=\"icon.skill" + buffId + "\" width=32 height=32></td>");
			
			tb.append("<td width=70></td>");
			
			if (!side)
				tb.append("</tr>");
			
			side = !side;
		}
		
		if (!side)
			tb.append("</tr>");
		
		if (isPageEmpty)
		{
			tb.append("<tr><td align=center><font color=FF6565 name=hs12>There is no buffs to be removed...</font></td><br></tr>");
			tb.append("<tr><td width=150 align=center valign=center><button value=\"Add Buffs\" action=\"bypass _bbsbuff;showBuffsToAddPage;" + profileName + ";1\" width=140 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		}
		
		text = text.replace("%buffs%", tb.toString());
		tb.clear();
		
		// +2, +1 to complete the number (X.4 would be 3), and +1 to reach the max
		for (int i = 1; i < Math.round(profile.buffs.size() / 22) + 2; i++)
		{
			if (profile.buffs.size() == 0)
				continue;
			
			if (i == page) // Current page, no bypass for it.
				tb.append("<td width=100 valign=top><button value=\"" + i + "\" width=25 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df_calculator\"></td>");
			else
				tb.append("<td width=100 valign=top><button value=\"" + i + "\" action=\"bypass _bbsbuff;deleteBuff;" + profileName + ";null;" + i + "\" width=25 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		if (tb.toString().isEmpty())
			tb.append("<td width=100 valign=top>&nbsp;</td>");
			
		text = text.replace("%limit%", Config.MAX_BUFFS_PER_PROFILE + "/" + profile.buffs.size());
		text = text.replace("%pages%", tb.toString());
		
		ShowBoard.separateAndSend(text, player);
	}
	
	/*
	 * Shows a window with the current profiles to add buffs
	 */
	private void showPageToAddBuffs(Player player)
	{
		String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/communitybuffer/profilebuff.htm", player);
		
		TextBuilder tb = new TextBuilder();
		
		for (String st : player.getProfiles().keySet())
		{
			tb.append("<tr>");
			tb.append("<td><button value=\"" + st + "\" action=\"bypass _bbsbuff;showBuffsToAddPage;" + st + ";1\" width=160 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			tb.append("<td><br><button value=\"\" action=\"bypass _bbsbuff;editProfile;" + st + "\" width=15 height=15 back=\"L2UI_CH3.QuestWndMinusBtn\" fore=\"L2UI_CH3.QuestWndMinusBtn\"></td>");
			tb.append("<td><br><button value=\"\" action=\"bypass _bbsbuff;deleteProfile;" + st + "\" width=15 height=15 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_ct1.Button_DF_Delete\"></td>");
			tb.append("</tr>");
		}
		
		if (tb.length() == 0) // No schemes
		{
			tb.append("<tr><td width=150 align=center valign=center><font color=EB6D6D name=hs9>(no schemes has been found)</font><br></td></tr>");
			tb.append("<tr><td width=150 align=center valign=center>Type the new scheme name and click the<br1> 'Create Scheme' button.<br></td></tr>");
			tb.append("<tr><td width=150 align=center valign=center><edit var=\"profilename\" width=140 length=16></td></tr>");
			tb.append("<tr><td width=150 align=center valign=center><button value=\"Create Scheme\" action=\"bypass _bbsbuff;createProfile; $profilename\" width=140 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		}
		
		text = text.replace("%profilebuff%", tb.toString());
		
		ShowBoard.separateAndSend(text, player);
	}
	
	/*
	 * Show all avaliable buffs to add to a profile
	 */
	private void showBuffsToAddPage(Player player, String profileName, int page)
	{
		String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/communitybuffer/showbufftoadd.htm", player);
		
		List<String> tempBuff = new ArrayList<>();
		tempBuff.addAll(SchemeBufferManager._buffs.keySet());
		TextBuilder tb = new TextBuilder();
		
		int toIndex = page * 22;
		if (toIndex > tempBuff.size())
			toIndex = tempBuff.size();
		
		boolean side = true;
		
		boolean isPageEmpty = true;
		for (String st : tempBuff.subList((page * 22) - 22, toIndex))
		{
			int buffId = SchemeBufferManager._buffs.get(st)[0];
			
			// The player already has this buff, so dont show it.
			if (player.getProfile(profileName) != null && player.getProfile(profileName).buffs.contains(st))
				continue;
			
			isPageEmpty = false;
			
			
			if (side)
				tb.append("<tr>");
			
			// if summoners skills (exception)
			if (buffId == 4699 || buffId == 4700)
				buffId = 1331;
			
			if (buffId == 4702 || buffId == 4703)
				buffId = 1332;

			if (buffId < 1000)
				tb.append("<td><img src=\"icon.skill0" + buffId + "\" width=32 height=32></td>");
			else
				tb.append("<td><img src=\"icon.skill" + buffId + "\" width=32 height=32></td>");
			
			tb.append("<td><button value=\"" + st + "\" action=\"bypass _bbsbuff;addBuff;" + profileName + ";" + st + ";" + page + "\" width=180 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			
			if (buffId < 1000)
				tb.append("<td><img src=\"icon.skill0" + buffId + "\" width=32 height=32></td>");
			else
				tb.append("<td><img src=\"icon.skill" + buffId + "\" width=32 height=32></td>");
			
			tb.append("<td width=70></td>");
			
			if (!side)
				tb.append("</tr>");
			
			side = !side;
		}
		
		if (!side)
			tb.append("</tr>");
		
		if (isPageEmpty)
			tb.append("<tr><td align=center><font color=FF6565 name=hs12>You already have all buffs from this page.</font></td></tr>");
		
		text = text.replace("%buffs%", tb.toString());
		tb.clear();
		
		// +2, +1 to complete the number (X.4 would be 3), and +1 to reach the max
		for (int i = 1; i < SchemeBufferManager._pages + 2; i++)
		{
			if (i == page) // Current page, no bypass for it.
				tb.append("<td><button value=\"" + i + "\" width=28 height=28 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df_calculator\"></td>");
			else
				tb.append("<td><button value=\"" + i + "\" action=\"bypass _bbsbuff;showBuffsToAddPage;" + profileName + ";" + i + "\" width=25 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		PlayerBuffProfile profile = player.getProfile(profileName);
		int buffs = 0;
		if (profile != null )
			buffs = profile.buffs.size();
		
		text = text.replace("%limit%", (profileName.equalsIgnoreCase("chosebuffs") ? "&nbsp;" : "<font name=hs9>Limit: " + buffs + "/" + Config.MAX_BUFFS_PER_PROFILE + "</font>"));
		text = text.replace("%pages%", tb.toString());
		text = text.replace("%profileName%", (profileName.equalsIgnoreCase("chosebuffs") ? "Click to buff yourself" : "Select Buff to add it in scheme: " + profileName));
		
		ShowBoard.separateAndSend(text, player);
	}
	
	/*
	 * Show all profiles to buff the player
	 */
	private void showProfilesToBuff(Player player)
	{
		String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/communitybuffer/profiles.htm", player);
		
		TextBuilder tb = new TextBuilder();
		StringBuilder sb = new StringBuilder();
		
		boolean hasSchemes = false;
		for (String st : player.getProfiles().keySet())
		{
			tb.append("<tr>");
			tb.append("<td><button value=\"" + st + "\" action=\"bypass _bbsbuff;getBuffs;" + st + "; $Who\" width=140 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			tb.append("<td><br><button value=\"\" action=\"bypass _bbsbuff;showBuffsToAddPage;" + st + ";1\" width=15 height=15 back=\"L2UI_CH3.QuestWndPlusBtn\" fore=\"L2UI_CH3.QuestWndPlusBtn\"></td>");
			tb.append("<td><br><button value=\"\" action=\"bypass _bbsbuff;editProfile;" + st + "\" width=15 height=15 back=\"L2UI_CH3.QuestWndMinusBtn\" fore=\"L2UI_CH3.QuestWndMinusBtn\"></td>");
			tb.append("<td><br><button value=\"\" action=\"bypass _bbsbuff;deleteProfile;" + st + "\" width=15 height=15 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_ct1.Button_DF_Delete\"></td>");
			tb.append("</tr>");
			hasSchemes = true;
		}
		
		if (!hasSchemes)
		{
			tb.append("<tr><td width=150 align=center valign=center><font color=EB6D6D name=hs9>(no schemes avaiable)</font><br></td></tr>");
			tb.append("<tr><td width=150 align=center valign=center>Type the new scheme name and click the<br1> 'Create Scheme' button.<br></td></tr>");
			tb.append("<tr><td width=150 align=center valign=center><edit var=\"profilename\" width=140 length=16></td></tr>");
			tb.append("<tr><td width=150 align=center valign=center><button value=\"Create Scheme\" action=\"bypass _bbsbuff;createProfile; $profilename\" width=140 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		}
		
		if (player.getProfiles().size() >= Config.MAX_SCHEME_PROFILES)
		{
			sb.append("<tr><td align=center valign=center><font color=EAFA00 name=hs9>Maximum scheme profiles reached!</font><br></td></tr>");
			sb.append("<tr><td width=150 align=center valign=center>To delete or edit scheme list please click the button..<br></td></tr>");
			sb.append("<tr><td width=150 align=center valign=center><button value=\"Scheme Profiles\" action=\"bypass _bbsbuff;showMyProfilesToEdit\" width=140 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		}
		else
		{
			sb.append("<tr><td width=250 align=center valign=center><font color=75FF5C name=hs9>Type desired name for Scheme Profile.</font><br></td></tr>");
			sb.append("<tr><td width=150 align=center valign=center><edit var=\"profilename2\" width=140 length=16></td></tr>");
			sb.append("<tr><td width=150 align=center valign=center><button value=\"Create Scheme\" action=\"bypass _bbsbuff;createProfile; $profilename2\" width=140 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		}
		
		text = text.replace("%profiles%", tb.toString());
		text = text.replace("%schememenu%", sb.toString());
		text = text.replace("%schemesleft%", "" + (Config.MAX_SCHEME_PROFILES - player.getProfiles().size()));
		
		ShowBoard.separateAndSend(text, player);
	}
	
	@SuppressWarnings("unused")
	private void showBuffsToAddRemovePage(Player player, String profileName, int page)
	{
		String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/communitybuffer/addbuffstoscheme.htm", player);
		
		List<String> tempBuff = new ArrayList<>();
		tempBuff.addAll(SchemeBufferManager._buffs.keySet());
		TextBuilder tb = new TextBuilder();
		
		int toIndex = page * 23;
		if (toIndex > tempBuff.size())
			toIndex = tempBuff.size();
		
		boolean side = false;
		for (String st : tempBuff.subList((page * 23) - 23, toIndex))
		{
			int buffId = SchemeBufferManager._buffs.get(st)[0];
			
			if (side)
				tb.append("<tr>");
			
			// if summoners skills (exception)
			if (buffId == 4699 || buffId == 4700)
				buffId = 1331;
			
			if (buffId == 4702 || buffId == 4703)
				buffId = 1332;
						
			if (buffId < 1000)
				tb.append("<td align=center><img src=\"icon.skill0" + buffId + "\" width=32 height=32></td>");
			else
				tb.append("<td align=center><img src=\"icon.skill" + buffId + "\" width=32 height=32></td>");
			
			// The player already has this buff, set the bypass to remove
			if (player.getProfile(profileName) != null && player.getProfile(profileName).buffs.contains(st))
				tb.append("<td align=center><button value=\"" + st + "\" action=\"bypass _bbsbuff;deleteBuff;" + profileName + ";" + st + ";" + page + "\" width=200 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			else
				tb.append("<td align=center><button value=\"" + st + "\" action=\"bypass _bbsbuff;addBuff;" + profileName + ";" + st + ";" + page + "\" width=200 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			
			if (buffId < 1000)
				tb.append("<td align=center><img src=\"icon.skill0" + buffId + "\" width=32 height=32></td>");
			else
				tb.append("<td align=center><img src=\"icon.skill" + buffId + "\" width=32 height=32></td>");
			
			tb.append("<td width=70></td>");
			
			if (side)
				tb.append("</tr>");
			
			side = !side;
		}
		
		text = text.replace("%profileName%", profileName);
		text = text.replace("%buffs%", tb.toString());
		tb.clear();
		
		// +2, +1 to complete the number (X.4 would be 3), and +1 to reach the max
		for (int i = 1; i < SchemeBufferManager._pages + 2; i++)
		{
			if (i == page) // Current page, no bypass for it.
				tb.append("<td><button value=\"" + i + "\" width=25 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df_calculator\"></td>");
			else
				tb.append("<td><button value=\"" + i + "\" action=\"bypass _bbsbuff;showBuffsToAddPage;" + profileName + ";" + i + "\" width=25 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		text = text.replace("%pages%", tb.toString());
		
		ShowBoard.separateAndSend(text, player);
	}
	
	private void buffMyClass(Player player)
	{
		int[][] buffs = null;
		
		switch(player.getClassId())
		{
			case duelist:
			case grandKhauatari:
			case dreadnought:
			case titan:
			case fortuneSeeker:
			case maestro:	
			case swordMuse:
			case spectralDancer:
				buffs = new int[][]{ {1204, 2 }, {1040, 3 }, {1068, 3 }, {1035, 4}, {1045, 6 }, {1048, 6 }, {1036, 2 }, {1259,4 }, {1086,2 }, {1388,3 }, {1397,3 }, {1087,3 }, {1352,2 }, {1364,1 }, {1250,3 }, {4699,13 }, {1357,1 }, {1353,2 }, {1304,3 }, { 1392,3 }, {1393,3 }, {1077,3 },  {1242,3 }, {825,1 }, {828,1 },  {1415,1 }, {1461,1 }, {1416,1 }, {1310,4 }, {264,1 }, {265,1 }, {267,1 }, {268,1 }, {269,1 }, {349,1 },  {270,1 }, {304,1 }, {306,1 }, {529,1 }, {308,1 },  {271,1 },  {272,1 },  {310,1 }, {275,1 },  {274,1 },  {307,2 },  {309,2 },  {311,2 },  {530,2 },  {266,2 },  {364,2 } };
				break;
			case phoenixKnight:
			case hellKnight:
			case evaTemplar:
			case shillienTemplar:
				buffs = new int[][]{{1204,2 }, {1068,3 }, {1045,6 }, {1048,6 }, {1268,4 }, {1077,3 }, {1086,2 }, {1242,3 },  {1397,3 },  {1087,3 },  {1364,1 },  {4699,13 },  {1036,2 },  {1040,3 },  {1035,4 },  {1259,4 },  {1243,6 },  {1392,3 },  {1393,3 },  {1389,3 },  {1304,3 },  {1353,2 },  {1352,2 },  {1043,2 },  {828,1 },  {826,1 },  {1415,1 },  {4703,13 },  {1363,1 },  {1416,1 },  {1461,1 },  {264,1 },  {265,1 },  {267,1 },  {268,1 },  {269,1 },  {349,1 },  {270,1 },  {304,1 },  {306,1 },  {529,1 }, { 308,1 },  {271,1 },  {272,1 },  {310,1 },  {275,1 },  {274,1 },  {307,2 },  {309,2 },  {311,2 },  {530,2 },  {266,2 },  {364,2 } };
				break;
			case sagittarius:
			case moonlightSentinel:
			case ghostSentinel:
			case adventurer:
			case windRider:
			case ghostHunter:
				buffs = new int[][]{ {1204,2 }, {1040,3 }, {1068,3 }, {1035,4 }, {1045, 6 }, {1048, 6 }, {1036, 2 }, {1259,4 }, {1268,4 }, {1086,2 }, {1397,3 }, {1087,3 }, {1352,2 }, {1363,1 }, {1364,1 }, {1250,3 }, {4699,13 }, {1353,2 }, {1304,3 }, {1392,3 }, {1393,3 }, {1242,3 }, {1077,3 }, {1388,3 }, {529,1 }, { 827,1 }, {1415,1 }, {1461,1 }, {1416,1 }, {264,1 },  {265,1 },  {267,1 },  {268,1 },  {269,1 },  {349,1 },  {270,1 },  {304,1 }, {306,1 },  {529,1 },  {308,1 },  {271,1 },  {272,1 },  {310,1 },  {275,1 },  {274,1 },  {307,2 },  {309,2 },  {311,2 },  {530,2 },  {266,2 },  {364,2 } };
				break;
			case mage:
			case archmage:
			case soultaker:
			case mysticMuse:
			case stormScreamer:
			case arcanaLord:
			case elementalMaster:
			case spectralMaster:
				buffs = new int[][]{ {1204,2 }, {1040,3 }, {1035, 4}, {1045, 6 }, {1048, 6 }, {1259,4 }, {1078,6 }, {1085,3 }, {1059,3 }, {1389,3 }, {1303,2 }, {1087,3 }, {1352,2 }, {1364,1 }, {1250,3 }, {4703,12 }, {1036,2 }, {1393,3 }, {1392,3 }, {1353,2 }, {1304,3 }, {1397,3 }, {830,1 }, {1415,1 }, {1461,1 }, {1363,1 }, {1416,1 }, {264,1 }, {265,1 }, {267,1 }, {268,1 }, {349,1 }, {270,1 }, {304,1 }, {363,1 }, {306,1 }, {529,1 }, {308,1 }, {273,1 }, {276,1 }, {265,1 }, {307,2 },  {309,2 },  {311,2 }, {530,2 },  {266,2 },  {364,2 } };
				break;
			case cardinal:
			case evaSaint:
			case shillienSaint:
			case hierophant:
			case dominator:
			case doomcryer:
				buffs = new int[][]{ {1204,2 },  {1040,3 }, {1035, 4}, {1045, 6 }, {1048, 6 },  {1259,4 },  {1078,6 }, {1085,3 },  {1059,3 },  {1389,3 },  {1303,2 },  {1087,3 },  {1352,2 },  {1364,1 },  {1250,3 },  {4703,12 },  {1036,2 },  {1393,3 },  {1392,3 },  {1353,2 },  {1304,3 }, {1397,3 },  {830,1 },  {1415,1 }, {1461,1 },  {1363,1 },  {1416,1 },  {264,1 },  {265,1 },  {267,1 },  {268,1 },  {349,1 },  {270,1 },  {304,1 },  {363,1 },  {306,1 },  {529,1 },  {308,1 },  {273,1 },  {276,1 },  {265,1 },  {307,2 },   {309,2 },   {311,2 },  {530,2 },   {266,2 },  {364,2 } };
				break;
			case doombringer:
			case maleSoulhound:
			case femaleSoulhound:
			case trickster:
			case judicator:
				buffs = new int[][]{{1078,6 }, {1085,3 }, {1059,3 }, {1303,2 }, {4703,12 }, {1204,2 }, {1040,3 }, {1068,3 }, {1035, 4}, {1045, 6 }, {1048,6 }, {1036, 2 }, {1259,4 }, {1268,4 }, {1077,3 }, {1086,2 }, {1242,3 }, {1397,3 }, {1087,3 }, {1352,2 }, {1364,1 }, {1250,3 }, {4699,13 }, {1393,3 }, {1392,3 }, {1353,2 }, {1304,3 }, {1389,3 }, {829,1 }, {826,1 }, {1415,1 }, {1363,1 }, {1461,1 }, {1416,1 }, {276,1 }, {365,1 }, {264,1 }, {265,1 }, {267,1 }, {268,1 }, {269,1 }, {349,1 }, {270,1 }, {304,1 }, {306,1 }, {529,1 }, {308,1 }, {271,1 }, {272,1 }, {310,1 }, {275,1 }, {274,1 }, {307,2 }, {309,2 },  {311,2 }, {530,2 },  {266,2 },  {364,2 } };
				break;
			default:
				if (player.getClassId().isMage())
					buffs = new int[][]{ {1204,2 },  {1040,3 }, {1035, 4}, {1045, 6 }, {1048, 6 },  {1259,4 },  {1078,6 }, {1085,3 },  {1059,3 },  {1389,3 },  {1303,2 },  {1087,3 },  {1352,2 },  {1364,1 },  {1250,3 },  {4703,12 },  {1036,2 },  {1393,3 },  {1392,3 },  {1353,2 },  {1304,3 }, {1397,3 },  {830,1 },  {1415,1 }, {1461,1 },  {1363,1 },  {1416,1 },  {264,1 },  {265,1 },  {267,1 },  {268,1 },  {349,1 },  {270,1 },  {304,1 },  {363,1 },  {306,1 },  {529,1 },  {308,1 },  {273,1 },  {276,1 },  {265,1 },  {307,2 },   {309,2 },   {311,2 },  {530,2 },   {266,2 },  {364,2 } };
				else
					buffs = new int[][]{ {1204, 2 }, {1040, 3 }, {1068, 3 }, {1035, 4}, {1045, 6 }, {1048, 6 }, {1036, 2 }, {1259,4 }, {1086,2 }, {1388,3 }, {1397,3 }, {1087,3 }, {1352,2 }, {1364,1 }, {1250,3 }, {4699,13 }, {1357,1 }, {1353,2 }, {1304,3 }, { 1392,3 }, {1393,3 }, {1077,3 },  {1242,3 }, {825,1 }, {828,1 },  {1415,1 }, {1461,1 }, {1416,1 }, {1310,4 }, {264,1 }, {265,1 }, {267,1 }, {268,1 }, {269,1 }, {349,1 },  {270,1 }, {304,1 }, {306,1 }, {529,1 }, {308,1 },  {271,1 },  {272,1 },  {310,1 }, {275,1 },  {274,1 },  {307,2 },  {309,2 },  {311,2 },  {530,2 },  {266,2 },  {364,2 } };
		}
		
		try
		{
			for(int skills[] : buffs)
				SkillTable.getInstance().getInfo(skills[0], skills[1]).getEffects(player, player, false, false, Config.BUFFS_TIME * 60000, false);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getBuffLevel(int skillId)
	{
		return SchemeBufferManager._skillLevels.get(skillId);
	}
	
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
}
