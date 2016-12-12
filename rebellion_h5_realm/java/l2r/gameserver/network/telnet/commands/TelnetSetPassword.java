package l2r.gameserver.network.telnet.commands;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.util.Base64;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.ChangePassword;
import l2r.gameserver.network.telnet.TelnetCommand;
import l2r.gameserver.network.telnet.TelnetCommandHolder;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedHashSet;
import java.util.Set;


public class TelnetSetPassword implements TelnetCommandHolder
{
	private Set<TelnetCommand> _commands = new LinkedHashSet<TelnetCommand>();

	public TelnetSetPassword()
	{
		_commands.add(new TelnetCommand("setpassword")
		{
			@Override
			public String getUsage()
			{
				return "setpassword <accname> <newpass>";
			}
			
			@Override
			public String handle(String[] args)
			{
				if(args.length < 2 || args[0].isEmpty() || args[1].isEmpty())
					return null;

				AuthServerCommunication.getInstance().sendPacket(new ChangePassword(args[0], "", args[1], "null"));
				return args[0] + "'s password has been changed to " + args[1] + ".\n";
			}
		});
		
		_commands.add(new TelnetCommand("setsecuritypassword")
		{
			@Override
			public String getUsage()
			{
				return "setsecuritypassword <charname> <newpass>";
			}
			
			@Override
			public String handle(String[] args)
			{
				if(args.length < 2 || args[0].isEmpty() || args[1].isEmpty())
					return null;

				Connection con = null;
				PreparedStatement statement = null;
				
				try
				{
					int objId = CharacterDAO.getInstance().getObjectIdByName(args[0]);
					byte[] raw = args[1].getBytes("UTF-8");
					raw = MessageDigest.getInstance("SHA").digest(raw);
					String passCrypt = Base64.encodeBytes(raw);
					con = DatabaseFactory.getInstance().getConnection();
					
					statement = con.prepareStatement("UPDATE `character_security` SET `password`=? WHERE `charId`=?");
					statement.setString(1, passCrypt);
					statement.setInt(2, objId);
					statement.executeUpdate();
					
					return "Security password successfully set to " + passCrypt + "(" + args[1] + ") for player " + args[0];
				}
				catch (Exception e)
				{
					return "Could not store security password: " + e.getMessage() + " for " + args[0] + ". Error: " + e.getMessage();
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}
			}
		});
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}
}