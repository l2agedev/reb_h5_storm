package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.conditions.ConditionTargetRelation;
import l2r.gameserver.templates.StatsSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Custom chain heal due players ...
// Retail info how should work : // http://boards.lineage2.com/showthread.php?t=215970

public class ChainHeal extends Skill
{
	private final double[] _healPercents;
	private final int _healRadius;
	private final int _maxTargets;

	public ChainHeal(StatsSet set)
	{
		super(set);
		_healRadius = set.getInteger("healRadius", 350);
		String[] params = set.getString("healPercents", "").split(";");
		_maxTargets = params.length;
		_healPercents = new double[params.length];
		for(int i = 0; i < params.length; i++)
			_healPercents[i] = Integer.parseInt(params[i]) / 100.;
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if (target == null)
			return false;
		if(activeChar.isPlayable() && target.isMonster() || target.isDoor())
			return false;
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int curTarget = 0;
		for (Creature target : targets)
		{
			if (target == null || target.isDead() || target.isHealBlocked())
				continue;
			
			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
				if (target.isPlayer() && target.isCursedWeaponEquipped())
					continue;
				else if (activeChar.isPlayer() && activeChar.isCursedWeaponEquipped())
					continue;
			
			getEffects(activeChar, target, getActivateRate() > 0, false);
			
			double hp = _healPercents[curTarget] * target.getMaxHp();
			double addToHp = Math.max(0, Math.min(hp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100. - target.getCurrentHp()));
			
			if (addToHp > 0)
			{
				target.setCurrentHp(addToHp + target.getCurrentHp(), false);
				
				// Nexus support for healing classes.
				if (activeChar != target && NexusEvents.isInEvent(target) && NexusEvents.isInEvent(activeChar))
				{
					if (EventManager.getInstance().getCurrentMainEvent().getBoolean("allowHealers"))
					{
						if (activeChar.getPlayer() != null && target.getPlayer() != null && activeChar.getPlayer().getEventInfo() != null && activeChar.getPlayer().getEventInfo().isPriest())
						{
							int currentHealAmount = (int) (activeChar.getPlayer().getEventInfo().getHealAmount() + addToHp);
							activeChar.getPlayer().getEventInfo().setHealAmount(currentHealAmount);
						}
					}
				}
			}
			
			if (target.isPlayer())
			{
				if (activeChar != target)
					target.sendPacket(new SystemMessage(SystemMessage.XS2S_HP_HAS_BEEN_RESTORED_BY_S1).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
				else
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToHp)));
			}
			else if (target.isSummon() || target.isPet())
			{
				Player owner = target.getPlayer();
				if (owner != null)
				{
					if (activeChar == target) // Пет лечит сам себя
						owner.sendMessage(new CustomMessage("YOU_HAVE_RESTORED_S1_HP_OF_YOUR_PET", owner).addNumber(Math.round(addToHp)));
					else if (owner == activeChar) // Хозяин лечит пета
						owner.sendMessage(new CustomMessage("YOU_HAVE_RESTORED_S1_HP_OF_YOUR_PET", owner).addNumber(Math.round(addToHp)));
					else
						// Пета лечит кто-то другой
						owner.sendMessage(new CustomMessage("S1_HAS_BEEN_RESTORED_S2_HP_OF_YOUR_PET", owner).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
				}
			}
			
			curTarget++;
		}
			

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	@Override
	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		List<Creature> result = new ArrayList<Creature>(_maxTargets);
		
		// Добавляем, если это возможно, текущую цель
		if (!aimingTarget.isInvul() && !aimingTarget.isDead() && !aimingTarget.isHealBlocked() && !aimingTarget.isCursedWeaponEquipped())
			result.add(aimingTarget);
				
		List<Creature> targets = aimingTarget.getAroundCharacters(_healRadius, 128);
		if(targets == null || targets.isEmpty())
			return result;
		
		List<HealTarget> healList = new ArrayList<HealTarget>();
		for(Creature target : targets)
		{
			if(target == null || target.isDead() || target.isHealBlocked() || target.isCursedWeaponEquipped() || target.isDoor() || target.isNpc())
				continue;
			if (activeChar.getObjectId() != aimingTarget.getObjectId() && target.getObjectId() == activeChar.getObjectId())
				continue;
			// TODO: confirm this
			if(target.isAutoAttackable(activeChar) && ConditionTargetRelation.getRelation(activeChar, target) != ConditionTargetRelation.Relation.Friend)
				continue;
			if(target.isInvisible())
				continue;
			
			healList.add(new HealTarget(target));
		}

		if (healList.isEmpty())
			return result;

		Collections.sort(healList);
		
		final int size = Math.min(_maxTargets - result.size(), healList.size()); // возможно текущая цель уже добавлена
		for (int i = 0; i < size; i++)
			result.add(i, healList.get(i).target); // сдвигаем текущую цель в конец если есть

		return result;
	}
	
	private static final class HealTarget implements Comparable<HealTarget>
	{
		private final double hpPercent;
		public final Creature target;

		public HealTarget(Creature target)
		{
			this.target = target;
			this.hpPercent = target.getCurrentHpPercents();
		}

		@Override
		public int compareTo(HealTarget ht)
		{
			if(hpPercent < ht.hpPercent)
				return -1;
			if(hpPercent > ht.hpPercent)
				return 1;
			return 0;
		}
	}
}