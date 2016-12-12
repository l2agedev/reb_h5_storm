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

import l2r.gameserver.Shutdown;
import l2r.gameserver.network.consolecon.Console;
import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.send.AnswereRequestManageServer;

/**
 * <a href="http://inc-gaming.eu/">InCompetence Gaming</a>
 * 
 * @author Forsaiken
 */
public final class RequestManageServer extends ReceivableConsolePacket
{
	public static final byte REQUEST_UPDATE_INFO = 1;
	public static final byte REQUEST_SHUTDOWN = 2;
	public static final byte REQUEST_RESTART = 3;
	public static final byte REQUEST_ABORT = 4;
	public static final byte REQUEST_RUN_GC = 5;
	
	private int _requestId;
	private int _request;
	@SuppressWarnings("unused")
	private int _param;
	
	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_request = super.readC();
		_param = super.readD();
	}
	
	@Override
	protected final void runImpl()
	{
		final Console console = super.getClient();
		
		switch (_request)
		{
			case REQUEST_UPDATE_INFO:
				console.sendPacket(new AnswereRequestManageServer(_requestId, AnswereRequestManageServer.RESPONSE_UPDATE_SUCCESS));
				break;
				
			case REQUEST_SHUTDOWN:
			{
				switch (Shutdown.getInstance().getMode())
				{
					case Shutdown.SHUTDOWN:
					case Shutdown.RESTART:
					{
						console.sendPacket(new AnswereRequestManageServer(_requestId, AnswereRequestManageServer.RESPONSE_SHUTDOWN_FAILED));
						break;
					}
					
					case Shutdown.NONE:
					default:
					{
						//Shutdown.getInstance().startConsoleShutdown(console.getIdent(), _param, false); TODO FIX
						console.sendPacket(new AnswereRequestManageServer(_requestId, AnswereRequestManageServer.RESPONSE_SHUTDOWN_SUCCESS));
						break;
					}
				}
				break;
			}
			
			case REQUEST_RESTART:
			{
				switch (Shutdown.getInstance().getMode())
				{
					case Shutdown.SHUTDOWN:
					case Shutdown.RESTART:
					{
						console.sendPacket(new AnswereRequestManageServer(_requestId, AnswereRequestManageServer.RESPONSE_RESTART_FAILED));
						break;
					}
					
					case Shutdown.NONE:
					default:
					{
						//Shutdown.getInstance().startConsoleShutdown(console.getIdent(), _param, true); TODO FIX
						console.sendPacket(new AnswereRequestManageServer(_requestId, AnswereRequestManageServer.RESPONSE_RESTART_SUCCESS));
						break;
					}
				}
				break;
			}
			
			case REQUEST_ABORT:
			{
				switch (Shutdown.getInstance().getMode())
				{
					case Shutdown.SHUTDOWN:
					case Shutdown.RESTART:
					{
						//Shutdown.getInstance().abortConsoleShutdown(console.getIdent()); TODO FIX
						console.sendPacket(new AnswereRequestManageServer(_requestId, AnswereRequestManageServer.RESPONSE_ABORT_SUCCESS));
						break;
					}
					
					case Shutdown.NONE:
					default:
					{
						console.sendPacket(new AnswereRequestManageServer(_requestId, AnswereRequestManageServer.RESPONSE_ABORT_FAILED));
						break;
					}
				}
				break;
			}
			
			case REQUEST_RUN_GC:
			{
				System.gc();
				console.sendPacket(new AnswereRequestManageServer(_requestId, AnswereRequestManageServer.RESPONSE_GC_SUCCESS));
				break;
			}
		}
	}
}