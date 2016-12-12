package l2r.gameserver.network.serverpackets;

public class ExEventMatchObserver extends L2GameServerPacket
{
	public enum EventMatchObserverType
	{
		EXIT, // 0
		PARTIAL, // 1 = Only Score + Party Leaders?
		FULL // 2 = Score + T1Party + T2Party + Return button
	}
	
	private int _raceId;
	private EventMatchObserverType _type;
	private String _team1Name;
	private String _team2Name;
	
	public ExEventMatchObserver(int raceId, EventMatchObserverType type, String team1Name, String team2Name)
	{
		_raceId = raceId;
		_type = type;
		_team1Name = team1Name;
		_team2Name = team2Name;
	}
	
	public static ExEventMatchObserver exitObserverMode(int raceId)
	{
		return new ExEventMatchObserver(raceId, EventMatchObserverType.EXIT, "", "");
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x0E);
		writeD(_raceId);
		writeC(_type.ordinal());
		writeC(0); // Unk TODO Find it out
		writeS(_team1Name); // Left Team
		writeS(_team2Name); // Right Team
	}
}