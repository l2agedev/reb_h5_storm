package l2r.gameserver.model.items;

import l2r.gameserver.model.items.ItemInstance.ItemLocation;
import l2r.gameserver.model.pledge.Clan;

public final class ClanWarehouse extends Warehouse
{
	public ClanWarehouse(Clan clan)
	{
		super(clan.getClanId());
	}

	@Override
	public ItemLocation getItemLocation()
	{
		return ItemLocation.CLANWH;
	}
}