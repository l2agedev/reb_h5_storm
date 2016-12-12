package l2r.gameserver.network.serverpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.VisualInstance;
import l2r.gameserver.model.pledge.Alliance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.randoms.Visuals;
import l2r.gameserver.skills.AbnormalEffectType;
import l2r.gameserver.tables.FakePcsTable;
import l2r.gameserver.tables.FakePcsTable.FakePc;
import l2r.gameserver.utils.Location;

import org.apache.commons.lang3.StringUtils;

public class NpcInfo extends L2GameServerPacket
{
	private boolean can_writeImpl = false;
	private int _npcObjId, _npcId, running, incombat, dead, _showSpawnAnimation;
	private int _runSpd, _walkSpd,_swimRunSpd, _swimWalkSpd,_flyRunSpd, _flyWalkSpd, _mAtkSpd, _pAtkSpd, _rhand, _lhand, _enchantEffect;
	private int karma, pvp_flag, _abnormalEffect, _abnormalEffect2, clan_id, clan_crest_id, ally_id, ally_crest_id, _formId, _titleColor;
	private double colHeight, colRadius, currentColHeight, currentColRadius;
	private boolean _isAttackable, _isNameAbove, isFlying;
	private Location _loc;
	private String _name = StringUtils.EMPTY;
	private String _title = StringUtils.EMPTY;
	private boolean _showName, _targetable;
	private int _state;
	private NpcString _nameNpcString = NpcString.NONE;
	private NpcString _titleNpcString = NpcString.NONE;
	private TeamType _team;
	private Creature _attacker;
	private Creature _cha;
	
	public NpcInfo(NpcInstance cha, Creature attacker)
	{
		_attacker = attacker;
		_cha = cha;
		
		_npcId = cha.getDisplayId() != 0 ? cha.getDisplayId() : cha.getTemplate().npcId;
		_isAttackable = attacker != null && cha.isAutoAttackable(attacker);
		_rhand = cha.getRightHandItem();
		_lhand = cha.getLeftHandItem();
		_enchantEffect = (cha.getChampionTemplate() != null ? cha.getChampionTemplate().weaponEnchant : 0);
		
		if(Config.SERVER_SIDE_NPC_NAME || cha.getTemplate().displayId != 0 || cha.getName() != cha.getTemplate().name)
			_name = cha.getName();
		if(!cha.isRaid() && (Config.SERVER_SIDE_NPC_TITLE || cha.getTemplate().displayId != 0 || cha.getTitle() != cha.getTemplate().title))
		{
			_title = cha.getTitle();
			if(Config.SERVER_SIDE_NPC_TITLE_ETC)
				if(cha.isMonster() && cha.canShowLevelInTitle())
					if(_title.isEmpty())
						_title = "LvL " + cha.getLevel();
		}
		_showSpawnAnimation = cha.getSpawnAnimation();
		_showName = cha.isShowName();
		_state = cha.getNpcState();
		_nameNpcString = (cha.getTemplate().getDisplayId() != 0 || cha.getName() != cha.getTemplate().getName()) ? NpcString.NONE : cha.getNameNpcString();
		_titleNpcString = (cha.getTemplate().getDisplayId() != 0 || cha.getName() != cha.getTemplate().getName()) ? NpcString.NONE : cha.getTitleNpcString();

		common(cha);
	}

	public NpcInfo(Summon cha, Creature attacker)
	{
		if(cha.getPlayer() != null && cha.getPlayer().isInvisible())
			setInvisible(true);

		_npcId = cha.getTemplate().npcId;
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = 0;
		_lhand = 0;
		_enchantEffect = 0;
		_showName = true;
		_name = cha.getName();
		_title = cha.isInvisible() ? "Invisible" : cha.getTitle();
		_showSpawnAnimation = cha.getSpawnAnimation();

		common(cha);
	}

