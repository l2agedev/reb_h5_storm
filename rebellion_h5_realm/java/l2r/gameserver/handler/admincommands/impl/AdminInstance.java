package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.instancemanager.SoDManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.utils.Util;

import org.apache.commons.lang3.math.NumberUtils;

public class AdminInstance implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_instance,
		admin_instance_id,
		admin_create_instance,
		admin_set_instance,
		admin_collapse,
		admin_instance_reset,
		admin_instance_all_reset,
		admin_set_reuse,
		admin_addtiatkill,
		admin_startbeleth,
		admin_endbeleth
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		switch(command)
		{
			case admin_instance:
				listOfInstances(activeChar);
				break;
			case admin_instance_id:
				if(wordList.length > 1)
					listOfCharsForInstance(activeChar, wordList[1]);
				break;
			case admin_create_instance:
				Reflection refl = new Reflection();
				InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(600);
				refl.init(iz);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admininstance.message1", activeChar, refl.getId(), iz.getId()));
				break;
			case admin_set_instance:
				if(wordList.length > 1 && Util.isDigit(wordList[1]))
					activeChar.teleToLocation(activeChar.getLoc(), Integer.parseInt(wordList[1]));
				break;
			case admin_collapse:
				if(!activeChar.getReflection().isDefault())
					activeChar.getReflection().collapse();
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admininstance.message2", activeChar));
				break;
			case admin_instance_reset:
				if (wordList.length > 2)
				{
					Iterable<Player> players = null;
					if ("party".equalsIgnoreCase(wordList[2]))
					{
						if (activeChar.isInParty())
							players = activeChar.getPlayerGroup();
						else
							activeChar.sendMessage("You are not in a party.");
					}
					else if ("cc".equalsIgnoreCase(wordList[2]))
					{
						if (activeChar.isInParty() && activeChar.getParty().isInCommandChannel())
							players = activeChar.getPlayerGroup();
						else
							activeChar.sendMessage("You are not in a command channel.");
					}
					else if (NumberUtils.toInt(wordList[2]) > 0)
					{
						players = activeChar.getAroundPlayers(NumberUtils.toInt(wordList[2]), 500);
					}
					else
					{
						activeChar.sendMessage("Usage: //instance_reset <instaceId> [party|cc|rangeAroundYou]");
					}
					
					int instanceId = NumberUtils.toInt(wordList[2]);
					int count = 0;
					for (Player plr : players)
					{
						count++;
						plr.removeInstanceReuse(instanceId);
					}
					activeChar.sendMessage("Instance reuse for " + instanceId + " has been removed for " + count + " players.");
				}
				else if(wordList.length > 1 && activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
				{
					Player p = activeChar.getTarget().getPlayer();
					p.removeInstanceReuse(Integer.parseInt(wordList[1]));
					Functions.sendDebugMessage(activeChar, "Instance reuse has been removed.");
				}
				break;
			case admin_instance_all_reset:
				if (wordList.length > 1)
				{
					Iterable<Player> players = null;
					if ("party".equalsIgnoreCase(wordList[1]))
					{
						if (activeChar.isInParty())
							players = activeChar.getPlayerGroup();
						else
							activeChar.sendMessage("You are not in a party.");
					}
					else if ("cc".equalsIgnoreCase(wordList[1]))
					{
						if (activeChar.isInParty() && activeChar.getParty().isInCommandChannel())
							players = activeChar.getPlayerGroup();
						else
							activeChar.sendMessage("You are not in a command channel.");
					}
					else if (NumberUtils.toInt(wordList[1]) > 0)
					{
						players = activeChar.getAroundPlayers(NumberUtils.toInt(wordList[1]), 500);
					}
					else
					{
						activeChar.sendMessage("Usage: //instance_reset <instaceId> [party|cc|rangeAroundYou]");
					}
					
					int count = 0;
					for (Player plr : players)
					{
						count++;
						plr.removeAllInstanceReuses();
					}
					activeChar.sendMessage("Instance reuse for all instances has been removed for " + count + " players.");
				}
				else if(activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
				{
					Player p = activeChar.getTarget().getPlayer();
					p.removeAllInstanceReuses();
					Functions.sendDebugMessage(activeChar, "All instance reuses has been removed.");
				}
				break;
			case admin_set_reuse:
				if(activeChar.getReflection() != null)
					activeChar.getReflection().setReenterTime(System.currentTimeMillis());
				break;
			case admin_addtiatkill:
				SoDManager.addTiatKill();
				break;
			case admin_endbeleth:
				Functions.callScripts("BelethManager", "endBeleth");
				break;
			case admin_startbeleth:
				Functions.callScripts("BelethManager", "StartBeleth");
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void listOfInstances(Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuffer replyMSG = new StringBuffer("<html><title>Instance Menu</title><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>List of Instances</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table><br><br>");

		for(Reflection reflection : ReflectionManager.getInstance().getAll())
		{
			if(reflection == null || reflection.isDefault() || reflection.isCollapseStarted())
				continue;
			int countPlayers = 0;
			if(reflection.getPlayers() != null)
				countPlayers = reflection.getPlayers().size();
			replyMSG.append("<a action=\"bypass -h admin_instance_id ").append(reflection.getId()).append(" \">").append(reflection.getName()).append("(").append(countPlayers).append(" players). Id: ").append(reflection.getId()).append("</a><br>");
		}

		replyMSG.append("<button value=\"Refresh\" action=\"bypass -h admin_instance\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void listOfCharsForInstance(Player activeChar, String sid)
	{
		Reflection reflection = ReflectionManager.getInstance().get(Integer.parseInt(sid));

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuffer replyMSG = new StringBuffer("<html><title>Instance Menu</title><body><br>");
		if(reflection != null)
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>List of players in ").append(reflection.getName()).append("</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_instance\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table><br><br>");

			for(Player player : reflection.getPlayers())
				replyMSG.append("<a action=\"bypass -h admin_teleportto ").append(player.getName()).append(" \">").append(player.getName()).append("</a><br>");
		}
		else
		{
			replyMSG.append("Instance not active.<br>");
			replyMSG.append("<a action=\"bypass -h admin_instance\">Back to list.</a><br>");
		}

		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
}