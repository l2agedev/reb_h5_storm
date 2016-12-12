package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.pledge.Alliance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.UnitMember;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


public class GMViewPledgeInfo extends L2GameServerPacket
{
	private String _playerName, clan_name, leader_name, ally_name;
	private int clan_id, clan_crest_id, clan_level, rank, rep, ally_id, ally_crest_id;
	private int hasCastle, hasHideout, hasFortress, atWar, _territorySide, _pledgeType;
	private List<PledgeMemberInfo> _members = new ArrayList<PledgeMemberInfo>();

	public GMViewPledgeInfo(Player activeChar)
	{
		Clan clan = activeChar.getClan();
		for(UnitMember member : clan)
		{
			if(member == null)
				continue;
			_playerName = member.getName();
			clan_level = member.getLevel();
			clan_id = member.getClassId();
			clan_crest_id = member.isOnline() ? member.getObjectId() : 0;
			rep = member.getSponsor() != 0 ? 1 : 0;
			_members.add(new PledgeMemberInfo(_playerName, clan_level, clan_id, clan_crest_id, member.getSex(), 1, rep));
		}

		_pledgeType = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN).getType();
		_playerName = activeChar.getName();
		clan_id = clan.getClanId();
		clan_name = clan.getName();
		leader_name = clan.getLeaderName();
		clan_crest_id = clan.getCrestId();
		clan_level = clan.getLevel();
		hasCastle = clan.getCastle();
		hasHideout = clan.getHasHideout();
		hasFortress = clan.getHasFortress();
		rank = clan.getRank();
		rep = clan.getReputationScore();
		_territorySide = clan.getWarDominion();
		Alliance alliance = clan.getAlliance();
		ally_id = alliance == null ? 0 : alliance.getAllyId();
		ally_name = alliance == null ? StringUtils.EMPTY  : alliance.getAllyName();
		ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		atWar = clan.isAtWar() ? 1 : 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x96);
		writeD(_pledgeType == Clan.SUBUNIT_MAIN_CLAN);
		writeS(_playerName);
		writeD(clan_id);
		writeD(_pledgeType);
		writeS(clan_name);
		writeS(leader_name);
		writeD(clan_crest_id);
		writeD(clan_level);
		writeD(hasCastle);
		writeD(hasHideout);
		writeD(hasFortress);
		writeD(rank);
		writeD(rep);
		writeD(0);
		writeD(0);
		writeD(ally_id);
		writeS(ally_name);
		writeD(ally_crest_id);
		writeD(atWar);
		writeD(_territorySide);
		writeD(_members.size());
		for(PledgeMemberInfo _info : _members)
		{
			writeS(_info._name);
			writeD(_info.level);
			writeD(_info.class_id);
			writeD(_info.sex);
			writeD(_info.race);
			writeD(_info.online);
			writeD(_info.sponsor);
		}
		_members.clear();
	}

	static class PledgeMemberInfo
	{
		public String _name;
		public int level, class_id, online, sex, race, sponsor;

		public PledgeMemberInfo(String __name, int _level, int _class_id, int _online, int _sex, int _race, int _sponsor)
		{
			_name = __name;
			level = _level;
			class_id = _class_id;
			online = _online;
			sex = _sex;
			race = _race;
			sponsor = _sponsor;
		}
	}
}