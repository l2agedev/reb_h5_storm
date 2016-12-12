package l2r.gameserver.model.items.attachment;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;

public interface FlagItemAttachment extends PickableAttachment
{
	void onLogout(Player player);
	
	void onDeath(Player owner, Creature killer);

	void onOutTerritory(Player player);
	
	boolean canAttack(Player player);

	boolean canCast(Player player, Skill skill);
}
