package l2r.gameserver.taskmanager.actionrunner.tasks;

import l2r.gameserver.Config;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;

/**
 * Task to check if offline shop is in not permited zone.
 * @author Infern0
 */
public class CheckOfflineShopsTask extends AutomaticTask
{
	public CheckOfflineShopsTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		if (!Config.CHECK_PRIVATE_SHOPS)
			return;
		
		for (Player plr : GameObjectsStorage.getAllPlayersForIterate())
		{
			if (plr != null && plr.isInOfflineMode() || plr.isInStoreMode())
			{
				if (plr.isInZone(ZoneType.offbuff))
					continue;
				
				if (plr.isActionBlocked(Zone.BLOCKED_ACTION_PRIVATE_STORE) || plr.isActionBlocked(Zone.BLOCKED_ACTION_PRIVATE_WORKSHOP))
				{
					plr.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					plr.standUp();
					plr.broadcastCharInfo();
					plr.kick();
				}
			}
		}
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return System.currentTimeMillis() + 60000L; // how often to check in ms
	}
}
