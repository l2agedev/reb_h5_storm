package l2r.gameserver.model.entity.events.impl;

import l2r.commons.collections.MultiValueSet;
import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.dao.CastleDamageZoneDAO;
import l2r.gameserver.dao.CastleDoorUpgradeDAO;
import l2r.gameserver.dao.CastleHiredGuardDAO;
import l2r.gameserver.dao.SiegeClanDAO;
import l2r.gameserver.data.xml.holder.EventHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Spawner;
import l2r.gameserver.model.base.RestartType;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.HeroDiary;
import l2r.gameserver.model.entity.SevenSigns;
import l2r.gameserver.model.entity.events.EventType;
import l2r.gameserver.model.entity.events.objects.DoorObject;
import l2r.gameserver.model.entity.events.objects.SiegeClanObject;
import l2r.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2r.gameserver.model.entity.events.objects.SpawnExObject;
import l2r.gameserver.model.entity.events.objects.SpawnSimpleObject;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.instances.ArtefactInstance;
import l2r.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.item.support.MerchantGuard;
import l2r.gameserver.utils.Location;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;

public class CastleSiegeEvent extends SiegeEvent<Castle, SiegeClanObject>
{


	public static final int MAX_SIEGE_CLANS = Config.MAX_SIEGE_CLANS;
	public static final long DAY_IN_MILISECONDS = 86400000L;

	public static final String DEFENDERS_WAITING = "defenders_waiting";
	public static final String DEFENDERS_REFUSED = "defenders_refused";

	public static final String CONTROL_TOWERS	= "control_towers";
	public static final String FLAME_TOWERS		= "flame_towers";
	public static final String BOUGHT_ZONES		= "bought_zones";
	public static final String GUARDS			= "guards";
	public static final String HIRED_GUARDS		= "hired_guards";

	private IntSet _nextSiegeTimes = Containers.EMPTY_INT_SET;
	private boolean _firstStep = false;

	private TIntIntHashMap _engrave = new TIntIntHashMap(1);
	public CastleSiegeEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	//========================================================================================================================================================================
	//                                                    Главные методы осады
	//========================================================================================================================================================================
	@Override
	public void initEvent()
	{
		super.initEvent();

		List<DoorObject> doorObjects = getObjects(DOORS);

		addObjects(BOUGHT_ZONES, CastleDamageZoneDAO.getInstance().load(getResidence()));

		for(DoorObject doorObject : doorObjects)
		{
			doorObject.setUpgradeValue(this, CastleDoorUpgradeDAO.getInstance().load(doorObject.getUId()));
			doorObject.getDoor().addListener(_doorDeathListener);
		}
	}

