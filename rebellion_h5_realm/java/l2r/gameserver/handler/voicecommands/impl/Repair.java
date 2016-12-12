/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.handler.voicecommands.impl;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.ItemsDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.ItemInstance.ItemLocation;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripts.Functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class Repair extends Functions implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"repair",
		"startrepair"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (activeChar == null)
			return false;
		
		if (!Config.ENABLE_REPAIR_COMMAND)
			return false;
		String repairChar = null;
		try
		{
			if (target != null)
				if (target.length() > 1)
				{
					String[] cmdParams = target.split(" ");
					repairChar = cmdParams[0];
				}
		}
		catch (Exception e)
		{
			repairChar = null;
		}
		
		// Send activeChar HTML page
		if (command.startsWith("repair"))
		{
			String htmContent;
			if (!getCharList(activeChar).isEmpty())
				htmContent = HtmCache.getInstance().getNotNull("mods/repair/repair.htm", activeChar);
			else
				htmContent = HtmCache.getInstance().getNotNull("mods/repair/repair-no-chars.htm", activeChar);
			
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			npcHtmlMessage.replace("%acc_chars%", getCharList(activeChar));
			activeChar.sendPacket(npcHtmlMessage);
			return true;
		}
		// Command for enter repairFunction from html
		if (command.startsWith("startrepair") && (repairChar != null))
		{
			// _log.warning("Repair Attempt: Character " + repairChar);
			if (checkAcc(activeChar, repairChar))
			{
				if (checkChar(activeChar, repairChar))
				{
					String htmContent = HtmCache.getInstance().getNotNull("mods/repair/repair-self.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.setHtml(htmContent);
					activeChar.sendPacket(npcHtmlMessage);
					return false;
				}
				else if (checkJail(repairChar))
				{
					String htmContent = HtmCache.getInstance().getNotNull("mods/repair/repair-jail.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.setHtml(htmContent);
					activeChar.sendPacket(npcHtmlMessage);
					return false;
				}
				else
				{
					repairBadCharacter(repairChar);
					String htmContent = HtmCache.getInstance().getNotNull("mods/repair/repair-done.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.setHtml(htmContent);
					activeChar.sendPacket(npcHtmlMessage);
					return true;
				}
			}
			String htmContent = HtmCache.getInstance().getNotNull("mods/repair/repair-error.htm", activeChar);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			npcHtmlMessage.setHtml(htmContent);
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		// _log.warning("Repair Attempt: Failed. ");
		return false;
	}
	
	private String getCharList(Player activeChar)
	{
		String result = "";
		String repCharAcc = activeChar.getAccountName();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
		    statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=?");
			statement.setString(1, repCharAcc);
			rset = statement.executeQuery();
			while (rset.next())
			{
				if (activeChar.getName().compareTo(rset.getString(1)) != 0)
					result += rset.getString(1) + ";";
			}
			// _log.warning("Repair Attempt: Output Result for searching characters on account:"+result);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return result;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}
	
	private boolean checkAcc(Player activeChar, String repairChar)
	{
		boolean result = false;
		String repCharAcc = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, repairChar);
			rset = statement.executeQuery();
			if (rset.next())
			{
				repCharAcc = rset.getString(1);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return result;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		if (activeChar.getAccountName().compareTo(repCharAcc) == 0)
			result = true;
		return result;
	}
	
	private boolean checkJail(String repairChar)
	{
		boolean result = false;
		int playerToCheck = CharacterDAO.getInstance().getObjectIdByName(repairChar);
		
		String varName = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM character_variables WHERE obj_id=? AND name='jailed'");
			statement.setInt(1, playerToCheck);
			rset = statement.executeQuery();
			if (rset.next())
			{
				varName = rset.getString("name");
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		if (varName != null && varName.equals("jailed"))
			result = true;
		
		return result;
	}
	
	private boolean checkChar(Player activeChar, String repairChar)
	{
		boolean result = false;
		if (activeChar.getName().compareTo(repairChar) == 0)
			result = true;
		return result;
	}
	
	private void repairBadCharacter(String charName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_id FROM characters WHERE char_name=?");
			statement.setString(1, charName);
			rset = statement.executeQuery();
			
			int objId = 0;
			if (rset.next())
			{
				objId = rset.getInt(1);
			}
			DbUtils.closeQuietly(statement, rset);
			if (objId == 0)
			{
				DbUtils.close(con);
				return;
			}
			
			Player player = World.getPlayer(objId);
			if (player != null && player.isOnline() && !player.isLogoutStarted())
				player.kick();
			
			statement = con.prepareStatement("UPDATE characters SET x=17144, y=170156, z=-3502 WHERE obj_id=?");
			statement.setInt(1, objId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_variables WHERE obj_id=? AND type='user-var' AND name='reflection'");
			statement.setInt(1, objId);
			statement.execute();
			DbUtils.close(statement);
			
			Collection<ItemInstance> items = ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(objId, ItemLocation.PAPERDOLL);
			for(ItemInstance item : items)
			{
				if(item.isEquipped())
				{
					item.setEquipped(false);
					item.setJdbcState(JdbcEntityState.UPDATED);
					item.update();
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("GameServer: could not repair character:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}