package npc.model.events;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import l2r.gameserver.data.xml.holder.EventHolder;
import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.EventType;
import l2r.gameserver.model.entity.events.impl.UndergroundColiseumEvent;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Util;

/**
 * @author VISTALL
 * @date 15:40/12.07.2011
 */
public class UndergroundColiseumManagerInstance extends UndergroundColiseumHelperInstance
{
	private String _startHtm;

	public UndergroundColiseumManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		_startHtm = getParameter("start_htm", StringUtils.EMPTY);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		UndergroundColiseumEvent coliseumEvent = getEvent(UndergroundColiseumEvent.class);
		if(coliseumEvent == null)
			return;

		List<Player> leaders = coliseumEvent.getObjects(UndergroundColiseumEvent.REGISTERED_LEADERS);

		if(command.equals("register"))
		{
			Party party = player.getParty();
			if(party == null)
				showChatWindow(player, "events/kerthang_manager008.htm");
			else if(party.getLeader() != player)
				showChatWindow(player, "events/kerthang_manager004.htm");
			else if(party.size() < UndergroundColiseumEvent.PARTY_SIZE)
				showChatWindow(player, "events/kerthang_manager010.htm");
			else
			{
				for(int i = 3; i <= 7; i++)
				{
					UndergroundColiseumEvent $event = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, i);
					if($event == null)
						continue;

					List<Player> $leaders = coliseumEvent.getObjects(UndergroundColiseumEvent.REGISTERED_LEADERS);

					for(Player object : $leaders)
						if(object == player)
						{
							showChatWindow(player, "events/kerthang_manager009.htm");
							return;
						}
				}

				for(Player $player : party)
				{
					if($player.getEffectList().getEffectsBySkillId(5661) != null)
					{
						showChatWindow(player, "events/kerthang_manager021.htm", "%name%", $player.getName());
						return;
					}

					if($player.getLevel() < coliseumEvent.getMinLevel() || $player.getLevel() > coliseumEvent.getMaxLevel())
					{
						showChatWindow(player, "events/kerthang_manager011.htm", "%name%", $player.getName());
						return;
					}

					if($player.getDistance(this) > 400)
					{
						showChatWindow(player, "events/kerthang_manager012.htm");
						return;
					}
				}

				if(leaders.size() >= 5)
				{
					showChatWindow(player, "events/kerthang_manager013.htm");
					return;
				}

				coliseumEvent.addObject(UndergroundColiseumEvent.REGISTERED_LEADERS, player);

				showChatWindow(player, "events/kerthang_manager014.htm");
			}
		}
		else if(command.equals("viewMostWins"))
		{
			Pair<String, Integer> mostWin = coliseumEvent.getTopWinner();
			if(mostWin == null)
				showChatWindow(player, "events/kerthang_manager020.htm");
			else
				showChatWindow(player, "events/kerthang_manager019.htm", "%name%", mostWin.getKey(), "%count%", mostWin.getValue());
		}
		else if(command.equals("cancel"))
		{
			Party party = player.getParty();
			if(party == null)
				showChatWindow(player, "events/kerthang_manager008.htm");
			else if(party.getLeader() != player)
				showChatWindow(player, "events/kerthang_manager004.htm");
			else
			{
				for(Player temp : leaders)
					if(temp == player)
					{
						leaders.remove(player);

						showChatWindow(player, "events/kerthang_manager005.htm");
						return;
					}

				showChatWindow(player, "events/kerthang_manager006.htm");
			}
		}
		else if(command.equals("viewTeams"))
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(0);
			msg.setFile("events/kerthang_manager003.htm");
			for(int i = 0; i < UndergroundColiseumEvent.REGISTER_COUNT; i++)
			{
				Player team = Util.safeGet(leaders, i);

				msg.replace("%team" + i + "%", team == null ? StringUtils.EMPTY : team.getName());
			}

			player.sendPacket(msg);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... ar)
	{
		showChatWindow(player, _startHtm);
	}
}
