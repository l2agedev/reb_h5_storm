package l2r.gameserver.model.base;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Effect.EffectsComparator;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2r.gameserver.network.serverpackets.AbnormalStatusUpdate;
import l2r.gameserver.network.serverpackets.AutoAttackStart;
import l2r.gameserver.network.serverpackets.ChangeMoveType;
import l2r.gameserver.network.serverpackets.ChangeWaitType;
import l2r.gameserver.network.serverpackets.CharSelected;
import l2r.gameserver.network.serverpackets.ClientSetTime;
import l2r.gameserver.network.serverpackets.Die;
import l2r.gameserver.network.serverpackets.EtcStatusUpdate;
import l2r.gameserver.network.serverpackets.ExAutoSoulShot;
import l2r.gameserver.network.serverpackets.ExBR_ExtraUserInfo;
import l2r.gameserver.network.serverpackets.ExBR_PremiumState;
import l2r.gameserver.network.serverpackets.ExBasicActionList;
import l2r.gameserver.network.serverpackets.ExDominionWarStart;
import l2r.gameserver.network.serverpackets.ExMPCCOpen;
import l2r.gameserver.network.serverpackets.ExNavitAdventPointInfo;
import l2r.gameserver.network.serverpackets.ExNavitAdventTimeChange;
import l2r.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2r.gameserver.network.serverpackets.ExReceiveShowPostFriend;
import l2r.gameserver.network.serverpackets.ExSetCompassZoneCode;
import l2r.gameserver.network.serverpackets.ExStorageMaxCount;
import l2r.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2r.gameserver.network.serverpackets.HennaInfo;
import l2r.gameserver.network.serverpackets.L2FriendList;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.PartySmallWindowAll;
import l2r.gameserver.network.serverpackets.PartySpelled;
import l2r.gameserver.network.serverpackets.PetInfo;
import l2r.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import l2r.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2r.gameserver.network.serverpackets.QuestList;
import l2r.gameserver.network.serverpackets.RecipeShopMsg;
import l2r.gameserver.network.serverpackets.RelationChanged;
import l2r.gameserver.network.serverpackets.RestartResponse;
import l2r.gameserver.network.serverpackets.Ride;
import l2r.gameserver.network.serverpackets.SSQInfo;
import l2r.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import l2r.gameserver.network.serverpackets.ShortCutInit;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.skills.effects.EffectTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Supersnoop
{
	private Player _player;
	private Player _snoopedTarget; // The target this player is snooping. 
	private List<Player> _snoopers; // List of players snooping this player
	
	public Supersnoop(Player player)
	{
		_player = player;
		_snoopers = new ArrayList<Player>();
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public boolean isActive()
	{
		if (_snoopedTarget != null)
			return true;
		
		return !_snoopers.isEmpty();
	}
	
	private boolean addSnooper(Player snooper)
	{
		if (_player.getObjectId() == snooper.getObjectId())
			return false;
		
		snooper.decayMe();
		
		if (!sendSnoopPackets(snooper, _player))
			return false;
		
		return _snoopers.add(snooper);
	}
	
	private boolean removeSnooper(Player snooper)
	{
		if (!_snoopers.remove(snooper))
			return false;
		
		snooper.spawnMe();
		return sendSnoopPackets(snooper, snooper);
	}
	
	public void setSnooping(Player snoopedTarget)
	{
		// Stop any snoopers from snooping a snooper, because snooperception happens.
		if (!_snoopers.isEmpty())
			onDelete();
		
		boolean stoppedSnooping = false;
		if (isSnooping())
		{
			if (_snoopedTarget == snoopedTarget)
				return;
			
			stoppedSnooping = _snoopedTarget.getSupersnoop().removeSnooper(_player);
		}
		
		_snoopedTarget = snoopedTarget;
		if (_snoopedTarget != null && _snoopedTarget.getSupersnoop().addSnooper(_player))
			return;
		
		// Target is null or an error occured.
		if (!stoppedSnooping)
		{
			_player.spawnMe();
			sendSnoopPackets(_player, _player);
		}
	}
	
	public boolean isSnooping()
	{
		return _snoopedTarget != null;
	}
	
	/**
	 * Use snooper and snoopedTarget as the same player, to retrieve his normal state.
	 * @param snooper : The player requesting the snoop.
	 * @param snoopedTarget : the player that is going to be snooped.
	 * @return true if no errors occured.
	 */
	private boolean sendSnoopPackets(Player snooper, Player snoopedTarget)
	{
		try
		{
			snooper.sendPacket(RestartResponse.OK);
			snooper.sendPacket(new CharSelected(snoopedTarget, snooper.getClient().getSessionKey().playOkID1)); // Show loading screen
			//snooper.sendPacket(new ExUISetting(snoopedTarget));
			snooper.sendPacket(new ChangeMoveType(snoopedTarget));
			snooper.sendPacket(new ExBR_PremiumState(snoopedTarget, snoopedTarget.hasBonus()));
			//Macro update?
			snooper.sendPacket(new SSQInfo(), new HennaInfo(snoopedTarget));
			snooper.sendItemList(false);
			snooper.sendPacket(new ShortCutInit(snoopedTarget), new SkillList(snoopedTarget), new SkillCoolTime(snoopedTarget));
			
			snooper.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
			if (snooper != snoopedTarget)
				snooper.sendMessage(new CustomMessage("l2r.gameserver.model.base.supersnoop.message1", snooper));
			else
				snooper.sendMessage(new CustomMessage("l2r.gameserver.model.base.supersnoop.message2", snooper));
			
			snooper.sendPacket(new L2FriendList(snoopedTarget), new ExStorageMaxCount(snoopedTarget), new QuestList(snoopedTarget), new ExBasicActionList(snoopedTarget), new EtcStatusUpdate(snoopedTarget));
			
			if(snoopedTarget.isCastingNow())
			{
				Creature castingTarget = snoopedTarget.getCastingTarget();
				Skill castingSkill = snoopedTarget.getCastingSkill();
				long animationEndTime = snoopedTarget.getAnimationEndTime();
				if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && snoopedTarget.getAnimationEndTime() > 0)
					snooper.sendPacket(new MagicSkillUse(snoopedTarget, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
			}

			if(snoopedTarget.isInBoat())
				snooper.sendPacket(snoopedTarget.getBoat().getOnPacket(snoopedTarget, snoopedTarget.getInBoatPosition()));

			if(snoopedTarget.isMoving() || snoopedTarget.isFollow())
				snooper.sendPacket(snoopedTarget.movePacket());

			if(snoopedTarget.getMountNpcId() != 0)
				snooper.sendPacket(new Ride(snoopedTarget));
			
			Thread.sleep(2000); // Wait!!! No crits pls...
			
			// Send UserInfo to show character.
			snooper.sendPacket(new UserInfo(snoopedTarget), new ExBR_ExtraUserInfo(snoopedTarget));
			DominionSiegeEvent siegeEvent = snoopedTarget.getEvent(DominionSiegeEvent.class);
			if(siegeEvent != null)
				sendPacket(new ExDominionWarStart(snoopedTarget));
			
			if(snoopedTarget.isSitting())
				snooper.sendPacket(new ChangeWaitType(snoopedTarget, ChangeWaitType.WT_SITTING));
			if(snoopedTarget.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
			{
				if(snoopedTarget.getPrivateStoreType() == Player.STORE_PRIVATE_BUY)
					snooper.sendPacket(new PrivateStoreMsgBuy(snoopedTarget));
				else if(snoopedTarget.getPrivateStoreType() == Player.STORE_PRIVATE_SELL || snoopedTarget.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE)
					snooper.sendPacket(new PrivateStoreMsgSell(snoopedTarget));
				else if(snoopedTarget.getPrivateStoreType() == Player.STORE_PRIVATE_MANUFACTURE)
					snooper.sendPacket(new RecipeShopMsg(snoopedTarget));
			}
			
			if (snoopedTarget.isDead())
				snooper.sendPacket(new Die(snoopedTarget));
			
			snooper.sendPacket(new ClientSetTime(snooper), new ExSetCompassZoneCode(snoopedTarget));
			
			if(snoopedTarget.getPet() != null)
				snooper.sendPacket(new PetInfo(snoopedTarget.getPet()));
			
			if(snoopedTarget.isInParty())
			{
				snooper.sendPacket(new PartySmallWindowAll(snoopedTarget.getParty(), snoopedTarget));

				for(Player member : snoopedTarget.getParty())
				{
					if (member == snoopedTarget)
						continue;
					
					snooper.sendPacket(new PartySpelled(member, true));
					if (member.getPet() != null)
						snooper.sendPacket(new PartySpelled(member.getPet(), true));
					
					snooper.sendPacket(RelationChanged.update(snoopedTarget, member, snoopedTarget));
				}

				if(snoopedTarget.getParty().isInCommandChannel())
					snooper.sendPacket(ExMPCCOpen.STATIC);
			}
			
			for(int shotId : snoopedTarget.getAutoSoulShot())
				snooper.sendPacket(new ExAutoSoulShot(shotId, true));
			
			for(Effect e : snoopedTarget.getEffectList().getAllFirstEffects())
				if(e.getSkill().isToggle())
					snooper.sendPacket(new MagicSkillLaunched(snoopedTarget.getObjectId(), e.getSkill().getId(), e.getSkill().getLevel(), snoopedTarget));
			
			// Update effects
			Effect[] effects = snoopedTarget.getEffectList().getAllFirstEffects();
			Arrays.sort(effects, EffectsComparator.getInstance());

			AbnormalStatusUpdate statusUpdate = new AbnormalStatusUpdate();
			for(Effect effect : effects)
			{
				if(effect.isInUse())
				{
					if(effect.getStackType().equals(EffectTemplate.HP_RECOVER_CAST))
						snooper.sendPacket(new ShortBuffStatusUpdate(effect));
					else
						effect.addIcon(statusUpdate);
				}
			}
			snooper.sendPacket(statusUpdate);
			
			if(Config.PCBANG_POINTS_ENABLED)
				snooper.sendPacket(new ExPCCafePointInfo(snoopedTarget, 0, 1, 2, 12));
			
			snooper.sendPacket(new ExReceiveShowPostFriend(snoopedTarget));
			
			snooper.sendPacket(new ExNavitAdventPointInfo(snoopedTarget.getNevitSystem().getPoints()));
			snooper.sendPacket(new ExNavitAdventTimeChange(snoopedTarget.getNevitSystem().isActive(), snoopedTarget.getNevitSystem().getTime()));
			
			snooper.sendPacket(new ExVoteSystemInfo(snoopedTarget));
			
			if(snoopedTarget.isInCombat())
				snooper.sendPacket(new AutoAttackStart(snoopedTarget.getObjectId()));
			
			// Send UserInfo once again, because sometimes the logon screen stucks.
			snooper.sendPacket(new UserInfo(snoopedTarget), new ExBR_ExtraUserInfo(snoopedTarget));
			siegeEvent = snoopedTarget.getEvent(DominionSiegeEvent.class);
			if(siegeEvent != null)
				sendPacket(new ExDominionWarStart(snoopedTarget));
			
			//TODO: Send Item list to update inv.
			//TODO: Send shortcut list.
			//TODO: Sometimes when a player teleports, you get crit. Find the cause.
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void sendPacket(L2GameServerPacket ... gsps)
	{
		if (_snoopers.isEmpty())
			return;
		
		try
		{
			for (Player player : _snoopers)
			{
				if (player == null || player.isInOfflineMode() || player.isLogoutStarted())
					continue;
				
				player.sendPacket(gsps);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void onDelete()
	{
		try
		{
			if (_snoopedTarget != null)
				_snoopedTarget.getSupersnoop().removeSnooper(_player);
			
			for (Player player : _snoopers)
				player.getSupersnoop().setSnooping(null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}