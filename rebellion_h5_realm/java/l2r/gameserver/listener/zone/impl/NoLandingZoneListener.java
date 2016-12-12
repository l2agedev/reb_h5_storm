package l2r.gameserver.listener.zone.impl;

import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.entity.residence.Residence;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.PetDataTable;

public class NoLandingZoneListener implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new NoLandingZoneListener();

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		Player player = actor.getPlayer();
		if(player != null)
			if(player.isFlying() && player.getMountNpcId() == PetDataTable.WYVERN_ID)
			{
				Residence residence = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence", 0));
				if(residence != null && player.getClan() != null && residence.getOwner() == player.getClan())
				{
					//
				}
				else
				{
					player.stopMove();
					player.sendPacket(SystemMsg.THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN);
					player.dismount();
				}
			}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature cha)
	{}
}
