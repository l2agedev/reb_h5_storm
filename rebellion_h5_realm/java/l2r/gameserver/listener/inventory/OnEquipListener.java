package l2r.gameserver.listener.inventory;

import l2r.commons.listener.Listener;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.items.ItemInstance;

public interface OnEquipListener extends Listener<Playable>
{
	public void onEquip(int slot, ItemInstance item, Playable actor);

	public void onUnequip(int slot, ItemInstance item, Playable actor);
}
