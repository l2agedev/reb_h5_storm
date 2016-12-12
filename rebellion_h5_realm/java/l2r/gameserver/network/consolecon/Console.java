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
package l2r.gameserver.network.consolecon;

import l2r.gameserver.network.consolecon.send.ServerConsoleMessage;

import org.inc.incolution.util.IncStringBuilder;
import org.inc.nionetcore.NioNetClient;
import org.inc.nionetcore.NioNetConnection;
import org.inc.nionetcore.util.crypt.Blowfish;
import org.inc.nionetcore.util.crypt.Crypt;

/**
 * @author Forsaiken
 */
public final class Console extends NioNetClient<Console>
{
	public static final byte PLAYER_STATUS_NOT_FOUND = 0;
	public static final byte PLAYER_STATUS_OFFLINE = 1;
	public static final byte PLAYER_STATUS_ONLINE_MODE = 2;
	public static final byte PLAYER_STATUS_OFFLINE_MODE = 3;
	
	private static final Crypt INITIAL_CRYPT = new Blowfish(new byte[]
	{
		(byte) 0x59, (byte) 0x4b, (byte) 0x79, (byte) 0x35, (byte) 0x4f, (byte) 0x4f, (byte) 0x69, (byte) 0x55, (byte) 0x45, (byte) 0x51, (byte) 0x6c, (byte) 0x63, (byte) 0x29, (byte) 0x26, (byte) 0x36, (byte) 0x47, (byte) 0x2d,
		(byte) 0x7b, (byte) 0x5a, (byte) 0x7d, (byte) 0x43, (byte) 0x55, (byte) 0x37, (byte) 0x63, (byte) 0x57, (byte) 0x7a, (byte) 0x7a, (byte) 0x70, (byte) 0x24, (byte) 0x6f, (byte) 0x6f, (byte) 0x65, (byte) 0x4a, (byte) 0x6f,
		(byte) 0x5c, (byte) 0x3b, (byte) 0x62, (byte) 0x5f, (byte) 0x7d, (byte) 0x56, (byte) 0x4f, (byte) 0x5c, (byte) 0x31, (byte) 0x53, (byte) 0x5f, (byte) 0x4d, (byte) 0x68, (byte) 0x43, (byte) 0x4a, (byte) 0x57, (byte) 0x46,
		(byte) 0x29, (byte) 0x7d, (byte) 0x46, (byte) 0x29, (byte) 0x29, (byte) 0x7d, (byte) 0x68, (byte) 0x3f, (byte) 0x4d, (byte) 0x7c, (byte) 0x2f, (byte) 0x74, (byte) 0x63,
	});
	
	private boolean _useStaticCrypt;
	private boolean _isAuthed;
	private Crypt _crypt;
	
	private String _ident;
	private int _lastServerConsoleMessageIdStd;
	private int _lastServerConsoleMessageIdErr;
	private int _lastServerConsoleMessageIdLog;
	
	public Console(final NioNetConnection<Console> con)
	{
		super(con);
		_useStaticCrypt = true;
	}
	
	public final int getAndSetLastServerConsoleMessageId(final byte type, final int lastServerConsoleMessageId)
	{
		int val = -1;
		
		switch (type)
		{
			case ServerConsoleMessage.TYPE_ERR:
			{
				val = _lastServerConsoleMessageIdErr;
				_lastServerConsoleMessageIdErr = lastServerConsoleMessageId;
				break;
			}
				
			case ServerConsoleMessage.TYPE_STD:
			{
				val = _lastServerConsoleMessageIdStd;
				_lastServerConsoleMessageIdStd = lastServerConsoleMessageId;
				break;
			}
				
			case ServerConsoleMessage.TYPE_LOG:
			{
				val = _lastServerConsoleMessageIdLog;
				_lastServerConsoleMessageIdLog = lastServerConsoleMessageId;
				break;
			}
		}
		
		return val;
	}
	
	public final String getIdent()
	{
		return _ident;
	}
	
	public final void setIdent(final String ident)
	{
		_ident = ident;
	}
	
	public final void setCrypt(final Crypt crypt)
	{
		_crypt = crypt;
	}
	
	public final boolean isAuthed()
	{
		return _isAuthed;
	}
	
	public final void setAuthed(final boolean authed)
	{
		_isAuthed = authed;
	}
	
	public final void sendPacket(final SendableConsolePacket packet)
	{
		super.getConnection().sendPacket(packet);
	}
	
	public final void close(final SendableConsolePacket closePacket)
	{
		super.getConnection().close(closePacket);
	}
	
	@Override
	public final boolean decrypt(final byte[] buf, final int offset, final int size)
	{
		final Crypt crypt = _useStaticCrypt ? INITIAL_CRYPT : _crypt;
		crypt.decrypt(buf, offset, size);
		return crypt.verifyChecksum(buf, offset, size);
	}
	
	@Override
	public final int encrypt(final byte[] buf, final int offset, int size)
	{
		final Crypt crypt;
		
		if (_useStaticCrypt)
		{
			_useStaticCrypt = false;
			crypt = INITIAL_CRYPT;
		}
		else
		{
			crypt = _crypt;
		}
		
		size = crypt.calcDataSize(size);
		crypt.appendChecksum(buf, offset, size);
		crypt.encrypt(buf, offset, size);
		return size;
	}
	
	@Override
	protected final void onConnection()
	{
		ConsoleController.getInstance().onConnection(this);
	}
	
	@Override
	protected final void onDisconnection(final boolean force)
	{
		ConsoleController.getInstance().onDisconnection(this);
		ServerConsole.getInstance().removeListener(this);
	}
	
	@Override
	public final String toString()
	{
		if (_isAuthed)
		{
			return '[' + _ident + '/' + super.getConnection().getInetAddress().getHostAddress() + ']';
		}

		return "[---/" + super.getConnection().getInetAddress().getHostAddress() + ']';
	}
	
	public final void appendTo(final IncStringBuilder sb)
	{
		
	}
	
	@Override
	protected final void onUncaughtException(final Throwable e)
	{
		e.printStackTrace();
		close(null);
	}
}