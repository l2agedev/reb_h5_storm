package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.items.ItemInstance;

/**
 * @author VISTALL
 */
public class ExBR_AgathionEnergyInfo extends L2GameServerPacket
{
	private int _size;
	private ItemInstance[] _itemList = null;

  	public ExBR_AgathionEnergyInfo(int size, ItemInstance... item)
	{
		_itemList = item;
		_size = size;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xDE);
		writeD(_size);
		for(ItemInstance item : _itemList)
		{
			if(item.getTemplate().getAgathionEnergy() == 0)
				continue;
			writeD(item.getObjectId());
			writeD(item.getdisplayId() > 0 ? item.getdisplayId() : item.getItemId());
			writeD(0x200000);
			writeD(item.getAgathionEnergy());//current energy
			writeD(item.getTemplate().getAgathionEnergy());   //max energy
		}
	}
}