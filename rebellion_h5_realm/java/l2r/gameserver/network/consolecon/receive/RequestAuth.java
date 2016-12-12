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
import l2r.gameserver.network.consolecon.send.AnswereRequestAuth;

import java.nio.charset.Charset;

/**
 * @author Forsaiken
 */
public final class RequestAuth extends ReceivableConsolePacket
{
	private byte[] _identAndPass;

	@Override
	protected final void readImpl()
	{
		final int size = super.readH();
		_identAndPass = new byte[size];
		super.readB(_identAndPass);
	}
	
	@Override
	protected final void runImpl()
	{
		final Console console = super.getClient();
		
		if (_identAndPass.length != 64)
		{
			console.close(AnswereRequestAuth.STATIC_PACKET_AUTH_FAILED_ERROR);
			return;
		}
		
		final byte[] bident = new byte[32];
		final byte[] bpass = new byte[32];
		System.arraycopy(_identAndPass, 0, bident, 0, 32);
		System.arraycopy(_identAndPass, 32, bpass, 0, 32);
		
		final Charset charset = Charset.forName("UTF-8");
		
		String ident = new String(bident, charset);
		final int identSub = ident.indexOf(0x00);
		if (identSub >= 0)
			ident = ident.substring(0, identSub);
		
		String pass = new String(bpass, charset);
		final int passSub = pass.indexOf(0x00);
		if (passSub >= 0)
			pass = pass.substring(0, passSub);
		
		ConsoleController.getInstance().checkAuthOk(ident, pass, console);
	}
}