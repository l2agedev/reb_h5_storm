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

import java.nio.charset.Charset;

/**
 * @author Forsaiken
 */
public final class RequestManageAccounts extends ReceivableConsolePacket
{
	public static final byte OPERATION_SHOW_ACCOUNTS = 1;
	public static final byte OPERATION_ADD_ACCOUNT = 2;
	public static final byte OPERATION_REMOVE_ACCOUNT = 3;
	
	private int _requestId;
	private int _operation;
	private byte[][] _identsAndPasses;

	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_operation = super.readC();
		final int size = super.readH();
		_identsAndPasses = new byte[size][];
		for (int i = 0; i < size; i++)
		{
			_identsAndPasses[i] = new byte[64];
			super.readB(_identsAndPasses[i]);
		}
	}
	
	@Override
	protected final void runImpl()
	{
		final Console console = super.getClient();
		
		final String[] idents = new String[_identsAndPasses.length];
		final String[] passes = new String[_identsAndPasses.length];
		
		final byte[] bident = new byte[32];
		final byte[] bpass = new byte[32];
		final Charset charset = Charset.forName("UTF-8");
		for (int i = 0; i < _identsAndPasses.length; i++)
		{
			System.arraycopy(_identsAndPasses[i], 0, bident, 0, 32);
			System.arraycopy(_identsAndPasses[i], 32, bpass, 0, 32);
			
			idents[i] = new String(bident, charset);
			final int identSub = idents[i].indexOf(0x00);
			if (identSub >= 0)
				idents[i] = idents[i].substring(0, identSub);
			
			passes[i] = new String(bpass, charset);
			final int passSub = passes[i].indexOf(0x00);
			if (passSub >= 0)
				passes[i] = passes[i].substring(0, passSub);
		}
		
		ConsoleController.getInstance().manageAccounts(_requestId, _operation, idents, passes, console);
	}
}