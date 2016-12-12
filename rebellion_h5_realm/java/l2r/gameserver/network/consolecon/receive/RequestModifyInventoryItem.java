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

import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Element;
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
public final class RequestModifyInventoryItem extends ReceivableConsolePacket
{
	public static final int ENCHANT_ATTRIBUTE_FIRE = 1;
	public static final int ENCHANT_ATTRIBUTE_WATER = 2;
	public static final int ENCHANT_ATTRIBUTE_WIND = 3;
	public static final int ENCHANT_ATTRIBUTE_EARTH = 4;
	public static final int ENCHANT_ATTRIBUTE_HOLY = 5;
	public static final int ENCHANT_ATTRIBUTE_UNHOLY = 6;
	
	private static final int MODIFICATION_SET_ENCHANT_LEVEL = 1;
	private static final int MODIFICATION_SET_AUGMENTATION = 2;
	private static final int MODIFICATION_SET_ENCHANT_ATTRIBIUTE = 3;
	private static final int MODIFICATION_SET_ITEM_ID = 4;
	private static final int MODIFICATION_SET_ITEM_COUNT = 5;
	
	private int _requestId;
	private int _mode;
	private String _playerName;
	
	private int _itemObjId;
	private int[][] _modifications;
	
	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_mode = super.readC();
		_playerName = super.readS();
		
		_itemObjId = super.readD();
		final int size = super.readH();
		_modifications = new int[size][3];
		for (int i = 0; i < size; i++)
		{
			_modifications[i][0] = super.readC();
			_modifications[i][1] = super.readD();
			_modifications[i][2] = super.readD();
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
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_ITEM_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_NOT_FOUND, null));
				break;
			}
				
			case Console.PLAYER_STATUS_OFFLINE:
			{
				console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_ITEM_FAILED, AnswereRequestPlayerInfo.MODE_ERROR_PLAYER_OFFLINE, null));
				break;
			}
				
			case Console.PLAYER_STATUS_ONLINE_MODE:
			case Console.PLAYER_STATUS_OFFLINE_MODE:
			{
				final Player player = reply.getPlayer();
				final byte mode = reply.getStatus() == Console.PLAYER_STATUS_ONLINE_MODE ? AnswereRequestPlayerInfo.MODE_PLAYER_ONLINE : AnswereRequestPlayerInfo.MODE_PLAYER_OFFLINE;
				final ItemInstance item = player.getInventory().getItemByObjectId(_itemObjId);
				if (item != null)
				{
					for (final int[] modification : _modifications)
					{
						switch (modification[0])
						{
							case MODIFICATION_SET_ENCHANT_LEVEL:
								item.setEnchantLevel(modification[1]);
								break;
							case MODIFICATION_SET_AUGMENTATION:
								if (modification[1] != 0)
									item.setAugmentationId(modification[1]);
								break;
							case MODIFICATION_SET_ENCHANT_ATTRIBIUTE:
								item.setAttributeElement(Element.getElementById(modification[1]), modification[2]);
								break;
							case MODIFICATION_SET_ITEM_ID:
								if (ItemHolder.getInstance().getTemplate(modification[1]) != null)
									item.setItemId(modification[1]);
								break;
							case MODIFICATION_SET_ITEM_COUNT:
								item.setCount(modification[1]);
								break;
						}
					}
					
					// Instantly save the item modification.
					item.update();
					player.sendItemList(true);
					
					ConsoleController.CONSOLE_LOG.log(Level.INFO, console + " changed player: '" + player.getName() + "', changed item: '" + item.getItemId() + '-' + item.getName() + "-(" + item.getObjectId() + '|' + item.getCount() + '|' + item.getEnchantLevel() + '-' + item.getAugmentationId() + '-' + item.getAttackElement().getId() + '-' + item.getAttackElementValue() + ")'");
					console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_MODIFY_ITEM_SUCCESS, mode, player));
				}
				else
				{
					console.sendPacket(new AnswereRequestPlayerInfo(_requestId, AnswereRequestPlayerInfo.RESPONSE_DELETE_ITEM_FAILED_NOT_FOUND, mode, player));
				}
				break;
			}
		}
	}
}