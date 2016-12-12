/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.consolecon;

import l2r.commons.util.Base64;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.consolecon.receive.RequestManageAccounts;
import l2r.gameserver.network.consolecon.send.AnswereRequestAuth;
import l2r.gameserver.network.consolecon.send.AnswereRequestChangePass;
import l2r.gameserver.network.consolecon.send.AnswereRequestManageAccounts;
import l2r.gameserver.network.consolecon.send.ConsoleClose;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.inc.incolution.util.list.IncArrayList;
import org.inc.nionetcore.NioNetConfig;
import org.inc.nionetcore.NioNetConnection;
import org.inc.nionetcore.NioNetSelector;
import org.inc.nionetcore.interfaces.INioNetAcceptFilter;
import org.inc.nionetcore.interfaces.INioNetClientFactory;
import org.inc.nionetcore.util.NioNetUtil;

/**
 * @author Forsaiken
 */
public final class ConsoleController implements INioNetClientFactory<Console>, INioNetAcceptFilter
{
	public static final byte PLAYER_STATUS_NOT_FOUND = 0;
	public static final byte PLAYER_STATUS_OFFLINE = 1;
	public static final byte PLAYER_STATUS_ONLINE_MODE = 2;
	public static final byte PLAYER_STATUS_OFFLINE_MODE = 3;
	
	public static final Logger CONSOLE_LOG = Logger.getLogger("incconsole");
	static final Logger _log = Logger.getLogger(ConsoleController.class.getName());
	
	public static ConsoleController _instance;
	
	public static final void init() throws Exception
	{
		_instance = new ConsoleController();
	}
	
	public static final boolean isInitialized()
	{
		return _instance != null;
	}
	
	public static final ConsoleController getInstance()
	{
		return _instance;
	}
	
	private static MessageDigest _passwordHashCrypt;
	private final THashMap<String, Console> _onlineTable;
	private final IncArrayList<Console> _onlineList;
	private final TLongObjectHashMap<FloodInformation> _floodingIPs;
	private final THashMap<String, OfflineModePlayer> _offlineModePlayers;
	
	private final ConsolePacketHandler _handler;
	private final NioNetSelector<Console> _selector;
	
	private byte[] _serverPassword;
	
	private ConsoleController() throws Exception
	{
		/*if (!CustomConfig.CONSOLE_CLIENT_SERVER_ENABLED)
		{
			_passwordHashCrypt = null;
			_onlineTable = null;
			_onlineList = null;
			_floodingIPs = null;
			_offlineModePlayers = null;
			_handler = null;
			_selector = null;
			return;
		}*/
		
		_passwordHashCrypt = MessageDigest.getInstance("SHA");
		_onlineTable = new THashMap<>();
		_onlineList = new IncArrayList<>();
		_floodingIPs = new TLongObjectHashMap<>();
		_offlineModePlayers = new THashMap<>();
		
		final Charset charset = Charset.forName("UTF-8");
		_serverPassword = "12345".getBytes(charset);//CustomConfig.CONSOLE_CLIENT_SERVER_PASSWORD.getBytes(charset);
		
		_handler = new ConsolePacketHandler();
		
		final String[] accounts = getAccounts();
		if (accounts != null && accounts.length == 0)
		{
			if (createAccount("Admin", "incdefault") == 1)
				_log.log(Level.INFO, "ConsoleController: Auto created admin account. Ident: Admin, Pass: incdefault");
		}
		
		final NioNetConfig config = new NioNetConfig();
		_selector = new NioNetSelector<>(config);
		
		try
		{
			//if (CustomConfig.CONSOLE_CLIENT_SERVER_HOSTNAME.equals("*"))
				_selector.registerServerSocket(this, this, _handler, _handler, 6666/*CustomConfig.CONSOLE_CLIENT_SERVER_PORT*/,  10, null);
			//else
			//	_selector.registerServerSocket(this, this, _handler, _handler, CustomConfig.CONSOLE_CLIENT_SERVER_PORT,  10, InetAddress.getByName(CustomConfig.CONSOLE_CLIENT_SERVER_HOSTNAME));
			
			//_log.log(Level.INFO, "Registered ServerSocket " + CustomConfig.CONSOLE_CLIENT_SERVER_HOSTNAME + ":" + CustomConfig.CONSOLE_CLIENT_SERVER_PORT + ".");
			_log.info("Registered server socket cor console.");
		}
		catch (Exception e)
		{
			_log.warning("Couldnt register console server socket");
			//_log.log(Level.WARNING, "Couldn`t register ServerSocket " + CustomConfig.CONSOLE_CLIENT_SERVER_HOSTNAME + ":" + CustomConfig.CONSOLE_CLIENT_SERVER_PORT + ".", e);
		}
	}
	
