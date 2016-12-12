package l2r.gameserver.network.serverpackets;

import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.instances.DecoyInstance;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.matching.MatchingRoom;
import l2r.gameserver.model.pledge.Alliance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.randoms.CharacterNameColorization;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.skills.AbnormalEffectType;
import l2r.gameserver.skills.effects.EffectCubic;
import l2r.gameserver.utils.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharInfo extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharInfo.class);

	private Player _player;
	private int[][] _inv;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private Location _loc, _fishLoc;
	private String _name, _title;
	private int _objId, _race, _sex, base_class, pvp_flag, karma, rec_have;
	private double speed_move, speed_atack, col_radius, col_height;
	private int hair_style, hair_color, face, _abnormalEffect, _abnormalEffect2;
	private int clan_id, clan_crest_id, large_clan_crest_id, ally_id, ally_crest_id, class_id;
	private int _sit, _run, _combat, _dead, private_store, _enchant;
	private int _noble, _hero, _fishing, mount_type;
	private int plg_class, pledge_type, clan_rep_score, cw_level, mount_id;
	private int _nameColor, _title_color, _transform, _agathion, _clanBoatObjectId;
	private EffectCubic[] cubics;
	private boolean _isPartyRoomLeader, _isFlying, _isInvisible;
	private TeamType _team;

	public CharInfo(Player cha)
	{
		this((Creature) cha, null);
	}
	
	public CharInfo(Player cha, Player forPlayer)
	{
		this((Creature) cha, forPlayer);
	}

	public CharInfo(DecoyInstance cha)
	{
		this((Creature) cha, null);
	}

	public CharInfo(Creature cha)
	{
		this(cha, null);
	}
	
	public CharInfo(Creature cha, Player forPlayer)
	{
		if(cha == null)
		{
			_log.error("CharInfo: cha is null!");
			Thread.dumpStack();
			return;
		}

		if(cha.isDeleted())
			return;
		
		_isInvisible = cha.isInvisible();

		Player player = cha.getPlayer();
		if(player == null)
			return;
		
		_player = player;

		if(player.isInBoat())
		{
			_loc = player.getInBoatPosition();
			if(player.isClanAirShipDriver())
			{
				_clanBoatObjectId = player.getBoat().getObjectId();
			}
		}

		if (_loc == null)
			_loc = cha.getLoc();

		_objId = cha.getObjectId();

		// Проклятое оружие и трансформации для ТВ скрывают имя и все остальные опознавательные знаки
		if(player.getTransformationName() != null || (player.getReflection() == ReflectionManager.GIRAN_HARBOR || player.getReflection() == ReflectionManager.PARNASSUS) && player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			_name = player.getTransformationName() != null ? player.getTransformationName() : player.getName();
			_title = "";
			clan_id = 0;
			clan_crest_id = 0;
			ally_id = 0;
			ally_crest_id = 0;
			large_clan_crest_id = 0;
			if(player.isCursedWeaponEquipped())
				cw_level = CursedWeaponsManager.getInstance().getLevel(player.getCursedWeaponEquippedId());
		}
		else
		{
			_name = player.getName();
			if(player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE && !player.isSellBuff())
				_title = "";
			else if(!player.isConnected() && !player.isSellBuff() && !player.isPhantom())
			{
				_title = "Disconnected";
				_title_color = 255;
			}
			else if (!player.isConnected() && player.isSellBuff())
			{
				_title = "SELL " + player.getClassId().name().toUpperCase() + " BUFFS";
				_title_color = player.getTitleColor();
			}
			else
			{
				_title = player.getTitle();
				_title_color = player.getTitleColor();
			}
			
			if(_isInvisible) // Show invisible title to others.
			{
				_title = "Invisible";
				_title_color = 0x00FFFF;
			}

			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			//
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			//
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();

			cw_level = 0;
		}

		if(player.isMounted())
		{
			_enchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = player.getMountType();
		}
		else
		{
			_enchant = player.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}

		// for visual system
		boolean showVisuals = forPlayer != null && forPlayer.getVarB("showVisuals", true);
		_inv = new int[PcInventory.PAPERDOLL_MAX][2];
		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = player.getInventory().getPaperdollItemDisplayId(PAPERDOLL_ID, showVisuals);
			_inv[PAPERDOLL_ID][1] = player.getInventory().getPaperdollAugmentationId(PAPERDOLL_ID);
		}

		_mAtkSpd = player.getMAtkSpd();
		_pAtkSpd = player.getPAtkSpd();
		speed_move = player.getMovementSpeedMultiplier();
		_runSpd = (int) (player.getRunSpeed() / speed_move);
		_walkSpd = (int) (player.getWalkSpeed() / speed_move);
		_flRunSpd = 0;
		_flWalkSpd = 0;
		_flyRunSpd = (int) (player.isFlying() ? player.getRunSpeed() / speed_move : 0);
		_flyWalkSpd = (int) (player.isFlying() ? player.getWalkSpeed() / speed_move : 0);

		_swimRunSpd = player.getSwimRunSpeed();
		_swimWalkSpd = player.getSwimWalkSpeed();
		_race = player.getBaseTemplate().race.ordinal();
		_sex = player.getSex();
		base_class = player.getBaseClassId();
		pvp_flag = player.getPvpFlag();
		karma = player.getKarma();

		speed_atack = player.getAttackSpeedMultiplier();
		col_radius = player.getColRadius();
		col_height = player.getColHeight();
		
		face = (player.getVisualItems() == null || player.getVisualItems()[8] == -1 && !showVisuals) ? player.getFace() : player.getVisualItems()[8];
		hair_style = (player.getVisualItems() == null || player.getVisualItems()[9] == -1 && !showVisuals) ? player.getHairStyle() : player.getVisualItems()[9];
		hair_color = (player.getVisualItems() == null || player.getVisualItems()[10] == -1 && !showVisuals) ? player.getHairColor() : player.getVisualItems()[10];
		
		if(clan_id > 0 && player.getClan() != null)
			clan_rep_score = player.getClan().getReputationScore();
		else
			clan_rep_score = 0;
		_sit = player.isSitting() ? 0 : 1; // standing = 1 sitting = 0
		_run = player.isRunning() ? 1 : 0; // running = 1 walking = 0
		_combat = player.isInCombat() ? 1 : 0;
		_dead = player.isAlikeDead() ? 1 : 0;
		private_store = player.isInObserverMode() ? Player.STORE_OBSERVING_GAMES : player.getPrivateStoreType();
		cubics = player.getCubics().toArray(new EffectCubic[player.getCubics().size()]);
		_abnormalEffect = player.getAbnormalEffect(AbnormalEffectType.FIRST);
		if (_isInvisible) // Invis players appear stealthy
			_abnormalEffect |= AbnormalEffect.STEALTH.getMask();
		_abnormalEffect2 = player.getAbnormalEffect(AbnormalEffectType.SECOND);
		rec_have = player.isGM() ? 0 : player.getRecomHave();
		class_id = player.getClassId().getId();
		_team = player.getTeam();

		_noble = player.isNoble() || player.canOverrideCond(PcCondOverride.HERO_AURA_CONDITIONS) ? 1 : 0; // 0x01: symbol on char menu ctrl+I
		_hero = player.isHero() || player.canOverrideCond(PcCondOverride.HERO_AURA_CONDITIONS) ? 1 : 0; // 0x01: Hero Aura
		_fishing = player.isFishing() ? 1 : 0;
		_fishLoc = player.getFishLoc();
		_nameColor = player.getNameColor();
		plg_class = player.getPledgeClass();
		pledge_type = player.getPledgeType();
		_transform = player.getTransformation();
		_agathion = player.getAgathionId();
		_isPartyRoomLeader = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		_isFlying = player.isInFlyingTransform();
	}

	@Override
	protected final void writeImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_objId == 0){ return; }

		if(activeChar.getObjectId() == _objId)
		{
			_log.error("You cant send CharInfo about his character to active user!!!");
			return;
		}
		
		boolean antifeed;
		try
		{
			antifeed = activeChar.getEventInfo().hasAntifeedProtection();
		}
		catch (NullPointerException e)
		{
			antifeed = false;
		}

		if (_nameColor == 0xFFFFFF)
			_nameColor = CharacterNameColorization.getColor(activeChar, _player);

		writeC(0x31);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_clanBoatObjectId);
		writeD(_objId);
		
		if (antifeed)
		{
			writeS("Unknown"); // visible name
			
			writeD(activeChar.getAntifeedTemplate().race.ordinal()); // race
			writeD(activeChar.getAntifeedSex() ? 0 : 1); // sex
			
			writeD(activeChar.getAntifeedTemplate().classId.ordinal()); // class
		}
		else
		{
			writeS(_name);
			writeD(_race);
			writeD(_sex);
			writeD(base_class);
		}

		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
			writeD(_inv[PAPERDOLL_ID][0]);

		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
			writeD(_inv[PAPERDOLL_ID][1]);

		writeD(_player.getTalismanCount());
		writeD(_player.getOpenCloak() ? 1 : 0);

		writeD(pvp_flag);
		writeD(karma);

		writeD(_mAtkSpd);
		writeD(_pAtkSpd);

		writeD(0x00);

		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);

		writeF(speed_move); // _cha.getProperMultiplier()
		writeF(speed_atack); // _cha.getAttackSpeedMultiplier()
		
		if (antifeed)
		{
			writeF(activeChar.getAntifeedTemplate().getCollisionRadius());
			writeF(activeChar.getAntifeedTemplate().getCollisionHeight());
		}
		else
		{
			writeF(col_radius);
			writeF(col_height);
		}

		if (antifeed)
		{
			writeD(0);
			writeD(0);
			writeD(0);
		}
		else
		{
			writeD(hair_style);
			writeD(hair_color);
			writeD(face);
		}
		
		if(antifeed)
			writeS("");
		else
			writeS(_title);
		
		if (antifeed)
		{
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
		else
		{
			writeD(clan_id);
			writeD(clan_crest_id);
			writeD(ally_id);
			writeD(ally_crest_id);
		}

		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(0x00); // is invisible
		writeC(mount_type); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(private_store);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(_isPartyRoomLeader ? 0x01 : 0x00); // find party members
		writeD(_abnormalEffect);
		writeC(_isFlying ? 0x02 : 0x00);
		
		if (antifeed)
			writeH(0);
		else
			writeH(rec_have);
		
		writeD(mount_id);
		
		if (antifeed)
			writeD(activeChar.getAntifeedTemplate().classId.ordinal());
		else
			writeD(class_id);

		writeD(0x00);
		writeC(_enchant);

		writeC(_team.ordinal()); // team circle around feet 1 = Blue, 2 = red
		
		if (antifeed)
		{
			writeD(0);
			writeC(1); // Symbol on char menu ctrl+I
			writeC(0); // Hero Aura
		}
		else
		{
			writeD(large_clan_crest_id);
			writeC(_noble);
			writeC(_hero);
		}

		writeC(_fishing);
		writeD(_fishLoc.x);
		writeD(_fishLoc.y);
		writeD(_fishLoc.z);

		if (antifeed)
			writeD(0xFFFFFF);
		else
			writeD(_nameColor);
		
		writeD(_loc.h);
		writeD(plg_class);
		writeD(pledge_type);
		
		if (antifeed)
			writeD(0xFFFF77);
		else
			writeD(_title_color);
		
		writeD(cw_level);
		writeD(clan_rep_score);
		writeD(_transform);
		writeD(_agathion);

		writeD(0x01); // T2

		writeD(_abnormalEffect2);
	}

	public static final int[] PAPERDOLL_ORDER =
	{
			Inventory.PAPERDOLL_UNDER,
			Inventory.PAPERDOLL_HEAD,
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET,
			Inventory.PAPERDOLL_BACK,
			Inventory.PAPERDOLL_LRHAND,
			Inventory.PAPERDOLL_HAIR,
			Inventory.PAPERDOLL_DHAIR,
			Inventory.PAPERDOLL_RBRACELET,
			Inventory.PAPERDOLL_LBRACELET,
			Inventory.PAPERDOLL_DECO1,
			Inventory.PAPERDOLL_DECO2,
			Inventory.PAPERDOLL_DECO3,
			Inventory.PAPERDOLL_DECO4,
			Inventory.PAPERDOLL_DECO5,
			Inventory.PAPERDOLL_DECO6,
			Inventory.PAPERDOLL_BELT
	};
}