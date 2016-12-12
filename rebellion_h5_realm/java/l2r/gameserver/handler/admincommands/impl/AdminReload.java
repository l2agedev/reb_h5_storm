package l2r.gameserver.handler.admincommands.impl;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.achievements.Achievements;
import l2r.gameserver.cache.ImagesChache;
import l2r.gameserver.dao.EmotionsTable;
import l2r.gameserver.dao.OlympiadNobleDAO;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.StringHolder;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.BuyListHolder;
import l2r.gameserver.data.xml.holder.MultiSellHolder;
import l2r.gameserver.data.xml.holder.ProductHolder;
import l2r.gameserver.data.xml.parser.FishParser;
import l2r.gameserver.data.xml.parser.InstantZoneParser;
import l2r.gameserver.data.xml.parser.ItemParser;
import l2r.gameserver.data.xml.parser.NpcParser;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.AutoAnnounce;
import l2r.gameserver.instancemanager.BetaServer;
import l2r.gameserver.instancemanager.SpawnManager;
import l2r.gameserver.instancemanager.VoteManager;
import l2r.gameserver.model.AcademyRewards;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.randoms.Visuals;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.tables.FakePcsTable;
import l2r.gameserver.tables.PetDataTable;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Strings;

public class AdminReload implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_reload,
		admin_reload_config,
		admin_reload_multisell,
		admin_reload_gmaccess,
		admin_reload_htm,
		admin_reload_qs,
		admin_reload_qs_help,
		admin_reload_skills,
		admin_reload_npc,
		admin_reload_spawn,
		admin_reload_fish,
		admin_reload_items,
		admin_reload_abuse,
		admin_reload_translit,
		admin_reload_shops,
		admin_reload_static,
		admin_reload_pets,
		admin_reload_locale,
		admin_reload_nobles,
		admin_reload_itemmall,
		admin_reload_premiumsystem,
		admin_reload_images,
		admin_reload_achievements,
		admin_reload_pollmanager,
		admin_reload_autoannounce,
		admin_reload_emoticons,
		admin_reload_visuals,
		admin_reload_fakepc,
		admin_reload_academyrewards,
		admin_reload_cbaugment,
		admin_reload_instances,
		admin_reload_betaaccess
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_reload:
				break;
			case admin_reload_config:
			{
				try
				{
					Config.load();
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message1", activeChar, e.getMessage()));
					return false;
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message2", activeChar));
				break;
			}
			case admin_reload_multisell:
			{
				try
				{
					MultiSellHolder.getInstance().reload();
				}
				catch(Exception e)
				{
					return false;
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message3", activeChar));
				break;
			}
			case admin_reload_gmaccess:
			{
				try
				{
					AdminTable.getInstance().load();
				}
				catch(Exception e)
				{
					return false;
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message4", activeChar));
				break;
			}
			case admin_reload_htm:
			{
				HtmCache.getInstance().clear();
				HtmCache.getInstance().reload();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message5", activeChar));
				break;
			}
			case admin_reload_qs:
			{
				if(fullString.endsWith("all"))
					for(Player p : GameObjectsStorage.getAllPlayersForIterate())
						reloadQuestStates(p);
				else
				{
					GameObject t = activeChar.getTarget();

					if(t != null && t.isPlayer())
					{
						Player p = (Player) t;
						reloadQuestStates(p);
					}
					else
						reloadQuestStates(activeChar);
				}
				break;
			}
			case admin_reload_qs_help:
			{
				activeChar.sendMessage("");
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message6", activeChar));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message7", activeChar));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message8", activeChar));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message9", activeChar));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message10", activeChar));
				activeChar.sendMessage("");
				break;
			}
			case admin_reload_skills:
			{
				SkillTable.getInstance().reload();
				break;
			}
			case admin_reload_npc:
			{
				NpcParser.getInstance().reload();
				break;
			}
			case admin_reload_spawn:
			{
				ThreadPoolManager.getInstance().execute(new RunnableImpl()
				{
					@Override
					public void runImpl() throws Exception
					{
						SpawnManager.getInstance().reloadAll();
					}
				});
				break;
			}
			case admin_reload_fish:
			{
				FishParser.getInstance().reload();
				break;
			}
			case admin_reload_items:
			{
				ItemParser.getInstance().reload();
				break;
			}
			case admin_reload_abuse:
			{
				Config.abuseLoad();
				break;
			}
			case admin_reload_translit:
			{
				Strings.reload();
				break;
			}
			case admin_reload_shops:
			{
				BuyListHolder.reload();
				break;
			}
			case admin_reload_static:
			{
				//StaticObjectsTable.getInstance().reloadStaticObjects();
				break;
			}
			case admin_reload_pets:
			{
				PetDataTable.getInstance().reload();
				break;
			}
			case admin_reload_locale:
			{
				StringHolder.getInstance().reload();
				break;
			}
			case admin_reload_nobles:
			{
				OlympiadNobleDAO.getInstance().select();
				OlympiadDatabase.loadNoblesRank();
				break;
			}
			case admin_reload_itemmall:
			{
				ProductHolder.getInstance().reload();
				break;
			}
			case admin_reload_premiumsystem:
			{
				PremiumAccountsTable.init();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message11", activeChar));
				break;
			}
			case admin_reload_achievements:
			{
				Achievements.getInstance().load();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message12", activeChar));
				break;
			}
			case admin_reload_images:
			{
				ImagesChache.getInstance().reload();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message13", activeChar));
				break;
			}
			case admin_reload_pollmanager:
			{
				VoteManager.getInstance();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message14", activeChar));
				break;
			}
			case admin_reload_autoannounce:
			{
				AutoAnnounce.getInstance().reload();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message15", activeChar));
				break;
			}
			case admin_reload_emoticons:
			{
				EmotionsTable.init();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message16", activeChar));
				break;
			}
			case admin_reload_visuals:
			{
				Visuals.getInstance().load();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message17", activeChar));
				return true;
			}
			case admin_reload_fakepc:
			{
				FakePcsTable.getInstance().reloadData();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message18", activeChar));
				return true;
			}
			case admin_reload_academyrewards:
			{
				AcademyRewards.getInstance().reload();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminreload.message19", activeChar));
				return true;
			}
			case admin_reload_cbaugment:
			{
				Functions.callScripts("services.community.CommunityBoardAugment", "reload", new Object[]{});
				activeChar.sendMessage("Community Augmentation Board has been reloaded..");
				return true;
			}
			case admin_reload_instances:
			{
				InstantZoneParser.getInstance().reload();
				activeChar.sendMessage("Instances has been reloaded.");
				return true;
			}
			case admin_reload_betaaccess:
			{
				BetaServer.getInstance().reload();
				activeChar.sendMessage("Beta Server configs has been reloaded...");
				return true;
			}
			
			
		}
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/reload.htm"));
		return true;
	}

	private void reloadQuestStates(Player p)
	{
		for(QuestState qs : p.getAllQuestsStates())
			p.removeQuestState(qs.getQuest().getName());
		Quest.restoreQuestStates(p);
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}