	public synchronized void engrave(Creature activeChar, ArtefactInstance target)
	{
		if (!getResidence().getArtefacts().contains(target.getNpcId()))
			return;
		_engrave.put(target.getNpcId(), activeChar.getPlayer().getClan().getClanId());
		if (_engrave.size() < getResidence().getArtefacts().size())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.model.entity.events.impl.castlesiegeevent.message1", activeChar.getPlayer()));
		}
		else if (_engrave.size() == getResidence().getArtefacts().size())
		{
			for (int art : getResidence().getArtefacts())
			{
				if (_engrave.get(art) != activeChar.getPlayer().getClan().getClanId())
					return;
			}
			_engrave.clear();
			processStep(activeChar.getPlayer().getClan());
		}
	}

	public synchronized boolean isActiveArtefact(Creature activeChar, ArtefactInstance target)
	{
		if(_engrave.containsKey(target.getNpcId()))
			if(_engrave.get(target.getNpcId()) == activeChar.getPlayer().getClan().getClanId())
				return false;
		return true;
	}

	@Override
	public void processStep(Clan newOwnerClan)
	{
		Clan oldOwnerClan = getResidence().getOwner();

		getResidence().changeOwner(newOwnerClan);

		// если есть овнер в резиденции, делаем его аттакером
		if(oldOwnerClan != null)
		{
			SiegeClanObject ownerSiegeClan = getSiegeClan(DEFENDERS, oldOwnerClan);
			removeObject(DEFENDERS, ownerSiegeClan);

			ownerSiegeClan.setType(ATTACKERS);
			addObject(ATTACKERS, ownerSiegeClan);
		}
		else
		{
			// Если атакуется замок, принадлежащий NPC, и только 1 атакующий - закончить осаду
			if(getObjects(ATTACKERS).size() == 1)
			{
				stopEvent();
				return;
			}

			// Если атакуется замок, принадлежащий NPC, и все атакующие в одном альянсе - закончить осаду
			int allianceObjectId = newOwnerClan.getAllyId();
			if(allianceObjectId > 0)
			{
				List<SiegeClanObject> attackers = getObjects(ATTACKERS);
				boolean sameAlliance = true;
				for(SiegeClanObject sc : attackers)
					if(sc != null && sc.getClan().getAllyId() != allianceObjectId)
						sameAlliance = false;
				if(sameAlliance)
				{
					stopEvent();
					return;
				}
			}
		}

		// ставим нового овнера защитником
		SiegeClanObject newOwnerSiegeClan = getSiegeClan(ATTACKERS, newOwnerClan);
		newOwnerSiegeClan.deleteFlag();
		newOwnerSiegeClan.setType(DEFENDERS);

		removeObject(ATTACKERS, newOwnerSiegeClan);

		// у нас защитник ток овнер
		List<SiegeClanObject> defenders = removeObjects(DEFENDERS);
		for(SiegeClanObject siegeClan : defenders)
			siegeClan.setType(ATTACKERS);

		// новый овнер это защитник
		addObject(DEFENDERS, newOwnerSiegeClan);

		// все дефендеры, стают аттакующими
		addObjects(ATTACKERS, defenders);

		updateParticles(true, ATTACKERS, DEFENDERS);

		teleportPlayers(ATTACKERS);
		teleportPlayers(SPECTATORS);

		// ток при первом захвате обнуляем мерчант гвардов и убираем апгрейд дверей
		if(!_firstStep)
		{
			_firstStep = true;

			broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED, ATTACKERS, DEFENDERS);

			if(_oldOwner != null)
			{
				spawnAction(HIRED_GUARDS, false);
				damageZoneAction(false);
				removeObjects(HIRED_GUARDS);
				removeObjects(BOUGHT_ZONES);

				CastleDamageZoneDAO.getInstance().delete(getResidence());
			}
			else
				spawnAction(GUARDS, false);

			List<DoorObject> doorObjects = getObjects(DOORS);
			for(DoorObject doorObject : doorObjects)
			{
				doorObject.setWeak(true);
				doorObject.setUpgradeValue(this, 0);

				CastleDoorUpgradeDAO.getInstance().delete(doorObject.getUId());
			}
		}

		spawnAction(DOORS, true);
		despawnSiegeSummons();
	}

	@Override
	public void startEvent()
	{
		 if (!_isInProgress.compareAndSet(false, true))
		      return;
		 
		_oldOwner = getResidence().getOwner();
		if(_oldOwner != null)
		{
			addObject(DEFENDERS, new SiegeClanObject(DEFENDERS, _oldOwner, 0));

			if(getResidence().getSpawnMerchantTickets().size() > 0)
			{
				for(ItemInstance item : getResidence().getSpawnMerchantTickets())
				{
					MerchantGuard guard = getResidence().getMerchantGuard(item.getItemId());

					addObject(HIRED_GUARDS, new SpawnSimpleObject(guard.getNpcId(), item.getLoc()));

					item.deleteMe();
				}

				CastleHiredGuardDAO.getInstance().delete(getResidence());

				spawnAction(HIRED_GUARDS, true);
			}
		}

		List<SiegeClanObject> attackers = getObjects(ATTACKERS);
		if(attackers.isEmpty())
		{
			if(_oldOwner == null)
				broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()));
			else
			{
				broadcastToWorld(new SystemMessage2(SystemMsg.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addResidenceName(getResidence()));
				getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
				getResidence().getLastSiegeDate().setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());
				getResidence().update();
			}
			reCalcNextTime(false);
			SiegeClanDAO.getInstance().delete(getResidence());
			return;
		}

		updateParticles(true, ATTACKERS, DEFENDERS);

		broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT, ATTACKERS);
		broadcastTo(new SystemMessage2(SystemMsg.YOU_ARE_PARTICIPATING_IN_THE_SIEGE_OF_S1_THIS_SIEGE_IS_SCHEDULED_FOR_2_HOURS).addResidenceName(getResidence()), ATTACKERS, DEFENDERS);

		broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_STARTED).addResidenceName(getResidence()));
		
		super.startEvent();

		if(_oldOwner == null)
			initControlTowers();
		else
			damageZoneAction(true);
	}

	@Override
	public void stopEvent(boolean step)
	{
		if (!_isInProgress.compareAndSet(true, false))
		      return;
		
		List<DoorObject> doorObjects = getObjects(DOORS);
		for(DoorObject doorObject : doorObjects)
			doorObject.setWeak(false);

		damageZoneAction(false);

		updateParticles(false, ATTACKERS, DEFENDERS);

		List<SiegeClanObject> attackers = removeObjects(ATTACKERS);
		for(SiegeClanObject siegeClan : attackers)
			siegeClan.deleteFlag();

		broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()));

		removeObjects(DEFENDERS);
		removeObjects(DEFENDERS_WAITING);
		removeObjects(DEFENDERS_REFUSED);

		Clan ownerClan = getResidence().getOwner();
		if(ownerClan != null)
		{
			if(_oldOwner == ownerClan)
			{
				getResidence().setRewardCount(getResidence().getRewardCount() + 1);
				ownerClan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(1500, false, toString())));
			}
			else
			{
				broadcastToWorld(new SystemMessage2(SystemMsg.CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE).addString(ownerClan.getName()).addResidenceName(getResidence()));

				ownerClan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(3000, false, toString())));

				if(_oldOwner != null)
				{
					SystemMessage sm = new SystemMessage(SystemMsg.YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE);
					sm.addNumber(-_oldOwner.incReputation(-3000, false, toString()));
					_oldOwner.broadcastToOnlineMembers(sm);
				}

				for(UnitMember member : ownerClan)
				{
					Player player = member.getPlayer();
					if(player != null)
					{
						if (Config.ENABLE_PLAYER_COUNTERS)
							player.getPlayer().getCounters().addPoint("_Castle_Sieges_Won");
						
						player.sendPacket(PlaySound.SIEGE_VICTORY);
						if(player.isOnline() && player.isNoble())
							Hero.getInstance().addHeroDiary(player.getObjectId(), HeroDiary.ACTION_CASTLE_TAKEN, getResidence().getId());
					}
				}
			}

			getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
			getResidence().getLastSiegeDate().setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());

			DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
			runnerEvent.registerDominion(getResidence().getDominion());
		}
		else
		{
			broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()));

			getResidence().getOwnDate().setTimeInMillis(0);
			getResidence().getLastSiegeDate().setTimeInMillis(0);

			DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
			runnerEvent.unRegisterDominion(getResidence().getDominion());
		}

		SiegeClanDAO.getInstance().delete(getResidence());
		
		if(_siegeStartTask != null)
		{
			_siegeStartTask.cancel(false);
			_siegeStartTask = null;
		}
		
		despawnSiegeSummons();

		if(_oldOwner != null)
		{
			spawnAction(HIRED_GUARDS, false);
			removeObjects(HIRED_GUARDS);
		}

		showResults();
		
		super.stopEvent(step);
	}
	//========================================================================================================================================================================

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		if(_siegeStartTask != null)
			return;
		
		clearActions();

		broadcastToWorld(new SystemMessage2(SystemMsg.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME).addResidenceName(getResidence()));
		
		correctSiegeDateTime();
		
		final Calendar startSiegeDate = getResidence().getSiegeDate();
		if (startSiegeDate.getTimeInMillis() + 120 * 60000 >= System.currentTimeMillis())
		{
			while (startSiegeDate.getTimeInMillis() <= System.currentTimeMillis())
			{
				// After rr siege will continue after 10 minutes.
				startSiegeDate.add(Calendar.MINUTE, 10);
				startSiegeDate.set(Calendar.SECOND, 0);
			}
		}
		
		registerActions(); // It will start event
		
		_siegeStartTask = ThreadPoolManager.getInstance().schedule(new SiegeStartTask(this), 1000);
	}

	static final class SiegeStartTask implements Runnable
	{
		private final CastleSiegeEvent _castle;
		
		public SiegeStartTask(CastleSiegeEvent castleSiegeEvent)
		{
			_castle = castleSiegeEvent;
		}
		
		@Override
		public void run()
		{
			long timeRemaining = _castle.getResidence().getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
			if(timeRemaining > 86400000)
				ThreadPoolManager.getInstance().schedule(new SiegeStartTask(_castle), timeRemaining - 86400000); // Prepare task for 24 before siege start to end registration
			else if(timeRemaining <= 86400000 && timeRemaining > 3600000)
			{
				ThreadPoolManager.getInstance().schedule(new SiegeStartTask(_castle), timeRemaining - 3600000); // Prepare task for 1 hr left before siege start.
			}
			else if(timeRemaining <= 3600000 && timeRemaining > 600000)
			{
				_castle.broadcastToWorld(new SystemMessage(Math.round(timeRemaining / 60000) + " minute(s) until " + _castle.getResidence().getName() + " siege begin."));
				ThreadPoolManager.getInstance().schedule(new SiegeStartTask(_castle), timeRemaining - 600000); // Prepare task for 10 minute left.
			}
			else if(timeRemaining <= 600000 && timeRemaining > 300000)
			{
				_castle.broadcastToWorld(new SystemMessage(Math.round(timeRemaining / 60000) + " minute(s) until " + _castle.getResidence().getName() + " siege begin."));
				ThreadPoolManager.getInstance().schedule(new SiegeStartTask(_castle), timeRemaining - 300000); // Prepare task for 5 minute left.
			}
			else if(timeRemaining <= 300000 && timeRemaining > 10000)
			{
				_castle.broadcastToWorld(new SystemMessage(Math.round(timeRemaining / 60000) + " minute(s) until " + _castle.getResidence().getName() + " siege begin."));
				ThreadPoolManager.getInstance().schedule(new SiegeStartTask(_castle), timeRemaining - 10000); // Prepare task for 10 seconds count down
			}
			else if(timeRemaining <= 10000 && timeRemaining > 0)
			{
				_castle.broadcastToWorld(new SystemMessage(_castle.getResidence().getName() + " siege " + Math.round(timeRemaining / 1000) + " second(s) to start!"));
				ThreadPoolManager.getInstance().schedule(new SiegeStartTask(_castle), timeRemaining); // Prepare task for second count down
			}
		}
	}
	private void correctSiegeDateTime()
	{
		boolean corrected = false;
		if (getResidence().getSiegeDate().getTimeInMillis() == 0)
		{
			corrected = true;
			setNextSiegeDate(1); // first sieges are scheduled for the first week
		}
		else if(getResidence().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			corrected = true;
			setNextSiegeDate(Config.PERIOD_CASTLE_SIEGE);
		}
		if(getResidence().getSiegeDate().get(Calendar.DAY_OF_WEEK) != _dayOfWeek)
		{
			corrected = true;
			getResidence().getSiegeDate().set(Calendar.DAY_OF_WEEK, _dayOfWeek);
		}
		if(getResidence().getSiegeDate().get(Calendar.HOUR_OF_DAY) != _hourOfDay)
		{
			corrected = true;
			getResidence().getSiegeDate().set(Calendar.HOUR_OF_DAY, _hourOfDay);
		}
		getResidence().getSiegeDate().set(Calendar.MINUTE, 0);
		if(corrected)
		{
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().update();
		}
	}
	
	private void setNextSiegeDate(int week)
	{
		if(getResidence().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Set next siege date if siege has passed
			getResidence().getSiegeDate().add(Calendar.WEEK_OF_YEAR, week);
			if(getResidence().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
				setNextSiegeDate(week); // Re-run again if still in the pass
		}
	}
	
	@Override
	public void loadSiegeClans()
	{
		super.loadSiegeClans();

		addObjects(DEFENDERS_WAITING, SiegeClanDAO.getInstance().load(getResidence(), DEFENDERS_WAITING));
		addObjects(DEFENDERS_REFUSED, SiegeClanDAO.getInstance().load(getResidence(), DEFENDERS_REFUSED));
	}

	@Override
	public void setRegistrationOver(boolean b)
	{
		if(b)
			broadcastToWorld(new SystemMessage2(SystemMsg.THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED).addResidenceName(getResidence()));

		super.setRegistrationOver(b);
	}

	@Override
	public void announce(int val)
	{
		SystemMessage2 msg;
		int min = val / 60;
		int hour = min / 60;

		if(hour > 0)
			msg = new SystemMessage2(SystemMsg.S1_HOURS_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(hour);
		else if(min > 0)
			msg = new SystemMessage2(SystemMsg.S1_MINUTES_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(min);
		else
			msg = new SystemMessage2(SystemMsg.THIS_CASTLE_SIEGE_WILL_END_IN_S1_SECONDS).addInteger(val);

		broadcastTo(msg, ATTACKERS, DEFENDERS);
	}
	//========================================================================================================================================================================
	//                                                   Control Tower Support
	//========================================================================================================================================================================
	private void initControlTowers()
	{
		List<SpawnExObject> objects = getObjects(GUARDS);
		List<Spawner> spawns = new ArrayList<Spawner>();
		for(SpawnExObject o : objects)
			spawns.addAll(o.getSpawns());

		List<SiegeToggleNpcObject> ct = getObjects(CONTROL_TOWERS);

		SiegeToggleNpcInstance closestCt;
		double distance, distanceClosest;

		for(Spawner spawn : spawns)
		{
			Location spawnLoc = spawn.getCurrentSpawnRange().getRandomLoc(ReflectionManager.DEFAULT.getGeoIndex());

			closestCt = null;
			distanceClosest = 0;

			for(SiegeToggleNpcObject c : ct)
			{
				SiegeToggleNpcInstance npcTower = c.getToggleNpc();
				distance = npcTower.getDistance(spawnLoc);

				if(closestCt == null || distance < distanceClosest)
				{
					closestCt = npcTower;
					distanceClosest = distance;
				}

				closestCt.register(spawn);
			}
		}
	}
	//========================================================================================================================================================================
	//                                                    Damage Zone Actions
	//========================================================================================================================================================================
	private void damageZoneAction(boolean active)
	{
		zoneAction(BOUGHT_ZONES, active);
	}
	//========================================================================================================================================================================
	//                                                    Суппорт Методы для установки времени осады
	//========================================================================================================================================================================
	@Override
	public boolean isAttackersInAlly()
	{
		return !_firstStep;
	}

	public int[] getNextSiegeTimes()
	{
		return _nextSiegeTimes.toArray();
	}

    @Override
    public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		try
		{
			if (target == null)
				return SystemMsg.INVALID_TARGET;
			
			if(attacker.isPlayer() && target.isPlayer() && attacker.getPlayer().getPlayerGroup() == target.getPlayer().getPlayerGroup()) // Party and CommandChannel check.
				return SystemMsg.INVALID_TARGET;
			if(attacker.isPlayer() && target.isPlayer() && attacker.getPlayer().getClan() != null && target.getPlayer().getClan() != null && attacker.getPlayer().getClan() == target.getPlayer().getClan())
				return SystemMsg.INVALID_TARGET;
			if(attacker.isPlayer() && target.isPlayer() && attacker.getPlayer().getClan() != null && attacker.getPlayer().getClan().getAlliance() != null && target.getPlayer().getClan() != null && target.getPlayer().getClan().getAlliance() != null && attacker.getPlayer().getClan().getAlliance() == target.getPlayer().getClan().getAlliance())
				return SystemMsg.INVALID_TARGET;
			
			Player targetPlayer = target.getPlayer();
			Player attackerPlayer = attacker.getPlayer();
			
			if (targetPlayer == null)
				return SystemMsg.INVALID_TARGET;
			
			CastleSiegeEvent siegeEvent = target.getEvent(CastleSiegeEvent.class);
			CastleSiegeEvent siegeEventatt = attacker.getEvent(CastleSiegeEvent.class);
			
			if(siegeEvent == null)
				return null;
			if(siegeEventatt == null)
				return null;
			if(!target.isPlayer())
				return null;
			//if(!targetPlayer.isOnSiegeField())
			//	return null;
			if(targetPlayer.getClan() == null)
				return null;
			if(attacker.getClan() == null)
				return null;
			if(siegeEvent != this)
				return null;
			SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan(ATTACKERS, targetPlayer.getClan());
			if(targetSiegeClan == null)
				targetSiegeClan = siegeEvent.getSiegeClan(DEFENDERS, targetPlayer.getClan());
			if(targetSiegeClan == null)
				return null;
			if(targetSiegeClan.getType().equals(ATTACKERS))
			{
				if(targetPlayer.getClan() == attackerPlayer.getClan())
					return SystemMsg.INVALID_TARGET;
				if(targetPlayer.getClan() != attackerPlayer.getClan())
					if(attackerPlayer.getAlliance() != null && attackerPlayer.getAlliance() == targetPlayer.getAlliance())
						return SystemMsg.INVALID_TARGET;
			}
			else
			{
				return null;
			}
		}
		catch(Exception e)
		{
			return null;
		}
        return null;
    }

    @Override
    public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		try
		{
			if (attacker == null || target == null)
				return false;
			
			if(attacker.isPlayer() && target.isPlayer() && attacker.getPlayer().getPlayerGroup() == target.getPlayer().getPlayerGroup()) // Party and CommandChannel check.
				return false;
			if(attacker.isPlayer() && target.isPlayer() && attacker.getPlayer().getClan() != null && target.getPlayer().getClan() != null && attacker.getPlayer().getClan() == target.getPlayer().getClan())
				return false;
			if(attacker.isPlayer() && target.isPlayer() && attacker.getPlayer().getClan() != null && attacker.getPlayer().getClan().getAlliance() != null && target.getPlayer().getClan() != null && target.getPlayer().getClan().getAlliance() != null && attacker.getPlayer().getClan().getAlliance() == target.getPlayer().getClan().getAlliance())
				return false;
			
			Player targetPlayer = target.getPlayer();
			
			if (targetPlayer == null)
				return false;
			
			CastleSiegeEvent siegeEvent = target.getEvent(CastleSiegeEvent.class);
			CastleSiegeEvent siegeEventatt = attacker.getEvent(CastleSiegeEvent.class);
			
			if(siegeEvent == null)
				return true;
			if(siegeEventatt == null)
				return true;
			if(!target.isPlayer())
				return true;
			if(targetPlayer.getClan() == null)
				return true;
			if(attacker.getClan() == null)
				return true;
			if(siegeEvent != this)
				return true;
			SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan(ATTACKERS, targetPlayer.getClan());
			if(targetSiegeClan == null)
				targetSiegeClan = siegeEvent.getSiegeClan(DEFENDERS, targetPlayer.getClan());
			if(targetSiegeClan == null)
				return true;
			if(targetSiegeClan.getType().equals(ATTACKERS))
			{
				if(targetPlayer.getClan() == attacker.getPlayer().getClan())
				{
					attacker.sendPacket(new SystemMessage2(SystemMsg.INVALID_TARGET));
					return false;
				}
				if(targetPlayer.getClan() != attacker.getPlayer().getClan())
					if(attacker.getPlayer().getAlliance() != null && attacker.getPlayer().getAlliance() == targetPlayer.getAlliance())
					{
						attacker.sendPacket(new SystemMessage2(SystemMsg.INVALID_TARGET));
						return false;
					}
			}
			else
			{
				return true;
			}
		}
		catch(Exception e)
		{
			return true;
		}
        return true;
    }

	@Override
	public boolean canRessurect(Player resurrectPlayer, Creature target, boolean force)
	{
		Player targetPlayer = target.getPlayer();
		// если оба вне зоны - рес разрешен
		if(!resurrectPlayer.isOnSiegeField() && !targetPlayer.isOnSiegeField())
			return true;
		// если таргет вне осадный зоны - рес разрешен
		if(!targetPlayer.isOnSiegeField())
			return true;

		if(resurrectPlayer.getClan() == null)
			return false;

		if(!resurrectPlayer.isInSiege())
			return false;

		CastleSiegeEvent siegeEvent = target.getEvent(CastleSiegeEvent.class);
		CastleSiegeEvent siegeEventatt = resurrectPlayer.getEvent(CastleSiegeEvent.class);
		// если чар не с нашей осады(или вообще нету осады) - рес запрещен
		// если таргет не с нашей осады(или вообще нету осады) - рес запрещен
		if(siegeEvent == null || siegeEventatt == null)
		{
			resurrectPlayer.sendPacket(new SystemMessage2(SystemMsg.INVALID_TARGET));
			return false;
		}
		if(siegeEvent != this || siegeEventatt != this)
		{
			if(force)
				targetPlayer.sendPacket(new SystemMessage2(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE));
			resurrectPlayer.sendPacket(new SystemMessage2(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET));
			return false;
		}

		SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan(ATTACKERS, targetPlayer.getClan());
		if(targetSiegeClan == null)
			targetSiegeClan = siegeEvent.getSiegeClan(DEFENDERS, targetPlayer.getClan());

		if(targetSiegeClan.getType().equals(ATTACKERS))
		{
			// если не твой клан или али рес запрещён
			if(resurrectPlayer.getClan() != targetSiegeClan.getClan())
			{
				if(resurrectPlayer.getAlliance() != null && resurrectPlayer.getAlliance() != targetPlayer.getAlliance())
				{
					if(force)
						targetPlayer.sendPacket(new SystemMessage2(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE));
					resurrectPlayer.sendPacket(new SystemMessage2(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET));
					return false;
				}
			}
			// если нету флага - рес запрещен
			if(targetSiegeClan.getFlag() == null)
			{
				if(force)
					targetPlayer.sendPacket(new SystemMessage2(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE));
				resurrectPlayer.sendPacket(new SystemMessage2(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET));
				return false;
			}
			
			return true;
		}
		else if (targetSiegeClan.getType().equals(DEFENDERS))
		{
			if (resurrectPlayer.getClan() != targetSiegeClan.getClan())
			{
				if (resurrectPlayer.getAlliance() != null && resurrectPlayer.getAlliance() != targetPlayer.getAlliance())
				{
					if (force)
						targetPlayer.sendPacket(new SystemMessage2(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE));
					resurrectPlayer.sendPacket(new SystemMessage2(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET));
					return false;
				}
			}
			if (isAllTowersDead())
			{
				if (force)
					targetPlayer.sendPacket(new SystemMessage2(SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE));
				resurrectPlayer.sendPacket(new SystemMessage2(force ? SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET));
				return false;
			}
			
			return true;
		}
		if(force)
			return true;
		
		resurrectPlayer.sendPacket(new SystemMessage2(SystemMsg.INVALID_TARGET));
		return false;
	}
	
	private boolean isAllTowersDead()
	{
		List<SiegeToggleNpcObject> towers = getObjects(CastleSiegeEvent.CONTROL_TOWERS);
		for(SiegeToggleNpcObject t : towers)
			if(t.isAlive())
				return false;

		return true;
	}
	
    @Override
	public Location getRestartLoc(Player player, RestartType type)
    {
        SiegeClanObject attackerClan = getSiegeClan(ATTACKERS, player.getClan());
                
        Location loc = null;
        switch(type)
		{
        case TO_VILLAGE:	    
		// Если печатью владеют лорды Рассвета (Dawn), и в данном городе идет осада, то телепортирует во 2-й по счету город.
			if(SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
			loc = _residence.getNotOwnerRestartPoint(player);
			break;          
        case TO_FLAG:
			if(!getObjects(FLAG_ZONES).isEmpty() && attackerClan != null && attackerClan.getFlag() != null)
			loc = Location.findPointToStay(attackerClan.getFlag(), 50, 75);
			else
			player.sendPacket(new SystemMessage2(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE));
			break;
        }
        return loc;
    }
    
    public void setNextSiegeTime(int time)
	{
		broadcastToWorld(new SystemMessage2(SystemMsg.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME).addResidenceName(getResidence()));

		clearActions();

		getResidence().getSiegeDate().setTimeInMillis(time * 1000);
		getResidence().setJdbcState(JdbcEntityState.UPDATED);
		getResidence().update();

		registerActions();
	}
}
