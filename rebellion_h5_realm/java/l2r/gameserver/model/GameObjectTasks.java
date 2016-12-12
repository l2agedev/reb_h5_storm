package l2r.gameserver.model;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExAutoSoulShot;
import l2r.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.stats.Stats;

import java.util.List;

public class GameObjectTasks
{
	public static class DeleteTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _ref;
		
		public DeleteTask(Creature c)
		{
			_ref = c.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Creature c = _ref.get();
			
			if (c != null)
				c.deleteMe();
		}
	}
	
	// ============================ Таски для L2Player ==============================
	public static class SoulConsumeTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public SoulConsumeTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			player.setConsumedSouls(player.getConsumedSouls() + 1, null);
		}
	}
	
	/** PvPFlagTask */
	public static class PvPFlagTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public PvPFlagTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			
			long diff = Math.abs(System.currentTimeMillis() - player.getlastPvpAttack());
			if (diff > Config.PVP_TIME)
				player.stopPvPFlag();
			else if (diff > Config.PVP_TIME - 20000)
				player.updatePvPFlag(2);
			else
				player.updatePvPFlag(1);
		}
	}
	
	/** Auto Potion Task */
	public static class AutoPotionTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		private final Integer _itemId;
		
		public AutoPotionTask(Player player, int itemId)
		{
			_playerRef = player.getRef();
			_itemId = itemId;
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			
			if (!Config.ENABLE_AUTO_POTIONS)
			{
				for (Integer items : player.getAutoPotion())
					player.sendPacket(new ExAutoSoulShot(items, false));
				
				player.stopAutoPotionTask(-1);
				return;
			}
			
			ItemInstance item = player.getInventory().getItemByItemId(_itemId);
			if (item == null)
			{
				player.removeAutoPotion(_itemId);
				player.stopAutoPotionTask(_itemId);
				return;
			}
			
			int itemId = item.getItemId();
			
			if (player.isDead())
				return;
			
			if (player.isInOfflineMode())
			{
				player.stopAutoPotionTask(itemId);
				return;
			}
			
			if (NexusEvents.isRegistered(player) || NexusEvents.isInEvent(player))
			{
				player.stopAutoPotionTask(itemId);
				return;
			}
			
			if (item.getCount() <= 0)
			{
				player.stopAutoPotionTask(itemId);
				return;
			}
			
			if (player.isInOlympiadMode())
			{
				player.stopAutoPotionTask(itemId);
				return;
			}
			
			switch (itemId)
			{
				case 22410:
				case 22411:
					if (player.getCurrentCpPercents() > 90)
						return;
					
					int restoreCP = itemId == 22410 ? 200 : 400;
					double addToCp = Math.max(0, Math.min(restoreCP, player.calcStat(Stats.CP_LIMIT, null, null) * player.getMaxCp() / 100. - player.getCurrentCp()));
					
					if (addToCp > 0)
					{
						player.setCurrentCp(addToCp + player.getCurrentCp());
						player.sendPacket(new SystemMessage2(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addInteger((long) addToCp));
						player.broadcastPacket(new MagicSkillUse(player, player, 2166, 1, 1000, 0));
					}
					break;
				case 22412:
				case 22413:
					if (player.getCurrentHpPercents() > 90)
						return;
					
					int restoreHP = itemId == 22412 ? 200 : 400;
					double addToHp = Math.max(0, Math.min(restoreHP, player.calcStat(Stats.HP_LIMIT, null, null) * player.getMaxHp() / 100. - player.getCurrentHp()));
					
					if (addToHp > 0)
					{
						player.sendPacket(new SystemMessage2(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addInteger(Math.round(addToHp)));
						player.setCurrentHp(addToHp + player.getCurrentHp(), false);
						player.broadcastPacket(new MagicSkillUse(player, player, 2592, 1, 1000, 0));
					}
					break;
				case 22414:
				case 22415:
					if (player.getCurrentMpPercents() > 90)
						return;
					
					int restoreMP = itemId == 22414 ? 200 : 400;
					double addToMp = Math.max(0, Math.min(restoreMP, player.calcStat(Stats.MP_LIMIT, null, null) * player.getMaxMp() / 100. - player.getCurrentMp()));
					
					if (addToMp > 0)
					{
						player.sendPacket(new SystemMessage2(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addInteger(Math.round(addToMp)));
						player.setCurrentMp(addToMp + player.getCurrentMp());
						player.broadcastPacket(new MagicSkillUse(player, player, 90001, 1, 1000, 0));
					}
					break;
			}
		}
	}
	
	/** HourlyTask */
	public static class HourlyTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public HourlyTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			// Каждый час в игре оповещаем персонажу сколько часов он играет.
			int hoursInGame = player.getHoursInGame();
			player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_S1).addInteger(hoursInGame));
			player.sendPacket(new SystemMessage2(SystemMsg.YOU_OBTAINED_S1_RECOMMENDS).addInteger(player.addRecomLeft()));
		}
	}
	
	/** RecomBonusTask */
	public static class RecomBonusTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public RecomBonusTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			player.setRecomBonusTime(0);
			player.sendPacket(new ExVoteSystemInfo(player));
		}
	}
	
	/** ChargeCountDownTask */
	public static class ChargeCountDownTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public ChargeCountDownTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			
			player.setIncreasedForce(0);
		}
	}
	
	/** WaterTask */
	public static class WaterTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public WaterTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			if (player.isDead() || !player.isInWater())
			{
				player.stopWaterTask();
				return;
			}
			
			double reduceHp = player.getMaxHp() < 100 ? 1 : player.getMaxHp() / 100;
			player.reduceCurrentHp(reduceHp, player, null, false, false, true, false, false, false, false);
			player.sendPacket(new SystemMessage2(SystemMsg.YOU_RECEIVED_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE).addInteger((long) reduceHp));
		}
	}
	
	/** KickTask */
	public static class KickTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public KickTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			player.setOfflineMode(false);
			player.kick();
		}
	}
	
	/** UnJailTask */
	public static class UnJailTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		private boolean _msg;
		
		public UnJailTask(Player player, boolean msg)
		{
			_playerRef = player.getRef();
			_msg = msg;
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			
			if (player == null)
				return;
			
			// String[] re = player.getVar("jailedFrom").split(";");
			player.unsetVar("jailedFrom");
			player.unsetVar("jailed");
			
			if (player.isBlocked()) // prevent locks
				player.unblock();
			
			// lets port them to floran on unjail.
			player.teleToLocation(17836, 170178, -3507, 0);
			
			// player.teleToLocation(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
			// player.setReflection(re.length > 3 ? Integer.parseInt(re[3]) : 0);
			
			if (_msg)
			{
				// player.sendPacket(new Say2(0, ChatType.TELL, "Server", "This time you left the jail. Try to not come back here :)"));
				player.sendPacket(new NpcHtmlMessage(0).setFile("jailed_out.htm"));
			}
			
			player.standUp();
		}
	}
	
	/** EndSitDownTask */
	public static class EndSitDownTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public EndSitDownTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			player.sittingTaskLaunched = false;
			player.getAI().clearNextAction();
		}
	}
	
	/** EndStandUpTask */
	public static class EndStandUpTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public EndStandUpTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if (player == null)
				return;
			player.sittingTaskLaunched = false;
			player.setSitting(false);
			if (!player.getAI().setNextIntention())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	// ============================ Таски для L2Character ==============================
	
	/** AltMagicUseTask */
	public static class AltMagicUseTask extends RunnableImpl
	{
		public final Skill _skill;
		private final HardReference<? extends Creature> _charRef;
		private final HardReference<? extends Creature> _targetRef;
		
		public AltMagicUseTask(Creature character, Creature target, Skill skill)
		{
			_charRef = character.getRef();
			_targetRef = target.getRef();
			_skill = skill;
		}
		
		@Override
		public void runImpl()
		{
			Creature cha, target;
			if ((cha = _charRef.get()) == null || (target = _targetRef.get()) == null)
				return;
			cha.altOnMagicUseTimer(target, _skill);
		}
	}
	
	/** CancelAttackStanceTask */
	public static class CancelAttackStanceTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		
		public CancelAttackStanceTask(Creature character)
		{
			_charRef = character.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if (character == null)
				return;
			character.stopAttackStanceTask();
		}
	}
	
	/** CastEndTimeTask */
	public static class CastEndTimeTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		
		public CastEndTimeTask(Creature character)
		{
			_charRef = character.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if (character == null)
				return;
			character.onCastEndTime();
		}
	}
	
	/** HitTask */
	public static class HitTask extends RunnableImpl
	{
		boolean _crit;
		boolean _miss;
		boolean _shld;
		boolean _soulshot;
		boolean _unchargeSS;
		boolean _notify;
		int _damage;
		private final HardReference<? extends Creature> _charRef;
		private final HardReference<? extends Creature> _targetRef;
		
		public HitTask(Creature cha, Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, boolean notify)
		{
			_charRef = cha.getRef();
			_targetRef = target.getRef();
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
			_unchargeSS = unchargeSS;
			_notify = notify;
		}
		
		@Override
		public void runImpl()
		{
			Creature character;
			Creature target;
			if ((character = _charRef.get()) == null || (target = _targetRef.get()) == null)
				return;
			
			if (character.isAttackAborted())
				return;
			
			character.onHitTimer(target, _damage, _crit, _miss, _soulshot, _shld, _unchargeSS);
			
			if (_notify)
				character.getAI().notifyEvent(CtrlEvent.EVT_READY_TO_ACT);
		}
	}
	
	/** Task launching the function onMagicUseTimer() */
	public static class MagicUseTask extends RunnableImpl
	{
		public boolean _forceUse;
		private final HardReference<? extends Creature> _charRef;
		
		public MagicUseTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if (character == null)
				return;
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if (castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			
			// if (!GeoEngine.canSeeTarget(character, castingTarget, false)) Supported via MagicCheckTask
			// character.abortCast(true, false); // Retail doesnt send abort cast message.
			// else
			character.onMagicUseTimer(castingTarget, castingSkill, _forceUse);
		}
	}
	
	/** Checks if the skill conditions are met during casttime, if not, skill will be aborted */
	public static class MagicCheckTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		
		public MagicCheckTask(Creature cha)
		{
			_charRef = cha.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if (character == null)
				return;
			
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if (castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			
			if (character.getCastingTime() >= Config.GEODATA_SKILL_CHECK_TASK_INTERVAL && !GeoEngine.canSeeTarget(character, castingTarget, false))
				character.abortCast(true, false); // Retail doesnt send abort cast message.
		}
	}
	
	/** MagicLaunchedTask */
	public static class MagicLaunchedTask extends RunnableImpl
	{
		public boolean _forceUse;
		private final HardReference<? extends Creature> _charRef;
		
		public MagicLaunchedTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if (character == null)
				return;
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if (castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			List<Creature> targets = castingSkill.getTargets(character, castingTarget, _forceUse);
			character.broadcastPacket(new MagicSkillLaunched(character.getObjectId(), castingSkill.getDisplayId(), castingSkill.getDisplayLevel(), targets));
		}
	}
	
	/** Task of AI notification */
	public static class NotifyAITask extends RunnableImpl
	{
		private final CtrlEvent _evt;
		private final Object _agr0;
		private final Object _agr1;
		private final HardReference<? extends Creature> _charRef;
		
		public NotifyAITask(Creature cha, CtrlEvent evt, Object agr0, Object agr1)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_agr0 = agr0;
			_agr1 = agr1;
		}
		
		public NotifyAITask(Creature cha, CtrlEvent evt)
		{
			this(cha, evt, null, null);
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if (character == null || !character.hasAI())
				return;
			
			character.getAI().notifyEvent(_evt, _agr0, _agr1);
		}
	}

	/** Task of Checking Skill Cast Landing **/
	public static class MagicGeoCheckTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;

		public MagicGeoCheckTask(Creature cha)
		{
			_charRef = cha.getRef();
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			Creature castingTarget = character.getCastingTarget();
			if (castingTarget == null)
				return;
			if (!GeoEngine.canSeeTarget(character, castingTarget, character.isFlying()))
				return;

			character._skillCheckTask = null;
		}
	}
}