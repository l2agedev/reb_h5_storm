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
package l2r.gameserver.randoms;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;

/**
 * @author Nik<br>
 */
public class CharacterNameColorization
{
	/**
	 * Broadcasts to known players CharInfo packet in oder to update name color.
	 * @param players : the persons which should broadcast their name color to others
	 */
	public static void updateColor(Player ... players)
	{
		if (!Config.CHARACTER_NAME_COLORIZATION)
			return;
		
		for (Player player : players)
		{
			if (player != null)
				player.broadcastCharInfo();
		}
	}
	
	public static int getColor(Player player, Player target)
	{
		int color = target.getNameColor();
		if (!Config.CHARACTER_NAME_COLORIZATION)
			return color;
		
		/*if (player != null && color == 0xFFFFFF && !player.hasCCPSetting(CharacterControlPanel.DISABLE_PLAYER_NAME_COLORING))
		{
			int relation = player.getRelation(target);
			
			if ((relation & RelationChanged.RELATION_HAS_PARTY) != 0)
				color = 0x00FF00; // Party green
			else if ((relation & RelationChanged.RELATION_CLAN_MATE) != 0)
				color = 0xCA6A6A; // Clan purple
			else if (player.getAllyId() > 0 && player.getAllyId() == target.getAllyId())
				color = 0x88FF55; // Ally green
			else if ((relation & RelationChanged.RELATION_MUTUAL_WAR) != 0)
				color = 0x555555;// Gray-black								writeD(0x000099); Darkish red
			else if ((relation & RelationChanged.RELATION_1SIDED_WAR) != 0)
				color = 0x999999;// Gray									writeD(0x5555AA); Brownish red
		}*/
		
		if (player != null && color == 0xFFFFFF && !player.getVarB("noColor"))
		{
			if (player.isInParty() && player.getParty().containsMember(target))
				color = 0x00FF00; // Party green
			else if (player.getClan() != null)
			{
				if (player.getClanId() == target.getClanId())
					color = 0xCA6A6A; // Clan purple
				else if (player.getClan().isAtWarWith(target.getClanId()))
				{
					if (target.getClan() != null && target.getClan().isAtWarWith(player.getClanId()))
						color = 0x555555;// Gray-black													writeD(0x000099); Darkish red
					else
						color = 0x999999;// Gray 														writeD(0x5555AA); Brownish red
				}
				else if (player.getAllyId() > 0 && player.getAllyId() == target.getAllyId())
					color = 0x88FF55; // Ally green
			}
			
		}
		
		return color;
	}
}
