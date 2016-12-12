package l2r.gameserver.handler.admincommands.impl;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.util.Base64;
import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.mysql;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SubClass;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.SummonInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.ChangePassword;
import l2r.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.randoms.GeoLocation;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.StringUtil;
import l2r.gameserver.utils.Util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

@SuppressWarnings("unused")
public class AdminEditChar implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_edit_character,
		admin_character_actions,
		admin_current_player,
		admin_nokarma,
		admin_setkarma,
		admin_character_list,
		admin_show_characters,
		admin_find_character,
		admin_save_modifications,
		admin_rec,
		admin_settitle,
		admin_setclass,
		admin_setname,
		admin_setsex,
		admin_setcolor,
		admin_add_exp_sp_to_character,
		admin_add_exp_sp,
		admin_sethero,
		admin_setnoble,
		admin_trans,
		admin_setsubclass,
		admin_setfame,
		admin_addfame,
		admin_setbday,
		admin_give_item,
		admin_add_bang,
		admin_set_bang,
		admin_setpassword,
		admin_setsecuritypassword,
		admin_getaccount,
		admin_find_ip,
		admin_find_hwid,
		admin_find_char_acc,
		admin_sealcharacter,
		admin_unsealcharacter,
		admin_partyinfo,
		admin_unsummon,
		admin_setvar,
		admin_unsetvar
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

			if(fullString.startsWith("admin_settitle"))
				try
				{
					String val = fullString.substring(15);
					GameObject target = activeChar.getTarget();
					Player player = null;
					if(target == null)
						return false;
					if(target.isPlayer())
					{
						player = (Player) target;
						player.setTitle(val);
						player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message1", player));
						player.sendChanges();
					}
					else if(target.isNpc())
					{
						((NpcInstance) target).setTitle(val);
						target.decayMe();
						target.spawnMe();
					}

					return true;
				}
				catch(StringIndexOutOfBoundsException e)
				{ // Case of empty character title
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message2", activeChar));
					return false;
				}
			else if(fullString.startsWith("admin_setclass"))
				try
				{
					String val = fullString.substring(15);
					int id = Integer.parseInt(val.trim());
					GameObject target = activeChar.getTarget();

					if(target == null || !target.isPlayer())
						target = activeChar;
					if(id > 136)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message3", activeChar));
						return false;
					}
					Player player = target.getPlayer();
					player.setClassId(id, true, false);
					player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message4", player));
					player.broadcastCharInfo();

					return true;
				}
				catch(StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message5", activeChar));
					return false;
				}
			else if(fullString.startsWith("admin_setname"))
				try
				{
					String val = fullString.substring(14);
					GameObject target = activeChar.getTarget();
					Player player;
					if(target != null && target.isPlayer())
						player = (Player) target;
					else
						return false;
					if(mysql.simple_get_int("count(*)", "characters", "`char_name` like '" + val + "'") > 0)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message6", activeChar));
						return false;
					}
					Log.addGame("Character " + player.getName() + " renamed to " + val + " by GM " + activeChar.getName(), "renames");
					player.reName(val);
					player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message7", player));
					return true;
				}
				catch(StringIndexOutOfBoundsException e)
				{ // Case of empty character name
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message8", activeChar));
					return false;
				}

		if(fullString.equals("admin_current_player"))
			showCharacterList(activeChar, null);
		else if(fullString.startsWith("admin_character_list"))
			try
			{
				String val = fullString.substring(21);
				Player target = GameObjectsStorage.getPlayer(val);
				showCharacterList(activeChar, target);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
			}
		else if(fullString.startsWith("admin_show_characters"))
			try
			{
				String val = fullString.substring(22);
				int page = Integer.parseInt(val);
				listCharacters(activeChar, page);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// Case of empty page
			}
		else if(fullString.startsWith("admin_find_character"))
			try
			{
				String val = fullString.substring(21);
				findCharacter(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message9", activeChar));

				listCharacters(activeChar, 0);
			}
		else if(fullString.equals("admin_edit_character"))
			editCharacter(activeChar, activeChar.getTargetId());
		else if(fullString.equals("admin_character_actions"))
			showCharacterActions(activeChar);
		else if(fullString.equals("admin_nokarma"))
			setTargetKarma(activeChar, 0);
		else if(fullString.startsWith("admin_setkarma"))
			try
			{
				String val = fullString.substring(15);
				int karma = Integer.parseInt(val);
				setTargetKarma(activeChar, karma);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message10", activeChar));
			}
		else if(fullString.startsWith("admin_save_modifications"))
			try
			{
				String val = fullString.substring(24);
				adminModifyCharacter(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message11", activeChar));
				listCharacters(activeChar, 0);
			}
		else if(fullString.equals("admin_rec"))
		{
			GameObject target = activeChar.getTarget();
			Player player = null;
			if(target != null && target.isPlayer())
				player = (Player) target;
			else
				return false;
			player.setRecomHave(player.getRecomHave() + 1);
			player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message12", player));
			player.broadcastCharInfo();
		}
		else if(fullString.startsWith("admin_rec"))
			try
			{
				String val = fullString.substring(10);
				int recVal = Integer.parseInt(val);
				GameObject target = activeChar.getTarget();
				Player player = null;
				if(target != null && target.isPlayer())
					player = (Player) target;
				else
					return false;
				player.setRecomHave(player.getRecomHave() + recVal);
				player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message13", player));
				player.broadcastCharInfo();
			}
			catch(NumberFormatException e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message14", activeChar));
			}
		else if(fullString.startsWith("admin_sethero"))
		{
			// Статус меняется только на текущую логон сессию
			GameObject target = activeChar.getTarget();
			Player player;
			if(wordList.length > 1 && wordList[1] != null)
			{
				player = GameObjectsStorage.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message15", activeChar, wordList[1]));
					return false;
				}
			}
			else if(target != null && target.isPlayer())
				player = (Player) target;
			else
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message16", activeChar));
				return false;
			}

			if(player.isHero())
			{
				player.setHero(false);
				player.updatePledgeClass();
				player.removeSkill(SkillTable.getInstance().getInfo(395, 1));
				player.removeSkill(SkillTable.getInstance().getInfo(396, 1));
				player.removeSkill(SkillTable.getInstance().getInfo(1374, 1));
				player.removeSkill(SkillTable.getInstance().getInfo(1375, 1));
				player.removeSkill(SkillTable.getInstance().getInfo(1376, 1));
			}
			else
			{
				player.setHero(true);
				player.updatePledgeClass();
				player.addSkill(SkillTable.getInstance().getInfo(395, 1));
				player.addSkill(SkillTable.getInstance().getInfo(396, 1));
				player.addSkill(SkillTable.getInstance().getInfo(1374, 1));
				player.addSkill(SkillTable.getInstance().getInfo(1375, 1));
				player.addSkill(SkillTable.getInstance().getInfo(1376, 1));
			}

			player.sendPacket(new SkillList(player));

			player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message17", player));
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_setnoble"))
		{
			// Статус сохраняется в базе
			GameObject target = activeChar.getTarget();
			Player player;
			if(wordList.length > 1 && wordList[1] != null)
			{
				player = GameObjectsStorage.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message18", activeChar, wordList[1]));
					return false;
				}
			}
			else if(target != null && target.isPlayer())
				player = (Player) target;
			else
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message19", activeChar));
				return false;
			}

			if(player.isNoble())
			{
				Olympiad.removeNoble(player);
				player.setNoble(false);
				player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message20", player));
			}
			else
			{
				Olympiad.addNoble(player);
				player.setNoble(true);
				player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message21", player));
			}

			player.updatePledgeClass();
			player.updateNobleSkills();
			player.sendPacket(new SkillList(player));
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_setsex"))
		{
			GameObject target = activeChar.getTarget();
			Player player = null;
			if(target != null && target.isPlayer())
				player = (Player) target;
			else
				return false;
			player.changeSex();
			player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message22", player));
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_setcolor"))
			try
			{
				String val = fullString.substring(15);
				GameObject target = activeChar.getTarget();
				Player player = null;
				if(target != null && target.isPlayer())
					player = (Player) target;
				else
					return false;
				player.setNameColor(Integer.decode("0x" + val));
				player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message23", player));
				player.broadcastUserInfo(true);
			}
			catch(Exception e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message24", activeChar));
			}
		else if(fullString.startsWith("admin_add_exp_sp_to_character"))
			addExpSp(activeChar);
		else if(fullString.startsWith("admin_add_exp_sp"))
			try
			{
				final String val = fullString.substring(16).trim();

			//	String[] vals = val.split(" ");
			//	long exp = NumberUtils.toLong(vals[0], 0L);
			//	int sp = vals.length > 1 ? NumberUtils.toInt(vals[1], 0) : 0;

				adminAddExpSp(activeChar, val);
			//	adminAddExpSp(activeChar, exp, sp);
			}
			catch(Exception e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message25", activeChar));
			}
		else if(fullString.startsWith("admin_trans"))
		{
			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				int transformId = 0;
				try
				{
					transformId = Integer.parseInt(st.nextToken());
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message26", activeChar));
					return false;
				}
				if(transformId != 0 && activeChar.getTransformation() != 0)
				{
					activeChar.sendPacket(SystemMsg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
					return false;
				}
				activeChar.setTransformation(transformId);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message27", activeChar));
			}
			else
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message28", activeChar));
		}
		else if(fullString.startsWith("admin_setsubclass"))
		{
			final GameObject target = activeChar.getTarget();
			if(target == null || !target.isPlayer())
			{
				activeChar.sendPacket(SystemMsg.SELECT_TARGET);
				return false;
			}
			final Player player = (Player) target;

			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				int classId = Short.parseShort(st.nextToken());
				if(!player.addSubClass(classId, true, 0))
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message29", activeChar));
					return false;
				}
				player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER); // Transfer to new class.
			}
			else
				setSubclass(activeChar, player);
		}
		else if (fullString.startsWith("admin_setfame"))
			try
			{
				String val = fullString.substring(14);
				int fame = Integer.parseInt(val);
				setTargetFame(activeChar, fame);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message30", activeChar));
			}
		else if (fullString.startsWith("admin_addfame"))
			try
			{
				String val = fullString.substring(14);
				int fame = Integer.parseInt(val);
				addTargetFame(activeChar, fame);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message31", activeChar));
			}
		else if(fullString.startsWith("admin_setbday"))
		{
			String msgUsage = "Usage: //setbday YYYY-MM-DD";
			String date = fullString.substring(14);
			if(date.length() != 10 || !Util.isMatchingRegexp(date, "[0-9]{4}-[0-9]{2}-[0-9]{2}"))
			{
				activeChar.sendMessage(msgUsage);
				return false;
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			try
			{
				dateFormat.parse(date);
			}
			catch(ParseException e)
			{
				activeChar.sendMessage(msgUsage);
			}

			if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message32", activeChar));
				return false;
			}

			if(!mysql.set("update characters set createtime = UNIX_TIMESTAMP('" + date + "') where obj_Id = " + activeChar.getTarget().getObjectId()))
			{
				activeChar.sendMessage(msgUsage);
				return false;
			}

			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message33", activeChar, activeChar.getTarget().getName(), date));
			activeChar.getTarget().getPlayer().sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message34", activeChar, date));
		}
		else if(fullString.startsWith("admin_give_item"))
		{
			if(wordList.length < 3)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message35", activeChar));
				return false;
			}
			int id = Integer.parseInt(wordList[1]);
			int count = Integer.parseInt(wordList[2]);
			if(id < 1 || count < 1 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message36", activeChar));
				return false;
			}
			ItemFunctions.addItem(activeChar.getTarget().getPlayer(), id, count, true);
		}
		else if(fullString.startsWith("admin_add_bang"))
		{
			if(!Config.PCBANG_POINTS_ENABLED)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message37", activeChar));
				return true;
			}
			if(wordList.length < 1)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message38", activeChar));
				return false;
			}
			int count = Integer.parseInt(wordList[1]);
			if(count < 1 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message39", activeChar));
				return false;
			}
			Player target = activeChar.getTarget().getPlayer();
			target.addPcBangPoints(count, false);
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message40", activeChar, count, target.getName()));
		}
		else if(fullString.startsWith("admin_set_bang"))
		{
			if(!Config.PCBANG_POINTS_ENABLED)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message41", activeChar));
				return true;
			}
			if(wordList.length < 1)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message42", activeChar));
				return false;
			}
			int count = Integer.parseInt(wordList[1]);
			if(count < 1 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message43", activeChar));
				return false;
			}
			Player target = activeChar.getTarget().getPlayer();
			target.setPcBangPoints(count);
			target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message44", target, count));
			target.sendPacket(new ExPCCafePointInfo(target, count, 1, 2, 12));
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message45", activeChar, count));
		}
		else if(fullString.startsWith("admin_setpassword"))
		{
			if(wordList.length != 3)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message46", activeChar));
				return false;
			}

			AuthServerCommunication.getInstance().sendPacket(new ChangePassword(wordList[1], "", wordList[2], "null"));
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message47", activeChar, wordList[1], wordList[2]));
		}
		else if(fullString.startsWith("admin_setsecuritypassword"))
		{
			String newpass;
			
			if(wordList.length != 3)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message48", activeChar));
				return false;
			}
			
			if (wordList.length == 1)
				newpass = "";
			else
				newpass = wordList[1];
			
			String charName = wordList[2];
			int charid = CharacterDAO.getInstance().getObjectIdByName(charName);
			
			if (charid == 0)
			{
				activeChar.sendMessage("Cannot find player with name: " + charName);
				return false;
			}
			
			Player plr = World.getPlayer(charid);
			if (plr != null)
			{
				if (newpass.isEmpty())
				{
					plr.setSecurityPassword(null);
					plr.saveSecurity();
					activeChar.sendMessage("You have removed security pass for " + plr.getName());
				}
				else
				{
					try
					{
						String newpassEnc = "";
						if (!newpass.isEmpty())
						{
							byte[] raw = newpass.getBytes("UTF-8");
							raw = MessageDigest.getInstance("SHA").digest(raw);
							newpassEnc = Base64.encodeBytes(raw);
						}
						
						plr.setSecurityPassword(newpassEnc);
						plr.saveSecurity();
						
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message49", activeChar, charName, newpass));
					}
					catch (UnsupportedEncodingException e) {e.printStackTrace();}
					catch (NoSuchAlgorithmException e) {e.printStackTrace();}
				}
			}
			else
			{
				try
				{
					String newpassEnc = "";
					if (!newpass.isEmpty())
					{
						byte[] raw = newpass.getBytes("UTF-8");
						raw = MessageDigest.getInstance("SHA").digest(raw);
						newpassEnc = Base64.encodeBytes(raw);
					}
					
					CharacterDAO.getInstance().setSecurity(charid, newpassEnc);
					
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message49", activeChar, charName, newpass));
				}
				catch (UnsupportedEncodingException e) {e.printStackTrace();}
				catch (NoSuchAlgorithmException e) {e.printStackTrace();}
			}
		}
		else if(fullString.startsWith("admin_getaccount"))
		{
			if(wordList.length != 2)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message50", activeChar));
				return false;
			}

			String accountName = CharacterDAO.getInstance().getAccountName(wordList[1]);
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message51", activeChar, wordList[1], accountName));
		}
		else if(fullString.startsWith("admin_find_ip"))
		{
			if(wordList.length != 2)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message52", activeChar));
				return false;
			}

			findCharactersPerIp(activeChar, wordList[1]);
		}
		else if(fullString.startsWith("admin_find_hwid"))
		{
			if(wordList.length != 2)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message53", activeChar));
				return false;
			}

			findCharactersPerHwid(activeChar, wordList[1]);
		}
		else if(fullString.startsWith("admin_find_char_acc"))
		{
			if(wordList.length != 2)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message54", activeChar));
				return false;
			}

			findCharactersPerAccount(activeChar, wordList[1]);
		}
		else if(fullString.startsWith("admin_sealcharacter"))
		{
			if(wordList.length != 2)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message55", activeChar));
				return false;
			}

			Player player = World.getPlayer(wordList[1]);
			if (player != null && !player.isInOfflineMode())
			{
				for (ItemInstance item : player.getInventory().getItems())
				{
					if (item != null && item.getCustomFlags() == 0 && item.canbeSealed(player))
					{
						item.setCustomFlags(ItemInstance.FLAG_NO_DESTROY | ItemInstance.FLAG_NO_TRADE | ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_TRANSFER);
						player.sendPacket(new InventoryUpdate().addModifiedItem(item));
						player.broadcastUserInfo(true);
						if(item.getJdbcState().isSavable())
						{
							item.save();		
						}
						else
						{
							item.setJdbcState(JdbcEntityState.UPDATED);
							item.update();
						}
					}
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message56", activeChar));
			}
			else
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message57", activeChar));
				showCharacterList(activeChar, null);
			}
		}
		else if(fullString.startsWith("admin_unsealcharacter"))
		{
			if(wordList.length != 2)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message58", activeChar));
				return false;
			}

			Player player = World.getPlayer(wordList[1]);
			if (player != null && !player.isInOfflineMode())
			{
				for (ItemInstance item : player.getInventory().getItems())
				{
					if (item != null && item.getCustomFlags() > 0 && item.canbeSealed(player))
					{
						item.setCustomFlags(0);
						player.sendPacket(new InventoryUpdate().addModifiedItem(item));
						player.broadcastUserInfo(true);
						if(item.getJdbcState().isSavable())
						{
							item.save();		
						}
						else
						{
							item.setJdbcState(JdbcEntityState.UPDATED);
							item.update();
						}
					}
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message59", activeChar));
			}
			else
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message60", activeChar));
				showCharacterList(activeChar, null);
			}
		}
		else if(fullString.startsWith("admin_partyinfo"))
		{
			if(wordList.length != 2)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message61", activeChar));
				return false;
			}

			Player player = World.getPlayer(wordList[1]);
			if (player != null && !player.isInOfflineMode())
			{
				if (player.isInParty())
					gatherPartyInfo(player, activeChar);
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message62", activeChar));
					showCharacterList(activeChar, null);
				}
			}
			else
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message63", activeChar));
				showCharacterList(activeChar, null);
			}
		}
		else if (fullString.startsWith("admin_unsummon"))
		{
			if (activeChar.getTarget() == null)
				return false;
			
			GameObject target = activeChar.getTarget();
			if (target != null && target.isPet())
			{
				target.getPlayer().getPet().unSummon();
			}
			else if (target != null && target.isSummon())
			{
				((SummonInstance) target).unSummon();
			}
			else
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message64", activeChar));
				showCharacterList(activeChar, null);
			}
		}
		else if (fullString.startsWith("admin_setvar"))
		{
			if(wordList.length < 2 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message65", activeChar));
				return false;
			}
			
			GameObject target = activeChar.getTarget();

			try
			{
				if (target != null && target.isPlayer())
				{
					if(wordList.length == 2)
						target.getPlayer().setVar(wordList[1], -1);
					else if (wordList.length == 3)
						target.getPlayer().setVar(wordList[1], Integer.parseInt(wordList[2]), -1);
					else if (wordList.length == 4)
						target.getPlayer().setVar(wordList[1], Integer.parseInt(wordList[2]), Long.parseLong(wordList[2]));
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message66", activeChar));
				return false;
			}
			
			if(wordList.length == 2)
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message67", activeChar, wordList[1], target.getPlayer().getName()));
			else if (wordList.length == 3)
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message68", activeChar, wordList[1], wordList[2], target.getPlayer().getName()));
			else if (wordList.length == 4)
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message69", activeChar, wordList[1], wordList[2], wordList[3], target.getPlayer().getName()));
		}
		else if (fullString.startsWith("admin_unsetvar"))
		{
			if(wordList.length != 2 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message70", activeChar));
				return false;
			}
			
			GameObject target = activeChar.getTarget();

			try
			{
				if (target != null && target.isPlayer())
					target.getPlayer().unsetVar(wordList[1]);
			}
			catch(Exception e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message71", activeChar));
				return false;
			}
			
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message72", activeChar, wordList[1], target.getPlayer().getName()));
		}
		
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void listCharacters(Player activeChar, int page)
	{
		List<Player> players = GameObjectsStorage.getAllPlayers();

		int MaxCharactersPerPage = 20;
		int MaxPages = players.size() / MaxCharactersPerPage;

		if(players.size() > MaxCharactersPerPage * MaxPages)
			MaxPages++;

		// Check if number of users changed
		if(page > MaxPages)
			page = MaxPages;

		int CharactersStart = MaxCharactersPerPage * page;
		int CharactersEnd = players.size();
		if(CharactersEnd - CharactersStart > MaxCharactersPerPage)
			CharactersEnd = CharactersStart + MaxCharactersPerPage;

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=270>You can find a character by writing his name and</td></tr>");
		replyMSG.append("<tr><td width=270>clicking Find bellow.<br></td></tr>");
		replyMSG.append("<tr><td width=270>Note: Names should be written case sensitive.</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		replyMSG.append("</td></tr></table></center><br><br>");

		for(int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");
		}
		replyMSG.append("<br>");

		// List Players in a Table
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=80>Name:</td><td width=110>Class:</td><td width=40>Level:</td></tr>");
		for(int i = CharactersStart; i < CharactersEnd; i++)
		{
			Player p = players.get(i);
			replyMSG.append("<tr><td width=80>" + "<a action=\"bypass -h admin_character_list " + p.getName() + "\">" + p.getName() + "</a></td><td width=110>" + p.getTemplate().className + "</td><td width=40>" + p.getLevel() + "</td></tr>");
		}
		replyMSG.append("</table>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public static void showCharacterList(Player activeChar, Player player)
	{
		if(player == null)
		{
			GameObject target = activeChar.getTarget();
			if(target != null && target.isPlayer())
				player = (Player) target;
			else
				return;
		}
		else
			activeChar.setTarget(player);

		String clanName = "No Clan";
		if(player.getClan() != null)
			clanName = player.getClan().getName() + "/" + player.getClan().getLevel();

		NumberFormat df = NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(1);

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table><br>");

		replyMSG.append("<table>");
		replyMSG.append("<tr>");
		replyMSG.append("<td><button value=\"Buffs\" action=\"bypass -h admin_show_effects\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Party\" action=\"bypass -h admin_partyinfo " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Stats\" action=\"bypass -h admin_edit_character\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr><tr>");
		replyMSG.append("<td><button value=\"Tele To\" action=\"bypass -h admin_teleportto " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Recall\" action=\"bypass -h admin_recall_char_menu " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Kick\" action=\"bypass -h admin_kick " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr><tr>");
		replyMSG.append("<td><button value=\"Skills\" action=\"bypass -h admin_show_skills\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Class\" action=\"bypass -h admin_show_html setclass.htm\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Subclass\" action=\"bypass -h admin_show_html charsubclasses.htm\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
	    replyMSG.append("</tr><tr>");
	    replyMSG.append("<td><button value=\"Set Noble\" action=\"bypass -h admin_setnoble\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Set Hero\" action=\"bypass -h admin_sethero\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
	    replyMSG.append("<td><button value=\"Change Sex\" action=\"bypass -h admin_setsex\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr><tr>");
		replyMSG.append("<td><button value=\"Ban Char\" action=\"bypass -h admin_ban " + player.getName() + " -1 menu_ban\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Ban Acc\" action=\"bypass -h admin_accban " + player.getAccountName() +"\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Ban HWID\" action=\"bypass -h admin_hwidban " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<table width=290>");
		
		if (activeChar.getAccessLevel().canViewAccountInfo())
		{
			replyMSG.append("<tr><td width=100>Account/IP: </td><td><a action=\"bypass -h admin_find_char_acc "+ player.getName() +"\">" + player.getAccountName() + "</a> / <a action=\"bypass -h admin_find_ip "+ player.getIP() +"\">" + player.getIP() + "</a></td></tr>");
			replyMSG.append("<tr><td width=100>HWID: </td><td><a action=\"bypass -h admin_find_hwid "+ player.getHWID() +"\">" + (player.hasHWID() ? player.getHWID() : "hwid missing") + "</a></td></tr>");
			replyMSG.append("<tr><td width=100>Country: </td><td>" + GeoLocation.getCountry(player) + " (" + GeoLocation.getCountryCode(player) + ")</td></tr>");
			replyMSG.append("<tr><td width=100>City: </td><td>" + GeoLocation.getCity(player) + " (" + GeoLocation.getCityRegion(player) + ")</td></tr>");
		}
		
		replyMSG.append("<tr><td width=100>Name/Level: </td><td>" + player.getName() + " / " + player.getLevel() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Class/Id: </td><td>" + player.getTemplate().className + " / " + player.getClassId().getId() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Clan/Level: </td><td>" + clanName + "</td></tr>");
		replyMSG.append("<tr><td width=100>Exp/Sp: </td><td>" + player.getExp() + "/" + player.getSp() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Cur/Max Hp: </td><td>" + (int) player.getCurrentHp() + "/" + player.getMaxHp() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Cur/Max Mp: </td><td>" + (int) player.getCurrentMp() + "/" + player.getMaxMp() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Cur/Max Load: </td><td>" + player.getCurrentLoad() + "/" + player.getMaxLoad() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Patk/Matk: </td><td>" + player.getPAtk(null) + "/" + player.getMAtk(null, null) + "</td></tr>");
		replyMSG.append("<tr><td width=100>Pdef/Mdef: </td><td>" + player.getPDef(null) + "/" + player.getMDef(null, null) + "</td></tr>");
		replyMSG.append("<tr><td width=100>PAtkSpd/MAtkSpd: </td><td>" + player.getPAtkSpd() + "/" + player.getMAtkSpd() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Acc/Evas: </td><td>" + player.getAccuracy() + "/" + player.getEvasionRate(null) + "</td></tr>");
		replyMSG.append("<tr><td width=100>Crit/MCrit: </td><td>" + player.getCriticalHit(null, null) + "/" + df.format(player.getMagicCriticalRate(null, null)) + "%</td></tr>");
		replyMSG.append("<tr><td width=100>Walk/Run: </td><td>" + player.getWalkSpeed() + "/" + player.getRunSpeed() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Karma/Fame: </td><td>" + player.getKarma() + "/" + player.getFame() + "</td></tr>");
		replyMSG.append("<tr><td width=100>PvP/PK: </td><td>" + player.getPvpKills() + "/" + player.getPkKills() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Coordinates: </td><td>" + player.getX() + "," + player.getY() + "," + player.getZ() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Direction: </td><td>" + Location.getDirectionTo(player, activeChar) + "</td></tr>");
		replyMSG.append("<tr><td width=100>Premium: </td><td>" + (PremiumAccountsTable.isPremium(activeChar) == true ? "Yes" : "No") + "</td></tr>");
		replyMSG.append("<tr><td width=100>Premium Template: </td><td>" + PremiumAccountsTable.getPremiumAccount(player).getTemplate().name + "</td></tr>");
		replyMSG.append("<tr><td width=100>Premium Expires: </td><td>" + Util.formatTime((int) (PremiumAccountsTable.getPremiumAccount(player).getTimeLeftInMilis() / 1000)) + "</td></tr>");
		replyMSG.append("<tr><td width=100>Uptime: </td><td>" + Util.formatTime((int) player.getUptime() / 1000) + "</td></tr>");
		replyMSG.append("</table><br>");
		
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void setTargetKarma(Player activeChar, int newKarma)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		Player player;
		if(target.isPlayer())
			player = (Player) target;
		else
			return;

		if(newKarma >= 0)
		{
			int oldKarma = player.getKarma();
			player.setKarma(newKarma);

			player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message73", player, oldKarma, newKarma));
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message74", activeChar, player.getName(), oldKarma, newKarma));
		}
		else
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message75", activeChar));
	}

	private void setTargetFame(Player activeChar, int newFame)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		Player player;
		if(target.isPlayer())
			player = (Player) target;
		else
			return;

		if(newFame >= 0)
		{
			int oldFame = player.getFame();
			player.setFame(newFame, "Admin");

			player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message76", player, oldFame, newFame));
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message77", activeChar, player.getName(), oldFame, newFame));
		}
		else
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message78", activeChar));
	}

	private void addTargetFame(Player activeChar, int newFame)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		Player player;
		if(target.isPlayer())
			player = (Player) target;
		else
			return;

		if(newFame >= 0)
		{
			int oldFame = player.getFame();
			player.setFame(oldFame + newFame, "Admin");

			player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message79", player, newFame, (oldFame + newFame)));
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message80", activeChar, newFame, player.getName(), (oldFame + newFame)));
		}
		else
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message81", activeChar));
	}
	
	private void adminModifyCharacter(Player activeChar, String modifications)
	{
		int objId = 0;
		try
		{
			String[] split = modifications.split("%");
			objId = Integer.parseInt(split[1]);
			modifications = split[2];
			
			GameObject target = World.getPlayer(objId);
			if(target == null || !target.isPlayer())
			{
				activeChar.sendPacket(SystemMsg.SELECT_TARGET);
				return;
			}

			Player player = (Player) target;
			String[] strvals = modifications.split("&");
			Integer[] vals = new Integer[strvals.length];
			for(int i = 0; i < strvals.length; i++)
			{
				strvals[i] = strvals[i].trim();
				vals[i] = strvals[i].isEmpty() ? null : Integer.valueOf(strvals[i]);
			}

			if(vals[0] != null)
				player.setCurrentHp(vals[0], false);

			if(vals[1] != null)
				player.setCurrentMp(vals[1]);
			
			if(vals[2] != null)
				player.setCurrentCp(vals[2]);

			if(vals[3] != null)
				player.setKarma(vals[3]);

			if(vals[4] != null)
				player.setPvpFlag(vals[4]);

			if(vals[5] != null)
				player.setPvpKills(vals[5]);

			if(vals[6] != null)
				player.setClassId(vals[6], true, false);
			
			if(vals[7] != null)
				player.setPkKills(vals[7]);
			
			if(vals[8] != null)
				player.setFame(vals[8]);
			
			if(vals[9] != null)
				player.setRecomHave(vals[9]);

			editCharacter(activeChar, objId); // Back to start
			player.broadcastCharInfo();
			player.decayMe();
			player.spawnMe(activeChar.getLoc());
		}
		catch (NumberFormatException | ArrayIndexOutOfBoundsException nfe)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message82", activeChar, nfe.getMessage()));
		}
	}

	private void editCharacter(Player activeChar, int objId)
	{
		GameObject target = World.getPlayer(objId);
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMsg.SELECT_TARGET);
			return;
		}

		Player player = (Player) target;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing character: <font color=LEVEL>" + player.getName() + "</font></center><br>");
		replyMSG.append("<table width=100% cellspacing=5>");
		replyMSG.append("<tr><td><font color=LEVEL>HP:</font> " + (int)player.getCurrentHp() + " (" + player.getMaxHp() + ")</td><td><edit var=\"hp\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>MP:</font> " + (int)player.getCurrentMp() + " (" + player.getMaxMp() + ")</td><td><edit var=\"mp\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>CP:</font> " + (int)player.getCurrentCp() + " (" + player.getMaxCp() + ")</td><td><edit var=\"cp\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>Karma:</font> " + player.getKarma() + "</td><td><edit var=\"karma\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>PvP Flag:</font> " + player.getPvpFlag() + "</td><td><edit var=\"pvpflag\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>PvP Kills:</font> " + player.getPvpKills() + "</td><td><edit var=\"pvpkills\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>PK Kills:</font> " + player.getPkKills() + "</td><td><edit var=\"pkills\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>Fame:</font> " + player.getFame() + "</td><td><edit var=\"fame\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>Rec:</font> " + player.getRecomHave() + "</td><td><edit var=\"rec\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>ClassId:</font> " + player.getClassId() + "(" + player.getClassId().getId() + ")</td><td><edit var=\"classid\" width=50></td></tr>");
		replyMSG.append("<tr><td><font color=LEVEL>Load:</font> " + player.getCurrentLoad() + "(" + player.getMaxLoad() + ")</td><td></td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><button value=\"Save Changes\" action=\"bypass -h admin_save_modifications %" + target.getObjectId() + "% $hp & $mp & $cp & $karma & $pvpflag & $pvpkills & $classid & $pkills & $fame & $rec &\" width=120 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center><br>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showCharacterActions(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		Player player;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
			return;

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table><br><br>");
		replyMSG.append("<center>Admin Actions for: " + player.getName() + "</center><br>");
		replyMSG.append("<center><table width=200><tr>");
		replyMSG.append("<td width=100>Argument(*):</td><td width=100><edit var=\"arg\" width=100></td>");
		replyMSG.append("</tr></table><br></center>");
		replyMSG.append("<table width=270>");

		replyMSG.append("<tr><td width=90><button value=\"Teleport\" action=\"bypass -h admin_teleportto " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=90><button value=\"Recall\" action=\"bypass -h admin_recall " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=90><button value=\"Quests\" action=\"bypass -h admin_quests " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");

		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void findCharacter(Player activeChar, String characterToFind)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		int charactersFound = 0;

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");

		for(Player element : GameObjectsStorage.getAllPlayersForIterate())
			if(element.getName().toLowerCase().contains(characterToFind.toLowerCase()))
			{
				charactersFound++;
				replyMSG.append("<table width=270>");
				replyMSG.append("<tr><td width=80>Name</td><td width=110>Class</td><td width=40>Level</td></tr>");
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + element.getName() + "\">" + element.getName() + "</a></td><td width=110>" + element.getTemplate().className + "</td><td width=40>" + element.getLevel() + "</td></tr>");
				replyMSG.append("</table>");
			}

		if(charactersFound == 0)
		{
			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td width=270>Your search did not find any characters.</td></tr>");
			replyMSG.append("<tr><td width=270>Please try again.<br></td></tr>");
			replyMSG.append("</table><br>");
			replyMSG.append("<center><table><tr><td>");
			replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			replyMSG.append("</td></tr></table></center>");
		}
		else
		{
			replyMSG.append("<center><br>Found " + charactersFound + " character");

			if(charactersFound == 1)
				replyMSG.append(".");
			else if(charactersFound > 1)
				replyMSG.append("s.");
		}

		replyMSG.append("</center></body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void findCharactersPerHwid(Player activeChar, String hwid)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		int CharactersFound = 0;
		
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		
		
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(hwid.equalsIgnoreCase(player.getHWID()))
			{
				CharactersFound += 1;
				replyMSG.append("<table width=270>");
				replyMSG.append("<tr><td width=80>Name</td><td width=110>Class</td><td width=40>Level</td></tr>");
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + player.getName() + "\">" + player.getName() + "</a></td><td width=110>" + player.getTemplate().className + "</td><td width=40>" + player.getLevel() + "</td></tr>");
				replyMSG.append("</table>");	
			}
		}
		
		if (CharactersFound == 0)
		{
			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td width=270>Your search did not find any characters.</td></tr>");
			replyMSG.append("<tr><td width=270>Please try again.<br></td></tr>");
			replyMSG.append("</table><br>");
			replyMSG.append("<center><table><tr><td>");
			replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			replyMSG.append("</td></tr></table></center>");
		}
		else
		{
			replyMSG.append("<center><br>Found " + CharactersFound + " character");
			
			if (CharactersFound == 1)
				replyMSG.append(".");
			else if (CharactersFound > 1)
				replyMSG.append("s.");
		}
		
		replyMSG.append("</center></body></html>");
		
		adminReply.replace("%hwid%", hwid);
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar
	 * @param IpAdress
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerIp(Player activeChar, String IpAdress) throws IllegalArgumentException
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		int CharactersFound = 0;
		
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		
		boolean findDisconnected = false;
		
		if (IpAdress.equals("disconnected"))
		{
			findDisconnected = true;
		}
		else
		{
			if (!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"))
			{
				throw new IllegalArgumentException("Malformed IPv4 number");
			}
		}
		
		String name, ip = "0.0.0.0";
		
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player!= null && !player.isInOfflineMode() && player.getClient() != null && player.getClient().getIpAddr().equalsIgnoreCase(IpAdress))
			{
				CharactersFound = CharactersFound + 1;
				replyMSG.append("<table width=270>");
				replyMSG.append("<tr><td width=80>Name</td><td width=110>Class</td><td width=40>Level</td></tr>");
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + player.getName() + "\">" + player.getName() + "</a></td><td width=110>" + player.getTemplate().className + "</td><td width=40>" + player.getLevel() + "</td></tr>");
				replyMSG.append("</table>");	
			}
		}
		
		if (CharactersFound == 0)
		{
			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td width=270>Your search did not find any characters.</td></tr>");
			replyMSG.append("<tr><td width=270>Please try again.<br></td></tr>");
			replyMSG.append("</table><br>");
			replyMSG.append("<center><table><tr><td>");
			replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			replyMSG.append("</td></tr></table></center>");
		}
		else
		{
			replyMSG.append("<center><br>Found " + CharactersFound + " character");
			
			if (CharactersFound == 1)
				replyMSG.append(".");
			else if (CharactersFound > 1)
				replyMSG.append("s.");
		}
		
		replyMSG.append("</center></body></html>");
		
		adminReply.replace("%ip%", IpAdress);
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void findCharactersPerAccount(Player activeChar, String characterName) throws IllegalArgumentException
	{
		if (characterName.matches(Config.CNAME_TEMPLATE))
		{
			String account = null;
			Map<Integer, String> chars;
			Player player = World.getPlayer(characterName);
			if (player == null)
			{
				throw new IllegalArgumentException("Player doesn't exist");
			}
			chars = player.getAccountChars();
			account = player.getAccountName();
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			
			StringBuilder replyMSG = new StringBuilder("<html><body>");
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			
			for (String charname : chars.values())
			{
				replyMSG.append("<table width=270>");
				replyMSG.append("<tr><td width=150> " + charname  + " <br1></td></tr>");
				replyMSG.append("</table>");
			}
			
			replyMSG.append("</center></body></html>");
			
			adminReply.setHtml(replyMSG.toString());
			
			activeChar.sendPacket(adminReply);
		}
		else
		{
			throw new IllegalArgumentException("Malformed character name");
		}
	}
	
	private void findDualbox(Player activeChar, int multibox)
	{
		Map<String, List<Player>> ipMap = new HashMap<>();
		
		String ip = "0.0.0.0";
		GameClient client;
		
		final Map<String, Integer> dualboxIPs = new HashMap<>();
		
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			client = player.getClient().getConnection().getClient();
			if ((client == null) || client.getActiveChar().isInOfflineMode())
			{
				continue;
			}
			
			ip = client.getIpAddr();
			if (ipMap.get(ip) == null)
			{
				ipMap.put(ip, new ArrayList<Player>());
			}
			ipMap.get(ip).add(player);
			
			if (ipMap.get(ip).size() >= multibox)
			{
				Integer count = dualboxIPs.get(ip);
				if (count == null)
				{
					dualboxIPs.put(ip, multibox);
				}
				else
				{
					dualboxIPs.put(ip, count + 1);
				}
			}
		}
		
		List<String> keys = new ArrayList<>(dualboxIPs.keySet());
		Collections.sort(keys, new Comparator<String>()
		{
			@Override
			public int compare(String left, String right)
			{
				return dualboxIPs.get(left).compareTo(dualboxIPs.get(right));
			}
		});
		Collections.reverse(keys);
		
		final StringBuilder results = new StringBuilder();
		for (String dualboxIP : keys)
		{
			StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP + "\">" + dualboxIP + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		adminReply.replace("%strict%", "");
		activeChar.sendPacket(adminReply);
	}
	
	private void addExpSp(final Player activeChar)
	{
		final GameObject target = activeChar.getTarget();
		Player player;
		if(target != null && target.isPlayer() && activeChar == target)
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table width=270><tr><td>Name: " + player.getName() + "</td></tr>");
		replyMSG.append("<tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr>");
		replyMSG.append("<tr><td>Exp: " + player.getExp() + "</td></tr>");
		replyMSG.append("<tr><td>Sp: " + player.getSp() + "</td></tr></table>");
		replyMSG.append("<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>");
		replyMSG.append("<tr><td>ruin the game...</td></tr></table><br>");
		replyMSG.append("<table width=270><tr><td>Note: Fill all values before saving the modifications.,</td></tr>");
		replyMSG.append("<tr><td>Note: Use 0 if no changes are needed.</td></tr></table><br>");
		replyMSG.append("<center><table><tr>");
		replyMSG.append("<td>Exp: <edit var=\"exp_to_add\" width=50></td>");
		replyMSG.append("<td>Sp:  <edit var=\"sp_to_add\" width=50></td>");
		replyMSG.append("<td>&nbsp;<button value=\"Save Changes\" action=\"bypass -h admin_add_exp_sp $exp_to_add & $sp_to_add &\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table></center>");
		replyMSG.append("<center><table><tr>");
		replyMSG.append("<td>LvL: <edit var=\"lvl\" width=50></td>");
		replyMSG.append("<td>&nbsp;<button value=\"Set Level\" action=\"bypass -h admin_setlevel $lvl\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table></center>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void adminAddExpSp(Player activeChar, final String ExpSp)
	{
		final GameObject target = activeChar.getTarget();
		if(target == null)
		{
			activeChar.sendPacket(SystemMsg.SELECT_TARGET);
			return;
		}

		if(!target.isPlayable())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		Player player = (Player) target;
		String[] strvals = ExpSp.split("&");
		long[] vals = new long[strvals.length];
		for(int i = 0; i < strvals.length; i++)
		{
			strvals[i] = strvals[i].trim();
			vals[i] = strvals[i].isEmpty() ? 0 : Long.parseLong(strvals[i]);
		}

		player.addExpAndSp(vals[0], vals[1], 0, 0, false, false);
		player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message83", player, vals[0], vals[1]));
		activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message84", activeChar, vals[0], vals[1], player.getName()));
	}

	private void setSubclass(final Player activeChar, final Player player)
	{
		StringBuilder content = new StringBuilder("<html><body>");
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		Set<PlayerClass> subsAvailable;
		subsAvailable = getAvailableSubClasses(player);

		if(subsAvailable != null && !subsAvailable.isEmpty())
		{
			content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");

			for(PlayerClass subClass : subsAvailable)
				content.append("<a action=\"bypass -h admin_setsubclass " + subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
		}
		else
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineditchar.message85", activeChar));
			return;
		}
		content.append("</body></html>");
		html.setHtml(content.toString());
		activeChar.sendPacket(html);
	}

	private Set<PlayerClass> getAvailableSubClasses(Player player)
	{
		final int charClassId = player.getBaseClassId();

		PlayerClass currClass = PlayerClass.values()[charClassId];// .valueOf(charClassName);

		/**
		 * If the race of your main class is Elf or Dark Elf, you may not select
		 * each class as a subclass to the other class, and you may not select
		 * Overlord and Warsmith class as a subclass.
		 *
		 * You may not select a similar class as the subclass. The occupations
		 * classified as similar classes are as follows:
		 *
		 * Treasure Hunter, Plainswalker and Abyss Walker Hawkeye, Silver Ranger
		 * and Phantom Ranger Paladin, Dark Avenger, Temple Knight and Shillien
		 * Knight Warlocks, Elemental Summoner and Phantom Summoner Elder and
		 * Shillien Elder Swordsinger and Bladedancer Sorcerer, Spellsinger and
		 * Spellhowler
		 *
		 * Kamael могут брать только сабы Kamael
		 * Другие классы не могут брать сабы Kamael
		 *
		 */
		Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
		if(availSubs == null)
			return null;

		// Из списка сабов удаляем мейн класс игрока
		availSubs.remove(currClass);

		for(PlayerClass availSub : availSubs)
		{
			// Удаляем из списка возможных сабов, уже взятые сабы и их предков
			for(SubClass subClass : player.getSubClasses().values())
			{
				if(availSub.ordinal() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов их родителей, если таковые есть у чара
				ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
				if(parent != null && parent.getId() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов родителей текущих сабклассов, иначе если взять саб berserker
				// и довести до 3ей профы - doombringer, игроку будет предложен berserker вновь (дежавю)
				ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
				if(subParent != null && subParent.getId() == availSub.ordinal())
					availSubs.remove(availSub);
			}

			// Особенности саб классов камаэль
			if(availSub.isOfRace(Race.kamael))
			{
				// Для Soulbreaker-а и SoulHound не предлагаем Soulbreaker-а другого пола
				if((currClass == PlayerClass.MaleSoulHound || currClass == PlayerClass.FemaleSoulHound || currClass == PlayerClass.FemaleSoulbreaker || currClass == PlayerClass.MaleSoulbreaker) && (availSub == PlayerClass.FemaleSoulbreaker || availSub == PlayerClass.MaleSoulbreaker))
					availSubs.remove(availSub);

				// Для Berserker(doombringer) и Arbalester(trickster) предлагаем Soulbreaker-а только своего пола
				if(currClass == PlayerClass.Berserker || currClass == PlayerClass.Doombringer || currClass == PlayerClass.Arbalester || currClass == PlayerClass.Trickster)
					if(player.getSex() == 1 && availSub == PlayerClass.MaleSoulbreaker || player.getSex() == 0 && availSub == PlayerClass.FemaleSoulbreaker)
						availSubs.remove(availSub);

				// Inspector доступен, только когда вкачаны 2 возможных первых саба камаэль(+ мейн класс):
				// doombringer(berserker), soulhound(maleSoulbreaker, femaleSoulbreaker), trickster(arbalester)
				if(availSub == PlayerClass.Inspector)
					// doombringer(berserker)
					if(!(player.getSubClasses().containsKey(131) || player.getSubClasses().containsKey(127)))
						availSubs.remove(availSub);
					// soulhound(maleSoulbreaker, femaleSoulbreaker)
					else if(!(player.getSubClasses().containsKey(132) || player.getSubClasses().containsKey(133) || player.getSubClasses().containsKey(128) || player.getSubClasses().containsKey(129)))
						availSubs.remove(availSub);
					// trickster(arbalester)
					else if(!(player.getSubClasses().containsKey(134) || player.getSubClasses().containsKey(130)))
						availSubs.remove(availSub);
			}
		}
		return availSubs;
	}

	private String formatClassForDisplay(PlayerClass className)
	{
		String classNameStr = className.toString();
		char[] charArray = classNameStr.toCharArray();

		for(int i = 1; i < charArray.length; i++)
			if(Character.isUpperCase(charArray[i]))
				classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);

		return classNameStr;
	}
	
	private void gatherPartyInfo(Player playerParty, Player gm)
	{
		boolean color = true;
		String html = HtmCache.getInstance().getNotNull("admin/partyinfo.htm", gm);
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder text = new StringBuilder(400);
		for (Player member : playerParty.getParty())
		{
			if (color)
			{
				text.append("<tr><td><table width=270 border=0 bgcolor=131210 cellpadding=2><tr><td width=30 align=right>");
			}
			else
			{
				text.append("<tr><td><table width=270 border=0 cellpadding=2><tr><td width=30 align=right>");
			}
			text.append(member.getLevel() + "</td><td width=130><a action=\"bypass -h admin_character_list " + member.getName() + "\">" + member.getName() + "</a>");
			text.append("</td><td width=110 align=right>" + member.getClassId().toString() + "</td></tr></table></td></tr>");
			color = !color;
		}
		
		adminReply.setHtml(html);
		adminReply.replace("%player%", playerParty.getName());
		adminReply.replace("%party%", text.toString());
		gm.sendPacket(adminReply);
	}
}