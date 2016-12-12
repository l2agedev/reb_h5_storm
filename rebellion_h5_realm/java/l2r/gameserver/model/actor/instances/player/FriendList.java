package l2r.gameserver.model.actor.instances.player;

import l2r.gameserver.dao.CharacterFriendDAO;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.L2Friend;
import l2r.gameserver.network.serverpackets.L2FriendStatus;
import l2r.gameserver.network.serverpackets.SystemMessage;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendList
{
	private static final Logger _log = LoggerFactory.getLogger(FriendList.class);
	
	private Map<Integer, Friend> _friendList = Collections.emptyMap();
	private final Player _owner;

	public FriendList(Player owner)
	{
		_owner = owner;
	}

	public void restore()
	{
		_friendList = CharacterFriendDAO.getInstance().select(_owner);
	}

	public void removeFriend(String name)
	{
		if(StringUtils.isEmpty(name))
			return;
		int objectId = 0;
		for(Map.Entry<Integer, Friend> entry : _friendList.entrySet())
		{
			if(name.equalsIgnoreCase(entry.getValue().getName()))
			{
				objectId = entry.getKey();
				break;
			}
		}

		if(objectId > 0)
		{
			_friendList.remove(objectId);
			CharacterFriendDAO.getInstance().delete(_owner, objectId);
			Player friendChar = World.getPlayer(objectId);

			_owner.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_REMOVED_FROM_YOUR_FRIEND_LIST).addString(name), new L2Friend(name, false, friendChar != null, objectId));

			if(friendChar != null)
				friendChar.sendPacket(new SystemMessage(SystemMessage.S1__HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_owner.getName()), new L2Friend(_owner, false));
		}
		else
			_owner.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST).addString(name));
	}

	public void notifyFriends(boolean login)
	{
		
		for (Friend friend : _friendList.values())
		{
			try
			{
				Player friendPlayer = GameObjectsStorage.getPlayer(friend.getObjectId());
				if (friendPlayer != null)
				{
					Friend thisFriend = friendPlayer.getFriendList().getList().get(_owner.getObjectId());
					if (thisFriend == null)
						continue;
					
					thisFriend.update(_owner, login);
					
					if (login)
						friendPlayer.sendPacket(new SystemMessage(SystemMessage.S1_FRIEND_HAS_LOGGED_IN).addString(_owner.getName()));
					
					friendPlayer.sendPacket(new L2FriendStatus(_owner, login));
					
					friend.update(friendPlayer, login);
				}
			}
			catch (Exception e)
			{
				_log.error("FriendList: notifyFriends -  Character: " + friend.getName() + ", could block the server with exception: ", e);
			}
		}
	}

	public void addFriend(Player friendPlayer)
	{
		if (_friendList.containsKey(friendPlayer.getObjectId()))
			return;
		
		_friendList.put(friendPlayer.getObjectId(), new Friend(friendPlayer));

		CharacterFriendDAO.getInstance().insert(_owner, friendPlayer);
	}

	public Map<Integer, Friend> getList()
	{
		return _friendList;
	}

	@Override
	public String toString()
	{
		return "FriendList[owner=" + _owner.getName() + "]";
	}
}
