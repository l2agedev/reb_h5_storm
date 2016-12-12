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
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.consolecon.Console;
import l2r.gameserver.network.consolecon.ConsoleController;
import l2r.gameserver.network.consolecon.ConsoleController.OfflineModeStatusReply;
import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.send.AnswereRequestPlayerInfo;

import java.util.logging.Level;

/**
 * @author Forsaiken
 */
public final class RequestAddInventoryItem extends ReceivableConsolePacket
{
	private int _requestId;
	private int _mode;
	private String _playerName;
	
	private int _itemId;
	private long _itemCount;
	
	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_mode = super.readC();
		_playerName = super.readS();
		
		_itemId = super.readD();
		_itemCount = super.readQ();
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
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_ITEM_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_NOT_FOUND, null));
				break;
			}
				
			case Console.PLAYER_STATUS_OFFLINE:
			{
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_ITEM_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_OFFLINE, null));
				break;
			}
				
			case Console.PLAYER_STATUS_ONLINE_MODE:
			case Console.PLAYER_STATUS_OFFLINE_MODE:
			{
				final Player player = reply.getPlayer();
				final byte mode = reply.getStatus() == Console.PLAYER_STATUS_ONLINE_MODE ? AnswereRequestPlayerInfo.MODE_PLAYER_ONLINE : AnswereRequestPlayerInfo.MODE_PLAYER_OFFLINE;
				
				final ItemInstance item = player.getInventory().addItem(_itemId, _itemCount);
				if (item != null)
				{
					ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', added item: '" + item.getItemId() + '-' + item.getName() + "-(" + item.getObjectId() + '|' + item.getCount() + '|' + item.getEnchantLevel() + '-' + item.getAugmentationId() + '-' + item.getAttackElement().ordinal() + '-' + item.getAttackElementValue() + ")'");
					console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_ITEM_SUCCESS, mode, player));
				}
				else
				{
					console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_ADD_ITEM_FAILED_UNKNOWN_ITEM, mode, player));
				}
				break;
			}
		}
	}
}