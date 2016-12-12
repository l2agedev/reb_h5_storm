/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.instancemanager.OfflineBufferManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;

public class Offlinebuff extends Functions implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"offlinebuff"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if (activeChar == null)
			return false;
		
		if (!Config.ENABLE_OFFLINE_BUFFERS)
			return false;
		
		OfflineBufferManager.getInstance().processBypass(activeChar, "showSellerMenu");
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
}
