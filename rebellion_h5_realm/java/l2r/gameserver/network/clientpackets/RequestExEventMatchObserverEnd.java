package l2r.gameserver.network.clientpackets;

import l2r.gameserver.network.serverpackets.ExEventMatchObserver;


public class RequestExEventMatchObserverEnd extends L2GameClientPacket
{
	int _raceId, unk;

	@Override
	protected void readImpl()
	{
		_raceId = readD();
		unk = readD(); // TODO Whats this?
		//System.out.println("Unk="+unk);
	}

	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() != null)
			getClient().getActiveChar().sendPacket(ExEventMatchObserver.exitObserverMode(_raceId));
	}
}