package l2r.gameserver.nexus_engine.events.engine.partymatcher;

import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.List;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastTable;

/**
 * Created by Lukas
 */
public class PartyMatcherSimple
{
	public static List<Registered> _registered = new FastTable<Registered>();

	public static Cleaner cleaner = new Cleaner();

	public static void showMenu(PlayerEventInfo player, int expanded)
	{
		String html;

		TextBuilder tb = new TextBuilder();

		tb.append("<html><title>Party manager</title><body>");

		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3><img src=\"L2UI.SquareGray\" width=280 height=2><img src=\"L2UI.SquareBlank\" width=270 height=3><table width=280 bgcolor=353535><tr><td align=left width=200><font color=9F9400>Registered players:</font></td><td width=80 align=right><button value=\"Back\" action=\"bypass -h gem_gemmain\" width=65 height=19 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><img src=\"L2UI.SquareBlank\" width=280 height=3><img src=\"L2UI.SquareGray\" width=280 height=2><img src=\"L2UI.SquareBlank\" width=270 height=3>");

		boolean reg = false;
		for(Registered data : _registered)
		{
			if(data._id == player.getPlayersId())
			{
				reg = true;
			}
		}

		if(!reg)
			tb.append("<table width=290><tr><td align=left><button value=\"Refresh\" action=\"bypass -h gem_pm_menu\" width=80 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td align=right><button value=\"I want a party!\" action=\"bypass -h gem_pm_register\" width=120 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br>");
		else
			tb.append("<table width=290><tr><td align=left><button value=\"Refresh\" action=\"bypass -h gem_pm_menu\" width=80 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td align=right><button value=\"Unregister\" action=\"bypass -h gem_pm_unregister\" width=120 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br>");


		boolean self = false;
		for(Registered registered : _registered)
		{
			self = false;
			if(registered._id == player.getPlayersId())
				self = true;

			if(registered._id == expanded)
			{
				tb.append("<table width=280 bgcolor=2f2f2f><tr><td align=left width=120><font color=" + getColorForClass(registered._player) + ">" + registered._player.getPlayersName() + " (" + registered._player.getLevel() + ")</font></td><td width=50 align=right>" + (self ? "" : "<button value=\"Invite\" action=\"bypass -h gem_pm_invite " + registered._id + "\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">") + "</td></tr></table>");
				tb.append("<table width=280 bgcolor=2f2f2f><tr><td width=280>" + registered._player.getClassName() + " | " + registered._player.getPvpKills() + " PvP</td></tr></table>");
			}
			else
			{
				tb.append("<table width=280 bgcolor=3f3f3f><tr><td align=left width=120><font color=" + getColorForClass(registered._player) + "><a action=\"bypass -h gem_pm_menu " + registered._id + "\">" + registered._player.getPlayersName() + "</a> (" + registered._player.getLevel() + ")</font></td><td width=50 align=right>" + (self ? "" : "<button value=\"Invite\" action=\"bypass -h gem_pm_invite " + registered._id + "\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">") + "</td></tr></table>");
			}
		}

		tb.append("<br1><table width=290><tr><td align=left></td></tr></table>");

		tb.append("</body></html>");

		html = tb.toString();

		player.sendHtmlText(html);
		player.sendStaticPacket();
	}

	public static String getColorForClass(PlayerEventInfo player)
	{
		switch(player.getClassType())
		{
			case Fighter:
				return "9D8862";
			case Mystic:
				return "629A9D";
			case Priest:
				return "96629D";
		}
		return "9f9f9f";
	}

	public static void onBypass(PlayerEventInfo player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass);
		st.nextToken();

		if(bypass.startsWith("menu"))
		{
			if(st.hasMoreTokens())
				showMenu(player, Integer.parseInt(st.nextToken()));
			else
				showMenu(player, 0);
		}
		else if(bypass.equalsIgnoreCase("register"))
		{
			addPlayer(player);
			sendAddedWindow(player);
		}
		else if(bypass.equalsIgnoreCase("unregister"))
		{
			removePlayer(player, false);
			showMenu(player, 0);
		}
		else if(bypass.startsWith("invite"))
		{
			int id = Integer.parseInt(st.nextToken());

			invitePlayer(player, id);
			showMenu(player, id);
		}
	}

	public static void addPlayer(PlayerEventInfo player)
	{
		if(_registered.size() > 25)
		{
			player.sendMessage("It's full. Try later again.");
			return;
		}

		if(player.getParty() != null)
		{
			player.sendMessage("You already have a party.");
			return;
		}

		Registered data = new Registered(player, System.currentTimeMillis());
		_registered.add(data);

		player.sendMessage("You have been added to the party matcher.");
	}

	public static void sendAddedWindow(PlayerEventInfo player)
	{
		String html;

		TextBuilder tb = new TextBuilder();

		tb.append("<html><title>Party manager</title><body>");

		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3><img src=\"L2UI.SquareGray\" width=280 height=2><img src=\"L2UI.SquareBlank\" width=270 height=3><table width=280 bgcolor=353535><tr><td align=left width=200><font color=LEVEL>Registered!</font></td><td width=80 align=right><button value=\"Back\" action=\"bypass -h gem_gemmain\" width=65 height=19 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><img src=\"L2UI.SquareBlank\" width=280 height=3><img src=\"L2UI.SquareGray\" width=280 height=2><img src=\"L2UI.SquareBlank\" width=270 height=3>");

		tb.append("<br><font color=LEVEL>You have added yourself to the party matcher!</font><br1>");
		tb.append("<font color=LEVEL>The other players will now see you seek a party and they can invite you easily.</font><br>");
		tb.append("<font color=AAA76C>The system will try to auto-match the registered players and create a party automatically in about 1-2 minutes.</font>");

		tb.append("<br><center><button value=\"Back\" action=\"bypass -h gem_pm_menu\" width=100 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");

		tb.append("</body></html>");

		html = tb.toString();

		player.sendHtmlText(html);
		player.sendStaticPacket();
	}

	public static void removePlayer(PlayerEventInfo player, boolean force)
	{
		for(Registered data : _registered)
		{
			if(data._id == player.getPlayersId())
			{
				_registered.remove(data);
				player.sendMessage("You have been removed from the party matcher.");
				break;
			}
		}
	}

	public static void invitePlayer(PlayerEventInfo player, int targetId)
	{
		/*for(Registered data : _registered)
		{
			if(data._id == targetId)
			{
				if(data._player != null && !data.isInBlacklist(player.getObjectId()))
				{
					RequestJoinParty req = new RequestJoinParty();
					req._itemDistribution = 1;
					req._name = data._player.getName();
					req.runAuto(player);
				}
				else
					player.sendMessage("Can't invite, player offline.");
			}
		}*/
	}

	public static void acceptedRequest(PlayerEventInfo player)
	{
		removePlayer(player, true);
	}

	public static void refusedRequest(PlayerEventInfo player, PlayerEventInfo requestor)
	{
		for(Registered data : _registered)
		{
			if(data._id == player.getPlayersId())
			{
				data.addToBlacklist(requestor.getPlayersId());
			}
		}
	}

	public static void onDisconnect(PlayerEventInfo player)
	{
		for(Registered data : _registered)
		{
			if(data._id == player.getPlayersId())
			{
				_registered.remove(data);
				break;
			}
		}
	}
}
