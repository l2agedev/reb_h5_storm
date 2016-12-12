package npc.model.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.templates.npc.NpcTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author PaInKiLlEr
 */
public class HeroStatuyaInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(HeroStatuyaInstance.class);
	
	private Map<String, String> _players = new HashMap<String, String>(9);
	
	public HeroStatuyaInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		loadSql();
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (!canBypassCheck(player, this))
			return;
		
		if (command.startsWith("status"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("event/cataclysm/cataclysm.htm");
			
			Connection con = null;
			PreparedStatement stmt = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				stmt = con.prepareStatement("SELECT * FROM cataclysm WHERE town=? DESC LIMIT 9;");
				rset = stmt.executeQuery();
				
				int number = 0;
				
				while (rset.next())
				{
					number++;
					String town_name = rset.getString("town");
					String player_name = rset.getString("player_name");
					_players.put(town_name, player_name); // Заполняем имеющиеся
					
					html.replace("%town" + number + "%", town_name);
					html.replace("%player_name" + number + "%", player_name);
				}
			}
			catch (Exception e)
			{
				_log.info("Exception: " + e, e);
			}
			finally
			{
				DbUtils.closeQuietly(con, stmt, rset);
			}
			
			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	private void loadSql()
	{
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			stmt = con.prepareStatement("SELECT * FROM cataclysm WHERE town=? DESC LIMIT 9;");
			rset = stmt.executeQuery();
			while (rset.next())
			{
				String town_name = rset.getString("town");
				String player_name = rset.getString("player_name");
				_players.put(town_name, player_name); // Заполняем имеющиеся
			}
		}
		catch (Exception e)
		{
			_log.info("Exception: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, stmt, rset);
		}
	}
}