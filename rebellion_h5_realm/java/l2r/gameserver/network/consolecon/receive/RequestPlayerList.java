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

import l2r.gameserver.network.consolecon.Console;
import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.send.AnswereRequestPlayerList;

/**
 * @author Forsaiken
 */
public final class RequestPlayerList extends ReceivableConsolePacket
{
	private int _requestId;
	private int _from;
	private int _to;
	private int _order;
	
	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_from = super.readH();
		_to = super.readH();
		_order = super.readC();
	}
	
	@Override
	protected final void runImpl()
	{
		final Console console = super.getClient();
		console.sendPacket(new AnswereRequestPlayerList(_requestId, AnswereRequestPlayerList.RESPONSE_VIEW_LIST_SUCCESS, _from, _to, _order));
	}
}