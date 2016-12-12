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

import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.ServerConsole;

/**
 * @author Forsaiken
 */
public final class RequestListenServerConsole extends ReceivableConsolePacket
{
	private boolean _listen;
	
	@Override
	protected final void readImpl()
	{
		_listen = super.readC() == 1;
	}
	
	@Override
	protected final void runImpl()
	{
		if (_listen)
		{
			ServerConsole.getInstance().addListener(super.getClient());
		}
		else
		{
			ServerConsole.getInstance().removeListener(super.getClient());
		}
	}
}