package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.olympiad.CompType;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.olympiad.OlympiadGame;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.utils.Strings;

public class RequestExOlympiadObserverEnd extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (!activeChar.isInObserverMode())
			return;
		
		NpcHtmlMessage reply = new NpcHtmlMessage(0);
		StringBuilder msg = new StringBuilder("");
		
		msg.append("!Current Olympiad Matches<br>");
		
		OlympiadManager manager = Olympiad._manager;
		if (manager != null)
		{
			for (int i = 0; i < Olympiad.STADIUMS.length; i++)
			{
				OlympiadGame game = manager.getOlympiadInstance(i);
				if (game != null && game.getState() > 0)
				{
					if (game.getType() == CompType.TEAM)
					{
						msg.append("<br1>Arena " + (i + 1) + ":&nbsp;<a action=\"bypass -h _olympiad?=move_op_field&=" + i + "\">Team vs Team:</a>");
						msg.append("<br1>- " + game.getTeamName1() + "<br1>- " + game.getTeamName2());
					}
					else
						msg.append("<br1>Arena " + (i + 1) + ":&nbsp;<a action=\"bypass -h _olympiad?=move_op_field&=" + i + "\">" + manager.getOlympiadInstance(i).getTeamName1() + " vs " + manager.getOlympiadInstance(i).getTeamName2() + "</a>");
					
					msg.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
				}
			}
		}
		reply.setHtml(Strings.bbParse(msg.toString()));
		activeChar.sendPacket(reply);
	}
}