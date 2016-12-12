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

import l2r.commons.util.Rnd;
import l2r.gameserver.network.consolecon.Console;
import l2r.gameserver.network.consolecon.ConsoleController;
import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.send.ServerHello;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import org.inc.nionetcore.util.crypt.Blowfish;

/**
 * @author Forsaiken
 */
public final class ConsoleHello extends ReceivableConsolePacket
{
	private byte[] _publicKey;
	private byte[] _pass;
	
	@Override
	protected final void readImpl()
	{
		int size = super.readH();
		_publicKey = new byte[size];
		super.readB(_publicKey);
		
		size = super.readH();
		_pass = new byte[size];
		super.readB(_pass);
	}
	
	@Override
	protected final void runImpl()
	{
		final Console console = super.getClient();
		
		if (!ConsoleController.getInstance().checkServerAuthOk(_pass, console))
		{
			console.close(new ServerHello(ServerHello.STATE_PASS_WRONG, null));
			return;
		}
		
		try
		{
			final byte[] blowfish = new byte[40];
			Rnd.nextBytes(blowfish);
			
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			final BigInteger publicKeyModules = new BigInteger(_publicKey);
			final RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(publicKeyModules, RSAKeyGenParameterSpec.F4);
			final PublicKey publicKey = keyFactory.generatePublic(rsaSpec);
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			
			console.sendPacket(new ServerHello(ServerHello.STATE_SUCCESS, rsaCipher.doFinal(blowfish)));
			console.setCrypt(new Blowfish(blowfish));
		}
		catch (final GeneralSecurityException e)
		{
			e.printStackTrace();
			console.close(new ServerHello(ServerHello.STATE_ERROR, null));
		}
	}
}