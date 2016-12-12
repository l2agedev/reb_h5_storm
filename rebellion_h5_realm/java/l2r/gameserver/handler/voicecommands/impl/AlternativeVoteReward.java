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
package l2r.gameserver.handler.voicecommands.impl;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.TimeUtils;
import l2r.gameserver.utils.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlternativeVoteReward extends Functions implements IVoicedCommandHandler
{
    // Queries
    private static final String DELETE_QUERY = "DELETE FROM alternative_voting_reward WHERE time < ?";
    private static final String SELECT_QUERY = "SELECT * FROM alternative_voting_reward";
    private static final String INSERT_QUERY = "INSERT INTO alternative_voting_reward (data, scope, time, top) VALUES (?, ?, ?, ?)";

    private static final Logger _log = LoggerFactory.getLogger(AlternativeVoteReward.class);

    private static final long VOTING_INTERVAL = TimeUnit.HOURS.toMillis(12); //12 HOURS

    private static final Map<UserScope, ScopeContainer> VOTTERS_CACHE = new EnumMap<UserScope, ScopeContainer>(UserScope.class);

    public static long TIME_TO_VOTE = TimeUnit.SECONDS.toMillis(Config.TIME_TO_VOTE_SEC); //60 sec
    private static boolean _isVotingHopzone = false;
    private static boolean _isVotingTopzone = false;
    public static String TOPZONE_URL = Config.TOPZONE_SERVER_LINK;
    public static String HOPZONE_URL = Config.HOPZONE_SERVER_LINK;
    
	private final String[] _commandList =
	{
		"votereward",
		"votetopzone",
		"votehopzone"
	};
	
    @Override
    public String[] getVoicedCommandList()
	{
    	load();
		return _commandList;
	}
	
    @Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
    	if (!Config.ENABLE_ALT_VOTE_REWARD)
    	{
    		activeChar.sendMessageS("Vote reward is Disabled", 5);
    		return false;
    	}
    	
    	if (command.equalsIgnoreCase("votereward"))
		{
    		showVoteHtml(activeChar);
			return false;
		}
    	else if (command.equalsIgnoreCase("votetopzone"))
    	{
    		if(!Config.ENABLE_HOPZONE_VOTING)
    		{
    			activeChar.sendMessageS("Topzone Voting is currently disabled please try again later!", 5);
    			return false;
    		}
    		
    		voteTopzone(activeChar);
    		return false;
    	}
    	else if (command.equalsIgnoreCase("votehopzone"))
    	{
    		if(!Config.ENABLE_TOPZONE_VOTING)
    		{
    			activeChar.sendMessageS("Hopzone Voting is currently disabled please try again later!", 5);
    			return false;
    		}
    		
    		voteHopzone(activeChar);
    		return false;
    	}
    	
    	return true;
	}
    
    private void showVoteHtml(Player player)
    {
    	String htmContent = HtmCache.getInstance().getNotNull("mods/votereward/altvote.htm", player);
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
		npcHtmlMessage.setHtml(htmContent);
		player.sendPacket(npcHtmlMessage);
    }
    
    private void voteHopzone(Player player)
    {
        final Player player2 = player;
        
        showVoteHtml(player2);
        
        long time = getLastVotedTime(player, "hopzone");
        
        if (player2.isVoting())
        {
            player2.sendMessageS("You are voting...", 2);
            return;
        }
        if (time > 0)
        {
            sendReEnterMessage(time, player);
            return;
        }
        
        if (_isVotingHopzone)
        {
            player2.sendMessageS("Someone is already voting. Please wait!", 4);
            return;
        }
        
        final int currVotes = getHopzoneCurrentVotes();
        _isVotingHopzone = true;

        player2.sendMessageS("You have " + TIME_TO_VOTE / 1000 + " seconds to vote for us on HOPZONE!", 5);
        
        player2.setIsVoting(true);
        ThreadPoolManager.getInstance().schedule(new Runnable()
        {
            @Override
            public void run()
            {
                if (getHopzoneCurrentVotes() > currVotes)
                {
                    player2.sendMessageS("Thank you for supporting our server.", 5);
                    markAsVotted(player2, "hopzone");
                    giveReward(player2, "HOPZONE");
                }
                else
                    player2.sendMessageS("Time for vote expired. Try again later.", 2);
                
                _isVotingHopzone = false;
                player2.setIsVoting(false);
            }
        }, TIME_TO_VOTE);

    }
    
    public void voteTopzone(Player player)
    {
        final Player player2 = player;
        showVoteHtml(player2);
        
		long time = getLastVotedTime(player, "topzone");
		
		if (player2.isVoting())
		{
			player2.sendMessageS("You are voting...", 2);
			return;
		}
		if (time > 0)
		{
			sendReEnterMessage(time, player);
			return;
		}
		if (_isVotingTopzone)
		{
			player2.sendMessageS("Someone is already voting. Please wait!", 4);
			return;
		}
		
		final int currVotes = getTopzoneCurrentVotes();
		
		_isVotingTopzone = true;
		player2.sendMessageS("You have " + TIME_TO_VOTE / 1000 + " seconds to vote for us on TOPZONE!", 5);
        
        player2.setIsVoting(true);
        ThreadPoolManager.getInstance().schedule(new Runnable()
        {
            @Override
            public void run()
            {
                if (getTopzoneCurrentVotes() > currVotes)
                {
                	player2.sendMessageS("Thank you for supporting our server.", 5);
                    markAsVotted(player2, "topzone");
                    giveReward(player2, "TOPZONE");
                }
                else
                	player2.sendMessageS("Time for vote expired. Try again later.", 2);
                
                _isVotingTopzone = false;
                player2.setIsVoting(false);
            }
        }, TIME_TO_VOTE);

    }
    
    private int getHopzoneCurrentVotes()
    {
        InputStreamReader isr = null;
        BufferedReader br = null;
        try
        {
            if (!HOPZONE_URL.endsWith(".html"))
            	HOPZONE_URL += ".html";
            
            URLConnection con = new URL(HOPZONE_URL).openConnection();
            
            con.addRequestProperty("User-L2Hopzone", "Mozilla/4.76");
            isr = new InputStreamReader(con.getInputStream());
            br = new BufferedReader(isr);
            
            String line;
            while ((line = br.readLine()) != null)
            {
                if (line.contains("no steal make love") || line.contains("no votes here") || line.contains("bang, you don't have votes") || line.contains("la vita e bella"))
                {
                    int votes = Integer.valueOf(line.split(">")[2].replace("</span", ""));
                    return votes;
                }
            }
            
            br.close();
            isr.close();
        }
        catch (Exception e)
        {
            _log.warn("Error while getting server vote count on HopZone!", e);
        }

        return -1;
    }
    
    private int getTopzoneCurrentVotes()
    {
        InputStreamReader isr = null;
        BufferedReader br = null;

        try
        {
            URLConnection con = new URL(TOPZONE_URL).openConnection();
            con.addRequestProperty("User-Agent", "L2TopZone");
            isr = new InputStreamReader(con.getInputStream());
            br = new BufferedReader(isr);

            boolean got = false;

            String line;
            while ((line = br.readLine()) != null)
            {
                if (line.contains("<div class=\"rank\"><div class=\"votes2\">Votes:<br>") && !got)
                {
                    got = true;
                    int votes = Integer.valueOf(line.split("<div class=\"rank\"><div class=\"votes2\">Votes:<br>")[1].replace("</div></div>", ""));
                    return votes;
                }
            }

            br.close();
            isr.close();
        }
        catch (Exception e)
        {
            _log.warn("Error while getting server vote count on Topzone!", e);
        }

        return -1;
    }
    
    private final long getLastVotedTime(Player activeChar, String top)
    {
        for (Entry<UserScope, ScopeContainer> entry : VOTTERS_CACHE.entrySet())
        {
            final String data = entry.getKey().getData(activeChar);
            final long reuse = entry.getValue().getReuse(data, top);
            if (reuse > 0)
                return reuse;
        }
        return 0;
    }

    private void sendReEnterMessage(long time, Player player)
    {
        if (time > System.currentTimeMillis())
        {
            final long remainingTime = (time - System.currentTimeMillis()) / 1000;

            player.sendMessage("Your next reward is available in: " + Util.formatTime((int) remainingTime));
        }
    }

    private final void load()
	{
		// Initialize the cache
		for (UserScope scope : UserScope.values())
			VOTTERS_CACHE.put(scope, new ScopeContainer());
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_QUERY);
			statement.setLong(1, System.currentTimeMillis());
			statement.execute();
			DbUtils.close(statement);
			
			statement = con.prepareStatement(SELECT_QUERY);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				final String data = rset.getString("data");
				final UserScope scope = UserScope.findByName(rset.getString("scope"));
				final Long time = rset.getLong("time");
				final String top = rset.getString("top");
				if (scope != null)
					VOTTERS_CACHE.get(scope).registerVotter(data, time, top);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
    }

    private enum UserScope
    {
        ACCOUNT
        {
            @Override
            public String getData(Player player)
            {
                return player.getAccountName();
            }
        },
        IP
        {
            @Override
            public String getData(Player player)
            {
                return player.getIP();
            }
        },
        //@formatter:off
        /*HWID
        {
            @Override
            public String getData(Player player)
            {
                return player.getHWID();
            }
        }*/
        //@formatter:on
        ;

        public abstract String getData(Player player);

        public static UserScope findByName(String name)
        {
            for (UserScope scope : values())
            {
                if (scope.name().equals(name))
                    return scope;
            }
            return null;
        }
    }

    private class ScopeContainer
    {
        private final Map<String, Long> _HopzoneVotters = new ConcurrentHashMap<>();
        private final Map<String, Long> _TopzoneVotters = new ConcurrentHashMap<>();

        public ScopeContainer()
        {
        }

        public void registerVotter(String data, long reuse, String top)
        {
            if (top.equalsIgnoreCase("hopzone"))
                _HopzoneVotters.put(data, reuse);
            if (top.equalsIgnoreCase("topzone"))
                _TopzoneVotters.put(data, reuse);
        }

        public long getReuse(String data, String top)
        {
            if (top.equalsIgnoreCase("hopzone"))
            {
                if (_HopzoneVotters.containsKey(data))
                {
                    long time = _HopzoneVotters.get(data);
                    if (time > System.currentTimeMillis())
                        return time;
                }
            }
            if (top.equalsIgnoreCase("topzone"))
            {
                if (_TopzoneVotters.containsKey(data))
                {
                    long time = _TopzoneVotters.get(data);
                    if (time > System.currentTimeMillis())
                        return time;
                }
            }
            return 0;
        }
    }

    static void markAsVotted(Player player, String top)
    {
        final long reuse = System.currentTimeMillis() + VOTING_INTERVAL;
        
        Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_QUERY);
			for (UserScope scope : UserScope.values())
            {
                final String data = scope.getData(player);
                final ScopeContainer container = VOTTERS_CACHE.get(scope);
                container.registerVotter(data, reuse, top);

                statement.setString(1, data);
                statement.setString(2, scope.name());
                statement.setLong(3, reuse);
                statement.setString(4, top);
                statement.execute();
            }
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
    }
    
    private void giveReward(Player activeChar, String site)
    {
    	Collections.shuffle(Arrays.asList(Config.ALT_VOTE_REWARDS));
		for (String rewards : Config.ALT_VOTE_REWARDS)
		{
			String[] reward2 = rewards.split(",");
			
			int id = Integer.parseInt(reward2[0]);
			long count = Long.parseLong(reward2[1]);
			int chance = Integer.parseInt(reward2[2]);
			
			if (Rnd.get(100) < chance)
			{
				Functions.addItem(activeChar, id, count, true);
				Log.voteReward(site + " - [" + TimeUtils.convertDateToString(System.currentTimeMillis()) + "] Acc: " + activeChar.getAccountName() + " - Char: " + activeChar.getName() + " - IP: " + activeChar.getIP());
				break;
			}
		}
    }
}