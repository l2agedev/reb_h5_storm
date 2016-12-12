package npc.model.residences.fortress;

import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.residence.Residence;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;


/**
 * @author VISTALL
 * @date 13:47/02.04.2011
 */
public class DoormanInstance extends npc.model.residences.DoormanInstance
{
	private Location _loc;

	public DoormanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		String loc = template.getAIParams().getString("tele_loc", null);
		if(loc != null)
			_loc = Location.parseLoc(loc);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;
		int cond = getCond(player);
		switch(cond)
		{
			case COND_OWNER:
				if(command.equalsIgnoreCase("openDoors"))
					for(int i : _doors)
						ZoneHolder.getDoor(i).openMe(player, true);
				else if (command.equalsIgnoreCase("closeDoors"))
					for(int i : _doors)
						ZoneHolder.getDoor(i).closeMe(player, true);
				break;
			case COND_SIEGE:
				if(command.equalsIgnoreCase("tele"))
					player.teleToLocation(_loc);
				break;
			case COND_FAIL:
				player.sendPacket(new NpcHtmlMessage(player, this, _failDialog, 0));
				break;
		}
	}

	@Override
	public void setDialogs()
	{
		 _mainDialog = "residence2/fortress/fortress_doorkeeper001.htm";
		 _failDialog = "residence2/fortress/fortress_doorkeeper002.htm";
		 _siegeDialog = "residence2/fortress/fortress_doorkeeper003.htm";
	}

	@Override
	public int getOpenPriv()
	{
		return Clan.CP_CS_ENTRY_EXIT;
	}

	@Override
	public Residence getResidence()
	{
		return getFortress();
	}
}
