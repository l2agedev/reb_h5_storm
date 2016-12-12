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
public final class ServerHello extends SendableConsolePacket
{
	private static final byte[] EMPTY_ARRAY = new byte[0];
	
	public static final byte STATE_SUCCESS = 1;
	public static final byte STATE_ERROR = 2;
	public static final byte STATE_PASS_WRONG = 3;
	
	private final byte _response;
	private final byte[] _blowfishKey;
	
	public ServerHello(final byte response, final byte[] blowfishKey)
	{
		_response = response;
		_blowfishKey = blowfishKey == null ? EMPTY_ARRAY : blowfishKey;
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x00);
		super.writeC(_response);
		super.writeH(_blowfishKey.length);
		super.writeB(_blowfishKey);
		super.writeC(0x01);
	}
}