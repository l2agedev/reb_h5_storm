package l2r.gameserver.network.telnet.commands;

import l2r.gameserver.Announcements;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.telnet.TelnetCommand;
import l2r.gameserver.network.telnet.TelnetCommandHolder;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;


public class TelnetSay implements TelnetCommandHolder
{
	private Set<TelnetCommand> _commands = new LinkedHashSet<TelnetCommand>();

	public TelnetSay()
	{
		_commands.add(new TelnetCommand("announce", "ann")
		{
			@Override
			public String getUsage()
			{
				return "announce <text>";
			}

			@Override
			public String handle(String[] args)
			{
				if(args.length == 0)
					return null;
				
				Announcements.getInstance().announceToAll(StringUtils.join(args, ' '));
				
				return "Announcement sent.\n";
			}			
		});
		
		_commands.add(new TelnetCommand("message", "msg")
		{
			@Override
			public String getUsage()
			{
				return "message <player> <text>";
			}

			@Override
			public String handle(String[] args)
			{
				if(args.length < 2)
					return null;

				Player player = World.getPlayer(args[0]);
				if(player == null)
					return "Player not found.\n";

				System.arraycopy(args, 1, args, 0, args.length); // Cut the 1st arg.
				Say2 cs = new Say2(0, ChatType.TELL, "[Admin]", StringUtils.join(args, ' '));
				player.sendPacket(cs);

				return "Message sent.\n";
			}
			
		});
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}
}