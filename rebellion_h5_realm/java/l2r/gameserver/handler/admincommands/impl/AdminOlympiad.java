package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.StatsSet;

import java.util.ArrayList;
import java.util.List;


public class AdminOlympiad implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_oly_save,
		admin_add_oly_points,
		admin_remove_oly_points,
		admin_oly_start,
		admin_add_hero,
		admin_oly_stop,
		admin_oly_end,
		admin_oly_clean_list,
		admin_oly_remove_from_list
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_oly_save:
			{
				if(!Config.ENABLE_OLYMPIAD)
					return false;

				try
				{
					OlympiadDatabase.save();
				}
				catch(Exception e)
				{

				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message1", activeChar));
				break;
			}
			case admin_add_oly_points:
			{
				if(wordList.length < 3)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message2", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message3", activeChar));
					return false;
				}

				Player player = World.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message4", activeChar, wordList[1]));
					return false;
				}

				int pointToAdd;

				try
				{
					pointToAdd = Integer.parseInt(wordList[2]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message5", activeChar));
					return false;
				}

				int curPoints = Olympiad.getNoblePoints(player.getObjectId());
				Olympiad.manualSetNoblePoints(player.getObjectId(), curPoints + pointToAdd);
				int newPoints = Olympiad.getNoblePoints(player.getObjectId());

				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message6", activeChar, pointToAdd, player.getName()));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message7", activeChar, curPoints, newPoints));
				break;
			}
			case admin_remove_oly_points:
			{
				if(wordList.length < 3)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message2", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message3", activeChar));
					return false;
				}

				Player player = World.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message4", activeChar).addString(wordList[1]));
					return false;
				}

				int pointsToRemove;

				try
				{
					pointsToRemove = Integer.parseInt(wordList[2]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message5", activeChar));
					return false;
				}

				int curPoints = Olympiad.getNoblePoints(player.getObjectId());
				Olympiad.manualSetNoblePoints(player.getObjectId(), Math.max(0, curPoints - pointsToRemove));
				int newPoints = Olympiad.getNoblePoints(player.getObjectId());

				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message13", activeChar).addNumber(pointsToRemove).addString(player.getName()));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message7", activeChar).addNumber(curPoints).addNumber(newPoints));
				break;
			}
			case admin_oly_start:
			{
				Olympiad._manager = new OlympiadManager();
				Olympiad._inCompPeriod = true;

				new Thread(Olympiad._manager).start();

				Announcements.getInstance().announceToAll(new SystemMessage2(SystemMsg.THE_OLYMPIAD_GAME_HAS_STARTED));
				break;
			}
			case admin_oly_stop:
			{
				Olympiad._inCompPeriod = false;
				Announcements.getInstance().announceToAll(new SystemMessage2(SystemMsg.THE_OLYMPIAD_GAME_HAS_ENDED));
				try
				{
					OlympiadDatabase.save();
				}
				catch(Exception e)
				{

				}

				break;
			}
			case admin_add_hero:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message8", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message9", activeChar));
					return false;
				}

				Player player = World.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message10", activeChar, wordList[1]));
					return false;
				}

				StatsSet hero = new StatsSet();
				hero.set(Olympiad.CLASS_ID, player.getBaseClassId());
				hero.set(Olympiad.CHAR_ID, player.getObjectId());
				hero.set(Olympiad.CHAR_NAME, player.getName());

				List<StatsSet> heroesToBe = new ArrayList<StatsSet>();
				heroesToBe.add(hero);

				Hero.getInstance().computeNewHeroes(heroesToBe);

				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message11", activeChar, player.getName()));
				break;
			}
			case admin_oly_end:
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message12", activeChar));
				OlympiadDatabase.sortHerosToBe();
				OlympiadDatabase.saveNobleData();
				if(Hero.getInstance().computeNewHeroes(Olympiad._heroesToBe))
					Olympiad._log.warn("Olympiad: Error while computing new heroes!");
				Announcements.getInstance().announceToAll("Olympiad Validation Period has ended.");
				Olympiad._period = 0;
				Olympiad._currentCycle++;
				OlympiadDatabase.cleanupNobles();
				OlympiadDatabase.loadNoblesRank();
				OlympiadDatabase.setNewOlympiadEnd();
				Olympiad.init();
				OlympiadDatabase.save();
				break;
			}
			case admin_oly_clean_list:
			{
				Olympiad.getOlyHwidList().clear();
				Olympiad.getOlyIPList().clear();
				activeChar.sendMessage("Hwid & IP list restriction has been cleaned!");
				break;
			}
			case admin_oly_remove_from_list:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message2", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message3", activeChar));
					return false;
				}

				Player player = World.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message4", activeChar).addString(wordList[1]));
					return false;
				}
				
				List<String> listIP = Olympiad.getOlyIPList();
				List<String> listHWID = Olympiad.getOlyHwidList();
				
				if (listIP.contains(player.getIP()))
				{
					listIP.remove(player.getIP());
					activeChar.sendMessage("Removed player IP from the list.");
				}
				
				if (player.hasHWID() && listHWID.contains(player.getHWID()))
				{
					listHWID.remove(player.getHWID());
					activeChar.sendMessage("Removed player HWID from the list.");
				}
				break;
			}
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}