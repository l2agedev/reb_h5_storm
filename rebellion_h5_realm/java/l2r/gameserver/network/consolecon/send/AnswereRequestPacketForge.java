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
public final class AnswereRequestPacketForge extends SendableConsolePacket
{
	private static final String[] EMPTY_PLAYERS = new String[0];
	
	public static final byte RESPONSE_SUCCESS = 1;
	public static final byte RESPONSE_FAILED = 2;
	
	private final int _requestId;
	private final byte _response;
	private final String[] _players;
	
	public AnswereRequestPacketForge(int requestId, final byte response, final String[] players)
	{
		_requestId = requestId;
		_response = response;
		_players = players == null ? EMPTY_PLAYERS : players;
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x04);
		super.writeC(0x00);
		super.writeD(_requestId);
		super.writeC(_response);
		super.writeD(_players.length);
		for (final String player : _players)
		{
			super.writeS(player);
		}
	}
}