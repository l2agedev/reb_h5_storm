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

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.network.consolecon.receive.ConsoleHello;
import l2r.gameserver.network.consolecon.receive.RequestAddInventoryItem;
import l2r.gameserver.network.consolecon.receive.RequestAddSkill;
import l2r.gameserver.network.consolecon.receive.RequestAuth;
import l2r.gameserver.network.consolecon.receive.RequestCancelOfflineMode;
import l2r.gameserver.network.consolecon.receive.RequestChangePass;
import l2r.gameserver.network.consolecon.receive.RequestChangePlayer;
import l2r.gameserver.network.consolecon.receive.RequestDeleteInventoryItem;
import l2r.gameserver.network.consolecon.receive.RequestDeleteSkill;
import l2r.gameserver.network.consolecon.receive.RequestGiveRemoveAllSkills;
import l2r.gameserver.network.consolecon.receive.RequestListenServerConsole;
import l2r.gameserver.network.consolecon.receive.RequestManageAccounts;
import l2r.gameserver.network.consolecon.receive.RequestManageServer;
import l2r.gameserver.network.consolecon.receive.RequestModifyInventoryItem;
import l2r.gameserver.network.consolecon.receive.RequestModifySkill;
import l2r.gameserver.network.consolecon.receive.RequestPacketForge;
import l2r.gameserver.network.consolecon.receive.RequestPlayerInfo;
import l2r.gameserver.network.consolecon.receive.RequestPlayerList;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.inc.incolution.util.IncStringBuilder;
import org.inc.nionetcore.ReceivableNioNetPacket;
import org.inc.nionetcore.interfaces.INioNetExecutor;
import org.inc.nionetcore.interfaces.INioNetPacketHandler;
import org.inc.nionetcore.util.NioNetUtil;

/**
 * @author Forsaiken
 */
public final class ConsolePacketHandler implements INioNetPacketHandler<Console>, INioNetExecutor<Console>
{
	private static final Logger _log = Logger.getLogger(Console.class.getName());
	
	@Override
	public final ReceivableNioNetPacket<Console> handlePacket(final ByteBuffer buf, final Console ls)
	{
		final short opcode = (short) (buf.get() & 0xFF);
		
		ReceivableConsolePacket packet = null;
		
		if (!ls.isAuthed())
		{
			switch (opcode)
			{
				case 0x00:
					packet = new ConsoleHello();
					break;
					
				case 0x01:
					packet = new RequestAuth();
					break;
			}
		}
		else
		{
			switch (opcode)
			{
				case 0x02:
					packet = new RequestChangePass();
					break;
					
				case 0x03:
					packet = new RequestManageAccounts();
					break;
					
				case 0x04:
				{
					if (buf.hasRemaining())
					{
						final short opcode1 = (short) (buf.get() & 0xFF);
						switch (opcode1)
						{
							case 0x00:
								packet = new RequestPacketForge();
								break;
								
							case 0x01:
								packet = new RequestManageServer();
								break;
								
							case 0x02:
								packet = new RequestPlayerList();
								break;
								
							case 0x03:
							{
								if (buf.hasRemaining())
								{
									final short opcode2 = (short) (buf.get() & 0xFF);
									switch (opcode2)
									{
										case 0x00:
											packet = new RequestPlayerInfo();
											break;
											
										case 0x01:
											packet = new RequestChangePlayer();
											break;
											
										case 0x02:
											packet = new RequestCancelOfflineMode();
											break;
											
										case 0x10:
											packet = new RequestAddInventoryItem();
											break;
											
										case 0x11:
											packet = new RequestDeleteInventoryItem();
											break;
											
										case 0x12:
											packet = new RequestModifyInventoryItem();
											break;
											
										case 0x20:
											packet = new RequestAddSkill();
											break;
											
										case 0x21:
											packet = new RequestDeleteSkill();
											break;
											
										case 0x22:
											packet = new RequestModifySkill();
											break;
											
										case 0x23:
											packet = new RequestGiveRemoveAllSkills();
											break;
									}
								}
								break;
							}
							
							case 0x04:
							{
								if (buf.hasRemaining())
								{
									final short opcode2 = (short) (buf.get() & 0xFF);
									switch (opcode2)
									{
										case 0x00:
											//TODO packet = new RequestPetitionList();
											break;
											
										case 0x01:
											//TODO packet = new RequestAcceptPetition();
											break;
											
										case 0x02:
											//TODO packet = new RequestClosePetition();
											break;
										
										case 0x03:
											//TODO packet = new RequestTalkInPetition();
											break;
									}
								}
								break;
							}
							
							case 0x05:
							{
								if (buf.hasRemaining())
								{
									final short opcode2 = (short) (buf.get() & 0xFF);
									switch (opcode2)
									{
										case 0x00:
											packet = new RequestListenServerConsole();
											break;
									}
								}
								break;
							}
						}
					}
					break;
				}
			}
		}
		
		if (packet == null)
		{
			ls.close(null);
			if (_log.isLoggable(Level.WARNING))
			{
				final IncStringBuilder isb = new IncStringBuilder("UNKNOWN PACKET: 0x");
				isb.append(Integer.toHexString(opcode));
				isb.append(',');
				isb.append(' ');
				ls.appendTo(isb);
				isb.append('\n');
				NioNetUtil.printData(isb, buf.array(), buf.position(), buf.remaining());
				_log.log(Level.WARNING, isb.toString());
			}
		}
		
		return packet;
	}
	
	@Override
	public final void execute(final ReceivableNioNetPacket<Console> packet)
	{
		ThreadPoolManager.getInstance().execute(packet);
	}
}