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
import l2r.gameserver.network.consolecon.ServerConsole.MessageEntry;

/**
 * @author Forsaiken
 */
public final class ServerConsoleMessage extends SendableConsolePacket
{
	public static final byte TYPE_STD = 0;
	public static final byte TYPE_ERR = 1;
	public static final byte TYPE_LOG = 2;
	
	private final MessageEntry[] _messages;
	private final byte _type;
	
	public ServerConsoleMessage(final byte type, final MessageEntry[] messages)
	{
		_messages = messages;
		_type = type;
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x04);
		super.writeC(0x05);
		super.writeC(0x01);
		super.writeC(_type);
		super.writeH(_messages.length);
		for (int i = 0; i < _messages.length; i++)
		{
			super.writeQ(_messages[i].getTime());
			super.writeH(_messages[i].getLevel());
			super.writeS(_messages[i].getLogger());
			super.writeS(_messages[i].getMessage());
		}
	}
}