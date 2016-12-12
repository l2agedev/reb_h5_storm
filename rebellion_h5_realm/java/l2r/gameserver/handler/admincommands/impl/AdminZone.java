package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.mapregion.DomainArea;

import java.util.ArrayList;
import java.util.List;


public class AdminZone implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_zone_check,
		admin_region,
		admin_pos,
		admin_vis_count,
		admin_domain
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_zone_check:
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message1", activeChar, String.valueOf(activeChar.getCurrentRegion())));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message2", activeChar));
				List<Zone> zones = new ArrayList<Zone>();
				World.getZones(zones, activeChar.getLoc(), activeChar.getReflection());
				for(Zone zone : zones)
					if (zone.isActive()) {
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message3", activeChar, zone.getType().toString(), zone.getName(),  String.valueOf(zone.checkIfInZone(activeChar)), String.valueOf(zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()))));
					} else {
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message4", activeChar, zone.getType().toString(), zone.getName(),  String.valueOf(zone.checkIfInZone(activeChar)), String.valueOf(zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()))));
					}
					// activeChar.sendMessage(activeChar.isLangRus() ? zone.getType().toString() + ", имя: " + zone.getName() + ", состояние: " + (zone.isActive() ? "active" : "not active") + ", внутри: " + zone.checkIfInZone(activeChar) + "/" + zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()) : zone.getType().toString() + ", name: " + zone.getName() + ", state: " + (zone.isActive() ? "active" : "not active") + ", inside: " + zone.checkIfInZone(activeChar) + "/" + zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()));

				break;
			}
			case admin_region:
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message5", activeChar, activeChar.getCurrentRegion()));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message6", activeChar));
				for(GameObject o : activeChar.getCurrentRegion())
					if(o != null)
						activeChar.sendMessage(o.toString());
				break;
			}
			case admin_vis_count:
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message7", activeChar, activeChar.getCurrentRegion()));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message8", activeChar, World.getAroundPlayers(activeChar).size()));
				break;
			}
			case admin_pos:
			{
				String pos = activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ() + ", " + activeChar.getHeading() + " Geo [" + (activeChar.getX() - World.MAP_MIN_X >> 4) + ", " + (activeChar.getY() - World.MAP_MIN_Y >> 4) + "] Ref " + activeChar.getReflectionId();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message9", activeChar, pos));
				break;
			}
			case admin_domain:
			{
				DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, activeChar);
				Castle castle = domain != null ? ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId()) : null;
				if (castle != null)
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message10", activeChar, castle.getName()));
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message11", activeChar));
			}
		}
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}