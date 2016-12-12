package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.items.ItemInfo;
import l2r.gameserver.model.items.ItemInstance;

/**
 * ddQhdhhhhhdhhhhhhhh - Gracia Final
 */
public class ExRpItemLink extends L2GameServerPacket
{
	private ItemInfo _item;

	private ItemInstance _itemInstance;
	
	public ExRpItemLink(ItemInfo item)
	{
		_item = item;
	}

	public ExRpItemLink(ItemInstance item)
	{
		_itemInstance = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(0x6c);
		if (_item != null)
			writeItemInfo(_item);
		else
			writeItemInfo(_itemInstance);
	}
}