package l2r.gameserver.listener.skills;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;

public interface OnSkillEnchantListener extends PlayerListener
{
	public void onSkillEnchant(Player player, Skill skill, boolean success, boolean safeEnchant);
}