	public final boolean isOfflineModeCharacter(String name)
	{
		/*if (!CustomConfig.CONSOLE_CLIENT_SERVER_ENABLED) TODO config
			return false;*/
		
		name = name.toLowerCase();
		
		synchronized (_offlineModePlayers)
		{
			final OfflineModePlayer offlineModePlayer = _offlineModePlayers.get(name);
			if (offlineModePlayer != null)
			{
				if (offlineModePlayer.checkExpired())
				{
					offlineModePlayer.stopTask();
					_offlineModePlayers.remove(name);
					return false;
				}
					
				return true;
			}
			
			return false;
		}
	}
	
	public final void disablePlayerOfflineMode(String name)
	{
		name = name.toLowerCase();
		
		synchronized (_offlineModePlayers)
		{
			final OfflineModePlayer offlineModePlayer = _offlineModePlayers.remove(name);
			if (offlineModePlayer != null)
			{
				offlineModePlayer.stopTask();
				//offlineModePlayer.getPlayer(false).updateDatabaseImpl(); TODO Equivalent?
			}
		}
	}
	
	public final OfflineModeStatusReply getPlayer(String name, final boolean offlineMode)
	{
		name = name.toLowerCase();
		
		synchronized (_offlineModePlayers)
		{
			OfflineModePlayer offlineModePlayer = _offlineModePlayers.get(name);
			if (offlineModePlayer != null)
			{
				return new OfflineModeStatusReply(offlineModePlayer, PLAYER_STATUS_OFFLINE_MODE);
			}
			
			Player player = World.getPlayer(name);
			if (player != null)
			{
				if (offlineMode)
				{
					offlineModePlayer = new OfflineModePlayer(player);
					_offlineModePlayers.put(name, offlineModePlayer);
					player.kick();
					
					player = Player.restore(player.getObjectId());
					if (player == null)
						return new OfflineModeStatusReply((Player) null, PLAYER_STATUS_NOT_FOUND);
					
					return new OfflineModeStatusReply(offlineModePlayer, PLAYER_STATUS_OFFLINE_MODE);
				}
				
				return new OfflineModeStatusReply(player, PLAYER_STATUS_ONLINE_MODE);
			}
			
			final int objId = CharacterDAO.getInstance().getObjectIdByName(name);
			if (objId == 0)
				return new OfflineModeStatusReply((Player) null, PLAYER_STATUS_NOT_FOUND);
			
			if (offlineMode)
			{
				player = Player.restore(objId);
				if (player == null)
					return new OfflineModeStatusReply((Player) null, PLAYER_STATUS_NOT_FOUND);
				
				offlineModePlayer = new OfflineModePlayer(player);
				_offlineModePlayers.put(name, offlineModePlayer);
				return new OfflineModeStatusReply(offlineModePlayer, PLAYER_STATUS_OFFLINE_MODE);
			}
			
			return new OfflineModeStatusReply((Player) null, PLAYER_STATUS_OFFLINE);
		}
	}
	
	public final boolean checkServerAuthOk(final byte[] pass, final Console console)
	{
		return Arrays.equals(_serverPassword, pass);
	}
	
