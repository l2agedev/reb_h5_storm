package l2r.gameserver.network.serverpackets;


import l2r.gameserver.data.xml.holder.HennaHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.templates.Henna;

import java.util.ArrayList;
import java.util.List;

public class HennaEquipList extends L2GameServerPacket
{
	private int _emptySlots;
	private long _adena;
	private List<Henna> _hennas = new ArrayList<Henna>();

	public HennaEquipList(Player player, boolean checkForItems)
	{
		_adena = player.getAdena();
		_emptySlots = player.getHennaEmptySlots();

		List<Henna> list = HennaHolder.getInstance().generateList(player);
		for(Henna element : list)
			if(checkForItems ? player.getInventory().getItemByItemId(element.getDyeId()) != null : true)
				_hennas.add(element);
	}

	public HennaEquipList(Player player)
	{
		this(player, true);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xee);

		writeQ(_adena);
		writeD(_emptySlots);
		if(_hennas.size() != 0)
		{
			writeD(_hennas.size());
			for(Henna henna : _hennas)
			{
				writeD(henna.getSymbolId()); //symbolid
				writeD(henna.getDyeId()); //itemid of dye
				writeQ(henna.getDrawCount());
				writeQ(henna.getPrice());
				writeD(1); //meet the requirement or not
			}
		}
		else
		{
			writeD(0x01);
			writeD(0x00);
			writeD(0x00);
			writeQ(0x00);
			writeQ(0x00);
			writeD(0x00);
		}
	}
}