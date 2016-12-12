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
package l2r.gameserver.utils;

import l2r.gameserver.model.Player;

import java.util.List;

import javolution.util.FastTable;

/**
 * @author Nik
 */
public class PlayerEventStatus
{
	public Player player = null;
	public Location initLoc = new Location(0, 0, 0);
	public int initReflectionId = 0;
	public int initKarma = 0;
	public int initPvpKills = 0;
	public int initPkKills = 0;
	public String initTitle = "";
	public List<Player> kills = new FastTable<>();
	public boolean eventSitForced = false;
	
	public PlayerEventStatus(Player player)
	{
		this.player = player;
		initLoc = new Location(player.getX(), player.getY(), player.getZ(), player.getHeading());
		initReflectionId = player.getReflectionId();
		initKarma = player.getKarma();
		initPvpKills = player.getPvpKills();
		initPkKills = player.getPkKills();
		initTitle = player.getTitle();
		
	}
	
	public void restoreInits()
	{
		player.teleToLocation(initLoc, initReflectionId);
		player.setKarma(initKarma);
		player.setPvpKills(initPvpKills);
		player.setPkKills(initPkKills);
		player.setTitle(initTitle);
	}
}
