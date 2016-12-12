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

import l2r.gameserver.network.consolecon.SendableConsolePacket;

/**
 * @author Forsaiken
 */
public final class AnswereRequestListenServerConsole extends SendableConsolePacket
{
	public static final AnswereRequestListenServerConsole STATIC_PACKET_LISTEN_ENABLE = new AnswereRequestListenServerConsole(true);
	public static final AnswereRequestListenServerConsole STATIC_PACKET_LISTEN_DISABLE = new AnswereRequestListenServerConsole(false);
	
	private final boolean _listen;
	
	private AnswereRequestListenServerConsole(final boolean listen)
	{
		_listen = listen;
	}
	
	@Override
	protected final void writeImpl()
	{
		super.writeC(0x04);
		super.writeC(0x05);
		super.writeC(0x00);
		super.writeC(_listen ? 1 : 0);
	}
}