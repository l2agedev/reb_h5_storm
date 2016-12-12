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
public final class AnswereCancelOfflineMode extends SendableConsolePacket
{
	public static final byte RESPONSE_CANCEL_SUCCESS = 0;
	public static final byte RESPONSE_CANCEL_FAILED = 1;
	
	private final int _requestId;
	private final byte _response;
	
	public AnswereCancelOfflineMode(final int requestId, final byte response)
	{
		_requestId = requestId;
		_response = response;
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x04);
		super.writeC(0x03);
		super.writeC(0x02);
		super.writeD(_requestId);
		super.writeC(_response);
	}
}