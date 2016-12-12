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
public final class RequestChangePass extends ReceivableConsolePacket
{
	private int _requestId;
	private byte[] _oldAndNewPass;

	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		final int size = super.readH();
		_oldAndNewPass = new byte[size];
		super.readB(_oldAndNewPass);
	}
	
	@Override
	protected final void runImpl()
	{
		final Console console = super.getClient();
		
		final byte[] boldPass = new byte[32];
		final byte[] bnewPass = new byte[32];
		System.arraycopy(_oldAndNewPass, 0, boldPass, 0, 32);
		System.arraycopy(_oldAndNewPass, 32, bnewPass, 0, 32);
		
		final Charset charset = Charset.forName("UTF-8");
		
		String oldPass = new String(boldPass, charset);
		final int oldPassSub = oldPass.indexOf(0x00);
		if (oldPassSub >= 0)
			oldPass = oldPass.substring(0, oldPassSub);
		
		String newPass = new String(bnewPass, charset);
		final int newPassSub = newPass.indexOf(0x00);
		if (newPassSub >= 0)
			newPass = newPass.substring(0, newPassSub);
		
		ConsoleController.getInstance().changePass(_requestId, oldPass, newPass, console);
	}
}