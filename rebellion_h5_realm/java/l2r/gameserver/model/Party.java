package l2r.gameserver.model;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.MatchingRoomManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.entity.DimensionalRift;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.matching.MatchingRoom;
import l2r.gameserver.network.serverpackets.ExAskModifyPartyLooting;
import l2r.gameserver.network.serverpackets.ExMPCCClose;
import l2r.gameserver.network.serverpackets.ExMPCCOpen;
import l2r.gameserver.network.serverpackets.ExPartyPetWindowAdd;
import l2r.gameserver.network.serverpackets.ExPartyPetWindowDelete;
import l2r.gameserver.network.serverpackets.ExReplyHandOverPartyMaster;
import l2r.gameserver.network.serverpackets.ExSetPartyLooting;
import l2r.gameserver.network.serverpackets.GetItem;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.PartyMemberPosition;
import l2r.gameserver.network.serverpackets.PartySmallWindowAdd;
import l2r.gameserver.network.serverpackets.PartySmallWindowAll;
import l2r.gameserver.network.serverpackets.PartySmallWindowDelete;
import l2r.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2r.gameserver.network.serverpackets.PartySpelled;
import l2r.gameserver.network.serverpackets.RelationChanged;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.IStaticPacket;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.randoms.CharacterNameColorization;
import l2r.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

public class Party implements PlayerGroup
{
	public static final int MAX_SIZE = Config.MAX_PARTY_SIZE;

	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;

	private final List<Player> _members = new CopyOnWriteArrayList<Player>();

	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemOrder = 0;
	private int _dimentionalRift;

	private Reflection _reflection;
	private CommandChannel _commandChannel;

	public double _rateExp;
	public double _rateSp;
	public double _rateDrop;
	public double _rateAdena;
	public double _rateSpoil;

	private ScheduledFuture<?> positionTask;

	private int _requestChangeLoot = -1;
	private long _requestChangeLootTimer = 0;
	private Set<Integer> _changeLootAnswers = null;
	private static final int[] LOOT_SYSSTRINGS = {487, 488, 798, 799, 800};
	private Future<?> _checkTask = null;

	/**
	 * constructor ensures party has always one member - leader
	 * @param leader создатель парти
	 * @param itemDistribution режим распределения лута
	 */
	public Party(Player leader, int itemDistribution)
	{
		_itemDistribution = itemDistribution;
		_members.add(leader);
		_partyLvl = leader.getLevel();
		_rateExp = leader.getBonus().getRateXp();
		_rateSp = leader.getBonus().getRateSp();
		_rateAdena = leader.getBonus().getDropAdena();
		_rateDrop = leader.getBonus().getDropItems();
		_rateSpoil = leader.getBonus().getDropSpoil();
	}

	/**
	 * @return all party members
	 */
	@Override
	public List<Player> getMembers(Player ... excluded)
	{
		if (excluded != null && excluded.length > 0)
		{
			List<Player> members = new ArrayList<Player>(_members.size());
			for(Player member : _members)
			{
				if (!Util.contains(excluded, member))
					members.add(member);
			}
			
			return members;
		}
		
		
		return _members;
	}
	/**
	 * @return next item looter
	 */
	@SuppressWarnings("unused")
	private Player getNextLooterInRange(Player player, ItemInstance item, int range)
	{
		synchronized (_members)
		{
			int antiloop = _members.size();
			while(--antiloop > 0)
			{
				int looter = _itemOrder;
				_itemOrder++;
				if(_itemOrder > _members.size() - 1)
					_itemOrder = 0;

				Player ret = looter < _members.size() ? _members.get(looter) : player;

				if(ret != null && !ret.isDead() && ret.isInRangeZ(player, range) && ret.getInventory().validateCapacity(item) && ret.getInventory().validateWeight(item))
					return ret;
			}
		}
		return player;
	}

	@Override
	public boolean containsMember(Player player)
	{
		return _members.contains(player);
	}

