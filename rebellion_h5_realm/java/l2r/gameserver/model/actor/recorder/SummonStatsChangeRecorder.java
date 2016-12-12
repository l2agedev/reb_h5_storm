package l2r.gameserver.model.actor.recorder;

import l2r.gameserver.model.Summon;

public class SummonStatsChangeRecorder extends CharStatsChangeRecorder<Summon>
{
	public SummonStatsChangeRecorder(Summon actor)
	{
		super(actor);
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();

		if ((_changes & SEND_CHAR_INFO) == SEND_CHAR_INFO)
			_activeChar.sendPetInfo();
		else if ((_changes & BROADCAST_CHAR_INFO) == BROADCAST_CHAR_INFO)
			_activeChar.broadcastCharInfo();
	}
}
