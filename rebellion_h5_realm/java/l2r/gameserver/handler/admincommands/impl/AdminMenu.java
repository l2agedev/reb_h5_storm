package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.AdminFunctions;
import l2r.gameserver.utils.Location;

import java.util.StringTokenizer;


@SuppressWarnings("unused")
public class AdminMenu implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_char_manage,
		admin_teleport_character_to_menu,
		admin_recall_char_menu,
		admin_goto_char_menu,
		admin_kick_menu,
		admin_kill_menu,
		admin_ban_menu,
		admin_unban_menu
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(fullString.startsWith("admin_teleport_character_to_menu"))
		{
			String[] data = fullString.split(" ");
			if(data.length == 5)
			{
				String playerName = data[1];
				Player player = World.getPlayer(playerName);
				if(player != null)
					teleportCharacter(player, new Location(Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4])), activeChar);
			}
		}
		else if(fullString.startsWith("admin_recall_char_menu"))
			try
		{
				String targetName = fullString.substring(23);
				Player player = World.getPlayer(targetName);
				teleportCharacter(player, activeChar.getLoc(), activeChar);
		}
		catch(StringIndexOutOfBoundsException e)
		{}
		else if(fullString.startsWith("admin_goto_char_menu"))
			try
		{
				String targetName = fullString.substring(21);
				Player player = World.getPlayer(targetName);
				teleportToCharacter(activeChar, player);
		}
		catch(StringIndexOutOfBoundsException e)
		{}
		else if(fullString.equals("admin_kill_menu"))
		{
			GameObject obj = activeChar.getTarget();
			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				Player plyr = World.getPlayer(player);
				if(plyr == null)
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminmenu.message1", activeChar, player));
				obj = plyr;
			}
			if(obj != null && obj.isCreature())
			{
				Creature target = (Creature) obj;
				target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, null, true, true, true, false, false, false, true);
			}
			else
				activeChar.sendPacket(SystemMsg.INVALID_TARGET);
		}
		else if(fullString.startsWith("admin_kick_menu"))
		{
			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				if (AdminFunctions.kick(player, "kick"))
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminmenu.message2", activeChar));
			}
		}

		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/charmanage.htm"));
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void teleportCharacter(Player player, Location loc, Player activeChar)
	{
		if(player != null)
		{
			if (activeChar != null && activeChar.getReflectionId() != 0)
			{
				player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminmenu.message3", player));
				player.teleToLocation(loc, activeChar.getReflectionId());
			}
			else
			{
				player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminmenu.message4", player));
				player.teleToLocation(loc);
			}
		}
	}

	private void teleportToCharacter(Player activeChar, GameObject target)
	{
		Player player;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		if(player.getObjectId() == activeChar.getObjectId())
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminmenu.message5", activeChar));
		else
		{
			activeChar.teleToLocation(player.getLoc());
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminmenu.message6", activeChar, player.getName()));
		}
	}
}