	public int indexOf(Player player)
	{
		return _members.indexOf(player);
	}
	
	/**
	 * adds new member to party
	 * @param player L2Player to add
	 */
	public boolean addPartyMember(Player player)
	{
		Player leader = getLeader();
		if(leader == null)
			return false;

		synchronized (_members)
		{
			if(_members.isEmpty())
				return false;
			if(_members.contains(player))
				return false;
			if(_members.size() == MAX_SIZE)
				return false;
			_members.add(player);
		}

		if(_requestChangeLoot != -1)
			finishLootRequest(false); // cancel on invite

		player.setParty(this);
		player.getListeners().onPartyInvite();

		Summon pet;
		List<L2GameServerPacket> addInfo = new ArrayList<L2GameServerPacket>(4 + _members.size() * 4);
		List<L2GameServerPacket> pplayer = new ArrayList<L2GameServerPacket>(20);

		//sends new member party window for all members
		//we do all actions before adding member to a list, this speeds things up a little
		pplayer.add(new PartySmallWindowAll(this, player));
		pplayer.add(new SystemMessage2(SystemMsg.YOU_HAVE_JOINED_S1S_PARTY).addName(leader));

		addInfo.add(new SystemMessage2(SystemMsg.S1_HAS_JOINED_THE_PARTY).addName(player));
		addInfo.add(new PartySpelled(player, true));
		if((pet = player.getPet()) != null)
		{
			addInfo.add(new ExPartyPetWindowAdd(pet));
			addInfo.add(new PartySpelled(pet, true));
		}

		CharacterNameColorization.updateColor(player);
		
		PartyMemberPosition pmp = new PartyMemberPosition();
		List<L2GameServerPacket> pmember;
		for(Player member : _members)
			if(member != player)
			{
				pmember = new ArrayList<L2GameServerPacket>(addInfo.size() + 4);
				pmember.addAll(addInfo);
				pmember.add(new PartySmallWindowAdd(member, player));
				pmember.add(new PartyMemberPosition().add(player));
				pmember.add(RelationChanged.update(member, player, member));
				
				member.sendPacket(pmember);

				pplayer.add(new PartySpelled(member, true));
				if((pet = member.getPet()) != null)
				{
					pplayer.add(new PartySpelled(pet, true));
					pet.broadcastCharInfoImpl(player);
				}
				
				pplayer.add(RelationChanged.update(player, member, player));
				pmp.add(member);
				
				CharacterNameColorization.updateColor(member);
			}

		pplayer.add(pmp);
		// Если партия уже в СС, то вновь прибывшем посылаем пакет открытия окна СС
		if(isInCommandChannel())
			pplayer.add(ExMPCCOpen.STATIC);

		player.sendPacket(pplayer);
		if(player.getPet() != null)
			player.getPet().broadcastCharInfoImpl(this);

		startUpdatePositionTask();
		recalculatePartyData();

		if(isInReflection() && getReflection() instanceof DimensionalRift)
			((DimensionalRift) getReflection()).partyMemberInvited();

		final MatchingRoom currentRoom = player.getMatchingRoom();
		final MatchingRoom room = leader.getMatchingRoom();
		if (currentRoom != null && currentRoom != room)
			currentRoom.removeMember(player, false);
		if(room != null && room.getType() == MatchingRoom.PARTY_MATCHING)
			room.addMemberForce(player);
		else
			MatchingRoomManager.getInstance().removeFromWaitingList(player);

		return true;
	}

	/**
	 * Удаляет все связи
	 */
	public void dissolveParty()
	{
		for(Player p : _members)
		{
			CharacterNameColorization.updateColor(p);
			p.sendPacket(PartySmallWindowDeleteAll.STATIC);
			p.setParty(null);
		}

		synchronized (_members)
		{
			_members.clear();
		}

		setDimensionalRift(null);
		setCommandChannel(null);
		stopUpdatePositionTask();
	}

