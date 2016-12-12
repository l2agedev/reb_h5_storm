package l2r.gameserver.network.telnet.commands;

import l2r.gameserver.network.telnet.TelnetCommand;
import l2r.gameserver.network.telnet.TelnetCommandHolder;
import l2r.gameserver.utils.AdminFunctions;

import java.util.LinkedHashSet;
import java.util.Set;


public class TelnetBan implements TelnetCommandHolder
{
	private Set<TelnetCommand> _commands = new LinkedHashSet<TelnetCommand>();

	public TelnetBan()
	{
		_commands.add(new TelnetCommand("kick")
		{
			@Override
			public String getUsage()
			{
				return "kick <name>";
			}
			
			@Override
			public String handle(String[] args)
			{
				if(args.length == 0 || args[0].isEmpty())
					return null;

				if (AdminFunctions.kick(args[0], "telnet"))
					return "Player kicked.\n";
				else
					return "Player not found.\n";
			}
		});
		
		_commands.add(new TelnetCommand("ban")
		{
			@Override
			public String getUsage()
			{
				return "ban <charname> <period days> <message>";
			}
			
			@Override
			public String handle(String[] args)
			{
				if(args.length < 3)
					return null;
				
				return AdminFunctions.ban(args[0], Integer.valueOf(args[1]), args[2], "telnet");
			}
		});
		
		_commands.add(new TelnetCommand("unban")
		{
			@Override
			public String getUsage()
			{
				return "unban <charname>";
			}
			
			@Override
			public String handle(String[] args)
			{
				if(args.length == 0 || args[0].isEmpty())
					return null;
				
				return AdminFunctions.unban(args[0], "telnet");
			}
		});
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}
}