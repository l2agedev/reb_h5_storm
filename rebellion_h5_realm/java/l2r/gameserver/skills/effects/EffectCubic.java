package l2r.gameserver.skills.effects;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.data.xml.holder.CubicHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.templates.CubicTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class EffectCubic extends Effect
{
	private class ActionTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(!isActive())
				return;

			Player player = _effected != null ? _effected.getPlayer() : null;
			if(player == null)
				return;

			doAction(player);
		}
	}

	private final CubicTemplate _template;
	private Future<?> _task = null;

	public EffectCubic(Env env, EffectTemplate template)
	{
		super(env, template);
		_template = CubicHolder.getInstance().getTemplate(getTemplate().getParam().getInteger("cubicId"), getTemplate().getParam().getInteger("cubicLevel"));
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = _effected.getPlayer();
		if(player == null)
			return;

		player.addCubic(this);
		_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ActionTask(), 1000L, 1000L);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		Player player = _effected.getPlayer();
		if(player == null)
			return;

		player.removeCubic(getId());
		_task.cancel(true);
		_task = null;
	}

	public void doAction(Player player)
	{
		int chance = Rnd.get(100);
		for (Map.Entry<Integer, List<CubicTemplate.SkillInfo>> entry : _template.getSkills())
			if ((chance -= entry.getKey()) < 0)
			{
				for (CubicTemplate.SkillInfo skillInfo : entry.getValue())
				{
					if (player.isSkillDisabled(skillInfo.getSkill()))
						continue;
					
					switch (skillInfo.getActionType())
					{
						case ATTACK:
							doAttack(player, skillInfo, _template.getDelay());
							break;
						case DEBUFF:
							doDebuff(player, skillInfo, _template.getDelay());
							break;
						case HEAL:
							doHeal(player, skillInfo, _template.getDelay());
							break;
						case CANCEL:
							doCancel(player, skillInfo, _template.getDelay());
							break;
					}
				}
				break;
			}
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public boolean isCancelable()
	{
		return false;
	}

	public int getId()
	{
		return _template.getId();
	}

	private static void doHeal(final Player player, CubicTemplate.SkillInfo info, final int delay)
	{
		final Skill skill = info.getSkill();
		Creature target = null;
		if(player.getParty() == null)
		{
			if(!player.isCurrentHpFull() && !player.isDead())
				target = player;
		}
		else
		{
			double currentHp = Integer.MAX_VALUE;
			for(Player member : player.getParty())
			{
				if(member == null)
					continue;

				if(player.isInRange(member, info.getSkill().getCastRange()) && !member.isCurrentHpFull() && !member.isDead() && member.getCurrentHp() < currentHp)
				{
					currentHp = member.getCurrentHp();
					target = member;
				}
			}
		}

		if(target == null)
			return;

		int chance = info.getChance((int) target.getCurrentHpPercents());

		if(!Rnd.chance(chance))
			return;

		final Creature aimTarget = target;
		player.broadcastPacket(new MagicSkillUse(player, aimTarget, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0));
		player.disableSkill(skill, delay * 1000L);
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				final List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(aimTarget);
				player.broadcastPacket(new MagicSkillLaunched(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);
			}
		}, skill.getHitTime());
	}

	private static void doAttack(final Player player, final CubicTemplate.SkillInfo info, final int delay)
	{
		if(!Rnd.chance(info.getChance()))
			return;

		final Skill skill = info.getSkill();
		
		Creature target = getTarget(player, info);
		if (target == null)
			return;
		
		final Creature aimTarget = target;
		player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0));
		player.disableSkill(skill, delay * 1000L);
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				final List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(aimTarget);

				player.broadcastPacket(new MagicSkillLaunched(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);

				if(aimTarget.isNpc())
					if(aimTarget.paralizeOnAttack(player))
					{
						if(Config.PARALIZE_ON_RAID_DIFF)
							player.paralizeMe(aimTarget, Skill.SKILL_RAID_CURSE_MUTE);
					}
					else
					{
						int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : (int) skill.getPower();
						aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, damage);
					}
			}
		}, skill.getHitTime());
	}

	private static void doDebuff(final Player player, final CubicTemplate.SkillInfo info, final int delay)
	{
		if(!Rnd.chance(info.getChance()))
			return;

		final Skill skill = info.getSkill();
		
		Creature target = getTarget(player, info);
		if (target == null)
			return;

		final Creature aimTarget = target;
		player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0));
		player.disableSkill(skill, delay * 1000L);
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				final List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(aimTarget);
				
				player.broadcastPacket(new MagicSkillLaunched(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets));
				
				Env env = new Env();
				env.character = player;
				env.target = aimTarget;
				env.skill = skill;
				env.value = info.getChance();
				env.chargedShot = player.getChargedSpiritShot();
				env.minChance = skill.getMinChance();
				env.maxChance = skill.getMaxChance();
				
				final boolean succ = Formulas.calcSkillSuccess(env);
				if(succ)
					player.callSkill(skill, targets, false);

				if(aimTarget.isNpc())
					if(aimTarget.paralizeOnAttack(player))
					{
						if(Config.PARALIZE_ON_RAID_DIFF)
							player.paralizeMe(aimTarget, Skill.SKILL_RAID_CURSE_MUTE);
					}
					else
					{
						int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : (int) skill.getPower();
						aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, damage);
					}
			}
		}, skill.getHitTime());
	}

	private static void doCancel(final Player player, final CubicTemplate.SkillInfo info, final int delay)
	{
		if(!Rnd.chance(info.getChance()))
			return;

		final Skill skill = info.getSkill();
		boolean hasDebuff = false;
		for(Effect e : player.getEffectList().getAllEffects())
			if(e != null && e.isOffensive() && e.isCancelable() && !e.getTemplate()._applyOnCaster)
				hasDebuff = true;

		if(!hasDebuff)
			return;

		player.broadcastPacket(new MagicSkillUse(player, player, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0));
		player.disableSkill(skill, delay * 1000L);
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				final List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(player);
				player.broadcastPacket(new MagicSkillLaunched(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);
			}
		}, skill.getHitTime());
	}

	private static final Creature getTarget(final Player owner, final CubicTemplate.SkillInfo info)
	{
		if (!owner.isInCombat())
			return null;

		final GameObject object = owner.getTarget();
		if (object == null || !object.isCreature())
			return null;
		
		final Creature target = (Creature)object;
		if (target.isDead())
			return null;
		
		if ((target.isDoor() && !info.isCanAttackDoor()))
			return null;

		if (!owner.isInRangeZ(target, info.getSkill().getCastRange()))
			return null;

		Player targetPlayer = target.getPlayer();
		if (targetPlayer != null && !targetPlayer.isInCombat())
			return null;

		if (!target.isAutoAttackable(owner))
			return null;

		return target;
	}
}