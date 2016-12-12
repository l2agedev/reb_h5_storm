package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Player;


public class ExEventMatchTeamInfo extends L2GameServerPacket
{
	private int _raceId;
	private int _teamId;
	private EventMatchPlayerInfo[] _members;
	
	public ExEventMatchTeamInfo(int raceId, int teamId, EventMatchPlayerInfo ... members)
	{
		_raceId = raceId;
		_teamId = teamId;
		_members = members;
	}
	
	public ExEventMatchTeamInfo(int raceId, int teamId, Player ... members)
	{
		_raceId = raceId;
		_teamId = teamId;
		_members = new EventMatchPlayerInfo[members.length];
		for (int i = 0; i < members.length; i++)
			_members[i] = new EventMatchPlayerInfo(members[i]);
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x1C);
		writeD(_raceId); // Event Id
		writeC(_teamId); // Team 1 or 2
		writeD(_members.length); // Players size.
		for (EventMatchPlayerInfo pi : _members)
		{
			writeD(pi.playerObjId); // Player ObjId
			writeS(pi.playerName); // Player Name
			writeD(pi.curHp); // Cur HP
			writeD(pi.maxHp); // Max HP
			writeD(pi.curMp); // Cur MP
			writeD(pi.maxMp); // Max MP
			writeD(pi.curCp); // Cur CP
			writeD(pi.maxCp); // Max CP
			writeD(pi.level); // Level
			writeD(pi.classId); // ClassId
			writeD(0);
			writeD(0);
		}
	}

	public static class EventMatchPlayerInfo
	{
		int playerObjId;
		String playerName;
		int curHp;
		int maxHp;
		int curMp;
		int maxMp;
		int curCp;
		int maxCp;
		int level;
		int classId;

		public EventMatchPlayerInfo(Player player)
		{
			playerObjId = player.getObjectId();
			playerName = player.getName();
			curHp = (int) player.getCurrentHp();
			maxHp = player.getMaxHp();
			curMp = (int) player.getCurrentMp();
			maxMp = player.getMaxMp();
			curCp = (int) player.getCurrentCp();
			maxCp = player.getMaxCp();
			level = player.getLevel();
			classId = player.getClassId().getId();
		}
	}
}