	private void common(Creature cha)
	{
		colHeight = cha.getTemplate().getCollisionHeight();
		colRadius = cha.getTemplate().getCollisionRadius();
		currentColHeight = cha.getColHeight();
		currentColRadius = cha.getColRadius();
		_npcObjId = cha.getObjectId();
		_loc = cha.getLoc();
		_mAtkSpd = cha.getMAtkSpd();
		//
		Clan clan = cha.getClan();
		Alliance alliance = clan == null ? null : clan.getAlliance();
		//
		clan_id = clan == null ? 0 : clan.getClanId();
		clan_crest_id = clan == null ? 0 : clan.getCrestId();
		//
		ally_id = alliance == null ? 0 : alliance.getAllyId();
		ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();

		_runSpd = (int) (cha.getRunSpeed() / cha.getMovementSpeedMultiplier());
		_walkSpd = (int) (cha.getWalkSpeed() / cha.getMovementSpeedMultiplier());
		_swimRunSpd = (int) (cha.getRunSpeed() / cha.getMovementSpeedMultiplier());
		_swimWalkSpd = (int) (cha.getWalkSpeed() / cha.getMovementSpeedMultiplier());
		_flyRunSpd = (int) (cha.getRunSpeed() / cha.getMovementSpeedMultiplier());
		_flyWalkSpd = (int) (cha.getWalkSpeed() / cha.getMovementSpeedMultiplier());
		karma = cha.getKarma();
		pvp_flag = cha.getPvpFlag();
		_pAtkSpd = cha.getPAtkSpd();
		running = cha.isRunning() ? 1 : 0;
		incombat = cha.isInCombat() ? 1 : 0;
		dead = cha.isAlikeDead() ? 1 : 0;
		_abnormalEffect = cha.getAbnormalEffect(AbnormalEffectType.FIRST);
		_abnormalEffect2 = cha.getAbnormalEffect(AbnormalEffectType.SECOND);
		isFlying = cha.isFlying();
		_team = cha.getTeam();
		if (cha.getChampionTemplate() != null)
		{
			if (cha.getChampionTemplate().blueCircle)
				_team = TeamType.BLUE;
			else if (cha.getChampionTemplate().redCircle)
				_team = TeamType.RED;
		}
		_formId = cha.getFormId();
		_isNameAbove = cha.isNameAbove();
		_titleColor = (cha.isSummon() || cha.isPet()) ? 1 : 0;
		_targetable = cha.isTargetable();

		can_writeImpl = true;
	}

	public NpcInfo update()
	{
		_showSpawnAnimation = 1;
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		if(!can_writeImpl)
			return;

		if (_cha != null && _cha.isNpc())
		{
			FakePc fpc = FakePcsTable.getInstance().getFakePc(_cha.getNpcId());
			if(fpc != null && _attacker.isPlayer())
			{
				//_cha.broadcastPacketToOthersFpc();
				_attacker.sendPacket(new NpcToPcCharInfo((NpcInstance) _cha));
				return;
			}
		}
		
		if(_cha != null && _cha instanceof VisualInstance)
		{
			String visual = Visuals.getInstance().getAllSpawnedNPCs().get(_cha.getObjectId());
			if (_attacker.getPlayer() != null && visual != null && visual.equalsIgnoreCase(_attacker.getPlayer().getName()))
				_attacker.sendPacket(new ModelCharInfo((VisualInstance) _cha, _attacker.getPlayer()));
			return;
		}
		
		writeC(0x0c);
		//ddddddddddddddddddffffdddcccccSSddddddddccffddddccd
		writeD(_npcObjId);
		writeD(_npcId + 1000000); // npctype id c4
		writeD(_isAttackable ? 1 : 0);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd/*0x32*/); // swimspeed
		writeD(_swimWalkSpd/*0x32*/); // swimspeed
		writeD(0/*_flRunSpd*/);
		writeD(0/*_flWalkSpd*/);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1.100000023841858); // взято из клиента
		writeF(_pAtkSpd / 277.478340719);
		writeF(colRadius);
		writeF(colHeight);
		writeD(_rhand); // right hand weapon
		writeD(0); //TODO chest
		writeD(_lhand); // left hand weapon
		writeC(_isNameAbove ? 1 : 0); // 2.2: name above char 1=true ... ??; 2.3: 1 - normal, 2 - dead
		writeC(running);
		writeC(incombat);
		writeC(dead);
		writeC(_showSpawnAnimation); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeD(_nameNpcString.getId());
		writeS(_name);
		writeD(_titleNpcString.getId());
		writeS(_title);
		writeD(_titleColor); // 0- светло зеленый титул(моб), 1 - светло синий(пет)/отображение текущего МП
		writeD(pvp_flag);
		writeD(karma); // hmm karma ??
		writeD(_abnormalEffect); // C2
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeC(isFlying ? 2 : 0); // C2
		writeC(_team.ordinal()); // team aura 1-blue, 2-red
		writeF(currentColRadius); // тут что-то связанное с colRadius
		writeF(currentColHeight); // тут что-то связанное с colHeight
		writeD(_enchantEffect); // C4
		writeD(0x00); // writeD(_npc.isFlying() ? 1 : 0); // C6
		writeD(0x00);
		writeD(_formId);// great wolf type
		writeC(_targetable ? 0x01 : 0x00); // targetable
		writeC(_showName ? 0x01 : 0x00); // show name
		writeD(_abnormalEffect2);
		writeD(_state);
	}
}