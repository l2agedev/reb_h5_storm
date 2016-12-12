package npc.model.residences.fortress.peace;

import l2r.gameserver.model.Player;

import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.CastleSiegeAttackerList;
import l2r.gameserver.network.serverpackets.CastleSiegeInfo;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.templates.npc.NpcTemplate;

public class SuspiciousMerchantInstance extends NpcInstance
{
	public SuspiciousMerchantInstance(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (!canBypassCheck(player, this))
			return;
		
		else if (command.equalsIgnoreCase("showSiegeReg"))
			showSiegeRegWindow(player);
		else if (command.equalsIgnoreCase("showSiegeInfo"))
			showSiegeInfoWindow(player);
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		Fortress fortress = getFortress();
		if (fortress.getOwner() != null)
		{
			html.setFile("residence2/fortress/fortress_ordery001a.htm");
			html.replace("%clan_name%", fortress.getOwner().getName());
		}
		else
			html.setFile("residence2/fortress/fortress_ordery001.htm");
		
		player.sendPacket(html);
	}
	
	public void showSiegeInfoWindow(Player player)
	{
		Fortress fortress = getFortress();
		player.sendPacket(new CastleSiegeInfo(fortress, player));
	}
	
	public void showSiegeRegWindow(Player player)
	{
		Fortress fortress = getFortress();
		player.sendPacket(new CastleSiegeAttackerList(fortress));
	}
}