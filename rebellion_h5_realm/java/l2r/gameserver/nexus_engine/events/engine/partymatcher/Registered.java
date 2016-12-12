package l2r.gameserver.nexus_engine.events.engine.partymatcher;

import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.List;

import javolution.util.FastTable;

/**
 * Created by Lukas
 */
public class Registered
{
	public long _registeredTime;
	public int _id;

	public PlayerEventInfo _player;

	public List<Integer> _blacklist = new FastTable<>();

	public Registered(PlayerEventInfo player, long _registeredTime)
	{
		this._player = player;
		this._id = player.getPlayersId();
		this._registeredTime = _registeredTime;
	}

	public boolean canBeRemoved(long current)
	{
		return _registeredTime + 1200000 <= current;
	}

	public void addToBlacklist(int id)
	{
		_blacklist.add(id);
	}

	public void removeBlacklist(int id)
	{
		_blacklist.remove((Integer)id);
	}

	public boolean isInBlacklist(int id)
	{
		return _blacklist.contains(id);
	}
}
