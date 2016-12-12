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
import l2r.gameserver.network.consolecon.ConsoleController;
import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.send.AnswereCancelOfflineMode;

/**
 * @author Forsaiken
 */
public final class RequestCancelOfflineMode extends ReceivableConsolePacket
{
	private int _requestId;
	private String _playerName;
	
	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_playerName = super.readS();
	}
	
	@Override
	protected final void runImpl()
	{
		final Console console = super.getClient();
		ConsoleController.getInstance().disablePlayerOfflineMode(_playerName);
		console.sendPacket(new AnswereCancelOfflineMode(_requestId, AnswereCancelOfflineMode.RESPONSE_CANCEL_SUCCESS));
	}
}