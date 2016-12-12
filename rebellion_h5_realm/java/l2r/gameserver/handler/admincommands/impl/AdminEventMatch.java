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
package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.ExEventMatchCreate;
import l2r.gameserver.network.serverpackets.ExEventMatchFirecracker;
import l2r.gameserver.network.serverpackets.ExEventMatchGMTest;
import l2r.gameserver.network.serverpackets.ExEventMatchManage;
import l2r.gameserver.network.serverpackets.ExEventMatchManage.EventMatchTeam;
import l2r.gameserver.network.serverpackets.ExEventMatchTeamInfo;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
/**
 * 
 * @author Nik
 *
 */
public class AdminEventMatch implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_eventmatch,
	};
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	@SuppressWarnings("unused")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		try
		{
			if (Commands.admin_eventmatch != comm)
				return false;
			
			if (wordList.length == 1)
			{
				activeChar.sendPacket(new ExEventMatchGMTest());
				return true;
			}
			
			int raceId;
			int teamId;
			switch (wordList[1])
			{
				case "create":
					raceId = Integer.parseInt(wordList[2]);
					String team1Name = wordList[3];
					String team2Name = wordList[4];
					int fenceX = Integer.parseInt(wordList[5]);
					int fenceY = Integer.parseInt(wordList[6]);
					int fenceZ = Integer.parseInt(wordList[7]);
					int width = Integer.parseInt(wordList[8]);
					int height = Integer.parseInt(wordList[9]);
					
					activeChar.sendPacket(new ExEventMatchCreate(raceId));
					EventMatchTeam team1 = new EventMatchTeam(team1Name, 1);
					EventMatchTeam team2 = new EventMatchTeam(team2Name, 2);
					ExEventMatchManage packet = new ExEventMatchManage(1, team1, team2);
					break;
				case "remove":
					raceId = Integer.parseInt(wordList[2]);
					break;
				case "fence":
					raceId = Integer.parseInt(wordList[2]);
					int fenceState = Integer.parseInt(wordList[3]); // 0 none, 1 down, 2 up
					break;
				case "leader":
					raceId = Integer.parseInt(wordList[2]);
					teamId = Integer.parseInt(wordList[3]);
					//wordList[3]; // Player name
					break;
				case "lock":
					raceId = Integer.parseInt(wordList[2]);
					teamId = Integer.parseInt(wordList[3]);
					activeChar.sendPacket(new ExEventMatchTeamInfo(raceId, teamId, activeChar));
					break;
				case "unlock":
					raceId = Integer.parseInt(wordList[2]);
					teamId = Integer.parseInt(wordList[3]);
					break;
				case "score":
					raceId = Integer.parseInt(wordList[2]);
					int team1Score = Integer.parseInt(wordList[3]);
					int team2Score = Integer.parseInt(wordList[4]);
					//new ExEventMatchScore(raceId, team1Score, team2Score);
					break;
				case "start":
					raceId = Integer.parseInt(wordList[2]);
					// Removes pause if paused.
					break;
				case "pause":
					raceId = Integer.parseInt(wordList[2]);
					// It should paralyze all players.
					break;
				case "useskill":
					raceId = Integer.parseInt(wordList[2]);
					int skillId = Integer.parseInt(wordList[3]);
					int skillLevel = Integer.parseInt(wordList[4]);
					// activeChar casts the skillId/Level on everyone in the event, doesnt matter if start or stop.
					break;
				case "dispellall":
					raceId = Integer.parseInt(wordList[2]);
					// removed the buffs of all players, doesnt matter if start or stop.
					break;
				case "msg":
					raceId = Integer.parseInt(wordList[2]);
					int msgType = Integer.parseInt(wordList[3]);
					if (msgType == 0) // Text message
						;//wordList[3] Text
					break;
				case "firecracker":
					Player player = World.getPlayer(wordList[2]);
					player.broadcastPacket(new ExEventMatchFirecracker(player.getObjectId()));
					break;
				case "list":
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message1", activeChar));
					//activeChar.sendMessage(raceId + ": '" + team1Name + "' vs '" + team2Name + "'");
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message2", activeChar));
					break;
				case "info":
					raceId = Integer.parseInt(wordList[2]);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message3", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message4", activeChar, raceId));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message5", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message6", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message7", activeChar)); // Fence center
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message8", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message9", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message10", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message11", activeChar));
					break;
				default: 
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message12", activeChar, fullString));
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace(); 
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineventmatch.message13", activeChar, fullString));
		}
		return true;
	}
}
