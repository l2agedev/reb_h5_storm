package l2r.gameserver.tables;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.AccessLevel;
import l2r.gameserver.model.base.AdminCommandAccessRight;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.StatsSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class AdminTable
{
	private static Logger _log = LoggerFactory.getLogger(AdminTable.class);
	
	private static final Map<Integer, AccessLevel> _accessLevels = new HashMap<>();
	private static final Map<String, AdminCommandAccessRight> _adminCommandAccessRights = new HashMap<>();
	private static final FastMap<Player, Boolean> _gmList = new FastMap<>();
	private int _highestLevel = 0;
	
	/**
	 * Instantiates a new admin table.
	 */
	protected AdminTable()
	{
		_gmList.shared();
		load();
	}
	
	public void load()
	{
		_accessLevels.clear();
		_adminCommandAccessRights.clear();
		
		NamedNodeMap attrs;
		Node attr;
		StatsSet set;
		AccessLevel level;
		AdminCommandAccessRight command;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			File file = Config.getFile("config/accessLevels.xml");
			Document doc = factory.newDocumentBuilder().parse(file);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("access".equalsIgnoreCase(d.getNodeName()))
						{
							set = new StatsSet();
							attrs = d.getAttributes();
							for (int i = 0; i < attrs.getLength(); i++)
							{
								attr = attrs.item(i);
								set.set(attr.getNodeName(), attr.getNodeValue());
							}
							level = new AccessLevel(set);
							if (level.getLevel() > _highestLevel)
							{
								_highestLevel = level.getLevel();
							}
							_accessLevels.put(level.getLevel(), level);
						}
						else if ("admin".equalsIgnoreCase(d.getNodeName()))
						{
							set = new StatsSet();
							attrs = d.getAttributes();
							for (int i = 0; i < attrs.getLength(); i++)
							{
								attr = attrs.item(i);
								set.set(attr.getNodeName(), attr.getNodeValue());
							}
							command = new AdminCommandAccessRight(set);
							_adminCommandAccessRights.put(command.getAdminCommand(), command);
						}
					}
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			_log.error("Failed loading AdminTable: ", e);
		}
		
		_log.info(getClass().getSimpleName() + ": Loaded: " + _accessLevels.size() + " Access Levels");
		_log.info(getClass().getSimpleName() + ": Loaded: " + _adminCommandAccessRights.size() + " Admin Command Access Rights");
	}
	
	/**
	 * Returns the access level by characterAccessLevel.
	 * @param accessLevelNum as int
	 * @return the access level instance by char access level
	 */
	public AccessLevel getAccessLevel(int accessLevelNum)
	{
		if (accessLevelNum < 0)
		{
			return _accessLevels.get(-1);
		}
		else if (!_accessLevels.containsKey(accessLevelNum))
		{
			_accessLevels.put(accessLevelNum, new AccessLevel());
		}
		return _accessLevels.get(accessLevelNum);
	}
	
	/**
	 * Gets the master access level.
	 * @return the master access level
	 */
	public AccessLevel getMasterAccessLevel()
	{
		return _accessLevels.get(_highestLevel);
	}
	
	/**
	 * Checks for access level.
	 * @param id the id
	 * @return {@code true}, if successful, {@code false} otherwise
	 */
	public boolean hasAccessLevel(int id)
	{
		return _accessLevels.containsKey(id);
	}
	
	/**
	 * Checks for access.
	 * @param adminCommand the admin command
	 * @param accessLevel the access level
	 * @return {@code true}, if successful, {@code false} otherwise
	 */
	public boolean hasAccess(String adminCommand, AccessLevel accessLevel)
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			return true;
		
		if (adminCommand != null && !adminCommand.isEmpty())
			adminCommand = adminCommand.split(" ")[0];
		
		AdminCommandAccessRight acar = _adminCommandAccessRights.get(adminCommand);
		if (acar == null)
		{
			// Trying to avoid the spam for next time when the gm would try to use the same command
			if ((accessLevel.getLevel() > 0) && (accessLevel.getLevel() == _highestLevel))
			{
				acar = new AdminCommandAccessRight(adminCommand, true, accessLevel.getLevel());
				_adminCommandAccessRights.put(adminCommand, acar);
				_log.info("AdminCommandAccessRights: No rights defined for admin command " + adminCommand + " auto setting accesslevel: " + accessLevel.getLevel() + " !");
			}
			else
			{
				_log.info("AdminCommandAccessRights: No rights defined for admin command " + adminCommand + " !");
				return false;
			}
		}
		return acar.hasAccess(accessLevel);
	}
	
	/**
	 * Require confirm.
	 * @param command the command
	 * @return {@code true}, if the command require confirmation, {@code false} otherwise
	 */
	public boolean requireConfirm(String command)
	{
		AdminCommandAccessRight acar = _adminCommandAccessRights.get(command);
		if (acar == null)
		{
			_log.info("AdminCommandAccessRights: No rights defined for admin command " + command + ".");
			return false;
		}
		return acar.getRequireConfirm();
	}
	
	/**
	 * Gets the all GMs.
	 * @param includeHidden the include hidden
	 * @return the all GMs
	 */
	public static List<Player> getAllGms(boolean includeHidden)
	{
		final List<Player> tmpGmList = new ArrayList<>();
		for (Entry<Player, Boolean> entry : _gmList.entrySet())
		{
			if (includeHidden || !entry.getValue())
			{
				tmpGmList.add(entry.getKey());
			}
		}
		return tmpGmList;
	}
	
	/**
	 * Gets the all GM names.
	 * @param includeHidden the include hidden
	 * @return the all GM names
	 */
	public List<String> getAllGmNames(boolean includeHidden)
	{
		final List<String> tmpGmList = new ArrayList<>();
		for (Entry<Player, Boolean> entry : _gmList.entrySet())
		{
			if (!entry.getValue())
			{
				tmpGmList.add(entry.getKey().getName());
			}
			else if (includeHidden)
			{
				tmpGmList.add(entry.getKey().getName() + " (invis)");
			}
		}
		return tmpGmList;
	}
	
	/**
	 * Add a Player player to the Set _gmList.
	 * @param player the player
	 * @param hidden the hidden
	 */
	public void addGm(Player player, boolean hidden)
	{
		if (Config.DEBUG)
		{
			_log.info("added gm: " + player.getName());
		}
		_gmList.put(player, hidden);
	}
	
	/**
	 * Delete a GM.
	 * @param player the player
	 */
	public void deleteGm(Player player)
	{
		if (Config.DEBUG)
		{
			_log.info("deleted gm: " + player.getName());
		}
		_gmList.remove(player);
	}
	
	/**
	 * GM will be displayed on clients GM list.
	 * @param player the player
	 */
	public void showGm(Player player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, false);
		}
	}
	
	/**
	 * GM will no longer be displayed on clients GM list.
	 * @param player the player
	 */
	public void hideGm(Player player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, true);
		}
	}
	
	/**
	 * Checks if is GM online.
	 * @param includeHidden the include hidden
	 * @return true, if is GM online
	 */
	public boolean isGmOnline(boolean includeHidden)
	{
		for (Entry<Player, Boolean> entry : _gmList.entrySet())
		{
			if (includeHidden || !entry.getValue())
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Send list to player.
	 * @param player the player
	 */
	public void sendListToPlayer(Player player)
	{
		if (isGmOnline(player.isGM()))
		{
			player.sendPacket(SystemMsg._GM_LIST_);
			
			for (String name : getAllGmNames(player.isGM()))
				player.sendPacket(new SystemMessage2(SystemMsg.GM_S1).addString(name));
		}
		else
			player.sendPacket(SystemMsg.THERE_ARE_NOT_ANY_GMS_THAT_ARE_PROVIDING_CUSTOMER_SERVICE_CURRENTLY);
	}
	
	/**
	 * Broadcast to GMs.
	 * @param packet the packet
	 */
	public static void broadcastToGMs(L2GameServerPacket packet)
	{
		for (Player gm : getAllGms(true))
		{
			gm.sendPacket(packet);
		}
	}
	
	/**
	 * Broadcast message to GMs.
	 * @param message the message
	 */
	public static void broadcastMessageToGMs(String message)
	{
		for (Player gm : getAllGms(true))
		{
			gm.sendMessage(message);
		}
	}
	
	public static Player[] getAllGMs()
	{
		return _gmList.keySet().toArray(new Player[_gmList.size()]);
	}
	
	/**
	 * Gets the single instance of AdminTable.
	 * @return AccessLevels: the one and only instance of this class<br>
	 */
	public static AdminTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminTable _instance = new AdminTable();
	}
}