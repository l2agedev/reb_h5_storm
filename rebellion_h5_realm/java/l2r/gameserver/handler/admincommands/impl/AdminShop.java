package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.data.xml.holder.BuyListHolder;
import l2r.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExBuySellList;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.GameStats;

public class AdminShop implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_buy,
		admin_gmshop,
		admin_finditem,
		admin_searchitem,
		admin_tax,
		admin_taxclear
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_buy:
				try
				{
					handleBuyRequest(activeChar, fullString.substring(10));
				}
				catch(IndexOutOfBoundsException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminshop.message1", activeChar));
				}
				break;
			case admin_gmshop:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/gmshops.htm"));
				break;
			case admin_finditem:
			case admin_searchitem:
				if (wordList.length > 1)
				{
					String search = wordList[1];
					for (int i = 2; i < wordList.length; i++)
						search += wordList[i];
					
					if (search.length() < 3)
					{
						activeChar.sendMessage("Specify at least 3 letters to search.");
						return false;
					}
					
					StringBuilder sb = new StringBuilder(1000);
					sb.append("<html><title>" + search + "</title><body><table>");
					for (ItemTemplate tmpl : ItemHolder.getInstance().getAllTemplates())
					{
						if (sb.length() > 19000)
						{
							sb.append("<tr><td>...</td></tr>");
							break;
						}
						
						if (tmpl != null && tmpl.getName().toLowerCase().contains(search.toLowerCase()))
						{
							sb.append("<tr><td>");
							sb.append(tmpl.getIcon32());
							sb.append("</td><td>");
							sb.append(tmpl.getName());
							sb.append("</td><td>");
							sb.append("<button value=" + tmpl.getItemId() + " action=\"bypass -h admin_create_item " + tmpl.getItemId() + " " + 1 + "\" width=50 height=23 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
							sb.append("</td></tr>");
						}
					}
					sb.append("</table></body></html>");
					activeChar.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
				}
				break;
			case admin_tax:
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminshop.message2", activeChar, GameStats.getTaxSum()));
				break;
			case admin_taxclear:
				GameStats.addTax(-GameStats.getTaxSum());
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminshop.message3", activeChar, GameStats.getTaxSum()));
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleBuyRequest(Player activeChar, String command)
	{
		int val = -1;

		try
		{
			val = Integer.parseInt(command);
		}
		catch(Exception e)
		{

		}

		NpcTradeList list = BuyListHolder.getInstance().getBuyList(val);

		if(list != null)
			activeChar.sendPacket(new ExBuySellList.BuyList(list, activeChar, 0.), new ExBuySellList.SellRefundList(activeChar, false));

		activeChar.sendActionFailed();
	}
}