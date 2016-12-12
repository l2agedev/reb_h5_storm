package npc.model;

import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.HellboundManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.templates.npc.NpcTemplate;

/**
 * @author pchayka
 */
public class WarpgateInstance extends NpcInstance
{
	
	public WarpgateInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (!canBypassCheck(player, this))
			return;
		
		if (command.startsWith("enter_hellbound"))
		{
			if (Config.HELLBOUND_ENTER_NO_QUEST)
			{
				player.setVar("EnterUrban", 0, -1);
				player.setVar("EnterHellbound", 1, -1);
				player.teleToLocation(-11272, 236464, -3248);
				return;
			}
			else if (HellboundManager.getHellboundLevel() != 0 && (player.isQuestCompleted("_130_PathToHellbound") || player.isQuestCompleted("_133_ThatsBloodyHot")))
			{
				player.setVar("EnterUrban", 0, -1);
				player.setVar("EnterHellbound", 1, -1);
				player.teleToLocation(-11272, 236464, -3248);
				return;
			}
			else
			{
				showChatWindow(player, "default/32318-1.htm");
				return;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}