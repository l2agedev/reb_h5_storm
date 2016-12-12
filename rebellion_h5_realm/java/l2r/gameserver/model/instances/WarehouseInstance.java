package l2r.gameserver.model.instances;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Log;

public class WarehouseInstance extends NpcInstance
{
	public WarehouseInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = "";
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		if(getTemplate().getHtmRoot() != null)
			return getTemplate().getHtmRoot() + pom + ".htm";
		else
			return "warehouse/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(player.getEnchantScroll() != null)
		{
			Log.addGame("Player " + player.getName() + " trying to use enchant exploit[Warehouse], ban this player!", "illegal-actions");
			player.setEnchantScroll(null);
			return;
		}

		if(command.startsWith("WithdrawP"))
		{
			int val = Integer.parseInt(command.substring(10));
			if(val == 99)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("warehouse/personal.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else
				player.showRetrieveWindow(val);
		}
		else if(command.equals("DepositP"))
			player.showDepositWindow();
		else if(command.startsWith("WithdrawC"))
		{
			int val = Integer.parseInt(command.substring(10));
			if(val == 99)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("warehouse/clan.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else
				player.showWithdrawWindowClan(val);
		}
		else if(command.equals("DepositC"))
			player.showDepositWindowClan();
		else
			super.onBypassFeedback(player, command);
	}
}