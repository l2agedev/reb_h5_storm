package l2r.gameserver.model.items.attachment;

import l2r.gameserver.model.Player;

public interface PickableAttachment extends ItemAttachment
{
	boolean canPickUp(Player player);

	void pickUp(Player player);
}
