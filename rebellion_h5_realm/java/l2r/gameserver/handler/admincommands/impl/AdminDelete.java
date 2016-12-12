package l2r.gameserver.handler.admincommands.impl;


import l2r.gameserver.Config;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Spawner;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.SpawnTable;

import org.apache.commons.lang3.math.NumberUtils;

public class AdminDelete implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_delete
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_delete:
				GameObject obj = wordList.length == 1 ? activeChar.getTarget() : GameObjectsStorage.getNpc(NumberUtils.toInt(wordList[1]));
				if(obj != null && obj.isNpc())
				{
					NpcInstance target = (NpcInstance) obj;
					if (Config.SAVE_GM_SPAWN)
						SpawnTable.getInstance().deleteSpawn(target.getSpawnedLoc(), target.getNpcId());
					target.deleteMe();

					Spawner spawn = target.getSpawn();

					if(spawn != null)
						spawn.stopRespawn();
				}
				else
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}