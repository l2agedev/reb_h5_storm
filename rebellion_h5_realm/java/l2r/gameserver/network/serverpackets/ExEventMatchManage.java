package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExEventMatchTeamInfo.EventMatchPlayerInfo;

/**
 * 
 * @author Nik, UnAfraid
 *
 */
public class ExEventMatchManage extends L2GameServerPacket
{
	private int _raceId;
	private EventMatchTeam[] _teams;
	
	public ExEventMatchManage(int raceId, EventMatchTeam ... teams)
	{
		if (teams.length != 2)
			return;
		
		_raceId = raceId;
		_teams = teams;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x30);
		writeD(_raceId); // Event ID
		writeC(_teams[0].teamId); // Team 1 ID
		writeC(_teams[1].teamId); // Team 2 ID
		writeS(_teams[0].teamName); // Team 1 Name
		writeC(_teams[0].partyStatus); // Team 1 Party Status: 0 not looking, 1 has party
		writeS(_teams[1].teamName); // Team 2 Name
		writeC(_teams[1].partyStatus); // Team 2 Party Status: 0 not looking, 1 has party
		writeD(_teams[0].playersInfo.length + _teams[1].playersInfo.length); // Players's size
		for (EventMatchTeam team : _teams)
		{
			for (EventMatchPlayerInfo pi : team.playersInfo)
			{
			    writeC(team.teamId); // Player Team ID
			    writeC(pi.playerObjId == team.partyLeaderObjId ? 1 : 0); // Party Representive (leader)
			    writeD(pi.playerObjId); // Player Object ID
			    writeS(pi.playerName); // Player Name
			    writeD(pi.classId); // Player Class ID
			    writeD(pi.level); // Player Level
			}
		}
	}
	
	public static class EventMatchTeam
	{
		public String teamName;
		public int teamId;
		public int partyStatus;
		public int partyLeaderObjId;
		public EventMatchPlayerInfo[] playersInfo;
		
		public EventMatchTeam(String teamName, int teamId, int partyStatus, int partyLeaderObjId, EventMatchPlayerInfo ... playersInfo)
		{
			this.teamName = teamName;
			this.teamId = teamId;
			this.partyStatus = partyStatus;
			this.partyLeaderObjId = partyLeaderObjId;
			this.playersInfo = playersInfo;
		}
		
		public EventMatchTeam(String teamName, int teamId, int partyStatus, int partyLeaderObjId, Player ... players)
		{
			this.teamName = teamName;
			this.teamId = teamId;
			this.partyStatus = partyStatus;
			this.partyLeaderObjId = partyLeaderObjId;
			playersInfo = new EventMatchPlayerInfo[players.length];
			for (int i = 0; i < players.length; i++)
				playersInfo[i] = new EventMatchPlayerInfo(players[i]);
		}
		
		public EventMatchTeam(String teamName, int teamId, int partyLeaderObjId, Player ... players)
		{
			this.teamName = teamName;
			this.teamId = teamId;
			this.partyLeaderObjId = partyLeaderObjId;
			playersInfo = new EventMatchPlayerInfo[players.length];
			for (int i = 0; i < players.length; i++)
				playersInfo[i] = new EventMatchPlayerInfo(players[i]);
		}
		
		public EventMatchTeam(String teamName, int teamId, Player ... players)
		{
			this.teamName = teamName;
			this.teamId = teamId;
			this.partyStatus = 1;
			this.partyLeaderObjId = players[0] != null ? players[0].getParty() != null ? players[0].getParty().getLeader().getObjectId() : 0 : 0 ;
			playersInfo = new EventMatchPlayerInfo[players.length];
			for (int i = 0; i < players.length; i++)
				playersInfo[i] = new EventMatchPlayerInfo(players[i]);
		}
		
		public EventMatchTeam(String teamName, int teamId)
		{
			this.teamName = teamName;
			this.teamId = teamId;
			this.partyStatus = 1;
			this.partyLeaderObjId = 0;
			playersInfo = new EventMatchPlayerInfo[0];
		}
	}
}