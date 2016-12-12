package l2r.gameserver.handler.admincommands.impl;


import l2r.gameserver.Config;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.impl.SiegeEvent;
import l2r.gameserver.model.instances.VillageMasterInstance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.SubUnit;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2r.gameserver.network.serverpackets.PledgeStatusChanged;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.utils.Util;

import java.util.StringTokenizer;

/**
 * Pledge Manipulation //pledge <create|setlevel|resetcreate|resetwait|addrep|setleader|setname>
 */
public class AdminPledge implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_pledge
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		@SuppressWarnings("unused")
		Commands command = (Commands) comm;


		GameObject curtarget = activeChar.getTarget();
		if (curtarget == null)
		{
			activeChar.sendMessage("No target has been found.");
			return false;
		}
		
		if (!curtarget.isPlayer())
		{
			activeChar.sendMessage("Your target should be a player.");
			return false;
		}
		
		Player target = curtarget.getPlayer();
		
		if(fullString.startsWith("admin_pledge"))
		{
			StringTokenizer st = new StringTokenizer(fullString);
			st.nextToken();

			String action = st.nextToken(); // setlevel|resetcreate|resetwait|addrep

			if(action.equals("create"))
				try
			{
					if(target == null)
					{
						activeChar.sendPacket(SystemMsg.INVALID_TARGET);
						return false;
					}
					if(target.getPlayer().getLevel() < 10)
					{
						activeChar.sendPacket(SystemMsg.YOU_DO_NOT_MEET_THE_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
						return false;
					}
					String pledgeName = st.nextToken();
					if(pledgeName.length() > 16)
					{
						activeChar.sendPacket(SystemMsg.CLAN_NAMES_LENGTH_IS_INCORRECT);
						return false;
					}
					if(!Util.isMatchingRegexp(pledgeName, Config.CLAN_NAME_TEMPLATE))
					{
						// clan name is not matching template
						activeChar.sendPacket(SystemMsg.CLAN_NAME_IS_INVALID);
						return false;
					}

					Clan clan = ClanTable.getInstance().createClan(target, pledgeName);
					if(clan != null)
					{
						target.sendPacket(clan.listAll());
						target.sendPacket(new PledgeShowInfoUpdate(clan), SystemMsg.YOUR_CLAN_HAS_BEEN_CREATED);
						target.updatePledgeClass();
						target.sendUserInfo(true);
						activeChar.sendHtml("admin/pledgemanage.htm");
						return true;
					}
					else
					{
						activeChar.sendPacket(SystemMsg.THIS_NAME_ALREADY_EXISTS);
						return false;
					}
			}
			catch(Exception e)
			{}
			else if(action.equals("setlevel"))
			{
				if(target.getClan() == null)
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}

				try
				{
					int level = Integer.parseInt(st.nextToken());
					Clan clan = target.getClan();

					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpledge.message1", activeChar, level, clan.getName()));
					clan.setLevel(level);
					clan.updateClanInDB();

				/*	if(level < CastleSiegeManager.getSiegeClanMinLevel())
						SiegeUtils.removeSiegeSkills(target);
					else
						SiegeUtils.addSiegeSkills(target);   */

					if(level == 5)
						target.sendPacket(SystemMsg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);

					PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
					PledgeStatusChanged ps = new PledgeStatusChanged(clan);

					for(Player member : clan.getOnlineMembers(0))
					{
						member.updatePledgeClass();
						member.sendPacket(SystemMsg.YOUR_CLANS_LEVEL_HAS_INCREASED, pu, ps);
						member.broadcastUserInfo(true);
					}

					activeChar.sendHtml("admin/pledgemanage.htm");
					
					return true;
				}
				catch(Exception e)
				{}
			}
			else if(action.equals("resetcreate"))
			{
				if(target.getClan() == null)
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				target.getClan().setExpelledMemberTime(0);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpledge.message2", activeChar, target.getName()));
				activeChar.sendHtml("admin/pledgemanage.htm");
				return true;
			}
			else if(action.equals("resetwait"))
			{
				target.setLeaveClanTime(0);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpledge.message3", activeChar, target.getName()));
				activeChar.sendHtml("admin/pledgemanage.htm");
				return true;
			}
			else if(action.equals("addrep"))
			{
				try
				{
					int rep = Integer.parseInt(st.nextToken());
					
					if (target.getClan() == null || target.getClan().getLevel() < 5)
					{
						activeChar.sendPacket(SystemMsg.INVALID_TARGET);
						return false;
					}
					target.getClan().incReputation(rep, false, "admin_manual");
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpledge.message4", activeChar, rep, target.getClan().getName()));
					activeChar.sendHtml("admin/pledgemanage.htm");
					return true;
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpledge.message5", activeChar));
				}
			}
			else if(action.equals("setleader"))
			{
				Clan clan = target.getClan();
				if(target.getClan() == null)
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				String newLeaderName = null;
				if(st.hasMoreTokens())
					newLeaderName = st.nextToken();
				else
					newLeaderName = target.getName();
				SubUnit mainUnit = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
				UnitMember newLeader = mainUnit.getUnitMember(newLeaderName);
				if(newLeader == null)
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				VillageMasterInstance.setLeader(activeChar, clan, mainUnit, newLeader);
				activeChar.sendHtml("admin/pledgemanage.htm");
				return true;
			}
			else if(action.equals("setname"))
			{
				String newClanName = null;
				if(st.hasMoreTokens())
					newClanName = st.nextToken();
				
				if(target.getClan() == null || newClanName == null)
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				
				if(target.getEvent(SiegeEvent.class) != null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpledge.message6", activeChar));
					return false;
				}
				
				if(!Util.isMatchingRegexp(newClanName, Config.CLAN_NAME_TEMPLATE))
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpledge.message7", activeChar));
					return false;
				}
				if(ClanTable.getInstance().getClanByName(newClanName) != null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpledge.message8", activeChar));
					return false;
				}
				
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpledge.message9", activeChar, target.getName(), target.getClan().getName(), newClanName));
				
				SubUnit sub = target.getClan().getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
				sub.setName(newClanName, true);
				
				target.getClan().broadcastClanStatus(true, true, false);
				
				activeChar.sendHtml("admin/pledgemanage.htm");
				return true;
			}
		}

		return false;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}