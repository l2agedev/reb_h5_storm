package l2r.gameserver.network.serverpackets;

public class ExEventMatchScore extends L2GameServerPacket
{
	private int _raceId;
	private int _team1Score;
	private int _team2Score;
	
	public ExEventMatchScore(int raceId, int team1Score, int team2Score)
	{
		_raceId = raceId;
		_team1Score = team1Score;
		_team2Score = team2Score;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x10);
		writeD(_raceId); // Event ID?
		writeD(_team1Score); // Team 1 (left) score (max 9)
		writeD(_team2Score); // Team 2 (right) score (max 9)
	}
}