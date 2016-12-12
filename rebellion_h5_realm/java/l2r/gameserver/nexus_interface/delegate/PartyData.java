package l2r.gameserver.nexus_interface.delegate;

import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.nexus_engine.l2r.delegate.IPartyData;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.List;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public class PartyData implements IPartyData
{
	private Party _party;
	
	public PartyData(Party p)
	{
		_party = p;
	}
	
	public PartyData(PlayerEventInfo leader)
	{
		leader.getOwner().setParty(new Party(leader.getOwner(), 1));
		_party = leader.getOwner().getParty();
	}
	
	public Party getParty()
	{
		return _party;
	}
	
	public boolean exists()
	{
		return _party != null;
	}
	
	@Override
	public void addPartyMember(PlayerEventInfo player)
	{
		player.getOwner().joinParty(_party);
	}
	
	@Override
	public void removePartyMember(PlayerEventInfo player)
	{
		_party.removePartyMember(player.getOwner(), false, false);
	}
	
	@Override
	public PlayerEventInfo getLeader()
	{
		return _party.getLeader().getEventInfo();
	}
	
	@Override
	public PlayerEventInfo[] getPartyMembers()
	{
		List<PlayerEventInfo> players = new FastTable<PlayerEventInfo>();
		
		for(Player player : _party)
		{
			players.add(player.getEventInfo());
		}
		
		return players.toArray(new PlayerEventInfo[players.size()]);
	}
	
	@Override
	public int size()
	{
		return _party.size();
	}
	
	@Override
	public int getLeadersId()
	{
		return _party.getLeader().getObjectId();
	}
}
