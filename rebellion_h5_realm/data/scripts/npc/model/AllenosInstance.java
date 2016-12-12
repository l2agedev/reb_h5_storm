package npc.model;

import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.instancemanager.SoDManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.templates.npc.NpcTemplate;

/**
 * @author pchayka
 */

public final class AllenosInstance extends NpcInstance
{
	private static final int tiatIzId = 110;

	public AllenosInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("enter_seed"))
		{
			// Время открытого SoD прошло
			if(SoDManager.isAttackStage())
			{
				Reflection r = player.getActiveReflection();
				if(r != null)
				{
					if(player.canReenterInstance(tiatIzId))
						player.teleToLocation(r.getTeleportLoc(), r);
				}
				else if(player.canEnterInstance(tiatIzId))
				{
					ReflectionManager.enterReflection(player, tiatIzId);
				}
			}
			else
				SoDManager.teleportIntoSeed(player);
		}
		else
			super.onBypassFeedback(player, command);
	}
}