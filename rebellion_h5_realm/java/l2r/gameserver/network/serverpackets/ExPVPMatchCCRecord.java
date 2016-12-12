package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.entity.events.impl.KrateisCubeEvent;
import l2r.gameserver.model.entity.events.objects.KrateisCubePlayerObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nik
 */
public class ExPVPMatchCCRecord extends L2GameServerPacket
{
	public enum PvPMatchRecordAction
	{
		SHOW_WITH_BUTTON, // Shows window and leaves unclosable button which can open the window again.
		UPDATE, // Updates the window only when its closed. Its used when the player clicks on the button to view the updated score.
		SHOW_WITHOUT_BUTTON // Shows window and removes the unclosable button.
	}
	
	private final PvPMatchRecordAction _action;
	private final List<PlayerPoints> _playerPoints;
	
	public ExPVPMatchCCRecord(PvPMatchRecordAction action, List<PlayerPoints> playerPoints)
	{
		_action = action;
		_playerPoints = playerPoints;
	}
	
	public ExPVPMatchCCRecord(PvPMatchRecordAction action, KrateisCubeEvent cube)
	{
		_action = action;
		_playerPoints = new ArrayList<PlayerPoints>(25);
		for (KrateisCubePlayerObject player : cube.getSortedPlayers())
			_playerPoints.add(new PlayerPoints(player.getName(), player.getPoints()));
	}

	@Override
	public void writeImpl()
	{
		writeEx(0x89);
		writeD(_action.ordinal());
		writeD(_playerPoints.size());
		for(PlayerPoints pp : _playerPoints)
		{
			writeS(pp.playerName);
			writeD(pp.killPoints);
		}
	}
	
	public static class PlayerPoints
	{
		String playerName;
		int killPoints;
		
		public PlayerPoints(String playerName, int killPoints)
		{
			this.playerName = playerName;
			this.killPoints = killPoints;
		}
	}
}