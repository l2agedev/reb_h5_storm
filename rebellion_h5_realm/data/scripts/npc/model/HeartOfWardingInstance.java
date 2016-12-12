package npc.model;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.templates.npc.NpcTemplate;
import bosses.AntharasManager;

/**
 * @author pchayka
 */

public final class HeartOfWardingInstance extends NpcInstance
{

	public HeartOfWardingInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("enter_lair"))
		{
			AntharasManager.enterTheLair(player);
			return;
		}
		else
			super.onBypassFeedback(player, command);
	}
}