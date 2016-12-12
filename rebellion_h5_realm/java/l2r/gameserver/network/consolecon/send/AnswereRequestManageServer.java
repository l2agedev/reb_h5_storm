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

import l2r.gameserver.Shutdown;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.network.consolecon.SendableConsolePacket;

/**
 * <a href="http://inc-gaming.eu/">InCompetence Gaming</a>
 * 
 * @author Forsaiken
 */
public final class AnswereRequestManageServer extends SendableConsolePacket
{
	public static final byte RESPONSE_UPDATE_SUCCESS = 1;
	public static final byte RESPONSE_SHUTDOWN_SUCCESS = 2;
	public static final byte RESPONSE_RESTART_SUCCESS = 3;
	public static final byte RESPONSE_ABORT_SUCCESS = 4;
	public static final byte RESPONSE_GC_SUCCESS = 5;
	
	public static final byte RESPONSE_UPDATE_FAILED = 6;
	public static final byte RESPONSE_SHUTDOWN_FAILED = 7;
	public static final byte RESPONSE_RESTART_FAILED = 8;
	public static final byte RESPONSE_ABORT_FAILED = 9;
	public static final byte RESPONSE_GC_FAILED = 10;
	
	public static final byte SERVER_STATUS_NORMAL = 0;
	public static final byte SERVER_STATUS_SHUTDOWN = 1;
	public static final byte SERVER_STATUS_RESTART = 2;
	
	private final int _requestId;
	private final int _response;
	private final int _onlineCount;
	private final int _memoryUsage;
	private final int _totalMemory;
	private final int _serverStatus;
	private final int _param;
	
	public AnswereRequestManageServer(final int requestId, final byte response)
	{
		_requestId = requestId;
		_response = response;
		_onlineCount = GameObjectsStorage.getAllPlayersCount();
		
		final long freeMemory = Runtime.getRuntime().freeMemory();
		final long maxMemory = Runtime.getRuntime().maxMemory();
		final long usedMemory = maxMemory - freeMemory;
		
		_memoryUsage = (int)(usedMemory / (1024 * 1024));
		_totalMemory = (int)(maxMemory / (1024 * 1024));
		
		_param = Shutdown.getInstance().getSeconds();
		
		switch (Shutdown.getInstance().getMode())
		{
			case Shutdown.SHUTDOWN:
			{
				_serverStatus = SERVER_STATUS_SHUTDOWN;
				break;
			}
			
			case Shutdown.RESTART:
			{
				_serverStatus = SERVER_STATUS_RESTART;
				break;
			}
			
			case Shutdown.NONE:
			default:
			{
				_serverStatus = SERVER_STATUS_NORMAL;
				break;
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x04);
		super.writeC(0x01);
		super.writeD(_requestId);
		super.writeC(_response);
		super.writeH(_onlineCount);
		super.writeD(_memoryUsage);
		super.writeD(_totalMemory);
		super.writeC(_serverStatus);
		super.writeD(_param);
	}
}