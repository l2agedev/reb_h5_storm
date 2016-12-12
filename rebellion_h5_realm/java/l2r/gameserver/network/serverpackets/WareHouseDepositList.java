package l2r.gameserver.network.serverpackets;

import l2r.commons.lang.ArrayUtils;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInfo;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.Warehouse.ItemClassComparator;
import l2r.gameserver.model.items.Warehouse.WarehouseType;

import java.util.ArrayList;
import java.util.List;


public class WareHouseDepositList extends L2GameServerPacket
{
	private int _whtype;
	private long _adena;
	private List<ItemInfo> _itemList;

	public WareHouseDepositList(Player cha, WarehouseType whtype)
	{
		_whtype = whtype.ordinal();
		_adena = cha.getAdena();

		ItemInstance[] items = cha.getInventory().getItems();
		ArrayUtils.eqSort(items, ItemClassComparator.getInstance());
		_itemList = new ArrayList<ItemInfo>(items.length);
		for(ItemInstance item : items)
			if(_whtype == 1)
			{
				if(item.canBeStored(cha, true))
					_itemList.add(new ItemInfo(item));
			}
			else if(_whtype == 2)
			{
				if(item.canBeStored(cha, false))
					_itemList.add(new ItemInfo(item));
			}
			else if(_whtype == 3)
			{
				if(item.canBeStored(cha, false))
					_itemList.add(new ItemInfo(item));
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		writeH(_whtype);
		writeQ(_adena);
		writeH(_itemList.size());
		for(ItemInfo item : _itemList)
		{
			writeItemInfo(item);
			writeD(item.getObjectId());
		}
	}
}