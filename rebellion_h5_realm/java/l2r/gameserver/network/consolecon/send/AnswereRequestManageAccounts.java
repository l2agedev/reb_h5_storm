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
public final class AnswereRequestManageAccounts extends SendableConsolePacket
{
	private static final String[] EMPTY_IDENTS = new String[0];
	
	public static final byte RESPONSE_SHOW_ACCOUNTS_SUCCESS = 1;
	public static final byte RESPONSE_ADD_ACCOUNT_SUCCESS = 2;
	public static final byte RESPONSE_REMOVE_ACCOUNTS_SUCCESS = 3;
	
	public static final byte RESPONSE_SHOW_ACCOUNTS_FAILED = 4;
	public static final byte RESPONSE_ADD_ACCOUNT_FAILED = 5;
	public static final byte RESPONSE_REMOVE_ACCOUNTS_FAILED = 6;
	
	private final int _requestId;
	private final byte _reponse;
	private final String[] _idents;
	
	public AnswereRequestManageAccounts(final int requestId, final byte response, final String[] idents)
	{
		_requestId = requestId;
		_reponse = response;
		_idents = idents == null ? EMPTY_IDENTS : idents;
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x03);
		super.writeD(_requestId);
		super.writeC(_reponse);
		super.writeD(_idents.length);
		for (final String ident : _idents)
		{
			super.writeS(ident);
		}
	}
}