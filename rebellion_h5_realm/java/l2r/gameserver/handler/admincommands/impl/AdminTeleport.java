package l2r.gameserver.handler.admincommands.impl;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.lang.ArrayUtils;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;


public class AdminTeleport implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_show_moves,
		admin_show_moves_other,
		admin_show_teleport,
		admin_teleport_to_character,
		admin_teleportto,
		admin_goto,
		admin_teleport_to,
		admin_tt,
		admin_move_to,
		admin_moveto,
		admin_teleport,
		admin_teleport_character,
		admin_sendhome,
		admin_teleporthome,
		admin_sendtown,
		admin_teleporttown,
		admin_recall,
		admin_walk,
		admin_recall_npc,
		admin_gonorth,
		admin_gosouth,
		admin_goeast,
		admin_gowest,
		admin_goup,
		admin_godown,
		admin_tele,
		admin_teleto,
		admin_tele_to,
		admin_instant_move,
		admin_tonpc,
		admin_to_npc,
		admin_toobject,
		admin_setref,
		admin_getref
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_show_moves:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/teleports.htm"));
				break;
			case admin_show_moves_other:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/tele/other.htm"));
				break;
			case admin_show_teleport:
				showTeleportCharWindow(activeChar);
				break;
			case admin_teleport_to_character:
				teleportToCharacter(activeChar, activeChar.getTarget());
				break;
			case admin_teleport_to:
			case admin_teleportto:
			case admin_goto:
			case admin_tt:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message1", activeChar));
					return false;
				}
				String chaName = Util.joinStrings(" ", wordList, 1);
				Player cha = GameObjectsStorage.getPlayer(chaName);
				if(cha == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message2", activeChar, chaName));
					return false;
				}
				teleportToCharacter(activeChar, cha);
				break;
			case admin_move_to:
			case admin_moveto:
			case admin_teleport:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message3", activeChar));
					return false;
				}
				teleportTo(activeChar, activeChar, Util.joinStrings(" ", wordList, 1, 3), (ArrayUtils.valid(wordList, 4) != null && !ArrayUtils.valid(wordList, 4).isEmpty() ? Integer.parseInt(wordList[4]) : 0));
				break;
			case admin_walk:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message4", activeChar));
					return false;
				}
				try
				{
					activeChar.moveToLocation(Location.parseLoc(Util.joinStrings(" ", wordList, 1)), 0, true);
				}
				catch(IllegalArgumentException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message5", activeChar));
					return false;
				}
				break;
			case admin_gonorth:
			case admin_gosouth:
			case admin_goeast:
			case admin_gowest:
			case admin_goup:
			case admin_godown:
				int val = wordList.length < 2 ? 150 : Integer.parseInt(wordList[1]);
				int x = activeChar.getX();
				int y = activeChar.getY();
				int z = activeChar.getZ();
				if(command == Commands.admin_goup)
					z += val;
				else if(command == Commands.admin_godown)
					z -= val;
				else if(command == Commands.admin_goeast)
					x += val;
				else if(command == Commands.admin_gowest)
					x -= val;
				else if(command == Commands.admin_gosouth)
					y += val;
				else if(command == Commands.admin_gonorth)
					y -= val;

				activeChar.teleToLocation(x, y, z);
				showTeleportWindow(activeChar);
				break;
			case admin_tele:
				showTeleportWindow(activeChar);
				break;
			case admin_teleto:
			case admin_tele_to:
			case admin_instant_move:
				if(wordList.length > 1 && wordList[1].equalsIgnoreCase("r"))
					activeChar.setTeleMode(2);
				else if(wordList.length > 1 && wordList[1].equalsIgnoreCase("end"))
					activeChar.setTeleMode(0);
				else
					activeChar.setTeleMode(1);
				showTeleportWindow(activeChar);
				break;
			case admin_tonpc:
			case admin_to_npc:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message6", activeChar));
					return false;
				}
				String npcName = Util.joinStrings(" ", wordList, 1);
				NpcInstance npc;
				try
				{
					if((npc = GameObjectsStorage.getByNpcId(Integer.parseInt(npcName))) != null)
					{
						teleportToCharacter(activeChar, npc);
						return true;
					}
				}
				catch(Exception e)
				{}
				if((npc = GameObjectsStorage.getNpc(npcName)) != null)
				{
					teleportToCharacter(activeChar, npc);
					return true;
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message7", activeChar, npcName));
				showTeleportWindow(activeChar);
				break;
			case admin_toobject:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message8", activeChar));
					return false;
				}
				int target = Integer.parseInt(wordList[1]);
				GameObject obj;
				if((obj = GameObjectsStorage.findObject(target)) != null)
				{
					teleportToCharacter(activeChar, obj);
					return true;
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message9", activeChar, target));
				showTeleportWindow(activeChar);
				break;
		}

		switch(command)
		{
			case admin_teleport_character:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message10", activeChar));
					return false;
				}
				teleportCharacter(activeChar, Util.joinStrings(" ", wordList, 1));
				showTeleportCharWindow(activeChar);
				break;
			case admin_sendhome:
			case admin_teleporthome:
				try
				{
					Player target = wordList.length > 1 ? World.getPlayer(wordList[1]) : activeChar.getTarget().getPlayer();
					target.teleToLocation(target.getTemplate().spawnLoc, ReflectionManager.DEFAULT);
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //sendhome (target player); //sendhome playerName; //teleporthome playerName");
					return false;
				}
				break;
			case admin_sendtown:
			case admin_teleporttown:
				try
				{
					Player target = wordList.length > 1 ? World.getPlayer(wordList[1]) : activeChar.getTarget().getPlayer();
					if (wordList.length > 2)
						; // TODO: Teleport to specific town.
					else
						target.teleToClosestTown();
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //sendtown (target player); //sendtown playerName; //teleporttown playerName");
					return false;
				}
				break;
			case admin_recall:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message11", activeChar));
					return false;
				}
				String targetName = Util.joinStrings(" ", wordList, 1);
				Player recall_player = GameObjectsStorage.getPlayer(targetName);
				if(recall_player != null)
				{
					teleportTo(activeChar, recall_player, activeChar.getLoc(), activeChar.getReflectionId());
					return true;
				}
				int obj_id = CharacterDAO.getInstance().getObjectIdByName(targetName);
				if(obj_id > 0)
				{
					teleportCharacter_offline(obj_id, activeChar.getLoc());
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message12", activeChar, targetName));
				}
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message13", activeChar, targetName));
				break;
			case admin_setref:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message14", activeChar));
					return false;
				}

				int ref_id = Integer.parseInt(wordList[1]);
				if(ref_id != 0 && ReflectionManager.getInstance().get(ref_id) == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message15", activeChar, ref_id));
					return false;
				}

				GameObject target = activeChar;
				GameObject obj = activeChar.getTarget();
				if(obj != null)
					target = obj;

				target.setReflection(ref_id);
				target.decayMe();
				target.spawnMe();
				break;
			}
			case admin_getref:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message16", activeChar));
					return false;
				}
				Player cha = GameObjectsStorage.getPlayer(wordList[1]);
				if(cha == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message17", activeChar, wordList[1]));
					return false;
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message18", activeChar, wordList[1], activeChar.getReflectionId(), activeChar.getReflection().getName()));
				break;
			case admin_recall_npc:
				recallNPC(activeChar);
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void showTeleportWindow(Player activeChar)
	{
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/move.htm"));
	}

	private void showTeleportCharWindow(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		Player player = null;
		if(target!= null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuilder replyMSG = new StringBuilder("<html><title>Teleport Character</title>");
		replyMSG.append("<body>");
		replyMSG.append("The character you will teleport is " + player.getName() + ".");
		replyMSG.append("<br>");

		replyMSG.append("Co-ordinate x");
		replyMSG.append("<edit var=\"char_cord_x\" width=110>");
		replyMSG.append("Co-ordinate y");
		replyMSG.append("<edit var=\"char_cord_y\" width=110>");
		replyMSG.append("Co-ordinate z");
		replyMSG.append("<edit var=\"char_cord_z\" width=110>");
		replyMSG.append("<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		replyMSG.append("<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void teleportTo(Player activeChar, Player target, String Cords, int ref)
	{
		try
		{
			teleportTo(activeChar, target, Location.parseLoc(Cords), ref);
		}
		catch(IllegalArgumentException e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message19", activeChar));
			return;
		}
	}

	private void teleportTo(Player activeChar, Player target, Location loc, int ref)
	{
		if(!target.equals(activeChar))
			target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message20", target));

		target.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		target.teleToLocation(loc, ref);

		if(target.equals(activeChar))
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message21", activeChar, String.valueOf(loc), ref));
	}

	private void teleportCharacter(Player activeChar, String Cords)
	{
		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}
		if(target.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message22", activeChar));
			return;
		}
		teleportTo(activeChar, (Player) target, Cords, target.getReflectionId());
	}

	private void teleportCharacter_offline(int obj_id, Location loc)
	{
		if(obj_id == 0)
			return;

		Connection con = null;
		PreparedStatement st = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("UPDATE characters SET x=?,y=?,z=? WHERE obj_Id=? LIMIT 1");
			st.setInt(1, loc.x);
			st.setInt(2, loc.y);
			st.setInt(3, loc.z);
			st.setInt(4, obj_id);
			st.executeUpdate();
		}
		catch(Exception e)
		{

		}
		finally
		{
			DbUtils.closeQuietly(con, st);
		}
	}

	private void teleportToCharacter(Player activeChar, GameObject target)
	{
		if(target == null)
			return;

		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		activeChar.teleToLocation(target.getLoc(), target.getReflectionId());

		activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message23", activeChar, target));
	}

	private void recallNPC(Player activeChar)
	{
		GameObject obj = activeChar.getTarget();
		if(obj != null && obj.isNpc())
		{
			obj.setLoc(activeChar.getLoc());
			((NpcInstance) obj).broadcastCharInfo();
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message24", activeChar, obj.getName(), activeChar.getLoc().toString()));
		}
		else
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminteleport.message25", activeChar));
	}
}