	public final void manageAccounts(final int requestId, final int operation, final String[] idents, final String[] passes, final Console console)
	{
		switch (operation)
		{
			case RequestManageAccounts.OPERATION_SHOW_ACCOUNTS:
				console.sendPacket(new AnswereRequestManageAccounts(requestId, AnswereRequestManageAccounts.RESPONSE_SHOW_ACCOUNTS_SUCCESS, getAccounts()));
				return;
				
			case RequestManageAccounts.OPERATION_ADD_ACCOUNT:
			{
				if (idents.length != 1 || passes.length != 1)
				{
					console.sendPacket(new AnswereRequestManageAccounts(requestId, AnswereRequestManageAccounts.RESPONSE_ADD_ACCOUNT_FAILED, getAccounts()));
					return;
				}
				
				switch (ConsoleController.createAccount(idents[0], passes[0]))
				{
					case -1:
					case 0:
					{
						console.sendPacket(new AnswereRequestManageAccounts(requestId, AnswereRequestManageAccounts.RESPONSE_ADD_ACCOUNT_FAILED, getAccounts()));
						return;
					}
					
					case 1:
					{
						CONSOLE_LOG.log(Level.WARNING, console + " added new console account: '" + idents[0] + '\'');
						console.sendPacket(new AnswereRequestManageAccounts(requestId, AnswereRequestManageAccounts.RESPONSE_ADD_ACCOUNT_SUCCESS, getAccounts()));
						return;
					}
				}
				break;
			}
				
			case RequestManageAccounts.OPERATION_REMOVE_ACCOUNT:
			{
				if (idents.length == 0)
				{
					console.sendPacket(new AnswereRequestManageAccounts(requestId, AnswereRequestManageAccounts.RESPONSE_ADD_ACCOUNT_FAILED, getAccounts()));
					return;
				}
				
				boolean success = false;
				for (final String ident : idents)
				{
					if (ConsoleController.deleteAccount(ident) == 1)
						success = true;
				}
				
				if (success)
				{
					CONSOLE_LOG.log(Level.WARNING, console + " deleted console accounts: '" + Arrays.toString(idents) + '\'');
					console.sendPacket(new AnswereRequestManageAccounts(requestId, AnswereRequestManageAccounts.RESPONSE_REMOVE_ACCOUNTS_SUCCESS, getAccounts()));
					return;
				}
				
				console.sendPacket(new AnswereRequestManageAccounts(requestId, AnswereRequestManageAccounts.RESPONSE_REMOVE_ACCOUNTS_FAILED, getAccounts()));
				return;
			}
		}
	}
	
	public final void changePass(final int requestId, final String oldPass, final String newPass, final Console console)
	{
		switch (ConsoleController.changeAccountPassword(console.getIdent(), newPass, oldPass))
		{
			case -1:
			case 0:
			{
				console.sendPacket(new AnswereRequestChangePass(requestId, AnswereRequestChangePass.PASS_CHANGE_FAILED));
				return;
			}
			
			case 1:
			{
				console.sendPacket(new AnswereRequestChangePass(requestId, AnswereRequestChangePass.PASS_CHANGE_SUCCESS));
				return;
			}
		}
	}
	
	public final void checkAuthOk(final String ident, final String pass, final Console console)
	{
		switch (checkPasswordOk(ident, pass))
		{
			case -1:
			{
				console.close(AnswereRequestAuth.STATIC_PACKET_AUTH_FAILED_ERROR);
				return;
			}
			
			case 0:
			{
				console.close(AnswereRequestAuth.STATIC_PACKET_AUTH_FAILED_WRONG_PASS_IDENT);
				return;
			}
			
			case 1:
			{
				synchronized (_onlineTable)
				{
					final Console temp = _onlineTable.putIfAbsent(ident, console);
					if (temp != null)
					{
						temp.close(ConsoleClose.STATIC_PACKET_KICKED);
						console.close(AnswereRequestAuth.STATIC_PACKET_AUTH_FAILED_ALREADY_ONLINE);
						return;
					}
					
					_onlineList.add(console);
				}
				
				
				console.setIdent(ident);
				console.setAuthed(true);
				console.sendPacket(AnswereRequestAuth.STATIC_PACKET_AUTH_SUCESSFULLY);
				CONSOLE_LOG.log(Level.INFO, console + " authed.");
				return;
			}
		}
	}
	
