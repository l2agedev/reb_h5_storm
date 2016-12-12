package l2r.gameserver.network.serverpackets;

import l2r.gameserver.GameTimeController;
import l2r.gameserver.model.Player;

public class ClientSetTime extends L2GameServerPacket
{
	private int _time;
	private int _speed;
	
	public ClientSetTime(Player player)
	{
		_time = GameTimeController.getInstance().getGameTime();
		_speed = 6;

		if (player.disableFogAndRain())
		{
			final int min = _time % (24 * 60);

			if (min >= 2 * 60 && min < 3 * 60) // 2:00-2:10 rain
			{
				_time = _time + 10 - (min - 2 * 60) / 6;
				_speed = 5;
			}
			else if (min >= 4 * 60 && min < 5 * 60) // 4:00-4:10 rain
			{
				_time = _time + 10 - 2 * (min - 4 * 60) / 3;				
				_speed = 2;
			}
			else if (min >= 5 * 60 && min < 6 * 60) // 5:00-6:00 fog
			{
				_time = _time - 30 - (min - 5 * 60) / 2;				
				_speed = 3;
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xf2);
		writeD(_time); // time in client minutes
		writeD(_speed); //constant to match the server time( this determines the speed of the client clock)
	}
}