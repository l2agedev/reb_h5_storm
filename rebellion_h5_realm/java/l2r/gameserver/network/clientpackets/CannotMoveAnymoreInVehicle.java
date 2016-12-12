package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.boat.Boat;
import l2r.gameserver.utils.Location;

// format: cddddd
public class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private Location _loc = new Location();
	private int _boatid;

	@Override
	protected void readImpl()
	{
		_boatid = readD();
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Boat boat = player.getBoat();
		if(boat != null && boat.getObjectId() == _boatid)
		{
			player.setInBoatPosition(_loc);
			player.setHeading(_loc.h);
			player.broadcastPacket(boat.inStopMovePacket(player));
		}
	}
}