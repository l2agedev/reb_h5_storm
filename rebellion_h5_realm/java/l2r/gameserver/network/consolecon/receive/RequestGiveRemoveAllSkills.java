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

import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.SkillLearn;
import l2r.gameserver.model.base.AcquireType;
import l2r.gameserver.network.consolecon.Console;
import l2r.gameserver.network.consolecon.ConsoleController;
import l2r.gameserver.network.consolecon.ConsoleController.OfflineModeStatusReply;
import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.send.AnswereRequestPlayerInfo;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.tables.SkillTable;

import java.util.Collection;
import java.util.logging.Level;

/**
 * @author Forsaiken
 */
public final class RequestGiveRemoveAllSkills extends ReceivableConsolePacket
{
	public static final byte REQUEST_GIVE_ALL_SKILLS = 0;
	public static final byte REQUEST_REMOVE_ALL_SKILLS = 1;
	
	private int _requestId;
	private int _mode;
	private String _playerName;
	
	private int _request;
	
	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_mode = super.readC();
		_playerName = super.readS();
		
		_request = super.readC();
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
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_DELETE_ALL_SKILLS_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_NOT_FOUND, null));
				break;
			}
				
			case Console.PLAYER_STATUS_OFFLINE:
			{
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_DELETE_ALL_SKILLS_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_OFFLINE, null));
				break;
			}
				
			case Console.PLAYER_STATUS_ONLINE_MODE:
			case Console.PLAYER_STATUS_OFFLINE_MODE:
			{
				final Player player = reply.getPlayer();
				switch (_request)
				{
					case REQUEST_GIVE_ALL_SKILLS:
					{
						giveAllSkills(player);
						player.sendPacket(new SkillList(player));
						final byte mode = reply.getStatus() == Console.PLAYER_STATUS_ONLINE_MODE ? AnswereRequestPlayerInfo.MODE_PLAYER_ONLINE : AnswereRequestPlayerInfo.MODE_PLAYER_OFFLINE;
						ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', gave all class skills.");
						console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_DELETE_ALL_SKILLS_SUCCESS, mode, player));
						break;
					}
						
					case REQUEST_REMOVE_ALL_SKILLS:
					{
						for (final Skill skill : player.getAllSkills())
							player.removeSkill(skill, true);
						
						player.sendPacket(new SkillList(player));
						player.broadcastUserInfo(true);
						final byte mode = reply.getStatus() == Console.PLAYER_STATUS_ONLINE_MODE ? AnswereRequestPlayerInfo.MODE_PLAYER_ONLINE : AnswereRequestPlayerInfo.MODE_PLAYER_OFFLINE;
						ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', removed all skills.");
						console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_DELETE_ALL_SKILLS_SUCCESS, mode, player));
						break;
					}
				}
				break;
			}
		}
	}
	
	private final void giveAllSkills(final Player player)
	{
		int unLearnable = 0;
		int skillCounter = 0;
		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);
		while(skills.size() > unLearnable)
		{
			unLearnable = 0;
			for(SkillLearn s : skills)
			{
				Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || !sk.getCanLearn(player.getClassId()))
				{
					unLearnable++;
					continue;
				}
				if(player.getSkillLevel(sk.getId()) == -1)
					skillCounter++;
				player.addSkill(sk, true);
			}
			skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);
		}

		player.sendMessage(player.isLangRus() ? "Администратор дал вам " + skillCounter + " умения." : "Admin gave you " + skillCounter + " skills.");
		player.sendPacket(new SkillList(player));
	}
}