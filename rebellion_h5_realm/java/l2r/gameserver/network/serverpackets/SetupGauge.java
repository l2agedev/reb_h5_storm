package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Creature;
/*
 * Colors: 0-blue, 1-red, 2-cyan, 3-green
 */
public class SetupGauge extends L2GameServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;
	public static final int GREEN = 3;

	private int _charId;
	private int _color;
	private int _time;
	private int _maxTime;

	public SetupGauge(int color, int time)
	{
		_color = color;
		_time = time;
		_maxTime = time;
	}
	
	public SetupGauge(Creature character, int color, int time)
	{
		_charId = character.getObjectId();
		_color = color;
		_time = time;
		_maxTime = time;
	}

	public SetupGauge(Creature character, int color, int time, int maxTime)
	{
		_charId = character.getObjectId();
		_color = color;
		_time = time;
		_maxTime = maxTime;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6b);
		writeD(_charId);
		writeD(_color);
		writeD(_time);
		writeD(_maxTime);
	}
}