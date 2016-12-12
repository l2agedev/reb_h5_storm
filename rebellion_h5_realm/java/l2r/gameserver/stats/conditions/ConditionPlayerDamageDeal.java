package l2r.gameserver.stats.conditions;

import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.stats.Formulas.AttackInfo;
import l2r.gameserver.templates.item.WeaponTemplate;
import l2r.gameserver.templates.item.WeaponTemplate.WeaponType;

public class ConditionPlayerDamageDeal extends Condition
{
	private final int _damage;

	/**
	 * If env.skill is present, it will check skill damage instead of autoattack damage.
	 * @param damage
	 */
	public ConditionPlayerDamageDeal(int damage)
	{
		_damage = damage;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if (env.skill != null && env.skill.isMagic())
			return Formulas.calcMagicDam(env.character, env.target, env.skill, 2) >= _damage;
		
		WeaponTemplate weaponItem = env.character.getActiveWeaponItem();
		boolean dual = false;
		if (weaponItem != null && (weaponItem.getItemType() == WeaponType.DUAL || weaponItem.getItemType() == WeaponType.DUALFIST || weaponItem.getItemType() == WeaponType.DUALDAGGER))
			dual = true;
		
		AttackInfo info = Formulas.calcPhysDam(env.character, env.target, env.skill, dual, false/*TODO Blow support*/, true, false);
		return info.damage >= _damage;
	}
}
