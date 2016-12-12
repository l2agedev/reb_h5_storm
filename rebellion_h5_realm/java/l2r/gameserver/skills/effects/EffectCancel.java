package l2r.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import l2r.commons.util.Rnd;
import l2r.gameserver.model.CancelSystem;
import l2r.gameserver.model.Effect;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.Stats;


public class EffectCancel extends Effect
{
	private final int _minChance;
	private final int _maxChance;
	private final int _cancelRate;
	private final String[] _stackTypes;
	private final int _negateCount;

	/*
	 * cancelRate is skill dependant constant:
	 * Cancel - 25
	 * Touch of Death/Insane Crusher - 25
	 * Mage/Warrior Bane - 80
	 * Mass Mage/Warrior Bane - 40
	 * Infinity Spear - 10
	 */

	public EffectCancel(Env env, EffectTemplate template)
	{
		super(env, template);
		_cancelRate = template.getParam().getInteger("cancelRate", 0);
		_minChance = template.getParam().getInteger("minChance", 25);
		_maxChance = template.getParam().getInteger("maxChance", 75);
		_negateCount = template.getParam().getInteger("negateCount", 5);
		final String st = template.getParam().getString("negateStackTypes", null);
		_stackTypes = st != null ? st.split(";") : null;
	}

	@Override
	public void onStart()
	{
		if(_effected.getEffectList().isEmpty())
			return;
		
		List<Effect> effectList = new ArrayList<Effect>(_effected.getEffectList().getAllEffects());

		List<Effect> buffList = new ArrayList<Effect>();
		for(Effect e : effectList)
		{
			if (!e.isCancelable() || e.getSkill().isToggle() || e.isOffensive())
				continue;

			if (_stackTypes != null)
				if (!ArrayUtils.contains(_stackTypes, e.getStackType()) && !ArrayUtils.contains(_stackTypes, e.getStackType2()))
					continue;

			buffList.add(e);
		}

		// Reversing lists and adding to a new list
		Collections.reverse(buffList);

		if(buffList.isEmpty())
			return;
		
		final double cancel_res_multiplier = Math.max(1 - _effected.calcStat(Stats.CANCEL_RESIST, 0, null, null) / 100., 0);
		final int magicLevel = getSkill().getMagicLevel();
		double prelimChance;
		int eml, dml;
		boolean result;
		int negated = 0;
		for(Effect e : buffList)
		{
			if (e == null)
				continue;
			
			if (e.getSkill().getId() == 1323)
				continue;
			
			if(negated >= _negateCount)
				break;

			eml = e.getSkill().getMagicLevel();
			dml = magicLevel - (eml == 0 ? _effected.getLevel() : eml);
			//buffTime = e.getTimeLeft() / 120;
			//prelimChance = (2. * dml + _cancelRate + buffTime) * cancel_res_multiplier; // retail formula
			
			prelimChance = (2. * dml + _cancelRate) * cancel_res_multiplier; // custom formula
			prelimChance = Math.max(Math.min(prelimChance, _maxChance), _minChance);
			result = Rnd.chance(prelimChance);
			
			negated++;
			
			if(result)
			{
				if (_effected.isPlayer())
				{
					_effected.sendPacket(new SystemMessage2(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getDisplayId(), e.getSkill().getDisplayLevel()));
					CancelSystem.getInstance().onCancel(_effected, e);
				}
				
				e.exit();
			}
		}

		buffList.clear();
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}