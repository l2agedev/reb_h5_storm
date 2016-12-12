package npc.model;

import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.residence.ResidenceType;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.npc.NpcTemplate;

import instances.RimPailaka;


/**
 * @author pchayka
 */

public final class PailakaGatekeeperInstance extends NpcInstance
{
	private static final int rimIzId = 80;

	public PailakaGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("rimentrance"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(rimIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(rimIzId))
			{
				if(checkGroup(player))
				{
					ReflectionManager.enterReflection(player, new RimPailaka(), rimIzId);
				}
				else
					//FIXME [G1ta0] кастом сообщение
					player.sendMessage(new CustomMessage("scripts.npc.model.pailakagatekeeperinstance.rimentrance", player));
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	private boolean checkGroup(Player p)
	{
		if(!p.isInParty())
			return false;
		for(Player member : p.getParty())
		{
			if(member.getClan() == null)
				return false;
			if(member.getClan().getResidenceId(ResidenceType.Castle) == 0 && member.getClan().getResidenceId(ResidenceType.Fortress) == 0)
				return false;
		}
		return true;
	}
}