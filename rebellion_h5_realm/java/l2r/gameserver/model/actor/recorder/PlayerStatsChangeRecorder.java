package l2r.gameserver.model.actor.recorder;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.matching.MatchingRoom;
import l2r.gameserver.network.serverpackets.ExStorageMaxCount;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.utils.Util;

import javolution.util.FastMap;

import org.apache.commons.lang3.StringUtils;

public final class PlayerStatsChangeRecorder extends CharStatsChangeRecorder<Player>
{
	public static final int BROADCAST_KARMA = 1 << 3;
	public static final int SEND_STORAGE_INFO = 1 << 4;
	public static final int SEND_MAX_LOAD = 1 << 5;
	public static final int SEND_CUR_LOAD = 1 << 6;
	public static final int BROADCAST_CHAR_INFO2 = 1 << 7;

	private int _maxCp;

	private int _maxLoad;
	private int _curLoad;

	private int[] _attackElement = new int[6];
	private int[] _defenceElement = new int[6];

	private long _exp;
	private int _sp;
	private int _karma;
	private int _pk;
	private int _pvp;
	private int _fame;

	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _trade;
	private int _recipeDwarven;
	private int _recipeCommon;
	private int _partyRoom;

	private String _title = StringUtils.EMPTY;

	private int _cubicsHash;

	private FastMap<String, Integer> _stats;
	
	public PlayerStatsChangeRecorder(Player activeChar)
	{
		super(activeChar);
		_stats = new FastMap<String, Integer>();
	}

	@Override
	protected void refreshStats()
	{
		super.refreshStats();
		
		_maxCp = set(SEND_STATUS_INFO, _maxCp, _activeChar.getMaxCp());
		
		_maxLoad = set(SEND_CHAR_INFO | SEND_MAX_LOAD, _maxLoad, _activeChar.getMaxLoad());
		_curLoad = set(SEND_CUR_LOAD, _curLoad, _activeChar.getCurrentLoad());

		for(Element e : Element.VALUES)
		{
			_attackElement[e.getId()] = set(SEND_CHAR_INFO, _attackElement[e.getId()], _activeChar.getAttack(e));
			_defenceElement[e.getId()] = set(SEND_CHAR_INFO, _defenceElement[e.getId()], _activeChar.getDefence(e));
		}

		_exp = set(SEND_CHAR_INFO, _exp, _activeChar.getExp());
		_sp = set(SEND_CHAR_INFO, _sp, _activeChar.getIntSp());
		_pk = set(SEND_CHAR_INFO, _pk, _activeChar.getPkKills());
		_pvp = set(SEND_CHAR_INFO, _pvp, _activeChar.getPvpKills());
		_fame = set(SEND_CHAR_INFO, _fame, _activeChar.getFame());

		_karma = set(BROADCAST_KARMA, _karma, _activeChar.getKarma());

		_inventory = set(SEND_STORAGE_INFO, _inventory, _activeChar.getInventoryLimit());
		_warehouse = set(SEND_STORAGE_INFO, _warehouse, _activeChar.getWarehouseLimit());
		_clan = set(SEND_STORAGE_INFO, _clan, Config.WAREHOUSE_SLOTS_CLAN);
		_trade = set(SEND_STORAGE_INFO, _trade, _activeChar.getTradeLimit());
		_recipeDwarven = set(SEND_STORAGE_INFO, _recipeDwarven, _activeChar.getDwarvenRecipeLimit());
		_recipeCommon = set(SEND_STORAGE_INFO, _recipeCommon, _activeChar.getCommonRecipeLimit());
		_cubicsHash = set(BROADCAST_CHAR_INFO, _cubicsHash, Util.hashCode(_activeChar.getCubics()));
		_partyRoom = set(BROADCAST_CHAR_INFO, _partyRoom, _activeChar.getMatchingRoom() != null && _activeChar.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && _activeChar.getMatchingRoom().getLeader() == _activeChar ? _activeChar.getMatchingRoom().getId() : 0);
		_team = set(BROADCAST_CHAR_INFO2, _team, _activeChar.getTeam());
		_title = set(BROADCAST_CHAR_INFO, _title, _activeChar.getTitle());
		
		if (Config.ALT_PLAYER_SHIFTCLICK)
			update();
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();

		if((_changes & BROADCAST_CHAR_INFO2) == BROADCAST_CHAR_INFO2)
		{
			_activeChar.broadcastCharInfo();
			if(_activeChar.getPet() != null)
				_activeChar.getPet().broadcastCharInfo();
		}
		if ((_changes & BROADCAST_CHAR_INFO) == BROADCAST_CHAR_INFO)
			_activeChar.broadcastCharInfo();
		else if ((_changes & SEND_CHAR_INFO) == SEND_CHAR_INFO)
			_activeChar.sendUserInfo();

		if ((_changes & SEND_CUR_LOAD) == SEND_CUR_LOAD)
			_activeChar.sendStatusUpdate(false, false, StatusUpdate.CUR_LOAD);

		if ((_changes & SEND_MAX_LOAD) == SEND_MAX_LOAD)
			_activeChar.sendStatusUpdate(false, false, StatusUpdate.MAX_LOAD);

		if ((_changes & BROADCAST_KARMA) == BROADCAST_KARMA)
			_activeChar.sendStatusUpdate(true, false, StatusUpdate.KARMA);

		if ((_changes & SEND_STORAGE_INFO) == SEND_STORAGE_INFO)
			_activeChar.sendPacket(new ExStorageMaxCount(_activeChar));
	}
	
	private void update()
	{
		_stats.put("patak", calculteBar(_physicAttack, Config.getLimit(Config.LIMIT_PATK, _activeChar)));
		_stats.put("matak", calculteBar(_magicAttack, Config.getLimit(Config.LIMIT_MATK, _activeChar)));
		_stats.put("pdef", calculteBar(_physicDefence, Config.getLimit(Config.LIMIT_PDEF, _activeChar)));
		_stats.put("mdef", calculteBar(_magicDefence, Config.getLimit(Config.LIMIT_MDEF, _activeChar)));
		_stats.put("accuracy", calculteBar(_accuracy, Config.getLimit(Config.LIMIT_ACCURACY, _activeChar)));
		_stats.put("evasion", calculteBar(_evasion, Config.getLimit(Config.LIMIT_EVASION, _activeChar)));
		_stats.put("criticalHit", calculteBar(_criticalHit, Config.getLimit(Config.LIMIT_CRIT, _activeChar)));
		_stats.put("runSpeed", calculteBar(_runSpeed, Config.getLimit(Config.LIMIT_MOVE, _activeChar)));
		_stats.put("attackSpeed", calculteBar(_attackSpeed, Config.getLimit(Config.LIMIT_PATK_SPD, _activeChar)));
		_stats.put("castSpeed", calculteBar(_castSpeed, Config.getLimit(Config.LIMIT_MATK_SPD, _activeChar)));
		
		_stats.put("fame", _fame);
		_stats.put("pvp", _pvp);
		_stats.put("pk", _pk);
	}
	
	private int calculteBar(int points, int max)
	{
		if (points >= max)
			return 60;
		
		return 60 * (points * 100 / max) / 100;
	}
	
	public FastMap<String, Integer> get()
	{
		return _stats;
	}
}