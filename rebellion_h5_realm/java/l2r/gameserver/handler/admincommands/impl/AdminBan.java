package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.Announcements;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.AdminFunctions;
import l2r.gameserver.utils.AutoBan;
import l2r.gameserver.utils.TimeUtils;

import java.util.StringTokenizer;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminBan implements IAdminCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(AdminBan.class);
	
	private static enum Commands
	{
		admin_ban,
		admin_unban,
		admin_cban,
		admin_chatban,
		admin_chatunban,
		admin_accban,
		admin_accunban,
		admin_trade_ban,
		admin_trade_unban,
		admin_jail,
		admin_unjail,
		admin_permaban,
		admin_hwidban
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		StringTokenizer st = new StringTokenizer(fullString);
		try
		{
			switch (command)
			{
				case admin_ban:
					ban(st, activeChar);
					break;
				case admin_unban:
				{
					st.nextToken();
					String charName = null;
					if (st.hasMoreTokens())
						charName = st.nextToken();
					if (charName != null)
					{
						if (AutoBan.Banned(charName, 0, 0, "", "Unban"))
							activeChar.sendMessage("Character " + charName + " has been unbanned.");
						else
							activeChar.sendMessage("No such character!");
					}
					else
						activeChar.sendMessage("Specify character name.");
					break;
				}
				case admin_accban:
				{
					st.nextToken();
					
					int banExpire = 0;
					
					String account = st.nextToken();
					
					if (st.hasMoreTokens())
						banExpire = (int) (System.currentTimeMillis() / 1000L) + Integer.parseInt(st.nextToken()) * 360000;
					
					_log.info(AdminFunctions.accountBan(activeChar.getTarget() != null ? activeChar.getTarget().getName() : "", account, banExpire, activeChar.getName()));
					break;
				}
				case admin_accunban:
				{
					st.nextToken();
					String account = null;
					if (st.hasMoreTokens())
						account = st.nextToken();
					if (account != null)
						_log.info(AdminFunctions.accountUnban(account, activeChar.getName()));
					else
						activeChar.sendMessage("Specify account name.");
					break;
				}
				
				case admin_trade_ban:
					return tradeBan(st, activeChar);
					
				case admin_trade_unban:
					return tradeUnban(st, activeChar);
					
				case admin_chatban:
				{
					try
					{
						st.nextToken();
						String player = st.nextToken();
						int period = Integer.parseInt(st.nextToken());
						String bmsg = "admin_chatban " + player + " " + period + " ";
						String msg = fullString.substring(bmsg.length(), fullString.length());
						
						if (AutoBan.ChatBan(player, period, msg, activeChar.getName()))
						{
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message2", activeChar).addString(player));
							
							Announcements.getInstance().announceToAll("Player " + player + " has been chat banned for " + period + " minutes.");
							
							Player p = World.getPlayer(player);
							if (p != null) // чар есть в мире
							{
								p.sendPacket(new Say2(activeChar.getObjectId(), ChatType.TELL, "->" + p.getName(), "You have been chat banned for " + TimeUtils.minutesToFullString(period) + ", reason: " + msg));
							}
						}
						else
						{
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message3", activeChar).addString(player));
						}
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message4", activeChar));
					}
					break;
				}
				
				case admin_chatunban:
					try
					{
						st.nextToken();
						String player = st.nextToken();
						
						if (AutoBan.ChatUnBan(player, activeChar.getName()))
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message5", activeChar).addString(player));
						else
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message6", activeChar).addString(player));
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message7", activeChar));
					}
					break;
				case admin_jail:
					try
					{
						st.nextToken();
						String player = st.hasMoreElements() ? st.nextToken() : activeChar.getTarget() != null ? activeChar.getTarget().getName() : "";
						int period = st.hasMoreElements() ? NumberUtils.toInt(st.nextToken()) : -1;
						String reason = "";
						while (st.hasMoreElements())
							reason += st.nextToken() + " ";
						
						_log.info(AdminFunctions.jail(player, period * 60, activeChar.getName(), reason));
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message11", activeChar));
					}
					break;
				case admin_unjail:
					try
					{
						st.nextToken();
						String player = st.nextToken();
						_log.info(AdminFunctions.unJail(player, activeChar.getName()));
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message14", activeChar));
					}
					break;
				case admin_cban:
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/cban.htm"));
					break;
				case admin_permaban:
					if (activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
					{
						Functions.sendDebugMessage(activeChar, "Target should be set and be a player instance.");
						return false;
					}
					_log.info(AdminFunctions.accountBan(activeChar.getTarget().getName(), activeChar.getTarget().getPlayer().getAccountName(), 0, activeChar.getName()));
					break;
				case admin_hwidban:
					try
					{
						st.nextToken();
						String playername = st.nextToken();
						_log.info(AdminFunctions.hwidBan(playername, activeChar.getName(), "Banned from ingame by " + activeChar.getName()));
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message16", activeChar));
					}
					break;
			}
		}
		catch (Exception e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.error", activeChar).addString(e.getMessage()));
			return false;
		}
		
		return true;
	}
	
	private boolean tradeBan(StringTokenizer st, Player activeChar)
	{
		try
		{
			if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
				return false;
			st.nextToken();
			String targetName = null;
			int hours = -1;
			if (st.hasMoreTokens())
				targetName = st.nextToken();
			if (st.hasMoreTokens())
				hours = Integer.parseInt(st.nextToken());
			
			if (targetName == null)
				targetName = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "";
			
			return !"Player not found ingame.".equals(AdminFunctions.tradeBan(targetName, hours, activeChar.getName()));
		}
		catch (Exception e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.error", activeChar).addString(e.getMessage()));
			return false;
		}
	}

	private boolean tradeUnban(StringTokenizer st, Player activeChar)
	{
		try
		{
			if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
				return false;
			st.nextToken();
			
			String targetName = null;
			if (st.hasMoreTokens())
				targetName = st.nextToken();
			if (targetName == null)
				targetName = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "";
			
			return !"Player not found ingame.".equals(AdminFunctions.tradeUnban(targetName, activeChar.getName()));
		}
		catch (Exception e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.error", activeChar).addString(e.getMessage()));
			return false;
		}
	}

	private boolean ban(StringTokenizer st, Player activeChar)
	{
		try
		{
			st.nextToken();
			String player = st.nextToken();
			int time = 0;
			String msg = "";

			if(st.hasMoreTokens())
				time = Integer.parseInt(st.nextToken());

			while(st.hasMoreTokens())
				msg += st.nextToken() + " ";
			
			msg.trim();

			AdminFunctions.ban(player, time, msg, activeChar.getName());
		}
		catch(Exception e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminban.message18", activeChar));
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
