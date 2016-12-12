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
package l2r.gameserver.network.consolecon.receive;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.consolecon.Console;
import l2r.gameserver.network.consolecon.ConsoleController;
import l2r.gameserver.network.consolecon.ConsoleController.OfflineModeStatusReply;
import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.send.AnswereRequestPlayerInfo;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.tables.SkillTable;

import java.util.logging.Level;

/**
 * @author Forsaiken
 */
public final class RequestAddSkill extends ReceivableConsolePacket
{
	private int _requestId;
	private int _mode;
	private String _playerName;
	
	private int _skillId;
	private int _skillLevel;
	
	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_mode = super.readC();
		_playerName = super.readS();
		
		_skillId = super.readH();
		_skillLevel = super.readH();
	}
	
	@Override
	protected final void runImpl()
	{
		final Console console = super.getClient();
		
		final OfflineModeStatusReply reply = ConsoleController.getInstance().getPlayer(_playerName, _mode == AnswereRequestPlayerInfo.MODE_PLAYER_OFFLINE);
		switch (reply.getStatus())
		{
			case Console.PLAYER_STATUS_NOT_FOUND:
			{
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_SKILL_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_NOT_FOUND, null));
				break;
			}
				
			case Console.PLAYER_STATUS_OFFLINE:
			{
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_SKILL_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_OFFLINE, null));
				break;
			}
				
			case Console.PLAYER_STATUS_ONLINE_MODE:
			case Console.PLAYER_STATUS_OFFLINE_MODE:
			{
				final Player player = reply.getPlayer();
				final byte mode = reply.getStatus() == Console.PLAYER_STATUS_ONLINE_MODE ? AnswereRequestPlayerInfo.MODE_PLAYER_ONLINE : AnswereRequestPlayerInfo.MODE_PLAYER_OFFLINE;
				final Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
				if (skill != null)
				{
					player.addSkill(skill, true);
					player.sendPacket(new SkillList(player));
					ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', added skill: '" + skill.getId() + '-' + skill.getLevel() + '\'');
					console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_SKILL_SUCCESS, mode, player));
				}
				else
				{
					console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_SKILL_FAILED_UNKNOWN_SKILL, mode, player));
				}
				break;
			}
		}
	}
}