	public final void onConnection(final Console console)
	{
		CONSOLE_LOG.log(Level.INFO, console + " connected.");
	}
	
	public final void onDisconnection(final Console console)
	{
		CONSOLE_LOG.log(Level.INFO, console + " disconnected.");
		
		if (!console.isAuthed())
			return;
		
		synchronized (_onlineTable)
		{
			_onlineList.remove(_onlineTable.remove(console.getIdent()));
		}
	}
	
	public final void shutdown()
	{
		_selector.shutdown();
	}
	
	@Override
	public final Console newInstance(final NioNetConnection<Console> con)
	{
		return new Console(con);
	}
	
	@Override
	public final boolean accept(final SocketChannel channel)
	{
		byte[] ip = null;
		
		try
		{
			ip = channel.socket().getInetAddress().getAddress();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		if (ip == null)
			return false;
		
		final long ipHash = NioNetUtil.getHashFromIP(ip);
		
		final long currentTime = System.currentTimeMillis();
		
		synchronized (_floodingIPs)
		{
			FloodInformation fi = _floodingIPs.get(ipHash);
			if (fi != null)
			{
				if (fi.block > 0)
				{
					if (fi.block > currentTime)
					{
						fi.time = currentTime;
						return false;
					}
					
					fi.time = currentTime;
					fi.trys = 0;
					fi.block = 0;
				}
				else
				{
					if (fi.time + 30000 > currentTime)
					{
						fi.time = currentTime;
						fi.trys++;
						
						if (fi.trys >= 5)
						{
							fi.block = currentTime + 60000;
							return false;
						}
					}
					else
					{
						fi.time = currentTime;
						fi.trys = 0;
						fi.block = 0;
					}
				}
			}
			else
			{
				fi = new FloodInformation();
				fi.time = currentTime;
				
				_floodingIPs.put(ipHash, fi);
			}
		}
		
		return true;
	}
	
	private final class FloodInformation
	{
		FloodInformation()
		{
			
		}
		
		long time;
		long block;
		byte trys;
	}
	
	private static final String[] getAccounts()
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT ident FROM console_accounts");
			
			try
			{
				final ResultSet rset = statement.executeQuery();
				final IncArrayList<String> accounts = new IncArrayList<>();
				while (rset.next())
				{
					accounts.add(rset.getString(1));
				}
				return accounts.toArray(new String[accounts.size()]);
			}
			finally
			{
				statement.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				
			}
		}
	}
	
	private static final int createAccount(final String account, final String password)
	{
		String cryptPassword;
		
		try
		{
			cryptPassword = Base64.encodeBytes(_passwordHashCrypt.digest(password.getBytes("UTF-8")));
		}
		catch (Exception e)
		{
			return -1;
		}
		
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("INSERT INTO console_accounts (ident,pass) VALUES (?,?)");
			
			try
			{
				statement.setString(1, account);
				statement.setString(2, cryptPassword);
				return statement.executeUpdate();
			}
			finally
			{
				statement.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				
			}
		}
	}
	
	private static final int checkPasswordOk(final String account, String password)
	{
		try
		{
			password = Base64.encodeBytes(_passwordHashCrypt.digest(password.getBytes("UTF-8")));
		}
		catch (Exception e)
		{
			return -1;
		}
		
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT pass FROM console_accounts WHERE ident=?");
			
			try
			{
				statement.setString(1, account);
				final ResultSet rset = statement.executeQuery();
				if (rset.next())
				{
					return password.equals(rset.getString(1)) ? 1 : 0;
				}
				
				return 0;
			}
			finally
			{
				statement.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				
			}
		}
	}
	
	private static final int changeAccountPassword(final String account, final String newPassword, final String oldPassword)
	{
		String newCryptPassword;
		String oldCryptPassword;
		
		try
		{
			newCryptPassword = Base64.encodeBytes(_passwordHashCrypt.digest(newPassword.getBytes("UTF-8")));
			oldCryptPassword = Base64.encodeBytes(_passwordHashCrypt.digest(oldPassword.getBytes("UTF-8")));
		}
		catch (Exception e)
		{
			return -1;
		}
		
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("UPDATE console_accounts SET pass=? WHERE ident=? AND pass=?");
			
			try
			{
				statement.setString(1, newCryptPassword);
				statement.setString(2, account);
				statement.setString(3, oldCryptPassword);
				return statement.executeUpdate();
			}
			finally
			{
				statement.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				
			}
		}
	}
	
	private static final int deleteAccount(final String account)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("DELETE FROM console_accounts WHERE ident=?");
			
			try
			{
				statement.setString(1, account);
				return statement.executeUpdate();
			}
			finally
			{
				statement.close();
			}
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				
			}
		}
	}
	
	public static final class OfflineModeStatusReply
	{
		private final OfflineModePlayer _offlineModePlayer;
		private final Player _player;
		private final byte _status;
		
		public OfflineModeStatusReply(final Player player, final byte status)
		{
			_offlineModePlayer = null;
			_player = player;
			_status = status;
		}
		
		public OfflineModeStatusReply(final OfflineModePlayer offlineModePlayer, final byte status)
		{
			_offlineModePlayer = offlineModePlayer;
			_player = null;
			_status = status;
		}
		
		public final Player getPlayer()
		{
			if (_offlineModePlayer != null)
				return _offlineModePlayer.getPlayer();
			
			return _player;
		}
		
		public final byte getStatus()
		{
			return _status;
		}
	}
	
	public static final class OfflineModePlayer implements Runnable
	{
		private final Player _player;
		private final Object _accessLock;
		
		private long _lastAccess;
		private ScheduledFuture<?> _task;
		
		public OfflineModePlayer(final Player player)
		{
			_player = player;
			_accessLock = new Object();
			_lastAccess = System.currentTimeMillis();
			_task = ThreadPoolManager.getInstance().schedule(this, 1000 * 60 * 10); // 10 minutes auto release
		}
		
		public final Player getPlayer()
		{
			return getPlayer(true);
		}
		
		public final Player getPlayer(final boolean startTask)
		{
			synchronized (_accessLock)
			{
				_lastAccess = System.currentTimeMillis();
				if (startTask)
				{
					_task.cancel(false);
					_task = ThreadPoolManager.getInstance().schedule(this, 1000 * 60 * 10); // 10 minutes auto release
				}
			}
			
			return _player;
		}
		
		public final boolean checkExpired()
		{
			return _lastAccess - System.currentTimeMillis() < 1000 * 60 * 5;
		}
		
		public final void stopTask()
		{
			synchronized (_accessLock)
			{
				_task.cancel(false);
			}
		}
		
		@Override
		public final void run()
		{
			ConsoleController.getInstance().disablePlayerOfflineMode(_player.getName());
		}
	}
	
	@SuppressWarnings("unused")
	private static String DB_TABLE = "DROP TABLE IF EXISTS `console_accounts`;" 
	+ "CREATE TABLE `console_accounts` ("
	+ "  `ident` tinytext NOT NULL," 
	+ "  `pass` tinytext NOT NULL," 
	+ " PRIMARY KEY (`ident`(32))" 
	+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1;" 
	+ "INSERT INTO `console_accounts` VALUES ('Admin', 'z7FcsxjMO7eZy7wMJYDcrk5NYiE=');" 
	+ "INSERT INTO `console_accounts` VALUES ('Nik', '3FGJZmUd32WoyE2C3iWkj4wqoLA=')";
}