package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Creature;
import l2r.gameserver.templates.item.support.FishGroup;
import l2r.gameserver.templates.item.support.LureType;
import l2r.gameserver.utils.Location;


public class ExFishingStart extends L2GameServerPacket
{
	private int _charObjId;
	private Location _loc;
	private FishGroup _fishGroup;
	private LureType _lureType;

	public ExFishingStart(Creature character, FishGroup fishGroup, Location loc, LureType type)
	{
		_charObjId = character.getObjectId();
		_fishGroup = fishGroup;
		_loc = loc;
		_lureType = type;
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x1e);
		writeD(_charObjId);
		writeD(_fishGroup.ordinal()); // fish type
		writeD(_loc.x); // x poisson
		writeD(_loc.y); // y poisson
		writeD(_loc.z); // z poisson
		writeC(_lureType.ordinal()); // 0 = day lure  1 = night lure
		writeC(0x01); // result Button
	}
}