	/**
	 * removes player from party
	 * @param player Player to remove
	 * @param kick меняет сообщения "вышел/кикнули"
	 * @param withdrawal выход по нажатию кнопки
	 */
	public boolean removePartyMember(Player player, boolean kick, boolean withdrawal)
	{
		boolean isLeader = isLeader(player);
		boolean dissolve = isLeader && withdrawal;

		synchronized (_members)
		{
			if(!_members.remove(player))
				return false;
			dissolve |= _members.size() == 1;
		}

		player.getListeners().onPartyLeave();

		player.setParty(null);
		recalculatePartyData();

		List<L2GameServerPacket> pplayer = new ArrayList<L2GameServerPacket>(4 + _members.size() * 2);

		// Отсылаемы вышедшему пакет закрытия СС
		if(isInCommandChannel())
			pplayer.add(ExMPCCClose.STATIC);
		if (kick)
			pplayer.add(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY));
		else
			pplayer.add(new SystemMessage2(SystemMsg.YOU_HAVE_WITHDRAWN_FROM_THE_PARTY));
		pplayer.add(PartySmallWindowDeleteAll.STATIC);

		CharacterNameColorization.updateColor(player);
		
		Summon pet;
		List<L2GameServerPacket> outsInfo = new ArrayList<L2GameServerPacket>(3);
		if((pet = player.getPet()) != null)
			outsInfo.add(new ExPartyPetWindowDelete(pet));
		outsInfo.add(new PartySmallWindowDelete(player));
		if (kick)
			outsInfo.add(new SystemMessage2(SystemMsg.C1_WAS_EXPELLED_FROM_THE_PARTY).addName(player));
		else
			outsInfo.add(new SystemMessage2(SystemMsg.S1_HAS_LEFT_THE_PARTY).addName(player));

		List<L2GameServerPacket> pmember;
		for(Player member : _members)
		{
			pmember = new ArrayList<L2GameServerPacket>(2 + outsInfo.size());
			pmember.addAll(outsInfo);
			pmember.add(RelationChanged.update(member, player, member));
			if(member.getPet() != null)
				member.getPet().broadcastCharInfoImpl(player);
			member.sendPacket(pmember);
			pplayer.add(RelationChanged.update(player, member, player));
		}

		if(player.getPet() != null)
			player.getPet().broadcastCharInfoImpl(this);

		player.sendPacket(pplayer);

		Reflection reflection = getReflection();

		if(reflection instanceof DarknessFestival)
			((DarknessFestival) reflection).partyMemberExited();
		else if(isInReflection() && getReflection() instanceof DimensionalRift)
			((DimensionalRift) getReflection()).partyMemberExited(player);
		if(reflection != null && player.getReflection() == reflection && reflection.getReturnLoc() != null)
			player.teleToLocation(reflection.getReturnLoc(), ReflectionManager.DEFAULT);

		Player leader = getLeader();
		final MatchingRoom room = leader != null ? leader.getMatchingRoom() : null;

		if(dissolve)
		{
			synchronized(this)
			{			
				// Если в партии остался 1 человек, то удаляем ее из СС
				if(isInCommandChannel())
					_commandChannel.removeParty(this);
				else if(reflection != null)
				{
					//lastMember.teleToLocation(getReflection().getReturnLoc(), 0);
					//getReflection().stopCollapseTimer();
					//getReflection().collapse();
					if(reflection.getInstancedZone() != null && reflection.getInstancedZone().isCollapseOnPartyDismiss())
					{
						if(reflection.getParty() == this) // TODO: убрать затычку
							reflection.startCollapseTimer(reflection.getInstancedZone().getTimerOnCollapse() * 1000);
						if(leader != null && leader.getReflection() == reflection)
							leader.broadcastPacket(new SystemMessage2(SystemMsg.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addInteger(1));
					}
				}
				
			if (room != null && room.getType() == MatchingRoom.PARTY_MATCHING)
				if (isLeader) // Вышел/отвалился лидер, остался один партиец, пати и комната распускаются
					room.disband();
				else // Вышел/кикнули единственного партийца, комната переходит к лидеру, пати распускается
					room.removeMember(player, kick);
					
				dissolveParty();
			}
		}
		else
		{
			if(isInCommandChannel() && _commandChannel.isLeader(player))
				_commandChannel.setChannelLeader(leader);

			if (room != null && room.getType() == MatchingRoom.PARTY_MATCHING)
				room.removeMember(player, kick);

			if(isLeader)
				updateLeaderInfo();
		}

		if(_checkTask != null)
		{
			_checkTask.cancel(true);
			_checkTask = null;
		}

		return true;
	}

