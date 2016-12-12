package l2r.gameserver.model;

import l2r.commons.collections.EmptyIterator;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.IStaticPacket;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public interface PlayerGroup extends Iterable<Player>
{
	static final PlayerGroup EMPTY = new PlayerGroup()
	{
		@Override
		public Iterator<Player> iterator()
		{
			return EmptyIterator.getInstance();
		}

		@Override
		public int size()
		{
			return 0;
		}

		@Override
		public Player getLeader()
		{
			return null;
		}

		@Override
		public List<Player> getMembers(Player ... excluded)
		{
			return Collections.emptyList();
		}

		@Override
		public boolean containsMember(Player player)
		{
			return false;
		}

		@Override
		public int getLevel()
		{
			return 0;
		}

		@Override
		public void sendPacket(IStaticPacket... packets)
		{
		}

		@Override
		public void sendPacket(Player exclude, IStaticPacket... packets)
		{
		}

		@Override
		public void sendPacketInRange(GameObject obj, int range, IStaticPacket... packets)
		{
		}

		@Override
		public void sendMessage(String message)
		{
		}

		@Override
		public void sendMessage(CustomMessage string)
		{
		}

		@Override
		public void sendChatMessage(int objectId, int messageType, String charName, String text)
		{
		}

		@Override
		public boolean isLeader(Player player)
		{
			return false;
		}

		@Override
		public List<Player> getMembersInRange(GameObject obj, int range)
		{
			return null;
		}

		@Override
		public int getMemberCountInRange(GameObject obj, int range)
		{
			return 0;
		}

		@Override
		public List<Integer> getMembersObjIds(Player... excluded)
		{
			return null;
		}

		@Override
		public List<Playable> getMembersWithPets(Player... excluded)
		{
			return null;
		}

		@Override
		public Player getPlayerByName(String name)
		{
			return null;
		}

		@Override
		public Player getPlayer(int objId)
		{
			return null;
		}
	};
	
	int size();
	
	Player getLeader();
	
	List<Player> getMembers(Player ... excluded);
	
	boolean containsMember(Player player);
	
	int getLevel();

	void sendPacket(IStaticPacket... packets);
	
	void sendPacket(Player exclude, IStaticPacket... packets);

	void sendPacketInRange(GameObject obj, int range, IStaticPacket... packets);

	void sendMessage(String message);
	
	void sendMessage(CustomMessage string);
	
	void sendChatMessage(int objectId, int messageType, String charName, String text);

	boolean isLeader(Player player);

	List<Player> getMembersInRange(GameObject obj, int range);

	int getMemberCountInRange(GameObject obj, int range);

	List<Integer> getMembersObjIds(Player ... excluded);

	List<Playable> getMembersWithPets(Player ... excluded);

	Player getPlayerByName(String name);
	
	Player getPlayer(int objId);
}
