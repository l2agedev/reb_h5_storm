package l2r.gameserver.network.serverpackets;

import l2r.gameserver.templates.PlayerTemplate;

import java.util.ArrayList;
import java.util.List;


public class NewCharacterSuccess extends L2GameServerPacket
{
	private List<PlayerTemplate> _chars = new ArrayList<PlayerTemplate>();

	public void addChar(PlayerTemplate template)
	{
		_chars.add(template);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x0d);
		writeD(_chars.size());

		for(PlayerTemplate temp : _chars)
		{
			writeD(temp.race.ordinal());
			writeD(temp.classId.getId());
			writeD(0x46);
			writeD(temp.getBaseSTR());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseDEX());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseCON());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseINT());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseWIT());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseMEN());
			writeD(0x0a);
		}
	}
}