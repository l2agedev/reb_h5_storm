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
public final class AnswereRequestAuth extends SendableConsolePacket
{
	public static final AnswereRequestAuth STATIC_PACKET_AUTH_SUCESSFULLY = new AnswereRequestAuth(1);
	public static final AnswereRequestAuth STATIC_PACKET_AUTH_FAILED_ERROR = new AnswereRequestAuth(2);
	public static final AnswereRequestAuth STATIC_PACKET_AUTH_FAILED_WRONG_PASS_IDENT = new AnswereRequestAuth(3);
	public static final AnswereRequestAuth STATIC_PACKET_AUTH_FAILED_ALREADY_ONLINE = new AnswereRequestAuth(4);
	
	private final int _response;
	
	private AnswereRequestAuth(final int response)
	{
		_response = response;
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x01);
		super.writeC(_response);
	}
}