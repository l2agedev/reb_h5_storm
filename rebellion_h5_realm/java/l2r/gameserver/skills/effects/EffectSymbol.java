package l2r.gameserver.skills.effects;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.SymbolInstance;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Env;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EffectSymbol extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(EffectSymbol.class);

	private NpcInstance _symbol = null;
	
	public EffectSymbol(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getSkill().getTargetType() != Skill.SkillTargetType.TARGET_SELF)
		{
			_log.error("Symbol skill with target != self, id = " + getSkill().getId());
			return false;
		}

		Skill skill = getSkill().getFirstAddedSkill();
		if(skill == null)
		{
			_log.error("Not implemented symbol skill, id = " + getSkill().getId());
			return false;
		}

		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		
		Skill skill = getSkill().getFirstAddedSkill();
		skill.setMagicType(getSkill().getMagicType()); // Затычка, в клиенте они почему-то не совпадают.
		
		Location loc = _effected.getLoc();
		if(_effected.isPlayer() && ((Player) _effected).getGroundSkillLoc() != null)
		{
			loc = ((Player) _effected).getGroundSkillLoc();
			((Player) _effected).setGroundSkillLoc(null);
		}
		
		NpcTemplate template = NpcHolder.getInstance().getTemplate(_skill.getSymbolId());
		NpcInstance symbol = new SymbolInstance(IdFactory.getInstance().getNextId(), template, _effected, skill, 120000);
		symbol.setLevel(_effected.getLevel());
		symbol.setReflection(_effected.getReflection());
		symbol.spawnMe(loc);

		_symbol = symbol;
	}

	@Override
	public void onExit()
	{
		super.onExit();
		
		if (_symbol == null)
			return;

		_symbol.deleteMe();
		_symbol = null;
	}

	@Override
	public boolean onActionTime()
	{
		if(getTemplate()._count <= 1 || getEffector() == null) // This is to avoid Symbol skills and allow Volcano, Cyclone, Raging Waves and Gehenna
			return false;
		
		Player caster = (Player) getEffector();
		NpcInstance symbol = _symbol;
		
		Skill skill = getSkill().getFirstAddedSkill();
		double mpConsume = getSkill().getMpConsume();

		if(skill == null || caster == null || symbol == null)
			return false;

		if(mpConsume > caster.getCurrentMp())
		{
			caster.sendPacket(SystemMsg.NOT_ENOUGH_MP);
			return false;
		}

		caster.reduceCurrentMp(mpConsume, caster);

		// Использовать разрешено только скиллы типа TARGET_ONE
		for(Creature cha : World.getAroundCharacters(symbol, getSkill().getSkillRadius(), 200))
			if(!cha.isDoor() && cha.getEffectList().getEffectsBySkill(skill) == null && skill.checkTarget(caster, cha, cha, false, false) == null)
			{
				if(skill.isOffensive() && !GeoEngine.canSeeTarget(symbol, cha, false))
					continue;
				List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(cha);
				caster.callSkill(skill, targets, true);
				caster.broadcastPacket(new MagicSkillLaunched(symbol.getObjectId(), getSkill().getDisplayId(), getSkill().getDisplayLevel(), cha));
				cha.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster, 1);
			}

		return true;
	}
	
	@Override
	public boolean isHidden()
	{
		return true;
	}
}