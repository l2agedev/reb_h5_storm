package npc.model;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;

import bosses.ValakasManager;

/**
 * @author pchayka
 */

public final class ValakasGatekeeperInstance extends NpcInstance
{
	private static final int FLOATING_STONE = 7267;
	private static final Location TELEPORT_POSITION = new Location(183831, -115457, -3296);
	
	// Debug mode for gm
	private static boolean DEBUG = Config.ALT_DEBUG_ENABLED;
	
	public ValakasGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_passage"))
		{
			// Debug mode for gm
			if (DEBUG && player.isGM())
				player.teleToLocation(TELEPORT_POSITION);
			
			if(!ValakasManager.isEnableEnterToLair())
			{
				player.sendMessage(new CustomMessage("scripts.npc.model.valakasgatekeeperinstance.noenter", player));
				return;
			}
			
			if(player.getInventory().getCountOf(FLOATING_STONE) < 1)
			{
				player.sendMessage(new CustomMessage("scripts.npc.model.valakasgatekeeperinstance.floating_stone", player));
				return;
			}
			
			player.setVar("EnterValakas", 1, -1);
			player.getInventory().destroyItemByItemId(FLOATING_STONE, 1);
			player.teleToLocation(TELEPORT_POSITION);
			return;
		}
		else if(command.equalsIgnoreCase("request_valakas"))
		{
			ValakasManager.enterTheLair(player);
			return;
		}
		else
			super.onBypassFeedback(player, command);
	}
}