package l2r.gameserver.skills.effects;

import l2r.commons.util.Rnd;
import l2r.gameserver.model.CancelSystem;
import l2r.gameserver.model.Effect;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EffectDispelEffects extends Effect
{
	private final int _cancelRate;
	private final int _negateCount;

	/*
	 * cancelRate is skill dependant constant:
	 * Cancel - 25
	 * Touch of Death/Insane Crusher - 25
	 * Mage/Warrior Bane - 80
	 * Mass Mage/Warrior Bane - 40
	 * Infinity Spear - 10
	 */

	public EffectDispelEffects(Env env, EffectTemplate template)
	{
		super(env, template);
		_cancelRate = template.getParam().getInteger("cancelRate", 0);
		_negateCount = template.getParam().getInteger("negateCount", 5);
	}

	@Override
	public void onStart()
	{
		if(_effected.getEffectList().isEmpty())
			return;
		
		List<Effect> effectList = new ArrayList<Effect>(_effected.getEffectList().getAllEffects());
		List<Effect> buffList = new ArrayList<Effect>();
		
		for(Effect e : effectList)
			if(e.isOffensive() && e.isCancelable())
				buffList.add(e);

		Collections.reverse(buffList);

		if(buffList.isEmpty())
			return;

		int negated = 0;

		for(Effect e : buffList)
		{
			if (e == null)
				continue;
			
			if (e.getSkill().getId() == 1323)
				continue;
			
			if(negated >= _negateCount)
				break;
			
			if (Rnd.chance(_cancelRate))
			{
				negated++;
				
				_effected.sendPacket(new SystemMessage2(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
				CancelSystem.getInstance().onCancel(_effected, e);
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