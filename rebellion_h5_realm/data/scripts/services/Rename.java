package services;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.database.mysql;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SubClass;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.entity.events.impl.SiegeEvent;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.SubUnit;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class Rename extends Functions
{
	public void rename_page()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.SERVICES_CHANGE_NICK_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		String htmlrename = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/changenickname.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_NICK_ITEM).getName();
		String cost = Util.formatAdena(Config.SERVICES_CHANGE_NICK_PRICE);
		
		htmlrename = htmlrename.replace("%item%", itemName);
		htmlrename = htmlrename.replace("%cost%", cost);
		htmlrename = htmlrename.replace("%playerName%", player.getName());
		
		show(htmlrename, player);
	}

	public void changesex_page()
	{
		Player player = getSelf();
		if(player == null)
			return;
		
		if (!Config.SERVICES_CHANGE_SEX_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		if(!player.isInPeaceZone())
		{
			show(player.isLangRus() ? "Вы должны быть в зону мира, чтобы воспользоваться этой услугой." : "You must be in peace zone to use this service.", player);
			return;
		}

		String htmlsex = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/changesex.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_SEX_ITEM).getName();
		String cost = Util.formatAdena(Config.SERVICES_CHANGE_SEX_PRICE);
		
		htmlsex = htmlsex.replace("%item%", itemName);
		htmlsex = htmlsex.replace("%cost%", cost);
		htmlsex = htmlsex.replace("%playerName%", player.getName());
		htmlsex = htmlsex.replace("%playersex%", player.getSex() == 1 ? "male" : "female");
		
		show(htmlsex, player);
	}

	public void separate_page()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.SERVICES_SEPARATE_SUB_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		if(player.isHero())
		{
			show(player.isLangRus() ? "Не доступно для героев." : "Not available for characters.", player);
			return;
		}

		if(player.getSubClasses().size() == 1)
		{
			show(player.isLangRus() ? "Вы должны иметь хотя бы 1 подкласс." : "You must have at least one subclass.", player);
			return;
		}

		if(!player.getActiveClass().isBase())
		{
			show(player.isLangRus() ? "Вы должны быть на основным классе." : "You must be on the base class.", player);
			return;
		}

		if(player.getActiveClass().getLevel() < 75)
		{
			show(player.isLangRus() ? "Вы должны иметь 75 уровень саб класса." : "You must have sub-class level 75.", player);
			return;
		}

		String append_ru = "Отделение Сабкласса:";
		append_ru += "<br>";
		append_ru += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.Separate.Price", player).addString(Util.formatAdena(Config.SERVICES_SEPARATE_SUB_PRICE)).addItemName(Config.SERVICES_SEPARATE_SUB_ITEM) + "</font>&nbsp;";
		append_ru += "<edit var=\"name\" width=80 height=15 /><br>";
		append_ru += "<table>";
		
		String append_en = "Subclass Separation:";
		append_en += "<br>";
		append_en += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.Separate.Price", player).addString(Util.formatAdena(Config.SERVICES_SEPARATE_SUB_PRICE)).addItemName(Config.SERVICES_SEPARATE_SUB_ITEM) + "</font>&nbsp;";
		append_en += "<edit var=\"name\" width=80 height=15 /><br>";
		append_en += "<table>";

		for(SubClass s : player.getSubClasses().values())
		{
			if(!s.isBase() && s.getClassId() != ClassId.inspector.getId() && s.getClassId() != ClassId.judicator.getId())
			{
				if(player.isLangRus())
				{
					append_ru += "<tr><td><button value=\"" + new CustomMessage("scripts.services.Separate.Button", player).addString(ClassId.VALUES[s.getClassId()].toString()) + "\" action=\"bypass -h scripts_services.Rename:separate " + s.getClassId() + " $name\" width=200 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
					append_ru += "</table>";
				} else {
					append_en += "<tr><td><button value=\"" + new CustomMessage("scripts.services.Separate.Button", player).addString(ClassId.VALUES[s.getClassId()].toString()) + "\" action=\"bypass -h scripts_services.Rename:separate " + s.getClassId() + " $name\" width=200 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";					
					append_en += "</table>";
				}
			}
		}
		show(player.isLangRus() ? append_ru : append_en, player);
	}

	public void separate(String[] param)
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.SERVICES_SEPARATE_SUB_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		if(player.isHero())
		{
			show(player.isLangRus() ? "Не доступно для героев." : "Not available for Heroes.", player);
			return;
		}

		if(player.getSubClasses().size() == 1)
		{
			show(player.isLangRus() ? "Вы должны иметь хотя бы 1 подкласс." : "You must have at least one subclass.", player);
			return;
		}

		if(!player.getActiveClass().isBase())
		{
			show(player.isLangRus() ? "Вы должны быть на основным классе." : "You must be on the base class.", player);
			return;
		}

		if(player.getActiveClass().getLevel() < 75)
		{
			show(player.isLangRus() ? "Вы должны иметь 75 уровень саб класса." : "You must have sub-class level 75.", player);
			return;
		}

		if(param.length < 2)
		{
			show(player.isLangRus() ? "Вы должны указать цель." : "You must specify the target.", player);
			return;
		}

		if(getItemCount(player, Config.SERVICES_SEPARATE_SUB_ITEM) < Config.SERVICES_SEPARATE_SUB_PRICE)
		{
			if(Config.SERVICES_SEPARATE_SUB_ITEM == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		int classtomove = Integer.parseInt(param[0]);
		int newcharid = 0;
		for(Entry<Integer, String> e : player.getAccountChars().entrySet())
			if(e.getValue().equalsIgnoreCase(param[1]))
				newcharid = e.getKey();

		if(newcharid == 0)
		{
			show(player.isLangRus() ? "Цель не существует." : "The goal does not exist.", player);
			return;
		}

		if(mysql.simple_get_int("level", "character_subclasses", "char_obj_id=" + newcharid + " AND level > 1") > 1)
		{
			show(player.isLangRus() ? "Цель должна иметь уровень 1." : "The aim should be level 1.", player);
			return;
		}

		mysql.set("DELETE FROM character_subclasses WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_skills WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_skills_save WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_effects_save WHERE object_id=" + newcharid);
		mysql.set("DELETE FROM character_hennas WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_shortcuts WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_variables WHERE obj_id=" + newcharid);

		mysql.set("UPDATE character_subclasses SET char_obj_id=" + newcharid + ", isBase=1, certification=0 WHERE char_obj_id=" + player.getObjectId() + " AND class_id=" + classtomove);
		mysql.set("UPDATE character_skills SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_skills_save SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_effects_save SET object_id=" + newcharid + " WHERE object_id=" + player.getObjectId() + " AND id=" + classtomove);
		mysql.set("UPDATE character_hennas SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_shortcuts SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);

		mysql.set("UPDATE character_variables SET obj_id=" + newcharid + " WHERE obj_id=" + player.getObjectId() + " AND name like 'TransferSkills%'");

		player.modifySubClass(classtomove, 0);

		removeItem(player, Config.SERVICES_CHANGE_BASE_ITEM, Config.SERVICES_CHANGE_BASE_PRICE);
		player.logout();
		Log.addDonation("Character " + player + " base changed to " + player, "services");
	}

	public void changebase_page()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.SERVICES_CHANGE_BASE_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		if(!player.isInPeaceZone())
		{
			show(player.isLangRus() ? "Вы должны быть в мирной зоне, чтобы воспользоваться этой услугой." : "You need to be in a peaceful area to use this service.", player);
			return;
		}

		if(player.isHero())
		{
			player.sendMessage(new CustomMessage("scripts.services.rename.not_available", player));
			return;
		}

		String append_ru = "Смена Базового класса:";
		append_ru += "<br>";
		append_ru += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.BaseChange.Price", player).addString(Util.formatAdena(Config.SERVICES_CHANGE_BASE_PRICE)).addItemName(Config.SERVICES_CHANGE_BASE_ITEM) + "</font>";
		append_ru += "<table>";
		
		String append_en = "Changing the base class:";
		append_en += "<br>";
		append_en += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.BaseChange.Price", player).addString(Util.formatAdena(Config.SERVICES_CHANGE_BASE_PRICE)).addItemName(Config.SERVICES_CHANGE_BASE_ITEM) + "</font>";
		append_en += "<table>";
		
		List<SubClass> possible = new ArrayList<SubClass>();
		if(player.getActiveClass().isBase())
		{
			possible.addAll(player.getSubClasses().values());
			possible.remove(player.getSubClasses().get(player.getBaseClassId()));

			for(SubClass s : player.getSubClasses().values())
				for(SubClass s2 : player.getSubClasses().values())
					if(s != s2 && !PlayerClass.areClassesComportable(PlayerClass.values()[s.getClassId()], PlayerClass.values()[s2.getClassId()]) || s2.getLevel() < 75)
						possible.remove(s2);
		}

		if(possible.isEmpty())
		{
			if(player.isLangRus())
			{
				append_ru += "<tr><td width=300>" + new CustomMessage("scripts.services.BaseChange.NotPossible", player) + "</td></tr>";
			}
			else
			{
				append_en += "<tr><td width=300>" + new CustomMessage("scripts.services.BaseChange.NotPossible", player) + "</td></tr>";
			}
		}
		else
		{
			for(SubClass s : possible)
			{
				if(player.isLangRus())
				{
					append_ru += "<tr><td><button value=\"" + new CustomMessage("scripts.services.BaseChange.Button", player).addString(ClassId.VALUES[s.getClassId()].toString()) + "\" action=\"bypass -h scripts_services.Rename:changebase " + s.getClassId() + "\" width=200 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
					append_ru += "</table>";
				}
				else
				{
					append_en += "<tr><td><button value=\"" + new CustomMessage("scripts.services.BaseChange.Button", player).addString(ClassId.VALUES[s.getClassId()].toString()) + "\" action=\"bypass -h scripts_services.Rename:changebase " + s.getClassId() + "\" width=200 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
					append_en += "</table>";
				}
			}
		}
		show(player.isLangRus() ? append_ru : append_en, player);
	}

	public void changebase(String[] param)
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.SERVICES_CHANGE_BASE_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		if(!player.isInPeaceZone())
		{
			show(player.isLangRus() ? "Вы должны быть в мирной зоне, чтобы воспользоваться этой услугой." : "You need to be in a peaceful area to use this service.", player);
			return;
		}

		if(!player.getActiveClass().isBase())
		{
			show(player.isLangRus() ? "Вы должны быть на основном классе, чтобы воспользоваться этой услугой." : "You must be on the main class to use this service.", player);
			return;
		}

		if(player.isHero())
		{
			show(player.isLangRus() ? "Не доступно для героев." : "Not available for characters.", player);
			return;
		}

		if(getItemCount(player, Config.SERVICES_CHANGE_BASE_ITEM) < Config.SERVICES_CHANGE_BASE_PRICE)
		{
			if(Config.SERVICES_CHANGE_BASE_ITEM == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		int target = Integer.parseInt(param[0]);
		SubClass newBase = player.getSubClasses().get(target);

		player.getActiveClass().setBase(false);
		player.getActiveClass().setCertification(newBase.getCertification());

		newBase.setCertification(0);
		player.getActiveClass().setExp(player.getExp());
		player.checkSkills();

		newBase.setBase(true);

		player.setBaseClass(target);

		player.setHairColor(0);
		player.setHairStyle(0);
		player.setFace(0);
		Olympiad.unRegisterNoble(player);
		removeItem(player, Config.SERVICES_CHANGE_BASE_ITEM, Config.SERVICES_CHANGE_BASE_PRICE);
		player.logout();
		//Log.addGame("Character " + player + " base changed to " + target, "services");
	}

	public void rename(String[] args)
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.SERVICES_CHANGE_NICK_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		/*
		if(player.isHero())
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Not available for Heroes");
			return;
		}
		*/

		if(args.length != 1)
		{
			show(new CustomMessage("scripts.services.Rename.incorrectinput", player), player);
			return;
		}

		if(player.getEvent(SiegeEvent.class) != null)
		{
			show(new CustomMessage("scripts.services.Rename.SiegeNow", player), player);
			return;
		}

		String name = args[0];
		if(!Util.isMatchingRegexp(name, Config.CNAME_TEMPLATE) && !Config.SERVICES_CHANGE_NICK_ALLOW_SYMBOL)
		{
			show(new CustomMessage("scripts.services.Rename.incorrectinput", player), player);
			return;
		}

		if(getItemCount(player, Config.SERVICES_CHANGE_NICK_ITEM) < Config.SERVICES_CHANGE_NICK_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}

		if(CharacterDAO.getInstance().getObjectIdByName(name) > 0)
		{
			show(new CustomMessage("scripts.services.Rename.Thisnamealreadyexists", player), player);
			return;
		}

		removeItem(player, Config.SERVICES_CHANGE_NICK_ITEM, Config.SERVICES_CHANGE_NICK_PRICE);

		String oldName = player.getName();
		player.reName(name, true);
		Log.addDonation("Character " + oldName + " renamed to " + name, "renames");
		player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Your nick was changed from " + oldName + " to " + name);
	}

	public void changesex()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.SERVICES_CHANGE_SEX_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		if(player.getRace() == Race.kamael)
		{
			show(player.isLangRus() ? "Не доступно для Kamael." : "Not available for Kamael.", player);
			return;
		}

		if(!player.isInPeaceZone())
		{
			show(player.isLangRus() ? "Вы должны быть в мирной зоне, чтобы воспользоваться этой услугой." : "You need to be in a peaceful area to use this service.", player);
			return;
		}

		if(getItemCount(player, Config.SERVICES_CHANGE_SEX_ITEM) < Config.SERVICES_CHANGE_SEX_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}

		Connection con = null;
		PreparedStatement offline = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("UPDATE characters SET sex = ? WHERE obj_Id = ?");
			offline.setInt(1, player.getSex() == 1 ? 0 : 1);
			offline.setInt(2, player.getObjectId());
			offline.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			show(new CustomMessage("common.Error", player), player);
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, offline);
		}

		player.setHairColor(0);
		player.setHairStyle(0);
		player.setFace(0);
		removeItem(player, Config.SERVICES_CHANGE_SEX_ITEM, Config.SERVICES_CHANGE_SEX_PRICE);
		player.logout();
		Log.addDonation("Character " + player + " sex changed to " + (player.getSex() == 1 ? "male" : "female"), "renames");
	}

	public void rename_clan_page()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.SERVICES_CHANGE_CLAN_NAME_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		if(player.getClan() == null)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You dont have a clan!");
			return;
		}
		
		if (!player.isClanLeader())
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You are not a clan leader of clan " + player.getClan().getName());
			return;
		}
		
		String htmlclanrename= HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/services/changeclanname.htm", player);
		
		String itemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_CLAN_NAME_ITEM).getName();
		String cost = Util.formatAdena(Config.SERVICES_CHANGE_CLAN_NAME_PRICE);
		
		htmlclanrename = htmlclanrename.replace("%item%", itemName);
		htmlclanrename = htmlclanrename.replace("%cost%", cost);
		htmlclanrename = htmlclanrename.replace("%playerName%", player.getName());
		
		show(htmlclanrename, player);
	}

	public void rename_clan(String[] param)
	{
		Player player = getSelf();
		if(player == null || param == null || param.length == 0)
			return;
		if (!Config.SERVICES_CHANGE_CLAN_NAME_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}

		if(player.getClan() == null)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You dont have a clan!");
			return;
		}
		
		if (!player.isClanLeader())
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "You are not a clan leader of clan " + player.getClan().getName());
			return;
		}
		
		if(player.getEvent(SiegeEvent.class) != null)
		{
			show(new CustomMessage("scripts.services.Rename.SiegeNow", player), player);
			return;
		}

		if(!Util.isMatchingRegexp(param[0], Config.CLAN_NAME_TEMPLATE))
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "This name is invalid.");
			return;
		}
		if(ClanTable.getInstance().getClanByName(param[0]) != null)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "This name already exists.");
			return;
		}

		if(getItemCount(player, Config.SERVICES_CHANGE_CLAN_NAME_ITEM) < Config.SERVICES_CHANGE_CLAN_NAME_PRICE)
		{
			player.sendChatMessage(player.getObjectId(), ChatType.PARTY.ordinal(), "Service", "Lack of items to use this function.");
			return;
		}

		show(new CustomMessage("scripts.services.Rename.changedname", player).addString(player.getClan().getName()).addString(param[0]), player);
		Log.addDonation("Character " + player + " changed clan name from " + player.getClan().getName() + " to " + param[0], "clanrename");
		SubUnit sub = player.getClan().getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
		sub.setName(param[0], true);

		removeItem(player, Config.SERVICES_CHANGE_CLAN_NAME_ITEM, Config.SERVICES_CHANGE_CLAN_NAME_PRICE);
		player.getClan().broadcastClanStatus(true, true, false);
	}
}