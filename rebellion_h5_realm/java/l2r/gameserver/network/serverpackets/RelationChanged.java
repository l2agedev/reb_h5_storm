package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PARTY1 = 0x00001; // party member
	public static final int RELATION_PARTY2 = 0x00002; // party member
	public static final int RELATION_PARTY3 = 0x00004; // party member
	public static final int RELATION_PARTY4 = 0x00008; // party member (for information, see L2PcInstance.getRelation())
	public static final int RELATION_PARTYLEADER = 0x00010; // true if is party leader
	public static final int RELATION_HAS_PARTY = 0x00020; // true if is in party
	public static final int RELATION_CLAN_MEMBER = 0x00040; // true if is in clan
	public static final int RELATION_LEADER = 0x00080; // true if is clan leader
	public static final int RELATION_CLAN_MATE    = 0x00100; // true if is in same clan
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_MUTUAL_WAR = 0x04000; // double fist
	public static final int RELATION_1SIDED_WAR = 0x08000; // single fist
	public static final int RELATION_ALLY_MEMBER  = 0x10000; // clan is in alliance
	public static final int RELATION_ISINTERRITORYWARS = 0x80000; // Territory Wars

	public static final int USER_RELATION_CLAN_MEMBER = 0x20;
	public static final int USER_RELATION_CLAN_LEADER = 0x40;
	public static final int USER_RELATION_IN_SIEGE = 0x80;
	public static final int USER_RELATION_ATTACKER = 0x100;
	public static final int USER_RELATION_IN_DOMINION_WAR = 0x1000;

	protected final List<RelationChangedData> _data;

	protected RelationChanged(int s)
	{
		_data = new ArrayList<RelationChangedData>(s);
	}

	protected void add(RelationChangedData data)
	{
		_data.add(data);
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xCE);
		writeD(_data.size());
		for(RelationChangedData d : _data)
		{
			writeD(d._charObjId);
			writeD(d._relation);
			writeD(d._isAutoAttackable);
			writeD(d._karma);
			writeD(d._pvpFlag);
		}
	}

	static class RelationChangedData
	{
		public final int _charObjId;
		public final boolean _isAutoAttackable;
		public final int _relation, _karma, _pvpFlag;

		public RelationChangedData(Playable cha, boolean isAutoAttackable, int relation)
		{
			_isAutoAttackable = isAutoAttackable;
			_relation = relation;
			_charObjId = cha.getObjectId();
			_karma = cha.getKarma();
			_pvpFlag = cha.getPvpFlag();
		}
	}

	/**
	 * @param targetPlayable игрок, отношение к которому изменилось
	 * @param activeChar игрок, которому будет отослан пакет с результатом
	 */
	public static L2GameServerPacket update(Player sendTo, Playable targetPlayable, Player activeChar)
	{
		if(sendTo == null || targetPlayable == null || activeChar == null)
			return null;

		Player targetPlayer = targetPlayable.getPlayer();

		int relation = targetPlayer== null ? 0 : targetPlayer.getRelation(activeChar);

		RelationChanged pkt = new RelationChanged(1);

		pkt.add(new RelationChangedData(targetPlayable, targetPlayable.isAutoAttackable(activeChar), relation));
		//if(pet != null)
		//	pkt.add(new RelationChangedData(pet, pet.isAutoAttackable(activeChar), relation));

		return pkt;
	}
}