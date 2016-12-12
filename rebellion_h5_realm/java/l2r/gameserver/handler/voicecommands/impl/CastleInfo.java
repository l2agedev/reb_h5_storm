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
package l2r.gameserver.handler.voicecommands.impl;


import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.Residence;
import l2r.gameserver.model.entity.residence.ResidenceType;
import l2r.gameserver.network.serverpackets.CastleSiegeInfo;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import org.apache.commons.lang3.math.NumberUtils;

public class CastleInfo implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"castle",
		"siege"
	};
	
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		
		if (command.equalsIgnoreCase("castle") || command.equalsIgnoreCase("siege"))
		{
			if (!Config.ENABLE_CASTLE_COMMAND)
				return false;
			
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			
			
			StringBuilder sb = new StringBuilder();
			
			for (Castle r : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			{
				sb.append("<center><a action=\"bypass -h user_castle " + r.getId() + "\"><font name=\"hs12\">" + r.getName() + "</font></a></center><br1>");
			}
			
			html.replace("%castle%", "" + sb.toString());
			html.setFile("mods/siege.htm");
			
			activeChar.sendPacket(html);
			
			if (!args.isEmpty() || args != null)
			{
				if (!NumberUtils.isNumber(args))
					return false;
				
				int castleID = Integer.valueOf(args);
				
				Residence r = ResidenceHolder.getInstance().getResidence(castleID);
				
				if(r == null)
					return false;
				else if (r.getType() == ResidenceType.Castle)
					activeChar.sendPacket(new CastleSiegeInfo((Castle) r, activeChar));
			}

		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
