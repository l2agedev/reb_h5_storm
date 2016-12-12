package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class AdminGeodata implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_geo_z,
		admin_geo_type,
		admin_geo_nswe,
		admin_geo_los,
		admin_geo_load,
		admin_geo_dump,
		admin_geo_trace,
		admin_geo_map
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_geo_z:
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message1", activeChar, GeoEngine.getHeight(activeChar.getLoc(), activeChar.getReflectionId()), activeChar.getZ()));
				break;
			case admin_geo_type:
				int type = GeoEngine.getType(activeChar.getX(), activeChar.getY(), activeChar.getReflectionId());
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message2", activeChar, type));
				break;
			case admin_geo_nswe:
				String result = "";
				byte nswe = GeoEngine.getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getReflectionId());
				if((nswe & 8) == 0)
					result += " N";
				if((nswe & 4) == 0)
					result += " S";
				if((nswe & 2) == 0)
					result += " W";
				if((nswe & 1) == 0)
					result += " E";
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message3", activeChar, nswe, result));
				break;
			case admin_geo_los:
				if(activeChar.getTarget() != null)
					if(GeoEngine.canSeeTarget(activeChar, activeChar.getTarget(), false))
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message4", activeChar));
					else
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message5", activeChar));
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message6", activeChar));
				break;
			case admin_geo_load:
				if(wordList.length != 3)
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message7", activeChar));
				else
					try
				{
						byte rx = Byte.parseByte(wordList[1]);
						byte ry = Byte.parseByte(wordList[2]);
						if(GeoEngine.LoadGeodataFile(rx, ry))
						{
							GeoEngine.LoadGeodataFile(rx, ry);
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message8", activeChar, rx, ry));
						}
						else
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message9", activeChar, rx, ry));
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("common.Error", activeChar));
				}
				break;
			case admin_geo_dump:
				if(wordList.length > 2)
				{
					GeoEngine.DumpGeodataFileMap(Byte.parseByte(wordList[1]), Byte.parseByte(wordList[2]));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message10", activeChar, wordList[1], wordList[2]));
				}
				GeoEngine.DumpGeodataFile(activeChar.getX(), activeChar.getY());
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message11", activeChar));
				break;
			case admin_geo_trace:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message12", activeChar));
					return false;
				}
				if(wordList[1].equalsIgnoreCase("on"))
					activeChar.setVar("trace", "1", -1);
				else if(wordList[1].equalsIgnoreCase("off"))
					activeChar.unsetVar("trace");
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message13", activeChar));
				break;
			case admin_geo_map:
				int x = (activeChar.getX() - World.MAP_MIN_X >> 15) + Config.GEO_X_FIRST;
				int y = (activeChar.getY() - World.MAP_MIN_Y >> 15) + Config.GEO_Y_FIRST;

				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message14", activeChar, x, y));
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