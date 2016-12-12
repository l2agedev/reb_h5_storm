package npc.model;

import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;


public final class NaiaCubeInstance extends NpcInstance
{
	private static final Location TELEPORT_POSITION = new Location(16328, 209384, -9382);
	
	public NaiaCubeInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_enter_beleth"))
		{
			if(ServerVariables.getLong("BelethKillTime", 0) > System.currentTimeMillis())
			{
				player.sendPacket(new NpcHtmlMessage(player, this).setHtml("Teleportation Cubic:<br><br>The machine is unable to draw on Beleth's energy and so is not functioning properly at this time."));
				return;
			}
			
			player.teleToLocation(TELEPORT_POSITION, 0);
		}
		else
			super.onBypassFeedback(player, command);
	}
}