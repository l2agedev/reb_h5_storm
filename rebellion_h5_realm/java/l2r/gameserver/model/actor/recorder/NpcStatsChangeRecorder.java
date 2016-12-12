package l2r.gameserver.model.actor.recorder;

import l2r.gameserver.model.instances.NpcInstance;

public class NpcStatsChangeRecorder extends CharStatsChangeRecorder<NpcInstance>
{
	public NpcStatsChangeRecorder(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();

		if ((_changes & BROADCAST_CHAR_INFO) == BROADCAST_CHAR_INFO)
			_activeChar.broadcastCharInfo();
	}
}
