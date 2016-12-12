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
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.network.consolecon.Console;
import l2r.gameserver.network.consolecon.ConsoleController;
import l2r.gameserver.network.consolecon.ConsoleController.OfflineModeStatusReply;
import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.send.AnswereRequestPlayerInfo;

import java.util.logging.Level;

/**
 * @author Forsaiken
 */
public final class RequestChangePlayer extends ReceivableConsolePacket
{
	private static final byte RQUEST_CHANGE_LEVEL = 0;
	private static final byte RQUEST_CHANGE_TITLE = 1;
	private static final byte RQUEST_CHANGE_BASE_CLASS = 2;
	private static final byte RQUEST_CHANGE_ADD_SUB_CLASS = 3;
	private static final byte RQUEST_CHANGE_SUB_CLASS = 4;
	private static final byte RQUEST_CHANGE_SET_ACTIVE_CLASS = 5;
	private static final byte RQUEST_CHANGE_SET_NOBLE = 6;
	
	private int _requestId;
	private int _mode;
	private String _playerName;
	
	private int _request;
	private String _text;
	private int[] _numbers;
	
	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_mode = super.readC();
		_playerName = super.readS();
		
		_request = super.readC();
		_text = super.readS();
		
		final int size = super.readH();
		_numbers = new int[size];
		for (int i = 0; i < size; i++)
		{
			_numbers[i] = super.readC();
		}
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
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_NOT_FOUND, null));
				break;
			}
				
			case Console.PLAYER_STATUS_OFFLINE:
			{
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_OFFLINE, null));
				break;
			}
				
			case Console.PLAYER_STATUS_ONLINE_MODE:
			case Console.PLAYER_STATUS_OFFLINE_MODE:
			{
				final Player player = reply.getPlayer();
				final byte mode = reply.getStatus() == Console.PLAYER_STATUS_ONLINE_MODE ? AnswereRequestPlayerInfo.MODE_PLAYER_ONLINE : AnswereRequestPlayerInfo.MODE_PLAYER_OFFLINE;
				
				switch (_request)
				{
					case RQUEST_CHANGE_LEVEL:
					{
						if (_numbers.length != 0 && _numbers[0] >= 1 && _numbers[0] <= Experience.getMaxLevel())
						{
							final long pXp = player.getExp();
							final long tXp = Experience.getExpForLevel(_numbers[0]);
							
							if (pXp > tXp)
							{
								player.addExpAndSp(-(pXp - tXp), 0); // Remove
							}
							else if (pXp < tXp)
							{
								player.addExpAndSp(tXp - pXp, 0);
							}
							
							ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', new level: '" + _numbers[0] + '\'');
							console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_SUCCESS, mode, player));
							return;
						}
						break;
					}
						
					case RQUEST_CHANGE_TITLE:
					{
						player.setTitle(_text);
						player.broadcastUserInfo(true);
						ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', new title: '" + _text + '\'');
						console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_SUCCESS, mode, player));
						return;
					}
						
					case RQUEST_CHANGE_BASE_CLASS:
					{
						if (_numbers.length != 0 && classIdValid(_numbers[0]) && (player.getBaseClassId() != _numbers[0]))
						{
							if (!player.isSubClassActive())
							{
								player.setClassId(_numbers[0], false, false);
							}
							
							player.setBaseClass(_numbers[0]);
							
							player.broadcastUserInfo(true);
							ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', new base class: '" + _numbers[0] + '\'');
							console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_SUCCESS, mode, player));
							return;
						}
						break;
					}
						
					case RQUEST_CHANGE_ADD_SUB_CLASS:
					{
						if (_numbers.length != 0 && classIdValid(_numbers[0]) && player.addSubClass(_numbers[0], true, 0));
						{
							console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_SUCCESS, mode, player));
							return;
						}
					}
						
					case RQUEST_CHANGE_SUB_CLASS:
					{
						if (_numbers.length != 0 && _numbers[0] >= 1 && classIdValid(_numbers[1]))
						{
							/*if (player.getClassIndex() == _numbers[0]) TODO FIX
								player.setActiveClass(0);*/ 
							
							if (player.modifySubClass(player.getClassId().getId(), _numbers[1]))
							{
								ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', sub class changed: '" + _numbers[0] + '-' + _numbers[1] + '\'');
								console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_SUCCESS, mode, player));
								return;
							}
						}
						break;
					}
					
					case RQUEST_CHANGE_SET_ACTIVE_CLASS:
					{
						if (_numbers.length != 0 && _numbers[0] >= 0)
						{
							player.setActiveSubClass(_numbers[0], true);
							console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_SUCCESS, mode, player));
						}
						break;
					}
					
					case RQUEST_CHANGE_SET_NOBLE:
					{
						if (_numbers.length != 0)
						{
							player.setNoble(_numbers[0] > 0);
							player.broadcastUserInfo(true);
							ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', set noble: '" + (_numbers[0] > 0) + '\'');
							console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_SUCCESS, mode, player));
						}
						break;
					}
				}
				
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_PLAYER_FAILED, mode, player));
			}
		}
	}
	
	private final boolean classIdValid(final int classId)
	{
		if (classId < 0)
			return false;
		
		final ClassId[] classes = ClassId.values();
		if (classId >= classes.length)
			return false;
		
		return classes[classId].getRace() != null;
	}
}