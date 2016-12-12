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
package l2r.gameserver.model.base;

import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.templates.StatsSet;

/**
 * @author FBIagent<br>
 */
public class AdminCommandAccessRight
{	
	/** The admin command<br> */
	private String _adminCommand = null;
	/** The access levels which can use the admin command<br> */
	private int _accessLevel;
	private boolean _requireConfirm;
	
	public AdminCommandAccessRight(StatsSet set)
	{
		_adminCommand = set.getString("command");
		_requireConfirm = set.getBool("confirmDlg", false);
		_accessLevel = set.getInteger("accessLevel", 7);		
	}
	
	public AdminCommandAccessRight(String command, boolean confirm, int level)
	{
		_adminCommand = command;
		_requireConfirm = confirm;
		_accessLevel = level;		
	}
	
	/**
	 * Returns the admin command the access right belongs to<br><br>
	 * 
	 * @return String: the admin command the access right belongs to<br>
	 */
	public String getAdminCommand()
	{
		return _adminCommand;
	}
	
	/**
	 * Checks if the given characterAccessLevel is allowed to use the admin command which belongs to this access right<br><br>
	 * 
	 * @param characterAccessLevel
	 * 
	 * @return boolean: true if characterAccessLevel is allowed to use the admin command which belongs to this access right, otherwise false<br>
	 */
	public boolean hasAccess(AccessLevel characterAccessLevel)
	{
		AccessLevel accessLevel = AdminTable.getInstance().getAccessLevel(_accessLevel);
		return characterAccessLevel.getLevel() == 100 || (accessLevel.getLevel() == characterAccessLevel.getLevel() || characterAccessLevel.hasChildAccess(accessLevel));
	}

	public boolean getRequireConfirm()
	{
		return _requireConfirm;
	}
}