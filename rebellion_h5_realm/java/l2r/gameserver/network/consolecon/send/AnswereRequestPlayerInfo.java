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
package l2r.gameserver.network.consolecon.send;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.consolecon.SendableConsolePacket;

import java.util.Collection;

import org.inc.incolution.util.list.IncArrayList;

/**
 * @author Forsaiken
 */
public final class AnswereRequestPlayerInfo extends SendableConsolePacket
{
	public static final int RESPONSE_VIEW_PLAYER_SUCCESS = 0;
	public static final int RESPONSE_VIEW_PLAYER_FAILED = 1;
	
	public static final int RESPONSE_MODIFY_PLAYER_SUCCESS = 2;
	public static final int RESPONSE_MODIFY_PLAYER_FAILED = 3;
	
	public static final int RESPONSE_ADD_ITEM_SUCCESS = 10;
	public static final int RESPONSE_ADD_ITEM_FAILED = 11;
	public static final int RESPONSE_ADD_ITEM_FAILED_UNKNOWN_ITEM = 12;
	
	public static final int RESPONSE_DELETE_ITEM_SUCCESS = 20;
	public static final int RESPONSE_DELETE_ITEM_FAILED = 21;
	public static final int RESPONSE_DELETE_ITEM_FAILED_NOT_FOUND = 22;
	
	public static final int RESPONSE_MODIFY_ITEM_SUCCESS = 30;
	public static final int RESPONSE_MODIFY_ITEM_FAILED = 31;
	public static final int RESPONSE_MODIFY_ITEM_FAILED_UNKNOWN_ITEM = 32;
	
	public static final int RESPONSE_ADD_SKILL_SUCCESS = 40;
	public static final int RESPONSE_ADD_SKILL_FAILED = 41;
	public static final int RESPONSE_ADD_SKILL_FAILED_UNKNOWN_SKILL = 42;
	
	public static final int RESPONSE_DELETE_SKILL_SUCCESS = 50;
	public static final int RESPONSE_DELETE_SKILL_FAILED = 51;
	public static final int RESPONSE_DELETE_SKILL_FAILED_UNKNOWN_SKILL = 52;
	
	public static final int RESPONSE_MODIFY_SKILL_SUCCESS = 60;
	public static final int RESPONSE_MODIFY_SKILL_FAILED = 61;
	public static final int RESPONSE_MODIFY_SKILL_FAILED_UNKNOWN_SKILL = 62;
	
	public static final int RESPONSE_ADD_DELETE_ALL_SKILLS_SUCCESS = 65;
	public static final int RESPONSE_ADD_DELETE_ALL_SKILLS_FAILED = 66;
	
	public static final byte MODE_ERROR_PLAYER_NOT_FOUND = -2;
	public static final byte MODE_ERROR_PLAYER_OFFLINE = -1;
	public static final byte MODE_PLAYER_ONLINE = 0;
	public static final byte MODE_PLAYER_OFFLINE = 1;
	
	private final int _requestId;
	private final int _response;
	private final byte _mode;
	private final Player _player;
	
	private final ItemInstance[] _inventoryItems;
	private final ItemInstance[] _equpiedItems;
	private final Skill[] _activeSkills;
	private final Skill[] _passiveSkills;
	
	public AnswereRequestPlayerInfo(final int requestId, final int response, final byte mode, final Player player)
	{
		_requestId = requestId;
		_response = response;
		_mode = mode;
		_player = player;
		
		if (mode >= 0 && player == null)
			throw new NullPointerException();
		
		if (_player != null)
		{
			final ItemInstance[] allItems = _player.getInventory().getItems();
			final IncArrayList<ItemInstance> inventoryItems = new IncArrayList<>();
			final IncArrayList<ItemInstance> equpiedItems = new IncArrayList<>();
			for (final ItemInstance item : allItems)
			{
				if (item.isEquipped())
				{
					equpiedItems.add(item);
				}
				else
				{
					inventoryItems.add(item);
				}
			}
			_inventoryItems = inventoryItems.toArray(new ItemInstance[inventoryItems.size()]);
			_equpiedItems = equpiedItems.toArray(new ItemInstance[equpiedItems.size()]);
			
			final Collection<Skill> allSkills = _player.getAllSkills();
			final IncArrayList<Skill> activeSkills = new IncArrayList<>();
			final IncArrayList<Skill> passiveSkills = new IncArrayList<>();
			for (final Skill skill : allSkills)
			{
				if (skill.isPassive())
				{
					passiveSkills.add(skill);
				}
				else
				{
					activeSkills.add(skill);
				}
			}
			_activeSkills = activeSkills.toArray(new Skill[activeSkills.size()]);
			_passiveSkills = passiveSkills.toArray(new Skill[passiveSkills.size()]);
		}
		else
		{
			_inventoryItems = null;
			_equpiedItems = null;
			_activeSkills = null;
			_passiveSkills = null;
		}
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x04);
		super.writeC(0x03);
		super.writeC(0x00);
		super.writeD(_requestId);
		super.writeD(_response);
		super.writeC(_mode);
		
		if (_player != null)
		{
			super.writePlayer(_player, true);
			super.writeStats(_player);
			super.writeItems(_inventoryItems);
			super.writeItems(_equpiedItems);
			super.writeSkills(_activeSkills);
			super.writeSkills(_passiveSkills);
			super.writeClan(_player.getClan());
		}
	}
}