	public boolean changePartyLeader(Player player)
	{
		Player leader = getLeader();

		// Меняем местами нового и текущего лидера
		synchronized (_members)
		{
			int index = _members.indexOf(player);
			if(index == -1)
				return false;
			_members.set(0, player);
			_members.set(index, leader);
		}

		leader.sendPacket(ExReplyHandOverPartyMaster.FALSE);
		player.sendPacket(ExReplyHandOverPartyMaster.TRUE);

		updateLeaderInfo();

		if(isInCommandChannel() && _commandChannel.isLeader(leader))
			_commandChannel.setChannelLeader(player);

		return true;
	}

	private void updateLeaderInfo()
	{
		Player leader = getLeader();
		if (leader == null) // некрасиво, но иначе NPE.
			return;

		SystemMessage2 msg = new SystemMessage2(SystemMsg.C1_HAS_BECOME_THE_PARTY_LEADER).addName(leader);

		for(Player member : _members)
			member.sendPacket(PartySmallWindowDeleteAll.STATIC, new PartySmallWindowAll(this, member), msg);

		// броадкасты состояний
		for(Player member : _members)
		{
			sendPacket(member, new PartySpelled(member, true)); // Показываем иконки
			if(member.getPet() != null)
				this.sendPacket(new ExPartyPetWindowAdd(member.getPet())); // Показываем окошки петов
			// broadcastToPartyMembers(member, new PartyMemberPosition(member)); // Обновляем позицию на карте
		}

		MatchingRoom room = leader.getMatchingRoom();
		if (room != null && room.getType() == MatchingRoom.PARTY_MATCHING)
			room.setLeader(leader);
	}

	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(Player player, ItemInstance item, NpcInstance fromNpc)
	{
		switch(item.getItemId())
		{
			case ItemTemplate.ITEM_ID_ADENA:
				distributeAdena(player, item, fromNpc);
				break;
			default:
				Player target = null;

				List<Player> ret = null;
				switch(_itemDistribution)
				{
					case ITEM_RANDOM:
					case ITEM_RANDOM_SPOIL:
						ret = new ArrayList<Player>(_members.size());
						for(Player member : _members)
							if(member.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) && !member.isDead() && member.getInventory().validateCapacity(item) && member.getInventory().validateWeight(item))
								ret.add(member);

						target = ret.isEmpty() ? null : ret.get(Rnd.get(ret.size()));
						break;
					case ITEM_ORDER:
					case ITEM_ORDER_SPOIL:
						synchronized (_members)
						{
							ret = new CopyOnWriteArrayList<Player>(_members);
							while(target == null && !ret.isEmpty())
							{
								int looter = _itemOrder;
								_itemOrder++;
								if(_itemOrder > ret.size() - 1)
									_itemOrder = 0;

								Player looterPlayer = looter < ret.size() ? ret.get(looter) : null;

								if(looterPlayer != null)
								{
									if( !looterPlayer.isDead() && looterPlayer.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) && ItemFunctions.canAddItem(looterPlayer, item))
										target = looterPlayer;
									else
										ret.remove(looterPlayer);
								}
							}
						}

						if(target == null)
							return;
						break;
					case ITEM_LOOTER:
					default:
						target = player;
						break;
				}

				if(target == null)
					target = player;

