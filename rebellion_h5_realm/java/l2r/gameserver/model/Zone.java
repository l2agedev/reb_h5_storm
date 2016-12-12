package l2r.gameserver.model;

import l2r.commons.collections.MultiValueSet;
import l2r.commons.lang.reference.HardReference;
import l2r.commons.listener.Listener;
import l2r.commons.listener.ListenerList;
import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.base.RestartType;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.EventOwner;
import l2r.gameserver.model.entity.events.impl.DuelEvent;
import l2r.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.EventTrigger;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.FuncAdd;
import l2r.gameserver.taskmanager.EffectTaskManager;
import l2r.gameserver.templates.ZoneTemplate;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Zone extends EventOwner
{
	//private static final Logger _log = LoggerFactory.getLogger(Zone.class);

	public static final Zone[] EMPTY_L2ZONE_ARRAY = new Zone[0];

	public static enum ZoneType
	{
		AirshipController,

		SIEGE,
		RESIDENCE,
		HEADQUARTER,
		FISHING,

		water,
		battle_zone,
		damage,
		instant_skill,
		mother_tree,
		peace_zone,
		poison,
		ssq_zone,
		swamp,
		no_escape,
		no_landing,
		no_restart,
		no_summon,
		dummy,
		offshore,
		epic,
		offbuff,
		special_pvp,
		UnderGroundColiseum,
		buff_store_only
	}

	public enum ZoneTarget
	{
		pc,
		npc,
		only_pc
	}

	public static final String BLOCKED_ACTION_PRIVATE_STORE = "open_private_store";
	public static final String BLOCKED_ACTION_PRIVATE_WORKSHOP = "open_private_workshop";
	public static final String BLOCKED_ACTION_DROP_MERCHANT_GUARD = "drop_merchant_guard";
	public static final String BLOCKED_ACTION_SAVE_BOOKMARK = "save_bookmark";
	public static final String BLOCKED_ACTION_USE_BOOKMARK = "use_bookmark";
	public static final String BLOCKED_ACTION_MINIMAP = "open_minimap";
	public static final String BLOCKED_ACTION_TRANSFORMATION = "transform";
	public static final String BLOCKED_ACTION_MOUNT = "mount";
	public static final String BLOCKED_ACTION_CURSED_WEAPON = "cursed_weapon";
	public static final String BLOCKED_ACTION_DROP_ITEM = "drop_item";

	/**
	 * Таймер зоны
	 */
	private abstract class ZoneTimer extends RunnableImpl
	{
		protected Creature cha;
		protected Future<?> future;
		protected boolean active;

		public ZoneTimer(Creature cha)
		{
			this.cha = cha;
		}

		public void start()
		{
			active = true;
			future = EffectTaskManager.getInstance().schedule(this, getTemplate().getInitialDelay() * 1000L);
		}

		public void stop()
		{
			active = false;
			if(future != null)
			{
				future.cancel(false);
				future = null;
			}
		}

		public void next()
		{
			if(!active)
				return;
			if(getTemplate().getUnitTick() == 0 && getTemplate().getRandomTick() == 0)
				return;
			future = EffectTaskManager.getInstance().schedule(this, (getTemplate().getUnitTick() + Rnd.get(0, getTemplate().getRandomTick())) * 1000L);
		}

		@Override
		public abstract void runImpl() throws Exception;
	}

	/**
	 * Таймер для наложения эффектов зоны
	 */
	private class SkillTimer extends ZoneTimer
	{
		public SkillTimer(Creature cha)
		{
			super(cha);
		}

		@Override
		public void runImpl() throws Exception
		{
			if(!isActive())
				return;

			if(!checkTarget(cha))
				return;

			Skill skill = getZoneSkill();
			if(skill == null)
				return;

			if(Rnd.chance(getTemplate().getSkillProb()) && !cha.isDead())
				skill.getEffects(cha, cha, false, false);

			next();
		}
	}

	/**
	 * Таймер для нанесения урона
	 */
	private class DamageTimer extends ZoneTimer
	{
		public DamageTimer(Creature cha)
		{
			super(cha);
		}

		@Override
		public void runImpl() throws Exception
		{
			if(!isActive())
				return;

			if(!checkTarget(cha))
				return;

			int hp = getDamageOnHP();
			int mp = getDamageOnMP();
			int message = getDamageMessageId();

			if(hp == 0 && mp == 0)
				return;

			if(hp > 0)
			{
				cha.reduceCurrentHp(hp, cha, null, false, false, true, false, false, false, true);
				if(message > 0)
					cha.sendPacket(new SystemMessage2(SystemMsg.valueOf(message)).addInteger(hp));
			}

			if(mp > 0)
			{
				cha.reduceCurrentMp(mp, null);
				if(message > 0)
					cha.sendPacket(new SystemMessage2(SystemMsg.valueOf(message)).addInteger(mp));
			}

			next();
		}
	}

	public class ZoneListenerList extends ListenerList<Zone>
	{
		public void onEnter(Creature actor)
		{
			if(!getListeners().isEmpty())
				for(Listener<Zone> listener : getListeners())
					((OnZoneEnterLeaveListener) listener).onZoneEnter(Zone.this, actor);
		}

		public void onLeave(Creature actor)
		{
			if(!getListeners().isEmpty())
				for(Listener<Zone> listener : getListeners())
					((OnZoneEnterLeaveListener) listener).onZoneLeave(Zone.this, actor);
		}
	}

	private ZoneType _type;
	private boolean _active;
	private final MultiValueSet<String> _params;

	private final ZoneTemplate _template;

	private Reflection _reflection;

	private final ZoneListenerList listeners = new ZoneListenerList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	private final List<Creature> _objects = new ArrayList<Creature>(32);
	private final Map<Creature, ZoneTimer> _zoneTimers = new ConcurrentHashMap<Creature, ZoneTimer>();
	
	public final static int ZONE_STATS_ORDER = 0x40;

	public Zone(ZoneTemplate template)
	{
		this(template.getType(), template);
	}

	public Zone(ZoneType type, ZoneTemplate template)
	{
		_type = type;
		_template = template;
		_params = template.getParams();
	}

	public ZoneTemplate getTemplate()
	{
		return _template;
	}

	public final String getName()
	{
		return getTemplate().getName();
	}

	public ZoneType getType()
	{
		return _type;
	}

	public void setType(ZoneType type)
	{
		_type = type;
	}

	public Territory getTerritory()
	{
		return getTemplate().getTerritory();
	}

	public final int getEnteringMessageId()
	{
		return getTemplate().getEnteringMessageId();
	}

	public final int getLeavingMessageId()
	{
		return getTemplate().getLeavingMessageId();
	}

	public Skill getZoneSkill()
	{
		return getTemplate().getZoneSkill();
	}

	public ZoneTarget getZoneTarget()
	{
		return getTemplate().getZoneTarget();
	}

	public Race getAffectRace()
	{
		return getTemplate().getAffectRace();
	}

	/**
	 * Номер системного вообщения которое будет отослано игроку при нанесении урона зоной
	 * @return SystemMessage ID
	 */
	public int getDamageMessageId()
	{
		return getTemplate().getDamageMessageId();
	}

	/**
	 * Сколько урона зона нанесет по хп
	 * @return количество урона
	 */
	public int getDamageOnHP()
	{
		return getTemplate().getDamageOnHP();
	}

	/**
	 * Сколько урона зона нанесет по мп
	 * @return количество урона
	 */
	public int getDamageOnMP()
	{
		return  getTemplate().getDamageOnMP();
	}

	/**
	 * @return Бонус к скорости движения в зоне
	 */
	public double getMoveBonus()
	{
		return getTemplate().getMoveBonus();
	}

	/**
	 * Возвращает бонус регенерации хп в этой зоне
	 * @return Бонус регенарации хп в этой зоне
	 */
	public double getRegenBonusHP()
	{
		return getTemplate().getRegenBonusHP();
	}

	/**
	 * Возвращает бонус регенерации мп в этой зоне
	 * @return Бонус регенарации мп в этой зоне
	 */
	public double getRegenBonusMP()
	{
		return getTemplate().getRegenBonusMP();
	}

	public long getRestartTime()
	{
		return getTemplate().getRestartTime();
	}

	public List<Location> getRestartPoints()
	{
		return getTemplate().getRestartPoints();
	}

	public List<Location> getPKRestartPoints()
	{
		return getTemplate().getPKRestartPoints();
	}

	public Location getSpawn()
	{
		if(getRestartPoints() == null)
			return null;
		Location loc = getRestartPoints().get(Rnd.get(getRestartPoints().size()));
		return loc.clone();
	}

	public Location getPKSpawn()
	{
		if(getPKRestartPoints() == null)
			return getSpawn();
		Location loc = getPKRestartPoints().get(Rnd.get(getPKRestartPoints().size()));
		return loc.clone();
	}
	
	public Location getReturnLoc()
	{
		return getTemplate().getReturnLoc();
	}
	
	public int getMaxLevel()
	{
		return getTemplate().getMaxLevel();
	}

	/**
	 * Проверяет находятся ли даные координаты в зоне.
	 * _loc - стандартная территория для зоны
	 * @param x координата
	 * @param y координата
	 * @return находятся ли координаты в локации
	 */
	public boolean checkIfInZone(int x, int y)
	{
		return getTerritory().isInside(x, y);
	}

	public boolean checkIfInZone(int x, int y, int z)
	{
		return checkIfInZone(x, y, z, getReflection());
	}

	public boolean checkIfInZone(int x, int y, int z, Reflection reflection)
	{
		return isActive() && _reflection == reflection && getTerritory().isInside(x, y, z);
	}

	public boolean checkIfInZone(Creature cha)
	{
		readLock.lock();
		try
		{
			return _objects.contains(cha);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public final double findDistanceToZone(GameObject obj, boolean includeZAxis)
	{
		return findDistanceToZone(obj.getX(), obj.getY(), obj.getZ(), includeZAxis);
	}

	public final double findDistanceToZone(int x, int y, int z, boolean includeZAxis)
	{
		return Location.calculateDistance(x, y, z, (getTerritory().getXmax() + getTerritory().getXmin()) / 2, (getTerritory().getYmax() + getTerritory().getYmin()) / 2, (getTerritory().getZmax() + getTerritory().getZmin()) / 2, includeZAxis);
	}

	/**
	 * Обработка входа в территорию
	 * Персонаж всегда добавляется в список вне зависимости от активности территории.
	 * Если зона акивная, то обработается вход в зону
	 * @param cha кто входит
	 */
	public void doEnter(Creature cha)
	{
		boolean added = false;

		writeLock.lock();
		try
		{
			if(!_objects.contains(cha))
				added = _objects.add(cha);
		}
		finally
		{
			writeLock.unlock();
		}

		if(added)
			onZoneEnter(cha);
		
		Functions.sendDebugMessage(cha, "ENTERED zone - " + getName());
	}

	/**
	 * Обработка входа в зону
	 * @param actor кто входит
	 */
	protected void onZoneEnter(Creature actor)
	{
		Player player = actor.getPlayer();
		
		checkEffects(actor, true);
		addZoneStats(actor);

		if(player != null)
		{
			// Initial
			if(getEnteringMessageId() != 0)
				player.sendPacket(new SystemMessage2(SystemMsg.valueOf(getEnteringMessageId())));
			if(getTemplate().getEventId() != 0)
				player.sendPacket(new EventTrigger(getTemplate().getEventId(), true));
			if(getTemplate().getBlockedActions() != null)
				player.blockActions(getTemplate().getBlockedActions());
			// Safe Check for duel event...
			if (getType() == ZoneType.peace_zone)
			{
				DuelEvent duel = player.getEvent(DuelEvent.class);
				if (duel != null)
					duel.abortDuel(player);
			}
			if (getType() == ZoneType.offbuff)
				player.sendMessage("You have entered in Buff Store area!");
			// Level Condition
			if (!player.isGM() && player.getLevel() > getMaxLevel())
			{
				ThreadPoolManager.getInstance().schedule(new BanishOut(player, "scripts.zones.epic.condition"), 5000);
				return;
			}
			// Disabled skills
			if (!player.isGM() && getTemplate().getForibiddenSkills() != null)
			{
				String[] skills = getTemplate().getForibiddenSkills();
				
				for (String skillId : skills)
				{
					Skill skill = player.getKnownSkill(Integer.parseInt(skillId));
					if (skill != null)
					{
						ThreadPoolManager.getInstance().schedule(new BanishOut(player, "scripts.zones.epic.condition"), 5000);
						break;
					}
				}
			}
			// Blocked Action's
			if (player.isActionBlocked(BLOCKED_ACTION_CURSED_WEAPON))
			{
				if (player.getTransformation() > 0)
				{
					player.setTransformation(0);
					player.sendMessage("Here is not allowed players to be transformed.");
				}
			}
			if (player.isActionBlocked(BLOCKED_ACTION_TRANSFORMATION))
			{
				if (player.getTransformation() > 0)
				{
					player.setTransformation(0);
					player.sendMessage("Here is not allowed players to be transformed.");
				}
			}
			if (player.isActionBlocked(BLOCKED_ACTION_TRANSFORMATION))
			{
				if (player.isMounted())
				{
					player.dismount();
					player.sendMessage("Sorry but this zone is only for non-mounted players.");
				}
			}
			// PvP flag zone
			if (getTemplate().isEnabled() && getTemplate().isPvPFlagZone())
			{
				if (player.getPvpFlag() == 0)
				{
					player.setPvpFlag(1);
					player.broadcastUserInfo(true);
				}
			}

		}

		listeners.onEnter(actor);
	}

	/**
	 * Обработка выхода из зоны
	 * Object всегда убирается со списка вне зависимости от зоны
	 * Если зона активная, то обработается выход из зоны
	 * @param cha кто выходит
	 */
	public void doLeave(Creature cha)
	{
		boolean removed = false;

		writeLock.lock();
		try
		{
			removed = _objects.remove(cha);
		}
		finally
		{
			writeLock.unlock();
		}

		if(removed)
			onZoneLeave(cha);
		
		Functions.sendDebugMessage(cha, "LEFT zone - " + getName());
	}

	/**
	 * Обработка выхода из зоны
	 * @param actor кто выходит
	 */
	protected void onZoneLeave(Creature actor)
	{
		Player player = actor.getPlayer();
		
		checkEffects(actor, false);
		removeZoneStats(actor);

		if(player != null)
		{
			if(getLeavingMessageId() != 0)
				player.sendPacket(new SystemMessage2(SystemMsg.valueOf(getLeavingMessageId())));
			if(getTemplate().getEventId() != 0)
				player.sendPacket(new EventTrigger(getTemplate().getEventId(), false));
			if(getTemplate().getBlockedActions() != null)
				player.unblockActions(getTemplate().getBlockedActions());
			if (getType() == ZoneType.offbuff)
				player.sendMessage("You have left the Buff Store area!");
			// TW flag schedule return timer.
			if (player.isTerritoryFlagEquipped())
			{
				TerritoryWardObject wardObject = (TerritoryWardObject) player.getActiveWeaponFlagAttachment();
				
				if (getType() == ZoneType.SIEGE && !wardObject.isFlagOut())
					wardObject.startTerrFlagCountDown(player);
			}
			
			if(getTemplate().isEnabled() && getTemplate().isPvPFlagZone())
			{
				if (player.getPvpFlag() != 0 && !player.isInCombat())
					player.stopPvPFlag();
			}
		}

		listeners.onLeave(actor);
	}

	/**
	 * Добавляет статы зоне
	 * @param cha персонаж которому добавляется
	 */
	private void addZoneStats(Creature cha)
	{
		// Проверка цели
		if(!checkTarget(cha))
			return;

		// Скорость движения накладывается только на L2Playable
		// affectRace в базе не указан, если надо будет влияние, то поправим
		if(getMoveBonus() != 0)
			if(cha.isPlayable())
			{
				cha.addStatFunc(new FuncAdd(Stats.RUN_SPEED, ZONE_STATS_ORDER, this, getMoveBonus()));
				cha.sendChanges();
			}

		// Если у нас есть что регенить
		if(getRegenBonusHP() != 0)
			cha.addStatFunc(new FuncAdd(Stats.REGENERATE_HP_RATE, ZONE_STATS_ORDER, this, getRegenBonusHP()));

		// Если у нас есть что регенить
		if(getRegenBonusMP() != 0)
			cha.addStatFunc(new FuncAdd(Stats.REGENERATE_MP_RATE, ZONE_STATS_ORDER, this, getRegenBonusMP()));
	}

	/**
	 * Убирает добавленые зоной статы
	 * @param cha персонаж у которого убирается
	 */
	private void removeZoneStats(Creature cha)
	{
		if(getRegenBonusHP() == 0 && getRegenBonusMP() == 0 && getMoveBonus() == 0)
			return;

		cha.removeStatsOwner(this);

		cha.sendChanges();
	}

	/**
	 * Применяет эффекты при входе/выходе из(в) зону
	 * @param cha обьект
	 * @param enter вошел или вышел
	 */
	private void checkEffects(Creature cha, boolean enter)
	{
		if(checkTarget(cha))
			if(enter)
			{
				if(getZoneSkill() != null)
				{
					ZoneTimer timer = new SkillTimer(cha);
					_zoneTimers.put(cha, timer);
					timer.start();
				}
				else if(getDamageOnHP() > 0 || getDamageOnHP() > 0)
				{
					ZoneTimer timer = new DamageTimer(cha);
					_zoneTimers.put(cha, timer);
					timer.start();
				}
			}
			else
			{
				ZoneTimer timer = _zoneTimers.remove(cha);
				if(timer != null)
					timer.stop();

				if(getZoneSkill() != null)
					cha.getEffectList().stopEffect(getZoneSkill());
			}
	}

	/**
	 * Проверяет подходит ли персонаж для вызвавшего действия
	 * @param cha персонаж
	 * @return подошел ли
	 */
	private boolean checkTarget(Creature cha)
	{
		switch(getZoneTarget())
		{
			case pc:
				if(!cha.isPlayable())
					return false;
				break;
			case only_pc:
				if(!cha.isPlayer())
					return false;
				break;
			case npc:
				if(!cha.isNpc())
					return false;
				break;
		}

		// Если у нас раса не "all"
		if(getAffectRace() != null)
		{
			Player player = cha.getPlayer();
			//если не игровой персонаж
			if(player == null)
				return false;
			// если раса не подходит
			if(player.getRace() != getAffectRace())
				return false;
		}

		return true;
	}

	public Creature[] getObjects()
	{
		readLock.lock();
		try
		{
			return _objects.toArray(new Creature[_objects.size()]);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public List<Player> getInsidePlayers()
	{
		List<Player> result = new ArrayList<Player>();
		readLock.lock();
		try
		{
			Creature cha;
			for(int i = 0; i < _objects.size(); i++)
				if((cha = _objects.get(i)) != null && cha.isPlayer())
					result.add((Player) cha);
		}
		finally
		{
			readLock.unlock();
		}
		return result;
	}

	public List<NpcInstance> getInsideNpcs()
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		readLock.lock();
		try
		{
			Creature cha;
			for(int i = 0; i < _objects.size(); i++)
				if((cha = _objects.get(i)) != null && cha.isNpc())
					result.add((NpcInstance) cha);
		}
		finally
		{
			readLock.unlock();
		}
		return result;
	}

	public List<Playable> getInsidePlayables()
	{
		List<Playable> result = new ArrayList<Playable>();
		readLock.lock();
		try
		{
			Creature cha;
			for(int i = 0; i < _objects.size(); i++)
				if((cha = _objects.get(i)) != null && cha.isPlayable())
					result.add((Playable) cha);
		}
		finally
		{
			readLock.unlock();
		}
		return result;
	}
	
	/**
	 * Установка активности зоны. При установки флага активности, зона добавляется в соотвествующие регионы. В случае сброса
	 * - удаляется.
	 * @param value активна ли зона
	 */
	public void setActive(boolean value)
	{
		writeLock.lock();
		try
		{
			if(_active == value)
				return;
			_active = value;
		}
		finally
		{
			writeLock.unlock();
		}

		if(isActive())
			World.addZone(Zone.this);
		else
			World.removeZone(Zone.this);
	}

	public boolean isActive()
	{
		return _active;
	}

	public void setReflection(Reflection reflection)
	{
		_reflection = reflection;
	}

	public Reflection getReflection()
	{
		return _reflection;
	}

	public void setParam(String name, String value)
	{
		_params.put(name, value);
	}

	public void setParam(String name, Object value)
	{
		_params.put(name, value);
	}

	public MultiValueSet<String> getParams()
	{
		return _params;
	}

	public <T extends Listener<Zone>> boolean addListener(T listener)
	{
		return listeners.add(listener);
	}

	public <T extends Listener<Zone>> boolean removeListener(T listener)
	{
		return listeners.remove(listener);
	}

	@Override
	public final String toString()
	{
		return "[Zone " + getType() + " name: " + getName() + "]";
	}

	public void broadcastPacket(L2GameServerPacket packet, boolean toAliveOnly)
	{
		List<Player> insideZoners = getInsidePlayers();

		if(insideZoners != null && !insideZoners.isEmpty())
			for(Player player : insideZoners)
				if(toAliveOnly)
				{
					if(!player.isDead())
						player.broadcastPacket(packet);
				}
				else
					player.broadcastPacket(packet);
	}
	
	private class BanishOut extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		private final String _message;
		public BanishOut(Player player, String msg)
		{
			_playerRef = player.getRef();
			_message = msg;
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			
			if (getReturnLoc() != null)
				player.teleToLocation(getReturnLoc());
			else
				player.teleToLocation(Location.getRestartLocation(player, RestartType.TO_VILLAGE));
			
			player.sendMessage(new CustomMessage(_message, player));
		}
	}
}