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

import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.consolecon.SendableConsolePacket;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Forsaiken
 */
public final class AnswereRequestPlayerList extends SendableConsolePacket
{
	public static final byte ORDER_NAME = 0;
	public static final byte ORDER_RACE = 1;
	public static final byte ORDER_CLASS = 2;
	public static final byte ORDER_LEVEL = 3;
	
	public static final byte RESPONSE_VIEW_LIST_SUCCESS = 0;
	public static final byte RESPONSE_VIEW_LIST_FAILED = 1;
	
	private final int _requestId;
	private final int _response;
	private final int _total;
	private final int _from;
	private final int _to;
	private final int _order;
	
	private final Player[] _players;
	
	public AnswereRequestPlayerList(final int requestId, final int response, int from, int to, final int order)
	{
		_requestId = requestId;
		_response = response;
		_order = order;

		final Player[] players = new Player[GameObjectsStorage.getAllPlayers().size()];
		int arrNum = 0;
		for (Player player : GameObjectsStorage.getAllPlayers())
		{
			players[arrNum] = player;
			arrNum++;
		}
		
		switch (_order)
		{
			case ORDER_NAME:
			{
				Arrays.sort(players, new Comparator<Player>() {
					@Override
					public final int compare(final Player o1, final Player o2)
					{
						return o1.getName().compareTo(o2.getName());
					}
				});
				break;
			}
			
			case ORDER_RACE:
			{
				Arrays.sort(players, new Comparator<Player>() {
					@Override
					public final int compare(final Player o1, final Player o2)
					{
						return o1.getRace().toString().compareTo(o2.getRace().toString());
					}
				});
				break;
			}
			
			case ORDER_CLASS:
			{
				Arrays.sort(players, new Comparator<Player>() {
					@Override
					public final int compare(final Player o1, final Player o2)
					{
						return o1.getClassId().toString().compareTo(o2.getClassId().toString());
					}
				});
				break;
			}
			
			case ORDER_LEVEL:
			{
				Arrays.sort(players, new Comparator<Player>() {
					@Override
					public final int compare(final Player o1, final Player o2)
					{
						Byte o1Lv = (byte) o1.getLevel();
						Byte o2Lv = (byte) o2.getLevel();
						return o1Lv.compareTo(o2Lv);
					}
				});
				break;
			}
		}
		
		_total = players.length;
		
		if (from > to)
		{
			_from = 0;
			_to = 10;
		}
		else if (from > _total)
		{
			_from = (_total / 10) * 10;
			_to = _from + 10;
		}
		else
		{
			_from = from;
			_to = to;
		}
		
		if (_to > _total)
			to = _total;
		else
			to = _to;
		
		_players = new Player[to - _from];
		for (int i = _from, j = 0; i < to; i++, j++)
		{
			_players[j] = players[i];
		}
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x04);
		super.writeC(0x02);
		super.writeD(_requestId);
		super.writeC(_response);
		
		super.writeH(_total);
		super.writeH(_from);
		super.writeH(_to);
		
		super.writePlayers(_players, false);
	}
}