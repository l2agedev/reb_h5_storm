/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 *
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

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class AdminEvent implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_event_draw,
		admin_get_random_player,
	}
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		//INFO: Ascii generator - http://lunatic.no/ol/img2aschtml.php
		if (activeChar == null)
			return false;
		
		int adenaId = 57;
		int adenaMinCount = 1;
		int adenaMaxCount = 1;
		StringTokenizer st = new StringTokenizer(fullString);
		String actualCommand = st.nextToken(); // Get actual command

		if (actualCommand.startsWith("admin_event_draw"))
		{
			String fieldName = "";
			try
			{
				int token = 0;
				while (st.hasMoreTokens())
				{
					token++;
					String param = st.nextToken();
					if (token == 1)
						fieldName = param;
					else if (Util.isDigit(param) && token == 2)
						adenaId = Integer.parseInt(param);
					else if (Util.isDigit(param) && token == 3)
						adenaMinCount = Integer.parseInt(param);
					else if (Util.isDigit(param) && token == 4)
						adenaMaxCount = Integer.parseInt(param);
				}
				if (fieldName.startsWith("random"))
				{
					int dx = 1;
					int dy = 1;
					int fill = 100;
					String[] fnSplit = fieldName.split("_");
					for (int i = 0; i < fnSplit.length; i++)
					{
						if (i == 1)
							dx = Integer.parseInt(fnSplit[i]);
						else if (i == 2)
							dy = Integer.parseInt(fnSplit[i]);
						else if (i == 3)
							fill = Integer.parseInt(fnSplit[i]);
					}
					drawText(activeChar, generateRandom(dx, dy, fill), 5, 5, adenaId, adenaMinCount, adenaMaxCount);
				}
				else
				{
					File file = new File(Config.DATAPACK_ROOT, "/data/events/eventdraw/" + fieldName + ".txt");
					if (!file.exists())
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminevent.message1", activeChar, file.getPath()));
					else
					{
						try
						{
							Scanner s = new Scanner(file);
							StringBuilder sb = new StringBuilder((int) file.length());
							while (s.hasNextLine())
							{
								String line = s.nextLine();
								if (line.isEmpty())
									continue;
								
								sb.append(line);
								sb.append(";");
							}
							s.close();
							drawText(activeChar, sb.toString(), 5, 5, adenaId, adenaMinCount, adenaMaxCount);
						}
						catch (Exception e)
						{
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminevent.message2", activeChar, file.getPath(), e.getMessage()));
							activeChar.sendMessage(e.getStackTrace()[0].toString());
							activeChar.sendMessage(e.getStackTrace()[1].toString());
							activeChar.sendMessage(e.getStackTrace()[2].toString());
							e.printStackTrace();
							return false;
						}
					}
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminevent.message3", activeChar, fieldName));
				e.printStackTrace();
			}
		}
		else if (actualCommand.startsWith("admin_get_random_player"))
		{
			try
			{
				int radius = Integer.parseInt(st.nextToken());
				List<Player> playersAround = new ArrayList<>();
				playersAround.addAll(World.getAroundPlayers(activeChar, radius, radius));
				Collections.shuffle(playersAround);
				
				Player chosenPlayer = null;
				if (!playersAround.isEmpty())
				{
					chosenPlayer = playersAround.get(0);
					
					for (Player player : playersAround)
					{
						if (chosenPlayer == null)
						{
							player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminevent.message4", player));
						}
						else
							player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminevent.message5", player, chosenPlayer.getName()));
					}
				}
				if (chosenPlayer == null) {
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminevent.message6", activeChar));
				} else {
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminevent.message7", activeChar, chosenPlayer.getName()));
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminevent.message8", activeChar));
			}
		}
		
		return true;
	}
	
	private void drawText(Player player, String toDraw, int linePointOffset, int lineToLineOffset, int adenaId, long adenaMinCount, long adenaMaxCount)
	{
		int[] startXYZ = new int[3];
		startXYZ[0] = player.getX();
		startXYZ[1] = player.getY();
		startXYZ[2] = player.getZ();
		
		int destX = player.getX();
		int destY = player.getY();

		String[] toDrawLines = toDraw.split(";");
		int itemIdToDrop = 0;
		
		for (int k = 0; k < toDrawLines.length; k++) // Width
		{
			for (int i = 0; i < toDrawLines[k].length(); i++) // Length
			{
				switch(toDrawLines[k].charAt(i))
				{
					case 'A':
					case '#':
					case '1':
						itemIdToDrop = adenaId;
						break;
					case 'S':
						itemIdToDrop = 1785;
						break;
					default:
						itemIdToDrop = 0;
						
				}
				
				// Drop an item at this point of the line
				dropEventItem(player, itemIdToDrop, Rnd.get(adenaMinCount, adenaMaxCount), destX, destY, startXYZ[2], player.getReflectionId());
				
				destX += linePointOffset;
			}
			
			// Start a new line, nullfy destX
			destY += lineToLineOffset;
			destX = startXYZ[0];
		}
	}
	
	private void dropEventItem(Player player, int itemId, long num, int x, int y, int z, int reflectionId)
	{
		if (itemId == 0)
			return;
		
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setCount(num);
		item.setReflection(reflectionId);
		item.setDropTime(285000);
		item.dropToTheGround(player, new Location(x, y, z));
	}
	
	private String generateRandom(int dx, int dy, int fillPercent)
	{
		StringBuilder sb = new StringBuilder((dx*dy) + dy);
		for (int y = 0; y < dy; y++)
		{
			for (int x = 0; x < dx; x++)
			{
				if (Rnd.get(100) < fillPercent)
					sb.append("#");
				else
					sb.append(" ");
			}
			sb.append(";");
		}
		
		return sb.toString();
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