				if(target.pickupItem(item, Log.PartyPickup))
				{
					if(fromNpc == null)
						player.broadcastPacket(new GetItem(item, player.getObjectId()));

					player.broadcastPickUpMsg(item);
					item.pickupMe();

					sendPacket(target, SystemMessage2.obtainItemsBy(item, target));
				}
				else
					item.dropToTheGround(player, fromNpc, true);
				break;
		}

	}

	private void distributeAdena(Player player, ItemInstance item, NpcInstance fromNpc)
	{
		if(player == null)
			return;

		List<Player> membersInRange = new ArrayList<Player>();

		if(item.getCount() < _members.size())
			membersInRange.add(player);
		else
		{
			for(Player member : _members)
				if(!member.isDead() && (member == player || player.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE)) && ItemFunctions.canAddItem(player, item))
					membersInRange.add(member);
		}

		if(membersInRange.isEmpty())
			membersInRange.add(player);

		long totalAdena = item.getCount();
		long amount = totalAdena / membersInRange.size();
		long ost = totalAdena % membersInRange.size();

		for(Player member : membersInRange)
		{
			long count = member.equals(player) ? amount + ost : amount;
			member.getInventory().addAdena(count);
			member.sendPacket(SystemMessage2.obtainItems(ItemTemplate.ITEM_ID_ADENA, count, 0));
		}

		if(fromNpc == null)
			player.broadcastPacket(new GetItem(item, player.getObjectId()));

		item.pickupMe();
	}

	public void distributeXpAndSp(double xpReward, double spReward, List<Player> rewardedMembers, Creature lastAttacker, MonsterInstance monster)
	{
		recalculatePartyData();

		List<Player> mtr = new ArrayList<Player>();
		int partyLevel = lastAttacker.getLevel();
		int partyLvlSum = 0;

		// считаем минимальный/максимальный уровень
		for(Player member : rewardedMembers)
		{
			if(!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;
			partyLevel = Math.max(partyLevel, member.getLevel());
		}

		// составляем список игроков, удовлетворяющих требованиям
		for(Player member : rewardedMembers)
		{
			if(!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;
			if(member.getLevel() <= partyLevel - Config.MAX_DISTRIBUTE_MEMBER_LEVEL_PARTY)
				continue;
			partyLvlSum += member.getLevel();
			mtr.add(member);
		}

		if(mtr.isEmpty())
			return;

		// бонус за пати
		double bonus = Config.ALT_PARTY_BONUS[mtr.size() - 1];

		// количество эксп и сп для раздачи на всех
		double XP = xpReward * bonus;
		double SP = spReward * bonus;

		for(Player member : mtr)
		{
			double lvlPenalty = Experience.penaltyModifier(monster.calculateLevelDiffForDrop(member.getLevel()), 9);
			int lvlDiff = partyLevel - member.getLevel();
			if(lvlDiff >= 10 && lvlDiff <= 20)
				lvlPenalty *= 0.3D;

			// отдаем его часть с учетом пенальти
			double memberXp = XP * lvlPenalty * member.getLevel() / partyLvlSum;
			double memberSp = SP * lvlPenalty * member.getLevel() / partyLvlSum;

			// больше чем соло не дадут
			memberXp = Math.min(memberXp, xpReward);
			memberSp = Math.min(memberSp, spReward);

			member.addExpAndCheckBonus(monster, (long) memberXp, (long) memberSp, memberXp / xpReward);
		}

		recalculatePartyData();
	}

	public void recalculatePartyData()
	{
		_partyLvl = 0;
		double rateExp = 0.;
		double rateSp = 0.;
		double rateDrop = 0.;
		double rateAdena = 0.;
		double rateSpoil = 0.;
		double minRateExp = Double.MAX_VALUE;
		double minRateSp = Double.MAX_VALUE;
		double minRateDrop = Double.MAX_VALUE;
		double minRateAdena = Double.MAX_VALUE;
		double minRateSpoil = Double.MAX_VALUE;
		int count = 0;

		for(Player member : _members)
		{
			int level = member.getLevel();
			_partyLvl = Math.max(_partyLvl, level);
			count++;

			rateExp += member.getBonus().getRateXp();
			rateSp += member.getBonus().getRateSp();
			rateDrop += member.getBonus().getDropItems();
			rateAdena += member.getBonus().getDropAdena();
			rateSpoil += member.getBonus().getDropSpoil();

			minRateExp = Math.min(minRateExp, member.getBonus().getRateXp());
			minRateSp = Math.min(minRateSp, member.getBonus().getRateSp());
			minRateDrop = Math.min(minRateDrop, member.getBonus().getDropItems());
			minRateAdena = Math.min(minRateAdena, member.getBonus().getDropAdena());
			minRateSpoil = Math.min(minRateSpoil, member.getBonus().getDropSpoil());
		}

		_rateExp = Config.RATE_PARTY_MIN ? minRateExp : rateExp / count;
		_rateSp = Config.RATE_PARTY_MIN ? minRateSp : rateSp / count;
		_rateDrop = Config.RATE_PARTY_MIN ? minRateDrop : rateDrop / count;
		_rateAdena = Config.RATE_PARTY_MIN ? minRateAdena : rateAdena / count;
		_rateSpoil = Config.RATE_PARTY_MIN ? minRateSpoil : rateSpoil / count;
	}

	/**
	 * @return Maximum level of all party members
	 */
	@Override
	public int getLevel()
	{
		return _partyLvl;
	}

	public int getLootDistribution()
	{
		return _itemDistribution;
	}

	public boolean isDistributeSpoilLoot()
	{
		boolean rv = false;

		if(_itemDistribution == ITEM_RANDOM_SPOIL || _itemDistribution == ITEM_ORDER_SPOIL)
			rv = true;

		return rv;
	}

	public boolean isInDimensionalRift()
	{
		return _dimentionalRift > 0 && getDimensionalRift() != null;
	}

	public void setDimensionalRift(DimensionalRift dr)
	{
		_dimentionalRift = dr == null ? 0 : dr.getId();
	}

	public DimensionalRift getDimensionalRift()
	{
		return _dimentionalRift == 0 ? null : (DimensionalRift) ReflectionManager.getInstance().get(_dimentionalRift);
	}

	public boolean isInReflection()
	{
		if(_reflection != null)
			return true;
		if(_commandChannel != null)
			return _commandChannel.isInReflection();
		return false;
	}

	public void setReflection(Reflection reflection)
	{
		_reflection = reflection;
	}

	public Reflection getReflection()
	{
		if(_reflection != null)
			return _reflection;
		if(_commandChannel != null)
			return _commandChannel.getReflection();
		return null;
	}

	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}

	public CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public void setCommandChannel(CommandChannel channel)
	{
		_commandChannel = channel;
	}

	/**
	 * Телепорт всей пати в одну точку (x,y,z)
	 */
	public void Teleport(int x, int y, int z)
	{
		TeleportParty(getMembers(), new Location(x, y, z));
	}

	/**
	 * Телепорт всей пати в одну точку dest
	 */
	public void Teleport(Location dest)
	{
		TeleportParty(getMembers(), dest);
	}

	/**
	 * Телепорт всей пати на территорию, игроки расставляются рандомно по территории
	 */
	public void Teleport(Territory territory)
	{
		RandomTeleportParty(getMembers(), territory);
	}

	/**
	 * Телепорт всей пати на территорию, лидер попадает в точку dest, а все остальные относительно лидера
	 */
	public void Teleport(Territory territory, Location dest)
	{
		TeleportParty(getMembers(), territory, dest);
	}

	public static void TeleportParty(List<Player> members, Location dest)
	{
		for(Player _member : members)
		{
			if(_member == null)
				continue;
			_member.teleToLocation(dest);
		}
	}

	public static void TeleportParty(List<Player> members, Territory territory, Location dest)
	{
		if(!territory.isInside(dest.x, dest.y))
		{
			Log.addGame("TeleportParty: dest is out of territory", "errors");
			Thread.dumpStack();
			return;
		}
		int base_x = members.get(0).getX();
		int base_y = members.get(0).getY();

		for(Player _member : members)
		{
			if(_member == null)
				continue;
			int diff_x = _member.getX() - base_x;
			int diff_y = _member.getY() - base_y;
			Location loc = new Location(dest.x + diff_x, dest.y + diff_y, dest.z);
			while(!territory.isInside(loc.x, loc.y))
			{
				diff_x = loc.x - dest.x;
				diff_y = loc.y - dest.y;
				if(diff_x != 0)
					loc.x -= diff_x / Math.abs(diff_x);
				if(diff_y != 0)
					loc.y -= diff_y / Math.abs(diff_y);
			}
			_member.teleToLocation(loc);
		}
	}

	public static void RandomTeleportParty(List<Player> members, Territory territory)
	{
		for(Player member : members)
			member.teleToLocation(Territory.getRandomLoc(territory, member.getGeoIndex()));
	}

	private void startUpdatePositionTask()
	{
		if(positionTask == null)
			positionTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(new UpdatePositionTask(), 1000, 1000);
	}

	private void stopUpdatePositionTask()
	{
		if(positionTask != null)
			positionTask.cancel(false);
	}

	private class UpdatePositionTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			List<Player> update = new ArrayList<Player>();

			for(Player member : _members)
			{
				Location loc = member.getLastPartyPosition();
				if(loc == null || member.getDistance(loc) > 256) //TODO подкорректировать
				{
					member.setLastPartyPosition(member.getLoc());
					update.add(member);
				}
			}

			if(!update.isEmpty())
				for(Player member : _members)
				{
					PartyMemberPosition pmp = new PartyMemberPosition();
					for(Player m : update)
						if(m != member)
							pmp.add(m);
					if(pmp.size() > 0)
						member.sendPacket(pmp);
				}
		}
	}

	public void requestLootChange(byte type)
	{
		if(_requestChangeLoot != -1)
		{
			if(System.currentTimeMillis() > _requestChangeLootTimer)
				finishLootRequest(false);
			else
				return;
		}
		_requestChangeLoot = type;
		int additionalTime = 45000; // timeout 45sec, guess
		_requestChangeLootTimer = System.currentTimeMillis() + additionalTime;
		_changeLootAnswers = new CopyOnWriteArraySet<Integer>();
		_checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ChangeLootCheck(), additionalTime + 1000, 5000);
		sendPacket(getLeader(), new ExAskModifyPartyLooting(getLeader().getName(), type));
		SystemMessage2 sm = new SystemMessage2(SystemMsg.REQUESTING_APPROVAL_CHANGE_PARTY_LOOT_S1);
		sm.addSysString(LOOT_SYSSTRINGS[type]);
		getLeader().sendPacket(sm);
	}

	public synchronized void answerLootChangeRequest(Player member, boolean answer)
	{
		if(_requestChangeLoot == -1)
			return;
		if(_changeLootAnswers.contains(member.getObjectId()))
			return;
		if(!answer)
		{
			finishLootRequest(false);
			return;
		}
		_changeLootAnswers.add(member.getObjectId());
		if(_changeLootAnswers.size() >= size() - 1)
		{
			finishLootRequest(true);
		}
	}

	private synchronized void finishLootRequest(boolean success)
	{
		if(_requestChangeLoot == -1)
			return;
		if(_checkTask != null)
		{
			_checkTask.cancel(false);
			_checkTask = null;
		}
		if(success)
		{
			this.sendPacket(new ExSetPartyLooting(1, _requestChangeLoot));
			_itemDistribution = _requestChangeLoot;
			SystemMessage2 sm = new SystemMessage2(SystemMsg.PARTY_LOOT_CHANGED_S1);
			sm.addSysString(LOOT_SYSSTRINGS[_requestChangeLoot]);
			this.sendPacket(sm);
		}
		else
		{
			this.sendPacket(new ExSetPartyLooting(0, (byte) 0));
			this.sendPacket(new SystemMessage2(SystemMsg.PARTY_LOOT_CHANGE_CANCELLED));
		}
		_changeLootAnswers = null;
		_requestChangeLoot = -1;
		_requestChangeLootTimer = 0;
	}

	private class ChangeLootCheck extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(System.currentTimeMillis() > _requestChangeLootTimer)
			{
				finishLootRequest(false);
			}
		}
	}

	@Override
	public Iterator<Player> iterator()
	{
		return _members.iterator();
	}
	
	@Override
	public int size()
	{
		return _members.size();
	}

	@Override
	public Player getLeader()
	{
		synchronized (_members)
		{
			if(_members.size() == 0)
				return null;
			return _members.get(0);
		}
	}
	
	@Override
	public void sendPacket(Player exclude, IStaticPacket... packets)
	{
		for (Player plr : getMembers(exclude))
		{
			if (plr != null)
				plr.sendPacket(packets);
		}
	}
	
	@Override
	public void sendPacketInRange(GameObject obj, int range, IStaticPacket... packets)
	{
		for (Player plr : getMembers())
		{
			if (plr != null && plr.isInRangeZ(obj, range))
				plr.sendPacket(packets);
		}
	}
	
	@Override
	public List<Player> getMembersInRange(GameObject obj, int range)
	{
		List<Player> result = new ArrayList<Player>();
		for(Player member : getMembers())
		{
			if (member != null && obj.isPlayer() && member.isInRange(obj, range))
				result.add(member);
		}
		
		return result;
	}
	
	@Override
	public int getMemberCountInRange(GameObject obj, int range)
	{
		int count = 0;
		
		for (Player plr : getMembers())
		{
			if (plr != null && plr.isInRangeZ(obj, range))
				count++;
		}
		
		return count;
	}
	
	@Override
	public List<Integer> getMembersObjIds(Player... excluded)
	{
		List<Integer> result = new ArrayList<Integer>();
		for (Player member : getMembers(excluded))
		{
			if (member != null)
				result.add(member.getObjectId());
		}
		
		return result;
		
	}
	
	@Override
	public List<Playable> getMembersWithPets(Player ... excluded)
	{
		List<Playable> result = new ArrayList<Playable>();
		for(Player member : getMembers(excluded))
		{
			if (member == null)
				continue;
			
			result.add(member);
			
			if(member.getPet() != null)
				result.add(member.getPet());
		}
		
		return result;
	}
	
	@Override
	public Player getPlayerByName(String name)
	{
		if (name == null)
			return null;
		
		for (Player member : getMembers())
		{
			if (name.equalsIgnoreCase(member.getName()))
				return member;
		}
		
		return null;
	}
	
	@Override
	public Player getPlayer(int objId)
	{
		if (getLeader() != null && getLeader().getObjectId() == objId)
			return getLeader();
		
		for (Player member : getMembers())
		{
			if (member != null && member.getObjectId() == objId)
				return member;
		}
		
		return null;
	}

	@Override
	public void sendPacket(IStaticPacket... packets)
	{
		for(Player member : _members)
			member.sendPacket(packets);
	}

	@Override
	public void sendMessage(String message)
	{
		for(Player member : _members)
			member.sendMessage(message);
	}

	@Override
	public void sendMessage(CustomMessage string)
	{
		for(Player member : _members)
			member.sendMessage(string);
	}

	@Override
	public void sendChatMessage(int objectId, int messageType, String charName, String text)
	{
		for(Player member : _members)
			member.sendChatMessage(objectId, messageType, charName, text);
	}

	@Override
	public boolean isLeader(Player player)
	{
		if (getLeader() == null)
			return false;
		
		return getLeader() == player;
	}
}