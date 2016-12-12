package handler.admincommands;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.dao.SiegeClanDAO;
import l2r.gameserver.data.xml.holder.EventHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.EventType;
import l2r.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2r.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2r.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2r.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2r.gameserver.model.entity.events.impl.SiegeEvent;
import l2r.gameserver.model.entity.events.objects.SiegeClanObject;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.entity.residence.Dominion;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.entity.residence.Residence;
import l2r.gameserver.model.entity.residence.ResidenceType;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.clientpackets.RequestExJoinDominionWar;
import l2r.gameserver.network.clientpackets.RequestJoinCastleSiege;
import l2r.gameserver.network.serverpackets.CastleSiegeInfo;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.tables.ClanTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import npc.model.residences.fortress.siege.BackupPowerUnitInstance;
import npc.model.residences.fortress.siege.PowerControlUnitInstance;

public class AdminResidence implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_residence_list,
		admin_residence,
		admin_set_owner,
		admin_set_siege_time,
		admin_add_attacker,
		admin_remove_attacker,
		admin_add_defender,
		admin_remove_defender,
		admin_list_siege_clans,
		// dominion
		admin_start_dominion_war,
		admin_stop_dominion_war,
		admin_set_dominion_time,
		admin_tw_add_player,
		admin_tw_remove_player,
		admin_tw_add_clan,
		admin_tw_remove_clan,
		//
		admin_quick_siege_start,
		admin_quick_siege_stop,
		// fortress
		admin_backup_unit_info,
		admin_fortress_spawn_flags
	}

	@SuppressWarnings({ "rawtypes", "incomplete-switch" })
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		final Residence r;
		final SiegeEvent event;
		Calendar calendar;
		NpcHtmlMessage msg;
		final DominionSiegeRunnerEvent runnerEvent;
		try
		{
			switch(command)
			{
				case admin_residence_list:
					msg = new NpcHtmlMessage(5);
					msg.setFile("admin/residence/residence_list.htm");

					StringBuilder replyMSG = new StringBuilder(300);
					
					replyMSG.append("<tr><td align=center>");
					replyMSG.append("<font color=LEVEL>----- Castle ------</font>");
					replyMSG.append("</td></tr>");
					// Castle
					appendResidence(replyMSG, ResidenceHolder.getInstance().getResidenceList(Castle.class));
					
					replyMSG.append("<tr><td align=center>");
					replyMSG.append("<font color=LEVEL>----- Dominion ------</font>");
					replyMSG.append("</td></tr>");
					
					// Domination
					appendResidence(replyMSG, ResidenceHolder.getInstance().getResidenceList(Dominion.class));
					
					replyMSG.append("<tr><td align=center>");
					replyMSG.append("<font color=LEVEL>----- Fortress ------</font>");
					replyMSG.append("</td></tr>");
					
					// Fortress
					appendResidence(replyMSG, ResidenceHolder.getInstance().getResidenceList(Fortress.class));
					
					replyMSG.append("<tr><td align=center>");
					replyMSG.append("<font color=LEVEL>----- Clan Hall ------</font>");
					replyMSG.append("</td></tr>");
					
					// Clanhall
					appendResidence(replyMSG, ResidenceHolder.getInstance().getResidenceList(ClanHall.class));
					
					msg.replace("%residence_list%", replyMSG.toString());
					activeChar.sendPacket(msg);
					break;
				case admin_residence:
					if(wordList.length != 2)
						return false;
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					event = r.getSiegeEvent();
					msg = new NpcHtmlMessage(5);
					if(r.getType() == ResidenceType.Dominion)
					{
						msg.setFile("admin/residence/dominion_siege_info.htm");
						msg.replace("%residence%", r.getName());
						msg.replace("%id%", String.valueOf(r.getId()));
						msg.replace("%owner%", r.getOwner() == null ? "NPC" : r.getOwner().getName());

						StringBuilder builder = new StringBuilder(100);
						List<SiegeClanObject> clans = event.getObjects(SiegeEvent.ATTACKERS);
						for(SiegeClanObject clan : clans)
							builder.append("<tr>").append("<td>").append(clan.getClan().getName()).append("</td>").append("<td>").append(clan.getClan().getLeaderName()).append("</td>").append("<td>").append(SiegeEvent.ATTACKERS).append("</td>").append("</tr>");

						clans = event.getObjects(SiegeEvent.DEFENDERS);
						for(SiegeClanObject clan : clans)
							builder.append("<tr>").append("<td>").append(clan.getClan().getName()).append("</td>").append("<td>").append(clan.getClan().getLeaderName()).append("</td>").append("<td>").append(SiegeEvent.DEFENDERS).append("</td>").append("</tr>");

						msg.replace("%clans%", builder.toString());

						builder = new StringBuilder(100);
						List<Integer> players = event.getObjects(DominionSiegeEvent.ATTACKER_PLAYERS);
						for(int i : players)
						{
							Player player = GameObjectsStorage.getPlayer(i);
							builder.append("<tr>").append("<td>").append(i).append("</td>").append("<td>").append(player == null ? "null" : player.getName()).append("</td>").append("<td>").append(DominionSiegeEvent.ATTACKER_PLAYERS).append("</td>").append("</tr>");
						}

						players = event.getObjects(DominionSiegeEvent.DEFENDER_PLAYERS);
						for(int i : players)
						{
							Player player = GameObjectsStorage.getPlayer(i);
							builder.append("<tr>").append("<td>").append(i).append("</td>").append("<td>").append(player == null ? "null" : player.getName()).append("</td>").append("<td>").append(DominionSiegeEvent.DEFENDER_PLAYERS).append("</td>").append("</tr>");
						}
						msg.replace("%players%", builder.toString());
					}
					else
					{
						msg.setFile("admin/residence/siege_info.htm");
						msg.replace("%residence%", r.getName());
						msg.replace("%id%", String.valueOf(r.getId()));
						msg.replace("%owner%", r.getOwner() == null ? "NPC" : r.getOwner().getName());
						msg.replace("%cycle%", String.valueOf(r.getCycle()));
						msg.replace("%paid_cycle%", String.valueOf(r.getPaidCycle()));
						msg.replace("%reward_count%", String.valueOf(r.getRewardCount()));
						msg.replace("%left_time%", String.valueOf(r.getCycleDelay()));

						StringBuilder clans = new StringBuilder(100);
						for(Map.Entry<String, List<Serializable>> entry : event.getObjects().entrySet())
						{
							for(Serializable o : entry.getValue())
							{
								if(o instanceof SiegeClanObject)
								{
									SiegeClanObject siegeClanObject = (SiegeClanObject)o;
									clans.append("<tr>").append("<td>").append(siegeClanObject.getClan().getName())
									.append("</td>").append("<td>").append(siegeClanObject.getClan().getLeaderName())
									.append("</td>").append("<td>").append(siegeClanObject.getType()).append("</td>").append("</tr>");
								}
							}
						}
						msg.replace("%clans%", clans.toString());
					}

					msg.replace("%hour%", String.valueOf(r.getSiegeDate().get(Calendar.HOUR_OF_DAY)));
					msg.replace("%minute%", String.valueOf(r.getSiegeDate().get(Calendar.MINUTE)));
					msg.replace("%day%", String.valueOf(r.getSiegeDate().get(Calendar.DAY_OF_MONTH)));
					msg.replace("%month%", String.valueOf(r.getSiegeDate().get(Calendar.MONTH) + 1));
					msg.replace("%year%", String.valueOf(r.getSiegeDate().get(Calendar.YEAR)));
					activeChar.sendPacket(msg);
					break;
				case admin_set_owner:
					if(wordList.length != 3)
						return false;
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					Clan clan = null;
					String clanName = wordList[2];
					if(!clanName.equalsIgnoreCase("npc"))
					{
						clan = ClanTable.getInstance().getClanByName(clanName);
						if(clan == null)
						{
							activeChar.sendPacket(SystemMsg.INCORRECT_NAME);
							AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
							return false;
						}
					}

					event = r.getSiegeEvent();

					event.clearActions();

					if(r.getType() == ResidenceType.Dominion)
						r.changeOwner(clan);
					else
					{
						r.getLastSiegeDate().setTimeInMillis(clan == null ? 0 : System.currentTimeMillis());
						r.getOwnDate().setTimeInMillis(clan == null ? 0 : System.currentTimeMillis());
						r.changeOwner(clan);

						event.reCalcNextTime(false);
					}
					break;
				case admin_add_attacker:
					GameObject target = activeChar.getTarget();
					if (target == null || !target.isPlayer())
					{
						activeChar.sendMessage(new CustomMessage("scripts.handler.admincommands.adminresidence.notarget_noplayer", activeChar));
						return false;
					}
					
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					else if (r.getType() == ResidenceType.Castle)
						RequestJoinCastleSiege.registerAtCastle(target.getPlayer(), (Castle) r, true, true);
					else if (r.getType() == ResidenceType.ClanHall && r.getSiegeEvent() instanceof ClanHallSiegeEvent)
						RequestJoinCastleSiege.registerAtClanHall(target.getPlayer(), (ClanHall) r, true);
					else if (r.getType() == ResidenceType.Fortress)
						RequestJoinCastleSiege.registerAtFortress(target.getPlayer(), (Fortress) r, true, true);
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_remove_attacker:
					target = activeChar.getTarget();
					if (target == null || !target.isPlayer())
					{
						activeChar.sendMessage(new CustomMessage("scripts.handler.admincommands.adminresidence.notarget_noplayer", activeChar));
						return false;
					}
					
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					else if (r.getType() == ResidenceType.Castle)
						RequestJoinCastleSiege.registerAtCastle(target.getPlayer(), (Castle) r, true, false);
					else if (r.getType() == ResidenceType.ClanHall && r.getSiegeEvent() instanceof ClanHallSiegeEvent)
						RequestJoinCastleSiege.registerAtClanHall(target.getPlayer(), (ClanHall) r, false);
					else if (r.getType() == ResidenceType.Fortress)
						RequestJoinCastleSiege.registerAtFortress(target.getPlayer(), (Fortress) r, true, false);
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_add_defender:
					target = activeChar.getTarget();
					if (target == null || !target.isPlayer())
					{
						activeChar.sendMessage(new CustomMessage("scripts.handler.admincommands.adminresidence.notarget_noplayer", activeChar));
						return false;
					}
					
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					else if (r.getType() == ResidenceType.Castle)
						RequestJoinCastleSiege.registerAtCastle(target.getPlayer(), (Castle) r, false, true);
					else if (r.getType() == ResidenceType.ClanHall && r.getSiegeEvent() instanceof ClanHallSiegeEvent)
						RequestJoinCastleSiege.registerAtClanHall(target.getPlayer(), (ClanHall) r, true);
					else if (r.getType() == ResidenceType.Fortress)
						RequestJoinCastleSiege.registerAtFortress(target.getPlayer(), (Fortress) r, false, true);
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_remove_defender:
					target = activeChar.getTarget();
					if (target == null || !target.isPlayer())
					{
						activeChar.sendMessage(new CustomMessage("scripts.handler.admincommands.adminresidence.notarget_noplayer", activeChar));
						return false;
					}
					
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					else if (r.getType() == ResidenceType.Castle)
						RequestJoinCastleSiege.registerAtCastle(target.getPlayer(), (Castle) r, false, false);
					else if (r.getType() == ResidenceType.ClanHall && r.getSiegeEvent() instanceof ClanHallSiegeEvent)
						RequestJoinCastleSiege.registerAtClanHall(target.getPlayer(), (ClanHall) r, false);
					else if (r.getType() == ResidenceType.Fortress)
						RequestJoinCastleSiege.registerAtFortress(target.getPlayer(), (Fortress) r, false, false);
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_list_siege_clans:
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					else if (r.getType() == ResidenceType.Castle)
						activeChar.sendPacket(new CastleSiegeInfo((Castle) r, activeChar));
					else if (r.getType() == ResidenceType.ClanHall && r.getSiegeEvent() instanceof ClanHallSiegeEvent)
						activeChar.sendPacket(new CastleSiegeInfo((ClanHall) r, activeChar));
					else if (r.getType() == ResidenceType.Fortress)
						activeChar.sendPacket(new CastleSiegeInfo((Fortress) r, activeChar));
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_set_siege_time:
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;

					calendar = (Calendar) r.getSiegeDate().clone();
					for(int i = 2; i < wordList.length; i++)
					{
						int type;
						int val = Integer.parseInt(wordList[i]);
						switch(i)
						{
							case 2: type = Calendar.HOUR_OF_DAY; break;
							case 3: type = Calendar.MINUTE; break;
							case 4: type = Calendar.DAY_OF_MONTH; break;
							case 5: type = Calendar.MONTH; val -= 1; break;
							case 6: type = Calendar.YEAR; break;
							default: continue;
						}
						calendar.set(type, val);
					}
					
					event = r.getSiegeEvent();
					event.clearActions();
					r.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
					event.registerActions();
					r.setJdbcState(JdbcEntityState.UPDATED);
					r.update();

					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_quick_siege_start:
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;

					calendar = Calendar.getInstance();
					int mins = 0;
					if(wordList.length >= 3)
						mins = Integer.parseInt(wordList[2]);

					calendar.add(Calendar.MINUTE, mins);
					event = r.getSiegeEvent();
					event.clearActions();
					r.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
					event.registerActions();
					r.setJdbcState(JdbcEntityState.UPDATED);
					r.update();
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_quick_siege_stop:
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null)
						return false;

					event = r.getSiegeEvent();

					event.clearActions();
					ThreadPoolManager.getInstance().execute(new RunnableImpl()
					{
						@Override
						public void runImpl() throws Exception
						{
							event.stopEvent();
						}
					});

					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_start_dominion_war:
					calendar = Calendar.getInstance();
					mins = 0;
					if(wordList.length >= 3)
						mins = Integer.parseInt(wordList[2]);

					calendar.add(Calendar.MINUTE, mins);

					runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
					runnerEvent.clearActions();

					for(Fortress f : ResidenceHolder.getInstance().getResidenceList(Fortress.class))
					{
						f.getSiegeEvent().clearActions();
						if(f.getSiegeEvent().isInProgress())
							f.getSiegeEvent().stopEvent();

						f.getSiegeEvent().removeObjects(SiegeEvent.ATTACKERS);
						SiegeClanDAO.getInstance().delete(f);
					}

					for(Dominion d : runnerEvent.getRegisteredDominions())
					{
						d.getSiegeEvent().clearActions();
						d.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
					}
					runnerEvent.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
					runnerEvent.registerActions();
					break;
				case admin_stop_dominion_war:
					runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
					runnerEvent.clearActions();
					ThreadPoolManager.getInstance().execute(new RunnableImpl()
					{
						@Override
						public void runImpl() throws Exception
						{
							for(Fortress f : ResidenceHolder.getInstance().getResidenceList(Fortress.class))
							{
								if(f.getSiegeEvent().isInProgress())
									f.getSiegeEvent().stopEvent();
							}

							for(Dominion d : runnerEvent.getRegisteredDominions())
							{
								d.getSiegeEvent().clearActions();
								d.getSiegeEvent().stopEvent();
							}
							runnerEvent.stopEvent();
						}
					});
					break;
				case admin_tw_add_player:
					target = activeChar.getTarget();
					if (target == null || !target.isPlayer())
					{
						activeChar.sendMessage(new CustomMessage("scripts.handler.admincommands.adminresidence.notarget_noplayer", activeChar));
						return false;
					}
					
					r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
					if(r == null || r.getType() != ResidenceType.Dominion)
						return false;
					
					RequestExJoinDominionWar.registerAtDominion(target.getPlayer(), (Dominion) r, true, false);
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_tw_remove_player:
					target = activeChar.getTarget();
					if (target == null || !target.isPlayer())
					{
						activeChar.sendMessage(new CustomMessage("scripts.handler.admincommands.adminresidence.notarget_noplayer", activeChar));
						return false;
					}
					
					r = ResidenceHolder.getInstance().getResidence(Dominion.class, Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					
					RequestExJoinDominionWar.registerAtDominion(target.getPlayer(), (Dominion) r, false, false);
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_tw_add_clan:
					target = activeChar.getTarget();
					if (target == null || !target.isPlayer())
					{
						activeChar.sendMessage(new CustomMessage("scripts.handler.admincommands.adminresidence.notarget_noplayer", activeChar));
						return false;
					}
					
					r = ResidenceHolder.getInstance().getResidence(Dominion.class, Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					
					RequestExJoinDominionWar.registerAtDominion(target.getPlayer(), (Dominion) r, true, true);
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_tw_remove_clan:
					target = activeChar.getTarget();
					if (target == null || !target.isPlayer())
					{
						activeChar.sendMessage(new CustomMessage("scripts.handler.admincommands.adminresidence.notarget_noplayer", activeChar));
						return false;
					}
					
					r = ResidenceHolder.getInstance().getResidence(Dominion.class, Integer.parseInt(wordList[1]));
					if(r == null)
						return false;
					
					RequestExJoinDominionWar.registerAtDominion(target.getPlayer(), (Dominion) r, false, true);
					
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					break;
				case admin_backup_unit_info:
					target = activeChar.getTarget();
					if(!(target instanceof PowerControlUnitInstance) && !(target instanceof BackupPowerUnitInstance))
						return false;

					List<String> t = new ArrayList<String>(3);
					if(target instanceof PowerControlUnitInstance)
						for(int i : ((PowerControlUnitInstance) target).getGenerated())
							t.add(String.valueOf(i));
					else
						for(int i : ((BackupPowerUnitInstance) target).getGenerated())
							t.add(i == 0 ? "A" : i == 1 ? "B" : i == 2 ? "C" : "D");

					activeChar.sendMessage(new CustomMessage("scripts.handler.admincommands.adminresidence.password", activeChar, t.toString()));
					return true;
				case admin_fortress_spawn_flags:
					if(wordList.length != 2)
						return false;
					Fortress fortress = ResidenceHolder.getInstance().getResidence(Fortress.class, Integer.parseInt(wordList[1]));
					if(fortress == null)
						return false;
					FortressSiegeEvent siegeEvent = fortress.getSiegeEvent();
					if(!siegeEvent.isInProgress())
						return false;
					boolean[] f = siegeEvent.getBarrackStatus();
					for(int i = 0; i < f.length; i++)
						siegeEvent.barrackAction(i, true);
					siegeEvent.spawnFlags();
					return true;
			}
		}
		catch (Exception e)
		{
			activeChar.sendMessage(new CustomMessage ("scripts.handler.admincommands.adminresidence.error", activeChar, e.getMessage()));
			return false;
		}
		
		return true;
	}
	
	private <T extends Residence> void appendResidence(StringBuilder sb, Iterable<T> list)
	{
		for(Residence residence : list)
		{
			if(residence != null)
			{
				sb.append("<tr><td>");
				sb.append("<a action=\"bypass -h admin_residence ").append(residence.getId()).append("\">").append(residence.getName()).append("</a>");
				sb.append("</td><td>");

				Clan owner = residence.getOwner();
				if(owner == null)
					sb.append("NPC");
				else
					sb.append(owner.getName());

				sb.append("</td></tr>");
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	@Override
	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}
}
